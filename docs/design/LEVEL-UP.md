# Design Spec: Level Up e Distribuicao de Pontos

> Documento de design UI/UX para o fluxo de progressao de nivel do personagem.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versao: 1.0 | Gerado em: 2026-04-02
> Regras de negocio: BA-NIVEIS-PROGRESSAO.md, BA-FICHA.md

---

## 1. Visao Geral

O Level Up e um evento disparado automaticamente quando o Mestre concede XP suficiente para o personagem atingir um novo nivel. O **Mestre** concede XP; o **Jogador** (ou o Mestre, se preferir) distribui os pontos ganhos.

**Regras criticas:**
- `nivel` e calculado no backend: `MAX(NivelConfig.nivel WHERE xpNecessaria <= fichaXp)`
- O frontend detecta level up comparando o nivel antes e depois da concessao de XP
- O Jogador so distribui pontos de atributo — pontos de aptidao sao controlados pelo Mestre
- Pontos de vantagem sao gastos na aba de Vantagens do FichaDetail, nao no wizard de level up
- O `limitadorAtributo` do novo nivel impoe teto maximo em qualquer atributo Total
- Nao e possivel distribuir mais pontos do que os disponiveis

**Quem distribui o que:**

| Acao | MESTRE | JOGADOR (dono) |
|------|--------|----------------|
| Ver pontos disponiveis | Sim | Sim |
| Distribuir pontos de atributo | Sim | Sim |
| Distribuir pontos de aptidao | Sim | Sim (requer confirmacao do Mestre?) |
| Gastar pontos de vantagem | Sim | Sim |
| Conceder XP (causa o level up) | Sim | Nao |

---

## 2. Onde Fica a UI de Level Up

### Decisao: Modal (p-dialog) disparado no FichaDetailPage

A distribuicao de pontos apos level up **nao e uma aba permanente**. E um evento que merece destaque visual proprio. Um `p-dialog` com steps internas (usando `p-stepper`) garante:

1. Foco total na tarefa de distribuicao (sem distracao das outras abas)
2. Guia o usuario pelos diferentes tipos de pontos em ordem logica
3. Pode ser descartado e retomado (pontos pendentes persistem no backend)
4. Funciona bem em mobile como drawer de tela cheia

**Alternativas consideradas e descartadas:**
- Aba dedicada "Level Up": faz a aba aparecer permanentemente, mesmo sem pontos pendentes
- Banner inline na aba Resumo: nao tem espaco suficiente para o formulario de distribuicao
- Redirect para nova rota: perde o contexto da ficha, backstack complicado

---

## 3. Notificacao de Level Up

### 3.1 Toast de Level Up (notificacao inicial)

Quando o Mestre concede XP e o nivel sobe, exibir um toast especial:

```typescript
// Detectar level up comparando nivel antes e depois:
const nivelAnterior = ficha().nivel;
// Apos receber a ficha atualizada do backend:
if (fichaAtualizada.nivel > nivelAnterior) {
  this.toastService.add({
    severity: 'success',
    summary: `NIVEL ${fichaAtualizada.nivel}!`,
    detail: `${ficha().nome} subiu para o Nivel ${fichaAtualizada.nivel}! Distribua os pontos ganhos.`,
    life: 8000,    // duracao maior — e um evento especial
    sticky: false,
    styleClass: 'level-up-toast',  // classe CSS para estilizacao especial
  });
}
```

