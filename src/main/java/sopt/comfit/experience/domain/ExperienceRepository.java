package sopt.comfit.experience.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sopt.comfit.user.domain.User;

import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience,Long> {
    Optional<Experience> findByUserIdAndIsDefaultTrue(Long userId);
    Page<Experience> findByUserId(Long userId, Pageable pageable);
    Page<Experience> findByUserIdAndType(Long userId, EType type,  Pageable pageable);
    Optional<Experience> findByIdAndUserId(Long experienceId, Long userId);

    Long user(User user);
}
