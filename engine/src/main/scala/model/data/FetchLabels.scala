package model.data

import model.common.utils.ClassNameMapping
import model.common.{LabeledData, RegistryLookup}
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchLabels{
    def fetch(dataConfig: RegistryLookup, spark: SparkSession): Dataset[LabeledData] = {
        // get labels
        val allData = ClassNameMapping.mapDataTypeToClass(dataConfig).asInstanceOf[{
            def fetch(spark: SparkSession): Dataset[LabeledData]}].fetch(spark)
        allData.show(10, false)
        allData
    }
}
