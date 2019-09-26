package model.common.utils

import model.common._
import model.pipelines.unsupervised._
import spray.json.DefaultJsonProtocol
/**
  * Created by yizhouyan on 9/6/19.
  */


object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val pipelineStageFormat = jsonFormat3(PipelineStage)
    implicit val pipelineConfigFormat = jsonFormat1(PipelineConfig)
    implicit val unsupervisedWorkflowInputFormat = jsonFormat4(UnsupervisedWorkflowInput)
    implicit val supervisedWorkflowInputFormat = jsonFormat5(SupervisedWorkflowInput)
    implicit val standardScalerParamsFormat = jsonFormat2(StandardScalerParams)
    implicit val isolationForestParamsFormat = jsonFormat3(IsolationForestParams)
    implicit val kNNBasedDetectionParamsFormat = jsonFormat1(KNNBasedDetectionParams)
    implicit val lofParamsFormat = jsonFormat1(LOFParams)
    implicit val mahalanobisParamsFormat = jsonFormat1(MahalanobisParams)
    implicit val customizedFileFormat = jsonFormat2(CustomizedFile)
}


