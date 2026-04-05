# Cronologia do Projeto — ficha-controlador

> Cronologia reversa (mais recente primeiro).
> Fonte: git log, docs existentes, sprint tracking.
> Ultima atualizacao: 2026-04-04

---

## 2026-04-04 — Sessao 10, Consolidacao Final Pos-Rodada 2

### Gerenciamento de Projeto
- Todos os docs de gerenciamento atualizados: HANDOFF-SESSAO.md (reescrito), PROXIMA-SESSAO.md (reescrito), SPRINT-ATUAL.md (rev.6), PM.md (rev.5), MASTER.md (rev.8)
- Rodada 3 planejada com **4 agentes** (nao 3): S007-T2, S007-T3 (sequencial com T2), S015-T1, QW-Bug1+Bug2
- Conflito identificado: T2 e T3 tocam mesmo arquivo (FichaCalculationService.java) — T3 roda APOS T2
- Sequencia de rodadas 4-7+ mapeada com tasks distribuidas por rodada

---

## 2026-04-04 — Sessao 10, Rodada 2: S007-T1 + S015-T5 + URG-01/URG-02 + QW-Bug3

### Backend — 474 testes passando (+10 da rodada 2)

**S007-T1 CONCLUIDA — Adaptar modelo de dados para efeitos**
- SCHEMA-01: `FichaAptidao.outros` campo adicionado
- SCHEMA-02: `FichaVidaMembro.bonusVantagens` campo adicionado
- `FichaProspeccao.dadoDisponivel` campo adicionado
- `FichaVantagemRepository.findByFichaIdWithEfeitos()` query JOIN FETCH criada
- `FichaCalculationService.recalcular()` aceita `List<FichaVantagem>` + stub `aplicarEfeitosVantagens()`

**S015-T5 CONCLUIDA — DefaultProvider: 8 bugs corrigidos**
- BUG-DC-02 a DC-09 corrigidos (Cabeca 75%, Indole 3 vals, Presenca 4 vals, Genero 3, Necromante, Sangue, limitadorAtributo DTO)
- BUG-DC-03 NAO implementado: entidade LimitadorConfig nao existe, funcionalidade em NivelConfig.limitadorAtributo
- Defaults adicionados: 9 BonusConfig, 8 PontosVantagem, 8 CategoriaVantagem, 22 vantagens canonicas
- 10 testes unitarios novos

**URG-01 CONCLUIDA** — PUT /fichas/{id}/xp ja tinha @PreAuthorize. Corrigidos erros compilacao pre-existentes.

### Frontend — 359/359 testes passando

**URG-02 CONCLUIDA** — 38 testes falhando corrigidos: base-config-table, atributos-config, niveis-config, npcs, formula-editor specs
**QW-Bug3 CONCLUIDA** — `verFicha()` em npcs.component.ts corrigida (/jogador/fichas -> /mestre/fichas)

### Nova regra de agentes (decisao PM)
- 1 task por agente (max). Tasks triviais (<30min): agrupar 2-3. Tasks >2h: agente proprio.

### Estado final da rodada 2
- Backend: **474 testes**, 0 falhas. Sprint 2: **6/35 concluidas** (17%)
- Frontend: **359 testes**, 0 falhas. Build limpo.
- Proxima rodada: S007-T2 (BONUS_ATRIBUTO) + S015-T1 (entidades) + QW-Bug1/Bug2

---

## 2026-04-04 — Sessao 10, Rodada 1: Spec 007 T0 Concluida + Spec 016 Completa

