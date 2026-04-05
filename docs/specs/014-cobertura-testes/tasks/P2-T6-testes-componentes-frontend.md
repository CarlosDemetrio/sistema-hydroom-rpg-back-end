# T6 — Adicionar Testes para Componentes Frontend Sem Cobertura

> Fase: Frontend Testes | Dependencias: T5 (relatorio de cobertura identifica gaps) | Bloqueia: Nenhuma
> Estimativa: 3–4 horas

---

## Objetivo

Adicionar testes para os componentes frontend criticos identificados pelo relatorio de cobertura (T5). Foco em componentes com logica de negocio (barras, pontos, validacao), nao em componentes puramente apresentacionais.

---

## Componentes Prioritarios

### P0 — Logica de negocio critica

| Componente | Testes estimados | O que testar |
|-----------|-----------------|-------------|
| `ficha-header` | 5 | Barras vida/essencia %, estado (ativa/morta), NPC vs jogador |
| `ficha-vantagens-tab` | 4 | Pontos disponiveis, categorias, pre-requisitos habilitados |

### P1 — Wizard e formularios

| Componente | Testes estimados | O que testar |
|-----------|-----------------|-------------|
| Wizard step 1 (Identificacao) | 3 | Campos obrigatorios, selects carregam, navegacao |
| Wizard step 3 (Atributos) | 3 | Distribuicao de pontos, limite nao excedido, total correto |
| Wizard step 5 (Vantagens) | 3 | Custo calculado, pontos restantes, pre-req validado |

### P2 — Signal stores

| Store | Testes estimados | O que testar |
|-------|-----------------|-------------|
| Ficha store | 4 | Loading state, error state, computed derivados, cache |
| Jogo store | 2 | Carregamento, selecao de jogo |

---

## Padrao de Teste (Vitest + Testing Library)

```typescript
import { render, screen } from '@testing-library/angular';
import { vi } from 'vitest';

describe('FichaHeaderComponent', () => {
  it('deve exibir barra de vida com percentual correto', async () => {
    // Arrange
    const ficha = {
      vidaAtual: 30,
      vidaTotal: 100,
      essenciaAtual: 15,
      essenciaTotal: 50,
    };

    // Act
    await render(FichaHeaderComponent, {
      inputs: { ficha },
    });

    // Assert
    const vidaBar = screen.getByTestId('vida-bar');
    expect(vidaBar).toHaveAttribute('aria-valuenow', '30');
  });

  it('deve esconder botao level up para NPC', async () => {
    const ficha = { ...baseFicha, isNpc: true };
    await render(FichaHeaderComponent, { inputs: { ficha } });
    expect(screen.queryByText('Level Up')).toBeNull();
  });
});
```

### Convencoes obrigatorias:
- `vi.fn()` para mocks (Vitest, nao Jest)
- `@testing-library/angular` para render
- `inject()` para DI, nunca constructor
- `signal()`/`computed()` em inputs, nao `@Input()`

---

## O que NAO testar nesta task

- Chamadas HTTP reais (coberto por testes de API service)
- Layout/CSS (testes visuais nao sao objetivo)
- Componentes PrimeNG internos (testados pelo PrimeNG)
- Fluxo E2E completo (fora do escopo)

---

## Estimativa de Testes

- P0 (ficha-header + vantagens-tab): 9 testes
- P1 (wizard steps): 9 testes
- P2 (signal stores): 6 testes
- **Total: ~24 testes**

---

## Criterios de Aceitacao

- [ ] `ficha-header`: 5 testes cobrindo barras, estado, NPC
- [ ] `ficha-vantagens-tab`: 4 testes cobrindo pontos e categorias
- [ ] Pelo menos 3 wizard steps com testes de validacao
- [ ] Signal stores com testes de estado derivado
- [ ] Total frontend >= 295 testes (271 + ~24)
- [ ] `npx vitest run` passa sem falhas
- [ ] `npm run test:coverage` mostra melhoria na cobertura dos componentes testados
