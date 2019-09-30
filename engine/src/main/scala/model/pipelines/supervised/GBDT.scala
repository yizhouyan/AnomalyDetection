package model.pipelines.supervised

import model.common.{Feature, LabeledExamples}
import model.pipelines.AbstractEvent
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
class GBDT extends AbstractEvent{
    override def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean): Any = {

    }

    override def transform(features: Dataset[Feature], runExplanations: Boolean, spark: SparkSession, model_params: Option[Any]): Unit = {

    }

    override def getName(): String = ???

    override def getHyperParameters(): mutable.Map[Any, Any] = ???
}
