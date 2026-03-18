package sopt.comfit.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.company.domain.Company;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReport;
import sopt.comfit.report.domain.AIReportRepository;
import sopt.comfit.report.exception.AIReportErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportCommandService {

    private final ObjectMapper objectMapper;
    private final AIReportRepository aIReportRepository;

    @Transactional
    public AIReport parseAndSave(String content, Experience experience, Company company, String jobDescription) {
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
                    experience.getUser().getId(),
                    experience.getTitle(),
                    experience.getSituation(),
                    experience.getTask(),
                    experience.getAction(),
                    experience.getResult(),
                    jobDescription,
                    json.get("perspectives").toString(),
                    json.get("density").toString(),
                    json.get("appealPoint").toString(),
                    json.get("suggestion").asText(),
                    json.get("guidance").asText(),
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