```css
/* Estilo especial para toast de level up */
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

### 3.2 Badge de Pontos Pendentes (persistente)

Enquanto houver pontos por distribuir, exibir badge no FichaHeaderComponent:

```
┌────────────────────────────────────────────────────────────────────┐
│  Aldric, Filho da Névoa   [Nv. 5]   [!] 3 pontos pendentes ▾      │
│  Humano • Guerreiro                                                │
└────────────────────────────────────────────────────────────────────┘
```

```html
@if (pontosAtributoDisponiveis() > 0 || pontosAptidaoDisponiveis() > 0) {
  <p-button
    [label]="labelPontosPendentes()"
    icon="pi pi-arrow-circle-up"
    severity="warn"
    size="small"
    (onClick)="abrirDialogLevelUp()"
    aria-label="Distribuir pontos de nivel — {{ labelPontosPendentes() }}"
    pTooltip="Voce tem pontos para distribuir!"
    tooltipPosition="bottom" />
}
```

```typescript
protected labelPontosPendentes = computed(() => {
  const partes = [];
  if (this.pontosAtributoDisponiveis() > 0)
    partes.push(`${this.pontosAtributoDisponiveis()} atrib.`);
  if (this.pontosAptidaoDisponiveis() > 0)
    partes.push(`${this.pontosAptidaoDisponiveis()} apt.`);
  return partes.join(' + ') + ' para distribuir';
});
```

---

## 4. Wireframes do Dialog de Level Up

### 4.1 Desktop — p-dialog com p-stepper

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Aldric subiu para o Nível 5!                                   [X]     │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                         │
│  [1. Atributos] ──────── [2. Aptidoes] ──────── [3. Vantagens]          │
│  (step ativo)              (desabilitado           (desabilitado         │
│                             ate distribuir)         ate distribuir)      │
│                                                                         │
│  ═══════════════════════════════════════════════════════════════════    │
│                                                                         │
│  Pontos de Atributo disponíveis: [3]  (contador regressivo)             │
│  Limitador deste nível: [50]  (nenhum total de atributo pode passar 50) │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Atributo       Base  Nível  Total  [+] [-]   Barra de progresso  │   │
│  │ Força (FOR)     25    10     35     [+][-]    [████████████░░░░]  │   │
│  │ Agilidade (AGI) 20     8     28     [+][-]    [█████████░░░░░░░]  │   │
│  │ Vigor (VIG)     22     9     31     [+][-]    [██████████░░░░░░]  │   │
│  │ ...                                                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  [Cancelar distribuicao]              [Proximo: Aptidoes →]             │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Mobile — p-dialog fullscreen

Em mobile, o dialog ocupa 100% da viewport (sem `max-width`):

```
┌─────────────────────────────┐
│ Nivel 5! Distribuir pontos  │  ← header do dialog
│                        [X]  │
├─────────────────────────────┤
│ [Atrib.] [Apt.] [Vantagens] │  ← stepper horizontal compacto
├─────────────────────────────┤
│ Pontos disponiveis: [3]     │  ← badge grande e centralizado
│ Limitador: 50               │
├─────────────────────────────┤
│ Forca (FOR)                 │
│  Base: 25  Nivel: 10        │
│  Total: 35                  │
│  [████████████░░] 35/50     │
│  [-]  10  [+]               │  ← controles grandes (48px touch target)
│                             │
│ Agilidade (AGI)             │
│  Base: 20  Nivel: 8         │
│  Total: 28                  │
│  [████████░░░░░░] 28/50     │
│  [-]   8  [+]               │
├─────────────────────────────┤
│ [Proximo: Aptidoes →]       │  ← botao full-width sticky no rodape
└─────────────────────────────┘
```

---

## 5. Step 1 — Distribuicao de Pontos de Atributo

### Componente: LevelUpAtributosStepComponent

**Arquivo**: `ficha/components/level-up-dialog/steps/level-up-atributos-step.component.ts`
**Tipo**: Dumb

```typescript
atributos = input.required<FichaAtributoResponse[]>();
pontosDisponiveis = input.required<number>();
limitadorAtributo = input.required<number>();

distribuicaoChanged = output<Record<string, number>>(); // sigla -> pontos adicionados no nivel
```

### Regras Visuais do Step

1. Contador de pontos restantes: destaque com `p-badge` severity="warn" quando > 0, severity="success" quando = 0
2. Botao `[+]` desabilitado quando: `pontosRestantes === 0` OU `atributo.total + 1 > limitadorAtributo`
3. Botao `[-]` desabilitado quando: `pontosAdicionados[sigla] === 0` (nao pode retirar mais do que adicionou nesta sessao)
4. Barra de progresso do atributo: mostra `total / limitadorAtributo` com cor warn quando > 80% do limitador
5. Quando total atinge o limitador: barra fica vermelha e exibe tooltip "Limite do nivel atingido"

```html
<!-- Contador de pontos restantes — prominente e reativo -->
<div class="flex items-center justify-center gap-3 py-4">
  <div class="flex flex-col items-center">
    <span class="text-4xl font-bold"
          [class.text-warn-color]="pontosRestantes() > 0"
          [class.text-success-color]="pontosRestantes() === 0"
          aria-live="polite"
          [attr.aria-label]="pontosRestantes() + ' pontos de atributo restantes'">
      {{ pontosRestantes() }}
    </span>
    <span class="text-sm text-color-secondary">pontos de atributo</span>
  </div>
  <p-divider layout="vertical" />
  <div class="flex flex-col items-center">
    <span class="text-2xl font-semibold text-color-secondary">{{ limitadorAtributo() }}</span>
    <span class="text-xs text-color-secondary">teto do nivel</span>
  </div>
