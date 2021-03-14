package model.pipelines.tools

import breeze.linalg.max
import model.common.SharedParams
import model.pipelines.unsupervised.examples.iforest.{IsolationForest, IsolationForestParams}
import model.pipelines.unsupervised.examples.{KNNBasedDetection, KNNBasedDetectionParams, LOF, LOFParams, Mahalanobis, MahalanobisParams, StandardScaler, StandardScalerParams}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.feature.{RobustScaler, VectorAssembler}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.ml.classification.SVM


object ToolsModel {

    def run_LOF(X: DataFrame, k: Int)
               (implicit spark: SparkSession, sharedParams:SharedParams)
    : DataFrame ={
        val lofParams = LOFParams(List(k), "lof_result", useSubspace = false, None, None)
        val LOF_estimator = new LOF(lofParams, -1)
        val lof_res = LOF_estimator.transform(X, -1)
        val lof_score = lof_res.withColumnRenamed(lof_res.columns.last, "scores")
        val score = lof_score.select("id", "scores")
        score
    }

    def run_KNN(X: DataFrame, k: Int)
               (implicit spark: SparkSession, sharedParams:SharedParams)
    : DataFrame ={
        val knnParams = KNNBasedDetectionParams(List(k), "knn_result", useSubspace = false, None, None)
        val knn_estimator = new KNNBasedDetection(knnParams, -1)
        val knn_res = knn_estimator.transform(X, -1)
        val knn_score = knn_res.withColumnRenamed(knn_res.columns.last, "scores")
        val score = knn_score.select("id", "scores")
        score
    }


    def run_Isolation_Forest(X: DataFrame, k: Double)
                            (implicit spark: SparkSession, sharedParams:SharedParams)
    : DataFrame = {
        val ifParams = IsolationForestParams("iforest_result", 100, 1, k, 10, 0.1, bootstrap = false,
                                             getClass.getName.hashCode.toLong)
        val if_estimator = new IsolationForest(ifParams, -1)
        val if_res = if_estimator.transform(X, -1)
        val if_score = if_res.withColumnRenamed("iforest_result", "scores")
        val score = if_score.select("id", "scores")
        score
    }

    def run_Mahalanobis(X: DataFrame)
                       (implicit spark: SparkSession, sharedParams:SharedParams)
    : DataFrame = {
        val maParams = MahalanobisParams("mahalanobis_result", useSubspace = false, None, None)
        val ma_estimator = new Mahalanobis(maParams, -1)
        val ma_res = ma_estimator.transform(X, -1)
        val ma_score = ma_res.withColumnRenamed("mahalanobis_result_subspace_0", "scores")
        val score = ma_score.select("id", "scores")
        score
    }

    def run_StandardScaler(oriData: DataFrame)
                          (implicit spark: SparkSession, sharedParams:SharedParams)
    : DataFrame = {
        val standardScalerParam = StandardScalerParams(None, None)
        val StandardScalerModel = new StandardScaler(standardScalerParam, -1)
        val data = StandardScalerModel.transform(oriData, -1, None)
        data
    }


    def standardScaler(df: DataFrame)
    : DataFrame = {
        val assembler = new VectorAssembler()
          .setInputCols(df.drop("id").columns)
          .setOutputCol("featuresVec")
        val transformVector = assembler.transform(df)
        val scaler = new org.apache.spark.ml.feature.StandardScaler()
          .setInputCol("featuresVec")
          .setOutputCol("features")
          .setWithStd(true)
          .setWithMean(false)
        val scalerModel = scaler.fit(transformVector)
        val scaled_data = scalerModel.transform(transformVector)
        scaled_data
    }

    def robustScaler(data_forTraining: DataFrame): DataFrame = {
        val assembler = new VectorAssembler()
          .setInputCols(data_forTraining.drop("id").columns)
          .setOutputCol("scaler_inputFeatures")
        val transformVector = assembler.transform(data_forTraining)
        val scaler = new RobustScaler()
          .setInputCol("scaler_inputFeatures")
          .setOutputCol("features")
          .setWithCentering(true)
        val scalerModel = scaler.fit(transformVector)
        val scaled_data = scalerModel.transform(transformVector)
        scaled_data
    }

