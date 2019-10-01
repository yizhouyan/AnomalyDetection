package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.*;
import anomalydetection.*;
import org.jooq.DSLContext;
import org.jooq.Record1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class contains logic for storing and reading fitting events.
 *
 * A FitEvent is when a EstimatorSpec is trained on a DataFrame to produce a Model.
 */
public class FitEventDao {
    /**
     * Read info about a FitEvent for a given model (recall that a model is a Transformer produced by a FitEvent).
     * @param modelId - The ID of a Transformer.
     * @param ctx - The database context.
     * @return The response that was generated when the FitEvent was stored.
     * @throws ResourceNotFoundException - Thrown if there's no Transformer with ID modelId.
     */
    public static FitEventResponse readResponse(int modelId, DSLContext ctx) throws ResourceNotFoundException {
        // Find the FitEvent that produced the Transformer with ID modelId.
        int fitEventId = getFitEventIdForModelId(modelId, ctx);
        FitEvent fe = read(fitEventId, ctx);

        // Read the entry in the Event table that was stored for this FitEvent.
        int eventId = EventDao.getEventId(fitEventId, "fit", ctx);

        // Return the response.
        return new FitEventResponse(fe.df.id, fe.spec.id, fe.model.id, eventId, fitEventId);
    }

    /**
     * Store a FitEvent in the database.
     * @param fe - The FitEvent.
     * @param ctx - The database context.
     * @return A response indicating that the FitEvent has been stored.
     */
    public static FitEventResponse store(FitEvent fe, DSLContext ctx) {
        // First, attempt to read the FitEvent if it's already stored.
        try {
            return readResponse(fe.model.id, ctx);
        } catch (ResourceNotFoundException rnf) {}

        // Store DataFrame, Model, and EstimatorSpec.
        DataframeRecord df = DataFrameDao.store(fe.df, fe.experimentRunId, ctx);
        ModelRecord t = ModelDao.store(fe.model, fe.experimentRunId, ctx);
        EstimatorspecRecord s = EstimatorSpecDao.store(fe.spec, fe.experimentRunId, ctx);

        // Store the FitEvent.
        FiteventRecord feRec = ctx.newRecord(Tables.FITEVENT);
        feRec.setId(null);
        feRec.setExperimentrun(fe.experimentRunId);
        feRec.setDf(df.getId());
        feRec.setModel(t.getId());
        feRec.setEstimatorspec(s.getId());
        feRec.setStagenumber(fe.stageNumber);

        // Make sure the prediction, label, and feature columns do not have duplicates.
        // Make sure the label columns and feature columns do not use a prediction column.
        fe.setFeatureColumns(
                fe.featureColumns
                        .stream()
                        .distinct()
                        .collect(Collectors.toList())
        );
        fe.setLabelColumns(
                fe.labelColumns
                        .stream()
                        .distinct()
                        .filter(col -> !fe.featureColumns.contains(col))
                        .sorted()
                        .collect(Collectors.toList())
        );

        feRec.setInputcolumns(fe.featureColumns.stream().collect(Collectors.joining(",")));
        feRec.setLabelcolumns(fe.labelColumns.stream().collect(Collectors.joining(",")));
        feRec.store();

        // Store Event.
        EventRecord ev = EventDao.store(feRec.getId(),"fit", fe.experimentRunId, fe.stageNumber, ctx);

        // Return the response.
        return new FitEventResponse(df.getId(), s.getId(), t.getId(), ev.getId(), feRec.getId());
    }

    /**
     * Read the FitEvent with the given ID.
     * @param fitEventId - The ID of the FitEvent.
     * @param ctx - The database context.
     * @return The FitEvent with ID fitEventId.
     * @throws ResourceNotFoundException - Thrown if there's no FitEvent with the given ID.
     */
    public static FitEvent read(int fitEventId, DSLContext ctx) throws ResourceNotFoundException {
        return read(Collections.singletonList(fitEventId), ctx).get(0);
    }

    /**
     * Read the FitEvent that produced the given model.
     * @param modelId - The ID of a Model.
     * @param ctx - The database context.
     * @return The ID of the FitEvent that created the Model with ID modelId.
     * @throws ResourceNotFoundException - Thrown if there's no FitEvent that produced the Model with the ID
     * modelId.
     */
    public static int getFitEventIdForModelId(int modelId, DSLContext ctx) throws ResourceNotFoundException {
        Record1<Integer> rec =
                ctx.select(Tables.FITEVENT.ID).from(Tables.FITEVENT).where(Tables.FITEVENT.MODEL.eq(modelId)).fetchOne();

        if (rec == null) {
            throw new ResourceNotFoundException(String.format("Could not find FitEvent for model %d", modelId));
        }

        return rec.value1();
    }

