package client

import client.event.{FitEvent, UnsupervisedEvent}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/29/19.
  */
class FitSyncerTest extends org.scalatest.FunSuite {
    class TestFitEvent extends IEvent{
        override def getName(): String = "Test Event for Fit"

        override def getHyperParameters(): mutable.Map[Any, Any] = {
            mutable.Map("params_1" -> 1, "params_2" -> "params_2", "params_3" -> 5.0)
        }
    }
    class TestModel(filePath: String) extends IModel{
        override def getName(): String = "model for test"

        override def getFilePath(): String = filePath
    }

    test("Create Unsupervised Event"){
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject("Demo",
                "yizhouyan",
                "Project to hold all models from the demo"
            ),
            experimentRunConfig = new NewExperimentRun
        ))

        val estimator = new TestFitEvent()
        val model = new TestModel("model_generated_by_fit")
        val conf = new SparkConf().setAppName("TestUnsupervisedEvent")
        val spark = SparkSession
                .builder()
                .master("local")  //"spark://localhost:7077"
                .getOrCreate()
        import spark.implicits._
        val inputDataframe = Seq(
            (8, "bat", 1),
            (64, "mouse", 1),
            (-27, "horse", 0)
        ).toDF("number_input", "word_input", "label")
        SyncableDataFramePaths.setPath(inputDataframe, "input_path")
        val featureCols = List("number_input", "word_input")
        val labelCols = List("label")
        FitEvent(estimator,
            inputDataframe,
            featureCols,
            labelCols,
            model,
            1
        ).sync(ModelStorageSyncer.syncer.get.client.get, Some(ModelStorageSyncer.syncer.get))

        spark.close()
    }
}
