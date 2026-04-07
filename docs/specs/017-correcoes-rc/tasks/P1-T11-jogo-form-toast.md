# T11 — Frontend: `jogo-form` adicionar `<p-toast>` ou usar `ToastService`

> Fase: Frontend | Prioridade: P1 (DESEJAVEL PRE-RC)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

O `jogo-form.component.ts` injeta `MessageService` (linha 8) e usa `this.messageService.add(...)` para mostrar mensagens de sucesso/erro apos criar ou editar um jogo. Porem:

1. O componente NAO importa `ToastModule` nos `imports`
2. O template NAO tem `<p-toast>`

Resultado: as mensagens sao silenciosas. O usuario nao ve feedback visual quando cria um jogo com sucesso ou falha.

O padrao do projeto (usado em todos os outros componentes) e o `ToastService` global em `services/toast.service.ts`, que ja tem o `<p-toast>` renderizado no `AppComponent` ou `MainLayoutComponent`.

A solucao mais limpa e **trocar `MessageService` por `ToastService`** (padrao do projeto).

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/features/mestre/pages/jogo-form/jogo-form.component.ts` | Substituir `MessageService` por `ToastService` |

---

## Passos Sugeridos

### Passo 1 — Identificar usos de `MessageService`

```
grep -n "messageService" ficha-controlador-front-end/src/app/features/mestre/pages/jogo-form/jogo-form.component.ts
```

Listar todos os `this.messageService.add(...)`.

### Passo 2 — Trocar DI

Antes:
```typescript
import { MessageService } from 'primeng/api';
// ...
private messageService = inject(MessageService);
```

Depois:
```typescript
import { ToastService } from '@services/toast.service';
// ...
private toastService = inject(ToastService);
```

### Passo 3 — Trocar chamadas

Para cada `this.messageService.add(...)`, substituir pelo equivalente no `ToastService`:

| Antes | Depois |
|-------|--------|
| `messageService.add({ severity: 'success', summary: 'X', detail: 'Y' })` | `toastService.success('Y')` (ou `'X: Y'`) |
| `messageService.add({ severity: 'error', ... })` | `toastService.error('Y')` |
| `messageService.add({ severity: 'info', ... })` | `toastService.info('Y')` |
| `messageService.add({ severity: 'warn', ... })` | `toastService.warn('Y')` |

(Confirmar a API exata do `ToastService` — pode aceitar `summary + detail` ou so `message`.)

### Passo 4 — Remover `MessageService` dos providers locais

Se o componente tinha `providers: [MessageService]`, remover (o `ToastService` e global).

### Passo 5 — Remover import de `ToastModule` se houver

Se o componente tinha `imports: [..., ToastModule]` sem o `<p-toast>` no template, remover o import morto.

### Passo 6 — Testes

```
cd ficha-controlador-front-end
npx vitest run src/app/features/mestre/pages/jogo-form/
```

Atualizar mocks de testes que esperavam `MessageService` para mockar `ToastService` em vez disso.

### Passo 7 — Validacao manual

1. Navegar para `/mestre/jogos/novo`
2. Criar um jogo
3. Esperado: toast de sucesso visivel
4. Induzir erro (ex: nome duplicado): toast de erro visivel

---

## Criterios de Aceite

- [ ] `jogo-form.component.ts` nao usa mais `MessageService` direto
- [ ] Usa `ToastService` (padrao do projeto)
- [ ] Mensagens de sucesso e erro aparecem na tela (validacao manual)
- [ ] Testes passam (com mocks atualizados)
- [ ] Sem imports orfaos de `ToastModule`/`MessageService` no componente

---

## Notas

- Se o `ToastService` tiver assinatura diferente (ex: `toastService.show(severity, message)`), adaptar
- Se houver OUTROS componentes usando `MessageService` direto (grep global), eles estao no escopo de T12 (limpar double-toast) ou ficam para P3. Mas provavelmente so o `jogo-form` tem esse problema.
- Esta task e rapida (~1h) — bom candidato para warmup.

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-04 (linha 239-244)
