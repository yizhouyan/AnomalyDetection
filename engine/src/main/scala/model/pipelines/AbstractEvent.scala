package model.pipelines

import client.IEvent
import model.common.LabeledExamples
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._

import scala.collection.mutable
/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractEvent extends IEvent{
    def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean): Any
    def transform(features: Dataset[Feature], runExplanations: Boolean, spark: SparkSession, model_params: Option[Any] = None)
}