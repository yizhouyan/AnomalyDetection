package utils

import java.util.UUID

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

/***
 * UDAF combining maps, overriding any duplicate key with "latest" value
 * @param keyType DataType of Map key
 * @param valueType DataType of Value key
 * @param merge function to merge values of identical keys
 * @tparam K key type
 * @tparam V value type
 */
class CombineMaps[K, V](keyType: DataType, valueType: DataType, merge: (V, V) => V) extends UserDefinedAggregateFunction {
    override def inputSchema: StructType = new StructType().add("map", dataType)
    override def bufferSchema: StructType = inputSchema
    override def dataType: DataType = MapType(keyType, valueType)
    override def deterministic: Boolean = true

    override def initialize(buffer: MutableAggregationBuffer): Unit = buffer.update(0, Map[K, V]())

    override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
        val map1 = buffer.getAs[Map[K, V]](0)
        val map2 = input.getAs[Map[K, V]](0)
        val result = map1 ++ map2.map { case (k,v) => k -> map1.get(k).map(merge(v, _)).getOrElse(v) }
        buffer.update(0, result)
    }

    override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = update(buffer1, buffer2)

    override def evaluate(buffer: Row): Any = buffer.getAs[Map[K, V]](0)
}

object Utils {
    def getRandomFilePath(pathPrefix: String="", filePrefix: String=""): String ={
        java.nio.file.Paths.get(pathPrefix, filePrefix + "_" + UUID.randomUUID()).toString
    }

    // instantiate a CombineMaps with the relevant types:
    def mergeFunc(v:Any, t: Any): Any = {v}
    val combineMapsStrDouble = new CombineMaps[String, Double](StringType, DoubleType, mergeFunc(_, _).asInstanceOf[Double])
    val combineMapsStrStr = new CombineMaps[String, String](StringType, StringType, mergeFunc(_,_).asInstanceOf[String])

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
