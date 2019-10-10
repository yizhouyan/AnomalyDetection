package server.storage;

import anomalydetection.*;
import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.Dataframe;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.EstimatorspecRecord;
import jooq.sqlite.gen.tables.records.EventRecord;
import jooq.sqlite.gen.tables.records.ExampleselectoreventRecord;
import org.jooq.DSLContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jooq.sqlite.gen.Tables.EXAMPLESELECTOREVENT;

/**
 * This class contains logic for storing and reading example selector events in the database.
 *
 * An ExampleSelectorEvent is when an example source/selector operates on a DataFrame to create a new DataFrame.
 */
public class ExampleSelectorEventDao {
    /**
     * Store an ExampleSelectorEvent in the database.
     * @param se - The ExampleSelectorEvent.
     * @param ctx - The database context.
     * @return A response indicating that the ExampleSelectorEvent has been stored.
     */
    public static ExampleSelectorEventResponse store(ExampleSelectorEvent se, DSLContext ctx) {
        // Store the input DataFrame, output DataFrame, label DataFrame and EstimatorSpec.
        DataframeRecord inputDf = DataFrameDao.store(se.oldDataFrame, se.experimentRunId, ctx);
        DataframeRecord outputDf = DataFrameDao.store(se.newDataFrame, se.experimentRunId, ctx);
        DataframeRecord labelDf = DataFrameDao.store(se.labelDataFrame, se.experimentRunId, ctx);

        EstimatorspecRecord esr = EstimatorSpecDao.store(se.estimatorSpec, se.experimentRunId, ctx);

        // Store an entry in the UnsupervisedEvent table.
        ExampleselectoreventRecord seRec = ctx.newRecord(EXAMPLESELECTOREVENT);
        seRec.setId(null);
        seRec.setExperimentrun(se.experimentRunId);
        seRec.setNewdf(outputDf.getId());
        seRec.setOlddf(inputDf.getId());
        seRec.setLabeldf(labelDf.getId());
        seRec.setEstimatorspec(esr.getId());
        seRec.setStagenumber(se.stageNumber);
        seRec.store();

        // Store an entry in the Event table.
        EventRecord ev = EventDao.store(seRec.getId(), "ExampleSelector", se.experimentRunId, se.getStageNumber(), ctx);
        ExampleSelectorEventResponse response = new ExampleSelectorEventResponse(inputDf.getId(), outputDf.getId(),
                labelDf.getId(), esr.getId(), ev.getId(), seRec.getId());
        return response;
    }

