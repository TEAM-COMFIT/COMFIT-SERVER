package sopt.comfit.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
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
public class AIReportController implements AIReportSwagger {

    private final AIReportService aiReportService;

    @Override
    public AIReportResponseDto matchExperience(@LoginUser Long userId,
                                               @Valid @RequestBody MatchExperienceRequestDto requestDto){
        return aiReportService.matchExperience(MatchExperienceCommandDto.of(userId, requestDto));
    }

    @Override
    public PageDto<GetReportSummaryResponseDto> getReportList(@LoginUser Long userId,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(required = false) String keyword){
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), 4);
        return aiReportService.getReportList(userId, pageable, keyword);
    }

    @Override
    public AIReportResponseDto getReport(@LoginUser Long userId,
                                         @PathVariable Long reportId){
        return aiReportService.getReport(userId, reportId);
    }

    @Override
    public GetReportExperienceResponseDto getReportExperience(@LoginUser Long userId){

        return aiReportService.getReportExperience(userId);
    }

    @Override
    public GetReportCompanyResponseDto getReportCompany(@PathVariable Long companyId){
        return aiReportService.getReportCompany(companyId);
    }
}
