# Auditoria — Gestão de Rotas e Tratamento de Erros (Frontend)

**Data**: 2026-04-07
**Auditor**: angular-tech-lead
**Escopo**: Frontend Angular 21 — interceptors, guards, rotas e tratamento de 401/403/erros genéricos.
**Repositório**: `ficha-controlador-front-end`

---

## Sumário Executivo

O fluxo de tratamento de erros HTTP do frontend tem um desenho razoável na intenção (interceptor centraliza erros, guard protege rotas, toast service centralizado), mas contém **três problemas críticos interligados** que produzem o sintoma observado pelo PO ("quando dá erro de não estar logado, aparece mensagem de erro interno do servidor"):

1. **O backend NÃO devolve 401 quando a sessão expira**. O `SecurityConfig` não configura um `AuthenticationEntryPoint` customizado. Com `oauth2Login()`, o Spring Security instala por padrão o `LoginUrlAuthenticationEntryPoint` que devolve **302 redirecionando para `/oauth2/authorization/google`** em vez de 401. Para uma requisição XHR/fetch com `withCredentials: true` isso vira erro de CORS (`status: 0`, texto vazio) no navegador — e o `error.interceptor.ts` cai no branch `default:`, exibindo uma mensagem genérica estilo "Erro 0: " ou despachando o texto HTML de uma página de erro Spring. Esse é o caminho mais provável para o "erro interno do servidor" que o PO vê.

2. **O `error.interceptor.ts` dispara toast para 401/403 mesmo quando o interceptor já decide redirecionar**, resultando em "toast feio + navegação para login" simultâneos. Pior: como o `error.status` para sessão expirada é 0 (não 401), o interceptor nem sequer identifica que é caso de auth, então NÃO redireciona e NÃO mostra a mensagem amigável "Sessão expirada" — mostra a genérica do `default:`.

3. **Muitos componentes também chamam `toastService.error(...)` no `error:` do subscribe**, em cima do toast que o interceptor já dispara. Isso gera **double toast** em erros comuns de CRUD e contribui para a percepção de mensagens de erro confusas/ruidosas.

Adicionalmente, a rota pública `/login` está no topo, o `authGuard` está no `MainLayoutComponent` pai e o callback do OAuth2 limpa o `REDIRECT_URL` muito cedo, então a rota originalmente pedida é perdida quando a sessão expira no meio da navegação.

---

## Problemas Identificados

### P1 — Backend devolve 302 (redirect OAuth2) em vez de 401 quando a sessão expira

- **Severidade**: CRÍTICA
- **Sintoma observado**: "Quando dá erro de não estar logado, aparece mensagem de erro interno do servidor". O usuário NÃO é levado à tela de login de forma limpa; em vez disso vê um toast genérico.
- **Causa-raiz**:
  - `SecurityConfig.filterChain()` configura `oauth2Login()` sem `exceptionHandling().authenticationEntryPoint(...)`.
  - Com isso, o Spring Security escolhe automaticamente `LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google")`. Qualquer requisição não autenticada a um endpoint sob `.anyRequest().authenticated()` recebe **HTTP 302** com `Location: /oauth2/authorization/google`.
  - O browser, quando a chamada é XHR com `credentials: 'include'`, tenta seguir o redirect. Como o destino leva a `accounts.google.com` (outra origem) sem CORS, a chamada falha com `status: 0` / opaque redirect → o `HttpErrorResponse` do Angular tem `status: 0`.
  - No `error.interceptor.ts` isso cai no `default:` e exibe "Erro 0: ". O `@ExceptionHandler(AuthenticationException.class)` do `GlobalExceptionHandler` **nunca é invocado** porque a falha ocorre no filtro de segurança, antes do dispatcher MVC.
