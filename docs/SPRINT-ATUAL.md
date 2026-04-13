# Sprint Atual — Sprint 4: "Pre-requisitos + UX + NPC"

> **Papel deste arquivo:** tracking ativo da sprint corrente — tasks em
> andamento, restante, bloqueios, sequenciamento de rodadas.
> Para visao mais macro, ver `MASTER.md`.
> Para detalhes por area e historico, ver `PM.md`.
> Para estado da ultima sessao, ver `HANDOFF-SESSAO.md`.
>
> Atualizado: 2026-04-13 (sessao 20 — repriorizacao backlog, Spec 023 aprovada, Spec 010+013 cortadas)
> PM: Scrum Orchestrator
> Objetivo: **Tasks desbloqueadas (S007-T10, S015-T4) + Spec 023 BE + UX fixes + NPC gaps**
> Duracao estimada: 2-3 semanas
> Cronologia: `docs/historico/CRONOLOGIA.md` | Rodadas: `docs/tracking/rodadas/` | Copilot: `docs/tracking/rodadas-copilot/`

---

## Sprint 3 — ENCERRADO (sessao 19)

| Metrica | Valor |
|---------|-------|
| Rodadas Claude | 3 (R12 + R13 + R14) |
| Rodadas Copilot | 4 (R01+R02+R03+R04) |
| Waves paralelas | Waves 1+2+3 COMPLETAS (2026-04-13) |
| Specs finalizadas no Sprint 3 | 008, 009-ext, 012, 015 (6/7), 016, 017, 018, 019, 021, 022 |
| Testes ao fechar | 796 BE, ~1208 FE |

---

## Sprint 4 — Progresso Geral

| Metrica | Valor |
|---------|-------|
| Rodadas concluidas | 0 (sprint recem-iniciado) |
| Tasks P0 | 5 (S007-T10, S015-T4, S023-BE, UX-JOGO-SELECT, NPC-FORM-CAMPOS) |
| Tasks P1 | 6 (S023-FE, UX-ACCEPT-BTN, UX-COR-PREVIEW, NPC-TEMPLATE, UX-TIPO-VANTAGEM, UX-NIVEL-MIN-PREREQ) |
| Tasks P2 | 5 (AUDIT-BE-FE, UX-BASE-COMP, UX-DIALOG-WIDTH, UX-PREREQ-EMPTY, S014-T2-T4+T6) |
| Total tasks Sprint 4 | **16** (5 P0 + 6 P1 + 5 P2) |
| Testes backend | **796 passando**, 0 falhas |
| Testes frontend | **~1208 passando** + 2 falhas pre-existentes + 2 OOM pre-existentes |
| Specs em andamento | Spec 007 (12/13 — T10 P0), Spec 015 (6/7 — T4 P0), **Spec 023** (NOVO) |
| Specs CORTADAS | ~~Spec 010~~, ~~Spec 013~~, ~~PA-017-04~~ |

### P0 — Tasks Ativas Sprint 4

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 1 | S007-T10 | FE | FormulaEditor para FORMULA_CUSTOMIZADA (selecionar campo-alvo atributo/bonus) | S007-T9 OK | [PENDENTE] |
| 2 | S015-T4 | BE | Auto-concessao vantagens pre-definidas + enum OrigemFichaVantagem (JOGADOR/MESTRE/SISTEMA) | S015-T3 OK | [PENDENTE] |
| 3 | S023-BE | BE | Refatorar VantagemPreRequisito: coluna `tipo`, campos nullable, AND/OR, migration Flyway | Spec 004 OK | [PENDENTE] — tasks por criar (BA/TL) |
| 4 | UX-JOGO-SELECT | FE | Seletor de jogo nas telas de configuracao do Mestre (bloqueador usabilidade) | Nenhuma | [PENDENTE] |
| 5 | NPC-FORM-CAMPOS | FE | Raça/Classe/configs no formulario de criacao de NPC | Spec 009 OK | [PENDENTE] |

### P1 — Proxima Rodada

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 6 | S023-FE | FE | Aba pre-requisitos polimorfica + chips removiveis por tipo | S023-BE | [PENDENTE] |
| 7 | UX-ACCEPT-BTN | FE | acceptButtonProps deprecated em 17 telas → `acceptButtonProps: { severity: 'danger' }` | Nenhuma | [PENDENTE] |
| 8 | UX-COR-PREVIEW | FE | Cores hex com preview visual (swatch) em RaridadeItemConfig | Nenhuma | [PENDENTE] |
| 9 | NPC-TEMPLATE | BE+FE | Config nivel dificuldade NPC (Facil/Medio/Dificil/Elite/Chefe) + foco FISICO/MAGICO | Nenhuma | [PENDENTE] |
| 10 | UX-TIPO-VANTAGEM | FE | tipoVantagem no form criacao vantagem (fix: Insolitus nao pode ser criado hoje) | Nenhuma | [PENDENTE] |
| 11 | UX-NIVEL-MIN-PREREQ | FE | nivelMinimo exibido na lista de pre-req tipo VANTAGEM | S023-FE | [PENDENTE] |

### P2 — Pos

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 12 | AUDIT-BE-FE | Auditoria | Auditar endpoints backend sem tela frontend | Nenhuma | [PENDENTE] |
| 13 | UX-BASE-COMP | FE | Migrar 4 telas para BaseConfigComponent (habilidades, itens, raridades, tipos-item) | Nenhuma | [PENDENTE] |
| 14 | UX-DIALOG-WIDTH | FE | Padronizar largura dialogs em 9 telas (width + maxWidth: 95vw) | Nenhuma | [PENDENTE] |
| 15 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos — CTA "Adicionar primeiro pre-requisito" | S023-FE | [PENDENTE] |
| 16 | S014-T2-T4+T6 | BE+FE | Cobertura de testes (JaCoCo 50% para 75%) | Nenhuma | [PENDENTE] |