    /**
     * Read the FitEvents associated with the given IDs.
     * @param fitEventIds - The FitEvent IDs to look up.
     * @return A list of FitEvents, fitEvents, where fitEvents.get(i) is the FitEvent associated
     *  with fitEventIds.get(i). The schema field of each FitEvent will be empty. This is done for performance
     *  reasons (reduces storage space and avoids extra query).
     * @throws ResourceNotFoundException - Thrown if any of the IDs do not have an associated FitEvent.
     */
    public static List<FitEvent> read(List<Integer> fitEventIds, DSLContext ctx)
            throws ResourceNotFoundException {
        Map<Integer, FitEvent> fitEventForId = new HashMap<>();

        // Run a query to get all the fields we care about.
        ctx.select(
                Tables.DATAFRAME.ID,
                Tables.DATAFRAME.TAG,
                Tables.DATAFRAME.FILEPATH,
                Tables.ESTIMATORSPEC.ID,
                Tables.ESTIMATORSPEC.ESTIMATORTYPE,
                Tables.ESTIMATORSPEC.TAG,
                Tables.FITEVENT.ID,
                Tables.FITEVENT.INPUTCOLUMNS,
                Tables.FITEVENT.LABELCOLUMNS,
                Tables.FITEVENT.EXPERIMENTRUN,
                Tables.FITEVENT.STAGENUMBER,
                Tables.MODEL.ID,
                Tables.MODEL.MODELTYPE,
                Tables.MODEL.TAG,
                Tables.MODEL.FILEPATH
        )
                .from(
                        Tables.FITEVENT
                                .join(Tables.MODEL).on(Tables.FITEVENT.MODEL.eq(Tables.MODEL.ID))
                                .join(Tables.DATAFRAME).on(Tables.FITEVENT.DF.eq(Tables.DATAFRAME.ID))
                                .join(Tables.ESTIMATORSPEC).on(Tables.FITEVENT.ESTIMATORSPEC.eq(Tables.ESTIMATORSPEC.ID)))
                .where(Tables.FITEVENT.ID.in(fitEventIds))
                .fetch()
                .forEach(rec -> {
                    // For each record returned from the query, we'll generate a FitEvent. First, we'll make a DataFrame.
                    DataFrame df = new DataFrame(
                            rec.get(Tables.DATAFRAME.ID),
                            Collections.emptyList(),
                            rec.get(Tables.DATAFRAME.TAG),
                            rec.get(Tables.DATAFRAME.FILEPATH)
                    );

                    // Now make a EstimatorSpec.
                    EstimatorSpec spec = new EstimatorSpec(
                            rec.get(Tables.ESTIMATORSPEC.ID),
                            rec.get(Tables.ESTIMATORSPEC.ESTIMATORTYPE),
                            Collections.emptyList(),
                            rec.get(Tables.ESTIMATORSPEC.TAG)
                    );

                    // Now make a Model.
                    Model model = new Model(
                            rec.get(Tables.MODEL.ID),
                            rec.get(Tables.MODEL.MODELTYPE),
                            rec.get(Tables.MODEL.TAG),
                            rec.get(Tables.MODEL.FILEPATH)
                    );

                    // Make the columns and experiment run ID.
                    int expRunId = rec.get(Tables.FITEVENT.EXPERIMENTRUN);
                    List<String> featureCols = Arrays.asList(rec.get(Tables.FITEVENT.INPUTCOLUMNS).split(","));
                    List<String> labelCols = Arrays.asList(rec.get(Tables.FITEVENT.LABELCOLUMNS).split(","));
                    int stageNum = rec.get(Tables.FITEVENT.STAGENUMBER);

                    // Put the FitEvent together.
                    FitEvent fitEvent = new FitEvent(
                            df,
                            spec,
                            model,
                            featureCols,
                            labelCols,
                            expRunId,
                            stageNum
                    );
                    // Create a mapping from ID to the FitEvent.
                    int fitEventId = rec.get(Tables.FITEVENT.ID);
                    fitEventForId.put(fitEventId, fitEvent);
                });

        // Turn the map into an array.
        List<FitEvent> fitEvents = new ArrayList<>();
        IntStream.range(0, fitEventIds.size()).forEach(i -> {
            fitEvents.add(fitEventForId.getOrDefault(fitEventIds.get(i), null));
        });

        // Check if any of the FitEvents could not be found, and if so, throw an Exception.
        if (fitEvents.contains(null)) {
            throw new ResourceNotFoundException("Could not find FitEvent with ID %s" + fitEvents.indexOf(null));
        }
        return fitEvents;
    }

    /**
     * Get the ID of the DataFrame that produced the model with the given ID.
     * @param modelId - The id of the produced model.
     * @param ctx - Jooq context.
     * @return The ID (or -1 if this model was not created by a FitEvent).
     */
    public static int getParentDfId(int modelId, DSLContext ctx) throws ResourceNotFoundException {
        FiteventRecord rec = ctx
                .selectFrom(Tables.FITEVENT)
                .where(Tables.FITEVENT.MODEL.eq(modelId))
                .fetchOne();
        if (rec == null) {
            throw new ResourceNotFoundException(String.format(
                    "Could not find the DataFrame that produced Transformer %d because that Transformer doesn't exist",
                    modelId
            ));
        }
        return rec.getDf();
    }
}