- **Arquivos envolvidos**:
  - `src/main/java/br/com/hydroom/rpg/fichacontrolador/config/SecurityConfig.java:42-51` (bloco `authorizeHttpRequests` sem entry point)
  - `src/main/java/br/com/hydroom/rpg/fichacontrolador/exception/GlobalExceptionHandler.java:82-99` (handler existe mas nunca é acionado para auth)
  - `ficha-controlador-front-end/src/app/interceptors/error.interceptor.ts:33-50` (switch não cobre `status === 0`)
- **Correção proposta**:
  - **Backend**: Adicionar em `SecurityConfig`:
    ```java
    .exceptionHandling(ex -> ex
        .defaultAuthenticationEntryPointFor(
            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
            new AntPathRequestMatcher("/api/**")
        )
    )
    ```
    Isso devolve 401 JSON puro para qualquer `/api/**` não autenticado e mantém o fluxo tradicional de redirect OAuth2 apenas para navegação de páginas (que o frontend nem usa, já que é SPA desacoplada).
  - **Frontend**: No `error.interceptor.ts`, tratar `error.status === 0` como potencialmente problema de auth/CORS — mas idealmente após o fix do backend, 401 real será entregue e o branch `case 401` passará a funcionar.

---

### P2 — `error.interceptor.ts` mostra toast genérico MESMO em 401/403 e disputa com redirect

- **Severidade**: ALTA
- **Sintoma observado**: Ao expirar sessão, usuário vê um toast de erro sobre ser redirecionado (conflito visual) e em alguns casos vê só o toast genérico sem redirecionamento algum.
- **Causa-raiz**: `error.interceptor.ts:33-63` executa o `switch` para determinar `errorMessage` e **em seguida, fora do switch**, chama SEMPRE `errorHandler.handleError(errorMessage)` — inclusive para 401/403 (linha 63). O `ErrorHandlerService.handleError()` (`src/app/services/error-handler.service.ts:18-25`) sempre dispara toast, sem distinguir gravidade. Assim o redirect para `/login` e o toast "Sessão expirada" são ambos disparados, e quando o backend dá 302→0 (P1), o toast sequer identifica como auth e cai no `default:`.
- **Arquivos envolvidos**:
  - `ficha-controlador-front-end/src/app/interceptors/error.interceptor.ts:24-66`
  - `ficha-controlador-front-end/src/app/services/error-handler.service.ts:18-25`
- **Correção proposta**:
  - Refatorar o interceptor para:
    1. Tratar 401 (após P1 estar resolvido) sem chamar `errorHandler.handleError()`: apenas redirecionar para `/login` e, se quiser feedback visual, chamar `toastService.info('Sessão expirada')` com severidade leve.
    2. Tratar 403 apenas navegando para `/unauthorized` sem toast redundante (a tela já informa o motivo).
    3. Para `status === 0` (rede/CORS/offline), mostrar toast "Sem conexão com o servidor" em vez do `default:` confuso.
    4. Só chamar `errorHandler.handleError()` para `status >= 500` ou erros realmente genéricos.
  - Introduzir `HttpContextToken` (`SKIP_ERROR_INTERCEPTOR`) para permitir que chamadas como `authService.getUserInfo()` (usada pelo `authGuard`) passem sem disparar toast — o guard já redireciona sozinho.

---

### P3 — `authService.getUserInfo()` passa pelo `error.interceptor` disparando toast no primeiro load

- **Severidade**: ALTA
- **Sintoma observado**: Ao entrar no app sem sessão, o `authGuard` chama `/api/v1/auth/me`, o backend devolve 401/302, o interceptor dispara toast genérico antes do guard conseguir redirecionar para `/login`. O usuário vê um "erro" logo na primeira tela.
- **Causa-raiz**:
  - `guards/auth.guard.ts:10-24` invoca `authService.getUserInfo()` — sem opt-out de interceptor.
  - `services/auth.service.ts:42-46` emite `GET /api/v1/auth/me`.
  - Passando pelo `error.interceptor.ts` qualquer erro vira toast.