### Spec 023 — Pre-requisitos Polimorficos de Vantagem (NOVO — aprovado PO sessao 20)

**Decisoes de PO:**
- Multiplos pre-req do mesmo tipo = **OR** (ex: RACA=Elfo OR RACA=Anão)
- Tipos diferentes = **AND** (ex: RACA=Elfo AND ATRIBUTO FOR >= 14)
- ATRIBUTO usa `valorBase` (pontos distribuidos), NAO `valorAtual` (pos-bonus)
- Concessao Insolitus e VantagemPreDefinida (SISTEMA) **ignoram** pre-requisitos
- 409 ao tentar deletar config usada como pre-req — deve ser **inativada** (campo `ativo`)
- Mudanca de raca/classe NAO revoga vantagem — Mestre decide manualmente

**Backend (tasks por criar por BA/TL):**
- Refatorar entidade VantagemPreRequisito: coluna `tipo`, campos nullable (`raca_id`, `classe_id`, `atributo_id`, `aptidao_id`, `valor_minimo`)
- Migration Flyway: registros existentes → `tipo = 'VANTAGEM'`
- FichaVantagemService.comprar() — verificacao por tipo
- 409 ao deletar Raca/Classe/Atributo/Aptidao usada como pre-req

**Frontend (tasks por criar por BA/TL):**
- Select de tipo → campos condicionais
- Chips removiveis: `[RACA] Elfo OU Anao`, `[ATRIBUTO] FOR >= 14`
- Hint "Requisitos do mesmo tipo sao alternativos (OU)"
| S017-T11 | jogo-form adicionar p-toast | PENDENTE |
| S017-T12 | Remover double-toast em 19 componentes | PENDENTE |

### Bloqueados
- **S023-BE**: Aguarda BA/TL criarem tasks individuais (spec aprovada, tasks por escrever)
- **S023-FE**: Depende de S023-BE
- **UX-PREREQ-EMPTY**: Depende de S023-FE
- **UX-NIVEL-MIN-PREREQ**: Depende de S023-FE

### PAs abertos
| ID | Descricao | Bloqueia | Status |
|----|-----------|----------|--------|
| PA-006 | VIG/SAB hardcoded (GAP-CALC-09) | Nao | PO decide |
| PA-R05-01 | FichaPreviewResponse incompleto | Nao | PO decide |
| PA-017-03 | Reativar SidebarComponent | Nao | Pos-MVP |

---

---

## Sprint 1 — ENCERRADO (2026-04-01 a 2026-04-03)

| Metrica | Valor Final |
|---------|-------------|
| Tasks totais | 31 |
| Concluidas | 29 (94%) |
| Nao concluidas | 1 (SP1-T13 barras HP membro) — SP1-T27 DDL cancelado (Hibernate ddl-auto suficiente para RC) |
| Testes backend ao fechar | **457 passando**, 0 falhas |
| Testes frontend ao fechar | 271 passando, ~34 falhando |
| Frontend build | 0 erros, 0 warnings |
| Decisao | Tasks restantes movidas para backlog (Sprint 3+) |

**Nota:** Sprint 1 focou em tornar a ficha visualizavel end-to-end. O objetivo foi atingido: FichaDetail funcional com dados reais (atributos, aptidoes, vantagens com categoriaNome). A criacao de ficha (wizard) e a progressao (XP/nivel) ficam para Sprint 2.

---

## Sprint 2 — Progresso Geral

| Metrica | Valor |
|---------|-------|
| Tasks totais Sprint 2 | **35** (13 Spec 007 + 13 Spec 006 + 6 Spec 005 + 1 bug XP + 1 fix testes + 1 T-QW frontend) |
| Concluidas | **34** + 2 bonus (S007-T0, T1, T2, T3+T4+T5, T7, T8, T9, T11, **T12**, S015-T5, T1, T2, T3, T4, S006-T1, T2, T4, T5, T6, T7, T8, T9, T10, **T11**, **T12b**, **T13**, S005-P1T1, P1T2, P1T3, P2T1, P2T2, P2T3, URG-01, URG-02, QW-Bug3) |
| Em andamento | 0 |
| Pendentes | 0 do escopo original |
| Bloqueadas | 0 (PA-004 resolvido sessao 19 — S007-T5alt e S007-T10 desbloqueados) |
| Testes backend | **581 passando**, 0 falhas |
| Testes frontend | **624 passando**, 0 falhas (+21 da rodada 11) |
| Gaps resolvidos pelo PO | **TODOS** (GAP-01 a GAP-08, INCONS-02, P-03, PA-001/002, Q14-Q17) |

**Novas decisoes do PO (Q14-Q17):**
- **Q14 Modo Sessao:** Polling 30s no MVP. SSE/WebSocket para versao futura.
- **Q15 Essencia:** Dois endpoints semanticos — `POST /fichas/{id}/essencia/gastar` (JOGADOR) + `POST /fichas/{id}/essencia/resetar` (MESTRE).
- **Q16 GAP-PONTOS-CONFIG:** Classe/Raca dando pontos extras por nivel = gap pos-MVP. Nao bloqueia Sprint 2.
- **Q17 pontosAptidaoGastos:** = SUM(FichaAptidao.base) — sem distincao criacao/level-up.

