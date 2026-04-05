# ficha-controlador — MASTER INDEX

> Fonte unica de verdade para navegacao e estado do projeto.
> Ultima atualizacao: 2026-04-05 (rev.9 — pos-rodada 5: 15/35 Sprint 2, 523B+359F testes) | Branch: `main`

---

## Estado do Projeto

| Metrica | Valor |
|---------|-------|
| Backend testes | **523 passando**, 0 falhas |
| Frontend testes | **359 passando**, 0 falhas |
| Frontend build | 0 erros, 0 warnings |
| Sprint 1 | **CONCLUIDO** (94%, 29/31 tasks) |
| Sprint 2 | **EM ANDAMENTO** — 35 tasks (**15/35 concluidas** (43%): S007-T0/T1/T2/T3+T4+T5/T7, S015-T1/T2/T3/T5, S006-T1/T2/T5, URG-01/02, QW-Bug3) |
| Specs com spec+plan+tasks | 005, 006, 007, 008, 009-ext, 010, 011, 012, 013, 014, **015** |
| Specs em especificacao | Nenhuma — **016** (Sistema de Itens) ja 100% especificada |
| Decisoes PO | **TODAS RESOLVIDAS** (GAP-01 a GAP-08, INCONS-02, P-03, PA-001/002, Q14-Q17) |
| MVP objetivo | Todas as specs 005-012 + 015 + **016** implementadas e testadas |
| Total tasks MVP | **~96** (45 backend + 47 frontend + 4 outros) — Spec 016 confirmada: 11 tasks |
| Total tasks pos-MVP (qualidade) | **12** (Spec 013: 6 + Spec 014: 6) |

---

## Quadro Completo de Specs — Repriorizado pelo PO (2026-04-03)

| Spec | Titulo | Diretorio | Prio PO | Tasks (B/F/Total) | Status |
|------|--------|-----------|---------|-------------------|--------|
| 001 | Data model inicial, 13 CRUDs | — | — | — | CONCLUIDO |
| 003 | Refactor: DTOs, validacoes, exceptions | — | — | — | CONCLUIDO |
| 004 | Siglas, formulas, relacionamentos | `specs/004-*/` | — | 17 | CONCLUIDO |
| **015** | **ConfigPontos Classe/Raca + DefaultProvider** | [`specs/015-config-pontos-classe-raca/`](specs/015-config-pontos-classe-raca/) | **P0** | 5B / 2F / **7** | **T1/T2/T3/T5 CONCLUIDOS** (5/7). T4 PENDENTE (depende S007-T6), T6/T7 frontend PENDENTES |
| **007** | **VantagemEfeito + Motor de Calculos** | [`specs/007-vantagem-efeito/`](specs/007-vantagem-efeito/) | **P0-ABSOLUTA** | 9B / 4F / **13** | **T0/T1/T2/T3+T4+T5/T7 CONCLUIDOS** (7/8 efeitos, 523 testes). T8 PENDENTE, T5alt BLOQUEADO (PA-004) |
| **006** | **Wizard de Criacao de Ficha** | [`specs/006-ficha-wizard/`](specs/006-ficha-wizard/) | **P0** | 5B / 8F / **13** | **T1/T2/T5 CONCLUIDOS** (3/13). T3-T4 backend PENDENTES, T6-T13 frontend PENDENTES |
| **005** | **Gestao de Participantes** | [`specs/005-participantes/`](specs/005-participantes/) | **P0** | 3B / 3F / **6** | spec+plan+tasks PRONTOS, implementacao PENDENTE |
| **008** | **Sub-recursos Classes/Racas (frontend)** | [`specs/008-sub-recursos-classes-racas/`](specs/008-sub-recursos-classes-racas/) | **P1** | 0B / 4F / **4** | spec+plan+tasks PRONTOS, implementacao PENDENTE |
| **009-ext** | **NPC Visibility + Prospeccao + Essencia** | [`specs/009-npc-visibility/`](specs/009-npc-visibility/) | **P1** | 6B / 4F+QW / **11** | spec+plan+tasks PRONTOS, T-QW adicionada, implementacao PENDENTE |
| **010** | **Roles ADMIN/MESTRE/JOGADOR refactor** | [`specs/010-roles-refactor/`](specs/010-roles-refactor/) | **P1** | 5B / 3F+1T / **9** | spec+plan+tasks PRONTOS, implementacao PENDENTE |
| **012** | **Niveis e Progressao (frontend)** | [`specs/012-niveis-progressao-frontend/`](specs/012-niveis-progressao-frontend/) | **P1** | 1B / 11F / **12** | spec+plan+tasks PRONTOS (T12/T13 fora MVP), implementacao PENDENTE |
| **016** | **Sistema de Itens/Equipamentos** | [`specs/016-sistema-itens/`](specs/016-sistema-itens/) | **P1** | 7B / 4F / **11** | spec+plan+tasks+dataset **100% PRONTOS**, implementacao PENDENTE |
| **011** | **Galeria e Anotacoes** | [`specs/011-galeria-anotacoes/`](specs/011-galeria-anotacoes/) | **P2** | 4B / 4F / **8** | spec+plan+tasks PRONTOS, implementacao PENDENTE |
| **013** | **Documentacao Tecnica** | [`specs/013-documentacao-tecnica/`](specs/013-documentacao-tecnica/) | **P3** | 3B / 2F / 1S / **6** | spec+plan+tasks PRONTOS, executar APOS specs funcionais |
| **014** | **Cobertura de Testes** | [`specs/014-cobertura-testes/`](specs/014-cobertura-testes/) | **P3** | 4B / 2F / **6** | spec+plan+tasks PRONTOS, executar APOS specs funcionais |
| 008-old | Utilidade e Fluidez (dashboard, export/import) | — | — | — | Backend ~100% implementado (pre-existente) |
| 009-old | NPC backend (fichas mestre, duplicacao) | — | — | — | Backend ~100% (457 testes, pre-existente) |

