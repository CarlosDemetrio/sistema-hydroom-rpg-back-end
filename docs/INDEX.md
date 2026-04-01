# INDEX — Klayrah RPG: Ficha Controlador
> Índice mestre de controle. Última atualização: 2026-04-01
> Branch: `feature/009-npc-fichas-mestre` | Completude geral: ~65%

Fontes: `docs/PM.md`, `docs/PRODUCT-BACKLOG.md`, `docs/UX-BACKLOG.md`, `docs/TEAM-PLAN.md`, `docs/EPICS-BACKLOG.md`

---

## Legenda

- `[x]` Concluído
- `[ ]` Pendente / Não iniciado
- `[~]` Parcialmente implementado
- `[CRÍTICO]` Bloqueia uso do sistema
- `[ALTO]` Alta prioridade, não bloqueador
- `[MÉDIO]` Importante, pode aguardar
- `[BAIXO]` Nice-to-have

---

## 1. DOCUMENTAÇÃO E PLANEJAMENTO

### 1.1 Documentação de Domínio

- [x] Glossário geral: `docs/GLOSSARIO.md`
- [x] Contexto geral (Klayrah): `docs/glossario/01-contexto-geral.md`
- [x] Configurações do jogo: `docs/glossario/02-configuracoes-jogo.md`
- [x] Termos de domínio: `docs/glossario/03-termos-dominio.md`
- [x] Siglas e fórmulas: `docs/glossario/04-siglas-formulas.md`
- [x] Termos técnicos e fluxo: `docs/glossario/05-termos-tecnicos-fluxo.md`

### 1.2 Documentação de Arquitetura Backend

- [x] Arquitetura e camadas: `docs/backend/01-architecture.md`
- [x] Entidades e DTOs: `docs/backend/02-entities-dtos.md`
- [x] Exceptions: `docs/backend/03-exceptions.md`
- [x] Repositories: `docs/backend/04-repositories.md`
- [x] Services: `docs/backend/05-services.md`
- [x] Mappers: `docs/backend/06-mappers.md`
- [x] Controllers: `docs/backend/07-controllers.md`
- [x] Segurança: `docs/backend/08-security.md`
- [x] Testes: `docs/backend/09-testing.md`
- [x] Banco de dados: `docs/backend/10-database.md`
- [x] OWASP: `docs/backend/11-owasp-security.md`
- [x] Contrato de API: `docs/API-CONTRACT.md`
- [x] Guidelines backend: `docs/AI_GUIDELINES_BACKEND.md`

### 1.3 Documentação de Produto / PM

- [x] Status geral do projeto: `docs/PM.md`
- [x] 93 User Stories: `docs/PRODUCT-BACKLOG.md`
- [x] Auditoria UX: `docs/UX-BACKLOG.md`
- [x] Plano do time: `docs/TEAM-PLAN.md`
- [x] Backlog de épicos: `docs/EPICS-BACKLOG.md`
- [x] Master Index: `docs/INDEX.md` (este arquivo)

### 1.4 Análises de Domínio (BA)

- [x] BA — VantagemConfig: `docs/analises/BA-VANTAGEM-CONFIG.md`
- [ ] BA — Ficha de Personagem: `docs/analises/BA-FICHA.md` `[ALTO]`
- [ ] BA — Gestão de Participantes: `docs/analises/BA-PARTICIPANTES.md` `[MÉDIO]`
- [ ] BA — Motor de Cálculos: `docs/analises/BA-MOTOR-CALCULOS.md` `[MÉDIO]`
- [ ] Guidelines de frontend: `docs/guidelines/FRONTEND-GUIDELINES.md` `[MÉDIO]`

---

## 2. BACKEND

### 2.1 Configurações — 13 CRUDs (todos com controller, service, mapper, DTOs, testes)

- [x] AtributoConfig — `controller/configuracao/AtributoController.java`
- [x] AptidaoConfig — `controller/configuracao/AptidaoController.java`
- [x] BonusConfig — `controller/configuracao/BonusController.java`
- [x] ClassePersonagem — `controller/configuracao/ClasseController.java`
- [x] DadoProspeccaoConfig — `controller/configuracao/DadoProspeccaoController.java`
- [x] GeneroConfig — `controller/configuracao/GeneroController.java`
- [x] IndoleConfig — `controller/configuracao/IndoleController.java`
- [x] MembroCorpoConfig — `controller/configuracao/MembroCorpoController.java`
- [x] NivelConfig — `controller/configuracao/NivelController.java`
- [x] PresencaConfig — `controller/configuracao/PresencaController.java`
- [x] Raca — `controller/configuracao/RacaController.java`
- [x] TipoAptidao — `controller/configuracao/TipoAptidaoController.java`
- [x] VantagemConfig — `controller/configuracao/VantagemController.java`

