package smart_attendance.exception;

/**
 * PHASE 2 - Exception handling for authentication failures
 * (e.g. invalid username or incorrect password).
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
