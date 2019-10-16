package client

import org.apache.spark.sql.{DataFrame, Dataset}

import scala.collection.mutable


object SyncableDataFramePaths {
  /**
    * A cache that maps from DataFrame to the path that it was loaded from.
    */
  private val pathForDf = mutable.HashMap[Dataset[_], String]()

  /**
    * Get the path that a given DataFrame was loaded from.
    * @param df - The DataFrame.
    * @return The path or None if the DataFrame does not have a path.
    */
  def getPath(df: Dataset[_]): String = pathForDf.getOrElse(df,"")

  /**
    * Set the path that a given DataFrame was loaded from.
    * @param df - The DataFrame.
    * @param path - The path.
    */
  def setPath(df: Dataset[_], path: String): Unit = pathForDf.put(df, path)

  /**
    * Clear all mappings from DataFrame to path.
    */
  def clear(): Unit = pathForDf.clear()
}
