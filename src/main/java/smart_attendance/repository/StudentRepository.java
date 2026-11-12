package smart_attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smart_attendance.model.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    boolean existsByRollNumberIgnoreCase(String rollNumber);
}