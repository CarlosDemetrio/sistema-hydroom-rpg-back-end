# Spec 005 — Indice de Tasks: Gestao de Participantes

> Total: 6 tasks | Backend: 3 | Frontend: 3
> Prerequisito: branch `feature/009-npc-fichas-mestre` mergeado ou cherry-picked

---

## Fase 1 — Backend

| ID | Titulo | Complexidade | Depende de | Bloqueia |
|----|--------|-------------|-----------|---------|
| [P1-T1](./P1-T1-corrigir-resolicitar.md) | Corrigir logica de re-solicitacao e constraint | 🔴 grande | — | P1-T2, P1-T3 |
| [P1-T2](./P1-T2-endpoints-faltantes.md) | Adicionar endpoints faltantes (banir, desbanir, remover, meu-status, cancelar, filtro) | 🟡 media | P1-T1 | P1-T3, Fase 2 |
| [P1-T3](./P1-T3-testes-integracao.md) | Testes de integracao completos para todos os cenarios da state machine | 🟡 media | P1-T1, P1-T2 | Fase 2 |

## Fase 2 — Frontend

| ID | Titulo | Complexidade | Depende de | Bloqueia |
|----|--------|-------------|-----------|---------|
| [P2-T1](./P2-T1-api-business-service.md) | Alinhar API service e Business service com novos endpoints | 🟢 pequena | P1-T2 | P2-T2, P2-T3 |
| [P2-T2](./P2-T2-jogo-detail-mestre.md) | Corrigir JogoDetail do Mestre (semantica remover/banir/desbanir + filtro + badge pendentes) | 🟡 media | P2-T1 | — |
| [P2-T3](./P2-T3-jogos-disponiveis-jogador.md) | Completar JogosDisponiveis do Jogador (solicitar, status, cancelar) | 🟡 media | P2-T1 | — |

---

## Criterios de Conclusao da Spec 005

- [ ] Todos os 9 endpoints da spec existem e respondem corretamente
- [ ] Re-solicitacao apos REJEITADO funciona sem erro 409
- [ ] Re-solicitacao apos REMOVIDO (soft delete) funciona
- [ ] BANIDO recebe 409 ao tentar solicitar
- [ ] Desbanir transiciona direto para APROVADO
- [ ] Testes de integracao passando (meta: +12 testes, total esperado ~469)
- [ ] Frontend: Mestre consegue banir, desbanir e remover com semanticas corretas
- [ ] Frontend: Jogador consegue ver status e cancelar solicitacao PENDENTE
- [ ] Sem regressao nos 457 testes existentes
