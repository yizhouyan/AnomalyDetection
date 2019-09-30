package client.event

import anomalydetection.ModelStorageService.FutureIface
import client.ModelStorageSyncer
/**
  * Created by yizhouyan on 9/27/19.
  * Events that can be synced to the ModelStorageServer must subclass this class.
  */
abstract class ModelStorageEvent {
    /**
      * Store this event on the ModelDb.
      *
      * @param client - The client that exposes the functions that we
      *               call to store objects in the ModelStorage.
      * @param mdbs - The ModelStorageSyncer, included so we can update the ID
      *             mappings after syncing.
      */
    def sync(client: FutureIface, mdbs: Option[ModelStorageSyncer]): Unit
}
