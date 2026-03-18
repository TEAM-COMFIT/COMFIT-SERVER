package sopt.comfit.report.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    Page<AIReport> findByUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT ar FROM AIReport ar
        JOIN ar.company c
        WHERE ar.userId = :userId
        AND (c.name LIKE %:keyword%)
        """)
    Page<AIReport> findByUserIdAndKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM AIReport a " +
            "JOIN FETCH a.company " +
            "WHERE a.userId = :userId AND a.id = :id")
    Optional<AIReport> findByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
}
