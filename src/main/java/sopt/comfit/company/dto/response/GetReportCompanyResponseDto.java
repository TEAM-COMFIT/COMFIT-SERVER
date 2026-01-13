package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.global.enums.EIndustry;

public record GetReportCompanyResponseDto(
        String name,

        EIndustry industry,

        String recruitUrl,

        String companyUrl,

      String logo
) {
    public static GetReportCompanyResponseDto from (Company company) {
        return new GetReportCompanyResponseDto(
                company.getName(),
                company.getIndustry(),
                company.getRecruitUrl(),
                company.getCompanyUrl(),
                company.getLogo()
        );
    }
}
