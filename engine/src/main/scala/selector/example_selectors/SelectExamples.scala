package selector.example_selectors

import org.apache.spark.sql.{DataFrame, SparkSession}
import selector.common.RegistryLookup
import selector.common.utils.ClassNameMapping

/**
  * Created by yizhouyan on 9/7/19.
  */
object SelectExamples {
    def fetch(exampleSelector: RegistryLookup,
              spark: SparkSession,
              allExamples: DataFrame,
              labeledExample: DataFrame): DataFrame = {
        return ClassNameMapping.mapClassNameToClass(exampleSelector).asInstanceOf[ {
                def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame
            }].fetch(labeledExample, spark)
    }
}
