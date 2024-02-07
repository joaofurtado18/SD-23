package pt.tecnico.distledger.userclient.exceptions;


public class ServerDoesNotExistException extends Exception {

    private static final String errorMessage = "Server does not exist.";

    public ServerDoesNotExistException() { super(errorMessage); }

}