> **Nota de nomenclatura:** Os antigos Spec 008 (Utilidade/Fluidez) e Spec 009 (NPC backend) referem-se a trabalho ja concluido em branches anteriores. Os novos `008-sub-recursos-classes-racas`, `009-npc-visibility`, etc. sao specs novas com escopo distinto.

> **Nota sobre contagem:** Spec 007 passou de 12 para **13 tasks** com a adicao de T0 (correcao de 6 bugs pre-existentes no motor de calculos). Spec 009-ext passou de 10 para **11 tasks** com a adicao de T-QW (3 bugs frontend criticos).

---

## Sequencia de Implementacao (decisao do PO)

A sequencia obrigatoria respeita dependencias tecnicas e decisoes do PO:

```
PRIORIDADE ABSOLUTA
  1. Spec 007 (VantagemEfeito + Motor)    — T0 corrige bugs ANTES; configs devem estar 100% corretas
     13 tasks: 9 backend + 4 frontend

P0 — FUNDACAO DE CONFIGURACAO
  1.5. Spec 015 (ConfigPontos + DefaultProvider)  — pontos por classe/raca + defaults corretos
       7 tasks: 5 backend + 2 frontend
       T5 (DefaultProvider) paralelizavel com Spec 007

P0 — FICHA FUNCIONAL
  2. Spec 006 (Wizard de Criacao)          — ficha funcional de verdade (precisa de Spec 015 T3)
     13 tasks: 5 backend + 8 frontend
  3. Spec 005 (Participantes)              — desbloqueia fluxo completo de aprovacao
     6 tasks: 3 backend + 3 frontend

P1 — GESTAO E PROGRESSAO
  4. Spec 008 (Sub-recursos Classes/Racas) — config essencial do Mestre no frontend
     4 tasks: 0 backend + 4 frontend
  5. Spec 009-ext (NPC Visibility)         — essencia, prospeccao, visibilidade granular
     11 tasks: 6 backend + 4 frontend + 1 quick-win
  6. Spec 010 (Roles ADMIN)                — onboarding e admin
     9 tasks: 5 backend + 3 frontend + 1 teste
  7. Spec 012 (Niveis/Progressao frontend) — level up e PontosVantagem
     12 tasks: 1 backend + 11 frontend

P1 — ITENS E EQUIPAMENTOS
  7.5. Spec 016 (Sistema de Itens)         — catalogo, inventario, calculos de equipamento
       ~21 tasks: ~15 backend + ~4 frontend + ~2 dataset
       SD-1 (Configuracao) paralelizavel com Sprint 2
       SD-2 (Ficha/Inventario) depende de Spec 007 + SD-1

P2 — ENRIQUECIMENTO
  8. Spec 011 (Galeria/Anotacoes)          — enriquecimento da ficha
     8 tasks: 4 backend + 4 frontend

P3 — DOCUMENTACAO E QUALIDADE (apos todas as specs funcionais)
  9. Spec 013 (Documentacao Tecnica)       — Javadoc, OpenAPI, TSDoc, swagger.json
     6 tasks: 3 backend + 2 frontend + 1 shared
 10. Spec 014 (Cobertura de Testes)        — JaCoCo, Vitest coverage, testes faltantes
     6 tasks: 4 backend + 2 frontend
```