### Backend — Spec 007 T0 (bugs motor de calculos CORRIGIDOS)
- **464 testes** passando, 0 falhas (+7 nesta sessao, era 457)
- GAP-CALC-07: `FichaAmeaca.recalcularTotal()` removido da entidade
- GAP-CALC-08: `FichaVida.recalcularTotal()` removido da entidade
- GAP-CALC-03: `RacaBonusAtributo` agora aplicado em `FichaAtributo.outros`
- GAP-CALC-01: `ClasseBonus.valorPorNivel * nivel` agora aplicado em `FichaBonus.classe`
- GAP-CALC-02: `ClasseAptidaoBonus.bonus` agora aplicado em `FichaAptidao.classe`
- GAP-CALC-06: Novo endpoint `PUT /api/v1/fichas/{id}/xp` com recalculo de nivel e flag `levelUp`
- Queries JOIN FETCH adicionadas em `ClasseBonusRepository`, `ClasseAptidaoBonusRepository`, `RacaBonusAtributoRepository`
- Reset para idempotencia em `resetarCamposDerivaveis()`
- 7 novos testes em `FichaCalculationServiceBugsIntegrationTest`

### Spec 016 — Especificacao 100% completa
- **P3-T11-ui-inventario-ficha.md** criada (799 linhas) — task mais complexa do frontend da Spec 016
- **Dataset D&D 5e SRD** criado em `dataset/dataset-itens-srd.md` (481 linhas): 40 itens, 7 raridades, 20 tipos, equipamentos iniciais por classe
- BA-016-02: status mudou de NAO LANCADO para CONCLUIDO
- 4 novos pontos pendentes para o PO: PA-016-DS-01..04

### Decisao Arquitetural Pendente
- `FichaAptidao.classe` nao e zerado no reset para compatibilidade com entrada manual. O `aplicarClasseAptidaoBonus` sobrescreve com valor calculado quando ha config automatica. Questao: deve sobrescrever ou somar com valor manual?

### Estado final da rodada 1
- Backend: 464 testes, 0 falhas. Spec 007 T0 CONCLUIDO.
- Frontend: sem alteracoes nesta rodada (271 passando, ~34 falhando)
- Spec 016 agora 100% especificada (11 tasks + dataset + API contracts + UX)
- Proxima implementacao: Spec 015 T5 (DefaultProvider) e Spec 007 T1 (adaptar modelo dados)

---

## 2026-04-03 — Sessao 9: Resolucao de Gaps + Sprint 2 Planejado

### Decisoes do PO (TODAS as gaps resolvidas)
- **GAP-01**: Wizard 5-6 passos, todos campos obrigatorios, auto-save rascunho no backend
- **GAP-02**: XP read-only para Jogador. Vulnerabilidade de seguranca ativa — URGENTE corrigir
- **GAP-03**: VantagemEfeito e P0-ABSOLUTA (Spec 007) antes de qualquer modulo de ficha
- **GAP-04**: REJEITADO pode re-solicitar sem cooldown. BANIDO reversivel. DELETE = remover provisorio
- **GAP-05**: NPC mecanicamente identico. descricao para todos. Mestre revela stats granularmente
- **GAP-06**: Pontos acumulam. Level up automatico. FichaResumoResponse inclui pontos disponiveis
- **GAP-07**: essenciaGasta persiste. Reset manual pelo Mestre. Endpoint POST /fichas/{id}/essencia/resetar
- **GAP-08**: Dois endpoints para prospeccao (conceder + usar). Mestre pode reverter; Jogador nao
- **INCONS-02 CRITICO**: Fichas NUNCA deletadas. Status "morta"/"abandonada". Remover DELETE /fichas. Backend retorna 405
- **P-03**: ADMIN = apenas gestao de usuarios no MVP. Sem bypass de canAccessJogo
- **PA-001/PA-002**: Mestre pode revogar QUALQUER vantagem (incluindo Insolitus). Enum TipoVantagem (VANTAGEM | INSOLITUS)
- **Renascimento**: FORA DO MVP. T12/T13 da Spec 012 removidos

### Specs criadas/atualizadas
- **Spec 008** (Sub-recursos Classes/Racas): BA criou spec completa com 4 tasks frontend
- **Spec 012** (Niveis/Progressao): BA criou spec completa com 14 tasks (T12/T13 removidas = 12 ativas)
- Spec 011 (Galeria/Anotacoes): BA atualizando (Cloudinary para imagens, editor Markdown, 3 niveis de pasta)

