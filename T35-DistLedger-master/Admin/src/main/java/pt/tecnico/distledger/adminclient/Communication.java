package pt.tecnico.distledger.adminclient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

public class Communication {
    private String target;

    private ManagedChannel channel;

    private AdminServiceGrpc.AdminServiceBlockingStub stub;

    public Communication(String target) {
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public AdminServiceGrpc.AdminServiceBlockingStub getStub() {
        return stub;
    }

    public void setStub(AdminServiceGrpc.AdminServiceBlockingStub stub) {
        this.stub = stub;
    }

}
