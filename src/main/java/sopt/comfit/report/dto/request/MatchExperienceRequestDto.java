package sopt.comfit.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MatchExperienceRequestDto(
        @NotNull
        @Schema(example = "1")
        Long companyId,

        @NotNull
        @Schema(example = "1")
        Long experienceId,

        @NotBlank
        @Schema(example = "[직무 설명 (JD 원문)]\n" +
                "\n" +
                "CJ ENM 엔터테인먼트부문은\n" +
                "콘텐츠 기획 및 운영 전반을 담당할 인재를 모집합니다.\n" +
                "\n" +
                "주요 업무\n" +
                "- 콘텐츠 기획 및 운영 업무 지원\n" +
                "- 디지털 콘텐츠 성과 분석 및 인사이트 도출\n" +
                "- 유관 부서 및 외부 파트너와의 협업\n" +
                "\n" +
                "자격 요건\n" +
                "- 콘텐츠 및 엔터테인먼트 산업에 대한 관심\n" +
                "- 데이터 기반으로 문제를 분석하고 개선안을 도출한 경험\n" +
                "- 원활한 커뮤니케이션 및 협업 능력 \n" +
                "\n" +
                "우대 사항\n" +
                "- 디지털 콘텐츠 또는 마케팅 관련 프로젝트 경험\n" +
                "- 글로벌 콘텐츠 트렌드에 대한 이해")
        String jobDescription
) {
}
