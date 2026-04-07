# Spec 018 — Deploy Backend: Cloud Run + GraalVM Native Image

> Spec: `018-deploy-backend-gcp`
> Epic: Infraestrutura e Deploy
> Status: PLANEJADO — spec+plan+tasks PRONTOS, implementacao PENDENTE
> Depende de: Backlog funcional concluido (Specs 005-016), Spec 019 (frontend — independente, paralela)
> Bloqueia: Tag `v0.0.1-RC`
> Prioridade: P0 — Obrigatorio para RC

---

## 1. Visao Geral

**Problema resolvido:** O plano original previa deploy na OCI (Oracle Cloud Infrastructure), mas nao ha maquinas disponiveis no free tier. A nova estrategia utiliza o **GCP Free Tier** com Cloud Run (serverless) para o backend e uma VM e2-micro dedicada exclusivamente ao PostgreSQL. O principal desafio tecnico e migrar o build de um JAR JVM tradicional para uma **Native Image via GraalVM/Spring Native**, reduzindo o cold start de ~15-30 segundos para ~100-300 milissegundos — essencial para o modelo serverless do Cloud Run.

**Objetivo:** Configurar pipeline completa de CI/CD para o backend Spring Boot no Google Cloud Run, com build nativo GraalVM, imagem Docker otimizada, e conectividade com PostgreSQL rodando em VM e2-micro dedicada.

**Valor entregue:**
- Backend em producao com custo R$ 0,00/mes (GCP Free Tier)
- Cold start < 300ms (Native Image) — UX profissional mesmo em serverless
- Deploy automatizado via GitHub Actions (push na `main`)
- Zero downtime em deploys (Cloud Run traffic splitting)
- SSL automatico gerenciado pelo Google (sem Caddy)
- Dominio customizado `api.seu-dominio.com`

---

## 2. Contexto: Migracao OCI → GCP

### Antes (OCI — descontinuado por falta de maquinas)

```
VM OCI ARM64 (2 OCPU, 12GB) → rodava TUDO:
  - Caddy (reverse proxy + SSL)
  - Docker: Spring Boot (JAR JVM) + PostgreSQL
  - Frontend (arquivos estaticos)
```

### Agora (GCP Free Tier)

```
Cloud Run (serverless)     → Backend Spring Boot (Native Image Docker)
VM e2-micro (1 vCPU, 1GB) → PostgreSQL 16 (APENAS banco de dados)
Firebase Hosting (CDN)     → Frontend Angular (Spec 019 — separada)
```

### Impactos da Migracao

| Aspecto | OCI (antes) | GCP (agora) |
|---------|-------------|-------------|
| Backend runtime | JVM (JAR) em Docker na VM | Native Image em Cloud Run (serverless) |
| SSL/TLS | Caddy gerenciava | Google gerencia automaticamente |
| Reverse proxy | Caddy | Nao necessario (Cloud Run gerencia) |
| Banco de dados | PostgreSQL na mesma VM | PostgreSQL em VM e2-micro separada |
| Cold start | Nao aplicavel (sempre ligado) | ~100-300ms (Native) ou ping cada 10min (Plano B) |
| Escala | Manual (1 instancia) | Automatica (0 a N instancias) |
| CI/CD | GitHub Actions → SSH deploy na VM | GitHub Actions → `gcloud run deploy` |
| Custo | $0 (OCI Free Tier) | $0 (GCP Free Tier: 2M req/mes) |

---

## 3. Arquitetura de Deploy

```
Internet (Usuarios)
   |
   +-- [Frontend] --> https://seu-dominio.com
   |       Firebase Hosting (CDN Global) — DDoS/SSL gerenciados pelo Google
   |
   +-- [API] ------> https://api.seu-dominio.com
           Google Cloud Run (Serverless)
           SSL automatico pelo Google
           Spring Security: OAuth2 + CORS (aceita APENAS https://seu-dominio.com)
           |
           | Direct VPC Egress (rede interna Google — SEM internet publica)
           |
           v
      [Banco de Dados] -> IP Interno VPC (ex: 10.128.0.2:5432)
           Google Compute Engine (e2-micro)
           PostgreSQL 16 (Docker Compose)
           Porta 5432 INVISIVEL para internet
           Firewall: aceita trafego APENAS da VPC interna
           SSH (porta 22): aceita APENAS do IP do desenvolvedor
```

