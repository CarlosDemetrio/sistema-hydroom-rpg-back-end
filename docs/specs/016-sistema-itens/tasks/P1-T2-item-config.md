# T2 — ItemConfig + ItemEfeito + ItemRequisito: Entidades, CRUDs e Sub-recursos

> Fase: Backend — Configuracao | Prioridade: P1
> Dependencias: T1 concluido
> Bloqueia: T3, T4, T6, T9 (frontend)
> Estimativa: 2-3 dias

---

## Objetivo

Implementar a entidade central do catalogo: `ItemConfig` com suas duas sub-entidades `ItemEfeito` e `ItemRequisito`. `ItemConfig` segue o padrao das entidades de configuracao existentes. `ItemEfeito` e `ItemRequisito` sao sub-entidades gerenciadas diretamente (sem lifecycle independente — deletadas quando o item e deletado, ou via endpoints de sub-recurso).

---

## Entidades a Criar

### ItemConfig

```java
@Entity
@Table(name = "item_configs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"jogo_id", "nome"}))
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class ItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raridade_id", nullable = false)
    private RaridadeItemConfig raridade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoItemConfig tipo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Column
    private Integer valor; // nullable — sem preco definido e permitido

    @Column
    private Integer duracaoPadrao; // null = indestrutivel

    @Column(nullable = false)
    private int nivelMinimo; // default 1

    @Column(length = 1000)
    private String propriedades; // texto livre: "versatil, finura, perfurante"

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false)
    private int ordemExibicao;

    // Sub-entidades — cascade para criacao/delecao junto
    @OneToMany(mappedBy = "itemConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemEfeito> efeitos = new ArrayList<>();

    @OneToMany(mappedBy = "itemConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemRequisito> requisitos = new ArrayList<>();
}
```

### ItemEfeito

```java
@Entity
@Table(name = "item_efeitos")
@Data @EqualsAndHashCode(callSuper = false)
@Builder @NoArgsConstructor @AllArgsConstructor
public class ItemEfeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoItemEfeito tipoEfeito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_alvo_id")
    private AtributoConfig atributoAlvo; // nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aptidao_alvo_id")
    private AptidaoConfig aptidaoAlvo; // nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_alvo_id")
    private BonusConfig bonusAlvo; // nullable

    @Column
    private Integer valorFixo; // nullable

    @Column(length = 200)
    private String formula; // nullable — apenas para FORMULA_CUSTOMIZADA

    @Column(length = 300)
    private String descricaoEfeito; // texto legivel do efeito

    // NOTA: ItemEfeito nao tem deletedAt — sem soft delete.
    // Deletar via endpoint remove fisicamente (via orphanRemoval = true).
    // Historico preservado via FichaItem que copia dados no momento de equipar (pos-MVP — no MVP nao copia).
}
```

**Enum TipoItemEfeito:**
```java
public enum TipoItemEfeito {
    BONUS_ATRIBUTO,    // valorFixo adicionado a FichaAtributo.itens
    BONUS_APTIDAO,     // valorFixo adicionado a FichaAptidao.itens (campo novo)
    BONUS_DERIVADO,    // valorFixo adicionado a FichaBonus.itens
    BONUS_VIDA,        // valorFixo adicionado a FichaVida.itens
    BONUS_ESSENCIA,    // valorFixo adicionado a FichaEssencia.itens
    FORMULA_CUSTOMIZADA, // formula exp4j avaliada
    EFEITO_DADO        // modifica dado de prospeccao (similar a DADO_UP de VantagemEfeito)
}
```

### ItemRequisito

```java
@Entity
@Table(name = "item_requisitos")
@Data @EqualsAndHashCode(callSuper = false)
@Builder @NoArgsConstructor @AllArgsConstructor
public class ItemRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoRequisito tipo;

    @Column(length = 50)
    private String alvo; // sigla, nome da classe, nome da raca, nome da aptidao, nome da vantagem

    @Column
    private Integer valorMinimo;
}
```

