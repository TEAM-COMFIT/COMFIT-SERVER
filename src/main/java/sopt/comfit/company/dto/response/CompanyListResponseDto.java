package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.global.enums.EIndustry;

public record CompanyListResponseDto(
        Long id,
        String name,
        EIndustry industry,
        String scale,
        String logo,
        boolean isRecruited,
        Long likeCounts
) {
    public static CompanyListResponseDto from(Company company, Long likeCounts) {
        return new CompanyListResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getScale().name(),
                company.getLogo(),
                company.isRecruiting(),
                likeCounts
        );
    }
}
