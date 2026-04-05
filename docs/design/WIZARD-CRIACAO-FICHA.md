# Design Spec: Wizard de Criação de Ficha

> Documento de design UI/UX para o fluxo de criação de personagem em múltiplos passos.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versão: 1.0 | Gerado em: 2026-04-02
> Decisões do PO: BA-GAPS-2026-04-02.md (GAP-01)

---

## 1. Visão Geral e Decisões do PO

O PO decidiu pela **Opção B**: wizard de 5-6 passos com experiência completa de criação. As regras fundamentais são:

- **Auto-save obrigatório**: a cada mudança de passo, o frontend salva o rascunho no backend (ficha com campos incompletos é aceita e persistida)
- **Todos os campos obrigatórios** para finalizar — a ficha só pode ser "ativada" com todos os passos completos
- **Atributos na criação**: a distribuição de pontos de base ocorre no passo 4 do wizard
- **Insólitus, Título Heróico, Arquétipo**: não entram no wizard — são concedidos pelo Mestre depois, na edição da ficha
- **XP é campo read-only para o Jogador**: o Mestre concede via endpoint separado

### Quem acessa este wizard

- **Jogador**: cria sua própria ficha em um jogo onde está APROVADO
- **Mestre**: pode criar ficha de jogador ou de NPC em qualquer jogo seu

---

## 2. Estrutura dos Passos

| Passo | Título | Obrigatório | Campos |
|-------|--------|-------------|--------|
| 1 | Identidade Básica | Sim | Nome, Gênero, Origem (texto livre) |
| 2 | Raça e Classe | Sim | Raça, Classe |
| 3 | Personalidade | Sim | Índole, Presença |
| 4 | Atributos | Sim | Distribuição de pontos de base (nivel 1) |
| 5 | Revisão e Confirmação | — | Resumo de todos os dados antes de confirmar |

> Para Mestre criando NPC: um passo adicional "Configurações de NPC" é inserido após o passo 1 com os campos `isNpc = true` (automático) e `descricao`.

---

## 3. Wireframes

### 3.1 Desktop — Layout Geral

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] Voltar   Criar Personagem   Nome do Jogo   [Avatar]     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ [p-stepper — linear, horizontal]                                 │   │
│  │  [1 Identidade] — [2 Raça/Classe] — [3 Personalidade] —         │   │
│  │  [4 Atributos] — [5 Revisão]                                     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ [p-card — conteúdo do passo ativo]                               │   │
│  │                                                                  │   │
│  │  H2: Identidade Básica                                           │   │
│  │  Defina quem é seu personagem no mundo.                          │   │
│  │                                                                  │   │
│  │  [campos do passo]                                               │   │
│  │                                                                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌── Rodapé do Wizard ────────────────────────────────────────────┐     │
│  │  [pi pi-check-circle] Salvo automaticamente   [Voltar] [Próximo]│     │
│  └─────────────────────────────────────────────────────────────────┘     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Mobile (< 640px) — Layout Geral

```
┌─────────────────────────────┐
│ [<] Criar Personagem        │  ← p-toolbar compacto
├─────────────────────────────┤
│ Passo 1 de 5: Identidade    │  ← indicador textual + p-progressBar
│ [████████░░░░░░░░░░░░] 20%  │
├─────────────────────────────┤
│                             │
│  H2: Identidade Básica      │
│  [campos do passo]          │
│                             │
├─────────────────────────────┤
│ [Salvo] [Voltar]  [Próximo] │  ← rodapé fixo no mobile
└─────────────────────────────┘
```

No mobile o `p-stepper` horizontal é substituído por um indicador textual ("Passo X de 5") + `p-progressBar` com `value` calculado em porcentagem.

---

## 4. Componentes PrimeNG — Estrutura do Wizard

### p-stepper (Desktop/Tablet)

```html
<p-stepper [value]="passoAtual()" [linear]="true">
  <p-step-list>
    <p-step [value]="1">
      <ng-template #content let-activateCallback="activateCallback">
        <span class="inline-flex items-center gap-2">
          <i class="pi pi-user"></i>
          <span class="hidden sm:inline">Identidade</span>
        </span>
      </ng-template>
    </p-step>
    <p-step [value]="2">
      <ng-template #content>
        <span class="inline-flex items-center gap-2">
          <i class="pi pi-shield"></i>
          <span class="hidden sm:inline">Raça e Classe</span>
        </span>
      </ng-template>
    </p-step>
    <p-step [value]="3">
      <ng-template #content>
        <span class="inline-flex items-center gap-2">
          <i class="pi pi-heart"></i>
          <span class="hidden sm:inline">Personalidade</span>
        </span>
      </ng-template>
    </p-step>
    <p-step [value]="4">
      <ng-template #content>
        <span class="inline-flex items-center gap-2">
          <i class="pi pi-chart-bar"></i>
          <span class="hidden sm:inline">Atributos</span>
        </span>
      </ng-template>
    </p-step>
    <p-step [value]="5">
      <ng-template #content>
        <span class="inline-flex items-center gap-2">
          <i class="pi pi-check-circle"></i>
          <span class="hidden sm:inline">Revisão</span>
        </span>
      </ng-template>
    </p-step>
  </p-step-list>

  <p-step-panels>
    <!-- Painel de cada passo — detalhado nas seções abaixo -->
  </p-step-panels>
</p-stepper>
```

