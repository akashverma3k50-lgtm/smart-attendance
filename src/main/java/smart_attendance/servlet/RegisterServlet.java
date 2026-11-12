package smart_attendance.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.exception.UserAlreadyExistsException;
import smart_attendance.model.User;
import smart_attendance.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================================
 * PHASE 4: Server-Side Integration and Execution (CLO1, CLO2, CLO3)
 * ============================================================================
 * - Process registration using HttpServlet (GET / POST).
 * - Server-side validation of user credentials.
 * - Handles both standard HTML form parameters and JSON body payloads.
 * ============================================================================
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/registerServlet")
public class RegisterServlet extends HttpServlet {

    private final UserService userService;

    public RegisterServlet(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests: redirects to registration form.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("register.html");
    }

    /**
     * Handles POST requests: processes user registration.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = req.getContentType();
        boolean isJson = contentType != null && contentType.toLowerCase().contains("application/json");
        User userToRegister;

        if (isJson) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString();
            String username = extractJsonField(json, "username");
            String password = extractJsonField(json, "password");
            String fullName = extractJsonField(json, "fullName");
            String email = extractJsonField(json, "email");
            String role = extractJsonField(json, "role");
            userToRegister = new User(username, password, fullName, email, role);
        } else {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");
            String role = req.getParameter("role");
            userToRegister = new User(username, password, fullName, email, role);
        }

        // Server-side credential and input validation
        try {
            if (userService != null) {
                userService.registerUser(userToRegister);
            }

            if (isJson) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, "Registration successful! You can now log in.");
            } else {
                resp.sendRedirect("login.html?registered=true");
            }

        } catch (InvalidInputException e) {
            handleError(resp, isJson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (UserAlreadyExistsException e) {
            handleError(resp, isJson, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            handleError(resp, isJson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    private void handleError(HttpServletResponse resp, boolean isJson, int status, String message) throws IOException {
        if (isJson) {
            sendJsonResponse(resp, status, message);
        } else {
            resp.sendRedirect("register.html?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String safeMessage = message != null ? message.replace("\"", "\\\"") : "";
        String json = String.format("{\"message\":\"%s\",\"status\":%d}", safeMessage, status);
        resp.getWriter().write(json);
    }

    // Simple JSON field parser using Java string handling
    private String extractJsonField(String json, String fieldName) {
        if (json == null) return null;
        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx == -1) return null;
        int firstQuote = json.indexOf('"', colonIdx + 1);
        if (firstQuote == -1) return null;
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }
}
