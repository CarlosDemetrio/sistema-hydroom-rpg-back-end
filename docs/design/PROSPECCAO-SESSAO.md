# Design Spec: Prospecção em Sessão

> Documento de design UI/UX para o fluxo de uso de prospecção em sessão de jogo.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versão: 1.0 | Gerado em: 2026-04-02
> Decisões do PO: BA-GAPS-2026-04-02.md (GAP-08)

---

## 1. Visão Geral e Decisões do PO

**Regra de negócio definida pelo PO:**
- **Jogador usa** prospecção (clica em "Usar dado de prospecção")
- **Mestre confirma** o uso (marca que foi consumido)
- **Jogador NÃO pode reverter** sozinho — é uma ação que requer arbitragem do Mestre
- **Mestre PODE reverter** se quiser (ex: erro na rodada, situação especial)

### Semântica dos Endpoints (a definir com backend)

| Ação | Endpoint | Role |
|------|----------|------|
| Mestre conceder dado | `POST /fichas/{id}/prospeccao/conceder` | MESTRE |
| Jogador marcar uso | `POST /fichas/{id}/prospeccao/usar` | JOGADOR |
| Mestre confirmar/reverter | `PATCH /fichas/{id}/prospeccao/{usoId}` | MESTRE |

### Contexto do Domínio

- Prospecção é um recurso **extremamente raro** — o Mestre concede poucos dados por nível
- O jogador possui um **contador por tipo de dado** (d4, d6, d8, d10, d12)
- Ao usar, o dado some do inventário do jogador — é consumido, não reciclado
- O resultado do dado é rolado fisicamente na mesa (não é simulado pelo sistema)
- O sistema registra **que foi usado**, não o valor obtido

---

## 2. Onde a Prospecção Aparece

A prospecção aparece em **dois contextos**:

1. **Ficha do Jogador** (`FichaDetailPage`) — aba Resumo — bloco de Prospecção com os dados disponíveis e o histórico de uso pendente
2. **Painel de sessão do Mestre** — visão do Mestre de todas as fichas em jogo — coluna "Pendentes" para confirmar ou reverter usos

---

## 3. Wireframes

### 3.1 Bloco de Prospecção — Perspectiva do Jogador (aba Resumo)

```
┌──────────────────────────────────────────────────────┐
│  Prospecção                                          │
│                                                      │
│  [p-fieldset toggleable legend="Dados Disponíveis"]  │
│  ┌────────────────────────────────────────────────┐  │
│  │  d4    d6    d8    d10   d12                   │  │
│  │  [1]   [2]   [0]   [1]   [0]                  │  │
│  │  🎲    🎲                 🎲                   │  │
│  │ [Usar] [Usar]            [Usar]                │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  [p-tag warn] 1 uso aguardando confirmação do Mestre │
│  ┌────────────────────────────────────────────────┐  │
│  │  d6 • Usado há 5 min • [pendente de confirm.] │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### 3.2 Bloco de Prospecção — Mobile (aba Resumo)

```
┌─────────────────────────────┐
│ Prospecção                  │
├─────────────────────────────┤
│ d4  x1   [Usar]             │
│ d6  x2   [Usar]             │
│ d8  x0   (sem estoque)      │
│ d10 x1   [Usar]             │
│ d12 x0   (sem estoque)      │
├─────────────────────────────┤
│ [!] 1 uso aguardando conf.  │
│ d6 · há 5 min · [pendente]  │
└─────────────────────────────┘
```

### 3.3 Painel do Mestre — Usos Pendentes de Prospecção

```
┌──────────────────────────────────────────────────────────────────────────┐
│ [p-panel] Prospecção — Usos Pendentes de Confirmação                     │
├──────────────────────────────────────────────────────────────────────────┤
│  Personagem      Dado    Momento          Ação                           │
│  ─────────────────────────────────────────────────────────────────────   │
│  Aldric Névoa    d6      há 3 min    [Confirmar] [Reverter]              │
│  Lyra Sombria    d10     há 1 min    [Confirmar] [Reverter]              │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Fluxo Detalhado — Perspectiva do Jogador

