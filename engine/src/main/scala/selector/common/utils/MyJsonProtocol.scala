package selector.common.utils

import selector.common.{MainWorkflowInput, RegistryLookup}
import selector.example_selectors.{SimilaritySelectorParams, WeightedSelectorParams}
import selector.example_sources.{ActiveLearningParams, AnomalyScoreDisagreeParams, AnomalyScoreParams, KmeansClustersParams}
import spray.json.DefaultJsonProtocol

/**
  * Created by yizhouyan on 9/6/19.
  */

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val registryLookupFormat = jsonFormat2(RegistryLookup)
    implicit val mainWorkflowInputFormat = jsonFormat6(MainWorkflowInput)
    implicit val anomalyScoreParamsFormat = jsonFormat5(AnomalyScoreParams)
    implicit val anomalyScoreDisagreeParamsFormat = jsonFormat5(AnomalyScoreDisagreeParams)
    implicit val kmeansClustersParamsFormat = jsonFormat5(KmeansClustersParams)
    implicit val activeLearningParamsFormat = jsonFormat1(ActiveLearningParams)
    implicit val weightedSelectorParamsFormat = jsonFormat3(WeightedSelectorParams)
    implicit val similaritySelectorParamsFormat = jsonFormat3(SimilaritySelectorParams)
}


