# T1 — RaridadeItemConfig + TipoItemConfig: Entidades, CRUDs e Testes

> Fase: Backend — Configuracao | Prioridade: P1
> Dependencias: Spec 007 T0 concluido (motor estavel)
> Bloqueia: T2, T4, T8 (frontend)
> Estimativa: 1-2 dias

---

## Objetivo

Implementar as duas entidades de configuracao base do sistema de itens: `RaridadeItemConfig` (raridades com restricao de adicao por role) e `TipoItemConfig` (categorias/subcategorias de itens). Ambas seguem o padrao das 13 entidades de configuracao existentes (AbstractConfiguracaoService, BaseEntity, ConfiguracaoEntity).

---

## Entidades a Criar

### RaridadeItemConfig

```java
@Entity
@Table(name = "raridade_item_configs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"jogo_id", "nome"}))
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class RaridadeItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 7)
    private String cor; // ex: #9d9d9d

    @Column(nullable = false)
    private int ordemExibicao;

    @Column(nullable = false)
    private boolean podeJogadorAdicionar;

    @Column
    private Integer bonusAtributoMin;

    @Column
    private Integer bonusAtributoMax;

    @Column
    private Integer bonusDerivadoMin;

    @Column
    private Integer bonusDerivadoMax;

    @Column(length = 500)
    private String descricao;
}
```

### TipoItemConfig

```java
@Entity
@Table(name = "tipo_item_configs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"jogo_id", "nome"}))
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class TipoItemConfig extends BaseEntity implements ConfiguracaoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CategoriaItem categoria; // ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private SubcategoriaItem subcategoria; // nullable

    @Column(nullable = false)
    private boolean requerDuasMaos;

    @Column(nullable = false)
    private int ordemExibicao;

    @Column(length = 300)
    private String descricao;
}
```

**Enum CategoriaItem:** `ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA`

**Enum SubcategoriaItem:** `ESPADA, ARCO, LANCA, MACHADO, MARTELO, CAJADO, ADAGA, ARREMESSO, BESTA, ARMADURA_LEVE, ARMADURA_MEDIA, ARMADURA_PESADA, ESCUDO, ANEL, AMULETO, BOTAS, CAPA, LUVAS, POCAO, MUNICAO, KIT, OUTROS`

---

## Arquivos a Criar

### Backend

| Arquivo | Descricao |
|---------|-----------|
| `model/RaridadeItemConfig.java` | Entidade |
| `model/TipoItemConfig.java` | Entidade |
| `model/enums/CategoriaItem.java` | Enum de 6 categorias |
| `model/enums/SubcategoriaItem.java` | Enum de 22 subcategorias |
| `repository/RaridadeItemConfigRepository.java` | JPA repository |
| `repository/TipoItemConfigRepository.java` | JPA repository |
| `service/configuracao/RaridadeItemConfigService.java` | extends AbstractConfiguracaoService |
| `service/configuracao/TipoItemConfigService.java` | extends AbstractConfiguracaoService |
| `dto/request/configuracao/RaridadeItemConfigRequest.java` | Record com validacoes |
| `dto/request/configuracao/RaridadeItemConfigUpdateRequest.java` | Record parcial |
| `dto/request/configuracao/TipoItemConfigRequest.java` | Record |
| `dto/request/configuracao/TipoItemConfigUpdateRequest.java` | Record parcial |
| `dto/response/configuracao/RaridadeItemConfigResponse.java` | Record com dataCriacao, dataUltimaAtualizacao |
| `dto/response/configuracao/TipoItemConfigResponse.java` | Record |
| `mapper/configuracao/RaridadeItemConfigMapper.java` | MapStruct |
| `mapper/configuracao/TipoItemConfigMapper.java` | MapStruct |
| `controller/configuracao/RaridadeItemConfigController.java` | @RestController, thin |
| `controller/configuracao/TipoItemConfigController.java` | @RestController, thin |

### Testes

| Arquivo | Descricao |
|---------|-----------|
| `test/.../RaridadeItemConfigServiceIntegrationTest.java` | extends BaseConfiguracaoServiceIntegrationTest |
| `test/.../TipoItemConfigServiceIntegrationTest.java` | extends BaseConfiguracaoServiceIntegrationTest |

