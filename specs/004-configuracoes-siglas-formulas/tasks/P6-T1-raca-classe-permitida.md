# P6-T1 — RacaClassePermitida: entity, CRUD e atualização de Raca

## Objetivo
Criar RacaClassePermitida e atualizar o CRUD de Raca para expor as classes permitidas.

## Depende de
Phase 5 (ClassePersonagem estável com seus sub-recursos)

## Steps

### 1. RacaClassePermitida.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raca_classes_permitidas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_raca_classe_permitida",
        columnNames = {"raca_id", "classe_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
public class RacaClassePermitida extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    @NotNull
    private Raca raca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @NotNull
    private ClassePersonagem classe;
}
```

### 2. RacaClassePermitidaRepository

```java
public interface RacaClassePermitidaRepository extends JpaRepository<RacaClassePermitida, Long> {
    List<RacaClassePermitida> findByRacaId(Long racaId);
    boolean existsByRacaIdAndClasseId(Long racaId, Long classeId);
}
```

### 3. Raca.java — adicionar lista

```java
@OneToMany(mappedBy = "raca", fetch = FetchType.LAZY)
@Builder.Default
private List<RacaClassePermitida> classesPermitidas = new ArrayList<>();
```

### 4. DTOs

```java
// RacaClassePermitidaResponse.java
public record RacaClassePermitidaResponse(Long id, Long classeId, String classeNome) {}
```

**RacaResponse** — adicionar lista:
```java
public record RacaResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    List<RacaBonusAtributoResponse> bonusAtributos,    // já existe
    List<RacaClassePermitidaResponse> classesPermitidas, // NOVO
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 5. Endpoints em RacaController

```java
// Listar classes permitidas para uma raça
GET /api/jogos/{jogoId}/config/racas/{id}/classes-permitidas

// Adicionar classe permitida
POST /api/jogos/{jogoId}/config/racas/{id}/classes-permitidas
Body: { "classeId": 1 }

// Remover classe permitida
DELETE /api/jogos/{jogoId}/config/racas/{id}/classes-permitidas/{classeId}
```

### 6. RacaConfiguracaoService — novos métodos

```java
@Transactional
public RacaClassePermitida permitirClasse(Long racaId, Long classeId) {
    Raca raca = buscarPorId(racaId);
    ClassePersonagem classe = classeRepository.findById(classeId)
        .orElseThrow(() -> new ResourceNotFoundException("ClassePersonagem", classeId));

    if (!classe.getJogo().getId().equals(raca.getJogo().getId())) {
        throw new ValidationException(ValidationMessages.RacaClassePermitida.JOGO_DIFERENTE);
    }

    if (racaClasseRepository.existsByRacaIdAndClasseId(racaId, classeId)) {
        throw new ConflictException(ValidationMessages.RacaClassePermitida.JA_EXISTE);
    }

    return racaClasseRepository.save(
        RacaClassePermitida.builder().raca(raca).classe(classe).build()
    );
}

@Transactional
public void removerClassePermitida(Long racaId, Long racaClasseId) {
    RacaClassePermitida rcp = racaClasseRepository.findById(racaClasseId)
        .orElseThrow(() -> new ResourceNotFoundException("RacaClassePermitida", racaClasseId));
    if (!rcp.getRaca().getId().equals(racaId)) {
        throw new ValidationException("Associação não pertence à raça informada.");
    }
    racaClasseRepository.delete(rcp);
}
```

### 7. RacaMapper — mapear classesPermitidas

```java
@Mapping(target = "classesPermitidas", source = "classesPermitidas")
RacaResponse toResponse(Raca entity);

@Mapping(target = "classeId", source = "classe.id")
@Mapping(target = "classeNome", source = "classe.nome")
RacaClassePermitidaResponse toClassePermitidaResponse(RacaClassePermitida rcp);
```

> **N+1**: usar `findByIdWithAll` com JOIN FETCH em `RacaRepository` para `GET /{id}`.

```java
@Query("SELECT r FROM Raca r " +
       "LEFT JOIN FETCH r.bonusAtributos ba LEFT JOIN FETCH ba.atributo " +
       "LEFT JOIN FETCH r.classesPermitidas cp LEFT JOIN FETCH cp.classe " +
       "WHERE r.id = :id")
Optional<Raca> findByIdWithRelationships(@Param("id") Long id);
```

## Acceptance Checks
- [ ] Associar classe a raça persiste RacaClassePermitida
- [ ] Duplicata `(racaId, classeId)` é rejeitada
- [ ] Classe de outro jogo é rejeitada
- [ ] GET Raca por ID retorna lista `classesPermitidas`
- [ ] DELETE remove a associação
- [ ] GET lista de raças (sem id) não dispara N+1

## File Checklist
- `model/RacaClassePermitida.java`
- `model/Raca.java` (adicionar lista)
- `repository/RacaClassePermitidaRepository.java`
- `repository/RacaRepository.java` (query JOIN FETCH)
- `dto/response/configuracao/RacaClassePermitidaResponse.java`
- `dto/response/configuracao/RacaResponse.java` (adicionar lista)
- `mapper/configuracao/RacaMapper.java`
- `service/configuracao/RacaConfiguracaoService.java`
- `controller/configuracao/RacaController.java`
- `exception/ValidationMessages.java`

## References
- Padrão de `RacaBonusAtributo` (já existente) — referência de sub-recurso de Raca
- `docs/backend/06-mappers.md`
- `docs/backend/07-controllers.md`
