# Handoff de Sessao — 2026-04-16 (sessao 22 — Rodada 19: FichaStatus FE + Aba Sessao + S026 T05/T08 + T-025-13)

> Branch atual: `main`
> Backend: **832 testes** pre-R18 + **+21 afetados** em R19 (JogoResumoResponse.criadoEm) — contagem global a reconciliar
> Frontend: entregas R18 + R19 consolidadas (multiplos commits — contagem total a reconciliar)
> Sprint 4: **ENCERRADO NA PRATICA** — todas P0 + todas P1 + 4 de 5 P2 entregues. GAP-EXPRT-01 CANCELADO. R19 fecha residuais de FichaStatus no FE + aba Sessao + badges sidebar. Unico P2 residual: AUDIT-BE-FE (baixa prio) + novo gap: endpoint `estado-combate` para `danoRecebido`.
> Ultima atualizacao: 2026-04-16 [R19 — FichaStatus FE (badges/canEdit/remove Excluir) + Aba Sessao (vida/essencia/membros + polling 30s) + S026-T05/T08 + T-025-13]

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

### Sessao tarde 2026-04-15 — Reorganizacao backlog (PM)
- **Spec 024 — UX Melhorias Sprint 4** criada com `spec.md`, `plan.md`, `tasks/INDEX.md` e `tasks/P1-T1-tipo-vantagem-frontend.md`
  - T1 (UX-TIPO-VANTAGEM): pendente — checkbox Insolitus no form + coluna Tipo na tabela + DTOs (~1-2h)
  - T2 (UX-NIVEL-MIN-PREREQ): registrada como **CONCLUIDA** — entregue como parte da Spec 023 FE (chip e input ja exibem `nivelMinimo` corretamente)
- Backlog Sprint 4 P1 reorganizado: UX-TIPO-VANTAGEM agora rastreado como Spec 024 T1; UX-NIVEL-MIN-PREREQ removido da lista pendente

### Rodada 17 (Sessao 21 tarde 2026-04-15) — Fechamento Spec 024 + P2

**3 agentes em paralelo entregaram 4 tarefas, totalizando +92 testes frontend:**

1. **Spec 024 T1 (UX-TIPO-VANTAGEM)** — CONCLUIDA: checkbox Insolitus no form de `vantagens-config` + coluna Tipo na tabela + comportamento que desabilita/limpa `formulaCusto` ao marcar Insolitus. **+14 testes** (total 70 no componente). 1 commit FE.
2. **UX-BASE-COMP (raridades-item-config)** — CONCLUIDA: migracao para `BaseConfigComponent`. **+27 testes**. Incluido em 3 commits FE.
3. **UX-BASE-COMP (itens-config)** — CONCLUIDA: migracao para `BaseConfigComponent`. **+35 testes**. Incluido nos mesmos 3 commits.
4. **GAP-DASH-01** — CONCLUIDA: Dashboard do Mestre em `/mestre/dashboard` com 3 cards (resumo, fichas por nivel, ultimas alteracoes) + link na sidebar. **+16 testes**. 1 commit FE.
5. **UX-PREREQ-EMPTY** — CONFIRMADA: estado vazio da aba pre-requisitos ja estava implementado no codigo (entregue durante Spec 023 FE). Marcado CONCLUIDO sem trabalho adicional.

**Spec 024 CONCLUIDA (2/2 tasks).** **14/14 P1 entregues.** **P2: 3/5 concluidas, 2 residuais (GAP-EXPRT-01 bloqueado por BE; AUDIT-BE-FE baixa prio).**

### Rodada 18 (Sessao 22 — 2026-04-16) — Pacote UX final + BE FichaStatus/Delete NPC

