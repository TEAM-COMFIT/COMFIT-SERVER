package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;

public record CompanySearchResponseDto(
        Long id,
        String name
) {
    public static CompanySearchResponseDto from(Company company) {
        return new CompanySearchResponseDto(
                company.getId(),
                company.getName()
        );
    }
}
