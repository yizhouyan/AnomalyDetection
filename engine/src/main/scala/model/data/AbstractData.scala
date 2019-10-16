package model.data

import model.common.SharedParams
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/6/19.
  */
abstract class AbstractData {
    def fetch()(implicit spark: SparkSession, sharedParams:SharedParams): DataFrame
}
