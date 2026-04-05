> Para navegacao rapida e indice completo, ver `docs/MASTER.md`. Este arquivo contem detalhes de status e tracking.

# PM.md — Klayrah RPG: Ficha Controlador

> Fonte de status detalhado do projeto.
> Gerado em: 2026-04-01 | Atualizado: 2026-04-04 (sessao 10, rodada 2, rev.5 — consolidacao final) | Branch: `feature/009-npc-fichas-mestre`
> Indice mestre: `docs/MASTER.md` | Cronologia: `docs/CRONOLOGIA.md`

---

## Status Geral

| Area | Progresso | Observacao |
|------|-----------|------------|
| Backend: Infraestrutura | 95% | DDL producao pendente (SP1-T27) |
| Backend: Configuracoes (13 CRUDs) | 100% | Todos com testes de integracao |
| Backend: Motor de Formulas | **80%** | FormulaEvaluatorService existe; 6 bugs CORRIGIDOS (T0); modelo adaptado para efeitos (T1 CONCLUIDO, 474 testes); VantagemEfeito NAO integrado no calculo (GAP-03, Spec 007 T2-T8 pendente) |
| Backend: Ficha de Personagem (Spec 006/007) | **95%** | FichaCalculationService, FichaVantagemService, GET atributos/aptidoes — TUDO com testes. Falta: VantagemEfeito no motor (Spec 007 T2-T8), wizard backend (Spec 006 T1-T5) |
| Backend: NPC + Duplicacao (Spec 009) | **100%** | POST /jogos/{id}/npcs + POST /fichas/{id}/duplicar implementados. 457 testes |
| Backend: Anotacoes | 100% | FichaAnotacaoController + Service implementados e testados |
| Backend: Seguranca (role checks) | **100%** | TODOS controllers com @PreAuthorize. ~~BUG XP~~ RESOLVIDO (rodada 2 — PUT /fichas/{id}/xp ja tinha @PreAuthorize) |
| Backend: Perfil Usuario | **100%** | GET/PUT /api/v1/usuarios/me implementado e testado |
| Backend: Participantes (Spec 005) | 80% | Entity e fluxo basico existem; faltam endpoints (banir, desbanir, meu-status, cancelar) |
| Frontend: Modelos e Servicos de API | 75% | Alinhados com backend; sub-recursos (008) pendentes |
| Frontend: Componentes | 70% | 26+ completos; FichaDetail com dados reais; NPC screen implementada |
| Frontend: Testes | **100%** | **359 passando**, 0 falhas (corrigidos 38 testes na rodada 2) |

**Completude geral estimada: ~80%** (era ~77% na rodada 1)
**Backend: 474 testes, 0 falhas** (+10 na rodada 2 — Spec 007 T1 + Spec 015 T5)
**Frontend: 359 testes passando, 0 falhas** (era ~271/~34 falhando)
**Total tasks MVP: 77** (34 backend + 42 frontend + 1 teste)

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
| Tasks movidas para backlog | 2 (SP1-T13 barras HP, SP1-T27 DDL) |

**Entregas principais:**
- FichaDetailComponent com 5 abas funcionais (atributos, aptidoes, vantagens, anotacoes, resumo)
- JogosDisponiveisComponent
- GET /fichas/{id}/atributos e /aptidoes com testes
- Security NPC: acesso restrito ao Mestre
- Perfil usuario: GET/PUT /usuarios/me
- Build Angular: 0 erros, 0 warnings
- 12 correcoes de build PrimeNG 21

### Sprint 2 — "Motor Correto + Ficha Funcional" — EM ANDAMENTO

**Periodo estimado:** proxima sessao a +3-4 semanas
**Tasks:** 35 total (19 backend + 14 frontend + 2 urgencias)
**Progresso:** 6/35 concluidas (S007-T0, S007-T1, S015-T5, URG-01, URG-02, QW-Bug3)

