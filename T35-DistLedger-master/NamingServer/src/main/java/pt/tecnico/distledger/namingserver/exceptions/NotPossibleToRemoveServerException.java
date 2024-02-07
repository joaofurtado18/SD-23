package pt.tecnico.distledger.server.exceptions;

public class NotPossibleToRemoveServerException extends Exception{
    private static final String errorMessage = "Not possible to remove the server";
    public NotPossibleToRemoveServerException() {
        super(errorMessage);
    }
}