**Frontend:**
1. [R18] **UX-TIPO-VANTAGEM (consolidado)** — `tipoVantagem` (VANTAGEM | INSOLITUS) no form de VantagemConfig; checkbox Insolitus; coluna Tipo na tabela; DTOs request/response atualizados. Consolida Spec 024 T1 (R17) com DTO update.
2. [R18] **Dashboard Mestre (GAP-DASH-01 consolidado)** — nova tela `/mestre/dashboard` com `JogoDashboard` (fichas ativas, pendentes, NPCs, etc.) + rota + sidebar link.
3. [R18] **Migracao BaseConfig** — `ItensConfigComponent` e `RaridadesItemConfigComponent` migrados para `BaseConfigComponent`. Fila UX-BASE-COMP oficialmente encerrada.
4. [R18] **FE-1 Quick Wins** — remove botoes Exportar/Importar do `ConfigLayoutComponent`; remove coluna "Data Criacao" do `JogosListComponent`; remove setTimeout do `JogoFormComponent`; exclui `ConfigComponent` (codigo morto).
5. [R18] **FE-2 Fixes** — corrige rota do botao Editar em `ficha-detail` (`/fichas/:id/editar` -> `/jogador/fichas/:id/edit`); remove `console.log` do `AuthService`; adiciona edicao de nome em `ProfileComponent` via `PUT /api/v1/usuarios/me`.
6. [R18] **FE-3 Back Buttons (UX-BACK-BUTTON)** — `PageHeaderComponent` com `backRoute` em 12 telas de config + dashboard.
7. [R18] **FE-4 NPC** — botao de exclusao com `ConfirmDialog` + botao voltar em `NpcsComponent`.
8. [R18] **FE-5 Type Fixes** — `TagSeverity`, `Partial<Record>` no level-up, `ProgressBar [color]`, correcoes de import.

**Backend:**
9. [R18] **FichaStatus** — adicionados ATIVA, MORTA, ABANDONADA; COMPLETA marcado @Deprecated. Alinha com decisao PO INCONS-02.
10. [R18] **PUT /fichas/{id}/status** — novo endpoint para Mestre alterar status; novo DTO `AtualizarStatusFichaRequest`.
11. [R18] **excluirNpc** — endpoint DELETE /fichas/{id} restaurado, mas restrito a NPCs (`isNpc=true`). Fichas de jogador continuam nao-deletaveis.

**Schema:** gerenciado por Hibernate `ddl-auto=update` — SEM migracoes manuais.

### Rodada 19 (Sessao 22 continuada — 2026-04-16) — FichaStatus FE + Aba Sessao + S026

Continuacao do pacote UX da R18, fechando os residuais de `FichaStatus` no frontend, implementando a aba Sessao em `ficha-detail`, e entregando Spec 026 T05/T08 + T-025-13.

**Frontend:**
1. [R19 10:—] **T-025-12** — Labels wizard: passo 5 = "Vantagens", passo 6 = "Revisao" (commit `27fd3b8`)
2. [R19 10:—] **T-025-08** — Wizard le fichaId do path param `:id` alem de queryParam (commit `3d04f3b`)
3. [R19 11:—] **T-025-01-FE + T-025-09 + T-025-10** — Badges de status na `fichas-list` (RASCUNHO/ATIVA/MORTA/ABANDONADA), `canEdit()` bloqueia MORTA/ABANDONADA, remove botao Excluir do Jogador (INCONS-02). **+33 testes** (commit `9b5ba1b`)
4. [R19 12:—] **T-025-03 + T-025-04 + T-025-07 + T-025-11** — Aba "Sessao" em `ficha-detail` com vida/essencia/membros do corpo, polling 30s, badge de status no `FichaHeaderComponent`. **+25 testes** (commit `65f0c19`)
5. [R19 13:—] **S026-T08 FE** — Coluna "Data Criacao" restaurada na lista de jogos (commit `ea0bb09`)
6. [R19 13:—] **S026-T05** — Badge de participantes pendentes no sidebar do Mestre (commit `aaa1abc`)
7. [R19 13:—] **T-025-13** — Remove item "Criar Ficha" do sidebar do Jogador (commit `aaa1abc`)

**Backend:**
8. [R19 14:—] **S026-T08 BE** — `criadoEm` adicionado ao `JogoResumoResponse` + mapper atualizado. **21 testes passando** nos afetados (commit `2fc8ece`)

