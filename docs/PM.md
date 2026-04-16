# PM.md — Klayrah RPG: Ficha Controlador

> **Papel deste arquivo:** tracking detalhado por area (backend, frontend,
> testes, motor de formulas) + **historico consolidado das sprints encerradas**.
> Complementa `MASTER.md` (indice) e `SPRINT-ATUAL.md` (sprint corrente).
>
> Ponto de entrada rapido: `HANDOFF-SESSAO.md` → `MASTER.md` → `PM.md`.
> Mapa completo de docs: `README.md`.
>
> Gerado em: 2026-04-01 | Atualizado: 2026-04-16 (sessao 22, rev.23 — Rodada 19: FichaStatus FE (badges + canEdit MORTA/ABANDONADA + remove Excluir Jogador) + Aba Sessao (vida/essencia/membros + polling 30s + badge header) + S026-T05 (badge pendentes sidebar Mestre) + S026-T08 (coluna Data Criacao + BE criadoEm) + T-025-13 (remove Criar Ficha sidebar Jogador) + wizard path param/labels. INCONS-02 agora end-to-end BE+FE. GAP novo: GAP-ESTADO-COMBATE P1 para R20. Backend 832 testes a auditar + 21 afetados em R19. Frontend +58 testes confirmados em R19. Completude ~99%+) | Branch: `main`
> Cronologia: `docs/historico/CRONOLOGIA.md`

---

## Status Geral

| Area | Progresso | Observacao |
|------|-----------|------------|
| Backend: Infraestrutura | 100% | Schema gerenciado pelo Hibernate (ddl-auto). Sem DDL manual necessario para 0.0.1-RC. |
| Backend: Configuracoes (13 CRUDs) | 100% | Todos com testes de integracao |
| Backend: Motor de Formulas | **100%** | 7/8 TipoEfeito + 20 testes integracao. FORMULA_CUSTOMIZADA desbloqueado (PA-004 resolvido). Frontend: T9/T10/T11/T12 CONCLUIDOS (T10 entregue Wave P0, 54 testes). |
| Backend: Conceder XP / Insolitus / Prospeccao | **100%** | Endpoints `PUT /fichas/{id}/xp`, concessao Insolitus e `GET /jogos/{id}/prospeccao/pendentes` com UI Mestre (wave 3) |
| Backend: NPC dificuldade | **100%** | NpcDificuldadeConfig + FocoNpc (BE wave 2) com selector + auto-fill no FE (wave 3) |
| Backend: Ficha de Personagem (Spec 006/007) | **100%** | Wizard 6 passos completo. **R18:** FichaStatus ganha MORTA/ABANDONADA (COMPLETA @Deprecated); PUT /fichas/{id}/status para Mestre; INCONS-02 do PO totalmente implementado no BE. **R19:** INCONS-02 end-to-end (FE alinhado com badges/canEdit/remove Excluir Jogador). **R20 pendente:** `GET /fichas/{id}/estado-combate` para expor `danoRecebido` por membro. |
| Backend: NPC + Duplicacao (Spec 009) | **100%** | POST /jogos/{id}/npcs + POST /fichas/{id}/duplicar implementados. **R18:** DELETE /fichas/{id} restaurado como `excluirNpc`, restrito a `isNpc=true`. |
| Backend: Lista de Jogos (Spec 026) | **100%** | **R19:** `criadoEm` adicionado a `JogoResumoResponse` (+ mapper) — desbloqueia coluna Data Criacao no FE. 21 testes afetados passando. |
| Backend: NPC Visibilidade (Spec 009-ext) | **100%** | visivelGlobalmente, FichaVisibilidade (4 endpoints), ProspeccaoUso (endpoints conceder/usar/reverter), resetar-estado, essenciaAtual/vidaAtual no resumo. 32 novos testes. |
| Backend: Anotacoes | 100% | FichaAnotacaoController + Service implementados e testados |
| Backend: Seguranca (role checks) | **100%** | TODOS controllers com @PreAuthorize. |
| Backend: Perfil Usuario | **100%** | GET/PUT /api/v1/usuarios/me implementado e testado |
| Backend: Participantes (Spec 005) | **100%** | P1T1/P1T2/P1T3 + P2T1/P2T2/P2T3 CONCLUIDOS (6/6). Strategy Reactivate, banir/desbanir/remover/meu-status, JogoDetail Mestre, JogosDisponiveis Jogador. |
| Frontend: Modelos e Servicos de API | **100%** | Sub-recursos (008), PontosVantagem/CategoriaVantagem (012), FichaVisibilidade + Prospeccao (009-ext), DTOs UX-TIPO-VANTAGEM (R18), AtualizarStatusFicha (R18), `criadoEm` em JogoResumoResponse (R19). |
| Frontend: Componentes | **~100%** | + S023-FE, UX mass fix, BaseConfig migration (habilidades + tipos-item + raridades + itens). **Wave 3:** GAP-NPC-FE-01 + GAP-XP-01 + GAP-INS-01 + GAP-PROS-01. **R17:** Spec 024 T1 + GAP-DASH-01. **R18:** Pacote UX final + Dashboard + type fixes. **R19:** FichaStatus FE (badges + canEdit + remove Excluir Jogador) + aba Sessao (vida/essencia/membros + polling 30s + badge header) + S026-T05 (badge sidebar) + S026-T08 (Data Criacao) + T-025-13 + wizard labels/path param. Residual: AUDIT-BE-FE baixa prio. |
| Frontend: Testes | **100%** | ~1525+ pos-R17 + R18 + **+58 testes confirmados em R19** (33 FichaStatus FE + 25 aba Sessao) — reconciliar total |

