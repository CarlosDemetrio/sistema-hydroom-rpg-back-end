# Controllers e Swagger

## Template Base

```java
/**
 * Controller REST para gerenciamento de MyEntity.
 * Coordena requisições HTTP e transformação de dados.
 * 
 * @author Seu Nome
 * @since 1.0
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "API para gerenciamento de recursos")
public class MyController {
    private final MyService service;
    private final MyMapper mapper;
    
    @GetMapping
    @Operation(summary = "Listar recursos", description = "Retorna lista de todos os recursos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<MyResponseDTO>> findAll() {
        List<MyEntity> entities = service.findAll();
        List<MyResponseDTO> dtos = mapper.toResponseDTOList(entities);
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna recurso específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recurso encontrado"),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<MyResponseDTO> findById(
            @Parameter(description = "ID do recurso") @PathVariable Long id) {
        MyEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(entity));
    }
    
    @PostMapping
    @Operation(summary = "Criar recurso", description = "Cria novo recurso")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Recurso criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito")
    })
    public ResponseEntity<MyResponseDTO> create(@Valid @RequestBody MyCreateDTO dto) {
        MyEntity entity = mapper.toEntity(dto);
        MyEntity created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponseDTO(created));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar recurso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atualizado"),
        @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public ResponseEntity<MyResponseDTO> update(
            @Parameter(description = "ID") @PathVariable Long id,
            @Valid @RequestBody MyUpdateDTO dto) {
        MyEntity entity = service.findById(id);
        mapper.updateEntityFromDTO(entity, dto);
        MyEntity updated = service.update(id, entity);
        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar recurso")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deletado"),
        @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## HTTP Status Codes

| Operação | Sucesso | Status |
|----------|---------|--------|
| GET      | Found   | 200 OK |
| POST     | Created | 201 Created |
| PUT      | Updated | 200 OK |
| DELETE   | Deleted | 204 No Content |

| Erro                  | Status |
|-----------------------|--------|
| Validação             | 400 Bad Request |
| Não autenticado       | 401 Unauthorized |
| Sem permissão         | 403 Forbidden |
| Não encontrado        | 404 Not Found |
| Conflito              | 409 Conflict |
| Regra de negócio      | 422 Unprocessable Entity |
| Erro interno          | 500 Internal Server Error |

## Swagger Annotations

### @Tag
```java
@Tag(name = "Jogos", description = "API para gerenciamento de jogos")
public class JogoController {
```

### @Operation
```java
@Operation(
    summary = "Criar novo jogo",
    description = "Cria um novo jogo com o usuário atual como mestre. " +
                  "Apenas usuários com role MESTRE podem criar jogos."
)
```

### @ApiResponses
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "Jogo criado com sucesso",
        content = @Content(schema = @Schema(implementation = GameResponseDTO.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Dados de entrada inválidos",
        content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "403",
        description = "Usuário não tem permissão (não é mestre)"
    ),
    @ApiResponse(
        responseCode = "409",
        description = "Já existe jogo com esse nome para este mestre"
    )
})
```

### @Parameter
```java
public ResponseEntity<GameDTO> findById(
    @Parameter(
        description = "ID do jogo a ser buscado",
        required = true,
        example = "1"
    )
    @PathVariable Long id
) {
```

### @Schema (nos DTOs)
```java
@Data
@Schema(description = "DTO para criação de jogo")
public class CreateGameDTO {
    
    @Schema(
        description = "Nome do jogo",
        example = "Campanha de D&D",
        required = true,
        minLength = 3,
        maxLength = 100
    )
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(max = 100)
    private String nome;
    
    @Schema(
        description = "Número máximo de jogadores",
        example = "6",
        minimum = "1",
        maximum = "20"
    )
    @Min(1)
    @Max(20)
    private Integer maxJogadores;
}
```

## RESTful Patterns

### ✅ Bons Endpoints
```
GET    /api/jogos              - Lista todos
GET    /api/jogos/{id}         - Busca um
POST   /api/jogos              - Cria
PUT    /api/jogos/{id}         - Atualiza
DELETE /api/jogos/{id}         - Deleta
GET    /api/jogos/{id}/fichas  - Lista fichas do jogo
```

### ❌ Evite
```
GET    /api/getJogos
POST   /api/createJogo
GET    /api/jogo/get/{id}
DELETE /api/deleteJogo/{id}
```

## Security na Controller

```java
@PreAuthorize("hasRole('MESTRE')")
@PostMapping
public ResponseEntity<GameDTO> create(@Valid @RequestBody CreateGameDTO dto) {
    // Apenas mestres podem criar
}

@PreAuthorize("@gameSecurityService.canEdit(#id, principal)")
@PutMapping("/{id}")
public ResponseEntity<GameDTO> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateGameDTO dto) {
    // Apenas o dono pode editar
}
```

## Query Parameters

```java
@GetMapping
@Operation(summary = "Buscar jogos com filtros")
public ResponseEntity<List<GameDTO>> search(
    @Parameter(description = "Filtrar por nome") 
    @RequestParam(required = false) String nome,
    
    @Parameter(description = "Apenas jogos ativos")
    @RequestParam(defaultValue = "true") boolean active,
    
    @Parameter(description = "Página (0-based)")
    @RequestParam(defaultValue = "0") int page,
    
    @Parameter(description = "Tamanho da página")
    @RequestParam(defaultValue = "20") int size
) {
    // Implementação
}
```

## Paginação

```java
@GetMapping
public ResponseEntity<Page<GameDTO>> findAll(
    @Parameter(hidden = true) Pageable pageable
) {
    Page<Game> games = service.findAll(pageable);
    Page<GameDTO> dtos = games.map(mapper::toResponseDTO);
    return ResponseEntity.ok(dtos);
}
```

## Best Practices

### ✅ DO
- Controllers thin (apenas coordenação)
- Mappers na controller
- ResponseEntity sempre
- @Valid para validação
- Swagger completo
- HTTP status corretos
- RESTful endpoints

### ❌ DON'T
- Lógica de negócio
- Mappers no service
- Expor entities
- Missing @Valid
- Poor documentation
- Wrong status codes
- Non-RESTful endpoints
