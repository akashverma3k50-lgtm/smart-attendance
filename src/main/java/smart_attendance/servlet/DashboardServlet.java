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
 * - Restrict dashboard access without login.
 * - Forwards authenticated user requests to the JSP dashboard page.
 * ============================================================================
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboardServlet")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Restrict dashboard access without login:
        // getSession(false) prevents creating a spurious session if not already logged in
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            // Unauthenticated user is redirected to login with an error flag
            resp.sendRedirect("login.html?error=unauthorized");
            return;
        }

        // Forward to the JSP page for user dashboard
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