**Módulo**: `StepperModule` de `primeng/stepper`

**Propriedades críticas**:
- `[linear]="true"` — impede pular passos sem completar o anterior
- `[value]` — passo atual controlado por signal do componente
- Cada `p-step` tem `[disabled]` computado se o passo anterior está incompleto

### Indicador de Auto-save (Rodapé)

```html
<!-- Rodapé do wizard -->
<div class="flex items-center justify-between mt-4 pt-4 border-t border-surface-border">
  <!-- Estado do auto-save (esquerda) -->
  <div class="flex items-center gap-2 text-color-secondary text-sm">
    @switch (estadoSalvamento()) {
      @case ('salvando') {
        <p-progressSpinner
          styleClass="w-4 h-4"
          strokeWidth="4"
          aria-label="Salvando..." />
        <span>Salvando...</span>
      }
      @case ('salvo') {
        <i class="pi pi-check-circle text-green-500"></i>
        <span>Salvo automaticamente</span>
      }
      @case ('erro') {
        <i class="pi pi-exclamation-triangle text-yellow-500"></i>
        <span>Erro ao salvar</span>
      }
    }
  </div>

  <!-- Navegação (direita) -->
  <div class="flex gap-2">
    @if (passoAtual() > 1) {
      <p-button label="Voltar" icon="pi pi-arrow-left" outlined
                (onClick)="voltarPasso()"
                [disabled]="estadoSalvamento() === 'salvando'" />
    }
    @if (passoAtual() < 5) {
      <p-button label="Próximo" icon="pi pi-arrow-right" iconPos="right"
                (onClick)="avancarPasso()"
                [disabled]="!passoAtualValido() || estadoSalvamento() === 'salvando'"
                [loading]="estadoSalvamento() === 'salvando'" />
    } @else {
      <p-button label="Criar Personagem" icon="pi pi-check" severity="success"
                (onClick)="confirmarCriacao()"
                [disabled]="!wizardCompleto() || estadoSalvamento() === 'salvando'"
                [loading]="criando()" />
    }
  </div>
</div>
```

---

## 5. Passo 1 — Identidade Básica

### Wireframe

```
┌─────────────────────────────────────────────────────┐
│  H2: Identidade Básica                              │
│  Dê vida ao seu personagem com um nome e origem.   │
│                                                     │
│  Nome do Personagem *                               │
│  ┌────────────────────────────────────────────┐    │
│  │ Ex: Aldric, Filho da Névoa                  │    │
│  └────────────────────────────────────────────┘    │
│                                                     │
│  Gênero *                                           │
│  ┌────────────────────────────────────────────┐    │
│  │ Selecione um gênero...            ▼        │    │
│  └────────────────────────────────────────────┘    │
│                                                     │
│  Origem (opcional)                                  │
│  ┌────────────────────────────────────────────┐    │
│  │ De onde vem seu personagem?                 │    │
│  │                                             │    │
│  └────────────────────────────────────────────┘    │
│                                                     │
│  [Para Mestre criando NPC] ───────────────────      │
│  Este personagem é um NPC?                          │
│  [p-toggleSwitch] Não / Sim                         │
│  Descrição do NPC                                   │
│  ┌────────────────────────────────────────────┐    │
│  │ (aparece apenas se isNpc = true)            │    │
│  └────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

### Campos e Componentes

| Campo | Obrigatório | Componente PrimeNG | Validação |
|-------|-------------|---------------------|-----------|
| Nome | Sim | `pInputText` | minlength 2, maxlength 100 |
| Gênero | Sim | `p-select` com `[options]="generos()"` | required |
| Origem | Não | `p-textarea` autoResize, maxlength 500 | — |
| isNpc (MESTRE) | Não | `p-toggleSwitch` | — |
| Descrição NPC | Não | `p-textarea` autoResize, maxlength 1000 | aparece se isNpc = true |

```html
<!-- Passo 1: Identidade Básica -->
<p-step-panel [value]="1">
  <ng-template #content>
    <div class="flex flex-col gap-4 max-w-lg mx-auto">
      <div class="flex flex-col gap-1">
        <label for="nome" class="font-medium text-sm">
          Nome do Personagem <span class="text-red-500">*</span>
        </label>
        <input pInputText id="nome"
               [ngModel]="form().nome"
               (ngModelChange)="atualizarCampo('nome', $event)"
               placeholder="Ex: Aldric, Filho da Névoa"
               [class.ng-invalid]="campoInvalido('nome')"
               [class.ng-dirty]="campoDirty('nome')"
               aria-describedby="nome-hint"
               maxlength="100" />
        @if (campoInvalido('nome')) {
          <small id="nome-hint" class="text-red-500">
            O nome precisa ter pelo menos 2 caracteres.
          </small>
        }
      </div>

      <div class="flex flex-col gap-1">
        <label for="genero" class="font-medium text-sm">
          Gênero <span class="text-red-500">*</span>
        </label>
        <p-select id="genero"
                  [options]="generos()"
                  optionLabel="nome"
                  optionValue="id"
                  [ngModel]="form().generoId"
                  (ngModelChange)="atualizarCampo('generoId', $event)"
                  placeholder="Selecione um gênero..."
                  [class.ng-invalid]="campoInvalido('generoId')"
                  styleClass="w-full"
                  aria-label="Gênero do personagem" />
      </div>

      <div class="flex flex-col gap-1">
        <label for="origem" class="font-medium text-sm">Origem</label>
        <p-textarea id="origem"
                    [ngModel]="form().origem"
                    (ngModelChange)="atualizarCampo('origem', $event)"
                    placeholder="De onde vem seu personagem? Sua terra natal, background..."
                    autoResize="true"
                    rows="3"
                    maxlength="500"
                    styleClass="w-full"
                    aria-label="Origem do personagem" />
        <small class="text-color-secondary text-right">
          {{ form().origem?.length ?? 0 }}/500
        </small>
      </div>

      @if (isMestre()) {
        <p-divider />
        <div class="flex items-center justify-between">
          <label class="font-medium">Este personagem é um NPC?</label>
          <p-toggleSwitch
            [ngModel]="form().isNpc"
            (ngModelChange)="atualizarCampo('isNpc', $event)"
            aria-label="Marcar como NPC" />
        </div>
        @if (form().isNpc) {
          <div class="flex flex-col gap-1">
            <label for="descricao" class="font-medium text-sm">Descrição do NPC</label>
            <p-textarea id="descricao"
                        [ngModel]="form().descricao"
                        (ngModelChange)="atualizarCampo('descricao', $event)"
                        placeholder="Descrição física, personalidade, papel na narrativa..."
                        autoResize="true"
                        rows="4"
                        maxlength="1000"
                        styleClass="w-full" />
          </div>
        }
      }
    </div>
  </ng-template>
