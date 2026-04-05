# T9 — LevelUp Step 2: Distribuição de Aptidões

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: T8 (LevelUpDialogComponent container)
> Bloqueia: T10 (Step 3 — Vantagens)

---

## Objetivo

Implementar o Step 2 do wizard de level up: distribuição de pontos de aptidão. As aptidões são agrupadas por `tipoAptidao` via `p-accordion`, e apenas o campo `base` é editável pelo Jogador.

## Arquivos

Novo:
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/steps/level-up-aptidoes-step/level-up-aptidoes-step.component.ts`

Modificado:
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/level-up-dialog.component.ts` — adicionar Step 2 no stepper

## Passos

### 1. LevelUpAptidoesStepComponent (dumb)

**Inputs:**
```typescript
aptidoes = input.required<FichaAptidaoResponse[]>();
pontosDisponiveis = input.required<number>();
```

**Output:**
```typescript
distribuicaoChanged = output<Record<number, number>>();
// aptidaoConfigId → quantidade adicionada ao base NESTA SESSÃO
```

**Estado interno:**
```typescript
protected pontosAdicionados = signal<Record<number, number>>({});
protected pontosRestantes = computed(() =>
  this.pontosDisponiveis() - Object.values(this.pontosAdicionados()).reduce((a, b) => a + b, 0)
);

// Agrupamento por tipoAptidao para p-accordion
protected aptidoesPorTipo = computed(() => {
  const grupos = new Map<string, FichaAptidaoResponse[]>();
  for (const apt of this.aptidoes()) {
    const tipo = apt.tipoAptidaoNome ?? 'Sem tipo';
    if (!grupos.has(tipo)) grupos.set(tipo, []);
    grupos.get(tipo)!.push(apt);
  }
  return Array.from(grupos.entries()).map(([tipo, aptidoes]) => ({ tipo, aptidoes }));
});
```

**Nota:** O `FichaAptidaoResponse` atual não tem `tipoAptidaoNome`. Verificar se o backend retorna esse campo ou se é necessário fazer join com `AptidaoConfig` para obter o tipo. Esta pode ser uma dependência de outro endpoint. Se o campo não existir, agrupar todas sem tipo ou buscar os `AptidaoConfig` do `ConfigStore`.

### 2. Layout com p-accordion

```html
<!-- Contador de pontos restantes -->
<div class="flex justify-content-center py-4">
  <span class="text-4xl font-bold"
        [class.text-warn-color]="pontosRestantes() > 0"
        [class.text-success-color]="pontosRestantes() === 0"
        aria-live="polite"
        [attr.aria-label]="pontosRestantes() + ' pontos de aptidão restantes'">
    {{ pontosRestantes() }}
  </span>
</div>

<!-- Grupos de aptidões -->
<p-accordion [multiple]="true">
  @for (grupo of aptidoesPorTipo(); track grupo.tipo) {
    <p-accordion-panel [value]="grupo.tipo">
      <p-accordion-header>{{ grupo.tipo }} ({{ grupo.aptidoes.length }})</p-accordion-header>
      <p-accordion-content>
        @for (apt of grupo.aptidoes; track apt.aptidaoConfigId) {
          <div class="flex align-items-center justify-content-between gap-2 py-2 border-bottom-1 surface-border">
            <!-- Nome e campos readonly -->
            <div class="flex-1">
              <span class="font-semibold text-sm">{{ apt.aptidaoNome }}</span>
              <div class="flex gap-3 text-xs text-color-secondary mt-1">
                <span pTooltip="Controlado pelo Mestre">Sorte: <strong>{{ apt.sorte }}</strong></span>
                <span pTooltip="Controlado pelo Mestre">Classe: <strong>{{ apt.classe }}</strong></span>
                <span>Total: <strong>{{ apt.total + (pontosAdicionados()[apt.aptidaoConfigId] ?? 0) }}</strong></span>
              </div>
            </div>

            <!-- Controles de base -->
            <div class="flex align-items-center gap-2">
              <p-button icon="pi pi-minus" text rounded size="small" severity="secondary"
                        [disabled]="(pontosAdicionados()[apt.aptidaoConfigId] ?? 0) === 0"
                        (onClick)="removerPonto(apt.aptidaoConfigId)"
                        [attr.aria-label]="'Remover ponto de ' + apt.aptidaoNome" />
              <span class="font-bold min-w-6 text-center" aria-live="polite">
                {{ apt.base + (pontosAdicionados()[apt.aptidaoConfigId] ?? 0) }}
              </span>
              <p-button icon="pi pi-plus" text rounded size="small" severity="success"
                        [disabled]="pontosRestantes() === 0"
                        (onClick)="adicionarPonto(apt.aptidaoConfigId)"
                        [attr.aria-label]="'Adicionar ponto em ' + apt.aptidaoNome" />
            </div>
          </div>
        }
      </p-accordion-content>
    </p-accordion-panel>
  }
</p-accordion>
```

### 3. Diferença para Step 1

- Sem limitador por aptidão (não há `limitadorAtributo` para aptidões)
- Os campos `sorte` e `classe` são readonly com tooltip "Controlado pelo Mestre"
- O campo editável é `base` (não `nivel` como nos atributos)
- Agrupamento por tipo com `p-accordion`

### 4. Salvar aptidões

No `LevelUpDialogComponent`, método `salvarAptidoes()`:

```typescript
protected salvarAptidoes(): void {
  if (Object.keys(this.distribuicaoAptidoes()).length === 0) {
    this.stepAtivo.set(2);
    return;
  }
  this.salvando.set(true);
  const dto: AtualizarAptidaoDto[] = this.aptidoes().map(a => ({
    aptidaoConfigId: a.aptidaoConfigId,
    base: a.base + (this.distribuicaoAptidoes()[a.aptidaoConfigId] ?? 0),
    sorte: a.sorte,
    classe: a.classe,
  }));
  this.fichasApi.atualizarAptidoes(this.fichaId(), dto).subscribe({
    next: () => {
      this.salvando.set(false);
      this.stepAtivo.set(2);
      this.distribuicaoSalva.emit();
    },
    error: () => {
      this.salvando.set(false);
      this.toastService.add({ severity: 'error', summary: 'Erro ao salvar aptidões', life: 6000 });
    },
  });
}
```

## Critérios de Aceitação

- [ ] Step 2 integrado ao LevelUpDialogComponent
- [ ] Aptidões agrupadas por tipo com `p-accordion`
- [ ] Campos `sorte` e `classe` readonly com tooltip "Controlado pelo Mestre"
- [ ] Apenas campo `base` editável com controles +/-
- [ ] Contador de pontos restantes decrementa/incrementa
- [ ] Botão `[+]` desabilitado quando `pontosRestantes === 0`
- [ ] Ao salvar: `PUT /api/v1/fichas/{id}/aptidoes` chamado corretamente
- [ ] Loading state e erro tratados
- [ ] Navegação para Step 3 após salvar

## Pontos em Aberto

- **P-04:** `FichaAptidaoResponse` tem `tipoAptidaoNome`? Se não, como obter o tipo para o agrupamento? Verificar o backend antes de implementar o agrupamento.
