# P4-T1 — VantagemPreRequisito: entity e repository

## Objetivo
Criar a entity VantagemPreRequisito representando que uma vantagem exige outra como pré-requisito.

## Depende de
P3-T3 (VantagemConfig estável com categoria FK)

## Modelo de domínio

```
VantagemConfig A  "requer"  VantagemConfig B em nivelMinimo X
```

Ou seja: para comprar A, o jogador precisa ter B no nível X ou superior.

## Steps

### 1. VantagemPreRequisito.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vantagem_pre_requisitos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_vantagem_prerequisito",
        columnNames = {"vantagem_id", "vantagem_requisito_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
public class VantagemPreRequisito extends BaseEntity {

    // A vantagem que TEM o pré-requisito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id", nullable = false)
    @NotNull
    private VantagemConfig vantagem;

    // A vantagem que É EXIGIDA como pré-requisito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_requisito_id", nullable = false)
    @NotNull
    private VantagemConfig requisito;

    // Nível mínimo que o pré-requisito deve estar (default: 1)
    @NotNull
    @Min(1)
    @Column(name = "nivel_minimo", nullable = false)
    @Builder.Default
    private Integer nivelMinimo = 1;
}
```

> **Constraint**: `(vantagem_id, vantagem_requisito_id)` unique — não faz sentido ter o mesmo pré-requisito duas vezes.
> **Auto-referência**: `vantagem_id != vantagem_requisito_id` deve ser validado no service (não no DB).

### 2. VantagemPreRequisitoRepository

```java
public interface VantagemPreRequisitoRepository extends JpaRepository<VantagemPreRequisito, Long> {

    // Lista pré-requisitos de uma vantagem
    List<VantagemPreRequisito> findByVantagemId(Long vantagemId);

    // Verifica se par já existe (para detecção de duplicata)
    boolean existsByVantagemIdAndRequisitoId(Long vantagemId, Long requisitoId);

    // Para detecção de ciclo: "quais vantagens TÊM B como pré-requisito?"
    // Usado para verificar se A já é requisito de B (ciclo direto)
    List<VantagemPreRequisito> findByRequisitoId(Long requisitoId);

    // Para exclusão em cascata ao deletar uma vantagem (soft delete não cuida disso automaticamente)
    List<VantagemPreRequisito> findByVantagemIdOrRequisitoId(Long vantagemId, Long requisitoId);
}
```

### 3. VantagemConfig — adicionar lista (lazy, não cascade delete)

```java
@OneToMany(mappedBy = "vantagem", fetch = FetchType.LAZY)
@Builder.Default
private List<VantagemPreRequisito> preRequisitos = new ArrayList<>();
```

> **Não usar cascade = ALL** — o lifecycle dos VantagemPreRequisito é gerenciado explicitamente no service.

### 4. DTOs auxiliares (para usar na task P4-T3)

```java
// VantagemPreRequisitoRequest.java
public record VantagemPreRequisitoRequest(
    @NotNull Long requisitoId,
    @Min(1) Integer nivelMinimo  // default 1 se null
) {}

// VantagemPreRequisitoResponse.java
public record VantagemPreRequisitoResponse(
    Long id,
    Long requisitoId,
    String requisitoNome,
    Integer nivelMinimo
) {}
```

## Acceptance Checks
- [ ] Entity persistida corretamente com foreign keys
- [ ] Unique constraint `(vantagem_id, vantagem_requisito_id)` funciona
- [ ] `findByVantagemId` retorna pré-requisitos da vantagem
- [ ] `findByRequisitoId` retorna vantagens que exigem o requisito (usado na detecção de ciclos)
- [ ] Auto-referência (`vantagem_id == vantagem_requisito_id`) é rejeitada (validação no service, task P4-T2)

## File Checklist
- `model/VantagemPreRequisito.java`
- `model/VantagemConfig.java` (adicionar `List<VantagemPreRequisito> preRequisitos`)
- `repository/VantagemPreRequisitoRepository.java`
- `dto/request/configuracao/VantagemPreRequisitoRequest.java`
- `dto/response/configuracao/VantagemPreRequisitoResponse.java`
