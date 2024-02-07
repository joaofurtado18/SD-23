package pt.tecnico.distledger.server.exceptions.userexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class UserAlreadyExistsException extends UserException {

    private static final String errorMessage = "Account already exists";
    public UserAlreadyExistsException() {
        super(errorMessage);
    }
}
