# Spec 005 — Gestão de Participantes

> Status: 📝 Planejado
> Épico: EPIC 3 do EPICS-BACKLOG.md
> Depende de: Spec 004 (parcialmente — nenhum bloqueio direto)
> Bloqueia: Spec 006 (Ficha de Personagem)

## Contexto

JogoParticipante já existe como entity, mas sem fluxo de convite/aprovação nem validações de permissão por participação. Para que jogadores possam criar fichas, é necessário: (1) um fluxo estruturado de entrada no jogo, (2) CRUD completo de participantes, (3) um SecurityService que valida participação antes de acessar recursos do jogo.

## User Stories

### Story 1 — Solicitar entrada em um jogo (P1)
**Como** jogador,
**Quero** solicitar entrada em um jogo pelo código ou ID,
**Para que** eu possa me tornar participante aprovado e criar fichas.

**Critérios de aceitação:**
- Jogador faz POST /api/jogos/{id}/participantes/solicitar
- Status inicial: PENDENTE
- Não pode solicitar entrada duas vezes no mesmo jogo
- Mestre vê solicitações pendentes

### Story 2 — Aprovar/Rejeitar solicitação (P1)
**Como** mestre,
**Quero** aprovar ou rejeitar solicitações de entrada,
**Para que** eu controle quem participa do meu jogo.

**Critérios de aceitação:**
- PUT /api/jogos/{id}/participantes/{pid}/aprovar → status APROVADO
- PUT /api/jogos/{id}/participantes/{pid}/rejeitar → status REJEITADO
- Apenas o Mestre do jogo pode aprovar/rejeitar
- Participante rejeitado não pode acessar recursos do jogo

### Story 3 — Gerenciar participantes (P1)
**Como** mestre,
**Quero** listar e remover participantes,
**Para que** eu mantenha o controle do grupo.

**Critérios de aceitação:**
- GET /api/jogos/{id}/participantes → lista todos os participantes com status
- DELETE /api/jogos/{id}/participantes/{pid} → remove (soft delete ou status BANIDO)
- Apenas Mestre pode remover participantes

### Story 4 — Permissões baseadas em participação (P1)
**Como** sistema,
**Quero** validar que jogadores só acessam recursos de jogos onde são participantes aprovados,
**Para que** dados de jogos privados sejam protegidos.

**Critérios de aceitação:**
- Jogador sem participação APROVADA recebe 403 ao acessar recursos do jogo
- Mestre sempre tem acesso ao seu próprio jogo
- SecurityService expõe: canAccessJogo(), isMestreDoJogo(), isParticipanteAprovado()

## Requisitos Funcionais

| ID | Descrição |
|----|-----------|
| FR-001 | JogoParticipante deve ter enum StatusParticipante: PENDENTE, APROVADO, REJEITADO, BANIDO |
| FR-002 | POST /api/jogos/{id}/participantes/solicitar — jogador solicita entrada (status PENDENTE) |
| FR-003 | PUT /api/jogos/{id}/participantes/{pid}/aprovar — mestre aprova (status → APROVADO) |
| FR-004 | PUT /api/jogos/{id}/participantes/{pid}/rejeitar — mestre rejeita (status → REJEITADO) |
| FR-005 | DELETE /api/jogos/{id}/participantes/{pid} — mestre bane/remove (status → BANIDO) |
| FR-006 | GET /api/jogos/{id}/participantes — lista participantes (Mestre vê todos, Jogador só APROVADO) |
| FR-007 | Validar que jogador não tem participação existente (qualquer status) ao solicitar |
| FR-008 | Validar que aprovador/rejeitador é o Mestre do jogo |
| FR-009 | JogoParticipanteService com métodos: solicitar, aprovar, rejeitar, banir, listar |
| FR-010 | ParticipanteSecurityService com: canAccessJogo(jogoId), isMestreDoJogo(jogoId), isParticipanteAprovado(jogoId) |
| FR-011 | Integrar ParticipanteSecurityService nos controllers de configuração (validar acesso por jogo) |
| FR-012 | DTOs: SolicitarParticipacaoRequest, ParticipanteResponse (id, usuario, status, dataCriacao) |
| FR-013 | Testes de integração cobrindo todos os cenários de fluxo |

## Requisitos Não-Funcionais

- Usar @PreAuthorize nos endpoints de aprovação/rejeição (MESTRE)
- Transações adequadas (readOnly onde aplicável)
- Logs de auditoria para mudanças de status

## Out of Scope

- Notificações por email/push ao aprovar/rejeitar
- Convites por código (pode vir em spec posterior)
- Transferência de mestria do jogo
