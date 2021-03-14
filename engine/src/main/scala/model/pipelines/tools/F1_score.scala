package model.pipelines.tools

import org.apache.spark.sql.DataFrame

object F1_score {

    private def countStatisticalMeasure(labelWithPredictionCached: List[(Double, Double)],
                                        labelVal: Double, predictionVal: Double): Int = {
        labelWithPredictionCached.count { labelWithPrediction =>
            val label = labelWithPrediction._1
            val prediction = labelWithPrediction._2
            label == labelVal && prediction == predictionVal
        }
    }

    def computeF1score(labeled_data: DataFrame, prediction: DataFrame): Double ={
        val labels = labeled_data.select("label").collect().map(_(0)).toList
        val predictions = prediction.select("predictions").collect().map(_(0)).toList
        var labelWithPredictionCached: List[(Double,Double)] = List()
        for(k <- labels.indices){
            val tmp = (labels(k).toString.toDouble, predictions(k).toString.toDouble)
            labelWithPredictionCached ++= List(tmp)
        }
        val TruePositive = countStatisticalMeasure(labelWithPredictionCached, 1.0, 1.0)
        val FalsePositive = countStatisticalMeasure(labelWithPredictionCached, 0.0, 1.0)
        val FalseNegative = countStatisticalMeasure(labelWithPredictionCached, 1.0, 0.0)

        val precision = TruePositive / Math.max(1.0, TruePositive + FalsePositive)
        val recall = TruePositive / Math.max(1.0, TruePositive + FalseNegative)

        val f1Score = 2.0 * precision * recall / (precision + recall)
        f1Score
    }
}
