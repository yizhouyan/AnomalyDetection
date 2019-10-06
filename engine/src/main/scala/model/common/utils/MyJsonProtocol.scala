package model.common.utils

import model.common._
import model.pipelines.unsupervised.examples.iforest.IsolationForestParams
import model.pipelines.unsupervised.examples.{KNNBasedDetectionParams, LOFParams, MahalanobisParams, StandardScalerParams}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * Created by yizhouyan on 9/6/19.
  */


object MyJsonProtocol extends DefaultJsonProtocol {
    // set default values for Isolation forest parameters
    implicit object IForestJSONFormat extends RootJsonFormat[IsolationForestParams] {
        override def read(json: JsValue): IsolationForestParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            IsolationForestParams(
                fields.get("outputFeatureName").fold("iforest")(_.convertTo[String]),
                fields.get("numTrees").fold(100)(_.convertTo[Int]),
                fields.get("maxSamples").fold(1.0)(_.convertTo[Double]),
                fields.get("maxFeatures").fold(1.0)(_.convertTo[Double]),
                fields.get("maxDepth").fold(10)(_.convertTo[Int]),
                fields.get("contamination").fold(0.1)(_.convertTo[Double]),
                fields.get("bootstrap").fold(false)(_.convertTo[Boolean]),
                fields.get("seed").fold(getClass.getName.hashCode.toLong)(_.convertTo[Long]),
                fields.get("inputFeatureNames").map(_.convertTo[List[String]])
            )
        }
        //        inputFeatureNames
        override def write(obj: IsolationForestParams): JsValue = JsObject(
            "outputFeatureName" -> JsString(obj.outputFeatureName),
            "numTrees" -> JsNumber(obj.numTrees),
            "maxSamples" -> JsNumber(obj.maxSamples),
            "maxFeatures" -> JsNumber(obj.maxFeatures),
            "maxDepth" -> JsNumber(obj.maxDepth),
            "contamination" -> JsNumber(obj.contamination),
            "bootstrap" -> JsBoolean(obj.bootstrap),
            "seed" -> JsNumber(obj.seed)
        )
    }

    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val pipelineStageFormat = jsonFormat3(PipelineStage)
    implicit val pipelineConfigFormat = jsonFormat1(PipelineConfig)
    implicit val unsupervisedWorkflowInputFormat = jsonFormat4(UnsupervisedWorkflowInput)
    implicit val supervisedWorkflowInputFormat = jsonFormat5(SupervisedWorkflowInput)
    implicit val standardScalerParamsFormat = jsonFormat2(StandardScalerParams)
//    implicit val isolationForestParamsFormat = jsonFormat1(IsolationForestParams)
    implicit val kNNBasedDetectionParamsFormat = jsonFormat1(KNNBasedDetectionParams)
    implicit val lofParamsFormat = jsonFormat1(LOFParams)
    implicit val mahalanobisParamsFormat = jsonFormat1(MahalanobisParams)
    implicit val customizedFileFormat = jsonFormat2(CustomizedFile)
}


