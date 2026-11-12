package smart_attendance.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import smart_attendance.exception.InvalidCredentialsException;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.exception.UserAlreadyExistsException;
import smart_attendance.model.User;
import smart_attendance.repository.UserRepository;
import smart_attendance.security.PasswordUtil;

/**
 * ============================================================================
 * PHASE 2: Concrete Service Implementation with Exception Handling & Security
 * ============================================================================
 * - Persists users securely in MySQL via Spring Data JPA.
 * - Hashes passwords using salted PBKDF2 with HMAC-SHA256 (65,536 iterations).
 * - Constant-time password verification prevents timing attacks.
 * - Pre-seeds administrator account securely if absent.
 * ============================================================================
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initDefaultAdmin() {
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            User admin = new User(
                    "admin",
                    PasswordUtil.hashPassword("admin123"),
                    "System Administrator",
                    "admin@kiit.ac.in",
                    "ADMIN"
            );
            userRepository.save(admin);
        }
    }

    @Override
    public User registerUser(User user) throws InvalidInputException, UserAlreadyExistsException {
        // 1. Strict validation
        validateUser(user);

        String username = user.getUsername().trim();

        // 2. Prevent duplicate usernames (case-insensitive)
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAlreadyExistsException("Username '" + username + "' is already registered. Please choose another.");
        }

        // 3. Cryptographically hash the plaintext password before storage
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

        User entity = new User(
                username,
                hashedPassword,
                user.getFullName() != null ? user.getFullName().trim() : null,
                user.getEmail() != null ? user.getEmail().trim() : null,
                user.getRole() != null ? user.getRole().trim().toUpperCase() : "STUDENT"
        );

        return userRepository.save(entity);
    }

    @Override
    public boolean authenticateUser(String username, String password) throws InvalidInputException, InvalidCredentialsException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidInputException("Password cannot be empty.");
        }

        String cleanUsername = username.trim();

        User user = userRepository.findByUsernameIgnoreCase(cleanUsername)
                .orElseThrow(() -> new InvalidCredentialsException("No account found with username '" + cleanUsername + "'."));

        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials. Please verify your password.");
        }

        return true;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
    }

    @Override
    public boolean validateUser(User user) throws InvalidInputException {
        if (user == null) {
            throw new InvalidInputException("User object cannot be null.");
        }

        // Username string validation
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new InvalidInputException("Username is required and cannot be blank.");
        }
        String username = user.getUsername().trim();
        if (username.length() < 3 || username.length() > 50) {
            throw new InvalidInputException("Username must be between 3 and 50 characters.");
        }
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new InvalidInputException("Username can only contain alphanumeric characters, underscores, dots, and hyphens.");
        }

        // Password string validation (plaintext before hashing)
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new InvalidInputException("Password is required and cannot be blank.");
        }
        if (user.getPassword().trim().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters long.");
        }

        // Optional full name length check
        if (user.getFullName() != null && user.getFullName().trim().length() > 100) {
            throw new InvalidInputException("Full name cannot exceed 100 characters.");
        }

        // Optional email format validation
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty() && !User.isValidEmail(user.getEmail())) {
            throw new InvalidInputException("Invalid email format provided.");
        }

        return true;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsernameIgnoreCase(username.trim());
    }
}
