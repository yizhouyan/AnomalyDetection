package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractExampleSource {
    def name(): String
    def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame
}