### Modelo de Seguranca (Defense in Depth)

| Fronteira | Quem protege | Como |
|-----------|-------------|------|
| **Frontend (Firebase)** | Google | CDN global, DDoS automatico, HTTPS forcado, headers de seguranca |
| **API (Cloud Run)** | Google + Spring | SSL automatico, container isolado (sem SSH), Spring Security OAuth2, CORS restritivo, rate limiting (Bucket4j) |
| **Banco (VM e2-micro)** | VPC + Firewall | PostgreSQL acessivel APENAS via IP interno VPC. Porta 5432 **fechada** para internet. Senha forte via Secret Manager |
| **SSH (VM)** | Firewall GCP | Porta 22 aberta APENAS para IP residencial do desenvolvedor |
| **Secrets** | GCP Secret Manager | Senhas/tokens NUNCA no codigo ou GitHub. Cloud Run puxa do Secret Manager no startup |

#### Ponto Critico: Direct VPC Egress

O Cloud Run, por padrao, nao tem acesso a rede interna (VPC) do GCP. Para que o Spring Boot conecte ao PostgreSQL via IP interno, e necessario configurar **Direct VPC Egress**:

```bash
gcloud run services update rpg-api \
  --network=default \
  --subnet=default \
  --vpc-egress=private-ranges-only \
  --region=us-central1
```

Com isso:
- Cloud Run envia trafego para IPs privados (`10.x.x.x`) via rede interna
- O trafego para a internet (Google OAuth, etc.) continua normal
- A VM e2-micro nao precisa de IP publico para receber conexoes do Cloud Run
- PostgreSQL escuta APENAS na interface interna da VM

---

## 3.1 CORS — Configuracao Critica

O CORS e a primeira linha de defesa da API contra uso indevido. Com frontend e backend em dominios diferentes, a configuracao deve ser **explicita e restritiva**.

**No `application-prod.properties`:**
```properties
app.cors.allowed-origins=${FRONTEND_URL:https://seu-dominio.com}
```

**O que isso garante:**
- Apenas `https://seu-dominio.com` pode fazer requests a API
- Qualquer outro site que tente usar a API recebe bloqueio CORS no navegador
- Requests server-to-server (curl, Postman) nao sao bloqueados por CORS (isso e normal), mas precisam de autenticacao OAuth2

**O que ja existe no Spring Security (`SecurityConfig`):**
- CORS configurado via `app.cors.allowed-origins`
- Metodos permitidos: GET, POST, PUT, DELETE, OPTIONS
- Credentials: `true` (necessario para cookies de sessao cross-origin)
- Headers: Authorization, Content-Type, X-XSRF-TOKEN

> **IMPORTANTE:** O `FRONTEND_URL` e injetado via Secret Manager no Cloud Run. Se mudar o dominio, basta atualizar o secret — sem redeploy.

---

## 4. Migracao para Native Image (GraalVM / Spring AOT)

### 4.1 O que muda no build

O Spring Boot 4.0.2 suporta compilacao AOT (Ahead-of-Time) nativamente via `spring-boot-maven-plugin`. O build gera um executavel nativo que:
- Nao precisa de JVM no runtime
- Inicia em ~100-300ms (vs ~15-30s com JVM)
- Consome ~50-80MB de RAM (vs ~256-384MB com JVM)
- Imagem Docker final ~80-120MB (vs ~300MB com JRE)

### 4.2 Riscos e Desafios do Native Build

