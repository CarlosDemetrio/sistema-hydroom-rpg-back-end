# Design Spec: FichaDetailPage

> Documento de design UI/UX para a tela de visualização de ficha de personagem.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versão: 1.0 | Gerado em: 2026-04-01
> Tasks relacionadas: SP1-T09, SP1-T10, SP1-T13, SP1-T17

---

## 1. Visão Geral e Contexto

A `FichaDetailPage` é a tela mais importante do produto para o **Jogador** (usa em toda sessão de jogo) e também crítica para o **Mestre** (monitora personagens, concede XP, edita NPCs). É o centro de consulta de todos os dados calculados de um personagem.

**Rota**: `/fichas/:id`
**Componente smart**: `FichaDetailComponent` (`ficha-detail.component.ts`) — já existe como placeholder, precisa ser totalmente implementado.
**Role atual do usuário**: determinado via `AuthService.currentUser()` signal — campo `role`.

---

## 2. Wireframe Geral — Desktop

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] Fichas   Nome do Jogo   [Avatar] [Tema] [Sair]          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  [FICHA-HEADER — sticky]                                         │   │
│  │  [Avatar inicial]  Aldric, Filho da Névoa          [Nv. 5]       │   │
│  │                    Humano • Guerreiro • Caótico Bom  [NPC] (se)  │   │
│  │                                                                  │   │
│  │  Vida  [████████░░░░] 25/30   Essência [██████░░] 12/20          │   │
│  │        |_p-progressBar_|                                         │   │
│  │  Ameaça: 16 XP: 4.200 / 6.000 [p-progressBar XP]               │   │
│  │                                                                  │   │
│  │  [Editar]  [Duplicar (MESTRE/dono)]  [Deletar (MESTRE)]         │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ [p-tabView]                                                      │   │
│  │  Resumo | Atributos | Aptidoes | Vantagens | Anotacoes           │   │
│  ├──────────────────────────────────────────────────────────────────┤   │
│  │                  [Conteudo da aba ativa]                         │   │
│  │                                                                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 3. Wireframe Geral — Mobile (< 768px)

```
┌─────────────────────────────┐
│ [<] Fichas        [Editar]  │  ← p-toolbar compacto
├─────────────────────────────┤
│ Aldric, Filho da Névoa      │
│ Humano • Guerreiro  [Nv. 5] │
│ [NPC badge se isNpc]        │
├─────────────────────────────┤
│ Vida  [████████░░] 25/30    │
│ Essência [█████░░░] 12/20   │
│ Ameaça: 16                  │
├─────────────────────────────┤
│ [Resumo][Atrib.][Apt.][...] │  ← p-tabView scrollable horizontal
├─────────────────────────────┤
│                             │
│   [Conteudo da aba ativa]   │
│                             │
└─────────────────────────────┘
```

No mobile, o header NÃO é sticky — ocupa espaço estático. As abas ficam em scroll horizontal (overflow-x: auto) sem quebrar em múltiplas linhas.

---

## 4. Componente: FichaDetailPage (Smart)

**Arquivo**: `ficha-detail.component.ts`
**Responsabilidades**:
- Ler `fichaId` da rota: `inject(ActivatedRoute).snapshot.params['id']`
- Carregar `ficha` e `resumo` em paralelo via `forkJoin` no `ngOnInit` / `effect`
- Expor signals para os filhos
- Orquestrar ações: duplicar, deletar, aumentar nível de vantagem

```typescript
// Signals do componente (não @Input — é a raiz da página)
protected ficha = signal<Ficha | null>(null);
protected resumo = signal<FichaResumo | null>(null);
protected atributos = signal<FichaAtributoResponse[]>([]);
protected aptidoes = signal<FichaAptidaoResponse[]>([]);
protected loading = signal(true);
protected erro = signal<string | null>(null);

// Derivados
protected podeEditar = computed(() => {
  const user = this.authService.currentUser();
  const f = this.ficha();
  if (!f || !user) return false;
  return user.role === 'MESTRE' || f.jogadorId === user.id;
});

protected podeDeletar = computed(() =>
  this.authService.currentUser()?.role === 'MESTRE'
);
```

**Imports necessários**:
```typescript
imports: [
  TabsModule, CardModule, ButtonModule, ProgressBarModule,
  TagModule, BadgeModule, AvatarModule, DividerModule,
  SkeletonModule, ToastModule, ConfirmDialogModule,
  DialogModule, InputTextModule,
  FichaHeaderComponent, FichaStatsBarComponent,
  FichaResumoTabComponent, FichaAtributosTabComponent,
  FichaAptidoesTabComponent, FichaVantagensTabComponent,
  FichaAnotacoesTabComponent
]
```

---

