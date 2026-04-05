# T11 — Frontend: UI de Aba Inventario/Equipamentos na FichaDetail

> Fase: Frontend — Ficha | Prioridade: P3
> Dependencias: T4 (FichaItem backend), T8 (chips de raridade disponiveis), T9 (modal de catalogo reutilizavel)
> Bloqueia: nada
> Estimativa: 3 dias

---

## Objetivo

Implementar a aba "Equipamentos" na tela de detalhe de ficha (`FichaDetailComponent`), disponivel
para Mestre e Jogador (dono da ficha). Esta e a task mais complexa do frontend da Spec 016:
envolve listar o inventario completo (`FichaItem`), equipar/desequipar itens com validacao de
requisitos, exibir durabilidade, barra de capacidade de carga, modal de adicao do catalogo, drawer
de detalhes e logica de permissoes diferenciada por role (MESTRE vs JOGADOR).

A aba sera inserida como 5a posicao (value `5`) nos `p-tabs` do `FichaDetailComponent`,
deslocando Anotacoes para value `6`.

---

## Pontos Pendentes que Afetam Esta Task

> As decisoes abaixo devem ser tomadas pelo PO antes de iniciar a implementacao.
> Marcar como [RESOLVIDO] quando respondidas.

| Codigo | Pergunta | Impacto |
|--------|----------|---------|
| PA-016-01 | Penalidade de sobrecarga (peso > capacidade) esta no MVP? | Se sim: exibir aviso e bloquear acoes. Se nao: barra de peso e apenas informativa. |
| PA-016-04 | Item customizado (sem ItemConfig) com efeitos automaticos no MVP? | Se nao (atual): botao "Adicionar item customizado" pode ser ocultado para Jogador no MVP. |
| PA-016-06 | Jogador ve o catalogo completo do jogo (incluindo itens Incomum+) ou apenas Comuns? | Filtro do modal de adicao muda: mostrar tudo (com lock) ou filtrar por raridade. |

**Premissas assumidas para esta task (enquanto PA nao resolvidos):**

- **PA-016-01 (premissa):** Sem penalidade automatica no MVP. Barra de peso exibida como informativa.
  Exibir aviso visual vermelho quando sobrecarregado, mas sem bloqueio de acoes.
- **PA-016-04 (premissa):** Itens customizados sem efeitos automaticos. Botao "Adicionar customizado"
  disponivel apenas para Mestre.
- **PA-016-06 (premissa):** Modal mostra catalogo completo. Itens com raridade restrita exibem icone
  de cadeado e tooltip explicativo para o Jogador. Jogador nao pode clicar em "Adicionar" para esses itens.

---

## Arquivos a Criar (Angular)

| Arquivo | Descricao |
|---------|-----------|
| `features/ficha/components/ficha-equipamentos-tab/ficha-equipamentos-tab.component.ts` | Componente principal da aba (dumb) |
| `features/ficha/components/ficha-equipamentos-tab/ficha-equipamentos-tab.component.html` | Template principal |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-card/ficha-item-card.component.ts` | Card de item individual |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-card/ficha-item-card.component.html` | Template do card |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-detalhe-drawer/ficha-item-detalhe-drawer.component.ts` | Drawer de detalhes do item |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-detalhe-drawer/ficha-item-detalhe-drawer.component.html` | Template do drawer de detalhes |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-adicionar-dialog/ficha-item-adicionar-dialog.component.ts` | Dialog de adicao de item do catalogo |
| `features/ficha/components/ficha-equipamentos-tab/ficha-item-adicionar-dialog/ficha-item-adicionar-dialog.component.html` | Template do dialog |
| `core/models/ficha-item.model.ts` | Interfaces TypeScript de FichaItem |
| `core/services/ficha-item.service.ts` | HTTP service para endpoints de FichaItem |
| `features/ficha/stores/ficha-equipamentos.store.ts` | Signal store para estado do inventario |

## Arquivos a Editar

| Arquivo | Alteracao |
|---------|-----------|
| `features/ficha/pages/ficha-detail/ficha-detail.component.ts` | Adicionar aba value=5 (Equipamentos) e value=6 (Anotacoes) |
| `features/ficha/pages/ficha-detail/ficha-detail.component.html` | Inserir `<p-tab>` e `<p-tabpanel>` de Equipamentos |

---

## Modelos TypeScript

```typescript
// core/models/ficha-item.model.ts

