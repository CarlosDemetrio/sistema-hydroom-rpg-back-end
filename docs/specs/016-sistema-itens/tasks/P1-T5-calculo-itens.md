# T5 — FichaCalculationService: Passo 6 — Aplicar ItemEfeito de Itens Equipados

> Fase: Backend — Motor de Calculos | Prioridade: P1
> Dependencias: T4 (FichaItem implementado), Spec 007 T0 (motor corrigido e estavel)
> Bloqueia: T7 (testes)
> Estimativa: 2 dias

---

## Objetivo

Adicionar o Passo 6 ao `FichaCalculationService`: aplicar os efeitos (`ItemEfeito`) de todos os itens equipados (`FichaItem.equipado=true`) na ficha. Os bonus de itens alimentam os novos campos `itens` adicionados as entidades de ficha (`FichaAtributo.itens`, `FichaBonus.itens`, `FichaVida.itens`, `FichaEssencia.itens`). Estes campos sao distintos dos campos `vantagens` e `classe` para permitir visibilidade de fonte na UI.

---

## Schema Changes Necessarias (SCHEMA-016-01)

Antes de implementar o calculo, adicionar os campos `itens` nas entidades de ficha:

| Entidade | Campo Novo | Tipo | Default |
|----------|-----------|------|---------|
| `FichaAtributo` | `itens` | int | 0 |
| `FichaBonus` | `itens` | int | 0 |
| `FichaVida` | `itens` | int | 0 |
| `FichaEssencia` | `itens` | int | 0 |

> **PA-016-03 (pendente):** `FichaAptidao.itens` — adicionar se PO confirmar que itens podem dar bonus em aptidoes.

**Migration H2 (testes) — adicionar a schema.sql de teste ou via @Transactional ddl-auto:**
```sql
ALTER TABLE ficha_atributos ADD COLUMN itens INT NOT NULL DEFAULT 0;
ALTER TABLE ficha_bonus ADD COLUMN itens INT NOT NULL DEFAULT 0;
ALTER TABLE ficha_vida ADD COLUMN itens INT NOT NULL DEFAULT 0;
ALTER TABLE ficha_essencias ADD COLUMN itens INT NOT NULL DEFAULT 0;
```

**Nota sobre total:** O campo `total` ja existente nas entidades precisa incluir `itens` em seu calculo. Revisar `calcularTotal()` em cada entidade afetada:
- `FichaAtributo.total = base + nivel + outros + itens` (adicionar `itens`)
- `FichaBonus.total = base + vantagens + classe + itens + gloria + outros`
- `FichaVida.total = vigorTotal + nivel + vt + renascimentos + itens + outros`
- `FichaEssencia.total = floor((VIG+SAB)/2) + nivel + renascimentos + vantagens + itens + outros`

---

## Sequencia de Calculo Completa Apos T5

```
Passo 1: reset — zera todos os campos derivaveis
         (inclui: outros, vantagens, classe, itens → todos zerados)
Passo 2: aplicar RacaBonusAtributo → FichaAtributo.outros
Passo 3: aplicar ClasseBonus → FichaBonus.classe
Passo 4: aplicar ClasseAptidaoBonus → FichaAptidao.classe
Passo 5: aplicar VantagemEfeito (Spec 007) → vantagens, vt, etc.
Passo 6: aplicar ItemEfeito de itens equipados → itens [NOVO]
Passo 7: calcular totais de atributos (base + nivel + outros + itens)
Passo 8: calcular formulas de Impeto (usando AtributoConfig.formulaImpeto)
Passo 9: calcular BonusConfig (formulas exp4j)
Passo 10: calcular VidaTotal, EssenciaTotal, AmeacaTotal
```

---

## Implementacao

### Passo 1: Atualizar resetarCamposDerivaveis()

```java
private void resetarCamposDerivaveis(
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,
        List<FichaBonus> bonus,
        FichaVida vida,
        FichaEssencia essencia) {

    // Existentes (Spec 007 T0)
    atributos.forEach(a -> {
        a.setOutros(0);
        a.setItens(0);  // NOVO
    });
    bonus.forEach(b -> {
        b.setVantagens(0);
        b.setClasse(0);
        b.setItens(0);  // NOVO
    });
    aptidoes.forEach(a -> a.setClasse(0));
    vida.setVt(0);
    vida.setItens(0);        // NOVO
    essencia.setVantagens(0);
    essencia.setItens(0);    // NOVO
}
```

