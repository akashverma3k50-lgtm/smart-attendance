package smart_attendance.exception;

/**
 * PHASE 2 - Exception handling for duplicate user registration attempts.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