    def assemblerScaler(df: DataFrame): DataFrame = {
        val assembler = new VectorAssembler()
          .setInputCols(df.drop("id").columns)
          .setOutputCol("features")
        val featuresDF = assembler.transform(df)
        featuresDF
    }

    def LR(training_dataLR: DataFrame, scaled_dataLR: DataFrame): DataFrame = {
        val logisticRegression = new LogisticRegression().setMaxIter(20)
        val logisticRegressionModel = logisticRegression.fit(training_dataLR)
        // add new columns rawPrediction, probability and prediction
        var predictionLR = logisticRegressionModel.transform(scaled_dataLR)
        // process Probability:
        val probabilityUDF = udf { (probability: org.apache.spark.ml.linalg.Vector) =>
            max(probability(0), probability(1))
        }
        val probUDF = probabilityUDF(col("probability"))
        predictionLR = predictionLR.withColumn("probability", probUDF)
        predictionLR
    }

    import org.apache.spark.ml.feature.MaxAbsScaler
    def SVM(training_dataSVM: DataFrame, dataSVM: DataFrame)
                   (implicit spark: SparkSession)
    : DataFrame = {
        import spark.implicits._
        // TrainingData
        val assembler1 = new VectorAssembler()
          .setInputCols(training_dataSVM.drop("id", "label").columns.toList.toArray)
          .setOutputCol("featureVec")
        val featuresForTrainSVM = assembler1.transform(training_dataSVM)
        // TransformData
        val assembler2 = new VectorAssembler()
          .setInputCols(dataSVM.drop("id", "label").columns.toList.toArray)
          .setOutputCol("featureVec")
        val featuresForSVM = assembler2.transform(dataSVM)
        // run_SVM:
        val scaler = new MinMaxScaler().setInputCol("featureVec").setOutputCol("scaler_features")
        val svm = new SVM().setKernelType("linear").setMaxIter(100).setFeaturesCol("scaler_features")
        val model = new Pipeline().setStages(Array(scaler, svm))
        val modelsvm = model.fit(featuresForTrainSVM)
        var scoresSVM = modelsvm.transform(featuresForSVM).cache()
        // score: change "prediction" title to "score"
        scoresSVM = scoresSVM.withColumnRenamed("prediction", "score")
        // prediction:
        val predictFunc :(Double => Int) = (arg: Double) => {if (arg < 0) 0 else 1}
        val predictUDF = udf(predictFunc)
        val predictSVM = scoresSVM.withColumn("prediction", predictUDF(scoresSVM("score")))
        // probability --- PlattScaling:
        val score = predictSVM.select("score").map(r => r.getDouble(0)).collect.toList
        val label = predictSVM.select("prediction").map(r => r.getInt(0)).collect.toList
        val pairs: List[(Double, Int)] = score zip label
        val platt = PlattScaling.trainFromRawScores(pairs.map{_._1}, 2)
        var proba: List[(Int, Double)] = List()
        for(i <- score.indices)
            proba ++= List((i+1, platt.scale(score(i))))
        val paobaDF = proba.toDF("id", "tmp_rawProbability")
        var predictionSVM = predictSVM.join(paobaDF, Seq("id"), "left")
        // process_probability:
        predictionSVM = maxScaler(predictionSVM)

        predictionSVM
    }


    def maxScaler(df: DataFrame): DataFrame ={
        var resDF = df
        val assembler = new VectorAssembler()
          .setInputCols(Array("tmp_rawProbability"))
          .setOutputCol("rawProbability")
        resDF = assembler.transform(df)
        val scalerprob = new MaxAbsScaler()
          .setInputCol("rawProbability")
          .setOutputCol("rProbability")
        // Compute summary statistics and generate MaxAbsScalerModel
        val scalerModel = scalerprob.fit(resDF)
        // rescale each feature to range [-1, 1]
        resDF = scalerModel.transform(resDF)
        val probabilityUDF = udf { (probability: org.apache.spark.ml.linalg.Vector) => probability(0) }
        resDF = resDF.withColumn("probability", probabilityUDF(col("rProbability")))
        resDF
    }
}