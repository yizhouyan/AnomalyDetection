package selector.workflows

import client.{ModelStorageSyncer, NewExperimentRun, NewOrExistingProject}
import conf.InputConfigs
import org.apache.commons.configuration.{CompositeConfiguration, PropertiesConfiguration}
import org.apache.spark.sql.SparkSession
import utils.Utils._
import selector.common.{MainWorkflowInput, SharedParams}
import selector.common.utils.ConfigParser

import scala.io.Source
import spray.json._
import selector.common.utils.MyJsonProtocol._
import selector.example_sources.FetchExampleSources
import selector.labeled_examples.FetchLabeledExample

/**
  * Created by yizhouyan on 9/7/19.
  */
object ExampleSelector{
    def main(args: Array[String]): Unit = {
        val configs: ConfigParser = new ConfigParser(args)
        val mainWorkflowInput: MainWorkflowInput = parseJson(configs.jsonFile)
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
        val allExamplesOutputFileName: String = mainWorkflowInput.allExamplesOutputFileName match {
            case Some(x) => x
            case None => getRandomFilePath(InputConfigs.outputPathPrefixConf, "all_examples")
        }
        val selectedExamplesOutputFileName: String = mainWorkflowInput.selectedExamplesOutputFileName match {
            case Some(x) => x
            case None => getRandomFilePath(InputConfigs.outputPathPrefixConf, "selected_examples")
        }
        val labeledExamplesOutputFileName: String = mainWorkflowInput.selectedExamplesOutputFileName match {
            case Some(x) => x
            case None => getRandomFilePath(InputConfigs.outputPathPrefixConf, "labeled_examples")
        }

        implicit val spark: SparkSession = initializeSparkContext("Example Selector")
        implicit val sharedParams: SharedParams = SharedParams(mainWorkflowInput.sharedFilePath,
                saveToDB, allExamplesOutputFileName,selectedExamplesOutputFileName,
                labeledExamplesOutputFileName
            )

        // get labeled examples
        val labeledExamples = mainWorkflowInput.labeledExamples match {
            case Some(a) => FetchLabeledExample.fetch(a, spark)
            case None => spark.emptyDataFrame
        }
        // save to labeled example output table

        // look up examples and union them together
        val allData = FetchExampleSources.fetch(mainWorkflowInput.exampleSources, labeledExamples)

        // save all data examples to output table

        //select the best examples

        //
        spark.stop()
    }

    private def parseJson(jsonPath: String): MainWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[MainWorkflowInput]
    }
}
