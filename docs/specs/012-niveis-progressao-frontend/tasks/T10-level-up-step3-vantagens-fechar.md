# T10 — LevelUp Step 3: Vantagens (Informativo) + Fechar com Confirmação

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: ALTO
> Depende de: T9 (Step 2 concluído)
> Bloqueia: nada

---

## Objetivo

Implementar o Step 3 do wizard de level up — um passo informativo que exibe o saldo de pontos de vantagem e direciona o Jogador para a aba Vantagens. Completar também a lógica de confirmação ao fechar o dialog com pontos não distribuídos.

## Arquivos

Novo:
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/steps/level-up-vantagens-step/level-up-vantagens-step.component.ts`

Modificado:
- `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/level-up-dialog.component.ts` — Step 3 + lógica de fechar

## Passos

### 1. LevelUpVantagensStepComponent (dumb)

**Inputs:**
```typescript
pontosVantagemGanhos = input.required<number>();   // total acumulado ao nível atual
pontosVantagemGastos = input.required<number>();   // custoPago total
pontosVantagemDisponiveis = input.required<number>(); // ganhos - gastos
```

**Output:**
```typescript
irParaVantagens = output<void>();  // fechar dialog e navegar/scroll para aba Vantagens
fechar = output<void>();           // apenas fechar
```

**Template:**
```html
<div class="flex flex-column gap-4 p-4">

  <!-- Saldo de pontos de vantagem -->
  <div class="flex flex-column gap-2">
    <h3 class="text-lg font-semibold m-0">Pontos de Vantagem</h3>

    <div class="flex justify-content-between py-2 border-bottom-1 surface-border">
      <span class="text-color-secondary">Total acumulado:</span>
      <span class="font-semibold">{{ pontosVantagemGanhos() }}</span>
    </div>
    <div class="flex justify-content-between py-2 border-bottom-1 surface-border">
      <span class="text-color-secondary">Gastos:</span>
      <span class="font-semibold">{{ pontosVantagemGastos() }}</span>
    </div>
    <div class="flex justify-content-between py-2">
      <span class="font-semibold">Disponíveis:</span>
      <p-badge
        [value]="pontosVantagemDisponiveis()"
        [severity]="pontosVantagemDisponiveis() > 0 ? 'success' : 'secondary'" />
    </div>
  </div>

  <!-- Texto orientativo -->
  <p-message severity="info">
    Acesse a aba "Vantagens" para comprar ou evoluir vantagens.
    Os pontos ficam disponíveis até serem gastos.
  </p-message>

  <!-- Ações -->
  <div class="flex justify-content-end gap-2">
    <p-button
      label="Fechar e fazer depois"
      severity="secondary"
      [outlined]="true"
      (onClick)="fechar.emit()" />
    <p-button
      label="Ir para Vantagens"
      icon="pi pi-star-fill"
      (onClick)="irParaVantagens.emit()" />
  </div>
</div>
```

### 2. Integrar Step 3 no LevelUpDialogComponent

No `p-step-panel [value]="2"`:

```html
<app-level-up-vantagens-step
  [pontosVantagemGanhos]="pontosVantagemGanhos()"
  [pontosVantagemGastos]="pontosVantagemGastos()"
  [pontosVantagemDisponiveis]="pontosVantagemDisponiveis()"
  (irParaVantagens)="irParaVantagens()"
  (fechar)="fecharDialog()" />
```

Calcular `pontosVantagemGastos` no LevelUpDialogComponent se não vier do resumo:
- Se `FichaResumo` tiver os 3 campos (após T5): `pontosVantagemGanhos = resumo.pontosVantagemDisponiveis + custoPagoTotal`
- Simplificar se possível: apenas exibir `pontosVantagemDisponiveis` do resumo, sem detalhar ganhos vs. gastos.

### 3. Navegar para aba Vantagens ao clicar "Ir para Vantagens"

No `LevelUpDialogComponent`, método `irParaVantagens()`:

Emitir evento para o `FichaDetailPage` fechar o dialog e ativar a aba de Vantagens.

```typescript
protected irParaVantagens(): void {
  this.fechado.emit();
  this.navegarParaVantagens.emit();  // novo output
}
```

No `FichaDetailPage`, ao receber `navegarParaVantagens`, ativar o índice da aba de Vantagens no `p-tabs`.

### 4. Confirmação ao fechar com pontos pendentes (consolidar)

Esta lógica já deve estar parcialmente em T8. Confirmar que:

1. `tentarFechar()` verifica `pontosAtributoPendentes()` E `pontosAptidaoPendentes()` (não os de vantagem — esses não são urgentes pois ficam disponíveis permanentemente).
2. O `p-confirmDialog` tem mensagem clara sobre persistência.
3. O botão X do dialog dispara `tentarFechar()`, não fecha diretamente.

### 5. Botão "Próximo: Atributos" → "Próximo: Aptidões" → "Ver Vantagens"

Navegação linear do stepper:
- Step 0 → Step 1: `salvarAtributos()` (T8) → avança ao completar o request
- Step 1 → Step 2: `salvarAptidoes()` (T9) → avança ao completar
- Step 2: botões próprios do componente de vantagens ("Ir para Vantagens" / "Fechar")

## Critérios de Aceitação

- [ ] Step 3 exibe: pontos ganhos totais, pontos gastos, pontos disponíveis
- [ ] Badge de disponíveis com severity="success" quando > 0
- [ ] Botão "Ir para Vantagens" fecha o dialog e ativa a aba Vantagens no FichaDetail
- [ ] Botão "Fechar e fazer depois" fecha o dialog sem confirmação (Step 3 = nenhum ponto perdido)
- [ ] Confirmação ao fechar com pontos de atributo OU aptidão pendentes (qualquer um dos dois)
- [ ] Confirmação NÃO exibida quando só há pontos de vantagem pendentes (esses não têm urgência)
- [ ] Mensagem de confirmação menciona que os pontos são mantidos para distribuição posterior
- [ ] p-confirmDialog funcional com "Sim, fechar" / "Continuar distribuindo"
