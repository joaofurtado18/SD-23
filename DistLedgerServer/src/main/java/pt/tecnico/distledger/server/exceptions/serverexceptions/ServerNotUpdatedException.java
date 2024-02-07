package pt.tecnico.distledger.server.exceptions.serverexceptions;

public class ServerNotUpdatedException extends ServerException {

    private static final String errorMessage = "Server not updated";

    public ServerNotUpdatedException() {
        super(errorMessage);
    }
}
