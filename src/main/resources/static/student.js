/**
 * Student & Attendance Management Client Script
 * Includes Dark Theme Support, Graphical Progress Bars, Live Metric Counters,
 * Real-Time Search Filtering & XSS Prevention.
 */

let allLoadedStudents = [];

function escapeHtml(text) {
    if (text === null || text === undefined) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

document.addEventListener("DOMContentLoaded", function () {
    const dateInput = document.getElementById("attendanceDate");
    if (dateInput) {
        dateInput.value = new Date().toISOString().split("T")[0];
        dateInput.addEventListener("change", function () {
            loadStudentsWithAttendance();
        });
    }

    loadStudentsWithAttendance();
    loadAttendanceHistory();

    const studentForm = document.getElementById("studentForm");
    if (studentForm) {
        studentForm.addEventListener("submit", handleAddStudent);
    }

    const searchInput = document.getElementById("searchStudentInput");
    if (searchInput) {
        searchInput.addEventListener("input", function (e) {
            filterStudentTable(e.target.value.toLowerCase().trim());
        });
    }
});

function showStudentAlert(message, type) {
    const alertBox = document.getElementById("studentAlert");
    if (!alertBox) return;
    alertBox.textContent = message;
    alertBox.className = "alert-box " + type;
    alertBox.style.display = "block";
    setTimeout(() => {
        alertBox.style.display = "none";
    }, 4000);
}

// 1. Add New Student
function handleAddStudent(event) {
    event.preventDefault();

    const rollNumber = document.getElementById("rollNumber").value.trim();
    const name = document.getElementById("studentName").value.trim();
    const department = document.getElementById("department").value.trim();
    const semester = document.getElementById("semester").value.trim();

    if (!rollNumber || !name || !department || !semester) {
        showStudentAlert("Please fill in all student fields.", "danger");
        return;
    }

    const student = { rollNumber, name, department, semester };

    fetch("/api/students", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(student)
    })
        .then(async response => {
            const data = await response.json().catch(() => ({}));
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html?error=unauthorized";
                    return;
                }
                throw new Error(data.message || data.error || "Duplicate Roll Number or invalid student data.");
            }
            return data;
        })
        .then(() => {
            showStudentAlert("Student '" + name + "' added successfully!", "success");
            document.getElementById("studentForm").reset();
            loadStudentsWithAttendance();
        })
        .catch(error => {
            showStudentAlert(error.message, "danger");
        });
}

// 2. Load Students with Attendance Summary & Graphics
async function loadStudentsWithAttendance() {
    const tableBody = document.getElementById("studentTableBody");
    if (!tableBody) return;

    try {
        let res = await fetch("/api/attendance/summary", { credentials: "include" });
        if (res.status === 401) {
            window.location.href = "login.html?error=unauthorized";
            return;
        }

        let students;
        if (res.ok) {
            students = await res.json();
        } else {
            const fallbackRes = await fetch("/api/students", { credentials: "include" });
            if (fallbackRes.status === 401) {
                window.location.href = "login.html?error=unauthorized";
                return;
            }
            if (!fallbackRes.ok) {
                throw new Error("Unable to load students.");
            }
            const rawStudents = await fallbackRes.json();
            students = rawStudents.map(s => ({
                ...s,
                presentCount: 0,
                totalClasses: 0,
                percentage: 0,
                todayStatus: "NOT MARKED"
            }));
        }

        allLoadedStudents = students || [];
        updateMetrics(allLoadedStudents);
        renderStudentTable(allLoadedStudents);

    } catch (error) {
        console.error("Error loading student summary:", error);
    }
}

// 3. Update Metric Overview Cards
function updateMetrics(students) {
    const totalEl = document.getElementById("metricTotalStudents");
    const presentEl = document.getElementById("metricTotalPresent");
    const absentEl = document.getElementById("metricTotalAbsent");
    const avgEl = document.getElementById("metricAvgAttendance");

    if (!students || students.length === 0) {
        if (totalEl) totalEl.textContent = "0";
        if (presentEl) presentEl.textContent = "0";
        if (absentEl) absentEl.textContent = "0";
        if (avgEl) avgEl.textContent = "0%";
        return;
    }

    let presentToday = 0;
    let absentToday = 0;
    let sumPercentage = 0;

    students.forEach(s => {
        if (s.todayStatus === "PRESENT") presentToday++;
        if (s.todayStatus === "ABSENT") absentToday++;
        sumPercentage += (s.percentage || 0);
    });

    const avg = students.length > 0 ? (sumPercentage / students.length).toFixed(1) : 0;

    if (totalEl) totalEl.textContent = students.length;
    if (presentEl) presentEl.textContent = presentToday;
    if (absentEl) absentEl.textContent = absentToday;
    if (avgEl) avgEl.textContent = avg + "%";
}

