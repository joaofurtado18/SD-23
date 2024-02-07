package pt.tecnico.distledger.server.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.vectorclock.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

import java.util.ArrayList;
import java.util.List;

public class ServerService {
    final static String NAMING_SERVER_TARGET = "localhost:5001";

    private static final String SERVICE = "DistLedger";

    private static final String PRIMARY_SERVER = "A";

    private static final String SECONDARY_SERVER = "B";

    private final ManagedChannel namingServerChannel =
            ManagedChannelBuilder.forTarget(NAMING_SERVER_TARGET).usePlaintext().build();

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub =
            NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    private ManagedChannel serverChannel;

    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub serverStub;

    private String target;

    private String qualifier;


    public ServerService (String target, String qualifier) {
        this.target = target;
        this.qualifier = qualifier;
    }


    public void createCommunication() {
        this.serverChannel = ManagedChannelBuilder.forTarget(lookup(qualifier.equals(PRIMARY_SERVER)
                ? SECONDARY_SERVER
                : PRIMARY_SERVER)).usePlaintext().build();
        this.serverStub = DistLedgerCrossServerServiceGrpc.newBlockingStub(serverChannel);
    }


    public boolean isPrimary(){
        return this.qualifier.equals(PRIMARY_SERVER);
    }


    public boolean isSecondary() {
        return this.qualifier.equals(SECONDARY_SERVER);
    }


    public void register() throws StatusRuntimeException {
        try {
            RegisterRequest request = RegisterRequest.newBuilder()
                    .setService(SERVICE)
                    .setQualifier(qualifier)
                    .setAddress(target)
                    .build();

            System.err.println("Sending register request from server to naming server.");
            namingServerStub.register(request);
            System.err.println("OK");
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }


    public void delete() throws StatusRuntimeException {
        try {
            DeleteRequest request = DeleteRequest.newBuilder()
                    .setService(SERVICE)
                    .setAddress(target)
                    .build();

            System.err.println("Sending delete request from server to naming server.");
            namingServerStub.delete(request);
            System.err.println("OK");
        } catch (StatusRuntimeException e) {
            throw e;
        }
    }


    public String lookup(String qualifier) {
        LookupRequest request = LookupRequest.newBuilder().setService(SERVICE).setQualifier(qualifier).build();
        LookupResponse response = namingServerStub.lookup(request);
        ArrayList<String> addresses = new ArrayList<String>(response.getServersList());
        return addresses.get(0);
    }


    public void propagateState(List<DistLedgerCommonDefinitions.Operation> state, VectorClock replicaTS) {
        createCommunication();
        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder()
                .addAllLedger(state)
                .build();
        PropagateStateRequest request = PropagateStateRequest.newBuilder()
                .setState(ledgerState)
                .addAllReplicaTS(replicaTS.getVectorClock())
                .build();
        PropagateStateResponse response = serverStub.propagateState(request);
    }
}