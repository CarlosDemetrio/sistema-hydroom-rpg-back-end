# OWASP Security Guidelines

## 🛡️ OWASP Top 10 - 2021

### A01:2021 - Broken Access Control

#### ✅ Implementações
```java
/**
 * Verificação de permissões em cada endpoint.
 */
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
public class GameController {
    private final SecurityService securityService;
    private final GameService gameService;
    
    @PreAuthorize("hasRole('MESTRE')")
    @PostMapping
    public ResponseEntity<GameDTO> create(@Valid @RequestBody CreateGameDTO dto) {
        User currentUser = securityService.getCurrentUser();
        Game game = gameService.create(game, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(game));
    }
    
    @PreAuthorize("@securityService.canEdit(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<GameDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGameDTO dto) {
        // Apenas o dono pode editar
    }
}
```

#### ✅ Best Practices
- SEMPRE use `@PreAuthorize` ou `@PostAuthorize`
- Valide permissões no service também (defense in depth)
- NUNCA confie apenas em IDs de URL
- Use SecurityService para verificações centralizadas

#### ❌ Vulnerabilidades
```java
// ❌ ERRADO - Sem verificação de permissão
@PutMapping("/{id}")
public ResponseEntity<GameDTO> update(@PathVariable Long id) {
    // Qualquer usuário pode editar qualquer jogo!
}

// ❌ ERRADO - Confia apenas no ID passado
Game game = gameService.findById(requestDTO.getGameId());
// Atacante pode passar ID de jogo de outra pessoa
```

---

### A02:2021 - Cryptographic Failures

#### ✅ Implementações
```java
/**
 * Configuração de HTTPS/TLS.
 */
// application.properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

// Força HTTPS
server.require-ssl=true
```

```java
/**
 * Senhas NUNCA em plain text.
 */
@Entity
public class User {
    // ❌ ERRADO
    private String password;
    
    // ✅ CORRETO - Usar OAuth2
    // Neste projeto: Google OAuth2 (sem senha local)
}
```

#### ✅ Best Practices
- Use HTTPS em produção (SEMPRE)
- OAuth2 para autenticação (não gerencie senhas)
- Secrets em variáveis de ambiente
- Use Spring Security para encryption

#### ❌ Vulnerabilidades
```java
// ❌ ERRADO - Senha hardcoded
private static final String API_KEY = "abc123456";

// ❌ ERRADO - Dados sensíveis no log
log.info("User logged in: {}", user.getPassword());

// ❌ ERRADO - HTTP em produção
# application-prod.properties
server.ssl.enabled=false
```

---

### A03:2021 - Injection

#### ✅ Implementações (SQL Injection)
```java
/**
 * USE JPA/JPQL - proteção automática.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // ✅ CORRETO - Query method (safe)
    List<Game> findByNomeContaining(String nome);
    
    // ✅ CORRETO - JPQL com parâmetros
    @Query("SELECT g FROM Game g WHERE g.nome LIKE %:nome%")
    List<Game> searchByName(@Param("nome") String nome);
}
```

#### ✅ Implementações (Command Injection)
```java
/**
 * NUNCA execute comandos do sistema.
 */
// ❌ ERRADO
Runtime.getRuntime().exec("rm -rf " + userInput);

// ✅ CORRETO - Use bibliotecas Java
Path path = Paths.get(basePath, sanitizedFileName);
Files.delete(path);
```

#### ❌ Vulnerabilidades
```java
// ❌ ERRADO - SQL concatenado
String query = "SELECT * FROM users WHERE name = '" + userInput + "'";
// userInput = "'; DROP TABLE users; --"

// ❌ ERRADO - Native query sem validação
@Query(value = "SELECT * FROM games WHERE name = " + name, nativeQuery = true)
List<Game> findByNameUnsafe(String name);
```

---

### A04:2021 - Insecure Design

#### ✅ Implementações
```java
/**
 * Rate limiting para prevenir abuse.
 */
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(10.0); // 10 requests/segundo
    }
}

@RestController
@RequiredArgsConstructor
public class GameController {
    private final RateLimiter rateLimiter;
    
    @PostMapping
    public ResponseEntity<GameDTO> create(@Valid @RequestBody CreateGameDTO dto) {
        if (!rateLimiter.tryAcquire()) {
            throw new TooManyRequestsException("Rate limit excedido");
        }
        // Processa request
    }
}
```

```java
/**
 * Validação de business rules.
 */
@Service
public class GameService {
    
    private static final int MAX_GAMES_PER_USER = 10;
    
    @Transactional
    public Game create(Game game, User user) {
        // Validação de limites
        long userGames = gameRepository.countByMestreId(user.getId());
        if (userGames >= MAX_GAMES_PER_USER) {
            throw new BusinessRuleException(
                "Limite de " + MAX_GAMES_PER_USER + " jogos por usuário atingido"
            );
        }
        
        return gameRepository.save(game);
    }
}
```

---

### A05:2021 - Security Misconfiguration

#### ✅ Implementações
```properties
# application-prod.properties

# Desabilita endpoints sensíveis
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true

# Oculta informações do Spring Boot
server.error.include-stacktrace=never
server.error.include-message=never
server.error.include-binding-errors=never

# Headers de segurança
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# Desabilita banner
spring.main.banner-mode=off
```

```java
/**
 * Security headers.
 */
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'")
            )
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .frameOptions(frame -> frame.deny())
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
            )
        );
        return http.build();
    }
}
```