// 4. Render Table with Graphical Progress Bars & Badges
function renderStudentTable(students) {
    const tableBody = document.getElementById("studentTableBody");
    if (!tableBody) return;

    tableBody.innerHTML = "";

    if (!students || students.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; color: var(--text-muted); padding: 28px;">
                    <div style="font-size: 1.8rem; margin-bottom: 8px;">🎓</div>
                    No students found. Use the form above to add students!
                </td>
            </tr>
        `;
        return;
    }

    students.forEach(student => {
        let statusBadgeHtml;
        if (student.todayStatus === "PRESENT") {
            statusBadgeHtml = `<span class="status-badge status-present">● PRESENT</span>`;
        } else if (student.todayStatus === "ABSENT") {
            statusBadgeHtml = `<span class="status-badge status-absent">● ABSENT</span>`;
        } else {
            statusBadgeHtml = `<span class="status-badge status-neutral">○ NOT MARKED</span>`;
        }

        const pct = student.percentage || 0;
        const total = student.totalClasses || 0;
        const present = student.presentCount || 0;

        let fillClass = "fill-good";
        let pctTextColor = "var(--success)";
        if (total > 0 && pct < 75) {
            fillClass = pct >= 50 ? "fill-warning" : "fill-danger";
            pctTextColor = pct >= 50 ? "var(--warning)" : "var(--danger)";
        }

        const safeId = parseInt(student.id, 10);
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><strong>${escapeHtml(student.rollNumber)}</strong></td>
            <td>${escapeHtml(student.name)}</td>
            <td><span class="status-badge status-neutral">${escapeHtml(student.department)}</span></td>
            <td>Sem ${escapeHtml(student.semester)}</td>
            <td>${statusBadgeHtml}</td>
            <td>
                <div class="attendance-progress-wrapper">
                    <div style="display: flex; justify-content: space-between; font-size: 0.82rem;">
                        <span>${present}/${total} Classes</span>
                        <strong style="color: ${pctTextColor};">${pct}%</strong>
                    </div>
                    <div class="attendance-progress-bar">
                        <div class="attendance-progress-fill ${fillClass}" style="width: ${Math.min(pct, 100)}%;"></div>
                    </div>
                </div>
            </td>
            <td>
                <div style="display: flex; gap: 8px;">
                    <button type="button" class="btn btn-success" style="width: auto; padding: 6px 12px; font-size: 0.82rem;"
                        onclick="markStudentAttendance(${safeId}, 'PRESENT')">
                        ✓ Present
                    </button>
                    <button type="button" class="btn btn-danger" style="width: auto; padding: 6px 12px; font-size: 0.82rem;"
                        onclick="markStudentAttendance(${safeId}, 'ABSENT')">
                        ✗ Absent
                    </button>
                </div>
            </td>
            <td>
                <button type="button" class="btn btn-secondary" style="width: auto; padding: 6px 12px; font-size: 0.82rem;"
                    onclick="deleteStudent(${safeId})">
                    🗑️ Delete
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// 5. Search / Filter Students in Real-Time
function filterStudentTable(query) {
    if (!query) {
        renderStudentTable(allLoadedStudents);
        return;
    }
    const filtered = allLoadedStudents.filter(s => {
        const roll = (s.rollNumber || "").toLowerCase();
        const name = (s.name || "").toLowerCase();
        const dept = (s.department || "").toLowerCase();
        return roll.includes(query) || name.includes(query) || dept.includes(query);
    });
    renderStudentTable(filtered);
}

// 6. Mark Attendance (Present or Absent)
function markStudentAttendance(studentId, status) {
    const dateInput = document.getElementById("attendanceDate");
    const date = dateInput && dateInput.value ? dateInput.value : new Date().toISOString().split("T")[0];

    fetch("/api/attendance/mark", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ studentId, status, date })
    })
        .then(async response => {
            const data = await response.json().catch(() => ({}));
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html?error=unauthorized";
                    return;
                }
                throw new Error(data.message || data.error || "Could not mark attendance.");
            }
            return data;
        })
        .then(data => {
            showStudentAlert(data.message, status === "PRESENT" ? "success" : "danger");
            loadStudentsWithAttendance();
            loadAttendanceHistory();
        })
        .catch(error => {
            showStudentAlert(error.message, "danger");
        });
}

// 7. Load Complete Attendance History Log
function loadAttendanceHistory() {
    fetch("/api/attendance", { credentials: "include" })
        .then(async response => {
            if (!response.ok) return [];
            return response.json();
        })
        .then(records => {
            const tbody = document.getElementById("attendanceHistoryBody");
            if (!tbody) return;
            tbody.innerHTML = "";

            if (!records || records.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">
                            No attendance records marked yet. Click "✓ Present" or "✗ Absent" above to mark attendance.
                        </td>
                    </tr>
                `;
                return;
            }

            records.forEach(rec => {
                const badgeClass = rec.status === "PRESENT" ? "status-present" : "status-absent";
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td><strong>${escapeHtml(rec.date)}</strong></td>
                    <td>${escapeHtml(rec.rollNumber)}</td>
                    <td>${escapeHtml(rec.studentName)}</td>
                    <td><span class="status-badge status-neutral">${escapeHtml(rec.department || "-")}</span></td>
                    <td><span class="status-badge ${badgeClass}">${escapeHtml(rec.status)}</span></td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            console.error("Error loading attendance history:", error);
        });
}

// 8. Delete Student
function deleteStudent(id) {
    if (!confirm("Are you sure you want to delete this student and all their attendance records?")) {
        return;
    }

    fetch("/api/students/" + id, {
        method: "DELETE",
        credentials: "include"
    })
        .then(async response => {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = "login.html?error=unauthorized";
                    return;
                }
                throw new Error("Student could not be deleted.");
            }
            return response.text();
        })
        .then(() => {
            showStudentAlert("Student deleted successfully.", "success");
            loadStudentsWithAttendance();
            loadAttendanceHistory();
        })
        .catch(error => {
            showStudentAlert(error.message, "danger");
        });
}
