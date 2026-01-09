package sopt.comfit.experience.dto.response;

import sopt.comfit.experience.domain.Experience;

import java.time.LocalDate;
import java.util.List;

public record GetReportExperienceResponseDto(

        int totalElements,

        List<item> content
) {

    public record item(
            Long id,

            String title,

            LocalDate updatedAt
    ){
        public static item from (Experience experience){
            return new item(experience.getId(), experience.getTitle(), experience.getUpdatedAt().toLocalDate());
        }
    }

    public static GetReportExperienceResponseDto of(List<item> items){
        return new GetReportExperienceResponseDto(items.size(), items);
    }
}
