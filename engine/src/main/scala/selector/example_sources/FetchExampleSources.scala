package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}
import selector.common.{RegistryLookup, SharedParams}
import selector.common.utils.ClassNameMapping

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchExampleSources {
    def fetch(exampleSources: List[RegistryLookup], labeledExample: DataFrame)
             (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
         // get all dataframes from different sources
        var allData: List[DataFrame] = List()
        for (source <- exampleSources){
            allData= allData :+ ClassNameMapping.mapClassNameToClass(source).asInstanceOf[ {
                def fetch(labeledExample: DataFrame)
                         (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame
            }].fetch(labeledExample)
        }
        // union these results together
        spark.emptyDataFrame
    }
}
