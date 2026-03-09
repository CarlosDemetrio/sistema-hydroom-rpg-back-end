# Plano de Implementação — Spec 005 (Gestão de Participantes)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, código existente

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 3
- `model/JogoParticipante.java` — entity existente
- `controller/JogoController.java` — padrão atual
- `config/SecurityConfig.java` — OAuth2 e roles

**Estado atual:**
- JogoParticipante: entity existe, sem status, sem CRUD exposto
- JogoController: CRUD básico de Jogo, sem endpoints de participantes
- Sem SecurityService de participação

## Phase 1 — JogoParticipante: Status + DTOs

**Objetivo:** Adicionar enum de status e preparar DTOs/repositório para o fluxo de participação.

**Tarefas:**
- P1-T1: Adicionar `StatusParticipante` enum e campo `status` na entity JogoParticipante
- P1-T1: Criar DTOs: `SolicitarParticipacaoRequest`, `AtualizarStatusParticipanteRequest`, `ParticipanteResponse`
- P1-T1: Adicionar queries no `JogoParticipanteRepository`: findByJogoIdAndUsuarioId, findByJogoIdAndStatus

## Phase 2 — JogoParticipanteService + Controller

**Objetivo:** Implementar lógica de negócio e endpoints REST completos.

**Tarefas:**
- P2-T1: `JogoParticipanteService` com métodos solicitar, aprovar, rejeitar, banir, listar
- P2-T1: `JogoParticipanteController` com todos os endpoints (POST solicitar, PUT aprovar/rejeitar, DELETE, GET)

## Phase 3 — ParticipanteSecurityService

**Objetivo:** Centralizar validações de acesso por participação.

**Tarefas:**
- P3-T1: `ParticipanteSecurityService` com canAccessJogo, isMestreDoJogo, isParticipanteAprovado
- P3-T1: Integrar nos controllers de configuração existentes (validar acesso ao jogo)

## Phase 4 — Testes de Integração

**Objetivo:** Cobertura completa do fluxo de participação.

**Tarefas:**
- P4-T1: Testes de integração para JogoParticipanteService

## Ordem de execução

Phase 1 → Phase 2 → Phase 3 → Phase 4

Phase 1 bloqueia Phase 2 (entity/DTOs necessários para service).
Phase 2 bloqueia Phase 3 (service necessário para security).

## Riscos

- JogoParticipante já pode ter dados sem status — migração necessária (default APROVADO para existentes)
- Integração do SecurityService nos controllers existentes pode gerar conflitos com testes atuais