| Prio | Spec | Tasks | Descricao | Status |
|------|------|-------|-----------|--------|
| ~~URGENTE~~ | 006 T3 | 1B | Fix bug XP | **CONCLUIDO** (rodada 2) |
| ~~URGENTE~~ | — | 1F | Fix 38 testes frontend falhando | **CONCLUIDO** (rodada 2 — 359/359) |
| ~~QW~~ | 009-ext T-QW | 1/3 bugs | QW-Bug3 (rota NPC) | **CONCLUIDO** (rodada 2) |
| QW | 009-ext T-QW | 2 bugs | QW-Bug1 (barras) + QW-Bug2 (pontos) | PENDENTE |
| ~~P0-ABS~~ | 007 T0 | 1B | Corrigir 6 bugs motor | **CONCLUIDO** (rodada 1) |
| ~~P0-ABS~~ | 007 T1 | 1B | Adaptar modelo dados para efeitos | **CONCLUIDO** (rodada 2) |
| ~~P0~~ | 015 T5 | 1B | DefaultProvider fixes (8 bugs) | **CONCLUIDO** (rodada 2) |
| P0-ABS | 007 | 10 (6B + 4F) | VantagemEfeito: T2-T8 backend + T9-T12 frontend | PENDENTE |
| P0 | 015 | 4 (4B) | ConfigPontos: T1-T4 entidades + CRUD | PENDENTE |
| P0 | 006 | 13 (5B + 8F) | Wizard de criacao de ficha 6 passos | PENDENTE |
| P0 | 005 | 6 (3B + 3F) | Gestao de participantes | PENDENTE |

**Criterio de sucesso (ATUALIZADO):**
- ~~Bug XP corrigido antes de qualquer deploy~~ FEITO
- ~~6 bugs do motor corrigidos (T0) com 7 cenarios de teste~~ FEITO
- ~~Modelo adaptado para efeitos (T1)~~ FEITO
- ~~34 testes frontend corrigidos~~ FEITO (38 corrigidos, 359/359)
- ~~DefaultProvider bugs corrigidos (S015-T5)~~ FEITO
- 8 tipos de VantagemEfeito integrados no FichaCalculationService com testes
- Wizard de ficha funcional de 6 passos com auto-save
- Participantes com maquina de estados completa
- 0 regressoes nos 474 testes backend existentes
- 2 bugs frontend restantes corrigidos (QW-Bug1, QW-Bug2)

---

## O que esta FEITO (por spec)

- **Spec 001** — 13 CRUDs de configuracao + Template Klayrah (GameConfigInitializerService)
- **Spec 003** — Refactor: DTOs records, validacoes, exceptions, mappers, testes base
- **Spec 004** — SiglaValidationService, CategoriaVantagem, PontosVantagem, VantagemPreRequisito, ClasseBonus, RacaClassePermitida, VantagemEfeito (8 tipos entity)
- **Spec 005 (parcial)** — JogoParticipante com fluxo basico de aprovacao (faltam endpoints)
- **Spec 006/007 (parcial)** — FichaService, FichaCalculationService, FichaVantagemService, FichaPreviewService, FichaResumoService, GET atributos/aptidoes, vida/prospeccao endpoints. T0 (6 bugs) e T1 (modelo dados) CONCLUIDOS. FALTAM: T2-T8 (efeitos no motor), wizard backend (006 T1-T5)
- **Spec 015 (parcial)** — T5 DefaultProvider CONCLUIDO: 8 bugs corrigidos, 22 vantagens canonicas, 9 BonusConfig, 8 PontosVantagem, 8 CategoriaVantagem defaults. FALTAM: T1-T4 (entidades + CRUD + integracao)
- **Spec 008-old** — DashboardController, duplicacao de jogo, export/import de config, resumo de ficha, filtros, reordenacao batch (tudo implementado)
- **Spec 009** — NPC security, POST /jogos/{id}/npcs, POST /fichas/{id}/duplicar, anotacoes, updates diretos, categoriaNome. 100% backend
- **Frontend Sprint 1+2** — Design system RPG, BaseConfigTable, 13 paginas de config, FichaDetail (5 abas), JogosDisponiveis, NPC screen, OAuth2, Quick Wins (badges, tooltips, formula editor). QW-Bug3 (rota NPC) corrigido. **359 testes passando, 0 falhas** (38 testes corrigidos na rodada 2)

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