**Caminho critico:** `~~007-T0~~ -> ~~007-T1~~ -> ~~007-T2~~ -> ~~T3+T4+T5~~ -> ~~T7~~ -> T8 -> 006-frontend -> 005` (7/8 efeitos concluidos; T8 PENDENTE, T5alt BLOQUEADO PA-004)
**Paralelo possivel:** Track B (015-T1, 006/005 backend), Track C (frontend: QWs, 008, 012-config)
**ULTIMO:** Spec 010 (Roles) deve ser implementada por ultimo — impacto transversal em ~50+ @PreAuthorize

---

## Dependencias entre Specs (atualizado)

```
004 CONCLUIDO
 |
 v
007 (VantagemEfeito + Motor) — T0 bloqueia T1..T12
 |
 +----> 015 (ConfigPontos + DefaultProvider) — T5 paralelizavel com 007
 |       |
 |       +----> 015-T3 (pontos disponiveis) ──> 006-T5/T8
 |       +----> 015-T4 (auto-vantagens) ──> 006 wizard nivel 1
 |
 +----> 016-SD1 (Itens Configuracao) — pode iniciar em paralelo com 007
 |       |
 |       +----> 016-SD3 (Dataset/Defaults) — paralelizavel com SD-1
 |       +----> 016-SD2 (Ficha/Inventario) — depende de SD-1 + Spec 007
 |               |
 |               +----> 006 (Wizard) usa ClasseEquipamentoInicial
 |
 v
006 (Wizard Ficha) ──────> 005 (Participantes)
 |                          |
 +----> 008 (Sub-recursos)  +----> 009-ext (NPC Visibility)
 +----> 009-ext             +----> 010 (Roles) — IMPLEMENTAR POR ULTIMO
 +----> 011 (Galeria)       +----> 012 (Niveis/Progressao)
 +----> 012 (Niveis)

APOS TODAS AS SPECS FUNCIONAIS:
005-012+015+016 completas ──> 013 (Documentacao Tecnica)
                          ──> 014 (Cobertura de Testes)
```

**Dependencias especificas entre GAPs do Tech Lead:**
- GAP-CALC-01/02/03/06/07/08 → corrigidos na Spec 007 T0 (ANTES de VantagemEfeito)
- GAP-09 (Insolitus flag) → desbloqueie GAP-03 (VantagemEfeito no motor) — ambos na Spec 007
- GAP-02 (bug XP) → URGENTE, independente de qualquer spec, corrigir imediatamente
- GAP-10 (Roles ADMIN) → implementar POR ULTIMO (impacto transversal)

---

## Decisoes Pendentes do PO

| ID | Decisao | Spec | Criticidade | Status |
|----|---------|------|-------------|--------|
| **P-03** | ADMIN faz bypass total de `canAccessJogo()`? | Spec 010 | Alta | RESOLVIDO: ADMIN gerencia apenas usuarios no MVP. SEM bypass de canAccessJogo. |
| PA-001 (007) | FichaInsolitus pode ser removida pelo Mestre? | Spec 007 | Media | RESOLVIDO: MESTRE pode revogar QUALQUER vantagem (incluindo Insolitus). JOGADOR nunca remove. |
| PA-002 (007) | Enum TipoVantagem vs boolean isInsolitus? | Spec 007 | Media | RESOLVIDO: Enum TipoVantagem (VANTAGEM \| INSOLITUS). |
| PA-004 (007) | FORMULA_CUSTOMIZADA sem alvo definido: onde aplica o resultado? | Spec 007 | Alta — afeta T6 | Pendente |
| PA-006 (007) | VIG/SAB hardcoded por abreviacao (GAP-CALC-09) | Spec 007 | Media | Pendente — fora do escopo de T0 |
| INCONS-02 | DELETE /fichas — fichas sao deletaveis? | Spec 006 | **CRITICO** | RESOLVIDO: Fichas NUNCA sao deletadas. Status morta/abandonada. Backend retorna 405. |
| GAP-02 | Bug XP: jogador altera propria XP via PUT /fichas/{id} | Spec 006 (T3) | **URGENTE** | **CORRIGIR IMEDIATAMENTE** |
| Q14 | Modo Sessao no MVP? | Geral | Media | RESOLVIDO: Polling 30s. SSE/WebSocket pos-MVP. |
| Q15 | Essencia: quantos endpoints? | Spec 009-ext | Media | RESOLVIDO: Dois — gastar (JOGADOR) + resetar (MESTRE). |
| Q16 | GAP-PONTOS-CONFIG: Classe/Raca pontos extras por nivel? | Spec 012 | Baixa | RESOLVIDO: Pos-MVP. |
| Q17 | Como calcular pontosAptidaoGastos? | Spec 006/012 | Media | RESOLVIDO: SUM(FichaAptidao.base). |

