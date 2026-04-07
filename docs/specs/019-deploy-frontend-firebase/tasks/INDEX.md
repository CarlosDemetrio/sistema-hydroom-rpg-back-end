# Spec 019 — Deploy Frontend Firebase: INDEX de Tasks

> Spec: `019-deploy-frontend-firebase`
> Status: PLANEJADO — implementacao PENDENTE
> Total: 4 tasks (2 config + 1 CI/CD + 1 validacao)
> Estimativa: ~2-3 dias
> Depende de: Spec 018 (backend no Cloud Run — para validacao E2E)

---

## Fase 1 — Configuracao Firebase

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T1 | [T1-firebase-init.md](T1-firebase-init.md) | Inicializar Firebase: firebase.json, .firebaserc, headers, cache, SPA rewrite | 0.5-1 dia | Nenhuma | PENDENTE |
| T2 | [T2-dominio-ssl.md](T2-dominio-ssl.md) | Dominio customizado + DNS + SSL automatico + redirect www | 0.5 dia | T1 | PENDENTE |

---

## Fase 2 — CI/CD + Validacao

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T3 | [T3-github-actions-firebase.md](T3-github-actions-firebase.md) | GitHub Actions: build prod + deploy Firebase Hosting | 0.5-1 dia | T1 | PENDENTE |
| T4 | [T4-validacao-e2e-frontend.md](T4-validacao-e2e-frontend.md) | Validacao end-to-end + rollback + documentacao | 0.5 dia | T2, T3, Spec 018 T5 | PENDENTE |

---

## Dependencias Visuais

```
[T1: firebase.json] ──> [T2: Dominio + SSL]
        |                       |
        v                       v
[T3: GitHub Actions] ──> [T4: Validacao E2E]
                              |
                              v
                     (depende de Spec 018 T5 — backend rodando)
```

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
