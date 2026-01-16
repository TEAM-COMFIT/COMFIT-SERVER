package sopt.comfit.university.dto.response;

import java.util.List;

public record UniversitySearchResponseDto(
        List<UniversityItemDto> universityList
) {
    public static UniversitySearchResponseDto from(List<UniversityItemDto> universityList) {
        return new UniversitySearchResponseDto(universityList);
    }
}
