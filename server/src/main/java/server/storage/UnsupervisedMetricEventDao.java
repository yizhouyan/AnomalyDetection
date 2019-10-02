package server.storage;

import anomalydetection.EstimatorSpec;
import anomalydetection.UnsupervisedMetricEvent;
import anomalydetection.UnsupervisedMetricEventResponse;
import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.EventRecord;
import jooq.sqlite.gen.tables.records.EstimatorspecRecord;
import jooq.sqlite.gen.tables.records.UnsupervisedmetriceventRecord;
import org.jooq.DSLContext;

/**
 * This class contains logic for storing and reading unsupervised metric events.
 */
public class UnsupervisedMetricEventDao {
  /**
   * Store a MetricEvent in the database.
   * @param me - The metric event.
   * @param ctx - The database context.
   * @return A response indicating that the ModelMetricEvent has been stored.
   */
  public static UnsupervisedMetricEventResponse store(UnsupervisedMetricEvent me, DSLContext ctx) {
    // Store the DataFrame being evaluated.
    DataframeRecord df = DataFrameDao.store(me.df, me.experimentRunId, ctx);

    // Store the EstimatorSpec being evaluated.
    EstimatorspecRecord espec = EstimatorSpecDao.store(me.estimatorSpec, me.experimentRunId, ctx);

    // Store an entry in the UnsupervisedMetricEvent table.
    UnsupervisedmetriceventRecord meRec = ctx.newRecord(Tables.UNSUPERVISEDMETRICEVENT);
    meRec.setId(null);
    meRec.setEstimatorspec(espec.getId());
    meRec.setDf(df.getId());
    meRec.setMetrictype(me.metricType);
    meRec.setMetricvalue(Double.valueOf(me.metricValue).floatValue());
    meRec.setExperimentrun(me.experimentRunId);
    meRec.store();

    // Store an entry in the Event table.
    EventRecord ev = EventDao.store(meRec.getId(), "unsupervised_metric", me.experimentRunId, -1, ctx);

    return new UnsupervisedMetricEventResponse(espec.getId(), df.getId(), ev.getId(), meRec.getId());
  }
}
