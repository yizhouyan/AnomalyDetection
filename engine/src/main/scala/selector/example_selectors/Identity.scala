package selector.example_selectors

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */

class Identity extends AbstractExampleSelector{
    override def name(): String = {
        "identity"
    }

    override def fetch(allExample: DataFrame, labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Example Selector:" + name())
        allExample
    }
}