---

## Gaps Criticos em Aberto

> Dossie completo: [`docs/gaps/BA-GAPS-2026-04-02.md`](gaps/BA-GAPS-2026-04-02.md)
> Review tecnico: [`docs/specs/TECH-LEAD-BACKEND-REVIEW.md`](specs/TECH-LEAD-BACKEND-REVIEW.md)

| Gap | Problema | Criticidade | Spec que resolve | Status |
|-----|---------|-------------|------------------|--------|
| GAP-01 | Wizard criacao envia apenas {nome} | A-Bloqueador | Spec 006 (T6-T11) | Pendente |
| GAP-02 | XP editavel por JOGADOR (vuln seguranca) | A-Bloqueador | Spec 006 (T3) | **URGENTE** |
| GAP-03 | VantagemEfeito ignorado pelo motor | A-Bloqueador | Spec 007 (T1-T8) | Pendente |
| GAP-04 | Participantes sem maquina de estados | A-Bloqueador | Spec 005 (P1-T1) | Pendente |
| GAP-05 | NPC visibilidade apenas binaria | B-Alta | Spec 009-ext (T1-T2) | Pendente |
| GAP-06 | Pontos disponiveis ausentes no response | B-Alta | Spec 006 (T5) | **CONCLUIDO** (rodada 5 — S006-T5) |
| GAP-07 | essenciaAtual sem endpoint dedicado | B-Alta | Spec 009-ext (T5) | Pendente |
| GAP-08 | Prospeccao sem endpoints semanticos | B-Alta | Spec 009-ext (T3) | Pendente |
| GAP-09 | Insolitus sem modelagem | B-Alta | Spec 007 (T7) | **CONCLUIDO** (rodada 5 — S007-T7) |
| GAP-10 | Roles sem ADMIN, inconsistencia global/jogo | B-Alta | Spec 010 (T1-T5) | Pendente |
| ~~GAP-CALC-01..08~~ | ~~6 bugs no motor de calculos~~ | ~~A-Bloqueador~~ | Spec 007 (T0) | **CONCLUIDO** (sessao 10, 464 testes) |

---

## Sprint 1 — "Ficha Jogavel" — CONCLUIDO (94%)

**Resultado:** 29/31 tasks. 457 testes backend. FichaDetail funcional end-to-end.
**Tasks movidas para backlog:** SP1-T13 (barras HP membro), SP1-T27 (DDL producao)

## Sprint 2 — "Motor Correto + Ficha Funcional" (EM ANDAMENTO — 15/35)

**Progresso:** 15/35 concluidas (43%). S007-T0/T1/T2/T3+T4+T5/T7, S015-T1/T2/T3/T5, S006-T1/T2/T5, URG-01/02, QW-Bug3 entregues. 523 testes backend, 359 testes frontend.
**Detalhes completos:** [`SPRINT-ATUAL.md`](SPRINT-ATUAL.md)

