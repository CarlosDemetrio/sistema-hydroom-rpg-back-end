# Sprint Atual — Sprint 1: "Ficha Jogavel"

> Gerado: 2026-04-01 | PM: Scrum Orchestrator  
> Objetivo do Sprint: Tornar a ficha de personagem funcional end-to-end (backend ja pronto, foco em frontend + polimento backend)  
> Duracao estimada: 2 semanas

---

## Diagnostico Pre-Sprint

### Descobertas da Auditoria de Codigo (2026-04-01)

| Item | Status Documentado (PM.md) | Status Real (codigo) | Impacto |
|------|---------------------------|---------------------|---------|
| B001: Role checks em ~18 controllers | CRITICA - pendente | **JA IMPLEMENTADO** em todos os 18 controllers | Issue FECHADA - nao bloqueia mais |
| B002: JogoController.criar() PreAuthorize | ALTA - pendente | **JA IMPLEMENTADO** | Issue FECHADA |
| Testes backend | 272 testes | **405 testes passando**, 0 failures | Melhoria significativa |
| FichaController endpoints | 90% | **100%** - todos os endpoints com PreAuthorize | Pronto |
| FichaDetailComponent | Placeholder | **Confirmado placeholder** - precisa implementacao do zero | CRITICO |
| JogosDisponiveisComponent | Placeholder | **Confirmado placeholder** - precisa implementacao do zero | CRITICO |
| FichaForm 10 secoes | Desalinhado | **Confirmado** - campos inexistentes (origem, linhagem, etc.), atributos hardcoded | CRITICO - reescrever |

### Conclusao: Backend esta mais avancado que o documentado. O caminho critico agora e 100% FRONTEND.

---

## Caminho Critico

```
FichaDetailComponent (F-P0a) ──> Valores Calculados (F-P1a) ──> Membros do corpo (F-P1b)
       |
       └── FichaForm Wizard (F-P2a) ──> reescrever step-identificacao + step-atributos

JogosDisponiveisComponent (F-P0b) ──> Participantes UI (F-P1c) [depende backend participantes OK]
```

---

## Quadro de Tarefas do Sprint

### P0 — Bloqueadores (Sprint Goal)

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T01 | angular-frontend-dev | FichaDetailComponent: smart page com abas (Resumo, Atributos, Aptidoes, Vantagens, Anotacoes) + header sticky + stats bar | [PENDENTE] | Nenhuma (backend pronto) | P2 (grande) |
| SP1-T02 | angular-frontend-dev | FichaDetailComponent: ficha-header + ficha-stats-bar sub-components | [PENDENTE] | SP1-T01 | P2 |
| SP1-T03 | angular-frontend-dev | FichaDetailComponent: ficha-atributos-tab + ficha-bonus-tab | [PENDENTE] | SP1-T01 | P2 |
| SP1-T04 | angular-frontend-dev | FichaDetailComponent: ficha-aptidoes-tab (agrupadas por tipo) | [PENDENTE] | SP1-T01 | P1 |
| SP1-T05 | angular-frontend-dev | FichaDetailComponent: ficha-vantagens-tab com cards por categoria | [PENDENTE] | SP1-T01 | P2 |
| SP1-T06 | angular-frontend-dev | FichaDetailComponent: ficha-anotacoes-tab com CRUD inline | [PENDENTE] | SP1-T01 | P2 |
| SP1-T07 | angular-frontend-dev | JogosDisponiveisComponent: cards de jogos + solicitar acesso + status | [PENDENTE] | Nenhuma | P2 |

### P1 — Alta Prioridade

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T08 | angular-frontend-dev | Consumir GET /fichas/{id}/resumo para exibir valores calculados | [PENDENTE] | SP1-T01 | P1 |
| SP1-T09 | primeng-ux-architect | Design specs: FichaDetailPage layout + componentes visuais (barras de vida, cards de atributo, badge NPC) | [PENDENTE] | Nenhuma | P1 |
| SP1-T10 | primeng-ux-architect | Design specs: JogosDisponiveisComponent layout (cards de jogo, status badges) | [PENDENTE] | Nenhuma | P1 |
| SP1-T11 | angular-tech-lead | Revisar arquitetura ficha-detail: signals, stores, facade, lazy loading | [PENDENTE] | Nenhuma | P1 |
| SP1-T12 | angular-tech-lead | Definir FichaBusinessService completo (loadFichaCompleta, pontosRestantes signals) | [PENDENTE] | SP1-T11 | P1 |
| SP1-T13 | primeng-ux-architect | Membros do corpo em VidaSectionComponent: barras de HP por membro | [PENDENTE] | SP1-T09 | P2 |
| SP1-T14 | angular-tech-lead | Participantes UI: aprovar/rejeitar/banir (componente + service) | [PENDENTE] | Nenhuma (backend ParticipanteController existe) | P2 |

