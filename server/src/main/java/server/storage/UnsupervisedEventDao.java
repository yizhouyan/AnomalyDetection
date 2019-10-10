package server.storage;

import anomalydetection.*;
import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.Dataframe;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.EventRecord;
import jooq.sqlite.gen.tables.records.EstimatorspecRecord;
import jooq.sqlite.gen.tables.records.UnsupervisedeventRecord;
import org.jooq.DSLContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jooq.sqlite.gen.Tables.UNSUPERVISEDEVENT;

/**
 * This class contains logic for storing and reading unsupervised events in the database.
 *
 * An UnsupervisedEvent is when an unsupervised method operates on a DataFrame to create a new DataFrame.
 */
public class UnsupervisedEventDao {
    /**
     * Store an UnsupervisedEvent in the database.
     * @param ue - The UnsupervisedEvent.
     * @param ctx - The database context.
     * @return A response indicating that the UnsupervisedEvent has been stored.
     */
    public static UnsupervisedEventResponse store(UnsupervisedEvent ue, DSLContext ctx) {
        // Store the input DataFrame, output DataFrame, and EstimatorSpec.
        DataframeRecord inputDf = DataFrameDao.store(ue.oldDataFrame, ue.experimentRunId, ctx);
        DataframeRecord outputDf = DataFrameDao.store(ue.newDataFrame, ue.experimentRunId, ctx);
        EstimatorspecRecord esr = EstimatorSpecDao.store(ue.estimatorSpec, ue.experimentRunId, ctx);

        // Store an entry in the UnsupervisedEvent table.
        UnsupervisedeventRecord ueRec = ctx.newRecord(UNSUPERVISEDEVENT);
        ueRec.setId(null);
        ueRec.setExperimentrun(ue.experimentRunId);
        ueRec.setNewdf(outputDf.getId());
        ueRec.setOlddf(inputDf.getId());
        ueRec.setEstimatorspec(esr.getId());

        // Remove duplicate columns and remove input columns from output columns. That is, if a column is considered
        // an output column, then it should NOT be considered an input column as well.
        ue.setInputColumns(
                ue.inputColumns
                        .stream()
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
        );
        ue.setOutputColumns(
                ue.outputColumns
                        .stream()
                        .distinct()
                        .filter(col -> !ue.inputColumns.contains(col))
                        .sorted()
                        .collect(Collectors.toList())
        );
        ueRec.setInputcolumns(ue.inputColumns.stream().collect(Collectors.joining(",")));
        ueRec.setOutputcolumns(ue.outputColumns.stream().collect(Collectors.joining(",")));
        ueRec.setStagenumber(ue.stageNumber);
        ueRec.store();

        // Store an entry in the Event table.
        EventRecord ev = EventDao.store(ueRec.getId(), "unsupervised", ue.experimentRunId, ue.getStageNumber(), ctx);

        return new UnsupervisedEventResponse(inputDf.getId(), outputDf.getId(), esr.getId(), ev.getId(), ueRec.getId());
    }

