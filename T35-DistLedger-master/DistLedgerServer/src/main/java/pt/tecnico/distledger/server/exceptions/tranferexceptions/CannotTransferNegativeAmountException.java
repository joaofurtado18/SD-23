package pt.tecnico.distledger.server.exceptions.tranferexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class CannotTransferNegativeAmountException extends TransferException {
    private static final String errorMessage = "Cannot transfer negative amount";

    public CannotTransferNegativeAmountException() {
        super(errorMessage);
    }
}
