# Tasks — Spec 004: Siglas, Fórmulas e Relacionamentos

## Ordem de execução

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7
```

- **Phase 1 bloqueia Phase 2** (validação de fórmulas usa siglas do jogo)
- **Phase 3 bloqueia Phase 4** (VantagemConfig precisa da FK de CategoriaVantagem antes dos pré-requisitos)
- **Phases 5 e 6 são independentes entre si**

---

## Phase 1 — Sistema de Siglas

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P1-T1 | [P1-T1-campos-sigla.md](P1-T1-campos-sigla.md) | 🟢 Baixa | — |
| P1-T2 | [P1-T2-repositorios.md](P1-T2-repositorios.md) | 🟢 Baixa | P1-T1 |
| P1-T3 | [P1-T3-sigla-validation-service.md](P1-T3-sigla-validation-service.md) | 🔴 Alta | P1-T2 |
| P1-T4 | [P1-T4-integracao-services.md](P1-T4-integracao-services.md) | 🟡 Média | P1-T3 |
| P1-T5 | [P1-T5-sigla-controller.md](P1-T5-sigla-controller.md) | 🟢 Baixa | P1-T3 |

## Phase 2 — Validação de Fórmulas

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P2-T1 | [P2-T1-formula-evaluator.md](P2-T1-formula-evaluator.md) | 🔴 Alta | Phase 1 (siglas) |
| P2-T2 | [P2-T2-formula-validation-services.md](P2-T2-formula-validation-services.md) | 🟡 Média | P2-T1 |
| P2-T3 | [P2-T3-formula-preview-endpoint.md](P2-T3-formula-preview-endpoint.md) | 🟡 Média | P2-T1 |

## Phase 3 — CategoriaVantagem e PontosVantagemConfig

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P3-T1 | [P3-T1-categoria-vantagem-crud.md](P3-T1-categoria-vantagem-crud.md) | 🟡 Média | Phase 2 |
| P3-T2 | [P3-T2-pontos-vantagem-crud.md](P3-T2-pontos-vantagem-crud.md) | 🟡 Média | Phase 2 |
| P3-T3 | [P3-T3-vantagem-categoria-fk.md](P3-T3-vantagem-categoria-fk.md) | 🟢 Baixa | P3-T1 |

## Phase 4 — VantagemPreRequisito

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P4-T1 | [P4-T1-prerequisito-entity.md](P4-T1-prerequisito-entity.md) | 🟢 Baixa | P3-T3 |
| P4-T2 | [P4-T2-cycle-detection.md](P4-T2-cycle-detection.md) | 🔴 Alta | P4-T1 |
| P4-T3 | [P4-T3-vantagem-crud-update.md](P4-T3-vantagem-crud-update.md) | 🟡 Média | P4-T2 |

## Phase 5 — ClasseBonus e ClasseAptidaoBonus

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P5-T1 | [P5-T1-classe-bonus-entities.md](P5-T1-classe-bonus-entities.md) | 🟢 Baixa | Phase 3 |
| P5-T2 | [P5-T2-classe-personagem-update.md](P5-T2-classe-personagem-update.md) | 🟡 Média | P5-T1 |

## Phase 6 — RacaClassePermitida

| Task | Arquivo | Complexidade | Depende de |
|------|---------|-------------|-----------|
| P6-T1 | [P6-T1-raca-classe-permitida.md](P6-T1-raca-classe-permitida.md) | 🟡 Média | Phase 5 |

## Phase 7 — Verificação Final

Ver checklist no `plan.md` — rodar `./mvnw test` + grepping de anti-patterns.