---

## Validacoes dos DTOs

### RaridadeItemConfigRequest

```java
public record RaridadeItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 50) String nome,
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve ser hexadecimal ex: #9d9d9d") String cor,
    @NotNull @Min(1) Integer ordemExibicao,
    @NotNull Boolean podeJogadorAdicionar,
    @Min(0) Integer bonusAtributoMin,
    @Min(0) Integer bonusAtributoMax,
    @Min(0) Integer bonusDerivadoMin,
    @Min(0) Integer bonusDerivadoMax,
    @Size(max = 500) String descricao
) {}
```

### TipoItemConfigRequest

```java
public record TipoItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 100) String nome,
    @NotNull CategoriaItem categoria,
    SubcategoriaItem subcategoria, // nullable
    boolean requerDuasMaos,
    @NotNull @Min(1) Integer ordemExibicao,
    @Size(max = 300) String descricao
) {}
```

---

## Endpoints

### RaridadeItemConfig

| Metodo | Path | Role | Status esperado |
|--------|------|------|----------------|
| GET | `/api/v1/configuracoes/raridades-item?jogoId={id}` | MESTRE, JOGADOR | 200 |
| GET | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE, JOGADOR | 200 / 404 |
| POST | `/api/v1/configuracoes/raridades-item` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE | 200 / 404 / 409 |
| DELETE | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE | 204 / 404 / 409 |
| PATCH | `/api/v1/configuracoes/raridades-item/reordenar` | MESTRE | 200 |

### TipoItemConfig

| Metodo | Path | Role | Status esperado |
|--------|------|------|----------------|
| GET | `/api/v1/configuracoes/tipos-item?jogoId={id}` | MESTRE, JOGADOR | 200 |
| GET | `/api/v1/configuracoes/tipos-item/{id}` | MESTRE, JOGADOR | 200 / 404 |
| POST | `/api/v1/configuracoes/tipos-item` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/tipos-item/{id}` | MESTRE | 200 / 409 |
| DELETE | `/api/v1/configuracoes/tipos-item/{id}` | MESTRE | 204 / 409 |

---

## Regras de Negocio

- **RN-T1-01:** `(jogo_id, nome)` deve ser unico para RaridadeItemConfig → HTTP 409 em duplicata
- **RN-T1-02:** `(jogo_id, nome)` deve ser unico para TipoItemConfig → HTTP 409 em duplicata
- **RN-T1-03:** `(jogo_id, ordemExibicao)` deve ser unico para RaridadeItemConfig → HTTP 409 em duplicata de ordem
- **RN-T1-04:** RaridadeItemConfig com `podeJogadorAdicionar = true` = Comum. Deve existir pelo menos uma raridade permissiva ao Jogador (sem validacao obrigatoria — mas documentado)
- **RN-T1-05:** Deletar RaridadeItemConfig usada em ItemConfig existente → HTTP 409 ("Raridade usada em X itens")
- **RN-T1-06:** Deletar TipoItemConfig usado em ItemConfig existente → HTTP 409 ("Tipo usado em X itens")
- **RN-T1-07:** Cor deve ser hex valido (#RRGGBB, 7 chars) — validar via @Pattern no DTO

---

## Criterios de Aceitacao

- [ ] Criar RaridadeItemConfig com todos os campos valida e persiste
- [ ] Cor invalida (ex: "azul" ou "#GG0000") retorna HTTP 400 com mensagem descritiva
- [ ] Nome duplicado no mesmo jogo retorna HTTP 409
- [ ] Deletar raridade usada em ItemConfig retorna HTTP 409
- [ ] GET lista todas as raridades do jogo ordenadas por `ordemExibicao`
- [ ] Criar TipoItemConfig com categoria e subcategoria persiste corretamente
- [ ] GET lista tipos agrupados por categoria (ou com campo categoria no response)
- [ ] `BaseConfiguracaoServiceIntegrationTest` passa para ambas as entidades (~10 cenarios automaticos)
- [ ] `./mvnw test` passa sem regressao (457+ testes)

---

*Produzido por: Business Analyst/PO | 2026-04-04*
