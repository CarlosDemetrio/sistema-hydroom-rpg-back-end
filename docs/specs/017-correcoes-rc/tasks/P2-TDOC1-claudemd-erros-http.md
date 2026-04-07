# T-DOC1 — Doc: Atualizar `CLAUDE.md` com contrato de erros HTTP

> Fase: Doc | Prioridade: P2 (POS-RC)
> Dependencias: T4 (interceptor refatorado), T12 (double-toast removido)
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-tech-lead

---

## Contexto

Apos a refatoracao do `error.interceptor.ts` e remocao dos double-toasts, o projeto tem um **contrato claro** sobre como tratar erros HTTP. Esse contrato deve ser documentado no `CLAUDE.md` para que agentes futuros (e desenvolvedores humanos) nao reintroduzam os bugs.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/CLAUDE.md` | Adicionar secao "Tratamento de erros HTTP" |
| `CLAUDE.md` (raiz do backend) | Adicionar nota sobre `HttpStatusEntryPoint` em `/api/**` |

---

## Passos Sugeridos

### Passo 1 — Adicionar secao no `CLAUDE.md` do frontend

Conteudo proposto:

```markdown
## Tratamento de erros HTTP

O projeto segue um contrato rigido para tratamento de erros HTTP:

### Regra 1 — O interceptor e a unica fonte de toasts genericos
O `error.interceptor.ts` e responsavel por exibir toasts para erros HTTP. Componentes NAO devem duplicar isso.

### Regra 2 — Mapeamento de status
| Status | Acao do interceptor |
|--------|---------------------|
| 401 | `toast.info('Sessao expirou')` + `router.navigate(['/login'])` |
| 403 | `router.navigate(['/unauthorized'])` (sem toast) |
| 404 | `toast.warn('Recurso nao encontrado')` |
| 0 | `toast.error('Sem conexao com o servidor')` |
| 4xx/5xx | `toast.error(error.error.message || 'Erro N')` |

### Regra 3 — Componentes NAO devem chamar `toastService.error()` em `subscribe.error`
Anti-pattern:
```typescript
// ERRADO — gera double-toast
this.service.criar(dto).subscribe({
  next: () => this.toastService.success('OK'),
  error: () => this.toastService.error('Erro ao criar')  // <-- REMOVER
});
```

Correto:
```typescript
this.service.criar(dto).subscribe({
  next: () => this.toastService.success('OK')
});
```

### Regra 4 — Para tratamento customizado, use `SKIP_ERROR_INTERCEPTOR`
Quando o componente precisa tratar o erro de forma especifica (ex: mensagem contextual), use o HttpContextToken:

```typescript
import { HttpContext } from '@angular/common/http';
import { SKIP_ERROR_INTERCEPTOR } from '@core/tokens/skip-error.token';

this.http.delete(`/api/v1/jogos/${id}`, {
  context: new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true)
}).subscribe({
  next: () => this.toastService.success(`Jogo "${nome}" excluido`),
  error: (err) => {
    if (err.status === 409) {
      this.toastService.error(`"${nome}" tem fichas ativas. Nao pode ser excluido.`);
    } else {
      this.toastService.error(`Erro ao excluir "${nome}"`);
    }
  }
});
```

### Regra 5 — `authService.getUserInfo()` sempre usa SKIP
O metodo `getUserInfo()` do `auth.service.ts` ja passa `SKIP_ERROR_INTERCEPTOR=true` para evitar toast quando o usuario simplesmente nao esta logado na primeira carga.
```

### Passo 2 — Adicionar nota no `CLAUDE.md` do backend

Adicionar em uma secao apropriada (ex: `## Security` ou nova subsecao):

```markdown
### AuthenticationEntryPoint

O `SecurityConfig` configura `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` para qualquer request em `/api/**` nao autenticado. Isso garante que o frontend SPA receba 401 JSON em vez de 302 redirect para `/oauth2/authorization/google`.

Rotas fora de `/api/**` (ex: `/oauth2/**`, `/login`) continuam usando o entry point padrao do `oauth2Login()`.

```java
.exceptionHandling(ex -> ex
    .defaultAuthenticationEntryPointFor(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
        new AntPathRequestMatcher("/api/**")
    )
)
```

Teste de regressao: `SecurityConfigIntegrationTest`.
```

### Passo 3 — Validacao

Nenhuma validacao automatizada — revisar manualmente a formatacao do Markdown.

---

## Criterios de Aceite

- [ ] `ficha-controlador-front-end/CLAUDE.md` tem secao "Tratamento de erros HTTP" com as 5 regras
- [ ] `CLAUDE.md` (raiz) tem nota sobre `HttpStatusEntryPoint`
- [ ] Exemplos de codigo estao sintaticamente corretos
- [ ] Referencia ao `SKIP_ERROR_INTERCEPTOR` token e ao teste de regressao

---

## Notas

- Esta task e POS-RC porque e documentacao. Porem, recomenda-se executar assim que T4 e T12 estiverem concluidas para consolidar o contrato enquanto a memoria esta fresca.
- NAO criar arquivos `.md` separados — integrar nos `CLAUDE.md` existentes.

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § Recomendacoes Adicionais (linha 157-168)
- T4 (interceptor refatorado)
- T12 (double-toast removido)