</div>

<!-- Grid de atributos -->
@for (atributo of atributosComDistribuicao(); track atributo.sigla) {
  <div class="flex flex-col gap-2 p-3 border border-surface-border rounded-lg mb-3"
       [class.border-yellow-400]="atributo.total >= limitadorAtributo() * 0.9"
       [attr.aria-label]="'Atributo ' + atributo.nome + ': ' + atributo.total + ' de ' + limitadorAtributo()">

    <!-- Nome e total atual -->
    <div class="flex items-center justify-between">
      <div class="flex flex-col gap-0.5">
        <span class="font-semibold text-sm">{{ atributo.nome }}</span>
        <span class="text-xs text-color-secondary font-mono">{{ atributo.sigla }}</span>
      </div>
      <span class="text-xl font-bold"
            [class.text-red-600]="atributo.total >= limitadorAtributo()"
            [class.text-warn-color]="atributo.total >= limitadorAtributo() * 0.9 && atributo.total < limitadorAtributo()">
        {{ atributo.total }}
      </span>
    </div>

    <!-- Barra de progresso ate o limitador -->
    <p-progressBar
      [value]="(atributo.total / limitadorAtributo()) * 100"
      [showValue]="false"
      styleClass="h-2"
      [class.barra-warn]="atributo.total >= limitadorAtributo() * 0.9"
      [class.barra-danger]="atributo.total >= limitadorAtributo()"
      pTooltip="{{ atributo.total }}/{{ limitadorAtributo() }} — Teto do nivel"
      tooltipPosition="top" />

    <!-- Detalhe dos campos + controles -->
    <div class="flex items-center justify-between gap-2 mt-1">
      <div class="flex gap-3 text-xs text-color-secondary">
        <span>Base: <strong>{{ atributo.base }}</strong></span>
        <span>Nivel: <strong>{{ atributo.nivelAtributo }}</strong></span>
        @if (atributo.bonusRaca > 0) {
          <span>Raca: <strong>+{{ atributo.bonusRaca }}</strong></span>
        }
      </div>

      <!-- Controles +/- -->
      <div class="flex items-center gap-2" role="group" [attr.aria-label]="'Controles de ' + atributo.nome">
        <p-button
          icon="pi pi-minus"
          text rounded
          size="small"
          severity="secondary"
          [disabled]="pontosAdicionados()[atributo.sigla] === 0"
          (onClick)="removerPonto(atributo.sigla)"
          [attr.aria-label]="'Remover ponto de ' + atributo.nome" />
        <span class="font-bold min-w-8 text-center text-lg"
              aria-live="polite">
          {{ atributo.nivelAtributo + (pontosAdicionados()[atributo.sigla] ?? 0) }}
        </span>
        <p-button
          icon="pi pi-plus"
          text rounded
          size="small"
          severity="success"
          [disabled]="pontosRestantes() === 0 || atributo.total >= limitadorAtributo()"
          (onClick)="adicionarPonto(atributo.sigla)"
          [attr.aria-label]="'Adicionar ponto em ' + atributo.nome"
          [pTooltip]="atributo.total >= limitadorAtributo() ? 'Limite do nivel atingido' : ''"
          tooltipPosition="top" />
      </div>
    </div>
  </div>
}
```

---

## 6. Step 2 — Distribuicao de Pontos de Aptidao

### Layout

Similar ao Step 1, mas com a estrutura especifica de aptidoes: `base`, `sorte`, `classe`, `total`.

- Apenas o campo `base` e editavel pelo Jogador
- Os campos `sorte` e `classe` sao controlados pelo Mestre (readonly neste wizard)
- Aptidoes agrupadas por `tipoAptidao`

```
┌─────────────────────────────────────────────────────────────────┐
│  Pontos de Aptidao disponíveis: [5]  ← contador regressivo      │
│                                                                 │
│  [p-accordion por TipoAptidao]                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ ▾ Combate (3 aptidoes)                                   │   │
│  │   Espadas        base: 3  sorte: 0  classe: 0  total: 3  │   │
│  │                  [-]  3  [+]                             │   │
│  │   Escudos        base: 2  sorte: 1  classe: 0  total: 3  │   │
│  │                  [-]  2  [+]                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ ▾ Sobrevivencia (2 aptidoes)                             │   │
│  │   Rastreamento   base: 0  sorte: 0  classe: 2  total: 2  │   │
│  │                  [-]  0  [+]  (classe somado auto.)      │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**Comportamento especial:**
- `sorte` e `classe` exibidos como readonly com cor secundaria
- tooltip nos campos readonly: "Controlado pelo Mestre"
- Nao ha limitador de atributo para aptidoes (sem barra de progresso)

