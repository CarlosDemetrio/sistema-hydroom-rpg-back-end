# Design Spec: Visibilidade de NPC

> Documento de design UI/UX para a gestão de visibilidade de estatísticas de NPC pelo Mestre.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versão: 1.0 | Gerado em: 2026-04-02
> Decisões do PO: BA-GAPS-2026-04-02.md (GAP-05)

---

## 1. Visão Geral e Decisões do PO

**Regra de negócio definida pelo PO:**
- NPC é mecanicamente idêntico à ficha de jogador — usa o mesmo motor de cálculo, mesmas abas, mesmos campos
- O Mestre pode revelar as estatísticas de um NPC para **jogadores específicos**, não para todos de uma vez
- Caso de uso típico: NPC é uma besta parceira ou invocação de apenas um jogador — somente esse jogador vê os stats
- A flag `visivelParaJogadores` no backend é **booleano global** — o controle granular por jogador precisa de modelagem adicional no frontend e possivelmente no backend

### Dois Níveis de Revelação

| Nível | Descrição | Quem controla |
|-------|-----------|---------------|
| **Global** | Todos os jogadores aprovados no jogo veem o NPC listado | Mestre, flag `visivelParaJogadores` |
| **Granular** | Apenas jogadores específicos veem as estatísticas completas | Mestre, via seletor de jogadores |

---

## 2. Onde a Visibilidade é Gerenciada

A visibilidade de NPC é gerenciada **dentro da própria tela de detalhe do NPC**, acessível apenas para o Mestre. A decisão de não criar uma tela separada de "gerenciar visibilidade" é intencional: o Mestre gerencia a visibilidade no contexto de ver a ficha do NPC.

**Localização na tela**: Painel lateral colapsável no lado direito da `FichaDetailPage` quando `ficha.isNpc = true` e `role = MESTRE`. Em mobile: botão de ação "Gerenciar Visibilidade" que abre um bottom sheet (`p-drawer` com position="bottom").

---

## 3. Wireframes

### 3.1 Desktop — FichaDetailPage de NPC com Painel de Visibilidade

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] NPCs   "Barahir, o Corvo Negro"   [Avatar]              │
├────────────────────────────────────────┬────────────────────────────────┤
│                                        │ [Painel: Visibilidade]         │
│  ┌─────────────────────────────────┐   │                                │
│  │ [FICHA-HEADER]                  │   │ H3: Visibilidade para Jogadores │
│  │ Barahir, o Corvo Negro  [NPC]   │   │                                │
│  │ Humano • Guerreiro • Nv. 8      │   │ Visível globalmente?            │
│  │                                 │   │ [p-toggleSwitch] Sim / Não      │
│  │ Vida [████] 40/50               │   │                                │
│  │ Essência [██] 8/20              │   │ ─────────────────────           │
│  └─────────────────────────────────┘   │                                │
│                                        │ Revelar stats para:             │
│  [p-tabs — Resumo|Atrib.|Apt.|...]     │ [p-multiselect]                │
│  ═══════════════════════════════════   │  Jogadores aprovados...         │
│                                        │                                │
│  [Conteúdo da aba ativa]               │ Participantes com acesso:       │
│                                        │  [p-avatar] Ana → VER stats    │
│                                        │  [p-avatar] João → SEM stats   │
│                                        │                                │
│                                        │ [Salvar visibilidade]           │
└────────────────────────────────────────┴────────────────────────────────┘
```

### 3.2 Mobile — Bottom Sheet de Visibilidade

```
┌─────────────────────────────┐
│ [<] NPCs         [Visibil.] │  ← botão "Visibilidade" na toolbar
├─────────────────────────────┤
│ [FICHA-HEADER do NPC]       │
├─────────────────────────────┤
│ [p-tabs]                    │
│ [Conteúdo da aba ativa]     │
└─────────────────────────────┘

    ↕ ao clicar em [Visibil.]:

