package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.domain.*;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++)
            System.out.printf("arg[%d] = %s%n", i, args[i]);

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];

        ServerService serverService = new ServerService("localhost:" + args[0], qualifier);
        ServerState serverState = new ServerState(serverService);

        final BindableService userImpl = new ServerUserImpl(serverState);
        final BindableService adminImpl = new ServerAdminImpl(serverState);
        final BindableService crossServerImpl = new ServerCrossImpl(serverState);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port)
                .addService(userImpl)
                .addService(adminImpl)
                .addService(crossServerImpl)
                .build();

        try {
            serverService.register();
            // Start the server
            server.start();

            // Server threads are running in the background.
            System.out.println("Server started");

            System.out.println("Press enter to shutdown");
            System.in.read();
            server.shutdown();
            serverService.delete();
            System.exit(0);

            // Do not exit the main thread. Wait until server is terminated.
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        }

    }

}


