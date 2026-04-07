# T5 — Frontend: `auth.guard` salva `state.url` + `getUserInfo` skip

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: T3 (token SKIP)
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

Dois problemas combinados nesta task (sao pequenos e tocam os mesmos arquivos):

### Problema A — `authGuard` perde a URL pretendida
Quando a sessao expira durante a navegacao, o `auth.guard.ts:15` redireciona para `/login` SEM salvar a `state.url`. Resultado: apos re-login, o `oauth-callback.component.ts` cai no fallback `/dashboard` em vez de voltar para a tela de origem (ex: `/mestre/config/atributos`).

### Problema B — `authService.getUserInfo()` dispara toast no primeiro load
O `auth.guard` chama `authService.getUserInfo()` para verificar se ha sessao. Quando nao ha sessao, o backend agora devolve 401 (T1) e o `error.interceptor` (T4) dispara `toast.info('Sessao expirou')`. Mas no PRIMEIRO load do app, o usuario nem sequer estava logado — esse toast e ruido.

A solucao e passar `SKIP_ERROR_INTERCEPTOR=true` no `getUserInfo()` para que o interceptor NAO dispare toast nesse caso especifico. O guard ja redireciona sozinho.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/guards/auth.guard.ts:6-24` | Adicionar `sessionStorage.setItem('REDIRECT_URL', state.url)` antes do redirect |
| `ficha-controlador-front-end/src/app/services/auth.service.ts:42-46` | `getUserInfo()` passar `HttpContext` com `SKIP_ERROR_INTERCEPTOR=true` |

---

## Passos Sugeridos

### Passo 1 — Atualizar `auth.guard.ts`

Antes:
```typescript
export const authGuard: CanActivateFn = (route, state) => {
  // ...
  if (!user) {
    router.navigate(['/login']);
    return false;
  }
  // ...
};
```

Depois:
```typescript
export const authGuard: CanActivateFn = (route, state) => {
  // ...
  if (!user) {
    // Salvar URL pretendida para redirect pos-login
    if (state.url && state.url !== '/login') {
      sessionStorage.setItem('REDIRECT_URL', state.url);
    }
    router.navigate(['/login']);
    return false;
  }
  // ...
};
```

### Passo 2 — Atualizar `auth.service.ts` `getUserInfo()`

Antes:
```typescript
getUserInfo(): Observable<User> {
  return this.http.get<User>('/api/v1/auth/me');
}
```

Depois:
```typescript
import { HttpContext } from '@angular/common/http';
import { SKIP_ERROR_INTERCEPTOR } from '@core/tokens/skip-error.token';
// ...

getUserInfo(): Observable<User> {
  return this.http.get<User>('/api/v1/auth/me', {
    context: new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true)
  });
}
```

### Passo 3 — Verificar `oauth-callback.component.ts`

O componente ja le `REDIRECT_URL` do sessionStorage (linha 24-25 segundo o relatorio). Confirmar que essa logica continua funcionando: apos login, o callback deve ler `REDIRECT_URL`, navegar para ela e remover do storage.

### Passo 4 — Testes manuais

1. Logado, navegar para `/mestre/config/atributos`
2. Limpar cookie de sessao manualmente (DevTools → Application → Cookies → delete `JSESSIONID`)
3. Refresh da pagina
4. Esperado: redirecionado para `/login` sem toast generico
5. Apos login: voltar para `/mestre/config/atributos` (nao `/dashboard`)

### Passo 5 — Rodar testes existentes

```
cd ficha-controlador-front-end
npx vitest run src/app/guards/
npx vitest run src/app/services/auth.service.spec.ts
```

Atualizar testes se a assinatura mudou. Adicionar teste novo: "ao redirecionar sem usuario, salva state.url no sessionStorage".

---

## Criterios de Aceite

- [ ] `auth.guard.ts` salva `state.url` em `sessionStorage.setItem('REDIRECT_URL', ...)` antes de `router.navigate(['/login'])`
- [ ] Nao salva quando `state.url === '/login'` (evitar loop)
- [ ] `auth.service.getUserInfo()` passa `HttpContext` com `SKIP_ERROR_INTERCEPTOR=true`
- [ ] Validacao manual: cenario "expirar sessao em /mestre/config/atributos" funciona — usuario volta a essa rota apos login
- [ ] Testes existentes passam
- [ ] Pelo menos 1 teste novo no `auth.guard.spec.ts` cobrindo o salvamento de `REDIRECT_URL`

---

## Notas

- O nome `REDIRECT_URL` ja e usado pelo `oauth-callback.component.ts` — manter consistencia
- Verificar o path de import correto para `SKIP_ERROR_INTERCEPTOR` (tsconfig paths podem usar `@core/tokens/...` ou `../../core/tokens/...`)
- Esta task NAO mexe no `oauth-callback.component.ts` — esse fica em T13 (P2)

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P3 (linha 72-90) e § P5 (linha 110-121)
- T3 (token criado)
- T4 (interceptor refatorado, mas esta task funciona mesmo se T4 ainda nao tiver rodado — apenas o `getUserInfo` skip nao tera efeito sem T4)
