package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.EventRecord;
import jooq.sqlite.gen.tables.records.ModelmetriceventRecord;
import jooq.sqlite.gen.tables.records.ModelRecord;
import anomalydetection.ModelMetricEvent;
import anomalydetection.ModelMetricEventResponse;
import org.jooq.DSLContext;

/**
 * This class contains logic for storing and reading metric events.
 */
public class ModelMetricEventDao {
  /**
   * Store a MetricEvent in the database.
   * @param me - The metric event.
   * @param ctx - The database context.
   * @return A response indicating that the ModelMetricEvent has been stored.
   */
  public static ModelMetricEventResponse store(ModelMetricEvent me, DSLContext ctx) {
    // Store the DataFrame being evaluated.
    DataframeRecord df = DataFrameDao.store(me.df, me.experimentRunId, ctx);

    // Store the Model being evaluated.
    ModelRecord t = ModelDao.store(me.model, me.experimentRunId, ctx);

    // Store an entry in the ModelMetricEvent table.
    ModelmetriceventRecord meRec = ctx.newRecord(Tables.MODELMETRICEVENT);
    meRec.setId(null);
    meRec.setModel(t.getId());
    meRec.setDf(df.getId());
    meRec.setMetrictype(me.metricType);
    meRec.setMetricvalue(Double.valueOf(me.metricValue).floatValue());
    meRec.setExperimentrun(me.experimentRunId);
    meRec.store();

    // Store an entry in the Event table.
    EventRecord ev = EventDao.store(meRec.getId(), "model_metric", me.experimentRunId, -1, ctx);

    return new ModelMetricEventResponse(t.getId(), df.getId(), ev.getId(), meRec.getId());
  }
}
