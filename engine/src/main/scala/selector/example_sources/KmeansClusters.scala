package selector.example_sources

import client.SyncableDataFramePaths
import model.pipelines.tools.Converters
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.utils.ReadInputData
import selector.common.{Example, LabeledExample, SharedParams}
import org.apache.spark.sql.functions._

import scala.collection.mutable
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.sql.expressions.Window

case class KmeansClustersParams(outputColName: Option[String],
                                numExamplesFromEachCluster: Int = 1,
                                nClusters: Int = 1000,
                                maxIter: Int = 100,
                                distanceMeasure: String = "euclidean",
                                seed: Long)

/**
 * Selects examples based on k-means clustering results. This is designed to ensure
 * the examples we picked are diverse and representative. Specifically, we will pick
 * several examples from each Kmeans cluster in order to make a diverse set of
 * examples.
 *
 * A weight is assigned to each cluster. The weight is computed as the number of
 * examples in the cluster divides by the maximum number of examples in each
 *     cluster. Larger clusters will have larger weights.
 * Created by yizhouyan on 9/8/19.
 */
class KmeansClusters(params: KmeansClustersParams) extends AbstractExampleSource{
    import KmeansClusters._
    override def getName(): String = {
        params.outputColName match{
            case Some(a) => a
            case None => "kmeans_clusters"
        }
    }

    override def fetch(labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
        import spark.implicits._
        logger.info("Get examples from kmeans clusters...")
        // get data from input path
        val data = ReadInputData.fetchInputData()
        val inputFeatureNames = data.columns.tail.toList.slice(0, sharedParams.numFeaturesInData)
        logger.info("Input Feature Names: " + inputFeatureNames.mkString(","))
        val dataset: DataFrame = Converters.createDenseVector(inputFeatureNames, data).select("id", "featureVec")
        // Trains a k-means model.
        val kmeans = new KMeans()
                .setFeaturesCol("featureVec")
                .setPredictionCol("cluster")
                .setMaxIter(params.maxIter)
                .setDistanceMeasure(params.distanceMeasure)
                .setK(params.nClusters)
                .setSeed(params.seed)
        val model = kmeans.fit(dataset)

        // Make predictions
        val predictions = model.transform(dataset).drop("featureVec")
        val clusterCnt = predictions.groupBy($"cluster").agg(count($"id").alias("cluster_cnt"))
        val clusterWeights = clusterCnt.crossJoin(clusterCnt.select(max($"cluster_cnt").alias("max_cnt")))
                .selectExpr("cluster", "cluster_cnt * 1.0/max_cnt AS weight")
                .withColumn("weight", col("weight").cast("double"))
        labeledExample.createOrReplaceTempView("labeledExample")
        val results = predictions.where("id not in (select id from labeledExample)")
                .withColumn("row_number", row_number.over(Window.partitionBy($"cluster").orderBy(rand())))
                .where($"row_number" <= params.numExamplesFromEachCluster)
                .drop($"row_number")
                .join(broadcast(clusterWeights), "cluster")
                .drop($"cluster")
                .withColumn("source",  lit(getName())).as[Example]
        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            logger.info("Save model to Storage")
            SyncableDataFramePaths.setPath(results.toDF, sharedParams.allExamplesOutputFileName)
            saveExampleSelectorEventsToDB(this,
                data,
                results,
                labeledExample,
                0
            )
        }
        results
    }

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        val paramsMap = mutable.Map[Any, Any]()
        paramsMap.put("outputColName", params.outputColName)
        paramsMap.put("numExamplesFromEachCluster", params.numExamplesFromEachCluster)
        paramsMap.put("nClusters", params.nClusters)
        paramsMap.put("maxIter", params.maxIter)
        paramsMap.put("distanceMeasure", params.distanceMeasure)
        paramsMap.put("seed", params.seed)
        paramsMap
    }
}

object KmeansClusters{
    val logger = Logger.getLogger(KmeansClusters.getClass)
}