| Risco | Descricao | Mitigacao |
|-------|-----------|-----------|
| **RISK-018-01** | Reflection: Lombok, MapStruct, JPA Hibernate usam reflection extensivamente. GraalVM precisa de hints explícitos. | Spring AOT processa automaticamente beans gerenciados. Testar cada entidade/mapper. |
| **RISK-018-02** | MapStruct 1.5.5: gera codigo em compile-time (compativel), mas o annotation processor precisa rodar antes do AOT. | Ordem de annotation processors no `maven-compiler-plugin` deve ser correta. |
| **RISK-018-03** | exp4j (formula evaluator): usa reflection para parsear expressoes. Pode precisar de `reflect-config.json` manual. | Testar formulas no native image. Se falhar, registrar classes via `RuntimeHintsRegistrar`. |
| **RISK-018-04** | Tempo de build: Native Image leva 5-15 minutos no CI (vs ~2 min JVM). | Cache de layers Docker + build apenas na `main`. |
| **RISK-018-05** | H2 em testes: testes continuam usando JVM (H2 nao roda em native). Build nativo e apenas para producao. | Profile `native` apenas no Dockerfile de producao. |
| **RISK-018-06** | springdoc-openapi: Swagger UI pode nao funcionar em native sem configuracao extra. | Desabilitar Swagger em prod (`springdoc.api-docs.enabled=false`) ou adicionar hints. |
| **RISK-018-07** | OAuth2 + Session: Spring Security OAuth2 client usa reflection para deserializar tokens. | Spring AOT resolve automaticamente para beans gerenciados. Testar fluxo OAuth2 em native. |
| **RISK-018-08** | Bucket4j: rate limiting usa classes internas que podem precisar de hints. | Testar endpoints com rate limit. Se falhar, adicionar ao `reflect-config.json`. |

### 4.3 Plano B — JVM com Cloud Scheduler Ping

Se o Native Build se mostrar inviavel (muitos hints manuais, bugs bloqueadores):

1. Manter build JVM tradicional (JAR)
2. Usar Cloud Scheduler para fazer GET `/actuator/health` a cada 10 minutos
3. Configurar `min-instances=0` no Cloud Run
4. Cold start ~15-30s (aceitavel com ping keepalive, instancia quase nunca dorme)
5. Custo: continua $0 (Cloud Scheduler tem 3 jobs gratis/mes)

---

## 5. Mudancas no pom.xml

### 5.1 Adicionar suporte GraalVM Native

```xml
<properties>
    <java.version>25</java.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <!-- GraalVM Native Build Support -->
    <native.maven.plugin.version>0.10.6</native.maven.plugin.version>
</properties>
```

### 5.2 Profile `native`

```xml
<profiles>
    <profile>
        <id>native</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.graalvm.buildtools</groupId>
                    <artifactId>native-maven-plugin</artifactId>
                    <version>${native.maven.plugin.version}</version>
                    <executions>
                        <execution>
                            <id>build-native</id>
                            <goals>
                                <goal>compile-no-fork</goal>
                            </goals>
                            <phase>package</phase>
                        </execution>
                    </executions>
                    <configuration>
                        <buildArgs>
                            <buildArg>--no-fallback</buildArg>
                            <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                        </buildArgs>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <image>
                            <builder>paketobuildpacks/builder-jammy-tiny:latest</builder>
                        </image>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### 5.3 RuntimeHints (se necessario)

```java
@Configuration
@ImportRuntimeHints(NativeHintsRegistrar.class)
public class NativeConfig {
}

public class NativeHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // exp4j: registrar classes de expressao
        hints.reflection().registerType(
            net.objecthunter.exp4j.Expression.class,
            MemberCategory.values()
        );
        // Bucket4j: se necessario
        // hints.reflection().registerType(...)
    }
}
```

---

## 6. Novo Dockerfile (Native Build)

```dockerfile
# =====================================================================
# Stage 1: Build Native Image
# =====================================================================
FROM ghcr.io/graalvm/native-image-community:25 AS build
WORKDIR /app

# Cache de dependencias Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar codigo e compilar native image
COPY src ./src
RUN ./mvnw clean package -Pnative -DskipTests

# =====================================================================
# Stage 2: Runtime minimo (distroless)
# =====================================================================
FROM gcr.io/distroless/base-debian12 AS production

WORKDIR /app

# Copiar executavel nativo (nao precisa de JVM!)
COPY --from=build /app/target/ficha-controlador /app/ficha-controlador

# Porta
EXPOSE 8081

# Health check (distroless nao tem curl — Cloud Run faz health check via HTTP)
# HEALTHCHECK removido: Cloud Run gerencia health checks nativamente

