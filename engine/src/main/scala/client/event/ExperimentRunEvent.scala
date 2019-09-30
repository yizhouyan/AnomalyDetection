package client.event
import anomalydetection.ModelStorageService
import client.ModelStorageSyncer
import com.twitter.util.Await

/**
  * Created by yizhouyan on 9/27/19.
  * Store a new experiment run on the server.
  * @param experimentRun - The experiment run to store.
  */
case class ExperimentRunEvent(experimentRun: anomalydetection.ExperimentRun) extends ModelStorageEvent {
    /**
      * Store this event on the ModelDb.
      *
      * @param client - The client that exposes the functions that we
      *               call to store objects in the ModelStorage.
      * @param mdbs   - The ModelStorageSyncer, included so we can update the ID
      *               mappings after syncing.
      */
    override def sync(client: ModelStorageService.FutureIface, mdbs: Option[ModelStorageSyncer]): Unit = {
        val res = Await.result(client.storeExperimentRunEvent(anomalydetection.ExperimentRunEvent(experimentRun)))
        mdbs.get.experimentRun = mdbs.get.experimentRun.copy(id = res.experimentRunId)
    }
}
