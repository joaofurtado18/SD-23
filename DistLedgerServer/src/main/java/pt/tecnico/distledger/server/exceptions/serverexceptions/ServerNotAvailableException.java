package pt.tecnico.distledger.server.exceptions.serverexceptions;

public class ServerNotAvailableException extends ServerException {

    private static final String errorMessage = "UNAVAILABLE";
    public ServerNotAvailableException() {
        super(errorMessage);
    }
}
