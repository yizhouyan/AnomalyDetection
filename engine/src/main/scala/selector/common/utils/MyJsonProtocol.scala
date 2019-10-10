package selector.common.utils

import selector.common.{MainWorkflowInput, RegistryLookup}
import selector.example_selectors.{SimilaritySelectorParams, WeightedSelectorParams}
import selector.example_sources.{ActiveLearningParams, AnomalyScoreDisagreeParams, AnomalyScoreParams, KmeansClustersParams}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * Created by yizhouyan on 9/6/19.
  */

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val mainWorkflowInputFormat = jsonFormat6(MainWorkflowInput)
    implicit val anomalyScoreDisagreeParamsFormat = jsonFormat5(AnomalyScoreDisagreeParams)
    implicit val kmeansClustersParamsFormat = jsonFormat5(KmeansClustersParams)
    implicit val activeLearningParamsFormat = jsonFormat1(ActiveLearningParams)
    implicit val weightedSelectorParamsFormat = jsonFormat3(WeightedSelectorParams)
    implicit val similaritySelectorParamsFormat = jsonFormat3(SimilaritySelectorParams)

    // set default values for anomaly score example source
    implicit object AnomalyScoreParamsJSONFormat extends RootJsonFormat[AnomalyScoreParams] {
        override def read(json: JsValue): AnomalyScoreParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            AnomalyScoreParams(
                fields.get("inputColName").fold("")(_.convertTo[String]),
                fields.get("filePath").map(_.convertTo[String]),
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
}


