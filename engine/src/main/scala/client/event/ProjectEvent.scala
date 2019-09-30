package client.event

import com.twitter.util.Await
import anomalydetection.ModelStorageService.FutureIface
import client.ModelStorageSyncer

/**
  * Created by yizhouyan on 9/27/19.
  * Stores a new project on the server.
  * @param project - The project to store.
  */
case class ProjectEvent(project: anomalydetection.Project) extends ModelStorageEvent{
    /**
      * Store this project on the ModelDb.
      *
      * @param client - The client that exposes the functions that we
      *               call to store objects in the ModelStorage.
      * @param mdbs   - The ModelStorageSyncer, included so we can update the ID
      *               mappings after syncing.
      */
    override def sync(client: FutureIface, mdbs: Option[ModelStorageSyncer]): Unit = {
        val res = Await.result(client.storeProjectEvent(anomalydetection.ProjectEvent(project)))
        mdbs.get.project = mdbs.get.project.copy(id = res.projectId)
    }
}
