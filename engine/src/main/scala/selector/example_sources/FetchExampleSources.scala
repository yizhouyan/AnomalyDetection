package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}
import selector.common.RegistryLookup
import selector.common.utils.ClassNameMapping

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchExampleSources {
    def fetch(exampleSources: List[RegistryLookup], spark: SparkSession, labeledExample: DataFrame): DataFrame = {
        // get all dataframes from different sources
        var allData: List[DataFrame] = List()
        for (source <- exampleSources){
            allData= allData :+ ClassNameMapping.mapClassNameToClass(source).asInstanceOf[ {
                def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame
            }].fetch(labeledExample, spark)
        }
        // union these results together
        spark.emptyDataFrame
    }
}
