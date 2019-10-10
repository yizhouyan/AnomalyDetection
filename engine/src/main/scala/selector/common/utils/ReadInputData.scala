package selector.common.utils

import java.io.File

import client.SyncableDataFramePaths
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Feature, SharedParams}
import utils.{FileNameNotSetException, FileTypeNotSupportedException, FileUtil, NoFileUnderInputFolderException}

object ReadInputData {
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

    def fetchInputData(inputFilePath: Option[String])
                      (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Feature] = {
        val finalInputFilePath = {
            if(inputFilePath.isDefined)
                inputFilePath.get
            else if(sharedParams.sharedFilePath.isDefined)
                sharedParams.sharedFilePath.get
            else
                throw FileNameNotSetException("Input file name not set!")
        }
        println("Read Data from file " + finalInputFilePath)
        val inputFile: File = new File(finalInputFilePath)
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
            if(finalInputFilePath.toLowerCase.contains("parquet")){
                dataDF = spark.read.parquet(finalInputFilePath)
            }else if(finalInputFilePath.toLowerCase.contains("json")){
                dataDF = spark.read.json(finalInputFilePath)
            }else if(finalInputFilePath.toLowerCase.contains("csv")) {
                dataDF = spark.read.format("csv")
                        .option("header", "true")
                        .load(finalInputFilePath)
            }else{
                throw FileTypeNotSupportedException("Input file type not supported! We support parquet, json and csv files.")
            }
        }
        import spark.implicits._
        val newData: Dataset[Feature] = dataDF.as[Feature]

        if(sharedParams.saveToDB == true){
            SyncableDataFramePaths.setPath(newData, finalInputFilePath)
        }
        newData
    }
}
