package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.EScale;
import sopt.comfit.global.enums.EIndustry;

public record GetSuggestionCompanyResponseDto(
        Long id,

        String name,

        EIndustry industry,

        EScale scale,

        String logo
) {
    public static GetSuggestionCompanyResponseDto from(Company company) {
        return new GetSuggestionCompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getScale(),
                company.getLogo()
        );
    }
}
