package selector.example_sources

import client.event.ExampleSelectorEvent
import client.{IEvent, ModelStorageSyncer}
import org.apache.spark.sql.{Dataset, SparkSession}
import selector.common.{Example, LabeledExample, SharedParams}

/**
 * Created by yizhouyan on 9/7/19.
 */
abstract class AbstractExampleSource extends IEvent{
    def fetch(labeledExample: Dataset[LabeledExample])
             (implicit spark: SparkSession, sharedParams: SharedParams): Dataset[Example]
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
