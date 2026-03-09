# P2-T1 — JogoParticipanteService + JogoParticipanteController

**Fase:** 2 — Service/Controller
**Complexidade:** 🔴 Alta
**Depende de:** P1-T1
**Bloqueia:** P3-T1

## Objetivo

Implementar toda a lógica de negócio do fluxo de participação e expor os endpoints REST.

## Checklist

### 1. JogoParticipanteService

- [ ] `solicitar(Long jogoId, String usuarioId)` — cria participação com status PENDENTE
  - Validar: jogo existe, usuário não é o Mestre, não existe participação prévia (qualquer status)
  - Lançar `ConflictException` se já existe
- [ ] `aprovar(Long jogoId, Long participanteId, String aprovadorId)` — status → APROVADO
  - Validar: aprovador é o Mestre do jogo, participante está PENDENTE
- [ ] `rejeitar(Long jogoId, Long participanteId, String aprovadorId)` — status → REJEITADO
  - Mesmas validações de `aprovar`
- [ ] `banir(Long jogoId, Long participanteId, String banidorId)` — status → BANIDO
  - Validar: banidor é o Mestre, não pode banir a si mesmo
- [ ] `listar(Long jogoId, String solicitanteId)` → List<JogoParticipante>
  - Mestre vê todos; Jogador vê apenas APROVADO
- [ ] `@Transactional(readOnly = true)` na classe, `@Transactional` nos métodos de escrita

### 2. JogoParticipanteController

Endpoints:
- [ ] `POST /api/jogos/{jogoId}/participantes/solicitar` — `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`
- [ ] `PUT /api/jogos/{jogoId}/participantes/{pid}/aprovar` — `@PreAuthorize("hasRole('MESTRE')")`
- [ ] `PUT /api/jogos/{jogoId}/participantes/{pid}/rejeitar` — `@PreAuthorize("hasRole('MESTRE')")`
- [ ] `DELETE /api/jogos/{jogoId}/participantes/{pid}` — `@PreAuthorize("hasRole('MESTRE')")`
- [ ] `GET /api/jogos/{jogoId}/participantes` — `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

### 3. Exceções esperadas

- `ResourceNotFoundException` — jogo ou participante não encontrado
- `ConflictException` — participação duplicada
- `AccessDeniedException` (403) — ação não autorizada (não é Mestre)
- `BusinessException` (400) — status inválido para transição (ex: aprovar um BANIDO)

## Arquivos afetados

- `service/JogoParticipanteService.java` (NOVO)
- `controller/JogoParticipanteController.java` (NOVO)

## Verificações de aceitação

- [ ] POST solicitar retorna 201 com ParticipanteResponse
- [ ] PUT aprovar retorna 200 com status APROVADO
- [ ] PUT rejeitar retorna 200 com status REJEITADO
- [ ] DELETE retorna 200 com status BANIDO
- [ ] GET lista retorna lista correta por perfil (Mestre vs Jogador)
- [ ] Tentativa de solicitar duas vezes retorna 409
- [ ] `./mvnw test` passa