### P2 — Should Have (se sobrar tempo no sprint)

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T15 | angular-frontend-dev | FichaForm Wizard 4 passos: reescrever step-identificacao (remover campos fantasma) | [PENDENTE] | SP1-T11 | P2 |
| SP1-T16 | angular-frontend-dev | FichaForm Wizard: step-atributos dinamico (carregar de config, nao hardcoded) | [PENDENTE] | SP1-T15 | P2 |
| SP1-T17 | primeng-ux-architect | Estados de loading/erro/vazio nos novos componentes (skeletons, empty states) | [PENDENTE] | SP1-T09 | P1 |
| SP1-T18 | java-spring-tech-lead | Revisao de seguranca: audit dos endpoints de Ficha (dono vs MESTRE checks) | [PENDENTE] | Nenhuma | P1 |
| SP1-T19 | senior-backend-dev | Endpoint PUT /fichas/{id}/vida e PUT /fichas/{id}/prospeccao | [PENDENTE] | Nenhuma | P1 |
| SP1-T20 | senior-backend-dev | NpcCreateRequest completo + validacoes de endpoint dedicado | [PENDENTE] | Nenhuma | P1 |

### Backend (Baixa Prioridade — nao bloqueia frontend)

| ID | Dono | Descricao | Status | Dependencia | Estimativa |
|---|---|---|---|---|---|
| SP1-T21 | senior-backend-dev | Perfil do usuario: GET/PUT /api/v1/usuarios/me | [PENDENTE] | Nenhuma | P1 |
| SP1-T22 | java-spring-tech-lead | Tech Lead review: N+1 queries nos endpoints de ficha | [PENDENTE] | Nenhuma | P1 |
| SP1-T23 | java-spring-tech-lead | Testes integracao: FichaController (todos os endpoints) | [PENDENTE] | Nenhuma | P2 |

### Issues Fechadas (pre-sprint audit)

| ID | Descricao | Motivo |
|---|---|---|
| ~~B001~~ | ~~Role checks em ~18 controllers~~ | JA IMPLEMENTADO - verificado em codigo 2026-04-01 |
| ~~B002~~ | ~~JogoController.criar() PreAuthorize~~ | JA IMPLEMENTADO - verificado em codigo 2026-04-01 |

---

## Gargalos Detectados

### GARGALO 1 — Frontend (CRITICO)

```
DETECTADO GARGALO NO FRONT
Agente afetado: angular-frontend-dev
Tasks em risco: SP1-T01 a SP1-T08, SP1-T15, SP1-T16 (10 tasks)
Impacto estimado: 10 tasks de frontend para um unico desenvolvedor = risco de nao entregar P0 no sprint
```

**Mitigacao aplicada:** Fragmentacao em 3 frentes:

| Agente | Foco | Tasks |
|--------|------|-------|
| angular-tech-lead | Arquitetura, facades, services, review | SP1-T11, SP1-T12, SP1-T14 |
| angular-frontend-dev | Implementacao de componentes (ficha-detail, jogos-disponiveis, wizard) | SP1-T01 a SP1-T08, SP1-T15, SP1-T16 |
| primeng-ux-architect | Design specs, componentes visuais, barras de vida, skeletons, UX | SP1-T09, SP1-T10, SP1-T13, SP1-T17 |

**Sequencia de acionamento recomendada:**
1. `primeng-ux-architect` primeiro (design specs para ficha-detail e jogos-disponiveis)
2. `angular-tech-lead` segundo (arquitetura de facades e services)
3. `angular-frontend-dev` terceiro (implementacao com design specs + arquitetura definidos)

### GARGALO 2 — Backend (BAIXO RISCO)

Backend esta saudavel (405 testes, 0 failures). Trabalho residual e pequeno.

---

## Plano Anti-Conflito

### Regras de Propriedade de Arquivos

