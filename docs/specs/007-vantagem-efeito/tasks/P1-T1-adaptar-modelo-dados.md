# T1 — Adaptar Modelo de Dados para Efeitos de Vantagem

> Fase: Backend | Dependencias: P0-T0 (deve ser concluida antes) | Bloqueia: T2, T3, T4, T5, T6
> Estimativa: 2–3 horas

---

## Objetivo

Preparar o modelo de dados e os repositorios para que o `FichaCalculationService` possa processar os 8 tipos de `VantagemEfeito`. Esta task nao implementa logica de calculo — apenas cria os campos e queries necessarios.

---

## Contexto

O `FichaCalculationService.recalcular()` recebe atualmente:
```java
recalcular(Ficha, List<FichaAtributo>, List<FichaBonus>, FichaVida, List<FichaVidaMembro>, FichaEssencia, FichaAmeaca)
```

Ele **nao** recebe `List<FichaVantagem>`, portanto nunca processa efeitos de vantagem. Alem disso, duas entidades de ficha estao sem campos necessarios para receber bonus de vantagem — esses campos sao pre-requisito para que os tipos de efeito `BONUS_APTIDAO` e `BONUS_VIDA_MEMBRO` funcionem.

> **Pre-requisito:** A task P0-T0 deve ser concluida antes desta task. P0-T0 corrige os bugs de ClasseBonus, RacaBonus e entidades inconsistentes. T1 assume que o modelo de dados de entidades existentes ja esta correto.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `model/FichaAptidao.java` | Adicionar campo `outros` (SCHEMA-01 — obrigatorio) |
| `model/FichaVidaMembro.java` | Adicionar campo `bonusVantagens` (SCHEMA-02 — obrigatorio) |
| `model/FichaProspeccao.java` | Adicionar campo `dadoDisponivel` (FK → DadoProspeccaoConfig, nullable) |
| `repository/FichaVantagemRepository.java` | Adicionar query `findByFichaIdWithEfeitos` |
| `service/FichaCalculationService.java` | Adicionar parametro `List<FichaVantagem>` ao `recalcular()` + metodo stub `aplicarEfeitosVantagens()` |

---

## Passos de Implementacao

### Passo 0 — Migrations de Schema (SCHEMA-01 e SCHEMA-02)

> Estes dois campos sao pre-requisito absoluto para os tipos de efeito `BONUS_APTIDAO` e `BONUS_VIDA_MEMBRO`. Devem ser o primeiro passo desta task.

**SCHEMA-01 — FichaAptidao.outros:**

A entidade `FichaAptidao` tem campos `base`, `sorte`, `classe`, `total` mas nao tem campo `outros`. O tipo `BONUS_APTIDAO` precisa de um campo dedicado para receber bonus de VantagemEfeito separado de `sorte` e `classe`.

```sql
-- Para testes (H2) e producao (PostgreSQL):
ALTER TABLE ficha_aptidoes ADD COLUMN outros INTEGER NOT NULL DEFAULT 0;
```

**SCHEMA-02 — FichaVidaMembro.bonus_vantagens:**

A entidade `FichaVidaMembro` nao tem campo `bonusVantagens`. O tipo `BONUS_VIDA_MEMBRO` precisa de um campo dedicado para acumular bonus de vantagem sem alterar o pool de vida global.

```sql
ALTER TABLE ficha_vida_membros ADD COLUMN bonus_vantagens INTEGER NOT NULL DEFAULT 0;
```

> **Nota sobre H2 (testes):** Com `ddl-auto=create-drop`, o H2 cria o schema via JPA. As anotacoes `@Column` nas entidades sao suficientes — nao e necessario script SQL adicional para testes. Para PostgreSQL (producao), criar migration Flyway se aplicavel ao projeto.

---

### Passo 1 — FichaAptidao: adicionar campo `outros`

```java
// Adicionar em FichaAptidao apos campo `classe`:

@NotNull
@Builder.Default
@Column(name = "outros", nullable = false)
private Integer outros = 0;
```

Atualizar `recalcularTotal()`:
```java
public void recalcularTotal() {
    this.total = (base != null ? base : 0) +
                 (sorte != null ? sorte : 0) +
                 (classe != null ? classe : 0) +
                 (outros != null ? outros : 0);
}
```

