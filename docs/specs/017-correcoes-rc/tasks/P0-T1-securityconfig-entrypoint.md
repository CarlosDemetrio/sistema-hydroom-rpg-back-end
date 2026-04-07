# T1 — Backend: SecurityConfig HttpStatusEntryPoint para `/api/**`

> Fase: Backend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: nenhuma
> Bloqueia: T2, T4, T14
> Estimativa: 1h
> Agente sugerido: java-spring-tech-lead

---

## Contexto

O `SecurityConfig.filterChain()` configura `oauth2Login()` sem nenhum `AuthenticationEntryPoint` customizado. Por padrao, o Spring Security instala o `LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google")`. Quando uma requisicao XHR/fetch para `/api/**` chega sem sessao valida, o Spring devolve **HTTP 302** com `Location: /oauth2/authorization/google` em vez de **HTTP 401**.

O browser tenta seguir o redirect cross-origin (Google), falha por CORS, e a chamada Angular cai com `error.status === 0`. O `error.interceptor.ts` cai no branch `default:` exibindo "Erro 0:" — o sintoma do PO ("erro interno quando expira sessao").

A correcao e instalar um `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` aplicado APENAS a `/api/**`, deixando o fluxo OAuth2 tradicional intacto para `/oauth2/**` e `/login`.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `src/main/java/br/com/hydroom/rpg/fichacontrolador/config/SecurityConfig.java:42-51` | Adicionar `.exceptionHandling(...)` no `HttpSecurity` |

---

## Passos Sugeridos

### Passo 1 — Adicionar imports

```java
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
```

### Passo 2 — Adicionar bloco `exceptionHandling`

No `filterChain(HttpSecurity http)`, antes ou depois do `oauth2Login(...)`, adicionar:

```java
.exceptionHandling(ex -> ex
    .defaultAuthenticationEntryPointFor(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
        new AntPathRequestMatcher("/api/**")
    )
)
```

Isso instrui o Spring Security a usar o `HttpStatusEntryPoint(401)` apenas quando a request casar com `/api/**`. Para qualquer outra rota (ex: `/oauth2/authorization/google`, `/login`), o entry point padrao do `oauth2Login()` continua valendo.

### Passo 3 — Validacao manual

1. Subir `./mvnw spring-boot:run`
2. Curl sem cookie:
   ```
   curl -i http://localhost:8080/api/v1/jogos
   ```
   Esperado: `HTTP/1.1 401 Unauthorized` (nao 302)
3. Curl em rota OAuth2:
   ```
   curl -i http://localhost:8080/oauth2/authorization/google
   ```
   Esperado: `HTTP/1.1 302 Found` (continua redirecionando)

### Passo 4 — Build

```
./mvnw package -DskipTests
```

Garantir que compila.

---

## Criterios de Aceite

- [ ] `SecurityConfig.filterChain` tem o bloco `exceptionHandling` com `HttpStatusEntryPoint(401)` aplicado a `/api/**`
- [ ] Curl manual em `/api/v1/jogos` sem sessao retorna 401 (nao 302)
- [ ] Curl em `/oauth2/authorization/google` ainda retorna 302 (fluxo OAuth2 intacto)
- [ ] `./mvnw package -DskipTests` compila sem erro
- [ ] Sem mudanca em outras configuracoes (`csrf`, `cors`, `authorizeHttpRequests`)

---

## Notas

- A unica mudanca neste arquivo deve ser o bloco `exceptionHandling`. NAO mexer em `csrf`, `cors`, `authorizeHttpRequests` ou `oauth2Login`.
- O teste de integracao automatizado vai na T2 (separado para granularidade).
- Esta task desbloqueia T4 (refactor do `error.interceptor` no frontend) que pode entao tratar 401 como caso esperado.

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P1 (linha 26-50)
- `src/main/java/br/com/hydroom/rpg/fichacontrolador/exception/GlobalExceptionHandler.java:82-99` (handler que NUNCA e acionado hoje)
