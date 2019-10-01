package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.Dataframe;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.EventRecord;
import jooq.sqlite.gen.tables.records.ModelRecord;
import jooq.sqlite.gen.tables.records.TransformeventRecord;
import anomalydetection.*;
import org.jooq.DSLContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jooq.sqlite.gen.Tables.TRANSFORMEVENT;

/**
 * This class contains logic for storing and reading transformation events in the database.
 *
 * A TransformEvent is when a Transformer operates on a DataFrame to create a new DataFrame.
 */
public class TransformEventDao {
    /**
     * Store a TransformEvent in the database.
     * @param te - The TransformEvent.
     * @param ctx - The database context.
     * @return A response indicating that the TransformEvent has been stored.
     */
    public static TransformEventResponse store(TransformEvent te, DSLContext ctx) {
        // Store the input DataFrame, output DataFrame, and Transformer.
        DataframeRecord inputDf = DataFrameDao.store(te.oldDataFrame, te.experimentRunId, ctx);
        DataframeRecord outputDf = DataFrameDao.store(te.newDataFrame, te.experimentRunId, ctx);
        ModelRecord t = ModelDao.store(te.model, te.experimentRunId, ctx);

        // Store an entry in the TransformEvent table.
        TransformeventRecord teRec = ctx.newRecord(TRANSFORMEVENT);
        teRec.setId(null);
        teRec.setExperimentrun(te.experimentRunId);
        teRec.setNewdf(outputDf.getId());
        teRec.setOlddf(inputDf.getId());
        teRec.setModel(t.getId());

        // Remove duplicate columns and remove input columns from output columns. That is, if a column is considered
        // an output column, then it should NOT be considered an input column as well.
        te.setInputColumns(
                te.inputColumns
                        .stream()
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
        );
        te.setOutputColumns(
                te.outputColumns
                        .stream()
                        .distinct()
                        .filter(col -> !te.inputColumns.contains(col))
                        .sorted()
                        .collect(Collectors.toList())
        );
        teRec.setInputcolumns(te.inputColumns.stream().collect(Collectors.joining(",")));
        teRec.setOutputcolumns(te.outputColumns.stream().collect(Collectors.joining(",")));
        teRec.setPredictioncolumn(te.predictionColumn);
        teRec.setStagenumber(te.stageNumber);
        teRec.store();

        // Store an entry in the Event table.
        EventRecord ev = EventDao.store(teRec.getId(), "transform", te.experimentRunId, te.getStageNumber(), ctx);

        return new TransformEventResponse(inputDf.getId(), outputDf.getId(), t.getId(), ev.getId(), teRec.getId());
    }

    /**
     * Read the TransformEvents associated with the given IDs.
     * @param transformEventIds - The TransformEvent IDs to look up.
     * @return A list of TransformEvents, transformEvents, where transformEvents.get(i) is the TransformEvent associated
     *  with transformEventIds.get(i). The schema field of each TransformEvent will be empty. This is done for performance
     *  reasons (reduces storage space and avoids extra query).
     * @throws ResourceNotFoundException - Thrown if any of the IDs do not have an associated TransformEvent.
     */
    public static List<TransformEvent> read(List<Integer> transformEventIds, DSLContext ctx)
            throws ResourceNotFoundException {
        // This maps from ID to the TransformEvent with the given ID.
        Map<Integer, TransformEvent> transformEventForId = new HashMap<>();

        // Set up table aliases.
        String OLD_DF_TABLE = "olddf";
        String NEW_DF_TABLE = "newdf";
        Dataframe oldDfTable = Tables.DATAFRAME.as(OLD_DF_TABLE);
        Dataframe newDfTable = Tables.DATAFRAME.as(NEW_DF_TABLE);

        // Query for the given TransformEvent IDs.
        ctx.select(
                oldDfTable.ID,
                oldDfTable.TAG,
                oldDfTable.FILEPATH,
                newDfTable.ID,
                newDfTable.TAG,
                newDfTable.FILEPATH,
                Tables.TRANSFORMEVENT.ID,
                Tables.TRANSFORMEVENT.INPUTCOLUMNS,
                Tables.TRANSFORMEVENT.OUTPUTCOLUMNS,
                Tables.TRANSFORMEVENT.EXPERIMENTRUN,
                Tables.TRANSFORMEVENT.PREDICTIONCOLUMN,
                Tables.TRANSFORMEVENT.STAGENUMBER,
                Tables.MODEL.ID,
                Tables.MODEL.MODELTYPE,
                Tables.MODEL.TAG,
                Tables.MODEL.FILEPATH
        )
                .from(
                        Tables.TRANSFORMEVENT
                                .join(Tables.MODEL).on(Tables.TRANSFORMEVENT.MODEL.eq(Tables.MODEL.ID))
                                .join(Tables.DATAFRAME.as(OLD_DF_TABLE)).on(Tables.TRANSFORMEVENT.OLDDF.eq(oldDfTable.ID))
                                .join(Tables.DATAFRAME.as(NEW_DF_TABLE)).on(Tables.TRANSFORMEVENT.NEWDF.eq(newDfTable.ID)))
                .where(Tables.TRANSFORMEVENT.ID.in(transformEventIds))
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
                    Model model = new Model(
                            rec.get(Tables.MODEL.ID),
                            rec.get(Tables.MODEL.MODELTYPE),
                            rec.get(Tables.MODEL.TAG),
                            rec.get(Tables.MODEL.FILEPATH)
                    );

                    // Construct the TransformEvent.
                    int expRunId = rec.get(Tables.TRANSFORMEVENT.EXPERIMENTRUN);
                    String predictCol = rec.get(Tables.TRANSFORMEVENT.PREDICTIONCOLUMN);
                    int stageNum = rec.get(Tables.TRANSFORMEVENT.STAGENUMBER);
                    List<String> inputCols = Arrays.asList(rec.get(Tables.TRANSFORMEVENT.INPUTCOLUMNS).split(","));
                    List<String> outputCols = Arrays.asList(rec.get(Tables.TRANSFORMEVENT.OUTPUTCOLUMNS).split(","));
                    TransformEvent transformEvent = new TransformEvent(oldDf, newDf, model, inputCols, outputCols,
                            predictCol,expRunId, stageNum);

                    // Create the mapping from ID to TransformEvent.
                    int transformEventId = rec.get(Tables.TRANSFORMEVENT.ID);
                    transformEventForId.put(transformEventId, transformEvent);
                });

        // Create a list of TransformEvents.
        List<TransformEvent> transformEvents = new ArrayList<>();
        IntStream.range(0, transformEventIds.size()).forEach(i -> {
            transformEvents.add(transformEventForId.getOrDefault(transformEventIds.get(i), null));
        });

        // Ensure there are no TransformEvents that could not be found.
        if (transformEvents.contains(null)) {
            throw new ResourceNotFoundException("Could not find TransformEvent with ID %s" + transformEvents.indexOf(null));
        }
        return transformEvents;
    }
}