---

## 7. Step 3 — Pontos de Vantagem

O terceiro step e informativo + atalho, nao distributivo:

```
┌─────────────────────────────────────────────────────────────────┐
│  Pontos de Vantagem                                             │
│                                                                 │
│  Total acumulado: 18 pontos                                     │
│  Gastos:          15 pontos                                     │
│  Disponíveis:     3 pontos  ← destaque com badge verde          │
│                                                                 │
│  Acesse a aba "Vantagens" para comprar ou evoluir vantagens.    │
│  Os pontos ficam disponíveis ate serem gastos.                  │
│                                                                 │
│  [Ir para Vantagens]  [Fechar e fazer depois]                   │
└─────────────────────────────────────────────────────────────────┘
```

Os pontos de vantagem NAO sao distribuidos neste wizard — o jogador navega para a aba Vantagens do FichaDetail para isso. Razao: a compra de vantagens e mais complexa (depende de pre-requisitos, categorias, nivelMaximo) e merece uma UI propria.

---

## 8. Navegacao entre Steps

### Validacao por step antes de avancar

- Step 1 (Atributos): botao "Proximo" sempre disponivel — o jogador pode avancar sem distribuir todos os pontos (pontos ficam pendentes)
- Step 2 (Aptidoes): idem
- Step 3 (Vantagens): botao "Concluir" fecha o dialog

### Confirmacao ao fechar com pontos pendentes

Se o usuario tentar fechar o dialog (clique no X ou fora do dialog) com pontos nao distribuidos:

```typescript
protected tentarFecharDialog(): void {
  const temPontos = this.pontosAtributoPendentes() > 0 || this.pontosAptidaoPendentes() > 0;

  if (temPontos) {
    this.confirmationService.confirm({
      header: 'Pontos nao distribuidos',
      message: `Voce ainda tem ${this.descricaoPontosPendentes()} para distribuir.
                Os pontos serao mantidos para distribuir mais tarde.
                Deseja fechar mesmo assim?`,
      acceptLabel: 'Sim, fechar',
      rejectLabel: 'Continuar distribuindo',
      acceptButtonStyleClass: 'p-button-secondary',
    });
  } else {
    this.fecharDialog();
  }
}
```

---

## 9. Persistencia dos Pontos Pendentes

O frontend deve consultar os pontos pendentes ao carregar o FichaDetail. O backend deve expor:

```
GET /api/v1/fichas/{id}/resumo
  → pontosAtributoDisponiveis: number    (pontos ganhos - pontos ja distribuidos)
  → pontosAptidaoDisponiveis: number
  → pontosVantagemDisponiveis: number
```

> Nota: GAP-06 ja documenta que `pontosDisponiveis` esta ausente do `FichaResumoResponse`. Este design depende da inclusao desses campos no backend.

Ao carregar o FichaDetail, se `pontosAtributoDisponiveis > 0 || pontosAptidaoDisponiveis > 0`, exibir automaticamente o badge de pontos pendentes no FichaHeaderComponent.

---

## 10. Animacao de Level Up

Para tornar o evento de level up memoravel (RPG e sobre momentos epicos):

```css
/* Animacao de "flash" no FichaHeaderComponent ao receber level up */
@keyframes levelUpFlash {
  0%   { box-shadow: 0 0 0 0 var(--yellow-400); }
  50%  { box-shadow: 0 0 0 16px rgba(234, 179, 8, 0.3); }
  100% { box-shadow: 0 0 0 0 rgba(234, 179, 8, 0); }
}

.ficha-header-level-up {
  animation: levelUpFlash 1.5s ease-out;
}
```

A classe `ficha-header-level-up` e adicionada ao FichaHeaderComponent por 1.5s quando level up e detectado, depois removida.

---

## 11. Estados da UI

### Loading (salvando distribuicao)

- Botao "Proximo" / "Concluir" exibe spinner e fica desabilitado
- Controles +/- ficam desabilitados durante o request

### Erro ao salvar distribuicao

```typescript
this.toastService.add({
  severity: 'error',
  summary: 'Erro ao salvar pontos',
  detail: 'Nao foi possivel salvar a distribuicao. Tente novamente.',
  life: 6000,
});
// Os pontos locais NAO sao revertidos — o usuario pode tentar novamente
```

### Estado vazio (sem pontos para distribuir)

O dialog de level up so e aberto quando ha pontos. Se nao ha, o badge de pontos pendentes nao aparece. Nao ha estado vazio para este dialog.

