package model.pipelines.supervised

import client.event.UnsupervisedEvent
import client.{IEvent, ModelStorageSyncer}
import model.common.LabeledExamples
import model.pipelines.AbstractEstimator
import org.apache.spark.sql.{DataFrame, Dataset}

/**
 * Created by yizhouyan on 9/7/19.
 */
abstract class AbstractSupervisedAlgo extends AbstractEstimator{
}