### Passo 2 — FichaVidaMembro: adicionar campo `bonusVantagens`

```java
// Adicionar em FichaVidaMembro apos campo `danoRecebido`:

@NotNull
@Builder.Default
@Column(name = "bonus_vantagens", nullable = false)
private Integer bonusVantagens = 0;
```

### Passo 3 — FichaProspeccao: adicionar campo `dadoDisponivel`

```java
// Adicionar em FichaProspeccao apos campo `quantidade`:

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dado_disponivel_id")
private DadoProspeccaoConfig dadoDisponivel; // null = sem DADO_UP ativo
```

### Passo 4 — FichaVantagemRepository: query com efeitos

```java
/**
 * Busca vantagens com JOIN FETCH no VantagemConfig e seus efeitos.
 * Necessario para o FichaCalculationService processar efeitos sem N+1.
 *
 * Atencao: efeitos e uma List, nao Set — nao adicionar outros JOIN FETCH
 * na mesma query para evitar MultipleBagFetchException.
 * Usar query separada se necessario buscar categoria tambem.
 */
@Query("""
    SELECT fv FROM FichaVantagem fv
    JOIN FETCH fv.vantagemConfig vc
    LEFT JOIN FETCH vc.efeitos e
    LEFT JOIN FETCH e.atributoAlvo
    LEFT JOIN FETCH e.aptidaoAlvo
    LEFT JOIN FETCH e.bonusAlvo
    LEFT JOIN FETCH e.membroAlvo
    WHERE fv.ficha.id = :fichaId
    AND fv.deletedAt IS NULL
    """)
List<FichaVantagem> findByFichaIdWithEfeitos(@Param("fichaId") Long fichaId);
```

**Atencao MultipleBagFetchException:** `VantagemConfig.efeitos` e `VantagemConfig.preRequisitos` sao duas `List<>` — nao fazer JOIN FETCH nas duas na mesma query. A query acima carrega apenas `efeitos`. Pre-requisitos nao sao necessarios no calculo.

### Passo 5 — FichaCalculationService: adicionar stub do novo metodo

Adicionar assinatura do metodo `aplicarEfeitosVantagens()` e atualizar `recalcular()`:

```java
// Novo parametro em recalcular():
public void recalcular(
        Ficha ficha,
        List<FichaAtributo> atributos,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia,
        FichaAmeaca ameaca,
        List<FichaVantagem> vantagens) {  // NOVO PARAMETRO

    // NOVA ETAPA 0: aplicar efeitos de vantagem (antes dos calculos)
    if (vantagens != null && !vantagens.isEmpty()) {
        aplicarEfeitosVantagens(vantagens, atributos, bonus, vida, membros, essencia);
    }

    // resto do metodo existente inalterado...
}

// Stub para preenchimento nas tasks T2–T6:
private void aplicarEfeitosVantagens(
        List<FichaVantagem> vantagens,
        List<FichaAtributo> atributos,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia) {
    // TODO: implementado nas tasks T2–T6
}
```

---

## Testes

Nao ha testes dedicados para esta task — os testes de integracao de T8 vao exercitar tudo isso. Verificar manualmente que a compilacao passa apos as mudancas.

---

## Criterios de Aceitacao

- [ ] **SCHEMA-01:** `FichaAptidao` tem campo `outros` com `DEFAULT 0`, `@NotNull` — coluna `ficha_aptidoes.outros` existe
- [ ] **SCHEMA-01:** `FichaAptidao.recalcularTotal()` inclui `outros` na soma: `total = base + sorte + classe + outros`
- [ ] **SCHEMA-02:** `FichaVidaMembro` tem campo `bonusVantagens` com `DEFAULT 0`, `@NotNull` — coluna `ficha_vida_membros.bonus_vantagens` existe
- [ ] `FichaProspeccao` tem campo `dadoDisponivel` como FK nullable para `DadoProspeccaoConfig`
- [ ] `FichaVantagemRepository.findByFichaIdWithEfeitos()` compila e retorna vantagens com efeitos carregados
- [ ] `FichaCalculationService.recalcular()` aceita `List<FichaVantagem>` sem compilacao quebrada
- [ ] `./mvnw test` passa (457 testes existentes nao quebram)
