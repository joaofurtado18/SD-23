package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.*;
import pt.tecnico.distledger.adminclient.Communication;
import pt.tecnico.distledger.adminclient.exceptions.ServerDoesNotExistException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminService {
    private HashMap<String, Communication> qualifierToCommunication;

    final static String NAMING_SERVER_TARGET = "localhost:5001";

    private static final String SERVICE = "DistLedger";

    final ManagedChannel namingServerChannel =
            ManagedChannelBuilder.forTarget(NAMING_SERVER_TARGET).usePlaintext().build();

    final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub =
            NamingServerServiceGrpc.newBlockingStub(namingServerChannel);


    public AdminService() { qualifierToCommunication = new HashMap<>(); }


    public void shutdown() {
        for (Map.Entry<String, Communication> entry: qualifierToCommunication.entrySet())
            entry.getValue().getChannel().shutdownNow();
        namingServerChannel.shutdownNow();
        System.err.println("Shutting down.");
    }


    public void lookup(String qualifier) throws ServerDoesNotExistException {
        if (qualifierToCommunication.containsKey(qualifier))
            return;

        NamingServer.LookupRequest request = NamingServer.LookupRequest.newBuilder().
                setService(SERVICE).setQualifier(qualifier).build();

        NamingServer.LookupResponse response = namingServerStub.lookup(request);
        ArrayList<String> addresses = new ArrayList<String>(response.getServersList());

        if (addresses.isEmpty()) throw new ServerDoesNotExistException();

        qualifierToCommunication.put(qualifier, new Communication(addresses.get(0)));
    }


    public void deactivate(String qualifier) {
        try {
            lookup(qualifier);
            AdminServiceGrpc.AdminServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
            System.err.println("Sending deactivate request");
            stub.deactivate(request);
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
        }
    }


    public void activate(String qualifier) {
        try {
            lookup(qualifier);
            AdminServiceGrpc.AdminServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
            System.err.println("Sending activate request");
            stub.activate(request);
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
        }
    }


    public DistLedgerCommonDefinitions.LedgerState getLedgerState(String qualifier) throws ServerDoesNotExistException,
            StatusRuntimeException {
        try {
            lookup(qualifier);
            AdminServiceGrpc.AdminServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
            System.err.println("Sending getLedgerState request");
            DistLedgerCommonDefinitions.LedgerState state = stub.getLedgerState(request).getLedgerState();
            return state;
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }


    public void gossip(String qualifier) throws ServerDoesNotExistException, StatusRuntimeException {
        try {
            lookup(qualifier);
            AdminServiceGrpc.AdminServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            AdminDistLedger.GossipRequest request = AdminDistLedger.GossipRequest.newBuilder().build();
            System.out.println("Sending gossip request");

            stub.gossip(request);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
        }

    }
}
