package pt.tecnico.distledger.server.exceptions.serverexceptions;

import pt.tecnico.distledger.server.exceptions.DistLedgerServerException;

public class ServerException extends DistLedgerServerException {

    public ServerException(String errorMessage) {
        super(errorMessage);
    }
}