### Passo 1: Jogador vê os dados disponíveis

```html
<!-- Componente: ProspeccaoJogadorComponent -->
<p-fieldset legend="Prospecção" [toggleable]="true">

  @if (dadosDisponiveis().length === 0) {
    <!-- Estado vazio -->
    <div class="flex flex-col items-center py-4 gap-2 text-center">
      <i class="pi pi-inbox text-color-secondary" style="font-size: 2rem"></i>
      <p class="text-color-secondary text-sm m-0">
        Nenhum dado de prospecção disponível.
        O Mestre concederá quando achar necessário.
      </p>
    </div>
  } @else {
    <!-- Grid de dados disponíveis -->
    <div class="flex flex-wrap gap-3 justify-center sm:justify-start">
      @for (dado of dadosDisponiveis(); track dado.dadoId) {
        @if (dado.quantidade > 0) {
          <div class="flex flex-col items-center gap-2 p-3 border border-surface-border rounded-lg
                       min-w-16 text-center"
               [attr.aria-label]="dado.nomeDado + ': ' + dado.quantidade + ' disponível(is)'">
            <!-- Ícone do dado (representação textual) -->
            <span class="text-2xl font-bold text-primary font-mono">{{ dado.nomeDado }}</span>
            <p-badge [value]="dado.quantidade"
                     [severity]="dado.quantidade === 1 ? 'warn' : 'info'"
                     pTooltip="{{ dado.quantidade }} dado(s) disponível(is)"
                     tooltipPosition="top" />
            <p-button label="Usar"
                      icon="pi pi-bolt"
                      size="small"
                      [loading]="usoEmAndamento() === dado.dadoId"
                      (onClick)="confirmarUso(dado)"
                      [attr.aria-label]="'Usar dado ' + dado.nomeDado + ' de prospecção'" />
          </div>
        }
      }
    </div>
  }

  <!-- Usos pendentes de confirmação -->
  @if (usosPendentes().length > 0) {
    <p-divider />
    <div class="flex flex-col gap-2">
      <div class="flex items-center gap-2">
        <p-tag value="Aguardando Mestre" severity="warn" icon="pi pi-clock" />
        <span class="text-sm text-color-secondary">
          {{ usosPendentes().length }} uso(s) aguardando confirmação
        </span>
      </div>
      @for (uso of usosPendentes(); track uso.id) {
        <div class="flex items-center justify-between p-2 border border-yellow-200
                     bg-yellow-50 rounded-lg"
             role="status"
             [attr.aria-label]="'Uso de ' + uso.dadoNome + ' pendente de confirmação'">
          <div class="flex items-center gap-2">
            <i class="pi pi-clock text-yellow-600"></i>
            <div class="flex flex-col">
              <span class="text-sm font-medium">{{ uso.dadoNome }}</span>
              <span class="text-xs text-color-secondary">
                Usado {{ uso.tempoAtras }} · Aguardando confirmação do Mestre
              </span>
            </div>
          </div>
          <p-tag value="Pendente" severity="warn" styleClass="text-xs" />
        </div>
      }
    </div>
  }

</p-fieldset>
```

### Passo 2: Confirmação de Uso (p-confirmDialog)

Ao clicar em "Usar", o jogador recebe um diálogo de confirmação — prospecção é irreversível pelo jogador.

```typescript
protected confirmarUso(dado: DadoProspeccao): void {
  this.confirmationService.confirm({
    header: `Usar dado ${dado.nomeDado} de Prospecção?`,
    message: `Você está prestes a usar 1 dado ${dado.nomeDado} de prospecção.
              Esta ação ficará pendente até o Mestre confirmar.
              <br><br>
              <strong>Você não poderá reverter sozinho.</strong>`,
    acceptLabel: 'Usar o dado',
    rejectLabel: 'Cancelar',
    acceptIcon: 'pi pi-bolt',
    acceptButtonStyleClass: 'p-button-warning',
    accept: () => this.executarUso(dado),
  });
}
```

