package model.workflows

import utils.Utils._
import client.{ModelStorageSyncer, NewExperimentRun, NewOrExistingProject}
import conf.InputConfigs
import model.common.utils.ConfigParser
import model.common.{SharedParams, UnsupervisedWorkflowInput, utils}
import org.apache.spark.sql.SparkSession
import spray.json._
import model.common.utils.MyJsonProtocol._
import model.data.FetchDataExample
import model.pipelines.Pipelines
import org.apache.commons.configuration.{CompositeConfiguration, PropertiesConfiguration}

import scala.io.Source
/**
 * Created by yizhouyan on 9/5/19.
 */
object UnsupervisedLearning{
    def main(args: Array[String]): Unit = {
        implicit val spark: SparkSession = initializeSparkContext("UnsupervisedLearning")
        val configs: utils.ConfigParser = new ConfigParser(args)
        val unsupervisedWorkflowInput: UnsupervisedWorkflowInput = parseJson(configs.jsonFile)
        val config: CompositeConfiguration = new CompositeConfiguration()
        config.addConfiguration(new PropertiesConfiguration(configs.confFile))

        // create Model Storage Syncer
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject(
                config.getString(InputConfigs.projectNameConf, "Demo"),
                config.getString(InputConfigs.userNameConf, "yizhouyan"),
                config.getString(InputConfigs.projectDescConf,
                    "Project to hold all models from the demo")
            ),
            experimentRunConfig = new NewExperimentRun
        ))

        val saveToDB: Boolean = config.getBoolean(InputConfigs.saveToDBConf, false)
        val finalOutputPath: String = unsupervisedWorkflowInput.finalOutputPath match {
            case Some(x) => x
            case None => getRandomFilePath(InputConfigs.outputPathPrefixConf, "final_output")
        }
        val runExplanations: Boolean = unsupervisedWorkflowInput.runExplanations
        implicit val sharedParams:SharedParams = new SharedParams(saveToDB, runExplanations, finalOutputPath)

        // read data from training
        val data = FetchDataExample.fetch(unsupervisedWorkflowInput.data)
        sharedParams.numPartitions = unsupervisedWorkflowInput.numPartitions match{
            case Some(x) => x
            case None =>{
                math.ceil(data.count()/5000.0).toInt
            }
        }
        // execute pipeline stages
        Pipelines.transform(
            data,
            unsupervisedWorkflowInput.pipelines
        )
        spark.stop()
    }

    private def parseJson(jsonPath: String): UnsupervisedWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[UnsupervisedWorkflowInput]
    }
}
