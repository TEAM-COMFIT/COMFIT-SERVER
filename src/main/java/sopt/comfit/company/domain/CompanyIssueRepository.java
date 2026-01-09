package sopt.comfit.company.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyIssueRepository extends JpaRepository<CompanyIssue, Long> {
    List<CompanyIssue> findByCompanyId(Long companyId);
}
