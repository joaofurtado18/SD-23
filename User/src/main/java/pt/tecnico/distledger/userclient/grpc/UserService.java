package pt.tecnico.distledger.userclient.grpc;

import io.grpc.*;
import pt.tecnico.distledger.userclient.Communication;
import pt.tecnico.distledger.userclient.exceptions.ServerDoesNotExistException;
import pt.tecnico.distledger.vectorclock.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    private HashMap<String, Communication> qualifierToCommunication;

    final static String NAMING_SERVER_TARGET = "localhost:5001";

    private static final String SERVICE = "DistLedger";

    final ManagedChannel namingServerChannel =
            ManagedChannelBuilder.forTarget(NAMING_SERVER_TARGET).usePlaintext().build();

    final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub =
            NamingServerServiceGrpc.newBlockingStub(namingServerChannel);

    private VectorClock prev = new VectorClock();


    public UserService() {
        qualifierToCommunication = new HashMap<>();
    }


    public VectorClock getPrev() {
        return prev;
    }


    public void setPrev(VectorClock prev) {
        this.prev = prev;
    }


    public void createAndSetNewVectorClock(List<Integer> list) {
        VectorClock newPrev = new VectorClock(new ArrayList<>(list));
        setPrev(newPrev);
    }


    public void shutdown() {
        for (Map.Entry<String, Communication> entry: qualifierToCommunication.entrySet())
            entry.getValue().getChannel().shutdownNow();
        namingServerChannel.shutdownNow();
        System.err.println("Shutting down.");
    }


    public void lookup(String qualifier) throws ServerDoesNotExistException {
        if (qualifierToCommunication.containsKey(qualifier))
            return;

        LookupRequest request = LookupRequest.newBuilder().
            setService(SERVICE).setQualifier(qualifier).build();

        LookupResponse response = namingServerStub.lookup(request);
        ArrayList<String> addresses = new ArrayList<String>(response.getServersList());

        if (addresses.isEmpty()) throw new ServerDoesNotExistException();

        qualifierToCommunication.put(qualifier, new Communication(addresses.get(0)));
    }

    public void balance(String qualifier, String userId) {
        try {
            lookup(qualifier);
            UserServiceGrpc.UserServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder()
                    .setUserId(userId)
                    .addAllPrevTS(this.prev.getVectorClock())
                    .build();
            System.err.println("Sending balance request");

            UserDistLedger.BalanceResponse result = stub.balance(request);
            createAndSetNewVectorClock(result.getValueTSList());
            System.out.println("OK");
            if (result.getValue() > 0) System.out.println(result.getValue());
            System.err.println("Client timestamp: " + this.prev);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
            System.err.println("Client timestamp: " + this.prev);
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
            System.err.println("Client timestamp: " + this.prev);
        }
    }

    public void createAccount(String qualifier, String userId) {
        try {
            lookup(qualifier);
            UserServiceGrpc.UserServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder()
                    .setUserId(userId)
                    .addAllPrevTS(this.prev.getVectorClock())
                    .build();

            System.err.printf("Sending createAccount %s request%n", userId);

            UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
            this.setPrev(new VectorClock(response.getTSList()));
            System.out.println("OK");
            System.err.println("Client timestamp: " + this.prev);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
            System.err.println("Client timestamp: " + this.prev);
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
            System.err.println("Client timestamp: " + this.prev);
        }
    }

    public void transferTo(String qualifier, String userFrom, String userTo, int amount) {
        try {
            lookup(qualifier);
            UserServiceGrpc.UserServiceBlockingStub stub = qualifierToCommunication.get(qualifier).getStub();

            UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest.newBuilder()
                    .setAccountFrom(userFrom)
                    .setAccountTo(userTo)
                    .setAmount(amount)
                    .addAllPrevTS(prev.getVectorClock())
                    .build();

            System.err.printf("Sending transferTo request of %d from %s to %s%n", amount, userFrom, userTo);

            UserDistLedger.TransferToResponse response = stub.transferTo(request);
            this.setPrev(new VectorClock(response.getTSList()));
            System.out.println("OK");
            System.err.println("Client timestamp: " + this.prev);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus().getDescription());
            System.err.println("Client timestamp: " + this.prev);
        } catch (ServerDoesNotExistException e) {
            System.out.println(e.getMessage());
            System.err.println("Client timestamp: " + this.prev);
        }
    }
}



