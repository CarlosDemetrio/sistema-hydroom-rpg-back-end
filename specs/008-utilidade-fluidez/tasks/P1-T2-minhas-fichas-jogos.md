# P1-T2 — Minhas Fichas + Meus Jogos

**Fase:** 1 — Dashboards
**Complexidade:** 🟢 Baixa
**Depende de:** Spec 005 (participação)
**Bloqueia:** nada

## Objetivo

Endpoints de conveniência para jogador navegar rapidamente pelos seus recursos.

## Checklist

### 1. GET /api/jogos/{id}/fichas/minhas
- [ ] Adicionar endpoint em FichaController
- [ ] Chama FichaService.listarMinhas(jogoId, usuarioId) — filtra por jogadorId + isNpc=false
- [ ] @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")

### 2. GET /api/jogos/meus
- [ ] Adicionar endpoint em JogoController
- [ ] Busca jogos onde: usuário tem participação APROVADA OU é o Mestre (criador) do jogo
- [ ] Retorna List<JogoResumoResponse>: id, nome, isMestre, totalFichas (das próprias fichas)
- [ ] JogoResumoResponse: record com id, nome, isMestre, meusPersonagens (count)

## Arquivos afetados
- `controller/FichaController.java` (MODIFICAR — novo endpoint)
- `controller/JogoController.java` (MODIFICAR — novo endpoint)
- `service/FichaService.java` (MODIFICAR — listarMinhas)
- `service/JogoService.java` (MODIFICAR — listarMeus)
- `dto/response/JogoResumoResponse.java` (NOVO)

## Verificações de aceitação
- [ ] GET /fichas/minhas retorna apenas fichas do usuário logado
- [ ] GET /jogos/meus retorna jogos como Mestre e como Jogador aprovado
- [ ] `./mvnw test` passa
