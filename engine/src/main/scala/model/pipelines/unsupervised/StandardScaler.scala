package model.pipelines.unsupervised

import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._
import org.apache.spark.sql.functions._

/**
  * Created by yizhouyan on 9/7/19.
  */

/**
  * Parameters for StandardScaler
  * @param inputFeatureNames  The list of features that we want to perform standarization.
  * @param outputFeatureNames  The names of the standarized features.
  */
case class StandardScalerParams(inputFeatureNames: Option[List[String]] = None,
                                outputFeatureNames: Option[List[String]] = None)

/**
  * This function computes the z-score for each feature.
  * @param standardScalerParams
  */
class StandardScaler(standardScalerParams: StandardScalerParams) extends AbstractTransformer{
    override def transform(features: Dataset[Feature],
                           runExplanations: Boolean,
                           spark: SparkSession,
                           model_params: Option[Any] = None): Unit = {
        val inputFeatureNames: List[String] = standardScalerParams.inputFeatureNames match{
            case Some(x) => x
            case None => features.head(1).apply(0).dense.keys.toList
        }
        val outputFeatureNames: List[String] = standardScalerParams.outputFeatureNames match{
            case Some(x) => x
            case None => inputFeatureNames.map(_ + "_scaled")
        }
        println("Input Feature Names: " + inputFeatureNames)
        println("Output Feature Names: " + outputFeatureNames)
        if (inputFeatureNames.length != outputFeatureNames.length)
            throw new IllegalArgumentException("Output Feature Name List has different length than Input Feature Name List")

        features.createOrReplaceTempView("features")
        // compute mean and std


        spark.sqlContext.sql("select AVG(dense['duration']) from features").show(5)

    }
}