| Prio | Descricao | Spec/Task | Tasks |
|------|-----------|-----------|-------|
| **URGENTE** | Corrigir bug XP (vulnerabilidade seguranca ativa) | Spec 006 T3 | 1 backend |
| **URGENTE** | Fix 34 testes frontend falhando | — | 1 frontend |
| **QW** | 3 bugs frontend criticos (barras, pontos, rota NPC) | Spec 009-ext T-QW | 1 frontend (3 bugs) |
| P0-ABS | Corrigir 6 bugs pre-existentes no motor de calculos | Spec 007 T0 | 1 backend |
| P0-ABS | VantagemEfeito integrado ao motor (8 tipos) | Spec 007 (T1-T8) | 8 backend |
| P0-ABS | VantagemEfeito UI (efeitos, formula editor, dado up, insolitus) | Spec 007 (T9-T12) | 4 frontend |
| P0 | Backend do wizard (status, validacao, XP, pontos) | Spec 006 (T1-T5) | 5 backend |
| P0 | Wizard criacao de ficha 6 passos | Spec 006 (T6-T13) | 8 frontend |
| P0 | Participantes backend (re-solicitar, endpoints, testes) | Spec 005 (P1-T1 a P1-T3) | 3 backend |
| P0 | Participantes frontend (API, mestre, jogador) | Spec 005 (P2-T1 a P2-T3) | 3 frontend |

**Total Sprint 2:** 35 tasks (19 backend + 14 frontend + 2 urgencias)

---

## Riscos em Aberto

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| ~~GAP-02 vuln XP~~ | ~~Jogador altera propria XP~~ | **RESOLVIDO** (rodada 2) |
| ~~6 bugs motor (GAP-CALC-01..08)~~ | ~~Calculos incorretos~~ | **RESOLVIDO** (rodada 1) |
| VantagemEfeito desconectado do motor | Calculos de vantagem incorretos em TODAS as fichas | Spec 007 P0-ABSOLUTA, ~20-30 arquivos impactados |
| FichaForm envia apenas {nome} | Criacao de ficha quebrada | Spec 006 (T6-T11) wizard rewrite |
| Spec 010 e transversal | ~50+ @PreAuthorize a revisar, pode quebrar todo o auth | Implementar POR ULTIMO, branch dedicada, testes extensivos |
| DDL producao pendente | Bloqueia deploy | SP1-T27 -- backlog Sprint 3+ |
| PA-004 nao resolvido | FORMULA_CUSTOMIZADA sem alvo bloqueia T6 | Escalar ao PO antes de T6 |
| PA-006 nao resolvido | VIG/SAB hardcoded (GAP-CALC-09) | Fora do escopo T0; escalar ao PO |

---

## Documentos de Referencia

### Tracking e Status
| Documento | Descricao |
|-----------|-----------|
| [`CRONOLOGIA.md`](CRONOLOGIA.md) | Cronologia reversa completa |
| [`SPRINT-ATUAL.md`](SPRINT-ATUAL.md) | Sprint 2 tracking detalhado com tracks paralelos |
| [`PM.md`](PM.md) | Status geral do projeto |
| [`EPICS-BACKLOG.md`](EPICS-BACKLOG.md) | 9+ epicos backend com tasks detalhadas |

### Roadmap
| Documento | Descricao |
|-----------|-----------|
| [`specs/ROADMAP-MVP.md`](specs/ROADMAP-MVP.md) | Roadmap MVP com 5 fases de implementacao |
| [`specs/GLOSSARIO-GAPS.md`](specs/GLOSSARIO-GAPS.md) | Inventario de conceitos do dominio vs codigo |
| [`specs/TECH-LEAD-BACKEND-REVIEW.md`](specs/TECH-LEAD-BACKEND-REVIEW.md) | Review tecnico dos GAPs com impacto detalhado |

### Produto e Negocio
| Documento | Descricao |
|-----------|-----------|
| [`PRODUCT-BACKLOG.md`](PRODUCT-BACKLOG.md) | 93 User Stories detalhadas (11 epicos) |
| [`UX-BACKLOG.md`](UX-BACKLOG.md) | Auditoria UX completa (26 componentes) |
| [`API-CONTRACT.md`](API-CONTRACT.md) | Contrato REST completo backend-frontend |
| [`gaps/BA-GAPS-2026-04-02.md`](gaps/BA-GAPS-2026-04-02.md) | Dossie de gaps com respostas do PO |