## 5. Componente: FichaHeaderComponent (Dumb)

**Arquivo**: `components/ficha-header/ficha-header.component.ts`
**Selector**: `app-ficha-header`

### Props (Angular 21 — nova sintaxe)

```typescript
ficha = input.required<Ficha>();
resumo = input.required<FichaResumo>();
podeEditar = input<boolean>(false);
podeDeletar = input<boolean>(false);
podeDuplicar = input<boolean>(false);

editarClick = output<void>();
deletarClick = output<void>();
duplicarClick = output<void>();
```

### Layout

```
┌─────────────────────────────────────────────────────────────────────┐
│  [p-avatar]  Aldric, Filho da Névoa          [p-tag "Nv. 5" filled] │
│  letra A     Humano • Guerreiro                                      │
│              Índole: Caótico Bom • Presença: Imponente               │
│              [p-tag "NPC" severity=warn] ← apenas se isNpc           │
├─────────────────────────────────────────────────────────────────────┤
│  Vida  [████████████░░░] 25/30   Essência [█████░░░░░] 12/20        │
│  Ameaça: 16    XP: 4.200     [►► XP próx. nível: 6.000]            │
├─────────────────────────────────────────────────────────────────────┤
│  [p-button Editar outlined]  [p-button Duplicar text]               │
│                              [p-button Deletar text severity=danger] │
└─────────────────────────────────────────────────────────────────────┘
```

### Especificações Visuais

- **Avatar**: `p-avatar` com `label` = primeira letra do nome, `size="xlarge"`, `shape="circle"`. Background: `var(--primary-color)`.
- **Nome**: `font-size: 1.5rem` (24px), `font-weight: 700`, `font-family: serif` (estilo RPG).
- **Raça/Classe**: `font-size: 0.875rem` (14px), `color: var(--text-color-secondary)`.
- **Tag NPC**: `p-tag value="NPC" severity="warn"` — visível apenas quando `ficha().isNpc === true`.
- **Tag Nível**: `p-tag value="Nv. {nivel}" severity="info"`.
- **Barras de Vida e Essência**: `p-progressBar` com `value` = porcentagem calculada. Vida: cor `--green-500`; Essência: cor `--blue-400`.
- **Ameaça**: exibida como texto com `p-badge` destacado.
- **Botão Editar**: `p-button label="Editar" icon="pi pi-pencil" outlined`. Visível apenas quando `podeEditar()`.
- **Botão Duplicar**: `p-button label="Duplicar" icon="pi pi-copy" text`. Visível quando `podeDuplicar()`.
- **Botão Deletar**: `p-button label="Deletar" icon="pi pi-trash" text severity="danger"`. Visível quando `podeDeletar()`.

### Acessibilidade

- `p-avatar` deve ter `aria-label="Avatar de {nome}"`.
- Botões de ação com `aria-label` descritivo: `aria-label="Editar ficha de Aldric"`.
- Barras de progresso com `aria-label="Vida: 25 de 30"`.

### Responsive (Mobile)

- Avatar reduz para `size="large"`.
- Nome reduz para `font-size: 1.25rem` (20px).
- Botões ficam em linha única com `justify-content: flex-start`, sem label no Duplicar/Deletar (apenas ícone com tooltip).

---

## 6. Componente: FichaStatsBarComponent (Dumb)

> Nota: Este componente está embutido no `FichaHeaderComponent` conforme wireframe. Pode ser extraído como componente separado se o header ficar muito grande.

### Props

```typescript
vidaTotal = input.required<number>();
vidaAtual = input<number>(0);       // Fase 2: vida atual editável pelo jogador
essenciaTotal = input.required<number>();
essenciaAtual = input<number>(0);   // Fase 2
ameacaTotal = input.required<number>();
xp = input.required<number>();
xpProximoNivel = input<number | null>(null);
```

**Observação sobre Fase 1**: `vidaAtual` e `essenciaAtual` não existem na `Ficha` nem no `FichaResumo` do backend atual. Para o MVP, exibir apenas o total (barra cheia). Preparar o componente para receber esses valores futuramente sem quebrar a interface.

---

## 7. Aba: FichaResumoTabComponent (Dumb)

**Arquivo**: `components/ficha-resumo-tab/ficha-resumo-tab.component.ts`
**Selector**: `app-ficha-resumo-tab`

### Props

```typescript
atributos = input.required<FichaAtributoResponse[]>();
atributosTotais = input.required<Record<string, number>>();
bonusTotais = input.required<Record<string, number>>();
resumo = input.required<FichaResumo>();
membrosCorpo = input<MembroCorpoComVida[]>([]);  // Fase 2
```

### Layout

