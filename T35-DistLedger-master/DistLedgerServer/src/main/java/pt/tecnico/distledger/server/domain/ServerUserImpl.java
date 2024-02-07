package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.exceptions.*;
import pt.tecnico.distledger.vectorclock.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServerUserImpl extends UserServiceGrpc.UserServiceImplBase{
    private ServerState serverState;

    public ServerUserImpl(ServerState serverState) {
        this.serverState = serverState;
    }


    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        try {
            VectorClock prev = new VectorClock(request.getPrevTSList());

            int balance = this.serverState.balance(request.getUserId(), prev);
            BalanceResponse response = BalanceResponse.newBuilder()
                    .setValue(balance)
                    .addAllValueTS(this.serverState.getValueTS().getVectorClock())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DistLedgerServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
            VectorClock prev = new VectorClock(request.getPrevTSList());

            this.serverState.createAccountWrapper(request.getUserId(), prev);
            CreateAccountResponse response = CreateAccountResponse.newBuilder()
                    .addAllTS(prev.getVectorClock())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DistLedgerServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        try {
            VectorClock prev = new VectorClock(request.getPrevTSList());

            this.serverState.transferToWrapper(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), prev);
            TransferToResponse response = TransferToResponse.newBuilder()
                    .addAllTS(prev.getVectorClock())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DistLedgerServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
