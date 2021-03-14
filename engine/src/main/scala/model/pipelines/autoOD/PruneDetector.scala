package model.pipelines.autoOD

import model.pipelines.tools.F1_score
import utils.Utils._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, DataFrame, Row, SparkSession}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.sql.types._
import org.apache.log4j.Logger

import scala.util.control._
import model.pipelines.tools.ToolsModel._


object PruneDetector {

  var spark: SparkSession = SparkSession.builder
    .appName("PruneDetector")
    .master("local[4]")
    .config("spark.driver.memory", "4G")
    .getOrCreate()

  // global parameter
  val logger: Logger = Logger.getLogger(PruneDetector.getClass)
  var prediction_result_list: List[DataFrame] = List()
  var classifier_result_list: List[DataFrame] = List()
  val schema: StructType = StructType(Seq(StructField("id", IntegerType)))
  var prediction_high_conf_outliers: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
  var prediction_high_conf_inliers: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
  var prediction_classifier_disagree: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
  var remain_params_tracking: List[Int] = List()

  val confidence_threshold = 0.6
  val max_iter = 200
  var N_size = 6

  def MultiRound(init_data: (DataFrame, DataFrame, List[Double]), scalerData: DataFrame, labelData: DataFrame,
                 pruneDetectorRange: Map[String, List[(Int, Int)]])
                (implicit spark: SparkSession)
  : Unit = {

    val X: DataFrame = scalerData    // id: String  label,features: Double
    val y: DataFrame = labelData     // id: String  label: Double
    val init_all_predictions: DataFrame = init_data._1
    val init_all_scores: DataFrame = init_data._2
    val f1s: List[Double] = init_data._3

    logger.info("Init Detectors Begin...")
    val data_label_indexes = Init_Detector(init_all_predictions, init_all_scores, X, pruneDetectorRange)
    val data_index = data_label_indexes._1
    val labels = data_label_indexes._2
    println("Init Detectors: data_index")
    data_index.printSchema()
    data_index.count()
    data_index.show(5)
    println("Init Detectors: labels")
    labels.printSchema()
    labels.show(5)
    logger.info("Init Detectors Done")

    logger.info("Prune Detectors Begin...")
    val last_TrainingData = Update_Detector(init_all_predictions, init_all_scores, X,
                                            data_index, labels, pruneDetectorRange)
    println("Prune Detectors: last_TrainingData")
    last_TrainingData.printSchema()
    last_TrainingData.count()
    last_TrainingData.show(5)
    logger.info("Get Last Training Data.")

//
//    import spark.implicits._
//
//    val transformed_X = robustScaler(X)
//    val training_dataSVM = transformed_X.join(last_TrainingData, Seq("id"), "left_semi")
//      .join(last_TrainingData, Seq("id"), "left").sort("id")
//    val labels_y = y.join(last_TrainingData, Seq("id"), "left_semi")
//      .join(last_TrainingData, Seq("id"), "left").sort("id")
//    val predictionSVM = SVM(training_dataSVM, transformed_X)
//
//    val code :(Double => Int) = (arg: Double) => {if (arg < 0.5) 0 else 1}
//    val addCol = udf(code)
//    val SVMf1 = List.tabulate(predictionSVM.count().toInt)(n=>n).toDF("id")
//    val predictionsTSVN = SVMf1.withColumn("predictions", addCol(predictionSVM("probability")))
//    println(F1_score.computeF1score(y, predictionsTSVN.select("predictions")))

  }

  def Init_Detector(init_all_predictions: DataFrame, init_all_scores: DataFrame, X: DataFrame,
                    pruneDetectorRange: Map[String, List[(Int, Int)]])
                   (implicit spark: SparkSession)
  : (DataFrame, DataFrame) = {
    var index_range: List[(Int, Int)] = pruneDetectorRange("init_index_range")
    var coef_index_range: List[(Int, Int)] = pruneDetectorRange("init_coef_index_range")
    var arr: Array[String] = get_TrainingIndexes(index_range, coef_index_range).toArray
    arr = Array("id") ++ arr
    logger.info("Init_Detector: get_TrainingData_Indexes Done.")
    var scores_for_training: DataFrame = init_all_scores.select(arr.head, arr.tail:_*)     // scores_for_training
    var predictions_for_training: DataFrame = init_all_predictions                              // L

    var counter = 0
    var last_training_data_indexes: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
    var last_training_labels: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)

