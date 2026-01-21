package sopt.comfit.company.dto.response;

import jakarta.persistence.Column;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.company.domain.EScale;
import sopt.comfit.global.enums.EIndustry;

import java.time.LocalDate;
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

        Boolean isRecruiting,

        List<IssueItem> issueList
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
                company.isRecruiting(),
                companyIssueList.stream()
                        .map(IssueItem::from)
                        .toList()
        );
    }

    public static record IssueItem(
            String title,

            String content,

            String issueURL,

            LocalDate issueDate
    ){
        public static IssueItem from (CompanyIssue issue){
            return new IssueItem(
                    issue.getTitle(),
                    issue.getContent(),
                    issue.getIssueURL(),
                    issue.getIssueDate()
            );
        }
    }

}
