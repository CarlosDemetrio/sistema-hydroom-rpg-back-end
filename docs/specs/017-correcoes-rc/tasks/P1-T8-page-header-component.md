# T8 — Frontend: Criar `PageHeaderComponent` reutilizavel

> Fase: Frontend | Prioridade: P1 (DESEJAVEL PRE-RC)
> Dependencias: nenhuma
> Bloqueia: T9, T10
> Estimativa: 2h
> Agente sugerido: primeng-ux-architect (spec) → angular-frontend-dev (impl)

---

## Contexto

8 telas internas nao tem botao "Voltar" e o usuario depende 100% do botao do browser. A solucao proposta pelo UX architect e criar um componente standalone reutilizavel `PageHeaderComponent` com:
- Botao Voltar (icone `pi pi-arrow-left`) opcional via input
- Titulo (obrigatorio)
- Subtitulo (opcional)
- Slot para botoes de acao no lado direito

Esta task cria APENAS o componente. A aplicacao nas 8 telas e feita em T9 (Mestre) e T10 (Jogador).

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/shared/components/page-header/page-header.component.ts` | CRIAR (novo) |
| `ficha-controlador-front-end/src/app/shared/components/page-header/page-header.component.spec.ts` | CRIAR (novo) |

---

## Passos Sugeridos

### Passo 1 — Resolver PA-017-02

Decidir com o PM se o componente deve usar `BreadcrumbModule` do PrimeNG ou apenas botao + titulo simples.

**Recomendacao do PM**: simples (botao + titulo + subtitulo + actions slot). Breadcrumb adiciona complexidade e exige config por rota — fica para iteracao futura se for util.

### Passo 2 — Criar o componente

```typescript
// src/app/shared/components/page-header/page-header.component.ts
import { Component, inject, input } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [ButtonModule, TooltipModule],
  template: `
    <div class="flex align-items-center gap-3 mb-4">
      @if (backRoute()) {
        <p-button
          icon="pi pi-arrow-left"
          [text]="true"
          [rounded]="true"
          severity="secondary"
          [pTooltip]="backLabel()"
          tooltipPosition="bottom"
          (onClick)="navegar()"
          [attr.aria-label]="backLabel()"
        />
      }
      <div class="flex flex-column gap-1 flex-1">
        <h1 class="text-2xl font-bold m-0">{{ titulo() }}</h1>
        @if (subtitulo()) {
          <p class="text-sm text-color-secondary m-0">{{ subtitulo() }}</p>
        }
      </div>
      <div class="flex gap-2">
        <ng-content select="[actions]" />
      </div>
    </div>
  `
})
export class PageHeaderComponent {
  // Inputs
  titulo = input.required<string>();
  subtitulo = input<string>('');
  backRoute = input<string | string[] | null>(null);
  backLabel = input<string>('Voltar');

  // DI
  private router = inject(Router);

  navegar(): void {
    const route = this.backRoute();
    if (!route) return;
    const segments = Array.isArray(route) ? route : [route];
    void this.router.navigate(segments);
  }
}
```

### Passo 3 — Criar testes

```typescript
// src/app/shared/components/page-header/page-header.component.spec.ts
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/angular';
import { provideRouter, Router } from '@angular/router';
import { PageHeaderComponent } from './page-header.component';

describe('PageHeaderComponent', () => {
  it('renderiza titulo obrigatorio', async () => {
    await render(PageHeaderComponent, {
      providers: [provideRouter([])],
      componentInputs: { titulo: 'Meu Titulo' }
    });
    expect(screen.getByText('Meu Titulo')).toBeTruthy();
  });

  it('renderiza subtitulo quando fornecido', async () => {
    await render(PageHeaderComponent, {
      providers: [provideRouter([])],
      componentInputs: { titulo: 'Titulo', subtitulo: 'Sub' }
    });
    expect(screen.getByText('Sub')).toBeTruthy();
  });

  it('NAO renderiza botao Voltar quando backRoute e null', async () => {
    await render(PageHeaderComponent, {
      providers: [provideRouter([])],
      componentInputs: { titulo: 'Titulo' }
    });
    expect(screen.queryByLabelText('Voltar')).toBeNull();
  });

  it('renderiza botao Voltar quando backRoute esta presente', async () => {
    await render(PageHeaderComponent, {
      providers: [provideRouter([])],
      componentInputs: { titulo: 'Titulo', backRoute: '/dashboard' }
    });
    expect(screen.getByLabelText('Voltar')).toBeTruthy();
  });

  it('navega ao clicar no botao Voltar', async () => {
    const navigateSpy = vi.fn().mockResolvedValue(true);
    await render(PageHeaderComponent, {
      providers: [
        provideRouter([]),
        { provide: Router, useValue: { navigate: navigateSpy } }
      ],
      componentInputs: { titulo: 'Titulo', backRoute: '/dashboard' }
    });
    const btn = screen.getByLabelText('Voltar');
    await fireEvent.click(btn);
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });
});
```

### Passo 4 — Build e testes

```
cd ficha-controlador-front-end
npx ng build --configuration development
npx vitest run src/app/shared/components/page-header/
```

Esperado: build OK, 5 testes passando.

---

## Criterios de Aceite

- [ ] Arquivo `page-header.component.ts` criado em `shared/components/page-header/`
- [ ] Standalone component com `imports: [ButtonModule, TooltipModule]`
- [ ] Inputs: `titulo` (required), `subtitulo` (opcional), `backRoute` (opcional), `backLabel` (default 'Voltar')
- [ ] `ng-content select="[actions]"` para slot de botoes de acao
- [ ] Botao Voltar so aparece quando `backRoute` esta definido
- [ ] Clica no botao chama `router.navigate(...)`
- [ ] Atributo `aria-label` no botao para acessibilidade
- [ ] 5 testes minimos (render titulo, subtitulo, botao ausente, botao presente, navegacao)
- [ ] `npx vitest run` passa (848 + 5 = 853)

---

## Notas

- Usar `input.required<T>()` e `input<T>()` (Angular 21+)
- Sem `CommonModule`, usar `@if` em vez de `*ngIf`
- O slot `[actions]` permite que cada tela injete seus proprios botoes (ex: Editar, Excluir) no lado direito do header
- NAO adicionar Breadcrumb nesta versao (PA-017-02 resolvido como "simples")

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § P1 — Padrao proposto (linha 44-100)
- PA-017-02 (decisao de complexidade do componente)
