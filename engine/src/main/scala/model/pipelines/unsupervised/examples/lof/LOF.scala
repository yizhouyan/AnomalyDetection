package model.pipelines.unsupervised.examples.lof

import client.SyncableDataFramePaths
import model.common.{SharedParams, SubspaceParams}
import model.pipelines.tools.Converters
import model.pipelines.tools.DefaultTools.generateSubspaces
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.ml.outlier.LocalOutlierFactor
import model.pipelines.tools.KNN._
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.shared.{HasFeaturesCol, HasOutputCol}
import org.apache.spark.ml.param.{IntParam, Param, Params}
import org.apache.spark.sql.expressions.Window

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import org.apache.spark.sql.functions.{arrays_zip, avg, col, explode, expr, max, row_number}
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
/**
  * Created by yizhouyan on 9/7/19.
  */

case class LOFParams(kList: List[Int],
                     outputFeatureName: String,
                     useSubspace: Boolean,
                     subspaceParams: Option[SubspaceParams],
                     inputFeatureNames: Option[List[String]]=None){
    require(kList.nonEmpty, "Must specify one k in the kList")
    require(kList.count(x => x > 0) == kList.length, "K must be greater than one")
}

/**
 * This is the local outlier factor (LOF) method.
 * The local outlier factor (LOF) is based on a concept of a local density,
 * where locality is given by k-nearest neighbors, whose distance is used to
 * estimate the density. By comparing the local density of an object to the
 * local densities of its neighbors, points that have a substantially lower
 * density than their neighbors are considered to be outliers.
 * LOF also gives each object an outlierness score.
 *
 * @param params - Parameters for LOF.
 * @param stageNum - Stage number for the current algorithm.
 */
