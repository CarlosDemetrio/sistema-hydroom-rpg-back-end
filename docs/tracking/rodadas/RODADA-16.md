# Rodada 16 — 2026-04-15 (PM/Scrum Orchestrator)

> Branch: `main`
> Sessao: 20 (continuacao tarde)
> Backend: 832 testes (sem alteracao)
> Frontend: ~1433 testes (sem alteracao)

---

## Resumo

Sessao curta de PM dedicada a reorganizacao do backlog Sprint 4 P1 apos validacao de duas tasks UX:

1. **UX-NIVEL-MIN-PREREQ** confirmado como ja entregue durante a Spec 023 FE (chip e input ja exibem `nivelMinimo` corretamente). Marcado CONCLUIDO.
2. **Spec 024 — UX Melhorias Sprint 4** criada (BA/PO) para formalizar a unica P1 ainda pendente: UX-TIPO-VANTAGEM (checkbox Insolitus no form de VantagemConfig + coluna Tipo na tabela).

Sem alteracoes em codigo, testes ou builds nesta rodada.

---

## Artefatos Criados

### Spec 024 — UX Melhorias Sprint 4
**Diretorio:** `docs/specs/024-ux-melhorias-sprint4/`

| Arquivo | Descricao |
|---------|-----------|
| `spec.md` | Visao de negocio, atores, regras, criterios de aceite (UX-TIPO-VANTAGEM) |
| `plan.md` | Plano tecnico de implementacao |
| `tasks/INDEX.md` | Indice de tasks (T1 PENDENTE, T2 CONCLUIDA) |
| `tasks/P1-T1-tipo-vantagem-frontend.md` | Task UX-TIPO-VANTAGEM detalhada |

### Status das Tasks da Spec 024

| Task | Titulo | Status | Notas |
|------|--------|--------|-------|
| T1 | UX-TIPO-VANTAGEM (tipoVantagem no form + coluna Tipo) | [PENDENTE] | ~1-2h, sem dependencia, FE puro |
| T2 | UX-NIVEL-MIN-PREREQ (nivelMinimo no chip pre-req) | [CONCLUIDO] | Entregue como parte da Spec 023 FE |

---

## Atualizacoes de Tracking

| Documento | Mudancas |
|-----------|----------|
| `HANDOFF-SESSAO.md` | + secao "Sessao tarde 2026-04-15"; Spec 024 adicionada na tabela de specs; backlog P1 reorganizado |
| `MASTER.md` | rev.20; Spec 024 na quadro completo de specs e na secao de documentacao tecnica; rodape atualizado |
| `PM.md` | rev.20; Spec 024 documentada em "O que esta FEITO" e no backlog P1 |

---

## Backlog Sprint 4 — Estado Apos Esta Rodada

### P1 PENDENTES (proxima rodada)
| ID | Tipo | Descricao | Status |
|----|------|-----------|--------|
| Spec 024 T1 | FE | UX-TIPO-VANTAGEM — checkbox Insolitus no form + coluna Tipo na tabela | [PENDENTE] |

### P2 PENDENTES (pos)
| ID | Tipo | Descricao |
|----|------|-----------|
| GAP-DASH-01 | FE | Tela Mestre `GET /jogos/{id}/dashboard` |
| GAP-EXPRT-01 | FE | Interface para Export/Import config |
| AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela |
| UX-BASE-COMP | FE | Migrar telas restantes (itens-config + raridades-item-config) para BaseConfigComponent |
| UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos |

---

## Proxima Rodada — Foco Recomendado

Implementacao de **Spec 024 T1 (UX-TIPO-VANTAGEM)** — criterios de aceite ja definidos:
- Checkbox "Esta vantagem e Insolitus" no form
- Ao marcar: `formulaCusto` desabilitado e limpo
- Tabela com coluna "Tipo" (label "Insolitus" ou "—")
- Pre-preenchimento na edicao
- Testes Vitest cobrindo criacao, edicao e comportamento do `formulaCusto`

Apos T1: avancar para P2 (GAP-DASH-01, GAP-EXPRT-01, AUDIT-BE-FE, UX-BASE-COMP, UX-PREREQ-EMPTY).

---

*Rodada 16 — PM/Scrum Orchestrator — 2026-04-15*
