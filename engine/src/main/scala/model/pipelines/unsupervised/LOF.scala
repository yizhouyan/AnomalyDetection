package model.pipelines.unsupervised

import model.common.Feature
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */

case class LOFParams(k: Int = 10)

class LOF(lofParams: LOFParams, stageNum: Int = -1) extends AbstractUnsupervisedAlgo {
    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {
        println("In LOF Class")
    }

    override def getName(): String = "Local Outlier Factor"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