┌─────────────────────────────┐
│  Visibilidade               │  ← p-drawer position="bottom"
│  ──────────────             │    height="60vh"
│                             │
│  Visível globalmente?       │
│  [p-toggleSwitch]           │
│                             │
│  Revelar stats para:        │
│  [p-multiselect full-width] │
│                             │
│  [Salvar]    [Cancelar]     │
└─────────────────────────────┘
```

---

## 4. Painel de Visibilidade — Componente Detalhado

**Componente**: `NpcVisibilidadeComponent` (Dumb)
**Selector**: `app-npc-visibilidade`
**Renderizado apenas quando**: `ficha.isNpc === true && role === 'MESTRE'`

### Props

```typescript
fichaId = input.required<number>();
jogoId = input.required<number>();
visivelParaJogadores = input<boolean>(false);
jogadoresComAcesso = input<number[]>([]); // IDs dos jogadores com acesso granular

visibilidadeAtualizada = output<NpcVisibilidadeUpdate>();

interface NpcVisibilidadeUpdate {
  visivelParaJogadores: boolean;
  jogadoresComAcesso: number[];
}
```

### Template do Painel

```html
<div class="flex flex-col gap-4" role="region" aria-label="Controles de visibilidade do NPC">

  <!-- Toggle: Visível globalmente -->
  <div class="flex items-start justify-between gap-3">
    <div class="flex flex-col gap-1">
      <span class="font-semibold text-sm">Visível para todos os jogadores?</span>
      <span class="text-xs text-color-secondary">
        Quando ativo, todos os jogadores aprovados veem este NPC listado.
      </span>
    </div>
    <p-toggleSwitch
      [(ngModel)]="visivelGlobalmente"
      aria-label="Tornar NPC visível para todos os jogadores" />
  </div>

  <p-divider />

  <!-- Seletor de jogadores com acesso granular -->
  <div class="flex flex-col gap-2">
    <label class="font-semibold text-sm" for="jogadores-acesso">
      Revelar estatísticas para:
    </label>
    <p class class="text-xs text-color-secondary m-0">
      Estes jogadores verão os stats completos do NPC (vida, atributos, etc.).
      Ideal para bestas parceiras e NPCs aliados de um personagem específico.
    </p>
    <p-multiselect
      id="jogadores-acesso"
      [options]="participantesAprovados()"
      optionLabel="nomePersonagem"
      optionValue="jogadorId"
      [(ngModel)]="jogadoresSelecionados"
      placeholder="Selecionar jogadores..."
      [filter]="participantesAprovados().length > 6"
      filterPlaceholder="Buscar jogador..."
      [showToggleAll]="true"
      display="chip"
      styleClass="w-full"
      [emptyMessage]="'Nenhum jogador aprovado neste jogo'"
      aria-label="Selecionar jogadores com acesso às estatísticas do NPC">
      <ng-template #item let-participante>
        <div class="flex items-center gap-2">
          <p-avatar
            [label]="participante.nomePersonagem?.charAt(0)"
            size="small"
            shape="circle"
            styleClass="flex-shrink-0" />
          <div class="flex flex-col">
            <span class="text-sm font-medium">{{ participante.nomePersonagem }}</span>
            <span class="text-xs text-color-secondary">{{ participante.jogadorNome }}</span>
          </div>
        </div>
      </ng-template>
      <ng-template #selecteditems let-selecionados>
        @for (jogadorId of selecionados; track jogadorId) {
          <p-chip
            [label]="obterNomeJogador(jogadorId)"
            [removable]="true"
            (onRemove)="removerJogador(jogadorId)"
            styleClass="text-xs" />
        }
      </ng-template>
    </p-multiselect>
  </div>

  <!-- Lista resumo dos participantes aprovados com status de acesso -->
  @if (participantesAprovados().length > 0) {
    <div class="flex flex-col gap-2">
      <span class="text-xs font-semibold text-color-secondary uppercase tracking-wide">
        Status de acesso
      </span>
      @for (participante of participantesAprovados(); track participante.jogadorId) {
        <div class="flex items-center justify-between gap-2 p-2 rounded-lg"
             [class.surface-50]="temAcesso(participante.jogadorId)"
             [class.surface-card]="!temAcesso(participante.jogadorId)">
          <div class="flex items-center gap-2">
            <p-avatar
              [label]="participante.nomePersonagem?.charAt(0)"
              size="small"
              shape="circle"
              [style]="{ 'background-color': temAcesso(participante.jogadorId) ? 'var(--green-100)' : 'var(--surface-200)', 'color': temAcesso(participante.jogadorId) ? 'var(--green-700)' : 'var(--text-color-secondary)' }" />
            <div class="flex flex-col">
              <span class="text-sm font-medium">{{ participante.nomePersonagem }}</span>
              <span class="text-xs text-color-secondary">{{ participante.jogadorNome }}</span>
            </div>
          </div>
          @if (temAcesso(participante.jogadorId)) {
            <p-tag value="Vê stats" severity="success" styleClass="text-xs" />
          } @else {
            <p-tag value="Não vê" severity="secondary" styleClass="text-xs" />
          }
        </div>
      }
    </div>
  }

  <!-- Botão Salvar -->
  <div class="flex gap-2 mt-2">
    <p-button
      label="Salvar visibilidade"
      icon="pi pi-save"
      styleClass="w-full"
      [loading]="salvando()"
      (onClick)="salvarVisibilidade()"
      [disabled]="!houveAlteracao()"
      aria-label="Salvar configurações de visibilidade do NPC" />
  </div>

