# Design Spec: JogosDisponiveisComponent

> Documento de design UI/UX para a tela de listagem de jogos disponíveis para o Jogador.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versão: 1.0 | Gerado em: 2026-04-01

---

## 1. Visão Geral e Contexto

A `JogosDisponiveisComponent` é a tela de entrada do fluxo do **Jogador**. Um usuário com role `JOGADOR` que ainda não participa de nenhum jogo precisa solicitar acesso a um jogo existente. Esta tela lista os jogos disponíveis (criados por qualquer Mestre) e permite ao Jogador solicitar participação.

**Rota**: `/jogador/jogos` (ou `/jogos/disponiveis`)
**Role permitido**: `JOGADOR`
**Componente atual**: placeholder vazio — implementar do zero.

### Fluxo principal do Jogador

```
Login → JogosDisponiveisComponent → [solicitar acesso] → aguardar aprovação → JogadorDashboard
```

### API utilizada

| Endpoint | Quando usar |
|----------|------------|
| `GET /api/v1/jogos` | Listar todos os jogos (inclui status de participação do usuário) |
| `POST /api/v1/jogos/{jogoId}/participantes/solicitar` | Solicitar acesso a um jogo |

**Observação sobre o modelo**: A resposta de `GET /api/v1/jogos` retorna `JogoResumo[]` com os campos `id`, `nome`, `descricao`, `totalParticipantes`, `ativo` e `meuRole`. O campo `meuRole` indica se o usuário já tem papel neste jogo. Para saber o status de participação (PENDENTE/APROVADO/REJEITADO/BANIDO), é necessário cruzar com `GET /api/v1/jogos/meus` (que retorna `MeuJogo[]`) ou adicionar campo `statusParticipacao` ao `JogoResumo`. Ver seção 7 para a recomendação.

---

## 2. Wireframe Geral — Desktop

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] Logo Klayrah    [Avatar]  [Perfil] [Sair]                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  h1: Jogos Disponíveis                                           │   │
│  │  p: Encontre um jogo e solicite sua participação ao Mestre.      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─── Filtros ────────────────────────────────────────────────────┐    │
│  │  [p-iconField] Buscar jogo...   [p-toggleButton Apenas Ativos] │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐       │
│  │ [p-card JOGO 1]  │ │ [p-card JOGO 2]  │ │ [p-card JOGO 3]  │       │
│  │                  │ │                  │ │                  │       │
│  │ Klayrah          │ │ Terra Sombria    │ │ Campanha Beta    │       │
│  │ Mestre: Carlos   │ │ Mestre: Ana      │ │ Mestre: Pedro    │       │
│  │ 4 participantes  │ │ 2 participantes  │ │ 1 participante   │       │
│  │ [ATIVO]          │ │ [INATIVO]        │ │ [ATIVO]          │       │
│  │                  │ │                  │ │                  │       │
│  │ [p-tag APROVADO] │ │ [Solicitar]      │ │ [p-tag PENDENTE] │       │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘       │
│                                                                         │
│  [p-paginator]                                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 3. Wireframe Geral — Mobile (< 768px)

```
┌─────────────────────────────┐
│ [p-toolbar] Klayrah [Avatar]│
├─────────────────────────────┤
│  Jogos Disponíveis          │
│  Encontre um jogo...        │
├─────────────────────────────┤
│  [Buscar jogo...       🔍]  │
│  [toggle Apenas Ativos]     │
├─────────────────────────────┤
│  ┌────────────────────────┐ │
│  │ Klayrah                │ │
│  │ Mestre: Carlos         │ │
│  │ 4 participantes [ATIVO]│ │
│  │ [APROVADO — jogar]     │ │
│  └────────────────────────┘ │
│  ┌────────────────────────┐ │
│  │ Terra Sombria          │ │
│  │ Mestre: Ana            │ │
│  │ 2 partic. [INATIVO]    │ │
│  │ [Solicitar Acesso]     │ │
│  └────────────────────────┘ │
│  [Ver mais 3 jogos]         │
└─────────────────────────────┘
```

