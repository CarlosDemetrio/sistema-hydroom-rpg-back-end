# T2 — Dockerfile.native: Multi-stage GraalVM → Distroless

> Fase: Docker | Prioridade: P0
> Dependencias: T1 concluido (pom.xml com profile native)
> Bloqueia: T5 (Cloud Run setup), T6 (GitHub Actions)
> Estimativa: 0.5-1 dia

---

## Objetivo

Criar um `Dockerfile.native` otimizado para producao no Cloud Run, usando build multi-stage: GraalVM Community 25 como builder e `distroless` como runtime. A imagem final deve conter APENAS o executavel nativo (sem JVM, sem shell, sem ferramentas).

Manter o `Dockerfile` existente como referencia para dev/JVM e criar `Dockerfile.jvm-cloudrun` como Plano B.

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `Dockerfile.native` | Multi-stage: GraalVM build → distroless runtime (~80-120MB) |
| `Dockerfile.jvm-cloudrun` | Plano B: JVM tradicional ajustado para Cloud Run (~300MB) |
| `.dockerignore` | Atualizar para excluir docs, specs, infra do contexto de build |

---

## Dockerfile.native

```dockerfile
# =====================================================================
# Dockerfile.native — Build nativo GraalVM para Cloud Run
# =====================================================================
# Uso: docker build -f Dockerfile.native -t rpg-api:native .
# Tamanho final: ~80-120MB (sem JVM)
# Cold start: ~100-300ms
# =====================================================================

# ----- Stage 1: Build Nativo -----
FROM ghcr.io/graalvm/native-image-community:25 AS build
WORKDIR /app

# Cache de dependencias Maven (layer separado)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

# Copiar codigo e compilar native image
COPY src ./src
RUN ./mvnw clean package -Pnative -DskipTests -q

# ----- Stage 2: Runtime Distroless -----
FROM gcr.io/distroless/base-debian12 AS production

WORKDIR /app

# Copiar APENAS o executavel nativo
COPY --from=build /app/target/ficha-controlador /app/ficha-controlador

# Cloud Run injeta a variavel PORT
ENV PORT=8081
EXPOSE ${PORT}

# Distroless roda como nonroot por padrao (UID 65534)
USER nonroot:nonroot

# Executar o binario nativo diretamente (sem java -jar)
ENTRYPOINT ["/app/ficha-controlador"]
```

---

## Dockerfile.jvm-cloudrun (Plano B)

```dockerfile
# =====================================================================
# Dockerfile.jvm-cloudrun — JVM tradicional para Cloud Run (Plano B)
# =====================================================================
# Usar SE o build nativo falhar por incompatibilidade.
# Tamanho final: ~300MB (com JRE)
# Cold start: ~15-30s (mitigado por Cloud Scheduler keepalive)
# =====================================================================

# ----- Build -----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ----- Runtime -----
FROM eclipse-temurin:25-jre AS production
WORKDIR /app

# Copiar JAR
COPY --from=build /app/target/*.jar app.jar

# Cloud Run injeta PORT
ENV PORT=8081
EXPOSE ${PORT}

# Criar usuario non-root
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Cloud Run faz health check via HTTP — sem HEALTHCHECK no Dockerfile
# JVM otimizada para serverless (menos memoria, startup rapido)
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:+UseZGC -XX:+ZGenerational -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Atualizar .dockerignore

```
# Docs e specs
docs/
specs/
*.md
!README.md

# Infra scripts
infra/

# IDE files
.idea/
*.iml

# Git
.git/
.gitignore

# Target (build local)
target/

# Docker
docker-compose*.yml
Dockerfile*
!Dockerfile.native
```

> **NOTA:** O `.dockerignore` usa `!Dockerfile.native` para nao excluir ele mesmo, mas o contexto de build NAO precisa do Dockerfile.

---

## Atualizar Dockerfile existente (comentario)

Adicionar comentario no topo do Dockerfile existente:

```dockerfile
# =====================================================================
# Dockerfile — Desenvolvimento e Build JVM (uso local/OCI)
# =====================================================================
# Para producao no Cloud Run, use:
#   - Dockerfile.native (recomendado — Native Image, ~100ms cold start)
#   - Dockerfile.jvm-cloudrun (fallback — JVM, ~15-30s cold start)
# =====================================================================
```

---

## Validacao

```bash
# Build nativo (pode levar 5-15 minutos na primeira vez)
docker build -f Dockerfile.native -t rpg-api:native .

# Verificar tamanho
docker images rpg-api:native
# Esperado: < 150MB

# Testar localmente (precisa de PostgreSQL rodando)
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/rpg_fichas \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  rpg-api:native

# Medir cold start
time curl http://localhost:8081/actuator/health
# Esperado: < 500ms total (startup + resposta)
```

---

## Criterios de Aceitacao

- [ ] `docker build -f Dockerfile.native` completa sem erros
- [ ] Imagem final < 150MB
- [ ] Container inicia e responde `/actuator/health` com `{"status":"UP"}`
- [ ] Cold start < 500ms medido via `time curl`
- [ ] Se native falhar: `Dockerfile.jvm-cloudrun` funciona como fallback
- [ ] `.dockerignore` exclui docs/specs/infra do contexto de build

---

*Produzido por: Tech Lead | 2026-04-07*
