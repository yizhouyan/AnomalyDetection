package model.data

import model.common.{Feature, SharedParams}
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/6/19.
  */
abstract class AbstractData {
    def fetch()(implicit spark: SparkSession, sharedParams:SharedParams): Dataset[Feature]
}
