package selector.common

import spray.json.JsValue

/**
 * Created by yizhouyan on 9/6/19.
 */

case class RegistryLookup(name:String, params: Option[JsValue] = None)

case class MainWorkflowInput(exampleSources: List[RegistryLookup],
                             exampleSelector: RegistryLookup,
                             allExamplesOutputFileName: Option[String] = None,
                             selectedExamplesOutputFileName: Option[String] = None,
                             labeledExamples: Option[RegistryLookup] = None,
                             sharedFilePath: String)

case class Feature(id: String, dense: Map[String, Double], results: Map[String, Double], explanations: Map[String, String])

case class Example(id: String,
                   source: String,
                   weight: Double)

case class ExampleWithFeatures(id: String,
                               source: String,
                               weight: Double,
                               dense: Map[String, Double],
                               results: Map[String, Double],
                               explanations: Map[String, String])

case class LabeledExample(id: String, label: Double)

case class SharedParams(sharedFilePath: String,
                        saveToDB: Boolean,
                        allExamplesOutputFileName: String,
                        selectedExamplesOutputFileName: String)