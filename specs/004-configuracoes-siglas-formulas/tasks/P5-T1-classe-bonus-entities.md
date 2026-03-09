# P5-T1 — Entities ClasseBonus e ClasseAptidaoBonus

## Objetivo
Criar as entities de relacionamento entre ClassePersonagem e BonusConfig/AptidaoConfig.

## Regras de negócio
- `ClasseBonus`: bônus que a classe concede em um BonusConfig específico, calculado por nível. Ex: Guerreiro +1 B.B.A. por nível.
- `ClasseAptidaoBonus`: bônus fixo que a classe concede em uma AptidaoConfig. Ex: Ladrão +2 em Furtividade.
- Unicidade: `(classe_id, bonus_id)` e `(classe_id, aptidao_id)` — classe não pode ter o mesmo bônus duas vezes.

## Steps

### 1. ClasseBonus.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classe_bonus",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_bonus_classe_bonus",
        columnNames = {"classe_id", "bonus_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
public class ClasseBonus extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @NotNull
    private ClassePersonagem classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id", nullable = false)
    @NotNull
    private BonusConfig bonus;

    // Valor acrescido por nível. Pode ser fracionário (ex: 0.5 por nível)
    @NotNull
    @Column(name = "valor_por_nivel", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPorNivel;
}
```

### 2. ClasseAptidaoBonus.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classe_aptidao_bonus",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_aptidao_bonus",
        columnNames = {"classe_id", "aptidao_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
public class ClasseAptidaoBonus extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @NotNull
    private ClassePersonagem classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_id", nullable = false)
    @NotNull
    private AptidaoConfig aptidao;

    // Bônus fixo (pode ser negativo? Glossário não menciona, usar Integer não negativo)
    @NotNull
    @Min(0)
    @Column(name = "bonus", nullable = false)
    private Integer bonus;
}
```

### 3. Repositories

```java
public interface ClasseBonusRepository extends JpaRepository<ClasseBonus, Long> {
    List<ClasseBonus> findByClasseId(Long classeId);
    boolean existsByClasseIdAndBonusId(Long classeId, Long bonusId);
    void deleteByClasseId(Long classeId);  // para substituição de lista
}

public interface ClasseAptidaoBonusRepository extends JpaRepository<ClasseAptidaoBonus, Long> {
    List<ClasseAptidaoBonus> findByClasseId(Long classeId);
    boolean existsByClasseIdAndAptidaoId(Long classeId, Long aptidaoId);
    void deleteByClasseId(Long classeId);
}
```

### 4. ClassePersonagem — adicionar listas (lazy, sem cascade)

```java
@OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
@Builder.Default
private List<ClasseBonus> bonusConfig = new ArrayList<>();

@OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
@Builder.Default
private List<ClasseAptidaoBonus> aptidaoBonus = new ArrayList<>();
```

### 5. DTOs auxiliares

```java
// ClasseBonusRequest.java
public record ClasseBonusRequest(
    @NotNull Long bonusId,
    @NotNull @DecimalMin("0.01") BigDecimal valorPorNivel
) {}

// ClasseAptidaoBonusRequest.java
public record ClasseAptidaoBonusRequest(
    @NotNull Long aptidaoId,
    @NotNull @Min(0) Integer bonus
) {}

// ClasseBonusResponse.java
public record ClasseBonusResponse(
    Long id, Long bonusId, String bonusNome, BigDecimal valorPorNivel
) {}

// ClasseAptidaoBonusResponse.java
public record ClasseAptidaoBonusResponse(
    Long id, Long aptidaoId, String aptidaoNome, Integer bonus
) {}
```

## Acceptance Checks
- [ ] Entities persistem corretamente com FK para Classe + Bonus/Aptidão
- [ ] Unique constraint rejeita duplicata `(classeId, bonusId)`
- [ ] `findByClasseId` retorna listas corretas
- [ ] ClassePersonagem tem listas `bonusConfig` e `aptidaoBonus` acessíveis

## File Checklist
- `model/ClasseBonus.java`
- `model/ClasseAptidaoBonus.java`
- `model/ClassePersonagem.java` (adicionar listas)
- `repository/ClasseBonusRepository.java`
- `repository/ClasseAptidaoBonusRepository.java`
- `dto/request/configuracao/ClasseBonusRequest.java`
- `dto/request/configuracao/ClasseAptidaoBonusRequest.java`
- `dto/response/configuracao/ClasseBonusResponse.java`
- `dto/response/configuracao/ClasseAptidaoBonusResponse.java`
