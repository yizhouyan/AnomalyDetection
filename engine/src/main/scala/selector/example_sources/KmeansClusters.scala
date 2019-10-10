package selector.example_sources

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}

/**
  * Created by yizhouyan on 9/8/19.
  */
case class KmeansClustersParams(outputColName: Option[String],
                                numExamplesFromEachCluster: Int = 1,
                                bottomThres: Double = 0.0,
                                topThres: Double = 1.0,
                                sortByDist: Boolean = true)

class KmeansClusters(kmeansClustersParams: KmeansClustersParams) extends AbstractExampleSource{
    override def name(): String = {
        kmeansClustersParams.outputColName match{
            case Some(a) => a
            case None => "kmeans_clusters_%.2f_%.2f".format(
                kmeansClustersParams.bottomThres,
                kmeansClustersParams.topThres)
        }
    }

    override def fetch(labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
        import spark.implicits._
        println("Get Kmeans Clusters: " + name())
        spark.emptyDataset[Example]
    }
}