**Completude geral estimada: ~99%+** (Sprint 4 ENCERRADO — Wave P0 + P1w2 + P1w3 + R17 + R18 + R19 CONCLUIDAS — 17/17 tasks P1 + 4/5 P2 + Spec 026 T05/T08 + T-025-* FE; GAP-EXPRT-01 CANCELADO R18)
**Backend: 832 testes pre-R18, 0 falhas — auditar apos R18 (FichaStatus + PUT /status + DELETE NPC) + R19 (21 testes afetados em JogoResumoResponse.criadoEm)**
**Frontend: ~1525+ testes pos-R17 + R18 + +58 em R19** (reconciliar total na proxima rodada)
**Total tasks MVP: ~130+** (estimado — inclui Spec 023, Spec 024, Spec 026 T05/T08, T-025-* R19, UX fixes, NPC gaps + 6 GAPs auditoria BE->FE — 5/6 entregues; GAP-EXPRT-01 CANCELADO R18)
**GAP novo R19:** GAP-ESTADO-COMBATE (P1, R20) — endpoint `GET /fichas/{id}/estado-combate` para expor `danoRecebido` por membro.

---

## Sprints

### Sprint 1 — "Ficha Jogavel" — CONCLUIDO (94%)

**Periodo:** 2026-04-01 a 2026-04-03
**Resultado:** 29/31 tasks concluidas. FichaDetail funcional com dados reais end-to-end.

| Metrica | Valor |
|---------|-------|
| Tasks concluidas | 29/31 (94%) |
| Testes backend ao fechar | 457 |
| Testes frontend ao fechar | 271 passando, ~34 falhando |
| Tasks movidas para backlog | 1 (SP1-T13 barras HP membro) |

**Entregas principais:**
- FichaDetailComponent com 5 abas funcionais (atributos, aptidoes, vantagens, anotacoes, resumo)
- JogosDisponiveisComponent
- GET /fichas/{id}/atributos e /aptidoes com testes
- Security NPC: acesso restrito ao Mestre
- Perfil usuario: GET/PUT /usuarios/me
- Build Angular: 0 erros, 0 warnings
- 12 correcoes de build PrimeNG 21

### Sprint 2 — "Motor Correto + Ficha Funcional" — ENCERRADO (97%)

**Periodo:** 2026-04-05 a 2026-04-06
**Tasks:** 35 total + 2 bonus
**Progresso:** 34/35 concluidas (97%) + 2 bonus — S007-T10 bloqueada PA-004. 581B+624F testes.

