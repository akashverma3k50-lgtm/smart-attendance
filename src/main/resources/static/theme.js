/**
 * Smart Attendance Management System - Theme Manager
 * Supports Dark Theme (Default) and Light Theme with smooth transitions.
 */
(function () {
    const THEME_KEY = "smart_attendance_theme";

    // Initialize theme as early as possible to prevent flicker
    function getPreferredTheme() {
        const storedTheme = localStorage.getItem(THEME_KEY);
        if (storedTheme) {
            return storedTheme;
        }
        // Default to dark theme for modern high-tech aesthetic
        return "dark";
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute("data-theme", theme);
        if (document.body) {
            document.body.setAttribute("data-theme", theme);
        }
        localStorage.setItem(THEME_KEY, theme);
        updateToggleButtons(theme);
    }

    function updateToggleButtons(theme) {
        const toggles = document.querySelectorAll(".theme-toggle-btn");
        toggles.forEach(btn => {
            if (theme === "dark") {
                btn.innerHTML = `<span class="theme-icon">☀️</span> <span class="theme-label">Light</span>`;
                btn.setAttribute("title", "Switch to Light Theme");
                btn.setAttribute("aria-label", "Switch to Light Theme");
            } else {
                btn.innerHTML = `<span class="theme-icon">🌙</span> <span class="theme-label">Dark</span>`;
                btn.setAttribute("title", "Switch to Dark Theme");
                btn.setAttribute("aria-label", "Switch to Dark Theme");
            }
        });
    }

    // Apply theme immediately
    const initialTheme = getPreferredTheme();
    document.documentElement.setAttribute("data-theme", initialTheme);

    document.addEventListener("DOMContentLoaded", function () {
        applyTheme(getPreferredTheme());

        // Attach click listeners to all theme toggle buttons
        document.querySelectorAll(".theme-toggle-btn").forEach(btn => {
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
                const newTheme = currentTheme === "dark" ? "light" : "dark";
                applyTheme(newTheme);
            });
        });
    });

    // Expose toggle function globally
    window.toggleTheme = function () {
        const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
        const newTheme = currentTheme === "dark" ? "light" : "dark";
        applyTheme(newTheme);
    };
})();
