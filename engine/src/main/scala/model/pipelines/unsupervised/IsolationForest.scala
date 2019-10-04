package model.pipelines.unsupervised

import model.common.Feature
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
case class IsolationForestParams(outputFeatureName: String,
                                 nTrees: Int = 100,
                                 inputFeatureNames: Option[List[String]])

class IsolationForest(isolationForestParams: IsolationForestParams, stageNum: Int = -1)
        extends AbstractUnsupervisedAlgo{
    override def transform(features: DataFrame,
                           runExplanations: Boolean,
                           stageNum: Int = -1,
                           model_params: Option[Any] = None)
                          (implicit spark: SparkSession,
                           saveToDB: Boolean,
                           finalOutputPath: String): Unit = {
        println("In Isolation Forest Class")
    }

    override def getName(): String = "Isolation Forest"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
