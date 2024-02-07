package pt.tecnico.distledger.server.exceptions.tranferexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class NotEnoughFundsException extends TransferException {

    private static final String errorMessage = "Not enough funds.";
    public NotEnoughFundsException() {
        super(errorMessage);
    }
}