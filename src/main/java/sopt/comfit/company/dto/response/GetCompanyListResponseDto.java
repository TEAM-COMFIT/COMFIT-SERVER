package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.global.enums.EIndustry;

public record GetCompanyListResponseDto(
        Long id,

        String name,

        EIndustry industry,

        String scale,

        String logo,

        boolean isRecruited,

        Long likeCounts
) {
    public static GetCompanyListResponseDto from(Company company, Long likeCounts) {
        return new GetCompanyListResponseDto(
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
