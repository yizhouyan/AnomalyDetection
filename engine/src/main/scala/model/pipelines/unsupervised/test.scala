package model.pipelines.unsupervised

import scala.collection.mutable.ListBuffer
import scala.util.Random
import util.control.Breaks._
object test {
    def main(args: Array[String]): Unit = {
        import model.pipelines.tools.DefaultTools._
        val x = breeze.linalg.DenseVector(0.1,0.2)
        val y = breeze.linalg.DenseVector(0.3,0.4)
        println(x + y)
    }
}
