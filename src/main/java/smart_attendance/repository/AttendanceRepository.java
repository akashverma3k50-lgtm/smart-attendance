package smart_attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import smart_attendance.model.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByStudentIdOrderByDateDesc(int studentId);

    Optional<Attendance> findByStudentIdAndDate(int studentId, LocalDate date);

    List<Attendance> findAllByOrderByDateDescIdDesc();

    @Modifying
    @Transactional
    void deleteByStudentId(int studentId);
}
