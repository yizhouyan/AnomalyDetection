package selector.example_sources
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */
case class AnomalyScoreParams(inputColName: String,
                              outputColName: Option[String],
                              bottomThres: Double = 0.9,
                              topThres: Double = 1.0,
                              usePercentile: Boolean = false)

class AnomalyScore(anomalyScoreParams: AnomalyScoreParams) extends AbstractExampleSource{
    override def name(): String = {
        anomalyScoreParams.outputColName match{
            case Some(a) => a
            case None => "anomaly_score_%s_%.2f_%.2f".format(
                anomalyScoreParams.inputColName,
                anomalyScoreParams.bottomThres,
                anomalyScoreParams.topThres)
        }
    }

    override def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Get Top Scored Anomalies: " + name())
        spark.emptyDataFrame
    }
}
