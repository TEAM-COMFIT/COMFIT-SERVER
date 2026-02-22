package sopt.comfit.report.infra.prompt;

import lombok.extern.slf4j.Slf4j;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.report.infra.dto.PreparedDataDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AIReportParallelPromptBuilder {

    // ============ 공통 컨텍스트 ============

    private static String buildContext(PreparedDataDto data) {
        return """
                [판단 기준]
                
                1. JD 기준
                - JD에서 반복되거나 '필수 / 주요 / 우대'로 명시된 요구 사항
                
                2. 직무 행동 기준
                - 단순 역량 키워드가 아닌 '행동 방식 + 판단 기준'
                
                3. 기업 맥락 기준
                - 인재상 및 기업이 선호하는 판단 태도
                
                4. 경험 구조 기준
                - 선택된 경험(STAR) 중 충분히 드러나지 않은 단계
                
                ---
                
                [사용자 경험]
                
                - Situation: %s
                - Task: %s
                - Action: %s
                - Result: %s
                
                ---
                
                [기업 정보]
                
                - 기업명: %s
                - 산업 분야: %s
                - 회사 요약: %s
                - 인재상: %s
                - 최근 6개월 주요 이슈: %s
                
                ---
                
                [직무 설명 JD 원문]
                
                %s
                
                [중요]\s
                - 백틱(```)을 사용하지 마라
                - JSON 외의 어떤 텍스트도 출력하지 마라
                - 첫 문자는 반드시 { 이어야 한다
                """.formatted(
                data.experience().getSituation(),
                data.experience().getTask(),
                data.experience().getAction(),
                data.experience().getResult(),
                data.company().getName(),
                data.company().getIndustry().getDescription(),
                data.company().getSummary(),
                data.company().getTalentProfile(),
                formatIssues(data.issues()),
                safeString(data.jobDescription())
        );
    }

    public static String buildPerspective(PreparedDataDto data) {
        log.info("Perspective 프롬프트 빌드 - companyId: {}, experienceId: {}", data.company().getId(), data.experience().getId());

        String context = buildContext(data);

        return """
                당신은 취업 컨설팅 전문가입니다.
                사용자의 경험과 지원 기업 정보를 분석하여 직무·기업 관점을 도출합니다.
                
                ---
                %s
                ---
                
                [TASK] 직무·기업 관점 도출
                
                이 기업이 해당 직무에서 중요하게 볼 가능성이 높은 관점을 **반드시 3개만** 도출하라.
                - 각 관점은 '행동 방식 + 판단 기준' 형태로 작성하라.
                - 각 관점마다 도출 근거(JD 또는 인재상)를 명확히 밝혀라.
                - 각 reason은 최소 2문장 이상 작성하고 -, : 등의 특수문자를 사용하지말고 문장형태로 끝나도록 해라.
                - 가장 경험과 적합한 관점을 도출하되 JD, 인재상, 기업 이슈 리스트 등 다양하게 조합해서 도출하라.
                - 문장이 두개 이상인 경우 줄바꿈(\\n)으로 구분하여 작성
                ---
                
                [출력 형식 - 반드시 JSON만 출력]
                
                {
                  "perspectives": [
                    {
                      "perspective": "관점 내용",
                      "source": "JD / 인재상 / 최근 이슈 중 도출 근거",
                      "reason": "왜 이 관점이 중요한지"
                    }
                  ]
                }
                """.formatted(context);
    }

    public static String buildDensity(PreparedDataDto data,
                                      String perspectivesJson) {
        log.info("Density 프롬프트 빌드 - companyId: {}, experienceId: {}", data.company().getId(), data.experience().getId());

        String context = buildContext(data);

        return """
                당신은 취업 컨설팅 전문가입니다.
                이전에 도출된 관점을 바탕으로 경험과의 연결 강도를 판단합니다.
                
                ---
                %s
                ---
                
                [이전 단계에서 도출된 관점]
                
                %s
                
                ---
                
                [TASK] 경험 연결 강도 판단
                
                각 관점과 경험의 연결 강도를 판단하라.
                - connection 값은 반드시 "직접 연결" 또는 "간접 연결" 중 하나만 사용한다.
                - 간접 연결일 경우, 왜 직접 연결이 어려운지 기준을 들어 설명하라.
                - reason은 최소 2문장 이상 작성하라.
                - 문장이 두개 이상인 경우 줄바꿈(\\n)으로 구분하여 작성
                
                ---
                
                [출력 형식 - 반드시 JSON만 출력]
                
                {
                  "density": [
                    {
                      "perspective": "관점 내용",
                      "connection": "직접 연결 / 간접 연결",
                      "reason": "연결 판단 근거"
                    }
                  ]
                }
                """.formatted(context, perspectivesJson);
    }

    public static String buildAppealPoint(PreparedDataDto data,
                                          String perspectivesJson) {
        log.info("AppealPoint 프롬프트 빌드 - companyId: {}, experienceId: {}", data.company().getId(), data.experience().getId());

        String context = buildContext(data);

        return """
                당신은 취업 컨설팅 전문가입니다.
                이전에 도출된 관점을 바탕으로 반드시 드러내야 할 요소를 분석합니다.
                
                ---
                %s
                ---
                
                [이전 단계에서 도출된 관점]
                
                %s
                
                ---
                
                [TASK] 반드시 드러내야 할 요소 도출
                
                최대 2개만 도출하라. 아래 조건을 모두 만족해야 한다.
                - JD 기준 + 기업 맥락 기준과 연결됨
                - 선택된 경험(STAR)에서 충분히 드러나지 않음
                - 문장이 두개 이상인 경우 줄바꿈(\\n)으로 구분하여 작성
                
                각 요소마다 아래를 모두 포함하라.
                - importance: 왜 중요한지 (기준 명시, 최소 2문장)
                - starPhase: S / T / A / R 중 해당 단계
                - direction: 어떻게 보완하면 좋을지 (최소 2문장)
                - placement: 자소서 내 배치 위치
                
                ---
                
                [출력 형식 - 반드시 JSON만 출력]
                
                {
                  "appealPoint": [
                    {
                      "element": "드러내야 할 요소",
                      "importance": "중요 이유",
                      "starPhase": "S / T / A / R",
                      "direction": "보완 방향",
                      "placement": "자소서 배치 위치"
                    }
                  ]
                }
                """.formatted(context, perspectivesJson);
    }

    public static String buildSuggestion(PreparedDataDto data,
                                         String perspectivesJson) {
        log.info("Suggestion 프롬프트 빌드 - companyId: {}, experienceId: {}", data.company().getId(), data.experience().getId());

        String context = buildContext(data);

        return """
                당신은 취업 컨설팅 전문가입니다.
                이전에 도출된 관점을 바탕으로 표현 조정 및 주의 포인트를 제안합니다.
                
                ---
                %s
                ---
                
                [이전 단계에서 도출된 관점]
                
                %s
                
                ---
                
                [TASK] 표현 조정 및 주의 포인트
                
                - 비판이나 지적이 아닌, 개선을 돕는 조언의 형태로 작성하라.
                - 왜 이런 조정이 도움이 될 수 있는지 기준과 함께 설명하라.
                - 최소 3개의 조언 포인트를 포함하라.
                - 각 포인트는 줄바꿈으로 구분하라.
                
                [톤 & 말투 규칙]
                - 평가하거나 단정하지 말고, 제안하듯 부드러운 어조로 작성하라.
                - "~해야 한다", "~필수적이다" 같은 표현 대신 "~해보면 좋다", "~를 고려해볼 수 있다" 사용
                
                ---
                
                [출력 형식 - 반드시 JSON만 출력]
                
                {
                  "suggestion": "조언 내용을 줄바꿈(\\n)으로 구분하여 작성"
                }
                """.formatted(context, perspectivesJson);
    }

    public static String buildGuidance(PreparedDataDto data,
                                       String perspectivesJson) {
        log.info("Guidance 프롬프트 빌드 - companyId: {}, experienceId: {}", data.company().getId(), data.experience().getId());

        String context = buildContext(data);

        return """
                당신은 취업 컨설팅 전문가입니다.
                이전에 도출된 관점을 바탕으로 자소서 구조 가이드를 제공합니다.
                
                ---
                %s
                ---
                
                [이전 단계에서 도출된 관점]
                
                %s
                
                ---
                
                [TASK] 자소서 구조 가이드
                
                - 완성된 자기소개서 문장은 작성하지 마라.
                - 문단 흐름과 메시지 구조를 설명하는 문장은 허용한다.
                - guidance는 **최소 6단계 이상**으로 구성하라.
                - 각 단계는 줄바꿈으로 구분하라.
                
                [guidance 형식 예시 — 내용은 참고하지 말고 형식만 따를 것]
                
                "도입부에서 기업과 직무에 대한 관심을 자연스럽게 제시\\n경험을 선택하게 된 배경과 문제 인식 과정을 설명\\n..."
                
                ---
                
                [출력 형식 - 반드시 JSON만 출력]
                
                {
                  "guidance": "구조 가이드를 줄바꿈(\\n)으로 구분하여 작성"
                }
                """.formatted(context, perspectivesJson);
    }

    // 유틸 메서드

    private static String formatIssues(List<CompanyIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "최근 이슈 정보 없음";
        }
        return issues.stream()
                .map(issue -> "· %s (%s, %s)".formatted(
                        issue.getTitle(),
                        issue.getIssueURL(),
                        issue.getIssueDate()
                ))
                .collect(Collectors.joining("\n"));
    }

    private static String safeString(String value) {
        return value != null ? value : "정보 없음";
    }
}