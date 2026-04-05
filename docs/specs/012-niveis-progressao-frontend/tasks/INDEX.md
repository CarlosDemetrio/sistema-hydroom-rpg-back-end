# Tasks — Spec 012: Níveis, Progressão e Level Up Frontend

> Status geral: Pendente
> Criado em: 2026-04-02
> Spec: `docs/specs/012-niveis-progressao-frontend/spec.md`
> Plan: `docs/specs/012-niveis-progressao-frontend/plan.md`

---

## Resumo das Tasks

| Task | Tipo | Título | Depende de | Prioridade |
|------|------|--------|-----------|-----------|
| T1 | Frontend | Modelo TypeScript + API service para PontosVantagemConfig | — | CRITICO |
| T2 | Frontend | PontosVantagemConfigComponent | T1 | CRITICO |
| T3 | Frontend | CategoriaVantagemConfigComponent com color picker | — | MEDIO |
| T4 | Frontend | Ajustes UX no NiveisConfigComponent | — | BAIXO |
| T5 | Backend | FichaResumoResponse — pontos disponíveis | — | CRITICO |
| T6 | Frontend | Modelo TypeScript FichaResumo — 3 campos de pontos | T5 | CRITICO |
| T7 | Frontend | Painel de XP do Mestre + detecção de level up | T6 | CRITICO |
| T8 | Frontend | LevelUpDialogComponent + Step 1 (atributos) | T7 | CRITICO |
| T9 | Frontend | Step 2 — Distribuição de Aptidões no wizard | T8 | CRITICO |
| T10 | Frontend | Step 3 — Vantagens (informativo) + fechar com confirmação | T9 | ALTO |
| T11 | Frontend | Conectar saldo de vantagens em FichaVantagensTab | T6 | ALTO |
| ~~T12~~ | ~~Backend~~ | ~~Endpoint POST /fichas/{id}/renascer~~ | — | ❌ FORA DO MVP |
| ~~T13~~ | ~~Frontend~~ | ~~UI de Renascimento no FichaDetail~~ | T12 | ❌ FORA DO MVP |
| T14 | Frontend | Rotas + sidebar para PontosVantagem e CategoriaVantagem | T2, T3 | MEDIO |

---

## Sequência de Implementação

```
Fase 1 (sem bloqueadores, paralelo):
  T1 → T4, T5, T3 (independentes)

Fase 2 (depende de T1):
  T2

Fase 3 (depende de T5):
  T6 → T7 → T8 → T9 → T10
               → T11

Fase 4 (depende de T12):
  T13

Fase 5 (depende de T2 e T3):
  T14
```
