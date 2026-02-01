# MapStruct - Best Practices Context

## Core Principles

MapStruct is a **compile-time** code generator for Java bean mappings. It generates type-safe, performant mapper implementations.

## Configuration

### Maven Dependency
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### With Lombok
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## Basic Usage

### 1. Simple Mapper
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDTO toDTO(User user);
    
    User toEntity(UserDTO dto);
    
    List<UserDTO> toDTOList(List<User> users);
}
```

### 2. Field Mapping
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(source = "usuario.nome", target = "nomeJogador")
    @Mapping(source = "jogo.nome", target = "nomeJogo")
    @Mapping(source = "nomePersonagem", target = "personagem")
    FichaResponse toResponse(Ficha ficha);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Ficha toEntity(CriarFichaRequest request);
}
```

### 3. Update Existing Entity
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(AtualizarFichaRequest dto, @MappingTarget Ficha entity);
}
```

## Advanced Features

### 1. Custom Mapping Methods
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(source = "genero", target = "generoString", qualifiedByName = "generoToString")
    FichaDTO toDTO(Ficha ficha);
    
    @Named("generoToString")
    default String generoToString(Genero genero) {
        return genero != null ? genero.name() : null;
    }
}
```

### 2. Using Other Mappers
```java
@Mapper(componentModel = "spring", uses = {EnderecoMapper.class})
public interface UsuarioMapper {
    
    // Automatically uses EnderecoMapper for nested objects
    UsuarioDTO toDTO(Usuario usuario);
}
```

### 3. Collections and Maps
```java
@Mapper(componentModel = "spring")
public interface AtributoMapper {
    
    @Mapping(source = "atributoConfig.nome", target = "nome")
    AtributoDTO toDTO(FichaAtributo atributo);
    
    List<AtributoDTO> toDTOList(List<FichaAtributo> atributos);
    
    Set<AtributoDTO> toDTOSet(Set<FichaAtributo> atributos);
}
```

### 4. Default Values
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "nivel", defaultValue = "1")
    Ficha toEntity(CriarFichaRequest request);
}
```

### 5. Expressions
```java
@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface FichaMapper {
    
    @Mapping(target = "criadoEm", expression = "java(LocalDateTime.now())")
    Ficha toEntity(CriarFichaRequest request);
}
```

### 6. Conditional Mapping
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(target = "compartilhadaComJogadores", source = "compartilhada", 
             conditionExpression = "java(compartilhada != null)")
    Ficha toEntity(FichaDTO dto);
}
```

## Best Practices

### ✅ DO

1. **Use `componentModel = "spring"`** - Makes mapper a Spring bean
2. **Prefer interface over abstract class** - Simpler and cleaner
3. **Use `@MappingTarget`** for updates - Avoids unnecessary object creation
4. **Ignore audit fields** - Let JPA handle them
5. **Use `nullValuePropertyMappingStrategy.IGNORE`** for PATCH operations
6. **Group related mappers** - Use package-level configuration
7. **Use `@Named` for reusable custom methods**
8. **Document complex mappings** with JavaDoc

### ❌ DON'T

1. **DON'T use JPA Converters** - Use MapStruct instead
2. **DON'T map everything** - Only map what's needed for the use case
3. **DON'T create circular dependencies** between mappers
4. **DON'T ignore null checks** - Use appropriate null strategies
5. **DON'T use expressions for simple mappings** - Use standard mapping
6. **DON'T forget to test** - Generated code can have bugs

## Mapping Strategies

### 1. Null Value Strategies
```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface FichaMapper { }
```

### 2. Collection Mapping Strategies
```java
@Mapper(
    componentModel = "spring",
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface FichaMapper { }
```

### 3. Builder Strategy
```java
@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = false)
)
public interface FichaMapper { }
```

## Testing

### Unit Test
```java
@ExtendWith(SpringExtension.class)
@SpringBootTest
class FichaMapperTest {
    
    @Autowired
    private FichaMapper mapper;
    
    @Test
    void shouldMapFichaToDTO() {
        Ficha ficha = new Ficha();
        ficha.setNomePersonagem("Test");
        
        FichaDTO dto = mapper.toDTO(ficha);
        
        assertThat(dto.getNomePersonagem()).isEqualTo("Test");
    }
}
```

## Performance

- **Compile-time generation** - No runtime overhead
- **Type-safe** - Errors caught at compile time
- **No reflection** - Direct field access
- **Optimized** - Efficient code generation

## Common Patterns

### Entity ↔ DTO
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    FichaResponse toResponse(Ficha ficha);
    Ficha toEntity(CriarFichaRequest request);
    void updateFromRequest(AtualizarFichaRequest request, @MappingTarget Ficha ficha);
}
```

### Nested Objects
```java
@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, JogoMapper.class})
public interface FichaMapper {
    // Automatically maps nested usuario and jogo using respective mappers
    FichaDetailResponse toDetailResponse(Ficha ficha);
}
```

### Enum Mapping
```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @ValueMapping(source = "MASCULINO", target = "MALE")
    @ValueMapping(source = "FEMININO", target = "FEMALE")
    GenderDTO map(Genero genero);
}
```

## Integration with Spring

### Injection
```java
@Service
@RequiredArgsConstructor
public class FichaService {
    
    private final FichaRepository repository;
    private final FichaMapper mapper; // Injected as Spring bean
    
    public FichaResponse criar(CriarFichaRequest request) {
        Ficha ficha = mapper.toEntity(request);
        ficha = repository.save(ficha);
        return mapper.toResponse(ficha);
    }
}
```

## Troubleshooting

### 1. Mapper Not Found
- Check annotation processor is configured
- Rebuild project: `mvn clean compile`

### 2. Circular Dependencies
- Use `@Context` parameter
- Split into multiple mappers

### 3. Lombok Conflicts
- Add `lombok-mapstruct-binding` dependency
- Order annotation processors correctly

## Migration from JPA Converters

### Before (JPA Converter)
```java
@Converter
public class AtributosConverter implements AttributeConverter<Atributos, String> {
    @Override
    public String convertToDatabaseColumn(Atributos attribute) {
        // Manual JSON conversion
    }
}
```

### After (MapStruct)
```java
@Mapper(componentModel = "spring")
public interface AtributoMapper {
    @Mapping(source = "atributoConfig.nome", target = "nome")
    AtributoDTO toDTO(FichaAtributo atributo);
}
```

## Resources

- Official Docs: https://mapstruct.org/documentation/stable/reference/html/
- Examples: https://github.com/mapstruct/mapstruct-examples
- Spring Integration: https://mapstruct.org/documentation/stable/reference/html/#spring
