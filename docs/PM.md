> Para navegacao rapida e indice completo, ver `docs/MASTER.md`. Este arquivo contem detalhes de status e tracking.

# PM.md — Klayrah RPG: Ficha Controlador

> Fonte de status detalhado do projeto.
> Gerado em: 2026-04-01 | Atualizado: 2026-04-05 [15:30] (sessao 12, rodada 8 parcial, rev.9 — 25/35 Sprint 2, 581B+459F testes) | Branch: `main`
> Indice mestre: `docs/MASTER.md` | Cronologia: `docs/CRONOLOGIA.md`

---

## Status Geral

| Area | Progresso | Observacao |
|------|-----------|------------|
| Backend: Infraestrutura | 95% | DDL producao pendente (SP1-T27) |
| Backend: Configuracoes (13 CRUDs) | 100% | Todos com testes de integracao |
| Backend: Motor de Formulas | **95%** | FormulaEvaluatorService existe; 6 bugs CORRIGIDOS (T0); modelo adaptado (T1); 7/8 TipoEfeito implementados (T2/T3+T4+T5/T7 CONCLUIDOS); **T8 CONCLUIDO (20 testes integracao)**; FORMULA_CUSTOMIZADA bloqueado (PA-004). **Frontend T9 CONCLUIDO (R7, 31 testes)**. T10-T12 DESBLOQUEADOS |
| Backend: Ficha de Personagem (Spec 006/007) | **98%** | FichaCalculationService, FichaVantagemService, GET atributos/aptidoes — TUDO com testes. S006-T1/T2/T4/T5 CONCLUIDOS. **Wizard passo 1 (T6 R7)**, **Wizard passo 2 descricao (T7 R8, 21 testes)** CONCLUIDOS. **Participantes API/Service frontend (S005-P2T1 R8)** CONCLUIDO. Falta: wizard passos 3-6 (T8-T13), S005-P2T2/T3 |
| Backend: NPC + Duplicacao (Spec 009) | **100%** | POST /jogos/{id}/npcs + POST /fichas/{id}/duplicar implementados. 457 testes |
| Backend: Anotacoes | 100% | FichaAnotacaoController + Service implementados e testados |
| Backend: Seguranca (role checks) | **100%** | TODOS controllers com @PreAuthorize. ~~BUG XP~~ RESOLVIDO (rodada 2 — PUT /fichas/{id}/xp ja tinha @PreAuthorize) |
| Backend: Perfil Usuario | **100%** | GET/PUT /api/v1/usuarios/me implementado e testado |
| Backend: Participantes (Spec 005) | **100%** | **P1T1/P1T2/P1T3 CONCLUIDOS**. Strategy Reactivate, banir/desbanir/remover/meu-status/filtro, 29 testes totais. Falta: frontend (P2T1-P2T3) |
| Frontend: Modelos e Servicos de API | 75% | Alinhados com backend; sub-recursos (008) pendentes |
| Frontend: Componentes | **75%** | 26+ completos; FichaDetail com dados reais; NPC screen; **EfeitoFormComponent (R7, 31 testes)**; **FichaWizardComponent passo 1 (R7, 34 testes)** |
| Frontend: Testes | **100%** | **424 passando**, 0 falhas |

**Completude geral estimada: ~86%** (era ~83% na rodada 6)
**Backend: 581 testes, 0 falhas** (+10 desde rodada 6 — S015-T4 auto-concessao vantagens + S005-P1T3 testes participantes)
**Frontend: 424 testes passando, 0 falhas** (+65 desde rodada 6 — S007-T9 efeitos UI + S006-T6 wizard passo 1)
**Total tasks MVP: 96** (45 backend + 47 frontend + 4 outros)

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
**Progresso:** 23/35 concluidas (66%) — S007-T0/T1/T2/T3+T4+T5/T7/T8/T9, S015-T1/T2/T3/T4/T5, S006-T1/T2/T4/T5/T6, S005-P1T1/P1T2/P1T3, URG-01, URG-02, QW-Bug3

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
| P0-ABS | 007 | 3F | VantagemEfeito: T10-T12 frontend | DESBLOQUEADO (T9 concluida) |
| P0 | 006 | 7F | Wizard: T7-T13 frontend | DESBLOQUEADO (T6 concluida) |
| P0 | 005 | 3F | P2T1-T3 frontend | DESBLOQUEADO (P1T3 concluida) |

