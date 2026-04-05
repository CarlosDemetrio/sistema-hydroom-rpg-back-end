# Spec 008 — Indice de Tasks: Sub-recursos de Classes e Racas

> Total: 4 tasks | Frontend only (backend ja implementado)
> Prerequisito: branch `feature/009-npc-fichas-mestre` mergeado ou cherry-picked

---

## Sumario

| ID | Titulo | Complexidade | Depende de | Bloqueia |
|----|--------|-------------|-----------|---------|
| [T1](./T1-tipagem-modelos-api-service.md) | Corrigir tipagem: modelos TypeScript + metodos do API service | pequena | — | T2, T3, T4 |
| [T2](./T2-classes-config-valorpornivel-bonus.md) | ClassesConfigComponent: campo valorPorNivel na aba Bônus + campo bonus na aba Aptidoes | media | T1 | T4 |
| [T3](./T3-racas-config-badge-penalidade.md) | RacasConfigComponent: badge de restricao na tabela + indicador textual de penalidade | pequena | T1 | T4 |
| [T4](./T4-testes.md) | Testes: API service sub-recursos + componentes | media | T1, T2, T3 | — |

---

## Criterios de Conclusao da Spec 008

- [ ] `ClasseBonusConfig` tem campo `valorPorNivel: number`
- [ ] `ClasseAptidaoBonus` tem campo `bonus: number`
- [ ] Todos os metodos de sub-recurso no `ConfigApiService` tem tipagem concreta (sem `unknown`)
- [ ] `addClasseBonus` envia `{ bonusConfigId, valorPorNivel }` ao backend
- [ ] `addClasseAptidaoBonus` envia `{ aptidaoConfigId, bonus }` ao backend
- [ ] Mestre consegue adicionar ClasseBonus com valorPorNivel decimal (ex: 0.5) via UI
- [ ] Mestre consegue adicionar ClasseAptidaoBonus com bonus inteiro >= 0 via UI
- [ ] Valores negativos de RacaBonusAtributo exibem label "(penalidade)" alem da cor vermelha
- [ ] Badge de restricao de classe na tabela de racas funciona (vazio = "Sem restricoes", com itens = "X classes")
- [ ] Zero regressao nos testes Vitest existentes (baseline: 271 testes)
- [ ] Novos testes cobrem os metodos de API service de sub-recurso
