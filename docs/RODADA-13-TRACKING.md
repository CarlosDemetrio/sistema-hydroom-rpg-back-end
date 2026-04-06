# Rodada 13 — Sprint 3: Spec 009-ext Frontend Completo

> Iniciado: 2026-04-06 [~13:00]
> Concluido: 2026-04-06 [~16:30]
> Branch: main
> Base: 613B + 748F testes, 0 falhas

---

## Objetivo

Concluir a Spec 009-ext no frontend (T7-T10): NPC Visibility, barra de essencia reativa, fluxo de prospeccao (Jogador + Mestre), e painel de reset do Mestre. Entregar 100% da spec antes de avancar para Spec 012 fase 2.

---

## Tracks

| Track | Spec | Escopo | Status |
|-------|------|--------|--------|
| A — Frontend | 009-ext | T7 NpcVisibilidadeComponent + integracao FichaDetailPage | Concluido |
| B — Frontend | 009-ext | T8 Barra essencia reativa + FichaHeaderComponent.spec | Concluido |
| C — Frontend | 009-ext | T9 ProspeccaoApiService + ProspeccaoComponent (Jogador+Mestre) + aba 5 | Concluido |
| D — Frontend | 009-ext | T10 resetarEstado + botao header + ConfirmationService | Concluido |

---

## Progresso

| Task | Tipo | Status | Horario | Commit | Testes novos |
|------|------|--------|---------|--------|--------------|
| S009-T7  | F | DONE | [~14:00] | 53468a4 | 40 |
| S009-T8  | F | DONE | [~14:45] | c4d75e9 | 14 |
| S009-T9  | F | DONE | [~15:45] | cfe012d | ~30 |
| S009-T10 | F | DONE | [~16:30] | fb22a54 | 10 |

**Frontend: 848 testes passando** (era 748 — +100)
**Backend: 613 testes** (sem alteracao nesta rodada)

---

## Detalhamento das Entregas

### S009-T7 — NPC Visibilidade (commit 53468a4)
- `NpcVisibilidadeComponent` (painel lateral multiselect de jogadores)
- `FichaVisibilidadeApiService` consumindo os 4 endpoints do backend
- Integracao em `FichaDetailPage` com badges para o Jogador (visibilidade granular)
- 40 testes novos

### S009-T8 — Barra Essencia Reativa (commit c4d75e9)
- `essenciaAtual` agora obrigatorio no modelo `FichaResumo`
- `FichaHeaderComponent.spec` novo com cobertura da barra reativa
- 14 testes novos

### S009-T9 — Prospeccao Jogador + Mestre (commit cfe012d)
- `ProspeccaoApiService` (conceder/usar/reverter)
- `ProspeccaoComponent` com dois fluxos: Jogador (usar) e Mestre (conceder + reverter)
- Aba 5 adicionada na ficha-detail
- ~30 testes novos

### S009-T10 — Reset de Estado (commit fb22a54)
- Metodo `resetarEstado()` no service
- Botao reset no `FichaHeaderComponent` com `podeResetar` computed
- Confirmacao via `ConfirmationService` antes de aplicar
- 10 testes novos

---

## Decisao Estrategica — Repriorizacao para Homologacao

Decisao do usuario nesta sessao: **focar a primeira parte para homologacao**. Apos aprovacao da homologacao, retomar specs em stand-by.

### Stand-by (pos-homologacao)
| Spec | Titulo | Razao |
|------|--------|-------|
| 010 | Roles ADMIN/MESTRE/JOGADOR refactor | Refactor transversal (~50 @PreAuthorize), so apos homologar fluxo principal |
| 011 | Galeria de imagens + Anotacoes com pastas | Funcionalidade complementar — nao bloqueia primeira parte |
| 013 | Documentacao tecnica (Javadoc, OpenAPI, TSDoc) | Ja estava em standby — executar apos primeira homologacao |
| 014 | Cobertura de testes (JaCoCo, Vitest) | Ja estava em standby — executar apos primeira homologacao |
| 016 | Sistema de Itens/Equipamentos | Decisao PO 2026-04-06: aguarda RC |

### Sequencia pos-RC (decidida pelo PO em 2026-04-06)
Apos aprovacao da homologacao, retomar specs nesta ordem:
1. Spec 011 — Galeria de imagens + Anotacoes com pastas
2. Spec 016 — Sistema de Itens/Equipamentos
3. Spec 014 — Cobertura de testes (JaCoCo, Vitest)
4. Spec 013 — Documentacao tecnica (Javadoc, OpenAPI, TSDoc)
5. Spec 010 — Roles ADMIN/MESTRE/JOGADOR refactor

