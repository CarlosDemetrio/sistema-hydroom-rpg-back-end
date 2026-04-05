# Spec 016 — Sistema de Itens e Equipamentos: INDEX de Tasks

> Spec: `016-sistema-itens`
> Status: PLANEJADO — implementacao PENDENTE
> Total: 11 tasks (7 backend + 4 frontend)
> Estimativa: ~20-24 dias
> Depende de: Spec 007 (motor corrigido), Spec 006 (ficha funcional)

---

## Fase 1 — Backend

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| P1-T1 | [P1-T1-raridade-tipo-config.md](P1-T1-raridade-tipo-config.md) | RaridadeItemConfig + TipoItemConfig: entidades, CRUDs, testes | 1-2 dias | Spec 007 T0 concluido | PENDENTE |
| P1-T2 | [P1-T2-item-config.md](P1-T2-item-config.md) | ItemConfig + ItemEfeito + ItemRequisito: entidades, CRUDs, sub-recursos | 2-3 dias | T1 | PENDENTE |
| P1-T3 | [P1-T3-classe-equipamento-inicial.md](P1-T3-classe-equipamento-inicial.md) | ClasseEquipamentoInicial: sub-recurso de ClassePersonagem | 1 dia | T2 | PENDENTE |
| P1-T4 | [P1-T4-ficha-item.md](P1-T4-ficha-item.md) | FichaItem entity + endpoints (add, equipar, remover, durabilidade, listar) | 2-3 dias | T1, T2 | PENDENTE |
| P1-T5 | [P1-T5-calculo-itens.md](P1-T5-calculo-itens.md) | FichaCalculationService: Passo 6 (ItemEfeito de itens equipados) + campos `itens` nas entidades | 2 dias | T4, Spec 007 T0 | PENDENTE |
| P2-T6 | [P2-T6-default-dataset.md](P2-T6-default-dataset.md) | DefaultGameConfigProviderImpl: 7 raridades + 20 tipos + 40 itens + equipamentos iniciais | 1-2 dias | T1, T2, T3 | PENDENTE |
| P1-T7 | [P1-T7-testes.md](P1-T7-testes.md) | Testes de integracao completos para todas as entidades e FichaCalculationService | 2-3 dias | T1-T6 | PENDENTE |

---

## Fase 3 — Frontend

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| P3-T8 | [P3-T8-ui-raridade-tipo.md](P3-T8-ui-raridade-tipo.md) | UI: CRUD de Raridades e Tipos de Item | 1 dia | T1 (backend) | PENDENTE |
| P3-T9 | [P3-T9-ui-item-config.md](P3-T9-ui-item-config.md) | UI: Catalogo de Itens (ItemConfig + ItemEfeito + ItemRequisito) | 3 dias | T2, T8 | PENDENTE |
| P3-T10 | [P3-T10-ui-classe-equip-inicial.md](P3-T10-ui-classe-equip-inicial.md) | UI: ClasseEquipamentoInicial na tela de ClassePersonagem | 1 dia | T9 | PENDENTE |
| P3-T11 | [P3-T11-ui-inventario-ficha.md](P3-T11-ui-inventario-ficha.md) | UI: Aba de Inventario/Equipamentos na FichaDetail | 3 dias | T4, T8, T9 | PENDENTE |

---

## Dependencias Visuais

```
[T1: Raridade+Tipo] ──> [T2: ItemConfig] ──> [T3: ClasseEquipamentoInicial]
        |                       |
        v                       v
[T4: FichaItem entity] ←────────┘
        |
        v
[T5: Calculo itens] ← depende de Spec 007 T0 (motor corrigido)

[T6: Dataset] ← depende de T1+T2+T3 (pode ser paralelo a T4+T5)

[T7: Testes] ← depende de TODOS T1-T6

Frontend:
[T8: UI Raridade+Tipo] ──> [T9: UI Catalogo] ──> [T10: UI ClasseEquip]
         |                          |
         └──────────────────────────┘
                        v
              [T11: UI Inventario Ficha]
```

---

## Decisoes Pendentes (resolucoes antes da implementacao)

| ID | Pergunta | Bloqueia |
|----|----------|---------|
| PA-016-01 | Penalidade de sobrecarga e MVP? | T4, T11 |
| PA-016-02 | Passo de equipamentos no wizard de criacao? | T7 (integracao 006), T11 |
| PA-016-03 | FichaAptidao precisa de campo `itens`? | T5 |
| PA-016-04 | Item customizado com efeitos automaticos no MVP? | T4, T11 |
| PA-016-05 | Municao stackeia automaticamente? | T4 |
| PA-016-06 | Jogador ve catalogo completo do jogo? | T11 |

---

*Produzido por: Business Analyst/PO | 2026-04-04*
