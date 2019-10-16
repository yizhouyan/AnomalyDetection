package model.pipelines.tools

import breeze.linalg.pinv
import org.apache.spark.ml.linalg.DenseMatrix
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

object Statistics {
    def avg(features: Dataset[_], inputFeatureNames: List[String])(implicit spark: SparkSession): DataFrame ={
        val queryAvgStr: String = inputFeatureNames.map(x => "AVG(" + x + ")").mkString(",")
        features.selectExpr("ARRAY("
                + queryAvgStr +
                ") AS feature_avg")
    }
    def std(features: Dataset[_], inputFeatureNames: List[String])(implicit spark: SparkSession): DataFrame = {
        val queryStdStr: String = inputFeatureNames.map(x => "STDDEV(" + x + ")").mkString(",")
        features.selectExpr("TRANSFORM(ARRAY(" +
                queryStdStr +
                "), x -> x + 0.0000001) AS feature_std")
    }
    def inv_cov(features: Dataset[_], inputFeatureNames: List[String])
               (implicit spark: SparkSession): breeze.linalg.DenseMatrix[Double] = {
        val queryStr = inputFeatureNames.map(i => {
            inputFeatureNames.map(j =>
                "covar_samp(" + i +", " + j + ")").mkString("ARRAY(",",",")")
        }).mkString("ARRAY(",",",") AS covariance")
        val cov = features.selectExpr(queryStr).head.get(0)
                .asInstanceOf[mutable.WrappedArray[mutable.WrappedArray[Double]]]
                .map(x => x.toArray).toArray.flatten
        pinv(new breeze.linalg.DenseMatrix(inputFeatureNames.length, inputFeatureNames.length, cov))
    }
}
