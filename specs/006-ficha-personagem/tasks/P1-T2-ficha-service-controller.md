# P1-T2 — FichaService + FichaController

**Fase:** 1 — CRUD Base
**Complexidade:** 🔴 Alta
**Depende de:** P1-T1
**Bloqueia:** P2-T1

## Objetivo

Implementar o CRUD de Ficha com inicialização automática de sub-registros ao criar.

## Checklist

### 1. FichaRepository
- [ ] `findByJogoIdAndIsNpcFalse(Long jogoId)` — fichas de jogadores
- [ ] `findByJogoIdAndIsNpcTrue(Long jogoId)` — NPCs
- [ ] `findByJogoIdAndJogadorId(Long jogoId, String jogadorId)` — fichas do jogador no jogo
- [ ] `findByIdWithAllSubEntities(Long id)` — JOIN FETCH para busca completa (evitar N+1)

### 2. FichaService

- [ ] `criar(FichaCreateRequest, String usuarioId)` → Ficha:
  - Validar participação APROVADA via ParticipanteSecurityService
  - Validar que todas as FKs de config (raca, classe, etc.) pertencem ao mesmo jogo
  - Criar Ficha
  - Chamar `inicializarSubRegistros(Ficha)` em mesma transação
- [ ] `inicializarSubRegistros(Ficha)`:
  - Para cada AtributoConfig do jogo → criar FichaAtributo(ficha, atributoConfig, base=0, nivel=0, outros=0)
  - Para cada AptidaoConfig do jogo → criar FichaAptidao(ficha, aptidaoConfig, base=0, sorte=0, classe=0)
  - Para cada BonusConfig do jogo → criar FichaBonus(ficha, bonusConfig, base=0, vantagens=0, ...)
  - Para cada MembroCorpoConfig do jogo → criar FichaVidaMembro(fichaVida, membroConfig, danoRecebido=0)
  - Para cada DadoProspeccaoConfig do jogo → criar FichaProspeccao(ficha, dadoConfig, quantidade=0)
  - Criar FichaVida, FichaEssencia, FichaAmeaca (únicos por Ficha)
- [ ] `buscar(Long fichaId, String usuarioId)` → FichaResponse (validar acesso)
- [ ] `listar(Long jogoId, String usuarioId)` → List (Mestre vê todas, Jogador só as próprias)
- [ ] `atualizar(Long fichaId, FichaUpdateRequest, String usuarioId)` → Ficha
- [ ] `deletar(Long fichaId, String usuarioId)` — soft delete
- [ ] `@Transactional(readOnly = true)` na classe, `@Transactional` em criar/atualizar/deletar

### 3. FichaController
- [ ] `POST /api/jogos/{jogoId}/fichas` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] `GET /api/jogos/{jogoId}/fichas` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] `GET /api/fichas/{id}` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] `PUT /api/fichas/{id}` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] `DELETE /api/fichas/{id}` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")

## Arquivos afetados
- `repository/FichaRepository.java` (NOVO)
- `service/FichaService.java` (NOVO)
- `controller/FichaController.java` (NOVO)

## Verificações de aceitação
- [ ] POST /fichas retorna 201 com FichaResponse
- [ ] Sub-registros são criados ao criar Ficha
- [ ] GET lista retorna apenas fichas do jogador logado (para Jogador)
- [ ] Mestre vê todas as fichas do jogo
- [ ] `./mvnw test` passa
