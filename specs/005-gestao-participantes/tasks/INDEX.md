# Índice de Tasks — Spec 005 (Gestão de Participantes)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-participante-status-entity.md) | Entity/DTOs | StatusParticipante enum + campos + repository queries + DTOs | 🟡 |
| [P2-T1](./P2-T1-participante-service-controller.md) | Service/Controller | JogoParticipanteService + JogoParticipanteController (todos endpoints) | 🔴 |
| [P3-T1](./P3-T1-security-service.md) | Security | ParticipanteSecurityService + integração nos controllers | 🟡 |
| [P4-T1](./P4-T1-testes-integracao.md) | Testes | Testes de integração completos do fluxo | 🟡 |

**Total**: 4 tasks, ~3-4 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
