package selector.labeled_examples

import org.apache.log4j.Logger
import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.LabeledExample
import selector.common.utils.ReadInputData.readDataFromFile
/**
  * Created by yizhouyan on 9/7/19.
  */

class ReadLabeledExample(labelFileName: String) extends AbstractLabeledExamples {
    import ReadLabeledExample._
    override def fetch()(implicit spark: SparkSession): Dataset[LabeledExample] = {
        import spark.implicits._
        logger.info("Read Labeled Examples...")
        var dataDF = readDataFromFile(labelFileName)
        dataDF = dataDF.withColumn("label", dataDF("label").cast("double"))
        dataDF.as[LabeledExample]
    }
}

object ReadLabeledExample{
    val logger = Logger.getLogger(ReadLabeledExample.getClass)
}