</p-step-panel>
```

---

## 6. Passo 2 — Raça e Classe

### Wireframe

```
┌─────────────────────────────────────────────────────┐
│  H2: Raça e Classe                                  │
│  Escolha a origem racial e a especialização.        │
│                                                     │
│  Raça *                                             │
│  ┌──────────────────┐  ┌──────────────────────┐    │
│  │ [Card Raça 1]    │  │ [Card Raça 2]        │    │
│  │ Humano           │  │ Elfo                 │    │
│  │ +0 FOR, +0 AGI   │  │ +2 AGI, -1 VIG       │    │
│  │ [SELECIONADO]    │  │                      │    │
│  └──────────────────┘  └──────────────────────┘    │
│                                                     │
│  Classe *                                           │
│  ┌────────────────────────────────────────────┐    │
│  │ Selecione uma classe...           ▼        │    │
│  └────────────────────────────────────────────┘    │
│                                                     │
│  [p-message info] Classes disponíveis para Humano: │
│  Guerreiro, Arqueiro, Mago... (lista filtrada)      │
└─────────────────────────────────────────────────────┘
```

### Componentes e Comportamento

**Raça — Cards Selecionáveis**

Usar `p-selectbutton` se o número de raças for pequeno (até 6), ou `p-listbox` com template customizado para listas maiores.

```html
<!-- Raças como cards clicáveis em grid -->
<div class="grid grid-cols-2 sm:grid-cols-3 gap-3" role="radiogroup" aria-label="Raça do personagem">
  @for (raca of racas(); track raca.id) {
    <div
      class="border-2 rounded-lg p-3 cursor-pointer transition-all duration-200"
      [class.border-primary]="form().racaId === raca.id"
      [class.border-surface-border]="form().racaId !== raca.id"
      [class.bg-primary-50]="form().racaId === raca.id"
      (click)="atualizarCampo('racaId', raca.id)"
      role="radio"
      [attr.aria-checked]="form().racaId === raca.id"
      [attr.aria-label]="'Raça: ' + raca.nome"
      tabindex="0"
      (keydown.enter)="atualizarCampo('racaId', raca.id)"
      (keydown.space)="atualizarCampo('racaId', raca.id)">
      <div class="font-semibold">{{ raca.nome }}</div>
      @if (raca.bonusAtributos?.length) {
        <div class="text-sm text-color-secondary mt-1">
          @for (bonus of raca.bonusAtributos; track bonus.atributoId) {
            <span [class.text-green-600]="bonus.bonus > 0"
                  [class.text-red-500]="bonus.bonus < 0">
              {{ bonus.bonus > 0 ? '+' : '' }}{{ bonus.bonus }} {{ bonus.atributoAbreviacao }}
            </span>
            @if (!$last) { <span>, </span> }
          }
        </div>
      } @else {
        <div class="text-sm text-color-secondary mt-1">Sem bônus de atributo</div>
      }
    </div>
  }
</div>
```

**Classe — Dropdown com filtragem por raça**

```html
<p-select
  [options]="classesDisponiveis()"
  optionLabel="nome"
  optionValue="id"
  [ngModel]="form().classeId"
  (ngModelChange)="atualizarCampo('classeId', $event)"
  placeholder="Selecione uma classe..."
  [filter]="true"
  filterPlaceholder="Buscar classe..."
  [emptyMessage]="'Nenhuma classe disponível para esta raça'"
  styleClass="w-full"
  aria-label="Classe do personagem" />
