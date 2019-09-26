package model.workflows

import model.common.{UnsupervisedWorkflowInput, utils}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import model.common.utils._
import spray.json._
import model.common.utils.MyJsonProtocol._
import model.data.FetchDataExample
import model.pipelines.Pipelines

import scala.io.Source
/**
  * Created by yizhouyan on 9/5/19.
  */
object UnsupervisedLearning extends Logging{
    def main(args: Array[String]): Unit = {
        val configs: utils.ConfigParser = new ConfigParser(args)
        val unsupervisedWorkflowInput: UnsupervisedWorkflowInput = parseJson(configs.jsonFile)
        val spark = initializeSparkContext()
        // read data from training
        val data = FetchDataExample.fetch(unsupervisedWorkflowInput.examples, spark)

        import spark.implicits._
        // execute pipeline stages
        Pipelines.transform(
            data,
            unsupervisedWorkflowInput.pipelines,
            false,
            spark
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
