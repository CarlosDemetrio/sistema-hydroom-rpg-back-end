# T6 — Frontend: Modelo FichaResumo com Campos de Pontos Disponíveis

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: T5 (backend retornar os campos)
> Bloqueia: T7, T8, T9, T10, T11

---

## Objetivo

Atualizar o modelo TypeScript `FichaResumo` com os 3 novos campos que o backend passará a retornar após T5. Também atualizar o `ConfigStore` para expor `pontosVantagem()` para PontosVantagemConfigComponent.

## Arquivos Afetados

- `src/app/core/models/ficha.model.ts` — adicionar campos em `FichaResumo`
- `src/app/core/stores/config.store.ts` — adicionar signal `pontosVantagem()`

## Passos

### 1. Atualizar `FichaResumo` em `ficha.model.ts`

```typescript
export interface FichaResumo {
  id: number;
  nome: string;
  nivel: number;
  xp: number;
  racaNome: string | null;
  classeNome: string | null;
  atributosTotais: Record<string, number>;
  bonusTotais: Record<string, number>;
  vidaTotal: number;
  essenciaTotal: number;
  ameacaTotal: number;
  // NOVOS CAMPOS (após T5 backend):
  pontosAtributoDisponiveis: number;
  pontosAptidaoDisponiveis: number;
  pontosVantagemDisponiveis: number;
}
```

### 2. Atualizar `ConfigStore` para PontosVantagemConfig

Verificar se `ConfigStore` já carrega `PontosVantagemConfig`. Se não, adicionar:
- Signal `pontosVantagem = signal<PontosVantagemConfig[]>([])`
- Método de carregamento via `PontosVantagemConfigService`

Isso é necessário para `PontosVantagemConfigComponent` (T2) e para o cálculo de acumulado.

### 3. Verificar lugares que usam FichaResumo

Fazer busca por `FichaResumo` e `fichaResumo` no frontend para garantir que nenhum código existente quebre com os novos campos opcionais. Os novos campos são aditivos — código existente que não usa esses campos não será afetado.

Locais a verificar:
- `ficha-detail.component.ts` — como usa `resumo`
- `ficha-resumo-tab/` — se exibe campos do resumo
- `FichaCompletaData` — interface que agrega `ficha` e `resumo`

### 4. Atualizar stub nos testes

Todos os testes que criam stubs de `FichaResumo` precisam incluir os 3 novos campos (mesmo que com valor 0) para evitar erros TypeScript:

```typescript
const fichaResumoStub: FichaResumo = {
  // ... campos existentes ...
  pontosAtributoDisponiveis: 0,
  pontosAptidaoDisponiveis: 0,
  pontosVantagemDisponiveis: 0,
};
```

## Critérios de Aceitação

- [ ] Interface `FichaResumo` tem os 3 novos campos
- [ ] Build Angular sem erros TypeScript
- [ ] Stubs de teste atualizados em todos os arquivos `.spec.ts` que usam `FichaResumo`
- [ ] `ConfigStore` expõe `pontosVantagem()` signal
- [ ] 0 regressões nos testes existentes

## Premissas

- T5 (backend) deve ser implementado antes deste task para que os campos realmente venham do servidor
- Se T5 ainda não estiver pronto, os campos podem ser adicionados com `| undefined` temporariamente: `pontosAtributoDisponiveis?: number`
