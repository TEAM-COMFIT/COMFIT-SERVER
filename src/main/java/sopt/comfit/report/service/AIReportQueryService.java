package sopt.comfit.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.company.domain.CompanyIssueRepository;
import sopt.comfit.company.domain.CompanyRepository;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.experience.domain.ExperienceRepository;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
import sopt.comfit.experience.exception.ExperienceErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReport;
import sopt.comfit.report.domain.AIReportRepository;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.PreparedDataDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportQueryService {

    private final CompanyRepository companyRepository;
    private final ExperienceRepository experienceRepository;
    private final AIReportRepository aIReportRepository;
    private final CompanyIssueRepository companyIssueRepository;

    @Transactional(readOnly = true)
    public PageDto<GetReportSummaryResponseDto> getReportList(Long userId, Pageable pageable, String keyword) {
        Page<AIReport> reports;

        if (keyword != null && !keyword.trim().isEmpty()) {
            reports = aIReportRepository.findByExperienceUserIdAndKeyword(userId, keyword, pageable);
        } else {
            reports = aIReportRepository.findByExperienceUserId(userId, pageable);
        }

        return PageDto.from(reports.map(GetReportSummaryResponseDto::from));
    }

    @Transactional(readOnly = true)
    public AIReportResponseDto getReport(Long userId, Long reportId) {
        AIReport aiReport = aIReportRepository.findByExperienceUserIdAndId(userId, reportId)
                .orElseThrow(() -> BaseException.type(AIReportErrorCode.AI_REPORT_NOT_FOUND));

        return AIReportResponseDto.from(aiReport);
    }

    @Transactional(readOnly = true)
    public GetReportExperienceResponseDto getReportExperience(Long userId) {
        List<Experience> experiences = experienceRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        return GetReportExperienceResponseDto
                .of(experiences.stream().map(GetReportExperienceResponseDto.item::from).toList());
    }

    @Transactional(readOnly = true)
    public GetReportCompanyResponseDto getReportCompany(Long companyId) {

        return GetReportCompanyResponseDto.from(companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND)));

    }

    @Transactional(readOnly = true)
    public PreparedDataDto prepareData(MatchExperienceCommandDto command) {
        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        Experience experience = experienceRepository.findByIdAndUserId(command.experienceId(), command.userId())
                .orElseThrow(() -> new BaseException(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        List<CompanyIssue> issues = companyIssueRepository.findByCompanyId(command.companyId());

        return PreparedDataDto.of(company, experience, command.jobDescription(), issues);
    }
}

