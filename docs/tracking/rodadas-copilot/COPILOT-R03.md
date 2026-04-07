# Copilot R03 — Spec 018 (Deploy Backend GCP) + Spec 019 (Deploy Frontend Firebase)

> Data: 2026-04-07
> Branch: `main`
> Base: 719 testes backend, 0 falhas
> Status: **CONCLUIDA**

---

## Contexto

Rodada de deploy total da plataforma Ficha Controlador: backend migrado de OCI
para GCP (Cloud Run + VM e2-micro PostgreSQL) e frontend migrado de OCI para
Firebase Hosting (CDN Global).

Docs OCI marcados como DESCONTINUADO em ambos os repositórios.

---

## Spec 018 — Deploy Backend GCP

> 7 tasks | todos os arquivos no repositório `ficha-controlador`

### Tasks Executadas

| Task | Descrição | Arquivos |
|------|-----------|---------|
| T1 | GraalVM Native Image — pom.xml profile + NativeConfig | `pom.xml`, `NativeConfig.java`, `NativeHintsRegistrar.java` |
| T2 | Dockerfile.native + Dockerfile.jvm-cloudrun | `Dockerfile.native`, `Dockerfile.jvm-cloudrun`, `Dockerfile` (comentário) |
| T3 | application-prod.properties Cloud Run | `application-prod.properties` |
| T4 | Scripts infra GCP (VM PostgreSQL) | `infra/gcp/setup-db-vm.sh`, `docker-compose-db.yml`, `.env.example`, `backup-postgres.sh` |
| T5 | Scripts Cloud Run + Secret Manager | `infra/gcp/cloud-run-deploy.sh`, `setup-secrets.sh`, `README.md` |
| T6 | GitHub Actions CI/CD | `.github/workflows/deploy-gcp.yml`, `.github/workflows/README.md` |
| T7 | Documentação deploy + .dockerignore | `docs/deploy/DEPLOY-GCP-BACKEND.md`, `.dockerignore` |

### Decisões Técnicas

- **Native vs JVM**: dois Dockerfiles mantidos — `Dockerfile.native` (primário, Cloud Run) e `Dockerfile.jvm-cloudrun` (fallback). GitHub Actions aceita input para escolher entre ambos.
- **Direct VPC Egress**: Cloud Run acessa PostgreSQL via IP interno da VM (`10.128.0.x`) — banco NUNCA exposto à internet.
- **HikariCP reduzido**: max-pool-size=3 para Cloud Run serverless — múltiplas instâncias × 3 conexões = carga gerenciável no e2-micro.
- **Sessão in-memory + min-instances=1**: OAuth2 com sessão HTTP (não JWT) requer `min-instances=1` no Cloud Run para garantir stickiness.
- **exp4j reflection hints**: `NativeHintsRegistrar` registra `Expression` e `ExpressionBuilder` para o GraalVM — necessário pois exp4j usa reflection internamente.
- **Swap 2GB na VM**: protege contra OOM Killer no e2-micro (1GB RAM) durante picos.
- **Docs OCI descontinuados**: `docs/deploy/DEPLOY-BACKEND.md` e `DEPLOY-OCI.md` marcados com banner ⚠️ DESCONTINUADO.

### Testes

719 testes passando após todas as alterações (base era 719 — zero regressões).

---

## Spec 019 — Deploy Frontend Firebase

> 4 tasks | todos os arquivos no repositório `ficha-controlador-front-end`

### Tasks Executadas

| Task | Descrição | Arquivos |
|------|-----------|---------|
| T1 | Firebase init — firebase.json + .firebaserc + .gitignore | `firebase.json`, `.firebaserc`, `.gitignore` |
| T2 | Guia domínio customizado + DNS + SSL | `docs/DEPLOY-FIREBASE-DNS.md` |
| T3 | GitHub Actions CI/CD Firebase | `.github/workflows/deploy-firebase.yml` |
| T4 | Docs atualização + DESCONTINUADO OCI | `docs/DEPLOY-FRONTEND.md`, `README.md` |

### Decisões Técnicas

- **SPA rewrite**: `**` → `/index.html` garante que rotas Angular profundas funcionem diretamente via URL.
- **Cache strategy**: assets hasheados (`*.js`, `*.css`, etc.) com `max-age=31536000, immutable`; `index.html` com `no-cache, no-store` — garante que deploy novo seja visto imediatamente.
- **Headers de segurança**: X-Frame-Options: DENY, X-Content-Type-Options: nosniff, Referrer-Policy, Permissions-Policy em todos os paths.
- **`FirebaseExtended/action-hosting-deploy@v0`**: action oficial Firebase para deploy a partir de GitHub Actions — usa `FIREBASE_SERVICE_ACCOUNT` secret.
- **`workflow_dispatch` com `skip_tests`**: permite deploy manual emergencial sem rodar testes.
- **Domínio Firebase placeholder**: `ficha-controlador-rpg` — substituir pelo ID real do projeto Firebase.
- **Docs OCI descontinuados**: `docs/DEPLOY-FRONTEND.md` marcado com banner ⚠️ DESCONTINUADO.

### Próximos Passos Manuais (não automatizáveis)

1. Criar projeto Firebase em console.firebase.google.com (pode ser o mesmo projeto GCP da Spec 018)
2. Gerar Service Account JSON → secret `FIREBASE_SERVICE_ACCOUNT` no GitHub do repositório frontend
3. Criar environment `production` no GitHub do repositório frontend
4. Configurar domínio customizado no Firebase Console (guia: `docs/DEPLOY-FIREBASE-DNS.md`)
5. Atualizar `src/environments/environment.prod.ts` com URL real da API (`https://api.seu-dominio.com/api/v1`)

---

## Arquivos Modificados/Criados — Resumo

### `ficha-controlador` (backend)

```
pom.xml                                                     ← profile native
src/main/java/.../config/NativeConfig.java                  ← NOVO
src/main/java/.../config/NativeHintsRegistrar.java          ← NOVO
src/main/resources/application-prod.properties              ← Cloud Run
Dockerfile                                                  ← comentário dev-only
Dockerfile.native                                           ← NOVO (Cloud Run primário)
Dockerfile.jvm-cloudrun                                     ← NOVO (fallback JVM)
.dockerignore                                               ← exclusões prod
infra/gcp/setup-db-vm.sh                                    ← NOVO
infra/gcp/docker-compose-db.yml                             ← NOVO
infra/gcp/.env.example                                      ← NOVO
infra/gcp/backup-postgres.sh                                ← NOVO
infra/gcp/cloud-run-deploy.sh                               ← NOVO
infra/gcp/setup-secrets.sh                                  ← NOVO
infra/gcp/README.md                                         ← NOVO
infra/README.md                                             ← NOVO
.github/workflows/deploy-gcp.yml                            ← NOVO
.github/workflows/README.md                                 ← NOVO
docs/deploy/DEPLOY-GCP-BACKEND.md                           ← NOVO
docs/deploy/DEPLOY-BACKEND.md                               ← DESCONTINUADO
docs/deploy/DEPLOY-OCI.md                                   ← DESCONTINUADO
```

### `ficha-controlador-front-end` (frontend)

```
firebase.json                                               ← NOVO
.firebaserc                                                 ← NOVO
.gitignore                                                  ← Firebase entries
.github/workflows/deploy-firebase.yml                       ← NOVO
docs/DEPLOY-FIREBASE-DNS.md                                 ← NOVO
docs/DEPLOY-FRONTEND.md                                     ← DESCONTINUADO
README.md                                                   ← seção Deploy
```

---

*Copilot CLI | Sessão 2026-04-07*
