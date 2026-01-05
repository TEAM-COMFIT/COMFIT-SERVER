package sopt.comfit.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sopt.comfit.user.dto.UserSecurityForm;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.id as userId, " +
            "       u.password as password, " +
            "       u.role as role " +
            "FROM User u " +
            "WHERE u.id = :id")
    Optional<UserSecurityForm> findUserSecurityFormById(@Param("id") Long id);

    Optional<User> findByEmail(String email);
}
