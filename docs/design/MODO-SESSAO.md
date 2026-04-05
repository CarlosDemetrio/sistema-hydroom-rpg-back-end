# Design Spec: Modo Sessao

> Documento de design UI/UX para a visao de jogo ativo — painel de sessao do Mestre e visao focada do Jogador.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versao: 1.0 | Gerado em: 2026-04-02
> Dependencias: FICHA-DETAIL-DESIGN.md, PROSPECCAO-SESSAO.md, RESET-ESTADO-MESTRE.md

---

## 1. O que e o Modo Sessao

O modo sessao e a experiencia de uso **durante uma partida ativa**. Ele nao e um estado separado do sistema — e uma visao otimizada do FichaDetail (para o Jogador) e uma nova tela de painel centralizado (para o Mestre).

**Diferencas chave em relacao a visualizacao normal:**

| Aspecto | Visualizacao Normal | Modo Sessao |
|---------|---------------------|-------------|
| Foco | Todos os dados da ficha | Dados de combate above the fold |
| Abas | Resumo, Atributos, Aptidoes, Vantagens, Anotacoes | Combate, Prospeccao, Bonus — reorganizadas |
| Acoes rapidas | Nao ha | Botoes de gasto de essencia, uso de prospeccao prominentes |
| Dados de combate | Dentro da aba Resumo | Above the fold, sempre visiveis |
| Mestre | Acessa cada ficha individualmente | Painel centralizado com todas as fichas |

**Rota**:
- Jogador: `/fichas/:id` em "modo sessao" (mesma rota, estado controlado por flag ou query param `?sessao=true`)
- Mestre: `/jogos/:jogoId/sessao` — nova rota com painel centralizado

---

## 2. Modo Sessao para o JOGADOR

### 2.1 O que muda no FichaDetail

O modo sessao para o Jogador e uma **visao reorganizada** do FichaDetail. As mudancas:

1. **Dados de combate acima da dobra**: vida, essencia, prospeccao, BBA, BBM, RD, RDM, Impeto vistos sem scroll
2. **Botoes de acao rapida** na barra de stats: gastar essencia, registrar dano, usar dado de prospeccao
3. **Barra de stats compacta e grande** — leitura rapida na mesa, fontes maiores
4. **Abas reorganizadas**: "Combate" como primeira aba (antes de "Resumo" padrao)

### 2.2 Wireframe Mobile — Modo Sessao do Jogador (< 768px)

```
┌─────────────────────────────┐
│ [<] Fichas   Aldric  [Sessao ON]  │  ← badge/chip indicando modo sessao
├─────────────────────────────┤
│                             │
│  VIDA                       │
│  [████████████████░░] 25/30 │  ← barra grande (height: 20px)
│  ─────────────────          │
│  ESSENCIA                   │
│  [████████████░░░░░] 12/20  │
│                             │
│  ┌────────┬────────┐        │
│  │ [-1]   │  [+1]  │        │  ← botoes de gasto/cura rapida
│  │Essencia│Essencia│        │     touch target 56px altura
│  └────────┴────────┘        │
│                             │
├─────────────────────────────┤
│  BBA  BBM  RD   Impeto      │  ← dados de combate em row de 4
│  +12  +8   5    Furacão     │     fonte mono, peso 700
├─────────────────────────────┤
│  [Combate][Resumo][Prosp.]  │  ← abas com Combate como primeira
├─────────────────────────────┤
│  [Conteudo da aba ativa]    │
└─────────────────────────────┘
```

### 2.3 Wireframe Desktop — Modo Sessao do Jogador

```
┌──────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] Fichas   Aldric, Filho da Névoa   [Sessao ATIVA]         │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  [FICHA-STATS-COMBATE — sticky, novo componente]                │    │
│  │                                                                 │    │
│  │  VIDA [████████████████░░░░] 25/30                              │    │
│  │  ESSENCIA [████████████░░░░░░░░] 12/20                          │    │
│  │  Ameaca: 16                                                     │    │
│  │                                                                 │    │
│  │  [Gastar Essencia -1]  [Gastar Essencia -5]  [Curar +1] [+5]   │    │
│  │                                                                 │    │
│  │  BBA: +12  BBM: +8  RD: 5  RDM: 2  Impeto: Furacão            │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  [p-tabs] Combate | Prospecção | Resumo | Atributos | ...       │    │
│  ├─────────────────────────────────────────────────────────────────┤    │
│  │  [Conteudo da aba ativa]                                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.4 Componente: FichaStatsCombateComponent

**Arquivo**: `ficha/components/ficha-stats-combate/ficha-stats-combate.component.ts`
**Tipo**: Dumb
**Renderizado quando**: `modoSessao() === true`

```typescript
resumo = input.required<FichaResumo>();
essenciaAtual = input.required<number>();
vidaAtual = input.required<number>();
modoSessao = input<boolean>(false);

