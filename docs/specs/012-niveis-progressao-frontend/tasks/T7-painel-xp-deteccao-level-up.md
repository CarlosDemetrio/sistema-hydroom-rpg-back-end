# T7 — Painel de XP do Mestre + Detecção de Level Up

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: T6 (FichaResumo com campos de pontos)
> Bloqueia: T8, T9, T10

---

## Objetivo

Implementar no FichaDetailPage:
1. Painel de XP visível para o Mestre com botão para conceder XP.
2. Barra de progresso de XP (XP atual / XP para próximo nível).
3. Lógica de detecção de level up ao receber ficha atualizada.
4. Toast especial de level up com estilo dourado.
5. Animação CSS "flash" no FichaHeaderComponent.
6. Badge de pontos pendentes no FichaHeaderComponent.

## Arquivos Afetados

- `src/app/features/jogador/pages/ficha-detail/ficha-detail.component.ts`
- `src/app/features/jogador/pages/ficha-detail/components/ficha-header/ficha-header.component.ts`
- `src/app/features/jogador/pages/ficha-detail/components/ficha-resumo-tab/` (exibição de XP)

## Passos

### 1. Painel de XP no FichaResumoTab ou FichaHeader

Verificar onde fica a exibição de XP atualmente na UI. Se não existir, adicionar no `FichaResumoTabComponent`:

```html
<!-- Visível para MESTRE e JOGADOR -->
<div class="xp-panel">
  <span>XP: {{ ficha().xp | number:'1.0-0':'pt-BR' }}</span>
  @if (proximoNivel(); as proximo) {
    <span> / {{ proximo.xpNecessaria | number:'1.0-0':'pt-BR' }}</span>
    <p-progressBar [value]="progressoXp()" [showValue]="false" />
  }

  <!-- Botão visível apenas para MESTRE -->
  @if (isMestre()) {
    <p-button
      label="+XP"
      icon="pi pi-plus"
      severity="secondary"
      size="small"
      (onClick)="abrirDialogXp()" />
  }
</div>
```

Computed para barra de progresso:
```typescript
protected progressoXp = computed(() => {
  const ficha = this.ficha();
  const niveis = this.configStore.niveis();
  const nivelAtual = niveis.find(n => n.nivel === ficha.nivel);
  const proximoNivel = niveis.find(n => n.nivel === ficha.nivel + 1);
  if (!nivelAtual || !proximoNivel) return 100;
  const xpBase = nivelAtual.xpNecessaria;
  const xpProximo = proximoNivel.xpNecessaria;
  return Math.min(100, ((ficha.xp - xpBase) / (xpProximo - xpBase)) * 100);
});
```

### 2. Dialog de Concessão de XP (Mestre)

`p-dialog` simples acionado por `abrirDialogXp()`:

```html
<p-dialog header="Conceder XP" [visible]="dialogXpVisivel()" (visibleChange)="dialogXpVisivel.set($event)">
  <div class="flex flex-column gap-3 p-3">
    <label for="qtdXp">Quantidade de XP</label>
    <p-input-number
      inputId="qtdXp"
      [(ngModel)]="quantidadeXp"
      [min]="1"
      [useGrouping]="true"
      placeholder="Ex: 500" />
  </div>
  <ng-template #footer>
    <p-button label="Cancelar" severity="secondary" (onClick)="fecharDialogXp()" />
    <p-button
      label="Confirmar"
      [loading]="salvandoXp()"
      [disabled]="!quantidadeXp() || quantidadeXp() < 1"
      (onClick)="concederXp()" />
  </ng-template>
</p-dialog>
```

Método `concederXp()`:
```typescript
protected concederXp(): void {
  const nivelAntes = this.ficha().nivel;
  this.salvandoXp.set(true);
  this.fichasApi.updateFicha(this.fichaId(), {
    xp: this.ficha().xp + this.quantidadeXp()
  }).subscribe({
    next: (fichaAtualizada) => {
      this.fecharDialogXp();
      this.salvandoXp.set(false);
      if (fichaAtualizada.nivel > nivelAntes) {
        this.onLevelUp(nivelAntes, fichaAtualizada.nivel);
      }
      this.carregarFicha(); // recarregar ficha completa incluindo resumo
    },
    error: () => this.salvandoXp.set(false)
  });
}
```

### 3. Método `onLevelUp(nivelAntes, nivelNovo)`

