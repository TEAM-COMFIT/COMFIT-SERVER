package sopt.comfit.report.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReport;
import sopt.comfit.report.exception.AIReportErrorCode;

import java.util.List;

public record AIReportResponseDto(
        String companyName,

        String experienceTitle,

        List<Perspective> perspectives,

        List<Density> density,

        List<AppealPoint> appealPoint,

        String suggestion,

        String guidance
) {
    public record Perspective(String perspective, String source, String reason) {}
    public record Density(String perspective, String connection, String reason) {}
    public record AppealPoint(String element, String importance, String starPhase, String direction, String placement) {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AIReportResponseDto from(AIReport report) {
            return new AIReportResponseDto(
                    report.getCompany().getName(),
                    report.getExperience().getTitle(),
                    fromJson(report.getPerspectives(), new TypeReference<List<Perspective>>() {}),
                    fromJson(report.getDensity(), new TypeReference<List<Density>>() {}),
                    fromJson(report.getAppealPoint(), new TypeReference<List<AppealPoint>>() {}),
                    report.getSuggestion(),
                    report.getGuidance()
            );
        }

        private static <T> T fromJson(String json, TypeReference<T> typeRef) {
            try {
                return objectMapper.readValue(json, typeRef);
            } catch (JsonProcessingException e) {
                throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
            }
        }
}

