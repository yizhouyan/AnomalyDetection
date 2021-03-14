package model.pipelines.autoOD

import model.common.SharedParams
import org.apache.spark.sql.{DataFrame, SparkSession}
import model.pipelines.tools.{F1_score, ToolsModel}
import org.apache.log4j.Logger


object InitTrainingData {

  val logger: Logger = Logger.getLogger(InitTrainingData.getClass)

  private def get_predictions(scores: DataFrame, num_outliers: Int, y: DataFrame)
  : (DataFrame, Double)= {
    import org.apache.spark.sql.functions._
    val sort_scores = scores.select("scores").sort(desc("scores")).rdd.collect
    val threshold = sort_scores(num_outliers)(0).toString.toDouble
    val code :(Double => Int) = (arg: Double) => {if (arg < threshold) 0 else 1}
    val addCol = udf(code)
    val predictions = scores.withColumn("predictions", addCol(scores("scores")))
      .drop("scores")
    logger.info("predictions computed. ")
    val f1score = F1_score.computeF1score(y, predictions)
    logger.info("f1score computed. ")
    (predictions, f1score)
  }

  private def update_Results(score: DataFrame, res: (DataFrame, Double),
                             predictions: DataFrame, scores: DataFrame, f1: List[Double])
                            (implicit spark: SparkSession, sharedParams:SharedParams)
  : (DataFrame, DataFrame, List[Double]) ={

    var all_predictions: DataFrame = spark.emptyDataFrame
    var all_scores: DataFrame = spark.emptyDataFrame
    var f1s: List[Double] = List()

    // update Predictions
    if(predictions.columns.length==0) {
      all_predictions = res._1.withColumnRenamed("predictions","0")
    } else {
      val bWithIndex = res._1.withColumnRenamed("predictions", (predictions.columns.length-1).toString)
      all_predictions = predictions
        .join(bWithIndex, Seq("id"),"left")
        .sort("id")
    }
    logger.info("predictions updated. ")

    // update Scores
    if(scores.columns.length == 0)
      all_scores = score.withColumnRenamed("scores","0")
    else{
      val bWithIndex  = score.withColumnRenamed("scores",(scores.columns.length-1).toString)
      all_scores = scores
        .join(bWithIndex, Seq("id") ,"left")
        .sort("id")
    }
    logger.info("scores updated. ")

    f1s = f1 ++ List(res._2)
    logger.info("f1score updated. ")

    (all_predictions, all_scores, f1s)

  }


  def init_TrainingData(scaledData: DataFrame, labelData: DataFrame,
                        initRange: Map[String, List[Int]], ifRange: List[Double])
                       (implicit spark: SparkSession, sharedParams:SharedParams)
  : (DataFrame, DataFrame, List[Double]) = {

    var all_predictions: DataFrame = spark.emptyDataFrame
    var all_scores: DataFrame = spark.emptyDataFrame
    var f1s: List[Double] = List()

    val lof_krange: List[Int] = initRange("LOF")
    val knn_krange: List[Int] = initRange("KNN")
    val if_krange: List[Double] = ifRange
    val mahalanobis_N_range: List[Int] = initRange("Mahalanobis")
    val N_range: List[Int] = initRange("N_range")

    // 1.LOF
    var temp_lof_results: Map[Int, DataFrame] = Map()
    for(i <- lof_krange.distinct.indices) {
      val lof_scores = ToolsModel.run_LOF(scaledData, lof_krange(i))
      temp_lof_results += (lof_krange(i) -> lof_scores)
    }
    for(k <- lof_krange.indices){
      val res = get_predictions(temp_lof_results(lof_krange(k)), N_range(k), labelData)
      val updateRes = update_Results(temp_lof_results(lof_krange(k)), res, all_predictions, all_scores, f1s)
      all_predictions = updateRes._1
      all_scores = updateRes._2
      f1s = updateRes._3
    }

    // 2.KNN
    var temp_knn_results: Map[Int, DataFrame] = Map()
    for(i <- knn_krange.distinct.indices) {
      val knn_scores = ToolsModel.run_KNN(scaledData, knn_krange(i))
      temp_knn_results += (knn_krange(i) -> knn_scores)
    }
    for(k <- knn_krange.indices){
      val res = get_predictions(temp_knn_results(knn_krange(k)),N_range(k),labelData)
      val updateRes = update_Results(temp_knn_results(knn_krange(k)), res, all_predictions, all_scores, f1s)
      all_predictions = updateRes._1
      all_scores = updateRes._2
      f1s = updateRes._3
    }

    // 3.IF
    var temp_if_results:Map[Double, DataFrame] = Map()
    for(i <- if_krange.distinct.indices) {
      val if_scores = ToolsModel.run_Isolation_Forest(scaledData, if_krange(i))
      temp_if_results += (if_krange(i) -> if_scores)
    }
    for(k <- if_krange.indices) {
      val res = get_predictions(temp_if_results(if_krange(k)),N_range(k),labelData)
      val updateRes = update_Results(temp_if_results(if_krange(k)), res, all_predictions, all_scores, f1s)
      all_predictions = updateRes._1
      all_scores = updateRes._2
      f1s = updateRes._3
    }

    // 4.Mahalanobis
    val mahalanobis_scores: DataFrame = ToolsModel.run_Mahalanobis(scaledData)
    for(k <- mahalanobis_N_range.indices){
      val res = get_predictions(mahalanobis_scores,N_range(k),labelData)
      val updateRes = update_Results(mahalanobis_scores, res, all_predictions, all_scores, f1s)
      all_predictions = updateRes._1
      all_scores = updateRes._2
      f1s = updateRes._3
    }
    logger.info("Init Training Data Done.")

    (all_predictions, all_scores, f1s)

  }

}