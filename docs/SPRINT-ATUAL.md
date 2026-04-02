# Sprint Atual — Sprint 1: "Ficha Jogavel"

> Gerado: 2026-04-01 | Atualizado: 2026-04-02 (sessao 4)
> PM: Scrum Orchestrator
> Objetivo do Sprint: Tornar a ficha de personagem funcional end-to-end (backend ja pronto, foco em frontend + polimento backend)
> Duracao estimada: 2 semanas

---

## Progresso Geral

| Metrica | Valor |
|---------|-------|
| Tasks totais Sprint 1 | 27 |
| Concluidas | 23 |
| Em andamento | 5 |
| Pendentes | 4 |
| Adiadas Sprint 2 | 2 |
| **Progresso Sprint 1** | **85% concluido** (23/27) |
| Testes backend | **422 testes** (era 405 pre-sprint) |
| Gaps criticos descobertos (sessao 4) | 23 (C1-C2, M1-M7, G1-G10) |
| User Stories mapeadas (BA) | 8 (US-FICHA-01 a US-FICHA-08) |

---

## Sessao 2026-04-02 (Sessao 3) — Correcoes de Build Frontend

### O que foi feito

12 correcoes de compilacao Angular realizadas nesta sessao. O frontend estava quebrado por incompatibilidades PrimeNG 21, modelos desalinhados com backend, e imports incorretos.

- [x] `tsconfig.json`: alias `@shared` adicionado (bare + wildcard)
- [x] `base-config-table`: `p-input-icon` migrado para `p-inputicon` (PrimeNG 21), null guard em `rowReorder`
- [x] `mestre-dashboard`: `totalJogadores` corrigido para `totalJogos`, `jogosRecentes` para `jogos`
- [x] `jogador-dashboard`: modelo Ficha alinhado (flat: `nivel`, `racaNome`, `classeNome` direto)
- [x] `classes-config`: `FormsModule` adicionado para `ngModel` em `p-select`
- [x] `limitadores-config`: componente zombie removido (entidade backend removida na Spec 004)
- [x] `ficha-form`: `@shared` import corrigido, modelo flat, `createFicha(jogoId, dto)` correto
- [x] `identificacao-section`: `@shared` import corrigido
- [x] `fichas-list`: `p-input-icon` migrado, modelo flat, removido `ficha.calculados` (inexistente)
- [x] `ficha-detail`: `valueChange` type fix, `AnotacaoCardComponent` warning removido
- [x] `ficha-resumo-tab`: `DecimalPipe` adicionado
- [x] `ficha-vantagens-tab`: `DividerModule` adicionado

**Testes frontend em andamento:** Dev 3 escrevendo specs para `base-config-table`, `atributos-config`, `niveis-config`.

### Tasks atualizadas nesta sessao

| ID | Mudanca |
|----|---------|
| SP1-T24 | [CONCLUIDO] — jogo-detail ja estava corrigido na sessao anterior + fix mestre-dashboard |
| SP1-T25 | [CONCLUIDO] — jogo-form fix completado + ficha-form + identificacao-section |
| SP1-T26 | [CONCLUIDO] — imports @shared corrigidos em todos os componentes afetados |

---

## Sessao 2026-04-02 (Sessao 4) — Quick Wins + Auditorias Profundas

### Quick Wins Implementados (Dev 1)

- [x] **QW-5**: Tooltips em tabs desabilitadas (classes, racas, vantagens) — explica ao usuario porque a tab esta desabilitada
- [x] **QW-3**: Aviso de pre-requisito ausente em AptidoesConfig — alerta visual quando aptidao referencia tipo inexistente
- [x] **QW-2**: Badges de contagem na sidebar + reordenacao logica de dependencias — sidebar mostra quantos itens cada config tem

### Bug Corrigido (Tech Lead)

- [x] **Fix totalFichas**: mestre-dashboard exibia array raw em vez de contagem — corrigido para `.length` via `computed()`

### Em Andamento (lancados nesta sessao)

- [ ] **FormulaEditorComponent** (Dev 2) — relancado, sem resultado confirmado ainda
- [ ] **QW-4**: Formula visivel na lista de bonus de Classe
- [ ] **QW-1**: Conectar reordenacao ao backend real (wiring handleReorder -> ConfigApiService)
- [ ] **Backend**: GET /fichas/{id}/atributos + GET /fichas/{id}/aptidoes + categoriaNome no response
- [ ] **NPC screen**: Tela de NPCs para o Mestre (listagem + criacao)

