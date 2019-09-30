package model.pipelines.unsupervised

import model.common.Feature
import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */

case class LOFParams(k: Int = 10)

class LOF(lofParams: LOFParams) extends AbstractTransformer {
    override def transform(features: Dataset[Feature],
                           runExplanations: Boolean,
                           spark: SparkSession,
                           model_params: Option[Any] = None): Unit = {
        println("In LOF Class")
    }

    override def getName(): String = "Local Outlier Factor"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