```html
<!-- Diálogo customizado com impacto descrito -->
<p-confirmDialog styleClass="max-w-sm">
  <ng-template #message let-message>
    <div class="flex flex-col gap-3">
      <div class="flex items-center gap-3">
        <div class="flex items-center justify-center w-12 h-12 rounded-full bg-yellow-100">
          <i class="pi pi-bolt text-yellow-600 text-xl"></i>
        </div>
        <p class="m-0" [innerHTML]="message.message"></p>
      </div>
      <p-message severity="warn">
        <ng-template #messageicon><i class="pi pi-exclamation-triangle mr-2"></i></ng-template>
        O Mestre precisará confirmar o uso. Ele também pode reverter se necessário.
      </p-message>
    </div>
  </ng-template>
</p-confirmDialog>
```

### Passo 3: Feedback ao Jogador após Usar

```typescript
protected async executarUso(dado: DadoProspeccao): Promise<void> {
  this.usoEmAndamento.set(dado.dadoId);

  this.prospeccaoApiService.usarDado(this.fichaId(), dado.dadoId).subscribe({
    next: (resposta) => {
      // Atualiza quantidade disponível localmente (otimistic)
      this.dadosDisponiveis.update(dados =>
        dados.map(d => d.dadoId === dado.dadoId
          ? { ...d, quantidade: d.quantidade - 1 }
          : d
        )
      );
      // Adiciona uso à lista de pendentes
      this.usosPendentes.update(usos => [...usos, resposta]);

      this.toastService.success(
        `${dado.nomeDado} usado!`,
        'O Mestre confirmará o uso em breve. Role o dado fisicamente na mesa.'
      );
      this.usoEmAndamento.set(null);
    },
    error: () => {
      this.toastService.error(
        'Erro ao usar dado',
        'Não foi possível registrar o uso. Tente novamente.'
      );
      this.usoEmAndamento.set(null);
    }
  });
}
```

---

## 5. Fluxo Detalhado — Perspectiva do Mestre

### 5.1 Painel de Prospecção no Modo Sessão

O Mestre tem acesso a um painel de sessão (fora do escopo deste documento, mas referenciado) onde pode ver todos os usos pendentes de todas as fichas de uma vez.

Dentro da **ficha individual** do Mestre, a seção de prospecção exibe:

```html
<!-- Prospecção — visão do MESTRE na ficha -->
<p-fieldset legend="Prospecção" [toggleable]="true">

  <!-- Concessão de dados (apenas Mestre) -->
  <div class="flex flex-col gap-3 mb-4">
    <span class="font-semibold text-sm">Conceder dado de prospecção</span>
    <div class="flex items-center gap-2 flex-wrap">
      <p-select
        [options]="dadosProspeccaoConfig()"
        optionLabel="nome"
        optionValue="id"
        [(ngModel)]="dadoParaConceder"
        placeholder="Tipo de dado..."
        styleClass="min-w-32"
        aria-label="Tipo de dado a conceder" />
      <p-inputNumber
        [(ngModel)]="quantidadeParaConceder"
        [min]="1" [max]="10"
        [showButtons]="true"
        inputStyleClass="w-14 text-center"
        aria-label="Quantidade de dados a conceder" />
      <p-button label="Conceder"
                icon="pi pi-gift"
                [loading]="concedendo()"
                [disabled]="!dadoParaConceder || !quantidadeParaConceder"
                (onClick)="concederDado()"
                aria-label="Conceder dado de prospecção ao jogador" />
    </div>
  </div>

  <p-divider />

  <!-- Dados disponíveis do personagem (view only para Mestre) -->
  <div class="flex flex-wrap gap-3 mb-4">
    @for (dado of dadosDisponiveis(); track dado.dadoId) {
      <div class="flex flex-col items-center gap-1 p-2 border border-surface-border rounded-lg min-w-14 text-center">
        <span class="font-mono text-lg font-bold text-color-secondary">{{ dado.nomeDado }}</span>
        <p-badge [value]="dado.quantidade" severity="info" />
      </div>
    }
    @if (dadosDisponiveis().every(d => d.quantidade === 0)) {
      <p class="text-color-secondary text-sm m-0">Personagem sem dados disponíveis.</p>
    }
  </div>

  <!-- Usos pendentes de confirmação -->
  @if (usosPendentes().length > 0) {
    <p-divider />
    <div class="flex flex-col gap-2">
      <span class="font-semibold text-sm flex items-center gap-2">
        <p-badge [value]="usosPendentes().length" severity="warn" />
        Usos pendentes de confirmação
      </span>
      @for (uso of usosPendentes(); track uso.id) {
        <div class="flex items-center justify-between p-3 border border-yellow-300
                     bg-yellow-50 rounded-lg gap-2">
          <div class="flex flex-col gap-0.5">
            <span class="text-sm font-medium">{{ uso.dadoNome }}</span>
            <span class="text-xs text-color-secondary">{{ uso.tempoAtras }}</span>
          </div>
          <div class="flex gap-2">
            <p-button label="Confirmar"
                      icon="pi pi-check"
                      size="small"
                      severity="success"
                      [loading]="confirmandoUso() === uso.id"
                      (onClick)="confirmarUsoMestre(uso)"
                      [attr.aria-label]="'Confirmar uso de ' + uso.dadoNome" />
            <p-button label="Reverter"
                      icon="pi pi-undo"
                      size="small"
                      severity="secondary"
                      outlined
                      [loading]="revertendoUso() === uso.id"
                      (onClick)="reverterUso(uso)"
                      [attr.aria-label]="'Reverter uso de ' + uso.dadoNome" />
          </div>
        </div>
      }
    </div>
  }

</p-fieldset>
```