No mobile: cards empilhados verticalmente, 1 coluna. Botão "Ver mais" em vez de paginator.

---

## 4. Componente: JogosDisponiveisComponent (Smart)

**Arquivo**: `jogos-disponiveis.component.ts`
**Responsabilidades**:
- Carregar lista de jogos via `JogosApiService.listar()`
- Cruzar com status de participação do usuário atual
- Filtrar por busca e por status ativo/inativo
- Orquestrar ação de solicitar participação

### Injeções

```typescript
private jogosApiService = inject(JogosApiService);
private participantesApiService = inject(ParticipantesApiService);
private authService = inject(AuthService);
private toastService = inject(ToastService);
private router = inject(Router);
```

### Estado interno

```typescript
// Dados
protected jogos = signal<JogoComParticipacao[]>([]);
protected loading = signal(false);
protected loadingSolicitar = signal<number | null>(null); // jogoId sendo processado

// Filtros
protected termoBusca = signal('');
protected apenasAtivos = signal(true);

// Paginação
protected totalRecords = signal(0);
protected first = signal(0);
protected rows = signal(12); // 12 cards por página (4x3 desktop)

// Derived
protected jogosFiltrados = computed(() => {
  const termo = this.termoBusca().toLowerCase();
  return this.jogos().filter(j =>
    (j.nome.toLowerCase().includes(termo) ||
     j.descricao?.toLowerCase().includes(termo)) &&
    (!this.apenasAtivos() || j.ativo)
  );
});

protected totalFiltrados = computed(() => this.jogosFiltrados().length);
```

### Interface JogoComParticipacao

Esta interface enriquece `JogoResumo` com dados de participação. Deve ser construída no frontend cruzando `JogoResumo` com os dados do Jogador:

```typescript
// A ser definida em jogo.model.ts
interface JogoComParticipacao extends JogoResumo {
  // meuRole já existe em JogoResumo: 'MESTRE' | 'JOGADOR'
  // Adicionamos o status de participação
  meuStatus: StatusParticipante | null; // null = não participa / nunca solicitou
  mestreNome: string | null;             // nome do Mestre do jogo (requer campo no backend)
}
```

**Nota importante para o backend**: `JogoResumo` não inclui `mestreNome`. Verificar se `GET /api/v1/jogos` já inclui esse dado ou se é necessário solicitá-lo ao backend. Se não disponível, omitir o campo "Mestre: X" do card por ora.

---

## 5. Componente: JogoCardComponent (Dumb)

**Arquivo**: `components/jogo-card/jogo-card.component.ts`
**Selector**: `app-jogo-card`

### Props

```typescript
jogo = input.required<JogoComParticipacao>();
loadingSolicitar = input<boolean>(false); // loading específico deste card

solicitarAcesso = output<number>(); // emite jogoId
irParaJogo = output<number>();      // emite jogoId (quando já aprovado)
```

### Layout do Card