### Specs (documentacao tecnica)
| Spec | Artefatos disponiveis |
|------|----------------------|
| 005 — Participantes | [`spec.md`](specs/005-participantes/spec.md), [`plan.md`](specs/005-participantes/plan.md), [`tasks/INDEX.md`](specs/005-participantes/tasks/INDEX.md) — 6 tasks |
| 006 — Wizard Ficha | [`spec.md`](specs/006-ficha-wizard/spec.md), [`plan.md`](specs/006-ficha-wizard/plan.md), [`tasks/INDEX.md`](specs/006-ficha-wizard/tasks/INDEX.md) — 13 tasks |
| 007 — VantagemEfeito | [`spec.md`](specs/007-vantagem-efeito/spec.md), [`plan.md`](specs/007-vantagem-efeito/plan.md), [`tasks/INDEX.md`](specs/007-vantagem-efeito/tasks/INDEX.md) — **13 tasks** (T0 nova) |
| 008 — Sub-recursos | [`spec.md`](specs/008-sub-recursos-classes-racas/spec.md), [`tasks/INDEX.md`](specs/008-sub-recursos-classes-racas/tasks/INDEX.md) — 4 tasks |
| 009-ext — NPC Visibility | [`spec.md`](specs/009-npc-visibility/spec.md), [`plan.md`](specs/009-npc-visibility/plan.md), [`tasks/INDEX.md`](specs/009-npc-visibility/tasks/INDEX.md) — **11 tasks** (T-QW nova) |
| 010 — Roles Refactor | [`spec.md`](specs/010-roles-refactor/spec.md), [`plan.md`](specs/010-roles-refactor/plan.md), [`tasks/INDEX.md`](specs/010-roles-refactor/tasks/INDEX.md) — 9 tasks |
| 011 — Galeria/Anotacoes | [`spec.md`](specs/011-galeria-anotacoes/spec.md), [`plan.md`](specs/011-galeria-anotacoes/plan.md), [`tasks/INDEX.md`](specs/011-galeria-anotacoes/tasks/INDEX.md) — 8 tasks |
| 012 — Niveis/Progressao | [`spec.md`](specs/012-niveis-progressao-frontend/spec.md), [`plan.md`](specs/012-niveis-progressao-frontend/plan.md), [`tasks/INDEX.md`](specs/012-niveis-progressao-frontend/tasks/INDEX.md) — 12 tasks ativas (T12/T13 fora MVP) |
| 013 — Documentacao Tecnica | [`spec.md`](specs/013-documentacao-tecnica/spec.md), [`plan.md`](specs/013-documentacao-tecnica/plan.md), [`tasks/INDEX.md`](specs/013-documentacao-tecnica/tasks/INDEX.md) — 6 tasks |
| 014 — Cobertura de Testes | [`spec.md`](specs/014-cobertura-testes/spec.md), [`plan.md`](specs/014-cobertura-testes/plan.md), [`tasks/INDEX.md`](specs/014-cobertura-testes/tasks/INDEX.md) — 6 tasks |
| 015 — ConfigPontos + DefaultProvider | [`spec.md`](specs/015-config-pontos-classe-raca/spec.md), [`plan.md`](specs/015-config-pontos-classe-raca/plan.md), [`tasks/INDEX.md`](specs/015-config-pontos-classe-raca/tasks/INDEX.md) — **7 tasks** |
| 016 — Sistema de Itens/Equipamentos | **100% ESPECIFICADA** — [`spec.md`](specs/016-sistema-itens/spec.md), [`tasks/INDEX.md`](specs/016-sistema-itens/tasks/INDEX.md), [`dataset/`](specs/016-sistema-itens/dataset/) — **11 tasks** |
| Tech Lead Review | [`TECH-LEAD-BACKEND-REVIEW.md`](specs/TECH-LEAD-BACKEND-REVIEW.md) |

### Analises de Dominio (BA)
| Documento | Dominio | Validade |
|-----------|---------|----------|
| [`BA-ATRIBUTOS-APTIDOES.md`](analises/BA-ATRIBUTOS-APTIDOES.md) | Atributos, Aptidoes, TipoAptidao, Bonus | Valido |
| [`BA-CLASSES-RACAS.md`](analises/BA-CLASSES-RACAS.md) | ClassePersonagem, Raca, sub-recursos | Valido |
| [`BA-CONFIGURACOES-SIMPLES.md`](analises/BA-CONFIGURACOES-SIMPLES.md) | Genero, Indole, Presenca, Prospeccao, MembroCorpo | Valido |
| [`BA-FICHA.md`](analises/BA-FICHA.md) | Ficha de Personagem | Parcialmente superado — ver GAP-01/GAP-03 |
| [`BA-NIVEIS-PROGRESSAO.md`](analises/BA-NIVEIS-PROGRESSAO.md) | Niveis, CategoriaVantagem, PontosVantagem | Valido |
| [`BA-VANTAGEM-CONFIG.md`](analises/BA-VANTAGEM-CONFIG.md) | VantagemConfig, pre-requisitos, efeitos | Valido |
| [`INTEGRACAO-CONFIG-FICHA.md`](analises/INTEGRACAO-CONFIG-FICHA.md) | Auditoria config->ficha, origem de T0 | **NOVO** |