    /**
     * Read the ExampleSelectorEvent associated with the given IDs.
     * @param exampleSelectorEventIds - The ExampleSelectorEvent IDs to look up.
     * @return A list of ExampleSelectorEvent, where exampleSelectorEventIds.get(i) is the ExampleSelectorEvent associated
     *  with exampleSelectorEventIds.get(i). The schema field of each ExampleSelectorEvent will be empty. This is done for performance
     *  reasons (reduces storage space and avoids extra query).
     * @throws ResourceNotFoundException - Thrown if any of the IDs do not have an associated ExampleSelectorEvent.
     */
    public static List<ExampleSelectorEvent> read(List<Integer> exampleSelectorEventIds, DSLContext ctx)
            throws ResourceNotFoundException {
        // This maps from ID to the ExampleSelectorEvent with the given ID.
        Map<Integer, ExampleSelectorEvent> exampleSelectorEventForId = new HashMap<>();

        // Set up table aliases.
        String OLD_DF_TABLE = "olddf";
        String NEW_DF_TABLE = "newdf";
        String LABEL_DF_TABLE = "labeldf";
        Dataframe oldDfTable = Tables.DATAFRAME.as(OLD_DF_TABLE);
        Dataframe newDfTable = Tables.DATAFRAME.as(NEW_DF_TABLE);
        Dataframe labelDfTable = Tables.DATAFRAME.as(LABEL_DF_TABLE);

        // Query for the given ExampleSelectorEvent IDs.
        ctx.select(
                oldDfTable.ID,
                oldDfTable.TAG,
                oldDfTable.FILEPATH,
                newDfTable.ID,
                newDfTable.TAG,
                newDfTable.FILEPATH,
                labelDfTable.ID,
                labelDfTable.TAG,
                labelDfTable.FILEPATH,
                Tables.EXAMPLESELECTOREVENT.ID,
                Tables.EXAMPLESELECTOREVENT.EXPERIMENTRUN,
                Tables.EXAMPLESELECTOREVENT.STAGENUMBER,
                Tables.ESTIMATORSPEC.ID,
                Tables.ESTIMATORSPEC.ESTIMATORTYPE,
                Tables.ESTIMATORSPEC.TAG
        )
                .from(
                        Tables.EXAMPLESELECTOREVENT
                                .join(Tables.ESTIMATORSPEC).on(Tables.EXAMPLESELECTOREVENT.ESTIMATORSPEC.eq(Tables.ESTIMATORSPEC.ID))
                                .join(Tables.DATAFRAME.as(OLD_DF_TABLE)).on(Tables.EXAMPLESELECTOREVENT.OLDDF.eq(oldDfTable.ID))
                                .join(Tables.DATAFRAME.as(NEW_DF_TABLE)).on(Tables.EXAMPLESELECTOREVENT.NEWDF.eq(newDfTable.ID))
                                .join(Tables.DATAFRAME.as(LABEL_DF_TABLE)).on(Tables.EXAMPLESELECTOREVENT.LABELDF.eq(labelDfTable.ID)))
                .where(Tables.EXAMPLESELECTOREVENT.ID.in(exampleSelectorEventIds))
                .fetch()
                .forEach(rec -> {
                    // Construct an object to represent the input DataFrame.
                    DataFrame oldDf = new DataFrame(
                            rec.get(oldDfTable.ID),
                            Collections.emptyList(),
                            rec.get(oldDfTable.TAG),
                            rec.get(oldDfTable.FILEPATH)
                    );

                    // Construct an object to represent the output DataFrame.
                    DataFrame newDf = new DataFrame(
                            rec.get(newDfTable.ID),
                            Collections.emptyList(),
                            rec.get(newDfTable.TAG),
                            rec.get(newDfTable.FILEPATH)
                    );

                    // Construct an object to represent the label DataFrame.
                    DataFrame labelDf = new DataFrame(
                            rec.get(labelDfTable.ID),
                            Collections.emptyList(),
                            rec.get(labelDfTable.TAG),
                            rec.get(labelDfTable.FILEPATH)
                    );

                    // Construct an object to represent the Transformer.
                    EstimatorSpec estimatorSpec = new EstimatorSpec(
                            rec.get(Tables.ESTIMATORSPEC.ID),
                            rec.get(Tables.ESTIMATORSPEC.ESTIMATORTYPE),
                            Collections.emptyList(),
                            rec.get(Tables.ESTIMATORSPEC.TAG)
                    );

                    // Construct the TransformEvent.
                    int expRunId = rec.get(Tables.EXAMPLESELECTOREVENT.EXPERIMENTRUN);
                    int stageNum = rec.get(Tables.EXAMPLESELECTOREVENT.STAGENUMBER);

                    ExampleSelectorEvent exampleSelectorEvent = new ExampleSelectorEvent(oldDf, labelDf, newDf, estimatorSpec,
                            expRunId, stageNum);

                    // Create the mapping from ID to ExampleSelectorEvent.
                    int exampleSelectorEventId = rec.get(Tables.EXAMPLESELECTOREVENT.ID);
                    exampleSelectorEventForId.put(exampleSelectorEventId, exampleSelectorEvent);
                });

        // Create a list of ExampleSelectorEvents.
        List<ExampleSelectorEvent> exampleSelectorEvents = new ArrayList<>();
        IntStream.range(0, exampleSelectorEventIds.size()).forEach(i -> {
            exampleSelectorEvents.add(exampleSelectorEventForId.getOrDefault(exampleSelectorEventIds.get(i), null));
        });

        // Ensure there are no ExampleSelectorEvents that could not be found.
        if (exampleSelectorEvents.contains(null)) {
            throw new ResourceNotFoundException("Could not find ExampleSelectorEvent with ID %s" + exampleSelectorEvents.indexOf(null));
        }
        return exampleSelectorEvents;
    }
}