| Prio | Spec | Tasks | Descricao | Status |
|------|------|-------|-----------|--------|
| ~~URGENTE~~ | 006 T3 | 1B | Fix bug XP | **CONCLUIDO** (rodada 2) |
| ~~URGENTE~~ | — | 1F | Fix 38 testes frontend falhando | **CONCLUIDO** (rodada 2 — 359/359) |
| ~~QW~~ | 009-ext T-QW | 3 bugs | QW-Bug1/2/3 | **CONCLUIDO** (rodadas 2-3) |
| ~~P0-ABS~~ | 007 T0 | 1B | Corrigir 6 bugs motor | **CONCLUIDO** (rodada 1) |
| ~~P0-ABS~~ | 007 T1 | 1B | Adaptar modelo dados para efeitos | **CONCLUIDO** (rodada 2) |
| ~~P0~~ | 015 T5 | 1B | DefaultProvider fixes (8 bugs) | **CONCLUIDO** (rodada 2) |
| ~~P0-ABS~~ | 007 T2 | 1B | BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_VIDA, BONUS_ESSENCIA | **CONCLUIDO** (rodada 3) |
| ~~P0-ABS~~ | 007 T3+T4+T5 | 1B | BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP | **CONCLUIDO** (rodada 4) |
| ~~P0-ABS~~ | 007 T7 | 1B | Insolitus — tipoVantagem + endpoint concessao | **CONCLUIDO** (rodada 5) |
| ~~P0-ABS~~ | 007 T8 | 1B | Testes integracao 7 tipos de efeito (20 testes) | **CONCLUIDO** (rodada 6 [13:20]) |
| ~~P0-ABS~~ | 007 T9 | 1F | VantagensConfig secao de efeitos (31 testes) | **CONCLUIDO** (rodada 7 — commit `f19c213`) |
| ~~P0~~ | 015 T1 | 1B | 4 novas entidades ConfigPontos | **CONCLUIDO** (rodada 3) |
| ~~P0~~ | 015 T2 | 1B | 14 CRUD endpoints sub-recursos | **CONCLUIDO** (rodada 4) |
| ~~P0~~ | 015 T3 | 1B | Integrar pontos no FichaResumoResponse | **CONCLUIDO** (rodada 5) |
| ~~P0~~ | 015 T4 | 1B | Auto-concessao vantagens pre-definidas (8 testes) | **CONCLUIDO** (rodada 7 — commit `1dec7db`) |
| ~~P0~~ | 006 T1 | 1B | FichaStatus + /completar | **CONCLUIDO** (rodada 4) |
| ~~P0~~ | 006 T2 | 1B | Validacao RacaClassePermitida | **CONCLUIDO** (rodada 5) |
| ~~P0~~ | 006 T4 | 1B | PUT /xp acumulativo + motivo + FichaResumoResponse | **CONCLUIDO** (rodada 6 [11:19]) |
| ~~P0~~ | 006 T5 | 1B | pontosDisponiveis no response | **CONCLUIDO** (rodada 5) |
| ~~P0~~ | 006 T6 | 1F | Wizard Passo 1 Identificacao (34 testes) | **CONCLUIDO** (rodada 7 — commit `064d648`) |
| ~~P0~~ | 005 P1T1 | 1B | Corrigir re-solicitacao (strategy Reactivate) | **CONCLUIDO** (rodada 6 [11:14]) |
| ~~P0~~ | 005 P1T2 | 1B | Endpoints faltantes (banir/desbanir/remover/meu-status) | **CONCLUIDO** (rodada 6 [11:19]) |
| ~~P0~~ | 005 P1T3 | 1B | Testes integracao participantes (2 testes novos) | **CONCLUIDO** (rodada 7 — commit `32d4b94`) |
| ~~P0-ABS~~ | 007 T9/T11/T12 | 3F | VantagemEfeito: efeitos UI, DadoUp, Insolitus | **CONCLUIDO** (R7/R9/R10) |
| ~~P0~~ | 006 T7-T13 | 7F | Wizard passos 2-6 + auto-save + badge | **CONCLUIDO** (R8-R11) |
| ~~P0~~ | 005 P2T1/T2/T3 | 3F | Participantes frontend | **CONCLUIDO** (R8-R10) |
| BLOQUEADO | 007 T10 | 1F | FormulaEditorEfeito (PA-004) | BLOQUEADO |

**Criterio de sucesso — TODOS ATINGIDOS:**
- ~~Bug XP corrigido~~ FEITO
- ~~6 bugs do motor corrigidos (T0)~~ FEITO
- ~~Modelo adaptado para efeitos (T1)~~ FEITO
- ~~34 testes frontend corrigidos~~ FEITO
- ~~DefaultProvider bugs corrigidos (S015-T5)~~ FEITO
- ~~7/8 tipos de VantagemEfeito integrados~~ FEITO (T5alt/T10 bloqueados PA-004)
- ~~Insolitus modelado com TipoVantagem + endpoint + UI~~ FEITO
- ~~ConfigPontos CRUD completo~~ FEITO (S015-T1/T2/T3/T4)
- ~~pontosDisponiveis no FichaResumoResponse~~ FEITO
- ~~RacaClassePermitida validada na criacao~~ FEITO
- ~~T8: testes de integracao extensivos para todos os efeitos~~ FEITO (20 testes, 7 tipos)
- ~~Wizard de ficha funcional de 6 passos com auto-save~~ **FEITO (R11)**
- ~~Participantes com maquina de estados completa~~ **FEITO (R10)**