```
┌─────────────────── ABA RESUMO ──────────────────────────────────────┐
│                                                                     │
│  ATRIBUTOS TOTAIS                                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│  │  FOR     │ │  AGI     │ │  VIG     │ │  SAB     │             │
│  │  [  18  ]│ │  [  14  ]│ │  [  15  ]│ │  [  12  ]│             │
│  │ ímp: 9.0 │ │ ímp: 7.0 │ │ ímp: 7.5 │ │ ímp: 6.0 │             │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘             │
│                                                                     │
│  BÔNUS DERIVADOS                                                    │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐               │
│  │ BBA    [+7]  │ │ BBM    [+4]  │ │ Bloqueio [3] │               │
│  └──────────────┘ └──────────────┘ └──────────────┘               │
│                                                                     │
│  MEMBROS DO CORPO  (Fase 2)                                         │
│  Cabeça [████████░░] 8/10 (33%)                                    │
│  Torso  [████████████░░░] 14/16 (53%)                              │
│  Braços [████░░░░] 5/8 (27%)                                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Seção: Atributos Totais

- Iteração via `@for (atrib of atributos(); track atrib.atributoConfigId)`.
- Cada card: `p-card styleClass="atributo-card"`.
- Abreviação: fonte `font-family: monospace`, `font-size: 0.75rem` (12px), uppercase, `color: var(--text-color-secondary)`.
- Total: fonte `font-family: monospace`, `font-size: 2rem` (32px), `font-weight: 700`, `color: var(--primary-color)`.
- Ímpeto: `font-size: 0.875rem`, `color: var(--text-color-secondary)`, exibido como "ímp: 9.0".
- Grid: `grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3`.

### Seção: Bônus Derivados

- Iteração via `@for (entry of bonusEntries(); track entry.nome)` onde `bonusEntries = computed(() => Object.entries(this.bonusTotais()).map(([nome, valor]) => ({ nome, valor })))`.
- Cada item: `p-card` menor, nome do bônus em `font-size: 0.875rem` e valor em `font-size: 1.5rem` monospace.
- Grid: `grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2`.

### Seção: Membros do Corpo (Fase 2)

- Reservar espaço no layout. Na Fase 1: não exibir esta seção (sem dados de vida por membro no backend atual).
- Interface preparatória:
```typescript
interface MembroCorpoComVida {
  membroId: number;
  membroNome: string;
  porcentagemVida: number; // 0.01 a 1.00 — do MembroCorpoConfig
  vidaCalculada: number;   // porcentagemVida * vidaTotal
  vidaAtual: number;       // editável pelo jogador (Fase 2)
}
```
- Quando implementado: `p-progressBar` por membro com `value` = `(vidaAtual / vidaCalculada) * 100`. Cor: verde se > 50%, amarelo se 25-50%, vermelho se < 25%.

### Empty State (atributos vazios)

```
┌────────────────────────────────────┐
│  [ícone pi pi-list-check grande]   │
│  Nenhum atributo distribuído ainda │
│  [p-button "Distribuir Atributos"] │
└────────────────────────────────────┘
```

---

## 8. Aba: FichaAtributosTabComponent (Dumb)

**Arquivo**: `components/ficha-atributos-tab/ficha-atributos-tab.component.ts`
**Selector**: `app-ficha-atributos-tab`

**Nota de escopo**: Esta aba é de **visualização** no `FichaDetailPage`. Edição de atributos ocorre no `FichaFormPage` (wizard step 2). Portanto este componente é read-only.

### Props

```typescript
atributos = input.required<FichaAtributoResponse[]>();
limitadorAtributo = input<number>(0); // do NivelConfig do nível atual
pontosAtributoTotal = input<number>(0);
pontosAtributoUsados = input<number>(0);
```

### Layout

```
┌─────────────────── ABA ATRIBUTOS ───────────────────────────────────┐
│  Pontos utilizados: 12 / 15   [p-progressBar verde]                 │
│                                                                     │
│  [p-table]                                                          │
│  ┌──────────────┬──────┬──────┬──────┬───────┬──────────┐          │
│  │ Atributo     │ Base │ Nív. │ Out. │ Total │  Ímpeto  │          │
│  ├──────────────┼──────┼──────┼──────┼───────┼──────────┤          │
│  │ Força (FOR)  │  10  │   2  │   0  │  12   │   6.0    │          │
│  │ Agilidade    │   8  │   1  │   1  │  10   │   5.0    │          │
│  └──────────────┴──────┴──────┴──────┴───────┴──────────┘          │
│                                                                     │
│  Limite por atributo (Nível {nivel}): {limitadorAtributo}           │
└─────────────────────────────────────────────────────────────────────┘
```

### Componente PrimeNG: p-table

```html
<p-table [value]="atributos()" [tableStyle]="{'min-width': '500px'}"
         styleClass="p-datatable-sm p-datatable-striped" responsiveLayout="scroll">
  <ng-template #header>
    <tr>
      <th>Atributo</th>
      <th class="text-center">Base</th>
      <th class="text-center">Nível</th>
      <th class="text-center">Outros</th>
      <th class="text-center">Total</th>
      <th class="text-center">Ímpeto</th>
    </tr>
  </ng-template>
  <ng-template #body let-atrib>
    <tr>
      <td>
        <span class="font-semibold">{{ atrib.atributoNome }}</span>
        <span class="text-color-secondary ml-2 font-mono text-sm">{{ atrib.atributoAbreviacao }}</span>
      </td>
      <td class="text-center font-mono">{{ atrib.base }}</td>
      <td class="text-center font-mono">{{ atrib.nivel }}</td>
      <td class="text-center font-mono">{{ atrib.outros }}</td>
      <td class="text-center">
        <strong class="text-primary font-mono text-lg">{{ atrib.total }}</strong>
      </td>
      <td class="text-center text-color-secondary font-mono">{{ atrib.impeto | number:'1.1-1' }}</td>
    </tr>
  </ng-template>
