# T8 — LevelUpDialogComponent + Step 1: Atributos

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: T7 (detecção de level up e badge)
> Bloqueia: T9 (Step 2 — Aptidões)
> Design detalhado: `docs/design/LEVEL-UP.md`

---

## Objetivo

Criar o `LevelUpDialogComponent` — container smart com `p-dialog` + `p-stepper` — e implementar o Step 1: distribuição de pontos de atributo. Este é o coração do fluxo de level up.

## Arquivos

Novos:
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/level-up-dialog.component.ts`
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/level-up-dialog.component.spec.ts`
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/steps/level-up-atributos-step/level-up-atributos-step.component.ts`

## Passos

### 1. LevelUpDialogComponent (container smart)

**Responsabilidades:**
- Receber inputs: `fichaId`, `nivelAtual`, `limitadorAtributo`, `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`
- Controlar `p-stepper` com 3 steps
- Emitir `fechado` output quando o dialog for fechado
- Orquestrar os requests de PUT atributos e PUT aptidões
- Gerenciar estado de loading e erros

**Inputs/Outputs:**
```typescript
fichaId = input.required<number>();
nivelNovo = input.required<number>();
fichaNome = input.required<string>();
limitadorAtributo = input.required<number>();
pontosAtributoDisponiveis = input.required<number>();
pontosAptidaoDisponiveis = input.required<number>();
pontosVantagemDisponiveis = input.required<number>();
atributos = input.required<FichaAtributoResponse[]>();
aptidoes = input.required<FichaAptidaoResponse[]>();

fechado = output<void>();
distribuicaoSalva = output<void>();  // sinal para recarregar resumo no pai
```

**Confirmação ao fechar:**
```typescript
protected tentarFechar(): void {
  const temPendentes = this.pontosAtributoPendentes() > 0 || this.pontosAptidaoPendentes() > 0;
  if (temPendentes) {
    this.confirmationService.confirm({
      header: 'Pontos não distribuídos',
      message: `Você ainda tem pontos para distribuir. Os pontos serão mantidos para distribuir mais tarde.`,
      acceptLabel: 'Sim, fechar',
      rejectLabel: 'Continuar distribuindo',
      accept: () => this.fechado.emit(),
    });
  } else {
    this.fechado.emit();
  }
}
```

### 2. Estrutura do template

```html
<p-dialog
  [header]="fichaNome() + ' — Nível ' + nivelNovo() + '!'"
  [visible]="true"
  (visibleChange)="$event ? null : tentarFechar()"
  [modal]="true"
  [closable]="true"
  [style]="{ width: '90vw', maxWidth: '720px' }"
  [breakpoints]="{ '768px': '100vw' }"
  [contentStyle]="{ padding: '0' }">

  <p-stepper [activeStep]="stepAtivo()" [linear]="false">
    <p-step-list>
      <p-step [value]="0">Atributos</p-step>
      <p-step [value]="1">Aptidões</p-step>
      <p-step [value]="2">Vantagens</p-step>
    </p-step-list>

    <p-step-panels>
      <p-step-panel [value]="0">
        <app-level-up-atributos-step
          [atributos]="atributos()"
          [pontosDisponiveis]="pontosAtributoRestantes()"
          [limitadorAtributo]="limitadorAtributo()"
          (distribuicaoChanged)="onAtributosChanged($event)" />

        <div class="flex justify-content-end gap-2 p-3">
          <p-button label="Próximo: Aptidões" icon="pi pi-arrow-right" iconPos="right"
                    (onClick)="stepAtivo.set(1)" />
        </div>
      </p-step-panel>

      <p-step-panel [value]="1">
        <!-- Step 2 — implementado em T9 -->
      </p-step-panel>

      <p-step-panel [value]="2">
        <!-- Step 3 — implementado em T10 -->
      </p-step-panel>
    </p-step-panels>
  </p-stepper>

  <p-confirmDialog />
</p-dialog>
```

### 3. LevelUpAtributosStepComponent (dumb)

**Tipo:** Dumb — recebe dados via `input()`, emite via `output()`.

**Inputs:**
```typescript
atributos = input.required<FichaAtributoResponse[]>();
pontosDisponiveis = input.required<number>();
limitadorAtributo = input.required<number>();
```

**Output:**
```typescript
distribuicaoChanged = output<Record<string, number>>();
// sigla → quantidade adicionada NESTA SESSÃO (não o valor total do campo)
```

**Estado interno:**
```typescript
protected pontosAdicionados = signal<Record<string, number>>({});
protected pontosRestantes = computed(() =>
  this.pontosDisponiveis() - Object.values(this.pontosAdicionados()).reduce((a, b) => a + b, 0)
);
protected atributosComDistribuicao = computed(() =>
  this.atributos().map(a => ({
    ...a,
    nivelComDistribuicao: a.nivel + (this.pontosAdicionados()[a.atributoAbreviacao] ?? 0),
    totalComDistribuicao: a.total + (this.pontosAdicionados()[a.atributoAbreviacao] ?? 0),
  }))
);
protected todosNoLimite = computed(() =>
  this.atributosComDistribuicao().every(a => a.totalComDistribuicao >= this.limitadorAtributo())
);
```

**Regras dos botões:**
```typescript
protected podeAdicionar(sigla: string): boolean {
  const atributo = this.atributosComDistribuicao().find(a => a.atributoAbreviacao === sigla);
  return this.pontosRestantes() > 0 && !!atributo && atributo.totalComDistribuicao < this.limitadorAtributo();
}

