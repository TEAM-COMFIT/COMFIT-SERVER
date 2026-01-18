package sopt.comfit.university.dto.response;

import sopt.comfit.university.domain.University;

public record SearchUniversityResponseDto(
        Long id,
        String name
) {
    public static SearchUniversityResponseDto from(University university) {
        return new SearchUniversityResponseDto(
                university.getId(),
                university.getName()
        );
    }
}
