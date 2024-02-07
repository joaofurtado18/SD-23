package pt.tecnico.distledger.server.exceptions.tranferexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class TransferException extends DistLedgerServerException {

    public TransferException(String errorMessage) {
        super(errorMessage);
    }
}
