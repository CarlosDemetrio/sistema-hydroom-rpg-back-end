# T1 — Entidades, Repositories, DTOs e Mappers (4 novas entidades)

> Fase: Backend | Prioridade: P1
> Dependencias: nenhuma (Spec 004 concluida — ClassePersonagem, Raca, VantagemConfig existem)
> Bloqueia: T2, T3, T4
> Estimativa: 4–6 horas

---

## Objetivo

Criar as 4 novas entidades de configuracao (`ClassePontosConfig`, `ClasseVantagemPreDefinida`, `RacaPontosConfig`, `RacaVantagemPreDefinida`), seus repositories, DTOs de request/response e mappers MapStruct. Adicionar as colecoes `Set<>` nas entidades pai (`ClassePersonagem`, `Raca`).

Esta task cria a camada de modelo e persistencia. Os endpoints CRUD serao criados na T2.

---

## Arquivos a Criar

### Entidades

**`model/ClassePontosConfig.java`**

```java
@Entity
@Table(name = "classe_pontos_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_pontos_nivel",
        columnNames = {"classe_personagem_id", "nivel"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ClassePontosConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_personagem_id", nullable = false)
    private ClassePersonagem classePersonagem;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @Column(name = "pontos_atributo", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosAtributo = 0;

    // pontosAptidao AUSENTE: aptidões são independentes de classe/raça.
    // Pool de aptidão vem SOMENTE de NivelConfig.pontosAptidao (global).
    // Aptidões servem apenas para testes específicos. Decisão PO 2026-04-04.

    @Column(name = "pontos_vantagem", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosVantagem = 0;
}
```

**`model/ClasseVantagemPreDefinida.java`**

```java
@Entity
@Table(name = "classe_vantagem_predefinida",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_vantagem_nivel",
        columnNames = {"classe_personagem_id", "nivel", "vantagem_config_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ClasseVantagemPreDefinida extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_personagem_id", nullable = false)
    private ClassePersonagem classePersonagem;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;
}
```

**`model/RacaPontosConfig.java`**

```java
@Entity
@Table(name = "raca_pontos_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_raca_pontos_nivel",
        columnNames = {"raca_id", "nivel"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class RacaPontosConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @Column(name = "pontos_atributo", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosAtributo = 0;

    // pontosAptidao AUSENTE: mesma regra de ClassePontosConfig.
    // Raça não interfere no pool de aptidão.

    @Column(name = "pontos_vantagem", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer pontosVantagem = 0;
}
```

**`model/RacaVantagemPreDefinida.java`**

```java
@Entity
@Table(name = "raca_vantagem_predefinida",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_raca_vantagem_nivel",
        columnNames = {"raca_id", "nivel", "vantagem_config_id"}
    ))
@SQLRestriction("deleted_at IS NULL")
@Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class RacaVantagemPreDefinida extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;

    @Column(nullable = false)
    @Min(1)
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagemConfig;
}
```

---

### Repositories

**`repository/ClassePontosConfigRepository.java`**

```java
public interface ClassePontosConfigRepository extends JpaRepository<ClassePontosConfig, Long> {

    List<ClassePontosConfig> findByClassePersonagemIdOrderByNivel(Long classeId);

    List<ClassePontosConfig> findByClassePersonagemIdAndNivelLessThanEqual(Long classeId, int nivel);

    Optional<ClassePontosConfig> findByClassePersonagemIdAndNivel(Long classeId, int nivel);

    boolean existsByClassePersonagemIdAndNivel(Long classeId, int nivel);
}
```

**`repository/ClasseVantagemPreDefinidaRepository.java`**

```java
public interface ClasseVantagemPreDefinidaRepository extends JpaRepository<ClasseVantagemPreDefinida, Long> {

    List<ClasseVantagemPreDefinida> findByClassePersonagemIdOrderByNivel(Long classeId);

    List<ClasseVantagemPreDefinida> findByClassePersonagemIdAndNivel(Long classeId, int nivel);

    @Query("""
        SELECT cvp FROM ClasseVantagemPreDefinida cvp
        JOIN FETCH cvp.vantagemConfig
        WHERE cvp.classePersonagem.id = :classeId
        AND cvp.nivel = :nivel
        AND cvp.deletedAt IS NULL
        """)
    List<ClasseVantagemPreDefinida> findByClasseIdAndNivelWithVantagem(
        @Param("classeId") Long classeId,
        @Param("nivel") int nivel);

    boolean existsByClassePersonagemIdAndNivelAndVantagemConfigId(
        Long classeId, int nivel, Long vantagemConfigId);
}
```

**`repository/RacaPontosConfigRepository.java`**

```java
public interface RacaPontosConfigRepository extends JpaRepository<RacaPontosConfig, Long> {

    List<RacaPontosConfig> findByRacaIdOrderByNivel(Long racaId);

    List<RacaPontosConfig> findByRacaIdAndNivelLessThanEqual(Long racaId, int nivel);

    Optional<RacaPontosConfig> findByRacaIdAndNivel(Long racaId, int nivel);

    boolean existsByRacaIdAndNivel(Long racaId, int nivel);
}
```

**`repository/RacaVantagemPreDefinidaRepository.java`**

