package sopt.comfit.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopt.comfit.company.domain.Company;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReport;
import sopt.comfit.report.domain.AIReportRepository;
import sopt.comfit.report.exception.AIReportErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIReportCommandService")
class AIReportCommandServiceTest {

    private AIReportCommandService service;

    @Mock
    private AIReportRepository reportRepository;

    @Mock
    private Company company;

    @Mock
    private Experience experience;

    // 검증할 핵심 로직:
    //   1) 필수 5개 필드(perspectives, density, appealPoint, suggestion, guidance) 존재 여부 검사
    //   2) 유효하지 않은 JSON 처리
    //   3) 성공 시 repository.save() 호출 확인

    @BeforeEach
    void setUp() {
        service = new AIReportCommandService(new ObjectMapper(), reportRepository);
        given(company.getId()).willReturn(1L);
        given(experience.getId()).willReturn(2L);
    }

    @Nested
    @DisplayName("parseAndSave() - 성공 케이스")
    class SuccessCase {

        @Test
        @DisplayName("5개 필드가 모두 포함된 JSON이면 리포트를 저장하고 반환한다")
        void savesReportWithAllRequiredFields() {
            // given: AI 응답에 모든 필수 필드 포함
            String validJson = """
                    {
                      "perspectives": [{"p": 1}],
                      "density": {"d": 2},
                      "appealPoint": [{"a": 3}],
                      "suggestion": "개선 포인트",
                      "guidance": "가이드"
                    }
                    """;

            AIReport mockReport = AIReport.create(
                    "job", "[{\"p\":1}]", "{\"d\":2}", "[{\"a\":3}]",
                    "개선 포인트", "가이드", experience, company);
            given(reportRepository.save(any(AIReport.class))).willReturn(mockReport);

            // when
            AIReport result = service.parseAndSave(validJson, experience, company, "job");

            // then
            assertThat(result).isNotNull();
            verify(reportRepository).save(any(AIReport.class));
        }
    }

    @Nested
    @DisplayName("parseAndSave() - 필수 필드 누락")
    class RequiredFieldMissing {

        @Test
        @DisplayName("perspectives 필드 누락 시 AI_RESPONSE_PARSE_FAILED 예외를 던진다")
        void throwsWhenPerspectivesMissing() {
            String json = """
                    {
                      "density": {},
                      "appealPoint": [],
                      "suggestion": "s",
                      "guidance": "g"
                    }
                    """;

            assertThatThrownBy(() -> service.parseAndSave(json, experience, company, "job"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("guidance 필드 누락 시 AI_RESPONSE_PARSE_FAILED 예외를 던진다")
        void throwsWhenGuidanceMissing() {
            String json = """
                    {
                      "perspectives": [],
                      "density": {},
                      "appealPoint": [],
                      "suggestion": "s"
                    }
                    """;

            assertThatThrownBy(() -> service.parseAndSave(json, experience, company, "job"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));
        }
    }

    @Nested
    @DisplayName("parseAndSave() - JSON 파싱 오류")
    class ParseError {

        @Test
        @DisplayName("JSON 형식이 아닌 문자열이면 AI_RESPONSE_PARSE_FAILED 예외를 던진다")
        void throwsOnInvalidJson() {
            // AI가 JSON 대신 일반 텍스트를 반환하는 장애 케이스
            String notJson = "이것은 JSON이 아닙니다";

            assertThatThrownBy(() -> service.parseAndSave(notJson, experience, company, "job"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("빈 문자열이면 AI_RESPONSE_PARSE_FAILED 예외를 던진다")
        void throwsOnEmptyString() {
            assertThatThrownBy(() -> service.parseAndSave("", experience, company, "job"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));
        }
    }
}
