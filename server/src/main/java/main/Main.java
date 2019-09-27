package main;

import conf.ModelStorageConfig;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportFactory;
import anomalydetection.ModelStorageService;
import server.ModelStorageServer;

/**
 * Created by yizhouyan on 9/26/19.
 * Revised based on code from ModelDB: https://github.com/mitdbg/modeldb
 * Main entry point of the ModelStorageServer
 *
 * When you execute this file, you can include the following command line arguments:
 * (optional) --conf [path_to_conf_file]
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // Read the configuration. This uses the default configuration if no configuration is given in the
        // command line arguments.
        ModelStorageConfig config = ModelStorageConfig.parse(args);

        // Attempt to launch the server.
        try {
            TServerTransport transport = new TServerSocket(config.thriftPort);

            // We use the binary protocol.
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            TTransportFactory transportFactory = new TFramedTransport.Factory();

            // Create a multi-threaded server. Process requests with an instance of
            // ModelDbServer.
            TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(transport)
                    .processor(new ModelStorageService.Processor<ModelStorageServer>(new ModelStorageServer(
                            config.dbUser,
                            config.dbPassword,
                            config.jbdcUrl,
                            config.dbType
                    )))
                    .protocolFactory(protocolFactory)
                    .transportFactory(transportFactory)
                    .minWorkerThreads(1)
                    .maxWorkerThreads(100);
            TThreadPoolServer server = new TThreadPoolServer(serverArgs);

            // Launch the server.
            System.out.printf("Starting the simple server on port %d...\n", config.thriftPort);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}