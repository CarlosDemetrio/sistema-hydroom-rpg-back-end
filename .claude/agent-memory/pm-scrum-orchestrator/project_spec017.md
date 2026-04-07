---
name: Spec 017 Correcoes Pre-RC
description: Spec corretiva criada 2026-04-07 consolidando auditorias tech-lead (rotas/erros) e UX architect — 22 tasks em 4 fases
type: project
---

Spec 017 (`docs/specs/017-correcoes-rc/`) criada em 2026-04-07 como consolidacao de duas auditorias:
- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` (angular-tech-lead) — 8 problemas, principal e SecurityConfig sem HttpStatusEntryPoint devolvendo 302 em vez de 401
- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` (primeng-ux-architect) — 8 telas sem botao Voltar, `SidebarComponent` inativo, bugs `hasBothRoles`/`verFicha`/`jogo-form` toast

Estrutura: 22 tasks em 4 fases (P0-P3), 15 ativas + 7 P3 backlog.

**Why:** PO reportou "erro interno quando expira sessao" e "falta botao voltar nas telas". Auditorias identificaram causa-raiz unica no backend (SecurityConfig) cascateando para 4 sintomas frontend, alem de 8+ bugs UX espalhados.

**How to apply:**
- **Spec 017 P0 (7 tasks, ~8h) e BLOQUEANTE PRE-RC** — T1/T2 backend (SecurityConfig + teste), T3-T5 frontend (token SKIP + interceptor + guard), T6/T7 bugs logicos (hasBothRoles, verFicha Mestre)
- P1 (T8-T12) DESEJAVEL PRE-RC — PageHeaderComponent + aplicacao em 8 telas + jogo-form toast + double-toast cleanup
- P2 (T13, T14, T-DOC1) POS-RC — oauth-callback cleanup, testes interceptor, doc
- P3 (T15-T21) POS-HOMOLOGACAO — sidebar, seletor jogo, refactor ErrorHandlerService, etc.

**Coordenacao critica:**
- Spec 017 T10 (PageHeader em ficha-detail) deve rodar APOS Spec 012 T11 (mesmo arquivo) — conflito de merge certo se paralelizadas
- Spec 017 NAO duplica BUG-DC-06..08 — ja estao em Spec 015 T5
- Pontos em aberto: PA-017-01 (rota nova vs guard adapt para verFicha), PA-017-02 (Breadcrumb no PageHeader?), PA-017-03 (sidebar ou PageHeader?), PA-017-04 (formato export/import)

Sequenciamento sugerido das rodadas:
- Rodada 14: Spec 012 T6-T8 + Spec 017 T1/T2/T6/T7
- Rodada 15: Spec 012 T9-T11 + Spec 017 T3/T4/T5
- Rodada 16: Spec 015 T6/T7 + Spec 017 T8/T9
- Rodada 17 (RC): Spec 017 T10/T11/T12
