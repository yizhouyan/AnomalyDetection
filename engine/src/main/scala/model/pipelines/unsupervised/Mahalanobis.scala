package model.pipelines.unsupervised

import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._

/**
  * Created by yizhouyan on 9/7/19.
  */
case class MahalanobisParams(k: Int = 10)

class Mahalanobis(mahalanobisParams: MahalanobisParams) extends AbstractTransformer{
    override def transform(features: Dataset[Feature],
                           runExplanations: Boolean,
                           spark: SparkSession,
                           model_params: Option[Any] = None): Unit = {
        println("In Mahalanobis Class")

    }
}