**Decisao arquitetural pendente (S007-T0):**
- `FichaAptidao.classe` nao e zerado no reset para compatibilidade com entrada manual. O `aplicarClasseAptidaoBonus` sobrescreve com valor calculado quando ha config automatica. Validar com Tech Lead: sobrescrever (atual) ou somar com manual?

---

## Tracks Paralelos — Sprint 4

```
TRACK A — Backend (sequencial):
  S015-T4 (auto-concessao vantagens, enum OrigemFichaVantagem)
  -> S023-BE (refatorar VantagemPreRequisito polimorficamente)

TRACK B — Frontend Tasks Desbloqueadas (paralelo com Track A):
  S007-T10 (FormulaEditor FORMULA_CUSTOMIZADA)

TRACK C — Frontend UX/NPC (paralelo com A+B, dominio diferente):
  UX-JOGO-SELECT (seletor de jogo configs)
  NPC-FORM-CAMPOS (raça/classe no form NPC)

TRACK D — Frontend Apos S023-BE (depende de Track A):
  S023-FE (aba pre-requisitos polimorfica)
  -> UX-PREREQ-EMPTY (estado vazio pre-req)
  -> UX-NIVEL-MIN-PREREQ (nivelMinimo na lista)

TRACK E — Frontend UX Quick Fixes (independente, pode paralelizar):
  UX-ACCEPT-BTN (17 telas acceptButtonProps)
  UX-COR-PREVIEW (cores hex com swatch)
  UX-TIPO-VANTAGEM (fix form criacao vantagem)

TRACK F — Backend + Frontend (independente):
  NPC-TEMPLATE (nivel dificuldade NPC — nova config BE + tela FE)

TRACK G — Qualidade (P2):
  S014-T2-T4+T6 (cobertura testes)
  AUDIT-BE-FE (auditoria features sem tela)
  UX-BASE-COMP (4 telas → BaseConfigComponent)
  UX-DIALOG-WIDTH (9 dialogs)
```

### Plano Anti-Conflito

