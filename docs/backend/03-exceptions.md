# Exceptions e Error Handling

## Hierarquia de Exceptions

```
BusinessException (abstract)
    ├── ResourceNotFoundException (404)
    ├── BusinessRuleException (422)
    ├── ConflictException (409)
    └── ForbiddenException (403)
```

## 1. BusinessException (Base)

```java
/**
 * Exception base para o domínio da aplicação.
 * Todas as exceptions de negócio devem estender esta classe.
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
    
    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 2. ResourceNotFoundException

```java
/**
 * Lançada quando um recurso não é encontrado.
 * Mapeia para HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s não encontrado(a) com ID: %d", resource, id));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Quando usar:** Recurso específico não existe no banco.

```java
// Exemplo
User user = userRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
```

## 3. BusinessRuleException

```java
/**
 * Lançada quando uma regra de negócio é violada.
 * Mapeia para HTTP 422 Unprocessable Entity.
 */
public class BusinessRuleException extends BusinessException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

**Quando usar:** Violação de regra de negócio da aplicação.

```java
// Exemplos
if (!user.isActive()) {
    throw new BusinessRuleException("Usuário inativo não pode criar jogos");
}

if (participantes.size() > game.getMaxPlayers()) {
    throw new BusinessRuleException("Jogo atingiu limite máximo de jogadores");
}
```

## 4. ConflictException

```java
/**
 * Lançada quando há conflito de dados (ex: duplicação).
 * Mapeia para HTTP 409 Conflict.
 */
public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message);
    }
}
```

**Quando usar:** Conflito de dados únicos ou duplicação.

```java
// Exemplos
if (userRepository.existsByEmail(email)) {
    throw new ConflictException("Email já cadastrado: " + email);
}

if (gameRepository.existsByNomeAndMestreId(nome, mestreId)) {
    throw new ConflictException("Você já possui um jogo com este nome");
}
```

## 5. ForbiddenException

```java
/**
 * Lançada quando o usuário não tem permissão para a ação.
 * Mapeia para HTTP 403 Forbidden.
 */
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(message);
    }
}
```

**Quando usar:** Usuário autenticado mas sem permissão.

```java
// Exemplos
if (!game.getMestre().getId().equals(currentUser.getId())) {
    throw new ForbiddenException("Apenas o mestre pode modificar este jogo");
}

if (!ficha.getParticipante().getUser().getId().equals(currentUser.getId())) {
    throw new ForbiddenException("Você não tem permissão para editar esta ficha");
}
```

## GlobalExceptionHandler

```java
/**
 * Handler global de exceptions da aplicação.
 * Centraliza tratamento de erros e padroniza respostas.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflito de dados: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        log.warn("Erro de validação");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Erro de validação nos campos")
            .fieldErrors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro interno não tratado", ex);
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Erro interno no servidor")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            BusinessException ex, HttpStatus status) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(status).body(error);
    }
}
```

## Error Response DTOs

```java
/**
 * DTO para resposta de erro padrão.
 */
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}

/**
 * DTO para resposta de erro de validação.
 */
@Data
@Builder
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
}
```

## HTTP Status Codes

| Exception                  | Status | Code |
|---------------------------|--------|------|
| ValidationError           | 400    | Bad Request |
| ForbiddenException        | 403    | Forbidden |
| ResourceNotFoundException | 404    | Not Found |
| ConflictException         | 409    | Conflict |
| BusinessRuleException     | 422    | Unprocessable Entity |
| Exception (genérica)      | 500    | Internal Server Error |

## Best Practices

### ✅ DO
- Use exceptions específicas
- Mensagens claras e descritivas
- Log apropriado (warn vs error)
- Padronize respostas de erro
- NUNCA exponha stack traces

### ❌ DON'T
- Usar Exception genérica
- Mensagens técnicas ao usuário
- Expor detalhes internos
- Ignorar exceptions
- Log excessivo (info/debug)

## Exemplo Completo

```java
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    
    public Game findById(Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", id));
    }
    
    @Transactional
    public Game create(Game game, User currentUser) {
        // Validação de permissão
        if (!currentUser.isMestre()) {
            throw new ForbiddenException("Apenas mestres podem criar jogos");
        }
        
        // Validação de conflito
        if (gameRepository.existsByNomeAndMestreId(game.getNome(), currentUser.getId())) {
            throw new ConflictException("Você já possui um jogo com o nome: " + game.getNome());
        }
        
        // Validação de negócio
        if (game.getMaxJogadores() < 1) {
            throw new BusinessRuleException("Jogo deve ter no mínimo 1 jogador");
        }
        
        game.setMestre(currentUser);
        return gameRepository.save(game);
    }
}
```
