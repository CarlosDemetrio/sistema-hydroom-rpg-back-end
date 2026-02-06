# GitHub Copilot Instructions - Ficha Controlador Backend

> 🤖 **Sistema de gerenciamento de fichas RPG** - Spring Boot + PostgreSQL + OAuth2

**Tech Stack**: Java 25 + Spring Boot 4.0.2 + PostgreSQL + MapStruct 1.5.5

## 📖 Documentation

**PRIMARY SOURCE**: `/docs/AI_GUIDELINES_BACKEND.md`

Modular documentation in `/docs/backend/`:
1. **Architecture** → `01-architecture.md`
2. **DTOs/Entities** → `02-entities-dtos.md`
3. **Exceptions** → `03-exceptions.md`
4. **Repositories** → `04-repositories.md`
5. **Services** → `05-services.md`
6. **Mappers** → `06-mappers.md`
7. **Controllers** → `07-controllers.md`
8. **Security** → `08-security.md` + `11-owasp-security.md`
9. **Testing** → `09-testing.md`
10. **Database** → `10-database.md`

## 🎯 Quick Rules

### Layer Responsibilities
```
HTTP Request → Controller → Mapper → Service → Repository → Database
                   ↓          ↓         ↓
                 @Valid     Entity  Business
                  DTO              Logic
```

- **Controller**: Thin (coordination only)
- **Service**: Fat (ALL business logic)
- **Mapper**: IN CONTROLLER (not in service!)

### Code Templates

#### Entity
```java
@Entity
@Table(name = "resources")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### DTO
```java
// Request
public record CreateResourceDTO(
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(max = 100)
    String name
) {}

// Response
@Data
@Builder
public class ResourceResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

#### Repository
```java
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Optional<Resource> findByName(String name);
    boolean existsByName(String name);
    List<Resource> findByUserId(Long userId);
}
```

#### Service
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {
    private final ResourceRepository repository;
    
    public Resource findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource", id));
    }
    
    @Transactional
    public Resource create(Resource resource) {
        if (repository.existsByName(resource.getName())) {
            throw new ConflictException("Name already exists");
        }
        return repository.save(resource);
    }
}
```

#### Mapper
```java
@Component
public class ResourceMapper {
    public ResourceResponseDTO toResponseDTO(Resource entity) {
        return ResourceResponseDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .createdAt(entity.getCreatedAt())
            .build();
    }
    
    public Resource toEntity(CreateResourceDTO dto) {
        return Resource.builder()
            .name(dto.name())
            .build();
    }
}
```

#### Controller
```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources")
public class ResourceController {
    private final ResourceService service;
    private final ResourceMapper mapper;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID")
    public ResponseEntity<ResourceResponseDTO> findById(@PathVariable Long id) {
        Resource entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(entity));
    }
    
    @PostMapping
    @Operation(summary = "Create resource")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "409", description = "Already exists")
    })
    public ResponseEntity<ResourceResponseDTO> create(
            @Valid @RequestBody CreateResourceDTO dto) {
        Resource entity = mapper.toEntity(dto);
        Resource created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponseDTO(created));
    }
}
```

#### Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ResourceControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ResourceRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Should create resource successfully")
    void shouldCreateResource() {
        // Arrange
        CreateResourceDTO dto = new CreateResourceDTO("Test Resource");
        
        // Act
        ResponseEntity<ResourceResponseDTO> response = restTemplate
            .postForEntity("/api/resources", dto, ResourceResponseDTO.class);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Test Resource");
        
        // Verify in database
        assertThat(repository.findAll()).hasSize(1);
    }
}
```

## 🛡️ Security

### OAuth2 Session-Based (NOT JWT!)
```java
@PreAuthorize("hasRole('MESTRE')")
@PostMapping
public ResponseEntity<GameDTO> create() { }

@PreAuthorize("@securityService.canEdit(#id)")
@PutMapping("/{id}")
public ResponseEntity<GameDTO> update(@PathVariable Long id) { }
```

### NEVER expose in DTOs:
- passwords
- tokens
- internal IDs
- sensitive data

## 🧪 Testing Priority

**80% Integration Tests + 20% Unit Tests**

ALWAYS:
- Test success AND error cases
- Verify database persistence
- Use descriptive names
- Follow Arrange-Act-Assert pattern

## ❌ Never Do

1. Business logic in controllers
2. Expose entities in APIs
3. Mappers in services
4. Generic exceptions
5. JWT for auth (use SESSION!)
6. Forget @Transactional
7. Skip @PreAuthorize
8. Stack traces in production

## 📊 HTTP Status Codes

| Action | Success | Code |
|--------|---------|------|
| GET    | Found   | 200  |
| POST   | Created | 201  |
| PUT    | Updated | 200  |
| DELETE | Deleted | 204  |

| Error          | Code |
|----------------|------|
| Validation     | 400  |
| Unauthorized   | 401  |
| Forbidden      | 403  |
| Not Found      | 404  |
| Conflict       | 409  |
| Business Rule  | 422  |

## 🔗 For More Details

See complete documentation in `/docs/backend/` for:
- Advanced patterns
- Security checklist
- OWASP guidelines
- Performance tips
- Complex examples
