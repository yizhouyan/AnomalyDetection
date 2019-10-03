package model.pipelines.unsupervised

import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import model.common._

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
case class MahalanobisParams(k: Int = 10)

class Mahalanobis(mahalanobisParams: MahalanobisParams, stageNum: Int = -1) extends AbstractTransformer{
    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {
        println("In Mahalanobis Class")

    }

    override def getName(): String = ???

    override def getHyperParameters(): mutable.Map[Any, Any] = ???
}
