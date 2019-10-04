package model.pipelines.unsupervised

import client.SyncableDataFramePaths
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import model.common._
import org.apache.spark.sql.functions._

import scala.collection.immutable.HashMap
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
    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {
        val inputFeatureNames: List[String] = standardScalerParams.inputFeatureNames match{
            case Some(x) => x
            case None => features.head(1).apply(0).getAs[HashMap[String, Double]]("dense").keySet.toList
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
        val queryAvgStr: String = inputFeatureNames.map(x => "AVG(dense['" + x + "'])").mkString(",")
        val queryStdStr: String = inputFeatureNames.map(x => "STDDEV(dense['" + x + "'])").mkString(",")
        val avgstdDF = spark.sqlContext.sql("select ARRAY("
                + queryAvgStr +
                ") AS feature_mean, TRANSFORM(ARRAY(" +
                queryStdStr +
                "), x -> x + 0.0000001) AS feature_std from features")
        avgstdDF.createOrReplaceTempView("avgstd")

        import spark.implicits._
        val newDF = spark.sqlContext.sql("select id, map_from_arrays(ARRAY("
                + outputFeatureNames.map(x => "'" + x + "'").mkString(",")
                +  "), ARRAY("
                + inputFeatureNames.zipWithIndex.map{case(e, i) =>
            "(dense['" + e + "']-feature_mean["+ (i) + "])/feature_std[" + (i) + "]"}.mkString(",")
                + ")) AS dense " +
                "from features a CROSS JOIN avgstd b")
                .withColumn("explanations", typedLit(Map.empty[String, String]))

        // if saveToDB is set to true, save the results to Storage
        if(saveToDB == true){
            SyncableDataFramePaths.setPath(newDF, finalOutputPath)
            newDF.write.mode(SaveMode.Overwrite).parquet(finalOutputPath)
            saveUnsupervisedToDB(this,
                features,
                newDF,
                inputFeatureNames,
                outputFeatureNames,
                "",
                stageNum
            )
        }
    }

    override def getName(): String = "Standard Scaler"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        mutable.Map.empty[Any, Any]
    }
}
