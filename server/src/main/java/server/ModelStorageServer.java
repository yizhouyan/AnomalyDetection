package server;

import anomalydetection.*;
import conf.ModelStorageConfig;
import org.apache.thrift.TException;
import org.jooq.DSLContext;
import server.storage.ExperimentRunDao;
import server.storage.ProjectDao;
import util.ContextFactory;
import util.ExceptionWrapper;

import java.util.List;

/**
 * Created by yizhouyan on 9/26/19.
 * Revised based on code from ModelDB: https://github.com/mitdbg/modeldb
 *
 * This class represents the processors that handles the requests that the ModelStorage service can receive.
 *
 * Try to make the handlers in this class very short. Ideally, each one should be just a single line. The advantage in
 * keeping them short is that it is easier to test the codebase.
 *
 * For documentation on the API methods, see the AnomalyDetection.thrift file.
 *
 */
public class ModelStorageServer implements ModelStorageService.Iface {
    /**
     * The database context.
     */
    private DSLContext ctx;

    /**
     * Create the service and connect to the database.
     * @param username - The username to connect to the database.
     * @param password - The password to connect to the database.
     * @param jdbcUrl - The JDBC URL that points to the database.
     * @param dbType - The type of the database (only SQLite is supported for now).
     */
    public ModelStorageServer(
            String username,
            String password,
            String jdbcUrl,
            ModelStorageConfig.DatabaseType dbType) {
        try {
            this.ctx = ContextFactory.create(username, password, jdbcUrl, dbType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ModelStorageServer(DSLContext ctx) {
        this.ctx = ctx;
    }


    @Override
    public int testConnection() throws TException {
        return 200;
    }

    @Override
    public int storeDataFrame(DataFrame df, int experimentRunId) throws InvalidExperimentRunException, ServerLogicException, TException {
        return 0;
    }

    @Override
    public String pathForModel(int modelId) throws ResourceNotFoundException, InvalidFieldException, ServerLogicException, TException {
        return null;
    }

    @Override
    public FitEventResponse storeFitEvent(FitEvent fe) throws InvalidExperimentRunException, ServerLogicException, TException {
        return null;
    }

    @Override
    public ModelMetricEventResponse storeModelMetricEvent(ModelMetricEvent me) throws InvalidExperimentRunException, ServerLogicException, TException {
        return null;
    }

    @Override
    public UnsupervisedMetricEventResponse storeUnsupervisedMetricEvent(UnsupervisedMetricEvent ume) throws InvalidExperimentRunException, ServerLogicException, TException {
        return null;
    }

    @Override
    public String getFilePath(Model t, int experimentRunId, String filename) throws ResourceNotFoundException, ServerLogicException, TException {
        return null;
    }

    @Override
    public TransformEventResponse storeTransformEvent(TransformEvent te) throws InvalidExperimentRunException, ServerLogicException, TException {
        return null;
    }

    @Override
    public UnsupervisedEventResponse storeUnsupervisedEvent(UnsupervisedEvent te) throws InvalidExperimentRunException, ServerLogicException, TException {
        return null;
    }

    @Override
    public ProjectEventResponse storeProjectEvent(ProjectEvent pr) throws ServerLogicException, TException {
        return ExceptionWrapper.run(() -> ProjectDao.store(pr, ctx));
    }

    @Override
    public ExperimentRunEventResponse storeExperimentRunEvent(ExperimentRunEvent er) throws ServerLogicException, TException {
        return ExceptionWrapper.run(() -> ExperimentRunDao.store(er, ctx));
    }

    @Override
    public ProjectExperimentsAndRuns getRunsAndExperimentsInProject(int projId) throws ServerLogicException, TException {
        return null;
    }
}
