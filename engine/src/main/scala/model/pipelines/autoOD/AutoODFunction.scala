package model.pipelines.autoOD

import model.common.SharedParams
import model.pipelines.tools.ToolsModel.run_StandardScaler
import model.pipelines.unsupervised.examples.{StandardScaler, StandardScalerParams}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

object AutoODFunction {

  def autoOD(WorkflowInput_df: (DataFrame, (Map[String, List[Int]], List[Double], Map[String, List[(Int, Int)]])) )
            (implicit spark: SparkSession, sharedParams:SharedParams): Unit = {

    val originData = WorkflowInput_df._1
    val labelData = originData.select("id","label")
    val initDetectorRange = WorkflowInput_df._2._1
    val initIfRange = WorkflowInput_df._2._2
    val pruneDetectorRange = WorkflowInput_df._2._3

    // execute StandardScaler : id + FeatureCols
    val standardData = run_StandardScaler(originData)

    // Init Training Data
    val Init_TrainingData = InitTrainingData.init_TrainingData(standardData, labelData, initDetectorRange, initIfRange)
    val all_predictions = Init_TrainingData._1
    val all_scores = Init_TrainingData._2
    val f1_score = Init_TrainingData._3

    try {
      all_predictions.write.mode("overwrite").option("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")
        .option("header", "true").csv("results_autoOD/pimaTest/predictions")
      all_scores.write.mode("overwrite").option("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")
        .option("header", "true").csv("results_autoOD/pimaTest/scores")
      import spark.implicits._
      f1_score.toDF("f1scores").write.mode("overwrite").option("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")
        .option("header", "true").csv("results_autoOD/pimaTest/f1scores")
    }catch {
      case ex: Exception => println(ex)
    }

//    val p_path = "results_autoOD/pimaTest/predictions/part-00000-28a91bfe-e292-4603-9bfd-4f7cfc7b6c75-c000.csv"
//    var predic = spark.read.format("csv").option("header", "true").load(p_path)
//    val columnsCastp = (col("id") +: predic.drop("id").columns.map(name => col(name).cast("double")))
//    predic = predic.select(columnsCastp :_*)
//
//
//    val s_path = "results_autoOD/pimaTest/scores/part-00000-62326a32-595a-45ce-a34f-a955cfa2b91f-c000.csv"
//    var scores = spark.read.format("csv").option("header", "true").load(s_path)
//    val columnsCasts = (col("id") +: scores.drop("id").columns.map(name => col(name).cast("double")))
//    scores = scores.select(columnsCasts :_*)
//
//    val f: List[Double] = List()
//
//    val Init_TrainingData = (predic, scores, f)


    // Prune Detector
    PruneDetector.spark = spark
    // TODO: Test -1
    PruneDetector.N_size = WorkflowInput_df._2._1("Mahalanobis").length
    PruneDetector.MultiRound(Init_TrainingData, originData, labelData, pruneDetectorRange)

  }

}
