# 📝 Sistema de Mensagens de Validação

## Visão Geral

Este projeto utiliza um sistema centralizado de mensagens de validação através da classe `ValidationMessages`. Todas as mensagens de erro, validações e limites de campos estão definidos em um único lugar para facilitar manutenção, consistência e futura internacionalização.

## Localização

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/constants/ValidationMessages.java
```

## Estrutura

A classe `ValidationMessages` é organizada em classes internas estáticas para cada domínio:

### 1. **ValidationMessages.Usuario**
Mensagens relacionadas à entidade Usuario

```java
ValidationMessages.Usuario.EMAIL_OBRIGATORIO
ValidationMessages.Usuario.EMAIL_INVALIDO
ValidationMessages.Usuario.EMAIL_TAMANHO
ValidationMessages.Usuario.NOME_OBRIGATORIO
// ... etc
```

### 2. **ValidationMessages.Ficha**
Mensagens relacionadas à entidade Ficha

```java
ValidationMessages.Ficha.NOME_PERSONAGEM_OBRIGATORIO
ValidationMessages.Ficha.CLASSE_OBRIGATORIA
ValidationMessages.Ficha.NIVEL_MINIMO
// ... etc
```

### 3. **ValidationMessages.Jogo**
Mensagens relacionadas à entidade Jogo (para implementação futura)

```java
ValidationMessages.Jogo.NOME_OBRIGATORIO
ValidationMessages.Jogo.SISTEMA_OBRIGATORIO
// ... etc
```

### 4. **ValidationMessages.Seguranca**
Mensagens relacionadas à segurança e autenticação

```java
ValidationMessages.Seguranca.NAO_AUTENTICADO
ValidationMessages.Seguranca.ACESSO_NEGADO
ValidationMessages.Seguranca.APENAS_MESTRE
// ... etc
```

### 5. **ValidationMessages.Erro**
Mensagens genéricas de erro

```java
ValidationMessages.Erro.INTERNO
ValidationMessages.Erro.NAO_ENCONTRADO
ValidationMessages.Erro.DADOS_INVALIDOS
// ... etc
```

### 6. **ValidationMessages.Formato**
Expressões regulares para validação de formato

```java
ValidationMessages.Formato.EMAIL_REGEX
ValidationMessages.Formato.NOME_PERSONAGEM_REGEX
ValidationMessages.Formato.TELEFONE_REGEX
// ... etc
```

### 7. **ValidationMessages.Limites**
Constantes numéricas para limites de tamanho

```java
ValidationMessages.Limites.USUARIO_EMAIL_MAX  // 255
ValidationMessages.Limites.FICHA_NOME_MIN     // 3
ValidationMessages.Limites.FICHA_NIVEL_MAX    // 20
// ... etc
```

## Como Usar

### Em Entidades JPA

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @NotBlank(message = ValidationMessages.Usuario.EMAIL_OBRIGATORIO)
    @Email(message = ValidationMessages.Usuario.EMAIL_INVALIDO)
    @Size(max = ValidationMessages.Limites.USUARIO_EMAIL_MAX, 
          message = ValidationMessages.Usuario.EMAIL_TAMANHO)
    @Column(nullable = false, unique = true, 
            length = ValidationMessages.Limites.USUARIO_EMAIL_MAX)
    private String email;
    
    // ... outros campos
}
```

### Em DTOs

```java
@Data
public class UsuarioDTO {
    
    @NotBlank(message = ValidationMessages.Usuario.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.USUARIO_NOME_MIN,
          max = ValidationMessages.Limites.USUARIO_NOME_MAX,
          message = ValidationMessages.Usuario.NOME_TAMANHO)
    private String nome;
    
    // ... outros campos
}
```

### Em Controllers (Exception Handling)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(ValidationMessages.Seguranca.ACESSO_NEGADO));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ValidationMessages.Erro.NAO_ENCONTRADO));
    }
}
```

### Em Services

```java
@Service
public class UsuarioService {
    
    public void validarPermissao(Usuario usuario) {
        if (!usuario.isAtivo()) {
            throw new BusinessException(ValidationMessages.Seguranca.ACESSO_NEGADO);
        }
    }
}
```

### Validação de Formato (Regex)

```java
@Entity
public class Ficha {
    
