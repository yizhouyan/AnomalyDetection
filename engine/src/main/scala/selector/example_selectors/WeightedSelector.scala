package selector.example_selectors

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */

case class WeightedSelectorParams(numExamples: Int,
                                  weightsMap: Map[String, Float],
                                  selectTop: Boolean = false)

class WeightedSelector(weightedSelectorParams: WeightedSelectorParams) extends AbstractExampleSelector{
    override def name(): String = {
        "weighted_example_selector"
    }

    override def fetch(allExample: DataFrame, labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Example Selector:" + name())
        allExample
    }
}
