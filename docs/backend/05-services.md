# Services

## Template Base

```java
/**
 * Service para gerenciamento de MyEntity.
 * Contém toda a lógica de negócio.
 * 
 * @author Seu Nome
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyService {
    private final MyRepository repository;
    
    public List<MyEntity> findAll() {
        return repository.findAll();
    }
    
    public MyEntity findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MyEntity", id));
    }
    
    public Optional<MyEntity> findByIdOptional(Long id) {
        return repository.findById(id);
    }
    
    @Transactional
    public MyEntity create(MyEntity entity) {
        // Validações de negócio
        validateBusinessRules(entity);
        return repository.save(entity);
    }
    
    @Transactional
    public MyEntity update(Long id, MyEntity entity) {
        MyEntity existing = findById(id);
        updateFields(existing, entity);
        return repository.save(existing);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("MyEntity", id);
        }
        repository.deleteById(id);
    }
    
    private void validateBusinessRules(MyEntity entity) {
        if (repository.existsByName(entity.getName())) {
            throw new ConflictException("Nome já existe: " + entity.getName());
        }
    }
}
```

## @Transactional

### Configuração
```java
@Transactional(readOnly = true)  // Na classe (padrão read-only)
public class MyService {
    
    @Transactional  // Sobrescreve para escrita
    public MyEntity create(MyEntity entity) {
        return repository.save(entity);
    }
}
```

### Rules
- ✅ `readOnly = true` na classe
- ✅ `@Transactional` sem readOnly para escritas
- ✅ Propagation.REQUIRED (padrão)
- ❌ NUNCA esqueça @Transactional em métodos de escrita

## Optional vs Exception

### Use findById (Exception)
```java
// Quando ausência É erro
public MyEntity findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("MyEntity", id));
}

// Uso
MyEntity entity = service.findById(id); // Lança exception se não existir
```

### Use findByIdOptional
```java
// Quando ausência NÃO é erro
public Optional<MyEntity> findByIdOptional(Long id) {
    return repository.findById(id);
}

// Uso
Optional<MyEntity> entity = service.findByIdOptional(id);
entity.ifPresent(e -> processEntity(e));
```

## Validações de Negócio

### No Service (Complexas)
```java
@Transactional
public Game create(Game game, User currentUser) {
    // Validação de permissão
    if (!currentUser.isMestre()) {
        throw new ForbiddenException("Apenas mestres podem criar jogos");
    }
    
    // Validação de duplicação
    if (repository.existsByNomeAndMestreId(game.getNome(), currentUser.getId())) {
        throw new ConflictException("Você já possui um jogo com este nome");
    }
    
    // Validação de regra de negócio
    if (game.getMaxJogadores() < game.getParticipantes().size()) {
        throw new BusinessRuleException("Número de participantes excede máximo permitido");
    }
    
    game.setMestre(currentUser);
    return repository.save(game);
}
```

### No DTO (Simples)
```java
@Data
public class CreateGameDTO {
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    private String nome;
    
    @Min(value = 1, message = ValidationMessages.MIN_PLAYERS)
    private Integer maxJogadores;
    
    @AssertTrue(message = ValidationMessages.NAME_NO_SPECIAL_CHARS)
    public boolean isValidName() {
        return nome != null && nome.matches("^[a-zA-Z0-9\\s]+$");
    }
}
```

## Padrão para Updates

```java
@Transactional
public MyEntity update(Long id, MyEntity updates) {
    MyEntity existing = findById(id);
    
    // Atualiza apenas campos permitidos
    if (updates.getName() != null) {
        existing.setName(updates.getName());
    }
    if (updates.getDescription() != null) {
        existing.setDescription(updates.getDescription());
    }
    // NÃO atualizar: id, createdAt, etc.
    
    return repository.save(existing);
}
```

## Best Practices

### ✅ DO
- TODA lógica de negócio no service
- @Transactional(readOnly = true) na classe
- @Transactional para escritas
- Validações de negócio complexas
- Optional quando ausência não é erro
- Exceptions específicas
- JavaDoc detalhado

### ❌ DON'T
- Lógica de negócio na controller
- Esquecer @Transactional em escritas
- Retornar DTOs do service
- Validações simples (vão no DTO)
- Exception genérica
- Código sem documentação

## Exemplo Completo

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final UserService userService;
    
    /**
     * Lista todos os jogos do usuário.
     */
    public List<Game> findByUser(User user) {
        if (user.isMestre()) {
            return gameRepository.findByMestreId(user.getId());
        }
        return gameRepository.findByParticipanteUserId(user.getId());
    }
    
    /**
     * Busca jogo por ID.
     * 
     * @throws ResourceNotFoundException se não encontrado
     */
    public Game findById(Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", id));
    }
    
    /**
     * Cria novo jogo.
     * 
     * @throws ForbiddenException se usuário não é mestre
     * @throws ConflictException se nome já existe
     */
    @Transactional
    public Game create(Game game, User currentUser) {
        log.info("Criando jogo '{}' para usuário {}", game.getNome(), currentUser.getId());
        
        validateMestre(currentUser);
        validateUniqueName(game.getNome(), currentUser.getId());
        validateBusinessRules(game);
        
        game.setMestre(currentUser);
        Game saved = gameRepository.save(game);
        
        log.info("Jogo criado com sucesso: ID {}", saved.getId());
        return saved;
    }
    
    private void validateMestre(User user) {
        if (!user.isMestre()) {
            throw new ForbiddenException("Apenas mestres podem criar jogos");
        }
    }
    
    private void validateUniqueName(String nome, Long mestreId) {
        if (gameRepository.existsByNomeAndMestreId(nome, mestreId)) {
            throw new ConflictException("Você já possui um jogo com o nome: " + nome);
        }
    }
    
    private void validateBusinessRules(Game game) {
        if (game.getMaxJogadores() < 1) {
            throw new BusinessRuleException("Jogo deve ter no mínimo 1 jogador");
        }
    }
}
```