**Gap tecnico identificado (R19) — Decisao PO:**

Durante a implementacao da aba Sessao, detectou-se que `FichaResumoResponse` nao expoe `danoRecebido` dos membros do corpo, fazendo o FE iniciar com `dano=0`. **Decisao:** criar endpoint dedicado `GET /fichas/{id}/estado-combate` (ou similar) em R20, em vez de engrossar o `FichaResumoResponse`. Implementacao programada para a proxima rodada.

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
| **024** | **UX Melhorias Sprint 4** | **CONCLUIDO (2/2)** | T1 (UX-TIPO-VANTAGEM) entregue R17 (+14 testes); T2 (UX-NIVEL-MIN-PREREQ) via Spec 023 FE |

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

### Rodada 17 — CONCLUIDA (sessao 21, 2026-04-15)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 14 | Spec 024 T1 (UX-TIPO-VANTAGEM) | FE | **CONCLUIDO** (+14 testes, 1 commit) |
| 15 | UX-BASE-COMP (raridades-item-config) | FE | **CONCLUIDO** (+27 testes) |
| 16 | UX-BASE-COMP (itens-config) | FE | **CONCLUIDO** (+35 testes, incluido nos 3 commits) |
| 17 | GAP-DASH-01 | FE | **CONCLUIDO** (+16 testes, 1 commit) |
| — | UX-PREREQ-EMPTY | FE | **CONCLUIDO** (confirmado ja implementado via Spec 023 FE) |

### Wave P1 — TODAS CONCLUIDAS

Spec 024 T1 era a ultima P1 pendente; entregue na R17. **14/14 P1 entregues.**

### Wave P2 — ESTADO APOS R18

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 1 | GAP-DASH-01 | FE | Dashboard do Mestre (cards + rota + sidebar) | **CONCLUIDO** (R17+R18 consolidado) |
| 2 | UX-BASE-COMP | FE | Migrar telas restantes (itens + raridades) | **CONCLUIDO** (R17+R18 consolidado) |
| 3 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | **CONCLUIDO** (via Spec 023 FE) |
| 4 | GAP-EXPRT-01 | FE | Interface Export/Import config | **CANCELADO (R18)** — botoes removidos no FE-1 Quick Win; BE sem endpoint |
| 5 | AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | [PENDENTE — baixa prio] |

### Rodada 18 — CONCLUIDA (sessao 22, 2026-04-16)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 18.1 | UX-TIPO-VANTAGEM (consolidacao DTOs) | FE | **CONCLUIDO** |
| 18.2 | Dashboard Mestre (JogoDashboard) | FE | **CONCLUIDO** |
| 18.3 | Migracao BaseConfig (itens + raridades) | FE | **CONCLUIDO** |
| 18.4 | FE-1 Quick Wins (UX-EXPORT-IMPORT, UX-DATA-CRIACAO, UX-TIMEOUT-NAV, UX-DEAD-CODE) | FE | **CONCLUIDO** |
| 18.5 | FE-2 Fixes (FE-ROUTE-BUG, FE-CONSOLE-LOG, UX-PROFILE-NOME) | FE | **CONCLUIDO** |
| 18.6 | UX-BACK-BUTTON (12 telas + dashboard) | FE | **CONCLUIDO** |
| 18.7 | NPC-DELETE + NPC-BACK | FE | **CONCLUIDO** |
| 18.8 | Type fixes (TagSeverity, ProgressBar, imports) | FE | **CONCLUIDO** |
| 18.9 | FichaStatus (ATIVA/MORTA/ABANDONADA; COMPLETA @Deprecated) | BE | **CONCLUIDO** |
| 18.10 | PUT /fichas/{id}/status + AtualizarStatusFichaRequest | BE | **CONCLUIDO** |
| 18.11 | DELETE /fichas/{id} restrito a NPC (excluirNpc) | BE | **CONCLUIDO** |