# Executar como non-root (distroless ja roda como nonroot por padrao)
USER nonroot:nonroot

ENTRYPOINT ["/app/ficha-controlador"]
```

### Dockerfile alternativo (Plano B — JVM)

```dockerfile
# Manter o Dockerfile atual com pequenos ajustes para Cloud Run
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre AS production
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 7. VM e2-micro — PostgreSQL Dedicado

### 7.1 Setup da VM

```bash
# Criar VM e2-micro (GCP Free Tier — regioes dos EUA obrigatorio)
gcloud compute instances create rpg-db \
  --machine-type=e2-micro \
  --zone=us-central1-a \
  --image-family=ubuntu-2404-lts-amd64 \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-standard \
  --tags=postgres-server

# IP estatico
gcloud compute addresses create rpg-db-ip --region=us-central1
gcloud compute instances add-access-config rpg-db \
  --access-config-name="External NAT" \
  --address=$(gcloud compute addresses describe rpg-db-ip --region=us-central1 --format='value(address)')
```

### 7.2 Firewall (Defense in Depth)

```bash
# ⚠️ REGRA 1: PostgreSQL — APENAS trafego VPC interno (NÃO expor para internet!)
gcloud compute firewall-rules create allow-postgres-vpc \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:5432 \
  --target-tags=postgres-server \
  --source-ranges=10.128.0.0/20 \
  --description="PostgreSQL - APENAS rede interna VPC (Cloud Run via Direct VPC Egress)"

# ⚠️ REGRA 2: SSH — APENAS o IP do desenvolvedor
gcloud compute firewall-rules create allow-ssh-admin \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:22 \
  --target-tags=postgres-server \
  --source-ranges=SEU_IP_RESIDENCIAL/32 \
  --description="SSH - APENAS IP do desenvolvedor em Brasilia"

# ⚠️ REGRA 3: NEGAR todo o resto (default deny ja e o padrao do GCP)
# GCP nega ingress por padrao — nao precisa criar regra explicita
```

> **CRITICO:** A porta 5432 fica INVISIVEL para a internet. Robos de scan NUNCA encontram o banco.
> Cloud Run acessa via IP interno (ex: `10.128.0.2`) usando Direct VPC Egress.

### 7.3 Script de Setup (gcp/setup-db-vm.sh)

Ver arquivo `infra/gcp/setup-db-vm.sh` para o script completo que:
1. Cria swap de 2GB (protecao OOM na e2-micro de 1GB)
2. Instala Docker
3. Sobe PostgreSQL via Docker Compose (bind na interface interna APENAS)
4. Configura backup automatico
5. Hardening basico (fail2ban, UFW)

### 7.4 Docker Compose (apenas PostgreSQL — bind interno)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: rpg-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-rpg_fichas}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      # Bind APENAS na interface interna da VPC — NAO expor para 0.0.0.0!
      # O IP interno e atribuido pela VPC (ex: 10.128.0.2)
      - "${DB_BIND_IP:-0.0.0.0}:5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${POSTGRES_DB:-rpg_fichas}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    security_opt:
      - no-new-privileges:true
    deploy:
      resources:
        limits:
          memory: 384m

volumes:
  pgdata:
    driver: local
```

> **NOTA:** O bind `0.0.0.0` no Docker e seguro porque a firewall GCP ja bloqueia acesso externo na porta 5432. Apenas trafego da VPC interna (source `10.128.0.0/20`) e permitido.

---

## 8. Cloud Run — Configuracao

### 8.1 Deploy Inicial (manual)

```bash
# Primeiro deploy: criar o servico com Direct VPC Egress
gcloud run deploy rpg-api \
  --image ghcr.io/carlosdemetrio/ficha-controlador:latest \
  --region us-central1 \
  --platform managed \
  --port 8081 \
  --memory 256Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 3 \
  --timeout 300 \
  --allow-unauthenticated \
  --network default \
  --subnet default \
  --vpc-egress private-ranges-only \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://<DB_VM_INTERNAL_IP>:5432/rpg_fichas" \
  --set-secrets "DB_USERNAME=rpg-db-username:latest" \
  --set-secrets "DB_PASSWORD=rpg-db-password:latest" \
  --set-secrets "GOOGLE_CLIENT_ID=rpg-google-client-id:latest" \
  --set-secrets "GOOGLE_CLIENT_SECRET=rpg-google-client-secret:latest" \
  --set-secrets "FRONTEND_URL=rpg-frontend-url:latest" \
  --set-secrets "BACKEND_URL=rpg-backend-url:latest"