```

`classesDisponiveis` é um `computed()` que filtra as classes pela raça selecionada (usando `RacaClassePermitida` — se a raça tem restrições de classe). Se a raça não tem restrições, retorna todas as classes.

**Aviso de classes restritas**

```html
@if (racaSelecionada()?.temRestricaoClasse) {
  <p-message severity="info" styleClass="mt-2">
    <ng-template #messageicon><i class="pi pi-info-circle mr-2"></i></ng-template>
    Esta raça possui restrições de classe. Somente as classes compatíveis estão disponíveis.
  </p-message>
}
```

---

## 7. Passo 3 — Personalidade

### Wireframe

```
┌─────────────────────────────────────────────────────┐
│  H2: Personalidade                                  │
│  Defina o alinhamento moral e comportamental.       │
│                                                     │
│  Índole *                 Presença *                │
│  (alinhamento moral)      (alinhamento ético)       │
│  ┌────────────────┐       ┌────────────────┐        │
│  │ Bom     ○      │       │ Bom      ○     │        │
│  │ Neutro  ●      │       │ Leal     ○     │        │
│  │ Mau     ○      │       │ Caótico  ●     │        │
│  └────────────────┘       └────────────────┘        │
│                                                     │
│  Combinação atual: "Neutro e Caótico"               │
│  [p-tag] Rogue / Agente do caos                     │
└─────────────────────────────────────────────────────┘
```

### Componentes

```html
<div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
  <!-- Índole -->
  <div class="flex flex-col gap-2">
    <label class="font-medium">
      Índole <span class="text-red-500">*</span>
    </label>
    <p class="text-sm text-color-secondary m-0">
      Tendência moral do personagem — bem, mal ou neutralidade.
    </p>
    <div class="flex flex-col gap-2">
      @for (indole of indoles(); track indole.id) {
        <div
          class="flex items-center gap-3 p-3 border rounded-lg cursor-pointer transition-all duration-200"
          [class.border-primary]="form().indoleId === indole.id"
          [class.border-surface-border]="form().indoleId !== indole.id"
          (click)="atualizarCampo('indoleId', indole.id)"
          role="radio"
          [attr.aria-checked]="form().indoleId === indole.id">
          <p-radioButton
            [value]="indole.id"
            [ngModel]="form().indoleId"
            (ngModelChange)="atualizarCampo('indoleId', $event)"
            [inputId]="'indole-' + indole.id" />
          <label [for]="'indole-' + indole.id" class="cursor-pointer font-medium">
            {{ indole.nome }}
          </label>
        </div>
      }
    </div>
  </div>

  <!-- Presença -->
  <div class="flex flex-col gap-2">
    <label class="font-medium">
      Presença <span class="text-red-500">*</span>
    </label>
    <p class="text-sm text-color-secondary m-0">
      Postura ética e relação com regras e autoridade.
    </p>
    <div class="flex flex-col gap-2">
      @for (presenca of presencas(); track presenca.id) {
        <div
          class="flex items-center gap-3 p-3 border rounded-lg cursor-pointer transition-all duration-200"
          [class.border-primary]="form().presencaId === presenca.id"
          [class.border-surface-border]="form().presencaId !== presenca.id"
          (click)="atualizarCampo('presencaId', presenca.id)"
          role="radio"
          [attr.aria-checked]="form().presencaId === presenca.id">
          <p-radioButton
            [value]="presenca.id"
            [ngModel]="form().presencaId"
            (ngModelChange)="atualizarCampo('presencaId', $event)"
            [inputId]="'presenca-' + presenca.id" />
          <label [for]="'presenca-' + presenca.id" class="cursor-pointer font-medium">
            {{ presenca.nome }}
          </label>
        </div>
      }
    </div>
  </div>
</div>
```

---

## 8. Passo 4 — Distribuição de Atributos

Este é o passo mais complexo e crítico. O personagem no nível 1 recebe pontos de atributo conforme `NivelConfig.pontosAtributo` para o nível 1. O jogador distribui esses pontos entre os atributos disponíveis no jogo.

### Wireframe

```
┌─────────────────────────────────────────────────────────────┐
│  H2: Atributos                                              │
│  Distribua os pontos de base do seu personagem.             │
│                                                             │
│  Pontos disponíveis: [p-badge] 15   Limitador: 10           │
│  [p-progressBar — pontos gastos/total]                      │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Atributo        Base   Ajustar             Ímpeto   │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │ Força (FOR)      5    [-] [p-inputNumber] [+]   15  │   │
│  │ Agilidade (AGI)  3    [-] [p-inputNumber] [+]    9  │   │
│  │ Vigor (VIG)      4    [-] [p-inputNumber] [+]   12  │   │
│  │ Sabedoria (SAB)  1    [-] [p-inputNumber] [+]    3  │   │
│  │ Inteligência(INT)1    [-] [p-inputNumber] [+]    3  │   │
│  │ Intuição (INTU)  1    [-] [p-inputNumber] [+]    3  │   │
│  │ Astúcia (AST)    0    [-] [p-inputNumber] [+]    0  │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  [p-message warn] Você tem 0 pontos restantes.              │
└─────────────────────────────────────────────────────────────┘
```

### Lógica de Distribuição

```typescript
// Estado do passo 4
protected pontosTotal = computed(() => {
  // Busca de NivelConfig nível 1 do jogo
  return this.nivelConfig().find(n => n.nivel === 1)?.pontosAtributo ?? 0;
});

