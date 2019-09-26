package selector.workflows

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import selector.common.MainWorkflowInput
import selector.common.utils.ConfigParser

import scala.io.Source
import spray.json._
import selector.common.utils.MyJsonProtocol._
import selector.example_sources.FetchExampleSources
import selector.labeled_examples.FetchLabeledExample

/**
  * Created by yizhouyan on 9/7/19.
  */
object ExampleSelector extends Logging{
    def main(args: Array[String]): Unit = {
        val configs: ConfigParser = new ConfigParser(args)
        val mainWorkflowInput: MainWorkflowInput = parseJson(configs.jsonFile)
        val spark = initializeSparkContext()
        // get labeled examples
        val labeledExamples = mainWorkflowInput.labeledExamples match {
            case Some(a) => FetchLabeledExample.fetch(a, spark)
            case None => spark.emptyDataFrame
        }
        // save to labeled example output table

        // look up examples and union them together
        val allData = FetchExampleSources.fetch(mainWorkflowInput.exampleSources, spark, labeledExamples)

        // save all data examples to output table

        //select the best examples


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
    private def parseJson(jsonPath: String): MainWorkflowInput = {
        val source: String = Source.fromFile(jsonPath).getLines.mkString
        val jsonAst = source.parseJson // or JsonParser(source)
        jsonAst.convertTo[MainWorkflowInput]
    }

}
