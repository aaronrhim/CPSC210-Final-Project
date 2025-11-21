package model.exceptions;

// Thrown when a scalar field expression or evaluation cannot produce a valid result
public class InvalidFunctionException extends RuntimeException {
    // EFFECTS: constructs exception with no message
    public InvalidFunctionException() {
        super();
    }

    // EFFECTS: constructs exception with diagnostic message
    public InvalidFunctionException(String message) {
        super(message);
    }
}
