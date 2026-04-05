# Spec 016 — Status dos BAs (Business Analysts)

> Tracking de todos os BAs alocados para a Spec 016 (Sistema de Itens/Equipamentos).
> Criado: 2026-04-04 | PM: Scrum Orchestrator

---

## Quadro de BAs

| ID | BA | Escopo | Sub-Dominio | Status | Artefatos Produzidos | Notas |
|----|-----|--------|-------------|--------|---------------------|-------|
| BA-016-01 | BA Spec Principal | Spec completa: spec.md, plan.md, tasks/INDEX.md, tasks individuais | SD-1 + SD-2 + SD-3 (geral) | **CONCLUIDO** | spec.md, plan.md, 10 tasks, api-contracts, regras | Entregue. |
| BA-016-02 | BA Dataset D&D 5e | Pesquisa D&D 5e SRD, catalogo de ~40 itens, mapeamento para model, defaults por classe | SD-3 (Dataset) | **CONCLUIDO** | `dataset/dataset-itens-srd.md` (481 linhas, 40 itens, 7 raridades, 20 tipos) | Entregue 2026-04-04. 4 pontos pendentes PO: PA-016-DS-01..04. |
| BA-UX-016 | BA/UX Designer | Wireframes e design specs para: (1) tela config catalogo, (2) aba inventario na ficha, (3) equipar/desequipar flow | Transversal (frontend) | **CONCLUIDO** | `ux/UX-EQUIPAMENTOS.md` (1581 linhas) | Entregue. |

---

## Sequencia e Dependencias entre BAs

```
BA-016-01 (Spec Principal)     BA-UX-016 (UX Design)
  |  CONCLUIDO                    |  CONCLUIDO
  |                               |
  v (entregou models e entities)  v (entregou wireframes)
BA-016-02 (Dataset)
  |  CONCLUIDO (2026-04-04)
  |
  v
  Entrega: dataset/dataset-itens-srd.md (40 itens)
  Pontos pendentes PO: PA-016-DS-01..04
```

### Regras de Lancamento

1. **BA-016-01** ja foi lancado e esta trabalhando. **NAO interromper.**
2. **BA-016-02** deve ser lancado APOS BA-016-01 produzir pelo menos `spec.md` com as entities definidas (RaridadeItemConfig, TipoItemConfig, ItemConfig). A pesquisa SRD pode iniciar antes.
3. **BA-UX-016** ja foi lancado e trabalha de forma independente. Deve considerar as perguntas em aberto (Q-016-01 a Q-016-06) no design.

---

## Criterios de Aceitacao por BA

### BA-016-01 — Spec Principal
- [ ] `spec.md` com todas as entities detalhadas (campos, tipos, constraints, relacionamentos)
- [ ] `plan.md` com fases de implementacao e dependencias
- [ ] `tasks/INDEX.md` com tabela de todas as tasks numeradas
- [ ] Tasks individuais em `tasks/` com criterios de aceitacao e estimativas
- [ ] Secao de "Decisoes do PO Necessarias" listando Q-016-01 a Q-016-06

### BA-016-02 — Dataset D&D 5e
- [ ] `dataset/CATALOGO-ITENS.md` com lista de ~40 itens adaptados do SRD
- [ ] Mapeamento de cada item para os campos de ItemConfig (nome, tipo, raridade, peso, valor, durabilidade, nivel minimo)
- [ ] `dataset/DEFAULTS-CLASSE.md` com equipamentos iniciais por classe
- [ ] `dataset/DEFAULTS-RARIDADE-TIPO.md` com raridades e tipos hierarquicos default

### BA-UX-016 — UX Design
- [ ] Wireframe da tela de configuracao de catalogo (RaridadeItem + TipoItem + ItemConfig)
- [ ] Wireframe da aba "Inventario" na FichaDetail
- [ ] Fluxo de equipar/desequipar item
- [ ] Integracao visual com o wizard (ClasseEquipamentoInicial)

---

## Historico de Eventos

| Data | Evento | Detalhes |
|------|--------|---------|
| 2026-04-04 | BA-016-01 lancado | Especificacao principal da Spec 016 iniciada |
| 2026-04-04 | BA-UX-016 lancado | Design de telas de equipamentos iniciado |
| 2026-04-04 | Documento de coordenacao criado | COORDENACAO-MULTI-BA.md e BA-STATUS.md criados pelo PM |
| 2026-04-04 | BA-016-01 CONCLUIDO | spec.md, plan.md, 10 tasks, api-contracts, regras — todos entregues |
| 2026-04-04 | BA-UX-016 CONCLUIDO | UX-EQUIPAMENTOS.md (1581 linhas) entregue |
| 2026-04-04 | BA-016-02 CONCLUIDO | Dataset D&D 5e SRD (481 linhas, 40 itens). 4 pontos pendentes PO. |
| 2026-04-04 | T11 criada | P3-T11-ui-inventario-ficha.md (799 linhas) — task faltante do INDEX.md |
| 2026-04-04 | **SPEC 016 100% ESPECIFICADA** | Todos os 3 BAs concluidos, 11 tasks criadas, dataset pronto |

---

*Atualizar este documento sempre que um BA mudar de status ou entregar artefatos.*
