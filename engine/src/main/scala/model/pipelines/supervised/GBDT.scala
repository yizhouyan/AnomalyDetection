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

    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {

    }

    override def getName(): String = "Gradient Boosted Decision Tree"

    override def getHyperParameters(): mutable.Map[Any, Any] = ???
}
