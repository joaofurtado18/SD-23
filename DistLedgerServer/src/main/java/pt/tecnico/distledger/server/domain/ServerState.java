package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.exceptions.serverexceptions.*;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.TransferException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserDoesNotExistException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.tecnico.distledger.vectorclock.VectorClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerState {
    private boolean status;

    private List<Operation> ledger;

    private List<Operation> ledgerReplica;

    private Map<String, Integer> accounts;

    private static final String BROKER = "broker";

    private ServerService serverService;

    private VectorClock valueTS = new VectorClock();

    private VectorClock replicaTS = new VectorClock();


    public ServerState(ServerService serverService) {
        this.ledger = new ArrayList<>();
        this.ledgerReplica = new ArrayList<>();
        this.accounts = new ConcurrentHashMap<>();
        this.accounts.put(BROKER, 1000);
        this.status = true;
        this.serverService = serverService;
    }


    public synchronized int balance(String userId, VectorClock prev) throws UserException, ServerException {
        try {
            checkStatus();
            System.err.println(String.format("got balance request with userID: %s%n", userId));

            if (!valueTS.greaterOrEqual(prev)) throw new ServerNotUpdatedException();

            if (accounts.containsKey(userId)) {
                int balance = accounts.get(userId);
                return balance;
            }
            else
                throw new UserDoesNotExistException();
        } catch (ServerException e) {
            throw e;
        }
    }


    public synchronized void createAccountWrapper(String userId, VectorClock clientTS) throws UserException, ServerException, TransferException {
        checkStatus();
        System.err.println(String.format("got create account request with userID: %s", userId));

        CreateOp op = new CreateOp(userId);

        incrementReplica(op);
        ledger.add(op);
        ledgerReplica.add(op);

        executeAndMerge(op, clientTS);
        updatePrevToReplica(clientTS);
        System.err.println("ValueTS: " + valueTS
                + " ReplicaTS: " + replicaTS
                + " OpTS: " + op.getTS());
    }

    public synchronized void createAccountReceiver(String userId, DistLedgerCommonDefinitions.Operation op)
            throws ServerException, UserException, TransferException {

        checkStatus();
        System.err.println(String.format("got create account request FROM STATE PROPAGATION with userID: %s", userId));

        VectorClock clientTS = new VectorClock(op.getPrevTSList());
        VectorClock opTS = new VectorClock(op.getTSList());
        CreateOp ledgerOp = new CreateOp(userId);

        ledgerOp.setTS(opTS);
        ledgerOp.setPrevTS(clientTS);
        ledger.add(ledgerOp);
        ledgerReplica.add(ledgerOp);

        if (valueTS.greaterOrEqual(clientTS))
            ledgerOp.setPrevTS(opTS);
        executeAndMerge(ledgerOp, clientTS);

        updatePrevToReplica(clientTS);
        System.err.println("ValueTS: " + valueTS
                + " ReplicaTS: " + replicaTS
                + " OpTS: " + ledgerOp.getTS());
    }


    public synchronized void transferToWrapper(String userIdFrom, String userIdTo, int amount, VectorClock clientTS)
            throws TransferException, UserException, ServerException {
        checkStatus();
        System.err.println(String.format("got transfer request with userIDFrom: %s and userIDTo: %s%n", userIdFrom, userIdTo));

        TransferOp op = new TransferOp(userIdFrom, userIdTo, amount);

        incrementReplica(op);
        ledger.add(op);
        ledgerReplica.add(op);

        executeAndMerge(op, clientTS);
        updatePrevToReplica(clientTS);
        System.err.println("ValueTS: " + valueTS
                + " ReplicaTS: " + replicaTS
                + " OpTS: " + op.getTS());
    }

    public synchronized void transferToReceiver(String userIdFrom, String userIdTo, int amount, DistLedgerCommonDefinitions.Operation op)
            throws TransferException, UserException, ServerException {
        checkStatus();
        System.err.println(String.format("got transfer request FROM STATE PROPAGATION with userIDFrom: %s and userIDTo: %s%n", userIdFrom, userIdTo));

        VectorClock clientTS = new VectorClock(op.getPrevTSList());
        VectorClock opTS = new VectorClock(op.getTSList());
        TransferOp ledgerOp = new TransferOp(userIdFrom, userIdTo, amount);

        ledgerOp.setTS(opTS);
        ledgerOp.setPrevTS(clientTS);
        ledger.add(ledgerOp);
        ledgerReplica.add(ledgerOp);

        if (valueTS.greaterOrEqual(clientTS))
            ledgerOp.setPrevTS(opTS);
        executeAndMerge(ledgerOp, clientTS);

        updatePrevToReplica(clientTS);
        System.err.println("ValueTS: " + valueTS
                + " ReplicaTS: " + replicaTS
                + " OpTS: " + ledgerOp.getTS());
    }


    public synchronized boolean isStatus() {
        return status;
    }


    public synchronized void setStatus(boolean status) {
        this.status = status;
        System.err.println(String.format("server status changed to " + (status ? "Active.":"Inactive.")));
    }


    public synchronized void checkStatus() throws ServerNotAvailableException {
        if (!status)
            throw new ServerNotAvailableException();
    }


    public synchronized DistLedgerCommonDefinitions.LedgerState getState() throws ServerException {
        checkStatus();

        List<DistLedgerCommonDefinitions.Operation> states = new ArrayList<DistLedgerCommonDefinitions.Operation>();
        DistLedgerCommonDefinitions.LedgerState.Builder builder = DistLedgerCommonDefinitions.LedgerState.newBuilder();

        for (Operation op : ledger) states.add(op.toGrpc());
        builder.addAllLedger(states);
        return builder.build();
    }


    // Receives propagate state request from another server
    public synchronized void propagateState(LedgerState state, VectorClock sendingServerReplicaTS) throws TransferException,
            ServerException, UserException {
        System.err.println("Received state propagation request from " + (this.serverService.isPrimary() ? "B" : "A"));

        try {
            checkStatus();

            // foreach operation in A
            for (DistLedgerCommonDefinitions.Operation op : state.getLedgerList()) {
                VectorClock vcPrevTS = new VectorClock(new ArrayList<>(op.getPrevTSList()));
                VectorClock vcTS = new VectorClock(new ArrayList<>(op.getTSList()));

                // insere no ledger
                if (!this.replicaTS.greaterOrEqual(vcTS)) {
                    OperationType operationType = op.getType();
                    String userId = op.getUserId();

                    if (operationType == OperationType.OP_TRANSFER_TO)
                        transferToReceiver(userId, op.getDestUserId(), op.getAmount(), op);
                    if (operationType == OperationType.OP_CREATE_ACCOUNT)
                        createAccountReceiver(userId, op);

                    this.replicaTS = VectorClock.merge(this.replicaTS, sendingServerReplicaTS);

                    if (this.valueTS.greaterOrEqual(vcPrevTS))
                        this.valueTS = VectorClock.merge(this.valueTS, sendingServerReplicaTS);
                }
            }

            // algumas operações do B podem ter ficado stable:
            for (Operation op : this.ledger) {
                if (this.valueTS.greaterOrEqual(op.getPrevTS()) && op.isStable() == false) {
                    op.setStable(true);
                    op.execute(accounts);
                    this.valueTS = VectorClock.merge(this.valueTS, op.getTS());
                }
            }
        } catch (TransferException | UserException | ServerException e) {
            System.err.println("Error while propagating state: " + e.getMessage());
        }
    }

    public synchronized void requestPropagateState(VectorClock sendingServerReplicaTS) throws ServerException {
        System.err.println("Sending state propagation request to " + (this.serverService.isPrimary() ? "A" : "B"));
        List<DistLedgerCommonDefinitions.Operation> state = this.ledgerReplica.stream()
                .map(op -> op.toGrpc())
                .collect(Collectors.toCollection(ArrayList::new));
        this.serverService.propagateState(state, sendingServerReplicaTS);
        ledgerReplica.clear();
    }

    public synchronized VectorClock getReplicaTs() {
        return this.replicaTS;
    }

    public void setReplicaTs(VectorClock replicaTS) {
        this.replicaTS = replicaTS;
    }

    public synchronized VectorClock getValueTS() {
        return valueTS;
    }

    public void setValueTs(VectorClock valueTS) {
        this.valueTS = valueTS;
    }

    public synchronized void incrementReplica(Operation op) {
        Integer index = this.serverService.isPrimary() ? 0 : 1;

        this.replicaTS.incrementVectorClockPosition(index);
        op.getTS().setVectorClockPosition(index, this.replicaTS.getVectorClockPosition(index));
    }

    public synchronized void updatePrevToReplica(VectorClock prev) {
        Integer index = this.serverService.isPrimary() ? 0 : 1;
        prev.setVectorClockPosition(index, this.replicaTS.getVectorClockPosition(index));
    }

    public synchronized void executeAndMerge(Operation op, VectorClock clientTS)
            throws TransferException, UserException {
        if (valueTS.greaterOrEqual(clientTS)) {
            op.setStable(true);
            op.execute(accounts);
            this.valueTS = VectorClock.merge(this.valueTS, op.getTS());
        }
    }
}