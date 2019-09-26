package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */

class KDDCupLabeledExample extends AbstractLabeledExamples {
    override def fetch(spark: SparkSession): DataFrame = {
        println("Create KDD Cup Labeled Examples")
        spark.emptyDataFrame
    }
}
