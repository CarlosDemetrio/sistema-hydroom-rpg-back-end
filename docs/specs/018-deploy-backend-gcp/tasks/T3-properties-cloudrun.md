# T3 — application-prod.properties: Ajustes para Cloud Run

> Fase: Backend/Config | Prioridade: P0
> Dependencias: Nenhuma (paralelo com T1)
> Bloqueia: T5 (Cloud Run setup)
> Estimativa: 0.5 dia

---

## Objetivo

Ajustar `application-prod.properties` para funcionar no modelo serverless do Cloud Run: porta dinamica, pool de conexoes reduzido, Swagger desabilitado, session in-memory otimizada.

---

## Arquivo a Editar

| Arquivo | Acao |
|---------|------|
| `src/main/resources/application-prod.properties` | EDITAR |

---

## Mudancas Detalhadas

### 1. Porta dinamica (Cloud Run injeta via env PORT)

```properties
# ANTES:
# (nao tinha — usava default 8081 via docker-compose.prod.yml)

# DEPOIS:
server.port=${PORT:8081}
```

### 2. Datasource aponta para IP INTERNO da VM (via Direct VPC Egress)

```properties
# ANTES:
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres:5432/rpg_fichas}

# DEPOIS (mesma variavel, default diferente):
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/rpg_fichas}
```

> **NOTA:** O default muda de `postgres` (hostname Docker) para `localhost` (dev local). Em producao, `SPRING_DATASOURCE_URL` e definido no Cloud Run com o IP **INTERNO** da VM (ex: `jdbc:postgresql://10.128.0.2:5432/rpg_fichas`). O Cloud Run acessa via Direct VPC Egress — **NUNCA** usar IP publico.

### 3. Pool HikariCP reduzido (serverless)

```properties
# ANTES:
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2

# DEPOIS:
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=5000
spring.datasource.hikari.idle-timeout=300000
```

> Serverless escala para multiplas instancias. Cada uma abre ate 3 conexoes. A VM e2-micro suporta ~50-100 conexoes PostgreSQL total.

### 4. Desabilitar Swagger/OpenAPI em producao

```properties
# NOVO:
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

> Swagger UI nao funciona bem em native image sem hints extras. Tambem e boa pratica de seguranca desabilitar em prod.

### 5. Session (manter in-memory para MVP)

```properties
# Manter como esta:
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=lax
server.servlet.session.timeout=30m

# NOTA: Cloud Run com min-instances=1 garante que a session sobrevive
# entre requests. Para escala com multiplas instancias, migrar para
# Spring Session JDBC (pos-MVP).
```

### 6. Forward headers (manter — Cloud Run tambem usa)

```properties
# Manter como esta:
server.forward-headers-strategy=framework
```

### 7. CORS — Configuracao CRITICA para seguranca

```properties
# Ja existe — verificar que esta presente e correto:
app.cors.allowed-origins=${FRONTEND_URL:https://seu-dominio.com}
```

> **O que isso garante:**
> - Apenas `https://seu-dominio.com` pode fazer requests a API via navegador
> - Qualquer outro site que tente consumir a API recebe bloqueio CORS
> - `FRONTEND_URL` e injetado via Secret Manager — se mudar dominio, atualizar o secret
> - Credentials=true e necessario para cookies de sessao cross-origin (frontend e backend em dominios diferentes)

---

## application-prod.properties Final

```properties
# Production Profile — Cloud Run + GCP
spring.config.activate.on-profile=prod

# -----------------------------------------------
# Server (Cloud Run injeta PORT)
# -----------------------------------------------
server.port=${PORT:8081}
server.forward-headers-strategy=framework

# -----------------------------------------------
# Database PostgreSQL (VM e2-micro via IP INTERNO VPC)
# Cloud Run acessa via Direct VPC Egress
# Ex: jdbc:postgresql://10.128.0.2:5432/rpg_fichas
# NUNCA usar IP publico!
# -----------------------------------------------
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/rpg_fichas}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=5000
spring.datasource.hikari.idle-timeout=300000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Disable Docker Compose auto-detection in production
spring.docker.compose.enabled=false

# -----------------------------------------------
# Session Configuration (Production - Secure)
# -----------------------------------------------
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=lax
server.servlet.session.timeout=30m

# -----------------------------------------------
# URLs (Production)
# -----------------------------------------------
app.frontend.url=${FRONTEND_URL:https://seu-dominio.com}
app.backend.url=${BACKEND_URL:https://api.seu-dominio.com}

# -----------------------------------------------
# CORS Configuration (Production)
# -----------------------------------------------
app.cors.allowed-origins=${FRONTEND_URL:https://seu-dominio.com}

# -----------------------------------------------
# Logging (Production - Less Verbose)
# -----------------------------------------------
logging.level.root=WARN
logging.level.org.springframework.security=WARN
logging.level.org.springframework.web=WARN
logging.level.br.com.hydroom.rpg=INFO

# -----------------------------------------------
# OAuth2 Google
# -----------------------------------------------
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

# -----------------------------------------------
# Actuator (Production - restritivo)
# -----------------------------------------------
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never

# -----------------------------------------------
# OpenAPI/Swagger (desabilitado em producao)
# -----------------------------------------------
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

---

## Criterios de Aceitacao

- [ ] `./mvnw test` passa com 613+ testes (nenhuma regressao)
- [ ] `./mvnw spring-boot:run` funciona localmente (profile default, nao prod)
- [ ] Property `server.port=${PORT:8081}` resolve corretamente
- [ ] Swagger UI continua disponivel em dev (`application.properties` padrao)
- [ ] Swagger UI desabilitado em prod (`application-prod.properties`)

---

*Produzido por: Tech Lead | 2026-04-07*
