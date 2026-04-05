# Spec 015 — Tasks Index

> Spec: `015-config-pontos-classe-raca`
> Total de tasks: 7
> Status geral: Nao iniciado

---

## Fase Backend Core (T1–T4)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T1](P1-T1-entidades-config-pontos.md) | Entidades, Repositories, DTOs e Mappers (4 novas entidades) | — | Pendente |
| [T2](P1-T2-crud-endpoints.md) | CRUD Endpoints sub-recurso (14 endpoints) + Testes de integracao | T1 | Pendente |
| [T3](P1-T3-calculo-pontos-disponiveis.md) | Calculo de pontosDisponiveis com 3 fontes | T1 | Pendente |
| [T4](P1-T4-auto-vantagens-level.md) | Auto-concessao de vantagens pre-definidas na criacao e level up | T1 | Pendente |

## Fase Backend Infra (T5)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T5](P2-T5-corrigir-default-provider.md) | Corrigir DefaultProvider (BUG-DC-02..09) + adicionar defaults ausentes | — (independente) | Pendente |

## Fase Frontend (T6–T7)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T6](P2-T6-ui-classe-pontos.md) | UI ClassePersonagem — abas Pontos por Nivel e Vantagens Pre-definidas | T2 | Pendente |
| [T7](P2-T7-ui-raca-pontos.md) | UI Raca — abas Pontos por Nivel e Vantagens Pre-definidas | T2 | Pendente |

---

## Grafo de dependencias

```
T1 (entidades + repos + DTOs + mappers)
 ├── T2 (CRUD endpoints + testes)
 │    ├── T6 (frontend Classe)
 │    └── T7 (frontend Raca)
 ├── T3 (calculo pontos disponiveis)
 └── T4 (auto-concessao vantagens)

T5 (DefaultProvider) — INDEPENDENTE, paralelizavel com T1-T4
```

---

## Resumo por tipo

| Tipo | Tasks | Total |
|------|-------|-------|
| Backend | T1, T2, T3, T4, T5 | 5 |
| Frontend | T6, T7 | 2 |
| **Total** | | **7** |

---

## Pontos em Aberto (confirmar antes de iniciar as tasks indicadas)

- **PA-015-01:** Dados default de ClassePontosConfig por classe (afeta T5)
- **PA-015-02:** Dados default de RacaPontosConfig por raca (afeta T5)
- **PA-015-03:** Quais vantagens pre-definidas por classe/raca no default? (afeta T5)
- **PA-015-04:** Campo `origem` em FichaVantagem: enum JOGADOR/MESTRE/SISTEMA (afeta T4)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
