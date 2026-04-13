# PM.md — Klayrah RPG: Ficha Controlador

> **Papel deste arquivo:** tracking detalhado por area (backend, frontend,
> testes, motor de formulas) + **historico consolidado das sprints encerradas**.
> Complementa `MASTER.md` (indice) e `SPRINT-ATUAL.md` (sprint corrente).
>
> Ponto de entrada rapido: `HANDOFF-SESSAO.md` → `MASTER.md` → `PM.md`.
> Mapa completo de docs: `README.md`.
>
> Gerado em: 2026-04-01 | Atualizado: 2026-04-13 (sessao 20, rev.16 — repriorizacao backlog, Spec 023 aprovada, Spec 010+013 cortadas, divida UX catalogada) | Branch: `main`
> Cronologia: `docs/historico/CRONOLOGIA.md`

---

## Status Geral

| Area | Progresso | Observacao |
|------|-----------|------------|
| Backend: Infraestrutura | 100% | Schema gerenciado pelo Hibernate (ddl-auto). Sem DDL manual necessario para 0.0.1-RC. |
| Backend: Configuracoes (13 CRUDs) | 100% | Todos com testes de integracao |
| Backend: Motor de Formulas | **97%** | 7/8 TipoEfeito + 20 testes integracao. FORMULA_CUSTOMIZADA desbloqueado (PA-004 resolvido). Frontend: T9/T11/T12 CONCLUIDOS. **T10 P0 Sprint 4**. |
| Backend: Ficha de Personagem (Spec 006/007) | **100%** | Wizard 6 passos completo (T6-T13). S006-T1/T2/T4/T5 backend CONCLUIDOS. PUT /fichas/{id}/completar funcional. Navega para FichaDetail. |
| Backend: NPC + Duplicacao (Spec 009) | **100%** | POST /jogos/{id}/npcs + POST /fichas/{id}/duplicar implementados. |
| Backend: NPC Visibilidade (Spec 009-ext) | **100%** | visivelGlobalmente, FichaVisibilidade (4 endpoints), ProspeccaoUso (endpoints conceder/usar/reverter), resetar-estado, essenciaAtual/vidaAtual no resumo. 32 novos testes. |
| Backend: Anotacoes | 100% | FichaAnotacaoController + Service implementados e testados |
| Backend: Seguranca (role checks) | **100%** | TODOS controllers com @PreAuthorize. |
| Backend: Perfil Usuario | **100%** | GET/PUT /api/v1/usuarios/me implementado e testado |
| Backend: Participantes (Spec 005) | **100%** | P1T1/P1T2/P1T3 + P2T1/P2T2/P2T3 CONCLUIDOS (6/6). Strategy Reactivate, banir/desbanir/remover/meu-status, JogoDetail Mestre, JogosDisponiveis Jogador. |
| Frontend: Modelos e Servicos de API | **100%** | Sub-recursos (008), PontosVantagem/CategoriaVantagem (012), FichaVisibilidade + Prospeccao (009-ext) CONCLUIDOS. |
| Frontend: Componentes | **97%** | Wizard completo, configs, sub-recursos, 009-ext, 012, 016 T8-T11, 021 T2. Falta: S007-T10 (FormulaEditor), Spec 023 FE, UX fixes, NPC form. |
| Frontend: Testes | **100%** | **~1208 passando** (2 falhas pre-existentes ficha-vantagens-tab) |

**Completude geral estimada: ~92%** (Sprint 4 INICIADO — Spec 023 + tasks desbloqueadas + UX + NPC gaps)
**Backend: 796 testes, 0 falhas**
**Frontend: ~1208 testes passando** (2 falhas pre-existentes ficha-vantagens-tab)
**Total tasks MVP: ~115+** (estimado — inclui Spec 023, UX fixes, NPC gaps)

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
- **Spec 007 (97% CONCLUIDA)** — 7/8 TipoEfeito + 20 testes integracao. Frontend: EfeitoFormComponent (31 testes), DadoUp seletor, UI Insolitus (dialog busca + revogar). T5alt/T10 DESBLOQUEADOS (PA-004 resolvido).
- **Spec 015 (6/7)** — 4 entidades ConfigPontos, 14 CRUD endpoints, pontos integrados no FichaResumoResponse, DefaultProvider 8 bugs corrigidos, T6/T7 FE concluidos. **T4 DESBLOQUEADO — P0 Sprint 4** (auto-concessao vantagens pre-definidas + enum OrigemFichaVantagem)
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