### Passo 6: aplicarItemEfeito()

```java
/**
 * Passo 6 da sequencia de calculo.
 * Aplica os efeitos de todos os itens EQUIPADOS (e nao quebrados) da ficha.
 *
 * Pre-condicao: campos `itens` ja foram zerados no Passo 1 (idempotencia garantida).
 *
 * @param itensEquipados Lista de FichaItem com equipado=true, carregada via JOIN FETCH com ItemConfig+ItemEfeito
 */
private void aplicarItemEfeito(
        List<FichaItem> itensEquipados,
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,
        List<FichaBonus> bonus,
        FichaVida vida,
        FichaEssencia essencia,
        Ficha ficha) {

    Map<Long, FichaAtributo> atributosMap = buildAtributosMap(atributos);
    Map<Long, FichaBonus> bonusMap = buildBonusMap(bonus);
    Map<Long, FichaAptidao> aptidoesMap = buildAptidoesMap(aptidoes);

    for (FichaItem fichaItem : itensEquipados) {
        // Garantia extra: nunca processar item quebrado mesmo que equipado=true
        if (fichaItem.getDuracaoAtual() != null && fichaItem.getDuracaoAtual() <= 0) {
            log.warn("FichaItem ID {} tem equipado=true mas duracaoAtual=0 — ignorado no calculo",
                fichaItem.getId());
            continue;
        }

        if (fichaItem.getItemConfig() == null) continue; // item customizado sem efeitos no MVP

        for (ItemEfeito efeito : fichaItem.getItemConfig().getEfeitos()) {
            switch (efeito.getTipoEfeito()) {
                case BONUS_ATRIBUTO -> {
                    if (efeito.getAtributoAlvo() == null) break;
                    FichaAtributo alvo = atributosMap.get(efeito.getAtributoAlvo().getId());
                    if (alvo != null && efeito.getValorFixo() != null) {
                        alvo.setItens(alvo.getItens() + efeito.getValorFixo());
                    }
                }
                case BONUS_DERIVADO -> {
                    if (efeito.getBonusAlvo() == null) break;
                    FichaBonus alvo = bonusMap.get(efeito.getBonusAlvo().getId());
                    if (alvo != null && efeito.getValorFixo() != null) {
                        alvo.setItens(alvo.getItens() + efeito.getValorFixo());
                    }
                }
                case BONUS_APTIDAO -> {
                    if (efeito.getAptidaoAlvo() == null) break;
                    FichaAptidao alvo = aptidoesMap.get(efeito.getAptidaoAlvo().getId());
                    if (alvo != null && efeito.getValorFixo() != null) {
                        // PA-016-03: campo itens em FichaAptidao (aguardar confirmacao)
                        // alvo.setItens(alvo.getItens() + efeito.getValorFixo());
                        log.info("BONUS_APTIDAO de item ignorado — aguardando decisao PA-016-03");
                    }
                }
                case BONUS_VIDA -> {
                    if (efeito.getValorFixo() != null) {
                        vida.setItens(vida.getItens() + efeito.getValorFixo());
                    }
                }
                case BONUS_ESSENCIA -> {
                    if (efeito.getValorFixo() != null) {
                        essencia.setItens(essencia.getItens() + efeito.getValorFixo());
                    }
                }
                case FORMULA_CUSTOMIZADA -> aplicarFormulaItemEfeito(efeito, ficha, atributosMap, bonusMap);
                case EFEITO_DADO -> aplicarEfeitoDadoItem(efeito, ficha);
            }
        }
    }
}
```

### aplicarFormulaItemEfeito()

