# 🚀 Quick Reference - Security Implementation

## Para Desenvolvedores

### 📝 Usando Mensagens de Validação

```java
import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;

// Em Entidades
@NotBlank(message = ValidationMessages.Usuario.EMAIL_OBRIGATORIO)
@Size(max = ValidationMessages.Limites.USUARIO_EMAIL_MAX)
private String email;

// Em Services
throw new BusinessException(ValidationMessages.Seguranca.ACESSO_NEGADO);

// Em Controllers
log.warn("Tentativa de acesso: {}", ValidationMessages.Seguranca.NAO_AUTENTICADO);
```

---

### 🛡️ Exception Handling

```java
// Lançar exceções
throw new ResourceNotFoundException("Usuario", id);
throw new BusinessException("Operação não permitida");
throw new ConflictException("Email já existe");

// São automaticamente capturadas pelo GlobalExceptionHandler
// e convertidas em ErrorResponse ou ValidationErrorResponse
```

---

### ✅ Validando Entidades

```java
@RestController
public class MeuController {
    
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody UsuarioDTO dto) {
        // @Valid dispara validações automáticas
        // Erros são capturados pelo GlobalExceptionHandler
        return ResponseEntity.ok(service.criar(dto));
    }
}
```

---

### 📊 Logging de Segurança

```java
@Slf4j
@RestController
public class MeuController {
    
    @GetMapping("/recurso")
    public ResponseEntity<?> metodo(HttpServletRequest request) {
        log.info("Usuário {} acessou recurso de IP: {}", 
                user.getEmail(), 
                request.getRemoteAddr());
        
        // Sempre logue:
        // - Acessos bem-sucedidos
        // - Tentativas de acesso negado
        // - Alterações de dados sensíveis
        // - Exceções de segurança
    }
}
```

---

### 🔐 CSRF Token (Frontend)

```typescript
// Angular - Automaticamente incluído pelo interceptor
// Apenas certifique-se que o cookie XSRF-TOKEN existe

// Verificar no browser:
// DevTools > Application > Cookies > XSRF-TOKEN
```

---

### 🌐 CORS Headers

```java
// Já configurado! Headers permitidos:
// - Authorization
// - Content-Type
// - Accept
// - X-Requested-With
// - X-XSRF-TOKEN
// - Cache-Control

// Não adicione outros headers sem revisar segurança
```

---

### ⚙️ Configuração por Ambiente

```properties
# Dev (application.properties)
app.frontend.url=http://localhost:4200

# Prod (application-prod.properties)
app.frontend.url=${FRONTEND_URL:https://production.com}

# Usar em Java:
@Value("${app.frontend.url}")
private String frontendUrl;
```

---

### 🧪 Testando Segurança

```bash
# 1. Teste endpoint protegido
curl http://localhost:8080/api/user
# Esperado: 401 Unauthorized

# 2. Teste validação
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid"}'
# Esperado: 400 Bad Request com ValidationErrorResponse

# 3. Verifique headers de segurança
curl -I http://localhost:8080/api/public/health
# Esperado: X-Frame-Options, X-XSS-Protection, etc

# 4. Verifique CSRF token
# Login > DevTools > Cookies > XSRF-TOKEN deve existir
```

---

### 📋 Checklist para Nova Feature

- [ ] Validações adicionadas usando ValidationMessages
- [ ] Exception handling considerado
- [ ] Logs de segurança implementados
- [ ] Testes de autorização
- [ ] Documentação atualizada
- [ ] CORS verificado se novos headers
- [ ] Rate limiting considerado (quando implementado)

---

### ⚠️ NÃO FAZER

```java
// ❌ Não retorne entidades direto
return repository.findById(id).get();

// ✅ Use DTOs
return repository.findById(id)
    .map(mapper::toDTO)
    .orElseThrow(() -> new ResourceNotFoundException("Entity", id));

// ❌ Não use strings hardcoded
throw new Exception("Email é obrigatório");

// ✅ Use ValidationMessages
throw new BusinessException(ValidationMessages.Usuario.EMAIL_OBRIGATORIO);

// ❌ Não exponha stacktraces
} catch (Exception e) {
    return ResponseEntity.ok(e.getMessage());
}

// ✅ Use GlobalExceptionHandler (automático)
// ou lance exceções customizadas

// ❌ Não desabilite CSRF sem motivo
.csrf(AbstractHttpConfigurer::disable)

// ✅ CSRF já está configurado corretamente

// ❌ Não use CORS permissivo
.setAllowedHeaders(List.of("*"))

// ✅ Headers específicos já configurados
```

---

### 🔍 Debug de Segurança

```bash
# Ver logs de segurança
tail -f logs/application.log | grep "Tentativa de acesso"

# Ver configuração de sessão
curl -v http://localhost:8080/api/user

# Verificar cookies
# DevTools > Application > Cookies

# Ver headers de resposta
curl -I http://localhost:8080/api/public/health
```

---

### 📚 Referências Rápidas

- **ValidationMessages:** `constants/ValidationMessages.java`
- **Exceptions:** `exception/`
- **Security Config:** `config/SecurityConfig.java`
- **Logging:** Use `@Slf4j` e `log.info/warn/error`

---

### 🆘 Problemas Comuns

**Q: CSRF Token não aparece**
```
A: Verifique se fez login via OAuth2 primeiro
   Cookie só é criado após autenticação
```

**Q: CORS error no frontend**
```
A: Verifique se origin está em app.cors.allowed-origins
   Verifique se header é um dos permitidos
```

**Q: Validação não funciona**
```
A: Certifique-se de usar @Valid no controller
   Verifique se ValidationMessages está importado
```

**Q: Exception não é capturada**
```
A: Use uma das exceções customizadas:
   - ResourceNotFoundException
   - BusinessException
   - ConflictException
```

---

**Última Atualização:** 31/01/2026  
**Dúvidas?** Consulte `SECURITY_AUDIT_REPORT.md`