```

> **CRITICO:** `<DB_VM_INTERNAL_IP>` e o IP **interno** da VM (ex: `10.128.0.2`), NAO o IP publico!
> Obter via: `gcloud compute instances describe rpg-db --zone=us-central1-a --format='value(networkInterfaces[0].networkIP)'`
>
> Os flags `--network`, `--subnet` e `--vpc-egress` habilitam Direct VPC Egress.
> Com `private-ranges-only`, o Cloud Run envia trafego para IPs privados via VPC interna,
> mas trafego para a internet (Google OAuth, etc.) continua normal.

### 8.2 Dominio Customizado

```bash
# Mapear api.seu-dominio.com ao servico Cloud Run
gcloud run domain-mappings create \
  --service rpg-api \
  --domain api.seu-dominio.com \
  --region us-central1
```

### 8.3 Cloud Scheduler (Plano B — keepalive)

```bash
# Ping a cada 10 minutos para evitar cold start (se nao usar Native)
gcloud scheduler jobs create http rpg-api-keepalive \
  --schedule="*/10 * * * *" \
  --uri="https://api.seu-dominio.com/actuator/health" \
  --http-method=GET \
  --attempt-deadline=30s \
  --location=us-central1
```

---

## 9. Mudancas no application-prod.properties

```properties
# Cloud Run nao usa Caddy — headers gerenciados pelo Google
server.forward-headers-strategy=framework

# Cloud Run define a porta via variavel PORT
server.port=${PORT:8081}

# Datasource aponta para IP INTERNO da VM via Direct VPC Egress
# Ex: jdbc:postgresql://10.128.0.2:5432/rpg_fichas
# NUNCA usar IP publico — o banco nao esta exposto para internet!
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Pool menor para serverless (conexoes efemeras)
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=5000