## Backlog Priorizado (pos-Sprint 2)

### P1 — Sprint 3

| Spec | Tasks | Descricao |
|------|-------|-----------|
| 008 | 4 (0B + 4F) | Sub-recursos Classes/Racas frontend |
| 012 | 12 ativas (1B + 11F) | Niveis, Progressao, Level Up frontend (T12/T13 fora MVP) |
| 009-ext | 10 (6B + 4F) | NPC Visibility + Prospeccao + Essencia + Reset (excluindo T-QW, feita no Sprint 2) |

### P1 — Sprint 4 (IMPLEMENTAR POR ULTIMO)

| Spec | Tasks | Descricao |
|------|-------|-----------|
| 010 | 9 (5B + 3F + 1T) | Roles ADMIN/MESTRE/JOGADOR refactor — impacto transversal ~50+ @PreAuthorize |

### P2 — Sprint 5+

| Spec | Tasks | Descricao |
|------|-------|-----------|
| 011 | 8 (4B + 4F) | Galeria de Imagens e Anotacoes |

### Pos-MVP

| Item | Descricao |
|------|-----------|
| GAP-PONTOS-CONFIG | Classe/Raca dando pontos extras por nivel (decisao PO Q16) |
| Renascimento | Mecanica completa de renascimento (nivel 31+) |
| Modo Sessao formal | SSE/WebSocket em vez de Polling 30s |
| XP em lote | Conceder XP para toda a mesa de uma vez |

### Tech Debt (backlog permanente)

| ID | Descricao | Prio |
|----|-----------|------|
| SP1-T13 | Barras HP por membro do corpo (VidaSection) | Baixa |
| SP1-T27 | DDL producao (3 ALTER TABLE) | Pre-deploy |
| C1 | handleReorder wiring 13o componente | Baixa |
| INCONS-01 | API-CONTRACT.md desatualizado | Media |
| DT-FE-01 | atualizarAnotacao() fantasma | Baixa |
| DT-FE-02 | CategoriaVantagem URL sem /v1/ | Baixa |
| DT-FE-03 | ConfigStore type assertions any | Baixa |

---

## Riscos em Aberto

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| ~~GAP-02 vuln XP~~ | ~~Jogador altera propria XP~~ | **RESOLVIDO** (rodada 2) — PUT /fichas/{id}/xp ja tinha @PreAuthorize |
| ~~6 bugs motor (GAP-CALC-01..08)~~ | ~~Calculos incorretos~~ | **RESOLVIDO** — T0 concluida (rodada 1), 464 testes |
| ~~S007-T1 adaptar modelo~~ | ~~Bloqueava T2-T7~~ | **RESOLVIDO** (rodada 2) — 474 testes |
| ~~34 testes frontend falhando~~ | ~~CI nao confiavel~~ | **RESOLVIDO** (rodada 2) — 359/359 passando |
| Spec 007 impacta ~20-30 arquivos | Regressao nos calculos | Spec 007 T8 com testes extensivos |
| PA-004 nao resolvido | FORMULA_CUSTOMIZADA sem alvo | Escalar ao PO antes de T6 |
| PA-006 nao resolvido | VIG/SAB hardcoded (GAP-CALC-09) | Fora do escopo de T0; escalar ao PO |
| Sprint 2: 29 tasks restantes | Atraso se efeitos demorarem | T2-T7 desbloqueados (T2/T3 sequenciais), modelo 1 task/agente, 4 agentes rodada 3 |
| Spec 010 transversal | ~50+ @PreAuthorize a revisar | Implementar por ULTIMO, branch dedicada |

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

*Atualizado: 2026-04-04 (rev.5 — consolidacao final pos-rodada 2: 6/35, 474B+359F, 4 agentes rodada 3) | PM/Scrum Master*
