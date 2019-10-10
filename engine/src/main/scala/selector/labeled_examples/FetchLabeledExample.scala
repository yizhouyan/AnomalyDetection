package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.utils.ClassNameMapping
import selector.common.{LabeledExample, RegistryLookup, SharedParams}

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchLabeledExample{
    def fetch(lookup: RegistryLookup)(implicit spark: SparkSession,sharedParams: SharedParams) : Dataset[LabeledExample] = {
        val callDataClass = ClassNameMapping.mapDataTypeToClass(lookup)
                .asInstanceOf[{ def fetch()(implicit spark: SparkSession,sharedParams: SharedParams): Dataset[LabeledExample]}]
        callDataClass.fetch()
    }
}
