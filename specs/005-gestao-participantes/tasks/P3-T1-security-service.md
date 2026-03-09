# P3-T1 — ParticipanteSecurityService + Integração nos Controllers

**Fase:** 3 — Security
**Complexidade:** 🟡 Média
**Depende de:** P2-T1
**Bloqueia:** nada (mas bloqueia Spec 006)

## Objetivo

Centralizar a lógica de validação de acesso por participação e integrá-la nos controllers existentes.

## Checklist

### 1. ParticipanteSecurityService

- [ ] `canAccessJogo(Long jogoId, String usuarioId)` → boolean
  - Retorna true se: usuário é Mestre do jogo OU tem participação APROVADA
- [ ] `isMestreDoJogo(Long jogoId, String usuarioId)` → boolean
  - Verifica se o usuário criou o jogo (jogo.mestreId == usuarioId)
- [ ] `isParticipanteAprovado(Long jogoId, String usuarioId)` → boolean
  - Verifica participação com status APROVADO
- [ ] `assertCanAccessJogo(Long jogoId, String usuarioId)` — lança AccessDeniedException se não pode acessar
- [ ] Anotar com `@Service`, injetar `JogoRepository` e `JogoParticipanteRepository`

### 2. Integração nos controllers de configuração

- [ ] Identificar todos os controllers de configuração que recebem `{jogoId}` no path
- [ ] Nos endpoints de leitura (GET), chamar `participanteSecurityService.assertCanAccessJogo()` antes de retornar dados
- [ ] Nos endpoints de escrita (POST/PUT/DELETE), já protegidos por `@PreAuthorize("hasRole('MESTRE')")` — adicionar validação de que o Mestre é dono do jogo

**Nota:** Não é necessário alterar todos os controllers de uma vez — priorizar os mais sensíveis (VantagemConfig, AtributoConfig).

## Arquivos afetados

- `service/ParticipanteSecurityService.java` (NOVO)
- `controller/configuracao/AtributoController.java` (MODIFICAR)
- `controller/configuracao/VantagemController.java` (MODIFICAR)
- (Outros controllers conforme necessário)

## Verificações de aceitação

- [ ] canAccessJogo retorna false para usuário sem participação
- [ ] canAccessJogo retorna true para Mestre e para participante APROVADO
- [ ] canAccessJogo retorna false para participante PENDENTE/REJEITADO/BANIDO
- [ ] GET /api/jogos/{id}/atributos retorna 403 para jogador sem participação aprovada
- [ ] `./mvnw test` passa
