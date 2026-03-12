package sopt.comfit.report.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonUtils")
class JsonUtilsTest {

    private JsonUtils jsonUtils;

    @BeforeEach
    void setUp() {
        jsonUtils = new JsonUtils(new ObjectMapper());
    }

    // ──────────────────────────────────────────────
    // clean()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("clean()")
    class CleanTest {

        @Test
        @DisplayName("null 입력 시 null 반환")
        void returnsNullForNullInput() {
            assertThat(jsonUtils.clean(null)).isNull();
        }

        @Test
        @DisplayName("순수 JSON 문자열은 그대로 반환")
        void returnsPureJsonUnchanged() {
            String input = "{\"key\": \"value\"}";
            assertThat(jsonUtils.clean(input)).isEqualTo(input);
        }

        @Test
        @DisplayName("```json 코드블록 마크다운을 제거한다")
        void removesJsonCodeBlock() {
            // AI 응답에 ```json ... ``` 형식이 포함될 수 있음
            String input = "```json\n{\"key\": \"value\"}\n```";
            String result = jsonUtils.clean(input);
            assertThat(result).isEqualTo("{\"key\": \"value\"}");
        }

        @Test
        @DisplayName("``` 코드블록 마크다운을 제거한다")
        void removesGenericCodeBlock() {
            String input = "```\n{\"key\": \"value\"}\n```";
            String result = jsonUtils.clean(input);
            assertThat(result).isEqualTo("{\"key\": \"value\"}");
        }

        @Test
        @DisplayName("JSON 앞뒤 불필요한 텍스트를 제거하고 {} 영역만 추출한다")
        void extractsJsonRegionFromSurroundingText() {
            // AI가 JSON 앞뒤에 설명 텍스트를 덧붙이는 경우
            String input = "다음은 결과입니다: {\"key\": \"value\"} 이상입니다.";
            String result = jsonUtils.clean(input);
            assertThat(result).isEqualTo("{\"key\": \"value\"}");
        }

        @Test
        @DisplayName("trailing comma를 제거한다 - 객체")
        void removesTrailingCommaInObject() {
            // JSON 표준을 위반하는 trailing comma 제거
            String input = "{\"key\": \"value\",}";
            String result = jsonUtils.clean(input);
            assertThat(result).isEqualTo("{\"key\": \"value\"}");
        }

        @Test
        @DisplayName("trailing comma를 제거한다 - 배열")
        void removesTrailingCommaInArray() {
            String input = "{\"arr\": [1, 2, 3,]}";
            String result = jsonUtils.clean(input);
            assertThat(result).isEqualTo("{\"arr\": [1, 2, 3]}");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거한다")
        void trimsSurroundingWhitespace() {
            String input = "   {\"key\": \"value\"}   ";
            assertThat(jsonUtils.clean(input)).isEqualTo("{\"key\": \"value\"}");
        }
    }

    // ──────────────────────────────────────────────
    // merge()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("merge()")
    class MergeTest {

        // 병렬 AI 호출 결과 5개를 하나의 JSON으로 합치는 로직 검증
        private final String perspectives = "{\"perspectives\": [{\"p\": 1}]}";
        private final String density      = "{\"density\": {\"d\": 2}}";
        private final String appealPoint  = "{\"appealPoint\": [{\"a\": 3}]}";
        private final String suggestion   = "{\"suggestion\": \"test\"}";
        private final String guidance     = "{\"guidance\": \"guide\"}";

        @Test
        @DisplayName("5개 JSON을 하나의 객체로 병합한다")
        void mergesFiveJsonPartsIntoOne() throws Exception {
            String merged = jsonUtils.merge(perspectives, density, appealPoint, suggestion, guidance);

            ObjectMapper mapper = new ObjectMapper();
            var node = mapper.readTree(merged);

            assertThat(node.has("perspectives")).isTrue();
            assertThat(node.has("density")).isTrue();
            assertThat(node.has("appealPoint")).isTrue();
            assertThat(node.has("suggestion")).isTrue();
            assertThat(node.has("guidance")).isTrue();
        }

        @Test
        @DisplayName("각 필드 값이 원본과 일치한다")
        void preservesOriginalValues() throws Exception {
            String merged = jsonUtils.merge(perspectives, density, appealPoint, suggestion, guidance);
            ObjectMapper mapper = new ObjectMapper();
            var node = mapper.readTree(merged);

            assertThat(node.get("suggestion").asText()).isEqualTo("test");
            assertThat(node.get("guidance").asText()).isEqualTo("guide");
        }

        @Test
        @DisplayName("유효하지 않은 JSON이 포함되면 AI_RESPONSE_PARSE_FAILED 예외를 던진다")
        void throwsParseFailedOnInvalidJson() {
            // 파싱 불가 JSON이 섞이면 즉시 예외 → AI 재시도 로직으로 전파됨
            assertThatThrownBy(() ->
                    jsonUtils.merge("NOT_JSON", density, appealPoint, suggestion, guidance))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));
        }
    }
}
