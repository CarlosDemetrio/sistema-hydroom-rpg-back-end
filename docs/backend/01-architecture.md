# Arquitetura e Estrutura

## Layer Structure

```
HTTP Request → Controller → Service → Repository → Database
                    ↓          ↓
                  DTO    ←  Entity
                    ↑          ↑
                Mapper --------+
```

## Dependency Injection

**SEMPRE use constructor injection com Lombok:**

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
    private final OtherService otherService;
}
```

**Rules:**
- ✅ Use `@RequiredArgsConstructor`
- ✅ Fields `final`
- ❌ NUNCA use `@Autowired`
- ❌ NUNCA use constructor manual

## Project Structure

```
src/main/java/br/com/yourapp/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access
├── entity/            # JPA entities
├── dto/               # Data Transfer Objects
│   ├── request/       # Request DTOs
│   └── response/      # Response DTOs
├── mapper/            # Entity ↔ DTO converters
├── exception/         # Custom exceptions
├── config/            # Configuration classes
└── security/          # Security configuration
```

## Responsabilidades por Camada

### Controller Layer
- ✅ Coordena requisições HTTP
- ✅ Valida DTOs (@Valid)
- ✅ Converte Entity ↔ DTO (via Mapper)
- ✅ Retorna ResponseEntity
- ❌ ZERO lógica de negócio
- ❌ ZERO acesso direto ao Repository

### Service Layer
- ✅ TODA lógica de negócio
- ✅ Gerencia transações (@Transactional)
- ✅ Trabalha com Entities
- ✅ Valida regras de negócio
- ✅ Lança exceptions específicas
- ❌ ZERO conhecimento de HTTP/DTOs

### Repository Layer
- ✅ Acesso a dados
- ✅ Query methods
- ✅ Custom queries (@Query)
- ❌ ZERO lógica de negócio

## Fluxo Completo de uma Request

### POST /api/resources

```java
// 1. Controller recebe DTO
@PostMapping
public ResponseEntity<MyResponseDTO> create(@Valid @RequestBody MyCreateDTO dto) {
    // 2. Mapper converte DTO → Entity
    MyEntity entity = mapper.toEntity(dto);
    
    // 3. Service processa (lógica de negócio)
    MyEntity created = service.create(entity);
    
    // 4. Mapper converte Entity → DTO
    MyResponseDTO response = mapper.toResponseDTO(created);
    
    // 5. Retorna ResponseEntity
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// Service
@Transactional
public MyEntity create(MyEntity entity) {
    // Validações de negócio
    if (repository.existsByName(entity.getName())) {
        throw new ConflictException("Nome já existe");
    }
    
    // Persiste
    return repository.save(entity);
}
```

## Best Practices

### ✅ DO
- Controller thin, service fat
- Mappers na controller
- Services trabalham com entities
- Exceptions específicas
- Documentação completa

### ❌ DON'T
- Lógica de negócio na controller
- Mappers no service
- Controllers acessando repository diretamente
- Exceptions genéricas
- Código sem documentação