```
┌──────────────────────────────────────────────────────────┐
│ [p-card]                                                 │
│                                                          │
│  Nome do Jogo                        [ATIVO / INATIVO]  │
│  [font-family: serif, font-size: 1.125rem]               │
│                                                          │
│  Descrição breve do jogo (2 linhas, overflow ellipsis)   │
│  [font-size: 0.875rem, text-color-secondary]             │
│                                                          │
│  ─────────────────────────────────────────────           │
│                                                          │
│  [pi pi-users]  4 participantes                          │
│  [pi pi-user]   Mestre: Carlos (se disponível)          │
│                                                          │
│  ─────────────────────────────────────────────           │
│                                                          │
│  [STATUS DO USUÁRIO — vide seção 6]                      │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### Especificações de Card

- `p-card styleClass="jogo-card h-full"` — altura uniforme no grid.
- Layout interno: `flex flex-col justify-between h-full` para empurrar o status/botão ao rodapé.
- Nome: `font-family: Georgia, serif`, `font-size: 1.125rem` (18px), `font-weight: 700`.
- Descrição: 2 linhas máximo via CSS `-webkit-line-clamp: 2`. Se ausente, exibir em itálico: *"Sem descrição disponível."*
- Badge ativo/inativo: `p-tag value="Ativo" severity="success"` / `p-tag value="Inativo" severity="secondary"` — posicionado no canto superior direito.
- Participantes e Mestre: `font-size: 0.875rem`, `color: var(--text-color-secondary)`.

---

## 6. Estados de Participação por Card

O rodapé do card muda completamente com base no `meuStatus` do jogador.

### Status: null (nunca solicitou)

```html
<!-- Jogo ativo: mostrar botão de solicitar -->
@if (jogo().ativo) {
  <p-button label="Solicitar Acesso" icon="pi pi-send"
            [loading]="loadingSolicitar()"
            (onClick)="solicitarAcesso.emit(jogo().id)"
            class="w-full" />
} @else {
  <!-- Jogo inativo: informativo -->
  <p-message severity="info" text="Jogo inativo. Aguarde o Mestre reativá-lo." />
}
```

### Status: PENDENTE

```html
<div class="flex flex-col gap-2">
  <p-tag value="Aguardando aprovação" severity="warn" icon="pi pi-clock" />
  <p class="text-color-secondary text-sm m-0">
    Sua solicitação foi enviada. O Mestre precisa aprová-la.
  </p>
</div>
```

### Status: APROVADO

```html
<p-button label="Entrar no Jogo" icon="pi pi-play"
          severity="success"
          (onClick)="irParaJogo.emit(jogo().id)"
          class="w-full" />
```

### Status: REJEITADO

```html
<div class="flex flex-col gap-2">
  <p-tag value="Solicitação rejeitada" severity="danger" icon="pi pi-times-circle" />
  <p-button label="Solicitar novamente" icon="pi pi-send" text size="small"
            [loading]="loadingSolicitar()"
            (onClick)="solicitarAcesso.emit(jogo().id)" />
</div>
```

### Status: BANIDO

```html
<div class="flex items-center gap-2">
  <p-tag value="Banido" severity="danger" icon="pi pi-ban" />
  <span class="text-color-secondary text-sm">Você não pode participar deste jogo.</span>
</div>
```

### Resumo visual dos estados

| Status | Visual | Ação disponível |
|--------|--------|----------------|
| null + jogo ativo | Botão azul "Solicitar Acesso" | Solicitar |
| null + jogo inativo | Mensagem info cinza | Nenhuma |
| PENDENTE | Tag amarelo "Aguardando" | Nenhuma |
| APROVADO | Botão verde "Entrar no Jogo" | Navegar para o jogo |
| REJEITADO | Tag vermelho + botão text "Solicitar novamente" | Solicitar novamente |
| BANIDO | Tag vermelho "Banido" | Nenhuma |

---

## 7. Fluxo: Solicitar Acesso

### No componente pai (JogosDisponiveisComponent)

```typescript
protected async solicitarAcesso(jogoId: number): Promise<void> {
  this.loadingSolicitar.set(jogoId);

  this.participantesApiService.solicitarParticipacao(jogoId).subscribe({
    next: () => {
      // Atualiza o status do card localmente (otimistic update)
      this.jogos.update(jogos =>
        jogos.map(j => j.id === jogoId
          ? { ...j, meuStatus: 'PENDENTE' as StatusParticipante }
          : j
        )
      );
      this.toastService.success(
        'Solicitação enviada!',
        'Aguarde o Mestre aprovar sua participação.'
      );
      this.loadingSolicitar.set(null);
    },
    error: (err) => {
      const msg = err.status === 409
        ? 'Você já tem uma solicitação pendente para este jogo.'
        : 'Erro ao solicitar participação. Tente novamente.';
      this.toastService.error('Erro', msg);
      this.loadingSolicitar.set(null);
    }
  });
}
```

### Otimistic Update

Ao solicitar, atualizar imediatamente o status do card para `PENDENTE` sem esperar nova chamada de lista. Isso garante feedback imediato ao usuário sem latência.

### Após aprovação (fluxo assíncrono)

A aprovação é feita pelo Mestre em sua interface. O Jogador terá de recarregar a página para ver o status `APROVADO`. Considerar na Fase 2: WebSocket ou polling a cada 30s para atualizar status.

---

## 8. Filtros e Busca

### p-iconField com busca

```html
<p-iconfield>
  <p-inputicon class="pi pi-search" />
  <input pInputText type="text"
         [ngModel]="termoBusca()"
         (ngModelChange)="termoBusca.set($event)"
         placeholder="Buscar por nome ou descrição..."
         class="w-full"
         aria-label="Buscar jogos" />