---

## O que esta FEITO (por spec)

- **Spec 001** — 13 CRUDs de configuracao + Template Klayrah (GameConfigInitializerService)
- **Spec 003** — Refactor: DTOs records, validacoes, exceptions, mappers, testes base
- **Spec 004** — SiglaValidationService, CategoriaVantagem, PontosVantagem, VantagemPreRequisito, ClasseBonus, RacaClassePermitida, VantagemEfeito (8 tipos entity)
- **Spec 005 (backend 100%)** — JogoParticipante: P1T1 (strategy Reactivate), P1T2 (5 endpoints), P1T3 (29 testes). Faltam: P2T1-P2T3 frontend
- **Spec 006 (100% CONCLUIDA)** — Wizard 6 passos completo (Identificacao, Descricao, Atributos, Aptidoes, Vantagens, Revisao). StepRevisaoComponent, WizardRodapeComponent shared, confirmarCriacao() -> PUT /fichas/{id}/completar -> navega /fichas/{id}. Badge "Incompleta" na listagem retoma rascunho. 624 testes frontend.
- **Spec 005 (100% CONCLUIDA)** — JogoParticipante: strategy Reactivate, banir/desbanir/remover/meu-status/filtro, 29 testes backend. Frontend: JogoDetail Mestre (remover/banir/filtro), JogosDisponiveis Jogador (solicitar/cancelar/status/badges).
- **Spec 007 (100% CONCLUIDA)** — 7/8 TipoEfeito + 20 testes integracao. Frontend: EfeitoFormComponent (31 testes), DadoUp seletor, UI Insolitus (dialog busca + revogar), **T10 FormulaEditor (54 testes, Wave P0)**. T5alt BE desbloqueado (PA-004 resolvido).
- **Spec 015 (7/7 CONCLUIDA)** — 4 entidades ConfigPontos, 14 CRUD endpoints, pontos integrados no FichaResumoResponse, DefaultProvider 8 bugs corrigidos, T6/T7 FE concluidos. **T4 CONCLUIDO (Wave P0)** — auto-concessao ja existia como OrigemVantagem, Wave P0 expôs campo `origem` no FichaVantagemResponse (commit `4c04a54`).
- **Spec 023 (CONCLUIDO BE+FE)** — Pre-requisitos polimorficos: 6 tipos (VANTAGEM/RACA/CLASSE/ATRIBUTO/NIVEL/APTIDAO), OR dentro do tipo + AND entre tipos, 409 em delecao. BE commit `934eaff` (+18 testes), FE commit `d08d1c9` (+56 testes, aba polimorfica + chips removiveis). Schema aplicado por Hibernate `ddl-auto=update`.
- **NPC Dificuldade BE (NOVO wave 2)** — Nova entidade `NpcDificuldadeConfig` + enum `FocoNpc` (FISICO/MAGICO). Templates Facil/Medio/Dificil/Elite/Chefe. +18 testes -> 832 total. FE (selector no form + auto-preenchimento atributos) pendente = GAP-NPC-FE-01 (P1).
- **Spec 024 (CONCLUIDO 2/2 — rodada 17 2026-04-15)** — UX Melhorias Sprint 4: T1 UX-TIPO-VANTAGEM entregue R17 (checkbox Insolitus no form + coluna Tipo na tabela + desabilitar formulaCusto ao marcar Insolitus + 14 testes novos, 70 totais no componente, 1 commit FE); T2 UX-NIVEL-MIN-PREREQ via Spec 023 FE.
- **UX-BASE-COMP (rodada 17)** — raridades-item-config (+27 testes) e itens-config (+35 testes) migradas para BaseConfigComponent. 3 commits FE. Juntas com as 2 telas da wave 2 (habilidades + tipos-item), todas as telas alvo estao migradas.
- **GAP-DASH-01 (rodada 17)** — Dashboard do Mestre implementado: rota `/mestre/dashboard`, 3 cards (resumo, fichas por nivel, ultimas alteracoes), link na sidebar (apenas MESTRE). +16 testes, 1 commit FE.
- **UX-PREREQ-EMPTY (rodada 17)** — confirmado CONCLUIDO: estado vazio da aba pre-requisitos ja havia sido implementado no codigo durante a Spec 023 FE (commit `d08d1c9`).
- **Spec 008-old** — DashboardController, duplicacao de jogo, export/import de config, resumo de ficha, filtros, reordenacao batch (100% backend)
- **Spec 009** — NPC security, POST /jogos/{id}/npcs, POST /fichas/{id}/duplicar, anotacoes. 100% backend
- **Frontend Sprint 2** — Wizard completo (6 passos), EfeitoFormComponent, JogoDetail Mestre, JogosDisponiveis Jogador, badge Incompleta, WizardRodapeComponent. **624 testes passando, 0 falhas**

