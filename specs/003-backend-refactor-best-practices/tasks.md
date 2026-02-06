# Tasks: Backend Refactor Best Practices

> Ordered by dependency. Integration tests are prioritized. Follow guideline order.

## Phase 0 - Architecture Inventory

- [ ] T0.1 Audit all controllers/services and list endpoints in scope (configuration + core controllers like `JogoController`).
- [ ] T0.2 Confirm controller/service/repository responsibilities align with `01-architecture.md`.

## Phase 1 - Entities & DTOs

- [ ] T1.1 Define request/response/update DTOs for each configuration entity with `ValidationMessages` annotations.
- [ ] T1.2 Review existing DTOs (e.g., `Jogo*Request/Response`) and align naming/validation to `02-entities-dtos.md`.
- [ ] T1.3 Identify and add missing `ValidationMessages` constants for any new validations.

## Phase 2 - Exceptions

- [ ] T2.1 Replace `IllegalArgumentException` with domain exceptions in all services.
- [ ] T2.2 Ensure `GlobalExceptionHandler` maps exceptions to correct HTTP status and message sources.

## Phase 3 - Repositories & Optional Usage

- [ ] T3.1 Standardize `Optional` usage patterns (`findById` + `orElseThrow` with domain exceptions).
- [ ] T3.2 Review repository methods for null returns or inconsistent query behaviors.

## Phase 4 - Services

- [ ] T4.1 Add service-level validations for configuration rules (uniqueness, ordering, formula rules).
- [ ] T4.2 Ensure all write operations are `@Transactional` and read methods remain `readOnly`.
- [ ] T4.3 Prepare formula validator and standardized variables for config formulas.

## Phase 5 - Mappers

- [ ] T5.1 Create mapper classes for each configuration entity.
- [ ] T5.2 Ensure mappers stay in controllers (no DTO mapping in services).

## Phase 6 - Controllers & Security

- [ ] T6.1 Refactor all configuration controllers to use DTOs + mappers and stop exposing entities.
- [ ] T6.2 Update non-config controllers (e.g., `JogoController`) to align DTO patterns and ensure no entity exposure.
- [ ] T6.3 Verify `@PreAuthorize` and security requirements align with session-based auth (`08-security.md`).
- [ ] T6.4 Audit Swagger metadata for all controllers to ensure completeness per `07-controllers.md`.

## Phase 7 - Validation Messages Cleanup

- [ ] T7.1 Replace hardcoded validation messages in entities/DTOs with `ValidationMessages` constants.
- [ ] T7.2 Normalize exception handler message strings to shared constants where applicable.

## Phase 8 - Spring Boot 4 / Java 25 Alignment

- [ ] T8.1 Convert request/response DTOs to Java records where feasible.
- [ ] T8.2 Update exception handler to return `ProblemDetail` consistently.
- [ ] T8.3 Enable `spring.mvc.problemdetails.enabled=true` and `spring.threads.virtual.enabled=true` in configuration.

## Phase 9 - Database Alignment

- [ ] T9.1 Review entity mappings and DTO fields against `10-database.md` (naming, audit fields, soft delete).
- [ ] T9.2 Ensure DTOs do not expose internal DB fields or audit fields unless required.

## Phase 10 - OWASP Security

- [ ] T10.1 Audit DTOs and error responses for sensitive data exposure (`11-owasp-security.md`).
- [ ] T10.2 Confirm error responses do not leak stack traces or internal identifiers.

## Phase 11 - Tests (Integration Priority)

- [ ] T11.1 Add integration tests for each configuration controller (success + error paths).
- [ ] T11.2 Add integration tests for validation failures ensuring no persistence.
- [ ] T11.3 Add/update unit tests for complex service logic.
- [ ] T11.4 Update existing tests impacted by DTO/exceptions changes.

## Phase 12 - Review & Cleanup

- [ ] T12.1 Run full test suite and fix regressions.
- [ ] T12.2 Update docs if API contracts changed.

## Phase 13 - CI/CD & Deployment (AWS Free Tier)

- [ ] T13.1 Create GitHub Actions workflow for build/test/package.
- [ ] T13.2 Add deployment automation to EC2 (artifact copy + systemd restart).
- [ ] T13.3 Document RDS connection configuration and secrets management.
- [ ] T13.4 Add JVM and Spring Boot tuning for low memory usage (free tier).
- [ ] T13.5 Validate rate limiting and security headers in production profile.
- [ ] T13.6 Enforce SSM Session Manager access (no SSH/bastion) and close port 22 in SG.
- [ ] T13.7 Restrict RDS SG to only allow EC2 SG on port 5432; keep RDS private.
- [ ] T13.8 Document SSM port-forwarding for local DB access (DBeaver).
- [ ] T13.9 Configure CloudWatch Agent to ship logs with 3-day retention and WARN-level framework logs.

## Checklist

See `specs/003-backend-refactor-best-practices/tasks/CHECKLIST.md` for the phase-by-phase tracking list.