</p-table>
```

### Mobile

- `responsiveLayout="scroll"` garante scroll horizontal na tabela em telas pequenas.
- Adicionar `[scrollable]="true" scrollHeight="flex"` para evitar overflow.

---

## 9. Aba: FichaAptidoesTabComponent (Dumb)

**Arquivo**: `components/ficha-aptidoes-tab/ficha-aptidoes-tab.component.ts`
**Selector**: `app-ficha-aptidoes-tab`

### Props

```typescript
aptidoes = input.required<FichaAptidaoResponse[]>();
// Aptidões agrupadas por tipoAptidaoNome são um computed interno
```

**Observação**: `FichaAptidaoResponse` (backend) não inclui `tipoAptidaoNome` diretamente. O agrupamento requer que a página pai injete também a lista de `AptidaoConfig` para fazer o join por `aptidaoConfigId → tipoAptidao.nome`. Se isso não estiver disponível, exibir lista plana sem agrupamento na Fase 1.

```typescript
// Computed interno para agrupamento
private aptidoesAgrupadas = computed(() => {
  const grupos = new Map<string, FichaAptidaoResponse[]>();
  for (const apt of this.aptidoes()) {
    const tipo = apt['tipoAptidaoNome'] ?? 'Geral';
    if (!grupos.has(tipo)) grupos.set(tipo, []);
    grupos.get(tipo)!.push(apt);
  }
  return grupos;
});
```

### Layout

```
┌─────────────────── ABA APTIDOES ────────────────────────────────────┐
│                                                                     │
│  [p-fieldset legend="Físicas" toggleable]                           │
│  ┌───────────────────┬──────┬───────┬────────┬───────┐             │
│  │ Aptidão           │ Base │ Sorte │ Classe │ Total │             │
│  ├───────────────────┼──────┼───────┼────────┼───────┤             │
│  │ Espadas           │   3  │    1  │     2  │   6   │             │
│  │ Escudos           │   2  │    0  │     1  │   3   │             │
│  └───────────────────┴──────┴───────┴────────┴───────┘             │
│                                                                     │
│  [p-fieldset legend="Mentais" toggleable]                           │
│  ┌───────────────────┬──────┬───────┬────────┬───────┐             │
│  │ Persuasão         │   4  │    2  │     0  │   6   │             │
│  └───────────────────┴──────┴───────┴────────┴───────┘             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

- `p-fieldset` com `legend` = nome do TipoAptidao, `toggleable="true"`.
- Tabela interna simples, sem paginação, com `styleClass="p-datatable-sm"`.
- Total em destaque: `font-weight: 700`, `color: var(--primary-color)`.

---

## 10. Aba: FichaVantagensTabComponent (Dumb)

**Arquivo**: `components/ficha-vantagens-tab/ficha-vantagens-tab.component.ts`
**Selector**: `app-ficha-vantagens-tab`

### Props

```typescript
vantagens = input.required<FichaVantagemResponse[]>();
pontosVantagemRestantes = input<number>(0);
podeAumentarNivel = input<boolean>(false); // role MESTRE ou dono

aumentarNivelVantagem = output<number>(); // emite vantagemId (vid)
```

**Nota sobre FichaVantagemResponse**: O model atual (`FichaVantagem`) está desalinhado. A interface esperada para esta aba (alinhada com o backend real) é:

```typescript
// Interface a ser criada/corrigida em ficha.model.ts
interface FichaVantagemResponse {
  id: number;                  // vid — ID da FichaVantagem
  fichaId: number;
  vantagemConfigId: number;
  vantagemNome: string;
  vantagemSigla: string | null;
  categoriaNome: string;
  nivelAtual: number;
  nivelMaximo: number;
  custoPago: number;
  descricaoEfeito: string | null;
}
```

### Layout

```
┌─────────────────── ABA VANTAGENS ───────────────────────────────────┐
│  Pontos disponíveis: 8  [p-badge value="8" severity="info"]         │
│                                                                     │
│  [p-tag "Físico"]  ← por categoria (iteração de grupos)             │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ [p-card]  Força Bruta (FB)          [Nv. 2 / 3]             │   │
│  │           "Bônus de Força crescente"                         │   │
│  │           Custo pago: 6 pontos                               │   │
│  │           [p-button "Subir Nível" text small]  ← se pode    │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  [p-tag "Mental"]                                                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ [p-card]  ...                                               │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Especificações de Card de Vantagem

- `p-card` com `styleClass="vantagem-card mb-3"`.
- Cabeçalho (ng-template #header): nome em `font-weight: 700` + sigla em `p-tag` outlined à direita + nível atual como `p-progressBar value={nivelAtual/nivelMaximo*100}` com label "{nivelAtual}/{nivelMaximo}".
- Corpo: `descricaoEfeito` em texto secundário + `p-divider` + custo pago em pequeno.
- Rodapé (se `podeAumentarNivel()` e `nivelAtual < nivelMaximo` e `pontosVantagemRestantes() >= custoProximoNivel`): `p-button label="Subir Nível" icon="pi pi-arrow-up" text size="small"`.
- Botão de subir nível emite `aumentarNivelVantagem.emit(vantagem.id)`.

### Agrupamento por Categoria

```typescript
private vantagensAgrupadas = computed(() => {
  const grupos = new Map<string, FichaVantagemResponse[]>();
  for (const v of this.vantagens()) {
    if (!grupos.has(v.categoriaNome)) grupos.set(v.categoriaNome, []);
    grupos.get(v.categoriaNome)!.push(v);
  }
  return grupos;
});
```

### Empty State

```
┌────────────────────────────────────┐
│  [pi pi-star-fill grande]          │
│  Nenhuma vantagem comprada ainda   │
│  [p-button "Comprar Vantagens"]    │  ← navega para FichaFormPage
└────────────────────────────────────┘
```

---

## 11. Aba: FichaAnotacoesTabComponent (Smart)

**Arquivo**: `components/ficha-anotacoes-tab/ficha-anotacoes-tab.component.ts`
**Selector**: `app-ficha-anotacoes-tab`
**Tipo**: Smart — faz chamadas à API diretamente.

### Props

```typescript
fichaId = input.required<number>();
userRole = input.required<'MESTRE' | 'JOGADOR'>();
userId = input.required<number>();
```

### Estado interno

```typescript
private fichasApiService = inject(FichasApiService);
private toastService = inject(ToastService);
private confirmationService = inject(ConfirmationService);

protected anotacoes = signal<Anotacao[]>([]);
protected loading = signal(false);
protected showForm = signal(false);
protected novaAnotacao = signal<CriarAnotacaoDto>({
  titulo: '',
  conteudo: '',
  tipoAnotacao: 'JOGADOR',
  visivelParaJogador: false
});
```

### Layout Completo

```
┌─────────────────── ABA ANOTACOES ───────────────────────────────────┐
│  [p-button "+ Nova Anotação" icon="pi pi-plus"]                     │
│                                                                     │
│  [@if showForm()]  ─────────────────────────────────               │
│  │ [p-card styleClass="nova-anotacao-card"]                        │
│  │   Título: [p-inputText placeholder="Título da anotação"]        │
│  │   [p-editor height="150px"]  ← conteúdo rico                   │
│  │   @if role = MESTRE:                                            │
│  │     Tipo: [p-selectButton JOGADOR | MESTRE]                     │
│  │     Visível para jogador: [p-toggleButton]                      │
│  │   [Cancelar] [Salvar]                                           │
│  └─────────────────────────────────────────────────────────────────│
│                                                                     │
│  @for (anotacao of anotacoes(); track anotacao.id)                  │
│  [app-anotacao-card]                                                │
│    @if tipoAnotacao = 'MESTRE' e !visivelParaJogador (visto pelo    │
│    Mestre) → card com fundo amarelo destacado                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Componente Filho: AnotacaoCardComponent (Dumb)

**Arquivo**: `components/anotacao-card/anotacao-card.component.ts`

```typescript
anotacao = input.required<Anotacao>();
podeDeletar = input<boolean>(false);

deletar = output<number>(); // emite anotacao.id
```

