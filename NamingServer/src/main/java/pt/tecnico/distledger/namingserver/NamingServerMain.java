package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.namingserver.domain.NamingServer;
import pt.tecnico.distledger.namingserver.domain.NamingServerServiceImpl;

import java.io.IOException;

public class NamingServerMain {

    private final static int port = 5001;

    public static void main(String[] args) throws IOException, InterruptedException {

        NamingServer namingServer = new NamingServer();

        final BindableService namingServerServiceImpl = new NamingServerServiceImpl(namingServer);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(namingServerServiceImpl)
                .build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Naming Server started");

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }

}