### Limitador atingido (todos os atributos no limite)

```html
@if (todosAtributosNoLimite() && pontosRestantes() > 0) {
  <p-message severity="warn" class="mb-3">
    <ng-template #messageicon><i class="pi pi-exclamation-triangle mr-2"></i></ng-template>
    Todos os atributos atingiram o teto do nivel ({{ limitadorAtributo() }}).
    Os {{ pontosRestantes() }} pontos restantes nao podem ser distribuidos neste nivel.
    Eles serao perdidos ao confirmar.
  </p-message>
}
```

---

## 12. Componentes PrimeNG Utilizados

| Componente | Modulo | Uso |
|-----------|--------|-----|
| `p-dialog` | `DialogModule` | Container principal do wizard de level up |
| `p-stepper` | `StepperModule` | Navegacao entre os 3 passos |
| `p-progressBar` | `ProgressBarModule` | Barra de progresso atributo/limitador |
| `p-button` | `ButtonModule` | Controles +/-, navegacao entre steps |
| `p-badge` | `BadgeModule` | Contador de pontos restantes, badge no header |
| `p-accordion` | `AccordionModule` | Agrupamento de aptidoes por tipo |
| `p-toast` | `ToastModule` | Notificacao de level up, feedback de erros |
| `p-confirmDialog` | `ConfirmDialogModule` | Confirmacao ao fechar com pontos pendentes |
| `p-message` | `MessageModule` | Avisos de limite atingido, pontos nao distribuiveis |
| `p-divider` | `DividerModule` | Separador no resumo de pontos |

---

## 13. Acessibilidade

- Contador de pontos restantes: `aria-live="polite"` para notificar mudancas via leitor de tela
- Controles +/-: `aria-label` descritivo ("Adicionar ponto em Forca", "Remover ponto de Forca")
- Stepper: cada step tem `aria-label` descritivo e indicacao de estado (atual/completo/bloqueado)
- Valor atual do atributo no controle: `aria-live="polite"` para anunciar mudancas
- Barra de progresso: `aria-valuemin`, `aria-valuemax`, `aria-valuenow` + `aria-label` descritivo
- Dialog: `aria-modal="true"`, foco inicial no primeiro controle interativo do step ativo
- Toast de level up: `aria-live="assertive"` (e um evento critico que merece interrupcao)

---

## 14. Estrutura de Arquivos

```
ficha/
  components/
    level-up-dialog/
      level-up-dialog.component.ts         [SMART] — orquestra os requests, gerencia estado
      level-up-dialog.component.html
      steps/
        level-up-atributos-step/
          level-up-atributos-step.component.ts   [DUMB]
          level-up-atributos-step.component.html
        level-up-aptidoes-step/
          level-up-aptidoes-step.component.ts    [DUMB]
          level-up-aptidoes-step.component.html
        level-up-vantagens-step/
          level-up-vantagens-step.component.ts   [DUMB]
          level-up-vantagens-step.component.html
```

---

## 15. Checklist de Implementacao

- [ ] Deteccao de level up no FichaDetailPage ao receber ficha atualizada apos concessao de XP
- [ ] Toast especial de level up com estilo diferenciado (borda amarela)
- [ ] Animacao CSS de "flash" no FichaHeaderComponent ao subir de nivel
- [ ] Badge de pontos pendentes no FichaHeaderComponent (visivel para Mestre e Jogador dono)
- [ ] `LevelUpDialogComponent` com `p-dialog` + `p-stepper` de 3 steps
- [ ] Step 1: grid de atributos com controles +/-, barra de progresso por limitador, contador regressivo
- [ ] Botao `[+]` desabilitado quando pontos = 0 ou total >= limitadorAtributo
- [ ] Step 2: aptidoes agrupadas por tipo com `p-accordion`, campo `base` editavel
- [ ] Step 3: resumo de pontos de vantagem + botao "Ir para Vantagens"
- [ ] Confirmacao ao fechar dialog com pontos nao distribuidos
- [ ] Loading state nos botoes durante request de salvamento
- [ ] Toast de erro ao falhar no salvamento (sem reverter estado local)
- [ ] Aviso quando todos os atributos atingiram o limitador e restam pontos
- [ ] Backend: confirmar que `pontosAtributoDisponiveis` e `pontosAptidaoDisponiveis` estao no FichaResumoResponse
- [ ] `aria-live` no contador de pontos e nos valores de atributo
- [ ] Versao mobile: dialog fullscreen, controles +/- com touch target minimo de 48px