### Rodada 19 — CONCLUIDA (sessao 22 continuada, 2026-04-16)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 19.1 | T-025-12 (labels wizard: Vantagens + Revisao) | FE | **CONCLUIDO** (`27fd3b8`) |
| 19.2 | T-025-08 (wizard le fichaId do path param `:id`) | FE | **CONCLUIDO** (`3d04f3b`) |
| 19.3 | T-025-01-FE + T-025-09 + T-025-10 (badges status + canEdit + remove Excluir Jogador) | FE | **CONCLUIDO** (+33 testes, `9b5ba1b`) |
| 19.4 | T-025-03 + T-025-04 + T-025-07 + T-025-11 (aba Sessao vida/essencia/membros + polling 30s + badge header) | FE | **CONCLUIDO** (+25 testes, `65f0c19`) |
| 19.5 | S026-T08 FE (coluna Data Criacao lista jogos) | FE | **CONCLUIDO** (`ea0bb09`) |
| 19.6 | S026-T05 (badge participantes pendentes sidebar Mestre) | FE | **CONCLUIDO** (`aaa1abc`) |
| 19.7 | T-025-13 (remove Criar Ficha sidebar Jogador) | FE | **CONCLUIDO** (`aaa1abc`) |
| 19.8 | S026-T08 BE (`criadoEm` no JogoResumoResponse + mapper) | BE | **CONCLUIDO** (21 testes afetados, `2fc8ece`) |

---

## Bloqueados / Pontos em Aberto

### PAs acumulados

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|----------|--------------|
| PA-R05-01 | FichaPreviewResponse incompleto | Nao | PO decide |
| PA-R05-02 | FichaPreviewService sem testes avancados | Nao | P2 |
| PA-006 | VIG/SAB hardcoded (GAP-CALC-09) | Nao | PO decide |
| PA-017-03 | Reativar SidebarComponent | Nao | Pos-MVP |

### GAPs de auditoria BE->FE (sessao 20-22)

| ID | Descricao | Prio | Status |
|----|-----------|------|--------|
| GAP-XP-01 | Conceder XP — `PUT /fichas/{id}/xp` | P1 | **CONCLUIDO** (wave 3, +14 testes) |
| GAP-INS-01 | Conceder Insolitus — UI + service FE | P1 | **CONCLUIDO** (wave 3, UI ja existia + testes) |
| GAP-PROS-01 | Prospeccao pendentes — `GET /jogos/{id}/prospeccao/pendentes` | P1 | **CONCLUIDO** (wave 3, +28 testes) |
| GAP-NPC-FE-01 | NPC dificuldade FE — selector + auto-fill atributos | P1 | **CONCLUIDO** (wave 3, +31 testes) |
| GAP-DASH-01 | Dashboard Mestre — JogoDashboard + rota + sidebar | P2 | **CONCLUIDO** (R17 + consolidacao R18) |
| GAP-EXPRT-01 | Export/Import config — endpoints sem interface | P2 | **CANCELADO (R18)** — botoes removidos no FE-1 Quick Win |

### PAs RESOLVIDOS (sessoes 19-22)
- **PA-R02-01**: Spec 016 T5 — RESOLVIDO
- **PA-021-03**: Tela habilidades JOGADOR — RESOLVIDO
- **PA-004**: FormulaEditor campo-alvo — RESOLVIDO (S007-T10)
- **PA-015-04**: Enum OrigemFichaVantagem — RESOLVIDO (S015-T4)
- **Spec 023 FE** — RESOLVIDO (wave 2)
- **GAP-XP-01 / GAP-INS-01 / GAP-PROS-01 / GAP-NPC-FE-01** — RESOLVIDOS (wave 3)
- **Spec 024 T1 / GAP-DASH-01 / UX-BASE-COMP / UX-PREREQ-EMPTY** — RESOLVIDOS (R17)
- **FichaStatus MORTA/ABANDONADA + PUT /status + DELETE NPC** — RESOLVIDOS (R18) — INCONS-02 do PO totalmente implementado BE
- **UX Quick Wins + Back Buttons + Profile nome + NPC delete/back** — RESOLVIDOS (R18)
- **GAP-EXPRT-01** — CANCELADO (R18)
- **T-025-* (FichaStatus FE, Aba Sessao, wizard path param, labels wizard, remove Excluir)** — RESOLVIDOS (R19) — INCONS-02 agora end-to-end BE+FE
- **S026-T05 (badge pendentes sidebar Mestre) + S026-T08 (Data Criacao + `criadoEm`)** — RESOLVIDOS (R19)