**Enum TipoRequisito:**
```java
public enum TipoRequisito {
    NIVEL,     // personagem.nivel >= valorMinimo
    ATRIBUTO,  // FichaAtributo[alvo].total >= valorMinimo
    BONUS,     // FichaBonus[alvo].total >= valorMinimo
    APTIDAO,   // FichaAptidao[alvo].total >= valorMinimo
    VANTAGEM,  // personagem tem FichaVantagem com vantagem.nome == alvo e nivel >= valorMinimo
    CLASSE,    // personagem.classe.nome == alvo
    RACA       // personagem.raca.nome == alvo
}
```

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `model/ItemConfig.java` | Entidade principal |
| `model/ItemEfeito.java` | Sub-entidade |
| `model/ItemRequisito.java` | Sub-entidade |
| `model/enums/TipoItemEfeito.java` | Enum 7 tipos |
| `model/enums/TipoRequisito.java` | Enum 7 tipos |
| `repository/ItemConfigRepository.java` | JPA repository com query filtrada |
| `service/configuracao/ItemConfigService.java` | extends AbstractConfiguracaoService |
| `service/ItemEfeitoService.java` | CRUD sub-recurso |
| `service/ItemRequisitoService.java` | CRUD sub-recurso |
| `dto/request/configuracao/ItemConfigRequest.java` | Record com validacoes |
| `dto/request/configuracao/ItemConfigUpdateRequest.java` | Record parcial |
| `dto/request/configuracao/ItemEfeitoRequest.java` | Record |
| `dto/request/configuracao/ItemRequisitoRequest.java` | Record |
| `dto/response/configuracao/ItemConfigResponse.java` | Record com raridade, tipo, efeitos, requisitos |
| `dto/response/configuracao/ItemConfigResumoResponse.java` | Record resumido para listagem (sem sub-entidades) |
| `dto/response/configuracao/ItemEfeitoResponse.java` | Record |
| `dto/response/configuracao/ItemRequisitoResponse.java` | Record |
| `mapper/configuracao/ItemConfigMapper.java` | MapStruct |
| `mapper/configuracao/ItemEfeitoMapper.java` | MapStruct |
| `mapper/configuracao/ItemRequisitoMapper.java` | MapStruct |
| `controller/configuracao/ItemConfigController.java` | Controller principal + sub-recursos |

---

## Validacoes Criticas

### ItemEfeitoRequest

```java
public record ItemEfeitoRequest(
    @NotNull TipoItemEfeito tipoEfeito,
    Long atributoAlvoId,    // condicional: obrigatorio se tipo == BONUS_ATRIBUTO
    Long aptidaoAlvoId,     // condicional: obrigatorio se tipo == BONUS_APTIDAO
    Long bonusAlvoId,       // condicional: obrigatorio se tipo == BONUS_DERIVADO
    Integer valorFixo,      // obrigatorio se tipo em [BONUS_*, EFEITO_DADO]
    @Size(max = 200) String formula,  // obrigatorio se tipo == FORMULA_CUSTOMIZADA
    @Size(max = 300) String descricaoEfeito
) {}
```

**Validacao cruzada em ItemEfeitoService:**
- `BONUS_ATRIBUTO`: `atributoAlvoId` obrigatorio, `aptidaoAlvoId` e `bonusAlvoId` devem ser null
- `BONUS_APTIDAO`: `aptidaoAlvoId` obrigatorio, outros null
- `BONUS_DERIVADO`: `bonusAlvoId` obrigatorio, outros null
- `BONUS_VIDA`, `BONUS_ESSENCIA`: nenhum alvo obrigatorio, `valorFixo` obrigatorio
- `FORMULA_CUSTOMIZADA`: `formula` obrigatorio; validar via `FormulaEvaluatorService.isValid()` → HTTP 422 se invalida
- `EFEITO_DADO`: `valorFixo` representa numero de posicoes para avancar no dado (igual a `nivelVantagem` em VantagemEfeito DADO_UP)
- Alvo (atributo/bonus/aptidao) deve pertencer ao mesmo Jogo do ItemConfig → HTTP 422

### ItemRequisitoRequest

```java
public record ItemRequisitoRequest(
    @NotNull TipoRequisito tipo,
    @Size(max = 50) String alvo, // obrigatorio para todos exceto NIVEL
    Integer valorMinimo
) {}
```

---

## Endpoints

