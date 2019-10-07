package model.pipelines.tools

import org.apache.spark.ml.knn.{KNN, KNNModel}
import org.apache.spark.sql.Dataset

object KNN {
    // TODO this function does not work when all points are the same
    def computeKNN(data: Dataset[_],
                   k: Int,
                   featureColName: String = "featureVec",
                   indexColName: String = "id"
                  ) ={
        import data.sparkSession.implicits._
        val knn = new KNN()
                .setFeaturesCol(featureColName)
                .setAuxCols(Array(indexColName))
                .setTopTreeSize(data.count().toInt / 500)
                .setK(k + 1)
        val knnModel:KNNModel = knn.fit(data)
        knnModel.setDistanceCol("distances")
                .transform(data).drop($"featureVec")
    }
}
