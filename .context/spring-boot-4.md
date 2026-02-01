# Spring Boot 4.0 - Best Practices Context

## Core Principles

### 1. Virtual Threads (Project Loom)
- **ALWAYS** enable virtual threads in application.properties:
  ```properties
  spring.threads.virtual.enabled=true
  ```
- Use `@Async` with virtual thread executor for async operations
- No thread pools needed - virtual threads are cheap

### 2. Problem Details (RFC 9457)
- **ALWAYS** use `ProblemDetail` for error responses
- Enable in properties:
  ```properties
  spring.mvc.problemdetails.enabled=true
  ```
- Example:
  ```java
  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
      return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }
  ```

### 3. Observability
- Use Micrometer for metrics
- Use `@Observed` annotation for automatic observability
- Configure management endpoints properly

### 4. New Features to Use
- `RestClient` instead of `RestTemplate` or `WebClient` (for blocking calls)
- `JdbcClient` for simpler JDBC operations
- Improved native image support with GraalVM

## Spring Data JPA Best Practices

### 1. Projections
- Use interface-based projections for DTOs:
  ```java
  public interface UserSummary {
      String getName();
      String getEmail();
  }
  ```

### 2. Query Methods
- Prefer derived query methods over `@Query` when possible
- Use `@EntityGraph` to avoid N+1 queries:
  ```java
  @EntityGraph(attributePaths = {"jogo", "usuario"})
  Optional<Ficha> findById(Long id);
  ```

### 3. Auditing
- Use `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`
- Enable with `@EnableJpaAuditing`

### 4. Specifications
- Use Specifications for dynamic queries instead of JPQL strings

## Security Best Practices

### 1. OAuth2 Resource Server
- Configure properly for stateless JWT validation
- Use method security: `@PreAuthorize`, `@PostAuthorize`

### 2. CSRF Protection
- Enabled by default for state-changing operations
- Use CSRF tokens in forms

### 3. Rate Limiting
- Use Bucket4j with latest API (non-deprecated methods):
  ```java
  Bandwidth limit = Bandwidth.builder()
      .capacity(100)
      .refillGreedy(100, Duration.ofHours(1))
      .build();
  ```

## Validation

### 1. Use Jakarta Validation
- `@Valid` on method parameters
- `@NotNull`, `@NotBlank`, `@Size`, etc.
- Custom validators when needed

### 2. DTO Validation
- Validate at controller level
- Return `ProblemDetail` with validation errors

## Testing

### 1. Use `@SpringBootTest` sparingly
- Prefer slice tests: `@WebMvcTest`, `@DataJpaTest`
- Use `@MockBean` for dependencies

### 2. Integration Tests
- Use `@Testcontainers` for real databases
- Use H2 only for simple unit tests

## Performance

### 1. Caching
- Use `@Cacheable`, `@CacheEvict`, `@CachePut`
- Configure appropriate cache provider

### 2. Async Operations
- Use `@Async` with virtual threads
- Use `CompletableFuture` for async results

### 3. Database
- Use connection pooling (HikariCP is default)
- Configure appropriate pool size
- Use indexes properly

## Configuration

### 1. Properties Organization
- Use `application.yml` or `application.properties`
- Use profiles: `application-dev.yml`, `application-prod.yml`
- Use `@ConfigurationProperties` for type-safe configuration

### 2. Logging
- Use SLF4J with Logback
- Configure different levels per package
- Use structured logging for production

## Docker & Deployment

### 1. Layered JARs
- Enabled by default in Spring Boot 4
- Optimizes Docker layer caching

### 2. Native Images
- Use GraalVM for faster startup and lower memory
- Configure hints for reflection/resources

## OpenAPI Documentation

### 1. SpringDoc OpenAPI
- Auto-generates from code
- Use `@Operation`, `@ApiResponse` for detailed docs
- Configure properly in application.properties

## Avoid Deprecated APIs

### 1. Removed in Spring Boot 4
- `WebSecurityConfigurerAdapter` - use `SecurityFilterChain` bean
- Old Actuator metrics - use Micrometer
- `RestTemplate` for new code - use `RestClient`

### 2. Check Migration Guide
- Always check Spring Boot 4.0 migration guide
- Review breaking changes from Spring Framework 7

## Example Application Structure

```
src/main/java
├── config/         # Configuration classes
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Data access
├── model/          # Entities
├── dto/            # Data Transfer Objects
├── mapper/         # MapStruct mappers (NOT converters)
├── exception/      # Custom exceptions
└── constants/      # Constants
```

## DO NOT USE

❌ **JPA Converters** - Use MapStruct mappers instead
❌ **JSON columns** - Use normalized tables
❌ **Hardcoded values** - Everything configurable via database
❌ **Deprecated Bucket4j methods** - Use builder pattern
❌ **Constructor injection for simple cases** - Lombok `@RequiredArgsConstructor` is fine

## ALWAYS USE

✅ Virtual threads
✅ ProblemDetail for errors
✅ MapStruct for object mapping
✅ Hibernate Envers for auditing
✅ Proper database normalization
✅ Flyway for database migrations
✅ Latest non-deprecated APIs
