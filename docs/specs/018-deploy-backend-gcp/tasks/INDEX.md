# Spec 018 — Deploy Backend GCP (Cloud Run + Native): INDEX de Tasks

> Spec: `018-deploy-backend-gcp`
> Status: PLANEJADO — implementacao PENDENTE
> Total: 7 tasks (3 backend/config + 2 infra + 1 CI/CD + 1 validacao)
> Estimativa: ~5-8 dias
> Depende de: Backlog funcional concluido (613+ testes passando)

---

## Fase 1 — Preparacao do Build Nativo

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T1 | [T1-native-build-config.md](T1-native-build-config.md) | pom.xml: profile native + GraalVM + RuntimeHints para exp4j/Bucket4j | 1-2 dias | Nenhuma | PENDENTE |
| T2 | [T2-dockerfile-native.md](T2-dockerfile-native.md) | Dockerfile.native: multi-stage GraalVM → distroless | 0.5-1 dia | T1 | PENDENTE |
| T3 | [T3-properties-cloudrun.md](T3-properties-cloudrun.md) | application-prod.properties: ajustes para Cloud Run (PORT, pool, swagger off) | 0.5 dia | Paralelo com T1 | PENDENTE |

---

## Fase 2 — Infraestrutura GCP

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T4 | [T4-vm-postgresql.md](T4-vm-postgresql.md) | VM e2-micro: setup PostgreSQL + swap + backup + hardening | 1 dia | Paralelo com T1-T3 | PENDENTE |
| T5 | [T5-cloudrun-setup.md](T5-cloudrun-setup.md) | Cloud Run: servico + dominio + secrets + Cloud Scheduler | 1 dia | T2 | PENDENTE |

---

## Fase 3 — CI/CD + Validacao

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T6 | [T6-github-actions.md](T6-github-actions.md) | GitHub Actions: workflow build native + deploy Cloud Run | 1 dia | T2, T5 | PENDENTE |
| T7 | [T7-validacao-e2e.md](T7-validacao-e2e.md) | Validacao end-to-end + rollback + documentacao DEPLOY-GCP-BACKEND.md | 1 dia | T4, T5, T6 | PENDENTE |

---

## Dependencias Visuais

```
[T1: pom.xml native] ──> [T2: Dockerfile.native]
        |                         |
        |                         v
[T3: properties] (paralelo)  [T5: Cloud Run setup]
                                  |
[T4: VM PostgreSQL] (paralelo)    v
        |                  [T6: GitHub Actions]
        |                         |
        +-------------------------+
                    |
                    v
           [T7: Validacao E2E]
```

---

## Plano B (JVM + Cloud Scheduler)

Se T1 falhar (native build incompativel), o fallback e:
1. Manter Dockerfile JVM existente (com ajustes menores)
2. Criar Cloud Scheduler job para ping `/health` a cada 10 minutos
3. Cold start ~15-30s mas instancia quase nunca dorme
4. Tasks T2 e T6 simplificam (sem native, build mais rapido)

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