gastarEssencia = output<number>();  // quantidade a gastar
curarEssencia = output<number>();
registrarDano = output<number>();
curarVida = output<number>();
```

```html
<div class="ficha-stats-combate" role="region" aria-label="Status de combate">

  <!-- Barras de recursos (mais grossas em modo sessao) -->
  <div class="flex flex-col gap-3 mb-4">

    <!-- Vida -->
    <div class="flex flex-col gap-1">
      <div class="flex justify-between items-center">
        <span class="font-bold text-sm uppercase tracking-wider">Vida</span>
        <span class="font-mono font-bold text-lg"
              aria-label="Vida: {{ vidaAtual() }} de {{ resumo().vidaTotal }}">
          {{ vidaAtual() }} / {{ resumo().vidaTotal }}
        </span>
      </div>
      <p-progressBar
        [value]="(vidaAtual() / resumo().vidaTotal) * 100"
        [showValue]="false"
        styleClass="sessao-vida-bar"
        [class.bar-danger]="vidaAtual() / resumo().vidaTotal < 0.25"
        [class.bar-warn]="vidaAtual() / resumo().vidaTotal >= 0.25 && vidaAtual() / resumo().vidaTotal < 0.5"
        aria-label="Vida: {{ vidaAtual() }} de {{ resumo().vidaTotal }}" />
    </div>

    <!-- Essencia -->
    <div class="flex flex-col gap-1">
      <div class="flex justify-between items-center">
        <span class="font-bold text-sm uppercase tracking-wider">Essência</span>
        <span class="font-mono font-bold text-lg"
              aria-label="Essencia: {{ essenciaAtual() }} de {{ resumo().essenciaTotal }}">
          {{ essenciaAtual() }} / {{ resumo().essenciaTotal }}
        </span>
      </div>
      <p-progressBar
        [value]="(essenciaAtual() / resumo().essenciaTotal) * 100"
        [showValue]="false"
        styleClass="sessao-essencia-bar"
        [class.bar-danger]="essenciaAtual() / resumo().essenciaTotal < 0.25"
        aria-label="Essencia: {{ essenciaAtual() }} de {{ resumo().essenciaTotal }}" />
    </div>
  </div>

  <!-- Botoes de acao rapida de essencia -->
  <div class="flex gap-2 mb-4 flex-wrap" role="group" aria-label="Acoes de essencia">
    <p-button label="-1 Essência" icon="pi pi-minus" size="small" severity="secondary" outlined
              (onClick)="gastarEssencia.emit(1)"
              aria-label="Gastar 1 ponto de essencia" />
    <p-button label="-5 Essência" icon="pi pi-minus-circle" size="small" severity="secondary" outlined
              (onClick)="gastarEssencia.emit(5)"
              aria-label="Gastar 5 pontos de essencia" />
    <p-button label="+1" icon="pi pi-plus" size="small" severity="success" text
              (onClick)="curarEssencia.emit(1)"
              aria-label="Recuperar 1 ponto de essencia" />
  </div>

  <!-- Dados de combate em grid -->
  <div class="grid grid-cols-3 sm:grid-cols-5 gap-3">
    @for (stat of statsCombate(); track stat.sigla) {
      <div class="flex flex-col items-center gap-0.5 p-2 bg-surface-50 rounded-lg"
           [attr.aria-label]="stat.nome + ': ' + stat.valor">
        <span class="text-xs text-color-secondary font-semibold uppercase tracking-wide">
          {{ stat.sigla }}
        </span>
        <span class="font-mono font-bold text-xl">{{ stat.valor }}</span>
        <span class="text-xs text-color-secondary hidden sm:block">{{ stat.nome }}</span>
      </div>
    }
  </div>
