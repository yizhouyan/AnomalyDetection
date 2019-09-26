package selector.common

import spray.json.JsValue

/**
  * Created by yizhouyan on 9/6/19.
  */

case class RegistryLookup(name:String, params: Option[JsValue] = None)

case class MainWorkflowInput(exampleSources: List[RegistryLookup],
                             exampleSelector: RegistryLookup,
                             allExamplesOutputTableName: Option[String] = None,
                             selectedExamplesOutputTableName: Option[String] = None,
                             labeledExamplesOutputTableName: Option[String] = None,
                             labeledExamples: Option[RegistryLookup] = None)

