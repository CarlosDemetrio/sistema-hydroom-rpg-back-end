# T14 — Sidebar + Rotas para PontosVantagem e CategoriaVantagem

> Spec: 012 | Fase: 5 | Tipo: Frontend | Prioridade: MEDIO
> Depende de: T2 (PontosVantagemConfigComponent), T3 (CategoriaVantagemConfigComponent)
> Bloqueia: nada

---

## Objetivo

Adicionar rotas lazy-load e entradas no sidebar de configurações para os dois novos componentes criados em T2 e T3.

## Arquivos Afetados

- `src/app/app.routes.ts` — 2 novas rotas dentro do bloco `config`
- `src/app/features/mestre/pages/config/config-sidebar.component.ts` — 2 novos itens de menu

## Passos

### 1. Adicionar rotas em `app.routes.ts`

Dentro do bloco de rotas de config (após a rota `niveis`):

```typescript
{
  path: 'pontos-vantagem',
  loadComponent: () => import('./features/mestre/pages/config/configs/pontos-vantagem-config/pontos-vantagem-config.component')
    .then(m => m.PontosVantagemConfigComponent)
},
{
  path: 'categorias-vantagem',
  loadComponent: () => import('./features/mestre/pages/config/configs/categorias-vantagem-config/categorias-vantagem-config.component')
    .then(m => m.CategoriaVantagemConfigComponent)
},
```

### 2. Adicionar itens no `ConfigSidebarComponent`

Verificar a posição lógica no menu. Por afinidade temática, inserir próximos de "Vantagens":

```typescript
// Após o item de "Vantagens":
{
  label: 'Categorias de Vantagem',
  description: 'Treinamento Físico, Ação, etc.',
  icon: 'pi pi-tag',
  route: '/mestre/config/categorias-vantagem',
  count: this.configStore.categoriasVantagem().length,  // novo signal no ConfigStore
},
{
  label: 'Pontos de Vantagem',
  description: 'Pontos ganhos por nível',
  icon: 'pi pi-star',
  route: '/mestre/config/pontos-vantagem',
  count: this.configStore.pontosVantagem().length,  // novo signal no ConfigStore
},
```

### 3. Adicionar signals no ConfigStore

Verificar se `ConfigStore` já tem `categoriasVantagem()` signal. Se não, adicionar:

```typescript
readonly categoriasVantagem = signal<CategoriaVantagem[]>([]);
readonly pontosVantagem = signal<PontosVantagemConfig[]>([]);
```

Com métodos de carregamento correspondentes.

### 4. Verificar guard de rota

As novas rotas devem estar dentro do bloco protegido por `roleGuard('MESTRE')`. Verificar na estrutura atual do `app.routes.ts` que o bloco de config já aplica esse guard.

## Critérios de Aceitação

- [ ] Rota `/mestre/config/pontos-vantagem` funcional com lazy loading
- [ ] Rota `/mestre/config/categorias-vantagem` funcional com lazy loading
- [ ] Ambas as rotas protegidas por `roleGuard('MESTRE')`
- [ ] `config-sidebar` exibe as 2 novas entradas com ícones e contagem
- [ ] Clique nos itens do sidebar navega corretamente
- [ ] `ConfigStore` tem signals para `categoriasVantagem()` e `pontosVantagem()`
- [ ] `routerLinkActive` destaca o item ativo no sidebar
- [ ] Build sem erros

## Premissas

- T2 e T3 devem estar concluídos antes desta task
- Os componentes exportam os seletores corretos para o lazy import
