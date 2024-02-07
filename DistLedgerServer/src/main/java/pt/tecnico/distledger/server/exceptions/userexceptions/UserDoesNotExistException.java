
package pt.tecnico.distledger.server.exceptions.userexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class UserDoesNotExistException extends UserException {

    private static final String errorMessage = "Account does not exist";
    public UserDoesNotExistException() {
        super(errorMessage);
    }
}
