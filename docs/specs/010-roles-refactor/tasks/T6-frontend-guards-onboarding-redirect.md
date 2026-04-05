# T6 — Frontend: Guards e redirect para onboarding

> Spec: 010 | Tipo: Frontend | Depende de: T2 (backend) | Bloqueia: T7, T8

---

## Objetivo

Atualizar o `AuthGuard` Angular para detectar `role = null` e redirecionar para `/onboarding`. Criar o `AdminGuard` para proteger rotas `/admin/**`. Atualizar o `AuthService` para incluir o campo `role` no modelo de usuario.

---

## Arquivos Afetados (Angular)

| Arquivo | Acao |
|---------|------|
| `src/app/core/models/usuario.model.ts` | Adicionar campo `role: string | null` |
| `src/app/core/services/auth.service.ts` | Atualizar tipo e logica de `isAdmin()`, `isMestre()`, `isJogador()` |
| `src/app/core/guards/auth.guard.ts` | Adicionar redirect para /onboarding se role = null |
| `src/app/core/guards/admin.guard.ts` | Criar guard que permite apenas ADMIN |
| `src/app/app.routes.ts` | Adicionar rota `/onboarding` e `/admin` com guards |

---

## Passos

### 1. Atualizar modelo de usuario

```typescript
// core/models/usuario.model.ts
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  imagemUrl: string | null;
  role: 'ADMIN' | 'MESTRE' | 'JOGADOR' | null; // null = onboarding pendente
}
```

### 2. Atualizar `AuthService`

Adicionar metodos helpers:
```typescript
// core/services/auth.service.ts
isAdmin(): boolean {
  return this.usuarioAtual()?.role === 'ADMIN';
}

isMestre(): boolean {
  return this.usuarioAtual()?.role === 'MESTRE' || this.isAdmin();
}

isJogador(): boolean {
  return this.usuarioAtual()?.role !== null; // qualquer role definida tem acesso de jogador
}

precisaOnboarding(): boolean {
  return this.usuarioAtual() !== null && this.usuarioAtual()?.role === null;
}
```

**Nota sobre `isMestre()`:** O metodo retorna true para ADMIN tambem, refletindo que ADMIN tem acesso a tudo que MESTRE tem. Util para guards de UI (mostrar/esconder botoes de Mestre).

### 3. Atualizar `AuthGuard`

```typescript
// core/guards/auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAutenticado()) {
    router.navigate(['/login']);
    return false;
  }

  // Redirecionar para onboarding se role nao definida
  if (authService.precisaOnboarding() && state.url !== '/onboarding') {
    router.navigate(['/onboarding']);
    return false;
  }

  return true;
};
```

### 4. Criar `AdminGuard`

```typescript
// core/guards/admin.guard.ts
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAdmin()) {
    router.navigate(['/403']);
    return false;
  }

  return true;
};
```

### 5. Atualizar rotas

```typescript
// app.routes.ts
export const routes: Routes = [
  // ... rotas existentes

  {
    path: 'onboarding',
    loadComponent: () => import('./features/onboarding/onboarding.component'),
    canActivate: [authGuard],  // Apenas autenticado (nao adminGuard, nao roleGuard)
    // Nota: o authGuard nao deve redirecionar para onboarding se JA estamos em /onboarding
  },

  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    children: [
      {
        path: 'usuarios',
        loadComponent: () => import('./features/admin/usuarios/admin-usuarios.component'),
      }
    ]
  },

  // Pagina de acesso negado
  {
    path: '403',
    loadComponent: () => import('./shared/pages/forbidden.component'),
  }
];
```

---

## Criterios de Aceitacao

- [ ] Usuario autenticado com `role = null` e redirecionado para /onboarding ao tentar acessar qualquer rota protegida
- [ ] Usuario em /onboarding nao entra em loop de redirect (guard verifica `state.url !== '/onboarding'`)
- [ ] Usuario com role MESTRE ou JOGADOR nao e redirecionado para /onboarding
- [ ] Rota /admin/usuarios e acessivel apenas para ADMIN (testa com MESTRE → deve ir para /403)
- [ ] `authService.isAdmin()` retorna true apenas para role ADMIN
- [ ] `authService.isMestre()` retorna true para MESTRE e ADMIN