    @Pattern(regexp = ValidationMessages.Formato.NOME_PERSONAGEM_REGEX,
             message = ValidationMessages.Ficha.NOME_PERSONAGEM_CARACTERES)
    private String nomePersonagem;
}
```

## Benefícios

### 1. **Centralização**
- Todas as mensagens em um único lugar
- Fácil de encontrar e modificar
- Evita duplicação de strings

### 2. **Consistência**
- Mensagens padronizadas em todo o sistema
- Mesma terminologia em toda a aplicação
- Experiência de usuário uniforme

### 3. **Manutenibilidade**
- Alteração em uma mensagem reflete em todo o sistema
- Fácil de adicionar novas mensagens
- Refatoração simplificada

### 4. **Internacionalização (i18n)**
- Base pronta para adicionar múltiplos idiomas
- Estrutura organizada para tradução
- Pode ser facilmente integrado com `MessageSource` do Spring

### 5. **Type Safety**
- Erro de compilação se referência inválida
- Autocomplete na IDE
- Refatoração segura

### 6. **Documentação Implícita**
- Mensagens servem como documentação
- Limites claramente definidos
- Regras de negócio explícitas

## Adicionando Novas Mensagens

### Passo 1: Adicionar a constante

```java
public static final class Jogo {
    public static final String STATUS_INVALIDO = "Status do jogo inválido";
    
    private Jogo() {}
}
```

### Passo 2: Adicionar limite se necessário

```java
public static final class Limites {
    public static final int JOGO_TITULO_MAX = 200;
    
    private Limites() {}
}
```

### Passo 3: Usar na entidade/DTO

```java
@Size(max = ValidationMessages.Limites.JOGO_TITULO_MAX,
      message = ValidationMessages.Jogo.TITULO_TAMANHO)
private String titulo;
```

## Internacionalização Futura

Para implementar i18n no futuro, a estrutura já está pronta:

```java
// messages_pt_BR.properties
usuario.email.obrigatorio=Email é obrigatório
usuario.email.invalido=Email deve ser válido

// messages_en_US.properties
usuario.email.obrigatorio=Email is required
usuario.email.invalido=Email must be valid

// Em ValidationMessages.java
public static final String EMAIL_OBRIGATORIO = "usuario.email.obrigatorio";
```

E configurar `MessageSource`:

```java
@Configuration
public class I18nConfig {
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
```

## Checklist para Novos Desenvolvedores

Ao criar novas entidades ou DTOs:

- [ ] Adicionar mensagens em `ValidationMessages`
- [ ] Definir limites em `ValidationMessages.Limites`
- [ ] Usar constantes nas anotações `@NotBlank`, `@Size`, etc
- [ ] Adicionar regex em `ValidationMessages.Formato` se necessário
- [ ] Documentar regras especiais como comentários
- [ ] Usar mensagens em exception handlers
- [ ] Testar validações com casos inválidos

## Exemplos Completos

### Entidade com Todas as Validações

```java
@Entity
@Table(name = "jogos")
public class Jogo {
    
    @NotBlank(message = ValidationMessages.Jogo.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.JOGO_NOME_MIN,
          max = ValidationMessages.Limites.JOGO_NOME_MAX,
          message = ValidationMessages.Jogo.NOME_TAMANHO)
    @Column(nullable = false, length = ValidationMessages.Limites.JOGO_NOME_MAX)
    private String nome;
    
    @NotBlank(message = ValidationMessages.Jogo.SISTEMA_OBRIGATORIO)
    @Size(max = ValidationMessages.Limites.JOGO_SISTEMA_MAX,
          message = ValidationMessages.Jogo.SISTEMA_TAMANHO)
    private String sistema;
    
    @Min(value = ValidationMessages.Limites.JOGO_MAX_JOGADORES_MIN,
         message = ValidationMessages.Jogo.MAX_JOGADORES_MINIMO)
    @Max(value = ValidationMessages.Limites.JOGO_MAX_JOGADORES_MAX,
         message = ValidationMessages.Jogo.MAX_JOGADORES_MAXIMO)
    private Integer maxJogadores;
}
```

### Exception Handler Completo

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest()
            .body(new ValidationErrorResponse(errors));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ValidationMessages.Erro.NAO_ENCONTRADO));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(ValidationMessages.Seguranca.ACESSO_NEGADO));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro não tratado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ValidationMessages.Erro.INTERNO));
    }
}
```

## Convenções

1. **Nomenclatura**: Use UPPER_SNAKE_CASE para constantes
2. **Organização**: Agrupe mensagens por domínio (Usuario, Ficha, etc)
3. **Clareza**: Mensagens devem ser claras e acionáveis
4. **Consistência**: Use terminologia uniforme
5. **Completude**: Sempre forneça context sobre o que está errado e como corrigir

## Referências

- [Bean Validation (JSR 380)](https://beanvalidation.org/)
- [Spring Boot Validation](https://spring.io/guides/gs/validating-form-input/)
- [Java Constants Best Practices](https://www.baeldung.com/java-constants-good-practices)
