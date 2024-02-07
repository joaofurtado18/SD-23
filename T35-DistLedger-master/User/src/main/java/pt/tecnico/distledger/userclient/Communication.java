package pt.tecnico.distledger.userclient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class Communication {

    private String target;
    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;

    public Communication(String target) {
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public UserServiceGrpc.UserServiceBlockingStub getStub() {
        return stub;
    }

    public void setStub(UserServiceGrpc.UserServiceBlockingStub stub) {
        this.stub = stub;
    }

}
