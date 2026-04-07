# T14 — Frontend: Testes completos do `error.interceptor`

> Fase: Frontend | Prioridade: P2 (POS-RC)
> Dependencias: T4 (interceptor refatorado)
> Bloqueia: nenhuma
> Estimativa: 2h
> Agente sugerido: angular-tech-lead

---

## Contexto

O `error.interceptor.ts` foi refatorado em T4 com 5 branches distintos (401, 403, 0, 404, 4xx/5xx default) e suporte a `SKIP_ERROR_INTERCEPTOR`. Esta task cria uma suite de testes dedicada que cobre todos os branches e evita regressoes.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/interceptors/error.interceptor.spec.ts` | CRIAR ou ATUALIZAR |

---

## Passos Sugeridos

### Passo 1 — Template do teste

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { ToastService } from '../services/toast.service';
import { SKIP_ERROR_INTERCEPTOR } from '../core/tokens/skip-error.token';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let toastService: { info: ReturnType<typeof vi.fn>; error: ReturnType<typeof vi.fn>; warn: ReturnType<typeof vi.fn>; success: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    toastService = {
      info: vi.fn(),
      error: vi.fn(),
      warn: vi.fn(),
      success: vi.fn()
    };
    router = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: ToastService, useValue: toastService },
        { provide: Router, useValue: router }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  describe('401 Unauthorized', () => {
    it('dispara toast.info e navega para /login', () => {
      http.get('/api/v1/jogos').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos').flush(null, { status: 401, statusText: 'Unauthorized' });

      expect(toastService.info).toHaveBeenCalledWith(expect.stringContaining('sess'));
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      expect(toastService.error).not.toHaveBeenCalled();
    });
  });

  describe('403 Forbidden', () => {
    it('navega para /unauthorized SEM toast', () => {
      http.get('/api/v1/jogos').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos').flush(null, { status: 403, statusText: 'Forbidden' });

      expect(router.navigate).toHaveBeenCalledWith(['/unauthorized']);
      expect(toastService.error).not.toHaveBeenCalled();
      expect(toastService.info).not.toHaveBeenCalled();
    });
  });

  describe('status 0 (rede/CORS)', () => {
    it('dispara toast.error com mensagem de conexao', () => {
      http.get('/api/v1/jogos').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos').error(new ProgressEvent('error'), { status: 0 });

      expect(toastService.error).toHaveBeenCalledWith(expect.stringContaining('conex'));
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('404 Not Found', () => {
    it('dispara toast.warn', () => {
      http.get('/api/v1/jogos/999').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos/999').flush(null, { status: 404, statusText: 'Not Found' });

      expect(toastService.warn).toHaveBeenCalled();
    });
  });

  describe('500 Internal Server Error', () => {
    it('dispara toast.error com mensagem do backend', () => {
      http.get('/api/v1/jogos').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos').flush(
        { message: 'Erro de banco' },
        { status: 500, statusText: 'Server Error' }
      );

      expect(toastService.error).toHaveBeenCalledWith('Erro de banco');
    });

    it('usa mensagem generica quando backend nao fornece', () => {
      http.get('/api/v1/jogos').subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/jogos').flush(null, { status: 500, statusText: 'Server Error' });

      expect(toastService.error).toHaveBeenCalledWith(expect.stringContaining('500'));
    });
  });

  describe('SKIP_ERROR_INTERCEPTOR', () => {
    it('nao dispara toast quando token e true', () => {
      const ctx = new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true);
      http.get('/api/v1/auth/me', { context: ctx }).subscribe({ error: () => {} });
      httpMock.expectOne('/api/v1/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });

      expect(toastService.info).not.toHaveBeenCalled();
      expect(toastService.error).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
```

### Passo 2 — Rodar testes

```
cd ficha-controlador-front-end
npx vitest run src/app/interceptors/error.interceptor.spec.ts
```

Esperado: todos os testes passando.

### Passo 3 — Suite completa

```
npx vitest run
```

Esperado: 848 + novos testes = 855+ passando.

---

## Criterios de Aceite

- [ ] Arquivo `error.interceptor.spec.ts` tem pelo menos 7 testes (1 por branch + 1 de SKIP)
- [ ] Cobre 401, 403, 0, 404, 500 com mensagem, 500 sem mensagem, SKIP
- [ ] Todos os testes passam
- [ ] Cobertura de codigo do interceptor: 100% das linhas

---

## Notas

- Se o `ToastService` tiver mais metodos (ex: `success`), mockar todos para evitar undefined
- O teste de SKIP e o mais importante para prevenir regressoes onde alguem remove acidentalmente o early-return
- Esta task e POS-RC, mas se o agente tiver tempo apos T4, pode ser puxada pra dentro

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § Recomendacoes Adicionais (linha 159-160)
- T4 (interceptor refatorado)
