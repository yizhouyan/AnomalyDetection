package model.data

import java.io.File

import model.common.CustomizedFile
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import model.common._
import model.common.utils._

/**
  * Created by yizhouyan on 9/6/19.
  */

class ReadDataFile(customizedFile: CustomizedFile) extends AbstractData{
    override def fetch(spark: SparkSession): Dataset[Feature] = {
        println("Create Dataset from file " + customizedFile.path)
        val inputFile: File = new File(customizedFile.path)
        var dataDF: DataFrame = null
        if(inputFile.isDirectory){
            var allFileNames: Array[String]  = FileUtil.getRecursiveListOfFiles(inputFile: File)
            if(allFileNames.length > 0) {
                if(customizedFile.fileType.toLowerCase.equals("parquet")){
                    dataDF = spark.read.parquet(allFileNames.toList:_*)
                }else if(customizedFile.fileType.toLowerCase.equals("json")){
                    dataDF = spark.read.json(allFileNames.toList:_*)
                }else if(customizedFile.fileType.toLowerCase.equals("csv")) {
                    dataDF = spark.read.format("csv")
                            .option("header", "true")
                            .load(allFileNames.toList: _*)
                }else{
                    println("Input File Type not supported! We support parquet, json and csv files. ")
                    System.exit(0)
                }
            }else{
                println("No file in this directory...")
                System.exit(0)
            }
        }else{
            if(customizedFile.fileType.toLowerCase.equals("parquet")){
                dataDF = spark.read.parquet(customizedFile.path)
            }else if(customizedFile.fileType.toLowerCase.equals("json")){
                dataDF = spark.read.json(customizedFile.path)
            }else if(customizedFile.fileType.toLowerCase.equals("csv")) {
                dataDF = spark.read.format("csv")
                        .option("header", "true")
                        .load(customizedFile.path)
            }else{
                println("Input File Type not supported! We support parquet, json and csv files. ")
                System.exit(0)
            }
        }
        import spark.implicits._
        var mapColumn: Column = map(dataDF.columns.tail.flatMap(name => Seq(lit(name), $"$name")): _*)
        dataDF.select($"index" as "id", mapColumn as "dense")
                .withColumn("explanations", typedLit(Map.empty[String, String]))
                .as[Feature]
    }
}
