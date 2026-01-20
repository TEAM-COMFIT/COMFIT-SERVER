package sopt.comfit.experience.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience,Long> {
    Optional<Experience> findByUserIdAndIsDefaultTrue(Long userId);
    Page<Experience> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId, Pageable pageable);
    Page<Experience> findByUserIdAndTypeOrderByIsDefaultDescCreatedAtDesc(Long userId, EType type, Pageable pageable);
    Optional<Experience> findByIdAndUserId(Long experienceId, Long userId);
    List<Experience> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
}
