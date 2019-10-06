package model.pipelines.unsupervised.examples.iforest

import client.SyncableDataFramePaths
import model.common.{Feature, SharedParams}
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import model.pipelines.unsupervised.tools.Converters
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}

import scala.collection.mutable

/**
 * Created by yizhouyan on 9/7/19.
 */

/**
 * Isolation Forest Parameters.
 *
 * @param outputFeatureName : the name of the output column
 * @param numTrees          : the number of trees in the iforest model (>0).
 * @param maxSamples    : The number of samples to draw from data to train each tree (>0).
 *                      If <= 1, the algorithm will draw maxSamples * totalSample samples.
 *                      If > 1, the algorithm will draw maxSamples samples.
 *                      This parameter will affect the driver's memory when splitting data.
 *                      The total memory is about maxSamples * numTrees * 4 + maxSamples * 8 bytes.
 * @param maxFeatures   : The number of features to draw from data to train each tree (>0).
 *                      If <= 1, the algorithm will draw maxFeatures * totalFeatures features.
 *                      If > 1, the algorithm will draw maxFeatures features.
 * @param maxDepth      : The height limit used in constructing a tree (>0).
 *                      The default value will be about log2(numSamples).
 * @param contamination : The proportion of outliers in the data set (0< contamination < 1).
 *                      It will be used in the prediction. In order to enhance performance,
 *                      Our method to get anomaly score threshold adopts DataFrameStsFunctions.approxQuantile,
 *                      which is designed for performance with some extent accuracy loss.
 *                      Set the param approxQuantileRelativeError (0 < e < 1) to calculate
 *                      an approximate quantile threshold of anomaly scores for large dataset.
 * @param bootstrap :  If true, individual trees are fit on random subsets of the training data
 *                  sampled with replacement. If false, sampling without replacement is performed.
 * @param seed: The seed used by the random number generator.
 * @param inputFeatureNames
 */
case class IsolationForestParams(outputFeatureName: String,
                                 numTrees: Int,
                                 maxSamples: Double,
                                 maxFeatures: Double,
                                 maxDepth: Int,
                                 contamination: Double,
                                 bootstrap: Boolean,
                                 seed: Long,
                                 inputFeatureNames: Option[List[String]]=None)

class IsolationForest(isolationForestParams: IsolationForestParams, stageNum: Int = -1)
        extends AbstractUnsupervisedAlgo{
    override def transform(features: Dataset[Feature],
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): Dataset[Feature] = {
        val inputFeatureNames: List[String] = isolationForestParams.inputFeatureNames match{
            case Some(x) => x
            case None => {
                features.head(1).apply(0).dense.keySet.toList
            }
        }
        println("Input Feature Names: " + inputFeatureNames)
        import spark.implicits._
        val featuresForIF = features.withColumn("featureVec", Converters.mapToVec(inputFeatureNames)($"dense"))
        val iforest = new IForest(isolationForestParams)
        val model = iforest.fit(featuresForIF)
        val results = iforest.transform(featuresForIF, model).drop($"featureVec").as[Feature]

        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            SyncableDataFramePaths.setPath(results, sharedParams.outputFilePath)
            results.write.mode(SaveMode.Overwrite).parquet(sharedParams.outputFilePath)
            saveUnsupervisedToDB(this,
                features,
                results,
                inputFeatureNames,
                List(isolationForestParams.outputFeatureName),
                isolationForestParams.outputFeatureName,
                stageNum
            )
        }
        results
    }

    override def getName(): String = "Isolation Forest"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}