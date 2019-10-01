package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.HyperparameterRecord;
import jooq.sqlite.gen.tables.records.EstimatorspecRecord;
import anomalydetection.HyperParameter;
import anomalydetection.ResourceNotFoundException;
import anomalydetection.EstimatorSpec;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Contains logic for storing and reading EstimatorSpec.
 */
public class EstimatorSpecDao {
    /**
     * Store an entry in the EstimatorSpec Table
     * @param s The EstimatorSpec from thrift.
     * @param experimentId - The experiment run that should contain the given EstimatorSpec.
     * @param ctx The database context.
     * @return The row of the EstimatorSpec table reflecting the given EstimatorSpec after it has been stored.
     */
    public static EstimatorspecRecord store(EstimatorSpec s, int experimentId, DSLContext ctx) {
        EstimatorspecRecord rec = ctx
                .selectFrom(Tables.ESTIMATORSPEC)
                .where(Tables.ESTIMATORSPEC.ID.eq(s.id))
                .fetchOne();

        if (rec != null) {
            return rec;
        }

        EstimatorspecRecord sRec = ctx.newRecord(Tables.ESTIMATORSPEC);
        sRec.setId(null);
        sRec.setExperimentrun(experimentId);
        sRec.setTag(s.tag);
        sRec.setEstimatortype(s.estimatorType);
        sRec.store();

        s.hyperparameters.forEach(hp -> {
            HyperparameterRecord hpRec = ctx.newRecord(Tables.HYPERPARAMETER);
            hpRec.setId(null);
            hpRec.setSpec(sRec.getId());
            hpRec.setParamname(hp.name);
            hpRec.setParamtype(hp.type);
            hpRec.setParamvalue(hp.value);
            hpRec.setExperimentrun(experimentId);
            hpRec.store();
            hpRec.getId();
        });

        sRec.getId();
        return sRec;
    }

    /**
     *
     * @param sId The id of an EstimatorSpec.
     * @param ctx The database context.
     * @return A thrift version of EstimatorSpec.
     * @throws ResourceNotFoundException - Thrown if there is no EstimatorSpec in the database that has the ID sId.
     */
    public static EstimatorSpec read(int sId, DSLContext ctx) throws ResourceNotFoundException {
        EstimatorspecRecord rec =
                ctx.selectFrom(Tables.ESTIMATORSPEC).where(Tables.ESTIMATORSPEC.ID.eq(sId)).fetchOne();
        List<HyperParameter> hps = ctx.selectFrom(Tables.HYPERPARAMETER)
                .where(Tables.HYPERPARAMETER.SPEC.eq(sId))
                .fetch()
                .map(hp -> new HyperParameter(
                        hp.getParamname(),
                        hp.getParamvalue(),
                        hp.getParamtype()
                ));

        if (rec == null) {
            throw new ResourceNotFoundException(String.format(
                    "Could not read TransformerSpec %d, it doesn't exist",
                    sId
            ));
        }

        return new EstimatorSpec(
                rec.getId(),
                rec.getEstimatortype(),
                hps,
                rec.getTag()
        );
    }
}
