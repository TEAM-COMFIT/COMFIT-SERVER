package sopt.comfit.report.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public String clean(String content) {
        if (content == null) return null;

        String cleaned = content.trim();

        // 코드블럭 제거
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        // JSON 영역만 추출
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start != -1 && end != -1 && start < end) {
            cleaned = cleaned.substring(start, end + 1);
        }

        // trailing comma 제거
        cleaned = cleaned.replaceAll(",\\s*}", "}");
        cleaned = cleaned.replaceAll(",\\s*]", "]");

        return cleaned.trim();
    }

    public String merge(String perspectives, String density, String appealPoint,
                        String suggestion, String guidance) {
        try {
            JsonNode perspectivesNode = objectMapper.readTree(clean(perspectives));
            JsonNode densityNode = objectMapper.readTree(clean(density));
            JsonNode appealPointNode = objectMapper.readTree(clean(appealPoint));
            JsonNode suggestionNode = objectMapper.readTree(clean(suggestion));
            JsonNode guidanceNode = objectMapper.readTree(clean(guidance));

            ObjectNode merged = objectMapper.createObjectNode();
            merged.set("perspectives", perspectivesNode.get("perspectives"));
            merged.set("density", densityNode.get("density"));
            merged.set("appealPoint", appealPointNode.get("appealPoint"));
            merged.set("suggestion", suggestionNode.get("suggestion"));
            merged.set("guidance", guidanceNode.get("guidance"));

            return objectMapper.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}
