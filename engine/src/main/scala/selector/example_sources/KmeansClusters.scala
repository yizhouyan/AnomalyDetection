package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}
import selector.common.SharedParams

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

    override def fetch(labeledExample: DataFrame)
                      (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        println("Get Kmeans Clusters: " + name())
        spark.emptyDataFrame
    }
}