### Gaps Criticos Descobertos — Auditoria Tech Lead Frontend

O Tech Lead Frontend realizou auditoria completa de codigo e descobriu divergencias significativas entre backend e frontend.

#### CRITICOS — Funcionalidade 100% quebrada

| ID | Descricao | Impacto |
|----|-----------|---------|
| **C1** | `handleReorder()` em TODOS os 13 config components mostra toast falso SEM chamar a API. `ConfigApiService` ja tem metodos prontos — falta wiring. | Reordenacao esta 100% fake em 13 telas |
| **C2** | Dashboard do Mestre: "Jogadores Ativos" exibe contagem de jogos, nao participantes. Backend tem `GET /jogos/{id}/dashboard` com `totalParticipantes`. | Dado errado exibido ao Mestre |

#### MAJOR — Endpoints backend sem cobertura no frontend

| ID | Descricao | Impacto |
|----|-----------|---------|
| **M1** | `PontosVantagemController` — CRUD completo no backend, ZERO no frontend (sem model, service, store, component) | Feature inteira invisivel |
| **M2** | `FormulaController` (POST /preview, GET /variaveis) sem API service no frontend | Formula Editor nao funciona sem isso |
| **M3** | `SiglaController` (GET /siglas) sem API service no frontend | Validacao cross-entity impossivel no frontend |
| **M4** | Atributos tab no FichaDetail: mockados com base=0, nivel=0 (falta GET /fichas/{id}/atributos no backend) | Atributos sempre zerados |
| **M5** | Aptidoes tab no FichaDetail: sempre vazia (falta GET /fichas/{id}/aptidoes no backend) | Tab inutilizavel |
| **M7** | PUT /usuarios/me sem UI no frontend (edicao de perfil) | Feature backend desperdicada |

#### DIVIDA TECNICA

| ID | Descricao |
|----|-----------|
| DT-FE-01 | `atualizarAnotacao()` no frontend sem endpoint PUT no backend (metodo fantasma) |
| DT-FE-02 | `CategoriaVantagem` URL usa `/api/jogos/` sem `/v1/` — inconsistencia com todos os outros services |
| DT-FE-03 | ConfigStore type assertions `any` em multiplos locais |

### Gaps Criticos Descobertos — Auditoria BA (Fluxo Ficha)

O BA realizou analise end-to-end do fluxo de ficha e descobriu que a maior parte da funcionalidade de ficha e uma casca vazia ou mockada.

#### BLOQUEADORES DE USO REAL

| ID | Descricao | Impacto |
|----|-----------|---------|
| **G1** | FichaFormComponent envia apenas `{nome}` para backend — ignora raca, classe, genero, indole, presenca. Formulario de 10 secoes e casca vazia. | Criacao de ficha nao funciona |
| **G2** | Atributos mockados (base=0, nivel=0, impeto=0) no FichaDetail | Dados falsos exibidos |
| **G3** | Aptidoes nunca carregadas — lista sempre vazia | Feature quebrada |
| **G4** | Sem UI para Mestre conceder XP ou renascimentos | Progressao de nivel impossivel |
| **G5** | Sem tela de NPC para o Mestre (listagem + criacao) | Funcionalidade totalmente ausente |

#### FUNCIONALIDADE INCOMPLETA

| ID | Descricao | Impacto |
|----|-----------|---------|
| **G6** | Barras de Vida e Essencia sempre 100% (backend nao retorna vidaAtual/essenciaAtual no resumo) | Dados falsos |
| **G7** | `pontosVantagemRestantes` hardcoded como 0 | Calculo de pontos nao funciona |
| **G8** | FichaVantagemResponse sem `categoriaNome` — vantagens sem organizacao por categoria | UX confusa |
| **G9** | Rota de edicao inconsistente entre List e Detail | Navegacao quebrada |
| **G10** | Sem marketplace de compra de vantagens | Feature planejada ausente |

#### DADOS QUE BACKEND PRECISA ADICIONAR

