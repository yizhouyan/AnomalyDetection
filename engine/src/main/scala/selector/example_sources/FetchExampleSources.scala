package selector.example_sources

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, RegistryLookup, SharedParams}
import selector.common.utils.ClassNameMapping

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchExampleSources {
    def fetch(exampleSources: List[RegistryLookup], labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
         // get all dataframes from different sources
        import spark.implicits._
        var allData: List[Dataset[Example]] = List()
        for (source <- exampleSources){
            allData= allData :+ ClassNameMapping.mapClassNameToClass(source).asInstanceOf[ {
                def fetch(labeledExample: Dataset[LabeledExample])
                         (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example]
            }].fetch(labeledExample)
        }
        // union these results together
        spark.emptyDataset[Example]
    }
}
