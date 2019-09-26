package model.data

import model.common.utils.ClassNameMapping
import model.common.{Feature, LabeledExamples, RegistryLookup}
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchLabels{
    def fetch(dataConfig: RegistryLookup, spark: SparkSession): Dataset[LabeledExamples] = {
        // get labels
        val allData = ClassNameMapping.mapDataTypeToClass(dataConfig).asInstanceOf[{
            def fetch(spark: SparkSession): Dataset[LabeledExamples]}].fetch(spark)
        allData.show(10, false)
        allData
    }
}
