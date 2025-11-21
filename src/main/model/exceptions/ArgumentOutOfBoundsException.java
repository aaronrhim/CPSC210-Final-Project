package model.exceptions;

// Thrown when a provided argument lies outside the accepted numeric or domain bounds
public class ArgumentOutOfBoundsException extends RuntimeException {
    // EFFECTS: constructs exception with diagnostic message
    public ArgumentOutOfBoundsException(String message) {
        super(message);
    }
}
