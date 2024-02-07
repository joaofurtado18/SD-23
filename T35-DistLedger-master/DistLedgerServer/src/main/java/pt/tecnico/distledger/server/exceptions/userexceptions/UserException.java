package pt.tecnico.distledger.server.exceptions.userexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class UserException extends DistLedgerServerException {
    public UserException(String errorMessage) {
        super(errorMessage);
    }
}
