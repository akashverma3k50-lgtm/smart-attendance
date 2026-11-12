package smart_attendance.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smart_attendance.exception.InvalidInputException;
import smart_attendance.model.Student;
import smart_attendance.repository.AttendanceRepository;
import smart_attendance.repository.StudentRepository;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentServiceImpl(StudentRepository studentRepository, AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public Student addStudent(Student student) {
        if (student == null) {
            throw new InvalidInputException("Student details cannot be empty.");
        }

        // Validate Roll Number
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            throw new InvalidInputException("Roll Number is required.");
        }
        String trimmedRoll = student.getRollNumber().trim();
        if (trimmedRoll.length() < 2 || trimmedRoll.length() > 30) {
            throw new InvalidInputException("Roll Number must be between 2 and 30 characters.");
        }

        // Validate Name
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new InvalidInputException("Student Name is required.");
        }
        String trimmedName = student.getName().trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new InvalidInputException("Student Name must be between 2 and 100 characters.");
        }

        // Validate Department
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new InvalidInputException("Department is required.");
        }
        String trimmedDept = student.getDepartment().trim();
        if (trimmedDept.length() > 50) {
            throw new InvalidInputException("Department name cannot exceed 50 characters.");
        }

        // Validate Semester
        if (student.getSemester() == null || student.getSemester().trim().isEmpty()) {
            throw new InvalidInputException("Semester is required.");
        }
        String trimmedSem = student.getSemester().trim();
        if (trimmedSem.length() > 10) {
            throw new InvalidInputException("Semester cannot exceed 10 characters.");
        }

        // Prevent duplicate Roll Numbers (case-insensitive)
        if (studentRepository.existsByRollNumberIgnoreCase(trimmedRoll)) {
            throw new InvalidInputException("Roll Number '" + trimmedRoll + "' already exists! Duplicate Roll Numbers are not allowed.");
        }

        student.setRollNumber(trimmedRoll);
        student.setName(trimmedName);
        student.setDepartment(trimmedDept);
        student.setSemester(trimmedSem);

        return studentRepository.save(student);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(int id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public boolean deleteStudent(int id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        // Clean up linked attendance records to prevent orphan data
        attendanceRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
        return true;
    }
}
