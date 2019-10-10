package model.data

import model.common.LabeledData
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/6/19.
  */
abstract class AbstractLabel {
    def fetch(spark: SparkSession): Dataset[LabeledData]
}
