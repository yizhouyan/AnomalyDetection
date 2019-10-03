package model.pipelines

import client.{IEvent, ModelStorageSyncer}
import client.event.UnsupervisedEvent
import model.common.LabeledExamples
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by yizhouyan on 9/7/19.
 */
abstract class AbstractTransformer extends AbstractEvent{
    override def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean) : Any= {}
    def saveTransformerToDB()(implicit mdbs: Option[ModelStorageSyncer]): Unit ={

    }
    def saveUnsupervisedToDB(unsupervised: IEvent,
                             inputDataframe: DataFrame,
                             outputDataframe: DataFrame,
                             inputCols: List[String],
                             outputCols: List[String],
                             predictionCol: String,
                             stageNum: Int)
                            (implicit mdbs: Option[ModelStorageSyncer]): Unit= {
        UnsupervisedEvent(unsupervised,
            inputDataframe,
            outputDataframe,
            inputCols,
            outputCols,
            predictionCol,
            stageNum
        ).sync(mdbs.get.client.get, Some(mdbs.get))
    }
}