- **Arquivos envolvidos**:
  - `ficha-controlador-front-end/src/app/guards/auth.guard.ts:10`
  - `ficha-controlador-front-end/src/app/services/auth.service.ts:42`
- **Correção proposta**:
  - Criar `HttpContextToken<boolean>` em `core/tokens/skip-error.token.ts`:
    ```ts
    export const SKIP_ERROR_INTERCEPTOR = new HttpContextToken<boolean>(() => false);
    ```
  - No `error.interceptor.ts`, checar `req.context.get(SKIP_ERROR_INTERCEPTOR)` e pular o toast quando true (ainda propagando o erro).
  - No `auth.service.ts`, passar `{ context: new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true) }` no `getUserInfo()`.
  - Alternativamente, verificar a URL: se terminar em `/auth/me`, não dispara toast.

---

### P4 — Double toast: componentes chamam `toastService.error()` EM CIMA do interceptor

- **Severidade**: ALTA
- **Sintoma observado**: Usuário vê dois toasts sobrepostos em erros de CRUD (um genérico do interceptor + um específico do componente).
- **Causa-raiz**: 19 componentes implementam `error: () => this.toastService.error('Erro ao X')` no `subscribe`, mas o `error.interceptor.ts` já chamou `errorHandler.handleError()` → `toastService.error()` antes do callback do componente ser chamado. Exemplos:
  - `features/mestre/pages/jogos-list/jogos-list.component.ts:281-292`
  - `features/jogador/pages/fichas-list/fichas-list.component.ts`
  - ~17 configs em `features/mestre/pages/config/configs/**`
- **Arquivos envolvidos**: ver lista completa via `grep toastService\.error src/app/features`.
- **Correção proposta**:
  - Definir contrato: **o interceptor é a única fonte de toasts de erro HTTP genéricos**. Componentes só usam `toastService.error()` para erros de validação local ou regras de negócio que não são HTTP.
  - Remover os callbacks `error: () => this.toastService.error(...)` dos 19 componentes OU, se precisarem de mensagem específica por contexto (ex: "Erro ao excluir jogo" vs. "Erro ao criar"), usar o `HttpContext` `SKIP_ERROR_INTERCEPTOR` + toast local.
  - Documentar a regra em `CLAUDE.md` do frontend.

---

### P5 — Rota originalmente pedida é perdida quando sessão expira durante navegação

- **Severidade**: MÉDIA
- **Sintoma observado**: Usuário estava em `/mestre/config/atributos` e a sessão expirou. Ele é redirecionado para `/login`; após logar, vai para `/dashboard` em vez de voltar para onde estava.
- **Causa-raiz**:
  - `auth.guard.ts:15` navega para `/login` mas não salva `state.url` em `sessionStorage` antes.
  - `auth.service.ts:49-57`: só salva `REDIRECT_URL` quando o usuário clica no botão "Login with Google" — portanto, para sessão expirada no meio da sessão, não há `REDIRECT_URL` armazenado.
  - `oauth-callback.component.ts:24-25` lê e limpa `REDIRECT_URL`, então cai no fallback `/dashboard`.
- **Arquivos envolvidos**:
  - `ficha-controlador-front-end/src/app/guards/auth.guard.ts:6-24`
  - `ficha-controlador-front-end/src/app/services/auth.service.ts:48-57`
- **Correção proposta**: No `authGuard`, antes de redirecionar, salvar `state.url` em `sessionStorage.setItem('REDIRECT_URL', state.url)`. Assinatura já recebe o segundo parâmetro (`_state`), basta usar.

---

### P6 — `setTimeout(1000)` no OAuthCallback para aguardar backend + error handling precário

