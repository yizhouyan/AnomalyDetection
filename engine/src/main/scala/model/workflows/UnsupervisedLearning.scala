package model.workflows

import client.{ModelStorageSyncer, NewExperimentRun, NewOrExistingProject, SyncableDataFramePaths}
import conf.InputConfigs
import model.common.{UnsupervisedWorkflowInput, utils}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import model.common.utils._
import spray.json._
import model.common.utils.MyJsonProtocol._
import model.data.FetchDataExample
import model.pipelines.Pipelines
import model.utils.Utils
import org.apache.commons.configuration.{CompositeConfiguration, PropertiesConfiguration}

import scala.io.Source
/**
 * Created by yizhouyan on 9/5/19.
 */
object UnsupervisedLearning{
    def main(args: Array[String]): Unit = {
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
        implicit val saveToDB: Boolean = config.getBoolean(InputConfigs.saveToDBConf, false)
        implicit val spark: SparkSession = initializeSparkContext()
        implicit val finalOutputPath: String = unsupervisedWorkflowInput.finalOutputPath match {
            case Some(x) => x
            case None => Utils.getRandomFilePath(InputConfigs.outputPathPrefixConf, "final_output")
        }
        // read data from training
        val data = FetchDataExample.fetch(unsupervisedWorkflowInput.examples)
        println(SyncableDataFramePaths.getPath(data))
        import spark.implicits._
        // execute pipeline stages
        Pipelines.transform(
            data,
            unsupervisedWorkflowInput.pipelines,
            false
        )
        spark.stop()
    }


    private def initializeSparkContext(): SparkSession = {
        val conf = new SparkConf().setAppName("UnsupervisedLearning")
        val spark = SparkSession
                .builder()
                .master("local")  //"spark://localhost:7077"
                .getOrCreate()
        spark
    }
    private def parseJson(jsonPath: String): UnsupervisedWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[UnsupervisedWorkflowInput]
    }
}
