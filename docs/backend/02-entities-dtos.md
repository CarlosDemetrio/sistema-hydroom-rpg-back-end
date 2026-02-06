# Entities e DTOs

## 1. Entities

```java
@Entity
@Table(name = "my_entity")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Rules
- ✅ Use Lombok annotations
- ✅ Table names em `snake_case`
- ✅ Audit fields (createdAt, updatedAt)
- ✅ FetchType.LAZY para relationships
- ✅ Constraints no banco (@Column)
- ❌ ZERO lógica de negócio
- ❌ ZERO validações (vão nos DTOs)

# Entities e DTOs (Java 25 + Spring Boot 4)

## 1. Entities

```java
@Entity
@Table(name = "my_entity")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Rules
- ✅ Use Lombok annotations
- ✅ Table names em `snake_case`
- ✅ Audit fields (createdAt, updatedAt)
- ✅ FetchType.LAZY para relationships
- ✅ Constraints no banco (@Column)
- ❌ ZERO lógica de negócio
- ❌ ZERO validações (vão nos DTOs)

## 2. DTOs (Java 25 Records)

### Request DTO - Record Simples
```java
/**
 * DTO para criação de recurso.
 * Record: imutável, conciso, ideal para Request DTOs.
 * 
 * @param name Nome do recurso
 * @param description Descrição detalhada
 */
public record MyCreateDTO(
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(max = 100, message = ValidationMessages.NAME_MAX_SIZE)
    String name,
    
    @NotBlank(message = ValidationMessages.DESCRIPTION_REQUIRED)
    @Size(max = 500, message = ValidationMessages.DESCRIPTION_MAX_SIZE)
    String description
) {
    /**
     * Compact constructor para validações customizadas.
     * Executa APÓS a inicialização dos campos.
     */
    public MyCreateDTO {
        // Validações adicionais de regra de negócio
        if (name != null && name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Nome não pode conter números");
        }
    }
    
    /**
     * Método helper para transformação.
     */
    public String getUpperCaseName() {
        return name.toUpperCase();
    }
}
```

### Request DTO - Record com Validação Complexa
```java
/**
 * DTO para criação de jogo.
 * Demonstra validações Bean Validation + regras customizadas.
 */
public record CreateGameDTO(
    @NotBlank(message = ValidationMessages.Game.NAME_REQUIRED)
    @Size(min = 3, max = 100, message = ValidationMessages.Game.NAME_SIZE)
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "Apenas letras, números, espaços e hífens")
    String nome,
    
    @Min(value = 1, message = ValidationMessages.Game.MAX_PLAYERS_MIN)
    @Max(value = 20, message = ValidationMessages.Game.MAX_PLAYERS_MAX)
    Integer maxJogadores,
    
    @Size(max = 1000, message = ValidationMessages.Game.DESCRIPTION_MAX_SIZE)
    String descricao
) {
    // Compact constructor com validações de negócio
    public CreateGameDTO {
        // Validação: descrição obrigatória se maxJogadores > 10
        if (maxJogadores != null && maxJogadores > 10 && 
            (descricao == null || descricao.isBlank())) {
            throw new IllegalArgumentException(
                "Jogos com mais de 10 jogadores devem ter descrição"
            );
        }
    }
}
```

### Response DTO - Record Simples
```java
/**
 * DTO de resposta simples.
 * Record: perfeito para responses imutáveis.
 */
public record MyResponseDTO(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    // NUNCA exponha campos sensíveis:
    // ❌ password, tokens, dados internos
}
```

### Response DTO - Record com Nested DTOs
```java
/**
 * DTO de resposta com relacionamentos.
 */
public record GameResponseDTO(
    Long id,
    String nome,
    Integer maxJogadores,
    UserSimpleDTO mestre,  // Nested DTO
    Integer participantesCount,
    LocalDateTime createdAt
) {
    /**
     * Factory method para criar com defaults.
     */
    public static GameResponseDTO from(Game game) {
        return new GameResponseDTO(
            game.getId(),
            game.getNome(),
            game.getMaxJogadores(),
            UserSimpleDTO.from(game.getMestre()),
            game.getParticipantes().size(),
            game.getCreatedAt()
        );
    }
}

/**
 * DTO simplificado para evitar circular references.
 */
public record UserSimpleDTO(
    Long id,
    String nome,
    String email
) {
    public static UserSimpleDTO from(User user) {
        return new UserSimpleDTO(
            user.getId(),
            user.getNome(),
            user.getEmail()
        );
    }
}
```

### Update DTO - Atualização Parcial (Java 25 Pattern Matching)
```java
/**
 * DTO para atualização parcial.
 * Campos null = não atualizar.
 */
public record MyUpdateDTO(
    @Size(max = 100, message = ValidationMessages.NAME_MAX_SIZE)
    String name,
    
    @Size(max = 500, message = ValidationMessages.DESCRIPTION_MAX_SIZE)
    String description
) {
    /**
     * Aplica atualizações apenas nos campos não-null.
     * Usa pattern matching do Java 25.
     */
    public void applyTo(MyEntity entity) {
        if (name != null) entity.setName(name);
        if (description != null) entity.setDescription(description);
    }
}
```

### Response DTO - Complexo com Builder Pattern
```java
/**
 * Para DTOs complexos que precisam de construção incremental.
 * Use @Builder quando Record não é suficiente.
 */
@Data
@Builder
public class GameDetailResponseDTO {
    private Long id;
    private String nome;
    private Integer maxJogadores;
    private String descricao;
    private UserSimpleDTO mestre;
    
    @Builder.Default
    private List<ParticipanteDTO> participantes = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Integer> estatisticas = new HashMap<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // NUNCA exponha campos sensíveis
}
```