| Agente | PODE tocar | NAO pode tocar |
|--------|-----------|----------------|
| **angular-frontend-dev** | `features/jogador/pages/ficha-detail/**`, `features/jogador/pages/jogos-disponiveis/**`, `features/jogador/pages/ficha-form/**` | `core/services/business/**`, `core/stores/**`, `features/mestre/**`, backend |
| **angular-tech-lead** | `core/services/business/ficha-business.service.ts`, `core/stores/fichas.store.ts`, `core/services/api/fichas-api.service.ts`, `features/mestre/pages/participantes/**` | `features/jogador/pages/**` (componentes), backend |
| **primeng-ux-architect** | `src/styles.scss`, `app.config.ts` (tema), `shared/components/**`, documentacao de design (md files) | `features/**` (pages), `core/**`, backend |
| **java-spring-tech-lead** | `controller/FichaController.java` (review only), `service/FichaCalculationService.java`, testes de integracao | `controller/configuracao/**`, model, repository |
| **senior-backend-dev** | `controller/FichaController.java` (novos endpoints vida/prospeccao), `dto/request/NpcCreateRequest.java`, `service/UsuarioService.java`, novos arquivos | `controller/configuracao/**`, `service/configuracao/**` |

### Zonas de Conflito Potencial

| Arquivo | Risco | Resolucao |
|---------|-------|-----------|
| `fichas-api.service.ts` | angular-tech-lead e angular-frontend-dev podem ambos precisar | **Dono: angular-tech-lead**. Frontend-dev consome, nao modifica |
| `fichas.store.ts` | angular-tech-lead e angular-frontend-dev | **Dono: angular-tech-lead**. Frontend-dev usa via inject() |
| `FichaController.java` | java-spring-tech-lead (review) e senior-backend-dev (novos endpoints) | **Dono: senior-backend-dev** para novos endpoints. TL faz review depois |

---

## Proximos 3 Agentes a Acionar (em ordem)

### 1. primeng-ux-architect (IMEDIATO)

**Missao:** Produzir design specs visuais para FichaDetailPage e JogosDisponiveisComponent. Definir layout, componentes PrimeNG, paleta de cores, barras de vida/essencia, cards de atributo, badges de NPC, empty states e skeletons.

**Tasks:** SP1-T09, SP1-T10, SP1-T13, SP1-T17

**Inputs necessarios:** BA-FICHA.md (secoes 4, 5, 6), API-CONTRACT.md (secao 4)

**Output esperado:** Documento de design com layout ASCII/wireframe, componentes PrimeNG especificos, props de cada componente, responsive breakpoints.

---

### 2. angular-tech-lead (APOS UX specs prontas)

**Missao:** Definir arquitetura tecnica do FichaDetailPage: facade service, signals, computed properties, lazy loading de tabs, FichaBusinessService completo com loadFichaCompleta(), pontosAtributoRestantes signal, pontosVantagemRestantes signal.

**Tasks:** SP1-T11, SP1-T12, SP1-T14

**Inputs necessarios:** Design specs do UX architect, BA-FICHA.md, API-CONTRACT.md, CLAUDE.md do frontend

**Output esperado:** Codigo dos services e stores. Interfaces e types. Skeleton de ficha-detail.page.ts com facade pattern.

---

### 3. angular-frontend-dev (APOS arquitetura definida)

**Missao:** Implementar FichaDetailComponent completo e JogosDisponiveisComponent usando os design specs e a arquitetura definida.

**Tasks:** SP1-T01 a SP1-T08

**Inputs necessarios:** Design specs, arquitetura definida, services prontos

**Output esperado:** Componentes funcionais conectados ao backend real.

---

## Metricas de Sucesso do Sprint

| Metrica | Target |
|---------|--------|
| FichaDetailPage funcional com dados reais | SIM |
| JogosDisponiveisComponent funcional | SIM |
| Valores calculados visiveis na ficha | SIM |
| Testes backend | >= 405 (manter) |
| Zero regressoes | SIM |

---

## Backlog do Proximo Sprint (Preview)

| Prioridade | Descricao |
|-----------|-----------|
| P0 | FichaForm Wizard completo (reescrever do zero alinhado com backend) |
| P1 | UI de NPCs para o Mestre |
| P1 | Testes de componente Angular (cobertura minima) |
| P1 | Editor de Formulas (modal com validacao) |
| P2 | Redesenho das 13 paginas de config |
| P2 | Mestre Dashboard melhorado |
| P2 | Historico Envers endpoint |
| P2 | Mobile responsiveness |
