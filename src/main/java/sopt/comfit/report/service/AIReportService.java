package sopt.comfit.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import sopt.comfit.report.infra.OpenAiClient;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportService {

    private final OpenAiClient openAiClient;
    private final CompanyRepository companyRepository;
    private final ExperienceRepository experienceRepository;
    private final CompanyIssueRepository companyIssueRepository;
    private final ObjectMapper objectMapper;
    private final AIReportRepository aIReportRepository;

    @Transactional
    public AIReportResponseDto matchExperience(MatchExperienceCommandDto command) {
        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        Experience experience = experienceRepository.findByIdAndUserId(command.experienceId(), command.userId())
                .orElseThrow(() -> new BaseException(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        List<CompanyIssue> companyIssueList = companyIssueRepository.findByCompanyId(command.companyId());

        log.info("OpenAI API 호출 시작 - companyId: {}, experienceId: {}", company.getId(), experience.getId());
        long startTime = System.currentTimeMillis();

        CreateReportAiResponseDto response = openAiClient
                .createReport(CreateReportAiRequestDto
                        .from(AIReportPromptBuilder
                                .build(company, experience, command.jobDescription(), companyIssueList)));

        long duration = System.currentTimeMillis() - startTime;
        log.info("OpenAI API 호출 완료 - companyId: {}, experienceId: {}, duration: {}ms, responseLength: {}",
                company.getId(), experience.getId(), duration, response.getContent().length());


        AIReport aiReport = parseAndSave(response.getContent(), experience, company, command.jobDescription());

        log.info("AI 분석 완료 - userId: {}, reportId: {}, companyId: {}, experienceId: {}",
                command.userId(), aiReport.getId(), company.getId(), experience.getId());

        return AIReportResponseDto.from(aiReport);
    }

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
        List<Experience> experiences = experienceRepository.findByUserId(userId);

        return GetReportExperienceResponseDto
                .of(experiences.stream().map(GetReportExperienceResponseDto.item::from).toList());
    }

    @Transactional(readOnly = true)
    public GetReportCompanyResponseDto getReportCompany(Long companyId) {

        return GetReportCompanyResponseDto.from(companyRepository.findById(companyId)
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND)));

    }

    private AIReport parseAndSave(String content, Experience experience, Company company, String jobDescription) {
        log.info("응답 파싱 시작 - companyId: {}, experienceId: {}, contentLength: {}",
                company.getId(), experience.getId(), content.length());
        try {
            JsonNode json = objectMapper.readTree(content);

            if (json.get("perspectives") == null || json.get("density") == null ||
                    json.get("appealPoint") == null || json.get("suggestion") == null ||
                    json.get("guidance") == null) {
                log.error("응답 JSON 필수 필드 누락 - companyId: {}, experienceId: {}, json: {}",
                        company.getId(), experience.getId(), content);
                throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
            }

            log.debug("JSON 파싱 성공 - perspectives: {}, density: {}, appealPoint: {}",
                    json.get("perspectives").size(),
                    json.get("density").size(),
                    json.get("appealPoint").size());

            AIReport report = AIReport.create(
                    jobDescription,
                    json.get("perspectives").toString(),
                    json.get("density").toString(),
                    json.get("appealPoint").toString(),
                    json.get("suggestion").asText(),
                    json.get("guidance").asText(),
                    experience,
                    company
            );

            AIReport savedReport = aIReportRepository.save(report);
            log.info("리포트 저장 완료 - reportId: {}, companyId: {}, experienceId: {}",
                    savedReport.getId(), company.getId(), experience.getId());

            return savedReport;

        } catch (JsonProcessingException e) {
            log.error("응답 파싱 실패 - companyId: {}, experienceId: {}, error: {}, content: {}",
                    company.getId(), experience.getId(), e.getMessage(), content, e);
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}

