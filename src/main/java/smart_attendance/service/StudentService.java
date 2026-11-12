package smart_attendance.service;

import java.util.List;

import smart_attendance.model.Student;

public interface StudentService {

    Student addStudent(Student student);

    List<Student> getAllStudents();

    Student getStudentById(int id);

    boolean deleteStudent(int id);
}