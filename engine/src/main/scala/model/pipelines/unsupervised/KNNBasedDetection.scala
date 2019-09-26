package model.pipelines.unsupervised

import model.common.Feature
import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
case class KNNBasedDetectionParams(k: Int = 10)

class KNNBasedDetection(kNNBasedDetectionParams: KNNBasedDetectionParams) extends AbstractTransformer{
    override def transform(features: Dataset[Feature],
                           runExplanations: Boolean,
                           spark: SparkSession,
                           model_params: Option[Any] = None): Unit = {
        println("In KNN-Based Class")
    }
}
