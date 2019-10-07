package model.pipelines.unsupervised

import scala.collection.mutable.ListBuffer
import scala.util.Random
import util.control.Breaks._
object test {

    def main(args: Array[String]): Unit = {
        import model.pipelines.tools.DefaultTools._
        val rng = new Random(1234)
        for(i <- 1 to 20)
            println(between(10, 20, rng))
    }
}
