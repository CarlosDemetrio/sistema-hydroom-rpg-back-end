# Índice de Tasks — Spec 008 (Utilidade e Fluidez)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-dashboard-mestre.md) | Dashboards | DashboardService + GET /api/jogos/{id}/dashboard | 🟡 |
| [P1-T2](./P1-T2-minhas-fichas-jogos.md) | Dashboards | GET /fichas/minhas + GET /jogos/meus | 🟢 |
| [P2-T1](./P2-T1-filtros-configs.md) | Filtros | Filtro ?nome= nos 13 endpoints de configuração | 🟢 |
| [P2-T2](./P2-T2-filtros-fichas.md) | Filtros | Filtros ?nome= ?classeId= ?racaId= ?nivel= nas fichas | 🟢 |
| [P3-T1](./P3-T1-reordenacao-batch.md) | Reordenação | ReordenacaoService + endpoints para 13 tipos de config | 🟡 |
| [P4-T1](./P4-T1-duplicacao-jogo.md) | Duplicação | JogoDuplicacaoService + POST /api/jogos/{id}/duplicar | 🔴 |
| [P5-T1](./P5-T1-export-import.md) | Export/Import | ConfigExportImportService + endpoints export + import | 🔴 |
| [P6-T1](./P6-T1-resumo-ficha.md) | Resumo | GET /api/fichas/{id}/resumo + FichaResumoResponse | 🟢 |
| [P7-T1](./P7-T1-testes-duplicacao.md) | Testes | Testes de integração para duplicação de jogo | 🟡 |
| [P7-T2](./P7-T2-testes-export-import.md) | Testes | Testes de integração para export/import | 🟡 |

**Total**: 10 tasks, ~6-7 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
