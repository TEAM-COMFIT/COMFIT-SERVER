package sopt.comfit.company.dto.response;

import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.ERandomPhoto;
import sopt.comfit.global.enums.EIndustry;

public record FeaturedCompanyResponseDto(
        Long id,
        String name,
        EIndustry industry,
        String scale,
        String logo,
        String photoUrl
) {
    public static FeaturedCompanyResponseDto from(Company company) {
        return new FeaturedCompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getScale().name(),
                company.getLogo(),
                ERandomPhoto.random().getPhotoUrl()
        );
    }
}
