# T4 — Frontend: Refatorar `error.interceptor.ts` (401/403/0/500)

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: T1 (backend devolve 401), T3 (token criado)
> Bloqueia: T12, T14, T-DOC1
> Estimativa: 2h
> Agente sugerido: angular-tech-lead

---

## Contexto

O `error.interceptor.ts` atual tem 3 problemas:

1. **Toast disparado para 401/403**: o `switch` define `errorMessage` mas FORA do switch, em todo caminho, chama `errorHandler.handleError(errorMessage)` (linha 33-63). Resultado: o usuario ve toast generico mesmo quando o interceptor decide redirecionar para `/login`.

2. **`status === 0` cai no `default:`**: quando o backend devolve 302 (T1 corrigiu) ou ha problema de rede/CORS, o `error.status` e 0 e nenhum branch trata isso especificamente. O usuario ve "Erro 0:" — sintoma do PO.

3. **Sem opt-out**: nao respeita o `SKIP_ERROR_INTERCEPTOR` token (T3 criou).

Esta task refatora o interceptor para tratar cada caso de forma adequada.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/interceptors/error.interceptor.ts` | REESCREVER |

---

## Passos Sugeridos

### Passo 1 — Ler o estado atual

```
ficha-controlador-front-end/src/app/interceptors/error.interceptor.ts
```

Identificar a estrutura atual (`switch` + `handleError` global).

### Passo 2 — Refatorar com novo contrato

Estrutura proposta:

```typescript
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { SKIP_ERROR_INTERCEPTOR } from '../core/tokens/skip-error.token';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toast = inject(ToastService);

  const skip = req.context.get(SKIP_ERROR_INTERCEPTOR);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (skip) {
        // Caller assume responsabilidade — apenas propaga
        return throwError(() => error);
      }

      switch (error.status) {
        case 401: {
          // Sessao expirada ou nao autenticado
          // Salvar URL atual no auth.guard (T5 cuida disso)
          toast.info('Sua sessao expirou. Faca login novamente.');
          router.navigate(['/login']);
          break;
        }
        case 403: {
          // Sem permissao — tela ja informa
          router.navigate(['/unauthorized']);
          break;
        }
        case 0: {
          // Rede/CORS/offline
          toast.error('Sem conexao com o servidor. Verifique sua internet.');
          break;
        }
        case 404: {
          // Recurso nao encontrado — toast leve
          toast.warn('Recurso nao encontrado.');
          break;
        }
        default: {
          // 4xx/5xx genericos
          const message = error.error?.message
            ?? error.error?.error
            ?? `Erro ${error.status}: ${error.statusText || 'Erro inesperado'}`;
          toast.error(message);
          break;
        }
      }

      return throwError(() => error);
    })
  );
};
```

### Passo 3 — Confirmar API do `ToastService`

Verificar se `ToastService` ja tem metodos `info`, `warn`, `error`. Se nao tiver `info`, adicionar (provavelmente `severity: 'info'` no `messageService.add`).

### Passo 4 — Remover dependencia de `ErrorHandlerService`

Importante: o `ErrorHandlerService` era um wrapper raso. Esta refatoracao **deixa de chamar** `errorHandler.handleError()`. NAO remover o arquivo `error-handler.service.ts` ainda (T17 P3 cuida disso para nao quebrar outros consumidores).

### Passo 5 — Rodar testes existentes do interceptor

```
cd ficha-controlador-front-end
npx vitest run src/app/interceptors/
```

Possivel cenario: testes existentes podem quebrar porque mudaram as expectativas (interceptor agora chama `toast.info` em vez de `errorHandler.handleError` para 401). Atualizar testes existentes ou marcar para refazer em T14.

### Passo 6 — Suite completa

```
npx vitest run
```

Esperado: 848 testes (talvez algumas atualizacoes minimas em testes existentes do interceptor).

---

## Criterios de Aceite

- [ ] `error.interceptor.ts` refatorado conforme estrutura proposta
- [ ] Respeita `SKIP_ERROR_INTERCEPTOR` (early return)
- [ ] 401 → `toast.info('Sessao expirou')` + navega `/login`
- [ ] 403 → navega `/unauthorized` (sem toast)
- [ ] 0 → `toast.error('Sem conexao')`
- [ ] 404 → `toast.warn('Recurso nao encontrado')`
- [ ] 4xx/5xx default → `toast.error(message)` com extracao de `error.error.message`
- [ ] NAO chama mais `errorHandler.handleError()`
- [ ] `npx vitest run` passa (com possiveis atualizacoes em testes existentes)

---

## Notas

- Esta refatoracao assume que **T1 ja foi executada** — o backend agora devolve 401 real. Se T1 nao foi feita, o caso 401 nunca sera atingido (continua caindo em 0).
- A logica de salvar `state.url` em sessionStorage fica em T5 (`auth.guard`), nao aqui.
- Testes detalhados (cobrir todos os branches) ficam em T14 (P2). Esta task pode atualizar testes existentes minimamente para nao quebrar.
- Se o `ToastService.info` nao existir, adicionar:
  ```typescript
  info(message: string) {
    this.messageService.add({ severity: 'info', summary: 'Aviso', detail: message, life: 4000 });
  }
  ```

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P2 (linha 54-68) e Tasks Sugeridas T3 (linha 179-185)
- T1 (backend devolvendo 401)
- T3 (token criado)
