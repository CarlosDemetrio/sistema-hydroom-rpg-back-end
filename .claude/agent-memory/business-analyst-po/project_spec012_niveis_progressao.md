---
name: Spec 012 — Níveis, Progressão e Level Up Frontend
description: Estado e decisões da Spec 012 — PontosVantagemConfig zero cobertura, level up automático, FichaResumoResponse gaps, renascimento pendente de PO
type: project
---

Spec 012 criada em 2026-04-02. 14 tasks em docs/specs/012-niveis-progressao-frontend/.

**Why:** O sistema de progressão tem backend 100% implementado (NivelConfig, PontosVantagemConfig, CategoriaVantagem) mas o frontend está incompleto: PontosVantagem sem nenhuma cobertura, level up sem UI, saldo de vantagens mockado como 0.

**How to apply:** Ao trabalhar em features de ficha ou nível, consultar esta spec para entender o escopo e dependências.

## Estado atual do frontend (auditado 2026-04-02)

- `NiveisConfigComponent`: funcional mas sem coluna permitirRenascimento na tabela, sem formatação de milhar, sem validação de consistência
- `PontosVantagemConfig`: ZERO cobertura (sem model, service, component, rota)
- `CategoriaVantagem`: API service existe; sem CRUD dedicado (apenas select em VantagensConfig)
- `FichaResumoResponse` (backend): sem pontosAtributoDisponiveis/pontosAptidaoDisponiveis/pontosVantagemDisponiveis
- `pontosVantagemRestantes` em FichaVantagensTab: mockado como 0
- Level up detection: inexistente

## Pontos em aberto críticos

- **P-01:** Comportamento do renascimento (nível vai para 0 ou 1? XP vai para 0 ou xpNecessaria do nível 1? Atributos/aptidões são resetados?) — aguarda PO antes de T12
- **P-02:** RESOLVIDO 2026-04-03 (Q14/Q15). Fontes de pontos MVP = apenas NivelConfig. pontosAtributoGastos = SUM(FichaAtributo.nivel); pontosAptidaoGastos = SUM(FichaAptidao.base). Classe/Raça não têm campos de pontos extras (GAP-PONTOS-CONFIG, pós-MVP).
- **P-03:** PontosVantagem na rota própria ou integrado em NivelConfig? — decisão de UX necessária antes de T2/T14

## Regras de negócio confirmadas

- Level up é AUTOMÁTICO — backend calcula nivel = MAX(NivelConfig.nivel WHERE xpNecessaria <= fichaXp)
- Pontos acumulam (saldo = ganhos totais - gastos totais); gastos são irreversíveis
- XP é read-only para jogador (MESTRE concede via PUT /fichas/{id} com campo xp)
- FichaVantagem nunca pode ser removida — nivel só sobe
- Renascimento: apenas em níveis com permitirRenascimento=true (padrão: 31-35)
- Step 3 do wizard é informativo — pontos de vantagem são gastos na aba Vantagens, não no wizard

## Dependências

T5 (backend FichaResumoResponse) bloqueia T6, T7, T8, T9, T10, T11
T12 (backend renascer endpoint) bloqueia T13
T1 bloqueia T2; T2 e T3 bloqueiam T14
