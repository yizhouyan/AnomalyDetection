package selector.common.utils

import selector.common.RegistryLookup
import selector.example_sources._
import spray.json._
import selector.common.utils.MyJsonProtocol._
import selector.example_selectors._

/**
  * Created by yizhouyan on 9/7/19.
  */
object ClassNameMapping {
    def mapClassNameToClass(lookup: RegistryLookup): Any = {
        val jsonAst = lookup.params.mkString.parseJson
        lookup.name match{
            case "AnomalyScore" => new AnomalyScore(jsonAst.convertTo[AnomalyScoreParams])
            case "AnomalyScoreDisagreement" => new AnomalyScoreDisagreement(jsonAst.convertTo[AnomalyScoreDisagreeParams])
            case "KmeansClusters" => new KmeansClusters(jsonAst.convertTo[KmeansClustersParams])
            case "ActiveLearning" => new ActiveLearning(jsonAst.convertTo[ActiveLearningParams])
            case "Identity" => new Identity
            case "WeightedSelector" => new WeightedSelector(jsonAst.convertTo[WeightedSelectorParams])
            case "SimilarityBasedSelector" => new SimilarityBasedSelector(jsonAst.convertTo[SimilaritySelectorParams])
        }
    }
}