</div>
```

```css
/* Barras de recursos com altura generosa em modo sessao */
:host .sessao-vida-bar :deep(.p-progressbar-value) {
  background: var(--green-500);
  height: 20px;
  border-radius: 4px;
  transition: width 300ms ease;
}

:host .sessao-essencia-bar :deep(.p-progressbar-value) {
  background: var(--blue-500);
  height: 20px;
  border-radius: 4px;
}

:host .bar-danger :deep(.p-progressbar-value) { background: var(--red-500); }
:host .bar-warn :deep(.p-progressbar-value) { background: var(--orange-500); }
```

---

## 3. Modo Sessao para o MESTRE — Painel Centralizado

### 3.1 Visao Geral do Painel do Mestre

O Mestre gerencia a sessao a partir de uma tela centralizada que agrega todas as fichas do jogo em uma unica visao. Ele NAO precisa abrir cada ficha individualmente para:

- Ver status de vida/essencia de todos os personagens
- Confirmar usos de prospeccao pendentes
- Conceder XP em lote ou individualmente
- Fazer resets de vida no inicio da sessao

**Rota**: `/jogos/:jogoId/sessao`
**Componente Smart**: `PainelSessaoComponent`

### 3.2 Wireframe Desktop — Painel do Mestre

```
┌──────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] Jogo: Klayrah   SESSAO ATIVA   [Resetar Vida de Todos]  │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ [p-toolbar] Fichas dos Jogadores          [Conceder XP em Lote] │    │
│  ├───────────────────────┬───────────────────────┬─────────────────┤    │
│  │ Aldric, Nv. 5         │ Lyra, Nv. 4           │ Thorin, Nv. 5   │    │
│  │ Humano • Guerreiro    │ Elfa • Arqueira        │ Anao • Ferreiro │    │
│  │ Vida [██████░] 25/30  │ Vida [████░░] 18/28    │ Vida [██████] 30/30│ │
│  │ Ess. [████░░] 10/16   │ Ess. [██░░░░] 5/14     │ Ess. [███░░░] 9/15│ │
│  │ BBA: +12  Impeto: F.  │ BBA: +9   Impeto: E.  │ BBA: +14  Imp: U.│  │
│  │                       │ [!] Prosp. pendente   │                 │    │
│  │ [XP] [Reset]          │ [XP] [Reset] [Prosp.] │ [XP] [Reset]   │    │
│  └───────────────────────┴───────────────────────┴─────────────────┘    │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ [ProspeccaoPainelMestreComponent]                               │    │
│  │ Prospecção Pendente [badge: 1]                                  │    │
│  │ Lyra Sombria    d10   ha 2 min    [Confirmar] [Reverter]        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ NPCs na Sessao (apenas NPCs com visivelParaJogadores = true)    │    │
│  │ Barahir, o Corvo Negro   Nv. 8   Vida [████████░░] 40/50       │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.3 Wireframe Mobile — Painel do Mestre

Em mobile, os cards de fichas ficam em carousel horizontal ou accordion:

```
┌─────────────────────────────┐
│ [<] Sessao: Klayrah         │  ← p-toolbar
│ [Resetar Todos] [Prosp. !1] │
├─────────────────────────────┤
│ ← Aldric → Lyra → Thorin → │  ← p-carousel ou scroll horizontal
│ ┌───────────────────────┐   │
│ │ Aldric, Nv. 5         │   │
│ │ Vida [████████░] 25/30│   │
│ │ Ess. [█████░░] 10/16  │   │
│ │ BBA:+12  Impeto:Furac.│   │
│ │ [XP] [Reset]          │   │
│ └───────────────────────┘   │
├─────────────────────────────┤
│ Prosp. Pendente [!1]        │  ← secao colapsavel
│ > Lyra d10 ha 2min [OK][X]  │
├─────────────────────────────┤
│ NPCs Ativos [v]             │  ← secao colapsavel
└─────────────────────────────┘
```

### 3.4 Componente: FichaSessaoCardComponent (dumb)

Card de ficha para o painel do Mestre. Mostra status resumido e acoes rapidas.

```typescript
ficha = input.required<Ficha>();
resumo = input.required<FichaResumo>();
vidaAtual = input.required<number>();
essenciaAtual = input.required<number>();
temProscaoPendente = input<boolean>(false);

concederXpClick = output<void>();
resetarVidaClick = output<void>();
abrirFichaClick = output<void>();
```

