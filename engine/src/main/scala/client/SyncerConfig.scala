package client

import spray.json._

case class ProjectJson(name: String, author: String, description: String)

case class ExperimentRunJson(description: String)

case class ThriftJson(host: String = "localhost", port: Int = 6543)

case class SyncerConfigJson(thrift: ThriftJson,
                            project: ProjectJson,
                            experimentRun: ExperimentRunJson
                            )

object SyncerConfigJsonProtocol extends DefaultJsonProtocol {
  implicit val projectFormat = jsonFormat(ProjectJson, "name", "author", "description")
  implicit val experimentRunFormat = jsonFormat(ExperimentRunJson, "description")
  implicit val thriftFormat = jsonFormat(ThriftJson, "host", "port")
  implicit val syncerConfigFormat = jsonFormat(
    SyncerConfigJson,
    "thrift",
    "project",
    "experimentRun"
  )
}
import client.SyncerConfigJsonProtocol._

object SyncerConfig {
  /**
    * Read from a JSON file and create a SyncerConfigJson. You can see a sample in [modeldb_root]/client/syncer.json.
    * @param path - The path to the JSON file.
    * @return The configuration object.
    */
  def apply(path: String): SyncerConfigJson = {
    val source = scala.io.Source.fromFile(path)
    val lines = try source.mkString finally source.close()
    lines.parseJson.convertTo[SyncerConfigJson]
  }
}