| Campo/Endpoint | Onde | Status |
|----------------|------|--------|
| `vidaAtual`/`essenciaAtual` no FichaResumoResponse | Backend response | PENDENTE |
| `base`, `nivel`, `outros`, `impeto` por atributo (GET /fichas/{id}/atributos) | Novo endpoint | EM ANDAMENTO |
| `categoriaNome` em FichaVantagemResponse | Backend response | EM ANDAMENTO |
| Pontos de atributo disponiveis vs. usados no resumo | Backend response | PENDENTE |
| GET /fichas/{id}/aptidoes | Novo endpoint | EM ANDAMENTO |

### 8 User Stories Mapeadas pelo BA

| ID | Titulo | Prioridade | Estimativa | Dependencia |
|----|--------|-----------|-----------|-------------|
| US-FICHA-01 | Reescrita do formulario de criacao (wizard alinhado ao backend) | P0 | P3 (grande) | Backend pronto |
| US-FICHA-02 | Atributos reais no Detail (sem mock) | P0 | P2 | GET /fichas/{id}/atributos (backend) |
| US-FICHA-03 | Distribuicao de atributos e aptidoes | P0 | P2 | US-FICHA-02 |
| US-FICHA-04 | Concessao de XP pelo Mestre | P0 | P2 | Backend pronto |
| US-FICHA-05 | Tela de NPCs para o Mestre | P0 | P2 | Backend pronto (NPC endpoints existem) |
| US-FICHA-06 | Barras de Vida e Essencia reativas | P1 | P1 | vidaAtual/essenciaAtual no response (backend) |
| US-FICHA-07 | Compra de Vantagens (marketplace) | P1 | P3 (grande) | US-FICHA-01, categoriaNome |
| US-FICHA-08 | Unificacao de rota de edicao | P2 | P1 | Nenhuma |

### Features faltantes identificadas (analise BA sessao 3)

Novas tasks criadas para Sprint 2 a partir da analise dos documentos BA:

| ID | Prioridade | Descricao |
|----|-----------|-----------|
| SP2-T01 | CRITICO | Formula Editor Component — editor visual com autocomplete de variaveis, botao "Validar", preview numerico |
| SP2-T02 | CRITICO | Sub-recursos de Classe no frontend (ClasseBonus + ClasseAptidaoBonus) — endpoints existem |
| SP2-T03 | CRITICO | Sub-recursos de Raca no frontend (RacaBonusAtributo + RacaClassePermitida) — endpoints existem |
| SP2-T04 | ALTA | Color picker para CategoriaVantagem |
| SP2-T05 | ALTA | Validacao async de unicidade de sigla nos campos de abreviacao |
| SP2-T06 | ALTA | Sub-recursos de Vantagem (efeitos e pre-requisitos) — UI de gestao |
| SP2-T07 | MEDIA | Inline editable table para NivelConfig |
| SP2-T08 | MEDIA | Validacao de sequencia crescente de XP no frontend |
| SP2-T09 | MEDIA | Progress bar visual para porcentagemVida em MembroCorpoConfig |
| SP2-T10 | BAIXA | Filtro/agrupamento por TipoAptidao nas aptidoes |
| SP2-T11 | BAIXA | Indicador visual de bonus negativo nas racas |

---

## Diagnostico Pre-Sprint

### Descobertas da Auditoria de Codigo (2026-04-01)

| Item | Status Documentado (PM.md) | Status Real (codigo) | Impacto |
|------|---------------------------|---------------------|---------|
| B001: Role checks em ~18 controllers | CRITICA - pendente | **JA IMPLEMENTADO** em todos os 18 controllers | Issue FECHADA - nao bloqueia mais |
| B002: JogoController.criar() PreAuthorize | ALTA - pendente | **JA IMPLEMENTADO** | Issue FECHADA |
| Testes backend | 272 testes | **422 testes passando**, 0 failures | Melhoria significativa |
| FichaController endpoints | 90% | **100%** - todos os endpoints com PreAuthorize | Pronto |
| FichaDetailComponent | Placeholder | **IMPLEMENTADO** (SP1-T01 a SP1-T06) | CONCLUIDO |
| JogosDisponiveisComponent | Placeholder | **IMPLEMENTADO** (SP1-T07) | CONCLUIDO |
| FichaForm 10 secoes | Desalinhado | **Confirmado** - campos inexistentes (origem, linhagem, etc.), atributos hardcoded | ADIADO Sprint 2 |

### Conclusao: Backend CONCLUIDO (422 testes). Frontend FichaDetail e JogosDisponiveis CONCLUIDOS. Build frontend corrigido (sessao 3). Restam reviews de backend e DDL.