    val loop = new Breaks
    var countloop = 1
    logger.info("Init_Detector: Loop Begin...")
    loop.breakable {
      logger.info("Init_Detector: prune_Detector Begin...")
      val label_and_data_indexes = prune_Detector(predictions_for_training, index_range)
      val data_indexes = label_and_data_indexes._1     // id
      val labels = label_and_data_indexes._2           // id + label
      logger.info("Init_Detector: prune_Detector Done")

      logger.info("Init_Detector: update_Data Begin...")
      val update_data = update_Data(data_indexes, labels, X,
                                    scores_for_training, predictions_for_training,
                                    index_range, coef_index_range, 0)
      coef_index_range = update_data._1
      index_range = update_data._2
      scores_for_training = update_data._3
      predictions_for_training = update_data._4
      logger.info("Init_Detector: update_Data Done")

      if(last_training_data_indexes.except(data_indexes).columns.length ==0 &&
        data_indexes.except(last_training_data_indexes).columns.length == 0 &&
        coef_index_range.max._2 < 2)
        counter += 1
      else counter = 0
      if (counter > 3 || countloop>50) loop.break()
      else countloop += 1
      last_training_data_indexes = data_indexes
      last_training_labels = labels
    }
    logger.info("Init_Detector: Loop Done")
    (last_training_data_indexes, last_training_labels)
  }

  def Update_Detector(init_all_predictions: DataFrame, init_all_scores: DataFrame, X: DataFrame,
                      last_training_data_indexes_input: DataFrame, last_training_labels_input: DataFrame,
                      pruneDetectorRange: Map[String, List[(Int, Int)]])
                     (implicit spark: SparkSession)
  : DataFrame = {
    var index_range: List[(Int, Int)] = pruneDetectorRange("update_index_range")
    var coef_index_range: List[(Int, Int)] = pruneDetectorRange("update_coef_index_range")
    val arr: Array[String] = get_TrainingIndexes(index_range, coef_index_range).toArray
    var scores_for_training: DataFrame = init_all_scores.select(arr.head, arr.tail: _*) // scores_for_training
    var predictions_for_training: DataFrame = init_all_predictions                      // L

    val data_indexes = last_training_data_indexes_input
    val labels = last_training_labels_input
    val update_data = update_Data(data_indexes, labels, X, scores_for_training, predictions_for_training,
                                  index_range, coef_index_range, 1)
    coef_index_range = update_data._1
    index_range = update_data._2
    scores_for_training = update_data._3
    predictions_for_training = update_data._4

    var last_training_data_indexes: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
    var counter = 0
    val loop = new Breaks
    var countloop = 1
    loop.breakable {
      val label_and_data_indexes = prune_Detector(predictions_for_training, index_range)
      val data_indexes = label_and_data_indexes._1
      val labels = label_and_data_indexes._2

      val update_data = update_Data(data_indexes, labels, X,
                                    scores_for_training, predictions_for_training,
                                    index_range, coef_index_range, 1)
      coef_index_range = update_data._1
      index_range = update_data._2
      scores_for_training = update_data._3
      predictions_for_training = update_data._4

      if(last_training_data_indexes.except(data_indexes).columns.length ==0 &&
        data_indexes.except(last_training_data_indexes).columns.length == 0 &&
        coef_index_range.max._2 < 2)
        counter += 1
      else counter = 0
      if (counter > 3 || countloop>50) loop.break()
      else countloop += 1
      last_training_data_indexes = data_indexes
    }
    last_training_data_indexes
  }

  private def get_TrainingIndexes(index_range:List[(Int, Int)],
                                  coef_index_range: List[(Int, Int)])
  : List[String] = {
    var scores_for_training_indexes: List[String] = List()
    for(k <- index_range.indices){
      val start = index_range(k)._1
      val temp_range = coef_index_range(k)._2 - coef_index_range(k)._1
      for( j <- List.tabulate(temp_range)(n => start + n))
        scores_for_training_indexes ++= List(j.toString)
    }
    scores_for_training_indexes
  }