#### ❌ Vulnerabilidades
```properties
# ❌ ERRADO
spring.jpa.show-sql=true  # em produção
management.endpoints.web.exposure.include=*
server.error.include-stacktrace=always
```

---

### A06:2021 - Vulnerable and Outdated Components

#### ✅ Best Practices
```xml
<!-- pom.xml -->
<!-- SEMPRE use versões atualizadas -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version> <!-- Versão atual -->
</parent>

<!-- Use Maven Enforcer Plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireMavenVersion>
                        <version>3.8.0</version>
                    </requireMavenVersion>
                    <requireJavaVersion>
                        <version>21</version>
                    </requireJavaVersion>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Checklist
- ✅ Use `mvn versions:display-dependency-updates` regularmente
- ✅ Configure Dependabot no GitHub
- ✅ Monitore CVEs com OWASP Dependency Check
- ✅ Atualize dependencies a cada 3 meses

---

### A07:2021 - Identification and Authentication Failures

#### ✅ Implementações
```java
/**
 * Session timeout configurado.
 */
// application.properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.max-age=1800

/**
 * OAuth2 com Google (implementado).
 */
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
                .maximumSessions(1)  // 1 sessão por usuário
                .maxSessionsPreventsLogin(false)
            );
        return http.build();
    }
}
```

#### ❌ Vulnerabilidades
```java
// ❌ ERRADO - Session sem timeout
server.servlet.session.timeout=-1

// ❌ ERRADO - Múltiplas sessões ilimitadas
.maximumSessions(-1)

// ❌ ERRADO - Cookie sem HttpOnly
server.servlet.session.cookie.http-only=false
```

---

### A08:2021 - Software and Data Integrity Failures

#### ✅ Implementações
```java
/**
 * Validação de dados de entrada.
 */
@Data
public class CreateGameDTO {
    @NotBlank(message = ValidationMessages.Game.NAME_REQUIRED)
    @Size(max = 100, message = ValidationMessages.Game.NAME_MAX_SIZE)
    @Pattern(
        regexp = "^[a-zA-Z0-9\\s-]+$",
        message = "Nome deve conter apenas letras, números, espaços e hífens"
    )
    private String nome;
}
```

```java
/**
 * Audit trail de mudanças.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;
    
    @LastModifiedBy
    private Long updatedBy;
}
```

---

### A09:2021 - Security Logging and Monitoring Failures

#### ✅ Implementações
```java
/**
 * Logging de ações sensíveis.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {
    
    @Transactional
    public Game create(Game game, User user) {
        log.info("User {} creating game: {}", user.getId(), game.getNome());
        
        Game created = gameRepository.save(game);
        
        log.info("Game created successfully. ID: {}, Name: {}", 
            created.getId(), created.getNome());
        
        return created;
    }
    
    @Transactional
    public void delete(Long id, User user) {
        Game game = findById(id);
        
        if (!game.getMestre().getId().equals(user.getId())) {
            log.warn("SECURITY: User {} attempted to delete game {} owned by {}", 
                user.getId(), id, game.getMestre().getId());
            throw new ForbiddenException("Você não pode deletar este jogo");
        }
        
        log.info("User {} deleting game {}", user.getId(), id);
        gameRepository.deleteById(id);
    }
}
```

```java
/**
 * Handler de exceptions com logging.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {
        
        log.warn("SECURITY: Access denied. User: {}, Endpoint: {}, Message: {}", 
            getCurrentUserId(), 
            request.getRequestURI(), 
            ex.getMessage()
        );
        
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }
}
```

#### Checklist
- ✅ Log tentativas de acesso negado
- ✅ Log mudanças em dados sensíveis
- ✅ Use níveis apropriados (INFO, WARN, ERROR)
- ✅ NUNCA logue senhas ou tokens
- ✅ Configure log rotation

---

### A10:2021 - Server-Side Request Forgery (SSRF)

#### ✅ Implementações
```java
/**
 * Validação de URLs externas.
 */
@Service
public class ExternalApiService {
    
    private static final Set<String> ALLOWED_HOSTS = Set.of(
        "api.google.com",
        "maps.googleapis.com"
    );
    
    public String fetchExternalData(String url) {
        try {
            URL parsedUrl = new URL(url);
            
            // Valida host
            if (!ALLOWED_HOSTS.contains(parsedUrl.getHost())) {
                throw new SecurityException("Host não permitido: " + parsedUrl.getHost());
            }
            
            // Valida protocolo
            if (!parsedUrl.getProtocol().equals("https")) {
                throw new SecurityException("Apenas HTTPS é permitido");
            }
            
            // Busca dados
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
            
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL inválida", e);
        }
    }
}
```

---

## Security Checklist

### Deploy em Produção
- [ ] HTTPS configurado
- [ ] Secrets em variáveis de ambiente
- [ ] CORS restrito
- [ ] Session cookies com HttpOnly e Secure
- [ ] Rate limiting configurado
- [ ] Error messages genéricas (sem stack traces)
- [ ] Logging de ações sensíveis
- [ ] Dependencies atualizadas
- [ ] Security headers configurados
- [ ] SQL Injection prevenido (JPA)
- [ ] XSS prevenido (escape de HTML)
- [ ] CSRF protection habilitado

### Code Review
- [ ] Validação de input (@Valid)
- [ ] Authorization checks (@PreAuthorize)
- [ ] Audit trail (createdBy, updatedBy)
- [ ] Sensitive data não exposto em DTOs
- [ ] Exceptions específicas
- [ ] Logging apropriado

---

## Referências

- [OWASP Top 10 - 2021](https://owasp.org/Top10/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
