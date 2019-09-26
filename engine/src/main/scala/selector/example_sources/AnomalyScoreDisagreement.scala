package selector.example_sources

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by yizhouyan on 9/8/19.
  */
case class AnomalyScoreDisagreeParams(inputColNames: List[String],
                              outputColName: Option[String],
                              bottomThres: Double = 0.9,
                              topThres: Double = 1.0,
                              usePercentile: Boolean = false)

class AnomalyScoreDisagreement(anomalyScoreDisagreeParams: AnomalyScoreDisagreeParams) extends AbstractExampleSource{
    override def name(): String = {
        anomalyScoreDisagreeParams.outputColName match{
            case Some(a) => a
            case None => "spoofing_score_disagreement_%.2f_%.2f".format(
                anomalyScoreDisagreeParams.bottomThres,
                anomalyScoreDisagreeParams.topThres)
        }
    }

    override def fetch(labeledExample: DataFrame, spark: SparkSession): DataFrame = {
        println("Get Anomalies with disagreement: " + name())
        spark.emptyDataFrame
    }
}