**Criterio de sucesso (ATUALIZADO):**
- ~~Bug XP corrigido antes de qualquer deploy~~ FEITO
- ~~6 bugs do motor corrigidos (T0) com 7 cenarios de teste~~ FEITO
- ~~Modelo adaptado para efeitos (T1)~~ FEITO
- ~~34 testes frontend corrigidos~~ FEITO (38 corrigidos, 359/359)
- ~~DefaultProvider bugs corrigidos (S015-T5)~~ FEITO
- ~~7/8 tipos de VantagemEfeito integrados no FichaCalculationService~~ FEITO (T2/T3+T4+T5/T7)
- ~~Insolitus modelado com TipoVantagem + endpoint concessao~~ FEITO (S007-T7)
- ~~ConfigPontos CRUD completo (4 entidades, 14 endpoints)~~ FEITO (S015-T1/T2/T3)
- ~~pontosDisponiveis no FichaResumoResponse~~ FEITO (S006-T5)
- ~~RacaClassePermitida validada na criacao~~ FEITO (S006-T2)
- ~~2 bugs frontend restantes corrigidos (QW-Bug1, QW-Bug2)~~ FEITO (rodada 3)
- T8: testes de integracao extensivos para todos os efeitos
- Wizard de ficha funcional de 6 passos com auto-save
- Participantes com maquina de estados completa
- 0 regressoes nos 523 testes backend existentes

---

## O que esta FEITO (por spec)

- **Spec 001** — 13 CRUDs de configuracao + Template Klayrah (GameConfigInitializerService)
- **Spec 003** — Refactor: DTOs records, validacoes, exceptions, mappers, testes base
- **Spec 004** — SiglaValidationService, CategoriaVantagem, PontosVantagem, VantagemPreRequisito, ClasseBonus, RacaClassePermitida, VantagemEfeito (8 tipos entity)
- **Spec 005 (backend 100%)** — JogoParticipante: P1T1 (strategy Reactivate), P1T2 (5 endpoints), P1T3 (29 testes). Faltam: P2T1-P2T3 frontend
- **Spec 006/007 (backend 100%, frontend em andamento)** — FichaService, FichaCalculationService, FichaVantagemService, FichaPreviewService, FichaResumoService, GET atributos/aptidoes, vida/prospeccao. T0/T1/T2/T3+T4+T5/T7/T8 CONCLUIDOS (7/8 efeitos + 20 testes integracao). S006-T1/T2/T4/T5 CONCLUIDOS. **S007-T9 CONCLUIDO (R7, efeitos UI, 31 testes)**. **S006-T6 CONCLUIDO (R7, wizard passo 1, 34 testes)**. FALTAM: T5alt (FORMULA_CUSTOMIZADA bloqueado PA-004), wizard frontend (006 T7-T13), efeitos frontend (007 T10-T12)
- **Spec 015 (backend 100%)** — T1/T2/T3/T4/T5 CONCLUIDOS (5/5 backend): 4 entidades ConfigPontos, 14 CRUD endpoints, pontos integrados no FichaResumoResponse, DefaultProvider 8 bugs corrigidos, **auto-concessao vantagens pre-definidas (T4 R7, 8 testes)**. FALTAM: T6/T7 (frontend)
- **Spec 008-old** — DashboardController, duplicacao de jogo, export/import de config, resumo de ficha, filtros, reordenacao batch (tudo implementado)
- **Spec 009** — NPC security, POST /jogos/{id}/npcs, POST /fichas/{id}/duplicar, anotacoes, updates diretos, categoriaNome. 100% backend
- **Frontend Sprint 1+2** — Design system RPG, BaseConfigTable, 13 paginas de config, FichaDetail (5 abas), JogosDisponiveis, NPC screen, OAuth2, Quick Wins (badges, tooltips, formula editor). QW-Bug1/2/3 TODOS corrigidos. **EfeitoFormComponent (R7, efeitos UI, 31 testes)**. **FichaWizardComponent passo 1 (R7, retomada rascunho, classesFiltradas, 34 testes)**. **424 testes passando, 0 falhas**

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
| Sprint 2: 12 tasks restantes | Maioria e frontend (wizard, efeitos, participantes) | Backend 100% exceto T5alt (PA-004); modelo 1 task/agente; 4 agentes paralelos por rodada |
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

*Atualizado: 2026-04-05 [14:54] (rev.8 — pos-rodada 7: 23/35, 581B+424F, 66% Sprint 2) | PM/Scrum Master*
