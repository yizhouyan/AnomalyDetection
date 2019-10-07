package model.pipelines.tools

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object Statistics {
    def avg(features: Dataset[_], inputFeatureNames: List[String])(implicit spark: SparkSession): DataFrame ={
        features.createOrReplaceTempView("features")
        val queryAvgStr: String = inputFeatureNames.map(x => "AVG(dense['" + x + "'])").mkString(",")
        spark.sqlContext.sql("select ARRAY("
                + queryAvgStr +
                ") AS feature_avg from features")
    }
    def std(features: Dataset[_], inputFeatureNames: List[String])(implicit spark: SparkSession): DataFrame = {
        features.createOrReplaceTempView("features")
        val queryStdStr: String = inputFeatureNames.map(x => "STDDEV(dense['" + x + "'])").mkString(",")
        spark.sqlContext.sql("select TRANSFORM(ARRAY(" +
                queryStdStr +
                "), x -> x + 0.0000001) AS feature_std from features")
    }
}
