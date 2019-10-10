package selector.example_sources

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractExampleSource {
    def name(): String
    def fetch(labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example]
}
