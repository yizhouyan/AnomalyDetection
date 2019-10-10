package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.LabeledExample

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractLabeledExamples {
    def fetch()(implicit spark: SparkSession): Dataset[LabeledExample]
}
