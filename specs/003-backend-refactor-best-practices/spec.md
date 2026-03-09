# Feature Specification: Backend Refactor Best Practices

**Feature Branch**: `003-backend-refactor-best-practices`  
**Created**: 2026-02-06  
**Status**: Draft  
**Input**: User description: "refactor all controllers/services/DTOs to follow AI guidelines; move hardcoded validation messages to ValidationMessages; use correct exceptions; use Optional consistently; add DTOs + validation for configuration controllers; update unit/integration tests"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consistent Validation & Errors (Priority: P1)

As an API consumer, I receive consistent validation messages and correct error categories so I can handle failures predictably.

**Why this priority**: Inconsistent validation and error handling breaks clients and creates support overhead.

**Independent Test**: Can be tested by submitting invalid and conflicting requests to representative endpoints (including configuration endpoints) and verifying standardized messages and error categories.

**Acceptance Scenarios**:

1. **Given** a request with missing or invalid fields, **When** the request is submitted, **Then** the response includes standardized validation messages and a validation error category.
2. **Given** a request that conflicts with existing data, **When** it is submitted, **Then** the response includes the appropriate conflict error category and a clear message.

---

### User Story 2 - Controllers Use DTOs (Priority: P1)

As a maintainer, I want all controllers to accept and return DTOs so entities are not exposed to clients and validation is centralized.

**Why this priority**: Entity exposure leaks internal structure and makes refactors risky.

**Independent Test**: Verify that controller request/response signatures are DTOs and responses exclude internal fields.

**Acceptance Scenarios**:

1. **Given** any controller in scope, **When** I inspect its request/response types, **Then** entities are not exposed directly.
2. **Given** a controller response, **When** it is serialized, **Then** internal IDs/sensitive fields are not present.

---

### User Story 3 - Configuration Inputs Are Validated (Priority: P2)

As an administrator, I can create or update configuration records using clear input rules so that only valid data is accepted.

**Why this priority**: Configuration data affects multiple features; invalid data can cause cascading issues.

**Independent Test**: Can be tested by creating and updating configuration records with valid and invalid inputs and verifying stored results and rejections.

**Acceptance Scenarios**:

1. **Given** a valid configuration payload, **When** it is submitted, **Then** the configuration is stored and returned in the response.
2. **Given** an invalid configuration payload, **When** it is submitted, **Then** the request is rejected with validation errors and no data is persisted.

---

### User Story 4 - Tests Guard the Refactor (Priority: P3)

As a maintainer, I rely on automated tests that cover success and error paths so refactors do not introduce regressions.

**Why this priority**: The refactor touches validation and error handling, which are critical behaviors.

**Independent Test**: Can be tested by running the automated test suite for representative endpoints and validating that both success and failure scenarios are covered.

**Acceptance Scenarios**:

1. **Given** the updated test suite, **When** it is executed, **Then** it verifies at least one success and one error case for each controller in scope, prioritizing configuration endpoints.

---

### Edge Cases

- What happens when a record is requested but does not exist?
- How does the system handle optional inputs being omitted or empty?
- What happens when multiple invalid fields are submitted in a single request?
- How does the system handle conflicts for unique or constrained values?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST use `ValidationMessages` for all validation messages within the refactor scope.
- **FR-002**: System MUST return correct error categories for validation errors, missing records, and conflicts using custom exceptions.
- **FR-003**: Services MUST handle missing records explicitly rather than returning null data.
- **FR-004**: All controllers in scope MUST accept and return DTOs with validation rules aligned to `02-entities-dtos.md`.
- **FR-005**: Configuration endpoints MUST accept and return defined request/response models with validation rules.
- **FR-006**: Controllers MUST not expose internal or sensitive fields in responses.
- **FR-007**: Automated tests MUST cover success and error scenarios for all controllers in scope, prioritizing configuration endpoints.
- **FR-008**: Automated tests MUST verify persistence on success and no persistence on validation failure.
- **FR-009**: Refactor steps MUST follow guideline order: architecture → entities/DTOs → exceptions → repositories/Optional → services → mappers → controllers → security → testing.
- **FR-010**: Controllers MUST follow `07-controllers.md` patterns for status codes, pagination, and Swagger metadata.
- **FR-011**: Security annotations MUST align with session-based auth and `08-security.md`.
- **FR-012**: DTOs and responses MUST avoid exposing sensitive fields per `11-owasp-security.md`.
- **FR-013**: Entity mappings and DTO constraints MUST respect `10-database.md` conventions.
- **FR-014**: DTOs MUST be implemented as Java records where feasible (Java 25).
- **FR-015**: Error responses MUST use `ProblemDetail` (RFC 9457) per Spring Boot 4.
- **FR-016**: Application configuration MUST enable virtual threads and Problem Details where applicable.
- **FR-017**: Deployment MUST target AWS EC2 (backend) and RDS (PostgreSQL) with CI/CD via GitHub Actions.
- **FR-018**: Application MUST be configured for minimal memory/CPU usage suitable for AWS free tier.
- **FR-019**: Runtime configuration MUST include Java 25/Spring Boot 4 tuning for low resource usage.
- **FR-020**: Rate limiting and security headers MUST remain enabled in production profiles.
- **FR-021**: SSH MUST be disabled (no port 22); access MUST use SSM Session Manager.
- **FR-022**: RDS MUST be private and only accept connections from the EC2 security group on port 5432.
- **FR-023**: Local DB access MUST use SSM port forwarding through EC2 (no public RDS).
- **FR-024**: CloudWatch logging MUST use 3-day retention and framework logs at WARN level.

### Key Entities *(include if feature involves data)*

- **Validation Message Catalog**: Centralized `ValidationMessages` constants used across endpoints.
- **Configuration Record**: A persisted configuration item with defined fields and constraints.
- **Error Response**: A standardized response describing the error category and messages returned to clients.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of validation messages within the refactor scope are sourced from `ValidationMessages`.
- **SC-002**: For each controller in scope, at least one automated test validates a success path and one validates an error path.
- **SC-003**: All controllers in scope reject invalid requests with clear validation messages and correct error categories.
- **SC-004**: No controller in scope exposes internal or sensitive fields in its responses.

## Assumptions

- The refactor scope includes all backend controllers/services/DTOs, with priority on configuration endpoints.
- Existing client contracts allow standardized error messages without breaking critical flows.
- Validation rules for configuration inputs are already defined or can be inferred from current behavior.

## Dependencies

- Access to current controller/service behaviors and existing test coverage baselines.
- Alignment on the `ValidationMessages` content and naming conventions.
