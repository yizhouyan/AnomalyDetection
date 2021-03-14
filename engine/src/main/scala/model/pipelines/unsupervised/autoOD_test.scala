//package model.pipelines.unsupervised
//
//import model.common.{ColumnTracking, SharedParams, SubspaceParams}
//import model.pipelines.unsupervised.examples.{LOF, LOFParams}
//import org.apache.spark.sql.types.DoubleType
//import org.apache.spark.sql.{DataFrame, SparkSession}
//import utils.Utils.initializeSparkContext
//
//
//object autoOD_test {
//  def main(args: Array[String]): Unit = {
//    implicit val spark: SparkSession = initializeSparkContext("LOF_test")
//
//    val path =  "../../data/PageBlocks_norm_10.csv"
//    val tmpdf: DataFrame = spark.read.format("csv").option("header", "true").load(path)
//    val df = tmpdf.select(tmpdf.columns.map(c => tmpdf.col(c).cast(DoubleType)):_*)
//      .withColumnRenamed("outlier","label")
//
//    val tmp_lof_krange: List[Int] = List.tabulate(10)(n => 10 + 10 * n)
//    val lof_krange: List[Int] = tmp_lof_krange ++ tmp_lof_krange ++ tmp_lof_krange ++
//      tmp_lof_krange ++ tmp_lof_krange ++ tmp_lof_krange
//
//    val featurelist = List("att1","att2","att3","att4","att5","att6","att7","att8","att9","att10")
//    val sub = SubspaceParams(10,15,2,true,12)
//    val lofParams = LOFParams(lof_krange, "lof_result", false, Some(sub), Some(featurelist))
//
//    implicit val sharedParams:SharedParams = SharedParams(true, false,
//      "../../results/lofTest", new ColumnTracking)
//    val LOF_estimator = new LOF(lofParams, -1)
//    val lof_res = LOF_estimator.transform(df, -1)
//
//    lof_res.printSchema()
//    lof_res.show(20)
//
//    spark.stop()
//
//  }
//}
