# T3 — ClasseEquipamentoInicial: Sub-recurso de ClassePersonagem

> Fase: Backend — Configuracao | Prioridade: P1
> Dependencias: T2 concluido
> Bloqueia: T6 (dataset), T7 (testes), T10 (frontend)
> Estimativa: 1 dia

---

## Objetivo

Implementar `ClasseEquipamentoInicial` como sub-recurso de `ClassePersonagem`. Esta entidade define quais itens um personagem recebe ao criar uma ficha com determinada classe. Suporta itens obrigatorios e grupos de escolha (mesmo `grupoEscolha` = Jogador escolhe um entre os opcoes do grupo).

---

## Entidade

```java
@Entity
@Table(name = "classe_equipamentos_iniciais")
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class ClasseEquipamentoInicial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id", nullable = false)
    private ItemConfig itemConfig;

    @Column(nullable = false)
    private boolean obrigatorio; // true = sempre concedido; false = opcao em grupo de escolha

    @Column
    private Integer grupoEscolha; // itens com mesmo grupoEscolha = escolher apenas um; null = obrigatorio redundante

    @Column(nullable = false)
    private int quantidade; // default 1
}
```

---

## Logica de Grupos de Escolha

Um `grupoEscolha` representa um conjunto de itens mutuamente exclusivos. Exemplo:

```
Guerreiro:
  - Cota de Malha        | obrigatorio=true  | grupoEscolha=null  → sempre recebe
  - Escudo de Aco         | obrigatorio=true  | grupoEscolha=null  → sempre recebe
  - Espada Longa          | obrigatorio=false | grupoEscolha=1     → escolhe UM do grupo 1
  - Machado de Batalha    | obrigatorio=false | grupoEscolha=1     → escolhe UM do grupo 1
  - Martelo de Guerra     | obrigatorio=false | grupoEscolha=1     → escolhe UM do grupo 1
```

Na criacao da ficha (Spec 006 Wizard), o sistema:
1. Adiciona automaticamente todos os itens `obrigatorio=true`
2. Apresenta ao Jogador um seletor para cada `grupoEscolha` distinto

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `model/ClasseEquipamentoInicial.java` | Entidade |
| `repository/ClasseEquipamentoInicialRepository.java` | JPA repository |
| `service/ClasseEquipamentoInicialService.java` | CRUD + validacoes |
| `dto/request/configuracao/ClasseEquipamentoInicialRequest.java` | Record |
| `dto/request/configuracao/ClasseEquipamentoInicialUpdateRequest.java` | Record parcial |
| `dto/response/configuracao/ClasseEquipamentoInicialResponse.java` | Record com item resumido |
| `mapper/configuracao/ClasseEquipamentoInicialMapper.java` | MapStruct |
| `controller/configuracao/ClasseEquipamentoInicialController.java` | Sub-recurso de ClassePersonagem |

---

## DTO

```java
public record ClasseEquipamentoInicialRequest(
    @NotNull Long itemConfigId,
    @NotNull Boolean obrigatorio,
    Integer grupoEscolha,   // nullable
    @Min(1) @Max(99) int quantidade
) {}
```

```java
public record ClasseEquipamentoInicialResponse(
    Long id,
    Long classeId,
    String classeNome,
    Long itemConfigId,
    String itemConfigNome,
    String itemRaridade,
    String itemRaridadeCor,
    String itemCategoria,
    boolean obrigatorio,
    Integer grupoEscolha,
    int quantidade,
    LocalDateTime dataCriacao
) {}
```

---

## Endpoints

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | MESTRE, JOGADOR | Listar equipamentos iniciais da classe |
| POST | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | MESTRE | Adicionar item aos equipamentos iniciais |
| PUT | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | MESTRE | Atualizar (quantidade, grupo, obrigatorio) |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | MESTRE | Remover (soft delete) |

---

## Regras de Negocio

- **RN-T3-01:** `itemConfig.jogo == classe.jogo` — validar mesmo jogo → HTTP 422 se diferentes
- **RN-T3-02:** Classe pode ter multiplos equipamentos obrigatorios (sem limite)
- **RN-T3-03:** Um `grupoEscolha` pode ter quantos itens o Mestre desejar (minimo 2 faz sentido, mas sem restricao tecnica)
- **RN-T3-04:** `obrigatorio=true` e `grupoEscolha != null` e situacao permitida para flexibilidade do Mestre, mas semanticamente contraditoria (obrigatorio num grupo de escolha = sempre recebe E tem opcao). Documentar no Swagger.
- **RN-T3-05:** Nao ha constraint de unicidade (mesmo item pode ser adicionado duas vezes com grupos diferentes)
- **RN-T3-06:** DELETE faz soft delete. Fichas ja criadas nao perdem os itens que receberam.

---

## Query no Repository

```java
// ClasseEquipamentoInicialRepository
List<ClasseEquipamentoInicial> findByClasseIdOrderByGrupoEscolhaNullsFirstAndNome(Long classeId);

// Ou via JPQL:
@Query("""
    SELECT c FROM ClasseEquipamentoInicial c
    JOIN FETCH c.itemConfig i
    JOIN FETCH i.raridade
    JOIN FETCH i.tipo
    WHERE c.classe.id = :classeId
    AND c.deletedAt IS NULL
    ORDER BY c.obrigatorio DESC, c.grupoEscolha NULLS FIRST, i.nome
    """)
List<ClasseEquipamentoInicial> findByClasseIdWithItems(@Param("classeId") Long classeId);
```

---

## Criterios de Aceitacao

- [ ] POST cria ClasseEquipamentoInicial com item do mesmo jogo que a classe
- [ ] POST com item de jogo diferente retorna HTTP 422
- [ ] GET lista equipamentos da classe com dados do item (nome, raridade, tipo) sem N+1
- [ ] DELETE faz soft delete; a ficha de personagem ja criada nao e afetada
- [ ] PUT atualiza quantidade e grupoEscolha corretamente
- [ ] Listagem retorna itens obrigatorios primeiro, depois por grupo de escolha
- [ ] `./mvnw test` passa sem regressao

---

*Produzido por: Business Analyst/PO | 2026-04-04*
