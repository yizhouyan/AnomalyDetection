package selector.example_selectors

import model.pipelines.tools.Converters
import org.apache.log4j.Logger
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.utils.ReadInputData
import selector.common.{Example, LabeledExample, SharedParams}
import org.apache.spark.sql.functions._
import breeze.linalg.{norm, DenseVector => BDV}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.sql.expressions.Window

import scala.collection.mutable

/**
 * An example selector that considers the similarity between unlabeled examples
 * and dissimilarity between labeled examples. The example selector mainly contains
 * two parts: (1) filtering points that are similar to the existing labels.
 * (2) sort the points according to the similarity to the other. The goal is to
 * avoid querying similar instances repetitively and to get more representative
 * candidates to label.
 *
 * Detailed Algorithm:
 *     1. Get feature vectors for example candidates as well as labeled examples.
 *     2. Compute similarities between candidates and labeled examples.
 *     3. Set a threshold to filter out examples that are similar to the
 * labeled examples.
 *     4. Do clustering on all remaining selected examples. Assign weight to each
 * cluster as num_instances_in_cluster/avg(min_similarity_to_labeled_examples).
 *     5. If the number of desired examples is smaller than the number of clusters.
 * We randomly return an instance in each top-weighted cluster. Otherwise, we
 * compute the number of examples selected from each cluster as k and randomly
 * return k points from each cluster.
 *
 * Created by yizhouyan on 9/8/19.
 */

case class SimilaritySelectorParams(numExamples: Option[Int],
                                    distFunction: String = "cosine",
                                    distThreshold: Float = 0.0f,
                                    nClusters: Option[Int],
                                    maxIter: Int = 10,
                                    seed: Long)


class SimilarityBasedSelector(params: SimilaritySelectorParams) extends AbstractExampleSelector{
    var numExamples: Int = 0
    var numClusters: Int = 0
    import SimilarityBasedSelector._
    override def getName(): String = {
        "Similarity_based_selector"
    }

    override def fetch(allExample: Dataset[Example], labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        import spark.implicits._
        logger.info("Example Selector:" + getName())
        var data: DataFrame = ReadInputData.fetchInputData()
        val inputFeatureNames = data.columns.tail.toList.slice(0, sharedParams.numFeaturesInData)
        logger.info("Input Feature Names: " + inputFeatureNames.mkString(","))
        data = Converters.createDenseVector(inputFeatureNames, data)

        // get feature vectors of all examples
        val allExampleFeatures = allExample.join(data,"id")

        // get feature vectors of labeled examples
        val labeledExampleFeatures = labeledExample.join(data,"id")

        println("All Examples Features Count: " + allExampleFeatures.count())
        println("LabeledExampleFeatures Count: " + labeledExampleFeatures.count())

        // compute similarity between example candidates and labeled instances. (use cosine similarity or euclidean
        // distance) and filter examples that are similar to the labeled examples (min_similarity <= threhsold)
        val euclideanDistanceUdf = udf{
            (x: Vector,y: Vector) => math.sqrt(Vectors.sqdist(x, y))
        }
        val cosineDistanceUdf = udf {
            (x: Vector, y: Vector) => {
                val bdv1 = new BDV(x.toArray)
                val bdv2 = new BDV(y.toArray)
                1 - (bdv1 dot bdv2) / (norm(bdv1) * norm(bdv2))
            }
        }
        val allExamplesWithSimilarity = {
                if(params.distFunction.equals("euclidean"))
                    allExampleFeatures.alias("a")
                            .crossJoin(labeledExampleFeatures.alias("b"))
                            .withColumn("dist", euclideanDistanceUdf($"a.featureVec", $"b.featureVec"))
                else
                    allExampleFeatures.alias("a")
                            .crossJoin(labeledExampleFeatures.alias("b"))
                            .withColumn("dist", cosineDistanceUdf($"a.featureVec", $"b.featureVec"))
                }.drop($"b.featureVec").drop($"b.id").drop($"b.label")
                .groupBy($"id", $"featureVec", $"weight",$"source")
                .agg(min($"dist").alias("min_dist"),
                    avg($"dist").alias("avg_dist"))
                .filter($"min_dist" > params.distThreshold)
                .drop($"min_dist").drop($"avg_dist")

        // cluster example candidates
        // Trains a k-means model.
        val numExampleCandidates = allExamplesWithSimilarity.count()
        numClusters = {
            if(params.nClusters.isDefined)
                params.nClusters.get
            else{
                math.ceil(numExampleCandidates * 1.0 / 100).toInt
            }
        }
        val kmeans = new KMeans()
                .setFeaturesCol("featureVec")
                .setPredictionCol("cluster")
                .setMaxIter(params.maxIter)
                .setDistanceMeasure(params.distFunction)
                .setK(numClusters)
                .setSeed(params.seed)
        val model = kmeans.fit(allExamplesWithSimilarity)
        numClusters = model.clusterCenters.length
        // Make predictions
        val predictions = model.transform(allExamplesWithSimilarity).drop("featureVec")

        val finalSelectedExamples = predictions.join(data,"id")
                .withColumn("row_number", row_number.over(Window.partitionBy($"cluster").orderBy($"weight")))
                .orderBy(asc("row_number"), desc("weight"))
                .drop("row_number").drop("cluster").drop("featureVec")
        if(params.numExamples.isDefined) {
            numExamples = params.numExamples.get
            finalSelectedExamples.limit(params.numExamples.get)
        }else{
            numExamples = numExampleCandidates.toInt
            finalSelectedExamples
        }
    }

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        val paramsMap = mutable.Map[Any, Any]()
        paramsMap
    }
}

object SimilarityBasedSelector{
    val logger = Logger.getLogger(SimilarityBasedSelector.getClass)
}