package pt.tecnico.distledger.namingserver.domain;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.exceptions.NotPossibleToRegisterServerException;
import pt.tecnico.distledger.server.exceptions.NotPossibleToRemoveServerException;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;

import java.util.ArrayList;

import static io.grpc.Status.INVALID_ARGUMENT;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServer namingServer;

    public NamingServerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }


    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            this.namingServer.register(request.getService(), request.getQualifier(), request.getAddress());
            RegisterResponse response = RegisterResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NotPossibleToRegisterServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        ArrayList<String> serverEntries = this.namingServer.lookup(request.getService(), request.getQualifier());
        LookupResponse response = LookupResponse.newBuilder().addAllServers(serverEntries).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            this.namingServer.delete(request.getService(), request.getAddress());
            DeleteResponse response = DeleteResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NotPossibleToRemoveServerException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}
