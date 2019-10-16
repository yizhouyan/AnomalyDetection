package model.data

import java.io.File

import utils.{FileTypeNotSupportedException,NoFileUnderInputFolderException}
import utils.FileUtil
import client.SyncableDataFramePaths
import model.common.CustomizedFile
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import model.common._
import org.apache.log4j.Logger

/**
  * Created by yizhouyan on 9/6/19.
  */

class ReadDataFile(customizedFile: CustomizedFile) extends AbstractData{
    import ReadDataFile._
    override def fetch()(implicit spark: SparkSession, sharedParams:SharedParams): DataFrame= {
        logger.info("Create Dataset from file " + customizedFile.path)
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
                    throw FileTypeNotSupportedException("Input File Type not supported! We support parquet, json and csv files. ")
                }
            }else{
                throw NoFileUnderInputFolderException("No file in this directory...")
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
                throw FileTypeNotSupportedException("Input File Type not supported! We support parquet, json and csv files. ")
            }
        }
        dataDF = dataDF.withColumnRenamed(dataDF.columns.head, "id")
        val columnsCast = col(dataDF.columns.head) +: dataDF.columns.tail.map(name => col(name).cast("double"))
        dataDF = dataDF.select(columnsCast :_*)

        sharedParams.columeTracking.addToFeatures(dataDF.columns.tail.toList)

        if(sharedParams.saveToDB == true){
            SyncableDataFramePaths.setPath(dataDF, customizedFile.path)
        }
        dataDF
    }
}

object ReadDataFile{
    val logger = Logger.getLogger(ReadDataFile.getClass)
}