```
┌─────────────────────────────────────────────────────────────────────┐
│ [p-card]                                                            │
│  Título da Anotação                                                 │
│  [p-tag "JOGADOR" severity="success"] ou [p-tag "MESTRE" sev="warn"]│
│  [p-tag "Visível" severity="info"] ← se visivelParaJogador          │
│  ─────────────────────────────────────────────                      │
│  Conteúdo da anotação aqui.                                         │
│  Por Carlos · 31/03/2026                                            │
│                                    [p-button deletar icon=trash]    │
└─────────────────────────────────────────────────────────────────────┘
```

- Anotações do Mestre ocultas (não visíveis para jogador): `styleClass="anotacao-mestre-oculta"` com `background-color: var(--yellow-100)` e borda `var(--yellow-400)`.
- Jogadores não enxergam essas anotações (filtradas pelo backend).
- Regra de exibição do botão deletar: `podeDeletar = userRole === 'MESTRE' || anotacao.autorId === userId`.

---

## 12. Dialog: Duplicar Ficha

Aberto pelo botão "Duplicar" no header. Implementado na `FichaDetailPage`.

```typescript
protected showDuplicarDialog = signal(false);
protected novoNomeDuplicar = signal('');
```

```html
<p-dialog header="Duplicar Ficha" [visible]="showDuplicarDialog()"
          (visibleChange)="showDuplicarDialog.set($event)"
          [modal]="true" [style]="{width: '400px'}"
          [draggable]="false" [resizable]="false">
  <div class="flex flex-col gap-4">
    <p class="text-color-secondary m-0">
      Cria uma cópia desta ficha com todos os atributos, aptidões e vantagens.
    </p>
    <div class="flex flex-col gap-1">
      <label for="novoNome" class="font-medium">Nome da nova ficha</label>
      <p-inputText id="novoNome" [ngModel]="novoNomeDuplicar()"
                   (ngModelChange)="novoNomeDuplicar.set($event)"
                   placeholder="Ex: Aldric Cópia" class="w-full" />
    </div>
  </div>
  <ng-template #footer>
    <p-button label="Cancelar" text (onClick)="showDuplicarDialog.set(false)" />
    <p-button label="Duplicar" icon="pi pi-copy" [disabled]="!novoNomeDuplicar().trim()"
              (onClick)="confirmarDuplicar()" />
  </ng-template>
</p-dialog>
```

---

## 13. Confirmação: Deletar Ficha

Usa `ConfirmationService` do PrimeNG. Disparado pelo botão "Deletar" no header.

```typescript
protected abrirConfirmacaoDeletar(): void {
  this.confirmationService.confirm({
    message: `Tem certeza que deseja deletar a ficha "${this.ficha()?.nome}"? Esta ação não pode ser desfeita.`,
    header: 'Deletar Ficha',
    icon: 'pi pi-exclamation-triangle',
    acceptButtonStyleClass: 'p-button-danger',
    acceptLabel: 'Sim, deletar',
    rejectLabel: 'Cancelar',
    accept: () => this.deletarFicha()
  });
}
```

Requer `<p-confirmdialog />` no template da `FichaDetailPage`.

---

## 14. Estados da UI

### Estado: Loading

O loading skeleton deve cobrir as três áreas: header, stats-bar e conteúdo das abas.

```html
@if (loading()) {
  <!-- Header skeleton -->
  <div class="flex items-center gap-4 p-4">
    <p-skeleton shape="circle" size="5rem" />
    <div class="flex flex-col gap-2 flex-1">
      <p-skeleton width="60%" height="1.5rem" />
      <p-skeleton width="40%" height="1rem" />
    </div>
    <p-skeleton width="80px" height="2rem" borderRadius="16px" />
  </div>
  <!-- Stats bar skeleton -->
  <div class="px-4 flex flex-col gap-2">
    <p-skeleton width="100%" height="1.25rem" borderRadius="8px" />
    <p-skeleton width="70%" height="1.25rem" borderRadius="8px" />
  </div>
  <!-- Content skeleton (grid de cards) -->
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 mt-4">
    @for (_ of [1,2,3,4]; track $index) {
      <p-skeleton height="100px" borderRadius="8px" />
    }
  </div>
}
```

### Estado: Erro ao Carregar

```html
@if (erro()) {
  <div class="flex flex-col items-center justify-center p-8 gap-4">
    <i class="pi pi-exclamation-circle text-red-500" style="font-size: 3rem"></i>
    <h2 class="text-xl font-semibold m-0">Erro ao carregar ficha</h2>
    <p class="text-color-secondary text-center m-0">{{ erro() }}</p>
    <p-button label="Tentar novamente" icon="pi pi-refresh" outlined
              (onClick)="recarregar()" />
  </div>
}
```