### Designs UX
| Documento | Descricao | Status |
|-----------|-----------|--------|
| [`WIZARD-CRIACAO-FICHA.md`](design/WIZARD-CRIACAO-FICHA.md) | Wizard 5-6 passos com auto-save | Completo |
| [`NPC-VISIBILITY.md`](design/NPC-VISIBILITY.md) | Visibilidade granular de NPC por jogador | Completo |
| [`PROSPECCAO-SESSAO.md`](design/PROSPECCAO-SESSAO.md) | Uso de prospeccao em sessao (jogador usa, mestre confirma) | Completo |
| [`FICHA-DETAIL-DESIGN.md`](design/FICHA-DETAIL-DESIGN.md) | Design da tela FichaDetail (5 abas) | Completo |
| [`JOGOS-DISPONIVEIS-DESIGN.md`](design/JOGOS-DISPONIVEIS-DESIGN.md) | Tela JogosDisponiveis | Completo |
| RESET-ESTADO-MESTRE.md | Reset de estado pelo Mestre | Em criacao |
| LEVEL-UP.md | Fluxo de level up | Em criacao |
| MODO-SESSAO.md | Modo sessao ativa | Em criacao |
| ADMINISTRACAO.md | Tela de administracao ADMIN | Em criacao |
| ANOTACOES-GALERIA.md | Anotacoes e galeria de imagens | Em criacao |

### Arquitetura e Padroes Backend
| Documento | Descricao |
|-----------|-----------|
| [`backend/01-architecture.md`](backend/01-architecture.md) | Camadas, DI, fluxo completo |
| [`backend/02-entities-dtos.md`](backend/02-entities-dtos.md) | Lombok, records, validacoes |
| [`backend/03-exceptions.md`](backend/03-exceptions.md) | Hierarquia de exceptions |
| [`backend/04-repositories.md`](backend/04-repositories.md) | Query methods, Optional |
| [`backend/05-services.md`](backend/05-services.md) | Transacoes, padroes de update |
| [`backend/06-mappers.md`](backend/06-mappers.md) | MapStruct patterns |
| [`backend/07-controllers.md`](backend/07-controllers.md) | REST patterns, Swagger |
| [`backend/08-security.md`](backend/08-security.md) | OAuth2, sessao, CORS |
| [`backend/09-testing.md`](backend/09-testing.md) | Integracao vs unitario |
| [`backend/10-database.md`](backend/10-database.md) | Naming conventions |
| [`backend/11-owasp-security.md`](backend/11-owasp-security.md) | OWASP Top 10 |

### Dominio Klayrah RPG
| Documento | Descricao |
|-----------|-----------|
| [`GLOSSARIO.md`](GLOSSARIO.md) | Resumo e indice do glossario |
| [`glossario/01-contexto-geral.md`](glossario/01-contexto-geral.md) | O que e Klayrah, conceitos estruturais |
| [`glossario/02-configuracoes-jogo.md`](glossario/02-configuracoes-jogo.md) | 13+1 configuracoes detalhadas |
| [`glossario/03-termos-dominio.md`](glossario/03-termos-dominio.md) | Glossario por sistema |
| [`glossario/04-siglas-formulas.md`](glossario/04-siglas-formulas.md) | Sistema de siglas, motor de formulas |
| [`glossario/05-termos-tecnicos-fluxo.md`](glossario/05-termos-tecnicos-fluxo.md) | Termos tecnicos, fluxo config-ficha |

### Outros
| Documento | Descricao |
|-----------|-----------|
| [`AI_GUIDELINES_BACKEND.md`](AI_GUIDELINES_BACKEND.md) | Guidelines backend para agentes AI |
| [`testes/CENARIOS-TESTE.md`](testes/CENARIOS-TESTE.md) | Cenarios de teste detalhados |
| [`TEAM-PLAN.md`](TEAM-PLAN.md) | Plano de time com 7 fases e 20+ issues |
| [`INDEX.md`](INDEX.md) | Indice antigo de documentos |
| [`PROXIMA-SESSAO.md`](PROXIMA-SESSAO.md) | Planejamento da proxima sessao |

---

*Este documento e a fonte unica de verdade para navegacao do projeto. Atualizar a cada sessao.*
