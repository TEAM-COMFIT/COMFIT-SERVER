package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;

public record GetCompanySearchResponseDto(
        Long id,
        String name
) {
    public static GetCompanySearchResponseDto from(Company company) {
        return new GetCompanySearchResponseDto(
                company.getId(),
                company.getName()
        );
    }
}