### 2.2 Configurações — CRUDs Adicionais

- [x] CategoriaVantagem — `controller/configuracao/CategoriaVantagemController.java`
- [x] PontosVantagemConfig — `controller/configuracao/PontosVantagemController.java`
- [x] VantagemEfeito (8 tipos) — `controller/configuracao/VantagemEfeitoController.java`
- [x] SiglaController — `controller/configuracao/SiglaController.java`
- [x] FormulaController — `controller/configuracao/FormulaController.java`

### 2.3 Configurações — Sub-entidades e relacionamentos

- [x] RacaBonusAtributo — sub-recurso de Raca (endpoints `bonus-atributos`)
- [x] RacaClassePermitida — sub-recurso de Raca (endpoints `classes-permitidas`)
- [x] ClasseBonus — sub-recurso de Classe (endpoints `bonus`)
- [x] ClasseAptidaoBonus — sub-recurso de Classe (endpoints `aptidao-bonus`)
- [x] VantagemPreRequisito — pré-requisitos com detecção de ciclos DFS
- [x] SiglaValidationService — unicidade cross-entity por jogo

### 2.4 Segurança — Role checks nos controllers `[CRÍTICO]`

- [~] Controllers de configuração (17 arquivos) — role checks inconsistentes — **ISSUE-B001**
- [x] `JogoController.criar()` — `@PreAuthorize` restaurado (ISSUE-B002 resolvido)
- [ ] Revisar e fixar `@PreAuthorize` em todos os 17 controllers de config `[CRÍTICO]`

### 2.5 Ficha de Personagem (Specs 006/007)

- [x] FichaService — criação, atualização, listagem, deleção
- [x] FichaCalculationService — recalcula atributos, ímpetos, bônus, vida, essência, ameaça
- [x] FichaVantagemService — compra/gestão de vantagens na ficha
- [x] FichaPreviewService — preview de cálculos sem persistir
- [x] FichaResumoService — versão compacta da ficha para listagens
- [x] FichaAnotacaoController + Service — CRUD de anotações
- [x] `GET /fichas/{id}/resumo` — resumo calculado
- [x] `PUT /fichas/{id}/atributos` — update direto de atributos
- [x] `PUT /fichas/{id}/aptidoes` — update direto de aptidões
- [ ] `PUT /fichas/{id}/vida` — update direto de vida `[ALTO]` — **ISSUE-B005**
- [ ] `PUT /fichas/{id}/prospeccao` — update direto de prospecção `[ALTO]` — **ISSUE-B005**
- [x] NivelConfig.permitirRenascimento — campo adicionado

### 2.6 NPC e Duplicação (Spec 009)

- [x] Endpoint NPC — `GET /jogos/{jogoId}/npcs` lista NPCs separados
- [x] FichaDuplicacaoService — duplicação de ficha existente
- [~] `NpcCreateRequest` — DTO incompleto — **ISSUE-B003** `[ALTO]`
- [ ] `POST /jogos/{jogoId}/npcs` — endpoint dedicado separado de fichas de jogadores `[ALTO]` — **ISSUE-B003**
- [ ] `POST /fichas/{id}/duplicar` — endpoint público de duplicação `[ALTO]` — **ISSUE-B004**

### 2.7 Gestão de Participantes

- [x] JogoParticipanteController — entity e repository existem
- [x] JogoParticipanteService — fluxo de solicitação/aprovação
- [ ] `POST /jogos/{id}/participantes/convidar` por email `[BAIXO]` — **ISSUE-B011**

### 2.8 Endpoints Utilitários (Spec 008)

- [x] `GET /jogos/{id}/dashboard` — DashboardController
- [x] `POST /jogos/{id}/duplicar` — duplicação de jogo
- [x] `GET /jogos/{id}/config/export` — export de configs
- [x] `POST /jogos/{id}/config/import` — import de configs
- [x] Filtros e busca por nome nas configurações
- [x] `PUT /jogos/{id}/config/{tipo}/reordenar` — reordenação batch
- [ ] `POST /jogos/{jogoId}/config/template/klayrah` — reaplicar template `[MÉDIO]` — **ISSUE-B006**

### 2.9 Endpoints Pendentes

