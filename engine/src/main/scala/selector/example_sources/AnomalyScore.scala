package selector.example_sources
import client.SyncableDataFramePaths
import org.apache.log4j.Logger
import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{Example, Feature, LabeledExample, SharedParams}
import selector.common.utils.ReadInputData
import org.apache.spark.sql.functions._

import scala.collection.mutable

case class AnomalyScoreParams(inputColName: String,
                              outputColName: Option[String],
                              bottomThres: Double = 0.9,
                              topThres: Double = 1.0,
                              usePercentile: Boolean = false)

/**
 * Selects examples whose anomaly scores are within [bottom_threshold, top_threshold].
 * If use_percentile is set to true, we select users whose spoofing scores are within the percentile.
 */
class AnomalyScore(params: AnomalyScoreParams) extends AbstractExampleSource{
    import AnomalyScore._
    override def getName(): String = {
        params.outputColName match{
            case Some(a) => a
            case None => "anomaly_score_%s_%.2f_%.2f".format(
                params.inputColName,
                params.bottomThres,
                params.topThres)
        }
    }

    override def fetch(labeledExample: Dataset[LabeledExample])
                      (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example] = {
        import spark.implicits._
        logger.info("Get Top Scored Anomalies: " + getName())
        // get data from input path
        val data: Dataset[Feature] = ReadInputData.fetchInputData()
        var bottomThreshold = params.bottomThres
        var topThreshold = params.topThres
        if(params.usePercentile){
            val thresholds = data.selectExpr("approx_percentile(results['" + params.inputColName + "'], " +
                    "array("+ params.bottomThres + "," + params.topThres + "))").first().getAs[mutable.WrappedArray[Double]](0)
            bottomThreshold = thresholds.apply(0)
            topThreshold = thresholds.apply(1)
        }
        logger.info("BottomThreshold = " + bottomThreshold + ", TopThreshold = " + topThreshold)
        labeledExample.createOrReplaceTempView("labeledExample")
        val results = data.drop($"dense").drop($"explanations")
                .where($"results".getItem(params.inputColName) >= bottomThreshold
                and $"results".getItem(params.inputColName) < topThreshold)
                .withColumn("weight", $"results".getItem(params.inputColName))
                .drop($"results")
                .withColumn("source", lit(getName())).as[Example]
                .where("id not in (select id from labeledExample)")

        // if saveToDB is set to true, save the results to Storage
        if(sharedParams.saveToDB == true){
            logger.info("Save model to Storage")
            SyncableDataFramePaths.setPath(results, sharedParams.allExamplesOutputFileName)
            saveExampleSelectorEventsToDB(this,
                data,
                results,
                labeledExample,
                0
            )
        }
        results
    }

    override def getHyperParameters(): mutable.Map[Any, Any] = {
        val paramsMap = mutable.Map[Any, Any]()
        paramsMap.put("inputColName", params.inputColName)
        paramsMap.put("outputColName", params.outputColName)
        paramsMap.put("bottomThres", params.bottomThres)
        paramsMap.put("topThres", params.topThres)
        paramsMap.put("usePercentile", params.usePercentile)
        paramsMap
    }
}

object AnomalyScore{
    val logger = Logger.getLogger(AnomalyScore.getClass)
}