### ItemConfig

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/configuracoes/itens?jogoId={id}&tipo={tipo}&raridade={rar}&nome={q}` | MESTRE, JOGADOR | Listar com filtros (retorna ItemConfigResumoResponse) |
| GET | `/api/v1/configuracoes/itens/{id}` | MESTRE, JOGADOR | Buscar por ID (retorna ItemConfigResponse completo com efeitos e requisitos) |
| POST | `/api/v1/configuracoes/itens` | MESTRE | Criar item |
| PUT | `/api/v1/configuracoes/itens/{id}` | MESTRE | Atualizar item |
| DELETE | `/api/v1/configuracoes/itens/{id}` | MESTRE | Soft delete |

### ItemEfeito (sub-recurso)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/configuracoes/itens/{id}/efeitos` | MESTRE, JOGADOR | Listar efeitos |
| POST | `/api/v1/configuracoes/itens/{id}/efeitos` | MESTRE | Adicionar efeito |
| PUT | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | MESTRE | Atualizar efeito |
| DELETE | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | MESTRE | Remover efeito (fisico — sem soft delete) |

### ItemRequisito (sub-recurso)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/configuracoes/itens/{id}/requisitos` | MESTRE, JOGADOR | Listar requisitos |
| POST | `/api/v1/configuracoes/itens/{id}/requisitos` | MESTRE | Adicionar requisito |
| DELETE | `/api/v1/configuracoes/itens/{id}/requisitos/{reqId}` | MESTRE | Remover requisito (fisico) |

---

## Regras de Negocio

- **RN-T2-01:** `(jogo_id, nome)` unico para ItemConfig → HTTP 409
- **RN-T2-02:** `raridade.jogo == itemConfig.jogo` → HTTP 422 se raridade de outro jogo
- **RN-T2-03:** `tipo.jogo == itemConfig.jogo` → HTTP 422 se tipo de outro jogo
- **RN-T2-04:** `ItemEfeito.atributoAlvo.jogo == itemConfig.jogo` → HTTP 422
- **RN-T2-05:** Formula customizada validada via FormulaEvaluatorService → HTTP 422 se invalida
- **RN-T2-06:** Deletar ItemConfig com FichaItem ativos nao e bloqueado (soft delete — FichaItems preservados com dados existentes)
- **RN-T2-07:** `ItemEfeito` e `ItemRequisito` nao tem soft delete proprio — gerenciados pelo lifecycle do ItemConfig
- **RN-T2-08:** `peso` deve ser > 0 (items sem peso = 0.00 e permitido apenas para itens de aventura sem massa)
- **RN-T2-09:** `nivelMinimo` default 1; minimo 0 (item sem nivel minimo) nao permitido (minimo e 1)

---

## Query de Listagem com Filtros

```java
// ItemConfigRepository
@Query("""
    SELECT i FROM ItemConfig i
    JOIN FETCH i.raridade r
    JOIN FETCH i.tipo t
    WHERE i.jogo.id = :jogoId
    AND i.deletedAt IS NULL
    AND (:nomeQuery IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeQuery, '%')))
    AND (:raridadeId IS NULL OR r.id = :raridadeId)
    AND (:categoriaItem IS NULL OR t.categoria = :categoriaItem)
    ORDER BY t.categoria, i.ordemExibicao
    """)
Page<ItemConfig> findByJogoIdWithFilters(
    @Param("jogoId") Long jogoId,
    @Param("nomeQuery") String nomeQuery,
    @Param("raridadeId") Long raridadeId,
    @Param("categoriaItem") CategoriaItem categoriaItem,
    Pageable pageable);
```

> Nota: A query de detalhe (buscar por ID) deve carregar efeitos e requisitos via JOIN FETCH para evitar N+1.

---

## Criterios de Aceitacao

- [ ] CRUD de ItemConfig com validacoes de jogo cruzado (raridade/tipo do mesmo jogo)
- [ ] Sub-recurso ItemEfeito: POST com tipo BONUS_DERIVADO cria efeito vinculado ao item
- [ ] Formula invalida em ItemEfeito retorna HTTP 422 com mensagem do FormulaEvaluatorService
- [ ] Sub-recurso ItemRequisito: POST com tipo ATRIBUTO e alvo "FOR" persiste corretamente
- [ ] DELETE em ItemConfig faz soft delete; FichaItem que referenciam o item permanecem
- [ ] GET listagem com filtro por raridade retorna apenas itens da raridade filtrada
- [ ] GET detalhe inclui lista de efeitos e requisitos (sem N+1)
- [ ] `./mvnw test` passa sem regressao

---

*Produzido por: Business Analyst/PO | 2026-04-04*