---

## Caminho Critico — ATUALIZADO (sessao 4)

```
SPRINT 1 — FASE DE FECHAMENTO (85% concluido)
=============================================

CONCLUIDOS:
  FichaDetailComponent (SP1-T01 a T06) .....  CONCLUIDO
  JogosDisponiveisComponent (SP1-T07) .....  CONCLUIDO
  Build fixes (SP1-T24, T25, T26) .....  CONCLUIDO (sessao 3)
  Quick Wins QW-2, QW-3, QW-5, fix totalFichas .....  CONCLUIDO (sessao 4)

EM ANDAMENTO (sessao 4):
  SP1-T18 (seguranca NPC) [EM ANDAMENTO] --> commit pending
  SP1-T22 (N+1 queries) [EM ANDAMENTO]
  SP1-T23 (testes FichaController) [EM ANDAMENTO]
  SP1-T28 (GET /fichas/{id}/atributos) [EM ANDAMENTO] --> desbloqueia G2, M4
  SP1-T29 (GET /fichas/{id}/aptidoes) [EM ANDAMENTO] --> desbloqueia G3, M5
  SP1-T30 (categoriaNome response) [EM ANDAMENTO] --> desbloqueia G8
  QW-1 (handleReorder wiring) [EM ANDAMENTO] --> resolve C1

PENDENTES:
  SP1-T27 (DDL producao) [PENDENTE] --> precisa antes do deploy
  SP1-T13 (barras HP) [PENDENTE]
  SP1-T14 (participantes UI) [PENDENTE]
  SP1-T17 (skeletons) [PENDENTE]

CAMINHO CRITICO PARA "FICHA USAVEL DE VERDADE":
  GET /fichas/{id}/atributos (SP1-T28) --> US-FICHA-02 --> US-FICHA-03
  FichaForm rewrite (US-FICHA-01) --> tudo depende de form funcional
  Tela NPC (US-FICHA-05) --> funcionalidade 100% ausente
  XP pelo Mestre (US-FICHA-04) --> progressao impossivel sem isso
```

---

## Quadro de Tarefas do Sprint

### P0 — Bloqueadores (Sprint Goal) — TODOS CONCLUIDOS

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T01 | angular-frontend-dev | FichaDetailComponent: smart page com abas + header sticky + stats bar | **[CONCLUIDO]** | Nenhuma | P2 (grande) |
| SP1-T02 | angular-frontend-dev | FichaDetailComponent: ficha-header sub-component | **[CONCLUIDO]** | SP1-T01 | P2 |
| SP1-T03 | angular-frontend-dev | FichaDetailComponent: ficha-atributos-tab + ficha-resumo-tab | **[CONCLUIDO]** | SP1-T01 | P2 |
| SP1-T04 | angular-frontend-dev | FichaDetailComponent: ficha-aptidoes-tab (agrupadas por tipo) | **[CONCLUIDO]** | SP1-T01 | P1 |
| SP1-T05 | angular-frontend-dev | FichaDetailComponent: ficha-vantagens-tab com cards por categoria | **[CONCLUIDO]** | SP1-T01 | P2 |
| SP1-T06 | angular-frontend-dev | FichaDetailComponent: ficha-anotacoes-tab com CRUD inline | **[CONCLUIDO]** | SP1-T01 | P2 |
| SP1-T07 | angular-frontend-dev | JogosDisponiveisComponent: cards de jogos + selecionar jogo | **[CONCLUIDO]** | Nenhuma | P2 |

### P1 — Alta Prioridade

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T08 | angular-frontend-dev | Consumir GET /fichas/{id}/resumo para exibir valores calculados | **[CONCLUIDO]** | SP1-T01 | P1 |
| SP1-T09 | primeng-ux-architect | Design specs: FichaDetailPage layout + componentes visuais | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T10 | primeng-ux-architect | Design specs: JogosDisponiveisComponent layout | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T11 | angular-tech-lead | Models: FichaVantagemResponse, ComprarVantagemDto, FichaCompletaData | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T12 | angular-tech-lead | FichaBusinessService completo (loadFichaCompleta, vantagens, anotacoes CRUD) | **[CONCLUIDO]** | SP1-T11 | P1 |
| SP1-T13 | primeng-ux-architect | Membros do corpo em VidaSectionComponent: barras de HP por membro | [PENDENTE] | SP1-T09 | P2 |
| SP1-T14 | angular-tech-lead | Participantes UI: aprovar/rejeitar/banir (componente + service) | [PENDENTE] | Nenhuma | P2 |

