package sopt.comfit.experience.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.experience.dto.command.CreateExperienceCommandDto;
import sopt.comfit.experience.dto.request.CreateExperienceRequestDto;
import sopt.comfit.experience.service.ExperienceService;
import sopt.comfit.global.annotation.LoginUser;

@RestController
@RequestMapping("/api/v1/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    public Long createExperience(@LoginUser Long userId,
                                  @Valid @RequestBody CreateExperienceRequestDto request){

        return experienceService.createExperience(CreateExperienceCommandDto.of(userId, request));
    }
}
