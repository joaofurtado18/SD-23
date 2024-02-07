package pt.tecnico.distledger.server.domain.operation;


import pt.tecnico.distledger.server.exceptions.userexceptions.UserAlreadyExistsException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.Map;

public class CreateOp extends Operation {

    public CreateOp(String account) {
        super(account);
    }


    public DistLedgerCommonDefinitions.Operation toGrpc() {
        String id = this.getAccount();

        DistLedgerCommonDefinitions.Operation proto_op = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
                .setUserId(id)
                .addAllPrevTS(this.getPrevTS().getVectorClock())
                .addAllTS(this.getTS().getVectorClock())
                .build();

        return proto_op;
    }

    @Override
    public void execute(Map<String, Integer> accounts) throws UserAlreadyExistsException {
        if (accounts.containsKey(this.getAccount())) throw new UserAlreadyExistsException();
        accounts.put(this.getAccount(), 0);
    }
}
