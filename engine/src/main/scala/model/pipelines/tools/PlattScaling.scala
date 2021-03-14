package model.pipelines.tools

import math.exp
import sys.process._
import java.util.Properties
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.PrintWriter
import java.io.File

class PlattScaling (val A:Double, val B:Double) extends OutputScaler {
  
  def scale(rawScore:Double) : Double = {
    val x = rawScore*A+B
    if(x >= 0.0){
      exp(-x)/(1.0+exp(-x))
    }else{
      1.0/(1.0+exp(x))
    }
  }
  
  def saveModel(modelDir:String) : Unit = {
    val props = new Properties()
    props.setProperty("A", A.toString())
    props.setProperty("B", B.toString())
    props.store(new FileOutputStream(s"$modelDir/platt_scaling.properties"), "Platt scaling 1.0/(1.0+exp(score*A+B))")
  }
  
}

object PlattScaling{
  
  def trainFromRawScores(rawScores : List[Double], topAndBottomSamples:Int) : PlattScaling = {
    
    val sortedRawScores = rawScores.sorted
    val negativeScores = sortedRawScores.take(topAndBottomSamples)
    val positiveScores = sortedRawScores.reverse.take(topAndBottomSamples)
    
    val file = File.createTempFile("platt/platt_scaling_", ".tmp", new File("src/main/scala/model/pipelines/tools/plattTmp"))
    //file.deleteOnExit()
    System.err.println(file.toString())
    val pw = new PrintWriter(file)
    for(rawScore <- positiveScores){
      pw.println(s"$rawScore +1")
    }
    for(rawScore <- negativeScores){
      pw.println(s"$rawScore -1")
    }
    pw.close()

    val script = "src/main/scala/model/pipelines/tools/platt.py"
    val cmd = "python " + script + " " + file.toString
    val res = cmd !!
    
    val fields = res.substring(1, res.length-3).split(", ")
    val A = fields(0).toDouble
    val B = fields(1).toDouble
    
    new PlattScaling(A, B)
  }
  
  def loadModel(modelDir:String) : PlattScaling = {
    val props = new Properties()
    props.load(new FileInputStream(s"$modelDir/platt_scaling.properties"))
    val A = props.getProperty("A").toDouble
    val B = props.getProperty("B").toDouble
    
    new PlattScaling(A,B)
  }

  def main(args: Array[String]): Unit = {

    val pairs:List[(Double, Int)] = List( (1.2321, 1), (3.112314, 1), (13.1231, -1), (-213.1, -1))
    val platt = PlattScaling.trainFromRawScores(pairs.map{_._1}, 2)

    println("1 "+platt.scale(-53.2131))
    println("2 "+platt.scale(-3.2131))
    println("3 "+platt.scale(3.2131))
    println("4 "+platt.scale(53.2131))

    platt.saveModel(".")

    val platt2 = PlattScaling.loadModel(".")
    println("1r "+platt2.scale(-53.2131))
    println("2r "+platt2.scale(-3.2131))
    println("3r "+platt2.scale(3.2131))
    println("4r "+platt2.scale(53.2131))
  }

}
