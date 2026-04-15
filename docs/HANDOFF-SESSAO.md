# Handoff de Sessao — 2026-04-14 (sessao 20 — Wave P1 Sprint 4 entregue)

> Branch atual: `main`
> Backend: **832 testes** passando, 0 falhas
> Frontend: **~1360 testes** passando (2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 4: Wave P0 + Wave P1 concluidas — 9/9 tasks entregues (5 P0 + 4 P1 wave 2).
> Ultima atualizacao: 2026-04-14

---

## Resumo Executivo

**Sessao 2026-04-14 (sessao 20, Wave P0 + Wave P1 wave 2)** entregou todas as 5 tasks P0 e as 4 tasks principais da Wave 2:

### Wave P0 (anterior)
1. **S007-T10 FE** — FormulaEditor para FORMULA_CUSTOMIZADA (select campo-alvo + input formula + hint variaveis), 54 testes
2. **S015-T4 BE** — Auto-concessao vantagens pre-definidas — ja existia como `OrigemVantagem`, faltava expor campo `origem` no FichaVantagemResponse (commit `4c04a54`)
3. **S023-BE** — Pre-requisitos polimorficos: 6 tipos (VANTAGEM/RACA/CLASSE/ATRIBUTO/NIVEL/APTIDAO), OR dentro do tipo / AND entre tipos, 409 em delecao. +18 testes -> 814 total (commit `934eaff`)
4. **UX-JOGO-SELECT + UX-COR-PREVIEW FE** — Game selector no ConfigLayoutComponent + p-colorpicker bidirecional em raridades. +22 testes -> 1258 total
5. **NPC-FORM-CAMPOS FE** — Ja pre-implementado. Adicionados 2 testes de cobertura (racaId/classeId)

### Wave P1 wave 2 (nova)
6. **S023-FE** — Aba pre-requisitos polimorfica + chips removiveis por tipo, 56 testes (commit `d08d1c9`)
7. **UX mass fix** — acceptButtonProps corrigido + dialog widths padronizados em 13-14 telas (commit `141b054`)
8. **NPC dificuldade config BE** — NpcDificuldadeConfig + enum FocoNpc (FISICO/MAGICO), +18 testes -> 832 total
9. **Migracao BaseConfigComponent** — habilidades-config + tipos-item-config migrados, +23 testes cada (commit `f30e74d`)

**Schema:** gerenciado por Hibernate `ddl-auto=update` — SEM migracoes manuais.

---

## Decisoes de PO registradas (sessao 20)

### Spec 023 — Pre-requisitos Polimorficos de Vantagem (IMPLEMENTADO BE+FE)

- `VantagemPreRequisito` refatorado: nova coluna `tipo` + campos nullable por tipo (`raca_id`, `classe_id`, `atributo_id`, `aptidao_id`, `valor_minimo`)
- Logica: **AND entre tipos diferentes, OR entre registros do mesmo tipo**
- `ATRIBUTO` usa `valorBase` (pontos distribuidos), NAO `valorAtual` (pos-bonus)
- Concessao de Insolitus e VantagemPreDefinida (SISTEMA) **ignoram** pre-requisitos
- 409 ao tentar deletar Raca/Classe/Atributo/Aptidao usada como pre-requisito
- FE entregue wave 2: aba polimorfica funcional com chips removiveis

### NPC Dificuldade (NOVO — implementado BE wave 2)
- Nova entidade `NpcDificuldadeConfig` (Facil/Medio/Dificil/Elite/Chefe)
- Enum `FocoNpc` (FISICO/MAGICO) vinculado ao template
- +18 testes -> 832 total
- Frontend (selector + auto-preenchimento) = GAP-NPC-FE-01 (P1)

---

## Estado das Specs (atualizado sessao 20 pos-Wave P1 wave 2)

| Spec | Titulo | Status | Nota |
|------|--------|--------|------|
| 004 | Siglas, formulas, relacionamentos | CONCLUIDO | — |
| 005 | Participantes, aprovacao, permissoes | CONCLUIDO | — |
| 006 | Wizard de criacao de ficha | CONCLUIDO | — |
| 007 | VantagemEfeito + Motor de calculos | CONCLUIDO 13/13 | — |
| 008 | Sub-recursos Classes/Racas (FE) | CONCLUIDO | — |
| 009-ext | NPC Visibility + Prospeccao + Essencia | CONCLUIDO | — |
| 011 | Galeria + Anotacoes | CONCLUIDO 9/9 | — |
| 012 | Niveis e progressao (FE) | CONCLUIDO | — |
| ~~014~~ | ~~Cobertura de testes~~ | **CORTADO** | — |
| 015 | ConfigPontos Classe/Raca | CONCLUIDO 7/7 | — |
| 016 | Sistema de Itens | CONCLUIDO | — |
| 017 | Correcoes Pre-RC | CONCLUIDO | — |
| 018 | Deploy Backend GCP | CONCLUIDO | — |
| 019 | Deploy Frontend Firebase | CONCLUIDO | — |
| 021 | Sistema de Habilidades | CONCLUIDO | — |
| 022 | DefaultGameConfigProvider refactor | CONCLUIDO | — |
| **023** | **Pre-requisitos Polimorficos Vantagem** | **CONCLUIDO BE+FE** | BE commit `934eaff`, FE commit `d08d1c9` (+56 testes) |

**Specs CORTADAS:** ~~010~~ (ADMIN), ~~013~~ (docs), ~~014~~ T2-T4+T6 (cobertura), ~~PA-017-04~~ (export/import)

---

## Backlog Priorizado — Sprint 4 (atualizado pos-Wave P1 wave 2)

### Wave P0 — CONCLUIDA (sessao 20)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 1 | S007-T10 | FE | CONCLUIDO (54 testes, FormulaEditor) |
| 2 | S015-T4 | BE | CONCLUIDO (commit `4c04a54`) |
| 3 | S023-BE | BE | CONCLUIDO (commit `934eaff`, +18 testes) |
| 4 | UX-JOGO-SELECT + UX-COR-PREVIEW | FE | CONCLUIDO (+22 testes) |
| 5 | NPC-FORM-CAMPOS | FE | CONCLUIDO (pre-impl + 2 testes) |

### Wave P1 wave 2 — CONCLUIDA (sessao 20)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 6 | S023-FE | FE | CONCLUIDO (+56 testes, commit `d08d1c9`) |
| 7 | UX mass fix (acceptButtonProps + dialog widths 13-14 telas) | FE | CONCLUIDO (commit `141b054`) |
| 8 | NPC-DIFICULDADE-BE | BE | CONCLUIDO (+18 testes -> 832 total) |
| 9 | Migracao BaseConfigComponent (habilidades + tipos-item) | FE | CONCLUIDO (+23 testes cada, commit `f30e74d`) |

### Wave P1 — PENDENTES (proxima rodada)

| # | ID | Tipo | Descricao | Prio | Dependencia |
|---|-----|------|-----------|------|-------------|
| 10 | GAP-XP-01 | FE | Tela Mestre para `PUT /fichas/{id}/xp` (conceder XP) | P1 | Nenhuma |
| 11 | GAP-INS-01 | FE | UI de selecao/concessao de Insolitus (service FE ja existe) | P1 | Nenhuma |
| 12 | GAP-PROS-01 | FE | Tela Mestre `GET /jogos/{id}/prospeccao/pendentes` | P1 | Nenhuma |
| 13 | GAP-NPC-FE-01 | FE | Selector dificuldade no form NPC + auto-preenchimento atributos | P1 | NPC-DIFICULDADE-BE OK |
| 14 | UX-TIPO-VANTAGEM | FE | tipoVantagem no form criacao vantagem (Insolitus) | P1 | Nenhuma |
| 15 | UX-NIVEL-MIN-PREREQ | FE | nivelMinimo na lista pre-req tipo VANTAGEM | P1 | S023-FE OK |

### Wave P2 — POS

| # | ID | Tipo | Descricao | Prio |
|---|-----|------|-----------|------|
| 16 | GAP-DASH-01 | FE | Tela Mestre `GET /jogos/{id}/dashboard` | P2 |
| 17 | GAP-EXPRT-01 | FE | Interface para endpoints Export/Import config | P2 |
| 18 | AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | P2 |
| 19 | UX-BASE-COMP | FE | Migrar telas restantes (itens, raridades) para BaseConfigComponent | P2 |
| 20 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | P2 |

---

## Bloqueados / Pontos em Aberto

### PAs acumulados

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|----------|--------------|
| PA-R05-01 | FichaPreviewResponse incompleto | Nao | PO decide |
| PA-R05-02 | FichaPreviewService sem testes avancados | Nao | P2 |
| PA-006 | VIG/SAB hardcoded (GAP-CALC-09) | Nao | PO decide |
| PA-017-03 | Reativar SidebarComponent | Nao | Pos-MVP |

### GAPs de auditoria BE->FE (novos — sessao 20)

| ID | Descricao | Prio |
|----|-----------|------|
| GAP-XP-01 | Conceder XP — `PUT /fichas/{id}/xp` sem UI Mestre | P1 |
| GAP-INS-01 | Conceder Insolitus — servico FE existe sem UI | P1 |
| GAP-PROS-01 | Prospeccao pendentes — `GET /jogos/{id}/prospeccao/pendentes` sem tela | P1 |
| GAP-NPC-FE-01 | NPC dificuldade FE — selector no form + auto-preenchimento | P1 |
| GAP-DASH-01 | Dashboard Mestre — `GET /jogos/{id}/dashboard` sem tela | P2 |
| GAP-EXPRT-01 | Export/Import config — endpoints sem interface | P2 |

### PAs RESOLVIDOS (sessoes 19+20)
- **PA-R02-01**: Spec 016 T5 — RESOLVIDO
- **PA-021-03**: Tela habilidades JOGADOR — RESOLVIDO
- **PA-004**: FormulaEditor campo-alvo — RESOLVIDO (S007-T10)
- **PA-015-04**: Enum OrigemFichaVantagem — RESOLVIDO (S015-T4)
- **Spec 023 FE** — RESOLVIDO (wave 2)

---

## Proxima Sessao — Sprint 4 Wave P1 (restante)

Foco: endereçar os 4 GAPs P1 de auditoria BE->FE (XP, Insolitus, Prospeccao, NPC dificuldade FE).
Podem rodar em paralelo (telas independentes).

### Plano Anti-Conflito (paralelo recomendado)

| Agente | Escopo | NAO tocar |
|--------|--------|-----------|
| FE-1 | GAP-XP-01 + GAP-INS-01 (telas Mestre ficha) | NPC*, prospeccao* |
| FE-2 | GAP-PROS-01 (tela prospeccao pendentes) | ficha-detail, NPC |
| FE-3 | GAP-NPC-FE-01 (form NPC dificuldade) | vantagens-config/*, ficha-* |

---

## Observacoes Tecnicas

- Backend: 832 testes (+18 NPC-DIFICULDADE-BE), 0 falhas
- Frontend: ~1360 testes (+56 Spec 023 FE + ~46 wave 2 UX/migracao BaseConfig)
- Schema: `ddl-auto=update` — Spec 023 + NPC dificuldade sem migracao manual
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- ficha-vantagens-tab: 2 falhas pre-existentes (nao bloqueia)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado
- OAuth2 com timeouts de 15s no token exchange e userinfo
