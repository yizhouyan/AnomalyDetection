package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.utils.ClassNameMapping
import selector.common.{LabeledExample, RegistryLookup}

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchLabeledExample{
    def fetch(lookup: RegistryLookup)(implicit spark: SparkSession) : Dataset[LabeledExample] = {
        val callDataClass = ClassNameMapping.mapDataTypeToClass(lookup)
                .asInstanceOf[{ def fetch()(implicit spark: SparkSession): Dataset[LabeledExample]}]
        callDataClass.fetch()
    }
}
