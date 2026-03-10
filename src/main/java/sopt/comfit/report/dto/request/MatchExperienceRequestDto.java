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
        @Schema(example = """
                업무 내용
                 고객센터의 각 채널 (Call Chat Mail App) 로 유입되는 고객 문의 운영 , 관리
                 VOC , Inquiry 분석 및 개선
                 신규사업, 마케팅, 이벤트 관련 고객 서비스 운영 지원
                자격 요건
                 3년 이상의 유관업무 경력이 있는 분 또는 프로세스 수립/개선 업무 경험이 있는분
                 유연한 사고와 원활한 커뮤니케이션 능력이 있는 분
                 주말 스케줄 근무 가능하신 분 - 1~2개월마다 1회 주말 근무 (주말근무시 : 11:00~20:00(휴게포함)
                우대사항
                 온라인 커머스 또는 배달서비스 비즈니스에 대한 이해도가 있으신 분
                 고객 중심의 서비스 마인드 보유하신 분
                 변화에 빠르게 적응 가능하신 분
                 일본어 가능하신 분
                """)
        String jobDescription
) {
}