---

## Decisoes do PO (2026-04-03) — Todas Resolvidas

| ID | Decisao | Impacto na Implementacao |
|----|---------|--------------------------|
| GAP-01 | Wizard 5-6 passos, todos campos obrigatorios, auto-save rascunho | Spec 006 T6-T13 desbloqueadas |
| GAP-02 | XP read-only para Jogador. URGENTE corrigir | Spec 006 T3 — URG-01 |
| GAP-03 | VantagemEfeito e P0-ABSOLUTA antes de ficha | Confirma 007 > 006 |
| GAP-04 | REJEITADO re-solicita sem cooldown. BANIDO reversivel | Spec 005 desbloqueada |
| GAP-05 | NPC mecanicamente identico. descricao para todos | Spec 009-ext |
| GAP-06 | Pontos acumulam. Level up automatico | Spec 006 T5, Spec 012 |
| GAP-07 | essenciaGasta persiste. Reset manual Mestre | Spec 009-ext |
| GAP-08 | Dois endpoints prospeccao (conceder + usar) | Spec 009-ext |
| INCONS-02 | Fichas NUNCA deletadas. Status morta/abandonada | Spec 006 T1. Remover DELETE /fichas |
| P-03 | ADMIN = gestao usuarios apenas. Sem bypass canAccessJogo | Spec 010 simplificada |
| PA-001/002 | Mestre revoga qualquer vantagem. Enum TipoVantagem | Spec 007 T7/T12 |
| Renascimento | FORA DO MVP | Spec 012 T12/T13 removidas |
| Q14 | Modo Sessao: Polling 30s no MVP. SSE/WebSocket futuro | Frontend: setInterval simples |
| Q15 | Essencia: dois endpoints semanticos (gastar/resetar) | Spec 009-ext T4-T5 |
| Q16 | GAP-PONTOS-CONFIG: Classe/Raca pontos extras por nivel = pos-MVP | Nao bloqueia Sprint 2 |
| Q17 | pontosAptidaoGastos = SUM(FichaAptidao.base) — sem distincao criacao/level-up | Simplifica Spec 006 T5 e Spec 012 T5 |

---

## Versao 0.0.1-RC — Criterio de Fechamento

> **Decisao registrada (2026-04-06):** Ao concluir todo o backlog funcional (Specs 005-016 implementadas), fecharemos a versao **0.0.1-RC** no backend e no frontend simultaneamente. Nesse momento:
>
> 1. **Tag de versao:** `git tag v0.0.1-RC` em ambos os repositorios (backend e frontend)
> 2. **Preparacao de deploy:** configuracao de ambiente (Docker Compose prod, variaveis de ambiente, secrets), healthcheck, CORS producao
> 3. **Armazenamento de imagens:** configurar Cloudinary (tier gratuito) para o ambiente de RC
> 4. **Documentacao GitHub:** toda a documentacao tecnica e de produto sera migrada para os recursos nativos do GitHub:
>    - `README.md` dos repositorios com badges de build e cobertura
>    - GitHub Wiki para documentacao de dominio e guias de usuario
>    - GitHub Pages para documentacao de API (Swagger/OpenAPI exportado)
>    - Releases com changelog estruturado
>    - Issues/Projects para rastreamento publico do roadmap
> 5. **Schema do banco:** Hibernate `ddl-auto=update` para RC. Migracao para Flyway apenas quando houver ambiente de producao real (pos-RC).
>
> **Nao ha tasks de deploy no backlog atual** — isso entra apenas ao fechar o backlog funcional.

---

## Backlog Priorizado — Sprint 4 (atualizado pos-Wave P0)

