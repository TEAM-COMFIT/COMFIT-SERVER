# COMFIT Project - CLAUDE.md

## 프로젝트 개요
AI 기반 의상 추천 서비스 (COMFIT)의 관측성(Observability) 강화 작업.
"감으로 느리다"가 아닌, **정량적 데이터 기반으로 병목을 특정하고 개선**하는 것이 목표.

---

## 현재 인프라 상태

### 기술 스택
- **Spring Boot 3.4.2** + Java (Virtual Thread 사용 중)
- **AWS EC2** 단일 서버 배포
- **PostgreSQL** — 메인 DB
- **Redis** — BRPOP 기반 비동기 Job Queue + 캐싱

### 관측성 스택 (현재)
| 구성 요소 | 상태 | 비고 |
|-----------|------|------|
| Prometheus | ✅ 동작 중 | 메트릭 수집 (HTTP 히스토그램, JVM, 시스템) |
| Grafana | ✅ 동작 중 | 시각화 대시보드 |
| Loki | ✅ 동작 중 | 로그 저장소 |
| Promtail | ❌ 제거됨 | 설정 문제로 제거 → 로그가 Loki로 안 가는 중 |
| 트레이싱 | ❌ 없음 | Span 단위 병목 특정 불가 |

### 기존 관측성의 한계
- traceId를 MDC에 심어 로그에 찍고 있지만, Loki로 안 가므로 조회 불가
- Prometheus 메트릭으로 "p99가 느리다"는 알 수 있지만, **어디서 느린지** 특정 불가
- Grafana 대시보드는 있으나 트레이싱 데이터소스 없음
- **Three Pillars(Metrics/Logs/Traces) 중 Logs와 Traces가 빠져있는 상태**

---

## 이번 작업 목표: Observability Three Pillars 완성

### 최종 아키텍처

```
[Spring Boot App]
    │
    ├── Micrometer Metrics ──→ Prometheus ──→ Grafana (메트릭 대시보드)
    │
    ├── OTLP Traces ──→ Alloy ──→ Tempo ──→ Grafana (트레이스 뷰)
    │
    └── 로그 파일 ──→ Alloy ──→ Loki ──→ Grafana (로그 뷰)
                                                    │
                                          traceId로 3개 연동
                                    (메트릭 ↔ 로그 ↔ 트레이스 원클릭 점프)
```

### 기술 결정 기록

| 결정 | 선택 | 비교군 | 선택 이유 |
|------|------|--------|-----------|
| 로그 에이전트 | **Alloy** | Promtail, Fluent Bit, Vector, Filebeat | Promtail이 이미 빠진 상태라 새로 셋업 필요. Alloy는 로그+트레이스 통합 수집 가능, Grafana 생태계 네이티브. Fluent Bit/Vector는 범용이지만 Tempo 연동에서 추가 설정 필요 |
| 트레이스 저장소 | **Tempo** | Zipkin, Jaeger | Grafana 네이티브 → Loki↔Tempo traceId 원클릭 연동. Zipkin은 독립 UI라 Grafana 통합이 약함. Jaeger는 기능 풍부하지만 현 목적 대비 오버 |
| 트레이스 전송 | **OTLP** | Brave→Zipkin | OpenTelemetry 표준 프로토콜. 벤더 종속 없음, 나중에 백엔드 교체 시 앱 코드 변경 불필요 |
| 트레이스 계측 | **Micrometer Tracing + OpenTelemetry Bridge** | Brave | Spring Boot 3.4.2 공식 지원, OTLP 전송에 OTel 브릿지가 자연스러움 |

### 비교군 내부 동작 이해 (면접 대비)

**Promtail 내부 동작:**
- 로그 파일 tail → Pipeline Stages (정규식 파싱 → 레이블 추출 → 타임스탬프 변환)
- `{labels: [(timestamp, logline)]}` 스트림 단위로 Loki에 HTTP push
- 읽은 오프셋을 `positions.yaml`에 저장 → 재시작 시 중복 수집 방지
- 데이터 모델: Loki 스트림 전용

**Alloy 내부 동작:**
- River 설정 언어 기반, 컴포넌트를 DAG(방향 비순환 그래프)로 연결
- 로그: `local.file_match` → `loki.source.file` → `loki.write`
- 트레이스: `otelcol.receiver.otlp` → `otelcol.exporter.otlphttp` (→ Tempo)
- 단일 에이전트로 로그 + 트레이스 동시 처리
- 데이터 모델: OTLP (로그/메트릭/트레이스 통합)

비교 테이블:

| 도구 | 데이터 모델 | 특징 |
|------|------------|------|
| Promtail | Loki 스트림 ({labels}, entries[]) | Loki 전용, 경량, push |
| Alloy | OTLP (로그+메트릭+트레이스 통합) | 범용, River 설정언어, Grafana 네이티브 |
| Fluent Bit | MessagePack 이벤트 버퍼 | 경량, 멀티 output 지원 |
| Filebeat | ECS JSON | ELK 전용 |
| Vector | Event 타입 (log/metric/trace) | Rust 기반 고성능, 범용 |
---

