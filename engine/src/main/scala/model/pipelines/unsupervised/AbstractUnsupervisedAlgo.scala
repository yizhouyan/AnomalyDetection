package model.pipelines.unsupervised

import client.event.UnsupervisedEvent
import client.{IEvent, ModelStorageSyncer}
import model.common.LabeledData
import model.pipelines.AbstractEstimator
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by yizhouyan on 9/7/19.
 */
abstract class AbstractUnsupervisedAlgo extends AbstractEstimator{
    override def fit(labels: Dataset[LabeledData], features: DataFrame, runExplanations: Boolean) : Any= {}
    def saveTransformerToDB()(implicit mdbs: Option[ModelStorageSyncer]): Unit ={

    }
    def saveUnsupervisedToDB(unsupervised: IEvent,
                             inputDataframe: Dataset[_],
                             outputDataframe: Dataset[_],
                             inputCols: List[String],
                             outputCols: List[String],
                             stageNum: Int)
                            (implicit mdbs: Option[ModelStorageSyncer]): Unit= {
        UnsupervisedEvent(unsupervised,
            inputDataframe,
            outputDataframe,
            inputCols,
            outputCols,
            stageNum
        ).sync(mdbs.get.client.get, Some(mdbs.get))
    }
}
