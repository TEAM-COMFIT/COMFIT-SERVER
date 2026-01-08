package sopt.comfit.report.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
}