## Phase 1: 관측성 파이프라인 구축

### Step 1. Tempo 배포 (Docker)
- EC2에 Tempo 컨테이너 추가
- OTLP gRPC (4317) / HTTP (4318) 수신 포트 오픈
- 로컬 스토리지 사용 (단일 EC2이므로)

### Step 2. Alloy 배포 및 설정 (Docker)
- Promtail 대체: 앱 로그 파일 → Loki 전송 파이프라인
- 트레이스 수신: OTLP → Tempo 전송 파이프라인
- 두 파이프라인을 하나의 Alloy config에 구성

### Step 3. Spring Boot 앱 설정 (3.4.2 기준)

**의존성 (build.gradle):**
```groovy
// Micrometer Tracing + OpenTelemetry Bridge
implementation 'io.micrometer:micrometer-tracing-bridge-otel'
// OTLP Exporter (Alloy → Tempo로 전송)
implementation 'io.opentelemetry:opentelemetry-exporter-otlp'
// Actuator (메트릭 + 트레이싱 자동 설정)
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

**application.yml 핵심 설정:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 개발/테스트: 전 요청 트레이싱 (프로덕션은 0.1 등)
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces  # Alloy OTLP HTTP 수신 주소

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

**Virtual Thread + Tracing Context Propagation 주의:**
- Virtual Thread 사용 시 ThreadLocal 기반 Context가 유실될 수 있음
- 이미 MDC + TaskDecorator를 구현해둔 상태
- Tracing context(Span, TraceId)도 같이 전파되는지 반드시 검증
- `ContextSnapshotFactory` 또는 기존 TaskDecorator 확장 필요할 수 있음

### Step 4. 커스텀 Span 추가

**자동 계측으로 잡히는 구간:**
- HTTP 요청/응답 (Controller)
- DB 쿼리 (JPA/JDBC)
- Redis 호출

**수동 Span이 필요한 구간:**
- AI API 호출 (OpenAI 등 외부 HTTP) → 전체 응답시간의 대부분 차지 예상
- Redis BRPOP Job Queue 처리 구간
- 비즈니스 로직 주요 단계

```java
// 방법 1: @Observed 어노테이션
@Observed(name = "ai.api.call", contextualName = "ai-recommendation")
public RecommendationResult callAiApi(RequestDto request) {
    // AI API 호출 로직
}

// 방법 2: ObservationRegistry 직접 사용 (세밀한 제어)
Observation.createNotStarted("ai.api.call", registry)
    .observe(() -> {
        // AI API 호출 로직
    });
```

### Step 5. Grafana 데이터소스 연결
- Tempo 데이터소스 추가 (http://tempo:3200)
- Loki 데이터소스에 **Derived Field** 설정:
    - traceId 정규식 매칭 → Tempo 링크 자동 생성
    - **핵심:** 로그에서 traceId 클릭 → Tempo 트레이스 뷰로 원클릭 점프
- Prometheus 메트릭 → Exemplars 설정으로 트레이스 연결

---

## Phase 2: K6 부하 테스트 + 병목 특정

### Step 6. K6 부하 테스트
- 기존 K6 스크립트 활용
- 테스트 중 Grafana에서 실시간 모니터링

### Step 7. 병목 분석
1. Grafana → Tempo에서 p95/p99 느린 트레이스 선택
2. Span 트리에서 각 구간별 소요시간 확인
3. 병목 구간 비율 계산 (예: "AI API 호출이 전체의 85%")
4. **스크린샷 캡처** → 포트폴리오/자소서 근거

### Step 8. 개선 + 재측정
- 병목 구간에 대한 최적화 적용
- K6 재실행
- Before/After 정량 비교
- 결과 문서화

---

## 제약 조건
- **타임라인**: 당근 자소서 제출 전까지 Phase 1~2 완료
- **인프라**: 단일 EC2 → 메모리 여유 확인 필요 (Tempo, Alloy 추가 시)
- **목적**: 프로덕션 운영이 아닌 **포트폴리오 + 면접 근거 확보**
- **기존 코드 유지**: Virtual Thread, MDC traceId, Redis BRPOP 등 기존 구현 유지

---

## 작업 순서 요약
1. Spring Boot 버전 확인 (3.4.2)
2. Docker Compose에 Tempo + Alloy 추가
3. Alloy config 작성 (로그→Loki, 트레이스→Tempo 파이프라인)
4. Spring Boot 의존성 추가 + application.yml 설정
5. Virtual Thread Context Propagation + Tracing 연동 검증
6. AI API 호출 구간에 커스텀 Span 추가
7. Grafana에 Tempo 데이터소스 + Loki Derived Field 설정
8. K6 부하 테스트 → 트레이스 수집 확인
9. 병목 분석 → 개선 → 재측정 → 문서화