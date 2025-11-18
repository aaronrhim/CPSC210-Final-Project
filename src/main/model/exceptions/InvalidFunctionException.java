package model.exceptions;

public class InvalidFunctionException extends RuntimeException {
    public InvalidFunctionException() {
        super();
    }

    public InvalidFunctionException(String message) {
        super(message);
    }
}