### GAP NOVO identificado em R19

| ID | Descricao | Prio | Status |
|----|-----------|------|--------|
| GAP-ESTADO-COMBATE | `GET /fichas/{id}/estado-combate` — expor `danoRecebido` por membro + vida/essencia atual. FichaResumoResponse nao exporta `danoRecebido`, aba Sessao do FE inicia dano=0. | P1 | [PENDENTE — proxima acao R20, PO aprovou endpoint dedicado] |

---

## Proxima Sessao — Sprint 4 ENCERRADO + Proxima Acao R20

Sprint 4: **17/17 tasks P1 + 4/5 P2** entregues + Spec 026 T05/T08 + T-025-* (R19). **GAP-EXPRT-01 CANCELADO**. Unico P2 residual: AUDIT-BE-FE. Gap novo: **GAP-ESTADO-COMBATE** (P1, R20).

**Prioridade imediata — Rodada 20:**
1. **GAP-ESTADO-COMBATE (BE + FE)** — criar endpoint `GET /fichas/{id}/estado-combate` (ou similar) que retorne `vidaAtual`, `essenciaAtual` e `danoRecebido` por membro. FE atualiza aba Sessao para consumir esse endpoint em vez do `FichaResumoResponse`.

Opcoes complementares:
2. **Auditoria de testes backend (R18+R19)** — validar cobertura dos novos endpoints (PUT /status, DELETE NPC, JogoResumoResponse.criadoEm); reconciliar contagem global (era 832 pre-R18).
3. **Reconciliar contagens de testes FE** — apos commits R18 + R19 (+58 testes confirmados em R19), recalcular numero total.
4. **AUDIT-BE-FE (P2)** — auditar demais endpoints backend sem tela correspondente.
5. **Fechamento v0.0.1-RC** — apos GAP-ESTADO-COMBATE, avaliar tag de versao.
6. **Novo ciclo** — PO prioriza proximos epicos (modo sessao real SSE/WebSocket, XP em lote, galeria multimedia, etc.).

**Specs ativas:** nenhuma pendente de implementacao. `docs/specs/024-ux-melhorias-sprint4/` CONCLUIDA.

---

## Observacoes Tecnicas

- Backend: 832 testes pre-R18 — **a auditar** apos R18 (FichaStatus novos valores, PUT /status, DELETE NPC) + R19 (21 testes afetados em JogoResumoResponse.criadoEm)
- Frontend: **~1525+ testes** pos-R17 + entregas R18 + **+58 testes confirmados em R19** (33 de T-025-01-FE/09/10 + 25 de aba Sessao) — reconciliar total em proxima rodada
- Schema: `ddl-auto=update` — sem migracao manual (FichaStatus ganha novos valores automaticamente)
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- ficha-vantagens-tab: 2 falhas pre-existentes (nao bloqueia)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado
- OAuth2 com timeouts de 15s no token exchange e userinfo
- **R18 — Ajuste semantico importante:** `FichaStatus.COMPLETA` foi marcada `@Deprecated`. Codigo futuro deve usar `ATIVA`. Checar refs antigas ao tocar em ficha lifecycle.
- **R19 — INCONS-02 end-to-end:** Frontend agora alinha totalmente com a decisao PO — badge de status visivel, `canEdit()` bloqueia MORTA/ABANDONADA, botao Excluir removido do Jogador. BE ja era consistente desde R18.
- **R19 — Aba Sessao:** Estrutura completa (vida + essencia + membros + polling 30s), porem `danoRecebido` inicia em 0 ate GAP-ESTADO-COMBATE (R20). Decisao PO aceitou estado transitorio.