### Estado: Ficha não encontrada (404)

```html
<!-- Variante do erro para 404 -->
<div class="flex flex-col items-center justify-center p-8 gap-4">
  <i class="pi pi-file-excel text-color-secondary" style="font-size: 3rem"></i>
  <h2 class="text-xl font-semibold m-0">Ficha não encontrada</h2>
  <p class="text-color-secondary text-center m-0">
    A ficha que você está buscando não existe ou foi removida.
  </p>
  <p-button label="Voltar para Fichas" icon="pi pi-arrow-left" outlined
            routerLink="/fichas" />
</div>
```

---

## 15. Organização do p-tabView

```html
<p-tabs [value]="0" scrollable>
  <p-tablist>
    <p-tab [value]="0">
      <i class="pi pi-chart-bar mr-2"></i>Resumo
    </p-tab>
    <p-tab [value]="1">
      <i class="pi pi-sliders-h mr-2"></i>Atributos
    </p-tab>
    <p-tab [value]="2">
      <i class="pi pi-list mr-2"></i>Aptidoes
    </p-tab>
    <p-tab [value]="3">
      <i class="pi pi-star mr-2"></i>Vantagens
    </p-tab>
    <p-tab [value]="4">
      <i class="pi pi-pencil mr-2"></i>Anotacoes
    </p-tab>
  </p-tablist>

  <p-tabpanels>
    <p-tabpanel [value]="0">
      <app-ficha-resumo-tab ... />
    </p-tabpanel>
    <p-tabpanel [value]="1">
      <app-ficha-atributos-tab ... />
    </p-tabpanel>
    <p-tabpanel [value]="2">
      <app-ficha-aptidoes-tab ... />
    </p-tabpanel>
    <p-tabpanel [value]="3">
      <app-ficha-vantagens-tab ... />
    </p-tabpanel>
    <p-tabpanel [value]="4">
      <app-ficha-anotacoes-tab ... />
    </p-tabpanel>
  </p-tabpanels>
</p-tabs>
```

**Nota PrimeNG 18+**: O componente é `p-tabs` (não `p-tabView`). O API mudou na v18. Use `TabsModule` de `primeng/tabs`.

**Lazy loading das abas**: Usar `p-deferred-content` ou carregar dados apenas quando a aba é ativada via evento `(onChange)` no `p-tabs`. Atributos e aptidões carregam apenas se o usuário abre as abas respectivas.

---

## 16. Comportamento Responsivo Detalhado

### Desktop (> 1024px)
- Header: sticky, `position: sticky; top: 64px` (abaixo do toolbar principal).
- Grid de atributos: 4 colunas.
- Grid de bônus: 4 colunas.
- Tabelas: largura total, sem scroll horizontal.
- Abas: labels completos com ícones.

### Tablet (768px–1024px)
- Header: não sticky, inline.
- Grid de atributos: 3 colunas.
- Grid de bônus: 3 colunas.
- Tabelas: scroll horizontal se necessário.
- Abas: labels completos.

### Mobile (< 768px)
- Header: não sticky, compacto (avatar menor, botões de ação apenas como ícone).
- Grid de atributos: 2 colunas.
- Grid de bônus: 2 colunas.
- `p-tabs` com `scrollable="true"` — abas em scroll horizontal, sem quebra de linha.
- Tabelas: `responsiveLayout="scroll"` com `overflow-x: auto`.
- Formulário de nova anotação: ocupa toda a largura da tela.
- Botão "Nova Anotação": texto completo no topo, visível.

---

## 17. Tokens e Variáveis CSS

Cores a usar via variáveis do tema Aura:

| Uso | Variável |
|-----|----------|
| Cor primária (total de atributos) | `var(--primary-color)` |
| Texto secundário | `var(--text-color-secondary)` |
| Superfície de card | `var(--surface-card)` |
| Borda | `var(--surface-border)` |
| Barra de vida verde | `var(--green-500)` |
| Barra de essência azul | `var(--blue-400)` |
| Barra de XP | `var(--primary-color)` com opacidade 70% |
| Anotação Mestre oculta (fundo) | `var(--yellow-100)` |
| Anotação Mestre oculta (borda) | `var(--yellow-400)` |
| Valor crítico (vida < 25%) | `var(--red-500)` |

Tipografia:
- Títulos RPG (nome do personagem, seções): `font-family: Georgia, 'Times New Roman', serif`
- Valores numéricos (total atributos, ímpeto): `font-family: 'Courier New', Courier, monospace`
- Corpo e labels: `font-family: var(--font-family)` (padrão do tema)

---

## 18. Acessibilidade (WCAG 2.1 AA)

