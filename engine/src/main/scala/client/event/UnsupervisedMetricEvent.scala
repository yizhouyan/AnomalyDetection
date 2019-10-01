package client.event

import anomalydetection.ModelStorageService.FutureIface
import client.{IModel, ModelStorageSyncer, SyncableDataFrame, SyncableModel}
import com.twitter.util.Await
import org.apache.spark.sql.DataFrame

/**
  * Event indicating that a metric has been computed for a model's prediction.
  *
  * @param dataframe - The DataFrame containing the predictions.
  * @param model - The model that made the predictions.
  * @param labelCol - The column of the true label.
  * @param predictionCol - The column of the prediction label.
  * @param metricType - The choice of metric.
  * @param metricValue - The value of the metric.
  */
case class UnsupervisedMetricEvent(dataframe: DataFrame,
                                   model: IModel,
                                   labelCol: String,
                                   predictionCol: String,
                                   metricType: String,
                                   metricValue: Float) extends ModelStorageEvent {

  /**
    * Store the metric event on the server and do object-ID mappings.
    * @param client - The client that exposes the functions that we
    *               call to store objects in the Model Storage.
    * @param mdbs - The ModelStorageSyncer, included so we can update the ID
    *             mappings after syncing.
    */
  override def sync(client: FutureIface, mdbs: Option[ModelStorageSyncer]): Unit = {
    val res = Await.result(client.storeModelMetricEvent(
      anomalydetection.ModelMetricEvent(
        SyncableDataFrame(dataframe),
        SyncableModel(model),
        metricType,
        metricValue,
        labelCol,
        predictionCol,
        experimentRunId = mdbs.get.experimentRun.id
      )
    ))

    // Update object-ID mappings.
    mdbs.get.associateObjectAndId(dataframe, res.dfId)
      .associateObjectAndId(model, res.modelId)
      .associateObjectAndId(this, res.eventId)
  }
}


