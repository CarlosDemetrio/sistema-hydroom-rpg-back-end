# T11 — Conectar Saldo de Vantagens em FichaVantagensTab

> Spec: 012 | Fase: 3 | Tipo: Frontend | Prioridade: ALTO
> Depende de: T6 (FichaResumo com pontosVantagemDisponiveis)
> Bloqueia: nada

---

## Objetivo

Conectar o campo `pontosVantagemDisponiveis` do `FichaResumo` ao `FichaVantagensTabComponent`, substituindo o mock atual `[pontosVantagemRestantes]="0"` pelo valor real, e bloquear a compra de vantagens quando o saldo for insuficiente.

## Contexto

No `ficha-detail.component.ts` há a linha:
```typescript
[pontosVantagemRestantes]="0"
```

Isso significa que o usuário nunca vê o saldo real de pontos e o botão "Comprar" está sempre habilitado (ou sempre desabilitado dependendo da lógica interna). Esta task conecta o dado real.

## Arquivos Afetados

- `src/app/features/jogador/pages/ficha-detail/ficha-detail.component.ts`
- `src/app/features/jogador/pages/ficha-detail/components/ficha-vantagens-tab/ficha-vantagens-tab.component.ts`

## Passos

### 1. Atualizar FichaDetailPage

Passar o valor real do resumo:

```html
<!-- Antes: -->
[pontosVantagemRestantes]="0"

<!-- Depois: -->
[pontosVantagemRestantes]="resumo()?.pontosVantagemDisponiveis ?? 0"
```

Também verificar se o componente precisa de `pontosVantagemGanhos` e `pontosVantagemGastos` separados. Se sim, calcular via resumo ou adicionar ao resumo.

### 2. Verificar FichaVantagensTabComponent

Abrir o arquivo e verificar:
- Como o `pontosVantagemRestantes` é usado
- Se o botão "Comprar" verifica `custoPago > pontosVantagemRestantes`
- Se o display do saldo está implementado ou precisa ser adicionado

**Display esperado no topo da aba:**
```html
<div class="flex align-items-center gap-2 mb-3">
  <span>Pontos disponíveis:</span>
  <p-badge
    [value]="pontosVantagemRestantes()"
    [severity]="pontosVantagemRestantes() > 0 ? 'success' : 'secondary'" />
</div>
```

**Botão Comprar bloqueado quando custo > saldo:**
```html
<p-button
  label="Comprar"
  [disabled]="vantagem.custo > pontosVantagemRestantes()"
  [pTooltip]="vantagem.custo > pontosVantagemRestantes() ? 'Pontos insuficientes' : ''"
  (onClick)="comprarVantagem(vantagem)" />
```

### 3. Recarregar resumo após compra de vantagem

Após `POST /fichas/{id}/vantagens` com sucesso, recarregar o `FichaResumo` para atualizar o saldo:

```typescript
protected comprarVantagem(vantagem: VantagemConfig): void {
  this.fichasApi.comprarVantagem(this.fichaId(), { vantagemConfigId: vantagem.id })
    .subscribe({
      next: () => {
        this.recarregarResumo.emit();  // sinal para o FichaDetailPage recarregar resumo
        // ou chamar diretamente se o resumo for gerenciado localmente
      },
      error: () => { /* toast de erro */ }
    });
}
```

### 4. Consistência de gastos irreversíveis

Verificar que não existe botão de "Desfazer compra" ou "Remover vantagem" na aba de vantagens (regra: FichaVantagem nunca pode ser removida — o nível só sobe). Se existir algum botão indevidamente, remover.

## Critérios de Aceitação

- [ ] `pontosVantagemRestantes` conectado ao valor real do `FichaResumo` (não mais 0 hardcoded)
- [ ] Display "Pontos disponíveis: N" visível no topo da aba Vantagens
- [ ] Badge com severity="success" quando > 0, severity="secondary" quando = 0
- [ ] Botão "Comprar" desabilitado com tooltip "Pontos insuficientes" quando custo > saldo
- [ ] Após compra bem-sucedida: resumo recarregado e saldo atualizado
- [ ] Nenhum botão de remoção de vantagem exposto (FichaVantagem é irreversível)
