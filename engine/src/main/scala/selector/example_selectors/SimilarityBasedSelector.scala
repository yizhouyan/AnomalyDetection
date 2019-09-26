package selector.example_selectors

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */

case class SimilaritySelectorParams(numExamples: Int,
                                    similarityFunction: String = "cosine",
                                    similarityThreshold: Float = 0.0f)

class SimilarityBasedSelector(similaritySelectorParams: SimilaritySelectorParams) extends AbstractExampleSelector{
    override def name(): String = {
        "Similarity_based_selector"
    }

    override def fetch(allExample: DataFrame, labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Example Selector:" + name())
        allExample
    }
}
