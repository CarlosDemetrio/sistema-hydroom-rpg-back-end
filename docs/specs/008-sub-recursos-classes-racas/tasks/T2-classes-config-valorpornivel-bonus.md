# T2 — ClassesConfigComponent: campo valorPorNivel na aba Bonus + campo bonus na aba Aptidoes

> **Complexidade:** media
> **Depende de:** T1 (tipagem corrigida)
> **Bloqueia:** T4
> **Arquivo principal:** `classes-config.component.ts`

---

## Objetivo

Completar a aba "Bônus" do drawer de ClassePersonagem com campo de `valorPorNivel` (decimal),
exibicao do valor na lista e envio correto ao backend. Completar a aba "Aptidoes c/ Bônus" com
campo de `bonus` inteiro (>= 0) e envio correto ao backend.

---

## Contexto

Apos T1, o TypeScript passara a exigir `valorPorNivel` em `addClasseBonus` e `bonus` em
`addClasseAptidaoBonus`. Este task resolve esses erros de compilacao e adiciona os campos visuais.

### Estado atual da aba "Bônus"

- Lista exibe `bonus.bonusNome` e a formula do BonusConfig (via `getBonusFormula`)
- **Nao exibe** `bonus.valorPorNivel`
- Dropdown de BonusConfig disponivel funciona
- **Nao tem** campo de input para `valorPorNivel`
- `addClasseBonus` envia `{ bonusConfigId: bonusId }` — falta `valorPorNivel`

### Estado atual da aba "Aptidoes c/ Bônus"

- Lista exibe apenas `apt.aptidaoNome`
- **Nao exibe** `apt.bonus`
- Dropdown de AptidaoConfig disponivel funciona
- **Nao tem** campo de input para `bonus`
- `addClasseAptidaoBonus` envia `{ aptidaoConfigId: aptidaoId }` — falta `bonus`

---

## Arquivos Afetados

1. `src/app/features/mestre/pages/config/configs/classes-config/classes-config.component.ts`

---

## Passos de Implementacao

### Passo 1 — Adicionar signal `valorPorNivelInput` na classe TypeScript

Na secao de signals do componente, adicionar:
```typescript
protected valorPorNivelInput = signal<number>(1.0);
protected bonusAptidaoInput = signal<number>(0);
```

### Passo 2 — Corrigir `addClasseBonus()`

Alterar o metodo para incluir `valorPorNivel`:

```typescript
addClasseBonus(): void {
  const classeId = this.selectedClasse()?.id;
  const bonusId = this.selectedBonusId();
  const valorPorNivel = this.valorPorNivelInput();
  if (!classeId || !bonusId || valorPorNivel <= 0) return;
  this.configApi.addClasseBonus(classeId, { bonusConfigId: bonusId, valorPorNivel })
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: () => {
        this.selectedBonusId.set(null);
        this.valorPorNivelInput.set(1.0);
        this.refreshSelectedClasse(classeId);
        this.toastService.success('Bônus adicionado à classe', 'Sucesso');
      },
    });
}
```

### Passo 3 — Corrigir `addClasseAptidaoBonus()`

```typescript
addClasseAptidaoBonus(): void {
  const classeId = this.selectedClasse()?.id;
  const aptidaoId = this.selectedAptidaoId();
  const bonus = this.bonusAptidaoInput();
  if (!classeId || !aptidaoId || bonus < 0) return;
  this.configApi.addClasseAptidaoBonus(classeId, { aptidaoConfigId: aptidaoId, bonus })
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: () => {
        this.selectedAptidaoId.set(null);
        this.bonusAptidaoInput.set(0);
        this.refreshSelectedClasse(classeId);
        this.toastService.success('Aptidão adicionada à classe', 'Sucesso');
      },
    });
}
```

### Passo 4 — Atualizar template da aba "Bônus"

Na secao de lista de `bonusConfig`, exibir o `valorPorNivel`:

```html
<div class="flex flex-column gap-1">
  <span class="font-semibold">{{ bonus.bonusNome }}</span>
  <span class="text-sm text-color-secondary">
    +{{ bonus.valorPorNivel }} por nível
  </span>
  @if (getBonusFormula(bonus.bonusConfigId); as formula) {
    <code class="text-xs text-color-secondary font-mono">{{ formula }}</code>
  }
</div>
```

