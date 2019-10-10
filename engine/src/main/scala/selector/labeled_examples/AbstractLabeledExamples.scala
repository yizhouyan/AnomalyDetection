package selector.labeled_examples

import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{LabeledExample, SharedParams}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractLabeledExamples {
    def fetch()(implicit spark: SparkSession,sharedParams: SharedParams): Dataset[LabeledExample]
}
