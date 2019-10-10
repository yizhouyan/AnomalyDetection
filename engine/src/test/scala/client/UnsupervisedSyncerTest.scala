package client

import client.event.UnsupervisedEvent
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/29/19.
  */
class UnsupervisedSyncerTest extends org.scalatest.FunSuite {
    class testEvent extends IEvent{
        override def getName(): String = "Test Event for Unsupervised"

        override def getHyperParameters(): mutable.Map[Any, Any] = {
            mutable.Map("params_1" -> 1, "params_2" -> "params_2", "params_3" -> 5.0)
        }
    }

    test("Create Unsupervised Event"){
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject("Demo",
                "yizhouyan",
                "Project to hold all models from the demo"
            ),
            experimentRunConfig = new NewExperimentRun
        ))

        val unsupervised = new testEvent()
        val conf = new SparkConf().setAppName("TestUnsupervisedEvent")
        val spark = SparkSession
                .builder()
                .master("local")  //"spark://localhost:7077"
                .getOrCreate()
        import spark.implicits._
        val inputDataframe = Seq(
            (8, "bat"),
            (64, "mouse"),
            (-27, "horse")
        ).toDF("number_input", "word_input")
        val outputDataframe = Seq(
            (0, "bat"),
            (2, "mouse"),
            (3, "horse")
        ).toDF("number_output", "word_output")
        SyncableDataFramePaths.setPath(inputDataframe, "input_path")
        SyncableDataFramePaths.setPath(outputDataframe, "output_path")
        val inputCols = List("number_input", "word_input")
        val outputCols = List("number_output", "word_output")
        val unsupervisedEvent = UnsupervisedEvent(unsupervised,
            inputDataframe,
            outputDataframe,
            inputCols,
            outputCols,
            1
        ).sync(ModelStorageSyncer.syncer.get.client.get, Some(ModelStorageSyncer.syncer.get))
        spark.close()
    }
}
