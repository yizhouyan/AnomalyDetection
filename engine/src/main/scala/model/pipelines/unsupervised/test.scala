package model.pipelines.unsupervised

import org.apache.commons.math3.random.{RandomDataGenerator, RandomGeneratorFactory}
import org.apache.spark.SparkConf
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object test {
    def main(args: Array[String]): Unit = {
        import model.pipelines.unsupervised.tools.DefaultTools._
        val x = Array(1,3,4,5,7)
        println(x.argSort.reverse.mkString(","))

    }
}