- **Severidade**: MÉDIA
- **Sintoma observado**: Tela de "Autenticando…" presa 1 segundo fixo; se a chamada falhar o usuário volta para `/login` mas sem mensagem do motivo (o toast que aparece é o genérico do `error.interceptor`).
- **Causa-raiz**: `oauth-callback.component.ts:28-40` usa `setTimeout(1000)` como workaround para garantir que o cookie de sessão esteja disponível. O `error:` do subscribe navega para `/login` mas não diz nada ao usuário.
- **Arquivos envolvidos**: `ficha-controlador-front-end/src/app/pages/oauth-callback/oauth-callback.component.ts`
- **Correção proposta**: Remover o `setTimeout` (cookie já estará disponível após o redirect). Usar retry leve (`retry({ count: 2, delay: 300 })`) caso haja race condition. Em caso de erro, mostrar toast informativo antes de navegar.

---

### P7 — Loading interceptor mantém spinner durante redirects/erros silenciosos

- **Severidade**: BAIXA
- **Sintoma observado**: Eventualmente spinner "gruda" quando há erro rápido em cadeia (ex: guard dispara, usuário já navegou para `/login`).
- **Causa-raiz**: `loading.interceptor.ts:27-29` usa `finalize()` que funciona, mas o `LoadingService` usa contador aberto; se uma nova navegação dispara antes do `finalize` rodar, o contador pode ficar positivo.
- **Arquivos envolvidos**: `ficha-controlador-front-end/src/app/interceptors/loading.interceptor.ts`, `services/loading.service.ts`
- **Correção proposta**: Verificar se `LoadingService` tem `reset()` e chamar quando há navegação de erro. Baixa prioridade — nice-to-fix.

---

### P8 — Rota `**` leva para `/404`, mas rota `/404` é pública (sem auth guard)

- **Severidade**: BAIXA
- **Sintoma observado**: Usuário não autenticado que digita URL errada vê a página 404 sem ser redirecionado para login. Pode ser um comportamento desejável, mas é inconsistente com o resto da app.
- **Causa-raiz**: `app.routes.ts:203-209` — `/404` e `/unauthorized` estão fora do bloco com `authGuard`.
- **Arquivos envolvidos**: `ficha-controlador-front-end/src/app/app.routes.ts:198-209`
- **Correção proposta**: Decisão de produto. Se for intencional, documentar. Se não, mover `404` e `unauthorized` para dentro do bloco autenticado ou criar um layout público para essas rotas.

---

## Recomendações Adicionais

1. **Padronizar contrato de erros HTTP** em documento curto em `CLAUDE.md` do frontend: "interceptor cuida de toast genérico; componentes nunca duplicam; use `SKIP_ERROR_INTERCEPTOR` para casos especiais".

2. **Adicionar testes de interceptor** cobrindo: 401 (sem toast, com redirect), 403 (sem toast, com redirect), 500 (com toast), status 0 (toast "sem conexão"), `SKIP_ERROR_INTERCEPTOR` set (sem toast).

3. **Testar E2E de sessão expirada** com Cypress/Playwright — invalidar o cookie de sessão e verificar que o usuário é levado para `/login` com toast amigável (não "erro interno").

4. **Remover `ErrorHandlerService` ou refatorá-lo**: hoje é só um wrapper fino do `ToastService` que acrescenta confusão. Considerar removê-lo e usar `ToastService` direto no interceptor com lógica de severidade.

5. **Considerar `loadComponent` para `LoginComponent` e `NotFoundComponent`** — ambas são importadas eager em `app.routes.ts:5-11`, o que adiciona peso ao bundle inicial.

6. **Auditar CSRF token refresh**: `auth.interceptor.ts:10` lê `XSRF-TOKEN` de cookie; se a sessão expirar, o CSRF também expira. Na primeira request autenticada pós-login, o CSRF pode estar desatualizado. Verificar se há refresh automático.

---

## Tasks Sugeridas para Spec Corretiva

