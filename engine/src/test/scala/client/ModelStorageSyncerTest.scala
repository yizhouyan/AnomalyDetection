package client

/**
  * Created by yizhouyan on 9/29/19.
  */
class ModelStorageSyncerTest extends org.scalatest.FunSuite {
    test("Create Model Storage Syncer") {
        ModelStorageSyncer.setSyncer(new ModelStorageSyncer(
            projectConfig = NewOrExistingProject("Demo",
                     "yizhouyan",
                     "Project to hold all models from the demo"
                   ),
                   experimentRunConfig = new NewExperimentRun
        ))
    }
}
