package model.common.utils

import model.common.{CustomizedFile, RegistryLookup}
import model.pipelines.unsupervised._
import spray.json._
import model.common.utils.MyJsonProtocol._
import model.data.{AbstractData, ReadDataFile}
import model.pipelines.AbstractEstimator
import model.pipelines.unsupervised.examples.iforest.{IsolationForest, IsolationForestParams}
import model.pipelines.unsupervised.examples.{KNNBasedDetection, KNNBasedDetectionParams, LOF, LOFParams, Mahalanobis, MahalanobisParams, StandardScaler, StandardScalerParams}

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

    def mapClassNameToClass(lookup: RegistryLookup, stageNum: Int = -1): AbstractEstimator = {
        val jsonAst = lookup.params.mkString.parseJson
        lookup.name match{
            case "StandardScaler" => new StandardScaler(jsonAst.convertTo[StandardScalerParams], stageNum)
            case "IsolationForest" => new IsolationForest(jsonAst.convertTo[IsolationForestParams], stageNum)
            case "KNNBasedDetection" => new KNNBasedDetection(jsonAst.convertTo[KNNBasedDetectionParams], stageNum)
            case "LOF" => new LOF(jsonAst.convertTo[LOFParams], stageNum)
            case "Mahalanobis" => new Mahalanobis(jsonAst.convertTo[MahalanobisParams], stageNum)
        }
    }
}