protected pontosGastos = computed(() => {
  return Object.values(this.form().atributosBase ?? {})
    .reduce((soma, valor) => soma + (valor ?? 0), 0);
});

protected pontosRestantes = computed(() =>
  this.pontosTotal() - this.pontosGastos()
);

protected limitadorAtributo = computed(() =>
  this.nivelConfig().find(n => n.nivel === 1)?.limitadorAtributo ?? 10
);

protected ajustarAtributo(atributoId: number, delta: number): void {
  const atual = this.form().atributosBase?.[atributoId] ?? 0;
  const novo = atual + delta;

  // Validações
  if (novo < 0) return; // mínimo 0
  if (novo > this.limitadorAtributo()) return; // não pode exceder limitador
  if (delta > 0 && this.pontosRestantes() <= 0) return; // sem pontos disponíveis

  this.atualizarCampo('atributosBase', {
    ...this.form().atributosBase,
    [atributoId]: novo
  });
}
```

### Template do Passo 4

```html
<p-step-panel [value]="4">
  <ng-template #content>
    <div class="flex flex-col gap-4">
      <!-- Contador de pontos -->
      <div class="flex items-center justify-between flex-wrap gap-3">
        <div class="flex items-center gap-3">
          <span class="font-medium">Pontos disponíveis:</span>
          <p-badge
            [value]="pontosRestantes()"
            [severity]="pontosRestantes() === 0 ? 'success' : 'info'"
            styleClass="text-lg" />
        </div>
        <div class="text-sm text-color-secondary">
          Limitador de atributo: <strong>{{ limitadorAtributo() }}</strong>
        </div>
      </div>

      <!-- Barra de progresso de gasto de pontos -->
      <p-progressBar
        [value]="(pontosGastos() / pontosTotal()) * 100"
        [showValue]="false"
        styleClass="h-2"
        aria-label="Progresso de distribuição de pontos" />

      <!-- Tabela de atributos -->
      <div class="overflow-x-auto">
        <table class="w-full" style="min-width: 480px;"
               role="table" aria-label="Distribuição de atributos">
          <thead>
            <tr class="text-left text-sm text-color-secondary border-b border-surface-border">
              <th class="py-2 pr-4">Atributo</th>
              <th class="py-2 pr-4 text-center">Valor base</th>
              <th class="py-2 pr-4 text-center">Ajustar</th>
              <th class="py-2 text-center">Ímpeto</th>
            </tr>
          </thead>
          <tbody>
            @for (atributo of atributos(); track atributo.id) {
              <tr class="border-b border-surface-border">
                <td class="py-3 pr-4">
                  <div class="font-medium">{{ atributo.nome }}</div>
                  <div class="text-sm text-color-secondary font-mono">{{ atributo.abreviacao }}</div>
                </td>
                <td class="py-3 pr-4 text-center">
                  <span class="font-mono text-lg font-bold">
                    {{ form().atributosBase?.[atributo.id] ?? 0 }}
                  </span>
                </td>
                <td class="py-3 pr-4">
                  <div class="flex items-center justify-center gap-2">
                    <p-button icon="pi pi-minus"
                              [rounded]="true" text
                              size="small"
                              (onClick)="ajustarAtributo(atributo.id, -1)"
                              [disabled]="(form().atributosBase?.[atributo.id] ?? 0) <= 0"
                              [attr.aria-label]="'Diminuir ' + atributo.nome" />
                    <p-inputNumber
                      [ngModel]="form().atributosBase?.[atributo.id] ?? 0"
                      (ngModelChange)="definirAtributo(atributo.id, $event)"
                      [min]="0"
                      [max]="limitadorAtributo()"
                      [showButtons]="false"
                      inputStyleClass="text-center w-12 font-mono"
                      [attr.aria-label]="atributo.nome + ' valor'" />
                    <p-button icon="pi pi-plus"
                              [rounded]="true" text
                              size="small"
                              (onClick)="ajustarAtributo(atributo.id, 1)"
                              [disabled]="pontosRestantes() <= 0 ||
                                          (form().atributosBase?.[atributo.id] ?? 0) >= limitadorAtributo()"
                              [attr.aria-label]="'Aumentar ' + atributo.nome" />
                  </div>
                </td>
                <td class="py-3 text-center">
                  <span class="font-mono text-color-secondary">
                    {{ calcularImpeto(atributo, form().atributosBase?.[atributo.id] ?? 0) }}
                  </span>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <!-- Aviso de pontos zerados -->
      @if (pontosRestantes() === 0) {
        <p-message severity="success" styleClass="mt-2">
          <ng-template #messageicon><i class="pi pi-check-circle mr-2"></i></ng-template>
          Todos os pontos foram distribuídos. Você pode prosseguir.
        </p-message>
      } @else if (pontosRestantes() > 0) {
        <p-message severity="info" styleClass="mt-2">
          <ng-template #messageicon><i class="pi pi-info-circle mr-2"></i></ng-template>
          Você ainda tem <strong>{{ pontosRestantes() }} ponto(s)</strong> para distribuir.
          Não é obrigatório gastar todos agora — pontos não utilizados ficam disponíveis.
        </p-message>
      }
    </div>
  </ng-template>
</p-step-panel>
```

---

## 9. Passo 5 — Revisão e Confirmação

### Wireframe

```
┌─────────────────────────────────────────────────────────────┐
│  H2: Revisão                                                │
│  Confirme os dados antes de criar o personagem.             │
│                                                             │
│  ┌─── Identidade ─────────────────────────────────────┐    │
│  │  Nome: Aldric, Filho da Névoa        [Editar]       │    │
│  │  Gênero: Masculino                                  │    │
│  │  Origem: Nascido nas montanhas...                   │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌─── Raça e Classe ──────────────────────────────────┐    │
│  │  Raça: Humano (+0 atributos)         [Editar]       │    │
│  │  Classe: Guerreiro                                  │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌─── Personalidade ──────────────────────────────────┐    │
│  │  Índole: Bom                         [Editar]       │    │
│  │  Presença: Caótico                                  │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌─── Atributos Base ─────────────────────────────────┐    │
│  │  FOR 5 | AGI 3 | VIG 4 | SAB 1       [Editar]       │    │
│  │  INT 1 | INTU 1 | AST 0                             │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Template

```html
<p-step-panel [value]="5">
  <ng-template #content>
    <div class="flex flex-col gap-4 max-w-2xl mx-auto">

      <!-- Seção Identidade -->
      <p-card>
        <ng-template #header>
          <div class="flex justify-between items-center px-4 pt-4">
            <h3 class="m-0">Identidade Básica</h3>
            <p-button label="Editar" icon="pi pi-pencil" text size="small"
                      (onClick)="irParaPasso(1)"
                      aria-label="Editar identidade básica" />
          </div>
        </ng-template>
        <div class="grid grid-cols-2 gap-2 text-sm">
          <div><span class="text-color-secondary">Nome:</span> <strong>{{ form().nome }}</strong></div>
          <div><span class="text-color-secondary">Gênero:</span> {{ generoSelecionado()?.nome }}</div>
          @if (form().origem) {
            <div class="col-span-2">
              <span class="text-color-secondary">Origem:</span> {{ form().origem }}
            </div>
          }
        </div>
      </p-card>

      <!-- Seção Raça e Classe -->
      <p-card>
        <ng-template #header>
          <div class="flex justify-between items-center px-4 pt-4">
            <h3 class="m-0">Raça e Classe</h3>
            <p-button label="Editar" icon="pi pi-pencil" text size="small"
                      (onClick)="irParaPasso(2)"
                      aria-label="Editar raça e classe" />
          </div>
        </ng-template>
        <div class="grid grid-cols-2 gap-2 text-sm">
          <div><span class="text-color-secondary">Raça:</span> <strong>{{ racaSelecionada()?.nome }}</strong></div>
          <div><span class="text-color-secondary">Classe:</span> <strong>{{ classeSelecionada()?.nome }}</strong></div>
        </div>
      </p-card>

      <!-- Seção Personalidade -->
      <p-card>
        <ng-template #header>
          <div class="flex justify-between items-center px-4 pt-4">
            <h3 class="m-0">Personalidade</h3>
            <p-button label="Editar" icon="pi pi-pencil" text size="small"
                      (onClick)="irParaPasso(3)"
                      aria-label="Editar personalidade" />
          </div>
        </ng-template>
        <div class="grid grid-cols-2 gap-2 text-sm">
          <div><span class="text-color-secondary">Índole:</span> {{ indoleSelecionada()?.nome }}</div>
          <div><span class="text-color-secondary">Presença:</span> {{ presencaSelecionada()?.nome }}</div>
        </div>
      </p-card>

      <!-- Seção Atributos -->
      <p-card>
        <ng-template #header>
          <div class="flex justify-between items-center px-4 pt-4">
            <h3 class="m-0">Atributos Base</h3>
            <p-button label="Editar" icon="pi pi-pencil" text size="small"
                      (onClick)="irParaPasso(4)"
                      aria-label="Editar atributos" />
          </div>
        </ng-template>
        <div class="flex flex-wrap gap-4">
          @for (atributo of atributos(); track atributo.id) {
            <div class="flex flex-col items-center gap-1 min-w-14">
              <span class="text-xs text-color-secondary font-mono">{{ atributo.abreviacao }}</span>
              <span class="text-xl font-bold font-mono">
                {{ form().atributosBase?.[atributo.id] ?? 0 }}
              </span>
            </div>
          }
        </div>
        @if (pontosRestantes() > 0) {
          <p-message severity="warn" styleClass="mt-3">
            <ng-template #messageicon><i class="pi pi-exclamation-triangle mr-2"></i></ng-template>
            Você tem {{ pontosRestantes() }} ponto(s) de atributo não distribuídos.
            Eles ficarão disponíveis para uso futuro.
          </p-message>
        }
      </p-card>

    </div>
  </ng-template>
</p-step-panel>
```

---

## 10. Mecanismo de Auto-save

### Fluxo de Auto-save

```
Usuário altera campo
        ↓
[debounce 1500ms]
        ↓
  estadoSalvamento.set('salvando')
        ↓
  PUT /api/v1/fichas/{id} (rascunho)
  ou POST se ainda não existe fichaId
        ↓
  Sucesso → estadoSalvamento.set('salvo')
  Erro    → estadoSalvamento.set('erro') + toast warn
```

### Regras de Auto-save

- O auto-save ocorre **a cada mudança de campo** com debounce de 1500ms
- Se a ficha ainda não existe no backend, o **primeiro auto-save** cria via `POST /fichas` com os campos disponíveis até o momento
- Os auto-saves subsequentes usam `PUT /fichas/{fichaId}` com apenas os campos preenchidos
- Ao avançar um passo, o auto-save é acionado imediatamente (sem debounce), antes de exibir o próximo passo
- O botão "Próximo" fica desabilitado durante o salvamento
- O botão "Criar Personagem" no passo 5 aguarda o auto-save concluir antes de emitir o `POST` final de ativação

### Indicador Visual de Estado de Salvamento

| Estado | Visual | Comportamento |
|--------|--------|---------------|
| `idle` | Nada exibido | Estado inicial antes de qualquer alteração |
| `salvando` | `p-progressSpinner` pequeno + "Salvando..." | Bloqueia navegação de passo |
| `salvo` | `pi pi-check-circle` verde + "Salvo automaticamente" | Desaparece após 3s |
| `erro` | `pi pi-exclamation-triangle` amarelo + "Erro ao salvar" | Persiste até nova tentativa |

---

## 11. Badge de Rascunho na Listagem de Fichas

Após a criação via wizard, a ficha pode estar em dois estados:

| Estado | Badge | Descrição |
|--------|-------|-----------|
| `RASCUNHO` | `p-tag severity="warn" value="Rascunho"` | Wizard iniciado mas não concluído |
| `ATIVO` | `p-tag severity="success" value="Ativo"` | Wizard concluído, ficha disponível para sessão |

```html
<!-- No card de listagem de fichas -->
@if (ficha.status === 'RASCUNHO') {
  <p-tag severity="warn" value="Rascunho" icon="pi pi-pencil"
         pTooltip="Esta ficha está incompleta. Continue o wizard para ativá-la."
         tooltipPosition="top" />
}
```

**Regra de negócio**: Fichas em estado `RASCUNHO` não podem ser usadas em sessões de jogo. O botão "Entrar em sessão" fica desabilitado para fichas com status RASCUNHO.

---

## 12. Validação de Passo (passoAtualValido)

```typescript
protected passoAtualValido = computed((): boolean => {
  switch (this.passoAtual()) {
    case 1:
      return !!(this.form().nome?.length >= 2 && this.form().generoId);
    case 2:
      return !!(this.form().racaId && this.form().classeId);
    case 3:
      return !!(this.form().indoleId && this.form().presencaId);
    case 4:
      // Todos os atributos definidos (pode ter pontos restantes — não é bloqueante)
      return this.atributos().every(a =>
        (this.form().atributosBase?.[a.id] ?? 0) >= 0
      );
    case 5:
      return this.wizardCompleto();
    default:
      return false;
  }
});

protected wizardCompleto = computed((): boolean =>
  !!(this.form().nome?.length >= 2 &&
     this.form().generoId &&
     this.form().racaId &&
     this.form().classeId &&
     this.form().indoleId &&
     this.form().presencaId)
);
```

---

## 13. Estados da UI

### Loading Inicial (carregando dados das configurações)

```html
@if (carregandoConfigs()) {
  <div class="flex flex-col items-center justify-center py-16 gap-4">
    <p-progressSpinner aria-label="Carregando configurações do jogo..." />
    <p class="text-color-secondary">Carregando configurações do jogo...</p>
  </div>
}
```

### Erro ao Carregar Configurações

```html
@if (erroConfigs()) {
  <div class="flex flex-col items-center justify-center py-16 gap-4 text-center">
    <i class="pi pi-exclamation-circle text-red-500" style="font-size: 3rem"></i>
    <h2 class="text-xl font-semibold m-0">Erro ao carregar configurações</h2>
    <p class="text-color-secondary m-0">{{ erroConfigs() }}</p>
    <p-button label="Tentar novamente" icon="pi pi-refresh"
              (onClick)="carregarConfiguracoes()" />
  </div>
}
```

### Confirmação de Saída (ao clicar em "Voltar" para fora do wizard)

```typescript
// Guard de saída
canDeactivate(): Observable<boolean> {
  if (this.form().nome || this.fichaId()) {
    // Houve progresso — confirmar saída
    return this.confirmationService.confirm({
      header: 'Sair da criação?',
      message: 'Seu progresso foi salvo automaticamente. Você pode continuar mais tarde.',
      acceptLabel: 'Sair',
      rejectLabel: 'Continuar aqui',
      acceptButtonStyleClass: 'p-button-outlined'
    });
  }
  return of(true);
}
```

---

## 14. Comportamento Responsivo

### Desktop (> 1024px)
- `p-stepper` horizontal com rótulos completos ("Identidade Básica", "Raça e Classe", etc.)
- Formulário centralizado com `max-w-lg mx-auto`
- Rodapé do wizard em linha: estado de auto-save à esquerda, botões à direita

### Tablet (640px–1024px)
- `p-stepper` horizontal com ícones e rótulos curtos ("Identidade", "Raça", etc.)
- Formulário com `max-w-full` com padding de 16px
- Grid de raças: 2 colunas

### Mobile (< 640px)
- `p-stepper` substituído por indicador textual + `p-progressBar`
- Formulário em coluna única, full width
- Grid de raças: 1 ou 2 colunas pequenas
- Rodapé fixo (`position: fixed; bottom: 0`) com botões de navegação
- Tabela de atributos com scroll horizontal

---

## 15. Componentes PrimeNG Utilizados

| Componente | Módulo | Uso |
|-----------|--------|-----|
| `p-stepper` | `StepperModule` | Indicador de passos e navegação (desktop/tablet) |
| `p-progressBar` | `ProgressBarModule` | Progresso geral (mobile) + distribuição de pontos |
| `p-progressSpinner` | `ProgressSpinnerModule` | Estado de auto-save, loading de configs |
| `p-card` | `CardModule` | Container de cada passo + cards da revisão |
| `p-button` | `ButtonModule` | Navegação de passos, editar seção na revisão |
| `pInputText` | `InputTextModule` | Campo nome |
| `p-select` | `SelectModule` | Dropdown de gênero e classe |
| `p-textarea` | `TextareaModule` | Origem, descrição NPC |
| `p-toggleSwitch` | `ToggleSwitchModule` | Flag isNpc (Mestre) |
| `p-radioButton` | `RadioButtonModule` | Seleção de índole e presença |
| `p-inputNumber` | `InputNumberModule` | Valor de atributo no passo 4 |
| `p-badge` | `BadgeModule` | Contador de pontos disponíveis |
| `p-message` | `MessageModule` | Avisos contextuais em cada passo |
| `p-tag` | `TagModule` | Badge de rascunho/ativo na listagem |
| `p-toast` | `ToastModule` | Feedback de auto-save (erro) e criação (sucesso) |
| `p-confirmDialog` | `ConfirmDialogModule` | Guard de saída do wizard |
| `p-divider` | `DividerModule` | Separador na seção NPC |

---

## 16. Acessibilidade (WCAG 2.1 AA)

- `p-stepper`: cada `p-step` tem `aria-label` descritivo com número e título
- Campos de raça como `role="radiogroup"` e itens com `role="radio"` + `aria-checked`
- Campos de índole e presença usam `p-radioButton` nativo com `for`/`id` vinculados
- Botões de ajuste de atributo (+/-) têm `aria-label="Aumentar Força"` / `aria-label="Diminuir Força"`
- `p-inputNumber` de atributo tem `aria-label` com nome do atributo
- Estado de auto-save é comunicado via `aria-live="polite"` no container do indicador
- `p-progressBar` de pontos tem `aria-label="Progresso de distribuição de pontos"`
- Todos os campos obrigatórios têm `aria-required="true"` e indicação visual (asterisco vermelho + texto)
- Tecla Tab navega corretamente entre campos — `p-stepper` respeita a ordem DOM
- Focus trap: ao abrir o guard de saída (`p-confirmDialog`), o foco fica preso no dialog até ser fechado

---

## 17. Estrutura de Arquivos

```
ficha/
  wizard/
    ficha-wizard.component.ts             [SMART] — orquestra todo o wizard
    ficha-wizard.component.html
    steps/
      step-identidade/
        step-identidade.component.ts      [DUMB] — campos do passo 1
      step-raca-classe/
        step-raca-classe.component.ts     [DUMB] — seleção de raça e classe
      step-personalidade/
        step-personalidade.component.ts   [DUMB] — índole e presença
      step-atributos/
        step-atributos.component.ts       [DUMB] — distribuição de pontos
      step-revisao/
        step-revisao.component.ts         [DUMB] — resumo + confirmação
```

---

## 18. Checklist de Implementação

- [ ] Interface `FichaWizardForm` com todos os campos do wizard
- [ ] `FichaWizardComponent` com signal `passoAtual`, `form`, `estadoSalvamento`
- [ ] Auto-save com debounce 1500ms + save imediato ao avançar passo
- [ ] Passo 1: campos nome (obrigatório), gênero (dropdown), origem (textarea), isNpc + descrição (Mestre)
- [ ] Passo 2: cards clicáveis de raças com bônus de atributo exibidos, dropdown de classe com filtro por raça
- [ ] Passo 3: radiobuttons de índole e presença em grid 2 colunas
- [ ] Passo 4: tabela de atributos com botões +/-, contador de pontos regressivo, limitador respeitado
- [ ] Passo 4: aviso de pontos restantes + confirmação de pontos esgotados
- [ ] Passo 5: cards de revisão com botão "Editar" por seção (navega para o passo)
- [ ] Validação por passo (`passoAtualValido`) — bloqueia "Próximo" se inválido
- [ ] `wizardCompleto` computed — habilita "Criar Personagem" no passo 5
- [ ] Guard de saída com `p-confirmDialog`
- [ ] Badge `RASCUNHO` na listagem de fichas
- [ ] Mobile: indicador textual + `p-progressBar` em vez do `p-stepper`
- [ ] Mobile: rodapé fixo com botões de navegação
- [ ] Skeleton de loading durante carregamento das configs do jogo
- [ ] `aria-label` em todos os campos e botões de ajuste de atributo
- [ ] `aria-live` no indicador de auto-save
