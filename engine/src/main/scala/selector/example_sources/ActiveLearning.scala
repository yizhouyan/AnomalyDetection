package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */
case class ActiveLearningParams(outputColName: Option[String])

class ActiveLearning(activeLearningParams: ActiveLearningParams) extends AbstractExampleSource{
    override def name(): String = {
        activeLearningParams.outputColName match{
            case Some(a) => a
            case None => "active_learning"
        }
    }

    override def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Active Learning: " + name())
        spark.emptyDataFrame
    }
}