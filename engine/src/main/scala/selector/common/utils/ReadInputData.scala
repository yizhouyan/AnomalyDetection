package selector.common.utils

import java.io.File

import client.SyncableDataFramePaths
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.SharedParams
import utils.{FileNameNotSetException, FileTypeNotSupportedException, FileUtil, NoFileUnderInputFolderException}

import scala.collection.mutable

object ReadInputData {
    val logger = Logger.getLogger(this.getClass)
    val inputFileNameToData = new mutable.HashMap[String, DataFrame]

    def detectFileType(allFileNames: Array[String]): String ={
        for (filename <- allFileNames){
            if(filename.toLowerCase.contains("parquet"))
                return "parquet"
            else if(filename.toLowerCase.contains("json"))
                return "json"
            else if(filename.toLowerCase.contains("csv"))
                return "csv"
        }
        throw FileTypeNotSupportedException("Input file type not supported! We support parquet, json and csv files.")
    }

    def readDataFromFile(inputFilePath: String)(implicit spark: SparkSession): DataFrame ={
        logger.info("Read Data from file " + inputFilePath)
        val inputFile: File = new File(inputFilePath)
        var dataDF: DataFrame = null
        if(inputFile.isDirectory){
            var allFileNames: Array[String]  = FileUtil.getRecursiveListOfFiles(inputFile: File)
            val fileType = detectFileType(allFileNames)
            if(allFileNames.length > 0) {
                if(fileType.equals("parquet")){
                    dataDF = spark.read.parquet(allFileNames.toList:_*)
                }else if(fileType.equals("json")){
                    dataDF = spark.read.json(allFileNames.toList:_*)
                }else if(fileType.equals("csv")) {
                    dataDF = spark.read.format("csv")
                            .option("header", "true")
                            .load(allFileNames.toList: _*)
                }else{
                    throw FileTypeNotSupportedException("Input file type not supported! We support parquet, json and csv files.")
                }
            }else{
                throw NoFileUnderInputFolderException("No input file under the input path!")
            }
        }else{
            if(inputFilePath.toLowerCase.contains("parquet")){
                dataDF = spark.read.parquet(inputFilePath)
            }else if(inputFilePath.toLowerCase.contains("json")){
                dataDF = spark.read.json(inputFilePath)
            }else if(inputFilePath.toLowerCase.contains("csv")) {
                dataDF = spark.read.format("csv")
                        .option("header", "true")
                        .load(inputFilePath)
            }else{
                throw FileTypeNotSupportedException("Input file type not supported! We support parquet, json and csv files.")
            }
        }
        dataDF
    }

    def fetchInputData()(implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        val finalInputFilePath = sharedParams.sharedFilePath
        if(inputFileNameToData.contains(finalInputFilePath))
            return inputFileNameToData.get(finalInputFilePath).get
        val dataDF = readDataFromFile(finalInputFilePath)

        inputFileNameToData.put(finalInputFilePath, dataDF)
        if(sharedParams.saveToDB == true){
            SyncableDataFramePaths.setPath(dataDF, finalInputFilePath)
        }
        dataDF
    }
}
