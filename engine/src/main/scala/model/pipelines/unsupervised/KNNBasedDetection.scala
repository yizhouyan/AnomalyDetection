package model.pipelines.unsupervised

import model.common.Feature
import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
case class KNNBasedDetectionParams(k: Int = 10)

class KNNBasedDetection(kNNBasedDetectionParams: KNNBasedDetectionParams, stageNum: Int = -1) extends AbstractTransformer{
    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {
        println("In KNN-Based Class")
    }

    override def getName(): String = "KNN based detection"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
