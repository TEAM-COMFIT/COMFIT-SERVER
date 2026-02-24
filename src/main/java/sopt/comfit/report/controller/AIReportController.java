package sopt.comfit.report.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.request.MatchExperienceRequestDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;
import sopt.comfit.report.service.AIReportFacade;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class AIReportController implements AIReportSwagger {

    private final AIReportFacade aiReportFacade;

    @Override
    public PageDto<GetReportSummaryResponseDto> getReportList(@LoginUser Long userId,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(required = false) String keyword){
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), 4);
        return aiReportFacade.getReportList(userId, pageable, keyword);
    }

    @Override
    public AIReportResponseDto getReport(@LoginUser Long userId,
                                         @PathVariable Long reportId){
        return aiReportFacade.getReport(userId, reportId);
    }

    @Override
    public GetReportExperienceResponseDto getReportExperience(@LoginUser Long userId){

        return aiReportFacade.getReportExperience(userId);
    }

    @Override
    public GetReportCompanyResponseDto getReportCompany(@PathVariable Long companyId){
        return aiReportFacade.getReportCompany(companyId);
    }

    @Override
    public AIReportResponseDto matchExperience(@LoginUser Long userId,
                                               @Valid @RequestBody MatchExperienceRequestDto requestDto){
        return aiReportFacade.matchExperience(MatchExperienceCommandDto.of(userId, requestDto));
    }

    @PostMapping("/match/async")
    @SecurityRequirement(name = "JWT")
    public Mono<AIReportResponseDto> matchAsync(@LoginUser Long userid ,
                                                @RequestBody MatchExperienceRequestDto request) {
        return aiReportFacade.matchExperienceAsync(MatchExperienceCommandDto.of(userid, request));
    }

    @PostMapping("/match/async/webclient")
    @SecurityRequirement(name = "JWT")
    public AIReportResponseDto matchAsyncWebClient(@LoginUser Long userid ,
                                                @RequestBody MatchExperienceRequestDto request) {
        return aiReportFacade.matchExperienceWebclient(MatchExperienceCommandDto.of(userid, request));
    }

    @PostMapping("/match/async/parallel")
    @SecurityRequirement(name = "JWT")
    public Mono<AIReportResponseDto> matchExperienceWebfluxParallel(@LoginUser Long userId,
                                                                    @RequestBody MatchExperienceRequestDto requestDto){
        return aiReportFacade.matchExperienceParallel(MatchExperienceCommandDto.of(userId, requestDto));
    }
}