```java
private void aplicarFormulaItemEfeito(
        ItemEfeito efeito,
        Ficha ficha,
        Map<Long, FichaAtributo> atributosMap,
        Map<Long, FichaBonus> bonusMap) {

    if (efeito.getFormula() == null) return;

    try {
        // Variaveis disponiveis: nivel_personagem, siglas de atributos
        Map<String, Double> variaveis = new HashMap<>();
        variaveis.put("nivel_personagem", (double) ficha.getNivel());

        // Adicionar totais de atributos como variaveis (usando abreviacao como nome de variavel)
        atributosMap.values().forEach(fa -> {
            if (fa.getAtributoConfig() != null && fa.getAtributoConfig().getAbreviacao() != null) {
                variaveis.put(fa.getAtributoConfig().getAbreviacao(), (double) fa.getTotal());
            }
        });

        double resultado = formulaEvaluatorService.avaliar(efeito.getFormula(), variaveis);
        int valorInteiro = (int) Math.floor(resultado);

        // Aplicar no alvo (se definido)
        if (efeito.getBonusAlvo() != null) {
            FichaBonus alvo = bonusMap.get(efeito.getBonusAlvo().getId());
            if (alvo != null) alvo.setItens(alvo.getItens() + valorInteiro);
        } else if (efeito.getAtributoAlvo() != null) {
            FichaAtributo alvo = atributosMap.get(efeito.getAtributoAlvo().getId());
            if (alvo != null) alvo.setItens(alvo.getItens() + valorInteiro);
        }
    } catch (Exception e) {
        log.error("Erro ao avaliar formula de ItemEfeito ID {}: {}", efeito.getId(), e.getMessage());
    }
}
```

---

## Carregamento dos Itens (sem N+1)

O `FichaService` deve passar a lista de itens equipados para `recalcular()`. A query deve carregar tudo via JOIN FETCH:

```java
// FichaItemRepository
@Query("""
    SELECT fi FROM FichaItem fi
    JOIN FETCH fi.itemConfig ic
    JOIN FETCH ic.efeitos e
    LEFT JOIN FETCH e.atributoAlvo
    LEFT JOIN FETCH e.bonusAlvo
    LEFT JOIN FETCH e.aptidaoAlvo
    WHERE fi.ficha.id = :fichaId
    AND fi.equipado = true
    AND fi.deletedAt IS NULL
    AND (fi.duracaoAtual IS NULL OR fi.duracaoAtual > 0)
    """)
List<FichaItem> findEquipadosWithEfeitos(@Param("fichaId") Long fichaId);
```

### Assinatura atualizada de recalcular()

```java
public void recalcular(
        Ficha ficha,
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia,
        FichaAmeaca ameaca,
        List<ClasseBonus> classeBonus,
        List<ClasseAptidaoBonus> classeAptidaoBonus,
        List<FichaVantagem> fichaVantagens, // Spec 007
        List<FichaItem> itensEquipados      // NOVO — T5
) { ... }
```

---

## Regras de Negocio

- **RN-T5-01:** Reset obrigatorio no Passo 1: `itens` deve ser zerado antes de qualquer calculo (idempotencia)
- **RN-T5-02:** Item com `duracaoAtual=0` NUNCA contribui para o calculo, mesmo que `equipado=true` (defensivo)
- **RN-T5-03:** Item customizado (itemConfig=null) nao tem efeitos automaticos no MVP — apenas informativo
- **RN-T5-04:** Formula de item usa `nivel_personagem` (nivel da ficha), nao nivel da vantagem (diferente de VantagemEfeito)
- **RN-T5-05:** FORMULA_CUSTOMIZADA com erro de avaliacao e logado mas nao lanca exception (degradacao gracil)
- **RN-T5-06:** `total` de cada entidade de ficha deve incluir `itens` no calculo

---

## Criterios de Aceitacao

- [ ] `FichaAtributo.itens`, `FichaBonus.itens`, `FichaVida.itens`, `FichaEssencia.itens` adicionados as entidades
- [ ] Campo `total` de cada entidade inclui `itens` na soma
- [ ] `resetarCamposDerivaveis()` zera o campo `itens` em todas as entidades
- [ ] Equipar item com BONUS_DERIVADO valorFixo=2: FichaBonus.itens == 2 apos recalculo
- [ ] Desequipar o mesmo item: FichaBonus.itens == 0 apos recalculo (idempotencia)
- [ ] Dois itens equipados com BONUS_DERIVADO valorFixo=1 cada: FichaBonus.itens == 2
- [ ] Recalcular() chamado duas vezes nao acumula
- [ ] Item quebrado (duracaoAtual=0) ignorado no calculo mesmo com equipado=true
- [ ] Item customizado (itemConfig=null) nao causa NullPointerException
- [ ] `./mvnw test` passa sem regressao (457+ testes)

---

*Produzido por: Business Analyst/PO | 2026-04-04*
