package client.event

import anomalydetection.ModelStorageService.FutureIface
import client._
import com.twitter.util.Await
import org.apache.spark.sql.{Dataset}

/**
  * Event indicating that a Unsupervised method transformed a DataFrame.
  *
  * @param estimator - The unsupervised estimator doing the transforming.
  * @param inputDataframe - The input to the transformer.
  * @param outputDataframe - The output from the transformer.
  * @param inputCols - Input Columns of the dataframe
  * @param outputCols - Output Columns of the dataframe
  * @param stageNum - Stage number (optional)
  */
case class UnsupervisedEvent(estimator: IEvent,
                             inputDataframe: Dataset[_],
                             outputDataframe: Dataset[_],
                             inputCols: List[String],
                             outputCols: List[String],
                             stageNum: Int
                         ) extends ModelStorageEvent {
  /**
    * Create the actual TransformEvent.
    * @param mdbs - The syncer. Used to get the experiment run id.
    * @return The TransformEvent.
    */
  def makeEvent(mdbs: ModelStorageSyncer) = anomalydetection.UnsupervisedEvent(
    SyncableDataFrame(inputDataframe),
    SyncableDataFrame(outputDataframe),
    SyncableEstimator(estimator),
    inputCols,
    outputCols,
    experimentRunId = mdbs.experimentRun.id,
    stageNum
  )


  /**
    * Update object ID mappings based on response.
    * @param ter - The response.
    * @param mdbs - The syncer. This contains the object ID mappings.
    * @return The syncer.
    */
  def associate(ter: anomalydetection.UnsupervisedEventResponse, mdbs: ModelStorageSyncer) = {
    mdbs.associateObjectAndId(inputDataframe, ter.oldDataFrameId)
      .associateObjectAndId(outputDataframe, ter.newDataFrameId)
      .associateObjectAndId(estimator, ter.specId)
      .associateObjectAndId(this, ter.eventId)
  }

  /**
    * Store the event and update object ID mappings.
    * @param client - The client that exposes the functions that we
    *               call to store objects in the ModelStorage.
    * @param mdbs - The ModelStorageSyncer, included so we can update the ID
    *             mappings after syncing.
    */
  override def sync(client: FutureIface, mdbs: Option[ModelStorageSyncer]): Unit = {
    val res = Await.result(client.storeUnsupervisedEvent(makeEvent(mdbs.get)))
    associate(res, mdbs.get)
  }
}