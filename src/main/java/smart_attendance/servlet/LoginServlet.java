package smart_attendance.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import smart_attendance.exception.InvalidCredentialsException;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.model.User;
import smart_attendance.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ============================================================================
 * PHASE 4: Server-Side Integration and Execution (CLO1, CLO2, CLO3)
 * ============================================================================
 * - Process login using HttpServlet (GET / POST).
 * - Validate user credentials on server side against hashed passwords.
 * - Protect against session fixation by rotating session ID upon login.
 * - Implement session tracking using HttpSession.
 * ============================================================================
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/loginServlet")
public class LoginServlet extends HttpServlet {

    private final UserService userService;

    public LoginServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = req.getContentType();
        boolean isJson = contentType != null && contentType.toLowerCase().contains("application/json");
        String username;
        String password;

        if (isJson) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString();
            username = extractJsonField(json, "username");
            password = extractJsonField(json, "password");
        } else {
            username = req.getParameter("username");
            password = req.getParameter("password");
        }

        try {
            // Validate user credentials on server side
            if (userService != null) {
                userService.authenticateUser(username, password);
            }

            // Invalidate previous session to prevent session fixation attacks
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Implement session tracking using a fresh HttpSession
            HttpSession session = req.getSession(true);
            session.setAttribute("username", username.trim());
            session.setAttribute("loginTime", new Date());

            if (userService != null) {
                User user = userService.findByUsername(username.trim());
                if (user != null) {
                    session.setAttribute("user", user);
                    session.setAttribute("role", user.getRole());
                }
            }

            if (isJson) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, "Login successful!");
            } else {
                resp.sendRedirect("dashboard.jsp");
            }

        } catch (InvalidInputException e) {
            handleError(resp, isJson, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InvalidCredentialsException e) {
            handleError(resp, isJson, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            handleError(resp, isJson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }

    private void handleError(HttpServletResponse resp, boolean isJson, int status, String message) throws IOException {
        if (isJson) {
            sendJsonResponse(resp, status, message);
        } else {
            resp.sendRedirect("login.html?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
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
