package sopt.comfit.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;

@Service
@RequiredArgsConstructor
public class AIReportFacade {

    private final AIReportQueryService aiReportQueryService;
    private final AIReportCallService aIReportCallService;

    public PageDto<GetReportSummaryResponseDto> getReportList(Long userId, Pageable pageable, String keyword){
        return aiReportQueryService.getReportList(userId, pageable, keyword);
    }

    public AIReportResponseDto getReport(Long userId, Long reportId){
        return aiReportQueryService.getReport(userId, reportId);
    }

    public GetReportExperienceResponseDto getReportExperience(Long userId){
        return aiReportQueryService.getReportExperience(userId);
    }

    public GetReportCompanyResponseDto getReportCompany(Long companyId){
        return aiReportQueryService.getReportCompany(companyId);
    }

    public AIReportResponseDto matchExperience(MatchExperienceCommandDto command) {
        return aIReportCallService.matchExperience(command);
    }

    public Mono<AIReportResponseDto> matchExperienceAsync(MatchExperienceCommandDto command){
        return aIReportCallService.matchExperienceAsync(command);
    }

    public AIReportResponseDto matchExperienceWebclient(MatchExperienceCommandDto command) {
        return aIReportCallService.matchExperienceWebclient(command);
    }

    public Mono<AIReportResponseDto> matchExperienceWebfluxParallel(MatchExperienceCommandDto command){
        return aIReportCallService.matchExperienceWebfluxParallel(command);
    }
}
