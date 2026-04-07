# T6 — Frontend: Bug `hasBothRoles()` (`||` → `&&`)

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 0.5h
> Agente sugerido: angular-frontend-dev

---

## Contexto

O metodo `hasBothRoles()` em `header.component.ts:136-139` usa o operador `||` (OR logico) em vez de `&&` (AND logico). Resultado: a funcao retorna `true` para QUALQUER usuario autenticado (Mestre OU Jogador), nao apenas usuarios que tem AS DUAS roles.

Consequencia: o seletor "Visualizar como" aparece para todo mundo. Usuarios com apenas uma role veem um switcher inutil.

```typescript
// ATUAL (incorreto):
hasBothRoles(): boolean {
  const user = this.authService.currentUser();
  return user?.role === 'MESTRE' || user?.role === 'JOGADOR'; // sempre true
}
```

**Atencao**: nao basta trocar `||` por `&&` porque `user.role` e um campo unico. Um usuario nao pode ser MESTRE E JOGADOR ao mesmo tempo se `role` e string. Pode ser que o model `User` tenha um campo `roles: string[]` em vez de `role: string`. **Verificar o modelo antes de corrigir.**

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/shared/components/header/header.component.ts:136-139` | Corrigir logica |
| `ficha-controlador-front-end/src/app/models/user.model.ts` (ou similar) | Verificar shape de `User.role(s)` |

---

## Passos Sugeridos

### Passo 1 — Inspecionar o model `User`

Procurar o arquivo do model:
```
src/app/models/user.model.ts
src/app/services/auth.service.ts (provavel TypeScript inline)
```

Verificar se `User` tem:
- `role: string` (uma role)
- OU `roles: string[]` (array de roles)
- OU outra estrutura

### Passo 2A — Se `User.role` e string unica

Significa que um usuario tem APENAS uma role no MVP atual (decisao de modelo). Nesse caso, `hasBothRoles()` SEMPRE deveria retornar `false` no MVP, e o seletor "Visualizar como" nao deveria existir. Solucao:

```typescript
hasBothRoles(): boolean {
  return false; // MVP: usuarios tem apenas uma role
}
```

OU remover o seletor "Visualizar como" do template do header.

### Passo 2B — Se `User.roles` e array

Corrigir para verificar se o array contem AMBAS as strings:

```typescript
hasBothRoles(): boolean {
  const user = this.authService.currentUser();
  return user?.roles?.includes('MESTRE') === true
      && user?.roles?.includes('JOGADOR') === true;
}
```

### Passo 3 — Verificar uso no template

Procurar `hasBothRoles()` no template do `header.component.ts` e confirmar que o seletor so aparece quando true.

### Passo 4 — Rodar testes

```
cd ficha-controlador-front-end
npx vitest run src/app/shared/components/header/
```

Atualizar testes se necessario. Adicionar teste cobrindo:
- Usuario MESTRE apenas: `hasBothRoles() === false`
- Usuario JOGADOR apenas: `hasBothRoles() === false`
- Usuario com ambas (se o modelo permite): `hasBothRoles() === true`

---

## Criterios de Aceite

- [ ] Investigado o shape do model `User` e documentado a decisao no commit
- [ ] `hasBothRoles()` corrigido (uma das duas estrategias acima)
- [ ] Testes do `header.component.spec.ts` passam
- [ ] Pelo menos 2 testes cobrindo: usuario com uma role e usuario sem ambas as roles
- [ ] Validacao manual: logar como Mestre e confirmar que o seletor "Visualizar como" NAO aparece (assumindo MVP de role unica)

---

## Notas

- Esta e a task mais simples do P0 (~30min). Otimo candidato para warmup de uma rodada.
- Se o investigador descobrir que o modelo e mais complexo (ex: usuario admin com ambas), reportar ao PM antes de codificar.
- O agente NAO deve adicionar suporte a multi-role nesta task — isso e Spec 010 (stand-by).

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-03 (linha 224-237)
