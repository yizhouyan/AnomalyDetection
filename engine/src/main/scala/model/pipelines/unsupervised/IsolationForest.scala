package model.pipelines.unsupervised

import model.common.Feature
import model.pipelines.AbstractTransformer
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/7/19.
  */
case class IsolationForestParams(outputFeatureName: String, nTrees: Int = 100, inputFeatureNames: Option[List[String]] = None)

class IsolationForest(isolationForestParams: IsolationForestParams)
        extends AbstractTransformer{
    override def transform(features: Dataset[Feature],
                           runExplanations: Boolean,
                           spark: SparkSession,
                           model_params: Option[Any] = None): Unit = {
        println("In Isolation Forest Class")
    }

    override def getName(): String = "Isolation Forest"

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        var params = mutable.Map[Any, Any]()
        params
    }
}
