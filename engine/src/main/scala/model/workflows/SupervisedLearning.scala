package model.workflows

import utils.Utils._
import conf.InputConfigs
import model.common.utils.MyJsonProtocol._
import model.common.utils._
import model.common.{SupervisedWorkflowInput, utils}
import model.data.{FetchDataExample, FetchLabels}
import model.pipelines.Pipelines
import org.apache.spark.sql.SparkSession
import spray.json._
import model.common._

import org.apache.commons.configuration.{CompositeConfiguration, PropertiesConfiguration}

import scala.io.Source

/**
  * Created by yizhouyan on 9/5/19.
  */
object SupervisedLearning{
    def main(args: Array[String]): Unit = {
        val configs: utils.ConfigParser = new ConfigParser(args)
        val supervisedWorkflowInput: SupervisedWorkflowInput = parseJson(configs.jsonFile)
        val config: CompositeConfiguration = new CompositeConfiguration()
        config.addConfiguration(new PropertiesConfiguration(configs.confFile))

        implicit val spark: SparkSession = initializeSparkContext("supervised method")
        // read data from training
        val saveToDB: Boolean = config.getBoolean(InputConfigs.saveToDBConf, false)
        val runExplanations: Boolean = supervisedWorkflowInput.runExplanations
        val finalOutputPath: String = supervisedWorkflowInput.finalOutputPath match {
            case Some(x) => x
            case None => getRandomFilePath(InputConfigs.outputPathPrefixConf, "final_output")
        }
        implicit val sharedParams:SharedParams = new SharedParams(saveToDB, runExplanations, finalOutputPath,
            new ColumnTracking)

        val labeledData = FetchLabels.fetch(supervisedWorkflowInput.labeledData, spark)
        val examples = FetchDataExample.fetch(supervisedWorkflowInput.data)

        // execute pipeline stages
        Pipelines.fit(labeledData, examples, supervisedWorkflowInput.pipelines, false)

        //        val dataDF = spark.read.format("csv").option("header", "true").load("file:///Users/yizhouyan/PycharmProjects/anomaly_detection/notebooks/kdd_09_data.csv")
        //        dataDF.show(10)
        spark.stop()
    }

    private def parseJson(jsonPath: String): SupervisedWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[SupervisedWorkflowInput]
    }
}
