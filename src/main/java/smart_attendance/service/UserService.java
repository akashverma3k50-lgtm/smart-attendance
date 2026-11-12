package smart_attendance.service;

import smart_attendance.exception.InvalidCredentialsException;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.exception.UserAlreadyExistsException;
import smart_attendance.model.User;

/**
 * ============================================================================
 * PHASE 2: Structured Programming and Reliability (CLO2)
 * ============================================================================
 * Defines an interface for all user-related business operations:
 * - Registration
 * - Authentication
 * - Validation
 * - User retrieval
 * Decouples controllers/servlets from underlying data persistence mechanisms.
 * ============================================================================
 */
public interface UserService {

    /**
     * Registers a new user in the system after validating inputs and checking uniqueness.
     * @param user the User entity to register
     * @return the registered User
     * @throws InvalidInputException if inputs fail validation checks
     * @throws UserAlreadyExistsException if the username is already taken
     */
    User registerUser(User user) throws InvalidInputException, UserAlreadyExistsException;

    /**
     * Authenticates a user against provided credentials.
     * @param username the entered username
     * @param password the entered password
     * @return true if credentials are valid
     * @throws InvalidInputException if inputs are empty or malformed
     * @throws InvalidCredentialsException if credentials do not match
     */
    boolean authenticateUser(String username, String password) throws InvalidInputException, InvalidCredentialsException;

    /**
     * Finds and retrieves a user by username.
     * @param username the username to look up
     * @return User object or null if not found
     */
    User findByUsername(String username);

    /**
     * Validates user entity fields using business rules and string handling.
     * @param user the user to validate
     * @return true if valid
     * @throws InvalidInputException if validation fails
     */
    boolean validateUser(User user) throws InvalidInputException;

    /**
     * Checks if a username already exists.
     * @param username the username to check
     * @return true if taken, false otherwise
     */
    boolean existsByUsername(String username);
}