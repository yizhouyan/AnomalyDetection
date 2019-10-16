package selector.example_selectors

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, RegistryLookup, SharedParams}
import selector.common.utils.ClassNameMapping

/**
  * Created by yizhouyan on 9/7/19.
  */
object SelectExamples {
    def fetch(exampleSelector: RegistryLookup,
              allExamples: Dataset[Example],
              labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        ClassNameMapping.mapClassNameToClass(exampleSelector).asInstanceOf[ {
                def fetch(allExamples: Dataset[Example], labeledExample: Dataset[LabeledExample])
                         (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame
            }].fetch(allExamples, labeledExample)
    }
}
