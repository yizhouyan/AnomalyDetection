package client.event

import anomalydetection.ModelStorageService.FutureIface
import client._
import com.twitter.util.Await
import org.apache.spark.sql.DataFrame

/**
  * Event indicating that a Transformer transformed a DataFrame.
  *
  * @param transformer - The transformer doing the transforming.
  * @param inputDataframe - The input to the transformer.
  * @param outputDataframe - The output from the transformer.
  * @param inputCols - Input Columns of the dataframe
  * @param outputCols - Output Columns of the dataframe
  * @param predictionCol - Column name that contains the prediction results
  * @param stageNum - Stage number (optional)
  */
case class TransformEvent(transformer: IModel,
                          inputDataframe: DataFrame,
                          outputDataframe: DataFrame,
                          inputCols: List[String],
                          outputCols: List[String],
                          predictionCol: String,
                          stageNum:Int
                         ) extends ModelStorageEvent {
    /**
      * Create the actual TransformEvent.
      * @param mdbs - The syncer. Used to get the experiment run id.
      * @return The TransformEvent.
      */
    def makeEvent(mdbs: ModelStorageSyncer) = anomalydetection.TransformEvent(
        SyncableDataFrame(inputDataframe),
        SyncableDataFrame(outputDataframe),
        SyncableModel(transformer),
        inputCols,
        outputCols,
        predictionCol,
        experimentRunId = mdbs.experimentRun.id,
        stageNum
    )


    /**
      * Update object ID mappings based on response.
      * @param ter - The response.
      * @param mdbs - The syncer. This contains the object ID mappings.
      * @return The syncer.
      */
    def associate(ter: anomalydetection.TransformEventResponse, mdbs: ModelStorageSyncer) = {
        mdbs.associateObjectAndId(inputDataframe, ter.oldDataFrameId)
                .associateObjectAndId(outputDataframe, ter.newDataFrameId)
                .associateObjectAndId(transformer, ter.modelId)
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
        val res = Await.result(client.storeTransformEvent(makeEvent(mdbs.get)))
        associate(res, mdbs.get)
    }
}