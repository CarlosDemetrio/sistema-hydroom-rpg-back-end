# H2 Database - Testing Best Practices

## 🎯 Objetivo

Usar H2 in-memory database para testes de integração rápidos e confiáveis.

## 📋 Versão

- **H2**: Vem com Spring Boot 4.0.2
- **Spring Boot Test**: 4.0.2
- **JUnit 5**: Jupiter 5.10.x

## ✅ Configuração

### 1. Perfil de Test (`application-test.properties`)

```properties
# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.clean-disabled=false
spring.flyway.locations=classpath:db/migration
```

### 2. Estrutura de Teste

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Meu Repositório - Testes de Integração")
class MeuRepositoryTest {
    
    @Autowired
    private MeuRepository repository;
    
    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Deve salvar entidade com sucesso")
    void testSalvar() {
        // Arrange
        MinhaEntidade entidade = new MinhaEntidade();
        entidade.setNome("Teste");
        
        // Act
        MinhaEntidade salva = repository.save(entidade);
        
        // Assert
        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getNome()).isEqualTo("Teste");
    }
}
```

## 🚀 Padrões de Teste

### 1. Testes de Repository

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {
    
    @Autowired
    private UsuarioRepository repository;
    
    @Test
    void testBuscarPorEmail() {
        // Arrange
        Usuario usuario = Usuario.builder()
            .nome("João")
            .email("joao@test.com")
            .build();
        repository.save(usuario);
        
        // Act
        Optional<Usuario> resultado = repository.findByEmail("joao@test.com");
        
        // Assert
        assertThat(resultado)
            .isPresent()
            .get()
            .extracting(Usuario::getNome)
            .isEqualTo("João");
    }
}
```

### 2. Testes de Service (com @Transactional)

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FichaServiceTest {
    
    @Autowired
    private FichaService service;
    
    @Autowired
    private FichaRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    void testCriarFicha() {
        // Arrange
        CriarFichaRequest request = new CriarFichaRequest();
        request.setNomePersonagem("Aragorn");
        
        // Act
        FichaResponse response = service.criarFicha(request);
        
        // Assert
        assertThat(response.getId()).isNotNull();
        assertThat(response.getNomePersonagem()).isEqualTo("Aragorn");
        
        // Verify persistence
        Optional<Ficha> fichaDb = repository.findById(response.getId());
        assertThat(fichaDb).isPresent();
    }
}
```

### 3. Testes de Controller (MockMvc)

```java
@WebMvcTest(FichaController.class)
@ActiveProfiles("test")
class FichaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FichaService service;
    
    @Test
    void testCriarFicha() throws Exception {
        // Arrange
        FichaResponse response = new FichaResponse();
        response.setId(1L);
        response.setNomePersonagem("Legolas");
        
        when(service.criarFicha(any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/fichas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nomePersonagem": "Legolas"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nomePersonagem").value("Legolas"));
    }
}
```

### 4. Testes de Integração Completa

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class FichaIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testFluxoCompleto() {
        // 1. Criar ficha
        CriarFichaRequest request = new CriarFichaRequest();
        request.setNomePersonagem("Gimli");
        
        ResponseEntity<FichaResponse> createResponse = restTemplate
            .postForEntity("/api/fichas", request, FichaResponse.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long fichaId = createResponse.getBody().getId();
        
        // 2. Buscar ficha
        ResponseEntity<FichaResponse> getResponse = restTemplate
            .getForEntity("/api/fichas/" + fichaId, FichaResponse.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getNomePersonagem()).isEqualTo("Gimli");
    }
}
```

## 🔧 Troubleshooting

### Problema: Flyway não está criando tabelas

**Solução**: Verificar que `spring.flyway.enabled=true` no application-test.properties

### Problema: Dados persistem entre testes

**Solução**: Usar `@Transactional` na classe ou `@BeforeEach` com `deleteAll()`

### Problema: Hibernate cria schema mas Flyway falha

**Solução**: Usar `spring.jpa.hibernate.ddl-auto=none` e confiar apenas no Flyway

## 📊 Assertions Recomendadas

### AssertJ (preferido)

```java
import static org.assertj.core.api.Assertions.*;

// Entity
assertThat(usuario)
    .isNotNull()
    .extracting(Usuario::getNome, Usuario::getEmail)
    .containsExactly("João", "joao@test.com");

// Collection
assertThat(usuarios)
    .hasSize(3)
    .extracting(Usuario::getNome)
    .containsExactlyInAnyOrder("João", "Maria", "Pedro");

// Optional
assertThat(optional)
    .isPresent()
    .get()
    .isEqualTo(esperado);
```

### JUnit 5 Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

assertAll(
    () -> assertNotNull(usuario.getId()),
    () -> assertEquals("João", usuario.getNome()),
    () -> assertTrue(usuario.isAtivo())
);
```

## ⚡ Performance

### 1. Reusar ApplicationContext

```java
// ✅ BOM - Mesmo contexto para todos os testes
@SpringBootTest
@ActiveProfiles("test")
class Suite1Test { }

@SpringBootTest
@ActiveProfiles("test")
class Suite2Test { }
```

### 2. Evitar Context Reload

```java
// ❌ RUIM - Cada teste recarrega contexto
@SpringBootTest(properties = "custom.value=1")
class Test1 { }

@SpringBootTest(properties = "custom.value=2")
class Test2 { }

// ✅ BOM - Usar @TestPropertySource ou perfis
@SpringBootTest
@TestPropertySource(properties = "custom.value=1")
class Test1 { }
```

## 🎯 Convenções de Nomenclatura

```java
// Classe
{Entity}RepositoryTest
{Entity}ServiceTest
{Entity}ControllerTest
{Entity}IntegrationTest

// Métodos
test{Acao}()
test{Acao}_{Cenario}()
test{Acao}_{Cenario}_{ResultadoEsperado}()

// Exemplos:
testSalvar()
testBuscar_QuandoIdNaoExiste()
testBuscar_QuandoIdNaoExiste_DeveLancarException()
```

## 📖 Referências

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/4.0.2/reference/html/features.html#features.testing)
- [H2 Database](http://www.h2database.com/)
- [AssertJ](https://assertj.github.io/doc/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)

---

**Última atualização:** 2026-02-01
