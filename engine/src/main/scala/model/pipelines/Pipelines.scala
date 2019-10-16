package model.pipelines

import model.common.utils.ClassNameMapping
import model.common._
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
object Pipelines {
    val logger = Logger.getLogger(Pipelines.getClass)
    def fit(labels: Dataset[LabeledData],
            features: DataFrame,
            pipelines: PipelineConfig,
            runExplanations: Boolean): Unit = {
        // To start off, we create empty features for each example

        // lookup all the estimators
        var estimators: List[List[Any]] = List()
        for (stage <- pipelines.stages){
            var curEstimators: List[Any] = List()
            for (lookup: RegistryLookup <- stage.estimators)
                curEstimators = curEstimators :+ ClassNameMapping.mapClassNameToClass(lookup)
            estimators = estimators :+ curEstimators
        }

        var modelParams: List[List[Any]] = List()

        for (i <- 0 until pipelines.stages.length){
            // for each estimator in the stage, learn the parameters
            var curModelParams: List[Any] = List()
            for (j <- 0 until pipelines.stages(i).estimators.length){
                curModelParams = curModelParams :+
                        estimators(i)(j).asInstanceOf[ {
                            def fit(labels: Dataset[LabeledData],
                                    features: DataFrame,
                                    runExplanations: Boolean): Any
                        }].fit(labels, features, runExplanations)
            }
            modelParams = modelParams :+ curModelParams

            // Transform latest features with each estimator in this stage
            var transformResults: List[Any] = List()
            for (j <- 0 until pipelines.stages(i).estimators.length){
                transformResults = transformResults :+ estimators(i)(j).asInstanceOf[ {
                    def transform(features: DataFrame,
                                  runExplanations: Boolean,
                                  model_params: Option[Any] = None): Unit}]
                        .transform(features, runExplanations)
            }

            // combine the outputs of all the estimators in the stage and update the features table so that we can feed
            // into the next stage
        }
    }

    def transform(inputFeatures: DataFrame,
                  pipelines: PipelineConfig,
                  model_params: Option[List[List[Any]]]=None)
                 (implicit spark: SparkSession,
                  sharedParams:SharedParams): Unit = {
        var features: DataFrame = inputFeatures
        // lookup all the estimators
        var estimators: List[List[Any]] = List()
        for (i <- 0 until pipelines.stages.length){
            val stage = pipelines.stages(i)
            var curEstimators: List[Any] = List()
            for (lookup: RegistryLookup <- stage.estimators){
                curEstimators = curEstimators :+ ClassNameMapping.mapClassNameToClass(lookup, i)
            }
            estimators = estimators :+ curEstimators
        }

        for (i <- 0 until pipelines.stages.length){
            for (j <- 0 until pipelines.stages(i).estimators.length){
                features = estimators(i)(j).asInstanceOf[ {
                    def transform(features: DataFrame,
                                  stageNum: Int = -1,
                                  model_params: Option[Any] = None)
                                 (implicit spark: SparkSession,
                                  sharedParams:SharedParams): DataFrame
                }].transform(features, i)
            }
        }
        features.write.mode(SaveMode.Overwrite).parquet(sharedParams.outputFilePath)
        features.show(5, false)
    }
}