```typescript
private onLevelUp(nivelAntes: number, nivelNovo: number): void {
  // 1. Toast especial
  this.toastService.add({
    severity: 'success',
    summary: `NÍVEL ${nivelNovo}!`,
    detail: `${this.ficha().nome} subiu para o Nível ${nivelNovo}! Distribua os pontos ganhos.`,
    life: 8000,
    styleClass: 'level-up-toast',
  });

  // 2. Animação CSS no header
  this.fichaHeaderAnimando.set(true);
  setTimeout(() => this.fichaHeaderAnimando.set(false), 1500);

  // 3. Abrir dialog de level up se há pontos (verificado após recarregar resumo)
  // A abertura do dialog é feita no effect/afterLoad para garantir que resumo foi recarregado
}
```

### 4. Toast de Level Up — CSS

Adicionar estilo global (ex: `styles.scss` ou arquivo de estilos globais):

```css
.level-up-toast .p-toast-message {
  border-left: 4px solid var(--yellow-500);
  background: linear-gradient(135deg, var(--surface-card) 0%, var(--yellow-50) 100%);
}
.level-up-toast .p-toast-summary {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--yellow-700);
  letter-spacing: 0.05em;
}
```

### 5. Animação CSS no FichaHeaderComponent

```typescript
// FichaHeaderComponent recebe input:
animandoLevelUp = input<boolean>(false);
```

```css
/* No FichaHeaderComponent ou global */
@keyframes levelUpFlash {
  0%   { box-shadow: 0 0 0 0 var(--yellow-400); }
  50%  { box-shadow: 0 0 0 16px rgba(234, 179, 8, 0.3); }
  100% { box-shadow: 0 0 0 0 rgba(234, 179, 8, 0); }
}
.level-up-animating {
  animation: levelUpFlash 1.5s ease-out;
}
```

```html
<div [class.level-up-animating]="animandoLevelUp()">
  <!-- conteúdo do header -->
</div>
```

### 6. Badge de Pontos Pendentes no FichaHeaderComponent

```typescript
// FichaHeaderComponent recebe inputs:
pontosAtributoDisponiveis = input<number>(0);
pontosAptidaoDisponiveis = input<number>(0);

protected temPontosPendentes = computed(() =>
  this.pontosAtributoDisponiveis() > 0 || this.pontosAptidaoDisponiveis() > 0
);

protected labelPontosPendentes = computed(() => {
  const partes: string[] = [];
  if (this.pontosAtributoDisponiveis() > 0)
    partes.push(`${this.pontosAtributoDisponiveis()} atrib.`);
  if (this.pontosAptidaoDisponiveis() > 0)
    partes.push(`${this.pontosAptidaoDisponiveis()} apt.`);
  return partes.join(' + ') + ' para distribuir';
});

abrirLevelUpDialog = output<void>();
```

```html
@if (temPontosPendentes()) {
  <p-button
    [label]="labelPontosPendentes()"
    icon="pi pi-arrow-circle-up"
    severity="warn"
    size="small"
    (onClick)="abrirLevelUpDialog.emit()"
    [pTooltip]="'Você tem pontos para distribuir!'"
    tooltipPosition="bottom" />
}
```

No FichaDetailPage, ao receber o evento `abrirLevelUpDialog`, abrir o `LevelUpDialogComponent`.

### 7. Carregar badge ao abrir FichaDetail

Ao carregar o `FichaDetail`, verificar `resumo.pontosAtributoDisponiveis` e `resumo.pontosAptidaoDisponiveis`. Se qualquer um > 0, o badge aparece imediatamente (sem esperar level up).

## Critérios de Aceitação

- [ ] Painel de XP exibe XP atual com formatação de milhar
- [ ] Barra de progresso exibe progresso para o próximo nível (0-100%)
- [ ] Botão `[+XP]` visível apenas para MESTRE
- [ ] Dialog de concessão com InputNumber e validação min=1
- [ ] Após conceder XP: ficha recarregada e nível atualizado na UI
- [ ] Toast de level up com estilo dourado exibido por 8s quando nível sobe
- [ ] Animação "flash" no FichaHeaderComponent por 1.5s ao subir nível
- [ ] Badge de pontos pendentes exibido quando `pontosAtributoDisponiveis > 0 || pontosAptidaoDisponiveis > 0`
- [ ] Badge visível ao carregar FichaDetail (sem precisar de level up na sessão)
- [ ] Badge não aparece quando não há pontos pendentes

## Premissas

- A detecção de level up é feita comparando `ficha.nivel` antes e depois do `updateFicha()`
- O nível mostrado em `ficha.nivel` é o calculado pelo backend — não há cálculo client-side
- `this.configStore.niveis()` já está carregado quando FichaDetail abre (ConfigStore carregado no guard ou na inicialização do jogo)
