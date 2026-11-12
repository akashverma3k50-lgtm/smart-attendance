package smart_attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smart_attendance.exception.InvalidInputException;
import smart_attendance.model.Attendance;
import smart_attendance.model.Student;
import smart_attendance.repository.AttendanceRepository;
import smart_attendance.repository.StudentRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Mark or update attendance for a student on a given date (defaults to today).
     */
    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, Object> payload) {
        if (payload == null || payload.get("studentId") == null || payload.get("status") == null) {
            throw new InvalidInputException("Both studentId and status are required.");
        }

        int studentId;
        try {
            studentId = Integer.parseInt(payload.get("studentId").toString());
            if (studentId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid studentId provided.");
        }

        String rawStatus = payload.get("status").toString().trim().toUpperCase();
        if (!"PRESENT".equals(rawStatus) && !"ABSENT".equals(rawStatus)) {
            throw new InvalidInputException("Attendance status must strictly be either 'PRESENT' or 'ABSENT'.");
        }

        LocalDate date;
        if (payload.get("date") != null && !payload.get("date").toString().trim().isEmpty()) {
            try {
                date = LocalDate.parse(payload.get("date").toString().trim());
            } catch (DateTimeParseException e) {
                throw new InvalidInputException("Invalid date format. Expected yyyy-MM-dd.");
            }
        } else {
            date = LocalDate.now();
        }

        // Do not allow marking attendance for future dates beyond tomorrow (timezone guard)
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new InvalidInputException("Attendance cannot be marked for future dates.");
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new InvalidInputException("Student with ID " + studentId + " does not exist.");
        }
        Student student = studentOpt.get();

        // Update existing record for this student & date if present, otherwise create a new one
        Optional<Attendance> existingOpt = attendanceRepository.findByStudentIdAndDate(studentId, date);
        Attendance record;
        if (existingOpt.isPresent()) {
            record = existingOpt.get();
            record.setStatus(rawStatus);
        } else {
            record = new Attendance(
                    student.getId(),
                    student.getRollNumber(),
                    student.getName(),
                    student.getDepartment(),
                    date,
                    rawStatus
            );
        }

        Attendance saved = attendanceRepository.save(record);
        return ResponseEntity.ok(Map.of(
                "message", "Attendance marked as " + rawStatus + " for " + student.getName() + " (" + student.getRollNumber() + ")",
                "record", saved
        ));
    }

    /**
     * Get all attendance records ordered by latest date.
     */
    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAllByOrderByDateDescIdDesc();
    }

    /**
     * Get attendance summary per student (present count, total classes, percentage, today's status).
     */
    @GetMapping("/summary")
    public List<Map<String, Object>> getStudentAttendanceSummary() {
        List<Student> students = studentRepository.findAll();
        List<Attendance> allRecords = attendanceRepository.findAll();
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Student s : students) {
            long presentCount = allRecords.stream()
                    .filter(a -> a.getStudentId() == s.getId() && "PRESENT".equalsIgnoreCase(a.getStatus()))
                    .count();
            long absentCount = allRecords.stream()
                    .filter(a -> a.getStudentId() == s.getId() && "ABSENT".equalsIgnoreCase(a.getStatus()))
                    .count();
            long total = presentCount + absentCount;
            double percentage = total > 0 ? Math.round((presentCount * 1000.0) / total) / 10.0 : 0.0;

            String todayStatus = allRecords.stream()
                    .filter(a -> a.getStudentId() == s.getId() && today.equals(a.getDate()))
                    .map(Attendance::getStatus)
                    .findFirst()
                    .orElse("NOT MARKED");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("rollNumber", s.getRollNumber());
            item.put("name", s.getName());
            item.put("department", s.getDepartment());
            item.put("semester", s.getSemester());
            item.put("presentCount", presentCount);
            item.put("absentCount", absentCount);
            item.put("totalClasses", total);
            item.put("percentage", percentage);
            item.put("todayStatus", todayStatus);
            result.add(item);
        }
        return result;
    }
}
