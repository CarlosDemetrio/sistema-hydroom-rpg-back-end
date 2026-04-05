---
name: Frontend Stack e Projeto Angular
description: Stack, localização do projeto Angular e padrões arquiteturais do frontend ficha-controlador
type: project
---

O projeto frontend Angular está em `/Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end/`.

**Stack:** Angular 21, PrimeNG 21.1.1, @ngrx/signals 21, standalone components, Vitest (não Jest) para testes.

**Modelo Ficha é FLAT** — sem objetos nested. Campos: `id, jogoId, nome, jogadorId, racaId, racaNome, classeId, classeNome, generoId, generoNome, indoleId, indoleNome, presencaId, presencaNome, nivel, xp, renascimentos, isNpc, dataCriacao, dataUltimaAtualizacao`.

**Path aliases tsconfig:**
- `@shared/*` → `src/app/shared/*` (com wildcard)
- `@models/*` → `src/app/core/models/*`
- `@core/*` → `src/app/core/*`
- `@services/*` → `src/app/services/*`
- `@shared` sem wildcard pode não funcionar — usar sempre `@shared/components/nome-do-componente`

**FichaBusinessService.createFicha** recebe DOIS argumentos: `createFicha(jogoId: number, dto: CreateFichaDto)`.

**PrimeNG v21:** `p-inputicon` (kebab-case) é o seletor correto, não `p-input-icon`.

**DecimalPipe** deve ser importado de `@angular/common` nos standalone components que usam o pipe `number` no template.

**DividerModule** vem de `primeng/divider`.

**Why:** Projeto de ficha de RPG (Klayrah) com backend Spring Boot 4. Frontend isolado do backend por contrato REST.

**How to apply:** Ao editar qualquer componente de Ficha, sempre usar campos flat do modelo. Nunca usar `ficha.identificacao`, `ficha.progressao` ou `ficha.calculados` — esses objetos nested não existem.

---

## Padrão de testes para Smart Components de Config (descoberto em 2026-04-02)

Os 14 Smart Components de config (`AtributosConfigComponent`, `NiveisConfigComponent`, etc.) seguem exatamente o mesmo padrão:

- Estendem `BaseConfigComponent<T, S>` (Directive abstrata em `@shared/components/base-config/base-config.component.ts`)
- Usam `BaseConfigTableComponent` como tabela genérica (Dumb Component)
- Têm `drawerVisible = signal(false)` local (separado do `dialogVisible` herdado da base)
- Métodos públicos: `openDrawer(item?)`, `closeDrawer()`, `save()`, `confirmDelete(id)`
- Injetam: service específico + `ConfirmationService` (providers: [ConfirmationService] no componente)

**Mock do CurrentGameService para testes:**
```typescript
{
  currentGameId:   () => 10,       // função, não signal
  hasCurrentGame:  () => true,     // função, não signal
  currentGame:     () => jogoObj,  // função, não signal
  availableGames:  signal([]).asReadonly(),
  selectGame: vi.fn(), clearGame: vi.fn(),
}
```

**Acesso a membros protected nos testes:** usar `(fixture.componentInstance as any).drawerVisible()` com eslint-disable.

**ConfirmationService mock para testar fluxo de confirmação:**
```typescript
confirmationServiceMock.confirm.mockImplementation(({ accept }) => { accept(); });
```

---

## Como rodar testes Angular com Vitest (corrigido em 2026-04-04)

**REGRA**: Usar `npx vitest run` ou `npx vitest run path/to/file.spec.ts` diretamente — funciona corretamente em modo JIT.

**Por quê:** O projeto roda em modo JIT (sem `@analogjs/vitest-angular`). Isso significa que `input()`, `output()` e `input.required()` têm limitações — ver padrões de JIT abaixo. O comando `npm test` (package.json) chama Vitest diretamente.

**vitest.config.ts** deve existir com `globals: true`, `setupFiles: ['src/setup-tests.ts']`, `resolve.alias` para os path aliases e `pool: 'forks'`.

**src/setup-tests.ts** deve inicializar o TestBed:
```typescript
import { getTestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
getTestBed().initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting(), { teardown: { destroyAfterEach: true } });
```

**ConfirmationService nos testes** (componentes com `providers: [ConfirmationService]`):
- O `ConfirmationService` interno ao componente é uma instância separada do TestBed root
- Acessar via: `result.fixture.componentRef.injector.get(ConfirmationService)`
- Usar `vi.spyOn(confirmationService, 'confirm').mockImplementation(...)` após obter a instância
- Fornecer o `ConfirmationService` real (não mock) no `providers` do `render()` — o ConfirmDialog precisa dele

**Mock do service com `temJogo`:**
- O componente usa `this.service.hasCurrentGame()` (não `CurrentGameService` diretamente)
- O mock do service específico (AtributoConfigService, etc.) precisa ter `hasCurrentGame: () => temJogo`
- Separar o parâmetro `temJogo` do mock do service do CurrentGameService mock

---

## Padrão de reordenação real (implementado em 2026-04-02)

Os 12 config components com `[canReorder]="true"` chamam `handleReorder()` real via `ConfigApiService`.

**Mapeamento de payload:**
- Entrada (emitida por `BaseConfigTableComponent`): `{ itemId: number; novaOrdem: number }[]`
- Saída (esperada pelo backend): `ReordenarRequest = { itens: ReordenarItem[] }` onde `ReordenarItem = { id: number; ordemExibicao: number }`

**Padrão canônico:**
```typescript
protected handleReorder(payload: { itemId: number; novaOrdem: number }[]): void {
  const jogoId = this.currentGameId();
  if (!jogoId || payload.length === 0) return;
  this.configApi.reordenar[Entidade](jogoId, { itens: payload.map((p) => ({ id: p.itemId, ordemExibicao: p.novaOrdem })) })
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: () => this.toastService.success('Ordem salva com sucesso.', 'Reordenação'),
      error: () => this.toastService.error('Erro ao salvar a ordem.', 'Reordenação'),
    });
}
```

**`niveis-config` PULADO**: usa `[canReorder]="false"` — níveis têm ordem implícita pelo campo `nivel`, sem endpoint `/reordenar` no backend.

**`currentGameId()` disponível via base class** como `computed()` — não precisa injetar `CurrentGameService` direto.
**`destroyRef` disponível via base class** — não precisa injetar `DestroyRef` nos componentes filhos.