### P2 — Should Have

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T15 | angular-frontend-dev | FichaForm Wizard 4 passos: reescrever step-identificacao | **[ADIADO SPRINT 2]** | SP1-T11 | P2 |
| SP1-T16 | angular-frontend-dev | FichaForm Wizard: step-atributos dinamico | **[ADIADO SPRINT 2]** | SP1-T15 | P2 |
| SP1-T17 | primeng-ux-architect | Estados de loading/erro/vazio (skeletons, empty states) | [PENDENTE] | SP1-T09 | P1 |
| SP1-T18 | senior-backend-dev | Revisao de seguranca: NPCs bloqueados para Jogadores + N+1 fixes | **[EM ANDAMENTO]** | Nenhuma | P1 |
| SP1-T19 | senior-backend-dev | Endpoint PUT /fichas/{id}/vida e PUT /fichas/{id}/prospeccao | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T20 | senior-backend-dev | NpcCreateRequest completo + campo descricao no fluxo NPC | **[CONCLUIDO]** | Nenhuma | P1 |

### Backend

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T21 | senior-backend-dev | Perfil do usuario: GET/PUT /api/v1/usuarios/me (6 testes) | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T22 | java-spring-tech-lead | Tech Lead review: N+1 queries nos endpoints de ficha | **[EM ANDAMENTO]** | Nenhuma | P1 |
| SP1-T23 | java-spring-tech-lead | Testes integracao: FichaController (todos os endpoints) | **[EM ANDAMENTO]** | Nenhuma | P2 |

### Novas Tasks Descobertas e Resolvidas (sessoes 2 e 3)

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T24 | angular-frontend-dev | Fix jogo-detail + mestre-dashboard: modelos alinhados com backend | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T25 | angular-frontend-dev | Fix jogo-form + ficha-form + identificacao-section: imports e modelos | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T26 | angular-tech-lead | Atualizar imports @shared em todos os componentes afetados | **[CONCLUIDO]** | Nenhuma | P1 |
| SP1-T27 | java-spring-tech-lead | DDL para producao: 3 ALTER TABLE statements | [PENDENTE] | SP1-T19 | P1 |

### Quick Wins e Fixes (sessao 4)

| ID | Dono | Descricao | Status | Estimativa |
|---|---|---|---|---|
| QW-5 | angular-frontend-dev | Tooltips em tabs desabilitadas (classes, racas, vantagens) | **[CONCLUIDO]** | P1 |
| QW-3 | angular-frontend-dev | Aviso de pre-requisito ausente em AptidoesConfig | **[CONCLUIDO]** | P1 |
| QW-2 | angular-frontend-dev | Badges de contagem na sidebar + reordenacao logica | **[CONCLUIDO]** | P1 |
| FIX-01 | angular-tech-lead | Fix totalFichas no mestre-dashboard: array -> .length via computed() | **[CONCLUIDO]** | P1 |
| QW-4 | angular-frontend-dev | Formula visivel na lista de bonus de Classe | [EM ANDAMENTO] | P1 |
| QW-1 | angular-frontend-dev | Conectar reordenacao ao backend real (wiring handleReorder) | [EM ANDAMENTO] | P1 |

### Backend — Novos endpoints em andamento (sessao 4)

| ID | Dono | Descricao | Status | Estimativa |
|---|---|---|---|---|
| SP1-T28 | senior-backend-dev | GET /fichas/{id}/atributos (detalhado: base, nivel, outros, impeto) | [EM ANDAMENTO] | P1 |
| SP1-T29 | senior-backend-dev | GET /fichas/{id}/aptidoes (lista real, nao mockada) | [EM ANDAMENTO] | P1 |
| SP1-T30 | senior-backend-dev | Adicionar `categoriaNome` em FichaVantagemResponse | [EM ANDAMENTO] | P1 |
| SP1-T31 | senior-backend-dev | Tela NPC para o Mestre (backend support — listagem/criacao) | [EM ANDAMENTO] | P1 |

### Issues Fechadas (pre-sprint audit)

