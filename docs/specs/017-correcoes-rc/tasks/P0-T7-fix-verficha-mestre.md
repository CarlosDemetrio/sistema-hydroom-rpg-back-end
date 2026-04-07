# T7 — Frontend: Bug `verFicha()` Mestre + nova rota `/mestre/fichas/:id`

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 1.5h
> Agente sugerido: angular-tech-lead

---

## Contexto

`jogo-detail.component.ts:543` tem:

```typescript
verFicha(fichaId: number) {
  this.router.navigate(['/jogador/fichas', fichaId]);
}
```

A rota `/jogador/fichas/:id` esta protegida por `roleGuard` com `roles: ['JOGADOR']`. Quando o **Mestre** clica em "ver ficha" no painel de detalhes do jogo, e redirecionado para `/unauthorized`. **Funcionalidade quebrada para o Mestre.**

Existem duas estrategias para corrigir:

### Estrategia A — Criar rota `/mestre/fichas/:id`
Adicionar nova rota em `app.routes.ts` que reusa o `FichaDetailComponent` mas com guard `MESTRE`. O `verFicha()` passa a navegar para essa rota.

### Estrategia B — Adaptar guard da rota existente
Mudar `roles: ['JOGADOR']` para `roles: ['MESTRE', 'JOGADOR']` na rota `/jogador/fichas/:id`. Mais simples, mas mantem o usuario Mestre numa URL com prefixo `/jogador/` (semantica confusa).

**PA-017-01: O PM precisa decidir antes do inicio desta task.** Recomendacao do PM: **Estrategia A** para preservar semantica de URLs.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/app.routes.ts` | Adicionar rota `/mestre/fichas/:id` (Estrategia A) OU modificar guard existente (Estrategia B) |
| `ficha-controlador-front-end/src/app/features/mestre/pages/jogo-detail/jogo-detail.component.ts:543` | Atualizar `verFicha()` para navegar para a rota correta |

---

## Passos Sugeridos (Estrategia A)

### Passo 1 — Adicionar rota em `app.routes.ts`

Localizar a secao de rotas `/mestre/...`. Adicionar:

```typescript
{
  path: 'mestre/fichas/:id',
  loadComponent: () => import('./features/jogador/pages/ficha-detail/ficha-detail.component').then(m => m.FichaDetailComponent),
  canActivate: [authGuard, roleGuard],
  data: { roles: ['MESTRE'] }
},
```

Nota: o componente `FichaDetailComponent` e o mesmo. So muda a rota e o guard. O componente deve funcionar para Mestre porque ja tem permissoes de leitura.

### Passo 2 — Verificar permissoes do componente

`FichaDetailComponent` provavelmente tem chamadas para:
- `GET /api/v1/fichas/:id` — permitido para MESTRE
- `PUT /api/v1/fichas/:id` — permitido para MESTRE
- Botoes de Editar/Duplicar/Deletar — verificar se nao chamam endpoints exclusivos de JOGADOR

Se houver alguma logica condicional `if (user.role === 'JOGADOR')`, garantir que o fluxo Mestre tambem funciona.

### Passo 3 — Atualizar `jogo-detail.component.ts:543`

Antes:
```typescript
verFicha(fichaId: number) {
  this.router.navigate(['/jogador/fichas', fichaId]);
}
```

Depois:
```typescript
verFicha(fichaId: number) {
  this.router.navigate(['/mestre/fichas', fichaId]);
}
```

### Passo 4 — Testes

```
cd ficha-controlador-front-end
npx vitest run src/app/features/mestre/pages/jogo-detail/
```

Atualizar testes que verificam navegacao do `verFicha()` (novo path).

### Passo 5 — Validacao manual

1. Logar como Mestre
2. Navegar para `/mestre/jogos/:id`
3. Clicar em "ver ficha" de algum personagem listado
4. Esperado: navega para `/mestre/fichas/:id` e renderiza `FichaDetailComponent` SEM cair em `/unauthorized`

---

## Criterios de Aceite

- [ ] PA-017-01 resolvido (estrategia escolhida documentada no commit)
- [ ] Nova rota `/mestre/fichas/:id` adicionada em `app.routes.ts` (se Estrategia A)
- [ ] `jogo-detail.component.ts:543` atualizado para usar a nova rota
- [ ] `FichaDetailComponent` funciona corretamente quando acessado por Mestre (sem erros, sem botoes quebrados)
- [ ] Testes existentes passam
- [ ] Validacao manual: Mestre consegue navegar para detalhe de ficha sem 403

---

## Notas

- Se houver multiplos lugares no codigo que navegam para `/jogador/fichas/:id` esperando que o Mestre tambem use, considerar criar um helper `navegarParaFicha(fichaId)` no `auth.service` ou `router.service` que escolha a rota baseada na role do usuario logado.
- Esta task NAO duplica componentes — apenas adiciona uma rota nova que aponta para o componente existente.
- Verificar tambem se ha rotas `/jogador/fichas/:id/editar` e `/jogador/fichas/:id/wizard` que sofrem do mesmo problema. Se sim, considerar criar `/mestre/fichas/:id/editar` na mesma task.

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-05 (linha 245-255)
- PA-017-01 (decisao de estrategia)
