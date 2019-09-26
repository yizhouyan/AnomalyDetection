package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractLabeledExamples {
    def fetch(spark: SparkSession): DataFrame
}
