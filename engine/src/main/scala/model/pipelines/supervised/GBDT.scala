package model.pipelines.supervised

import model.common.{Feature, LabeledExamples}
import model.pipelines.AbstractEstimator
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
class GBDT extends AbstractEstimator{
    override def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean): Any = {

    }

    override def transform(features: Dataset[Feature], runExplanations: Boolean, spark: SparkSession, model_params: Option[Any]): Unit = {

    }
}
