package sopt.comfit.experience.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.dto.command.CreateExperienceCommandDto;
import sopt.comfit.experience.dto.command.UpdateExperienceCommandDto;
import sopt.comfit.experience.dto.request.ExperienceRequestDto;
import sopt.comfit.experience.dto.response.GetExperienceResponseDto;
import sopt.comfit.experience.dto.response.GetSummaryExperienceResponseDto;
import sopt.comfit.experience.service.ExperienceService;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;

@RestController
@RequestMapping("/api/v1/experiences")
@RequiredArgsConstructor
public class ExperienceController implements ExperienceSwagger {

    private final ExperienceService experienceService;

    @Override
    public Long createExperience(@LoginUser Long userId,
                                  @Valid @RequestBody ExperienceRequestDto request){

        return experienceService.createExperience(CreateExperienceCommandDto.of(userId, request));
    }

    @Override
    public PageDto<GetSummaryExperienceResponseDto> getSummaryExperienceList(@LoginUser Long userId,
                                                                             @RequestParam(required = false) EType type,
                                                                             @RequestParam(defaultValue = "0") int page){
        Pageable pageable = PageRequest.of(page, 6);
        return experienceService.getSummaryExperienceList(userId, type, pageable);
    }

    @Override
    public GetExperienceResponseDto getExperience( @LoginUser Long userId,
                                                   @PathVariable Long experienceId){
        return experienceService.getExperience(userId, experienceId);
    }

    @Override
    public Long updateExperience(@LoginUser Long userId,
                                 @PathVariable Long experienceId,
                                 @Valid @RequestBody ExperienceRequestDto request){
        return experienceService.updateExperience(UpdateExperienceCommandDto.of(userId, experienceId, request));
    }

    @Override
    public void deleteExperience(@LoginUser Long userId,
                                 @PathVariable Long experienceId){
        experienceService.deleteExperience(userId, experienceId);
    }
}