</div>
```

---

## 5. Posicionamento no Layout Desktop

No `FichaDetailPage`, quando `ficha.isNpc === true` e o usuário é Mestre, o layout muda de uma coluna para duas:

```html
<!-- FichaDetailPage com NPC — layout desktop -->
<div class="grid gap-4"
     [class.grid-cols-1]="!mostrarPainelNpc()"
     [class.lg:grid-cols-3]="mostrarPainelNpc()">

  <!-- Coluna principal: header + tabs (2/3 do espaço) -->
  <div [class.lg:col-span-2]="mostrarPainelNpc()">
    <app-ficha-header [ficha]="ficha()" />
    <p-tabs [value]="abaAtiva()" scrollable>
      ...
    </p-tabs>
  </div>

  <!-- Coluna lateral: painel de visibilidade NPC (1/3 do espaço) -->
  @if (mostrarPainelNpc()) {
    <div class="lg:block hidden">
      <p-card>
        <ng-template #header>
          <div class="px-4 pt-4">
            <h3 class="m-0 flex items-center gap-2">
              <i class="pi pi-eye text-primary"></i>
              Visibilidade
            </h3>
          </div>
        </ng-template>
        <app-npc-visibilidade
          [fichaId]="ficha()!.id"
          [jogoId]="ficha()!.jogoId"
          [visivelParaJogadores]="ficha()!.visivelParaJogadores"
          [jogadoresComAcesso]="jogadoresComAcesso()"
          (visibilidadeAtualizada)="onVisibilidadeAtualizada($event)" />
      </p-card>
    </div>
  }

</div>
```

`mostrarPainelNpc = computed(() => this.ficha()?.isNpc && this.isMestre())`

---

## 6. Mobile — Drawer de Visibilidade

```html
<!-- Botão na toolbar (visível apenas em mobile quando isNpc + Mestre) -->
@if (mostrarPainelNpc() && isMobile()) {
  <p-button icon="pi pi-eye"
            [badge]="jogadoresComAcesso().length > 0 ? jogadoresComAcesso().length + '' : undefined"
            badgeSeverity="success"
            text rounded
            (onClick)="abrirDrawerVisibilidade()"
            pTooltip="Gerenciar visibilidade"
            aria-label="Gerenciar visibilidade do NPC" />
}

<!-- Drawer mobile -->
<p-drawer
  [(visible)]="drawerVisibilidadeAberto"
  position="bottom"
  styleClass="h-auto max-h-screen-60"
  header="Visibilidade do NPC"
  aria-label="Painel de visibilidade do NPC">
  <app-npc-visibilidade
    [fichaId]="ficha()!.id"
    [jogoId]="ficha()!.jogoId"
    [visivelParaJogadores]="ficha()!.visivelParaJogadores"
    [jogadoresComAcesso]="jogadoresComAcesso()"
    (visibilidadeAtualizada)="onVisibilidadeAtualizada($event)" />