## Backlog Priorizado — Sprint 4 (sessao 20)

### P0 — AGORA (tasks desbloqueadas + fundacao Spec 023)

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 1 | S007-T10 | FE | FormulaEditor para FORMULA_CUSTOMIZADA | S007-T9 OK | [PENDENTE] |
| 2 | S015-T4 | BE | Auto-concessao vantagens pre-definidas + enum OrigemFichaVantagem | S015-T3 OK | [PENDENTE] |
| 3 | S023-BE | BE | Refatorar VantagemPreRequisito polimorficamente (tipos, AND/OR, migration) | Spec 004 OK | [PENDENTE] — tasks por criar |
| 4 | UX-JOGO-SELECT | FE | Seletor de jogo nas telas de configuracao do Mestre | Nenhuma | [PENDENTE] |
| 5 | NPC-FORM-CAMPOS | FE | Raça/Classe/configs no formulario de criacao de NPC | Spec 009 OK | [PENDENTE] |

### P1 — PROXIMA RODADA

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 6 | S023-FE | FE | Aba pre-requisitos polimorfica + chips removiveis por tipo | S023-BE | [PENDENTE] |
| 7 | UX-ACCEPT-BTN | FE | Corrigir acceptButtonProps deprecated em 17 telas | Nenhuma | [PENDENTE] |
| 8 | UX-COR-PREVIEW | FE | Cores hex com preview visual (swatch) junto ao codigo | Nenhuma | [PENDENTE] |
| 9 | NPC-TEMPLATE | BE+FE | Nivel dificuldade NPC (Facil/Medio/Dificil/Elite/Chefe) + foco FISICO/MAGICO | Nenhuma | [PENDENTE] |
| 10 | UX-TIPO-VANTAGEM | FE | tipoVantagem no form de criacao de vantagem (Insolitus nao pode ser criado) | Nenhuma | [PENDENTE] |
| 11 | UX-NIVEL-MIN-PREREQ | FE | nivelMinimo exibido na lista de pre-req tipo VANTAGEM | S023-FE | [PENDENTE] |

### P2 — POS

| # | ID | Tipo | Descricao | Dependencia | Status |
|---|-----|------|-----------|-------------|--------|
| 12 | AUDIT-BE-FE | Auditoria | Auditar endpoints backend sem tela frontend correspondente | Nenhuma | [PENDENTE] |
| 13 | UX-BASE-COMP | FE | Migrar 4 telas para BaseConfigComponent (habilidades, itens, raridades, tipos-item) | Nenhuma | [PENDENTE] |
| 14 | UX-DIALOG-WIDTH | FE | Padronizar largura de dialogs em 9 telas | Nenhuma | [PENDENTE] |
| 15 | UX-PREREQ-EMPTY | FE | Estado vazio aba pre-requisitos — CTA "Adicionar primeiro pre-requisito" | S023-FE | [PENDENTE] |
| 16 | S014-T2-T4+T6 | BE+FE | Cobertura de testes (JaCoCo 50% para 75%) | Nenhuma | [PENDENTE] |

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
| Spec 023 refatora VantagemPreRequisito | Schema change em tabela com dados | Migration Flyway com default tipo='VANTAGEM' |
| 17 telas com acceptButtonProps deprecated | Botao confirmar exclusao visual incorreto | Fix rapido P1 |
| NPC sem raça/classe no form | Feature incompleta para Mestre | P0 Sprint 4 |
| Divida UX (dialogs, BaseConfig, cores) | UX inconsistente | P1/P2 |

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

*Atualizado: 2026-04-13 (rev.16 — sessao 20: repriorizacao backlog, Spec 023 aprovada, Spec 010+013 cortadas, divida UX catalogada, ~92% completude) | PM/Scrum Master*