### 5.2 Confirmação de Reversão (Dialog)

A reversão pelo Mestre **não exige confirmDialog** — é uma ação administrativa e o Mestre é um power user. Mas deve haver feedback de sucesso.

```typescript
protected reverterUso(uso: ProspeccaoUso): void {
  this.revertendoUso.set(uso.id);

  this.prospeccaoApiService.reverterUso(this.fichaId(), uso.id).subscribe({
    next: () => {
      // Remove da lista de pendentes
      this.usosPendentes.update(usos => usos.filter(u => u.id !== uso.id));
      // Devolve o dado ao inventário (otimistic)
      this.dadosDisponiveis.update(dados =>
        dados.map(d => d.dadoId === uso.dadoId
          ? { ...d, quantidade: d.quantidade + 1 }
          : d
        )
      );
      this.toastService.success(
        'Uso revertido',
        `O dado ${uso.dadoNome} foi devolvido ao inventário de ${this.fichaNome()}.`
      );
      this.revertendoUso.set(null);
    },
    error: () => {
      this.toastService.error('Erro', 'Não foi possível reverter o uso. Tente novamente.');
      this.revertendoUso.set(null);
    }
  });
}
```

---

## 6. Indicador Visual de Uso Pendente (Em Toda a UI)

Quando há usos pendentes de confirmação, outros pontos da UI devem indicar isso:

### Na lista de fichas do Mestre

```html
<!-- Badge no card da ficha que tem prospecção pendente -->
<p-badge
  value="!"
  severity="warn"
  pTooltip="Prospecção pendente de confirmação"
  tooltipPosition="top"
  styleClass="absolute -top-1 -right-1" />
```

### Na aba de navegação da FichaDetailPage

Quando a ficha tem usos pendentes, a aba "Resumo" exibe um badge de alerta:

```html
<p-tab [value]="0">
  <ng-template #titleContent>
    <span class="flex items-center gap-2">
      Resumo
      @if (usosPendentes().length > 0 && isMestre()) {
        <p-badge [value]="usosPendentes().length" severity="warn" styleClass="text-xs" />
      }
    </span>
  </ng-template>
</p-tab>
```

---

## 7. Painel Global de Prospecção (Modo Sessão)

Para o Mestre gerenciar prospecção de **todas as fichas** em uma tela centralizada (sem precisar abrir cada ficha individualmente):