### P0 — CONCLUIDA (Wave P0, sessao 20, 2026-04-14)

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 1 | S007-T10 | FE | FormulaEditor para FORMULA_CUSTOMIZADA | S007-T9 OK | **[CONCLUIDO]** (54 testes) |
| 2 | S015-T4 | BE | Auto-concessao vantagens pre-definidas + campo `origem` no response | S015-T3 OK | **[CONCLUIDO]** (pre-impl + commit `4c04a54`) |
| 3 | S023-BE | BE | Pre-requisitos polimorficos: 6 tipos, AND/OR, 409 em delecao | Spec 004 OK | **[CONCLUIDO]** (commit `934eaff`, +18 testes) |
| 4 | UX-JOGO-SELECT + UX-COR-PREVIEW | FE | Game selector no ConfigLayout + p-colorpicker bidirecional raridades | Nenhuma | **[CONCLUIDO]** (+22 testes) |
| 5 | NPC-FORM-CAMPOS | FE | Raça/Classe/configs no formulario de NPC | Spec 009 OK | **[CONCLUIDO]** (pre-impl + 2 testes) |

### P1 wave 2 — CONCLUIDA (sessao 20, 2026-04-14)

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 6 | S023-FE | FE | Aba pre-requisitos polimorfica + chips removiveis | **[CONCLUIDO]** (+56 testes, commit `d08d1c9`) |
| 7 | UX mass fix | FE | acceptButtonProps + dialog widths em 13-14 telas | **[CONCLUIDO]** (commit `141b054`) |
| 8 | NPC-DIFICULDADE-BE | BE | NpcDificuldadeConfig + enum FocoNpc | **[CONCLUIDO]** (+18 testes -> 832 total) |
| 9 | BaseConfig migration | FE | habilidades-config + tipos-item-config → BaseConfigComponent | **[CONCLUIDO]** (+23 testes cada, commit `f30e74d`) |

### P1 wave 3 — CONCLUIDA (sessao 20, 2026-04-15)

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 10 | GAP-NPC-FE-01 | FE | Selector dificuldade no form NPC + auto-preenchimento atributos | **[CONCLUIDO]** (+31 testes) |
| 11 | GAP-XP-01 | FE | Tela Mestre `PUT /fichas/{id}/xp` (Conceder XP — fix endpoint) | **[CONCLUIDO]** (+14 testes) |
| 12 | GAP-INS-01 | FE | UI selecao/concessao Insolitus | **[CONCLUIDO]** (UI ja existia + testes incluidos em GAP-XP-01) |
| 13 | GAP-PROS-01 | FE | Painel Mestre prospeccao pendentes (componente + rota + sidebar) | **[CONCLUIDO]** (+28 testes) |

### Rodada 17 — CONCLUIDA (sessao 21, 2026-04-15)

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 14 | **Spec 024 T1** (UX-TIPO-VANTAGEM) | FE | checkbox Insolitus no form + coluna Tipo na tabela | **[CONCLUIDO]** (+14 testes) |
| 15 | UX-BASE-COMP (raridades-item-config) | FE | migracao para BaseConfigComponent | **[CONCLUIDO]** (+27 testes) |
| 16 | UX-BASE-COMP (itens-config) | FE | migracao para BaseConfigComponent | **[CONCLUIDO]** (+35 testes) |
| 17 | GAP-DASH-01 | FE | Dashboard Mestre 3 cards + rota + sidebar | **[CONCLUIDO]** (+16 testes) |
| — | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | **[CONCLUIDO]** (via Spec 023 FE) |

### Rodada 18 — CONCLUIDA (sessao 22, 2026-04-16)

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 18.1 | UX-TIPO-VANTAGEM (consolidacao DTOs) | FE | tipoVantagem no form + coluna Tipo + DTOs req/resp | **[CONCLUIDO]** |
| 18.2 | Dashboard Mestre (JogoDashboard) | FE | Nova tela `/mestre/dashboard` com fichas ativas, pendentes, NPCs | **[CONCLUIDO]** |
| 18.3 | Migracao BaseConfig (itens + raridades) | FE | `ItensConfigComponent` + `RaridadesItemConfigComponent` migrados | **[CONCLUIDO]** |
| 18.4 | FE-1 Quick Wins | FE | remove Export/Import, Data Criacao, setTimeout, ConfigComponent dead code | **[CONCLUIDO]** |
| 18.5 | FE-ROUTE-BUG | FE | corrige rota Editar em ficha-detail (`/jogador/fichas/:id/edit`) | **[CONCLUIDO]** |
| 18.6 | FE-CONSOLE-LOG | FE | remove console.log do AuthService | **[CONCLUIDO]** |
| 18.7 | UX-PROFILE-NOME | FE | edicao de nome em Profile via PUT /api/v1/usuarios/me | **[CONCLUIDO]** |
| 18.8 | UX-BACK-BUTTON | FE | PageHeaderComponent com backRoute em 12 telas de config + dashboard | **[CONCLUIDO]** |
| 18.9 | NPC-DELETE + NPC-BACK | FE | botao delete com ConfirmDialog + botao voltar em NpcsComponent | **[CONCLUIDO]** |
| 18.10 | Type fixes | FE | TagSeverity, Partial<Record>, ProgressBar [color], imports | **[CONCLUIDO]** |
| 18.11 | FichaStatus MORTA/ABANDONADA | BE | adicionados ATIVA/MORTA/ABANDONADA; COMPLETA @Deprecated | **[CONCLUIDO]** |
| 18.12 | PUT /fichas/{id}/status | BE | novo endpoint para Mestre alterar status + AtualizarStatusFichaRequest | **[CONCLUIDO]** |
| 18.13 | excluirNpc | BE | DELETE /fichas/{id} restaurado, restrito a NPC (isNpc=true) | **[CONCLUIDO]** |

