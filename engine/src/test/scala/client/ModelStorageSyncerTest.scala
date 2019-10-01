package client
import client.event.{TransformEvent, UnsupervisedEvent}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable
import anomalydetection.ModelStorageService.FutureIface
/**
  * Created by yizhouyan on 9/29/19.
  */
class ModelStorageSyncerTest extends org.scalatest.FunSuite {
    class testEvent extends IEvent{
        override def getName(): String = "Test Event"

        override def getHyperParameters(): mutable.Map[Any, Any] = {
            mutable.Map("params_1" -> 1, "params_2" -> "params_2", "params_3" -> 5.0)
        }
    }
    test("Create Syncer"){
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject("Demo",
                "yizhouyan",
                "Project to hold all models from the demo"
            ),
            experimentRunConfig = new NewExperimentRun
        ))
    }

    test("Create Transfrom Event"){
        val transformer = new testEvent()
        val conf = new SparkConf().setAppName("TestTransformEvent")
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
        val transformEvent = TransformEvent(transformer,
            inputDataframe,
            outputDataframe,
            inputCols,
            outputCols,
            "word_output",
            "output_model_path",
            1
        ).sync(ModelStorageSyncer.syncer.get.client.get, Some(ModelStorageSyncer.syncer.get))
        spark.close()
    }

    test("Create Unsupervised Event"){
        val transformer = new testEvent()
        val conf = new SparkConf().setAppName("TestTransformEvent")
        val spark = SparkSession
                .builder()
                .master("local")  //"spark://localhost:7077"
                .getOrCreate()
        import spark.implicits._
        val inputDataframe = Seq(
            (8, "bat"),
            (64, "mouse"),
            (-27, "horse")
        ).toDF("unsupervised_input", "unsupervised_word_input")
        val outputDataframe = Seq(
            (0, "bat"),
            (2, "mouse"),
            (3, "horse")
        ).toDF("unsupervised_output", "unsupervised_word_output")
        SyncableDataFramePaths.setPath(inputDataframe, "input_path")
        SyncableDataFramePaths.setPath(outputDataframe, "output_path")
        val inputCols = List("unsupervised_input", "unsupervised_word_input")
        val outputCols = List("unsupervised_output", "unsupervised_word_output")
        val unsupervisedEvent = UnsupervisedEvent(transformer,
            inputDataframe,
            outputDataframe,
            inputCols,
            outputCols,
            "word_output",
            0
        ).sync(ModelStorageSyncer.syncer.get.client.get, Some(ModelStorageSyncer.syncer.get))
        spark.close()
    }

}
