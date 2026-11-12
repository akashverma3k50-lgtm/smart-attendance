package smart_attendance.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Enterprise Authentication & Access Control Filter.
 * Strictly verifies HttpSession authentication before allowing access
 * to protected web pages (students.html, dashboard.jsp) and REST endpoints
 * (/api/students/**, /api/attendance/**).
 */
@Component
@Order(1)
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Force browser to fetch fresh content during development
        res.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // Determine if current path requires authentication
        if (isProtectedPath(path)) {
            HttpSession session = req.getSession(false);
            boolean isAuthenticated = session != null && session.getAttribute("username") != null;

            if (!isAuthenticated) {
                // If it's a REST API endpoint, return 401 JSON
                if (path.startsWith("/api/")) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Please log in to access this resource.\"}");
                    return;
                }

                // If it's a web page, redirect to login
                res.sendRedirect(req.getContextPath() + "/login.html?error=unauthorized");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        if (path == null) return false;

        // Specific protected HTML/JSP pages & servlets
        if (path.equals("/students.html") ||
            path.equals("/dashboard.jsp") ||
            path.equals("/dashboard.html") ||
            path.equals("/dashboardServlet")) {
            return true;
        }

        // Protected REST APIs
        if (path.startsWith("/api/students") ||
            path.startsWith("/api/attendance") ||
            path.equals("/api/dashboard")) {
            return true;
        }

        return false;
    }
}