### Planejamento (PM)
- Sprint 1 formalmente ENCERRADO (94%, 29/31 tasks, 457 testes backend)
- Sprint 2 planejado: 33 tasks (12 Spec 007 + 13 Spec 006 + 6 Spec 005 + 1 bug XP + 1 fix testes)
- SPRINT-ATUAL.md reescrito para Sprint 2
- PROXIMA-SESSAO.md atualizado com ponto de retomada detalhado
- MASTER.md reescrito com quadro completo de specs
- ROADMAP-MVP.md criado com 5 fases e totais consolidados
- EPICS-BACKLOG.md atualizado com specs 008 e 012

### Estado final da sessao
- Backend: 457 testes, 0 falhas. Bug XP ainda NAO corrigido
- Frontend: 271 passando, ~34 falhando, build limpo
- Todas as specs 005-012 com especificacao completa (spec+plan+tasks)

---

## 2026-04-03 — Sessao 8: Reprioritizacao Completa do Projeto

### Planejamento (PM)
- Reprioritizacao completa de todas as specs pelo PO
- MASTER.md reescrito com quadro completo de 8 specs ativas (007-012)
- ROADMAP-MVP.md criado com 5 fases de implementacao e totais consolidados
- EPICS-BACKLOG.md atualizado com specs 008 e 012
- Prioridades definidas: 007 (P0-ABSOLUTA) > 006+005 (P0) > 008+009+010+012 (P1) > 011 (P2)
- Bug GAP-02 (XP) marcado como URGENTE — vulnerabilidade de seguranca ativa
- Decisao pendente P-03 documentada: ADMIN faz bypass de canAccessJogo()?
- Contagem real de tasks: 59+ confirmadas, ~70-80 estimadas com specs 008/012

### Specs com tasks completas (spec+plan+tasks)
- 005 (6 tasks), 006 (13 tasks), 007 (12 tasks), 009-ext (10 tasks), 010 (9 tasks), 011 (8 tasks)

### Specs em criacao
- 008-sub-recursos-classes-racas: spec.md pronto, tasks pendentes
- 012-niveis-progressao-frontend: diretorio criado, spec pendente

---

## 2026-04-03 — Sessao 7: Especificacao em Paralelo + Atualizacao de Backlog

### Especificacao (BAs ativas em paralelo)
- Spec 005 (Participantes): spec.md + plan.md + tasks/INDEX.md completos (6 tasks: 3 backend, 3 frontend)
- Spec 006 (Wizard Ficha): spec.md + plan.md + tasks/INDEX.md completos (13 tasks: 5 backend, 8 frontend)
- Spec 007 (VantagemEfeito + Motor): spec.md completo, plan+tasks pendentes
- Spec 009-ext (NPC Visibility): diretorio criado, spec em elaboracao
- Spec 010 (Roles Refactor): diretorio criado, spec em elaboracao
- Spec 011 (Galeria/Anotacoes): diretorio criado, spec em elaboracao

### Designs UX em criacao
- RESET-ESTADO-MESTRE.md, LEVEL-UP.md, MODO-SESSAO.md, ADMINISTRACAO.md, ANOTACOES-GALERIA.md

### Planejamento
- MASTER.md atualizado com todas as specs e prioridades
- EPICS-BACKLOG.md atualizado com 009-ext, 010, 011
- Prioridade de implementacao definida: 007 > 006 > 005 > 009-ext > 010 > 011

---

## 2026-04-02 — Sessoes 5-6: Backend Endpoints + Testes + Gaps BA

### Backend
- **457 testes** passando, 0 falhas (+35 nesta sessao)
- `027e709` — 35 testes de integracao (GET atributos, aptidoes, categoriaNome)
- `9f87701` — GET /fichas/{id}/atributos e /aptidoes + categoriaNome em FichaVantagemResponse
- `d650ddf` — Security: NPCs restritos ao Mestre em todos os services
- `4702887` — PUT /fichas/{id}/vida e /prospeccao + NPC descricao + N+1 fixes (422 testes)
- `8fece26` — UsuarioController GET/PUT /usuarios/me (perfil do usuario)
- `c84549a` — Fix 4 issues moderados de code review