</p-drawer>
```

---

## 7. Perspectiva do Jogador

### O que o Jogador vê dependendo da configuração do Mestre

| Cenário | O que o Jogador vê |
|---------|-------------------|
| `visivelParaJogadores = false` | NPC não aparece na lista de fichas do jogo |
| `visivelParaJogadores = true`, sem acesso granular | Vê o NPC listado, mas sem acesso à tela de detalhe com stats |
| `visivelParaJogadores = true`, com acesso granular | Vê o NPC listado E pode abrir a ficha com todos os stats |

> **Nota de implementação**: O controle granular pode exigir ajuste no contrato de API — o backend precisará filtrar os dados do NPC na resposta com base no ID do jogador solicitante. Registrar como dívida técnica se o backend ainda não suportar isso.

### Lista de Fichas do Jogo (perspectiva do Jogador)

```html
<!-- Card de NPC visível para o Jogador -->
<p-card styleClass="npc-card">
  <ng-template #header>
    <div class="flex justify-between items-center p-3">
      <p-tag value="NPC" severity="warn" icon="pi pi-robot" />
      <!-- Badge indicando que o jogador tem acesso aos stats -->
      @if (npc.jogadorTemAcessoStats) {
        <p-tag value="Aliado" severity="success" icon="pi pi-star"
               pTooltip="Você tem acesso completo às estatísticas deste personagem"
               tooltipPosition="top" />
      }
    </div>
  </ng-template>
  <div class="font-semibold">{{ npc.nome }}</div>
  <div class="text-sm text-color-secondary">{{ npc.racaNome }} • {{ npc.classeNome }}</div>

  @if (npc.jogadorTemAcessoStats) {
    <p-button label="Ver ficha completa" icon="pi pi-eye"
              styleClass="w-full mt-3"
              (onClick)="abrirFicha(npc.id)"
              aria-label="Ver ficha completa de {{ npc.nome }}" />
  } @else {
    <div class="flex items-center gap-2 mt-3 text-color-secondary text-sm">
      <i class="pi pi-lock"></i>
      <span>Estatísticas ocultadas pelo Mestre</span>
    </div>
  }
</p-card>
```

---

## 8. Badge de Visibilidade no Header do NPC (tela do Mestre)

No `FichaHeaderComponent`, quando a ficha é NPC, exibir indicador de status de visibilidade:

```html
<!-- Dentro de FichaHeaderComponent, abaixo do badge [NPC] -->
@if (ficha().isNpc && isMestre()) {
  <div class="flex items-center gap-2 mt-1">
    @if (ficha().visivelParaJogadores) {
      <p-tag severity="success" icon="pi pi-eye" styleClass="text-xs">
        <ng-template #icon><i class="pi pi-eye mr-1 text-xs"></i></ng-template>
        Visível para {{ jogadoresComAcesso().length }} jogador(es)
      </p-tag>
    } @else {
      <p-tag severity="secondary" icon="pi pi-eye-slash" styleClass="text-xs">
        <ng-template #icon><i class="pi pi-eye-slash mr-1 text-xs"></i></ng-template>
        Oculto para jogadores
      </p-tag>
    }
  </div>
}
```

---

## 9. API Contract — Visibilidade

### Endpoints envolvidos

| Método | Endpoint | Payload | Quem usa |
|--------|----------|---------|---------|
| `PATCH` | `/api/v1/fichas/{id}/visibilidade` | `{ visivelParaJogadores, jogadoresComAcesso: number[] }` | MESTRE |
| `GET` | `/api/v1/fichas/{id}` | (resposta inclui `visivelParaJogadores`, `jogadoresComAcesso`) | MESTRE, JOGADOR |

> **Nota**: O endpoint `PATCH /fichas/{id}/visibilidade` pode não existir ainda — verificar contrato atual. Se o backend usa `PUT /fichas/{id}` genérico, o frontend pode enviar apenas os campos de visibilidade. O campo `jogadoresComAcesso` como array de IDs de jogadores pode exigir uma nova tabela de relacionamento no backend (`NpcVisibilidadeJogador`).

---

## 10. Estados da UI

### Salvando visibilidade

```html
<!-- Botão com loading state -->
<p-button label="Salvando..." icon="pi pi-spin pi-spinner"
          [loading]="salvando()"
          [disabled]="true" />
