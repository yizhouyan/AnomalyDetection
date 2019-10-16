package model.data

import model.common.{RegistryLookup, SharedParams}
import model.common.utils.ClassNameMapping
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchDataExample{
    def fetch(dataConfig: RegistryLookup)(implicit spark: SparkSession, sharedParams:SharedParams): DataFrame = {
        // get data
        val allData = ClassNameMapping.mapDataTypeToClass(dataConfig).asInstanceOf[{
            def fetch()(implicit spark: SparkSession, sharedParams:SharedParams): DataFrame}].fetch()
        allData
    }
}