</p-iconfield>
```

### Toggle "Apenas Ativos"

```html
<p-toggleButton
  [ngModel]="apenasAtivos()"
  (ngModelChange)="apenasAtivos.set($event)"
  onLabel="Apenas Ativos"
  offLabel="Todos os Jogos"
  onIcon="pi pi-check-circle"
  offIcon="pi pi-circle" />
```

### Comportamento da busca

- Busca é **local** (filtra o array já carregado), não uma nova chamada API.
- Debounce não necessário pois não há chamada HTTP.
- A busca filtra por `nome` e `descricao` (case insensitive).
- Limpar busca ao desativar filtro "Apenas Ativos" não é necessário — os filtros são independentes.

---

## 9. Paginação e Grid

### Grid responsivo

```html
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
  @for (jogo of jogosPaginados(); track jogo.id) {
    <app-jogo-card
      [jogo]="jogo"
      [loadingSolicitar]="loadingSolicitar() === jogo.id"
      (solicitarAcesso)="solicitarAcesso($event)"
      (irParaJogo)="irParaJogo($event)" />
  }
</div>
```

Breakpoints do grid:
- Mobile (< 640px): 1 coluna
- Small tablet (640px–1024px): 2 colunas
- Desktop (1024px–1280px): 3 colunas
- Wide desktop (> 1280px): 4 colunas

### Paginação (Desktop/Tablet)

```html
<p-paginator
  [rows]="rows()"
  [totalRecords]="totalFiltrados()"
  [first]="first()"
  (onPageChange)="onPageChange($event)"
  [rowsPerPageOptions]="[8, 12, 24]"
  styleClass="mt-4" />