### Rodada 19 — CONCLUIDA (sessao 22 continuada, 2026-04-16)

| # | ID | Tipo | Descricao | Status |
|---|-----|------|-----------|--------|
| 19.1 | T-025-12 | FE | Labels wizard: passo 5 = "Vantagens", passo 6 = "Revisao" | **[CONCLUIDO]** (`27fd3b8`) |
| 19.2 | T-025-08 | FE | Wizard le fichaId do path param `:id` alem de queryParam | **[CONCLUIDO]** (`3d04f3b`) |
| 19.3 | T-025-01-FE + T-025-09 + T-025-10 | FE | Badges de status na fichas-list + canEdit bloqueia MORTA/ABANDONADA + remove botao Excluir do Jogador (INCONS-02 FE) | **[CONCLUIDO]** (+33 testes, `9b5ba1b`) |
| 19.4 | T-025-03 + T-025-04 + T-025-07 + T-025-11 | FE | Aba "Sessao" em ficha-detail (vida/essencia/membros do corpo) + polling 30s + badge status no FichaHeaderComponent | **[CONCLUIDO]** (+25 testes, `65f0c19`) |
| 19.5 | S026-T08 FE | FE | Coluna "Data Criacao" restaurada na lista de jogos | **[CONCLUIDO]** (`ea0bb09`) |
| 19.6 | S026-T05 | FE | Badge de participantes pendentes no sidebar do Mestre | **[CONCLUIDO]** (`aaa1abc`) |
| 19.7 | T-025-13 | FE | Remove item "Criar Ficha" do sidebar do Jogador | **[CONCLUIDO]** (`aaa1abc`) |
| 19.8 | S026-T08 BE | BE | `criadoEm` adicionado ao JogoResumoResponse + mapper atualizado | **[CONCLUIDO]** (21 testes afetados, `2fc8ece`) |

**Gap tecnico identificado na R19 (P1, R20):** **GAP-ESTADO-COMBATE** — `FichaResumoResponse` nao expoe `danoRecebido` dos membros do corpo, fazendo a aba Sessao iniciar com dano=0. **Decisao PO:** criar novo endpoint `GET /fichas/{id}/estado-combate` (ou similar) em R20, em vez de engrossar o `FichaResumoResponse`.

### P1 — TODOS ENTREGUES

Spec 024 T1 era a ultima P1 pendente; entregue em R17 e consolidada em R18 com DTOs. **14/14 P1 entregues.**

### P2 — Estado apos R18

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| — | GAP-DASH-01 | FE | Dashboard do Mestre | — | **[CONCLUIDO]** (R17 + consolidacao R18) |
| — | UX-BASE-COMP | FE | itens + raridades para BaseConfigComponent | — | **[CONCLUIDO]** (R17 + consolidacao R18) |
| — | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos | — | **[CONCLUIDO]** (via Spec 023 FE) |
| 4 | GAP-EXPRT-01 | FE | Interface Export/Import config | Endpoint BE | **[CANCELADO] (R18)** — botoes removidos no FE-1 Quick Win |
| 5 | AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | Nenhuma | [PENDENTE — baixa prio] |

### Pos-MVP

| Item | Descricao |
|------|-----------|
| GAP-PONTOS-CONFIG | Classe/Raca dando pontos extras por nivel (decisao PO Q16) |
| Renascimento | Mecanica completa de renascimento (nivel 31+) |
| Modo Sessao formal | SSE/WebSocket em vez de Polling 30s |
| XP em lote | Conceder XP para toda a mesa de uma vez |

### CORTADOS (sessao 20)

