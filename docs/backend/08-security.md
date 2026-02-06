# Security Configuration

## ⚠️ CRÍTICO: OAuth2 com Session, NÃO JWT!

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final OAuth2SuccessHandler successHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("MESTRE")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/login?error");
                })
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

## OAuth2 Success Handler

```java
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserService userService;
    
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String nome = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        
        // Busca ou cria usuário
        User user = userService.findByEmailOrCreate(email, nome, picture);
        
        // Redireciona para frontend
        String redirectUrl = "http://localhost:4200/auth/callback?success=true";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
```

## NUNCA Use

```java
// ❌ ERRADO - JWT Resource Server
http.oauth2ResourceServer(oauth2 -> 
    oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
);

// ❌ ERRADO - Stateless
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
);

// ❌ ERRADO - JWT no Authorization header
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Security Service

```java
@Service
@RequiredArgsConstructor
public class SecurityService {
    
    /**
     * Retorna usuário atualmente autenticado.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("Usuário não autenticado");
        }
        
        OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
        String email = oauth2User.getAttribute("email");
        
        return userService.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
    
    /**
     * Verifica se usuário tem role específica.
     */
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user.getRole().name().equals(role);
    }
    
    /**
     * Verifica se usuário é mestre.
     */
    public boolean isMestre() {
        return hasRole("MESTRE");
    }
    
    /**
     * Verifica se usuário pode editar recurso.
     */
    public boolean canEdit(Long resourceOwnerId) {
        User current = getCurrentUser();
        return current.getId().equals(resourceOwnerId);
    }
}
```

## Method Security

```java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
    // Habilita @PreAuthorize, @PostAuthorize, etc.
}
```

```java
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
public class GameController {
    
    @PreAuthorize("hasRole('MESTRE')")
    @PostMapping
    public ResponseEntity<GameDTO> create(@Valid @RequestBody CreateGameDTO dto) {
        // Apenas mestres podem criar
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

## CORS Configuration

### Development
```properties
# application-dev.properties
cors.allowed-origins=http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allow-credentials=true
```

### Production
```properties
# application-prod.properties
cors.allowed-origins=https://your-domain.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allow-credentials=true
```

## Session Configuration

```properties
# Session timeout (30 minutos)
server.servlet.session.timeout=30m

# Cookie settings
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=lax
```

## OAuth2 Configuration

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google
```

## Best Practices

### ✅ DO
- Use session-based auth
- HttpOnly cookies
- CORS properly configured
- @PreAuthorize para permissões
- SecurityService para lógica de auth
- Secure session cookies (production)

### ❌ DON'T
- JWT tokens para auth
- OAuth2 Resource Server
- Stateless sessions
- Credentials no código
- CORS aberto (*) em prod
- Session cookies inseguros
