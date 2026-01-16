package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.company.domain.EScale;
import sopt.comfit.global.enums.EIndustry;

import java.util.List;

public record GetCompanyResponseDto(
        String name,

        String logo,

        Boolean isLiked,

        EIndustry industry,

        EScale scale,

        String companyURL,

        String summary,

        String talentProfile,

        List<CompanyIssue> issueList
) {
    public static GetCompanyResponseDto of(Company company, Boolean isLiked, List<CompanyIssue> companyIssueList) {
        return new GetCompanyResponseDto(
                company.getName(),
                company.getLogo(),
                isLiked,
                company.getIndustry(),
                company.getScale(),
                company.getCompanyUrl(),
                company.getSummary(),
                company.getTalentProfile(),
                companyIssueList
        );
    }
}