| Item | Motivo |
|------|--------|
| ~~Spec 010~~ (Roles ADMIN refactor) | CORTADO — complexidade transversal, baixo valor para MVP |
| ~~Spec 013~~ (Documentacao tecnica) | CORTADO — baixa prioridade, nao impacta funcionalidade |
| ~~PA-017-04~~ (Exportar/Importar config) | CORTADO — escopo excessivo para MVP |

### Tech Debt (backlog permanente)

| ID | Descricao | Prio |
|----|-----------|------|
| SP1-T13 | Barras HP por membro do corpo (VidaSection) | Baixa |
| C1 | handleReorder wiring 13o componente | Baixa |
| INCONS-01 | API-CONTRACT.md desatualizado | Media |
| DT-FE-01 | atualizarAnotacao() fantasma | Baixa |
| DT-FE-02 | CategoriaVantagem URL sem /v1/ | Baixa |
| DT-FE-03 | ConfigStore type assertions any | Baixa |

---

## Riscos em Aberto

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| PA-006 nao resolvido | VIG/SAB hardcoded (GAP-CALC-09) | Fora do escopo T0; PO decide |
| ~~Spec 023 refatora VantagemPreRequisito~~ | — | **RESOLVIDO (Wave P0+P1w2)** — BE+FE entregues |
| ~~17 telas com acceptButtonProps deprecated~~ | — | **RESOLVIDO (wave 2)** — UX mass fix em 13-14 telas |
| ~~NPC sem raça/classe no form~~ | — | **RESOLVIDO (Wave P0)** |
| ~~GAPs BE->FE (XP/Insolitus/Prospeccao/NPC-FE/DASH)~~ | — | **RESOLVIDOS (wave 3 + R17)** — 5 entregues; GAP-EXPRT-01 CANCELADO (R18) |
| ~~Divida UX parcial (dialogs, BaseConfig)~~ | — | **RESOLVIDO (wave 2 + R17 + R18)** — dialogs padronizados + 4 telas migradas (habilidades, tipos-item, raridades, itens) |
| ~~Spec 024 T1 UX-TIPO-VANTAGEM~~ | — | **RESOLVIDO (R17+R18)** — checkbox Insolitus + coluna Tipo + DTOs consolidados |
| ~~INCONS-02 (fichas deletadas fisicamente)~~ | — | **RESOLVIDO end-to-end** — BE (R18): FichaStatus + PUT /status + DELETE restrito NPC; FE (R19): badges + canEdit + remove Excluir Jogador |
| Auditoria backend pos-R18+R19 | FichaStatus novos valores + novos endpoints + JogoResumoResponse.criadoEm | Proxima rodada: rodar `./mvnw test` e reconciliar contagem global (era 832 pre-R18) |
| **GAP-ESTADO-COMBATE** (R19) | FichaResumoResponse nao expoe `danoRecebido` → aba Sessao FE inicia dano=0 | **R20 PRIORIDADE** — criar `GET /fichas/{id}/estado-combate` (decisao PO) |

---

## Documentos de Referencia

| Documento | Descricao |
|-----------|-----------|
| [`MASTER.md`](MASTER.md) | Indice mestre do projeto |
| [`SPRINT-ATUAL.md`](SPRINT-ATUAL.md) | Sprint 2 tracking detalhado com tracks paralelos |
| [`CRONOLOGIA.md`](CRONOLOGIA.md) | Cronologia reversa completa |
| [`PROXIMA-SESSAO.md`](PROXIMA-SESSAO.md) | Ponto de retomada para proxima sessao |
| [`specs/ROADMAP-MVP.md`](specs/ROADMAP-MVP.md) | Roadmap MVP com 5 fases |
| [`PRODUCT-BACKLOG.md`](PRODUCT-BACKLOG.md) | 93 User Stories |
| [`gaps/BA-GAPS-2026-04-02.md`](gaps/BA-GAPS-2026-04-02.md) | Dossie de gaps com respostas do PO |

---

*Atualizado: 2026-04-16 (rev.23 — sessao 22 Rodada 19: FichaStatus FE (badges + canEdit + remove Excluir Jogador) + Aba Sessao (vida/essencia/membros + polling 30s + badge header) + S026-T05 (badge pendentes sidebar Mestre) + S026-T08 (Data Criacao FE + BE criadoEm) + T-025-13 (remove Criar Ficha sidebar Jogador) + wizard labels/path param. INCONS-02 end-to-end BE+FE. Sprint 4 ENCERRADO. GAP-EXPRT-01 CANCELADO. Residuais: AUDIT-BE-FE baixa prio + **GAP-ESTADO-COMBATE** P1 para R20. Proxima rodada: criar endpoint `estado-combate`) | PM/Scrum Master*