### Frontend
- Build limpo (0 erros, 0 warnings)
- 271 testes passando, ~34 falhando (correcao em andamento)
- US-FICHA-02/03 (atributos/aptidoes reais no Detail) EM ANDAMENTO
- US-FICHA-05 (tela NPC Mestre) EM ANDAMENTO

### Planejamento
- BA entregou dossie de gaps: `docs/gaps/BA-GAPS-2026-04-02.md`
  - 4 bloqueadores criticidade A (GAP-01 a GAP-04)
  - 4 alta prioridade criticidade B (GAP-05 a GAP-08)
  - 5 inconsistencias tecnicas (INCONS-01 a INCONS-05)
  - PO respondeu TODAS as perguntas inline no dossie
- 5 gaps resolvidos nesta sessao: G2, G3, G8, M4, M5
- Sprint 1 subiu de 85% para 94% (29/31 tasks)
- Sprint 2 planejado com 4 fases + decisoes de negocio

---

## 2026-04-02 — Sessao 4: Quick Wins + Auditorias Profundas

### Frontend Quick Wins
- QW-5: Tooltips em tabs desabilitadas (classes, racas, vantagens)
- QW-3: Aviso de pre-requisito ausente em AptidoesConfig
- QW-2: Badges de contagem na sidebar + reordenacao logica de dependencias
- FIX-01: totalFichas mestre-dashboard (array -> .length via computed())

### Auditorias
- **Tech Lead Frontend**: auditoria completa de codigo
  - 2 criticos (C1: reordenacao fake em 13 telas, C2: dashboard dados errados)
  - 6 major (M1-M5, M7: endpoints sem cobertura frontend)
  - 3 divida tecnica (DT-FE-01 a DT-FE-03)
- **BA (Fluxo Ficha)**: analise end-to-end
  - 5 bloqueadores de uso real (G1-G5)
  - 5 funcionalidade incompleta (G6-G10)
  - 8 User Stories mapeadas (US-FICHA-01 a US-FICHA-08)
  - 11 novas tasks para Sprint 2 (SP2-T01 a SP2-T11)

---

## 2026-04-02 — Sessao 3: Correcoes de Build Frontend

- 12 correcoes de compilacao Angular realizadas
- Build frontend passou de quebrado para 0 erros, 0 warnings
- Correcoes: PrimeNG 21 migration, modelos flat, imports @shared, componente zombie removido
- Tasks concluidas: SP1-T24, SP1-T25, SP1-T26

---

## 2026-04-01 — Sessoes 1-2: Sprint 1 Kickoff + FichaDetail + JogosDisponiveis

### Backend
- `97afae1` — Spec 009: NPC, duplicacao de ficha, anotacoes, updates diretos, seguranca
- `960bdcb` — VantagemEfeito com 8 tipos, NivelConfig.permitirRenascimento
- `48efe07` — API-CONTRACT.md criado
- `5cffcf1` — PM.md, PRODUCT-BACKLOG, UX-BACKLOG, BA analises criados

### Frontend
- FichaDetailComponent implementado (SP1-T01 a SP1-T06) — 5 abas
- JogosDisponiveisComponent implementado (SP1-T07)
- FichaBusinessService, models, design specs criados

### Planejamento
- Sprint 1 "Ficha Jogavel" iniciado com 27 tasks
- Time de 4+ agentes formado
- UX Backlog completo (auditoria de 26 componentes)
- Product Backlog: 93 User Stories mapeadas

---

## 2026-03-31 — Documentacao e Contrato de API

- `48efe07` — API-CONTRACT.md completo (contrato backend-frontend)
- `97afae1` — Spec 009 backend completo (NPC, duplicacao, anotacoes)
- `960bdcb` — VantagemEfeito (8 tipos) + permitirRenascimento
- TEAM-PLAN.md criado com 7 fases e 20+ issues

