package sopt.comfit.report.service;

import lombok.extern.slf4j.Slf4j;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.experience.domain.Experience;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AIReportPromptBuilder {

    public static String build(Company company, Experience experience, List<CompanyIssue> issues) {
        log.info("프롬프트 빌드 시작 companyId: {}, experienceId: {}", company.getId(), experience.getId());
        return """
            당신은 취업 컨설팅 전문가입니다.
            사용자의 경험과 지원 기업 정보를 분석하여 자기소개서 작성 가이드를 제공합니다.
            
            모든 판단은 [판단 기준]을 참조하여 도출하고, 새로운 기준을 생성하거나 추측하지 마세요.
            
            ---
            
            [판단 기준]
            
            1. JD 기준
            - JD에서 2회 이상 반복되거나 '필수 / 주요 / 우대'로 명시된 요구
            
            2. 직무 행동 기준
            - 역량 키워드가 아닌 '행동 + 판단 방식'으로 변환된 기준
            
            3. 기업 맥락 기준
            - 인재상에서 요구하는 판단 태도
            
            4. 경험 구조 기준
            - 선택된 경험(STAR) 중 위 기준과 연결되지만 충분히 드러나지 않은 단계
            
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
            - 최근 6개월 주요이슈: %s
            
            ---
            
            [직무 설명 JD 원문]
            %s
           
            
            ---
            
            [TASK]
            
           
            1. perspectives: 이 기업이 해당 직무에서 중요하게 보는 관점 3개
            2. density: 선택한 경험과 각 관점의 연결 강도
            3. appealPoint: 반드시 드러내야 할 요소 (최대 2개)
            4. suggestion: 표현 조정 및 주의 포인트
            5. guidance: 자소서 활용 구조 가이드
            
            아래 단계를 순서대로 수행하라.
            모든 판단은 [판단 기준] 섹션을 참조하여 도출하라.
            새로운 기준을 생성하거나 추측하지 마라.
            
            [STEP 1] 직무·기업 관점 도출 - perspectives
            이 기업이 해당 직무에서 중요하게 보는 관점을
            반드시 3개만 도출하라. (2개 이하, 4개 이상 금지)
            
            - 각 관점은 ‘행동 + 판단 방식’으로 표현하라.
            - 각 관점마다 도출 근거를 명시하라.
            - 최근 이슈 정보가 없을 경우, JD + 인재상 기준만 사용하라.
            
            [STEP 2] 경험 연결 강도 판단 - density
            선택된 경험이 각 관점과 어떻게 연결되는지 판단하라.
            
            - 연결 강도는 반드시 아래 중 하나로만 표시한다.
            → 직접 연결 / 간접 연결
            
            - 간접 연결일 경우, 왜 직접 연결이 어려운지 기준을 명시하라.
            
            [STEP 3] 반드시 드러내야 할 요소 도출 - appealPoint
            아래 조건을 모두 만족하는 요소만 도출하라.
            
            - [판단 기준]의 JD 기준 + 기업 맥락 기준과 연결되고
            - 선택된 경험(STAR)에서 충분히 드러나지 않은 요소
            
            각 요소에 대해 반드시 포함하라.
            - 왜 중요한가 (참조 기준 명시)
            - 경험의 어느 단계(STAR)에서 끌어와야 하는가
            - 어떻게 재구성하면 되는가
            - 자소서 구조상 배치 위치
            
            [STEP 4] 표현 조정 및 주의 포인트 - suggestion
            기업 맥락상 오해 소지가 있거나
            과도할 수 있는 표현만 제시하라.
            
            - 반드시 ‘왜 조정이 필요한지’를 기준과 함께 설명하라.
            
            [STEP 5] 자소서 구조 가이드 - guidance
            문장 작성은 금지한다.
            문단 흐름과 메시지 구조만 제시하라.
            
            ---
            
            [출력 형식 - 반드시 이 JSON 구조로만 응답]
            
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
                  "starPhase": "S / T / A / R 중 해당 단계",
                  "direction": "보완 방향",
                  "placement": "자소서 배치 위치"
                }
              ],
              "suggestion": "표현 조정이 필요한 부분과 이유, 추천 조정 방향",
              "guidance": "도입부 메시지 → 본문 전개 흐름 → 마무리 연결 관점"
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
                safeString(company.getJobDescription())
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

