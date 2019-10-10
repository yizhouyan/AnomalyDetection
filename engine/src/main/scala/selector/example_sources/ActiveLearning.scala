package selector.example_sources

import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}

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

    override def fetch(labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
        import spark.implicits._
        println("Active Learning: " + name())
        spark.emptyDataset[Example]
    }
}
