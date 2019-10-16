package model.pipelines

import client.IEvent
import model.common.LabeledData
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._

import scala.collection.mutable
/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractEstimator extends IEvent{
    def fit(labels: Dataset[LabeledData], features: DataFrame, runExplanations: Boolean): Any
    def transform(features: DataFrame,
                  stageNum: Int = -1,
                  model_params: Option[Any] = None)
                 (implicit spark: SparkSession, sharedParams:SharedParams): DataFrame
}
