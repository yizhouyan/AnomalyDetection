package model.pipelines.unsupervised.examples

import client.SyncableDataFramePaths
import model.common._
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import model.pipelines.tools.Statistics
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
 * Created by yizhouyan on 9/7/19.
 */

/**
 * Parameters for StandardScaler
 * @param inputFeatureNames  The list of features that we want to perform standarization.
 * @param outputFeatureNames  The names of the standarized features.
 */
case class StandardScalerParams(inputFeatureNames: Option[List[String]] ,
                                outputFeatureNames: Option[List[String]])

/**
 * This function computes the z-score for each feature.
 * @param standardScalerParams
 */
class StandardScaler(standardScalerParams: StandardScalerParams, stageNum: Int = -1) extends AbstractUnsupervisedAlgo{
    import StandardScaler._
    override def transform(features: DataFrame,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): DataFrame = {
        val inputFeatureNames: List[String] = standardScalerParams.inputFeatureNames match{
            case Some(x) => x
            case None => sharedParams.columeTracking.getFeatureCols()
        }
        val outputFeatureNames: List[String] = standardScalerParams.outputFeatureNames match{
            case Some(x) => x
            case None => inputFeatureNames.map(_ + "_scaled")
        }

        logger.info("Input Feature Names: " + inputFeatureNames)
        logger.info("Output Feature Names: " + outputFeatureNames)
        if (inputFeatureNames.length != outputFeatureNames.length)
            throw new IllegalArgumentException("Output Feature Name List has different length than Input Feature Name List")

        features.createOrReplaceTempView("features")

        // compute mean and std
        Statistics.avg(features, inputFeatureNames).createOrReplaceTempView("avg")
        Statistics.std(features, inputFeatureNames).createOrReplaceTempView("std")

        import spark.implicits._
        var newDF:DataFrame = spark.sqlContext.sql("select id, "
                + inputFeatureNames.zipWithIndex.map{case(e, i) =>
            "(" + e + "-feature_avg["+ (i) + "])/feature_std[" + (i) + "] AS " + outputFeatureNames.apply(i)}.mkString(",") +
                " from features a CROSS JOIN avg b CROSS JOIN std c")
        sharedParams.columeTracking.resetFeatureCols()
        sharedParams.columeTracking.addToFeatures(outputFeatureNames)
        // if saveToDB is set to true, save the results to Storage
        newDF = newDF.coalesce(sharedParams.numPartitions)
        if(sharedParams.saveToDB){
            SyncableDataFramePaths.setPath(newDF, sharedParams.outputFilePath)
            saveUnsupervisedToDB(this,
                features,
                newDF,
                inputFeatureNames,
                outputFeatureNames,
                stageNum
            )
        }
        newDF
    }

    override def getName(): String = "Standard Scaler"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        mutable.Map.empty[Any, Any]
    }
}

object StandardScaler{
    val logger = Logger.getLogger(StandardScaler.getClass)
}