# CORS — CRITICO: aceitar APENAS requests do frontend
# Bloqueia qualquer outro site de usar a API via navegador
app.cors.allowed-origins=${FRONTEND_URL:https://seu-dominio.com}

# Session: Cloud Run nao garante sticky sessions — avaliar
# Para MVP com 50 usuarios, session in-memory funciona se min-instances=1
# Pos-MVP: migrar para Redis ou Spring Session JDBC
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=lax
server.servlet.session.timeout=30m

# Desabilitar Swagger em prod (native compat + seguranca)
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

---

## 10. GitHub Actions Workflow

### Backend CI/CD (`.github/workflows/deploy-gcp.yml`)

```yaml
name: Deploy Backend to Cloud Run

on:
  push:
    branches: [main]
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests?'
        type: boolean
        default: false
      build_mode:
        description: 'Build mode'
        type: choice
        options: [native, jvm]
        default: native

jobs:
  test:
    if: ${{ !inputs.skip_tests }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 25
      - run: ./mvnw test

  build-and-deploy:
    needs: [test]
    if: always() && (needs.test.result == 'success' || needs.test.result == 'skipped')
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build & Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: .
          file: ./Dockerfile.native
          push: true
          tags: |
            ghcr.io/${{ github.repository_owner }}/ficha-controlador:latest
            ghcr.io/${{ github.repository_owner }}/ficha-controlador:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Auth GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}

      - name: Deploy to Cloud Run
        uses: google-github-actions/deploy-cloudrun@v2
        with:
          service: rpg-api
          region: us-central1
          image: ghcr.io/${{ github.repository_owner }}/ficha-controlador:${{ github.sha }}
```

---

## 11. Secrets Necessarios (GitHub)

| Secret | Descricao | Onde usar |
|--------|-----------|----------|
| `GCP_SA_KEY` | Service Account JSON com permissao `roles/run.admin` | GitHub Actions |
| `GITHUB_TOKEN` | Automatico (para GHCR push) | GitHub Actions |

### Secrets no GCP (Secret Manager)

| Secret | Descricao |
|--------|-----------|
| `rpg-db-username` | Username do PostgreSQL |
| `rpg-db-password` | Senha do PostgreSQL |
| `rpg-google-client-id` | Google OAuth2 Client ID |
| `rpg-google-client-secret` | Google OAuth2 Client Secret |
| `rpg-frontend-url` | URL do frontend (ex: `https://seu-dominio.com`) |

---

## 12. Tabela de Custos Projetada

| Servico | Nivel Gratuito Mensal | Consumo Estimado (50 usuarios) | Custo |
|---------|----------------------|-------------------------------|-------|
| **Compute Engine (e2-micro)** | 1 instancia / ~740 horas | 730 horas (24/7) | **R$ 0,00** |
| **Disco Permanente (Standard)** | 30 GB | 30 GB | **R$ 0,00** |
| **Trafego de Saida (Egress)** | 1 GB | ~200 MB | **R$ 0,00** |
| **Cloud Run (Requisicoes)** | 2.000.000 requisicoes | ~50.000 requisicoes | **R$ 0,00** |
| **Cloud Run (CPU)** | 180.000 vCPU-segundos | ~10.000 vCPU-segundos | **R$ 0,00** |
| **Secret Manager** | 6 versoes ativas gratis | 5 secrets | **R$ 0,00** |
| **Cloud Scheduler** | 3 jobs gratis | 1 job (keepalive) | **R$ 0,00** |

---

## 13. Requisitos Funcionais

| # | Requisito | Responsavel |
|---|-----------|-------------|
| RF-01 | Build nativo GraalVM funcional com todos os 613+ testes passando (JVM) | Dev |
| RF-02 | Dockerfile nativo multi-stage com imagem < 150MB | Dev |
| RF-03 | Deploy automatizado via GitHub Actions (push na main) | DevOps |
| RF-04 | VM e2-micro com PostgreSQL acessivel pelo Cloud Run | DevOps |
| RF-05 | SSL automatico para `api.seu-dominio.com` via Cloud Run | DevOps |
| RF-06 | OAuth2 Google funcional com redirect URI atualizado | Dev |
| RF-07 | Health check `/actuator/health` respondendo no Cloud Run | Dev |
| RF-08 | Rollback possivel via re-deploy de tag anterior no GHCR | DevOps |
| RF-09 | Backup diario automatico do PostgreSQL na VM | DevOps |
| RF-10 | Logs acessiveis via `gcloud run logs` | DevOps |

---

## 14. Criterios de Aceitacao

- [ ] `./mvnw test` passa com 613+ testes (nenhuma regressao)
- [ ] `./mvnw package -Pnative -DskipTests` gera executavel nativo sem erros
- [ ] Imagem Docker nativa < 150MB
- [ ] Cold start no Cloud Run < 500ms (ideal < 300ms)
- [ ] `curl https://api.seu-dominio.com/actuator/health` retorna `{"status":"UP"}`
- [ ] OAuth2 login funcional end-to-end (Google → redirect → session)
- [ ] CORS permite requests do frontend (`https://seu-dominio.com`)
- [ ] Endpoints protegidos por `@PreAuthorize` funcionam corretamente
- [ ] FormulaEvaluatorService funciona em native (formulas exp4j avaliadas)
- [ ] Deploy via GitHub Actions completa sem erros
- [ ] Rollback funcional: re-deploy de tag anterior restaura versao

---

## 15. Pontos em Aberto

| ID | Pergunta | Impacto |
|----|----------|---------|
| PA-018-01 | Session in-memory e suficiente no Cloud Run (min-instances=1) ou precisa de Spring Session JDBC? | Impacta T3 (properties) e custo |
| PA-018-02 | Swagger UI deve ficar disponivel em prod ou apenas dev? | Impacta T2 (native hints) e seguranca |
| PA-018-03 | Cloud Run region: us-central1 (mais barato) ou southamerica-east1 (mais proximo)? Free tier so cobre regioes dos EUA. | Impacta latencia vs custo |

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
*Baseado em: Documento de Arquitetura GCP, pom.xml atual, Dockerfile existente*
