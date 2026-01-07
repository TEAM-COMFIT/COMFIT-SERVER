package sopt.comfit.experience.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public class ExperienceController {

    private final ExperienceService experienceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    public Long createExperience(@LoginUser Long userId,
                                  @Valid @RequestBody ExperienceRequestDto request){

        return experienceService.createExperience(CreateExperienceCommandDto.of(userId, request));
    }

    @GetMapping
    @SecurityRequirement(name = "JWT")
    public PageDto<GetSummaryExperienceResponseDto> getSummaryExperienceList(@LoginUser Long userId,
                                                                             @RequestParam(required = false) EType type){
        Pageable pageable = PageRequest.of(0, 6);
        return experienceService.getSummaryExperienceList(userId, type, pageable);
    }

    @GetMapping("/{experienceId}")
    @SecurityRequirement(name="JWT")
    public GetExperienceResponseDto getExperience( @LoginUser Long userId,
                                                   @PathVariable Long experienceId){
        return experienceService.getExperience(userId, experienceId);
    }

    @PatchMapping("/{experienceId}")
    @SecurityRequirement(name = "JWT")
    public Long updateExperience(@LoginUser Long userId,
                                 @PathVariable Long experienceId,
                                 @Valid @RequestBody ExperienceRequestDto request){
        return experienceService.updateExperience(UpdateExperienceCommandDto.of(userId, experienceId, request));
    }
}
