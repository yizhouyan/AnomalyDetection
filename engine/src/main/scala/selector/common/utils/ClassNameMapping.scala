package selector.common.utils

import selector.common.RegistryLookup
import selector.example_sources._
import spray.json._
import selector.common.utils.MyJsonProtocol._
import selector.example_selectors._
import selector.labeled_examples.{AbstractLabeledExamples, ReadLabeledExample}

/**
  * Created by yizhouyan on 9/7/19.
  */
object ClassNameMapping {
    def mapDataTypeToClass(lookup: RegistryLookup): AbstractLabeledExamples = {
        val jsonAst = lookup.params.mkString.parseJson
        lookup.name match{
            case "ReadLabeledExample" => new ReadLabeledExample(jsonAst.convertTo[String])
        }
    }

    def mapClassNameToClass(lookup: RegistryLookup): Any = {
        val jsonAst = {
            if (lookup.params.isDefined)
                lookup.params.mkString.parseJson
            else
                "{}".parseJson
        }
        lookup.name match{
            case "AnomalyScore" => new AnomalyScore(jsonAst.convertTo[AnomalyScoreParams])
            case "KmeansClusters" => new KmeansClusters(jsonAst.convertTo[KmeansClustersParams])
            case "ActiveLearning" => new ActiveLearning(jsonAst.convertTo[ActiveLearningParams])
            case "Identity" => new Identity(jsonAst.convertTo[IdentityParams])
            case "SimilarityBasedSelector" => new SimilarityBasedSelector(jsonAst.convertTo[SimilaritySelectorParams])
        }
    }
}
