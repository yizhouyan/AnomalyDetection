package client

/**
  * Created by yizhouyan on 9/27/19.
  * Revised based on code from ModelDB: https://github.com/mitdbg/modeldb
  */
import java.util.UUID

import anomalydetection.ModelStorageService.FutureIface
import anomalydetection._
import client.event.{ExperimentRunEvent, ProjectEvent}
import client.util.Timer
import com.twitter.finagle.Thrift
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Duration}

/**
  * Represents a project configuration for the syncer to use.
  * @param id - The ID of a project.
  */
abstract class ProjectConfig(val id: Int)

/**
  * Indicates that the syncer should use an existing project with the given ID.
  * @param id - The ID of a project.
  */
case class ExistingProject(override val id: Int) extends ProjectConfig(id)

/**
  * Indicates that the syncer should create a new project.
  * @param name - The name of the new project.
  * @param author - The author of the new project.
  * @param description - The description of the new project.
  */
case class NewOrExistingProject(name: String, author: String, description: String) extends ProjectConfig(-1)

/**
  * Indicates that the project is unspecified.
  */
private case class UnspecifiedProject() extends ProjectConfig(-1)

/**
  * Represents the configuration of the experiment run used by the syncer.
  * @param id - The ID of the experiment run.
  */
abstract class ExperimentRunConfig(val id: Int)

/**
  * Indicates that the syncer will use an existing experiment run with the given ID.
  * @param id - The ID of the experiment run.
  */
case class ExistingExperimentRun(override val id: Int) extends ExperimentRunConfig(id)

/**
  * Indicates that the syncer will create a new experiment run.
  * @param description - The description of the experiment run.
  */
case class NewExperimentRun(description: String="") extends ExperimentRunConfig(-1)

/**
  * This is the syncer that is responsible for storing events in the Model Storage.
  *
  * @param hostPortPair - The hostname and port of the ModelDB Server Thrift API.
  * @param projectConfig - The desired project configuration.
  * @param experimentRunConfig - The desired experiment run configuration
  */
