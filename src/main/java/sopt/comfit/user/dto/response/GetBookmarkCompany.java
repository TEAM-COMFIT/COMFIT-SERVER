package sopt.comfit.user.dto.response;

import sopt.comfit.user.domain.UserCompany;

import java.time.LocalDate;

public record GetBookmarkCompany(

        Long id,

        Long companyId,

        String name,

        LocalDate createdAt,

        boolean isConnected
) {
    public static GetBookmarkCompany from(UserCompany userCompany){
        return new GetBookmarkCompany(
                userCompany.getId(),
                userCompany.getCompany().getId(),
                userCompany.getCompany().getName(),
                userCompany.getCreatedAt().toLocalDate(),
                userCompany.isConnected()
        );
    }
}