- [ ] `GET /api/v1/usuarios/me` — perfil do usuário `[MÉDIO]` — **ISSUE-B007**
- [ ] `PUT /api/v1/usuarios/me` — editar nome/foto `[MÉDIO]` — **ISSUE-B007**
- [ ] `POST /fichas/{id}/restaurar` — restaurar soft-delete `[BAIXO]` — **ISSUE-B008**
- [ ] `GET /fichas/{id}/historico` — histórico Envers `[MÉDIO]` — **ISSUE-B010**

### 2.10 Infraestrutura

- [x] Docker Compose — PostgreSQL local
- [x] OAuth2 Google + sessão (JSESSIONID + XSRF-TOKEN)
- [x] GlobalExceptionHandler, RateLimit, Swagger
- [x] FormulaEvaluatorService (exp4j) + validarFormula()
- [x] GameConfigInitializerService — template Klayrah padrão
- [ ] Atualizar portas no compose.yaml (5453/8181) `[MÉDIO]` — **INFRA-001**
- [ ] CORS aceitar porta 4201 `[MÉDIO]` — **INFRA-004**

---

## 3. FRONTEND

### 3.1 Autenticação e Sessão

- [x] `LoginComponent` — `/login` — OAuth2 Google
- [x] `OAuthCallbackComponent` — callback pós-login
- [x] `AuthService` — estado de autenticação
- [x] `IdleService` — logout automático após 30 min
- [x] `ProfileComponent` — `/profile` — nome, email, role, foto
- [ ] Loading state no botão "Entrar com Google" `[BAIXO]` — UX-009

### 3.2 Dashboards

- [x] `DashboardComponent` — roteamento por role
- [x] `MestreDashboardComponent` — `/mestre/dashboard`
- [x] `JogadorDashboardComponent` — `/jogador/dashboard`
- [ ] Jogo ativo visível no header `[MÉDIO]` — ISSUE-U001, UX-008
- [ ] Indicador de jogo selecionado (dropdown no header) `[MÉDIO]`

### 3.3 Gestão de Jogos (Mestre)

- [x] `JogosListComponent` — `/mestre/jogos` — tabela com filtros e busca
- [x] `JogoFormComponent` — `/mestre/jogos/novo`
- [x] `JogoDetailComponent` — `/mestre/jogos/:id` — 3 abas

### 3.4 Configurações do Jogo — 13 páginas (Mestre)

- [x] `ConfigLayoutComponent` — sidebar de navegação
- [x] `atributos-config` — CRUD de AtributoConfig
- [x] `aptidoes-config` — CRUD de AptidaoConfig
- [x] `bonus-config` — CRUD de BonusConfig
- [x] `classes-config` — CRUD de ClassePersonagem
- [x] `generos-config` — CRUD de GeneroConfig
- [x] `indoles-config` — CRUD de IndoleConfig
- [x] `membros-corpo-config` — CRUD de MembroCorpoConfig
- [x] `niveis-config` — CRUD de NivelConfig
- [x] `presencas-config` — CRUD de PresencaConfig
- [x] `prospeccao-config` — CRUD de DadoProspeccaoConfig
- [x] `racas-config` — CRUD de Raca
- [x] `tipos-aptidao-config` — CRUD de TipoAptidao
- [x] `vantagens-config` — CRUD de VantagemConfig
- [ ] Validação assíncrona de sigla única no frontend `[ALTO]` — ISSUE-U002, UX-007
- [ ] `EditorFormulaComponent` — modal com validação e autocomplete `[ALTO]` — UX-006

### 3.5 Ficha do Jogador — Criação

- [x] `FichaFormComponent` — `/jogador/fichas/nova` — 10 seções
- [x] `IdentificacaoSectionComponent` — nome, raça, classe, gênero
- [x] `ProgressaoSectionComponent` — nível, XP
- [x] `DescricaoFisicaSectionComponent` — altura, peso, aparência
- [x] `AtributosSectionComponent` — distribuição de atributos
- [x] `VidaSectionComponent` — vida total (sem membros do corpo)
- [x] `PericiasSectionComponent` — aptidões/perícias
- [x] `VantagensSectionComponent` — vantagens compradas
- [x] `ObservacoesSectionComponent` — anotações livres
- [x] `TitulosRunasSectionComponent` — títulos e runas
- [x] `EquipamentosSectionComponent` — equipamentos
- [ ] Membros do corpo em `VidaSectionComponent` `[ALTO]` — ISSUE-F005
- [ ] VantagemEfeito estruturado em `VantagensSectionComponent` `[MÉDIO]`
- [ ] Converter `FichaFormComponent` para wizard (5–6 passos) `[MÉDIO]` — UX-004