```java
public interface RacaVantagemPreDefinidaRepository extends JpaRepository<RacaVantagemPreDefinida, Long> {

    List<RacaVantagemPreDefinida> findByRacaIdOrderByNivel(Long racaId);

    List<RacaVantagemPreDefinida> findByRacaIdAndNivel(Long racaId, int nivel);

    @Query("""
        SELECT rvp FROM RacaVantagemPreDefinida rvp
        JOIN FETCH rvp.vantagemConfig
        WHERE rvp.raca.id = :racaId
        AND rvp.nivel = :nivel
        AND rvp.deletedAt IS NULL
        """)
    List<RacaVantagemPreDefinida> findByRacaIdAndNivelWithVantagem(
        @Param("racaId") Long racaId,
        @Param("nivel") int nivel);

    boolean existsByRacaIdAndNivelAndVantagemConfigId(
        Long racaId, int nivel, Long vantagemConfigId);
}
```

---

### DTOs de Request

**`dto/request/configuracao/ClassePontosConfigRequest.java`**

```java
public record ClassePontosConfigRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull @Min(0) Integer pontosAtributo,
    @NotNull @Min(0) Integer pontosAptidao,
    @NotNull @Min(0) Integer pontosVantagem
) {}
```

**`dto/request/configuracao/ClasseVantagemPreDefinidaRequest.java`**

```java
public record ClasseVantagemPreDefinidaRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull Long vantagemConfigId
) {}
```

**`dto/request/configuracao/RacaPontosConfigRequest.java`**

```java
public record RacaPontosConfigRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull @Min(0) Integer pontosAtributo,
    @NotNull @Min(0) Integer pontosAptidao,
    @NotNull @Min(0) Integer pontosVantagem
) {}
```

**`dto/request/configuracao/RacaVantagemPreDefinidaRequest.java`**

```java
public record RacaVantagemPreDefinidaRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull Long vantagemConfigId
) {}
```

---

### DTOs de Response

**`dto/response/configuracao/ClassePontosConfigResponse.java`**

```java
public record ClassePontosConfigResponse(
    Long id,
    Long classePersonagemId,
    Integer nivel,
    Integer pontosAtributo,
    Integer pontosAptidao,
    Integer pontosVantagem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

**`dto/response/configuracao/ClasseVantagemPreDefinidaResponse.java`**

```java
public record ClasseVantagemPreDefinidaResponse(
    Long id,
    Long classePersonagemId,
    Integer nivel,
    Long vantagemConfigId,
    String vantagemConfigNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

**`dto/response/configuracao/RacaPontosConfigResponse.java`**

```java
public record RacaPontosConfigResponse(
    Long id,
    Long racaId,
    Integer nivel,
    Integer pontosAtributo,
    Integer pontosAptidao,
    Integer pontosVantagem,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

**`dto/response/configuracao/RacaVantagemPreDefinidaResponse.java`**

```java
public record RacaVantagemPreDefinidaResponse(
    Long id,
    Long racaId,
    Integer nivel,
    Long vantagemConfigId,
    String vantagemConfigNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

---

### Mappers (MapStruct)

**`mapper/configuracao/ClassePontosConfigMapper.java`**

```java
@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClassePontosConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classePersonagem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ClassePontosConfig toEntity(ClassePontosConfigRequest request);

    @Mapping(target = "classePersonagemId", source = "classePersonagem.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    ClassePontosConfigResponse toResponse(ClassePontosConfig entity);

    List<ClassePontosConfigResponse> toResponseList(List<ClassePontosConfig> entities);
}
```

Seguir o mesmo padrao para `ClasseVantagemPreDefinidaMapper`, `RacaPontosConfigMapper` e `RacaVantagemPreDefinidaMapper`.

Para os mappers de VantagemPreDefinida, incluir:
```java
@Mapping(target = "vantagemConfigId", source = "vantagemConfig.id")
@Mapping(target = "vantagemConfigNome", source = "vantagemConfig.nome")
```

---

### Adicionar colecoes nas entidades pai

**`model/ClassePersonagem.java`** — adicionar:

```java
@OneToMany(mappedBy = "classePersonagem", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private Set<ClassePontosConfig> pontosConfig = new HashSet<>();

@OneToMany(mappedBy = "classePersonagem", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private Set<ClasseVantagemPreDefinida> vantagensPreDefinidas = new HashSet<>();
```

> Usar `Set<>` (nao `List<>`) para evitar `MultipleBagFetchException` — ClassePersonagem ja tem `Set<ClasseBonus>` e `Set<ClasseAptidaoBonus>`.

**`model/Raca.java`** — adicionar:

```java
@OneToMany(mappedBy = "raca", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private Set<RacaPontosConfig> pontosConfig = new HashSet<>();

@OneToMany(mappedBy = "raca", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private Set<RacaVantagemPreDefinida> vantagensPreDefinidas = new HashSet<>();
```

---

## Regras de Negocio

- Entidades estendem `BaseEntity` (soft delete via `@SQLRestriction("deleted_at IS NULL")`)
- Unique constraints previnem duplicatas de nivel (para PontosConfig) e de nivel+vantagem (para VantagemPreDefinida)
- `Set<>` em todas as colecoes OneToMany
- `FetchType.LAZY` em todos os `@ManyToOne`

---

## Criterios de Aceitacao

- [ ] 4 novas entidades criadas seguindo padrao BaseEntity
- [ ] 4 repositories com queries especificas por nivel e por pai
- [ ] 8 DTOs (4 request + 4 response) como records com validacoes `@NotNull`, `@Min`
- [ ] 4 mappers MapStruct com `NullValuePropertyMappingStrategy.IGNORE`
- [ ] Colecoes `Set<>` adicionadas em `ClassePersonagem` e `Raca`
- [ ] Nenhum teste existente quebrado (`./mvnw test` passa)
- [ ] H2 cria as tabelas automaticamente (ddl-auto=create-drop em test profile)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
