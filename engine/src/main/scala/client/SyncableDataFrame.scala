package client

import org.apache.spark.sql.{DataFrame, Dataset}

/**
  * Created by yizhouyan on 9/30/19.
  */
object SyncableDataFrame {
    /**
      * Convert a Spark DataFrame into an anomalydetection.DataFrame.
      * @param df - The Spark DataFrame.
      * @param mdbs - The syncer (used for the id mapping).
      * @return An anomalydetection.DataFrame representing the Spark DataFrame.
      */
    def apply(df: Dataset[_])(implicit mdbs: Option[ModelStorageSyncer]): anomalydetection.DataFrame = {
        val id = mdbs.get.id(df).getOrElse(-1)
        val tag = mdbs.get.tag(df).getOrElse("")

        // If this dataframe already has an ID, the columns are already stored on the server, so we leave them empty.
        val columns = if (id != -1) {
            Seq[anomalydetection.DataFrameColumn]()
        } else {
            df.toDF.schema.map(field => anomalydetection.DataFrameColumn(field.name, field.dataType.simpleString))
        }

        val modeldbDf = anomalydetection.DataFrame(
            id,
            columns,
            tag=tag,
            filepath = SyncableDataFramePaths.getPath(df)
        )
        modeldbDf
    }
}
