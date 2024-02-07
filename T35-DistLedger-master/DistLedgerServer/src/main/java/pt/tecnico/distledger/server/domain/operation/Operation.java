package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.CannotTransferNegativeAmountException;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.NotEnoughFundsException;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.TransferException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserAlreadyExistsException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserException;
import pt.tecnico.distledger.vectorclock.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.Map;

public abstract class Operation {
    private String account;

    private boolean stable;

    private VectorClock TS;

    private VectorClock prevTS;

    private Map<String, Integer> accounts;


    public Operation(String fromAccount) {
        this.account = fromAccount;
        this.stable = false;
        this.TS = new VectorClock();
        this.prevTS = new VectorClock();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public abstract DistLedgerCommonDefinitions.Operation toGrpc();

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public VectorClock getTS() {
        return TS;
    }

    public void setTS(VectorClock TS) {
        this.TS = TS;
    }

    public VectorClock getPrevTS() {
        return prevTS;
    }

    public void setPrevTS(VectorClock prevTS) {
        this.prevTS = prevTS;
    }

    public abstract void execute(Map<String, Integer> accounts) throws UserException, TransferException;
}
