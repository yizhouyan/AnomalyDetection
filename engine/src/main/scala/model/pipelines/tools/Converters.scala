package model.pipelines.tools

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf

import scala.collection.mutable.ArrayBuffer

object Converters {
    /**
     * Map a Map[String, Double] structure to a spark Vector.
     * @param inputFeatureNames - Input feature name list. Only features in the list will be presented in the vectors.
     *                          The vector will have the same order as the input feature name list.
     * @return a UDF that can map a map structure to a spark Vector
     */
    def mapToVec(inputFeatureNames: List[String]): UserDefinedFunction ={
        val mapToVec = udf((feature: Map[String, Double]) => {
            var resultArr = ArrayBuffer[Double]()
            for (elem <- inputFeatureNames) {
                resultArr += feature.get(elem).get
            }
            Vectors.dense(resultArr.toArray)
        })
        mapToVec
    }

    def createDenseVector(inputFeatureNames: List[String], inputFeatures: DataFrame)
                         (implicit spark: SparkSession): DataFrame = {
        import spark.implicits._
        val assembler = new VectorAssembler()
                .setInputCols(inputFeatureNames.toArray)
                .setOutputCol("featureVec")
        val featuresForIF = assembler.transform(inputFeatures)
        val toDense = udf((v: org.apache.spark.ml.linalg.Vector) => v.toDense)
        featuresForIF.withColumn("featureVec", toDense($"featureVec"))
    }
}