class ModelStorageSyncer(var hostPortPair: Option[(String, Int)] = Some("localhost", 6543),
                    var projectConfig: ProjectConfig = new UnspecifiedProject,
                    var experimentRunConfig: ExperimentRunConfig = new NewExperimentRun) {
    /**
      * Configure this syncer with the configuration from JSON.
      * @param conf - The JSON configuration. You can create this by doing SyncerConfig(pathToFile).
      */
    def this(conf: SyncerConfigJson) {
        this(
            hostPortPair = Some(conf.thrift.host, conf.thrift.port),
            projectConfig = NewOrExistingProject(
                conf.project.name,
                conf.project.author,
                conf.project.description
            ),
            experimentRunConfig = NewExperimentRun(
                conf.experimentRun.description
            )
        )
    }

    /**
      * This is the Thrift client that is responsible for talking to the Model Storage server.
      */
    val client: Option[FutureIface] = hostPortPair match {
        case Some((host, port)) =>  Some(Thrift.client.
                configured(Transport.Liveness(keepAlive=Some(true),readTimeout = Duration.Top, writeTimeout = Duration.Top)).
                newIface[ModelStorageService.FutureIface](s"$host:$port"))
        case None => None
    }

    /**
      * We keep a mapping between objects and their IDs.
      */
    val objectIdMappings = new TwoWayMapping[Int, Any]

    /**
      * We keep a mapping between objects and their tags. Tags are short, user-given names.
      */
    val objectTagMappings = new TwoWayMapping[String, Any]

    /**
      * We will map DataFrames to the contents of their feature vectors.
      */
//    val featureTracker = new FeatureTracker


    /**
      * Associates object and ID.
      * @param obj - object.
      * @param id - ID.
      * @return The syncer.
      */
    def associateObjectAndId(obj: Any, id: Int): ModelStorageSyncer = {
        objectIdMappings.putVK(obj, id)
        this
    }

    /**
      * Associates object and ID.
      * @param id - The ID.
      * @param obj - The object.
      * @return The syncer.
      */
    def associateObjectAndId(id: Int, obj: Any): ModelStorageSyncer = associateObjectAndId(obj, id)

    /**
      * Get the ID of a given object.
      * @param obj - The object.
      * @return The ID of the given object, or None if the object cannot be found.
      */
    def id(obj: Any): Option[Int] = objectIdMappings.getV(obj)

    /**
      * Get the object associated with the given ID.
      * @param id -  The ID.
      * @return The object assocaited with the given ID, or None if there's no object with the given ID.
      */
    def objectForId(id: Int): Option[Any] = objectIdMappings.getK(id)

    /**
      * Associate an object with a tag.
      * @param obj - The object.
      * @param tag - The tag, which is a short textual description to associate with the object.
      * @return The syncer.
      */
    def associateObjectAndTag(obj: Any, tag: String): ModelStorageSyncer = {
        objectTagMappings.putVK(obj, tag)
        this
    }

    /**
      * Associate an object with a tag.
      * @param tag - The tag, which is a short textual description to associate with the object.
      * @param obj - The object.
      * @return The syncer.
      */
    def associateObjectAndTag(tag: String, obj: Any): ModelStorageSyncer = associateObjectAndTag(obj, tag)

    /**
      * Get the tag associated with a given object.
      * @param obj - The object.
      * @return The tag associated with the object, or None if the object has no tag.
      */
    def tag(obj: Any): Option[String] = objectTagMappings.getV(obj)

    /**
      * Get the object associated with the given tag.
      * @param tag - The tag.
      * @return The object associated with the tag, or None if the object has no tag.
      */
    def objectForTag(tag: String): Option[Any] = objectTagMappings.getK(tag)


//    /**
//      * Load the Transformer, with the given ID, from the server and cast it to type T.
//      * @param id - The ID of the Transformer.
//      * @param reader - The reader that is responsible for reading from the model filesystem and converting the data
//      *               into an object of type T.
//      * @tparam T - The type of the Transformer.
//      * @return The deserialized Transformer, or None if there is no serialized model file for the Transformer with the
//      *         given ID.
//      */
//    def load[T](id: Int, reader: MLReader[T]): Option[T] = {
//        val path = Await.result(client.get.pathForTransformer(id))
//        if (path == null)
//            None
//        else
//            Some(reader.load(path))
//    }

    // Set up the project.
    var project = projectConfig match {
        case ExistingProject(id) => anomalydetection.Project(id, "", "", "")
        case np: NewOrExistingProject => anomalydetection.Project(np.id, np.name, np.author, np.description)
        case UnspecifiedProject() => anomalydetection.Project(-1, "", "", "")
    }

    // If it's a new project, buffer a ProjectEvent.
    projectConfig match {
        case np: NewOrExistingProject =>
            Timer.time("Syncing Project Event")(ProjectEvent(project).sync(client.get, Some(this)))
    }

    // Set up the experiment run.
    var experimentRun = experimentRunConfig match {
        case ExistingExperimentRun(id) => anomalydetection.ExperimentRun(id, project.id, "")
        case ner: NewExperimentRun =>
            anomalydetection.ExperimentRun(ner.id,
                project.id,
                if (ner.description == "")
                    "Experiment " + UUID.randomUUID()
                else
                    ner.description
            )
    }

    // If it's a new experiment run, buffer an ExperimentRunEvent.
    experimentRunConfig match {
        case ner: NewExperimentRun =>
            Timer.time("Syncing ExperimentRun Event")(ExperimentRunEvent(experimentRun).sync(client.get, Some(this)))
    }

//    /**
//      * Get or create a filepath to the given Transformer (see ModelDB.thrift - getFilePath).
//      * @param t - The Transformer.
//      * @param desiredFileName - The desired filename to use if the Transformer does not already have a filepath and if
//      *                       the name is not already used.
//      * @return The filepath of the Transformer.
//      */
//    def getFilepath(t: Transformer, desiredFileName: String): String = {
//        val st = SyncableTransformer.apply(t)
//        Await.result(client.get.getFilePath(st, experimentRun.id, desiredFileName))
//    }

    /**
      * @param items - The objects whose IDs we seek.
      * @return None if any of the given objects does not have an ID. Otherwise, returns the IDs of the objects.
      */
    private def idsOrNone(items: Object*): Option[Seq[Int]] = {
        val ids = items.map(id)
        if (ids.exists(_.isEmpty))
            None
        else
            Some(ids.map(_.get))
    }

//    /**
//      * Compare the hyperparameters between two models. (see ModelDB.thrift - compareHyperparameters).
//      * @param m1Id - The ID of the first model.
//      * @param m2Id - The ID of the second model.
//      * @return The comparison of hyperparameters.
//      */
//    def compareHyperparameters(m1Id: Int, m2Id: Int): CompareHyperParametersResponse =
//        Await.result(client.get.compareHyperparameters(m1Id, m2Id))
//
//    /**
//      * Compare the hyperparameters between two models. (see ModelDB.thrift - compareHyperparameters).
//      * @param m1 - The first model. Must have an ID.
//      * @param m2 - The second model. Must have an ID.
//      * @return The comparison of hyperparameters.
//      */
//    def compareHyperparameters(m1: Transformer, m2: Transformer): CompareHyperParametersResponse = {
//        val (m1Id, m2Id) = (id(m1), id(m2))
//        if (m1Id.isEmpty || m2Id.isEmpty)
//            CompareHyperParametersResponse()
//        else
//            compareHyperparameters(m1Id.get, m2Id.get)
//    }
}

/**
  * By adding all these traits to the ModelDBSyncer object, we get all the
  * implicit classes easily.
  */
object ModelStorageSyncer {
    implicit var syncer: Option[ModelStorageSyncer] = None

    // Allow the user to configure the syncer.
    def setSyncer(mdbs: ModelStorageSyncer) = syncer = Some(mdbs)
}