/**
 * Prune Detector
 * inputParam:  input => predictions_for_training, index_range
 *                Train LR/SVM Model -> scores_for_training[data_indexes] & X[data_indexes]
 *                Predict on LR/SVM Model -> scores_for_training & X
 * outputParam  => data_indexes, labels
 */
  private def prune_Detector(predictions_for_training: DataFrame,
                             index_range: List[(Int, Int)])
                            (implicit spark: SparkSession)
  : (DataFrame, DataFrame) = {
    val num_methods = predictions_for_training.columns.length-1
    val sumCols = predictions_for_training.withColumn("sum", predictions_for_training.drop("id").columns.map(col).reduce(_ + _))
    val all_predictoin_withID = sumCols
    val agree_outlier_indexes = all_predictoin_withID.where(all_predictoin_withID("sum") === num_methods).select("id")
    val agree_inlier_indexes = all_predictoin_withID.where(all_predictoin_withID("sum") === 0).select("id")
    val disagree_indexes = all_predictoin_withID
      .filter(not(all_predictoin_withID("sum") === num_methods or all_predictoin_withID("sum") === 0))
      .select("id")

    var all_inlier_indexes = agree_inlier_indexes.except(prediction_high_conf_outliers)
    if(prediction_high_conf_inliers.count() != 0){
      all_inlier_indexes = all_inlier_indexes.intersect(prediction_high_conf_inliers)
    }
    var all_outlier_indexes = agree_outlier_indexes.union(prediction_high_conf_outliers)
    all_inlier_indexes = all_inlier_indexes.except(prediction_classifier_disagree)

    var self_agree_index_list: DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
    if(all_outlier_indexes.count()==0 || (all_inlier_indexes.count()/all_outlier_indexes.count())>1000){
      for(k <- index_range.indices) {
        val k1 = index_range(k)._1
        val k2 = index_range(k)._2
        if (k2 - k1 <= 6) {}
        else {
          val tmp_pred = all_predictoin_withID.join(disagree_indexes, Seq("id"), "left_anti")
          val sum_tmpPred = tmp_pred.columns.collect { case x if (x.toInt >= k1 && x.toInt < k2) => col(x) }.reduce(_ + _)
          val sum_Pred = tmp_pred.withColumn("sumSpecific", sum_tmpPred)
          val tmp_index = sum_Pred.where(sum_Pred("sumSpecific") === (k2 - k1)).select("id")
          self_agree_index_list = self_agree_index_list.union(tmp_index)
        }
      }
    }

    all_outlier_indexes = all_outlier_indexes.union(self_agree_index_list)
    all_outlier_indexes = all_outlier_indexes.except(prediction_classifier_disagree)

    val data_indexes = all_inlier_indexes.union(all_outlier_indexes)  // 只有id一列
    val inIdLabel = all_inlier_indexes.select(col("id"),lit("0").cast(DoubleType).as("label"))
    val outIdLabel = all_outlier_indexes.select(col("id"),lit("1").cast(DoubleType).as("label"))
    val labels = inIdLabel.union(outIdLabel)   // id + label
    (data_indexes, labels)
  }

