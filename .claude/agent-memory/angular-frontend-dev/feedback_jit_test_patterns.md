---
name: Padrões críticos de testes JIT Angular (Vitest sem plugin)
description: Armadilhas e soluções confirmadas para testes com input()/output() em modo JIT — Angular 21 + Vitest 4
type: feedback
---

O projeto roda Vitest em modo JIT (sem `@analogjs/vitest-angular`). Isso cria 4 armadilhas críticas.

## Armadilha 1: `input()` / `input.required()` não funcionam com setInput/componentInputs

**Sintoma:** `NG0315`, `NG0316`, `NG0950` — inputs não reconhecidos, template não compilado.
**Causa:** Em JIT, `input()` não registra no `ɵcmp.inputs`, então `setInput()` e `componentInputs` falham silenciosamente. Zone.js dispara CD antes do valor ser atribuído.

**Solução para Dumb Components com `input()` / `input.required()`:**
```typescript
import { ɵSIGNAL as SIGNAL_SYM } from '@angular/core';

function setSignalInput<T>(component: unknown, inputName: string, value: T): void {
  const signalFn = (component as Record<string, unknown>)[inputName];
  if (signalFn && (signalFn as Record<symbol, unknown>)[SIGNAL_SYM as symbol]) {
    const node = (signalFn as Record<symbol, unknown>)[SIGNAL_SYM as symbol] as {
      applyValueToInputSignal: (node: unknown, v: T) => void;
    };
    node.applyValueToInputSignal(node, value);
  }
}

// Usar assim:
const result = await render(MeuComponente, { detectChangesOnRender: false, schemas: [NO_ERRORS_SCHEMA] });
setSignalInput(result.fixture.componentInstance, 'nomeDoInput', valor);
result.fixture.detectChanges();
await result.fixture.whenStable();
```

**Subscrever em outputs:** usar `comp.meuOutput.subscribe(spy)` diretamente — não usar `outputBinding`.

## Armadilha 2: Smart Components com filhos usando `input.required()`

**Sintoma:** NG0950 propagado dos componentes filhos mesmo com `NO_ERRORS_SCHEMA`.
**Causa:** Mesmo com `NO_ERRORS_SCHEMA`, o compilador JIT ainda instancia os filhos standalone do array `imports`, causando NG0950.

**Solução para Smart Components (e.g. AtributosConfigComponent, NiveisConfigComponent):**
```typescript
const TEMPLATE_STUB = `
  <div>
    @if (hasGame()) { <span>{{ currentGameName() }}</span> }
    @if (!hasGame()) { <p>Nenhum jogo selecionado</p> }
  </div>
`;

const result = await render(MeuSmartComponent, {
  configureTestBed: (tb) => {
    tb.overrideTemplate(MeuSmartComponent, TEMPLATE_STUB);
  },
  providers: [ /* mocks */ ],
});
```
Os testes verificam lógica via `fixture.componentInstance` (signals, métodos) — não via DOM.

## Armadilha 3: Static attribute bindings para `input()` em JIT

**Sintoma:** `message="texto"` como atributo estático passa a string mas o `input()` signal não recebe o valor — renderiza com o default.
**Causa:** Em JIT, atributos estáticos passados para `input()` de componentes filhos não propagam via o mecanismo de signal inputs.

**Solução:** Nos testes, verificar a presença do componente filho via `querySelector('app-empty-state')` em vez de verificar o texto do conteúdo interno. Ou verificar o sinal do componente pai que controla qual branch renderiza.

## Armadilha 4: `vi.useFakeTimers()` no `beforeEach` trava `fixture.whenStable()`

**Sintoma:** Testes de debounce/timer travam com timeout de 5000ms.
**Causa:** `fixture.whenStable()` usa Zone.js que aguarda macrotasks. Com fake timers ativos, macrotasks nunca disparam automaticamente, causando hang infinito.

**Solução:** Chamar `vi.useFakeTimers()` APÓS o `await criarFixture()` em cada teste individual — não no `beforeEach`:
```typescript
it('deve disparar validação após 600ms', async () => {
  const { component } = await criarFixture(); // real timers durante init
  vi.useFakeTimers();                          // fake timers DEPOIS
  // ... lógica do teste com vi.advanceTimersByTime(600)
});
```
O `afterEach` global com `vi.useRealTimers()` garante limpeza.

**Why:** Descoberto em 2026-04-04 ao corrigir 38 testes falhando em URG-02. Padrões validados e funcionando em 359 testes.

**How to apply:** Toda vez que criar ou editar specs Angular neste projeto, verificar qual das 4 armadilhas se aplica ANTES de escrever o teste.