| Agente | Escopo | NAO tocar |
|--------|--------|-----------|
| BE-1 | S015-T4 + S023-BE | Frontend, EfeitoForm |
| FE-1 | S007-T10 (FormulaEditor) | NPC, jogo-select, configs gerais |
| FE-2 | UX-JOGO-SELECT + NPC-FORM-CAMPOS | vantagens-config/*, efeito-form/* |
| FE-3 | UX-ACCEPT-BTN + UX-COR-PREVIEW | wizard, ficha-detail, NPC |

---

## Prioridade de Ataque — Sprint 2

### URGENTE + QUICK WINS (independente, fazer PRIMEIRO)

| ID | Spec/Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| URG-01 | Spec 006 T3 | Backend | Bloquear XP no PUT /fichas/{id} para JOGADOR — vuln seguranca ativa | Nenhuma | **[CONCLUIDO]** (rodada 2 — ja tinha @PreAuthorize, fix erros compilacao) |
| URG-02 | — | Frontend | Corrigir ~34 testes frontend falhando | Nenhuma | **[CONCLUIDO]** (rodada 2 — 38 testes corrigidos, 359/359 passando) |
| QW-Bug3 | 009-ext T-QW | Frontend | Corrigir rota errada no NpcsComponent (L432: /jogador/ -> /mestre/) | Nenhuma | **[CONCLUIDO]** (rodada 2) |

### P0-ABSOLUTA: Spec 007 — VantagemEfeito + Motor de Calculos (13 tasks)

> Sem o motor correto, TODA ficha criada tera valores matematicamente errados. Bloqueia Spec 006.

**Fase Pre-Requisito (T0) — BLOQUEIA TODAS AS DEMAIS**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S007-T0 | 007/T0 | Backend | Corrigir 6 bugs no FichaCalculationService (ClasseBonus, RacaBonus, ClasseAptidaoBonus zerados + nivel nao recalcula ao ganhar XP) | — | **[CONCLUIDO]** (sessao 10, 464 testes) |

**Bugs corrigidos por T0:**
- GAP-CALC-01: `FichaBonus.classe` = `ClasseBonus.valorPorNivel * ficha.nivel` (nunca calculado)
- GAP-CALC-02: `FichaAptidao.classe` = `ClasseAptidaoBonus.bonus` (nunca calculado)
- GAP-CALC-03: `FichaAtributo.outros` = `RacaBonusAtributo.bonus` (nunca aplicado)
- GAP-CALC-06: `Ficha.nivel` nao recalculava ao ganhar XP
- GAP-CALC-07: `FichaAmeaca.recalcularTotal()` nao incluia `nivel`
- GAP-CALC-08: `FichaVida.recalcularTotal()` ignorava `vigorTotal` e `nivel`

**Fase Backend (T1-T8)**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S007-T1 | 007/T1 | Backend | Adaptar modelo de dados para efeitos de vantagem | S007-T0 | **[CONCLUIDO]** (rodada 2 — SCHEMA-01/02, FichaProspeccao.dadoDisponivel, findByFichaIdWithEfeitos, stub aplicarEfeitosVantagens) |
| S007-T2 | 007/T2 | Backend | FichaCalculationService — BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_VIDA, BONUS_ESSENCIA | S007-T1 | **[CONCLUIDO]** (rodada 3 — escopo expandido, commit `52738da`) |
| S007-T3+T4+T5 | 007/T3-T5 | Backend | FichaCalculationService — BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP | S007-T2 | **[CONCLUIDO]** (rodada 4 — commit `0621bc8`, 7/8 efeitos) |
| S007-T5alt | 007/T5alt | Backend | FichaCalculationService — FORMULA_CUSTOMIZADA | S007-T1, PA-004 | **[PENDENTE]** (PA-004 resolvido sessao 19 — desbloqueado) |
| S007-T7 | 007/T7 | Backend | Insolitus — campo tipoVantagem + endpoint de concessao | S007-T1 | **[CONCLUIDO]** (rodada 5 — commit `bd75582`) |
| S007-T8 | 007/T8 | Backend | Testes de integracao para todos os tipos de efeito | T3-T7 | **[CONCLUIDO]** (rodada 6 [13:20] — commit `e1bbe50`, 20 testes, 7 tipos cobertos) |

**Fase Frontend (T9-T12)**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S007-T9 | 007/T9 | Frontend | VantagensConfigComponent — secao de efeitos (31 testes) | S007-T8 | **[CONCLUIDO]** (rodada 7 — commit `f19c213`, EfeitoFormComponent standalone, formulario dinamico, preview calculado, aba "Efeitos" com badge) |
| S007-T10 | 007/T10 | Frontend | FormulaEditor integrado para FORMULA_CUSTOMIZADA | S007-T9 | **[PENDENTE]** (PA-004 resolvido sessao 19 — editor seleciona campo-alvo) |
| S007-T11 | 007/T11 | Frontend | Seletor de dado para DADO_UP (progressao visual) | S007-T9 | **[CONCLUIDO]** (rodada 9 — commit `0c5fb29`, seletor dado base + slider nivel + dado resultante calculado) |
| S007-T12 | 007/T12 | Frontend | UI de concessao de Insolitus pelo Mestre | S007-T9 | **[CONCLUIDO]** (rodada 10 — commit `d23a3cf`, dialog Mestre, busca por nome filtrando vantagens INSOLITUS, botoes Conceder/Revogar, 17 testes) |

**Pontos em Aberto (confirmar antes de iniciar T6/T7):**
- PA-001: RESOLVIDO — MESTRE pode revogar QUALQUER vantagem (incluindo Insolitus)
- PA-002: RESOLVIDO — Enum TipoVantagem (VANTAGEM | INSOLITUS)
- PA-004: **RESOLVIDO** (sessao 19) — editor seleciona campo-alvo (atributo/bonus); 1 formula por campo
- PA-006: PENDENTE — VIG/SAB hardcoded por abreviacao (GAP-CALC-09) — fora do escopo de T0

### P0: Spec 006 — Wizard de Criacao de Ficha (13 tasks)

> Depende de Spec 007 para calculos corretos. Backend (T1-T5) pode iniciar em paralelo com 007.

**Fase Backend (T1-T5) — pode iniciar em paralelo com Spec 007**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S006-T1 | 006/T1 | Backend | Campo status + endpoint /completar | — | **[CONCLUIDO]** (rodada 4 — commit `d55e312`, 9 testes) |
| S006-T2 | 006/T2 | Backend | Validacao RacaClassePermitida na criacao | — | **[CONCLUIDO]** (rodada 5 — commit `1cb523a`) |
| S006-T3 | 006/T3 | Backend | Bloquear XP no PUT /fichas/{id} para JOGADOR | — | (ABSORVIDA por URG-01) |
| S006-T4 | 006/T4 | Backend | Endpoint PUT /fichas/{id}/xp acumulativo + motivo | — | **[CONCLUIDO]** (rodada 6 [11:19] — commit `d37b227`, 14 testes) |
| S006-T5 | 006/T5 | Backend | pontosDisponiveis no FichaResumoResponse | — | **[CONCLUIDO]** (rodada 5 — commit `61b0bb4`) |

> **NOTA:** S006-T3 e URG-01 sao a mesma task. Corrigir imediatamente como urgencia, nao esperar o restante da Spec 006.

**Fase Frontend (T6-T13) — depende de T1 e T5, e de Spec 007 para calculos**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S006-T6 | 006/T6 | Frontend | Passo 1: Identificacao (rewrite do wizard) (34 testes) | S006-T1 | **[CONCLUIDO]** (rodada 7 — commit `064d648`, FichaWizardComponent + StepIdentificacaoComponent, rotas criar/criar-npc, retomada rascunho, classesFiltradas) |
| S006-T7 | 006/T7 | Frontend | Passo 2: Descricao fisica (21 testes) | S006-T6 | **[CONCLUIDO]** (rodada 8 — StepDescricaoComponent, formPasso2, salvarPasso2 via PUT /fichas/{id}, campo descricao em ficha.model.ts + UpdateFichaDto. 11 testes step + 10 wizard) |
| S006-T8 | 006/T8 | Frontend | Passo 3: Distribuicao de atributos (17 testes) | S006-T7 | **[CONCLUIDO]** (rodada 9 — commit `dd2677d`, StepAtributosComponent, UI distribuicao pontos, forkJoin dados, 9 testes step + 8 wizard) |
| S006-T9 | 006/T9 | Frontend | Passo 4: Distribuicao de aptidoes | S006-T5, S006-T6 | **[CONCLUIDO]** (rodada 10 — commit `1895648`, StepAptidoesComponent, pool de NivelConfig.pontosAptidao, formPasso4 + carregarDadosPasso4 + salvarPasso4 PUT /fichas/{id}/aptidoes, 22 testes. Agente A sem tokens, bugfixes manuais pelo PM) |
| S006-T10 | 006/T10 | Frontend | Passo 5: Compra de vantagens iniciais | S006-T5, S006-T6 | **[CONCLUIDO]** (rodada 10 — commit `0702b03`, StepVantagensComponent smart, filtro categoria + busca, agrupamento por categoria, estadoBotao comprar/comprada/sem-pontos/comprando, POST /fichas/{id}/vantagens + GET /fichas/{id}/resumo, 40 testes) |
| S006-T11 | 006/T11 | Frontend | Passo 6: Revisao e confirmacao | S006-T1, S006-T6 | **[CONCLUIDO]** (rodada 11 — commit `bc4cb06`, StepRevisaoComponent 5 secoes, confirmarCriacao() + completar(), 27 testes) |
| S006-T12 | 006/T12 | Frontend | Auto-save visual (indicador de salvamento) | S006-T6 | **[CONCLUIDO]** (rodada 11 — commit `bc4cb06`, WizardRodapeComponent standalone shared, 4 estados, 17 testes) |
| S006-T13 | 006/T13 | Frontend | Badge "incompleta" na listagem de fichas | S006-T1 | **[CONCLUIDO]** (rodada 11 — commit `6c6ccda`, FichasListComponent badge p-tag severity=warn, retomar() navega para wizard com fichaId, FichaStatus 4 valores, 14 testes) |

### P0: Spec 005 — Gestao de Participantes (6 tasks)

> Pode iniciar backend em paralelo com Specs 007 e 006. Frontend depende de P1-T2.

**Fase Backend (P1-T1 a P1-T3)**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S005-P1T1 | 005/P1-T1 | Backend | Corrigir logica de re-solicitacao (strategy Reactivate) | — | **[CONCLUIDO]** (rodada 6 [11:14] — commit `32b984f`, 6 testes) |
| S005-P1T2 | 005/P1-T2 | Backend | Endpoints faltantes (banir, desbanir, remover, meu-status, filtro) | S005-P1T1 | **[CONCLUIDO]** (rodada 6 [11:19] — commit `7a71a55`, 12 testes) |
| S005-P1T3 | 005/P1-T3 | Backend | Testes de integracao completos (2 novos, 29 total) | S005-P1T1, S005-P1T2 | **[CONCLUIDO]** (rodada 7 — commit `32d4b94`, `naoDeveCancelarSolicitacaoInexistente` + `banirNaoDeveSetarDeletedAt`) |

**Fase Frontend (P2-T1 a P2-T3)**

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S005-P2T1 | 005/P2-T1 | Frontend | Alinhar API service e Business service com novos endpoints | S005-P1T2 | **[CONCLUIDO]** (rodada 8→R9 — commit `d6c3b34`, testes API completos) |
| S005-P2T2 | 005/P2-T2 | Frontend | JogoDetail do Mestre (semantica remover/banir/desbanir + filtro + badge) | S005-P2T1 | **[CONCLUIDO]** (rodada 9 — commit `f3637e9`, bug critico corrigido: remover chamava banir. 11 testes, OnPush, filtro status) |
| S005-P2T3 | 005/P2-T3 | Frontend | JogosDisponiveis do Jogador (solicitar, status, cancelar) | S005-P2T1 | **[CONCLUIDO]** (rodada 10 — commit `d2c262c`, statusPorJogo signal, helpers podeEntrar/podeSolicitar/podeCancelar, badges p-tag, 34 testes) |

### Spec 015 — ConfigPontos + DefaultProvider (rodada 2: T5 CONCLUIDA)

| ID | Spec Task | Tipo | Descricao | Dependencia | Status |
|----|-----------|------|-----------|-------------|--------|
| S015-T5 | 015/T5 | Backend | Corrigir 8 bugs DefaultProvider + defaults canonicos | Nenhuma (independente) | **[CONCLUIDO]** (rodada 2 — BUG-DC-02..09 exceto DC-03, 10 testes unitarios) |
| S015-T1 | 015/T1 | Backend | 4 novas entidades, repos, DTOs, mappers | Nenhuma | **[CONCLUIDO]** (rodada 3 — 22 arquivos, commit `9ac2465`) |
| S015-T2 | 015/T2 | Backend | CRUD endpoints como sub-recursos (14 endpoints) | S015-T1 (CONCLUIDA) | **[CONCLUIDO]** (rodada 4 — commit `ba52d29`, 26 testes) |
| S015-T3 | 015/T3 | Backend | Integrar pontos no FichaResumoResponse | S015-T1 (CONCLUIDA), S007-T1 (CONCLUIDA) | **[CONCLUIDO]** (rodada 5 — commit `5dc8bf2`) |
| S015-T4 | 015/T4 | Backend | Auto-concessao de vantagens pre-definidas (8 testes) | S015-T1 (CONCLUIDA), S007-T7 (CONCLUIDA) | **[CONCLUIDO]** (rodada 7 — commit `1dec7db`, enum OrigemVantagem, VantagemAutoConcessaoService, integracao criar/concederXp, loop pulo niveis) |

> **Nota S015-T5:** BUG-DC-03 (LimitadorConfig) NAO implementado — entidade nao existe, funcionalidade ja coberta por NivelConfig.limitadorAtributo. Os 8 bugs corrigidos incluem: Cabeca 75%, Indole 3 valores, Presenca 4 valores, Genero 3, Necromante fixes, Sangue fixes, limitadorAtributo do DTO. Defaults adicionados: 9 BonusConfig, 8 PontosVantagem, 8 CategoriaVantagem, 22 vantagens canonicas.

### Quick Wins Frontend: 009-ext T-QW (3 bugs)

> Independentes de qualquer spec. Podem ser corrigidos AGORA.

| ID | Arquivo | Descricao | Estimativa | Dependencia | Status |
|----|---------|-----------|-----------|-------------|--------|
| QW-Bug1 | ficha-header.component.ts | Barras vida/essencia hardcoded | 1h30 | Nenhuma | **[CONCLUIDO]** (R3 — ja corrigido R2) |
| QW-Bug2 | ficha-vantagens-tab.component.ts | Pontos vantagem hardcoded 0 | 15min | Nenhuma | **[CONCLUIDO]** (R3 — ja corrigido R2) |
| QW-Bug3 | npcs.component.ts L432 | Mestre redirecionado para /jogador/fichas em vez de rota de Mestre | 5min | Nenhuma | **[CONCLUIDO]** (rodada 2) |

---

## Backlog Sprint 3+ (P1/P2 — nao entram no Sprint 2)

| Prio | Spec | Tasks | Descricao |
|------|------|-------|-----------|
| P1 | 008 | 4 | Sub-recursos Classes/Racas frontend (T1-T4) |
| **P1** | **016** | **~21** | **Sistema de Itens/Equipamentos — EM ESPECIFICACAO (3 BAs em paralelo). SD-1 config backend pode iniciar Sprint 3. Ver [`COORDENACAO-MULTI-BA.md`](specs/016-sistema-itens/COORDENACAO-MULTI-BA.md)** |
| P1 | 012 | 12 ativas | Niveis e Progressao frontend (T1-T11, T14; T12/T13 fora do MVP) |
| P1 | 009-ext | 10 | NPC Visibility + Prospeccao + Essencia + Reset (T1-T10, excluindo T-QW) |
| P1 | 010 | 9 | Roles ADMIN/MESTRE/JOGADOR refactor — IMPLEMENTAR POR ULTIMO |
| P2 | 011 | 8 | Galeria e Anotacoes |
| Tech Debt | SP1-T13 | 1 | Membros do corpo em VidaSectionComponent (barras HP) |
| ~~Tech Debt~~ | ~~SP1-T27~~ | ~~1~~ | ~~DDL producao~~ — **CANCELADO** (Hibernate ddl-auto para RC) |
| Tech Debt | C1 | 1 | handleReorder wiring para 13 componentes (12/13 feito) |
| Tech Debt | INCONS-01 | 1 | API-CONTRACT.md desatualizado |
| Tech Debt | DT-FE-01/02/03 | 3 | Divida tecnica frontend |
| **P3** | **013** | **6** | **Documentacao Tecnica — Javadoc, OpenAPI, TSDoc, swagger.json** |
| **P3** | **014** | **6** | **Cobertura de Testes — JaCoCo 75% branch, Vitest coverage, testes faltantes** |
| Pos-MVP | GAP-PONTOS-CONFIG | — | Classe/Raca dando pontos extras por nivel (decisao PO: pos-MVP) |

> **Nota Spec 016:** Especificacao **100% COMPLETA** (2026-04-04). 11 tasks (7B+4F), dataset D&D 5e SRD (40 itens), API contracts, UX wireframes — tudo pronto. SD-1 (Configuracao backend) pode iniciar implementacao no Sprint 3. SD-2 (Inventario/Calculos) depende de Spec 007 completa. 4 novos pontos pendentes para PO: PA-016-DS-01..04 (em PERGUNTAS-PENDENTES-PO.md).

---

## Decisoes do PO (todas resolvidas em 2026-04-03)

| ID | Decisao | Impacto |
|----|---------|---------|
| GAP-01 | Wizard 5-6 passos, todos campos obrigatorios, auto-save rascunho no backend | Spec 006 T6-T13 desbloqueadas |
| GAP-02 | XP read-only para Jogador. Vulnerabilidade ativa — URGENTE corrigir | URG-01 / S006-T3 |
| GAP-03 | VantagemEfeito e P0-ABSOLUTA (Spec 007) antes de qualquer modulo de ficha | Confirma sequencia 007 > 006 |
| GAP-04 | REJEITADO pode re-solicitar sem cooldown. BANIDO reversivel. DELETE = remover provisorio | Spec 005 desbloqueada |
| GAP-05 | NPC mecanicamente identico. descricao para todos. Mestre revela stats granularmente | Spec 009-ext T1-T2 |
| GAP-06 | Pontos acumulam. Level up automatico. FichaResumoResponse inclui pontos disponiveis | Spec 006 T5, Spec 012 T5 |
| GAP-07 | essenciaGasta persiste. Reset manual pelo Mestre. Endpoint POST /fichas/{id}/essencia/resetar | Spec 009-ext T4-T5 |
| GAP-08 | Dois endpoints para prospeccao (conceder + usar). Mestre pode reverter; Jogador nao | Spec 009-ext T3 |
| INCONS-02 | Fichas NUNCA deletadas. Status "morta"/"abandonada". Remover DELETE /fichas. Backend retorna 405 | Spec 006 T1 |
| P-03 | ADMIN = apenas gestao de usuarios no MVP. Sem bypass de canAccessJogo | Spec 010 T3/T4 simplificadas |
| PA-001 | Mestre pode revogar QUALQUER vantagem (incluindo Insolitus). Jogador nunca remove | Spec 007 T7, T12 |
| PA-002 | Enum TipoVantagem (VANTAGEM / INSOLITUS) | Spec 007 T7 |
| Renascimento | FORA DO MVP. T12/T13 da Spec 012 removidos | Spec 012 reduzida para 12 tasks |
| Q14 | Modo Sessao: Polling 30s no MVP. SSE/WebSocket para versao futura | Frontend: setInterval simples |
| Q15 | Essencia: dois endpoints semanticos (gastar/resetar) | Spec 009-ext T4-T5 |
| Q16 | GAP-PONTOS-CONFIG: Classe/Raca pontos extras por nivel = pos-MVP | Nao bloqueia Sprint 2 |
| Q17 | pontosAptidaoGastos = SUM(FichaAptidao.base) — sem distincao criacao/level-up | Simplifica Spec 006 T5 e Spec 012 T5 |

---

## Caminho Critico — Sprint 2

```
RODADA 2 CONCLUIDA (sessao 10):
  [Backend]  URG-01: Corrigir bug XP ........................ **[CONCLUIDO]** (ja tinha @PreAuthorize)
  [Backend]  S007-T0: Corrigir 6 bugs calculo base .......... **[CONCLUIDO]** (rodada 1, 464 testes)
  [Backend]  S007-T1: Adaptar modelo dados ................... **[CONCLUIDO]** (rodada 2, 474 testes)
  [Backend]  S015-T5: DefaultProvider fixes .................. **[CONCLUIDO]** (rodada 2, 10 testes unitarios)
  [Frontend] QW-Bug3: Rota NPC errada ....................... **[CONCLUIDO]** (rodada 2)
  [Frontend] URG-02: Fix 38 testes frontend falhando ........ **[CONCLUIDO]** (rodada 2, 359/359)

RODADA 3 CONCLUIDA:
  [Backend]  S007-T2: BONUS_ATRIBUTO+APTIDAO+VIDA+ESSENCIA .. **[CONCLUIDO]** (commit 52738da, 474 testes)
  [Backend]  S015-T1: 4 entidades ConfigPontos .............. **[CONCLUIDO]** (commit 9ac2465, 22 arquivos)
  [Frontend] QW-Bug1/2: Barras+pontos vantagem .............. **[CONCLUIDO]** (ja corrigidos R2)

RODADA 4 CONCLUIDA:
  [Backend]  S007-T3+T4+T5: DERIVADO+VIDA_MEMBRO+DADO_UP ... **[CONCLUIDO]** (commit 0621bc8, 509 testes)
  [Backend]  S006-T1: FichaStatus + /completar .............. **[CONCLUIDO]** (commit d55e312, 9 testes novos)
  [Backend]  S015-T2: 14 CRUD endpoints sub-recursos ........ **[CONCLUIDO]** (commit ba52d29, 26 testes novos)

RODADA 5 CONCLUIDA:
  [Backend]  S007-T7: Insolitus + endpoint concessao ......... **[CONCLUIDO]** (commit bd75582)
  [Backend]  S006-T2: validacao RacaClassePermitida .......... **[CONCLUIDO]** (commit 1cb523a)
  [Backend]  S006-T5: pontosDisponiveis no response .......... **[CONCLUIDO]** (commit 61b0bb4)
  [Backend]  S015-T3: integrar pontos no FichaResumo ......... **[CONCLUIDO]** (commit 5dc8bf2)

RODADA 6 CONCLUIDA [14:01]:
  [Backend]  S005-P1T1: re-solicitacao Reactivate ............ **[CONCLUIDO]** (commit 32b984f [11:14])
  [Backend]  S005-P1T2: endpoints faltantes participantes .... **[CONCLUIDO]** (commit 7a71a55 [11:19])
  [Backend]  S006-T4: XP acumulativo + motivo ................ **[CONCLUIDO]** (commit d37b227 [11:19])
  [Backend]  S007-T8: testes integracao 7 efeitos ............ **[CONCLUIDO]** (commit e1bbe50 [13:20])

RODADA 7 CONCLUIDA [14:54]:
  [Backend]  S015-T4: auto-concessao vantagens ............... **[CONCLUIDO]** (commit 1dec7db)
  [Backend]  S005-P1T3: testes integracao participantes ...... **[CONCLUIDO]** (commit 32d4b94)
  [Frontend] S007-T9: frontend efeitos UI .................... **[CONCLUIDO]** (commit f19c213)
  [Frontend] S006-T6: wizard passo 1 identificacao ........... **[CONCLUIDO]** (commit 064d648)

RODADA 8 CONCLUIDA [15:30] (commits formalizados R9):
  [Frontend] S006-T7: wizard passo 2 descricao ............... **[CONCLUIDO]** (commit dd2677d)
  [Frontend] S005-P2T1: participantes frontend API/service ... **[CONCLUIDO]** (commit d6c3b34)

RODADA 9 CONCLUIDA [21:05] (5 tasks, +31F testes, 581B+490F):
  [Frontend] S005-P2T1: participantes API testes ............. **[CONCLUIDO]** (commit d6c3b34)
  [Frontend] S006-T7: wizard passo 2 descricao ............... **[CONCLUIDO]** (commit dd2677d)
  [Frontend] S006-T8: wizard passo 3 atributos ............... **[CONCLUIDO]** (commit dd2677d)
  [Frontend] S005-P2T2: JogoDetail Mestre remover/banir ...... **[CONCLUIDO]** (commit f3637e9)
  [Frontend] S007-T11: DadoUp seletor progressao ............. **[CONCLUIDO]** (commit 0c5fb29)

RODADA 10 CONCLUIDA [07:10] (4 tasks, +113F testes, 581B+603F):
  [Frontend] S006-T9: wizard passo 4 aptidoes ................ **[CONCLUIDO]** (commit 1895648, 22 testes)
  [Frontend] S006-T10: wizard passo 5 vantagens .............. **[CONCLUIDO]** (commit 0702b03, 40 testes)
  [Frontend] S005-P2T3: JogosDisponiveis jogador ............. **[CONCLUIDO]** (commit d2c262c, 34 testes)
  [Frontend] S007-T12: UI concessao Insolitus ................ **[CONCLUIDO]** (commit d23a3cf, 17 testes)

RODADA 11 CONCLUIDA (3 tasks, +21F testes, 581B+624F):
  [Frontend] S006-T11: wizard passo 6 revisao ................ **[CONCLUIDO]** (commit bc4cb06, 27 testes)
  [Frontend] S006-T12: auto-save visual WizardRodape ......... **[CONCLUIDO]** (commit bc4cb06, 17 testes)
  [Frontend] S006-T13: badge incompleta listagem ............. **[CONCLUIDO]** (commit 6c6ccda, 14 testes)

PROXIMA RODADA (rodada 12 — Sprint 3):
  Spec 008 T1-T4 (sub-recursos Classes/Racas frontend)
  Spec 012 T1-T4, T14 (PontosVantagem/CategoriaVantagem)
  Spec 009 T1-T8 (NPC visibility + essencia + prospeccao)

DESBLOQUEADO (PA-004 resolvido sessao 19):
  [Frontend] S007-T10 (FormulaEditorEfeito) .................. [PENDENTE]
```

**Sprint 2 ENCERRADO (R11):** 34/35 tasks (97%) + 2 bonus concluidas. Wizard de criacao de ficha 100% completo (6 passos). Backend 100% (exceto T5alt). Frontend: 624 testes (vs 271 do Sprint 1). PA-004 resolvido na sessao 19 — S007-T10 e T5alt agora PENDENTES.

---

## Riscos em Aberto

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| ~~GAP-02 vuln XP ATIVA~~ | ~~Jogador altera propria XP~~ | **RESOLVIDO** (rodada 2) — PUT /fichas/{id}/xp ja tinha @PreAuthorize("hasRole('MESTRE')") |
| ~~Spec 007 T0 corrige 6 bugs~~ | ~~Bugs encadeados~~ | **RESOLVIDO** — T0 concluido com 7 testes, 464 total |
| ~~S007-T1 adaptar modelo~~ | ~~Bloqueava T2-T7~~ | **RESOLVIDO** (rodada 2) — SCHEMA-01, SCHEMA-02, stub aplicarEfeitosVantagens |
| ~~34 testes frontend falhando~~ | ~~Build CI nao confiavel~~ | **RESOLVIDO** (rodada 2) — 359/359 testes passando, 0 falhas |
| Spec 007 impacta ~20-30 arquivos | Risco de regressao nos calculos existentes | Spec 007 T8 cobre com testes de integracao extensivos |
| ~~PA-004 nao resolvido~~ | ~~FORMULA_CUSTOMIZADA sem alvo~~ | **RESOLVIDO** (sessao 19) — editor seleciona campo-alvo |
| PA-006 nao resolvido | VIG/SAB hardcoded por abreviacao (GAP-CALC-09) | Fora do escopo de T0; escalar ao PO |
| ~~Sprint 2 encerrado com S007-T10 bloqueada~~ | ~~PA-004 sem decisao do PO~~ | **RESOLVIDO** (sessao 19) — PA-004 e PA-015-04 ambos resolvidos |

---

## Referencia: Sprint 1 Completo (historico)

<details>
<summary>Clique para expandir o Sprint 1 completo</summary>

### Tasks Concluidas (29/31)

| ID | Descricao | Status |
|----|-----------|--------|
| SP1-T01 a T07 | FichaDetail + JogosDisponiveis | CONCLUIDO |
| SP1-T08 a T12 | Resumo, design specs, models, business service | CONCLUIDO |
| SP1-T18 a T23 | Security NPC, vida/prospeccao, perfil, N+1, testes 457 | CONCLUIDO |
| SP1-T24 a T26 | Build fixes Angular | CONCLUIDO |
| SP1-T28 a T31 | GET atributos/aptidoes, categoriaNome, NPC backend | CONCLUIDO |
| QW-2, QW-3, QW-5, FIX-01 | Quick wins + fixes | CONCLUIDO |

### Tasks Nao Concluidas (movidas para backlog)

| ID | Descricao | Motivo |
|----|-----------|--------|
| SP1-T13 | Barras HP por membro do corpo | Prioridade rebaixada — Sprint 3 |
| ~~SP1-T27~~ | ~~DDL producao~~ | **CANCELADO** — Hibernate ddl-auto suficiente para 0.0.1-RC |

### Commits Finais do Sprint 1

| Commit | Descricao |
|--------|-----------|
| `027e709` | test(ficha): testes de integracao GET atributos, aptidoes e categoriaNome — 457 testes |
| `9f87701` | feat(ficha): GET atributos/aptidoes por ficha + categoriaNome em FichaVantagemResponse |
| `d650ddf` | feat(ficha): restringir acesso a NPCs apenas para o Mestre |
| `4702887` | feat(ficha): vida/prospeccao endpoints, NPC descricao, N+1 fixes — 422 testes |
| `8fece26` | feat(usuario): implementar atualizacao de perfil do usuario |

</details>

---

*Atualizado: 2026-04-06 [07:10] (rodada 10 concluida: 33/35, 581B+603F testes) | PM/Scrum Master*