```html
<p-card styleClass="ficha-sessao-card"
        [style]="{ 'border-top': '3px solid ' + corNivel() }">
  <ng-template #header>
    <div class="flex justify-between items-center px-4 pt-3">
      <div class="flex flex-col gap-0.5">
        <span class="font-bold">{{ ficha().nome }}</span>
        <span class="text-xs text-color-secondary">
          {{ ficha().racaNome }} • {{ ficha().classeNome }} • Nv. {{ ficha().nivel }}
        </span>
      </div>
      <div class="flex items-center gap-1">
        @if (temProspeccaoPendente()) {
          <p-badge value="!" severity="warn"
                   pTooltip="Prospecção pendente"
                   aria-label="Prospecção pendente de confirmacao" />
        }
        <p-button icon="pi pi-external-link" text rounded size="small"
                  (onClick)="abrirFichaClick.emit()"
                  pTooltip="Abrir ficha completa"
                  aria-label="Abrir ficha completa de {{ ficha().nome }}" />
      </div>
    </div>
  </ng-template>

  <!-- Barras de recursos compactas -->
  <div class="flex flex-col gap-2 px-4 pb-2">
    <div class="flex items-center gap-2">
      <span class="text-xs font-semibold w-12">Vida</span>
      <p-progressBar [value]="(vidaAtual() / resumo().vidaTotal) * 100"
                     [showValue]="false"
                     styleClass="flex-1 h-2"
                     aria-label="Vida: {{ vidaAtual() }}/{{ resumo().vidaTotal }}" />
      <span class="font-mono text-xs font-bold min-w-12 text-right">
        {{ vidaAtual() }}/{{ resumo().vidaTotal }}
      </span>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-xs font-semibold w-12">Ess.</span>
      <p-progressBar [value]="(essenciaAtual() / resumo().essenciaTotal) * 100"
                     [showValue]="false"
                     styleClass="flex-1 h-2 essencia-bar"
                     aria-label="Essencia: {{ essenciaAtual() }}/{{ resumo().essenciaTotal }}" />
      <span class="font-mono text-xs font-bold min-w-12 text-right">
        {{ essenciaAtual() }}/{{ resumo().essenciaTotal }}
      </span>
    </div>
  </div>

  <!-- Stats de combate em linha compacta -->
  <div class="flex gap-3 px-4 pb-3 text-xs">
    <span>BBA: <strong>{{ resumo().bonusTotais['BBA'] }}</strong></span>
    <span>Ímpeto: <strong>{{ resumo().impetoNome }}</strong></span>
  </div>

  <ng-template #footer>
    <div class="flex gap-2">
      <p-button label="XP" icon="pi pi-star" size="small" severity="success" outlined
                styleClass="flex-1"
                (onClick)="concederXpClick.emit()"
                aria-label="Conceder XP para {{ ficha().nome }}" />
      <p-button icon="pi pi-refresh" size="small" severity="secondary" outlined
                (onClick)="resetarVidaClick.emit()"
                pTooltip="Resetar vida"
                aria-label="Resetar vida de {{ ficha().nome }}" />
    </div>
  </ng-template>
</p-card>
```

---

## 4. Botao de Reset em Lote na Toolbar do Painel

```html
<p-button
  label="Resetar Vida de Todos"
  icon="pi pi-refresh"
  severity="secondary"
  outlined
  (onClick)="confirmarResetEmLote()"
  aria-label="Resetar vida de todos os personagens do jogo" />
```

Ao clicar, dispara `p-confirmDialog`:

```
"A vida de TODOS os [N] personagens sera zerada de volta ao total.
 Ideal para fazer no inicio de uma nova sessao.
 Esta acao nao pode ser desfeita."
```

Endpoint: `POST /api/v1/jogos/:jogoId/reset/vida-todos`

---

## 5. Diferenca de Visao: MESTRE vs JOGADOR no Modo Sessao

| Elemento | Mestre | Jogador |
|----------|--------|---------|
| Painel centralizado com todas as fichas | SIM (nova rota `/sessao`) | NAO |
| Botoes de gasto de essencia na propria ficha | Pode gastar pela ficha do personagem | SIM, na propria ficha |
| Confirmar uso de prospeccao | SIM | NAO |
| Conceder XP inline | SIM (botao no card e na ficha) | NAO |
| Resetar vida | SIM (individual e em lote) | NAO |
| Dados de combate acima da dobra | SIM (no painel e na ficha individual) | SIM (na ficha individual) |
| Badge "Sessao Ativa" | NAO (e o Mestre, nao faz sentido) | SIM — badge discreto na toolbar |

