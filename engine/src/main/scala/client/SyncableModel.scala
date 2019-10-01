package client

import org.apache.spark.sql.DataFrame



object SyncableModel{
  /**
    * Convert from a Transformer into a Thrift structure.
    * @param transformer - The Transformer.
    * @param mdbs - The syncer.
    * @return A the anomalydetection.Model, which is a Thrift structure.
    */
  def apply(transformer: IEvent, filepath: String)
           (implicit mdbs: Option[ModelStorageSyncer]): anomalydetection.Model = {
    val id = mdbs.get.id(transformer).getOrElse(-1)
    val tag = mdbs.get.tag(transformer).getOrElse("")
    val transformerType = transformer.getName()
    transformer match {
      case _ => anomalydetection.Model(id, transformerType, tag=tag, filepath=filepath)
    }
  }
}

