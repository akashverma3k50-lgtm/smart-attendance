package smart_attendance.exception;

/**
 * PHASE 2 - Exception handling for invalid user inputs
 * (e.g. blank username, short password, invalid email format).
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
