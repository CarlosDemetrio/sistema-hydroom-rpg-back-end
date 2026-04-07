# Spec 018 — Plano de Implementacao: Deploy Backend GCP (Cloud Run + Native)

> Spec: `018-deploy-backend-gcp`
> Baseado em: spec.md v1.0 | 2026-04-07
> Estimativa total: ~5-8 dias de trabalho
> Depende de: Backlog funcional concluido (testes passando)

---

## Fases e Dependencias

```
FASE 1 — Preparacao do Build Nativo (local)
  T1: Configurar pom.xml com profile native + GraalVM hints
  T2: Criar Dockerfile.native (multi-stage com GraalVM)
  T3: Ajustar application-prod.properties para Cloud Run
  [T1 primeiro; T2 depende de T1; T3 paralelo com T1]

FASE 2 — Infraestrutura GCP
  T4: Setup VM e2-micro + PostgreSQL + Swap + Firewall + Backup
  T5: Configurar Cloud Run (servico, dominio, secrets)
  [T4 e T5 paralelos; T5 depende de T2]

FASE 3 — CI/CD + Validacao
  T6: GitHub Actions workflow (build native + deploy Cloud Run)
  T7: Validacao end-to-end + Rollback + Documentacao
  [T6 depende de T2+T5; T7 depende de T4+T5+T6]
```

---

## Fase 1 — Preparacao do Build Nativo

### T1 — pom.xml: Profile Native + GraalVM Hints
**Estimativa:** 1-2 dias
**Arquivos editados:**
- `pom.xml` — adicionar profile `native` com `native-maven-plugin`
- `src/main/java/.../config/NativeConfig.java` — RuntimeHintsRegistrar para exp4j e Bucket4j

**Detalhes:**
1. Adicionar `<profile id="native">` com `graalvm native-maven-plugin`
2. Criar `NativeHintsRegistrar` para registrar classes que usam reflection:
   - `net.objecthunter.exp4j.*` (formulas)
   - `com.bucket4j.*` (rate limiting)
   - Qualquer classe que falhe no build nativo
3. Testar: `./mvnw package -Pnative -DskipTests` deve compilar sem erros
4. Testar: executavel nativo roda localmente contra PostgreSQL local

**Riscos:**
- Lombok + MapStruct: geram codigo em compile-time → devem funcionar sem hints
- JPA/Hibernate: Spring AOT resolve automaticamente → testar entidades complexas
- OAuth2 Session: Spring Security AOT → testar fluxo completo

**Fallback:** Se native build falhar por incompatibilidade grave, documentar o problema e seguir com Plano B (JVM + Cloud Scheduler keepalive).

---

### T2 — Dockerfile.native (Multi-stage GraalVM)
**Estimativa:** 0.5-1 dia
**Dependencias:** T1 concluido
**Arquivos novos:**
- `Dockerfile.native` — multi-stage: GraalVM builder → distroless runtime
- `Dockerfile` (editar) — adicionar comentario referenciando Dockerfile.native para prod

**Dockerfile.native:**
```
Stage 1: ghcr.io/graalvm/native-image-community:25
  - Copia pom.xml + baixa dependencias (cache layer)
  - Copia src/ + compila native image
Stage 2: gcr.io/distroless/base-debian12
  - Copia apenas o executavel (~80-120MB)
  - Roda como nonroot
  - Sem JVM, sem curl, sem shell
```

**Dockerfile.jvm (Plano B):**
- Manter Dockerfile atual mas ajustar para Cloud Run:
  - Remover HEALTHCHECK (Cloud Run faz via HTTP)
  - Usar `PORT` env var

**Validacao:**
```bash
docker build -f Dockerfile.native -t rpg-api:native .
docker run -p 8081:8081 rpg-api:native
# Deve iniciar em < 500ms
```

---

### T3 — application-prod.properties para Cloud Run
**Estimativa:** 0.5 dia
**Paralelo com:** T1
**Arquivos editados:**
- `src/main/resources/application-prod.properties`

**Mudancas:**
1. `server.port=${PORT:8081}` — Cloud Run injeta PORT
2. `spring.datasource.url=${SPRING_DATASOURCE_URL}` — IP **interno** da VM via Direct VPC Egress
3. `spring.datasource.hikari.maximum-pool-size=3` — pool menor para serverless
4. `spring.datasource.hikari.minimum-idle=1` — serverless nao precisa de muitas idle
5. `springdoc.api-docs.enabled=false` — desabilitar Swagger em prod (seguranca + compat)
6. CORS: `app.cors.allowed-origins=${FRONTEND_URL}` — ja existente, manter
7. Session: manter in-memory para MVP (min-instances=1 no Cloud Run garante stickiness)

**NAO mudar:**
- OAuth2 config (mesma estrutura, apenas secrets diferentes)
- forward-headers-strategy=framework (Cloud Run tambem usa X-Forwarded-*)

---

## Fase 2 — Infraestrutura GCP

### T4 — VM e2-micro: PostgreSQL Dedicado
**Estimativa:** 1 dia
**Paralelo com:** T1, T2, T3
**Arquivos novos:**
- `infra/gcp/setup-db-vm.sh` — script de setup da VM (swap, Docker, PostgreSQL, backup, hardening)
- `infra/gcp/docker-compose-db.yml` — Compose com apenas PostgreSQL
- `infra/gcp/.env.example` — template de variaveis

