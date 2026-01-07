package sopt.comfit.experience.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sopt.comfit.experience.domain.EType;

import java.time.LocalDate;

public record ExperienceRequestDto(
        @NotBlank
        @Size(min = 2, max = 40, message = "title은 2자 이상 20자 이하여야 합니다")
        @Schema(example = "인스타그램 마케팅 캠페인 기획 및 실행", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotNull
        @Schema(example = "INTERNSHIP", requiredMode = Schema.RequiredMode.REQUIRED, implementation = EType.class)
        EType type,

        @NotNull
        @Schema(example = "2025-12-23", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startAt,

        @NotNull
        @Schema(example = "2025-12-28", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endAt,

        @NotBlank
        @Size(min = 30, max = 200, message = "situation은 30자 이상 200자 이하여야 합니다")
        @Schema(example = "대학생 마케팅 동아리에서 신규 브랜드 인지도를 높이기 위한 프로젝트를 진행함")
        String situation,

        @NotBlank
        @Size(min = 30, max = 200, message = "task는 30자 이상 200자 이하여야 합니다")
        @Schema(example = "한정된 예산 안에서 브랜드 메시지를 효과적으로 전달한 콘텐츠 방향을 설정해야 했음")
        String task,

        @NotBlank
        @Size(min = 40, max = 500, message = "action은 40자 이상 500자 이하여야 합니다")
        @Schema(example = "초기에는 트렌디한 이미지 위주의 콘텐츠를 기획했으나, 게시 후 반응을 분석한 결과 조회 수 대비 브랜드 인지 반응이 낮다고 판단함." +
                "이에 메시지 전달이 명확한 짧은 영상 포맷으로 방향을 조정함")
        String action,

        @NotBlank
        @Size(min = 30, max = 300, message = "result은 30자 이상 300자 이하여야 합니다")
        @Schema(example = "캠페인 종료 시 브랜드 계정 팔로워 수가 약 25% 증가했고, 댓글에서 브랜드 언급 비율이 눈에 띄게 높아짐" +
                "이 결과를 통해 콘텐츠 성과를 단순 수치가 아니라 메시지 전달관점에서 해석하는 중요성을 배움.")
        String result,

        @Schema(example = "true")
        boolean isDefault
){
}
