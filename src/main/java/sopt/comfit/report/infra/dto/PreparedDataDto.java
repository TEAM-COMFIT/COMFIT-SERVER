package sopt.comfit.report.infra.dto;

import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.experience.domain.Experience;

import java.util.List;

public record PreparedDataDto(
        Company company,
        Experience experience,
        String jobDescription,
        List<CompanyIssue> issues
) {
    public static PreparedDataDto of(Company company,
                                     Experience experience,
                                     String jobDescription,
                                     List<CompanyIssue> issues) {
        return new PreparedDataDto(company, experience, jobDescription, issues);
    }
}
