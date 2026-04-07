# T3 — Frontend: Criar `SKIP_ERROR_INTERCEPTOR` HttpContextToken

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: nenhuma
> Bloqueia: T4, T5
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

O `error.interceptor.ts` global dispara toast em qualquer erro HTTP. Em alguns cenarios essa ativacao e indesejavel:
- `authService.getUserInfo()` (chamado pelo `authGuard`) — quando da 401 e esperado e o guard ja redireciona para login. Toast vira ruido.
- Qualquer chamada futura que precise de tratamento de erro custom no proprio componente.

A solucao Angular padrao e usar um `HttpContextToken` para sinalizar ao interceptor que a request deve ser ignorada por ele.

Esta task cria APENAS o token. O consumo no interceptor (T4) e nas chamadas (T5) sao tasks separadas.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/core/tokens/skip-error.token.ts` | CRIAR (novo) |

---

## Passos Sugeridos

### Passo 1 — Criar o diretorio se nao existir

```
src/app/core/tokens/
```

(O diretorio `core/` ja existe se houver outros tokens; senao, criar `core/tokens/`.)

### Passo 2 — Criar o arquivo

```typescript
// src/app/core/tokens/skip-error.token.ts
import { HttpContextToken } from '@angular/common/http';

/**
 * Token para sinalizar ao ErrorInterceptor que a request nao deve disparar
 * toast generico de erro. O caller assume responsabilidade pelo tratamento.
 *
 * Uso:
 * ```ts
 * import { HttpContext } from '@angular/common/http';
 * import { SKIP_ERROR_INTERCEPTOR } from '@core/tokens/skip-error.token';
 *
 * this.http.get('/api/v1/auth/me', {
 *   context: new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true)
 * });
 * ```
 *
 * Default: `false` (interceptor age normalmente).
 */
export const SKIP_ERROR_INTERCEPTOR = new HttpContextToken<boolean>(() => false);
```

### Passo 3 — Validar build

```
cd ficha-controlador-front-end
npx ng build --configuration development
```

Esperado: build OK (token nao consumido ainda, sem efeito colateral).

### Passo 4 — Testes existentes

```
npx vitest run
```

Esperado: 848 testes passando (sem regressao).

---

## Criterios de Aceite

- [ ] Arquivo `src/app/core/tokens/skip-error.token.ts` criado
- [ ] Exporta `SKIP_ERROR_INTERCEPTOR` como `HttpContextToken<boolean>` com default `false`
- [ ] Tem JSDoc com exemplo de uso
- [ ] `npx ng build` compila sem erro
- [ ] `npx vitest run` continua 848/848 (sem regressao)

---

## Notas

- NAO consumir o token no interceptor nesta task. Isso e responsabilidade da T4.
- NAO consumir o token em servicos nesta task. Isso e responsabilidade da T5.
- O nome do diretorio `core/` segue convencao Angular para utilitarios cross-feature. Se o projeto ja usar outra convencao (`shared/tokens/`, `services/tokens/`), seguir a do projeto.

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P3 (linha 84-90)
- Angular docs: https://angular.dev/api/common/http/HttpContextToken
