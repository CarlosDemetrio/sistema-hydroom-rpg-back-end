# Spec 007 — Tasks Index

> Spec: `007-vantagem-efeito`
> Total de tasks: 13
> Status geral: Nao iniciado

---

## Fase P0 — Pre-requisito critico (T0)

> T0 BLOQUEIA TODAS AS OUTRAS TASKS. Deve ser concluida e validada antes de iniciar T1.

| Task | Titulo | Dependencias | Status | Bugs corrigidos |
|------|--------|-------------|--------|----------------|
| [T0](P0-T0-corrigir-bugs-calc-base.md) | Corrigir bugs pre-existentes no FichaCalculationService | — | Pendente | GAP-CALC-01, 02, 03, 06, 07, 08 |

**Escopo de T0:**
- **GAP-CALC-01:** `FichaBonus.classe` = `ClasseBonus.valorPorNivel * ficha.nivel` (nunca calculado)
- **GAP-CALC-02:** `FichaAptidao.classe` = `ClasseAptidaoBonus.bonus` (nunca calculado)
- **GAP-CALC-03:** `FichaAtributo.outros` = `RacaBonusAtributo.bonus` (nunca aplicado)
- **GAP-CALC-06:** `Ficha.nivel` recalculado automaticamente ao ganhar XP (ausente no `FichaService`)
- **GAP-CALC-07:** `FichaAmeaca.recalcularTotal()` nao incluia `nivel`
- **GAP-CALC-08:** `FichaVida.recalcularTotal()` ignorava `vigorTotal` e `nivel`

**Fora do escopo de T0:**
- GAP-CALC-09 (VIG/SAB hardcoded): aguarda decisao do PO (PA-006)

---

## Fase Backend (T1–T8)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T1](P1-T1-adaptar-modelo-dados.md) | Adaptar modelo de dados (SCHEMA-01, SCHEMA-02, FichaProspeccao, queries) | T0 | Pendente |
| [T2](P1-T2-bonus-atributo-aptidao.md) | FichaCalculationService — BONUS_ATRIBUTO e BONUS_APTIDAO | T1 | Pendente |
| [T3](P1-T3-bonus-vida-essencia.md) | FichaCalculationService — BONUS_VIDA e BONUS_ESSENCIA | T1 | Pendente |
| [T4](P1-T4-bonus-derivado-vida-membro.md) | FichaCalculationService — BONUS_DERIVADO e BONUS_VIDA_MEMBRO | T1 | Pendente |
| [T5](P1-T5-dado-up.md) | FichaCalculationService — DADO_UP | T1 | Pendente |
| [T6](P1-T6-formula-customizada.md) | FichaCalculationService — FORMULA_CUSTOMIZADA | T1 | Pendente |
| [T7](P1-T7-insolitus.md) | Insolitus — campo tipoVantagem + endpoint de concessao | T1 | Pendente |
| [T8](P1-T8-testes-integracao.md) | Testes de integracao para todos os tipos de efeito | T2–T7 | Pendente |

**Mudancas de schema em T1:**
- **SCHEMA-01:** `FichaAptidao.outros` (campo ausente — necessario para BONUS_APTIDAO)
- **SCHEMA-02:** `FichaVidaMembro.bonus_vantagens` (campo ausente — necessario para BONUS_VIDA_MEMBRO)

## Fase Frontend (T9–T12)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T9](P2-T9-vantagens-config-efeitos-ui.md) | VantagensConfigComponent — secao de efeitos | T8 | Pendente |
| [T10](P2-T10-formula-editor-efeito.md) | FormulaEditor integrado para FORMULA_CUSTOMIZADA | T9 | Pendente |
| [T11](P2-T11-dado-up-seletor.md) | Seletor de dado para DADO_UP | T9 | Pendente |
| [T12](P2-T12-insolitus-ui.md) | UI de concessao de Insolitus pelo Mestre | T7, T9 | Pendente |

---

## Grafo de dependencias

```
T0 (bugs base)
 └─ T1 (schema + model)
     ├─ T2 (BONUS_ATRIBUTO, BONUS_APTIDAO)
     ├─ T3 (BONUS_VIDA, BONUS_ESSENCIA)
     ├─ T4 (BONUS_DERIVADO, BONUS_VIDA_MEMBRO)
     ├─ T5 (DADO_UP)
     ├─ T6 (FORMULA_CUSTOMIZADA)
     └─ T7 (Insolitus)
          └─ T8 (testes integracao — espera T2–T7)
               └─ T9, T10, T11, T12 (Frontend)
```

---

## Pontos em Aberto (confirmar antes de iniciar as tasks indicadas)

- **PA-001:** FichaInsolitus pode ser removida pelo Mestre? (afeta T7 e T12)
- **PA-002:** Enum TipoVantagem vs boolean isInsolitus? (afeta T7)
- **PA-004:** FORMULA_CUSTOMIZADA sem alvo definido: onde aplica o resultado? (afeta T6)
- **PA-006 (GAP-CALC-09):** VIG e SAB hardcoded por abreviacao — manter convencao ou tornar configuravel? (afeta arquitetura de longo prazo, nao bloqueia as tasks atuais)

---

*Produzido por: Business Analyst/PO | 2026-04-02 | Revisado: 2026-04-03 — adicionada T0 (P0, 6 bugs), totalizando 13 tasks*
