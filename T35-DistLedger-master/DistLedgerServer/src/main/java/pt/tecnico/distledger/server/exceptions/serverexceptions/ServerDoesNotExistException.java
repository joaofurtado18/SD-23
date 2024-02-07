package pt.tecnico.distledger.server.exceptions.serverexceptions;

public class ServerDoesNotExistException extends ServerException {

    private static final String errorMessage = "Server does not exist.";

    public ServerDoesNotExistException() { super(errorMessage); }

}
