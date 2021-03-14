package model.pipelines.unsupervised.examples

import client.SyncableDataFramePaths
import model.common.{SharedParams, SubspaceParams}
import model.pipelines.tools.Converters
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import org.apache.spark.sql.{DataFrame, SparkSession}
import model.pipelines.tools.KNN._
import model.pipelines.tools.DefaultTools._
import org.apache.log4j.Logger
import org.apache.spark.sql.functions.{col, udf}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by yizhouyan on 9/7/19.
  */
case class KNNBasedDetectionParams(kList: List[Int],
                                   outputFeatureName: String,
                                   useSubspace: Boolean,
                                   subspaceParams: Option[SubspaceParams],
                                   inputFeatureNames: Option[List[String]]=None){
    require(kList.nonEmpty, "Must specify one k in the kList")
    require(kList.count(x => x > 0) == kList.length, "K must be greater than one")
}

/**
 * This is the distance-based(Knn-based) anomaly detection method.
 * The average distance of each data point to its 1..k-th nearest neighbor
 * is reported as its knn-based anomaly score.
 *
 * @param params - parameters for knn based detection.
 * @param stageNum - stage number of the current algorithm.
 */
class KNNBasedDetection(params: KNNBasedDetectionParams, stageNum: Int = -1)
        extends AbstractUnsupervisedAlgo with Serializable {
    var inputFeatureNames: List[String] = List()
    var subspacesList: List[List[String]] = List()
    import KNNBasedDetection._
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
        val maxK = params.kList.max
        logger.info("Max K: "+ maxK)

        this.subspacesList = {
            if(params.useSubspace) {
                require(params.subspaceParams.isDefined, "Please set Subspace Parameters! ")
                generateSubspaces(inputFeatureNames, params.subspaceParams.get)
            } else
                List(inputFeatureNames)
        }
        import spark.implicits._

        val getKdistUDF = udf{
            distances: mutable.WrappedArray[Double] =>{
                params.kList.map{k => {
                    val curArray = distances.slice(1, k+1)
                    curArray.sum/curArray.length
                }
                }.toArray
            }
        }
        var results = features
        val allOutputColNames = new ListBuffer[String]
        for ((subspace:List[String], index:Int) <- subspacesList.zipWithIndex) {
            logger.info("Processing Subspace: " + subspace)
            val resultsColNames = params.kList.map(x =>
                params.outputFeatureName + "_subspace_" + index + "_k_" + x).toArray
            allOutputColNames ++= resultsColNames
            val featuresForKNN: DataFrame = Converters.createDenseVector(subspace, results)
            val knnResults = computeKNN(featuresForKNN, maxK)

            results = knnResults.drop($"neighbors")
                    .withColumn("dataArray",getKdistUDF(col("distances")))
                    .drop("distances")

            results = resultsColNames.zipWithIndex.foldLeft(results) {
                (memodDF, column) => {
                    memodDF.withColumn(column._1, col("dataArray")(column._2))
                }
            }.drop("dataArray")
            sharedParams.columeTracking.addToResult(resultsColNames.toList)
        }
        results = results.coalesce(sharedParams.numPartitions)
        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            logger.info("Save model to Storage")
            SyncableDataFramePaths.setPath(results, sharedParams.outputFilePath)
            saveUnsupervisedToDB(this,
                features,
                results,
                inputFeatureNames,
                allOutputColNames.toList,
                stageNum
            )
        }
        results
    }

    override def getName(): String = "KNN based detection"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var paramsMap = mutable.Map[Any, Any]()
        paramsMap.put("outputFeatureName", params.outputFeatureName)
        paramsMap.put("kList", params.kList)
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

object KNNBasedDetection{
    val logger = Logger.getLogger(KNNBasedDetection.getClass)
}
