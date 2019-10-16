package selector.example_selectors

import client.{IEvent, ModelStorageSyncer}
import client.event.ExampleSelectorEvent
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}

/**
  * Created by yizhouyan on 9/7/19.
  */
abstract class AbstractExampleSelector extends IEvent{
    def fetch(allExample: Dataset[Example], labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame

    def saveExampleSelectorEventsToDB(exampleSelector: IEvent,
                                      inputDataframe: Dataset[_],
                                      outputDataframe: Dataset[_],
                                      labelDataframe: Dataset[_],
                                      stageNum: Int)
                                     (implicit mdbs: Option[ModelStorageSyncer]): Unit= {
        ExampleSelectorEvent(exampleSelector,
            inputDataframe,
            outputDataframe,
            labelDataframe,
            stageNum
        ).sync(mdbs.get.client.get, Some(mdbs.get))
    }
}