```
┌──────────────────────────────────────────────────────────────────────────┐
│ [p-panel] Prospecção — Usos Pendentes                                    │
├──────────────────────────────────────────────────────────────────────────┤
│  @if (totalPendentes() === 0) {                                          │
│    [pi pi-check-circle verde] Nenhum uso pendente                       │
│  }                                                                       │
│                                                                          │
│  Aldric, Filho da Névoa    d6    há 3 min    [Confirmar] [Reverter]     │
│  Lyra Sombria              d10   há 1 min    [Confirmar] [Reverter]     │
│                                                                          │
│  [Confirmar todos] (ação em lote, requer confirmação)                   │
└──────────────────────────────────────────────────────────────────────────┘
```

```html
<!-- ProspeccaoPainelMestreComponent — componente separado para o modo sessão -->
<p-panel header="Prospecção Pendente" [toggleable]="true">
  <ng-template #icons>
    @if (totalPendentes() > 0) {
      <p-badge [value]="totalPendentes()" severity="warn" styleClass="mr-2" />
    }
  </ng-template>

  @if (totalPendentes() === 0) {
    <div class="flex items-center gap-2 py-2 text-color-secondary">
      <i class="pi pi-check-circle text-green-500"></i>
      <span>Nenhum uso pendente de confirmação.</span>
    </div>
  } @else {
    <p-table [value]="usosPendentesGlobal()" styleClass="p-datatable-sm">
      <ng-template #header>
        <tr>
          <th>Personagem</th>
          <th>Dado</th>
          <th>Há quanto tempo</th>
          <th class="text-right">Ações</th>
        </tr>
      </ng-template>
      <ng-template #body let-uso>
        <tr>
          <td>{{ uso.personagemNome }}</td>
          <td><span class="font-mono font-bold">{{ uso.dadoNome }}</span></td>
          <td>{{ uso.tempoAtras }}</td>
          <td class="text-right">
            <div class="flex gap-1 justify-end">
              <p-button icon="pi pi-check" size="small" severity="success"
                        [loading]="confirmandoUso() === uso.id"
                        (onClick)="confirmarUso(uso)"
                        pTooltip="Confirmar uso"
                        [attr.aria-label]="'Confirmar uso de ' + uso.dadoNome + ' de ' + uso.personagemNome" />
              <p-button icon="pi pi-undo" size="small" outlined
                        [loading]="revertendoUso() === uso.id"
                        (onClick)="reverterUso(uso)"
                        pTooltip="Reverter uso"
                        [attr.aria-label]="'Reverter uso de ' + uso.dadoNome + ' de ' + uso.personagemNome" />
            </div>
          </td>
        </tr>
      </ng-template>
    </p-table>

    <!-- Ação em lote -->
    @if (usosPendentesGlobal().length > 1) {
      <div class="flex justify-end mt-3">
        <p-button label="Confirmar todos"
                  icon="pi pi-check-circle"
                  severity="success"
                  outlined
                  (onClick)="confirmarTodos()"
                  aria-label="Confirmar todos os usos de prospecção pendentes" />
      </div>
    }
  }
</p-panel>
```

---

## 8. Estados da UI

### Estado: Sem dados disponíveis (Jogador)

```html
<div class="flex flex-col items-center py-4 gap-2 text-center text-color-secondary">
  <i class="pi pi-inbox" style="font-size: 2rem"></i>
  <p class="text-sm m-0">
    Nenhum dado de prospecção disponível.
    O Mestre concederá quando achar necessário.
  </p>
</div>
```

### Estado: Uso concedido mas sem estoque de determinado dado

```html
<!-- No grid de dados, quando quantidade = 0 -->
<div class="flex flex-col items-center gap-2 p-3 border border-surface-border
             rounded-lg min-w-16 text-center opacity-40"
     aria-disabled="true">
  <span class="font-mono text-lg text-color-secondary">{{ dado.nomeDado }}</span>
  <p-badge value="0" severity="secondary" />
  <span class="text-xs text-color-secondary">Esgotado</span>
</div>
```