```

### Sucesso ao salvar

Toast + atualização otimista do estado local:

```typescript
this.toastService.success(
  'Visibilidade atualizada',
  `Configurações de visibilidade de ${this.ficha().nome} foram salvas.`
);
// Atualizar o signal de jogadoresComAcesso localmente sem novo request
```

### Sem participantes aprovados no jogo

```html
@if (participantesAprovados().length === 0) {
  <p-message severity="info">
    <ng-template #messageicon><i class="pi pi-info-circle mr-2"></i></ng-template>
    Nenhum jogador aprovado neste jogo ainda.
    Aprove participantes para controlar a visibilidade.
  </p-message>
}
```

---

## 11. Componentes PrimeNG Utilizados

| Componente | Módulo | Uso |
|-----------|--------|-----|
| `p-toggleSwitch` | `ToggleSwitchModule` | Flag visível globalmente |
| `p-multiselect` | `MultiSelectModule` | Seleção de jogadores com acesso |
| `p-chip` | `ChipModule` | Display de jogadores selecionados |
| `p-avatar` | `AvatarModule` | Ícone de jogador nas opções do multiselect |
| `p-tag` | `TagModule` | Status de acesso por jogador, badge de visibilidade |
| `p-drawer` | `DrawerModule` | Bottom sheet mobile |
| `p-card` | `CardModule` | Container do painel lateral |
| `p-button` | `ButtonModule` | Salvar visibilidade, abrir drawer |
| `p-divider` | `DividerModule` | Separador entre toggle e seletor |
| `p-message` | `MessageModule` | Aviso de sem participantes |
| `p-toast` | `ToastModule` | Feedback de salvamento |

---

## 12. Acessibilidade

- `p-multiselect`: `aria-label="Selecionar jogadores com acesso às estatísticas do NPC"`
- `p-toggleSwitch`: `aria-label` descritivo + `aria-describedby` apontando para o texto explicativo
- Lista de status de acesso: `role="list"` com cada item como `role="listitem"`
- Badge de visibilidade no header: comunica via texto, não só por cor ("Visível para N jogadores", "Oculto para jogadores")
- `p-drawer` mobile: `aria-label="Painel de visibilidade do NPC"` + foco preso no drawer quando aberto

---

## 13. Estrutura de Arquivos

```
ficha/
  components/
    npc-visibilidade/
      npc-visibilidade.component.ts    [DUMB] — painel de controle de visibilidade
      npc-visibilidade.component.html
```

---

## 14. Checklist de Implementação

- [ ] `NpcVisibilidadeComponent` dumb com inputs: `fichaId`, `jogoId`, `visivelParaJogadores`, `jogadoresComAcesso`
- [ ] Toggle "Visível globalmente" com `p-toggleSwitch`
- [ ] `p-multiselect` de participantes aprovados com template customizado (avatar + nome do personagem)
- [ ] Lista de status de acesso (Vê stats / Não vê) por participante
- [ ] Botão "Salvar visibilidade" com loading state e disabled quando sem alterações
- [ ] Layout desktop: segunda coluna no `FichaDetailPage` quando `isNpc + isMestre`
- [ ] Botão na toolbar mobile com badge de contagem de jogadores com acesso
- [ ] `p-drawer` bottom mobile para o painel de visibilidade
- [ ] Badge de visibilidade no `FichaHeaderComponent`
- [ ] Perspectiva do Jogador: card de NPC com badge "Aliado" e botão "Ver ficha completa"
- [ ] Card de NPC sem acesso: cadeado + "Estatísticas ocultadas pelo Mestre"
- [ ] Toast de sucesso/erro ao salvar visibilidade
- [ ] Empty state "Sem participantes aprovados"
- [ ] Verificar se endpoint `PATCH /fichas/{id}/visibilidade` existe ou requer criação no backend
