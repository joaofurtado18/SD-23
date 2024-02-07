package pt.tecnico.distledger.server.domain;

import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServerCrossImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase{

    private ServerState serverState;


    public ServerCrossImpl(ServerState serverState) {
        this.serverState = serverState;
    }


    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        try {
            this.serverState.propagateState(request.getState(), this.serverState.getReplicaTs());
            PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
