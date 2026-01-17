package sopt.comfit.company.dto.response;

import java.util.List;

public record CompanySearchListResponseDto(
        List<CompanySearchResponseDto> searchList
) {
    public static CompanySearchListResponseDto from(List<CompanySearchResponseDto> searchList) {
        return new CompanySearchListResponseDto(searchList);
    }
}