| ID | Descricao | Motivo |
|---|---|---|
| ~~B001~~ | ~~Role checks em ~18 controllers~~ | JA IMPLEMENTADO - verificado em codigo 2026-04-01 |
| ~~B002~~ | ~~JogoController.criar() PreAuthorize~~ | JA IMPLEMENTADO - verificado em codigo 2026-04-01 |

### Fixes de Build Integrados (sessao 3 — sem task ID)

Correcoes aplicadas diretamente em componentes existentes:

- `base-config-table`: p-input-icon migrado para p-inputicon (PrimeNG 21), null guard rowReorder
- `mestre-dashboard`: totalJogadores corrigido para totalJogos, jogosRecentes para jogos
- `jogador-dashboard`: modelo Ficha flat (nivel, racaNome, classeNome direto)
- `classes-config`: FormsModule adicionado para ngModel em p-select
- `limitadores-config`: componente zombie removido
- `fichas-list`: p-input-icon migrado, modelo flat, removido ficha.calculados
- `ficha-detail`: valueChange type fix, AnotacaoCardComponent warning removido
- `ficha-resumo-tab`: DecimalPipe adicionado
- `ficha-vantagens-tab`: DividerModule adicionado

---

## Gargalos Detectados

### GARGALO 1 — Frontend (RESOLVIDO sessao 2-3)

```
GARGALO RESOLVIDO
Agente afetado: angular-frontend-dev
Resolucao: Trabalho distribuido entre 3 agentes (UX, TL, Dev). Todos os P0 entregues.
Build corrigido na sessao 3 (12 fixes de compilacao).
Resultado: SP1-T01 a SP1-T08 CONCLUIDOS, build limpo.
```

### GARGALO 2 — Backend (BAIXO RISCO — MANTIDO)

Backend esta saudavel (422 testes, 0 failures). 3 tasks em andamento com 2 agentes. SP1-T18 tem codigo pronto mas faltando commit (6 arquivos de security fixes).

### GARGALO 3 — Frontend-Backend Gap (NOVO — sessao 4)

```
DETECTADO GARGALO: DIVERGENCIA MASSIVA FRONTEND-BACKEND
Escopo: 23 gaps (C1-C2 criticos, M1-M7 major, G1-G10 fluxo ficha)
Impacto: Funcionalidades que parecem prontas estao mockadas ou quebradas
  - 13 telas de config com reordenacao fake (C1)
  - Dashboard com dados errados (C2)
  - Criacao de ficha envia apenas {nome} (G1)
  - Atributos e aptidoes sempre zerados/vazios (G2, G3)
  - Progressao de nivel impossivel (G4)
  - Tela NPC inexistente (G5)
Agentes afetados: angular-frontend-dev, senior-backend-dev
Resolucao em andamento: QW-1 (C1), SP1-T28/T29/T30 (M4/M5/G8)
Sprint 2 absorvera: US-FICHA-01 a US-FICHA-08
```

---

## Plano Anti-Conflito — ATUALIZADO

### Regras de Propriedade de Arquivos

| Agente | PODE tocar | NAO pode tocar |
|--------|-----------|----------------|
| **angular-frontend-dev** | `features/jogador/pages/**`, `features/mestre/pages/jogo-detail/**`, `features/mestre/pages/jogo-form/**` | `core/services/business/**`, `core/stores/**`, backend |
| **angular-tech-lead** | `core/services/business/**`, `core/stores/**`, `core/services/api/**`, `core/models/**` | `features/**` (pages/componentes), backend |
| **primeng-ux-architect** | `src/styles.scss`, `shared/components/**`, documentacao de design | `features/**` (pages), `core/**`, backend |
| **java-spring-tech-lead** | Testes de integracao, DDL scripts, review de queries | `controller/configuracao/**`, model, `service/configuracao/**` |
| **senior-backend-dev** | `service/Ficha*.java` (security fixes), novos arquivos | `controller/configuracao/**`, `service/configuracao/**` |

---

## Proximos Despachos — Atualizado sessao 4

### P0 — Bloqueadores imediatos

| Prioridade | Task | Agente | Justificativa |
|-----------|------|--------|---------------|
| **1** | Commitar security fixes backend (SP1-T18) | senior-backend-dev | 6 arquivos nao commitados no working tree |
| **2** | Confirmar SP1-T28/T29/T30 (atributos/aptidoes/categoriaNome) | senior-backend-dev | Desbloqueia G2, G3, G8, M4, M5 |
| **3** | Confirmar QW-1 (handleReorder wiring) | angular-frontend-dev | Resolve C1 em 13 telas |
| **4** | SP1-T27 | java-spring-tech-lead | DDL para producao — precisa antes do deploy |

