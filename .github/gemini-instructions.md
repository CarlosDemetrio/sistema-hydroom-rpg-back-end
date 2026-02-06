# Gemini AI Instructions - Ficha Controlador Backend

> 🤖 Sistema de gerenciamento de fichas RPG - Spring Boot + PostgreSQL + OAuth2

## 📖 Documentação Principal

**SEMPRE CONSULTE**: `/docs/AI_GUIDELINES_BACKEND.md`

Guias modulares em `/docs/backend/`:
- **01-architecture.md** - Estrutura de camadas (Controller → Service → Repository)
- **02-entities-dtos.md** - Entities, DTOs e ValidationMessages
- **03-exceptions.md** - Hierarquia de exceptions e GlobalExceptionHandler
- **04-repositories.md** - JpaRepository patterns e Optional
- **05-services.md** - Business logic e @Transactional
- **06-mappers.md** - Entity ↔ DTO (NA CONTROLLER!)
- **07-controllers.md** - REST APIs e Swagger
- **08-security.md** - OAuth2 Session-based (NÃO JWT!)
- **09-testing.md** - Integration tests (80%) + Unit tests (20%)
- **10-database.md** - PostgreSQL, JPA e naming conventions
- **11-owasp-security.md** - OWASP Top 10 e security checklist

## 🎯 Contexto do Projeto

### Stack Tecnológico
- **Java 21** (LTS)
- **Spring Boot 3.x**
- **PostgreSQL** (database)
- **OAuth2 Google** (session-based authentication)
- **Lombok** (boilerplate reduction)
- **JPA/Hibernate** (ORM)
- **Maven** (build tool)

### Arquitetura
```
HTTP Request → Controller → Mapper → Service → Repository → Database
                   ↓          ↓         ↓
                @Valid     Entity  Business
                 DTO               Logic
```

**Princípio**: Controller thin (coordenação), Service fat (lógica de negócio)

## ⚡ Templates Essenciais

### Entity (JPA)
```java
@Entity
@Table(name = "games")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nome;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### DTO
```java
// Request (Record - Java 25)
/**
 * DTO para criação de jogo.
 * Record: imutável, conciso, validação automática.
 */
public record CreateGameDTO(
    @NotBlank(message = ValidationMessages.Game.NAME_REQUIRED)
    @Size(max = 100, message = ValidationMessages.Game.NAME_MAX_SIZE)
    String nome,
    
    @Min(value = 1, message = ValidationMessages.Game.MAX_PLAYERS_MIN)
    @Max(value = 20, message = ValidationMessages.Game.MAX_PLAYERS_MAX)
    Integer maxJogadores
) {
    // Compact constructor para validações customizadas
    public CreateGameDTO {
        if (maxJogadores != null && maxJogadores > 10 && nome.length() < 10) {
            throw new IllegalArgumentException(
                "Jogos com mais de 10 jogadores devem ter nome descritivo"
            );
        }
    }
}

// Response (Record - Java 25)
public record GameResponseDTO(
    Long id,
    String nome,
    Integer maxJogadores,
    UserSimpleDTO mestre,
    LocalDateTime createdAt
) {
    // Factory method
    public static GameResponseDTO from(Game game) {
        return new GameResponseDTO(
            game.getId(),
            game.getNome(),
            game.getMaxJogadores(),
            UserSimpleDTO.from(game.getMestre()),
            game.getCreatedAt()
        );
    }
}

// Response Complexo (Class com @Builder quando necessário)
@Data
@Builder
public class GameDetailResponseDTO {
    private Long id;
    private String nome;
    private Integer maxJogadores;
    private UserSimpleDTO mestre;
    
    @Builder.Default
    private List<ParticipanteDTO> participantes = new ArrayList<>();
    
    private LocalDateTime createdAt;
}
```

### Repository
```java
public interface GameRepository extends JpaRepository<Game, Long> {
    // Query methods
    Optional<Game> findByNomeAndMestreId(String nome, Long mestreId);
    boolean existsByNomeAndMestreId(String nome, Long mestreId);
    List<Game> findByMestreId(Long mestreId);
    
    // Custom query
    @Query("SELECT g FROM Game g LEFT JOIN FETCH g.participantes WHERE g.id = :id")
    Optional<Game> findByIdWithParticipantes(@Param("id") Long id);
}
```

### Service
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final SecurityService securityService;
    
    /**
     * Busca jogo por ID.
     * @throws ResourceNotFoundException se não encontrado
     */
    public Game findById(Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", id));
    }
    
    /**
     * Cria novo jogo.
     * @throws ForbiddenException se usuário não é mestre
     * @throws ConflictException se nome duplicado
     */
    @Transactional
    public Game create(Game game, User currentUser) {
        log.info("Criando jogo '{}' para usuário {}", game.getNome(), currentUser.getId());
        
        // Validações de negócio
        if (!currentUser.isMestre()) {
            throw new ForbiddenException("Apenas mestres podem criar jogos");
        }
        
        if (gameRepository.existsByNomeAndMestreId(game.getNome(), currentUser.getId())) {
            throw new ConflictException("Você já possui um jogo com este nome");
        }
        
        game.setMestre(currentUser);
        Game created = gameRepository.save(game);
        
        log.info("Jogo criado com sucesso: ID {}", created.getId());
        return created;
    }
}
```

