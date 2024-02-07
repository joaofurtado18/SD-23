package pt.tecnico.distledger.server.domain;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;
import pt.tecnico.distledger.server.exceptions.serverexceptions.ServerException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class ServerAdminImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private ServerState serverState;


    public ServerAdminImpl(ServerState serverState) {
        this.serverState = serverState;
    }


    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        if (this.serverState.isStatus()) this.serverState.setStatus(false);
        DeactivateResponse response = DeactivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        if (!this.serverState.isStatus()) this.serverState.setStatus(true);
        ActivateResponse response = ActivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        try {
            DistLedgerCommonDefinitions.LedgerState state = this.serverState.getState();
            getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(state).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ServerException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        try {
            this.serverState.requestPropagateState(this.serverState.getReplicaTs());
            GossipResponse response = GossipResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DistLedgerServerException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