```

- Paginação cliente-side: fatiar `jogosFiltrados()` com `slice(first(), first() + rows())`.
- `jogosPaginados = computed(() => this.jogosFiltrados().slice(this.first(), this.first() + this.rows()))`.

### Mobile: "Ver mais"

Em vez do paginator clássico, mobile usa botão "Ver mais":

```html
@if (isMobile() && temMais()) {
  <div class="flex justify-center mt-4">
    <p-button label="Ver mais {{ jogosRestantes() }} jogos"
              icon="pi pi-chevron-down" text
              (onClick)="carregarMais()" />
  </div>
}
```

```typescript
protected isMobile = signal(window.innerWidth < 768);
protected itemsVisiveis = signal(6);
protected jogosPaginados = computed(() =>
  this.isMobile()
    ? this.jogosFiltrados().slice(0, this.itemsVisiveis())
    : this.jogosFiltrados().slice(this.first(), this.first() + this.rows())
);
protected temMais = computed(() =>
  this.isMobile() && this.itemsVisiveis() < this.jogosFiltrados().length
);
protected jogosRestantes = computed(() =>
  Math.min(6, this.jogosFiltrados().length - this.itemsVisiveis())
);
protected carregarMais(): void {
  this.itemsVisiveis.update(n => n + 6);
}
```

---

## 10. Estados da UI

### Estado: Loading inicial

```html
@if (loading()) {
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
    @for (_ of [1,2,3,4,5,6,7,8]; track $index) {
      <p-card>
        <div class="flex flex-col gap-3">
          <p-skeleton width="70%" height="1.5rem" />
          <p-skeleton width="100%" height="1rem" />
          <p-skeleton width="80%" height="1rem" />
          <p-divider />
          <p-skeleton width="50%" height="0.875rem" />
          <p-skeleton width="40%" height="0.875rem" />
          <p-divider />
          <p-skeleton width="100%" height="2.5rem" borderRadius="6px" />
        </div>
      </p-card>
    }
  </div>
}
```

8 cards skeleton, mesmo número que a primeira página.

### Estado: Nenhum jogo disponível (lista vazia do servidor)

```html
@if (!loading() && jogos().length === 0) {
  <div class="flex flex-col items-center justify-center py-16 gap-4 text-center">
    <i class="pi pi-compass" style="font-size: 4rem; color: var(--text-color-secondary)"></i>
    <h2 class="text-xl font-semibold m-0">Nenhum jogo disponível</h2>
    <p class="text-color-secondary m-0 max-w-sm">
      Ainda não há jogos cadastrados. Peça ao seu Mestre para criar um jogo
      e compartilhar o código de convite com você.
    </p>
    <p-button label="Atualizar lista" icon="pi pi-refresh" outlined
              (onClick)="carregarJogos()" />
  </div>
}
```

### Estado: Sem resultados na busca

```html
@if (!loading() && jogos().length > 0 && jogosFiltrados().length === 0) {
  <div class="flex flex-col items-center justify-center py-12 gap-3 text-center">
    <i class="pi pi-search" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
    <h3 class="text-lg font-semibold m-0">Nenhum jogo encontrado</h3>
    <p class="text-color-secondary m-0">
      Nenhum jogo corresponde a "{{ termoBusca() }}".
    </p>
    <p-button label="Limpar busca" text icon="pi pi-times"
              (onClick)="termoBusca.set('')" />
  </div>
}
```

### Estado: Erro ao carregar

```html
@if (erro()) {
  <div class="flex flex-col items-center justify-center py-16 gap-4 text-center">
    <i class="pi pi-exclamation-circle text-red-500" style="font-size: 3rem"></i>
    <h2 class="text-xl font-semibold m-0">Erro ao carregar jogos</h2>
    <p class="text-color-secondary m-0">{{ erro() }}</p>
    <p-button label="Tentar novamente" icon="pi pi-refresh" outlined
              (onClick)="carregarJogos()" />
  </div>
}
```

---

## 11. Contextualizando o Jogador: Banner Informativo

Quando o Jogador não possui nenhum jogo com status `APROVADO`, exibir um banner informativo no topo antes do grid, orientando o próximo passo:

```html
@if (!loading() && !temJogoAprovado()) {
  <p-message severity="info" styleClass="mb-4 w-full"
             icon="pi pi-info-circle">
    <ng-template #messageicon>
      <i class="pi pi-info-circle mr-2"></i>
    </ng-template>
    <span>
      Você ainda não participa de nenhum jogo. Solicite acesso a um dos jogos abaixo
      e aguarde a aprovação do Mestre para criar seu personagem.
    </span>
  </p-message>
}
```

```typescript
protected temJogoAprovado = computed(() =>
  this.jogos().some(j => j.meuStatus === 'APROVADO')
);
```

---

## 12. Navegação após Aprovação

Quando o Jogador clica em "Entrar no Jogo" (status APROVADO), navegar para o dashboard do jogador daquele jogo específico:

```typescript
protected irParaJogo(jogoId: number): void {
  this.router.navigate(['/jogador', 'jogos', jogoId, 'fichas']);
}
```

O jogo ativo do Jogador deve ser salvo no `JogosStore` ou `AuthStore` para contexto global.

---

## 13. Componentes PrimeNG Utilizados

| Componente | Módulo | Uso |
|-----------|--------|-----|
| `p-toolbar` | `ToolbarModule` | Header global |
| `p-card` | `CardModule` | Card de cada jogo |
| `p-button` | `ButtonModule` | Ações: solicitar, entrar, limpar busca |
| `p-tag` | `TagModule` | Status ativo/inativo, status de participação |
| `p-badge` | `BadgeModule` | Número de participantes (alternativa) |
| `p-message` | `MessageModule` | Banner informativo + jogo inativo + erro |
| `p-toast` | `ToastModule` | Feedback de solicitar acesso (sucesso/erro) |
| `p-skeleton` | `SkeletonModule` | Loading state dos cards |
| `p-paginator` | `PaginatorModule` | Paginação desktop |
| `p-divider` | `DividerModule` | Separador visual no card |
| `p-iconfield` + `p-inputicon` | `IconFieldModule` | Campo de busca com ícone |
| `pInputText` | `InputTextModule` | Input de busca |
| `p-toggleButton` | `ToggleButtonModule` | Filtro "Apenas Ativos" |

**Imports do componente**:
```typescript
imports: [
  ToolbarModule, CardModule, ButtonModule, TagModule, BadgeModule,
  MessageModule, ToastModule, SkeletonModule, PaginatorModule,
  DividerModule, IconFieldModule, InputIconModule, InputTextModule,
  ToggleButtonModule, FormsModule, RouterModule,
  JogoCardComponent
]
```

---

## 14. Comportamento Responsivo Detalhado

### Desktop (> 1280px)
- Grid 4 colunas.
- Filtros em linha única: [busca — flex 1] [toggle — auto].
- Paginator visível com opções de rows per page.
- Cards com altura uniforme via `align-items: stretch` no grid.

### Tablet (768px–1280px)
- Grid 2–3 colunas.
- Filtros em linha única.
- Paginator simples (sem rows per page).
- Cards com altura uniforme.

### Mobile (< 768px)
- Grid 1 coluna.
- Filtros empilhados: busca em cima, toggle embaixo (full width).
- "Ver mais" em vez do paginator.
- Cards: layout mais compacto, botão de ação sempre visível sem scroll interno.
- Nome do jogo truncado em 1 linha com ellipsis.

---

## 15. Especificações de Espaçamento e Tipografia

### Espaçamentos principais

| Elemento | Valor |
|----------|-------|
| Padding da página | `1.5rem` (24px) desktop / `1rem` (16px) mobile |
| Gap entre cards | `1rem` (16px) |
| Padding interno do card | gerenciado pelo p-card (padrão ~1.25rem) |
| Margem entre filtros e grid | `1.5rem` (24px) |
| Margem entre título e filtros | `1rem` (16px) |

### Tipografia

| Elemento | Tamanho | Peso | Família |
|----------|---------|------|---------|
| Título da página (h1) | 28px | 700 | serif |
| Subtítulo descritivo | 16px | 400 | default |
| Nome do jogo (card) | 18px | 700 | serif |
| Descrição do jogo | 14px | 400 | default |
| Metadados (participantes, mestre) | 14px | 400 | default |
| Labels de status (p-tag) | 12px | 600 | default |

---

## 16. Tokens CSS e Cores

| Uso | Variável/Valor |
|-----|---------------|
| Superfície de card | `var(--surface-card)` |
| Borda de card | `var(--surface-border)` |
| Texto primário | `var(--text-color)` |
| Texto secundário | `var(--text-color-secondary)` |
| Status Ativo | `p-tag severity="success"` → `var(--green-500)` |
| Status Inativo | `p-tag severity="secondary"` → cinza |
| Status APROVADO | `p-button severity="success"` |
| Status PENDENTE | `p-tag severity="warn"` → `var(--yellow-500)` |
| Status REJEITADO | `p-tag severity="danger"` → `var(--red-500)` |
| Status BANIDO | `p-tag severity="danger"` |
| Hover do card | `box-shadow: 0 4px 16px rgba(0,0,0,0.12)` via CSS |

**Efeito hover no card** (adicionar via `:host ::ng-deep` ou custom CSS global):
```css
.jogo-card {
  transition: box-shadow 200ms ease, transform 200ms ease;
  cursor: pointer;
}
.jogo-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}
```

---

## 17. Acessibilidade (WCAG 2.1 AA)

- `p-card` do jogo: adicionar `role="article"` e `aria-label="Jogo: {nome}"`.
- Botão "Solicitar Acesso" com `aria-label="Solicitar acesso ao jogo {nome}"` — específico por jogo.
- Campo de busca: `aria-label="Buscar jogos por nome ou descrição"`.
- `p-toggle-button`: `aria-pressed` gerenciado pelo componente PrimeNG.
- Status de participação comunicado via texto, não apenas cor: tag tem texto descritivo.
- Loading: `aria-busy="true"` no container do grid durante o loading.
- Cards skeleton: `aria-hidden="true"` para não atrapalhar leitores de tela.
- Resultado da busca: `aria-live="polite"` em um `<span>` informando quantos resultados foram encontrados.

```html
<span class="sr-only" aria-live="polite">
  {{ jogosFiltrados().length }} jogo(s) encontrado(s)
