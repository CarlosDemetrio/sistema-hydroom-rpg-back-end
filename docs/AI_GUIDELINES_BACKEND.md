# Backend AI Guidelines

## Project Context
- Java 21 + Spring Boot 3.x
- PostgreSQL database
- OAuth2 Google authentication with session-based security
- RESTful API architecture

## Architecture Patterns

### Layer Structure
```
Controller → Service → Repository → Database
     ↓          ↓
    DTO    ←  Entity
```

### Dependency Injection
Always use constructor injection with Lombok:
```java
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
}
```

## Code Standards

### 1. Entities
```java
@Entity
@Table(name = "table_name")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Rules:**
- Always use Lombok annotations
- Table names in snake_case
- Add audit fields (createdAt, updatedAt)
- Use FetchType.LAZY for relationships
- Never expose sensitive data (passwords, tokens)

### 2. DTOs
```java
@Data
@Builder
public class MyDTO {
    private Long id;
    
    @NotBlank(message = "Campo obrigatório")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String name;
    
    @Email(message = "Email inválido")
    private String email;
}
```

**Rules:**
- Use Bean Validation annotations
- DTOs for requests AND responses
- Never return entities directly from controllers
- Use record classes for simple DTOs when appropriate

### 3. Repositories
```java
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    List<MyEntity> findByUserId(Long userId);
    
    @Query("SELECT e FROM MyEntity e WHERE e.name LIKE %:name%")
    List<MyEntity> searchByName(@Param("name") String name);
}
```

**Rules:**
- Extend JpaRepository<Entity, ID>
- Use query methods naming convention
- Use @Query for complex queries
- Add custom queries only when needed

### 4. Services
```java
@Service
@RequiredArgsConstructor
@Transactional
public class MyService {
    private final MyRepository repository;
    private final MyMapper mapper;
    
    public List<MyDTO> findAll() {
        return repository.findAll().stream()
            .map(mapper::toDTO)
            .toList();
    }
    
    public MyDTO findById(Long id) {
        return repository.findById(id)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Not found: " + id));
    }
    
    @Transactional
    public MyDTO create(MyCreateDTO dto) {
        MyEntity entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }
}
```

**Rules:**
- All business logic in services
- @Transactional for data modification
- Always use DTOs, never expose entities
- Throw specific exceptions
- Use mappers for Entity ↔ DTO conversion

### 5. Controllers
```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class MyController {
    private final MyService service;
    
    @GetMapping
    public ResponseEntity<List<MyDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MyDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<MyDTO> create(@Valid @RequestBody MyCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MyUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Rules:**
- Controllers coordinate only, no business logic
- Always return ResponseEntity<T>
- Use appropriate HTTP status codes
- RESTful endpoints (no /getResource, /createResource)
- @Valid for DTO validation
- Thin controllers, fat services

### 6. Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
            .body(new ValidationErrorResponse(errors));
    }
}
```

### 7. Security Configuration

**CRITICAL: OAuth2 with Session, NOT JWT Resource Server**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        return http.build();
    }
}
```

**NEVER USE:**
- ❌ `.oauth2ResourceServer()` - conflicts with session auth
- ❌ `SessionCreationPolicy.STATELESS` - we need sessions!
- ❌ JWT tokens for user authentication

## Database

### Naming Conventions
- Tables: `snake_case` (users, user_games, character_sheets)
- Columns: `snake_case` (created_at, user_id)
- Foreign keys: `{table}_id` (user_id, game_id)

### Migration
Use Hibernate DDL auto for development:
```properties
spring.jpa.hibernate.ddl-auto=update  # dev
spring.jpa.hibernate.ddl-auto=validate  # prod
```

## Testing

```java
@SpringBootTest
class MyServiceTest {
    @Autowired
    private MyService service;
    
    @MockBean
    private MyRepository repository;
    
    @Test
    void shouldFindById() {
        MyEntity entity = MyEntity.builder()
            .id(1L)
            .name("Test")
            .build();
            
        when(repository.findById(1L))
            .thenReturn(Optional.of(entity));
            
        MyDTO result = service.findById(1L);
        
        assertNotNull(result);
        assertEquals("Test", result.getName());
    }
}
```

## Project Specific Rules

### User Roles
- MESTRE: Full access, creates games, manages players
- JOGADOR: Limited access, only own games and sheets

### OAuth2 Flow
1. User clicks "Login with Google"
2. Backend redirects to Google
3. Google authenticates and returns to backend
4. Backend creates session with httpOnly cookie
5. Backend redirects to frontend callback
6. Frontend stores user info and navigates to home

### API Endpoints Pattern
```
/api/public/*     - Public endpoints (no auth)
/api/user         - Current user info
/api/jogos        - Games management
/api/fichas       - Character sheets
/api/admin/*      - Admin only (MESTRE role)
```

## Common Mistakes to Avoid

1. ❌ Exposing entities directly from controllers
2. ❌ Business logic in controllers
3. ❌ Missing @Transactional on data modification
4. ❌ Not validating DTOs
5. ❌ Using JWT when we have session-based auth
6. ❌ Returning null instead of Optional
7. ❌ Not handling exceptions properly
8. ❌ Missing CORS configuration
9. ❌ Hardcoded values instead of properties
10. ❌ Not using Lombok annotations

## Dependencies

```xml
<!-- Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```
