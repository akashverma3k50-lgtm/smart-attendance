package smart_attendance.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smart_attendance.model.User;
import smart_attendance.service.UserService;

import java.util.Date;
import java.util.Map;

/**
 * ============================================================================
 * PHASE 3 & 4: REST Controller for Authentication & Session Tracking
 * ============================================================================
 * - Validates credentials server-side against hashed PBKDF2 passwords.
 * - Protects against session fixation by rotating session ID upon login.
 * - Maintains secure HttpSession state.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Validates user credentials on server side, hashes password, and saves user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok(Map.of(
                "message", "Registration successful! You can now login.",
                "username", registeredUser.getUsername()
        ));
    }

    /**
     * POST /api/auth/login
     * Validates credentials, destroys any stale session to prevent session fixation,
     * and creates a fresh authenticated session.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

        User user = userService.findByUsername(loginRequest.getUsername());

        // Invalidate any pre-existing session to prevent session fixation attacks
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create a new authenticated session
        HttpSession session = request.getSession(true);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        session.setAttribute("loginTime", new Date());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    /**
     * POST /api/auth/logout
     * Implements logout and session invalidation.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }

    /**
     * GET /api/auth/me
     * Checks current session authentication status.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("loggedIn", false, "message", "Not logged in."));
        }
        return ResponseEntity.ok(Map.of(
                "loggedIn", true,
                "username", session.getAttribute("username"),
                "role", session.getAttribute("role") != null ? session.getAttribute("role") : "STUDENT"
        ));
    }
}
