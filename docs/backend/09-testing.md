# Testing - Prioridade: Integração

## 🎯 Estratégia de Testes

```
1. Testes de Integração (PRIORIDADE) - 80%
2. Testes Unitários (quando necessário) - 20%
```

## Testes de Integração

### Template Base

```java
/**
 * Testes de integração para MyController.
 * Testa todo o fluxo: HTTP → Controller → Service → Repository → DB
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MyControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private MyRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Deve criar recurso com sucesso")
    void shouldCreateResource() {
        // Arrange
        MyCreateDTO dto = MyCreateDTO.builder()
            .name("Test Resource")
            .description("Test Description")
            .build();
        
        // Act
        ResponseEntity<MyResponseDTO> response = restTemplate.postForEntity(
            "/api/resources",
            dto,
            MyResponseDTO.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Resource");
        
        // Verifica persistência no banco
        List<MyEntity> entities = repository.findAll();
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getName()).isEqualTo("Test Resource");
    }
    
    @Test
    @DisplayName("Deve retornar 404 quando recurso não existe")
    void shouldReturn404WhenNotFound() {
        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
            "/api/resources/999",
            ErrorResponse.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("não encontrado");
    }
    
    @Test
    @DisplayName("Deve retornar 400 em validação")
    void shouldReturn400OnValidation() {
        // Arrange
        MyCreateDTO dto = MyCreateDTO.builder()
            .name("") // Inválido
            .build();
        
        // Act
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/resources",
            dto,
            ValidationErrorResponse.class
        );
        
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getFieldErrors()).containsKey("name");
    }
    
    @Test
    @DisplayName("Deve atualizar recurso")
    void shouldUpdateResource() {
        // Arrange
        MyEntity entity = repository.save(MyEntity.builder()
            .name("Original")
            .build());
        
        MyUpdateDTO dto = MyUpdateDTO.builder()
            .name("Updated")
            .build();
        
        // Act
        restTemplate.put("/api/resources/" + entity.getId(), dto);
        
        // Assert
        MyEntity updated = repository.findById(entity.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated");
    }
    
    @Test
    @DisplayName("Deve deletar recurso")
    void shouldDeleteResource() {
        // Arrange
        MyEntity entity = repository.save(MyEntity.builder()
            .name("To Delete")
            .build());
        
        // Act
        restTemplate.delete("/api/resources/" + entity.getId());
        
        // Assert
        assertThat(repository.findById(entity.getId())).isEmpty();
    }
}
```

