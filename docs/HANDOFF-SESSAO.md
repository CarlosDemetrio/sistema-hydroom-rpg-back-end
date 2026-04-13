# Handoff de Sessao — 2026-04-13 (sessao 20 — Repriorizacao backlog pos-analise UX+pre-requisitos)

> Branch atual: `main`
> Backend: **796 testes** passando, 0 falhas
> Frontend: **~1208 testes** passando (2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 4: INICIADO — foco em tasks desbloqueadas + Spec 023 + divida UX
> Ultima atualizacao: 2026-04-13

---

## Resumo Executivo

**Sessao 2026-04-13 (sessao 20)** focou em repriorizacao do backlog com base em:
1. **Analise de UX** — 17 de 19 telas com `acceptButtonProps` deprecated, 9 dialogs sem largura, 4 telas fora do BaseConfigComponent, cores hex sem preview visual
2. **Spec 023 aprovada pelo PO** — Pre-requisitos polimorficos de vantagem (RACA, CLASSE, ATRIBUTO, APTIDAO alem de VANTAGEM)
3. **Tasks desbloqueadas** — S007-T10 (FormulaEditor) e S015-T4 (auto-concessao vantagens) com PAs resolvidos
4. **NPC gaps identificados** — raça/classe ausentes no cadastro + template de dificuldade
5. **Itens cortados** — Spec 010, Spec 013, PA-017-04 removidos do backlog ativo

Nenhum codigo alterado nesta sessao — apenas tracking e priorizacao.

---

## Decisoes de PO registradas (sessao 20)

### Spec 023 — Pre-requisitos Polimorficos de Vantagem (APROVADO)

- Refatorar `VantagemPreRequisito`: nova coluna `tipo`, campos nullable por tipo (`raca_id`, `classe_id`, `atributo_id`, `aptidao_id`, `valor_minimo`)
- Registros existentes recebem `tipo = 'VANTAGEM'` como default (Migration Flyway)
- Logica: **AND entre tipos diferentes, OR entre registros do mesmo tipo**
- `ATRIBUTO` usa `valorBase` (pontos distribuidos), NAO `valorAtual` (pos-bonus)
- Concessao de Insolitus e VantagemPreDefinida (SISTEMA) **ignoram** pre-requisitos
- 409 ao tentar deletar Raca/Classe/Atributo/Aptidao usada como pre-requisito
- Mudanca de raca/classe NAO revoga vantagem — Mestre decide manualmente
- Config usada como pre-req NAO pode ser deletada — deve ser **inativada** (campo `ativo`)

---

## Estado das Specs (atualizado sessao 20)

| Spec | Titulo | Status | Nota |
|------|--------|--------|------|
| 004 | Siglas, formulas, relacionamentos | CONCLUIDO | — |
| 005 | Participantes, aprovacao, permissoes | CONCLUIDO | — |
| 006 | Wizard de criacao de ficha | CONCLUIDO | — |
| 007 | VantagemEfeito + Motor de calculos | 12/13 | T10 DESBLOQUEADO — **P0 sessao 20** |
| 008 | Sub-recursos Classes/Racas (FE) | CONCLUIDO | — |
| 009-ext | NPC Visibility + Prospeccao + Essencia | CONCLUIDO | — |
| 011 | Galeria + Anotacoes | CONCLUIDO 9/9 | — |
| 012 | Niveis e progressao (FE) | CONCLUIDO | — |
| 014 | Cobertura de testes | Parcial | T1+T5 OK; T2-T4+T6 pendentes (P2) |
| 015 | ConfigPontos Classe/Raca | 6/7 | T4 DESBLOQUEADO — **P0 sessao 20** |
| 016 | Sistema de Itens | CONCLUIDO | T1-T11 todos concluidos |
| 017 | Correcoes Pre-RC | CONCLUIDO | P0+P1+P2+P3 |
| 018 | Deploy Backend GCP | CONCLUIDO | — |
| 019 | Deploy Frontend Firebase | CONCLUIDO | — |
| 021 | Sistema de Habilidades | CONCLUIDO | BA + T1 BE + T2 FE |
| 022 | DefaultGameConfigProvider refactor | CONCLUIDO | — |
| **023** | **Pre-requisitos Polimorficos Vantagem** | **NOVO — PENDENTE** | Aprovado PO. Tasks por criar (BA/TL) |

**Specs CORTADAS do backlog ativo:**
- ~~Spec 010~~ (Roles ADMIN refactor) — CORTADO
- ~~Spec 013~~ (Documentacao tecnica) — CORTADO
- ~~PA-017-04~~ (Exportar/Importar config) — CORTADO

---

## Backlog Priorizado — Sprint 4

### P0 — AGORA

| # | ID | Tipo | Descricao | Dependencia |
|---|-----|------|-----------|-------------|
| 1 | S007-T10 | FE | FormulaEditor para FORMULA_CUSTOMIZADA (PA-004 resolvido) | S007-T9 CONCLUIDO |
| 2 | S015-T4 | BE | Auto-concessao vantagens pre-definidas + enum OrigemFichaVantagem (PA-015-04 resolvido) | S015-T3 CONCLUIDO |
| 3 | S023-BE | BE | Spec 023 backend — refatorar VantagemPreRequisito polimorficamente | Spec 004 CONCLUIDO |
| 4 | UX-JOGO-SELECT | FE | Seletor de jogo nas telas de configuracao (bloqueador de usabilidade) | Nenhuma |
| 5 | NPC-FORM-CAMPOS | FE | Raça/Classe/configs no formulario de criacao de NPC | Spec 009 CONCLUIDO |

### P1 — PROXIMA RODADA

| # | ID | Tipo | Descricao | Dependencia |
|---|-----|------|-----------|-------------|
| 6 | S023-FE | FE | Aba pre-requisitos polimorfica + chips por tipo | S023-BE |
| 7 | UX-ACCEPT-BTN | FE | Corrigir acceptButtonProps deprecated em 17 telas (fix rapido) | Nenhuma |
| 8 | UX-COR-PREVIEW | FE | Cores hex com preview visual (swatch) | Nenhuma |
| 9 | NPC-TEMPLATE | BE+FE | Nivel de dificuldade NPC (Facil/Medio/Dificil/Elite/Chefe) + foco FISICO/MAGICO | Nenhuma |

### P2 — POS

| # | ID | Tipo | Descricao | Dependencia |
|---|-----|------|-----------|-------------|
| 10 | AUDIT-BE-FE | Auditoria | Auditar endpoints backend sem tela frontend | Nenhuma |
| 11 | UX-BASE-COMP | FE | Migrar 4 telas para BaseConfigComponent (habilidades, itens, raridades, tipos-item) | Nenhuma |
| 12 | UX-DIALOG-WIDTH | FE | Padronizar largura de dialogs em 9 telas | Nenhuma |
| 13 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos — CTA "Adicionar primeiro" | S023-FE |
| 14 | S014-T2-T4+T6 | BE+FE | Cobertura de testes (JaCoCo 50% para 75%) | Nenhuma |

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
- **PA-004**: FormulaEditor campo-alvo (atributo/bonus) — RESOLVIDO. Desbloqueia S007-T10.
- **PA-015-04**: Enum OrigemFichaVantagem (JOGADOR, MESTRE, SISTEMA) — RESOLVIDO. Desbloqueia S015-T4.

---

## Proxima Sessao — Sprint 4

O foco e implementar as tasks P0 desbloqueadas e iniciar Spec 023. A ordem recomendada:

1. **S007-T10** (FE) e **S015-T4** (BE) podem rodar em paralelo — nao tocam nos mesmos arquivos
2. **S023-BE** (BE) pode iniciar assim que tasks de Spec 023 forem escritas por BA/TL
3. **UX-JOGO-SELECT** (FE) e **NPC-FORM-CAMPOS** (FE) podem rodar em paralelo entre si, mas NAO com S007-T10 (possivel conflito em EfeitoFormComponent/vantagens-config)

### Plano Anti-Conflito (paralelo recomendado)

| Agente | Escopo | NAO tocar |
|--------|--------|-----------|
| BE-1 | S015-T4 (auto-concessao vantagens) | Frontend, VantagemPreRequisito |
| FE-1 | S007-T10 (FormulaEditor) | Ficha*, NPC*, jogo-select |
| BE-2 | S023-BE (pre-requisitos polimorficos) | FichaVantagem*, Frontend |
| FE-2 | UX-JOGO-SELECT + NPC-FORM-CAMPOS | vantagens-config/*, efeito-form/* |

---

## Observacoes Tecnicas

- Frontend budget warning pre-existente: bundle 1.14MB vs limite 1MB (nao bloqueia)
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- HabilidadeConfigController usa path `/api/jogos/{jogoId}/config/habilidades` (nao `/api/v1/`)
- GraalVM native image funcional com distroless/cc-debian12
- Structured logging GCP com formato ECS
- Micrometer Prometheus configurado para observability
- OAuth2 com timeouts de 15s no token exchange e userinfo
