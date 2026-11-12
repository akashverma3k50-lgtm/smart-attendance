package smart_attendance.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * PHASE 4: Dashboard API endpoint with session check.
 * Restricts dashboard access without login.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public ResponseEntity<?> dashboard(HttpSession session) {
        Object username = session.getAttribute("username");

        // Restrict dashboard access without login
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Object loginTimeObj = session.getAttribute("loginTime");
        String loginTimeStr = (loginTimeObj instanceof Date)
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) loginTimeObj)
                : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String role = session.getAttribute("role") != null
                ? session.getAttribute("role").toString()
                : "STUDENT";

        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Smart Attendance Dashboard",
                "username", username,
                "role", role,
                "loginTime", loginTimeStr
        ));
    }
}