</span>
```

---

## 18. Fluxo de Dados Completo

### Carregamento inicial

```typescript
ngOnInit(): void {
  this.loading.set(true);
  this.erro.set(null);

  this.jogosApiService.listar().subscribe({
    next: (jogos) => {
      // Enriquecer com status de participação
      // JogoResumo já inclui meuRole — se o backend incluir meuStatus, usar diretamente
      // Caso contrário, fazer segundo request para /jogos/meus e cruzar
      this.jogos.set(jogos.map(j => ({
        ...j,
        meuStatus: this.inferirStatus(j), // lógica de cruzamento
        mestreNome: null // aguardar campo do backend
      })));
      this.loading.set(false);
    },
    error: (err) => {
      this.erro.set('Não foi possível carregar os jogos. Verifique sua conexão.');
      this.loading.set(false);
    }
  });
}

private inferirStatus(jogo: JogoResumo): StatusParticipante | null {
  // Se meuRole existir no JogoResumo, o usuário já participa com status APROVADO
  // A API atual não retorna statusParticipante no JogoResumo — isso é uma limitação
  // Temporariamente: se meuRole === 'JOGADOR', assumir APROVADO
  if (jogo.meuRole === 'JOGADOR') return 'APROVADO';
  if (jogo.meuRole === 'MESTRE') return 'APROVADO'; // Mestre é sempre aprovado
  return null;
}
```

**Limitação conhecida do contrato de API atual**: `GET /api/v1/jogos` retorna `meuRole` mas não `statusParticipacao` (PENDENTE, REJEITADO, BANIDO). Para exibir corretamente os status PENDENTE/REJEITADO/BANIDO, o backend precisaria incluir esse campo no `JogoResumo`. Registrar como dívida técnica / request ao backend team.

---

## 19. Estrutura de Arquivos

```
jogos-disponiveis/
  jogos-disponiveis.component.ts   [SMART] — página principal
  components/
    jogo-card/
      jogo-card.component.ts       [DUMB] — card individual de jogo
