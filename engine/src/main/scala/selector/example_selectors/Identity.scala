package selector.example_selectors

import client.SyncableDataFramePaths
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}
import org.apache.spark.sql.functions._
import selector.common.utils.ReadInputData

import scala.collection.mutable

/**
  * Created by yizhouyan on 9/8/19.
  */

case class IdentityParams(numExamples: Option[Int])

class Identity(params: IdentityParams) extends AbstractExampleSelector{
    var numSamples: Int  = 0

    import Identity._
    override def getName(): String = {
        "Identity"
    }

    override def fetch(allExample: Dataset[Example], labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        logger.info("Use " + getName() + " as example selector")
        val data: DataFrame = ReadInputData.fetchInputData()
        val finalResults = {
            if(params.numExamples.isDefined) {
                numSamples = params.numExamples.get
                allExample.orderBy(desc("weight")).limit(params.numExamples.get)
            } else {
                numSamples = allExample.count().toInt
                allExample
            }
        }.join(data, "id").orderBy(desc("weight"))
        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            logger.info("Save model to Storage")
            SyncableDataFramePaths.setPath(allExample, sharedParams.selectedExamplesOutputFileName)
            saveExampleSelectorEventsToDB(this,
                allExample,
                finalResults,
                labeledExample,
                1
            )
        }
        finalResults
    }

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        val paramsMap = mutable.Map[Any, Any]()
        paramsMap.put("numSamples", numSamples)
        paramsMap
    }
}

object Identity{
    val logger = Logger.getLogger(Identity.getClass)
}