1. **T1 — Backend: devolver 401 JSON em `/api/**` não autenticado**
   Arquivo: `SecurityConfig.java`. Adicionar `exceptionHandling().defaultAuthenticationEntryPointFor(HttpStatusEntryPoint(401), AntPathRequestMatcher("/api/**"))`. Adicionar teste de integração: `mockMvc.perform(get("/api/v1/jogos")).andExpect(status().isUnauthorized())`.

2. **T2 — Frontend: criar `SKIP_ERROR_INTERCEPTOR` HttpContextToken**
   Novo arquivo em `src/app/core/tokens/skip-error.token.ts`. Consumir no `error.interceptor.ts` para pular toast quando presente.

3. **T3 — Frontend: refatorar `error.interceptor.ts`**
   - 401 → navigate `/login` + toast INFO "Sessão expirada" (não error).
   - 403 → navigate `/unauthorized`, sem toast.
   - 0 → toast "Sem conexão com o servidor".
   - 4xx/5xx → toast error via ToastService direto (remover ErrorHandlerService do caminho).
   - Respeitar `SKIP_ERROR_INTERCEPTOR`.
   - Testes unitários para cada branch.

4. **T4 — Frontend: `auth.guard.ts` salvar `state.url` em `REDIRECT_URL`**
   Antes de `router.navigate(['/login'])`, `sessionStorage.setItem('REDIRECT_URL', state.url)`. Atualizar testes do guard.

5. **T5 — Frontend: `authService.getUserInfo()` usar `SKIP_ERROR_INTERCEPTOR`**
   O guard não deve disparar toast quando o usuário simplesmente não está logado. Passar `HttpContext` ao `http.get`.

6. **T6 — Frontend: remover `toastService.error()` duplicado dos componentes**
   19 componentes identificados. Apenas remover o callback `error` dos subscribes (ou substituir por tratamento específico com `SKIP_ERROR_INTERCEPTOR` + toast local).

7. **T7 — Frontend: melhorar `oauth-callback.component.ts`**
   Remover `setTimeout(1000)` e usar `retry({ count: 2, delay: 300 })`. Mostrar toast informativo em caso de falha antes de navegar.

8. **T8 — Frontend: adicionar testes de interceptor**
   Cobrir 401, 403, 500, status 0, `SKIP_ERROR_INTERCEPTOR`, e double-toast regression.

9. **T9 — Backend: teste de integração para `AuthenticationEntryPoint`**
   Verificar que `GET /api/v1/jogos` sem sessão retorna 401 JSON (não 302).

10. **T10 — Documentação: atualizar `CLAUDE.md` do frontend**
    Adicionar seção "Tratamento de erros HTTP" com o contrato interceptor-vs-componente.

---

## Arquivos Auditados

- `ficha-controlador-front-end/src/app/interceptors/auth.interceptor.ts`
- `ficha-controlador-front-end/src/app/interceptors/error.interceptor.ts`
- `ficha-controlador-front-end/src/app/interceptors/loading.interceptor.ts`
- `ficha-controlador-front-end/src/app/guards/auth.guard.ts`
- `ficha-controlador-front-end/src/app/guards/role.guard.ts`
- `ficha-controlador-front-end/src/app/guards/current-game.guard.ts`
- `ficha-controlador-front-end/src/app/app.routes.ts`
- `ficha-controlador-front-end/src/app/app.config.ts`
- `ficha-controlador-front-end/src/app/services/auth.service.ts`
- `ficha-controlador-front-end/src/app/services/error-handler.service.ts`
- `ficha-controlador-front-end/src/app/services/toast.service.ts`
- `ficha-controlador-front-end/src/app/pages/login/login.component.ts`
- `ficha-controlador-front-end/src/app/pages/oauth-callback/oauth-callback.component.ts`
- `ficha-controlador-front-end/src/app/shared/layout/main-layout.component.ts`
- `ficha-controlador/src/main/java/.../config/SecurityConfig.java`
- `ficha-controlador/src/main/java/.../controller/AuthController.java`
- `ficha-controlador/src/main/java/.../exception/GlobalExceptionHandler.java`
