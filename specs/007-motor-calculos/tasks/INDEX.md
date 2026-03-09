# Índice de Tasks — Spec 007 (Motor de Cálculos da Ficha)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-calculo-atributos.md) | Cálculos Core | FichaCalculationService — calcular total e ímpeto dos atributos | 🟡 |
| [P1-T2](./P1-T2-calculo-bonus.md) | Cálculos Core | FichaCalculationService — calcular base e total dos bônus | 🟡 |
| [P1-T3](./P1-T3-calculo-vida-essencia.md) | Cálculos Core | FichaCalculationService — vida total, vida por membro, essência, ameaça | 🟡 |
| [P2-T1](./P2-T1-integracao-save-nivel.md) | Integração | Injetar calculationService no FichaService + nível automático por XP | 🟡 |
| [P3-T1](./P3-T1-validacao-service.md) | Validações | FichaValidationService — pontos, limitadores, classe/raça, pré-requisitos | 🟡 |
| [P4-T1](./P4-T1-preview-endpoint.md) | Preview | FichaPreviewService + POST /api/fichas/{id}/preview | 🟡 |
| [P5-T1](./P5-T1-testes-unitarios.md) | Testes | Testes unitários FichaCalculationService (sem DB) | 🟢 |
| [P5-T2](./P5-T2-testes-integracao.md) | Testes | Testes integração fluxo completo (salvar → verificar calculados) | 🟡 |

**Total**: 8 tasks, ~5-6 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