---

## 2026-03-10-11 — Specs 005-008 Backend

### Spec 008 — Utilidade e Fluidez
- `5fa50ff` — Dashboard, duplicacao de jogo, export/import, resumo ficha
- `0525c17` — Filtros por nome, reordenacao batch em todos controllers

### Spec 007 — Motor de Calculos
- `fd59d79` — FichaCalculationService, validacao e preview

### Spec 006 — Ficha de Personagem
- `7620feb` — Entity Ficha, repository, DTOs, mapper, sub-entidades, FichaService
- `508e1f5` — Testes de integracao com cobertura de branches
- `f8c3d8f` — FichaVantagemService, endpoints e testes
- `4470bab` — Suporte a filtros na listagem de fichas

### Spec 005 — Gestao de Participantes
- `18f5bfc` — StatusParticipante e evolucao JogoParticipante
- `ac7ef6e` — Service e controller do fluxo de participacao
- `105bb9c` — ParticipanteSecurityService
- `cdb41c5` — Testes de integracao do fluxo

---

## 2026-03-09 — Spec 004: Configuracoes, Siglas e Formulas (17 tasks)

### Fase 1 — Siglas
- `d965053` — Campo sigla em BonusConfig e VantagemConfig (P1-T1)
- `4f0ace5` — Metodos de repositorio para validacao cross-entity (P1-T2)
- `f0b7800` — SiglaValidationService (P1-T3)
- `e9b9e1e` — Integracao nos services de configuracao (P1-T4)
- `da88377` — SiglaController (P1-T5)

### Fase 2 — Formulas
- `be70464` — validarFormula() no FormulaEvaluatorService (P2-T1)
- `698e85b` — Validacao de formulas nos services (P2-T2)
- `5f16c2f` — FormulaPreviewService e FormulaController (P2-T3)

### Fase 3 — CategoriaVantagem e PontosVantagem
- `d9b0a64` — CRUD de CategoriaVantagem (P3-T1)
- `2e52d13` — CRUD de PontosVantagemConfig (P3-T2)
- `a0db0c6` — FK VantagemConfig -> CategoriaVantagem (P3-T3)

### Fase 4 — Pre-requisitos
- `1008320` — VantagemPreRequisito entity (P4-T1)
- `1e794c2` — Deteccao de ciclos DFS (P4-T2)
- `17515fd` — Endpoints dedicados de pre-requisitos (P4-T3)

### Fase 5-6 — Classe e Raca sub-recursos
- `fd6ad6b` + `8711d6c` — ClasseBonus, ClasseAptidaoBonus (P5-T1, P5-T2)
- `48fa366` — RacaClassePermitida + bonus atributo (P6-T1)

---

## 2026-03-08 — Documentacao e Glossario

- `a095c02` — Glossario Klayrah RPG expandido (5 partes)
- `110a474` — Guidelines backend atualizados
- `d379925` — Validacoes de unicidade nos services
- `3e791b5` — CLAUDE.md atualizado
- `4a50c5a` — Renomeacao campo ordemExibicao

---

## 2026-02-05-06 — Spec 003: Refactor + Specs 001: Data Model Inicial

- Implementacao de BaseEntity com soft delete
- ConfiguracaoEntity interface
- AbstractConfiguracaoService + BaseConfiguracaoServiceIntegrationTest
- 13 CRUDs de configuracao completos
- GameConfigInitializerService (template Klayrah)
- OAuth2 Google + sessao
- Testes de integracao para todos os services
- Infraestrutura: Docker, Swagger, RateLimit, GlobalExceptionHandler

---

## Antes de 2026-02 — Setup Inicial

- Projeto Spring Boot 4 / Java 25 criado
- Estrutura base de pacotes definida
- PostgreSQL + H2 configurados
- Primeiras entidades e repositorios

---

*Este documento e atualizado a cada sessao de desenvolvimento.*
