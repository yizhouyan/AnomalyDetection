package model.common.utils

import model.common.{SubspaceParams, _}
import model.pipelines.unsupervised.examples.iforest.IsolationForestParams
import model.pipelines.unsupervised.examples.{KNNBasedDetectionParams, LOFParams, MahalanobisParams, StandardScalerParams}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * Created by yizhouyan on 9/6/19.
  */


object MyJsonProtocol extends DefaultJsonProtocol {
    // set default values for KNN-based detection method
    implicit object SubspaceParamsJSONFormat extends RootJsonFormat[SubspaceParams] {
        override def read(json: JsValue): SubspaceParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            SubspaceParams(
                fields.get("subspaceMinDim").fold(1)(_.convertTo[Int]),
                fields.get("subspaceMaxDim").fold(5)(_.convertTo[Int]),
                fields.get("subspaceNumSpaces").fold(5)(_.convertTo[Int]),
                fields.get("useFullSpace").fold(true)(_.convertTo[Boolean]),
                fields.get("seed").fold(getClass.getName.hashCode.toLong)(_.convertTo[Long])
            )
        }
        override def write(obj: SubspaceParams): JsValue = JsObject(
            "subspaceMinDim" -> JsNumber(obj.subspaceMinDim),
            "subspaceMaxDim" -> JsNumber(obj.subspaceMaxDim),
            "subspaceNumSpaces" -> JsNumber(obj.subspaceNumSpaces),
            "useFullSpace" -> JsBoolean(obj.useFullSpace),
            "seed" -> JsNumber(obj.seed)
        )
    }

    implicit val subspaceParamsFormat = jsonFormat5(SubspaceParams)
    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val pipelineStageFormat = jsonFormat3(PipelineStage)
    implicit val pipelineConfigFormat = jsonFormat1(PipelineConfig)
    implicit val unsupervisedWorkflowInputFormat = jsonFormat4(UnsupervisedWorkflowInput)
    implicit val supervisedWorkflowInputFormat = jsonFormat5(SupervisedWorkflowInput)
    implicit val standardScalerParamsFormat = jsonFormat2(StandardScalerParams)
    implicit val mahalanobisParamsFormat = jsonFormat1(MahalanobisParams)
    implicit val customizedFileFormat = jsonFormat2(CustomizedFile)

    // set default values for Isolation forest
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

        override def write(obj: IsolationForestParams): JsValue = JsObject(
            "outputFeatureName" -> JsString(obj.outputFeatureName),
            "numTrees" -> JsNumber(obj.numTrees),
            "maxSamples" -> JsNumber(obj.maxSamples),
            "maxFeatures" -> JsNumber(obj.maxFeatures),
            "maxDepth" -> JsNumber(obj.maxDepth),
            "contamination" -> JsNumber(obj.contamination),
            "bootstrap" -> JsBoolean(obj.bootstrap),
            "seed" -> JsNumber(obj.seed),
            "inputFeatureNames" -> JsString(obj.inputFeatureNames.mkString(","))
        )
    }

    // set default values for KNN-based detection method
    implicit object KNNDetectionJSONFormat extends RootJsonFormat[KNNBasedDetectionParams] {
        override def read(json: JsValue): KNNBasedDetectionParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            KNNBasedDetectionParams(
                fields.get("kList").fold(List(3))(_.convertTo[List[Int]]),
                fields.get("outputFeatureName").fold("knn_based_results")(_.convertTo[String]),
                fields.get("useSubspace").fold(false)(_.convertTo[Boolean]),
                fields.get("subspaceParams").map(_.convertTo[SubspaceParams]),
                fields.get("inputFeatureNames").map(_.convertTo[List[String]])
            )
        }

        override def write(obj: KNNBasedDetectionParams): JsValue = JsObject(
            "kList" -> JsString(obj.kList.mkString(",")),
            "outputFeatureName" -> JsString(obj.outputFeatureName),
            "useSubspace" -> JsBoolean(obj.useSubspace),
            "inputFeatureNames" -> JsString(obj.inputFeatureNames.mkString(","))
        )
    }

    // set default values for LOF method
    implicit object LOFJSONFormat extends RootJsonFormat[LOFParams] {
        override def read(json: JsValue): LOFParams = {
            val fields = json.asJsObject("Invalid JSON Object").fields
            LOFParams(
                fields.get("kList").fold(List(3))(_.convertTo[List[Int]]),
                fields.get("outputFeatureName").fold("lof_results")(_.convertTo[String]),
                fields.get("useSubspace").fold(false)(_.convertTo[Boolean]),
                fields.get("subspaceParams").map(_.convertTo[SubspaceParams]),
                fields.get("inputFeatureNames").map(_.convertTo[List[String]])
            )
        }

        override def write(obj: LOFParams): JsValue = JsObject(
            "kList" -> JsString(obj.kList.mkString(",")),
            "outputFeatureName" -> JsString(obj.outputFeatureName),
            "useSubspace" -> JsBoolean(obj.useSubspace),
            "inputFeatureNames" -> JsString(obj.inputFeatureNames.mkString(","))
        )
    }
}


