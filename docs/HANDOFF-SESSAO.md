# Handoff de Sessao — 2026-04-15 (sessao 20 — Wave P0 + P1w2 + P1w3 entregues)

> Branch atual: `main`
> Backend: **832 testes** passando, 0 falhas (sem alteracoes na wave 3)
> Frontend: **~1433 testes** passando (+73 wave 3 sobre ~1360 wave 2; 2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 4: Wave P0 + Wave P1w2 + Wave P1w3 concluidas — **13/13 tasks entregues** (5 P0 + 4 P1w2 + 4 P1w3).
> Ultima atualizacao: 2026-04-15

---

## Resumo Executivo

**Sessao 2026-04-14/15 (sessao 20, Wave P0 + Wave P1 wave 2 + Wave P1 wave 3)** entregou todas as 13 tasks acumuladas (5 P0 + 4 P1w2 + 4 P1w3):

### Wave P0 (anterior)
1. **S007-T10 FE** — FormulaEditor para FORMULA_CUSTOMIZADA (select campo-alvo + input formula + hint variaveis), 54 testes
2. **S015-T4 BE** — Auto-concessao vantagens pre-definidas — ja existia como `OrigemVantagem`, faltava expor campo `origem` no FichaVantagemResponse (commit `4c04a54`)
3. **S023-BE** — Pre-requisitos polimorficos: 6 tipos (VANTAGEM/RACA/CLASSE/ATRIBUTO/NIVEL/APTIDAO), OR dentro do tipo / AND entre tipos, 409 em delecao. +18 testes -> 814 total (commit `934eaff`)
4. **UX-JOGO-SELECT + UX-COR-PREVIEW FE** — Game selector no ConfigLayoutComponent + p-colorpicker bidirecional em raridades. +22 testes -> 1258 total
5. **NPC-FORM-CAMPOS FE** — Ja pre-implementado. Adicionados 2 testes de cobertura (racaId/classeId)

### Wave P1 wave 2
6. **S023-FE** — Aba pre-requisitos polimorfica + chips removiveis por tipo, 56 testes (commit `d08d1c9`)
7. **UX mass fix** — acceptButtonProps corrigido + dialog widths padronizados em 13-14 telas (commit `141b054`)
8. **NPC dificuldade config BE** — NpcDificuldadeConfig + enum FocoNpc (FISICO/MAGICO), +18 testes -> 832 total
9. **Migracao BaseConfigComponent** — habilidades-config + tipos-item-config migrados, +23 testes cada (commit `f30e74d`)

### Wave P1 wave 3 (nova — 2026-04-15)
10. **GAP-NPC-FE-01** — Selector de dificuldade NPC + auto-fill de atributos no form, +31 testes
11. **GAP-XP-01** — Tela Mestre Conceder XP (fix endpoint correto + 14 testes integrados)
12. **GAP-INS-01** — UI Conceder Insolitus (UI ja existia, testes adicionados — incluido em GAP-XP-01)
13. **GAP-PROS-01** — Painel prospeccao pendentes (novo componente + rota + entrada na sidebar), +28 testes

**Total wave 3:** +73 testes frontend (~1360 -> ~1433). Backend inalterado (832).

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

### Wave P1 wave 3 — CONCLUIDA (sessao 20, 2026-04-15)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 10 | GAP-NPC-FE-01 | FE | CONCLUIDO (+31 testes — selector + auto-fill atributos) |
| 11 | GAP-XP-01 | FE | CONCLUIDO (+14 testes — fix endpoint Conceder XP) |
| 12 | GAP-INS-01 | FE | CONCLUIDO (UI ja existia, testes incluidos em GAP-XP-01) |
| 13 | GAP-PROS-01 | FE | CONCLUIDO (+28 testes — novo painel + rota + sidebar) |

### Wave P1 — PENDENTES (proxima rodada)

| # | ID | Tipo | Descricao | Prio | Dependencia |
|---|-----|------|-----------|------|-------------|
| 14 | UX-TIPO-VANTAGEM | FE | tipoVantagem no form criacao vantagem (Insolitus) | P1 | Nenhuma |
| 15 | UX-NIVEL-MIN-PREREQ | FE | nivelMinimo na lista pre-req tipo VANTAGEM | P1 | S023-FE OK |

### Wave P2 — POS

| # | ID | Tipo | Descricao | Prio |
|---|-----|------|-----------|------|
| 16 | GAP-DASH-01 | FE | Tela Mestre `GET /jogos/{id}/dashboard` | P2 |
| 17 | GAP-EXPRT-01 | FE | Interface para endpoints Export/Import config | P2 |
| 18 | AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | P2 |
| 19 | UX-BASE-COMP | FE | Migrar telas restantes (itens-config + raridades-item-config) para BaseConfigComponent | P2 |
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

### GAPs de auditoria BE->FE (sessao 20)

| ID | Descricao | Prio | Status |
|----|-----------|------|--------|
| GAP-XP-01 | Conceder XP — `PUT /fichas/{id}/xp` | P1 | **CONCLUIDO** (wave 3, +14 testes) |
| GAP-INS-01 | Conceder Insolitus — UI + service FE | P1 | **CONCLUIDO** (wave 3, UI ja existia + testes) |
| GAP-PROS-01 | Prospeccao pendentes — `GET /jogos/{id}/prospeccao/pendentes` | P1 | **CONCLUIDO** (wave 3, +28 testes) |
| GAP-NPC-FE-01 | NPC dificuldade FE — selector + auto-fill atributos | P1 | **CONCLUIDO** (wave 3, +31 testes) |
| GAP-DASH-01 | Dashboard Mestre — `GET /jogos/{id}/dashboard` sem tela | P2 | [PENDENTE] |
| GAP-EXPRT-01 | Export/Import config — endpoints sem interface | P2 | [PENDENTE] |

### PAs RESOLVIDOS (sessoes 19+20)
- **PA-R02-01**: Spec 016 T5 — RESOLVIDO
- **PA-021-03**: Tela habilidades JOGADOR — RESOLVIDO
- **PA-004**: FormulaEditor campo-alvo — RESOLVIDO (S007-T10)
- **PA-015-04**: Enum OrigemFichaVantagem — RESOLVIDO (S015-T4)
- **Spec 023 FE** — RESOLVIDO (wave 2)
- **GAP-XP-01 / GAP-INS-01 / GAP-PROS-01 / GAP-NPC-FE-01** — RESOLVIDOS (wave 3)

---

## Proxima Sessao — Sprint 4 Restante

Foco curto: UX-TIPO-VANTAGEM e UX-NIVEL-MIN-PREREQ (P1 menores).
Depois: P2 (GAP-DASH-01, GAP-EXPRT-01, AUDIT-BE-FE, UX-BASE-COMP itens/raridades, UX-PREREQ-EMPTY).

---

## Observacoes Tecnicas

- Backend: 832 testes (+18 NPC-DIFICULDADE-BE wave 2; sem alteracoes na wave 3), 0 falhas
- Frontend: **~1433 testes** (+73 wave 3: GAP-NPC-FE-01 +31, GAP-XP-01/INS-01 +14, GAP-PROS-01 +28)
- Schema: `ddl-auto=update` — Spec 023 + NPC dificuldade sem migracao manual
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- ficha-vantagens-tab: 2 falhas pre-existentes (nao bloqueia)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado
- OAuth2 com timeouts de 15s no token exchange e userinfo
