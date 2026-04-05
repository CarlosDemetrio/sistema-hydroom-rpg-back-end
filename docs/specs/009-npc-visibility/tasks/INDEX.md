# Tasks — Spec 009-ext: Visibilidade NPC, Prospeccao, Essencia, Reset

> Branch: `feature/009-npc-visibility`
> Base: `feature/009-npc-fichas-mestre`
> Sequencia: T1 → T2 → T3 → T4 → T5 → T6 | T7, T8, T9, T10 (paralelos apos T6)

---

## Visao Geral

| Task | Tipo | Dependencias | Status |
|------|------|-------------|--------|
| [T-QW — Quick Wins: Bugs Criticos de Frontend](T-QW-bugs-frontend-criticos.md) | Frontend | nenhuma | Pendente — **fazer agora** |
| [T1 — Campo visivelGlobalmente em Ficha](T1-visivel-globalmente.md) | Backend | nenhuma | Pendente |
| [T2 — FichaVisibilidade entity + endpoints](T2-ficha-visibilidade.md) | Backend | T1 | Pendente |
| [T3 — ProspeccaoUso entity + endpoints](T3-prospeccao-uso.md) | Backend | nenhuma | Pendente |
| [T4 — Endpoint resetar-estado](T4-resetar-estado.md) | Backend | nenhuma | Pendente |
| [T5 — Verificar essenciaAtual no FichaResumoResponse](T5-essencia-resumo.md) | Backend | nenhuma | Pendente |
| [T6 — Testes de integracao](T6-testes.md) | Backend | T1 a T5 | Pendente |
| [T7 — Frontend toggle de visibilidade NPC](T7-frontend-visibilidade.md) | Frontend | T2 | Pendente |
| [T8 — Frontend barra de essencia reativa](T8-frontend-essencia.md) | Frontend | T5, T-QW Bug 1 | Pendente |
| [T9 — Frontend prospeccao (usar + reverter)](T9-frontend-prospeccao.md) | Frontend | T3 | Pendente |
| [T10 — Frontend painel de reset do Mestre](T10-frontend-reset.md) | Frontend | T4 | Pendente |

---

## Fluxo de Desbloqueio

```
T-QW → independente (pode comecar imediatamente)
T1 desbloqueia T2
T3 desbloqueia T9
T4 desbloqueia T10
T5 desbloqueia T8 (T-QW Bug 1 antecipa parte do T8)
T1+T2+T3+T4+T5 → T6
T6 desbloqueia T7 (confirmacao dos endpoints antes de criar UI)
```

---

## Estimativa de Esforco

| Task | Complexidade | Razao |
|------|-------------|-------|
| T-QW | Baixa (~2h) | 3 bugs: modelo TypeScript, template binding, navegacao — sem novos endpoints |
| T1 | Baixa | Apenas novo campo + migracao + DTO |
| T2 | Alta | Nova entidade + service com logica de idempotencia + 4 endpoints + mudanca em GET /fichas |
| T3 | Alta | Nova entidade + enum + service com maquina de estados + 5 endpoints |
| T4 | Media | Novo endpoint + metodo atomico no service existente |
| T5 | Baixa | Verificar/corrigir DTO de resposta |
| T6 | Alta | 10+ cenarios de teste por funcionalidade |
| T7 | Alta | Componente com multiselect + painel lateral + mobile drawer |
| T8 | Baixa | Substituir valores hardcoded por dados do resumo (T-QW Bug 1 ja prepara o terreno) |
| T9 | Alta | Dois componentes (Jogador + Mestre) com fluxos diferentes |
| T10 | Media | Dialog de confirmacao + toast + chamada ao endpoint |
