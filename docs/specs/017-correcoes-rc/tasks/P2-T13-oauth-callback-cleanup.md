# T13 — Frontend: `oauth-callback` remover `setTimeout(1000)`

> Fase: Frontend | Prioridade: P2 (POS-RC)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

`oauth-callback.component.ts:28-40` usa `setTimeout(1000)` como workaround para aguardar o cookie de sessao estar disponivel apos o redirect do Google. Problemas:

1. **1 segundo fixo** e arbitrario — muito longo quando a rede e rapida, muito curto em conexoes lentas
2. Em caso de falha, o `error:` do subscribe navega para `/login` **sem mensagem** do motivo — o usuario fica perdido
3. O cookie geralmente ja esta disponivel apos o redirect; o `setTimeout` e paranoia desnecessaria

A solucao e usar `retry()` do RxJS com backoff curto, e mostrar toast informativo em caso de erro.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/pages/oauth-callback/oauth-callback.component.ts:24-45` | Remover `setTimeout`, adicionar `retry`, melhorar error handling |

---

## Passos Sugeridos

### Passo 1 — Estado atual

Provavelmente algo como:
```typescript
ngOnInit() {
  setTimeout(() => {
    this.authService.getUserInfo().subscribe({
      next: user => {
        const redirect = sessionStorage.getItem('REDIRECT_URL') || '/dashboard';
        sessionStorage.removeItem('REDIRECT_URL');
        this.router.navigate([redirect]);
      },
      error: () => this.router.navigate(['/login'])
    });
  }, 1000);
}
```

### Passo 2 — Refatoracao

```typescript
import { retry, timer } from 'rxjs';

ngOnInit() {
  this.authService.getUserInfo().pipe(
    retry({
      count: 2,
      delay: (_, retryCount) => timer(retryCount * 300) // 300ms, 600ms
    })
  ).subscribe({
    next: user => {
      const redirect = sessionStorage.getItem('REDIRECT_URL') || '/dashboard';
      sessionStorage.removeItem('REDIRECT_URL');
      this.router.navigate([redirect]);
    },
    error: (err) => {
      this.toastService.error('Nao foi possivel completar o login. Tente novamente.');
      this.router.navigate(['/login']);
    }
  });
}
```

### Passo 3 — Atualizar testes

```
npx vitest run src/app/pages/oauth-callback/
```

Testes podem ter mock de `setTimeout` que precisa ser removido. Atualizar para usar `fakeAsync` ou mocks de timers se necessario.

### Passo 4 — Validacao manual

1. Fazer logout
2. Fazer login via Google
3. Esperado: redirecionamento rapido (< 500ms tipicamente) para `/dashboard` ou `REDIRECT_URL`
4. Em caso de erro (simular com devtools offline): toast informativo + redirect para `/login`

---

## Criterios de Aceite

- [ ] `setTimeout(1000)` removido
- [ ] Uso de `retry({ count: 2, delay: ... })` para resistir a race condition de cookie
- [ ] Em caso de erro, mostra toast informativo ANTES de navegar
- [ ] Testes passam
- [ ] Login manual funciona mais rapido que antes

---

## Notas

- O `getUserInfo()` passa por `SKIP_ERROR_INTERCEPTOR` (apos T5), entao o interceptor NAO dispara toast. O toast aqui e manual e e o unico feedback ao usuario.
- `retry` com delay crescente resolve a race condition sem o atraso fixo de 1 segundo.
- Esta task e P2 (pos-RC) porque o `setTimeout` atual funciona, so e feio. O refactor e qualidade.

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P6 (linha 125-132)
