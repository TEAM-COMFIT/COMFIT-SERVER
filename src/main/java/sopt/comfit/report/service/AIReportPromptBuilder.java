package sopt.comfit.report.service;

import lombok.extern.slf4j.Slf4j;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.experience.domain.Experience;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AIReportPromptBuilder {

    public static String build(Company company,
                               Experience experience,
                               String jobDescription,
                               List<CompanyIssue> issues) {
        log.info("프롬프트 빌드 시작 companyId: {}, experienceId: {}", company.getId(), experience.getId());
        return """
                당신은 취업 컨설팅 전문가입니다.
                사용자의 경험과 지원 기업 정보를 분석하여
                자기소개서 작성을 돕는 상세한 가이드를 제공합니다.
                모든 판단은 반드시 [판단 기준]에 근거해야 하며,
                새로운 기준을 생성하거나 추측하지 마세요.
                ---
                [판단 기준]
            
                1. JD 기준
                - JD에서 반복되거나 ‘필수 / 주요 / 우대’로 명시된 요구 사항
            
                2. 직무 행동 기준
                - 단순 역량 키워드가 아닌 ‘행동 방식 + 판단 기준’
            
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
            
                ---
            
                [TASK 수행 규칙 — 반드시 지켜라]
            
                1. 모든 설명 필드는 요약형 한 문장으로 작성하는 것을 금지한다.
                2. reason, importance, direction 항목은 **각각 최소 2문장 이상** 작성하라.
                3. guidance는 **최소 6단계 이상**의 흐름으로 구성하라.
                4. 지나치게 간결한 출력은 잘못된 응답으로 간주된다.
            
                [톤 & 말투 규칙]
            
                - 평가하거나 단정하지 말고, 제안하듯 부드러운 어조로 작성하라.
                - “~해야 한다”, “~필수적이다”, “~이 부족하다”와 같은 표현을 사용하지 마라.
                - 아래와 같은 권유형 표현을 적극적으로 사용하라.
                  → “~해보면 좋다”
                  → “~를 고려해볼 수 있다”
                  → “~을 강조하면 도움이 될 수 있다”
                - 독자를 존중하는 컨설턴트의 관점에서 조언하듯 서술하라.
            
                [형식 규칙]
            
                - 모든 필드는 가독성을 위해 **줄바꿈을 포함해야 한다.**
                - 줄바꿈은 반드시 JSON 문자열 내부에서 "\\\\n" 문자로 표현하라.
                - 실제 개행 문자는 사용하지 마라.
                - 한 문단을 한 줄로만 출력하는 것을 금지한다.
            
                ---
            
                [STEP 1] 직무·기업 관점 도출 — perspectives
            
                - 이 기업이 해당 직무에서 중요하게 볼 가능성이 높은 관점을 **반드시 3개만** 도출하라.
                - 각 관점은 ‘행동 방식 + 판단 기준’ 형태로 작성하라.
                - 각 관점마다 도출 근거(JD 또는 인재상)를 명확히 밝혀라.
                - 각 reason은 최소 2문장 이상 작성하라.
            
                ---
            
                [STEP 2] 경험 연결 강도 판단 — density
            
                - 각 관점과 경험의 연결 강도를 판단하라.
                - connection 값은 반드시 아래 중 하나만 사용한다.
                  → 직접 연결 / 간접 연결
                - 간접 연결일 경우, 왜 직접 연결이 어려운지 기준을 들어 설명하라.
                - reason은 최소 2문장 이상 작성하라.
            
                ---
            
                [STEP 3] 반드시 드러내야 할 요소 — appealPoint
            
                - 최대 2개만 도출하라.
                - 아래 조건을 모두 만족해야 한다.
                  - JD 기준 + 기업 맥락 기준과 연결됨
                  - 선택된 경험(STAR)에서 충분히 드러나지 않음
            
                각 요소마다 아래를 모두 포함하라.
                - importance: 왜 중요한지 (기준 명시, 최소 2문장)
                - starPhase: S / T / A / R 중 해당 단계
                - direction: 어떻게 보완하면 좋을지 (최소 2문장)
                - placement: 자소서 내 배치 위치
            
                ---
            
                [STEP 4] 표현 조정 및 주의 포인트 — suggestion
            
                - 비판이나 지적이 아닌, 개선을 돕는 조언의 형태로 작성하라.
                - 왜 이런 조정이 도움이 될 수 있는지 기준과 함께 설명하라.
                - 최소 3개의 조언 포인트를 포함하라.
                - 각 포인트는 줄바꿈으로 구분하라.
            
                ---
            
                [STEP 5] 자소서 구조 가이드 — guidance
            
                - 완성된 자기소개서 문장은 작성하지 마라.
                - 문단 흐름과 메시지 구조를 설명하는 문장은 허용한다.
                - guidance는 **최소 6단계 이상**으로 구성하라.
                - 각 단계는 줄바꿈으로 구분하라.
            
                [guidance 형식 예시 — 내용은 참고하지 말고 형식만 따를 것]
            
                "guidance": "도입부에서 기업과 직무에 대한 관심을 자연스럽게 제시\\n경험을 선택하게 된 배경과 문제 인식 과정을 설명\\n상황 속에서 맡았던 역할과 판단 기준을 드러냄\\n팀 내 협업과 의견 조율 과정을 강조\\n데이터 분석을 통해 방향을 조정한 흐름 제시\\n성과와 배운 점을 정리하며 직무 적합성 연결\\n기업 가치와의 연결로 마무리"
            
                ---

                [출력 형식 — 반드시 이 JSON 구조만 사용]
            
                {
                  "perspectives": [
                    {
                      "perspective": "관점 내용",
                      "source": "JD / 인재상 중 도출 근거",
                      "reason": "왜 이 관점이 중요한지"
                    }
                  ],
                  "density": [
                    {
                      "perspective": "관점 내용",
                      "connection": "직접 연결 / 간접 연결",
                      "reason": "연결 판단 근거"
                    }
                  ],
                  "appealPoint": [
                    {
                      "element": "드러내야 할 요소",
                      "importance": "중요 이유 (기준 명시)",
                      "starPhase": "S / T / A / R",
                      "direction": "보완 방향",
                      "placement": "자소서 배치 위치"
                    }
                  ],
                  "suggestion": "조언 내용을 줄바꿈으로 구분",
                  "guidance": "구조 가이드를 줄바꿈으로 구성"
                }
            """.formatted(experience.getSituation(),
                experience.getTask(),
                experience.getAction(),
                experience.getResult(),
                company.getName(),
                company.getIndustry().getDescription(),
                company.getSummary(),
                company.getTalentProfile(),
                formatIssues(issues),
                safeString(jobDescription)
        );
    }

    private static String formatIssues(List<CompanyIssue> issues) {
        if (issues.isEmpty()) {
            return "최근 이슈 정보 없음";
        }

        return issues.stream()
                .map(issue -> "· %s (%s, %s)".formatted(
                        issue.getTitle(),
                        issue.getSource(),
                        issue.getIssueDate()
                ))
                .collect(Collectors.joining("\n"));
    }

    private static String safeString(String value){
        return value != null ? value : "정보 없음";
    }

}

