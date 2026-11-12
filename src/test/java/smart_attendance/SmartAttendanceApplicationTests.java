package smart_attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import smart_attendance.exception.InvalidCredentialsException;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.exception.UserAlreadyExistsException;
import smart_attendance.model.User;
import smart_attendance.repository.UserRepository;
import smart_attendance.security.PasswordUtil;
import smart_attendance.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmartAttendanceApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.findByUsernameIgnoreCase("testuser_phase2")
                .ifPresent(u -> userRepository.delete(u));
    }

    @Test
    void contextLoads() {
        assertNotNull(userService);
    }

    @Test
    void testPhase1UserValidationLogic() {
        // Valid user
        User validUser = new User("student01", "securePass123", "Alice Smith", "alice@kiit.ac.in", "STUDENT");
        assertTrue(validUser.isValid());
        assertTrue(User.isValidEmail(validUser.getEmail()));

        // Invalid user (short password & empty username)
        User invalidUser = new User("  ", "123");
        assertFalse(invalidUser.isValid());
    }

    @Test
    void testPhase2UserServiceAndExceptions() {
        // Register new user
        User newUser = new User("testuser_phase2", "password123", "Test User", "test@kiit.ac.in", "STUDENT");
        User registered = userService.registerUser(newUser);
        assertEquals("testuser_phase2", registered.getUsername());

        // Duplicate registration should throw UserAlreadyExistsException
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(newUser));

        // Invalid input should throw InvalidInputException
        User badUser = new User("ab", "123");
        assertThrows(InvalidInputException.class, () -> userService.registerUser(badUser));

        // Valid authentication against hashed password
        assertTrue(userService.authenticateUser("testuser_phase2", "password123"));

        // Invalid credentials should throw InvalidCredentialsException
        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser("testuser_phase2", "wrongPass"));
    }

    @Test
    void testPasswordSecurityHashing() {
        String rawPassword = "MySecretPassword!987";
        String hash1 = PasswordUtil.hashPassword(rawPassword);
        String hash2 = PasswordUtil.hashPassword(rawPassword);

        // Different salts must produce distinct hashes
        assertNotEquals(hash1, hash2);

        // Both verify successfully
        assertTrue(PasswordUtil.verifyPassword(rawPassword, hash1));
        assertTrue(PasswordUtil.verifyPassword(rawPassword, hash2));

        // Wrong password fails
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash1));
    }
}
