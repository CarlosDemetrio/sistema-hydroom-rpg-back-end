# Research: Backend Refactor Best Practices

**Date**: 2026-02-06
**Scope**: Backend controllers, services, DTOs, validation messages, exception taxonomy, Optional handling, tests
**Primary Sources**: `docs/AI_GUIDELINES_BACKEND.md`, `docs/backend/*`

## Codebase Scan Summary

### Controllers in Scope

- `controller/configuracao/*` includes: `AtributoController`, `AptidaoController`, `BonusController`, `ClasseController`, `DadoProspeccaoController`, `GeneroController`, `IndoleController`, `MembroCorpoController`, `NivelController`, `PresencaController`, `RacaController`, `TipoAptidaoController`, `VantagemController`.
- Core controller example: `JogoController` already uses DTOs and mapper.

### Services in Scope

- `ConfiguracaoService` centralizes all configuration CRUD and uses `IllegalArgumentException` for not found cases.
- Other services exist (ex: `JogoService`), but configuration is the largest refactor surface.

### DTOs & Mappers

- DTOs exist for `Jogo` and defaults under `dto/defaults`, but configuration controllers currently accept/return entities directly.
- Mappers exist for `Jogo` and `Usuario`, but configuration controllers do not use mappers.

### Validation Messages

- `ValidationMessages` exists and is used in some entities.
- Hardcoded validation messages are present (e.g., `GeneroConfig` uses string literals in `@NotNull/@NotBlank/@Size`).
- `GlobalExceptionHandler` includes at least one hardcoded message string.

### Exceptions & Optional Handling

- `ConfiguracaoService` uses `orElseThrow(() -> new IllegalArgumentException(...))` across `findById` calls.
- Custom exceptions exist (`ResourceNotFoundException`, `ConflictException`, `ValidationException`, etc.) but are not consistently applied.

### Tests

- Current tests focus on `Jogo` service/repository; no integration tests for configuration endpoints.
- No automated coverage for configuration error handling or DTO validation.

## Guideline-Driven Audit (Order of Review)

### 01 - Architecture & Layering
- Controllers should coordinate HTTP + DTO validation; services hold business rules; repositories only data access.
- Current state: configuration controllers expose entities and call a monolithic `ConfiguracaoService` directly.

### 02 - Entities & DTOs
- Entities must not be exposed via API; DTOs should be request/response with validation and `ValidationMessages`.
- Current state: configuration endpoints use entity request/response; `GeneroConfig` has hardcoded validation messages.
- DTOs exist for `Jogo`, but need alignment with guideline patterns (Request/Response/Update naming).

### 03 - Exceptions & Error Handling
- Use custom exceptions (ResourceNotFound, Conflict, BusinessRule, Validation) and map to Problem Details.
- Current state: `ConfiguracaoService` uses `IllegalArgumentException` for not found; hardcoded message in `GlobalExceptionHandler`.

### 04 - Repositories & Optional Usage
- Use `Optional` consistently and `orElseThrow` with domain exceptions; avoid null returns.
- Current state: repositories return `Optional`, but service layer wraps with `IllegalArgumentException`.

### 05 - Services
- Services must be transactional for writes and contain all business logic.
- Current state: write methods are transactional, but business validation for configs is minimal/inconsistent.

### 06 - Mappers
- Mappers must live in controller layer; services should not map DTOs.
- Current state: configuration controllers lack mappers; `Jogo` uses `JogoMapper` correctly.

### 07 - Controllers & Swagger
- Controllers should return DTOs, use correct status codes, and Swagger metadata.
- Current state: configuration controllers return entities; `SecurityRequirement` uses `bearer-jwt` while auth is session-based.

### 08 - Security
- Use `@PreAuthorize` for role checks and session-based OAuth2 (no JWT).
- Current state: configuration endpoints mostly use `@PreAuthorize`; one `JogoController` line is commented.

### 09 - Testing (Integration Priority)
- 80% integration tests; validate success + error cases and persistence.
- Current state: tests focused on `Jogo` only; no config controller integration tests.

### 10 - Database
- Enforce naming conventions, JPA mapping patterns, and audit fields.
- Current state: entities use soft delete patterns, but DTO refactor must preserve database constraints.

### 11 - OWASP Security
- Confirm no exposure of sensitive data in DTOs and error responses.
- Current state: entity exposure in configuration controllers risks overexposing fields.

## Spring Boot 4 / Java 25 Requirements

- DTOs should be Java records where applicable.
- Error responses should use `ProblemDetail` (RFC 9457) and Spring Boot 4 configuration (`spring.mvc.problemdetails.enabled=true`).
- Virtual threads should be enabled in configuration (`spring.threads.virtual.enabled=true`).
- Ensure Spring Boot 4 best practices are reflected in controllers and exception handling.

## Gap Summary
- DTOs missing for all configuration controllers; entities exposed in API.
- Validation messages not centralized in `ValidationMessages`.
- Exceptions not aligned with domain-specific taxonomy.
- Optional handling inconsistent at service boundary.
- Integration tests missing for configuration endpoints.

## Refactor Priorities

1. Replace entity exposure in configuration controllers with DTOs + mappers.
2. Standardize exceptions across services (ResourceNotFound, Conflict, BusinessRule, Validation).
3. Replace hardcoded validation strings with `ValidationMessages`.
4. Expand integration tests for configuration endpoints; update existing tests impacted by DTO/exceptions.

## Open Questions

- Should OpenAPI security requirement names be updated from `bearer-jwt` to a session-based naming convention?
- Are any configuration endpoints intended to remain entity-based for internal use only?

## Hosting & Operations Constraints (AWS Free Tier)

- Deployment target: EC2 for backend, RDS for PostgreSQL; GitHub Actions for CI/CD.
- Constraints: minimize memory and CPU usage; low traffic/low user count; cost-focused.
- Implications: prefer lightweight runtime, conservative thread usage, optimized JPA queries, and strict rate limiting.
- Required security posture: OWASP alignment, hardened error responses, and safe defaults.

## Performance & Resource Tuning (Java 25 / Spring Boot 4)

- Favor virtual threads for request handling to reduce footprint under low load.
- Keep connection pools minimal (Hikari) and tuned for RDS free tier limits.
- Prefer pagination and avoid N+1 queries; use `@EntityGraph` when needed.
- Enable gzip/compression and cache headers only where safe.
