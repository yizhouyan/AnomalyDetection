package selector.example_sources

import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{Example, LabeledExample, RegistryLookup, SharedParams}
import selector.common.utils.ClassNameMapping
import org.apache.spark.sql.functions._
import utils.Utils
/**
 * Created by yizhouyan on 9/7/19.
 */
object FetchExampleSources {
    def fetch(exampleSources: List[RegistryLookup], labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
        // get all dataframes from different sources and union these results together
        import spark.implicits._
        var allData = spark.emptyDataset[Example]
        var count = 0
        for (source <- exampleSources){
            val curData: Dataset[Example] = ClassNameMapping.mapClassNameToClass(source).asInstanceOf[ {
                def fetch(labeledExample: Dataset[LabeledExample])
                         (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example]
            }].fetch(labeledExample)
            allData = {
                if(count == 0)
                    curData
                else
                    allData.union(curData)
            }
            count += 1
        }
        // finally remove duplicates
        allData = allData.groupBy("id")
                .agg(max($"weight").alias("weight"),
                    collect_list($"source").cast("string").alias("source")).as[Example]
        allData
    }
}
