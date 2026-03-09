# P2-T1 — NpcController + FichaService Adaptações

**Fase:** 2 — NPC CRUD
**Complexidade:** 🟡 Média
**Depende de:** P1-T1
**Bloqueia:** P4-T1

## Objetivo

Expor CRUD de NPCs via NpcController, reutilizando FichaService internamente.

## Checklist

### 1. NpcCreateRequest DTO

- [ ] Record com os mesmos campos de FichaCreateRequest EXCETO jogadorId (sempre null para NPC)
- [ ] Pode ser um alias/wrapper ou record próprio

### 2. FichaService — adaptações

- [ ] `criarNpc(Long jogoId, NpcCreateRequest, String mestreId)` → Ficha:
  - Igual a criar(), mas seta isNpc=true e jogadorId=null
  - Não valida participação de jogador (Mestre não precisa ser "participante")
- [ ] `listarNpcs(Long jogoId, String mestreId)` → List:
  - FichaRepository.findByJogoIdAndIsNpcTrue(jogoId)
  - Validar que mestreId é Mestre do jogo

### 3. NpcController

- [ ] `POST /api/jogos/{jogoId}/npcs` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `GET /api/jogos/{jogoId}/npcs` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `GET /api/npcs/{id}` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `PUT /api/npcs/{id}` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `DELETE /api/npcs/{id}` — @PreAuthorize("hasRole('MESTRE')")

### 4. Garantir separação

- [ ] GET /api/jogos/{id}/fichas (FichaController) NÃO retorna NPCs (isNpc=false)
- [ ] GET /api/jogos/{id}/npcs NÃO retorna fichas de jogadores (isNpc=true)

## Arquivos afetados

- `dto/request/NpcCreateRequest.java` (NOVO)
- `service/FichaService.java` (MODIFICAR — criarNpc, listarNpcs)
- `controller/NpcController.java` (NOVO)

## Verificações de aceitação

- [ ] POST /npcs retorna 201 com isNpc=true e jogadorId=null
- [ ] GET /fichas não inclui NPCs
- [ ] GET /npcs não inclui fichas de jogadores
- [ ] Jogador recebe 403 ao acessar qualquer endpoint de NPC
- [ ] `./mvnw test` passa
