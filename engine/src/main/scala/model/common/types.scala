package model.common

import spray.json.JsValue

/**
  * Created by yizhouyan on 9/6/19.
  */

case class RegistryLookup(name:String, params: Option[JsValue] = None)

case class PipelineStage(estimators: List[RegistryLookup],
                         dropFeaturesIfInList: Option[List[String]],
                         dropFeaturesIfnotInList: Option[List[String]])

case class PipelineConfig(stages: List[PipelineStage])

case class UnsupervisedWorkflowInput(examples: RegistryLookup,
                                     pipelines: PipelineConfig,
                                     runExplanations: Boolean = false,
                                     finalOutputPath: Option[String] = None)

case class SupervisedWorkflowInput(labeledData: RegistryLookup,
                                   examples: RegistryLookup,
                                   pipelines: PipelineConfig,
                                   runExplanations: Boolean = false,
                                   finalOutputPath: Option[String] = None)

case class CustomizedFile(path: String, fileType: String)

case class Feature(id: String, dense: Map[String, Double], explanations: Map[String, String])

case class LabeledExamples(id: String, label: Float)