### Estado: Loading ao usar dado (Jogador)

O botão "Usar" exibe spinner enquanto o request é processado. O grid fica `pointer-events: none` para evitar duplo clique.

### Estado: Erro ao conceder (Mestre)

```typescript
this.toastService.error(
  'Erro ao conceder',
  'Não foi possível conceder o dado de prospecção. Tente novamente.'
);
```

---

## 9. Componentes PrimeNG Utilizados

| Componente | Módulo | Uso |
|-----------|--------|-----|
| `p-fieldset` | `FieldsetModule` | Container colapsável da seção de prospecção |
| `p-badge` | `BadgeModule` | Quantidade de dados, contador de pendentes |
| `p-button` | `ButtonModule` | Usar, Confirmar, Reverter, Conceder |
| `p-select` | `SelectModule` | Tipo de dado a conceder (Mestre) |
| `p-inputNumber` | `InputNumberModule` | Quantidade a conceder (Mestre) |
| `p-table` | `TableModule` | Painel global de usos pendentes |
| `p-tag` | `TagModule` | Status "Pendente" nos usos |
| `p-message` | `MessageModule` | Aviso no confirmDialog de uso |
| `p-toast` | `ToastModule` | Feedback de todas as ações |
| `p-confirmDialog` | `ConfirmDialogModule` | Confirmação de uso pelo Jogador |
| `p-panel` | `PanelModule` | Painel global de prospecção do Mestre |
| `p-divider` | `DividerModule` | Separador entre seções |

---

## 10. Acessibilidade

- Grid de dados: `role="list"` com cada dado como `role="listitem"` e `aria-label` descritivo com quantidade
- Botão "Usar": `aria-label="Usar dado d6 de prospecção"` — específico por tipo de dado
- Usos pendentes: `role="status"` em cada item para leitores de tela captarem o estado
- `p-confirmDialog`: foco preso no dialog, `aria-modal="true"`
- Botões de ação do Mestre (Confirmar/Reverter): `aria-label` específico com nome do personagem e dado
- Contador de pendentes: `aria-live="polite"` para notificar quando o número muda

---

## 11. Estrutura de Arquivos

```
ficha/
  components/
    prospeccao-jogador/
      prospeccao-jogador.component.ts    [DUMB] — bloco de prospecção na aba do Jogador
    prospeccao-mestre/
      prospeccao-mestre.component.ts     [DUMB] — bloco de prospecção na ficha (Mestre)
  painel-sessao/
    components/
      prospeccao-painel-mestre/
        prospeccao-painel-mestre.component.ts  [SMART] — painel global de pendentes
```

---

## 12. Checklist de Implementação

- [ ] `ProspeccaoJogadorComponent` com grid de dados disponíveis por tipo (d4–d12)
- [ ] Botão "Usar" com `p-confirmDialog` descrevendo irreversibilidade
- [ ] Estado de uso pendente com tag amarela por uso
- [ ] Otimistic update: quantidade diminui imediatamente ao usar, volta se erro
- [ ] `ProspeccaoMestreComponent` com seção de concessão (select + inputNumber + botão)
- [ ] Seção de usos pendentes na visão do Mestre com botões Confirmar/Reverter
- [ ] Feedback de reversão: dado devolvido ao inventário + toast informativo
- [ ] Badge de alerta na aba "Resumo" quando há pendentes (só para Mestre)
- [ ] Badge no card da ficha na listagem do Mestre quando há pendentes
- [ ] `ProspeccaoPainelMestreComponent` com tabela global de todos os usos pendentes
- [ ] Botão "Confirmar todos" (com confirmDialog) quando há múltiplos pendentes
- [ ] Estado vazio "Nenhum dado disponível" com texto explicativo
- [ ] Loading state por botão individual (não bloqueia outros botões)
- [ ] Verificar endpoints: `POST /fichas/{id}/prospeccao/conceder` e `POST /fichas/{id}/prospeccao/usar`
- [ ] `aria-label` específicos em todos os botões de ação
