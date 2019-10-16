package selector.workflows

import client.{ModelStorageSyncer, NewExperimentRun, NewOrExistingProject, SyncableDataFrame, SyncableDataFramePaths}
import conf.InputConfigs
import org.apache.commons.configuration.{CompositeConfiguration, PropertiesConfiguration}
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}
import utils.Utils._
import selector.common.{LabeledExample, MainWorkflowInput, SharedParams}
import selector.common.utils.ConfigParser

import scala.io.Source
import spray.json._
import selector.common.utils.MyJsonProtocol._
import selector.example_selectors.SelectExamples
import selector.example_sources.FetchExampleSources
import selector.labeled_examples.FetchLabeledExample

/**
  * Created by yizhouyan on 9/7/19.
  */
object ExampleSelector{
    val logger = Logger.getLogger(ExampleSelector.getClass)
    def main(args: Array[String]): Unit = {
        implicit val spark: SparkSession = initializeSparkContext("Example Selector")
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
        val numFeaturesInData: Int = mainWorkflowInput.numFeaturesInData match{
            case Some(x) => x
            case None => 0
        }
        implicit val sharedParams: SharedParams = SharedParams(mainWorkflowInput.sharedFilePath,
                saveToDB, allExamplesOutputFileName,selectedExamplesOutputFileName, numFeaturesInData
            )
        import spark.implicits._
        // get labeled examples
        logger.info("Get labeled examples...")
        val labeledExamples = mainWorkflowInput.labeledExamples match {
            case Some(a) => FetchLabeledExample.fetch(a)
            case None => spark.emptyDataset[LabeledExample]
        }

        // look up examples and union them together
        logger.info("Start gathering examples from different sources...")
        val allData = FetchExampleSources.fetch(mainWorkflowInput.exampleSources, labeledExamples)
        val allDataCount = allData.count()
        logger.info("All data size: " + allDataCount)
        if(sharedParams.saveToDB)
            SyncableDataFramePaths.setPath(allData.toDF, sharedParams.allExamplesOutputFileName)
        // save all data examples to output file
        logger.info("Write all data examples to output file...")
        allData.coalesce(math.ceil(allDataCount/5000.0).toInt).write.mode(SaveMode.Overwrite).parquet(sharedParams.allExamplesOutputFileName)

        //select the best examples
        logger.info("Select examples from all data...")
        val selectedData = SelectExamples.fetch(mainWorkflowInput.exampleSelector, allData, labeledExamples)
        val selectedDataCount = selectedData.count()
        logger.info("Selected data size: " + selectedDataCount)
        selectedData.show(10, false)
        // save selected examples to output file
        selectedData.coalesce(math.ceil(selectedDataCount/5000.0).toInt)
                .write.mode(SaveMode.Overwrite)
                .parquet(sharedParams.selectedExamplesOutputFileName)
        spark.stop()
    }

    private def parseJson(jsonPath: String): MainWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[MainWorkflowInput]
    }
}
