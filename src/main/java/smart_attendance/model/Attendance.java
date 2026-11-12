package smart_attendance.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

/**
 * JPA Entity representing a Student's Attendance record on a specific date.
 */
@Entity
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private LocalDate date;
    private String status; // "PRESENT" or "ABSENT"

    public Attendance() {
    }

    public Attendance(int studentId, String rollNumber, String studentName,
                      String department, LocalDate date, String status) {
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.department = department;
        this.date = date != null ? date : LocalDate.now();
        this.status = status != null ? status.toUpperCase() : "PRESENT";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
