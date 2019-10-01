package client.event

import anomalydetection.ModelStorageService.FutureIface
import client._
import com.twitter.util.Await
import org.apache.spark.sql.DataFrame

/**
  * Event indicating that an Estimator was used to fit a model.
  * @param estimator  - The estimator performing the fitting.
  * @param dataframe - The data being fit.
  * @param featureCols - The feature columns
  * @param labelCols - The label columns
  * @param model - trained model object
  * @param stageNum - stage number
  */
case class FitEvent(estimator: IEvent,
                    dataframe: DataFrame,
                    featureCols: List[String],
                    labelCols: List[String],
                    model: IModel,
                    stageNum:Int
                   ) extends ModelStorageEvent {

    def makeEvent(mdbs: ModelStorageSyncer) = {
        anomalydetection.FitEvent(
            SyncableDataFrame(dataframe),
            SyncableEstimator(estimator),
            SyncableModel(model),
            featureCols,
            labelCols,
            experimentRunId = mdbs.experimentRun.id,
            stageNum
        )
    }

    def associate(fer: anomalydetection.FitEventResponse, mdbs: ModelStorageSyncer) = {
        mdbs.associateObjectAndId(dataframe, fer.dfId)
                .associateObjectAndId(estimator, fer.specId)
                .associateObjectAndId(model, fer.modelId)
                .associateObjectAndId(this, fer.eventId)
    }
    override def sync(client: FutureIface, mdbs: Option[ModelStorageSyncer]): Unit = {
        val res = Await.result(client.storeFitEvent(makeEvent(mdbs.get)))
        associate(res, mdbs.get)
    }
}