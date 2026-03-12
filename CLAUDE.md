# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

COMFIT SERVER is a Spring Boot REST API that analyzes company-employee fit using AI. Users submit work experiences and target companies to generate personalized AI reports via OpenAI.

**Stack:** Java 21, Spring Boot 3.4.2, PostgreSQL, Redis, QueryDSL, Resilience4J, OpenAI API, Kakao OAuth

## Commands

```bash
# Build
./gradlew build           # Full build with tests
./gradlew build -x test   # Skip tests

# Run
./gradlew bootRun         # Runs with local profile by default

# Test
./gradlew test            # Run all tests
./gradlew test --tests "sopt.comfit.ClassName.methodName"  # Single test
```

## Architecture

**Package:** `sopt.comfit` with domain-driven modules: `auth`, `company`, `experience`, `report`, `university`, `user`, `global`

Each module follows: `controller → service → domain (entity + repository) + dto + exception`

### Key Flows

**Authentication:** Kakao OAuth → JWT (access 1h / refresh 24h in Redis) → `JwtAuthenticationFilter` → `JwtAuthenticationProvider` → `UserPrincipal`

**AI Report Generation (async):**
1. `AIReportController` → `AIReportCommandService` creates `AIReportJob` entity
2. `JobCreatedEvent` triggers `JobEventListener` → `AIReportJobWorker`
3. Worker calls OpenAI via `RetryableAiCallerService` wrapped with Resilience4J (CircuitBreaker + Retry + RateLimiter + BulkHead + TimeLimiter)
4. Results stored in `AIReport` entity; job status tracked via `EJobStatus`

**Company Search:** QueryDSL dynamic queries with Redis caching; supports multi-condition (duplicate) selection

### Resilience4J Configuration (OpenAI calls)
- CircuitBreaker: 60% failure threshold, 20-call window
- Retry: 3 attempts, 1s wait
- RateLimiter: 8 calls/sec (aligned with OpenAI RPM)
- TimeLimiter: 45s timeout

### Database
- PostgreSQL via JPA/Hibernate; DDL-auto: `update` (local), `create` (prod)
- QueryDSL custom repositories for complex queries
- JSONB columns on `AIReport` for storing AI metrics (perspectives, density, appealPoint)
- Indexes on `Company`: `industry`, `scale`, `created_at`

## Configuration Profiles

- `local`: PostgreSQL `localhost:5432`, Redis `localhost:6379`
- `prod`: AWS RDS, Docker network Redis, deployed via Blue-Green (port 8080/8081)
- `test`: `application-test.yml`

## Deployment

CI/CD via `.github/workflows/deploy.yml` on push to `dev` branch. Blue-Green deployment using `docker-compose-blue.yml` / `docker-compose-green.yml` with Nginx traffic switching and automated health checks.
