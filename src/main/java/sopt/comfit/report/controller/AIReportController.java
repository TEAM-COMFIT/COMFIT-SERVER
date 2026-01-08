package sopt.comfit.report.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.request.MatchExperienceRequestDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;
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

    @GetMapping
    @SecurityRequirement(name = "JWT")
    public PageDto<GetReportSummaryResponseDto> getReportList(@LoginUser Long userId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(required = false) String keyword){
        Pageable pageable = PageRequest.of(page, 4);
        return aiReportService.getReportList(userId, pageable, keyword);
    }

    @GetMapping("/{reportId}")
    @SecurityRequirement(name = "JWT")
    public AIReportResponseDto getReport(@LoginUser Long userId,
                                         @PathVariable Long reportId){
        return aiReportService.getReport(userId, reportId);
    }
}
