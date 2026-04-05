# T8 — Frontend: Barra de Essencia Reativa

> Tipo: Frontend (Angular)
> Dependencias: T5 (confirmar que essenciaAtual esta no FichaResumoResponse)
> Complexidade: Baixa

---

## Objetivo

Substituir qualquer valor hardcoded de essencia no frontend por dados vindos de `GET /fichas/{id}/resumo`. A barra de essencia deve exibir `essenciaAtual / essenciaTotal` em tempo real, da mesma forma que a barra de vida.

---

## Contexto

O `PUT /fichas/{id}/vida` ja aceita e persiste `essenciaAtual`. O `FichaEssencia` ja tem o campo. A T5 garante que o `/resumo` expoe esses valores. Esta task e o ajuste do frontend para consumir esses campos.

---

## Arquivos a Verificar/Alterar

| Arquivo | O que verificar |
|---------|----------------|
| `ficha/components/ficha-stats-bar/ficha-stats-bar.component.ts` | Usa valores hardcoded? Recebe `essenciaAtual` e `essenciaTotal` como inputs? |
| `ficha/pages/ficha-detail/ficha-detail.page.ts` | Passa `resumo().essenciaAtual` e `resumo().essenciaTotal` para o stats-bar? |
| `ficha/models/ficha-resumo.model.ts` (ou similar) | Interface TypeScript tem `essenciaAtual` e `essenciaTotal`? |

---

## Passos

### Passo 1 — Interface FichaResumoResponse

Verificar e adicionar se necessario:
```typescript
export interface FichaResumoResponse {
  // ...existentes...
  essenciaTotal: number;
  essenciaAtual: number;    // adicionar se ausente
  vidaTotal: number;
  vidaAtual: number;
  // ...
}
```

### Passo 2 — FichaStatsBarComponent

Props esperadas:
```typescript
vidaTotal = input.required<number>();
vidaAtual = input.required<number>();
essenciaTotal = input.required<number>();
essenciaAtual = input.required<number>();
ameacaTotal = input.required<number>();
```

Template (barra de essencia):
```html
<!-- Essencia -->
<div class="flex flex-col gap-1">
  <div class="flex justify-between text-sm">
    <span class="font-semibold text-blue-700">Essencia</span>
    <span>{{ essenciaAtual() }} / {{ essenciaTotal() }}</span>
  </div>
  <p-progressbar
    [value]="essenciaTotal() > 0 ? (essenciaAtual() / essenciaTotal() * 100) : 0"
    [showValue]="false"
    styleClass="h-3"
    aria-label="Essencia: {{ essenciaAtual() }} de {{ essenciaTotal() }}" />
</div>
```

> Nunca calcular `(essenciaAtual / 100)` ou usar valor hardcoded como denominador.

### Passo 3 — FichaDetailPage

Confirmar que o page passa os valores do resumo para o stats-bar:
```html
<app-ficha-stats-bar
  [vidaTotal]="resumo()?.vidaTotal ?? 0"
  [vidaAtual]="resumo()?.vidaAtual ?? 0"
  [essenciaTotal]="resumo()?.essenciaTotal ?? 0"
  [essenciaAtual]="resumo()?.essenciaAtual ?? 0"
  [ameacaTotal]="resumo()?.ameacaTotal ?? 0" />
```

### Passo 4 — Atualizar apos PUT /vida

O `PUT /fichas/{id}/vida` ja retorna `FichaResumoResponse` (confirmado no backend). O frontend deve atualizar o signal `resumo()` com a resposta do PUT para refletir imediatamente na barra.

```typescript
// No handler de atualizarVida do FichaDetailPage:
this.fichasApiService.atualizarVida(this.fichaId(), dto).subscribe(resumoAtualizado => {
  this.resumo.set(resumoAtualizado);
});
```

---

## Testes (Vitest)

- `FichaStatsBarComponent`: testar que `essenciaAtual=10`, `essenciaTotal=20` resulta em progressbar com value=50
- `FichaStatsBarComponent`: testar que `essenciaTotal=0` nao causa divisao por zero (value=0)
- `FichaDetailPage`: testar que signal `resumo` e atualizado apos sucesso do PUT

---

## Criterios de Aceitacao

- [ ] Barra de essencia exibe `essenciaAtual / essenciaTotal` vindos do endpoint `/resumo`
- [ ] Nenhum valor de essencia e hardcoded no template ou no component
- [ ] Divisao por zero impossivel (if guard `essenciaTotal > 0`)
- [ ] Barra atualiza imediatamente apos PUT /vida com novo `essenciaAtual`
- [ ] `aria-label` na progressbar inclui os valores numericos para acessibilidade
- [ ] 0 erros TypeScript, 0 warnings