### Continua em escopo de RC (primeira parte) — FINAL
- Spec 012 fase 2 (T6-T11): Niveis e progressao frontend (LevelUpDialog, painel XP)
- Spec 015 T6/T7: ClassePontosConfig + RacaPontosConfig frontend
- Estimativa: ~3-4 rodadas

---

## Estimativa para RC (Primeira Parte)

### O que esta pronto (nao precisa de mais trabalho)

**Backend (613 testes, 0 falhas)**
- Spec 003, 004 — refactor + siglas/formulas/relacionamentos
- Spec 005 — Participantes (P1T1/T2/T3 + reactivate strategy)
- Spec 006 — Wizard backend completo (FichaStatus, /completar, /xp, pontosDisponiveis)
- Spec 007 — Motor de calculos (T0-T9, T11, T12). 7/8 TipoEfeito integrados
- Spec 008-old + 009-old — backend ja pre-existente
- Spec 009-ext — backend completo (T1-T6): visivelGlobalmente, FichaVisibilidade, ProspeccaoUso, resetar-estado, essenciaAtual/vidaAtual
- Spec 015 — T1/T2/T3/T5 (4 entidades, 14 endpoints, integracao no FichaResumo, DefaultProvider corrigido)

**Frontend (848 testes, 0 falhas)**
- Sprint 1 — FichaDetail 5 abas, JogosDisponiveis, Perfil
- Sprint 2 — Wizard 6 passos completo, Participantes, VantagensEfeitos UI
- Spec 008 — Sub-recursos Classes/Racas (T1-T4)
- Spec 012 fase 1 — PontosVantagemConfig, CategoriaVantagemConfig, NiveisConfig UX, rotas/sidebar (T1-T4, T14)
- Spec 009-ext — Frontend completo (T7-T10) **entregue nesta rodada**

### O que falta para homologar a primeira parte

| Spec | Tasks restantes | Tipo | Estimativa | Bloqueios |
|------|----------------|------|------------|-----------|
| 012 fase 2 | T6 (modelo FichaResumo 3 pontos) | F | 0.5 rodada | nenhum |
| 012 fase 2 | T7 (painel XP Mestre + level up badge/toast) | F | 0.5 rodada | T6 |
| 012 fase 2 | T8 (LevelUpDialog Step 1 — atributos) | F | 0.5 rodada | T7 |
| 012 fase 2 | T9 (LevelUpDialog Step 2 — aptidoes) | F | 0.5 rodada | T8 |
| 012 fase 2 | T10 (LevelUpDialog Step 3 — vantagens informativo) | F | 0.5 rodada | T9 |
| 012 fase 2 | T11 (saldo vantagens em FichaVantagensTab) | F | 0.5 rodada | T6 |
| 015 T6 | ClassePontosConfig frontend | F | 0.5 rodada | backend T2 OK |
| 015 T7 | RacaPontosConfig frontend | F | 0.5 rodada | backend T2 OK |
| 007 T10 | FormulaEditorEfeito | F | — | BLOQUEADO PA-004 |

**Total tasks restantes para RC primeira parte:** 8 frontend (Spec 016 movida para stand-by por decisao PO 2026-04-06)

### Stand-by (pos-homologacao)
- Spec 010 (Roles refactor) — ~9 tasks
- Spec 011 (Galeria + Anotacoes) — ~8 tasks
- Spec 013 (Documentacao) — ~15-20 tasks (replanejamento T0)
- Spec 014 (Cobertura) — ~6 tasks
- Spec 016 (Sistema de Itens) — 11 tasks (7B + 4F) — decisao PO 2026-04-06
- Spec 007 T10 — bloqueada por PA-004 (decisao PO)

### Estimativa de sessoes restantes para RC

Cenario A — **escolhido pelo PO em 2026-04-06** (sem Spec 016 na primeira parte):
- Spec 012 fase 2 (T6-T11) + Spec 015 T6/T7 = ~3-4 rodadas
- 1 rodada de smoke tests / ajustes finais antes da homologacao
- **Total: 4-5 rodadas (~2-3 sessoes de trabalho)**

Recomendacao do PM (CONFIRMADA pelo PO 2026-04-06): adotar Cenario A. Spec 016 movida para stand-by pos-RC. Sequencia pos-homologacao: 011 -> 016 -> 014 -> 013 -> 010.

---

## Rodada 13 — CONCLUIDA [~16:30]

Todos os 4 tasks da rodada entregues. Commits: 53468a4, c4d75e9, cfe012d, fb22a54.
