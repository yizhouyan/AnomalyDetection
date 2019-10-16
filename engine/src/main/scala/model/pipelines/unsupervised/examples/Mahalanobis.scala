package model.pipelines.unsupervised.examples

import model.common._
import model.pipelines.tools.{Converters, Statistics}
import model.pipelines.tools.DefaultTools.generateSubspaces
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.linalg.Vector
import breeze.numerics.sqrt
import client.SyncableDataFramePaths
import org.apache.spark.sql.functions.{col, lit, map, map_concat, udf}
import model.pipelines.tools.DefaultTools._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by yizhouyan on 9/7/19.
  */
case class MahalanobisParams(outputFeatureName: String,
                             useSubspace: Boolean,
                             subspaceParams: Option[SubspaceParams],
                             inputFeatureNames: Option[List[String]]=None)

/**
 * This is the Mahalanobis method for outlier detection.
 * Mahalanobis method is a distribution-dependent approach. It first models the
 * entire dataset to be normally distributed about its mean in the form of a
 * multivariate Gaussian distribution. Then for each point, we compute the
 * Mahalanobis distance between the point and the multivariate Gaussian distribution.
 *
 * The mahalanobis distance for one data point can be computed as:
 * Mahalanobis(X, μ, Σ) = (X − μ)Σ−1 (X − μ)T, where μ is the mean vector,
 * and Σ is a covariance matrix.
 *
 * Once we get the Mahalanobis distance for each data point, we can use any
 * extreme value detection methods to detect outliers.
 *
 * Explanations for mahalanobis method - feature-wise explanations for mahalanobis dist. For a given
 * example, we replace each feature with its mean value and use the change
 * in Mahalanobis distance as the feature importance. The result is a set of
 * (feature, importance) pairs that how important the feature was in computing
 * the score for the example.
 *
 * @param params - Input parameters for mahalanobis method
 * @param stageNum - Stage number for mahalanobis method
 */
class Mahalanobis(params: MahalanobisParams, stageNum: Int = -1)
        extends AbstractUnsupervisedAlgo{
    var inputFeatureNames: List[String] = List()
    var subspacesList: List[List[String]] = List()
    import Mahalanobis._
    override def transform(features: DataFrame,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): DataFrame = {
        this.inputFeatureNames = params.inputFeatureNames match{
            case Some(x) => x
            case None => {
                sharedParams.columeTracking.getFeatureCols()
            }
        }
        logger.info("Input Feature Names: " + inputFeatureNames)
        this.subspacesList = {
            if(params.useSubspace) {
                require(params.subspaceParams.isDefined, "Please set Subspace Parameters! ")
                generateSubspaces(inputFeatureNames, params.subspaceParams.get)
            } else
                List(inputFeatureNames)
        }
        import spark.implicits._
        var results = features
        val allOutputColNames = new ListBuffer[String]
        for ((subspace:List[String], index:Int) <- subspacesList.zipWithIndex) {
            logger.info("Processing Subspace: " + subspace)
            //Compute the inverse covariance matrix
            val resultsColName = params.outputFeatureName + "_subspace_" + index
            val explanationColName = "mahalanobis_explaination_subspace_" + index
            allOutputColNames += resultsColName
            val invCov = Statistics.inv_cov(results, subspace)
            results = Converters.createDenseVector(subspace, results)
            val mean = breeze.linalg.DenseVector(Statistics.avg(results, subspace)
                    .head().get(0).asInstanceOf[mutable.WrappedArray[Double]].toArray)
            val invCovBroadCast = spark.sparkContext.broadcast(invCov)
            val meanBroadCast = spark.sparkContext.broadcast(mean)

            val mahalanobisDistUDF = udf{
                featureVec: Vector =>{
                    val x = breeze.linalg.DenseVector(featureVec.toArray)
                    val xMinusMu = x-meanBroadCast.value
                    sqrt(xMinusMu.t * invCovBroadCast.value * xMinusMu)
                }
            }
            // By default, we use top-3 features to explain why the point has been classified as an outlier.
            val numExplanations = math.min(subspace.length,sharedParams.numFeaturesForExplain)
            val mahalanobisExplainUDF = udf{
                featureVec: Vector =>{
                    var featureImportance = Array.fill(featureVec.size)(0.0)
                    val x = breeze.linalg.DenseVector(featureVec.toArray)
                    val xMinusMu = x-meanBroadCast.value
                    val mahalanobisDist = sqrt(xMinusMu.t * invCovBroadCast.value * xMinusMu)
                    for (i <- 0 until featureVec.size){
                        val revisedX = x.copy
                        revisedX(i) = meanBroadCast.value(i)
                        val revisedXMinusMu = revisedX-meanBroadCast.value
                        featureImportance(i) =
                                math.abs(sqrt(revisedXMinusMu.t * invCovBroadCast.value * revisedXMinusMu)-mahalanobisDist)
                    }
                    featureImportance.argSort.reverse.take(numExplanations).map(x => subspace(x)).mkString(",")
                }
            }

            results = results.withColumn(resultsColName, mahalanobisDistUDF($"featureVec"))
            sharedParams.columeTracking.addToResult(resultsColName)
            if(sharedParams.runExplanations){
                results = results.withColumn(explanationColName, mahalanobisExplainUDF($"featureVec"))
                sharedParams.columeTracking.addToExplain(explanationColName)
            }
            results = results.drop($"featureVec")
        }
        val finalRes = results.coalesce(sharedParams.numPartitions)
        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            logger.info("Save model to Storage")
            SyncableDataFramePaths.setPath(finalRes, sharedParams.outputFilePath)
//            results.write.mode(SaveMode.Overwrite).parquet(sharedParams.outputFilePath)
            saveUnsupervisedToDB(this,
                features,
                finalRes,
                inputFeatureNames,
                allOutputColNames.toList,
                stageNum
            )
        }
        finalRes
    }

    override def getName(): String = "Mahalanobis Method"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var paramsMap = mutable.Map[Any, Any]()
        paramsMap.put("outputFeatureName", params.outputFeatureName)
        paramsMap.put("useSubspace", params.useSubspace)
        paramsMap.put("inputFeatureNames", inputFeatureNames)
        if (params.subspaceParams.isDefined){
            val subspaceParams: SubspaceParams = params.subspaceParams.get
            paramsMap.put("subspaceMinDim", subspaceParams.subspaceMinDim)
            paramsMap.put("subspaceMaxDim", subspaceParams.subspaceMaxDim)
            paramsMap.put("subspaceNumSpaces", subspaceParams.subspaceNumSpaces)
            paramsMap.put("useFullSpace", subspaceParams.useFullSpace)
            paramsMap.put("seed", subspaceParams.seed)
            paramsMap.put("subspacesList", subspacesList)
        }
        paramsMap
    }
}

object Mahalanobis{
    val logger = Logger.getLogger(Mahalanobis.getClass)
}
