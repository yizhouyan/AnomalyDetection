package model.common

import scala.collection.mutable.ListBuffer

class ColumnTracking {
    private var featureCols: ListBuffer[String] = new ListBuffer[String]
    private var resultCols: ListBuffer[String] = new ListBuffer[String]
    private var explanationCols: ListBuffer[String] =new ListBuffer[String]

    def addToFeatures(newFeatureCols: List[String]): Unit = {
        this.featureCols ++= newFeatureCols
    }

    def addToResult(newResultCols: List[String]): Unit = {
        this.resultCols ++= newResultCols
    }

    def addToExplain(newExplainCols: List[String]): Unit = {
        this.explanationCols ++= newExplainCols
    }

    def addToResult(newResultCol: String): Unit = {
        this.resultCols += newResultCol
    }

    def addToExplain(newExplainCol: String): Unit = {
        this.explanationCols += newExplainCol
    }

    def resetFeatureCols(): Unit = {
        this.featureCols = new ListBuffer[String]
    }

    def getFeatureCols(): List[String] = {
        featureCols.toList
    }

    def getResultCols(): List[String] = {
        resultCols.toList
    }

    def getExplanationCols(): List[String] = {
        explanationCols.toList
    }
}