### 3.6 Ficha do Jogador — Visualização `[CRÍTICO]`

- [x] `FichasListComponent` — `/jogador/fichas` — busca, filtros, empty states
- [ ] `FichaDetailComponent` — `/jogador/fichas/:id` — **PLACEHOLDER** `[CRÍTICO]` — F-P0a
  - [ ] Abas: Resumo | Atributos | Aptidões | Vantagens | Anotações
  - [ ] Header fixo: nome, nível, raça, classe
  - [ ] Consumir `GET /fichas/{id}/resumo` para valores calculados
  - [ ] Exibir valores calculados (ímpeto, bônus, vida total) `[ALTO]` — F-P1a

### 3.7 Jogos Disponíveis (Jogador) `[CRÍTICO]`

- [ ] `JogosDisponiveisComponent` — `/jogador/jogos` — **PLACEHOLDER** `[CRÍTICO]` — F-P0b
  - [ ] Cards de jogos disponíveis
  - [ ] Botão "Solicitar Acesso"
  - [ ] Indicador de status da solicitação (PENDENTE/APROVADO/REJEITADO)

### 3.8 Participantes (Mestre)

- [ ] UI de gerenciamento de participantes em `JogoDetailComponent` `[ALTO]` — ISSUE-F007
  - [ ] Listar participantes com status
  - [ ] Ações: aprovar, rejeitar, banir

### 3.9 NPCs (Mestre)

- [ ] `UI de NPCs` — seção exclusiva do mestre `[MÉDIO]` — ISSUE-F008, F-P1d
  - [ ] Listar NPCs separados das fichas de jogadores
  - [ ] Criar/editar NPC

### 3.10 Camada de API e Integração

- [~] Serviços de API alinhados com backend — URLs com jogoId corrigidas parcialmente
- [ ] `proxy.conf.local.json` para dev local (localhost:8181) `[CRÍTICO]` — INFRA-002, ISSUE-F002
- [ ] Script `npm run start:local` no package.json `[CRÍTICO]` — INFRA-003
- [ ] `CurrentGameService` integrado em todas as páginas protegidas `[ALTO]` — ISSUE-F003
- [ ] Chamadas de API: dashboard, participantes (aprovar/rejeitar/banir), NPCs `[ALTO]` — ISSUE-F004

### 3.11 Design System e UX

- [x] Design system RPG (CSS variables, tema Aura customizado)
- [x] `BaseConfigTableComponent` — componente reutilizável excelente
- [ ] `NotificationCenterComponent` — bell icon + badge `[MÉDIO]` — UX-005
- [ ] `StepperComponent` — para form wizard `[MÉDIO]`
- [ ] `BarraVidaComponent` — barra de vida com cor dinâmica `[MÉDIO]`
- [ ] Skeleton loaders em todos os componentes com chamadas async `[MÉDIO]` — ISSUE-U004
- [ ] Empty states com CTA ilustrado `[MÉDIO]`
- [ ] Mobile responsiveness (< 768px) `[BAIXO]` — ISSUE-U006
- [ ] Design tokens faltantes: `--rpg-success-color`, `--rpg-failure-color`, `--rpg-crit-color` `[BAIXO]`

---

## 4. TESTES

### 4.1 Backend — Testes de Integração (38 arquivos)

- [x] 13 `*ConfiguracaoServiceIntegrationTest` — um por config, ~10 cenários cada
- [x] `CategoriaVantagemServiceIntegrationTest`
- [x] `PontosVantagemServiceIntegrationTest`
- [x] `SiglaValidationServiceIntegrationTest`
- [x] `VantagemPreRequisitoIntegrationTest`
- [x] `RacaSubResourcesIntegrationTest` + `ClasseSubRecursosIntegrationTest`
- [x] `FichaServiceIntegrationTest`
- [x] `FichaCalculationIntegrationTest`
- [x] `FichaVantagemServiceIntegrationTest`
- [x] `FichaAtualizacaoDirectaIntegrationTest`
- [x] `FichaDuplicacaoIntegrationTest`
- [x] `NpcFichaMestreIntegrationTest`
- [x] `JogoServiceIntegrationTest` + `JogoDuplicacaoServiceIntegrationTest`
- [x] `JogoParticipanteServiceIntegrationTest`
- [x] `ConfigExportImportServiceIntegrationTest`
- [ ] Testes de controller (role checks) — todos os 17 controllers de config `[ALTO]` — ISSUE-Q001
- [ ] Testes de FichaController — duplicar, vida, prospeccao, NPC `[ALTO]` — ISSUE-Q002

