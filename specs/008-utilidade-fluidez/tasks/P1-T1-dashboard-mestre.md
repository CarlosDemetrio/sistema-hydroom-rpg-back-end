# P1-T1 — Dashboard do Mestre

**Fase:** 1 — Dashboards
**Complexidade:** 🟡 Média
**Depende de:** Spec 006 + 007
**Bloqueia:** nada

## Objetivo

Endpoint de resumo do jogo para o Mestre acompanhar o estado das fichas e participantes.

## Checklist

### 1. DashboardMestreResponse DTO
- [ ] Record: `totalFichas (int)`, `totalParticipantes (int)`, `fichasPorNivel (Map<Integer,Long>)`, `ultimasAlteracoes (List<FichaAlteracaoResumo>)`
- [ ] Record interno `FichaAlteracaoResumo`: fichaId, nome, dataUltimaAlteracao

### 2. DashboardService
- [ ] `getDashboardMestre(Long jogoId)` → DashboardMestreResponse:
  - `totalFichas`: FichaRepository.countByJogoIdAndIsNpcFalse(jogoId)
  - `totalParticipantes`: JogoParticipanteRepository.countByJogoIdAndStatus(jogoId, APROVADO)
  - `fichasPorNivel`: query com GROUP BY nivel e COUNT (FichaRepository custom query)
  - `ultimasAlteracoes`: findTop5ByJogoIdOrderByUpdatedAtDesc (fichas de jogadores)

### 3. Controller
- [ ] `GET /api/jogos/{id}/dashboard`
- [ ] `@PreAuthorize("hasRole('MESTRE')")`
- [ ] Validar que usuário é Mestre do jogo (não apenas role MESTRE)

## Arquivos afetados
- `dto/response/DashboardMestreResponse.java` (NOVO)
- `service/DashboardService.java` (NOVO)
- `controller/DashboardController.java` (NOVO)

## Verificações de aceitação
- [ ] GET /dashboard retorna dados corretos
- [ ] Jogador recebe 403
- [ ] fichasPorNivel agrupa corretamente
- [ ] `./mvnw test` passa
