package sopt.comfit.experience.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience,Long> {
    Optional<Experience> findByUserIdAndIsDefaultTrue(Long userId);
}