class LOF(params: LOFParams, stageNum: Int = -1) extends AbstractUnsupervisedAlgo with Serializable {
    var inputFeatureNames: List[String] = List()
    var subspacesList: List[List[String]] = List()
    var minPts = 10
    import LOF._
    override def transform(features: DataFrame,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): DataFrame = {
        this.inputFeatureNames = params.inputFeatureNames match {
            case Some(x) => x
            case None =>
                sharedParams.columeTracking.getFeatureCols()
        }
        logger.info("Input Feature Names: " + inputFeatureNames)
        val maxK = params.kList.max
        logger.info("Max K: " + maxK)

        this.subspacesList = {
            if (params.useSubspace) {
                require(params.subspaceParams.isDefined, "Please set Subspace Parameters! ")
                generateSubspaces(inputFeatureNames, params.subspaceParams.get)
            } else
                List(inputFeatureNames)
        }
        import spark.implicits._
        var results = features
        val allOutputColNames = new ListBuffer[String]
        for ((subspace: List[String], index: Int) <- subspacesList.zipWithIndex) {
            logger.info("Processing Subspace: " + subspace)
            val featuresDF: DataFrame = Converters.createDenseVector(subspace, results)
              .withColumnRenamed("featureVec", "features")
            for (k: Int <- params.kList) {
                logger.info("Processing K = " + k)
                val resultsColName = params.outputFeatureName + "_subspace_" + index + "_k_" + k
                val lofResult = new LocalOutlierFactor().setMinPts(k).transform(featuresDF)
                val lofMerge = lofResult.select("id", "lof")
                  .withColumnRenamed("lof", resultsColName)
                results = results.join(lofMerge, Seq("id"),"right")
                results.show(5)
            }
        }
        sharedParams.columeTracking.addToResult(allOutputColNames.toList)
        results = results.coalesce(sharedParams.numPartitions)
        // if saveToDB is set to true, save the results to Storage
        if (sharedParams.saveToDB) {
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


    override def getName(): String = "Local Outlier Factor"

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

object LOF{
    val logger: Logger = Logger.getLogger(LOF.getClass)
}



//package model.pipelines.unsupervised.examples
//
//import client.SyncableDataFramePaths
//import model.common.{SharedParams, SubspaceParams}
//import model.pipelines.tools.Converters
//import model.pipelines.tools.DefaultTools.generateSubspaces
//import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
//import org.apache.log4j.Logger
//import org.apache.spark.sql.{DataFrame, SparkSession}
//import model.pipelines.tools.KNN._
//import org.apache.spark.sql.expressions.Window
//
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//import org.apache.spark.sql.functions.{arrays_zip, avg, col, explode, expr, max, row_number}
//import org.apache.spark.sql.types.DataTypes
///**
// * Created by yizhouyan on 9/7/19.
// */
//
//case class LOFParams(kList: List[Int],
//                     outputFeatureName: String,
//                     useSubspace: Boolean,
//                     subspaceParams: Option[SubspaceParams],
//                     inputFeatureNames: Option[List[String]]=None){
//    require(kList.length > 0, "Must specify one k in the kList")
//    require(kList.filter(x => x > 0).length == kList.length, "K must be greater than one")
//}
//
///**
// * This is the local outlier factor (LOF) method.
// * The local outlier factor (LOF) is based on a concept of a local density,
// * where locality is given by k-nearest neighbors, whose distance is used to
// * estimate the density. By comparing the local density of an object to the
// * local densities of its neighbors, points that have a substantially lower
// * density than their neighbors are considered to be outliers.
// * LOF also gives each object an outlierness score.
// *
// * @param params - Parameters for LOF.
// * @param stageNum - Stage number for the current algorithm.
// */
//class LOF(params: LOFParams, stageNum: Int = -1)
//  extends AbstractUnsupervisedAlgo with Serializable {
//    var inputFeatureNames: List[String] = List()
//    var subspacesList: List[List[String]] = List()
//    import LOF._
//    override def transform(features: DataFrame,
//                           stageNum: Int = -1,
//                           model_params: Option[Any] = None)
//                          (implicit spark: SparkSession,
//                           sharedParams:SharedParams): DataFrame = {
//        this.inputFeatureNames = params.inputFeatureNames match{
//            case Some(x) => x
//            case None => {
//                sharedParams.columeTracking.getFeatureCols()
//            }
//        }
//        logger.info("Input Feature Names: " + inputFeatureNames)
//        val maxK = params.kList.max
//        logger.info("Max K: "+ maxK)
//
//        this.subspacesList = {
//            if(params.useSubspace) {
//                require(params.subspaceParams.isDefined, "Please set Subspace Parameters! ")
//                generateSubspaces(inputFeatureNames, params.subspaceParams.get)
//            } else
//                List(inputFeatureNames)
//        }
//        import spark.implicits._
//        var results = features
//        val allOutputColNames = new ListBuffer[String]
//        for ((subspace:List[String], index:Int) <- subspacesList.zipWithIndex) {
//            logger.info("Processing Subspace: " + subspace)
//            val featuresForKNN: DataFrame = Converters.createDenseVector(subspace, results)
//            val knnResults = computeKNN(featuresForKNN, maxK)
//            val flatten_knn_table = knnResults.withColumn("knn", explode(arrays_zip($"neighbors", $"distances")))
//              .select($"id", $"knn.neighbors.id".alias("knn_id"), $"knn.distances".alias("dist"))
//              .filter($"id" =!= $"knn_id")
//              .withColumn("row_number", row_number()
//                .over(Window.partitionBy("id")
//                  .orderBy("dist"))
//                .cast(DataTypes.IntegerType))
//            for(k: Int <- params.kList){
//                logger.info("Processing K = " + k)
//                val resultsColName = params.outputFeatureName + "_subspace_" + index + "_k_" + k
//                allOutputColNames += resultsColName
//                val flatten_knn_table_for_specific_k = flatten_knn_table
//                  .filter($"row_number"<=k)
//                  .drop($"row_number")
//                val kdistScores = flatten_knn_table_for_specific_k
//                  .groupBy($"id")
//                  .agg(max($"dist").alias("kdist"))
//                logger.info("K-dist computed. ")
//                val lrdScores = flatten_knn_table_for_specific_k.alias("a")
//                  .join(kdistScores.alias("b"), $"a.knn_id" === $"b.id", "left")
//                  .select($"a.id", $"knn_id", expr("IF(dist > kdist, dist, kdist) AS reachdist"))
//                  .groupBy($"id")
//                  .agg(expr("IF(avg(reachdist) > 0, 1.0 / avg(reachdist), -1.0) AS lrd"))
//                logger.info("LRD computed. ")
//                val lofScores = lrdScores.alias("a").join(
//                    flatten_knn_table_for_specific_k.alias("a")
//                      .join(lrdScores.alias("b"), $"a.knn_id" === $"b.id", "left")
//                      .groupBy($"a.id")
//                      .agg(avg($"lrd").alias("knn_lrd")).alias("b"),
//                    $"a.id" === $"b.id",
//                    "left").selectExpr("a.id", "IF(lrd > 0, knn_lrd / lrd, -1.0) AS lof")
//                logger.info("LOF computed. ")
//                results = results.alias("a")
//                  .join(lofScores.alias("b"), $"a.id" === $"b.id", "left")
//                  .drop($"b.id")
//                  .withColumn(resultsColName, col("lof"))
//                  .drop("lof")
//            }
//        }
//        sharedParams.columeTracking.addToResult(allOutputColNames.toList)
//        results = results.coalesce(sharedParams.numPartitions)
//        // if saveToDB is set to true, save the results to Storage
//        if(sharedParams.saveToDB){
//            logger.info("Save model to Storage")
//            SyncableDataFramePaths.setPath(results, sharedParams.outputFilePath)
//            saveUnsupervisedToDB(this,
//                features,
//                results,
//                inputFeatureNames,
//                allOutputColNames.toList,
//                stageNum
//            )
//        }
//        results
//    }
//
//    override def getName(): String = "Local Outlier Factor"
//
//    override def getHyperParameters(): mutable.Map[Any, Any] = {
//        var paramsMap = mutable.Map[Any, Any]()
//        paramsMap.put("outputFeatureName", params.outputFeatureName)
//        paramsMap.put("kList", params.kList)
//        paramsMap.put("useSubspace", params.useSubspace)
//        paramsMap.put("inputFeatureNames", inputFeatureNames)
//        if (params.subspaceParams.isDefined){
//            val subspaceParams: SubspaceParams = params.subspaceParams.get
//            paramsMap.put("subspaceMinDim", subspaceParams.subspaceMinDim)
//            paramsMap.put("subspaceMaxDim", subspaceParams.subspaceMaxDim)
//            paramsMap.put("subspaceNumSpaces", subspaceParams.subspaceNumSpaces)
//            paramsMap.put("useFullSpace", subspaceParams.useFullSpace)
//            paramsMap.put("seed", subspaceParams.seed)
//            paramsMap.put("subspacesList", subspacesList)
//        }
//        paramsMap
//    }
//}
//
//object LOF{
//    val logger = Logger.getLogger(LOF.getClass)
//}

