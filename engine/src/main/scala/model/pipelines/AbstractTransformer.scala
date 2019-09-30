package model.pipelines

import model.common.LabeledExamples
import org.apache.spark.sql.{DataFrame, Dataset}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractTransformer extends AbstractEvent{
    override def fit(labels: Dataset[LabeledExamples], features: DataFrame, runExplanations: Boolean) : Any= {

    }
}
