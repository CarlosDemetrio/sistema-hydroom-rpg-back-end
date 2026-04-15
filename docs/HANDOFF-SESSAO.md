# Handoff de Sessao — 2026-04-14 (sessao 20 — Wave P0 Sprint 4 entregue)

> Branch atual: `main`
> Backend: **814 testes** passando, 0 falhas
> Frontend: **1258 testes** passando (2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 4: Wave P0 concluida — 5/5 tasks entregues. Passa para Wave P1 (Spec 023 FE + UX).
> Ultima atualizacao: 2026-04-14

---

## Resumo Executivo

**Sessao 2026-04-14 (sessao 20, Wave P0)** entregou todas as 5 tasks P0 do Sprint 4:
1. **S007-T10 FE** — FormulaEditor para FORMULA_CUSTOMIZADA (select campo-alvo + input formula + hint variaveis), 54 testes
2. **S015-T4 BE** — Auto-concessao vantagens pre-definidas — ja existia como `OrigemVantagem`, faltava expor campo `origem` no FichaVantagemResponse (commit `4c04a54`)
3. **S023-BE** — Pre-requisitos polimorficos: 6 tipos (VANTAGEM/RACA/CLASSE/ATRIBUTO/NIVEL/APTIDAO), OR dentro do tipo / AND entre tipos, 409 em delecao. +18 testes -> 814 total (commit `934eaff`)
4. **UX-JOGO-SELECT + UX-COR-PREVIEW FE** — Game selector no ConfigLayoutComponent + p-colorpicker bidirecional em raridades. +22 testes -> 1258 total
5. **NPC-FORM-CAMPOS FE** — Ja pre-implementado. Adicionados 2 testes de cobertura (racaId/classeId)

**Schema:** gerenciado por Hibernate `ddl-auto=update` — SEM migracoes manuais (Spec 023 refactor nao precisa de Flyway).

---

## Decisoes de PO registradas (sessao 20)

### Spec 023 — Pre-requisitos Polimorficos de Vantagem (IMPLEMENTADO BE)

- `VantagemPreRequisito` refatorado: nova coluna `tipo` + campos nullable por tipo (`raca_id`, `classe_id`, `atributo_id`, `aptidao_id`, `valor_minimo`)
- Logica: **AND entre tipos diferentes, OR entre registros do mesmo tipo**
- `ATRIBUTO` usa `valorBase` (pontos distribuidos), NAO `valorAtual` (pos-bonus)
- Concessao de Insolitus e VantagemPreDefinida (SISTEMA) **ignoram** pre-requisitos
- 409 ao tentar deletar Raca/Classe/Atributo/Aptidao usada como pre-requisito
- Frontend (aba polimorfica) passa para proxima wave (P1)

---

## Estado das Specs (atualizado sessao 20 pos-Wave P0)

| Spec | Titulo | Status | Nota |
|------|--------|--------|------|
| 004 | Siglas, formulas, relacionamentos | CONCLUIDO | — |
| 005 | Participantes, aprovacao, permissoes | CONCLUIDO | — |
| 006 | Wizard de criacao de ficha | CONCLUIDO | — |
| **007** | **VantagemEfeito + Motor de calculos** | **CONCLUIDO 13/13** | T10 FE entregue Wave P0 (FormulaEditor, 54 testes) |
| 008 | Sub-recursos Classes/Racas (FE) | CONCLUIDO | — |
| 009-ext | NPC Visibility + Prospeccao + Essencia | CONCLUIDO | — |
| 011 | Galeria + Anotacoes | CONCLUIDO 9/9 | — |
| 012 | Niveis e progressao (FE) | CONCLUIDO | — |
| ~~014~~ | ~~Cobertura de testes~~ | **CORTADO** | Cortado pelo PO (2026-04-13). T1+T5 entregues |
| **015** | **ConfigPontos Classe/Raca** | **CONCLUIDO 7/7** | T4 concluido Wave P0 (pre-impl + `origem` no response, commit `4c04a54`) |
| 016 | Sistema de Itens | CONCLUIDO | — |
| 017 | Correcoes Pre-RC | CONCLUIDO | — |
| 018 | Deploy Backend GCP | CONCLUIDO | — |
| 019 | Deploy Frontend Firebase | CONCLUIDO | — |
| 021 | Sistema de Habilidades | CONCLUIDO | — |
| 022 | DefaultGameConfigProvider refactor | CONCLUIDO | — |
| **023** | **Pre-requisitos Polimorficos Vantagem** | **BE CONCLUIDO / FE PENDENTE** | BE: 6 tipos, AND/OR, +18 testes (commit `934eaff`). FE aba polimorfica = P1 Wave 2 |

**Specs CORTADAS:** ~~010~~ (ADMIN), ~~013~~ (docs), ~~014~~ T2-T4+T6 (cobertura), ~~PA-017-04~~ (export/import)

---

## Backlog Priorizado — Sprint 4 (atualizado pos-Wave P0)

### Wave P0 — CONCLUIDA (sessao 20)

| # | ID | Tipo | Status |
|---|-----|------|--------|
| 1 | S007-T10 | FE | CONCLUIDO (54 testes, FormulaEditor) |
| 2 | S015-T4 | BE | CONCLUIDO (commit `4c04a54`, campo `origem` no response) |
| 3 | S023-BE | BE | CONCLUIDO (commit `934eaff`, +18 testes) |
| 4 | UX-JOGO-SELECT + UX-COR-PREVIEW | FE | CONCLUIDO (+22 testes, game selector + colorpicker) |
| 5 | NPC-FORM-CAMPOS | FE | CONCLUIDO (pre-impl + 2 testes de cobertura) |

### Wave P1 — PROXIMA RODADA

| # | ID | Tipo | Descricao | Dependencia |
|---|-----|------|-----------|-------------|
| 6 | S023-FE | FE | Aba pre-requisitos polimorfica + chips removiveis por tipo | S023-BE CONCLUIDO |
| 7 | UX-ACCEPT-BTN | FE | Corrigir acceptButtonProps deprecated em 17 telas | Nenhuma |
| 8 | NPC-TEMPLATE | BE+FE | Nivel dificuldade NPC (Facil/Medio/Dificil/Elite/Chefe) + foco | Nenhuma |
| 9 | UX-TIPO-VANTAGEM | FE | tipoVantagem no form criacao vantagem (Insolitus) | Nenhuma |
| 10 | UX-NIVEL-MIN-PREREQ | FE | nivelMinimo exibido na lista pre-req tipo VANTAGEM | S023-FE |

### Wave P2 — POS

| # | ID | Tipo | Descricao | Dependencia |
|---|-----|------|-----------|-------------|
| 11 | AUDIT-BE-FE | Auditoria | Auditar endpoints backend sem tela frontend | Nenhuma |
| 12 | UX-BASE-COMP | FE | Migrar 4 telas para BaseConfigComponent | Nenhuma |
| 13 | UX-DIALOG-WIDTH | FE | Padronizar largura de dialogs em 9 telas | Nenhuma |
| 14 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | S023-FE |

---

## Bloqueados / Pontos em Aberto

### PAs acumulados

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|----------|--------------|
| PA-R05-01 | FichaPreviewResponse incompleto (sem aptidoes/dado prospeccao) | Nao (decisao PO) | PO decide se amplia resposta |
| PA-R05-02 | FichaPreviewService sem testes avancados | Nao | P2 |
| PA-006 | VIG/SAB hardcoded por abreviacao (GAP-CALC-09) | Nao | Fora do escopo T0; PO decide |
| PA-017-03 | Reativar SidebarComponent (T15, P3) | Nao | Pos-MVP |

### PAs RESOLVIDOS (sessoes 19+20)
- **PA-R02-01**: Spec 016 T5 FichaItemService recalcularStats — RESOLVIDO
- **PA-021-03**: Tela habilidades JOGADOR em secao separada — RESOLVIDO
- **PA-004**: FormulaEditor campo-alvo (atributo/bonus) — RESOLVIDO. S007-T10 entregue.
- **PA-015-04**: Enum OrigemFichaVantagem — RESOLVIDO. S015-T4 entregue.

---

## Proxima Sessao — Sprint 4 Wave P1

Foco: **Spec 023 FE** (aba polimorfica) + **UX-ACCEPT-BTN** (17 telas). Podem rodar em paralelo (dominios diferentes).

### Plano Anti-Conflito (paralelo recomendado)

| Agente | Escopo | NAO tocar |
|--------|--------|-----------|
| FE-1 | S023-FE (aba pre-req polimorfica em vantagens-config) | Demais configs, ficha*, NPC* |
| FE-2 | UX-ACCEPT-BTN (17 telas com confirm dialog) | vantagens-config/*, efeito-form/* |
| BE-1 | NPC-TEMPLATE BE (nova config nivel dificuldade) | VantagemPreRequisito, vantagens |
| FE-3 | NPC-TEMPLATE FE (tela + integracao) | Dominios que FE-1 e FE-2 estao tocando |

---

## Observacoes Tecnicas

- Backend: 814 testes (+18 Spec 023), 0 falhas
- Frontend: 1258 testes (+22 Wave P0 — FormulaEditor, game selector, colorpicker, NPC cobertura)
- Schema: `ddl-auto=update` — Spec 023 refactor sem migracao manual
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- ficha-vantagens-tab: 2 falhas pre-existentes (nao bloqueia)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado para observability
- OAuth2 com timeouts de 15s no token exchange e userinfo
