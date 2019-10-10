package selector.example_sources
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, Feature, LabeledExample, SharedParams}
import selector.common.utils.ReadInputData
import org.apache.spark.sql.functions._

/**
  * Created by yizhouyan on 9/8/19.
  */
case class AnomalyScoreParams(inputColName: String,
                              filePath: Option[String],
                              outputColName: Option[String],
                              bottomThres: Double = 0.9,
                              topThres: Double = 1.0,
                              usePercentile: Boolean = false)

class AnomalyScore(params: AnomalyScoreParams) extends AbstractExampleSource{
    override def name(): String = {
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
        println("Get Top Scored Anomalies: " + name())
        // get data from input path
        val data: Dataset[Feature] = ReadInputData.fetchInputData(params.filePath)
        var results = {
            if(!params.usePercentile)
                data.where($"results".getItem(params.inputColName) >= params.bottomThres
                        and $"results".getItem(params.inputColName) < params.topThres)
                        .withColumn("weight", $"results".getItem(params.inputColName))
                        .withColumn("source", lit(name())).as[Example]
            else{
                spark.emptyDataset[Example]
            }
        }
        results
    }
}
