# Spec 017 — Tasks Index

> Spec: `017-correcoes-rc`
> Total de tasks: 23 (16 ativas + 7 P3 backlog)
> Status geral: PLANEJADO
> Data: 2026-04-07 (T22 incluida 2026-04-07 apos bug UX reportado pelo PO)

---

## P0 — BLOQUEANTE PRE-RC (8 tasks, ~10h)

| Task | Titulo | Tipo | Dependencias | Agente Sugerido |
|------|--------|------|--------------|-----------------|
| [T1](P0-T1-securityconfig-entrypoint.md) | Backend: SecurityConfig HttpStatusEntryPoint para `/api/**` | Backend | — | java-spring-tech-lead |
| [T2](P0-T2-teste-integracao-401.md) | Backend: Teste integracao 401 vs 302 | Backend | T1 | senior-backend-dev |
| [T3](P0-T3-skip-error-interceptor-token.md) | Frontend: Criar `SKIP_ERROR_INTERCEPTOR` HttpContextToken | Frontend | — | angular-frontend-dev |
| [T4](P0-T4-refactor-error-interceptor.md) | Frontend: Refatorar `error.interceptor.ts` (401/403/0/500) | Frontend | T1, T3 | angular-tech-lead |
| [T5](P0-T5-auth-guard-redirect-url.md) | Frontend: `auth.guard` salva `state.url` + `getUserInfo` skip | Frontend | T3 | angular-frontend-dev |
| [T6](P0-T6-fix-hasbothroles.md) | Frontend: Bug `hasBothRoles()` (`\|\|` → `&&`) | Frontend | — | angular-frontend-dev |
| [T7](P0-T7-fix-verficha-mestre.md) | Frontend: Bug `verFicha()` Mestre + nova rota | Frontend | — | angular-tech-lead |
| [T22](P0-T22-fix-overlay-clipping-dialogs.md) | Frontend: Fix overlay clipping de selects/dropdowns dentro de dialogs | Frontend | — | angular-frontend-dev |

---

## P1 — DESEJAVEL PRE-RC (5 tasks, ~10h)

| Task | Titulo | Tipo | Dependencias | Agente Sugerido |
|------|--------|------|--------------|-----------------|
| [T8](P1-T8-page-header-component.md) | Frontend: Criar `PageHeaderComponent` | Frontend | — | primeng-ux-architect → angular-frontend-dev |
| [T9](P1-T9-aplicar-page-header-mestre.md) | Frontend: Aplicar PageHeader em telas Mestre | Frontend | T8 | angular-frontend-dev |
| [T10](P1-T10-aplicar-page-header-jogador.md) | Frontend: Aplicar PageHeader em telas Jogador/wizard | Frontend | T8 | angular-frontend-dev |
| [T11](P1-T11-jogo-form-toast.md) | Frontend: `jogo-form` adicionar `<p-toast>` | Frontend | — | angular-frontend-dev |
| [T12](P1-T12-remover-double-toast.md) | Frontend: Remover double-toast em 19 componentes | Frontend | T4 | angular-frontend-dev |

---

## P2 — POS-RC (3 tasks, ~4h)

| Task | Titulo | Tipo | Dependencias | Agente Sugerido |
|------|--------|------|--------------|-----------------|
| [T13](P2-T13-oauth-callback-cleanup.md) | Frontend: `oauth-callback` remover `setTimeout(1000)` | Frontend | — | angular-frontend-dev |
| [T14](P2-T14-testes-interceptor.md) | Frontend: Testes do `error.interceptor` (5 branches) | Frontend | T4 | angular-tech-lead |
| [T-DOC1](P2-TDOC1-claudemd-erros-http.md) | Doc: Atualizar CLAUDE.md com contrato erros HTTP | Doc | T4, T12 | angular-tech-lead |

---

## P3 — BACKLOG POS-HOMOLOGACAO (7 tasks, ~16h)

| Task | Titulo | Tipo | Dependencias | Razao do adiamento |
|------|--------|------|--------------|--------------------|
| [T15](P3-T15-reativar-sidebar.md) | Frontend: Reativar `SidebarComponent` no `MainLayoutComponent` | Frontend | — | Decisao de produto |
| [T16](P3-T16-seletor-jogo-header.md) | Frontend: Seletor de jogo no `HeaderComponent` | Frontend | — | Componente novo, ~4h |
| [T17](P3-T17-refactor-error-handler-service.md) | Frontend: Refactor/remover `ErrorHandlerService` | Frontend | T4 | Wrapper raso, sem urgencia |
| [T18](P3-T18-export-import-config.md) | Frontend: Botoes Exportar/Importar `config-layout` | Frontend | — | Depende de discussao do contrato |
| [T19](P3-T19-renomear-drawervisible.md) | Frontend: Renomear `drawerVisible` → `dialogVisible` | Frontend | — | Divida nominal |
| [T20](P3-T20-racas-config-loading-states.md) | Frontend: `racas-config` loading em chamadas auxiliares | Frontend | — | Bug menor |
| [T21](P3-T21-confirm-delete-base-config.md) | Frontend: Auditar `confirmDelete` sem dialog | Frontend | — | Verificacao defensiva |

---

## Grafo de dependencias (P0 + P1 + P2)

```
T1 (BE SecurityConfig)
 └── T2 (BE teste 401)
 └── T4 (FE refactor interceptor)
      ├── T12 (remover double-toast)
      ├── T14 (testes interceptor)
      └── T-DOC1 (doc CLAUDE.md)

T3 (token SKIP)
 ├── T4 (refactor interceptor)
 └── T5 (auth.guard + getUserInfo)

T6 (hasBothRoles)        — independente
T7 (verFicha Mestre)     — independente
T22 (overlay clipping)   — independente (config global em app.config.ts)

T8 (PageHeaderComponent)
 ├── T9 (aplicar Mestre)
 └── T10 (aplicar Jogador)

T11 (jogo-form toast)    — independente
T13 (oauth-callback)     — independente
```

---

## Resumo por tipo

| Tipo | Tasks | Total |
|------|-------|-------|
| Backend | T1, T2 | 2 |
| Frontend | T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T22, T15-T21 | 20 |
| Documentacao | T-DOC1 | 1 |
| **Total ativo (P0+P1+P2)** | | **16** |
| **Total backlog (P3)** | | **7** |

---

## Pontos Em Aberto

- **PA-017-01**: Para `verFicha()` do Mestre — criar rota nova ou adaptar guard? (afeta T7)
- **PA-017-02**: `PageHeaderComponent` com `BreadcrumbModule` ou simples? (afeta T8)
- **PA-017-03**: Reativar sidebar (T15) e `PageHeaderComponent` coexistem ou um substitui o outro? (afeta P3)
- **PA-017-04**: Contrato Exportar/Importar (T18) — CSV ou JSON? (afeta P3)

PA-017-01 deve ser resolvido antes do inicio de T7.
PA-017-02 deve ser resolvido antes do inicio de T8.

---

*Index produzido por: PM/Scrum Master | 2026-04-07*
