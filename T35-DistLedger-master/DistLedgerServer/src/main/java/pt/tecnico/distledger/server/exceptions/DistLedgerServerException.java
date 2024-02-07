package pt.tecnico.distledger.server.exceptions;

public class DistLedgerServerException extends Exception {
    public DistLedgerServerException(String errorMessage) {
        super(errorMessage);
    }
}