## Testes com Autenticação

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GameControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User mestre;
    private User jogador;
    
    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
        
        mestre = userRepository.save(User.builder()
            .nome("Mestre")
            .email("mestre@test.com")
            .role(UserRole.MESTRE)
            .build());
        
        jogador = userRepository.save(User.builder()
            .nome("Jogador")
            .email("jogador@test.com")
            .role(UserRole.JOGADOR)
            .build());
    }
    
    @Test
    @DisplayName("Mestre deve conseguir criar jogo")
    @WithMockUser(username = "mestre@test.com", roles = "MESTRE")
    void mestreShouldCreateGame() {
        CreateGameDTO dto = CreateGameDTO.builder()
            .nome("Novo Jogo")
            .maxJogadores(5)
            .build();
        
        ResponseEntity<GameResponseDTO> response = restTemplate
            .withBasicAuth("mestre@test.com", "password")
            .postForEntity("/api/jogos", dto, GameResponseDTO.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(gameRepository.findAll()).hasSize(1);
    }
    
    @Test
    @DisplayName("Jogador não deve conseguir criar jogo")
    @WithMockUser(username = "jogador@test.com", roles = "JOGADOR")
    void jogadorShouldNotCreateGame() {
        CreateGameDTO dto = CreateGameDTO.builder()
            .nome("Novo Jogo")
            .build();
        
        ResponseEntity<ErrorResponse> response = restTemplate
            .withBasicAuth("jogador@test.com", "password")
            .postForEntity("/api/jogos", dto, ErrorResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
```

## Testes Unitários (Service)

Use apenas para lógica complexa isolada:

```java
/**
 * Testes unitários para lógica de negócio complexa.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    
    @Mock
    private GameRepository gameRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private GameService gameService;
    
    @Test
    @DisplayName("Deve lançar exception se usuário não é mestre")
    void shouldThrowExceptionIfNotMestre() {
        // Arrange
        User jogador = User.builder()
            .role(UserRole.JOGADOR)
            .build();
        
        Game game = Game.builder().build();
        
        // Act & Assert
        assertThatThrownBy(() -> gameService.create(game, jogador))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("Apenas mestres");
    }
    
    @Test
    @DisplayName("Deve validar nome duplicado")
    void shouldValidateDuplicateName() {
        // Arrange
        User mestre = User.builder()
            .id(1L)
            .role(UserRole.MESTRE)
            .build();
        
        Game game = Game.builder()
            .nome("Jogo Existente")
            .build();
        
        when(gameRepository.existsByNomeAndMestreId("Jogo Existente", 1L))
            .thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> gameService.create(game, mestre))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("já possui um jogo");
    }
    
    @Test
    @DisplayName("Deve criar jogo com sucesso")
    void shouldCreateGameSuccessfully() {
        // Arrange
        User mestre = User.builder()
            .id(1L)
            .role(UserRole.MESTRE)
            .build();
        
        Game game = Game.builder()
            .nome("Novo Jogo")
            .maxJogadores(5)
            .build();
        
        when(gameRepository.existsByNomeAndMestreId(anyString(), anyLong()))
            .thenReturn(false);
        when(gameRepository.save(any(Game.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Game created = gameService.create(game, mestre);
        
        // Assert
        assertThat(created.getMestre()).isEqualTo(mestre);
        verify(gameRepository).save(game);
    }
}
```

## Padrão Arrange-Act-Assert

```java
@Test
void testMethod() {
    // Arrange - Prepara dados e mocks
    MyEntity entity = MyEntity.builder()
        .name("Test")
        .build();
    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    
    // Act - Executa a ação
    MyEntity result = service.findById(1L);
    
    // Assert - Verifica resultado
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test");
    verify(repository).findById(1L);
}
```

## AssertJ Best Practices

```java
// Assertions fluentes
assertThat(list)
    .isNotNull()
    .hasSize(3)
    .extracting(MyEntity::getName)
    .containsExactly("A", "B", "C");

// Exceptions
assertThatThrownBy(() -> service.findById(999L))
    .isInstanceOf(ResourceNotFoundException.class)
    .hasMessageContaining("não encontrado");

// Optional
assertThat(optional)
    .isPresent()
    .get()
    .extracting(MyEntity::getName)
    .isEqualTo("Test");
```

## Test Data Builders

```java
public class GameTestBuilder {
    private String nome = "Test Game";
    private Integer maxJogadores = 5;
    private User mestre;
    
    public static GameTestBuilder aGame() {
        return new GameTestBuilder();
    }
    
    public GameTestBuilder withNome(String nome) {
        this.nome = nome;
        return this;
    }
    
    public GameTestBuilder withMestre(User mestre) {
        this.mestre = mestre;
        return this;
    }
    
    public Game build() {
        return Game.builder()
            .nome(nome)
            .maxJogadores(maxJogadores)
            .mestre(mestre)
            .build();
    }
}

// Uso
Game game = aGame()
    .withNome("Custom Game")
    .withMestre(mestre)
    .build();
```

## Best Practices

### ✅ DO
- PREFIRA testes de integração (80%)
- Use @DisplayName descritivo
- Padrão Arrange-Act-Assert
- Limpe dados entre testes
- Verifique persistência no banco
- Teste casos de sucesso E erro
- Use AssertJ
- Test data builders

### ❌ DON'T
- Testes unitários para tudo
- Nomes genéricos (test1, test2)
- Testes sem assertions
- Dados compartilhados entre testes
- Ignorar verificação de persistência
- Só testar happy path
