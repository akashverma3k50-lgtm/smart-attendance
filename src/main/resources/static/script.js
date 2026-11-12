/**
 * ============================================================================
 * PHASE 3: Web Interface and Client Interaction (CLO2)
 * ============================================================================
 * Features:
 * 1. JavaScript validation:
 *    - Empty fields check
 *    - Format checks (password min length, matching passwords, username format)
 * 2. Basic interaction handling:
 *    - Form submission flow interception
 *    - Dynamic UI alerts (error/success banners)
 *    - Redirects upon success
 * 3. Represent user data format using JSON (simple structure):
 *    - Structured JavaScript object serialized with JSON.stringify()
 *    - Sent with application/json header via modern fetch API
 *    - Parses server JSON response
 * ============================================================================
 */

document.addEventListener("DOMContentLoaded", function () {
    initRegistrationForm();
    initLoginForm();
});

// Helper: Show alert banner on forms
function showAlert(boxId, message, type) {
    const box = document.getElementById(boxId);
    if (!box) return;
    box.textContent = message;
    box.className = "alert-box " + type; // "danger" or "success"
    box.style.display = "block";
}

// Helper: Clear alert banner
function clearAlert(boxId) {
    const box = document.getElementById(boxId);
    if (!box) return;
    box.textContent = "";
    box.style.display = "none";
}

/**
 * ----------------------------------------------------------------------------
 * 1. Registration Form Validation & JSON Submission Flow
 * ----------------------------------------------------------------------------
 */
function initRegistrationForm() {
    const registerForm = document.getElementById("registerForm");
    if (!registerForm) return;

    registerForm.addEventListener("submit", async function (event) {
        // Prevent default browser form submission to handle interaction flow via JS
        event.preventDefault();
        clearAlert("alertBox");

        const fullNameInput = document.getElementById("fullName");
        const usernameInput = document.getElementById("username");
        const emailInput = document.getElementById("email");
        const passwordInput = document.getElementById("password");
        const confirmPasswordInput = document.getElementById("confirmPassword");
        const submitBtn = document.getElementById("registerBtn");

        const fullName = fullNameInput ? fullNameInput.value.trim() : "";
        const username = usernameInput ? usernameInput.value.trim() : "";
        const email = emailInput ? emailInput.value.trim() : "";
        const password = passwordInput ? passwordInput.value : "";
        const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : "";

        // --- Client-Side JavaScript Validation: Empty Field Checks ---
        if (fullName === "") {
            showAlert("alertBox", "Please enter your full name.", "danger");
            fullNameInput.focus();
            return;
        }

        if (username === "") {
            showAlert("alertBox", "Please enter a username.", "danger");
            usernameInput.focus();
            return;
        }

        if (password === "") {
            showAlert("alertBox", "Please enter a password.", "danger");
            passwordInput.focus();
            return;
        }

        // --- Client-Side JavaScript Validation: Format Checks ---
        if (username.length < 3) {
            showAlert("alertBox", "Username must be at least 3 characters long.", "danger");
            usernameInput.focus();
            return;
        }

        if (password.length < 6) {
            showAlert("alertBox", "Password must contain at least 6 characters.", "danger");
            passwordInput.focus();
            return;
        }

        if (password !== confirmPassword) {
            showAlert("alertBox", "Passwords do not match. Please re-enter.", "danger");
            confirmPasswordInput.focus();
            return;
        }

        if (email !== "" && !email.includes("@")) {
            showAlert("alertBox", "Please enter a valid email address.", "danger");
            emailInput.focus();
            return;
        }

        // --- Represent User Data Format Using JSON (Simple Structure) ---
        const userJSONPayload = {
            fullName: fullName,
            username: username,
            email: email,
            password: password,
            role: "STUDENT"
        };

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = "Registering...";

            // Send JSON data to backend (supports both Servlet and REST endpoints)
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(userJSONPayload)
            });

            const data = await response.json();

            if (!response.ok) {
                const errorMsg = data.message || data.error || "Registration failed.";
                showAlert("alertBox", errorMsg, "danger");
                submitBtn.disabled = false;
                submitBtn.textContent = "Register";
                return;
            }

            // Success feedback
            showAlert("alertBox", (data.message || "Registration successful!") + " Redirecting to login...", "success");
            setTimeout(function () {
                window.location.href = "login.html";
            }, 1200);

        } catch (err) {
            console.error("Registration error:", err);
            showAlert("alertBox", "Network error while connecting to server.", "danger");
            submitBtn.disabled = false;
            submitBtn.textContent = "Register";
        }
    });
}

/**
 * ----------------------------------------------------------------------------
 * 2. Login Form Validation & JSON Submission Flow
 * ----------------------------------------------------------------------------
 */
function initLoginForm() {
    const loginForm = document.getElementById("loginForm");
    if (!loginForm) return;

    // Check URL parameters for status banners
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("error") === "unauthorized") {
        showAlert("loginAlertBox", "Access Denied: Please log in to access this page.", "danger");
    } else if (urlParams.get("error") === "invalid") {
        showAlert("loginAlertBox", "Invalid username or password.", "danger");
    } else if (urlParams.get("registered") === "true") {
        showAlert("loginAlertBox", "Registration successful! You may now log in.", "success");
    } else if (urlParams.get("logged_out") === "true" || urlParams.get("logout") === "true") {
        showAlert("loginAlertBox", "You have been logged out securely.", "success");
    }

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();
        clearAlert("loginAlertBox");

        const usernameInput = document.getElementById("loginUsername");
        const passwordInput = document.getElementById("loginPassword");
        const submitBtn = document.getElementById("loginBtn");

        const username = usernameInput ? usernameInput.value.trim() : "";
        const password = passwordInput ? passwordInput.value : "";

        // --- JavaScript Validation: Empty field checks ---
        if (username === "" || password === "") {
            showAlert("loginAlertBox", "Please enter both username and password.", "danger");
            return;
        }

        // --- Represent User Data Format Using JSON ---
        const loginJSONPayload = {
            username: username,
            password: password
        };

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = "Signing in...";

            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include", // preserves HttpSession cookie across requests
                body: JSON.stringify(loginJSONPayload)
            });

            const data = await response.json();

            if (!response.ok) {
                const errorMsg = data.message || data.error || "Invalid username or password.";
                showAlert("loginAlertBox", errorMsg, "danger");
                submitBtn.disabled = false;
                submitBtn.textContent = "Login";
                return;
            }

            showAlert("loginAlertBox", "Login successful! Loading dashboard...", "success");
            setTimeout(function () {
                // Redirect directly to JSP dashboard (or HTML dashboard)
                window.location.href = "/dashboard.jsp";
            }, 800);

        } catch (err) {
            console.error("Login error:", err);
            showAlert("loginAlertBox", "Network error while connecting to server.", "danger");
            submitBtn.disabled = false;
            submitBtn.textContent = "Login";
        }
    });
}