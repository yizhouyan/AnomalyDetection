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

case class UnsupervisedWorkflowInput(data: RegistryLookup,
                                     pipelines: PipelineConfig,
                                     runExplanations: Boolean = false,
                                     numPartitions: Option[Int] = None,
                                     finalOutputPath: Option[String] = None)

case class SupervisedWorkflowInput(labeledData: RegistryLookup,
                                   data: RegistryLookup,
                                   pipelines: PipelineConfig,
                                   runExplanations: Boolean = false,
                                   finalOutputPath: Option[String] = None)

case class CustomizedFile(path: String, fileType: String)

case class LabeledData(id: String, label: Float)

case class SharedParams(saveToDB: Boolean,
                        runExplanations: Boolean,
                        outputFilePath: String,
                        columeTracking: ColumnTracking,
                        var numPartitions: Int = 100,
                        numFeaturesForExplain: Int = 3)

case class SubspaceParams(subspaceMinDim: Int,
                          var subspaceMaxDim: Int,
                          subspaceNumSpaces: Int,
                          useFullSpace: Boolean,
                          seed: Long){
    require(subspaceMinDim >=1, "SubspaceMinDim should be greater than 0")
}
