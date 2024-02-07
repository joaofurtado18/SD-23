package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.exceptions.tranferexceptions.CannotTransferNegativeAmountException;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.NotEnoughFundsException;
import pt.tecnico.distledger.server.exceptions.tranferexceptions.TransferException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserAlreadyExistsException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserDoesNotExistException;
import pt.tecnico.distledger.server.exceptions.userexceptions.UserException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

import java.util.Map;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount) {
        super(fromAccount);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public DistLedgerCommonDefinitions.Operation toGrpc() {
        String idAcc = this.getAccount();
        String idDest = this.getDestAccount();
        int amount = this.getAmount();

        DistLedgerCommonDefinitions.Operation proto_op = DistLedgerCommonDefinitions.Operation.newBuilder()
                .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
                .setUserId(idAcc)
                .setDestUserId(idDest)
                .setAmount(amount)
                .addAllPrevTS(this.getPrevTS().getVectorClock())
                .addAllTS(this.getTS().getVectorClock())
                .build();

        return proto_op;
    }

    private synchronized void addFunds(Map<String, Integer> accounts, String userId, int funds) throws UserException {
        if (!accounts.containsKey(userId)) throw new UserDoesNotExistException();

        int balance = accounts.get(userId);
        balance += funds;
        accounts.put(userId, balance);
        System.err.println(String.format("added %d funds to account %s%n", funds, userId));
    }

    private synchronized void subtractFunds(Map<String, Integer> accounts, String userId, int funds) throws UserException, TransferException {
        if (!accounts.containsKey(userId)) throw new UserDoesNotExistException();

        int balance = accounts.get(userId);
        if (balance < funds) throw new NotEnoughFundsException();

        balance -= funds;
        accounts.put(userId, balance);
        System.err.println(String.format("subtracted %d funds from account %s%n", funds, userId));
    }

    private synchronized boolean checkEnoughFunds(Map<String, Integer> accounts, String userId, int funds) throws UserException {
        if (!accounts.containsKey(userId)) throw new UserDoesNotExistException();

        int balance = accounts.get(userId);
        System.err.println(String.format("balance: %s, funds: %s", balance, funds));
        return balance < funds;
    }

    @Override
    public void execute(Map<String, Integer> accounts)
            throws UserException, TransferException {
        if (checkEnoughFunds(accounts, this.getAccount(), this.getAmount())) throw new NotEnoughFundsException();

        if (amount < 0) throw new CannotTransferNegativeAmountException();

        if (!(accounts.containsKey(this.getAccount()) && accounts.containsKey(this.getDestAccount())))
            throw new UserDoesNotExistException();

        subtractFunds(accounts, this.getAccount(), amount);
        addFunds(accounts, this.getDestAccount(), amount);
    }
}
