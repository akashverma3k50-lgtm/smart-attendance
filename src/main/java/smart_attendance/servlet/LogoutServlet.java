package smart_attendance.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * ============================================================================
 * PHASE 4: Server-Side Integration and Execution (CLO1, CLO2, CLO3)
 * ============================================================================
 * - Implement logout and session invalidation using HttpSession.
 * - Redirects safely back to the login interface.
 * ============================================================================
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logoutServlet")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processLogout(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processLogout(req, resp);
    }

    private void processLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Retrieve current session without creating a new one
        HttpSession session = req.getSession(false);
        if (session != null) {
            // Invalidate the session, clearing all session attributes
            session.invalidate();
        }

        // Redirect user back to login page
        resp.sendRedirect("login.html?logged_out=true");
    }
}
