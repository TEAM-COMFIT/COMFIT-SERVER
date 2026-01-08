package sopt.comfit.report.dto.response;

import sopt.comfit.report.domain.AIReport;

import java.time.LocalDate;

public record GetReportSummaryResponseDto(
        Long id,

        String companyName,

        String experienceTitle,

        LocalDate createdAt
) {
    public static GetReportSummaryResponseDto from(AIReport aiRepot) {
        return new GetReportSummaryResponseDto(
                aiRepot.getId(),
                aiRepot.getCompany().getName(),
                aiRepot.getExperience().getTitle(),
                aiRepot.getCreatedAt().toLocalDate()
                );
    }
}