export type OrigemFichaItem = 'CATALOGO' | 'CUSTOMIZADO' | 'INICIAL_CLASSE' | 'INICIAL_RACA';

export interface FichaItemEfeitoResumo {
  id: number;
  tipoEfeito: string;
  resumo: string; // ex: "FOR +2", "BBA +1", "Defesa +3"
}

export interface FichaItemRequisitoResumo {
  tipo: string;       // ex: "NIVEL", "ATRIBUTO"
  descricao: string;  // ex: "Nivel 5+", "FOR >= 12"
  atendido: boolean;
}

export interface FichaItemResponse {
  id: number;
  fichaId: number;
  itemConfigId?: number;
  nome: string;
  equipado: boolean;
  duracaoAtual?: number;
  duracaoPadrao?: number;  // null = indestrutivel
  quantidade: number;
  peso: number;
  notas?: string;
  raridade?: {
    id: number;
    nome: string;
    cor: string;
    ordemExibicao: number;
    podeJogadorAdicionar: boolean;
  };
  tipo?: {
    id: number;
    nome: string;
    categoria: string;
    subcategoria?: string;
    requerDuasMaos: boolean;
  };
  efeitos: FichaItemEfeitoResumo[];
  requisitos: FichaItemRequisitoResumo[];
  origem: OrigemFichaItem;
  obrigatorioClasse: boolean; // derivado de ClasseEquipamentoInicial
  adicionadoPor: string;
  dataCriacao: string;
}

export interface AdicionarFichaItemRequest {
  itemConfigId: number;
  quantidade: number;
  notas?: string;
  forcarAdicao?: boolean; // apenas MESTRE
}

export interface AdicionarFichaItemCustomizadoRequest {
  nome: string;
  raridadeId: number;
  peso: number;
  quantidade: number;
  notas?: string;
}

export interface AlterarDurabidadeRequest {
  decremento?: number;
  restaurar?: boolean;
}

// ViewModel utilizado internamente pelo componente (derivado do response)
export interface FichaItemViewModel extends FichaItemResponse {
  raridadeCor: string;     // shortcut para raridade?.cor ?? '#9d9d9d'
  tipoNomeCompleto: string; // ex: "Arma > Espada"
  tipoIcone: string;        // pi icon baseado na categoria
  estaQuebrado: boolean;    // duracaoAtual === 0
  requisitosAtendidos: boolean;
}
```

---

## Mapeamento de Icones por Categoria

```typescript
// Util: mapeia categoria de TipoItemConfig para icone PrimeIcons
export const CATEGORIA_ICONE: Record<string, string> = {
  ARMA:       'pi pi-hammer',
  ARMADURA:   'pi pi-shield',
  ACESSORIO:  'pi pi-star',
  CONSUMIVEL: 'pi pi-heart',
  FERRAMENTA: 'pi pi-wrench',
  AVENTURA:   'pi pi-box',
};

export function getTipoIcone(categoria?: string): string {
  return CATEGORIA_ICONE[categoria ?? ''] ?? 'pi pi-box';
}

export function buildTipoNomeCompleto(tipo?: { nome: string; categoria: string }): string {
  if (!tipo) return 'Item';
  const cat = categoria_label[tipo.categoria] ?? tipo.categoria;
  return `${cat} > ${tipo.nome}`;
}

export const CATEGORIA_LABEL: Record<string, string> = {
  ARMA:       'Arma',
  ARMADURA:   'Armadura',
  ACESSORIO:  'Acessorio',
  CONSUMIVEL: 'Consumivel',
  FERRAMENTA: 'Ferramenta',
  AVENTURA:   'Aventura',
};
```

---

## HTTP Service

```typescript
// core/services/ficha-item.service.ts
@Injectable({ providedIn: 'root' })
export class FichaItemService {
  private readonly http = inject(HttpClient);
  private readonly base = (fichaId: number) => `/api/v1/fichas/${fichaId}/itens`;

  listar(fichaId: number): Observable<FichaItemResponse[]> {
    return this.http.get<FichaItemResponse[]>(this.base(fichaId));
  }