---

## 6. Ativacao do Modo Sessao

O "modo sessao" para o Jogador e ativado via query param na rota da ficha:

```
/fichas/:id?mode=sessao
```

Ou via botao "Entrar em modo sessao" no FichaHeaderComponent:

```html
<p-toggleButton
  [(ngModel)]="modoSessao"
  onLabel="Modo Sessão ON"
  offLabel="Modo Sessão"
  onIcon="pi pi-bolt"
  offIcon="pi pi-bolt"
  styleClass="p-button-sm"
  aria-label="Ativar ou desativar modo sessao"
  (onChange)="onModoSessaoChange($event)" />
```

Ao ativar o modo sessao, a URL e atualizada via `router.navigate` com `queryParams: { mode: 'sessao' }` sem recarregar o componente. O estado persiste no `localStorage` para que o Jogador nao precise ativar toda vez.

---

## 7. Dados de Combate: Mapeamento dos Bonus

Os dados de combate exibidos no modo sessao sao derivados do `FichaResumoResponse.bonusTotais`:

| Sigla UI | Campo no bonusTotais | Descricao |
|----------|---------------------|-----------|
| BBA | `bonusTotais['BBA']` | Bonus de Batalha de Ataque |
| BBM | `bonusTotais['BBM']` | Bonus de Batalha de Magia |
| RD | `bonusTotais['RD']` | Reducao de Dano fisico |
| RDM | `bonusTotais['RDM']` | Reducao de Dano Magico |
| Impeto | `atributosTotais.impeto` (campo especial) | Nome do nivel de impeto calculado pelo FormulaEvaluatorService |

> Nota: Os nomes das siglas dependem das configuracoes do jogo (BonusConfig). Nao sao hardcoded. O frontend deve exibir todos os bonusTotais como grid dinamico, nao apenas esses 5.

---

## 8. Estados da UI

### Loading (carregando fichas do jogo)

```html
<!-- Skeleton de card de ficha -->
@for (_ of [1, 2, 3]; track $index) {
  <div class="flex flex-col gap-2 p-4 border border-surface-border rounded-lg">
    <p-skeleton width="60%" height="1.2rem" />
    <p-skeleton width="40%" height="0.875rem" />
    <p-skeleton width="100%" height="0.75rem" />
    <p-skeleton width="100%" height="0.75rem" />
  </div>
}
```

### Sem fichas no jogo

```html
<div class="flex flex-col items-center py-12 gap-4 text-center">
  <i class="pi pi-users" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
  <p class="text-color-secondary">
    Nenhuma ficha de jogador neste jogo ainda.
    Crie fichas ou aguarde os jogadores entrarem.
  </p>
  <p-button label="Criar ficha" icon="pi pi-plus" routerLink="/fichas/nova"
            aria-label="Criar nova ficha de personagem" />
</div>
```

### Erro de carregamento

```html
<p-message severity="error" class="w-full">
  <ng-template #messageicon><i class="pi pi-exclamation-circle mr-2"></i></ng-template>
  Erro ao carregar fichas da sessao. Verifique a conexao e tente novamente.
  <p-button label="Tentar novamente" text size="small" (onClick)="recarregar()"
            styleClass="ml-2" aria-label="Tentar recarregar fichas" />
</p-message>
```

---

## 9. Componentes PrimeNG Utilizados

| Componente | Modulo | Uso |
|-----------|--------|-----|
| `p-progressBar` | `ProgressBarModule` | Barras de vida/essencia (modo grosso no modo sessao) |
| `p-card` | `CardModule` | Card de ficha no painel do Mestre |
| `p-button` | `ButtonModule` | Acoes rapidas (XP, reset, gastar essencia) |
| `p-badge` | `BadgeModule` | Indicador de prospeccao pendente, badge "Sessao Ativa" |
| `p-carousel` | `CarouselModule` | Cards de fichas em mobile (scroll horizontal) |
| `p-panel` | `PanelModule` | Secoes colapsaveis no painel mobile (NPCs, prospeccao) |
| `p-toolbar` | `ToolbarModule` | Toolbar do painel de sessao |
| `p-toggleButton` | `ToggleButtonModule` | Ativar/desativar modo sessao no Jogador |
| `p-toast` | `ToastModule` | Feedbacks de acoes (XP concedido, reset executado) |
| `p-confirmDialog` | `ConfirmDialogModule` | Confirmacao de reset em lote |
| `p-skeleton` | `SkeletonModule` | Loading state dos cards de fichas |
| `p-message` | `MessageModule` | Estado de erro de carregamento |

