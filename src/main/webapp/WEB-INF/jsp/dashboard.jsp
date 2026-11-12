<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="smart_attendance.model.User" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

<%
    // Restrict dashboard access without login:
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("username") == null) {
        response.sendRedirect("login.html?error=unauthorized");
        return;
    }

    String username = (String) userSession.getAttribute("username");
    Object loginTimeObj = userSession.getAttribute("loginTime");
    String loginTimeStr = (loginTimeObj instanceof Date) 
        ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) loginTimeObj) 
        : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    User user = (User) userSession.getAttribute("user");
    String fullName = (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) ? user.getFullName() : username;
    String role = (userSession.getAttribute("role") != null) ? (String) userSession.getAttribute("role") : "STUDENT";

    // Escape HTML for XSS prevention in scriptlet output
    fullName = fullName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    username = username.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    role = role.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
%>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Smart Attendance Management System</title>
    <link rel="stylesheet" href="style.css">
    <script src="theme.js"></script>
</head>
<body data-theme="dark">
    <!-- Ambient Lighting Effects -->
    <div class="ambient-glow-1"></div>
    <div class="ambient-glow-2"></div>

    <header>
        <a href="index.html" class="brand">
            <div class="brand-icon">📋</div>
            <span class="brand-text">Smart Attendance Dashboard</span>
        </a>
        <div class="nav-links">
            <button type="button" class="theme-toggle-btn" aria-label="Toggle Theme">
                <span class="theme-icon">☀️</span> <span class="theme-label">Light</span>
            </button>
            <span class="user-badge">👤 <%= username %> (<%= role %>)</span>
            <a href="students.html">Students &amp; Attendance</a>
            <form action="/logoutServlet" method="POST" style="display: inline; margin: 0;">
                <button type="submit" class="btn btn-danger" style="width: auto; padding: 7px 16px; font-size: 0.85rem; margin: 0;">
                    Logout
                </button>
            </form>
        </div>
    </header>

    <div class="main-wrapper" style="align-items: flex-start;">
        <div class="container" style="max-width: 1100px;">
            <div class="dashboard-header">
                <div>
                    <h1>Welcome, <span class="gradient-text"><%= fullName %></span>!</h1>
                    <p class="subtitle" style="margin-bottom: 0;">
                        Authenticated via <strong>HttpSession</strong> (Login Time: <%= loginTimeStr %>)
                    </p>
                </div>
                <div>
                    <span class="status-badge status-present">
                        <span class="pulse-dot"></span> Session Active
                    </span>
                </div>
            </div>

            <!-- Dashboard Overview Cards -->
            <div class="dashboard-cards">
                <div class="card">
                    <h3>👨‍🎓 Students &amp; Attendance</h3>
                    <p>Add students, mark daily attendance, and view full attendance percentages.</p>
                    <a href="students.html" class="btn" style="padding: 10px 18px; font-size: 0.9rem;">
                        <span>Open Student Attendance &rarr;</span>
                    </a>
                </div>

                <div class="card">
                    <h3>📊 Total Enrolled Students</h3>
                    <p>Active verified students registered in the system.</p>
                    <h2 id="totalStudentsCount" style="color: var(--primary); font-size: 2.2rem; margin: 0;">0</h2>
                </div>

                <div class="card">
                    <h3>🔒 Session Security</h3>
                    <p>End your authenticated session and clear your security tokens.</p>
                    <a href="/logoutServlet" class="btn btn-secondary" style="padding: 10px 18px; font-size: 0.9rem;">
                        <span>Logout Securely</span>
                    </a>
                </div>
            </div>

            <!-- Live Student Attendance Table on Dashboard -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 10px;">
                <h3 style="margin: 0;">📋 Today's Attendance Roster</h3>
                <a href="students.html" class="btn" style="width: auto; padding: 8px 16px; font-size: 0.88rem;">+ Manage Students</a>
            </div>
            <div id="dashAlert" class="alert-box"></div>

            <div style="overflow-x: auto; border-radius: var(--radius);">
                <table>
                    <thead>
                        <tr>
                            <th>Roll Number</th>
                            <th>Student Name</th>
                            <th>Department</th>
                            <th>Sem</th>
                            <th>Today's Status</th>
                            <th>Attendance %</th>
                            <th>Quick Action</th>
                        </tr>
                    </thead>
                    <tbody id="dashboardStudentBody">
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">Loading student attendance...</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
        function escapeHtml(text) {
            if (text === null || text === undefined) return "";
            return String(text)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        function loadDashboardAttendance() {
            fetch("/api/attendance/summary", { credentials: "include" })
                .then(res => {
                    if (!res.ok) {
                        if (res.status === 401) {
                            window.location.href = "login.html?error=unauthorized";
                        }
                        return [];
                    }
                    return res.json();
                })
                .then(students => {
                    const countEl = document.getElementById("totalStudentsCount");
                    if (countEl) countEl.textContent = students.length;

                    const tbody = document.getElementById("dashboardStudentBody");
                    if (!tbody) return;
                    tbody.innerHTML = "";

                    if (!students || students.length === 0) {
                        tbody.innerHTML = `
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 24px; color: var(--text-muted);">
                                    No students registered yet. <a href="students.html" style="color: var(--primary); font-weight: 600;">Click here to add students</a>.
                                </td>
                            </tr>
                        `;
                        return;
                    }

                    students.forEach(s => {
                        let badge = '<span class="status-badge status-neutral">○ NOT MARKED</span>';
                        if (s.todayStatus === "PRESENT") badge = '<span class="status-badge status-present">● PRESENT</span>';
                        if (s.todayStatus === "ABSENT") badge = '<span class="status-badge status-absent">● ABSENT</span>';

                        const pct = s.percentage || 0;
                        const total = s.totalClasses || 0;
                        const present = s.presentCount || 0;

                        let fillClass = "fill-good";
                        let pctTextColor = "var(--success)";
                        if (total > 0 && pct < 75) {
                            fillClass = pct >= 50 ? "fill-warning" : "fill-danger";
                            pctTextColor = pct >= 50 ? "var(--warning)" : "var(--danger)";
                        }

                        const safeId = parseInt(s.id, 10);
                        const tr = document.createElement("tr");
                        tr.innerHTML = 
                            '<td><strong>' + escapeHtml(s.rollNumber) + '</strong></td>' +
                            '<td>' + escapeHtml(s.name) + '</td>' +
                            '<td><span class="status-badge status-neutral">' + escapeHtml(s.department) + '</span></td>' +
                            '<td>Sem ' + escapeHtml(s.semester) + '</td>' +
                            '<td>' + badge + '</td>' +
                            '<td>' +
                                '<div class="attendance-progress-wrapper">' +
                                    '<div style="display: flex; justify-content: space-between; font-size: 0.82rem;">' +
                                        '<span>' + present + '/' + total + '</span>' +
                                        '<strong style="color: ' + pctTextColor + ';">' + pct + '%</strong>' +
                                    '</div>' +
                                    '<div class="attendance-progress-bar">' +
                                        '<div class="attendance-progress-fill ' + fillClass + '" style="width: ' + Math.min(pct, 100) + '%;"></div>' +
                                    '</div>' +
                                '</div>' +
                            '</td>' +
                            '<td>' +
                                '<div style="display: flex; gap: 6px;">' +
                                    '<button type="button" class="btn btn-success" style="width: auto; padding: 5px 10px; font-size: 0.8rem;" ' +
                                        'onclick="quickMark(' + safeId + ', \'PRESENT\')">✓ Present</button>' +
                                    '<button type="button" class="btn btn-danger" style="width: auto; padding: 5px 10px; font-size: 0.8rem;" ' +
                                        'onclick="quickMark(' + safeId + ', \'ABSENT\')">✗ Absent</button>' +
                                '</div>' +
                            '</td>';
                        tbody.appendChild(tr);
                    });
                })
                .catch(err => console.error("Error loading dashboard attendance:", err));
        }

        function quickMark(studentId, status) {
            fetch("/api/attendance/mark", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ studentId, status })
            })
            .then(res => res.json())
            .then(data => {
                const alertBox = document.getElementById("dashAlert");
                alertBox.textContent = data.message;
                alertBox.className = "alert-box " + (status === "PRESENT" ? "success" : "danger");
                alertBox.style.display = "block";
                setTimeout(() => alertBox.style.display = "none", 3000);
                loadDashboardAttendance();
            });
        }

        document.addEventListener("DOMContentLoaded", loadDashboardAttendance);
    </script>
</body>
</html>