protected podeRemover(sigla: string): boolean {
  return (this.pontosAdicionados()[sigla] ?? 0) > 0;
}

protected adicionarPonto(sigla: string): void {
  if (!this.podeAdicionar(sigla)) return;
  const atual = this.pontosAdicionados()[sigla] ?? 0;
  this.pontosAdicionados.update(m => ({ ...m, [sigla]: atual + 1 }));
  this.distribuicaoChanged.emit(this.pontosAdicionados());
}

protected removerPonto(sigla: string): void {
  if (!this.podeRemover(sigla)) return;
  const atual = this.pontosAdicionados()[sigla] ?? 0;
  this.pontosAdicionados.update(m => ({ ...m, [sigla]: atual - 1 }));
  this.distribuicaoChanged.emit(this.pontosAdicionados());
}
```

### 4. Salvar distribuição de atributos

No `LevelUpDialogComponent`, ao avançar para Step 2 (ou ao clicar "Salvar Atributos"):

```typescript
protected salvarAtributos(): void {
  if (Object.keys(this.distribuicaoAtributos()).length === 0) {
    this.stepAtivo.set(1);
    return;
  }
  this.salvando.set(true);
  const dto: AtualizarAtributoDto[] = this.atributos().map(a => ({
    atributoConfigId: a.atributoConfigId,
    base: a.base,
    nivel: a.nivel + (this.distribuicaoAtributos()[a.atributoAbreviacao] ?? 0),
    outros: a.outros,
  }));
  this.fichasApi.atualizarAtributos(this.fichaId(), dto).subscribe({
    next: () => {
      this.salvando.set(false);
      this.stepAtivo.set(1);
      this.distribuicaoSalva.emit();
    },
    error: () => {
      this.salvando.set(false);
      this.toastService.add({ severity: 'error', summary: 'Erro ao salvar atributos', life: 6000 });
    },
  });
}
```

### 5. Aviso quando todos no limite

```html
@if (atributosStep.todosNoLimite() && atributosStep.pontosRestantes() > 0) {
  <p-message severity="warn" class="m-3">
    Todos os atributos atingiram o teto do nível ({{ limitadorAtributo() }}).
    Os {{ atributosStep.pontosRestantes() }} pontos restantes não podem ser distribuídos.
  </p-message>
}
```

### 6. Acessibilidade

- Contador de pontos restantes: `aria-live="polite"` e `aria-label="N pontos de atributo restantes"`
- Botões `[+]` e `[-]`: `aria-label="Adicionar ponto em {nome}"` / `"Remover ponto de {nome}"`
- Barra de progresso: `aria-valuemin="0"`, `aria-valuemax="{{ limitadorAtributo() }}"`, `aria-valuenow="{{ total }}"`
- Dialog: `aria-modal="true"`, foco inicial no primeiro `[+]` habilitado

## Critérios de Aceitação

- [ ] `LevelUpDialogComponent` abre via badge de pontos pendentes e via level up automático
- [ ] `p-stepper` com 3 steps navegáveis
- [ ] Step 1: grid de atributos com controles `[+]` e `[-]`
- [ ] Contador de pontos restantes decrementa/incrementa reativamente
- [ ] Botão `[+]` desabilitado quando `pontosRestantes === 0`
- [ ] Botão `[+]` desabilitado quando `total >= limitadorAtributo`
- [ ] Botão `[-]` desabilitado quando `pontosAdicionados[sigla] === 0`
- [ ] Barra de progresso por atributo (total / limitador)
- [ ] Aviso quando todos os atributos atingiram o limite e restam pontos
- [ ] Ao salvar: `PUT /api/v1/fichas/{id}/atributos` chamado corretamente
- [ ] Loading state no botão durante o request
- [ ] Toast de erro sem reverter estado local em caso de falha
- [ ] Confirmação ao fechar com pontos pendentes
- [ ] Acessibilidade: aria-live, aria-label nos controles
- [ ] Mobile: dialog fullscreen em viewports < 768px
