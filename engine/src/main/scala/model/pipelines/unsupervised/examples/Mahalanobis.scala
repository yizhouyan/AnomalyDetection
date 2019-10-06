package model.pipelines.unsupervised.examples

import model.common._
import model.pipelines.unsupervised.AbstractUnsupervisedAlgo
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
case class MahalanobisParams(k: Int = 10)

class Mahalanobis(mahalanobisParams: MahalanobisParams, stageNum: Int = -1) extends AbstractUnsupervisedAlgo{
    override def transform(features: Dataset[Feature],
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           sharedParams:SharedParams): Dataset[Feature] = {
        println("In Mahalanobis Class")
        features
    }

    override def getName(): String = ???

    override def getHyperParameters(): mutable.Map[Any, Any] = ???
}
