# Implementation Plan: Backend Refactor Best Practices

**Branch**: `003-backend-refactor-best-practices` | **Date**: 2026-02-06 | **Spec**: `specs/003-backend-refactor-best-practices/spec.md`
**Input**: Feature specification from `specs/003-backend-refactor-best-practices/spec.md`

## Summary

Refactor all backend controllers, services, and DTOs to align with the AI guidelines: centralized validation messages, correct exception taxonomy, proper Optional handling, DTOs + validation for configuration endpoints, and expanded integration/unit tests to guard behavior.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.0.2 (Web, Validation, Data JPA, OAuth2 client), Lombok, MapStruct 1.5.5  
**Storage**: PostgreSQL (AWS RDS free tier)  
**Testing**: JUnit 5 + Spring Boot Test (integration priority) + AssertJ  
**Target Platform**: AWS EC2 (free tier)  
**Project Type**: Single backend service  
**Performance Goals**: Minimize memory/CPU usage on free tier  
**Constraints**: Session-based OAuth2 (no JWT), mapper stays in controller, services own business logic, no entity exposure in API responses  
**Scale/Scope**: Low traffic, small user base, cost-focused operations

## Constitution Check

No constitution file found; follow `docs/AI_GUIDELINES_BACKEND.md` and `docs/backend/*`.

## Project Structure

### Documentation (this feature)

```text
specs/003-backend-refactor-best-practices/
├── plan.md
├── research.md
├── tasks.md
└── spec.md
```

### Source Code (repository root)

```text
src/main/java/br/com/hydroom/rpg/fichacontrolador/
├── controller/
│   └── configuracao/
├── service/
├── repository/
├── dto/
│   ├── request/
│   ├── response/
│   └── defaults/
├── mapper/
├── exception/
└── constants/

src/test/java/br/com/hydroom/rpg/fichacontrolador/
├── controller/
├── service/
└── repository/
```

**Structure Decision**: Single backend project with layered architecture; configuration endpoints live under `controller/configuracao` and share `ConfiguracaoService`.

## Implementation Approach

### Phase 0 - Research (completed)
- Inventory controllers/services/DTOs and map gaps against AI guidelines in guideline order.
- Identify hardcoded validation messages and exception misuse.
- Assess test coverage for configuration endpoints.

### Phase 1 - Architecture & DTO Design
- Confirm layering rules per `01-architecture.md` and ensure controllers stay thin.
- Define DTOs and validation rules for all configuration endpoints (`02-entities-dtos.md`).
- Define mapper responsibilities per controller (`06-mappers.md`).

### Phase 2 - Exceptions, Optional, Services
- Replace generic exceptions with domain-specific exceptions (`03-exceptions.md`).
- Standardize Optional handling in services (`04-repositories.md`, `05-services.md`).
- Add service-level validation for configuration rules (business logic).

### Phase 3 - Controllers & Security
- Refactor all controllers to use DTOs/mappers and stop exposing entities (`07-controllers.md`).
- Re-verify security annotations and session-based auth alignment (`08-security.md`).

### Phase 4 - Validation Messages
- Replace hardcoded validation messages with `ValidationMessages` (`02-entities-dtos.md`).
- Normalize exception handler messages to shared constants when applicable.

### Phase 5 - Spring Boot 4 / Java 25 Alignment
- Convert DTOs to Java records where feasible.
- Update exception handling to use `ProblemDetail` (RFC 9457).
- Ensure application properties enable virtual threads and Problem Details.
- Apply low-resource JVM and server tuning suitable for EC2 free tier.

### Phase 6 - Database Alignment
- Review entity mappings and DTO constraints against `10-database.md`.
- Ensure soft delete/audit fields are preserved in response DTOs only when appropriate.
- Tune datasource pool sizes for RDS free tier constraints.

### Phase 7 - OWASP Security Review
- Validate DTOs and error responses do not expose sensitive data (`11-owasp-security.md`).
- Confirm session-based security assumptions are reflected in controllers and docs.
- Confirm rate limiting and security headers remain enabled.

### Phase 8 - Tests
- Add integration tests for each configuration endpoint (success + error paths).
- Add unit tests for complex service logic (as needed).
- Update existing tests for DTO/exceptions changes (`09-testing.md`).

### Phase 9 - CI/CD & Deployment
- Define GitHub Actions workflow for build/test/package.
- Add deployment steps for EC2 (artifact copy + restart) and RDS environment configuration.
- Document minimal-resource runtime configuration.
- Enforce SSM Session Manager access (no bastion, port 22 closed) and SG isolation (RDS only from EC2 SG).
- Document SSM port-forwarding for DB access from local tools.
- Configure CloudWatch Agent with 3-day retention and WARN-level framework logs.