Na secao de adicionar bônus, adicionar campo `valorPorNivel` antes do botao "Adicionar".
Trocar a linha `<div class="flex gap-2 mt-2">` por um layout em duas linhas:

```html
<!-- Linha 1: dropdown de bônus -->
<p-select
  [options]="bonusDisponiveis()"
  [(ngModel)]="selectedBonusId"
  optionLabel="nome"
  optionValue="id"
  placeholder="Selecione um bônus..."
  class="w-full"
/>
<!-- Linha 2: valor por nível + botão -->
<div class="flex gap-2 align-items-center">
  <div class="flex flex-column gap-1 flex-1">
    <label class="text-sm font-semibold">Valor por nível</label>
    <p-input-number
      [(ngModel)]="valorPorNivelInput"
      [showButtons]="true"
      [min]="0.01"
      [step]="0.01"
      [minFractionDigits]="2"
      [maxFractionDigits]="2"
      style="width: 100%"
    />
    @if (selectedBonusId() && valorPorNivelInput()) {
      <small class="text-color-secondary">
        Exemplo no nível 5: +{{ (valorPorNivelInput() * 5) | number:'1.0-2' }}
      </small>
    }
  </div>
  <p-button
    icon="pi pi-plus"
    label="Adicionar"
    [disabled]="!selectedBonusId() || valorPorNivelInput() <= 0"
    (onClick)="addClasseBonus()"
    class="align-self-end"
  />
</div>
```

Adicionar `DecimalPipe` aos imports do componente (necessario para `| number`):
```typescript
import { DecimalPipe } from '@angular/common';
// e na lista de imports do decorator:
DecimalPipe,
```

### Passo 5 — Atualizar template da aba "Aptidoes c/ Bônus"

Na lista de aptidoes, exibir o `bonus`:

```html
<div class="flex justify-content-between align-items-center p-3 surface-100 border-round">
  <div class="flex align-items-center gap-2">
    <span class="font-semibold">{{ apt.aptidaoNome }}</span>
    <span class="text-sm font-semibold text-green-400">+{{ apt.bonus }}</span>
  </div>
  <p-button
    icon="pi pi-trash"
    [rounded]="true"
    [text]="true"
    severity="danger"
    (onClick)="removeClasseAptidaoBonus(apt.id)"
    pTooltip="Remover aptidão"
  />
</div>
```

Na secao de adicionar aptidao, adicionar campo `bonus`:

```html
<div class="flex gap-2 mt-2">
  <p-select
    [options]="aptidoesDisponiveis()"
    [(ngModel)]="selectedAptidaoId"
    optionLabel="nome"
    optionValue="id"
    placeholder="Selecione uma aptidão..."
    class="flex-1"
  />
  <p-input-number
    [(ngModel)]="bonusAptidaoInput"
    [showButtons]="true"
    [min]="0"
    placeholder="Bônus"
    pTooltip="Bônus fixo (mín. 0)"
    style="width: 7rem"
  />
  <p-button
    icon="pi pi-plus"
    label="Adicionar"
    [disabled]="!selectedAptidaoId() || bonusAptidaoInput() < 0"
    (onClick)="addClasseAptidaoBonus()"
  />
</div>
```

---

## Criterios de Aceitacao

- [ ] Campo `valorPorNivel` aparece na aba "Bônus" com `step=0.01` e aceita valores como 0.5
- [ ] Lista da aba "Bônus" exibe `valorPorNivel` ao lado do nome do BonusConfig
- [ ] Preview "Exemplo no nivel 5: +X" atualiza em tempo real conforme o usuario digita
- [ ] `addClasseBonus` envia `{ bonusConfigId, valorPorNivel }` ao backend (verificar via DevTools Network)
- [ ] Campo `bonus` aparece na aba "Aptidoes c/ Bônus" com `min=0` e nao aceita negativos
- [ ] Lista da aba "Aptidoes" exibe `bonus` ao lado do nome da AptidaoConfig
- [ ] `addClasseAptidaoBonus` envia `{ aptidaoConfigId, bonus }` ao backend
- [ ] Botao "Adicionar" ClasseBonus desabilitado quando `valorPorNivel <= 0`
- [ ] Botao "Adicionar" ClasseAptidaoBonus desabilitado quando `bonus < 0`
- [ ] `npm run build` sem erros
- [ ] `npx vitest run` sem regressao
