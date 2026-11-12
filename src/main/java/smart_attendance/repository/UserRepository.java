package smart_attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smart_attendance.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
