package smart_attendance.exception;

/**
 * PHASE 2 - Exception handling when user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
