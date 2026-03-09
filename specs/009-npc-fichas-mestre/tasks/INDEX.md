# Índice de Tasks — Spec 009 (NPC e Fichas do Mestre)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-ficha-isnpc-flag.md) | Entity | isNpc + jogadorId nullable na Ficha (verificar/adicionar) | 🟢 |
| [P2-T1](./P2-T1-npc-controller.md) | NPC CRUD | NpcController + FichaService adaptações para NPCs | 🟡 |
| [P3-T1](./P3-T1-duplicacao-ficha.md) | Duplicação | FichaDuplicacaoService + POST /api/fichas/{id}/duplicar | 🟡 |
| [P4-T1](./P4-T1-testes-npc.md) | Testes | Testes de integração NPC CRUD | 🟢 |
| [P4-T2](./P4-T2-testes-duplicacao.md) | Testes | Testes de integração duplicação de ficha | 🟢 |

**Total**: 5 tasks, ~3-4 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