/**
Update Training Data
@para: input => scores_for_training, X, data_indexes, labels
        Train LR/SVM Model -> scores_for_training[data_indexes] & X[data_indexes]
        Predict on LR/SVM Model -> scores_for_training & X
        ** LogisticRegression --- scores
        ** SVM --- X
     output => predictions_for_training, scores_for_training
**/
  private def update_Data(data_indexes: DataFrame, labels: DataFrame, X: DataFrame,
                          scores_for_training: DataFrame, predictions_for_training: DataFrame,
                          index_range:List[(Int, Int)], coef_index_range: List[(Int, Int)], flag: Int)
                         (implicit spark: SparkSession)
  : (List[(Int, Int)], List[(Int, Int)], DataFrame, DataFrame) = {
    var up_coef_index_range = coef_index_range
    var up_index_range = index_range
    var up_scores_for_training = scores_for_training
    var up_predictions_for_training = predictions_for_training

    logger.info("update_Data: robustScaler Data scores_for_trainging")
//    val transformed_scores = standardScaler(scores_for_training)
    val transformed_scores = assemblerScaler(scores_for_training)
    val training_dataLR = transformed_scores.join(labels, Seq("id"), "left_semi")
      .join(labels, Seq("id"), "left").sort("id")
    logger.info("update_Data: LR Process...")
    val predictionLR = LR(training_dataLR, transformed_scores)

    logger.info("update_Data: SVM Process...")
    val predictionSVM = SVM(X, X)


    // update prediction_result_list
    prediction_result_list ++= List(predictionLR.select("id", "probability", "prediction"))
    // update classifier_result_list
    classifier_result_list ++= List(predictionSVM.select("id", "probability", "prediction"))

    logger.info("update_Data: Update LR & SVM Done")

    val tmp_predLast = prediction_result_list.last
    val tmp_calssLast = classifier_result_list.last

    val out_condition: Column = col("prediction") === 1 && col("probability") > confidence_threshold
    val in_condition: Column = col("prediction") === 0 && col("probability") > confidence_threshold
    prediction_high_conf_outliers = tmp_predLast.where(out_condition).select("id")
      .intersect(tmp_calssLast.where(out_condition).select("id"))
    prediction_high_conf_inliers = tmp_predLast.where(in_condition).select("id")
      .intersect(tmp_calssLast.where(in_condition).select("id"))
    val temp_prediction = tmp_predLast.select("prediction","id")
    val temp_classifier = tmp_calssLast.select("prediction","id")
    prediction_classifier_disagree = temp_prediction.except(temp_classifier).select("id")

    if(coef_index_range.max._2 >= 2) {
      var new_training_dataLR = training_dataLR
      if(prediction_high_conf_outliers.count() > 0 && prediction_high_conf_inliers.count() > 0){
        val new_data_indexes = prediction_high_conf_outliers.union(prediction_high_conf_inliers)
        val new_inIdLabel = prediction_high_conf_inliers.select(col("id"), lit("0").cast(DoubleType).as("label"))
        val new_outIdLabel = prediction_high_conf_outliers.select(col("id"), lit("1").cast(DoubleType).as("label"))
        val new_labels = new_outIdLabel.union(new_inIdLabel)
        // LR prun2
        new_training_dataLR = transformed_scores.join(new_labels, Seq("id"), "left_semi").join(new_labels, Seq("id"), "left")
      }

      // LR purn
      val lr = new LogisticRegression().setMaxIter(max_iter)
      val lrModel = lr.fit(new_training_dataLR)
      val lr_prune_coef = lrModel.coefficientMatrix.toArray
      val combined_coef = lr_prune_coef

      var cond: Boolean = coef_index_range.max._2 >= 2
      if(flag == 1)
        cond = (coef_index_range.max._2 >= 2) || (combined_coef.max/combined_coef.min >= 1.1 && coef_index_range.max._2 >=2 )
      if(cond) {
        if (combined_coef.length > 1) {
          val cur_clf_coef = combined_coef
          val combined_coef_mean = combined_coef.map(a=>a).sum / combined_coef.length
          val combined_coef_std = combined_coef.map(a=>(a-combined_coef_mean)*(a-combined_coef_mean)).sum/combined_coef.length
          val cutoff = List(List(0, combined_coef_mean-combined_coef_std).max, combined_coef.toList.min).max

          val remain_indexes_after_cond = cur_clf_coef.map(a=>{if(a<=cutoff) 0 else 1})
          var tmp_remain_indexes_after_cond: List[Int] = List()
          for(k <- remain_indexes_after_cond.indices)
            if(remain_indexes_after_cond(k)==1)
              tmp_remain_indexes_after_cond ++= List(k)
          remain_params_tracking = tmp_remain_indexes_after_cond

          var remain_indexes_after_cond_expanded: List[Int] = List()
          for(k <- coef_index_range.indices){
            val s1=coef_index_range(k)._1; val s2=index_range(k)._1
            val e1=coef_index_range(k)._2; val e2=index_range(k)._2
            val s_e_range = e1-s1
            var saved_indexes: List[Int] = List()
            for(i <- s1 until e1)
              if(cur_clf_coef(i)>cutoff)
                saved_indexes ++= List(i)
            for(j <- 0 until N_size)
              remain_indexes_after_cond_expanded ++= saved_indexes.toArray.map(a=>a+j*s_e_range+s2).toList
          }
          var new_coef_index_range_seq: List[Int] = List()
          for(i <- coef_index_range.indices) {
            val s = coef_index_range(i)._1; val e = coef_index_range(i)._2
            var c = 0
            for (j <- s until e)
              c += remain_indexes_after_cond(j)
            new_coef_index_range_seq ++= List(c)
          }
          up_coef_index_range = List()
          up_index_range = List()
          var cur_sum = 0
          for(i <- new_coef_index_range_seq.indices) {
            up_coef_index_range ++= List((cur_sum, cur_sum + new_coef_index_range_seq(i)))
            up_index_range ++= List((cur_sum * 6, 6 * (cur_sum + new_coef_index_range_seq(i))))
            cur_sum += new_coef_index_range_seq(i)
          }

          val arr = List("id") ++ tmp_remain_indexes_after_cond.map(_.toString)
          up_scores_for_training = scores_for_training.select(arr.head,arr.tail:_*)
          val newforhead = List("id") ++ List.tabulate(up_scores_for_training.columns.length-1)(n => n).map(_.toString)
          up_scores_for_training = up_scores_for_training.toDF(newforhead:_*)

          val arr2 = List("id") ++ remain_indexes_after_cond_expanded.toArray.map(_.toString)
          up_predictions_for_training = predictions_for_training.select(arr2.head,arr2.tail:_*)
          val newforhead2 = List("id") ++ List.tabulate(up_predictions_for_training.columns.length-1)(n => n).map(_.toString)
          up_predictions_for_training = up_predictions_for_training.toDF(newforhead2:_*)
        }
      }
    }
    (up_coef_index_range, up_index_range, up_scores_for_training, up_predictions_for_training)
  }

//  private def addID(inputDF: DataFrame): DataFrame = {
//    val IDlist = List.tabulate(inputDF.count().toInt)(n => n)
//    val rdd = spark.sparkContext.parallelize(IDlist)
//    val rdd_new = inputDF.rdd.zip(rdd).map(r => Row.fromSeq(r._1.toSeq ++ Seq(r._2)))
//    val outDF = spark.createDataFrame(rdd_new, inputDF.schema.add("id", IntegerType))
//    outDF
//  }

}