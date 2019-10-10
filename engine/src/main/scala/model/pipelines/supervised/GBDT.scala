package model.pipelines.supervised

import model.common.{Feature, LabeledData, SharedParams}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
class GBDT extends AbstractSupervisedAlgo{
    override def fit(labels: Dataset[LabeledData], features: DataFrame, runExplanations: Boolean): Any = {

    }

    override def transform(features: Dataset[Feature],
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): Dataset[Feature] = {
        features
    }

    override def getName(): String = "Gradient Boosted Decision Tree"

    override def getHyperParameters(): mutable.Map[Any, Any] = ???
}
