package client
import client.event.{TransformEvent, UnsupervisedEvent}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable
import anomalydetection.ModelStorageService.FutureIface
/**
  * Created by yizhouyan on 9/29/19.
  */
class TransformSyncerTest extends org.scalatest.FunSuite {
    class TestModel(filepath: String) extends IModel{
        override def getName(): String = "Test Event"

        override def getFilePath(): String = filepath
    }

    test("Create Transfrom Event"){
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject("Demo",
                "yizhouyan",
                "Project to hold all models from the demo"
            ),
            experimentRunConfig = new NewExperimentRun
        ))

        val transformer = new TestModel("model_path_1")
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
            3
        ).sync(ModelStorageSyncer.syncer.get.client.get, Some(ModelStorageSyncer.syncer.get))
        spark.close()
    }
}
