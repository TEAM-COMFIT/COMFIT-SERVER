package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.ERandomPhoto;
import sopt.comfit.global.enums.EIndustry;

public record FeaturedCompanyResponseDto(
        Long id,
        String name,
        String scale,
        String photoUrl
) {
    public static FeaturedCompanyResponseDto from(Company company) {
        return new FeaturedCompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getScale().name(),
                ERandomPhoto.random().getPhotoUrl()
        );
    }
}
