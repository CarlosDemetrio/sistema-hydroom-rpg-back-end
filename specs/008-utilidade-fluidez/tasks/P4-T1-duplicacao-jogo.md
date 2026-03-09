# P4-T1 — Duplicação de Jogo

**Fase:** 4 — Duplicação
**Complexidade:** 🔴 Alta
**Depende de:** nada (usa repos existentes)
**Bloqueia:** nada

## Objetivo

Criar cópia de um jogo com todas as 13 configurações base (sem fichas, participantes ou sub-entidades de relacionamento).

## Checklist

### 1. DuplicarJogoRequest + DuplicarJogoResponse DTOs
- [ ] Request: record com `@NotBlank String novoNome`
- [ ] Response: record com `Long jogoId`, `String nome`

### 2. JogoDuplicacaoService
- [ ] `duplicar(Long jogoId, String novoNome, String mestreId)` → Jogo (novo):
  - Buscar jogo original com JOIN FETCH para cada tipo de config (ou N queries separadas em batch)
  - Criar novo Jogo com novoNome, mestreId atual
  - Para cada tipo de config (13 tipos):
    - Buscar todas as configs do jogo original
    - Criar novas entities com mesmo nome/campos, novo jogoId, novos IDs (não referenciar IDs originais)
    - Preservar ordemExibicao
  - Criar JogoParticipante(status=APROVADO) para o mestreId no novo jogo
  - Tudo em @Transactional único
- [ ] **NÃO copiar**: RacaBonusAtributo, ClasseBonus, ClasseAptidaoBonus, RacaClassePermitida, VantagemPreRequisito — esses relacionamentos precisam ser reconfigurados

### 3. Endpoint
- [ ] `POST /api/jogos/{id}/duplicar`
- [ ] `@PreAuthorize("hasRole('MESTRE')")`
- [ ] Validar que usuário é dono do jogo original

## Arquivos afetados
- `dto/request/DuplicarJogoRequest.java` (NOVO)
- `dto/response/DuplicarJogoResponse.java` (NOVO)
- `service/JogoDuplicacaoService.java` (NOVO)
- `controller/JogoController.java` (MODIFICAR)

## Verificações de aceitação
- [ ] POST /duplicar cria novo jogo com mesmo total de configs
- [ ] Alterações no jogo original não afetam a cópia
- [ ] Usuário que duplicou é Mestre do novo jogo
- [ ] Sub-entidades de relacionamento NÃO são copiadas
- [ ] `./mvnw test` passa
