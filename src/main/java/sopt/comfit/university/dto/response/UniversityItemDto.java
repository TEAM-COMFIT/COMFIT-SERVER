package sopt.comfit.university.dto.response;

import sopt.comfit.university.domain.University;

public record UniversityItemDto(
        Long id,
        String name
) {
    public static UniversityItemDto from(University university) {
        return new UniversityItemDto(
                university.getId(),
                university.getName()
        );
    }
}
