package sopt.comfit.report.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.request.MatchExperienceRequestDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.service.AIReportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class AIReportController {

    private final AIReportService aiReportService;

    @PostMapping
    @SecurityRequirement(name = "JWT")
    public AIReportResponseDto matchExperience(@LoginUser Long userId,
                                               @RequestBody MatchExperienceRequestDto requestDto){
        return aiReportService.matchExperience(MatchExperienceCommandDto.of(userId, requestDto));
    }
}
