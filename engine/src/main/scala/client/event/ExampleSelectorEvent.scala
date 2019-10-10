package client.event

import anomalydetection.ModelStorageService.FutureIface
import client._
import com.twitter.util.Await
import org.apache.spark.sql.Dataset

/**
 * Event indicating that an ExampleSelector method transformed a DataFrame.
 *
 * @param estimator - The example selector doing the transforming.
 * @param inputDataframe - The input to the transformer.
 * @param outputDataframe - The output from the transformer.
 * @param labelDataframe - The label dataframe.
 * @param stageNum - Stage number
 */
case class ExampleSelectorEvent(estimator: IEvent,
                                inputDataframe: Dataset[_],
                                outputDataframe: Dataset[_],
                                labelDataframe: Dataset[_],
                                stageNum: Int
                               ) extends ModelStorageEvent {
  /**
   * Create the actual ExampleSelectorEvent.
   * @param mdbs - The syncer. Used to get the experiment run id.
   * @return The TransformEvent.
   */
  def makeEvent(mdbs: ModelStorageSyncer) = anomalydetection.ExampleSelectorEvent(
    SyncableDataFrame(inputDataframe),
    SyncableDataFrame(labelDataframe),
    SyncableDataFrame(outputDataframe),
    SyncableEstimator(estimator),
    experimentRunId = mdbs.experimentRun.id,
    stageNum
  )


  /**
   * Update object ID mappings based on response.
   * @param ter - The response.
   * @param mdbs - The syncer. This contains the object ID mappings.
   * @return The syncer.
   */
  def associate(ter: anomalydetection.ExampleSelectorEventResponse, mdbs: ModelStorageSyncer) = {
    mdbs.associateObjectAndId(inputDataframe, ter.oldDataFrameId)
            .associateObjectAndId(outputDataframe, ter.newDataFrameId)
            .associateObjectAndId(labelDataframe, ter.labelDataFrameId)
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
    val res = Await.result(client.storeExampleSelectorEvent(makeEvent(mdbs.get)))
    associate(res, mdbs.get)
  }
}