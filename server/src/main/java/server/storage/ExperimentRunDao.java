package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.ExperimentrunRecord;
import anomalydetection.*;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import util.Pair;

/**
 * Revised based on code from ModelDB: https://github.com/mitdbg/modeldb
 * This class contains logic for reading and storing experiment runs.
 */
public class ExperimentRunDao {
    /**
     * Store an experiment run in the database.
     * @param erun - The experiment run.
     * @param ctx - The database context.
     * @return A response containing information about the stored experiment run.
     */
    public static ExperimentRunEventResponse store(ExperimentRunEvent erun, DSLContext ctx) {
        // ExperimentRun description is not necessarily unique and therefore we
        // don't look for it
        // ExperimentRun ID is unique however and so we use that unique id.
        ExperimentRun er = erun.experimentRun;
        ExperimentrunRecord erRec = ctx.newRecord(Tables.EXPERIMENTRUN);
        erRec.setId(er.id < 0 ? null : er.id);
        erRec.setProject(er.project);
        erRec.setDescription(er.description);
        erRec.setCreated(new Timestamp((new Date()).getTime()));
        if (er.isSetSha()) {
            erRec.setSha(er.getSha());
        }
        erRec.store();
        return new ExperimentRunEventResponse(erRec.getId());
    }

    /**
     * Converts a row of the ExperimentRun table into a anomalydetection.ExperimentRun object.
     * @param erRec - The row of the table.
     * @return The anomalydetection.ExperimentRun object.
     */
    public static ExperimentRun recordToThrift(ExperimentrunRecord erRec) {
        ExperimentRun er = new ExperimentRun(
                erRec.getId(),
                erRec.getProject(),
                erRec.getDescription()
        );
        er.setSha(erRec.getSha());
        er.setCreated(erRec.getCreated().toString());
        return er;
    }

    /**
     * Verifies that there exists an experiment run in the ExperimentRun table with the given ID.
     * @param id - The ID of an experiment run.
     * @param ctx - The database context.
     * @throws InvalidExperimentRunException - Thrown if there is no ExperimentRun with the given ID.
     * No exception is thrown otherwise.
     */
    public static void validateExperimentRunId(int id, DSLContext ctx) throws InvalidExperimentRunException {
        if (ctx.selectFrom(Tables.EXPERIMENTRUN).where(Tables.EXPERIMENTRUN.ID.eq(id)).fetchOne() == null) {
            throw new InvalidExperimentRunException(String.format("Can't find experiment run ID %d", id));
        }
    }

    /**
     * Read the ExperimentRun with the given ID.
     * @param experimentRunId - The ID of an experiment run.
     * @param ctx - The database context.
     * @return The ExperimentRun with ID experimentRunId.
     * @throws ResourceNotFoundException - Thrown if there is no ExperimentRun with ID experimentRunId.
     */
    public static ExperimentRun read(int experimentRunId, DSLContext ctx) throws ResourceNotFoundException {
        ExperimentrunRecord rec = ctx
                .selectFrom(Tables.EXPERIMENTRUN)
                .where(Tables.EXPERIMENTRUN.ID.eq(experimentRunId))
                .fetchOne();
        if (rec == null) {
            throw new ResourceNotFoundException(String.format(
                    "Can't find ExperimentRun with ID %d",
                    experimentRunId
            ));
        }
        return recordToThrift(rec);
    }

    /**
     * Read all the experiment runs in a given project.
     * @param projId - The ID of a project.
     * @param ctx - The database context.
     * @return The experiment runs in the project with ID projId.
     */
    public static ProjectExperimentRuns readExperimentsRunsInProject(int projId, DSLContext ctx) {
        // Get all the experiment run ID pairs in the project.
        List<Integer> experimentRunIds = ctx
                .select(
                        Tables.EXPERIMENTRUN.ID
                )
                .from(Tables.EXPERIMENTRUN)
                .where(Tables.EXPERIMENTRUN.PROJECT.eq(projId))
                .fetch()
                .map(r -> r.value1());

        // Fetch all the experiment runs in the project.
        List<ExperimentRun> experimentRuns = ctx
                .selectFrom(Tables.EXPERIMENTRUN)
                .where(Tables.EXPERIMENTRUN.ID.in(experimentRunIds))
                .orderBy(Tables.EXPERIMENTRUN.ID.asc())
                .fetch()
                .map(rec -> recordToThrift(rec));

        return new ProjectExperimentRuns(projId, experimentRuns);
    }
}