  adicionar(fichaId: number, request: AdicionarFichaItemRequest): Observable<FichaItemResponse> {
    return this.http.post<FichaItemResponse>(this.base(fichaId), request);
  }

  adicionarCustomizado(
    fichaId: number,
    request: AdicionarFichaItemCustomizadoRequest
  ): Observable<FichaItemResponse> {
    return this.http.post<FichaItemResponse>(`${this.base(fichaId)}/customizado`, request);
  }

  equipar(fichaId: number, itemId: number): Observable<FichaItemResponse> {
    return this.http.patch<FichaItemResponse>(`${this.base(fichaId)}/${itemId}/equipar`, {});
  }

  desequipar(fichaId: number, itemId: number): Observable<FichaItemResponse> {
    return this.http.patch<FichaItemResponse>(`${this.base(fichaId)}/${itemId}/desequipar`, {});
  }

  alterarDurabilidade(
    fichaId: number,
    itemId: number,
    request: AlterarDurabidadeRequest
  ): Observable<FichaItemResponse> {
    return this.http.post<FichaItemResponse>(
      `${this.base(fichaId)}/${itemId}/durabilidade`,
      request
    );
  }

  remover(fichaId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(fichaId)}/${itemId}`);
  }
}
```

---

## Signal Store

```typescript
// features/ficha/stores/ficha-equipamentos.store.ts
export const FichaEquipamentosStore = signalStore(
  withState({
    itens: [] as FichaItemResponse[],
    loading: false,
    erro: null as string | null,
    itemDetalhado: null as FichaItemResponse | null,
    drawerDetalheAberto: false,
    dialogAdicionarAberto: false,
  }),
  withComputed(({ itens }) => ({
    itensEquipados: computed(() => itens().filter(i => i.equipado)),
    itensNoInventario: computed(() => itens().filter(i => !i.equipado)),
    pesoTotal: computed(() =>
      itens().reduce((acc, i) => acc + (i.peso * i.quantidade), 0)
    ),
    countEquipados: computed(() => itens().filter(i => i.equipado).length),
    countInventario: computed(() => itens().filter(i => !i.equipado).length),
  })),
  withMethods((store, service = inject(FichaItemService)) => ({
    carregar(fichaId: number): void {
      patchState(store, { loading: true, erro: null });
      service.listar(fichaId).subscribe({
        next: itens => patchState(store, { itens, loading: false }),
        error: () => patchState(store, { loading: false, erro: 'Erro ao carregar inventario' }),
      });
    },
    // demais metodos: equipar, desequipar, remover, adicionar
  }))
);
```

---

## Layout e Wireframes

### Posicionamento da Aba em FichaDetailComponent

```html
<!-- ficha-detail.component.html — adicionar apos aba Vantagens (value=3) -->
<p-tab [value]="5">
  <i class="pi pi-shield mr-2"></i>Equipamentos
  @if (store.countEquipados() > 0) {
    <p-badge [value]="store.countEquipados()" severity="secondary" class="ml-2" />
  }
</p-tab>

<!-- ... aba Anotacoes passa a ser value=6 ... -->

<!-- TabPanel de Equipamentos -->
<p-tabpanel [value]="5">
  <app-ficha-equipamentos-tab
    [itensFicha]="store.itens()"
    [capacidadeCarga]="fichaStore.capacidadeCarga()"
    [pesoTotal]="equipamentosStore.pesoTotal()"
    [podeEditar]="podeEditar()"
    [isMestre]="isMestre()"
    [loading]="equipamentosStore.loading()"
    (equipar)="onEquipar($event)"
    (desequipar)="onDesequipar($event)"
    (remover)="onRemover($event)"
    (adicionarItem)="onAbrirAdicionarItem()"
    (verDetalhes)="onVerDetalhes($event)"
  />
</p-tabpanel>
```

### Layout Principal da Aba (ficha-equipamentos-tab.component.html)

```
┌─────────────────────────────────────────────────────────────────┐
│ CAPACIDADE DE CARGA                                             │
│  [pi pi-database]  14.5 / 18 kg  (81%)                         │
│  [p-progressBar value="81" severidade dinamica]                 │
│  [aviso vermelho se >= 80%: "Sobrecarregado!"]                  │
├─────────────────────────────────────────────────────────────────┤
│  [p-tabs scrollable]                                            │
│  [Equipado (3)] [Inventario (5)]      [+ Adicionar Item]        │
│                                       [+ Customizado] (MESTRE)  │
├─────────────────────────────────────────────────────────────────┤
│  [TabPanel: Equipado]                                           │
│                                                                 │
│  [Estado vazio: "Nenhum item equipado"]                         │
│  OU                                                             │
│  [grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3]               │
│  ┌───────────────────────────────────────┐                      │
│  │ [borda esquerda cor raridade]         │                      │
│  │ [chip raridade] [nome] [tipo]         │                      │
│  │ [barra durabilidade — se aplicavel]   │                      │
│  │ [chips de efeitos: "FOR +2", "BBA +1"]│                      │
│  │ [X.X kg]                              │                      │
│  │ [Desequipar] [Ver detalhes] [Remover] │                      │
│  └───────────────────────────────────────┘                      │
│                                                                 │
│  [TabPanel: Inventario]                                         │
│  (mesma estrutura de cards, botao "Equipar" em vez de          │
│   "Desequipar"; desabilitado se quebrado ou req nao atendidos) │
└─────────────────────────────────────────────────────────────────┘
```

### Barra de Capacidade de Carga

```html
<!-- ficha-equipamentos-tab.component.html -->
@let percentualPeso = capacidadeCarga() > 0
  ? (pesoTotal() / capacidadeCarga()) * 100
  : 0;
@let sevPeso = percentualPeso < 50 ? 'success'
  : percentualPeso < 80 ? 'warn'
  : 'danger';

<div class="mb-4 px-1">
  <div class="flex justify-content-between align-items-center mb-1">
    <span class="text-sm font-semibold">
      <i class="pi pi-database mr-1"></i>Capacidade de Carga
    </span>
    <span
      class="text-sm font-mono"
      [class.text-red-500]="percentualPeso >= 80"
      [class.text-yellow-600]="percentualPeso >= 50 && percentualPeso < 80"
    >
      {{ pesoTotal() | number:'1.1-1' }} / {{ capacidadeCarga() }} kg
    </span>
  </div>
  <p-progressBar
    [value]="percentualPeso"
    [severity]="sevPeso"
    styleClass="h-1rem"
    [attr.aria-label]="'Peso: ' + pesoTotal() + ' de ' + capacidadeCarga() + ' kg'"
  />
  @if (percentualPeso >= 80) {
    <small class="text-red-500 flex align-items-center gap-1 mt-1">
      <i class="pi pi-exclamation-triangle text-xs"></i>
      Sobrecarregado! Verifique o peso do inventario.
    </small>
  }
  <small class="text-xs text-color-secondary mt-1 block">
    Impeto de Forca (FOR x 3): {{ capacidadeCarga() }} kg maximos
  </small>
</div>
```

### Card de Item (ficha-item-card.component.html)

```html
<div
  class="border-1 surface-border border-round-lg p-3 flex flex-col gap-2 surface-card
         transition-shadow transition-duration-200 hover:shadow-2"
  [class.opacity-50]="item.estaQuebrado"
  [class.border-left-3]="true"
  [style.border-left-color]="item.raridadeCor"
  [attr.aria-label]="'Item: ' + item.nome + (item.equipado ? ', equipado' : ', no inventario')"
>
  <!-- Header: raridade chip + nome + tipo -->
  <div class="flex align-items-start justify-content-between gap-2">
    <div class="flex align-items-center gap-2 flex-1 min-w-0">
      <i [class]="item.tipoIcone + ' text-color-secondary flex-shrink-0 text-lg'"></i>
      <div class="flex flex-col min-w-0">
        <span class="font-semibold text-sm leading-tight truncate" [title]="item.nome">
          {{ item.nome }}
        </span>
        <span class="text-xs text-color-secondary">{{ item.tipoNomeCompleto }}</span>
      </div>
    </div>
    <div class="flex flex-col align-items-end gap-1 flex-shrink-0">
      <p-tag
        [value]="item.raridade?.nome ?? 'Customizado'"
        [style.background-color]="item.raridadeCor + '22'"
        [style.color]="item.raridadeCor"
        [style.border]="'1px solid ' + item.raridadeCor"
        class="text-xs"
      />
      @if (item.estaQuebrado) {
        <span class="text-xs font-bold text-red-500">QUEBRADO</span>
      }
    </div>
  </div>

  <!-- Barra de durabilidade (apenas se tem durabilidade) -->
  @if (item.duracaoPadrao != null) {
    @let percDur = item.duracaoPadrao > 0
      ? ((item.duracaoAtual ?? 0) / item.duracaoPadrao) * 100
      : 0;
    <div>
      <div class="flex justify-content-between text-xs text-color-secondary mb-1">
        <span>Durabilidade</span>
        @if (item.estaQuebrado) {
          <span class="text-red-500 font-semibold">0 / {{ item.duracaoPadrao }}</span>
        } @else {
          <span class="font-mono">{{ item.duracaoAtual }} / {{ item.duracaoPadrao }}</span>
        }
      </div>
      <p-progressBar
        [value]="percDur"
        [style.height]="'5px'"
        [severity]="percDur < 25 ? 'danger' : percDur < 50 ? 'warn' : 'success'"
        [attr.aria-label]="'Durabilidade: ' + item.duracaoAtual + ' de ' + item.duracaoPadrao"
      />
    </div>
  }

  <!-- Chips de efeitos -->
  @if (item.efeitos.length > 0) {
    <div class="flex flex-wrap gap-1">
      @for (efeito of item.efeitos.slice(0, 3); track efeito.id) {
        <p-tag [value]="efeito.resumo" severity="secondary" class="text-xs" />
      }
      @if (item.efeitos.length > 3) {
        <p-tag
          [value]="'+' + (item.efeitos.length - 3) + ' mais'"
          severity="secondary"
          class="text-xs"
          [pTooltip]="item.efeitos.slice(3).map(e => e.resumo).join(', ')"
          tooltipPosition="top"
        />
      }
    </div>
  }

  <!-- Peso e quantidade -->
  <div class="flex justify-content-between align-items-center text-xs text-color-secondary">
    <span>
      <i class="pi pi-database mr-1"></i>
      {{ item.peso | number:'1.1-2' }} kg
      @if (item.quantidade > 1) {
        <span class="ml-1">(x{{ item.quantidade }})</span>
      }
    </span>
    @if (item.origem === 'INICIAL_CLASSE') {
      <span
        class="text-xs text-color-secondary"
        pTooltip="Item obrigatorio da classe"
        tooltipPosition="top"
      >
        <i class="pi pi-lock text-xs"></i> Classe
      </span>
    }
  </div>

  <!-- Acoes condicionais -->
  <div class="flex gap-1 justify-content-end mt-1 flex-wrap">
    @if (item.equipado) {
      <p-button
        label="Desequipar"
        icon="pi pi-arrow-down"
        size="small"
        outlined
        severity="secondary"
        [disabled]="!podeEditar()"
        [attr.aria-label]="'Desequipar ' + item.nome"
        (onClick)="desequipar.emit(item.id)"
      />
    } @else {
      <p-button
        label="Equipar"
        icon="pi pi-arrow-up"
        size="small"
        outlined
        [disabled]="!podeEditar() || item.estaQuebrado"
        [title]="item.estaQuebrado ? 'Item quebrado — conserte antes de equipar' : ''"
        [attr.aria-label]="'Equipar ' + item.nome"
        (onClick)="tentarEquipar(item)"
      />
    }

    <p-button
      icon="pi pi-info-circle"
      size="small"
      text
      [attr.aria-label]="'Ver detalhes de ' + item.nome"
      (onClick)="verDetalhes.emit(item)"
    />

    @if (podeRemoverItem(item)) {
      <p-button
        icon="pi pi-trash"
        size="small"
        text
        severity="danger"
        [attr.aria-label]="'Remover ' + item.nome + ' do inventario'"
        (onClick)="confirmarRemover(item)"
      />
    }
  </div>
</div>
```

---

## Logica de Permissoes por Role

```typescript
// ficha-item-card.component.ts
protected podeRemoverItem(item: FichaItemViewModel): boolean {
  // Mestre pode remover qualquer item (RN-ITEM-03)
  if (this.isMestre()) return true;

  // Jogador nao pode remover itens obrigatorios de classe (RN-ITEM-16)
  if (item.obrigatorioClasse) return false;

  // Jogador so pode remover itens de raridade Comum (podeJogadorAdicionar=true) (RN-ITEM-03)
  return item.raridade?.podeJogadorAdicionar === true;
}

protected tentarEquipar(item: FichaItemViewModel): void {
  const reqNaoAtendidos = item.requisitos.filter(r => !r.atendido);

  if (reqNaoAtendidos.length > 0) {
    // Mestre pode forcar equipar sem atender requisitos (RN-ITEM-06)
    if (this.isMestre()) {
      // Para o Mestre: aviso mas permite
      this.confirmationService.confirm({
        message: `${item.nome} nao atende os requisitos:\n${reqNaoAtendidos.map(r => '• ' + r.descricao).join('\n')}\n\nEquipar mesmo assim?`,
        header: 'Requisitos Nao Atendidos',
        icon: 'pi pi-exclamation-triangle',
        acceptLabel: 'Equipar assim mesmo',
        rejectLabel: 'Cancelar',
        accept: () => this.equipar.emit(item.id),
      });
    } else {
      // Para Jogador: bloqueio com mensagem explicativa
      this.messageService.add({
        severity: 'warn',
        summary: 'Requisitos insuficientes',
        detail: reqNaoAtendidos.map(r => r.descricao).join(' | '),
        life: 5000,
      });
    }
  } else {
    this.equipar.emit(item.id);
  }
}
```

---

## Dialog de Adicao de Item (ficha-item-adicionar-dialog.component)

### Interface de Componente

```typescript
@Component({
  selector: 'app-ficha-item-adicionar-dialog',
  standalone: true,
  imports: [ /* PrimeNG, FormsModule */ ],
})
export class FichaItemAdicionarDialogComponent {
  visible = model.required<boolean>();
  jogoId = input.required<number>();
  isMestre = input.required<boolean>();
  fichaId = input.required<number>();

  adicionar = output<AdicionarFichaItemRequest>();
}
```

### Layout do Dialog

```
[p-dialog header="Adicionar Item ao Inventario" style="{width: 'min(700px, 100vw)'}"]
┌─────────────────────────────────────────────────────────────┐
│ [p-inputText placeholder="Buscar item..." styleClass="w-full"] │
│                                                             │
│ Filtros:                                                    │
│  [Select: Tipo ▾] [Select: Raridade ▾] [Limpar]            │
│  (se JOGADOR: raridades restritas ficam com cadeado)        │
│                                                             │
│ [p-table styleClass="p-datatable-sm p-datatable-hoverable"] │
│ ┌────────────────────────────────────────────────────────┐  │
│ │ Nome         │ Tipo      │ Raridade  │ Peso  │ Acao   │  │
│ ├────────────────────────────────────────────────────────┤  │
│ │ Espada Curta │ Arma      │ ●Comum    │ 2 kg  │ [+ Add]│  │
│ │ Arco Curto   │ Arma      │ ●Comum    │ 1 kg  │ [+ Add]│  │
│ │ Espada Longa │ Arma      │ ●Incomum  │ 2.5 kg│ [🔒]  │  │
│ │              │           │ (Requer Mestre)           │  │
│ └────────────────────────────────────────────────────────┘  │
│                                                             │
│ Qtd: [p-inputNumber min=1 max=99 default=1]                 │
│ Notas: [p-inputText placeholder="Observacoes opcionais..."] │
│                                                             │
│ (MESTRE apenas) [forcar adicao: p-toggleButton]             │
│  Tooltip: "Bypassa validacao de requisitos ao adicionar"    │
│                                                             │
│ [aviso amarelo: "Itens Incomum+ requerem aprovacao do       │
│  Mestre. Somente itens Comuns podem ser adicionados         │
│  pelo Jogador."]                                            │
│                                                             │
│                              [Fechar]                       │
└─────────────────────────────────────────────────────────────┘
```

**Logica de acoes na tabela:**
- `podeJogadorAdicionar = true` e usuario e Jogador: botao `[+ Adicionar]` habilitado
- `podeJogadorAdicionar = false` e usuario e Jogador: icone `pi pi-lock` com tooltip
- `podeJogadorAdicionar = false` e usuario e Mestre: botao `[+ Adicionar]` habilitado normalmente

---

## Drawer de Detalhes do Item (ficha-item-detalhe-drawer.component)

### Layout

```
[p-drawer position="right" style="{width: 'min(480px, 100vw)'}"]
┌──────────────────────────────────────────────────────────┐
│ [p-breadcrumb — ex: "Arma > Espada > Espada Longa +2"]  │
│                                                          │
│ [chip raridade com cor]  [nome do item]                  │
│ [tipo completo — Niv. 5+]                                │
│                                                          │
│ PROPRIEDADES                                             │
│  Peso: 3 kg   Valor: 150 po                              │
│  Durabilidade:                                           │
│   80/100 [p-progressBar verde/amarelo/vermelho]          │
│  [tags de propriedades: "Versatil", "Magico"]            │
│                                                          │
│ REQUISITOS PARA EQUIPAR                                  │
│  [icone check/x] Nivel 5 (personagem e Nivel 7)          │
│  [icone check/x] FOR >= 12 (personagem tem FOR 14)       │
│                                                          │
│ EFEITOS QUANDO EQUIPADO                                  │
│  [p-tag] FOR +2   [p-tag] BBA +1                         │
│                                                          │
│ DESCRICAO                                                │
│  [texto da descricao do item]                            │
│                                                          │
│ NOTAS DO JOGADOR                                         │
│  [p-inputTextarea readonly ou editavel]                  │
│                                                          │
│ [Equipar / Desequipar]   [✎ Editar Durabilidade (MESTRE)]│
└──────────────────────────────────────────────────────────┘
```

**Logica de requisitos no drawer:**

```html
<!-- Exibicao de requisitos com status visual -->
@for (req of itemDetalhe.requisitos; track req.tipo + req.descricao) {
  <div class="flex align-items-center gap-2 text-sm py-1">
    <i
      [class]="req.atendido ? 'pi pi-check-circle text-green-500' : 'pi pi-times-circle text-red-500'"
    ></i>
    <span [class.text-color-secondary]="req.atendido">{{ req.descricao }}</span>
  </div>
}
```

---

## Responsividade

| Viewport | Comportamento |
|----------|--------------|
| Mobile (<640px) | Cards largura 100%; acoes apenas icones; drawer de detalhes como `p-drawer position="bottom"` height 80vh; botao "Adicionar" como FAB no canto inferior direito |
| Tablet (640–1024px) | Grid 2 colunas; drawer detalhes `position="right"` 50vw |
| Desktop (>1024px) | Grid 2-3 colunas; drawer detalhes 480px fixo |

---

## Estado Vazio

```html
<!-- Nenhum item no inventario (aba ativa) -->
<div class="flex flex-col align-items-center justify-content-center p-8 gap-4 text-center">
  <i class="pi pi-shield text-color-secondary" style="font-size: 3rem"></i>
  <h3 class="font-semibold m-0">Inventario vazio</h3>
  <p class="text-color-secondary m-0 max-w-20rem">
    Nenhum item no inventario. Adicione itens do catalogo do jogo.
  </p>
  @if (podeEditar()) {
    <p-button
      label="Adicionar primeiro item"
      icon="pi pi-plus"
      (onClick)="adicionarItem.emit()"
    />
  }
</div>
```

---

## Tratamento de Erros

| Erro HTTP | Origem | Mensagem ao Usuario |
|-----------|--------|---------------------|
| 403 | Adicionar item de raridade restrita como Jogador | "Apenas o Mestre pode adicionar itens Incomum ou superiores." |
| 403 | Remover item obrigatorio de classe como Jogador | "Este item e obrigatorio da sua classe e nao pode ser removido." |
| 422 | Requisitos nao atendidos ao equipar | Mensagem descritiva de qual requisito falhou (vem da API). |
| 422 | Item quebrado ao tentar equipar | "Item quebrado nao pode ser equipado. O Mestre pode restaurar a durabilidade." |
| 422 | Itens indestrutiveis ao alterar durabilidade | "Este item e indestrutivel e nao possui durabilidade." |
| 404 | Item nao encontrado | "Item nao encontrado no inventario. Recarregando..." + reload da lista. |
| 500 | Erro generico | Toast "Erro inesperado. Tente novamente." |

---

## Criterios de Aceitacao

### Listagem e Filtragem

- [ ] Aba "Equipamentos" aparece na FichaDetail como 5a aba (deslocando Anotacoes para 6a)
- [ ] Aba exibe badge com contagem de itens equipados quando > 0
- [ ] Tabs "Equipado (N)" e "Inventario (N)" alternam corretamente entre os grupos
- [ ] Barra de capacidade de carga exibe peso total / capacidade em kg com progressbar colorida
- [ ] Progressbar muda de cor: verde (< 50%), amarelo (50-79%), vermelho (>= 80%)
- [ ] Aviso de sobrecarga aparece quando peso >= 80% da capacidade
- [ ] Estado vazio exibido corretamente quando inventario esta vazio

### Cards de Item

- [ ] Card exibe chip de raridade com cor correta
- [ ] Card exibe barra de durabilidade apenas para itens com `duracaoPadrao` definido
- [ ] Barra de durabilidade muda de cor (verde > amarelo > vermelho < 25%)
- [ ] Item quebrado (`duracaoAtual == 0`) exibe badge "QUEBRADO" e fica com opacidade 50%
- [ ] Chips de efeitos exibem ate 3; efeitos extras mostram "+N mais" com tooltip
- [ ] Icone de cadeado e tooltip "Item obrigatorio da classe" para itens com `origem === 'INICIAL_CLASSE'`

### Equipar / Desequipar

- [ ] Botao "Equipar" desabilitado para item quebrado (com tooltip explicativo)
- [ ] Botao "Equipar" para Jogador com requisitos nao atendidos: exibe toast de aviso (nao executa)
- [ ] Botao "Equipar" para Mestre com requisitos nao atendidos: exibe modal de confirmacao
- [ ] Equipar com sucesso atualiza a lista (item move de Inventario para Equipado)
- [ ] Desequipar com sucesso atualiza a lista (item move de Equipado para Inventario)
- [ ] Erro 422 da API exibe mensagem descritiva do requisito que falhou

### Adicionar / Remover Item

- [ ] Botao "+ Adicionar Item" abre dialog de catalogo
- [ ] Dialog de catalogo filtra por nome, tipo e raridade
- [ ] Jogador ve itens de raridade restrita com icone de cadeado (nao pode adicionar)
- [ ] Mestre pode adicionar itens de qualquer raridade
- [ ] Campo "Forcar adicao" visivel apenas para Mestre com tooltip explicativo
- [ ] Adicionar item com sucesso fecha dialog e atualiza a lista
- [ ] Erro 403 ao adicionar item restrito como Jogador exibe mensagem clara
- [ ] Botao "+ Customizado" visivel apenas para Mestre
- [ ] Botao "Remover" visivel para Mestre (qualquer item) e para Jogador (apenas Comuns nao-obrigatorios)
- [ ] Confirmacao de remocao via `p-confirmDialog` antes de executar DELETE
- [ ] Remocao com sucesso remove o card da lista sem reload de pagina

### Drawer de Detalhes

- [ ] Botao "Ver detalhes" abre drawer lateral com informacoes completas do item
- [ ] Requisitos exibidos com check verde (atendido) ou X vermelho (nao atendido)
- [ ] Efeitos exibidos com chip por efeito
- [ ] Barra de durabilidade no drawer com valores numericos
- [ ] Botao "Editar Durabilidade" visivel apenas para Mestre
- [ ] Botao "Equipar/Desequipar" no drawer funcional

### Testes

- [ ] `npx vitest run src/app/features/ficha/components/ficha-equipamentos-tab/` passa sem erros
- [ ] Testes cobrem: render do card, logica `podeRemoverItem`, logica `tentarEquipar`, filtragem no dialog
- [ ] Mock do `FichaItemService` via `vi.fn()` (Vitest, nao Jest)

---

*Produzido por: Business Analyst/PO | 2026-04-04*