### 4.2 Frontend — Testes de Componente

- [x] `app.spec.ts` — smoke test básico
- [x] `example.component.spec.ts` — exemplo
- [ ] `ConfigApiService` — mock HTTP `[ALTO]` — ISSUE-Q003
- [ ] `FichasApiService` — mock HTTP `[ALTO]` — ISSUE-Q003
- [ ] `AuthService` — mock OAuth2 `[ALTO]` — ISSUE-Q003
- [ ] `BaseConfigComponent` — render e interação `[ALTO]` — ISSUE-Q003
- [ ] Cobertura mínima: 1 teste por componente crítico (Vitest + Testing Library) `[ALTO]`

---

## 5. ÉPICOS — STATUS RESUMIDO

| Épico | Descrição | Status | Spec |
|-------|-----------|--------|------|
| E1 | Autenticação e Onboarding | 60% | — |
| E2 | Mestre: Gestão de Jogos | 62% | — |
| E3 | Mestre: Gestão de Participantes | 60% | 005 |
| E4 | Mestre: Configurações (13 CRUDs) | 70% | 001/004 |
| E5 | Jogador: Criação de Ficha | 40% | 006 |
| E6 | Jogador: Ficha — Visualização | 0% | 006/007 |
| E7 | Mestre: Visão de Fichas e NPCs | 25% | 009 |
| E8 | Modo Sessão (Jogo Ativo) | 0% | — |
| E9 | Flow de Level Up | 0% | — |
| E10 | NPC | 40% | 009 |
| E11 | Testes e Qualidade | 5% | — |

---

## 6. PRIORIDADES IMEDIATAS (Sprint Atual)

### P0 — Bloqueadores Críticos

- [ ] `FichaDetailComponent` — implementar tela de visualização da ficha `[CRÍTICO]` — F-P0a
- [ ] `JogosDisponiveisComponent` — implementar busca e solicitação de acesso `[CRÍTICO]` — F-P0b
- [ ] Role checks nos 17 controllers de configuração `[CRÍTICO]` — ISSUE-B001
- [ ] `proxy.conf.local.json` + script `start:local` no frontend `[CRÍTICO]` — INFRA-002/003

### P1 — Alta Prioridade

- [ ] `NpcCreateRequest` completo + endpoint `POST /jogos/{id}/npcs` — ISSUE-B003
- [ ] `PUT /fichas/{id}/vida` e `PUT /fichas/{id}/prospeccao` — ISSUE-B005
- [ ] Valores calculados visíveis na ficha (consumir `/resumo`) — F-P1a
- [ ] Membros do corpo em `VidaSectionComponent` — F-P1b
- [ ] UI de participantes (aprovar/rejeitar/banir) — F-P1c
- [ ] Cobertura básica de testes frontend (Vitest) — F-QA

### P2 — Médio Prazo

- [ ] `FichaFormComponent` → wizard 5–6 passos — F-P2a
- [ ] `EditorFormulaComponent` — modal com validação — F-P2b
- [ ] `NotificationCenterComponent` — F-P1 UX
- [ ] Perfil do usuário: `GET/PUT /api/v1/usuarios/me` — ISSUE-B007
- [ ] Histórico Envers: `GET /fichas/{id}/historico` — ISSUE-B010

### P3 — Backlog Futuro

- [ ] Modo Sessão Ativa — `SessaoModoComponent` — UX P2
- [ ] Level Up Wizard — `LevelUpWizardComponent` — UX P2
- [ ] Histórico visual de alterações — `HistoricoAlteracoesComponent` — UX P2
- [ ] Convidar jogador por email — ISSUE-B011
- [ ] Restauração de soft-delete — ISSUE-B008
- [ ] Mobile responsiveness completo — ISSUE-U006
- [ ] Export para PDF

---

## 7. RISCOS EM ABERTO

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| VantagemEfeito não consumido no frontend | MÉDIO | Implementar em VantagensSectionComponent |
| 2 arquivos .spec.ts no frontend | ALTO | Sprint dedicado a testes (Q003) |
| Ciclos DFS em pré-requisitos adiado | BAIXO | Retomar antes de EPIC 4 completo |
| proxy.conf.json aponta para Docker | CRÍTICO | Criar proxy.conf.local.json imediatamente |
| Role checks ausentes em controllers | CRÍTICO | ISSUE-B001 — sprint atual |
