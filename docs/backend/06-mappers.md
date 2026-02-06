# Mappers

## Por que Mapper na Controller?

```
❌ ERRADO: Service → Mapper → DTO
✅ CORRETO: Controller → Mapper → Entity → Service
```

**Razões:**
1. Service trabalha com domain (entities)
2. Controller trabalha com API (DTOs)
3. Facilita testes de integração
4. Facilita testes unitários do service
5. Separação clara de responsabilidades

## Template Base

```java
/**
 * Mapper para conversão entre MyEntity e DTOs.
 * Centraliza transformação de dados.
 * 
 * @author Seu Nome
 * @since 1.0
 */
@Component
public class MyMapper {
    
    /**
     * Converte Entity para Response DTO.
     */
    public MyResponseDTO toResponseDTO(MyEntity entity) {
        return MyResponseDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    /**
     * Converte lista de Entities para Response DTOs.
     */
    public List<MyResponseDTO> toResponseDTOList(List<MyEntity> entities) {
        return entities.stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Converte Create DTO para Entity.
     */
    public MyEntity toEntity(MyCreateDTO dto) {
        return MyEntity.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .build();
    }
    
    /**
     * Atualiza Entity com dados do Update DTO.
     */
    public void updateEntityFromDTO(MyEntity entity, MyUpdateDTO dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        // NÃO atualizar: id, createdAt, etc.
    }
}
```

## Uso na Controller

```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class MyController {
    private final MyService service;
    private final MyMapper mapper;
    
    @PostMapping
    public ResponseEntity<MyResponseDTO> create(@Valid @RequestBody MyCreateDTO dto) {
        // 1. Mapper: DTO → Entity
        MyEntity entity = mapper.toEntity(dto);
        
        // 2. Service processa (domain logic)
        MyEntity created = service.create(entity);
        
        // 3. Mapper: Entity → DTO
        MyResponseDTO response = mapper.toResponseDTO(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MyResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MyUpdateDTO dto) {
        // 1. Busca entity existente
        MyEntity entity = service.findById(id);
        
        // 2. Mapper atualiza entity
        mapper.updateEntityFromDTO(entity, dto);
        
        // 3. Service persiste
        MyEntity updated = service.update(id, entity);
        
        // 4. Mapper: Entity → DTO
        MyResponseDTO response = mapper.toResponseDTO(updated);
        
        return ResponseEntity.ok(response);
    }
}
```

## Mappers com Relacionamentos

```java
@Component
@RequiredArgsConstructor
public class GameMapper {
    private final UserMapper userMapper;
    
    public GameResponseDTO toResponseDTO(Game game) {
        return GameResponseDTO.builder()
            .id(game.getId())
            .nome(game.getNome())
            .mestre(userMapper.toSimpleDTO(game.getMestre()))
            .participantesCount(game.getParticipantes().size())
            .build();
    }
    
    public GameDetailDTO toDetailDTO(Game game) {
        return GameDetailDTO.builder()
            .id(game.getId())
            .nome(game.getNome())
            .descricao(game.getDescricao())
            .mestre(userMapper.toResponseDTO(game.getMestre()))
            .participantes(game.getParticipantes().stream()
                .map(p -> participanteMapper.toResponseDTO(p))
                .toList())
            .build();
    }
}
```

## Evitar Campos Sensíveis

```java
@Component
public class UserMapper {
    
    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .nome(user.getNome())
            .email(user.getEmail())
            .role(user.getRole())
            // ✅ SEM campos sensíveis
            .build();
    }
    
    // ❌ NUNCA exponha:
    // - password
    // - tokens
    // - dados internos do sistema
}
```

## MapStruct (Alternativa)

Para projetos grandes, considere MapStruct:

```java
@Mapper(componentModel = "spring")
public interface MyMapper {
    MyResponseDTO toResponseDTO(MyEntity entity);
    List<MyResponseDTO> toResponseDTOList(List<MyEntity> entities);
    MyEntity toEntity(MyCreateDTO dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(MyUpdateDTO dto, @MappingTarget MyEntity entity);
}
```

## Best Practices

### ✅ DO
- Mappers na controller
- Um mapper por entity
- JavaDoc completo
- Métodos: toResponseDTO, toEntity, updateEntityFromDTO
- Injetar outros mappers se necessário
- NUNCA exponha campos sensíveis

### ❌ DON'T
- Mappers no service
- Lógica de negócio no mapper
- Expor entities diretamente
- Campos sensíveis em response DTOs
- Mappers genéricos demais

## Testando Mappers

```java
class MyMapperTest {
    private MyMapper mapper = new MyMapper();
    
    @Test
    void shouldMapEntityToResponseDTO() {
        // Arrange
        MyEntity entity = MyEntity.builder()
            .id(1L)
            .name("Test")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Act
        MyResponseDTO dto = mapper.toResponseDTO(entity);
        
        // Assert
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test");
        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
```
