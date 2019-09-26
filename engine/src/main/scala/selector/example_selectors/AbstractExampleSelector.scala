package selector.example_selectors

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractExampleSelector {
    def name(): String
    def fetch(allExample: DataFrame, labeledExample: DataFrame, spark: SparkSession): DataFrame
}