**Passos do script:**
1. Criar swap de 2GB (VM tem apenas 1GB RAM)
2. Instalar Docker CE
3. Subir PostgreSQL 16 via Docker Compose
4. Configurar backup diario (cron + pg_dump)
5. Hardening basico: fail2ban, SSH key-only
6. UFW: PostgreSQL (5432) APENAS da rede VPC interna (`10.128.0.0/20`)

**Firewall GCP (regras manuais via gcloud):**
- Porta 5432: `source-ranges=10.128.0.0/20` (APENAS VPC interna — Cloud Run via Direct VPC Egress)
- Porta 22: `source-ranges=SEU_IP/32` (APENAS IP do desenvolvedor)
- PostgreSQL fica **INVISIVEL** para a internet

**Comandos GCP (documentados, execucao manual):**
```bash
gcloud compute instances create rpg-db --machine-type=e2-micro --zone=us-central1-a ...
gcloud compute addresses create rpg-db-ip --region=us-central1
gcloud compute firewall-rules create allow-postgres --rules=tcp:5432 ...
```

---

### T5 — Cloud Run: Servico + Dominio + Secrets
**Estimativa:** 1 dia
**Dependencias:** T2 (imagem Docker disponivel)
**Arquivos novos:**
- `infra/gcp/cloud-run-deploy.sh` — script de deploy inicial (manual, primeira vez)
- `infra/gcp/setup-secrets.sh` — criar secrets no GCP Secret Manager

**Passos:**
1. Criar secrets no GCP Secret Manager:
   - `rpg-db-username`, `rpg-db-password`
   - `rpg-google-client-id`, `rpg-google-client-secret`
   - `rpg-frontend-url`, `rpg-backend-url`
2. Deploy inicial do servico Cloud Run **com Direct VPC Egress** (`--network default --vpc-egress private-ranges-only`)
3. Datasource URL usa IP **interno** da VM (ex: `10.128.0.2`), NAO IP publico
4. Mapear dominio customizado `api.seu-dominio.com`
5. Configurar DNS (registro CNAME para `ghs.googlehosted.com`)
6. Verificar SSL automatico
7. Verificar CORS: requests de `https://seu-dominio.com` aceitos, outros bloqueados
8. (Plano B) Criar Cloud Scheduler job para keepalive

---

## Fase 3 — CI/CD + Validacao

### T6 — GitHub Actions: Build + Deploy
**Estimativa:** 1 dia
**Dependencias:** T2, T5
**Arquivos novos:**
- `.github/workflows/deploy-gcp.yml` — workflow completo

**Jobs:**
1. `test` — `./mvnw test` (pula se skip_tests)
2. `build-and-push` — Build Docker native + push para GHCR
3. `deploy` — `gcloud run deploy` com a nova imagem

**Secrets GitHub:**
- `GCP_SA_KEY` — Service Account JSON

**Triggers:**
- Push na branch `main`
- Manual via `workflow_dispatch`

---

### T7 — Validacao End-to-End + Documentacao
**Estimativa:** 1 dia
**Dependencias:** T4, T5, T6 — TODOS concluidos
**Arquivos novos/editados:**
- `docs/deploy/DEPLOY-GCP-BACKEND.md` — guia completo de deploy GCP
- `docs/deploy/DEPLOY-BACKEND.md` — marcar como DESCONTINUADO (OCI), referenciar novo doc

**Validacoes:**
1. Health check: `curl https://api.seu-dominio.com/actuator/health`
2. OAuth2 login end-to-end: frontend → backend → Google → callback
3. CRUD completo: criar jogo, configuracoes, ficha
4. Formulas: testar FormulaEvaluatorService em native
5. Rate limiting: testar que Bucket4j funciona
6. Cold start: medir tempo de primeira request
7. Rollback: re-deploy tag anterior, verificar que funciona

---

## Estimativa Consolidada

| Task | Tipo | Estimativa | Dependencias |
|------|------|-----------|-------------|
| T1 | Backend/Config | 1-2 dias | Nenhuma |
| T2 | Docker | 0.5-1 dia | T1 |
| T3 | Backend/Config | 0.5 dia | Paralelo com T1 |
| T4 | Infra/GCP | 1 dia | Paralelo com T1-T3 |
| T5 | Infra/GCP | 1 dia | T2 |
| T6 | CI/CD | 1 dia | T2, T5 |
| T7 | Validacao | 1 dia | T4, T5, T6 |
| **TOTAL** | | **~5-8 dias** | |

> Paralelismo: T1+T3+T4 podem ocorrer simultaneamente. T2 depende de T1, mas e rapido. T5+T6 na sequencia. T7 e a validacao final.

---

## Decisoes Tecnicas Tomadas

| Decisao | Escolha | Justificativa |
|---------|---------|---------------|
| Registry de imagens | GHCR (GitHub Container Registry) | Gratis, integrado ao GitHub Actions |
| Base image nativa | `gcr.io/distroless/base-debian12` | Minima superficie de ataque, sem shell |
| GraalVM version | Community Edition 25 | Compativel com Java 25 |
| Cloud Run region | `us-central1` | Free tier so cobre regioes dos EUA |
| Session storage | In-memory (MVP) | min-instances=1 garante stickiness para 50 usuarios |
| Swagger em prod | Desabilitado | Seguranca + compatibilidade native |
| Pool HikariCP | max=3, min-idle=1 | Serverless precisa de pool menor |

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
