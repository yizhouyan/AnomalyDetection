package selector.labeled_examples

import org.apache.spark.sql.{DataFrame, SparkSession}
import selector.common.RegistryLookup

/**
  * Created by yizhouyan on 9/7/19.
  */
object FetchLabeledExample{
    def fetch(lookup: RegistryLookup, spark: SparkSession) : DataFrame = {
        val callDataClass = Class.forName("selector.labeled_examples." + lookup.name)
                .getConstructors()(0)
                .newInstance().asInstanceOf[{ def fetch(spark: SparkSession): DataFrame }]
        callDataClass.fetch(spark)
    }
}