---

## 10. Acessibilidade

- Barras de vida/essencia: `aria-label="Vida: X de Y"` — nao transmite info apenas pela barra
- Botoes de acao rapida: `aria-label` especifico com nome do personagem ("Conceder XP para Aldric")
- Badge de prospeccao pendente: `aria-label="Prospecção pendente de confirmacao"` — nao apenas "!"
- Modo sessao ativo: `aria-live="polite"` para anunciar ativacao/desativacao do modo
- Cards de ficha no carousel: `role="listitem"`, container com `role="list"`
- `p-toggleButton` do modo sessao: estados ON/OFF com label descritivo, nao apenas icone

---

## 11. Estrutura de Arquivos

```
sessao/
  painel-sessao/
    painel-sessao.page.ts               [SMART] — carrega todas as fichas do jogo
    painel-sessao.page.html
    components/
      ficha-sessao-card/
        ficha-sessao-card.component.ts  [DUMB] — card de ficha no painel do Mestre
      sessao-toolbar/
        sessao-toolbar.component.ts     [DUMB] — toolbar com acoes globais

ficha/
  components/
    ficha-stats-combate/
      ficha-stats-combate.component.ts  [DUMB] — barras + acoes rapidas + grid de combat stats
    sessao-toggle/
      sessao-toggle.component.ts        [DUMB] — botao de ativacao do modo sessao
```

---

## 12. API Contract

| Metodo | Endpoint | Uso |
|--------|----------|-----|
| `GET` | `/api/v1/jogos/{jogoId}/fichas` | Listar todas as fichas do jogo (para o painel do Mestre) |
| `GET` | `/api/v1/fichas/{id}/resumo` | Status individual de vida/essencia/bonus |
| `POST` | `/api/v1/fichas/{id}/essencia/gastar` | `{ quantidade }` — Jogador gasta essencia |
| `POST` | `/api/v1/fichas/{id}/essencia/curar` | `{ quantidade }` — Mestre restaura essencia |
| `POST` | `/api/v1/fichas/{id}/xp` | `{ quantidade }` — Mestre concede XP |
| `POST` | `/api/v1/jogos/{jogoId}/reset/vida-todos` | Reset de vida em lote |

---

## 13. Checklist de Implementacao

- [ ] `FichaStatsCombateComponent` com barras de recursos maiores (20px height) no modo sessao
- [ ] Botoes de acao rapida de essencia (+1/-1/-5) com touch target >= 48px em mobile
- [ ] Grid de dados de combate (BBA, BBM, RD, RDM, Impeto) dinamico (via bonusTotais)
- [ ] `p-toggleButton` de "Modo Sessao" no FichaHeaderComponent
- [ ] Persistencia do modo sessao no `localStorage` (nao reseta ao recarregar)
- [ ] Query param `?mode=sessao` na URL ao ativar
- [ ] Reorganizacao das abas em modo sessao: Combate como primeira aba
- [ ] Badge "Sessao Ativa" discreto na toolbar do Jogador
- [ ] Rota `/jogos/:jogoId/sessao` para o painel centralizado do Mestre
- [ ] `PainelSessaoComponent` com grid de `FichaSessaoCardComponent`
- [ ] Em mobile: `p-carousel` de cards de fichas
- [ ] Secao de prospeccao pendente (reusa `ProspeccaoPainelMestreComponent` de PROSPECCAO-SESSAO.md)
- [ ] Botao "Resetar Vida de Todos" na toolbar com `p-confirmDialog`
- [ ] Skeleton loading para cards de fichas
- [ ] Estado vazio "sem fichas" com CTA de criacao
- [ ] Estado de erro de carregamento com botao de retry
- [ ] Endpoints `POST /fichas/{id}/essencia/gastar` e `POST /fichas/{id}/essencia/curar` implementados no backend (especificados em Spec 009-ext RF-ESS-005 e RF-ESS-006 — decisao de design 2026-04-03, ver spec.md secao 3.2)
