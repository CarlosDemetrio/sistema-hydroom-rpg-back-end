# Handoff de Sessao — 2026-04-15 (sessao 21 — Rodada 17: Spec 024 fechada + UX-BASE-COMP + GAP-DASH-01)

> Branch atual: `main`
> Backend: **832 testes** passando, 0 falhas (sem alteracoes)
> Frontend: **~1525 testes** passando (+92 rodada 17 sobre ~1433 wave 3; 2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 4: Wave P0 + Wave P1w2 + Wave P1w3 + **Rodada 17** concluidas — **17/17 tasks entregues** (5 P0 + 4 P1w2 + 4 P1w3 + 4 R17). **Spec 024 CONCLUIDA (2/2)**. Restam 2 P2 (GAP-EXPRT-01 bloqueado, AUDIT-BE-FE baixa prio).
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

### Wave P2 — ESTADO APOS R17

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 1 | GAP-DASH-01 | FE | Dashboard do Mestre (3 cards + rota + sidebar) | **CONCLUIDO** (R17) |
| 2 | UX-BASE-COMP | FE | Migrar telas restantes (itens + raridades) | **CONCLUIDO** (R17) |
| 3 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | **CONCLUIDO** (via Spec 023 FE) |
| 4 | GAP-EXPRT-01 | FE | Interface Export/Import config | [SKIP — BE sem endpoint] |
| 5 | AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | [PENDENTE — baixa prio] |

---

## Bloqueados / Pontos em Aberto

### PAs acumulados

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|----------|--------------|
| PA-R05-01 | FichaPreviewResponse incompleto | Nao | PO decide |
| PA-R05-02 | FichaPreviewService sem testes avancados | Nao | P2 |
| PA-006 | VIG/SAB hardcoded (GAP-CALC-09) | Nao | PO decide |
| PA-017-03 | Reativar SidebarComponent | Nao | Pos-MVP |

### GAPs de auditoria BE->FE (sessao 20-21)

| ID | Descricao | Prio | Status |
|----|-----------|------|--------|
| GAP-XP-01 | Conceder XP — `PUT /fichas/{id}/xp` | P1 | **CONCLUIDO** (wave 3, +14 testes) |
| GAP-INS-01 | Conceder Insolitus — UI + service FE | P1 | **CONCLUIDO** (wave 3, UI ja existia + testes) |
| GAP-PROS-01 | Prospeccao pendentes — `GET /jogos/{id}/prospeccao/pendentes` | P1 | **CONCLUIDO** (wave 3, +28 testes) |
| GAP-NPC-FE-01 | NPC dificuldade FE — selector + auto-fill atributos | P1 | **CONCLUIDO** (wave 3, +31 testes) |
| GAP-DASH-01 | Dashboard Mestre — 3 cards + rota + sidebar | P2 | **CONCLUIDO** (R17, +16 testes) |
| GAP-EXPRT-01 | Export/Import config — endpoints sem interface | P2 | [SKIP — backend nao tem endpoint] |

### PAs RESOLVIDOS (sessoes 19-21)
- **PA-R02-01**: Spec 016 T5 — RESOLVIDO
- **PA-021-03**: Tela habilidades JOGADOR — RESOLVIDO
- **PA-004**: FormulaEditor campo-alvo — RESOLVIDO (S007-T10)
- **PA-015-04**: Enum OrigemFichaVantagem — RESOLVIDO (S015-T4)
- **Spec 023 FE** — RESOLVIDO (wave 2)
- **GAP-XP-01 / GAP-INS-01 / GAP-PROS-01 / GAP-NPC-FE-01** — RESOLVIDOS (wave 3)
- **Spec 024 T1 / GAP-DASH-01 / UX-BASE-COMP / UX-PREREQ-EMPTY** — RESOLVIDOS (R17)

---

## Proxima Sessao — Sprint 4 praticamente encerrado

Sprint 4: **17/17 tasks** entregues (5 P0 + 4 P1w2 + 4 P1w3 + 4 R17). Spec 024 CONCLUIDA.

Opcoes para proxima rodada:
1. **AUDIT-BE-FE (P2)** — auditar endpoints backend sem tela correspondente; avaliar se entram no backlog pos-MVP
2. **GAP-EXPRT-01** — bloqueado: exige endpoint backend que nao existe; PO precisa decidir se cria Spec nova
3. **Fechamento v0.0.1-RC** — com Sprint 4 concluido, avaliar se o backlog funcional (Specs 005-024) esta pronto para tag de versao
4. **Novo ciclo** — PO prioriza proximos epicos

**Specs ativas:** nenhuma pendente de implementacao. `docs/specs/024-ux-melhorias-sprint4/` CONCLUIDA.

---

## Observacoes Tecnicas

- Backend: 832 testes (sem alteracoes na R17), 0 falhas
- Frontend: **~1525 testes** (+92 R17: Spec 024 T1 +14, UX-BASE-COMP raridades +27 / itens +35, GAP-DASH-01 +16)
- Schema: `ddl-auto=update` — sem migracao manual
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- ficha-vantagens-tab: 2 falhas pre-existentes (nao bloqueia)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado
- OAuth2 com timeouts de 15s no token exchange e userinfo