- `p-tabView` / `p-tabs`: role `tablist`/`tab`/`tabpanel` aplicados automaticamente pelo PrimeNG. Navegação por teclado com setas ja funciona.
- Barras de progresso: `aria-label` descritivo — `aria-label="Vida: 25 de 30 pontos"`.
- Botões de ação destrutivos (deletar): `aria-label="Deletar ficha de Aldric"`.
- Dialog de duplicar: `aria-labelledby` aponta para o `header` do dialog.
- `p-confirmDialog` gerencia focus trap automaticamente.
- Skeleton loading: `aria-busy="true"` no container durante loading.
- Contraste: valores de atributos em `var(--primary-color)` sobre `var(--surface-card)` devem respeitar 4.5:1. Verificar no tema Aura Dark/Light.
- Informação de tipo de anotação (MESTRE vs JOGADOR): transmitida via texto + cor, nunca só por cor.

---

## 19. Fluxo de Dados e Chamadas API

### Inicialização da página

```typescript
ngOnInit(): void {
  const fichaId = +this.route.snapshot.params['id'];

  this.loading.set(true);
  forkJoin({
    ficha: this.fichasApiService.getFicha(fichaId),
    resumo: this.fichasApiService.getFichaResumo(fichaId)
  }).subscribe({
    next: ({ ficha, resumo }) => {
      this.ficha.set(ficha);
      this.resumo.set(resumo);
      this.loading.set(false);
    },
    error: (err) => {
      this.erro.set(err.status === 404
        ? 'Ficha não encontrada'
        : 'Erro ao carregar ficha. Tente novamente.');
      this.loading.set(false);
    }
  });
}
```

### Atualização após ação

Após qualquer ação que mude dados calculados (aumentar nível de vantagem, conceder XP):
1. Executar a ação.
2. Chamar `getFichaResumo(fichaId)` novamente.
3. Atualizar signal `resumo`.
4. Exibir `p-toast` de sucesso.

### Cache vs ao-vivo

| Dado | Estratégia |
|------|-----------|
| `ficha` | Cache no signal; atualiza após PUT |
| `resumo` | Sempre ao-vivo após qualquer save |
| `atributos` | Carregado ao abrir aba Atributos (lazy) |
| `aptidoes` | Carregado ao abrir aba Aptidoes (lazy) |
| `vantagens` | Carregado ao abrir aba Vantagens (lazy) |
| `anotacoes` | Gerenciado internamente em FichaAnotacoesTabComponent |

---

## 20. Estrutura de Arquivos Final

```
ficha-detail/
  ficha-detail.component.ts         [SMART] — página principal
  ficha-detail.routes.ts
  components/
    ficha-header/
      ficha-header.component.ts     [DUMB]
    ficha-resumo-tab/
      ficha-resumo-tab.component.ts [DUMB]
    ficha-atributos-tab/
      ficha-atributos-tab.component.ts [DUMB]
    ficha-aptidoes-tab/
      ficha-aptidoes-tab.component.ts  [DUMB]
    ficha-vantagens-tab/
      ficha-vantagens-tab.component.ts [DUMB]
    ficha-anotacoes-tab/
      ficha-anotacoes-tab.component.ts [SMART — faz chamadas API]
      components/
        anotacao-card/
          anotacao-card.component.ts   [DUMB]
```

---

## 21. Checklist de Implementação

- [ ] `FichaHeaderComponent` com avatar, nome, badges, barras de vida/essência, botões de ação
- [ ] `FichaDetailPage` lendo `fichaId` da rota, carregando ficha + resumo em paralelo
- [ ] `p-tabs` com 5 abas, lazy loading de dados por aba
- [ ] `FichaResumoTabComponent` com grid de atributos totais + bônus derivados
- [ ] `FichaAtributosTabComponent` com `p-table` responsiva
- [ ] `FichaAptidoesTabComponent` com `p-fieldset` por tipo de aptidão
- [ ] `FichaVantagensTabComponent` com cards por categoria + botão "Subir Nível"
- [ ] `FichaAnotacoesTabComponent` com CRUD inline, tipos JOGADOR/MESTRE, visibilidade
- [ ] `AnotacaoCardComponent` com badge de tipo e botão deletar condicionado por role
- [ ] Dialog de duplicar ficha com input de nome
- [ ] `p-confirmDialog` para deletar ficha
- [ ] Skeleton loading em todas as 3 áreas (header, stats, conteúdo)
- [ ] Empty states para atributos, vantagens e anotações vazios
- [ ] Toast de sucesso/erro para todas as ações
- [ ] Interface `FichaVantagemResponse` corrigida no `ficha.model.ts`
- [ ] Responsividade mobile verificada (scroll horizontal em tabelas, abas scroll)
- [ ] Atributos ARIA em todas as barras de progresso e botões de ação