### Sprint 1 Closing — Em paralelo

| Prioridade | Task | Agente | Justificativa |
|-----------|------|--------|---------------|
| **1** | SP1-T22 (N+1 queries) | java-spring-tech-lead | Em andamento |
| **2** | SP1-T23 (testes FichaController) | java-spring-tech-lead | Em andamento |
| **3** | SP1-T13 | primeng-ux-architect | Barras de HP por membro |
| **4** | SP1-T14 | angular-tech-lead | Participantes UI |
| **5** | SP1-T17 | primeng-ux-architect | Skeletons e empty states |

### Sprint 2 Preparation — Iniciar assim que Sprint 1 fechar

| Prioridade | Task | Agente sugerido | Justificativa |
|-----------|------|----------------|---------------|
| **1** | US-FICHA-01 (FichaForm wizard rewrite) | angular-frontend-dev | Bloqueador #1 de uso real |
| **2** | US-FICHA-05 (Tela NPC Mestre) | angular-frontend-dev | Feature 100% ausente |
| **3** | US-FICHA-04 (XP pelo Mestre) | angular-tech-lead + senior-backend-dev | Progressao impossivel |

---

## Metricas de Sucesso do Sprint

| Metrica | Target | Status Atual |
|---------|--------|-------------|
| FichaDetailPage funcional com dados reais | SIM | **PARCIAL** — estrutura pronta mas atributos/aptidoes mockados (G2, G3) |
| JogosDisponiveisComponent funcional | SIM | **CONCLUIDO** |
| Valores calculados visiveis na ficha | SIM | **PARCIAL** — resumo funciona, atributos detalhados pendentes |
| Testes backend | >= 405 (manter) | **422** (+17) |
| Zero regressoes | SIM | **SIM** |
| TypeScript compila sem erros | SIM | **PENDENTE VERIFICACAO** (12 fixes aplicados, build nao confirmado) |
| FichaForm cria ficha completa | SIM | **FALHOU** — envia apenas {nome} (G1) |
| Reordenacao de configs funcional | SIM | **FALHOU** — toast falso em 13 telas (C1, fix em andamento) |

**NOTA sessao 4**: A auditoria profunda revelou que varios itens marcados como "CONCLUIDO" nas sessoes anteriores estao parcialmente funcionais. Os componentes existem e compilam, mas exibem dados mockados ou nao conectam ao backend real. Isso muda a percepcao de "85% concluido" — o Sprint 1 entregou a **estrutura visual** mas nao a **funcionalidade real** em varias areas criticas.

---

## Debitos Tecnicos Conhecidos

| ID | Descricao | Impacto | Sprint |
|----|-----------|---------|--------|
| DT-01 | FichaForm.component.ts com campos fantasma (origem, linhagem, etc.) | CRITICO — form nao funciona (G1) | Sprint 2 (US-FICHA-01) |
| DT-02 | Atributos hardcoded no FichaForm (FOR/AGI/VIG/SAB/INT) | CRITICO — nao generalizavel (G2) | Sprint 2 (US-FICHA-02) |
| DT-03 | DDL de producao nao gerado (3 ALTER TABLE pendentes) | MEDIO — necessario antes do deploy | Sprint 1 (SP1-T27) |
| DT-04 | Zero testes de componente Angular (cobertura minima = 0) | MEDIO | Sprint 2 |
| DT-05 | Features faltantes: Formula Editor, sub-recursos Classe/Raca no frontend | ALTO — gap entre backend e frontend | Sprint 2 (SP2-T01 a T03) |
| DT-06 | 13 handleReorder() com toast falso sem chamar API (C1) | CRITICO — feature fake em 13 telas | Sprint 1/2 (QW-1 em andamento) |
| DT-07 | `atualizarAnotacao()` frontend sem endpoint PUT backend (fantasma) | MEDIO — metodo morto | Sprint 2 |
| DT-08 | CategoriaVantagem URL `/api/jogos/` sem `/v1/` (inconsistencia) | BAIXO — funciona mas inconsistente | Sprint 2 |
| DT-09 | ConfigStore type assertions `any` em multiplos locais | BAIXO — type safety | Sprint 2 |
| DT-10 | PontosVantagemController: CRUD backend completo, ZERO frontend (M1) | ALTO — feature invisivel | Sprint 2 |
| DT-11 | FormulaController + SiglaController sem API service frontend (M2, M3) | ALTO — infraestrutura ausente | Sprint 2 |

