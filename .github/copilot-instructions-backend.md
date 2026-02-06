# Padrões do Projeto Spring Boot (Java 21)

> 📚 **Documentação completa em**: `/docs/AI_GUIDELINES_BACKEND.md` e `/docs/backend/`

## 🎯 Quick Reference

Para implementação detalhada, consulte a [documentação modular](../docs/AI_GUIDELINES_BACKEND.md):
- **Arquitetura**: [01-architecture.md](../docs/backend/01-architecture.md)
- **DTOs e Validação**: [02-entities-dtos.md](../docs/backend/02-entities-dtos.md)
- **Exceptions**: [03-exceptions.md](../docs/backend/03-exceptions.md)
- **Security**: [08-security.md](../docs/backend/08-security.md) + [11-owasp-security.md](../docs/backend/11-owasp-security.md)
- **Testing**: [09-testing.md](../docs/backend/09-testing.md)

## Tecnologias e Versões
- **Java 25**: Virtual Threads, Records, Pattern Matching, Sealed Classes, String Templates
- **Spring Boot 4.0.2**: RestClient, JdbcClient, Problem Details (RFC 9457), Virtual Threads nativo
- **PostgreSQL**: Database
- **OAuth2 Google**: Session-based auth (NÃO JWT!)
- **Lombok**: Redução de boilerplate
- **MapStruct 1.5.5**: Mapeamento compile-time

## ⚡ Regras de Codificação (Essenciais)

### Dependency Injection
```java
@RequiredArgsConstructor  // Lombok
public class MyService {
    private final MyRepository repository;
}
```

### DTOs (Records ou Classes)
```java
// Request/Response simples (Record - Java 25)
/**
 * DTO para criação de jogo.
 */
public record CreateGameDTO(
    @NotBlank String nome,
    @Min(1) @Max(20) Integer maxJogadores
) {
    // Compact constructor para validações
    public CreateGameDTO {
        if (maxJogadores > 10 && nome.length() < 10) {
            throw new IllegalArgumentException("Nome deve ser mais descritivo");
        }
    }
}

// Response complexo (Class + Builder quando necessário)
@Data
@Builder
public class GameResponseDTO {
    private Long id;
    private String nome;
    private UserDTO mestre;
    
    @Builder.Default
    private List<ParticipanteDTO> participantes = new ArrayList<>();
}
```

### Entities
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
    
    @Column(nullable = false)
    private String nome;
}
```

### Services (Lógica de Negócio)
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {
    private final GameRepository repository;
    
    // SEMPRE use exceptions específicas
    public Game findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", id));
    }
    
    @Transactional
    public Game create(Game game) {
        // Validações de negócio aqui
        return repository.save(game);
    }
}
```

### Controllers (Thin - apenas coordenação)
```java
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
@Tag(name = "Jogos")
public class GameController {
    private final GameService service;
    private final GameMapper mapper;
    
    @PostMapping
    @Operation(summary = "Criar jogo")
    public ResponseEntity<GameResponseDTO> create(@Valid @RequestBody CreateGameDTO dto) {
        Game game = mapper.toEntity(dto);
        Game created = service.create(game);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponseDTO(created));
    }
}
```

## 🛡️ Security (CRÍTICO)

### OAuth2 Session-Based (NÃO JWT!)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .oauth2Login(oauth2 -> oauth2.successHandler(handler))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        return http.build();
    }
}
```

### Validação de Permissões
```java
@PreAuthorize("hasRole('MESTRE')")
@PostMapping
public ResponseEntity<GameDTO> create() { }

@PreAuthorize("@securityService.canEdit(#id)")
@PutMapping("/{id}")
public ResponseEntity<GameDTO> update(@PathVariable Long id) { }
```

## 🧪 Testing (Prioridade: Integração)

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class GameControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateGame() {
        CreateGameDTO dto = new CreateGameDTO("Novo Jogo", 5);
        
        ResponseEntity<GameResponseDTO> response = restTemplate
            .postForEntity("/api/jogos", dto, GameResponseDTO.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Verificar no banco também
    }
}
```

## ❌ NUNCA Faça

1. ❌ Lógica de negócio na controller
2. ❌ Expor entities nas APIs (use DTOs)
3. ❌ Mappers no service (devem estar na controller)
4. ❌ Exceptions genéricas
5. ❌ JWT para auth (usamos session!)
6. ❌ Esquecer @Transactional em escritas
7. ❌ Não validar permissões (@PreAuthorize)
8. ❌ Stack traces em produção

## 📖 Documentação Completa

**SEMPRE consulte**: `/docs/AI_GUIDELINES_BACKEND.md` para detalhes completos.

### Arquivos por Tópico:
- Arquitetura → `01-architecture.md`
- DTOs/Entities → `02-entities-dtos.md`
- Exceptions → `03-exceptions.md`
- Repositories → `04-repositories.md`
- Services → `05-services.md`
- Mappers → `06-mappers.md`
- Controllers/Swagger → `07-controllers.md`
- Security → `08-security.md`
- Testing → `09-testing.md`
- Database → `10-database.md`
- OWASP Security → `11-owasp-security.md`