## 3. Quando usar Record vs Class?

### ✅ Use Record quando:
- DTO de **Request** simples
- DTO de **Response** simples
- Dados **imutáveis**
- Não precisa de herança
- Não precisa de @Builder
- Validação via Bean Validation é suficiente

### ✅ Use Class + @Builder quando:
- DTO com **valores default**
- DTO com **construção incremental**
- DTO com **relacionamentos complexos**
- Precisa de **herança**
- Precisa de **métodos mutáveis**
- DTO de **Response** muito complexo

## 4. Java 25 Features em DTOs

### Sealed Classes (Hierarquia Controlada)
```java
/**
 * Sealed class para hierarquia de comandos.
 */
public sealed interface GameCommand
    permits CreateGameCommand, UpdateGameCommand, DeleteGameCommand {
    Long gameId();
}

public record CreateGameCommand(
    Long gameId,
    String nome,
    Integer maxJogadores
) implements GameCommand {}

public record UpdateGameCommand(
    Long gameId,
    String nome
) implements GameCommand {}

public record DeleteGameCommand(
    Long gameId
) implements GameCommand {}
```

### Pattern Matching em Switch (Java 25)
```java
/**
 * Pattern matching melhorado para processar comandos.
 */
public class GameCommandHandler {
    public void handle(GameCommand command) {
        switch (command) {
            case CreateGameCommand c -> createGame(c.nome(), c.maxJogadores());
            case UpdateGameCommand u -> updateGame(u.gameId(), u.nome());
            case DeleteGameCommand d -> deleteGame(d.gameId());
        }
    }
}
```

### Unnamed Patterns (Java 25)
```java
/**
 * Unnamed patterns para ignorar valores.
 */
public record Coordinate(int x, int y, int z) {
    // Ignora z para cálculo 2D
    public int distanceTo(Coordinate other) {
        return switch (other) {
            case Coordinate(var x, var y, _) -> 
                Math.abs(this.x - x) + Math.abs(this.y - y);
        };
    }
}
```

## 3. ValidationMessages

```java
/**
 * Centralizador de mensagens de validação.
 * Facilita manutenção e internacionalização.
 */
public final class ValidationMessages {
    private ValidationMessages() {}
    
    // Generic
    public static final String REQUIRED = "{validation.required}";
    public static final String INVALID = "{validation.invalid}";
    
    // Name
    public static final String NAME_REQUIRED = "{validation.name.required}";
    public static final String NAME_MAX_SIZE = "{validation.name.maxSize}";
    public static final String NAME_MIN_SIZE = "{validation.name.minSize}";
    
    // Description
    public static final String DESCRIPTION_REQUIRED = "{validation.description.required}";
    public static final String DESCRIPTION_MAX_SIZE = "{validation.description.maxSize}";
    
    // Email
    public static final String EMAIL_INVALID = "{validation.email.invalid}";
    public static final String EMAIL_REQUIRED = "{validation.email.required}";
    
    // Business Rules
    public static final String BUSINESS_RULE_VIOLATED = "{validation.businessRule.violated}";
    public static final String INSUFFICIENT_PERMISSIONS = "{validation.permissions.insufficient}";
}
```

### ValidationMessages.properties
```properties
validation.required=Campo obrigatório
validation.invalid=Valor inválido

validation.name.required=Nome é obrigatório
validation.name.maxSize=Nome deve ter no máximo {max} caracteres
validation.name.minSize=Nome deve ter no mínimo {min} caracteres

validation.description.required=Descrição é obrigatória
validation.description.maxSize=Descrição deve ter no máximo {max} caracteres

validation.email.invalid=Email inválido
validation.email.required=Email é obrigatório

validation.businessRule.violated=Regra de negócio violada
validation.permissions.insufficient=Permissões insuficientes
```

## Bean Validation Annotations

### Comumente Usadas
```java
@NotNull        // Não pode ser null
@NotBlank       // Não pode ser null, vazio ou só espaços (Strings)
@NotEmpty       // Não pode ser null ou vazio (Collections, Arrays, Strings)

@Size(min, max) // Tamanho de String, Collection, Array
@Min(value)     // Valor mínimo (números)
@Max(value)     // Valor máximo (números)

@Email          // Validação de email
@Pattern(regex) // Regex customizado

@Past           // Data no passado
@Future         // Data no futuro

@AssertTrue     // Método de validação customizada
```

## Best Practices

### ✅ DO
- Validações simples nos DTOs
- ValidationMessages centralizado
- DTOs separados (Create, Update, Response)
- @AssertTrue para validações complexas
- Records para DTOs imutáveis simples

### ❌ DON'T
- Mensagens hardcoded
- Expor entities nas APIs
- Validações de negócio complexas em DTOs
- DTOs genéricos para tudo
- Campos sensíveis em response DTOs

## Quando usar Record vs Class

### Use Record
```java
// DTO simples, imutável, sem validações complexas
public record SimpleDTO(
    Long id,
    String name
) {}
```

### Use Class
```java
// DTO com validações, builder, lógica customizada
@Data
@Builder
public class ComplexDTO {
    @NotBlank
    private String name;
    
    @AssertTrue
    public boolean isValid() {
        // Lógica customizada
    }
}
```
