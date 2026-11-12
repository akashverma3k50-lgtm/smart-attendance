package smart_attendance.model;

import jakarta.persistence.*;

/**
 * ============================================================================
 * PHASE 1: Core Application Setup and Design (CLO1)
 * ============================================================================
 * - Identifies User entity of the system with JPA persistence.
 * - Attributes: id, username, password, fullName, email, role.
 * - Class structure with constructors, getters, setters, and methods.
 * - Basic validation logic in Java using string handling (null checks, length,
 *   trimming, format checks).
 * ============================================================================
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String role; // e.g. "STUDENT", "FACULTY", "ADMIN"

    // 1. Default (No-argument) Constructor
    public User() {
        this.role = "STUDENT";
    }

    // 2. Parameterized Constructor (Credentials only)
    public User(String username, String password) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.role = "STUDENT";
    }

    // 3. Full Parameterized Constructor
    public User(String username, String password, String fullName, String email, String role) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.fullName = fullName != null ? fullName.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.role = role != null && !role.trim().isEmpty() ? role.trim().toUpperCase() : "STUDENT";
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim().toUpperCase() : "STUDENT";
    }

    // --- Basic Validation Logic in Java (String Handling) ---

    /**
     * Comprehensive validation using standard Java String methods:
     * - trim(), isEmpty(), length(), indexOf()
     */
    public boolean isValid() {
        return isValidUsername(this.username) && isValidPassword(this.password);
    }

    /**
     * String handling: username must not be null, must not be blank,
     * and must have at least 3 characters and at most 50 characters.
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        String trimmed = username.trim();
        return !trimmed.isEmpty() && trimmed.length() >= 3 && trimmed.length() <= 50;
    }

    /**
     * String handling: password must not be null and must have at least 6 characters.
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.trim().length() >= 6;
    }

    /**
     * String handling: basic email format validation if provided.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // email is optional
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        int dotIndex = trimmed.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < trimmed.length() - 1;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
