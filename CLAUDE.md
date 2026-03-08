# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application (requires PostgreSQL via Docker or local)
./mvnw spring-boot:run

# Run all tests (uses H2 in-memory — no PostgreSQL needed)
./mvnw test

# Run a single test class
./mvnw test -Dtest=AtributoConfiguracaoServiceIntegrationTest

# Run a single test method
./mvnw test -Dtest=AtributoConfiguracaoServiceIntegrationTest#deveCriarConfiguracaoComSucesso

# Build without tests
./mvnw package -DskipTests

# Start infrastructure (PostgreSQL)
docker compose up -d
```

## Architecture

This is a Spring Boot 4 / Java 25 REST API for managing RPG character sheets. The core design principle is **everything configurable by the game master (Mestre), nothing hardcoded**.

### Package structure

```
model/          — JPA entities (29 entities)
repository/     — Spring Data JPA repositories
service/        — Business logic
  configuracao/ — Configuration services (abstract base + 14 concrete)
controller/     — REST controllers (thin layer)
  configuracao/ — Configuration controllers (13 controllers)
dto/
  request/configuracao/   — Input DTOs
  response/configuracao/  — Output DTOs
mapper/configuracao/      — MapStruct mappers (request ↔ entity ↔ response)
config/         — Spring configuration (Security, OpenAPI, RateLimit, Audit)
exception/      — Custom exceptions + GlobalExceptionHandler
filter/         — Servlet filters (rate limiting)
constants/      — Application constants
```

### Core abstractions

**`BaseEntity`** — all entities extend this: provides soft delete (`deleted_at`), audit fields (`created_at`, `updated_at`, `created_by`, `updated_by`) with `@SQLRestriction("deleted_at IS NULL")` so deleted records are invisible by default. Use `.delete()` / `.restore()` for soft delete.

**`ConfiguracaoEntity`** — marker interface for all game configuration entities; requires `getId()` and `getJogo()`.

**`BaseConfiguracaoService<T>`** — interface with CRUD operations per game. All configurations are scoped to a `Jogo` (game).

**`AbstractConfiguracaoService<T, R>`** — abstract class implementing the interface with common CRUD logic. Concrete services extend it and override:
- `validarAntesCriar()` — duplicate name checks etc.
- `validarAntesAtualizar()` — conflict checks
- `atualizarCampos()` (**required**) — field update mapping

### Request flow pattern

```
HTTP Request → Controller (thin: validation, mapping only)
  → Mapper.toEntity(request) → Service.criar/atualizar/deletar
  → Repository → DB
  → Mapper.toResponse(entity) → HTTP Response
```

Controllers use `@PreAuthorize("hasRole('MESTRE')")` for write operations and `hasAnyRole('MESTRE', 'JOGADOR')` for reads.

### Authentication

OAuth2 login via Google (`CustomOAuth2UserService`). Session-based auth (not stateless JWT). CORS is configured via `app.cors.allowed-origins` property.

### Testing

- Tests run with `@ActiveProfiles("test")` using H2 in-memory with `ddl-auto=create-drop` — no Flyway.
- **`BaseConfiguracaoServiceIntegrationTest`** — abstract base for all configuration service tests. Concrete tests implement: `getService()`, `getRepository()`, `criarConfiguracaoValida()`, `criarConfiguracaoComNomeDuplicado()`, `atualizarCamposParaTeste()`, `verificarCamposAtualizados()`. The base class provides ~10 common tests automatically.
- Tests annotated `@Transactional` roll back after each test; `@BeforeEach` / `@AfterEach` clean repositories manually for reliability.

### Adding a new configuration entity

1. Create model implementing `BaseEntity` + `ConfiguracaoEntity`
2. Create repository extending `JpaRepository`
3. Create service extending `AbstractConfiguracaoService`, implement `atualizarCampos()`
4. Create request/response DTOs and a MapStruct mapper
5. Create controller following the pattern in `AtributoController`
6. Create integration test extending `BaseConfiguracaoServiceIntegrationTest`

### Key properties

| Property | Default |
|---|---|
| DB URL | `jdbc:postgresql://localhost:5432/rpg_fichas` |
| Port | `8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| CORS origins | `http://localhost:4200,http://localhost:80` |
| Rate limit (general) | 100 req/min per IP |
| Rate limit (auth) | 10 req/min per IP |