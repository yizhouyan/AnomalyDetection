package model.data

import model.common.Feature
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/6/19.
  */
abstract class AbstractData {
    def fetch(spark: SparkSession): Dataset[Feature]
}
