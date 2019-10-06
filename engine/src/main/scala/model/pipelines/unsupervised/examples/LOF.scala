package model.pipelines.unsupervised.examples

import model.common.{Feature, SharedParams}
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */

case class LOFParams(k: Int = 10)

class LOF(lofParams: LOFParams, stageNum: Int = -1) extends AbstractUnsupervisedAlgo {
    override def transform(features: Dataset[Feature],
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): Dataset[Feature] = {
        println("In LOF Class")
        features
    }

    override def getName(): String = "Local Outlier Factor"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