---

## Backlog do Proximo Sprint (Preview — Atualizado sessao 4)

### P0 — Critico (proxima implementacao imediata)

| Prioridade | Descricao | Origem | Estimativa |
|-----------|-----------|--------|-----------|
| P0-1 | **US-FICHA-01**: Reescrita FichaForm wizard (10 secoes alinhadas ao backend) | G1, SP1-T15/T16 adiados | P3 (grande) |
| P0-2 | **US-FICHA-05**: Tela de NPCs para o Mestre (listagem + criacao) | G5 — 100% ausente | P2 |
| P0-3 | **US-FICHA-04**: Concessao de XP pelo Mestre (progressao de nivel) | G4 — impossivel sem isso | P2 |
| P0-4 | **US-FICHA-02 + US-FICHA-03**: Atributos e aptidoes reais no Detail + distribuicao | G2, G3, M4, M5 | P2 |

### P1 — Alta Prioridade (sprint proxima)

| Prioridade | Descricao | Origem | Estimativa |
|-----------|-----------|--------|-----------|
| P1-1 | **SP2-T01**: Formula Editor Component (autocomplete, validacao, preview) | BA analise, M2 | P2 |
| P1-2 | **M1**: PontosVantagem completo no frontend (model + service + store + component) | Tech Lead audit | P2 |
| P1-3 | **US-FICHA-07**: Marketplace de compra de vantagens | G10, G7 | P3 (grande) |
| P1-4 | **M2 + M3**: FormulaController + SiglaController API services no frontend | Tech Lead audit | P1 |
| P1-5 | **SP2-T02**: Sub-recursos de Classe (ClasseBonus + ClasseAptidaoBonus) | BA analise, endpoints existem | P2 |
| P1-6 | **SP2-T03**: Sub-recursos de Raca (RacaBonusAtributo + RacaClassePermitida) | BA analise, endpoints existem | P2 |

### P2 — Media Prioridade

| Prioridade | Descricao | Origem | Estimativa |
|-----------|-----------|--------|-----------|
| P2-1 | **US-FICHA-06**: Barras de Vida e Essencia reativas | G6 — depende de backend | P1 |
| P2-2 | **C2**: Dashboard do Mestre com dados reais (totalParticipantes) | Tech Lead audit | P1 |
| P2-3 | **M7**: Edicao de perfil (PUT /usuarios/me) com UI | Tech Lead audit | P1 |
| P2-4 | **US-FICHA-08**: Unificacao de rota de edicao List vs Detail | G9 | P1 |
| P2-5 | **SP2-T04**: Color picker CategoriaVantagem | BA analise | P1 |
| P2-6 | **SP2-T05**: Validacao async de unicidade de sigla | BA analise | P1 |
| P2-7 | **SP2-T06**: Sub-recursos de Vantagem (efeitos e pre-requisitos UI) | BA analise | P2 |

### Divida Tecnica (Sprint 2)

| Item | Descricao | Origem |
|------|-----------|--------|
| C1 | 13 handleReorder() conectados ao backend real | Tech Lead audit (QW-1 em andamento) |
| DT-FE-01 | atualizarAnotacao() fantasma: remover ou backend implementa PUT | Tech Lead audit |
| DT-FE-02 | CategoriaVantagem URL corrigida (`/api/v1/jogos/`) | Tech Lead audit |
| DT-FE-03 | ConfigStore type assertions `any` removidas | Tech Lead audit |
| SP2-T07 | NivelConfig inline editable table + validacao XP crescente | BA analise |
| SP2-T09 | Progress bar porcentagemVida MembroCorpoConfig | BA analise |

### P3 — Baixa Prioridade / Backlog Futuro

| Item | Descricao | Origem |
|------|-----------|--------|
| SP2-T10 | Filtro/agrupamento por TipoAptidao nas aptidoes | BA analise |
| SP2-T11 | Indicador visual de bonus negativo nas racas | BA analise |
| — | Testes de componente Angular (cobertura minima) | Backlog original |
| — | Historico Envers endpoint | Backlog original |
| — | Mobile responsiveness | Backlog original |