    /**
     * Read the UnsupervisedEvent associated with the given IDs.
     * @param unsupervisedEventIds - The UnsupervisedEvent IDs to look up.
     * @return A list of UnsupervisedEvent, where unsupervisedEvents.get(i) is the UnsupervisedEvent associated
     *  with unsupervisedEventIds.get(i). The schema field of each UnsupervisedEvent will be empty. This is done for performance
     *  reasons (reduces storage space and avoids extra query).
     * @throws ResourceNotFoundException - Thrown if any of the IDs do not have an associated UnsupervisedEvent.
     */
    public static List<UnsupervisedEvent> read(List<Integer> unsupervisedEventIds, DSLContext ctx)
            throws ResourceNotFoundException {
        // This maps from ID to the UnsupervisedEvent with the given ID.
        Map<Integer, UnsupervisedEvent> unsupervisedEventForId = new HashMap<>();

        // Set up table aliases.
        String OLD_DF_TABLE = "olddf";
        String NEW_DF_TABLE = "newdf";
        Dataframe oldDfTable = Tables.DATAFRAME.as(OLD_DF_TABLE);
        Dataframe newDfTable = Tables.DATAFRAME.as(NEW_DF_TABLE);

        // Query for the given UnsupervisedEvent IDs.
        ctx.select(
                oldDfTable.ID,
                oldDfTable.TAG,
                oldDfTable.FILEPATH,
                newDfTable.ID,
                newDfTable.TAG,
                newDfTable.FILEPATH,
                Tables.UNSUPERVISEDEVENT.ID,
                Tables.UNSUPERVISEDEVENT.INPUTCOLUMNS,
                Tables.UNSUPERVISEDEVENT.OUTPUTCOLUMNS,
                Tables.UNSUPERVISEDEVENT.EXPERIMENTRUN,
                Tables.UNSUPERVISEDEVENT.STAGENUMBER,
                Tables.ESTIMATORSPEC.ID,
                Tables.ESTIMATORSPEC.ESTIMATORTYPE,
                Tables.ESTIMATORSPEC.TAG
        )
                .from(
                        Tables.UNSUPERVISEDEVENT
                                .join(Tables.ESTIMATORSPEC).on(Tables.UNSUPERVISEDEVENT.ESTIMATORSPEC.eq(Tables.ESTIMATORSPEC.ID))
                                .join(Tables.DATAFRAME.as(OLD_DF_TABLE)).on(Tables.UNSUPERVISEDEVENT.OLDDF.eq(oldDfTable.ID))
                                .join(Tables.DATAFRAME.as(NEW_DF_TABLE)).on(Tables.UNSUPERVISEDEVENT.NEWDF.eq(newDfTable.ID)))
                .where(Tables.UNSUPERVISEDEVENT.ID.in(unsupervisedEventIds))
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

                    // Construct an object to represent the Transformer.
                    EstimatorSpec estimatorSpec = new EstimatorSpec(
                            rec.get(Tables.ESTIMATORSPEC.ID),
                            rec.get(Tables.ESTIMATORSPEC.ESTIMATORTYPE),
                            Collections.emptyList(),
                            rec.get(Tables.ESTIMATORSPEC.TAG)
                    );

                    // Construct the TransformEvent.
                    int expRunId = rec.get(Tables.UNSUPERVISEDEVENT.EXPERIMENTRUN);
                    int stageNum = rec.get(Tables.UNSUPERVISEDEVENT.STAGENUMBER);
                    List<String> inputCols = Arrays.asList(rec.get(Tables.UNSUPERVISEDEVENT.INPUTCOLUMNS).split(","));
                    List<String> outputCols = Arrays.asList(rec.get(Tables.UNSUPERVISEDEVENT.OUTPUTCOLUMNS).split(","));
                    UnsupervisedEvent unsupervisedEvent = new UnsupervisedEvent(oldDf, newDf, estimatorSpec, inputCols, outputCols,
                            expRunId, stageNum);

                    // Create the mapping from ID to TransformEvent.
                    int unsupervisedEventId = rec.get(Tables.UNSUPERVISEDEVENT.ID);
                    unsupervisedEventForId.put(unsupervisedEventId, unsupervisedEvent);
                });

        // Create a list of TransformEvents.
        List<UnsupervisedEvent> unsupervisedEvents = new ArrayList<>();
        IntStream.range(0, unsupervisedEventIds.size()).forEach(i -> {
            unsupervisedEvents.add(unsupervisedEventForId.getOrDefault(unsupervisedEventIds.get(i), null));
        });

        // Ensure there are no UnsupervisedEvents that could not be found.
        if (unsupervisedEvents.contains(null)) {
            throw new ResourceNotFoundException("Could not find TransformEvent with ID %s" + unsupervisedEvents.indexOf(null));
        }
        return unsupervisedEvents;
    }
}
