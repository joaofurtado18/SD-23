package pt.tecnico.distledger.server.exceptions;

public class NotPossibleToRegisterServerException extends Exception{
    private static final String errorMessage = "Not possible to register the server";
    public NotPossibleToRegisterServerException() {
        super(errorMessage);
    }
}
