package model.pipelines

import model.common.utils.ClassNameMapping
import model.common._
import model.pipelines.unsupervised.{IsolationForest, IsolationForestParams}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by yizhouyan on 9/7/19.
  */
object Pipelines {
    def fit(labels: Dataset[LabeledExamples],
            features: Dataset[Feature],
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
                            def fit(labels: Dataset[LabeledExamples],
                                    features: Dataset[Feature],
                                    runExplanations: Boolean): Any
                        }].fit(labels, features, runExplanations)
            }
            modelParams = modelParams :+ curModelParams

            // Transform latest features with each estimator in this stage
            var transformResults: List[Any] = List()
            for (j <- 0 until pipelines.stages(i).estimators.length){
                transformResults = transformResults :+ estimators(i)(j).asInstanceOf[ {
                    def transform(features: Dataset[Feature],
                                  runExplanations: Boolean,
                                  model_params: Option[Any] = None): Unit}]
                        .transform(features, runExplanations)
            }

            // combine the outputs of all the estimators in the stage and update the features table so that we can feed
            // into the next stage
        }
    }

    def transform(features: Dataset[Feature],
                  pipelines: PipelineConfig,
                  runExplanations: Boolean,
                  model_params: Option[List[List[Any]]]=None)
                 (implicit spark: SparkSession,
                  saveToDB: Boolean,
                  finalOutputPath: String): Unit = {
        // To start off, we create empty features for each example

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
            var transformResults: List[Any] = List()
            for (j <- 0 until pipelines.stages(i).estimators.length){
                transformResults = transformResults :+ estimators(i)(j).asInstanceOf[ {
                    def transform(features: Dataset[Feature],
                                  runExplanations: Boolean,
                                  stageNum: Int = -1,
                                  model_params: Option[Any] = None)
                                 (implicit spark: SparkSession,
                                  saveToDB: Boolean,
                                  finalOutputPath: String): Unit
                }].transform(features, runExplanations, i)
            }
            // combine the outputs of all the estimators in the stage and update the features table so that we can feed
            // into the next stage
        }
    }
}
