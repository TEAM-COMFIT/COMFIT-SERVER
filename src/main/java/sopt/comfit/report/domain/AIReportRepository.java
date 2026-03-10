package sopt.comfit.report.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    Page<AIReport> findByExperienceUserId(Long experienceUserId, Pageable pageable);
    @Query("""
        SELECT ar FROM AIReport ar
        JOIN ar.experience e
        JOIN ar.company c
        WHERE e.user.id = :userId
        AND (c.name LIKE %:keyword%)
        """)
    Page<AIReport> findByExperienceUserIdAndKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM AIReport a " +
            "JOIN FETCH a.company " +
            "JOIN FETCH a.experience " +
            "WHERE a.experience.user.id = :userId AND a.id = :id")
    Optional<AIReport> findByExperienceUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    boolean existsByCompanyIdAndExperienceUserId(Long companyId, Long userId);
}
