package utils

import java.util.UUID

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Utils {
    def getRandomFilePath(pathPrefix: String="", filePrefix: String=""): String ={
        java.nio.file.Paths.get(pathPrefix, filePrefix + "_" + UUID.randomUUID()).toString
    }

    def initializeSparkContext(appName: String): SparkSession = {
        val conf = new SparkConf().setAppName(appName)
        import org.apache.log4j.{Level, Logger}

        val rootLogger = Logger.getRootLogger()
        rootLogger.setLevel(Level.ERROR)

        Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
        Logger.getLogger("org.spark-project").setLevel(Level.WARN)
        val spark = SparkSession
                .builder()
                .master("local")  //"spark://localhost:7077"
                .config("spark.sql.codegen.wholeStage", "false")
                .getOrCreate()
        spark
    }
}
