package selector.labeled_examples

import client.SyncableDataFramePaths
import org.apache.log4j.Logger
import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{LabeledExample, SharedParams}
import selector.common.utils.ReadInputData.readDataFromFile
/**
  * Created by yizhouyan on 9/7/19.
  */

class ReadLabeledExample(labelFileName: String) extends AbstractLabeledExamples {
    import ReadLabeledExample._
    override def fetch()(implicit spark: SparkSession, sharedParams: SharedParams): Dataset[LabeledExample] = {
        import spark.implicits._
        logger.info("Read Labeled Examples...")
        var dataDF = readDataFromFile(labelFileName)
        dataDF = dataDF.withColumn("label", dataDF("label").cast("double"))
        val newData = dataDF.as[LabeledExample]
        if(sharedParams.saveToDB == true){
            SyncableDataFramePaths.setPath(newData, labelFileName)
        }
        newData
    }
}

object ReadLabeledExample{
    val logger = Logger.getLogger(ReadLabeledExample.getClass)
}