### Mapper
```java
@Component
@RequiredArgsConstructor
public class GameMapper {
    private final UserMapper userMapper;
    
    public GameResponseDTO toResponseDTO(Game game) {
        return GameResponseDTO.builder()
            .id(game.getId())
            .nome(game.getNome())
            .maxJogadores(game.getMaxJogadores())
            .mestre(userMapper.toSimpleDTO(game.getMestre()))
            .createdAt(game.getCreatedAt())
            .build();
    }
    
    public List<GameResponseDTO> toResponseDTOList(List<Game> games) {
        return games.stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    public Game toEntity(CreateGameDTO dto) {
        return Game.builder()
            .nome(dto.nome())
            .maxJogadores(dto.maxJogadores())
            .build();
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
@Tag(name = "Jogos", description = "API para gerenciamento de jogos")
public class GameController {
    private final GameService gameService;
    private final GameMapper gameMapper;
    private final SecurityService securityService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar jogo por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Jogo encontrado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<GameResponseDTO> findById(
            @Parameter(description = "ID do jogo") @PathVariable Long id) {
        Game game = gameService.findById(id);
        return ResponseEntity.ok(gameMapper.toResponseDTO(game));
    }
    
    @PostMapping
    @Operation(summary = "Criar novo jogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Jogo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Usuário não é mestre"),
        @ApiResponse(responseCode = "409", description = "Nome duplicado")
    })
    @PreAuthorize("hasRole('MESTRE')")
    public ResponseEntity<GameResponseDTO> create(@Valid @RequestBody CreateGameDTO dto) {
        User currentUser = securityService.getCurrentUser();
        Game game = gameMapper.toEntity(dto);
        Game created = gameService.create(game, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gameMapper.toResponseDTO(created));
    }
}
```

### Integration Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class GameControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Deve criar jogo com sucesso")
    void shouldCreateGame() {
        // Arrange
        CreateGameDTO dto = new CreateGameDTO("Novo Jogo", 5);
        
        // Act
        ResponseEntity<GameResponseDTO> response = restTemplate
            .postForEntity("/api/jogos", dto, GameResponseDTO.class);
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNome()).isEqualTo("Novo Jogo");
        
        // Verifica no banco
        List<Game> games = gameRepository.findAll();
        assertThat(games).hasSize(1);
    }
}
```

## 🛡️ Security (CRÍTICO)

### OAuth2 Session-Based
**NUNCA use JWT!** Este projeto usa OAuth2 com sessões.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            );
        return http.build();
    }
}
```

### Validação de Permissões
```java
// Por role
@PreAuthorize("hasRole('MESTRE')")
@PostMapping
public ResponseEntity<GameDTO> create() { }

// Por ownership
@PreAuthorize("@securityService.canEdit(#id)")
@PutMapping("/{id}")
public ResponseEntity<GameDTO> update(@PathVariable Long id) { }
```

## 🧪 Testing Strategy

### Prioridade: Integration Tests (80%)
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class IntegrationTest {
    // Testa TODO o fluxo: HTTP → Controller → Service → Repository → DB
    @Autowired TestRestTemplate restTemplate;
    @Autowired MyRepository repository;
    
    @Test
    void testCompleteFlow() {
        // Arrange, Act, Assert + verificar no banco
    }
}
```

### Unit Tests (20% - apenas lógica complexa)
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock MyRepository repository;
    @InjectMocks MyService service;
    
    @Test
    void testBusinessLogic() {
        // Testar regras de negócio isoladas
    }
}
```

## ❌ NUNCA Faça

1. **Lógica de negócio na controller** - Vai no service!
2. **Expor entities nas APIs** - Use DTOs!
3. **Mappers no service** - Vão na controller!
4. **Exceptions genéricas** - Use específicas (ResourceNotFoundException, etc.)
5. **JWT para auth** - Usamos SESSION!
6. **Esquecer @Transactional** - Obrigatório em escritas
7. **Não validar permissões** - Use @PreAuthorize
8. **Stack traces em produção** - Configure error handling

## 📊 HTTP Status Codes

| Ação | Sucesso | Código |
|------|---------|--------|
| GET | OK | 200 |
| POST | Created | 201 |
| PUT | OK | 200 |
| DELETE | No Content | 204 |

| Erro | Código |
|------|--------|
| Validação | 400 |
| Não autenticado | 401 |
| Sem permissão | 403 |
| Não encontrado | 404 |
| Conflito | 409 |
| Regra de negócio | 422 |

## 🗂️ Naming Conventions

### Database (snake_case)
- Tables: `users`, `games`, `user_games`
- Columns: `created_at`, `user_id`, `max_jogadores`
- Foreign Keys: `{table}_id`

### Java (camelCase/PascalCase)
- Classes: `GameService`, `UserRepository`
- Methods: `findById`, `createGame`
- Variables: `currentUser`, `maxPlayers`
- Constants: `MAX_PLAYERS`, `DEFAULT_ROLE`

## 🔗 Referências Completas

Para implementações detalhadas e casos avançados:
- Consulte `/docs/AI_GUIDELINES_BACKEND.md`
- Veja guias específicos em `/docs/backend/`
- Siga os templates acima

## 💡 Dicas de Implementação

1. **Sempre comece pelos testes** (TDD)
2. **Valide permissões em TODOS os endpoints**
3. **Use Optional corretamente** (apenas quando ausência não é erro)
4. **Documente com JavaDoc** (métodos públicos)
5. **Documente API com Swagger** (todos os endpoints)
6. **Verifique OWASP** (consulte `11-owasp-security.md`)

---

**Qualidade > Velocidade**: Siga os padrões, escreva testes, consulte a documentação!
