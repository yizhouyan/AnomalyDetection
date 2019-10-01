package client


import scala.collection.mutable

/**
  * Created by yizhouyan on 9/30/19.
  */
object SyncableEstimator {
    /**
      * Create a sequence of ModelDB Hyperparameters from a Spark ParamMap.
      * @param pm - The ParamMap.
      * @return The Hyperparameters extracted from the ParamMap.
      */
    def extractHyperParameters(pm: mutable.Map[Any, Any]): Seq[anomalydetection.HyperParameter] = {
        pm.toSeq.map { (pair) =>
            // Note that a transformer spec should override the bounds [Float.MinValue, Float.MaxValue] as appropriate.
            anomalydetection.HyperParameter(pair._1.toString,
                pair._2.toString,
                pair._2.getClass.getSimpleName)
        }
    }

    /**
      * Create a TransformerSpec from an Estimator.
      *
      * @param estimator - The Estimator. We set it as a PipelineStage because we can't easily type parametrize all
      *                  Estimators.
      * @return A TransformerSpec.
      */
    def apply(estimator: IEvent)(implicit mdbs: Option[ModelStorageSyncer]): anomalydetection.EstimatorSpec = {
        // Default values.
        var id = mdbs.get.id(estimator).getOrElse(-1)
        val tag = mdbs.get.tag(estimator).getOrElse("")
        var name = estimator.getName()
        var hyperparameters = extractHyperParameters(estimator.getHyperParameters())

        anomalydetection.EstimatorSpec(
            id,
            name,
            hyperparameters,
            tag
        )
    }
}
