# PM.md — Klayrah RPG: Ficha Controlador

> Fonte única de verdade para status do projeto.
> Gerado em: 2026-04-01 | Branch: `feature/009-npc-fichas-mestre`
> Substitui: TEAM-PLAN.md, EPICS-BACKLOG.md, UX-BACKLOG.md (para fins de status)

---

## Status Geral

| Área | Progresso | Observação |
|------|-----------|------------|
| Backend: Infraestrutura | 95% | Portas Docker ainda não atualizadas (INFRA-001) |
| Backend: Configurações (13 CRUDs) | 100% | Todos com testes de integração |
| Backend: Motor de Fórmulas | 70% | FormulaEvaluatorService existe; faltam FormulaController exposto, validação cross-entity |
| Backend: Ficha de Personagem (Spec 006/007) | 90% | FichaCalculationService, FichaVantagemService com testes; faltam role checks e endpoint `/fichas/{id}/vida` |
| Backend: NPC + Duplicação (Spec 009) | 80% | Endpoint NPC e duplicação existem; NpcCreateRequest incompleto |
| Backend: Anotações | 90% | FichaAnotacaoController + Service implementados |
| Backend: Segurança (role checks) | 70% | JogoController.criar() restaurado; ~18 controllers de config sem @PreAuthorize consistente |
| Frontend: Modelos e Serviços de API | 75% | Alinhados com backend; URLs com jogoId corrigidas parcialmente |
| Frontend: Componentes | 60% | 26 completos; 2 placeholders críticos; membros do corpo e valores calculados ausentes |
| Frontend: Testes | 5% | Apenas 2 arquivos .spec.ts em todo o projeto |

**Completude geral estimada: ~65%**

---

## O que está FEITO (por spec)

- **Spec 001** — 13 CRUDs de configuração + Template Klayrah (GameConfigInitializerService)
- **Spec 003** — Refactor: DTOs records, validações, exceptions, mappers, testes base
- **Spec 004 (parcial)** — SiglaValidationService, CategoriaVantagem, PontosVantagem, VantagemPreRequisito, ClasseBonus, RacaClassePermitida, VantagemEfeito (8 tipos)
- **Spec 006/007** — FichaService, FichaCalculationService, FichaVantagemService, FichaPreviewService, FichaResumoService, NivelConfig.permitirRenascimento
- **Spec 008** — DashboardController, duplicação de jogo, export/import de config, resumo de ficha, filtros, reordenação batch
- **Spec 009 (parcial)** — NPC, duplicação de ficha, anotações, updates diretos (atributos/aptidões), segurança básica
- **Frontend** — Design system RPG, BaseConfigTable, 13 páginas de config funcionais, FichaForm (10 seções), OAuth2

> Detalhes de specs: `docs/TEAM-PLAN.md`, `docs/EPICS-BACKLOG.md`
> Contrato de API completo: `docs/API-CONTRACT.md`
> 93 User Stories detalhadas: `docs/PRODUCT-BACKLOG.md`
> Auditoria UX completa: `docs/UX-BACKLOG.md`

---

## Sprint Atual — Prioridades Imediatas

### P0 — Bloqueadores Críticos

**1. [FRONT] FichaDetailComponent — implementar**
- Rota: `/jogador/fichas/:id` — atualmente: placeholder "Em Desenvolvimento"
- Mínimo: abas (Resumo | Atributos | Aptidões | Vantagens | Anotações), header com nome/nível/raça/classe, valores calculados via `GET /fichas/{id}/resumo`
- Arquivo: `src/app/features/jogador/pages/ficha-detail/`

**2. [FRONT] JogosDisponiveisComponent — implementar**
- Rota: `/jogador/jogos` — atualmente: placeholder "Em Desenvolvimento"
- Cards de jogos, "Solicitar Acesso", status da solicitação
- Arquivo: `src/app/features/jogador/pages/jogos-disponiveis/`

**3. [BACK] Role checks nos ~18 controllers de configuração — ISSUE-B001**
- Escrita: `hasRole('MESTRE')`, leitura: `hasAnyRole('MESTRE', 'JOGADOR')`
- Dir: `src/main/java/.../controller/configuracao/`

### P1 — Alta Prioridade

**4. [FRONT] Exibir valores calculados na ficha**
- Consumir `GET /api/v1/fichas/{id}/resumo` (endpoint já existe)
- Atualizar `AtributosSectionComponent` e `VidaSectionComponent`

**5. [FRONT] Membros do corpo em VidaSectionComponent**
- Backend já tem `MembroCorpoConfig` e `FichaVidaMembro`
- Seção de localização de danos por membro ausente

**6. [BACK] NpcCreateRequest completo + endpoint dedicado — ISSUE-B003**
- `POST /api/v1/jogos/{jogoId}/npcs` separado de fichas de jogadores

---

## Backlog Priorizado

### Backend

| Issue | Descrição | Prio |
|-------|-----------|------|
| B001 | Role checks em ~18 controllers de config | CRÍTICA |
| B003 | NpcCreateRequest + endpoint NPC dedicado | ALTA |
| B005 | Endpoint `PUT /fichas/{id}/vida` e `PUT /fichas/{id}/prospeccao` | ALTA |
| B007 | Perfil do usuário: `GET/PUT /api/v1/usuarios/me` | MÉDIA |
| B010 | Histórico Envers: `GET /fichas/{id}/historico` | MÉDIA |
| B011 | Convidar jogador por email | BAIXA |

### Frontend

| Issue | Descrição | Prio |
|-------|-----------|------|
| F-P0a | FichaDetailComponent | CRÍTICA |
| F-P0b | JogosDisponiveisComponent | CRÍTICA |
| F-P1a | Valores calculados na ficha (consumir /resumo) | ALTA |
| F-P1b | Membros do corpo em VidaSectionComponent | ALTA |
| F-P1c | UI de participantes (aprovar/rejeitar/banir) | ALTA |
| F-P1d | UI de NPCs para o Mestre | MÉDIA |
| F-P2a | FichaForm → Wizard (5–6 passos) | P2 |
| F-P2b | Editor de Fórmulas (modal com validação) | P2 |
| F-QA | Cobertura básica de testes (Vitest + Testing Library) | ALTA |

### QA / Tech Lead

| Issue | Descrição |
|-------|-----------|
| Q001 | Testes integração: todos os 18 controllers de config |
| Q002 | Testes integração: FichaController (NPCs, vida, duplicar) |
| Q003 | Testes componente Angular (FichasApiService, AuthService, BaseConfigComponent) |

---

## Riscos em Aberto

| Item | Risco |
|------|-------|
| VantagemEfeito | Backend OK (8 tipos), frontend não consome ainda |
| Ciclos DFS em pré-requisitos | Adiado — retomar antes de EPIC 4 completo |
| Testes frontend | 2 arquivos .spec.ts — risco alto de regressão |
| Proxy dev local | Verificar se `proxy.conf.local.json` existe no frontend |
