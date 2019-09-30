package model.common.utils

import model.common.{CustomizedFile, RegistryLookup}
import model.pipelines.unsupervised._
import spray.json._
import model.common.utils.MyJsonProtocol._
import model.data.{AbstractData, ReadDataFile}
import model.pipelines.AbstractEvent

/**
  * Created by yizhouyan on 9/7/19.
  */
object ClassNameMapping {
    def mapDataTypeToClass(lookup: RegistryLookup): AbstractData = {
        val jsonAst = lookup.params.mkString.parseJson
        lookup.name match{
            case "ReadDataFile" => new ReadDataFile(jsonAst.convertTo[CustomizedFile])
        }
    }

    def mapClassNameToClass(lookup: RegistryLookup): AbstractEvent = {
        val jsonAst = lookup.params.mkString.parseJson
        lookup.name match{
            case "StandardScaler" => new StandardScaler(jsonAst.convertTo[StandardScalerParams])
            case "IsolationForest" => new IsolationForest(jsonAst.convertTo[IsolationForestParams])
            case "KNNBasedDetection" => new KNNBasedDetection(jsonAst.convertTo[KNNBasedDetectionParams])
            case "LOF" => new LOF(jsonAst.convertTo[LOFParams])
            case "Mahalanobis" => new Mahalanobis(jsonAst.convertTo[MahalanobisParams])
        }
    }
}
