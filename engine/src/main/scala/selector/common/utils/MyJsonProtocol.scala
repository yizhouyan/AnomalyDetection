package selector.common.utils

import selector.common.{MainWorkflowInput, RegistryLookup}
import selector.example_selectors.{IdentityParams, SimilaritySelectorParams}
import selector.example_sources.{ActiveLearningParams, AnomalyScoreParams, KmeansClustersParams}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * Created by yizhouyan on 9/6/19.
  */

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val mainWorkflowInputFormat = jsonFormat7(MainWorkflowInput)
    implicit val activeLearningParamsFormat = jsonFormat1(ActiveLearningParams)
    implicit val similaritySelectorParamsFormat = jsonFormat6(SimilaritySelectorParams)
    implicit val identityParamsFormat = jsonFormat1(IdentityParams)

    // set default values for anomaly score example source
    implicit object AnomalyScoreParamsJSONFormat extends RootJsonFormat[AnomalyScoreParams] {
        override def read(json: JsValue): AnomalyScoreParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            AnomalyScoreParams(
                fields.get("inputColName").fold("")(_.convertTo[String]),
                fields.get("outputColName").map(_.convertTo[String]),
                fields.get("bottomThres").fold(0.9)(_.convertTo[Double]),
                fields.get("topThres").fold(1.0)(_.convertTo[Double]),
                fields.get("usePercentile").fold(false)(_.convertTo[Boolean])
            )
        }
        override def write(obj: AnomalyScoreParams): JsValue = JsObject(
            "inputColName" -> JsString(obj.inputColName),
            "bottomThres" -> JsNumber(obj.bottomThres),
            "topThres" -> JsNumber(obj.topThres),
            "usePercentile" -> JsBoolean(obj.usePercentile)
        )
    }

    // set default values for kmeans example source
    implicit object KmeansParamsJSONFormat extends RootJsonFormat[KmeansClustersParams] {
        override def read(json: JsValue): KmeansClustersParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            KmeansClustersParams(
                fields.get("outputColName").map(_.convertTo[String]),
                fields.get("numExamplesFromEachCluster").fold(1)(_.convertTo[Int]),
                fields.get("nClusters").fold(1000)(_.convertTo[Int]),
                fields.get("maxIter").fold(10)(_.convertTo[Int]),
                fields.get("distanceMeasure").fold("euclidean")(_.convertTo[String]),
                fields.get("seed").fold(getClass.getName.hashCode.toLong)(_.convertTo[Long])
            )
        }
        override def write(obj: KmeansClustersParams): JsValue = JsObject(
            "numExamplesFromEachCluster" -> JsNumber(obj.numExamplesFromEachCluster),
            "nClusters" -> JsNumber(obj.nClusters),
            "maxIter" -> JsNumber(obj.maxIter),
            "seed" -> JsNumber(obj.seed)
        )
    }
}