```

---

## 20. Checklist de Implementação

- [ ] Interface `JogoComParticipacao` criada em `jogo.model.ts` (estende `JogoResumo` com `meuStatus`)
- [ ] `JogosDisponiveisComponent` carregando lista via `JogosApiService.listar()`
- [ ] Grid responsivo 1/2/3/4 colunas por breakpoint
- [ ] `JogoCardComponent` com todos os 6 estados de participação (null, PENDENTE, APROVADO, REJEITADO, BANIDO, inativo)
- [ ] Campo de busca com `p-iconField` filtrando localmente
- [ ] Toggle "Apenas Ativos" funcional
- [ ] Paginação desktop via `p-paginator`
- [ ] "Ver mais" mobile
- [ ] Ação "Solicitar Acesso" com optimistic update para PENDENTE
- [ ] Toast de sucesso/erro para solicitar acesso
- [ ] Loading skeleton de 8 cards
- [ ] Empty state "Nenhum jogo disponível" (lista vazia do servidor)
- [ ] Empty state "Sem resultados" (filtro sem correspondência)
- [ ] Empty state "Erro" com botão tentar novamente
- [ ] Banner informativo para jogador sem jogo aprovado
- [ ] Navegação para jogo aprovado (`/jogador/jogos/{id}/fichas`)
- [ ] Efeito hover nos cards
- [ ] `aria-label` específicos por card, campo de busca e botões de ação
- [ ] `aria-live` para resultados de busca
- [ ] Limitação do `statusParticipacao` documentada como dívida técnica para o backend
