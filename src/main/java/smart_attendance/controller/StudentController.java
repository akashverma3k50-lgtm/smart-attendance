package smart_attendance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smart_attendance.model.Student;
import smart_attendance.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Add Student
    @PostMapping
    public ResponseEntity<Student> addStudent(
            @RequestBody Student student) {

        Student newStudent =
                studentService.addStudent(student);

        return ResponseEntity.ok(newStudent);
    }

    // Get All Students
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    // Get Student By ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @PathVariable int id) {

        Student student =
                studentService.getStudentById(id);

        if (student == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(student);
    }

    // Delete Student
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable int id) {

        boolean deleted =
                studentService.deleteStudent(id);

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Student deleted successfully"
        );
    }
}