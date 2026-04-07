# T12 — Frontend: Remover double-toast em 19 componentes

> Fase: Frontend | Prioridade: P1 (DESEJAVEL PRE-RC)
> Dependencias: T4 (interceptor refatorado)
> Bloqueia: T-DOC1
> Estimativa: 3h
> Agente sugerido: angular-frontend-dev

---

## Contexto

19 componentes implementam `error: () => this.toastService.error('Erro ao X')` no `.subscribe(...)`. O `error.interceptor.ts` (refatorado em T4) JA dispara toast de erro para 5xx e 4xx genericos. Resultado atual: **double-toast** — o usuario ve DOIS toasts sobrepostos ao falhar um CRUD.

A decisao arquitetural (registrada em T-DOC1) e:
> **O interceptor e a unica fonte de toasts de erro HTTP genericos. Componentes so usam `toastService.error()` para erros de validacao local ou regras de negocio nao-HTTP.**

Esta task:
1. Lista todos os 19 componentes afetados (grep)
2. Remove o callback `error` redundante de cada `.subscribe()`
3. OU, se a mensagem e especifica de contexto (ex: "Erro ao excluir jogo X"), usa `SKIP_ERROR_INTERCEPTOR` + toast local

---

## Arquivos Envolvidos

Lista preliminar (ver `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P4):
- `features/mestre/pages/jogos-list/jogos-list.component.ts:281-292`
- `features/jogador/pages/fichas-list/fichas-list.component.ts`
- ~17 configs em `features/mestre/pages/config/configs/**`

A lista completa deve ser obtida via grep na Passo 1.

---

## Passos Sugeridos

### Passo 1 — Levantar a lista completa

```
cd ficha-controlador-front-end
grep -rn "toastService\.error\|toast\.error" src/app/features --include="*.ts" | grep -v ".spec.ts" > /tmp/double-toast-candidates.txt
```

Filtrar manualmente os que estao DENTRO de `subscribe({ ... error: ... })`. Alguns podem ser legitimos (ex: erro de validacao de formulario local).

### Passo 2 — Para cada componente, analisar caso a caso

Para cada ocorrencia:

**Opcao A — Remocao simples** (a maioria dos casos)
O `error:` apenas dispara toast sem logica adicional → DELETAR o callback `error`. O interceptor cuida.

Antes:
```typescript
this.service.criar(dto).subscribe({
  next: () => {
    this.toastService.success('Criado com sucesso');
    this.recarregar();
  },
  error: () => {
    this.toastService.error('Erro ao criar');
  }
});
```

Depois:
```typescript
this.service.criar(dto).subscribe({
  next: () => {
    this.toastService.success('Criado com sucesso');
    this.recarregar();
  }
});
```

**Opcao B — Toast local com contexto** (minoria)
Se a mensagem tem contexto especifico util (nome do jogo, validacao custom), usar `SKIP_ERROR_INTERCEPTOR` para evitar o toast generico e manter o especifico:

```typescript
import { HttpContext } from '@angular/common/http';
import { SKIP_ERROR_INTERCEPTOR } from '@core/tokens/skip-error.token';

this.http.delete(`/api/v1/jogos/${jogoId}`, {
  context: new HttpContext().set(SKIP_ERROR_INTERCEPTOR, true)
}).subscribe({
  next: () => this.toastService.success(`Jogo "${nome}" excluido`),
  error: () => this.toastService.error(`Nao foi possivel excluir "${nome}". Verifique se ha fichas ativas.`)
});
```

### Passo 3 — Atualizar testes

Cada componente alterado provavelmente tem testes que verificam o callback `error`. Atualizar ou remover esses testes.

### Passo 4 — Rodar suite

```
npx vitest run
```

Esperado: todos os testes passando. Algumas correcoes em mocks.

### Passo 5 — Auditoria final (grep de regressao)

```
grep -rn "error:\s*(\([^)]*\)\s*=>\|function\|\(\s*err\|\(\s*error)" src/app/features --include="*.ts" | grep -v ".spec.ts" | grep -v "SKIP_ERROR_INTERCEPTOR"
```

Idealmente: zero resultados apos a task. Qualquer resultado restante deve ter justificativa (opcao B com SKIP).

---

## Criterios de Aceite

- [ ] Lista completa dos 19 componentes gerada
- [ ] Cada componente analisado e atualizado (Opcao A ou B)
- [ ] Testes existentes passam (com mocks atualizados)
- [ ] `grep` final mostra zero `error: () => toastService.error(...)` residual em components (exceto casos com `SKIP_ERROR_INTERCEPTOR`)
- [ ] Validacao manual em pelo menos 3 componentes: induzir erro HTTP e verificar UM toast aparece

---

## Notas

- Esta e a task mais tediosa do P1 (~3h). Pode ser dividida em sub-tasks T12a/T12b se o agente preferir.
- NAO alterar componentes que usam `error:` para logica que NAO e toast (ex: atualizar `signal loading = false`). Manter o callback mas remover so a linha do toast.
- Se o agente encontrar mais de 19 componentes, reportar ao PM — pode haver mais double-toasts nao mapeados.
- Criar uma TaskList interna com os 19 componentes e marcar cada um conforme concluido, para tracking.

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § P4 (linha 94-106)
- T4 (interceptor refatorado)
- T-DOC1 (contrato documentado)
