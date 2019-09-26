package model.pipelines

import model.common.LabeledExamples
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._
/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractEstimator {
    def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean): Any
    def transform(features: Dataset[Feature], runExplanations: Boolean, spark: SparkSession, model_params: Option[Any] = None)
}
