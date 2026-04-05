# UX-EQUIPAMENTOS — Dossiê de Design: Sistema de Equipamentos

**Spec:** 016 — Sistema de Itens  
**Projeto:** Klayrah RPG / ficha-controlador  
**Stack:** Angular 21 + PrimeNG 21.1.1 (tema Aura, Styled mode)  
**Data:** 2026-04-04  
**Autor:** PrimeNG UX Architect

---

## Índice

1. [Sidebar de Configurações — Seção Equipamentos](#1-sidebar-de-configurações--seção-equipamentos)
2. [Tela RaridadeItemConfig](#2-tela-raridadeitemconfig)
3. [Tela TipoItemConfig](#3-tela-tipoitemconfig)
4. [Tela Catálogo de ItemConfig](#4-tela-catálogo-de-itemconfig)
5. [Aba Inventário na FichaDetail](#5-aba-inventário-na-fichadetail)
6. [Passo Equipamentos Iniciais no Wizard](#6-passo-equipamentos-iniciais-no-wizard)
7. [Design Tokens e Paleta de Raridades](#7-design-tokens-e-paleta-de-raridades)
8. [Interações Críticas e Regras de Negócio na UI](#8-interações-críticas-e-regras-de-negócio-na-ui)
9. [Componentes Reutilizáveis Propostos](#9-componentes-reutilizáveis-propostos)

---

## 1. Sidebar de Configurações — Seção Equipamentos

### Problema

A `ConfigSidebarComponent` atual lista 13 itens em sequência plana sem separadores de grupo ou indicação de dependência entre configs. Adicionar 3 novos itens (Raridades, Tipos, Catálogo) nesse modelo aumentaria a lista para 16 itens — comprometendo a escaneabilidade.

### Solução: Agrupamento com `p-divider` + Cabeçalhos de Seção

Reorganizar a sidebar em 3 grupos lógicos com cabeçalhos visuais simples:

```
┌────────────────────────────────────┐
│ Configurações                      │
├────────────────────────────────────┤
│  PERSONAGEM                        │  ← cabeçalho de seção (text-xs uppercase)
│  ⊞ Tipos de Aptidão          [3]   │
│  ∿ Atributos                 [7]   │
│  ★ Aptidões                  [12]  │
│  ⊕ Bônus                     [4]   │
│  ♟ Classes                   [5]   │
│  ☺ Raças                     [3]   │
│  ✦ Vantagens                 [18]  │
│  ↑ Níveis                    [10]  │
│  ─────────────────────────────     │  ← p-divider
│  IDENTIDADE                        │
│  ○ Gêneros                   [3]   │
│  ♥ Índoles                   [6]   │
│  ✨ Presenças                [4]   │
│  □ Prospecção                [2]   │
│  ✋ Membros do Corpo          [6]  │
│  ─────────────────────────────     │  ← p-divider
│  EQUIPAMENTOS                      │  ← NOVO GRUPO
│  ◆ Raridades de Itens        [7]   │
│  ⊞ Tipos de Itens            [12]  │
│  ✦ Catálogo de Itens         [40]  │
└────────────────────────────────────┘
```

### Especificação Técnica do Cabeçalho de Grupo

```typescript
// Estrutura do item de menu atualizada
type MenuItem = {
  label: string;
  description: string;
  icon: string;
  route: string;
  count: number;
};

type MenuGroup = {
  groupLabel: string;
  items: MenuItem[];
};
```

```html
<!-- Template atualizado da sidebar -->
@for (grupo of menuGroups(); track grupo.groupLabel) {
  <div class="px-3 pt-3 pb-1">
    <span class="text-xs font-semibold uppercase tracking-wider text-color-secondary">
      {{ grupo.groupLabel }}
    </span>
  </div>
  @for (item of grupo.items; track item.route) {
    <a [routerLink]="item.route" routerLinkActive="active-menu-item" ...>
      ...
    </a>
  }
  <!-- Não exibir divider no último grupo -->
  @if (!$last) {
    <p-divider styleClass="my-1" />
  }
}
```

### Ícones Propostos para Equipamentos

| Config | Ícone PrimeIcons | Justificativa |
|--------|-----------------|---------------|
| Raridades de Itens | `pi pi-gem` | Gema representa raridade |
| Tipos de Itens | `pi pi-sitemap` | Hierarquia de tipos |
| Catálogo de Itens | `pi pi-book` | Catálogo/livro de itens |

### Responsividade da Sidebar

- **Desktop (>1024px):** Sidebar fixa à esquerda, 260px de largura
- **Tablet (768–1024px):** Sidebar colapsável com toggle — botão `pi pi-bars` no topo
- **Mobile (<768px):** Sidebar vira sheet inferior (`p-drawer position="bottom"`) ou dropdown seletor de seção no topo

> Nota: O comportamento mobile atual (sidebar como coluna acima do conteúdo) está documentado como gap na memória do projeto. A nova seção Equipamentos agrava esse problema — fortemente recomendado implementar o toggle mobile junto com esta spec.

---

## 2. Tela RaridadeItemConfig

### Caso de Uso

O Mestre define as 7 raridades do sistema (Comum, Incomum, Raro, Muito Raro, Epico, Lendario, Unico), cada uma com cor visual e range de bonus medio. A cor é fundamental — aparece em badges em todo o sistema.

### Decisão de Componente: p-table + Color Swatch inline

Para raridades, `p-table` é superior a cards porque:
- 7 linhas fixas — sem paginação, sem virtualização
- A cor é uma coluna de dado, não um título de card
- Permite edição inline com p-inplace (alternativa ao drawer)
- Comparação visual das 7 raridades fica clara em tabela

### Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ [p-toolbar]                                                     │
│  Raridades de Itens                    [+ Nova Raridade]        │
│  Define as raridades disponíveis no catálogo de itens           │
├─────────────────────────────────────────────────────────────────┤
│ [p-table styleClass="p-datatable-sm p-datatable-striped"]       │
│ ┌──────┬───────────────┬──────────┬────────────┬────────────┐  │
│ │ Cor  │ Nome          │ Bônus Mín│ Bônus Máx  │ Ações      │  │
│ ├──────┼───────────────┼──────────┼────────────┼────────────┤  │
│ │ ●    │ Comum         │ +0       │ +1         │ [✎] [🗑]  │  │
│ │ ●    │ Incomum       │ +1       │ +2         │ [✎] [🗑]  │  │
│ │ ●    │ Raro          │ +2       │ +3         │ [✎] [🗑]  │  │
│ │ ●    │ Muito Raro    │ +3       │ +5         │ [✎] [🗑]  │  │
│ │ ●    │ Epico         │ +5       │ +7         │ [✎] [🗑]  │  │
│ │ ●    │ Lendario      │ +7       │ +10        │ [✎] [🗑]  │  │
│ │ ●    │ Unico         │ +10      │ —          │ [✎] [🗑]  │  │
│ └──────┴───────────────┴──────────┴────────────┴────────────┘  │
└─────────────────────────────────────────────────────────────────┘

[p-drawer position="right" width="w-full md:w-30rem"]
  Título: Nova Raridade / Editar Raridade
  ┌─────────────────────────────────┐
  │ Nome *           [__________]   │
  │ Cor *            [p-colorPicker]│
  │                  Preview: [●]   │
  │ Bônus Médio Mín. [___] (integer)│
  │ Bônus Médio Máx. [___] (integer)│
  │ Ordem            [___]          │
  │ Descrição        [________]     │
  │                                 │
  │         [Cancelar] [Salvar]     │
  └─────────────────────────────────┘
```

### Coluna "Cor" — Color Swatch

```html
<!-- Coluna de cor na tabela -->
<ng-template #body let-raridade>
  <td>
    <span
      class="inline-block border-round"
      style="width: 1.5rem; height: 1.5rem; background-color: {{ raridade.cor }};"
      [attr.title]="raridade.cor"
      [attr.aria-label]="'Cor da raridade: ' + raridade.nome + ', ' + raridade.cor"
    ></span>
  </td>
  <td>{{ raridade.nome }}</td>
  ...
</ng-template>
```

### Campo Cor no Drawer — p-colorPicker

```html
<div class="flex flex-col gap-1">
  <label for="corRaridade" class="font-medium">
    Cor <span class="text-red-500">*</span>
  </label>
  <div class="flex align-items-center gap-3">
    <p-colorPicker
      id="corRaridade"
      [ngModel]="form().cor"
      (ngModelChange)="form.update(f => ({...f, cor: $event}))"
      format="hex"
      aria-label="Selecionar cor da raridade"
    />
    <span class="text-sm font-mono text-color-secondary">{{ form().cor }}</span>
    <!-- Preview ao vivo -->
    <span
      class="inline-block border-round px-2 py-1 text-xs font-semibold"
      [style.background-color]="form().cor"
      [style.color]="getContrastColor(form().cor)"
    >
      {{ form().nome || 'Preview' }}
    </span>
  </div>
</div>
```

> A função `getContrastColor()` calcula se o texto deve ser preto ou branco baseado na luminância da cor de fundo, garantindo contraste WCAG 4.5:1.

### Coluna de Bônus com Tag Colorida

```html
<!-- Exibir range de bônus com badge visual -->
<td>
  <p-tag
    [value]="'+' + raridade.bonusMin + ' ~ +' + raridade.bonusMax"
    [style.background]="raridade.cor + '33'"
    [style.color]="raridade.cor"
    [style.border]="'1px solid ' + raridade.cor"
  />
</td>
```

### Estado Vazio

```html
<!-- Quando nenhuma raridade configurada -->
<div class="flex flex-col align-items-center justify-content-center p-8 gap-4 text-center">
  <i class="pi pi-gem text-color-secondary" style="font-size: 3rem"></i>
  <h3 class="text-lg font-semibold m-0">Nenhuma raridade configurada</h3>
  <p class="text-color-secondary m-0 max-w-20rem">
    Raridades definem a escassez e o poder dos itens.
    Crie ao menos 1 raridade antes de adicionar itens ao catálogo.
  </p>
  <p-button label="Criar primeira raridade" icon="pi pi-plus" (onClick)="abrirCriar()" />
</div>
```

### Acessibilidade

- `role="table"` implícito via `p-table`
- `aria-label="Tabela de raridades de itens"` no `p-table`
- `aria-label` no color swatch: "Cor da raridade Comum: #6b7280"
- Botões de ação: `aria-label="Editar raridade Comum"` e `aria-label="Excluir raridade Comum"`
- p-colorPicker já tem suporte a teclado nativo; complementar com `aria-describedby` apontando para o preview

### Responsividade

- **Mobile:** Tabela com `responsiveLayout="scroll"` — colunas de bônus ficam truncadas mas nome e cor são as primeiras colunas (mais importantes)
- Ações viram ícones `p-button [text]` sem label em mobile (largura de coluna fixa: 5rem)

---

## 3. Tela TipoItemConfig

### Decisão de Componente: p-tree vs Tabela Agrupada

**Avaliação comparativa:**

| Opção | Vantagens | Desvantagens |
|-------|-----------|-------------|
| `p-tree` | Visual de hierarquia claro, drag-drop built-in, expansão por nó | Formulários de edição ficam distantes da árvore; criar nó filho requer saber contexto; mobile difícil |
| `p-table` agrupada (pai + filhos indentados) | Familiar ao padrão BaseConfigTable; edição em linha mais simples | Hierarquia não fica evidente com 3+ níveis |
| Split: `p-tree` à esquerda + `p-table` à direita | Melhor dos dois mundos; permite ver pai e filhos simultaneamente | Mais complexo de implementar; quebra em mobile |

**Decisão: p-tree para a listagem + p-drawer para formulário**

Justificativa: A hierarquia de tipos é o dado primário (a razão de existir desta tela). Um `p-tree` com ícone de pasta para pais e ícone de item para folhas é imediatamente legível. A adição de filhos acontece no `p-drawer` onde o campo "Tipo Pai" é um `p-select` (ou `p-treeSelect`).

### Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ [p-toolbar]                                                     │
│  Tipos de Itens                        [+ Novo Tipo Raiz]       │
│  Hierarquia de categorias de itens                              │
├──────────────────────────────┬──────────────────────────────────┤
│ [p-tree selectionMode="none"]│ Painel Vazio (até selecionar)    │
│                              │                                  │
│ ▼ Armas                      │   Selecione um tipo para ver     │
│   ▼ Armas Corpo-a-Corpo      │   seus detalhes ou clique em     │
│     > Espada                 │   [+ Adicionar Subtipo]          │
│     > Machado                │                                  │
│     > Martelo                │                                  │
│   ▷ Armas à Distância        │                                  │
│ ▼ Armaduras                  │                                  │
│   ▷ Leve                     │                                  │
│   ▷ Media                    │                                  │
│   ▷ Pesada                   │                                  │
│ ▷ Acessórios                 │                                  │
│ ▷ Itens Consumíveis          │                                  │
│ ▷ Itens Magicos              │                                  │
│                              │                                  │
│ [+ Novo Tipo]                │                                  │
└──────────────────────────────┴──────────────────────────────────┘

[p-drawer position="right" width="w-full md:w-30rem"]
  Título: Novo Tipo / Editar Tipo
  ┌────────────────────────────────────┐
  │ Nome *              [__________]  │
  │ Tipo Pai            [p-select   ] │  ← nulo = tipo raiz
  │ Ícone               [p-select   ] │  ← lista de pi-icons curada
  │ Descrição           [__________ ] │
  │ Ordem               [___]         │
  │                                   │
  │ [+ Adicionar Subtipo a este tipo] │  ← atalho visual
  │                                   │
  │            [Cancelar] [Salvar]    │
  └────────────────────────────────────┘
```

### Interação ao Clicar em Nó da Árvore

Ao clicar em um tipo na árvore, o painel direito exibe:
- Nome e breadcrumb (ex: "Armas > Corpo-a-Corpo > Espada")
- Quantidade de itens do catálogo que usam esse tipo
- Botões: "Editar", "Adicionar Subtipo", "Excluir" (desabilitado se tem filhos ou itens)

```
┌────────────────────────────────────────────┐
│ Armas > Corpo-a-Corpo                      │
│ ─────────────────────────────────────────  │
│ Ícone: ⚔    Tipos filhos: 3               │
│ Itens no catálogo: 8                       │
│                                            │
│ Subtipos:                                  │
│   • Espada (3 itens)                       │
│   • Machado (2 itens)                      │
│   • Martelo (3 itens)                      │
│                                            │
│  [✎ Editar]  [+ Subtipo]  [🗑 Excluir]   │
│  (Excluir desabilitado: tem 3 subtipos)    │
└────────────────────────────────────────────┘
```

### Template p-tree com Nós Customizados

```html
<p-tree
  [value]="tiposTree()"
  styleClass="w-full border-none"
  selectionMode="single"
  [(selection)]="tipoSelecionado"
  (onNodeSelect)="onNodeSelect($event)"
  aria-label="Hierarquia de tipos de itens"
>
  <ng-template #default let-node>
    <span class="flex align-items-center gap-2">
      <i [class]="node.data.icone || 'pi pi-folder'" class="text-color-secondary"></i>
      <span class="font-medium">{{ node.label }}</span>
      @if (node.data.totalItens > 0) {
        <p-badge
          [value]="node.data.totalItens"
          severity="secondary"
          class="text-xs"
        />
      }
    </span>
  </ng-template>
</p-tree>
```

### Estado Vazio da Árvore

```html
<div class="flex flex-col align-items-center p-6 gap-3 text-center">
  <i class="pi pi-sitemap text-color-secondary" style="font-size: 2.5rem"></i>
  <p class="text-color-secondary m-0">
    Nenhum tipo criado ainda.
    Comece com tipos raiz como "Armas" e "Armaduras".
  </p>
  <p-button label="Criar primeiro tipo" icon="pi pi-plus" size="small" outlined />
</div>
```

### Responsividade

- **Desktop/Tablet:** Layout split (árvore esquerda 300px + painel direito flex-1) via `p-splitter`
- **Mobile:** Apenas a árvore visível. Ao selecionar um nó, o painel de detalhe abre como `p-drawer` na parte inferior

```html
<!-- Mobile: p-drawer na parte inferior para detalhe do nó -->
@if (isMobile()) {
  <p-drawer
    [visible]="tipoSelecionado() !== null"
    position="bottom"
    [style]="{height: '50vh'}"
  >
    <!-- conteúdo do painel de detalhe -->
  </p-drawer>
} @else {
  <!-- painel direito inline -->
}
```

### Acessibilidade

- `aria-label="Hierarquia de tipos de itens"` no p-tree
- `aria-expanded` nos nós pai (gerenciado pelo p-tree automaticamente)
- `role="treeitem"` por nó (gerenciado pelo p-tree)
- Teclas: seta direita expande, seta esquerda colapsa, Enter seleciona
- Botão "Excluir" com `aria-disabled="true"` e `title` explicativo quando desabilitado

---

## 4. Tela Catálogo de ItemConfig

### Visão Geral

Esta é a tela mais complexa do sistema de configurações. Envolve:
- Listagem densa com múltiplos filtros
- Formulário com 4 seções (básico, requisitos, efeitos, template)
- Listas dinâmicas (adicionar/remover requisitos e efeitos)
- Preview de bônus calculados

### Decisão: Grid de Cards vs Tabela

**Cards (p-dataView)** para a listagem principal porque:
- Itens têm muitos atributos visuais relevantes (raridade = cor, tipo = ícone, imagem)
- A cor de raridade como card border/header é muito mais impactante que numa célula de tabela
- Facilita scan visual rápido ("quero ver todos os itens Lendários")
- Em mobile, cards empilhados funcionam muito melhor que tabela scrollável com 8 colunas

**Tabela como modo alternativo** (toggle grid/table) para o Mestre que prefere densidade de informação.

### Layout Geral

```
┌─────────────────────────────────────────────────────────────────┐
│ [p-toolbar]                                                     │
│  Catálogo de Itens                 [◫ Grid] [≡ Tabela]         │
│  [Buscar por nome...        🔍]                                 │
│                                                                 │
│  Filtros: [Tipo ▾] [Raridade ▾] [Nível ▾]  [✕ Limpar Filtros] │
│           [Todos os tipos ] [Todas  ] [1-20]                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [+ Novo Item]  [⚙ Importar padrão D&D 5e]  Mostrando 40 itens│
│                                                                 │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│ │[Lendário]│ │[Raro    ]│ │[Comum   ]│ │[Épico   ]│           │
│ │ Espada   │ │  Arco   │ │  Faca   │ │ Cajado  │           │
│ │ Longa+3  │ │  Élfico  │ │  Simples│ │ Arcano  │           │
│ │ ⚔ Arma  │ │ ⚔ Arma  │ │ ⚔ Arma  │ │  Arma   │           │
│ │ Nível 10 │ │ Nível 5  │ │ Nível 1  │ │ Nível 8  │           │
│ │ 12 kg    │ │ 2 kg     │ │ 1 kg     │ │ 3 kg     │           │
│ │[✎][🗑]  │ │[✎][🗑]  │ │[✎][🗑]  │ │[✎][🗑]  │           │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│                                                                 │
│ [p-paginator rowsPerPageOptions="8,16,32"]                      │
└─────────────────────────────────────────────────────────────────┘
```

### Card de Item no Grid

```html
<p-card
  styleClass="item-card cursor-pointer transition-shadow transition-duration-200 hover:shadow-4"
  [style.border-top]="'3px solid ' + item.raridadeCor"
>
  <ng-template #header>
    <!-- Badge de raridade colorida -->
    <div class="flex justify-content-between align-items-center px-3 pt-3">
      <p-tag
        [value]="item.raridadeNome"
        [style.background-color]="item.raridadeCor + '22'"
        [style.color]="item.raridadeCor"
        [style.border]="'1px solid ' + item.raridadeCor"
        class="text-xs"
      />
      <span class="text-xs text-color-secondary font-mono">Nv. {{ item.nivelMinimo }}</span>
    </div>
  </ng-template>

  <ng-template #content>
    <div class="flex flex-col gap-2">
      <!-- Ícone do tipo + nome -->
      <div class="flex align-items-center gap-2">
        <i [class]="item.tipoIcone + ' text-xl text-color-secondary'"></i>
        <span class="font-semibold text-sm leading-tight">{{ item.nome }}</span>
      </div>

      <!-- Tipo breadcrumb -->
      <span class="text-xs text-color-secondary">{{ item.tipoNomeCompleto }}</span>

      <!-- Stats em linha -->
      <div class="flex gap-3 text-xs">
        <span title="Peso"><i class="pi pi-database"></i> {{ item.peso }} kg</span>
        <span title="Valor"><i class="pi pi-coins"></i> {{ item.valor }}</span>
        <span title="Durabilidade">
          <i class="pi pi-heart"></i> {{ item.durabilidade }}
        </span>
      </div>

      <!-- Efeitos principais (resumo) -->
      @if (item.efeitosResumo.length > 0) {
        <div class="flex flex-wrap gap-1">
          @for (efeito of item.efeitosResumo.slice(0, 2); track efeito) {
            <p-tag [value]="efeito" severity="secondary" class="text-xs" />
          }
          @if (item.efeitosResumo.length > 2) {
            <p-tag [value]="'+' + (item.efeitosResumo.length - 2)" severity="secondary" class="text-xs" />
          }
        </div>
      }
    </div>
  </ng-template>

  <ng-template #footer>
    <div class="flex gap-2 justify-content-end">
      <p-button
        icon="pi pi-pencil"
        size="small"
        text
        [attr.aria-label]="'Editar ' + item.nome"
        (onClick)="editarItem(item); $event.stopPropagation()"
      />
      <p-button
        icon="pi pi-trash"
        size="small"
        text
        severity="danger"
        [attr.aria-label]="'Excluir ' + item.nome"
        (onClick)="confirmarExcluir(item); $event.stopPropagation()"
      />
    </div>
  </ng-template>
</p-card>
```

### Formulário de Criação/Edição — p-drawer Largo

O formulário de item é complexo demais para um drawer simples. Usar `p-drawer` com `style="{width: '100vw', maxWidth: '900px'}"` em desktop, `w-full` em mobile.

```
[p-drawer position="right" style="{width: min(900px, 100vw)}"]

Título: Novo Item / Editar: [Nome do Item]

[p-tabs value="0" scrollable]
  [Tab 0: Dados Básicos]  [Tab 1: Requisitos (2)]  [Tab 2: Efeitos (3)]
  ─────────────────────────────────────────────────────────────────

  ABA 0 — DADOS BÁSICOS
  ┌─────────────────────────────────────────────────────────────┐
  │ Nome *                    [__________________________]      │
  │                                                             │
  │ Tipo *                    [p-treeSelect — hierarquia  ▾]  │
  │ Raridade *                [p-select ▾]  ← com dot de cor   │
  │                                                             │
  │ [grid grid-cols-2 gap-4]                                    │
  │  Peso (kg) *  [p-inputnumber ±]  Valor (ouro) [p-inputnumber ±] │
  │  Durabilidade [p-inputnumber ±]  Nível mínimo [p-inputnumber ±] │
  │                                                             │
  │ Descrição     [p-editor ou p-textarea — 3 linhas]          │
  │ Propriedades  [p-chips — ex: "Versátil", "Pesado"]         │
  │                                                             │
  │ Empunhamento  [p-toggleButton: Uma mão / Duas mãos]        │
  │  (apenas para armas)                                        │
  └─────────────────────────────────────────────────────────────┘

  ABA 1 — REQUISITOS PARA EQUIPAR
  ┌─────────────────────────────────────────────────────────────┐
  │ Lista de requisitos para equipar o item:                    │
  │                                                             │
  │ ┌────────────────────────────────────────────────────────┐  │
  │ │ [p-select: Tipo] ▾  [campo alvo]   [valor]  [🗑]      │  │
  │ │  NIVEL            —                 5       [✕]       │  │
  │ │  ATRIBUTO         [FOR ▾]           12      [✕]       │  │
  │ └────────────────────────────────────────────────────────┘  │
  │                                                             │
  │ [+ Adicionar Requisito]                                     │
  │                                                             │
  │ Tipos de requisito:                                         │
  │  • NIVEL — nível mínimo do personagem                      │
  │  • ATRIBUTO — valor mínimo de um atributo (ex: FOR >= 12)  │
  │  • APTIDAO — possuir aptidão específica                    │
  │  • VANTAGEM — possuir vantagem específica                  │
  └─────────────────────────────────────────────────────────────┘

  ABA 2 — EFEITOS E BÔNUS
  ┌─────────────────────────────────────────────────────────────┐
  │ Bônus concedidos quando o item está EQUIPADO:               │
  │                                                             │
  │ ┌────────────────────────────────────────────────────────┐  │
  │ │ [Tipo ▾]           [Alvo ▾]    [Valor/Fórmula]  [🗑]  │  │
  │ │  BONUS_ATRIBUTO    [FOR ▾]     [+2       ]      [✕]   │  │
  │ │  BONUS_BBA         —           [+1       ]      [✕]   │  │
  │ │  FORMULA_CUSTOM    [SAB ▾]     [SAB/2    ]      [✕]   │  │
  │ └────────────────────────────────────────────────────────┘  │
  │                                                             │
  │ [+ Adicionar Efeito]                                        │
  │                                                             │
  │ Preview de bônus (personagem nível 5, FOR=14):              │
  │  FOR: +2 → total FOR = 16                                  │
  │  BBA: +1 → BBA total = +3                                  │
  │  SAB(7)/2 = 3 → +3 bônus mágico                           │
  └─────────────────────────────────────────────────────────────┘

[Cancelar]                                      [Salvar Item]
```

### Tipos de Efeito — Mapeamento Visual

| Tipo | Campo Alvo | Campo Valor | Ícone |
|------|-----------|-------------|-------|
| `BONUS_ATRIBUTO` | Dropdown atributos | Número inteiro (±) | `pi pi-chart-bar` |
| `BONUS_BBA` | — (nulo) | Número inteiro (±) | `pi pi-sword` |
| `BONUS_BBM` | — (nulo) | Número inteiro (±) | `pi pi-sparkles` |
| `BONUS_VIDA` | — (nulo) | Número inteiro (±) | `pi pi-heart` |
| `BONUS_ESSENCIA` | — (nulo) | Número inteiro (±) | `pi pi-circle` |
| `BONUS_APTIDAO` | Dropdown aptidões | Número inteiro (±) | `pi pi-star` |
| `FORMULA_CUSTOM` | Dropdown atributos (opcional) | FormulaEditorComponent | `pi pi-code` |

### Criação a partir de Template por Raridade

Botão "Importar padrão" na toolbar abre um `p-dialog` de seleção de template:

```
[p-dialog header="Importar Itens Padrão" width="600px"]
┌──────────────────────────────────────────────────────┐
│ Selecione quais itens padrão importar ao catálogo:   │
│                                                      │
│ Filtrar por tipo: [Armas ▾]  Raridade: [Todas ▾]   │
│                                                      │
│ [p-table styleClass="p-datatable-sm"]               │
│ ┌────┬────────────────┬──────────┬──────────────┐   │
│ │ ☐  │ Nome           │ Tipo     │ Raridade     │   │
│ ├────┼────────────────┼──────────┼──────────────┤   │
│ │ ☑  │ Espada Longa   │ Arma     │ ● Comum      │   │
│ │ ☑  │ Escudo Redondo │ Armadura │ ● Comum      │   │
│ │ ☐  │ Arco Longo     │ Arma     │ ● Comum      │   │
│ └────┴────────────────┴──────────┴──────────────┘   │
│                                                      │
│  Aviso: Itens com mesmo nome serao ignorados.        │
│                                                      │
│            [Cancelar]  [Importar 2 itens]            │
└──────────────────────────────────────────────────────┘
```

### Estado Vazio do Catálogo

```html
<div class="flex flex-col align-items-center justify-content-center p-8 gap-4 text-center">
  <i class="pi pi-book text-color-secondary" style="font-size: 3rem"></i>
  <h3 class="font-semibold m-0">Catálogo vazio</h3>
  <p class="text-color-secondary m-0 max-w-25rem">
    Seu catálogo de itens está vazio.
    Importe itens padrão para começar rapidamente ou crie o primeiro item manualmente.
  </p>
  <div class="flex gap-2">
    <p-button label="Importar padrão" icon="pi pi-download" outlined (onClick)="abrirImportar()" />
    <p-button label="Criar item" icon="pi pi-plus" (onClick)="abrirCriar()" />
  </div>
</div>
```

### Acessibilidade — Catálogo

- Grid de cards: `role="list"` no container, `role="listitem"` em cada card
- `aria-label="Item: Espada Longa, Raridade: Raro, Tipo: Arma"` no card
- Filtros: `aria-label="Filtrar por tipo"`, `aria-label="Filtrar por raridade"`
- Toggle grid/tabela: `aria-pressed="true/false"` nos botões de modo de visualização
- Busca: `aria-controls="item-catalog-results"` no input, `id="item-catalog-results"` no container de resultados
- `aria-live="polite"` no contador de resultados ("Mostrando 12 de 40 itens")

### Responsividade — Catálogo

```
Mobile  (<640px):  1 coluna de cards, filtros em acordeão colapsável
Tablet  (640-1024px): 2 colunas de cards
Desktop (>1024px):  3 colunas de cards (padrão) ou 4 em wide
```

```html
<div
  class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
  id="item-catalog-results"
  role="list"
  aria-label="Catálogo de itens"
>
  @for (item of itemsFiltrados(); track item.id) {
    <div role="listitem">
      <app-item-config-card [item]="item" ... />
    </div>
  }
</div>
```

### Modo Tabela (alternativo)

Quando o Mestre alterna para o modo tabela, exibir `p-table` densa com colunas:

| # | Nome | Tipo | Raridade | Peso | Valor | Nível | Efeitos | Ações |
|---|------|------|----------|------|-------|-------|---------|-------|

A coluna Raridade exibe `p-tag` com cor dinâmica. Colunas Efeitos exibe badge "N efeitos".

---

## 5. Aba Inventário na FichaDetail

### Posicionamento na Estrutura de Abas

A aba Equipamentos é inserida como 5ª aba (valor `5`), deslocando Anotações para valor `6`:

```typescript
// ficha-detail.component.ts
// ANTES: [Resumo(0), Atributos(1), Aptidoes(2), Vantagens(3), Anotacoes(4)]
// DEPOIS: [Resumo(0), Atributos(1), Aptidoes(2), Vantagens(3), Equipamentos(5), Anotacoes(6)]
// Nota: manter valor 5 e 6 para evitar conflito com URL params se existirem
```

```html
<p-tab [value]="5">
  <i class="pi pi-shield mr-2"></i>Equipamentos
</p-tab>
```

### Layout Geral da Aba

```
┌─────────────────────────────────────────────────────────────────┐
│ ABA EQUIPAMENTOS                                                │
│                                                                 │
│  Capacidade de Carga: ██████████░░░░░  14.5 / 18 kg  (81%)    │
│  [p-progressBar value="81" color="var(--yellow-500)"]          │
│  Ímpeto de Força (FOR × 3): 18 kg máximo                       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  [Equipado (3)]   [Inventário (5)]    [+ Adicionar Item]        │
│  ─────────────────────────────────────                         │
│                                                                 │
│  EQUIPADO                                                       │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ [◆Raro] Espada Longa +2     ⚔ Arma > Espada              │ │
│  │ Dur: ████████░░  80/100   Peso: 3 kg                      │ │
│  │ Efeitos: FOR +2, BBA +1                                   │ │
│  │                    [Desequipar] [Ver detalhes] [Remover]  │ │
│  ├───────────────────────────────────────────────────────────┤ │
│  │ [◆Incomum] Cota de Malha    🛡 Armadura > Media           │ │
│  │ Dur: ██████████  100/100  Peso: 8 kg                      │ │
│  │ Efeitos: Defesa +3                                        │ │
│  │                    [Desequipar] [Ver detalhes] [Remover]  │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  INVENTÁRIO                                                     │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ [●Comum] Poção de Cura      ⚗ Consumível                  │ │
│  │ Dur: N/A                  Peso: 0.5 kg                    │ │
│  │ Efeitos: +20 Vida ao usar                                 │ │
│  │                      [Equipar]  [Ver detalhes] [Remover]  │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Componente da Aba: FichaEquipamentosTabComponent

```typescript
// Dumb component — recebe tudo via input()
@Component({
  selector: 'app-ficha-equipamentos-tab',
  ...
})
export class FichaEquipamentosTabComponent {
  itensFicha = input.required<FichaItemResponse[]>();
  capacidadeCarga = input.required<number>(); // FOR * 3
  pesoTotal = input.required<number>();
  podeEditar = input.required<boolean>(); // MESTRE ou dono da ficha
  isMestre = input.required<boolean>();

  // Outputs
  equipar = output<number>(); // itemFichaId
  desequipar = output<number>();
  remover = output<number>();
  adicionarItem = output<void>();
  verDetalhes = output<FichaItemResponse>();
}
```

### Barra de Capacidade de Carga

```html
<!-- Barra de peso com severidade dinâmica -->
@let percentualPeso = (pesoTotal() / capacidadeCarga()) * 100;
@let severidadePeso = percentualPeso < 50 ? 'success' : percentualPeso < 80 ? 'warning' : 'danger';

<div class="mb-4">
  <div class="flex justify-content-between align-items-center mb-2">
    <span class="text-sm font-semibold">
      <i class="pi pi-database mr-1"></i>Capacidade de Carga
    </span>
    <span class="text-sm font-mono"
      [class.text-red-500]="percentualPeso >= 80"
      [class.text-yellow-600]="percentualPeso >= 50 && percentualPeso < 80"
    >
      {{ pesoTotal() | number: '1.1-1' }} / {{ capacidadeCarga() }} kg
    </span>
  </div>
  <p-progressBar
    [value]="percentualPeso"
    [style.background]="'var(--surface-200)'"
    styleClass="peso-progress-{{ severidadePeso }}"
    aria-label="Peso carregado: {{ pesoTotal() }} de {{ capacidadeCarga() }} kg"
  />
  @if (percentualPeso >= 80) {
    <small class="text-red-500 flex align-items-center gap-1 mt-1">
      <i class="pi pi-exclamation-triangle text-xs"></i>
      Sobrecarregado! Penalidade de movimento ativa.
    </small>
  }
</div>
```

### Card de Item no Inventário

```html
<!-- app-ficha-item-card.component.html -->
<div
  class="border-1 surface-border border-round-lg p-3 flex flex-col gap-2 surface-card"
  [class.opacity-50]="item.durablidadeAtual === 0"
  [style.border-left]="'3px solid ' + item.raridadeCor"
  [attr.aria-label]="'Item: ' + item.nome + (item.equipado ? ', equipado' : ', no inventário')"
>
  <!-- Header: raridade badge + nome + tipo -->
  <div class="flex align-items-start justify-content-between gap-2">
    <div class="flex align-items-center gap-2 flex-1 min-w-0">
      <i [class]="item.tipoIcone + ' text-color-secondary flex-shrink-0'"></i>
      <div class="flex flex-col min-w-0">
        <span class="font-semibold text-sm truncate" [title]="item.nome">
          {{ item.nome }}
        </span>
        <span class="text-xs text-color-secondary">{{ item.tipoNomeCompleto }}</span>
      </div>
    </div>
    <p-tag
      [value]="item.raridadeNome"
      [style.background-color]="item.raridadeCor + '22'"
      [style.color]="item.raridadeCor"
      [style.border]="'1px solid ' + item.raridadeCor"
      class="text-xs flex-shrink-0"
    />
  </div>

  <!-- Durabilidade -->
  @if (item.durablidadeMaxima > 0) {
    @let percDur = (item.durablidadeAtual / item.durablidadeMaxima) * 100;
    <div>
      <div class="flex justify-content-between text-xs text-color-secondary mb-1">
        <span>Durabilidade</span>
        @if (item.durablidadeAtual === 0) {
          <span class="text-red-500 font-semibold">QUEBRADO</span>
        } @else {
          <span class="font-mono">{{ item.durablidadeAtual }}/{{ item.durablidadeMaxima }}</span>
        }
      </div>
      <p-progressBar
        [value]="percDur"
        [style.height]="'6px'"
        [styleClass]="percDur < 25 ? 'dur-critical' : percDur < 50 ? 'dur-low' : 'dur-ok'"
        [attr.aria-label]="'Durabilidade: ' + item.durablidadeAtual + ' de ' + item.durablidadeMaxima"
      />
    </div>
  }

  <!-- Efeitos em linha -->
  @if (item.efeitos.length > 0) {
    <div class="flex flex-wrap gap-1">
      @for (efeito of item.efeitos; track efeito.id) {
        <p-tag [value]="efeito.resumo" severity="secondary" class="text-xs" />
      }
    </div>
  }

  <!-- Linha de peso -->
  <div class="flex align-items-center gap-1 text-xs text-color-secondary">
    <i class="pi pi-database text-xs"></i>
    <span>{{ item.peso }} kg</span>
  </div>

  <!-- Ações: condicionais por role e estado do item -->
  <div class="flex gap-2 justify-content-end mt-1">
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
        [disabled]="!podeEditar() || item.durablidadeAtual === 0 || !item.requisitosAtendidos"
        [title]="item.durablidadeAtual === 0 ? 'Item quebrado — não pode ser equipado' :
                 !item.requisitosAtendidos ? 'Requisitos não atendidos' : ''"
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

    @if (podeRemover(item)) {
      <p-button
        icon="pi pi-trash"
        size="small"
        text
        severity="danger"
        [attr.aria-label]="'Remover ' + item.nome + ' do inventário'"
        (onClick)="confirmarRemover(item)"
      />
    }
  </div>
</div>
```

### Regras de Visibilidade por Role

| Elemento | MESTRE | JOGADOR (dono) |
|----------|--------|----------------|
| Adicionar item Comum | Sim | Sim |
| Adicionar item Incomum+ | Sim | Nao |
| Equipar/Desequipar | Sim | Sim (proprio) |
| Editar efeitos do item | Sim | Nao |
| Remover item | Sim (qualquer) | Sim (Comuns apenas) |
| Ver bônus calculados | Sim | Sim |
| Editar durabilidade | Sim | Nao |

```typescript
// Logic no componente
protected podeRemover(item: FichaItemResponse): boolean {
  if (this.isMestre()) return true;
  if (this.podeEditar()) {
    // Jogador so pode remover itens Comuns
    return item.raridadeOrdem === 1; // 1 = Comum
  }
  return false;
}

protected podeAdicionar(raridadeOrdem: number): boolean {
  if (this.isMestre()) return true;
  return raridadeOrdem === 1; // Apenas Comum para Jogador
}
```

### Modal de Adição de Item

```
[p-dialog header="Adicionar Item ao Inventário" style="{width: 'min(700px, 100vw)'}"]
┌─────────────────────────────────────────────────────────────┐
│ [Buscar item...                          🔍]                │
│                                                             │
│ Filtros: [Tipo ▾] [Raridade ▾]  (Jogador: só vê Comuns)   │
│                                                             │
│ [p-table styleClass="p-datatable-sm selectable-rows"]      │
│ ┌────────────────────────────────────────────────────────┐  │
│ │ Nome         │ Tipo      │ Raridade  │ Peso  │ Ações  │  │
│ ├────────────────────────────────────────────────────────┤  │
│ │ Espada Curta │ Arma      │ ●Comum    │ 2 kg  │ [+ Add] │ │
│ │ Arco Curto   │ Arma      │ ●Comum    │ 1 kg  │ [+ Add] │ │
│ │ Escudo       │ Armadura  │ ●Incomum  │ 5 kg  │ [🔒]   │ │
│ │              │           │ (requer Mestre)            │  │
│ └────────────────────────────────────────────────────────┘  │
│                                                             │
│ Aviso: Itens acima de Comum requerem aprovação do Mestre.  │
│                                                             │
│                              [Fechar]                       │
└─────────────────────────────────────────────────────────────┘
```

### Drawer de Detalhes do Item

```
[p-drawer position="right" style="{width: 'min(480px, 100vw)'}"]
┌──────────────────────────────────────────────────────────┐
│ [p-breadcrumb: Armas > Espada > Espada Longa +2]        │
│                                                          │
│ ┌──────────────────────────────────────────────────────┐ │
│ │  [●RARO]  Espada Longa +2                           │ │
│ │  Arma > Espada — Nível 5+                           │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                          │
│ PROPRIEDADES                                             │
│  Peso: 3 kg   Valor: 150 po   Durabilidade: 80/100      │
│  [p-progressBar 80% verde]                              │
│  Tags: Versátil, Acuidade                               │
│                                                          │
│ REQUISITOS PARA EQUIPAR                                  │
│  ✓ Nível 5 (personagem é Nível 7)                       │
│  ✓ FOR >= 12 (personagem tem FOR 14)                    │
│                                                          │
│ EFEITOS QUANDO EQUIPADO                                  │
│  ┌──────────────────────────────────────────────────┐   │
│  │ FOR +2                          Atributo         │   │
│  │ BBA +1                          Combate          │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│ DESCRIÇÃO                                                │
│  Uma espada encantada com runas élficas...               │
│                                                          │
│ [Equipar / Desequipar]   [✎ Editar (só Mestre)]        │
└──────────────────────────────────────────────────────────┘
```

### Interação: Equipar Item sem Requisito

Quando o personagem tenta equipar um item sem atender os requisitos:

```typescript
protected tentarEquipar(item: FichaItemResponse): void {
  const requisitosNaoAtendidos = item.requisitos.filter(r => !r.atendido);

  if (requisitosNaoAtendidos.length > 0 && !this.isMestre()) {
    // Mestre pode forçar equipar qualquer item
    this.confirmationService.confirm({
      message: `${item.nome} não atende os requisitos:\n${requisitosNaoAtendidos.map(r => '• ' + r.descricao).join('\n')}\n\nEquipar mesmo assim?`,
      header: 'Requisitos Não Atendidos',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Equipar assim mesmo',
      rejectLabel: 'Cancelar',
      accept: () => this.equipar.emit(item.id),
    });
  } else {
    this.equipar.emit(item.id);
  }
}
```

Visual do botão Equipar com requisito não atendido:
- `[disabled]="true"` para Jogador
- `[disabled]="false"` para Mestre (com tooltip explicativo)
- Badge vermelho sobreposto ao botão: "Requisitos insuficientes"

### Interação: Item com Durabilidade Zero

```html
<!-- Item quebrado — visual diferenciado -->
<div class="border-1 border-red-300 border-round-lg p-3 opacity-60 relative">
  <span
    class="absolute top-0 right-0 m-2 text-xs font-semibold text-red-500"
    aria-label="Item quebrado"
  >
    QUEBRADO
  </span>
  <!-- ... conteúdo do card ... -->
  <p-button
    label="Equipar"
    [disabled]="true"
    title="Item quebrado — conserte antes de equipar"
    aria-disabled="true"
  />
</div>
```

### Estado Vazio do Inventário

```html
<div class="flex flex-col align-items-center justify-content-center p-8 gap-4 text-center">
  <i class="pi pi-shield text-color-secondary" style="font-size: 3rem"></i>
  <h3 class="font-semibold m-0">Inventário vazio</h3>
  <p class="text-color-secondary m-0">
    Nenhum item no inventário. Adicione itens do catálogo.
  </p>
  @if (podeEditar()) {
    <p-button label="Adicionar primeiro item" icon="pi pi-plus" (onClick)="adicionarItem.emit()" />
  }
</div>
```

### Responsividade da Aba Inventário

```
Mobile (<640px):
  - Barra de capacidade: exibir (sempre visível)
  - Toggle Equipado/Inventário: p-tabs scrollable (sem labels longos)
  - Cards: largura 100%, sem ações de texto — apenas ícones
  - Drawer de detalhes: full-width, position="bottom" com height="80vh"
  - Botão "Adicionar": floating action button no canto inferior direito

Tablet (640-1024px):
  - Cards em grid de 2 colunas
  - Drawer de detalhes: position="right", width="50vw"

Desktop (>1024px):
  - Cards em grid de 2-3 colunas
  - Drawer de detalhes: width="480px" fixo
```

---

## 6. Passo Equipamentos Iniciais no Wizard

### Contexto: Wizard de Criação de Ficha

O wizard tem 6 passos. Este documenta o passo de Equipamentos Iniciais (passo 5 ou 6 — posição exata a definir na Spec 006).

### Filosofia de Design

Este passo deve ser **visualmente gratificante** — é o momento em que o jogador "veste" o personagem pela primeira vez. A interface deve criar antecipação e identidade.

### Layout do Passo

```
┌─────────────────────────────────────────────────────────────────┐
│ [p-steps value="4" linear]                                      │
│  ●──●──●──●──●──○──○                                           │
│  1  2  3  4  5  6  7                                            │
│                 ↑ Equipamentos Iniciais                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Equipamentos Iniciais                                          │
│  Como [Nome do Personagem], o [Classe], você começa com:        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  INCLUSOS AUTOMATICAMENTE                               │   │
│  │  (sem custo, fazem parte da sua classe)                 │   │
│  │                                                         │   │
│  │  ┌─────────────────┐  ┌─────────────────┐              │   │
│  │  │ 🛡               │  │ 🗡               │              │   │
│  │  │ Cota de Malha   │  │ Escudo Redondo  │              │   │
│  │  │ Armadura Media  │  │ Armadura Escudo │              │   │
│  │  │ [● Comum]       │  │ [● Comum]       │              │   │
│  │  │ Defesa +3       │  │ Defesa +1       │              │   │
│  │  └─────────────────┘  └─────────────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  ESCOLHA SUA ARMA PRINCIPAL                             │   │
│  │  Selecione 1 opção:                                     │   │
│  │                                                         │   │
│  │  ◉ Espada Longa  ○ Machado de Guerra  ○ Martelo        │   │
│  │                                                         │   │
│  │  ┌────────────────────────────────────────────────┐    │   │
│  │  │ [SELECIONADO] Espada Longa                    │    │   │
│  │  │ ⚔ Arma > Espada    [● Comum]                 │    │   │
│  │  │ BBA +1   Versátil   2.5 kg                   │    │   │
│  │  │ "Uma espada balanceada para guerreiros..."    │    │   │
│  │  └────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  ESCOLHA SEU EQUIPAMENTO SECUNDÁRIO                     │   │
│  │  Selecione 1 opção:                                     │   │
│  │                                                         │   │
│  │  ○ Arco Curto + 20 Flechas  ◉ Adaga + Kit de Viagem   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  Resumo do inventário inicial:                                  │
│  Cota de Malha, Escudo Redondo, Espada Longa,                  │
│  Adaga, Kit de Viagem   — Peso total: 17 kg / 18 kg            │
│  [p-progressBar 94% — amarelo]                                  │
│                                                                 │
│                    [← Anterior]    [Próximo: Revisão →]         │
└─────────────────────────────────────────────────────────────────┘
```

### Componentes por Seção

#### Seção "Inclusos Automaticamente"

```html
<p-fieldset>
  <ng-template #legend>
    <span class="flex align-items-center gap-2">
      <i class="pi pi-check-circle text-green-500"></i>
      <span class="font-semibold">Inclusos Automaticamente</span>
    </span>
  </ng-template>

  <p class="text-sm text-color-secondary mt-0 mb-3">
    Estes itens fazem parte do equipamento padrão de todo {{ classe().nome }}.
  </p>

  <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
    @for (item of itensObrigatorios(); track item.id) {
      <div class="border-1 border-green-300 border-round-lg p-3 surface-card">
        <div class="flex flex-col align-items-center gap-2 text-center">
          <i [class]="item.tipoIcone" style="font-size: 2rem; color: var(--green-500)"></i>
          <span class="font-semibold text-sm">{{ item.nome }}</span>
          <p-tag [value]="item.raridadeNome" severity="secondary" class="text-xs" />
          <span class="text-xs text-color-secondary">{{ item.peso }} kg</span>
        </div>
      </div>
    }
  </div>
</p-fieldset>
```

#### Seção de Escolha (grupo de opções mutuamente exclusivas)

```html
<!-- Para cada grupo de escolha do equipamento inicial da classe -->
@for (grupo of gruposEscolha(); track grupo.id) {
  <p-fieldset>
    <ng-template #legend>
      <span class="flex align-items-center gap-2">
        <i class="pi pi-question-circle text-primary"></i>
        <span class="font-semibold">{{ grupo.titulo }}</span>
        <span class="text-sm text-color-secondary">(escolha {{ grupo.quantidadeEscolher }})</span>
      </span>
    </ng-template>

    <!-- Radio buttons como cards -->
    <div class="flex flex-col gap-3" [attr.role]="'radiogroup'" [attr.aria-label]="grupo.titulo">
      @for (opcao of grupo.opcoes; track opcao.id) {
        <label
          class="border-2 border-round-lg p-3 cursor-pointer transition-all transition-duration-150"
          [class.border-primary]="grupoSelecao()[grupo.id] === opcao.id"
          [class.surface-highlight]="grupoSelecao()[grupo.id] === opcao.id"
          [class.surface-border]="grupoSelecao()[grupo.id] !== opcao.id"
          [class.hover:surface-hover]="grupoSelecao()[grupo.id] !== opcao.id"
          [attr.aria-checked]="grupoSelecao()[grupo.id] === opcao.id"
        >
          <div class="flex align-items-start gap-3">
            <input
              type="radio"
              [name]="'grupo-' + grupo.id"
              [value]="opcao.id"
              [checked]="grupoSelecao()[grupo.id] === opcao.id"
              (change)="selecionarOpcao(grupo.id, opcao.id)"
              class="sr-only"
            />
            <!-- Indicador visual de seleção -->
            <div
              class="flex-shrink-0 w-1rem h-1rem border-circle border-2 mt-1"
              [class.border-primary]="grupoSelecao()[grupo.id] === opcao.id"
              [class.bg-primary]="grupoSelecao()[grupo.id] === opcao.id"
              [class.border-300]="grupoSelecao()[grupo.id] !== opcao.id"
            ></div>

            <!-- Conteúdo da opção -->
            <div class="flex-1">
              <div class="flex align-items-center gap-2 mb-1">
                <i [class]="opcao.tipoIcone" class="text-color-secondary"></i>
                <span class="font-semibold text-sm">{{ opcao.nome }}</span>
                <p-tag
                  [value]="opcao.raridadeNome"
                  [style.background-color]="opcao.raridadeCor + '22'"
                  [style.color]="opcao.raridadeCor"
                  class="text-xs"
                />
              </div>

              <!-- Efeitos resumidos -->
              @if (opcao.efeitosResumo.length > 0) {
                <div class="flex flex-wrap gap-1 mb-1">
                  @for (efeito of opcao.efeitosResumo; track efeito) {
                    <p-tag [value]="efeito" severity="secondary" class="text-xs" />
                  }
                </div>
              }

              <!-- Peso e tipo -->
              <div class="flex gap-3 text-xs text-color-secondary">
                <span>{{ opcao.tipoNomeCompleto }}</span>
                <span>{{ opcao.peso }} kg</span>
              </div>
            </div>
          </div>
        </label>
      }
    </div>
  </p-fieldset>
}
```

### Resumo de Peso em Tempo Real

O resumo de peso é atualizado realmente ao vivo conforme o usuário seleciona opções:

```typescript
protected readonly pesoTotal = computed(() => {
  const pesoObrigatorio = this.itensObrigatorios().reduce((s, i) => s + i.peso, 0);
  const pesoOpcoes = Object.values(this.grupoSelecao()).reduce((soma, opcaoId) => {
    const item = this.todosPossiveisItens().find(i => i.id === opcaoId);
    return soma + (item?.peso ?? 0);
  }, 0);
  return pesoObrigatorio + pesoOpcoes;
});

protected readonly percentualPeso = computed(() =>
  Math.min(100, (this.pesoTotal() / this.capacidadeCarga()) * 100)
);
```

```html
<!-- Resumo dinâmico no rodapé do passo -->
<div class="mt-4 p-3 border-1 surface-border border-round-lg surface-50">
  <div class="flex justify-content-between align-items-center mb-2">
    <span class="text-sm font-semibold">Inventário Inicial</span>
    <span class="text-sm font-mono" [class.text-yellow-600]="percentualPeso() > 80">
      {{ pesoTotal() | number: '1.1-1' }} / {{ capacidadeCarga() }} kg
    </span>
  </div>
  <p-progressBar [value]="percentualPeso()" aria-label="Peso do inventário inicial" />
  <p class="text-xs text-color-secondary mt-2 m-0">
    {{ resumoNomesItens() }}
  </p>
</div>
```

### Estado "Nenhum Equipamento Inicial" (Classe sem config)

Se a classe não tiver equipamentos iniciais configurados pelo Mestre:

```html
<div class="flex flex-col align-items-center p-6 gap-3 text-center">
  <i class="pi pi-shield text-color-secondary" style="font-size: 2.5rem"></i>
  <p class="text-color-secondary m-0">
    {{ classe().nome }} não possui equipamentos iniciais definidos.
    Você pode adicionar itens ao inventário após criar a ficha.
  </p>
  <!-- Botão de continuar ainda ativo — passo é opcional -->
</div>
```

### Acessibilidade do Passo

- Grupos de escolha com `role="radiogroup"` e `aria-label` descritivo
- Inputs radio ocultos visualmente mas presentes para tecnologia assistiva (`class="sr-only"`)
- Labels clicáveis no card inteiro (não apenas no texto)
- `aria-checked` nos labels de card
- Itens obrigatórios: `role="list"` + `role="listitem"`
- Resumo de peso: `aria-live="polite"` para anunciar mudanças ao selecionar opções

### Responsividade

```
Mobile (<640px):
  - Itens obrigatórios: grid 2 colunas
  - Grupos de escolha: cards empilhados em coluna única
  - Resumo de peso: sempre visível (sticky no rodapé do step)

Tablet (640-1024px):
  - Itens obrigatórios: grid 3 colunas
  - Grupos de escolha: 2 colunas (se caber)

Desktop (>1024px):
  - Itens obrigatórios: grid 4 colunas
  - Grupos de escolha: 2 colunas com cards largos
```

---

## 7. Design Tokens e Paleta de Raridades

### Tokens CSS Propostos para Raridades

Definir como CSS custom properties globais no `styles.css` do projeto:

```css
/* Klayrah RPG — Paleta de Raridades de Itens */
:root {
  --rarity-comum-color: #6b7280;       /* gray-500 */
  --rarity-incomum-color: #22c55e;     /* green-500 */
  --rarity-raro-color: #3b82f6;        /* blue-500 */
  --rarity-muito-raro-color: #8b5cf6;  /* violet-500 */
  --rarity-epico-color: #f97316;       /* orange-500 */
  --rarity-lendario-color: #eab308;    /* yellow-500 */
  --rarity-unico-color: #ef4444;       /* red-500 */
}
```

> Referência direta ao D&D 5e com ajuste para o tema escuro (as cores acima têm boa legibilidade tanto em modo claro quanto escuro com o alpha 22% de background).

### Mapeamento Raridade → Badge Severity (fallback)

Para casos onde a cor customizada não está disponível (ex: loading), usar o severity do PrimeNG como fallback:

| Raridade | Severity | Cor |
|---------|----------|-----|
| Comum | `secondary` | Cinza |
| Incomum | `success` | Verde |
| Raro | `info` | Azul |
| Muito Raro | `info` | Azul (sem nativo para violeta) |
| Epico | `warn` | Laranja/Amarelo |
| Lendario | `warn` | Amarelo |
| Unico | `danger` | Vermelho |

### Ícones Sugeridos por Tipo de Item

| Categoria | Ícone PrimeIcons | Alternativa Unicode |
|-----------|-----------------|---------------------|
| Arma corpo-a-corpo | `pi pi-bolt` | — |
| Arma à distância | `pi pi-send` | — |
| Armadura | `pi pi-shield` | — |
| Escudo | `pi pi-stop-circle` | — |
| Acessório/Colar | `pi pi-circle` | — |
| Anel | `pi pi-minus-circle` | — |
| Consumível/Poção | `pi pi-filter` | — |
| Ferramenta | `pi pi-wrench` | — |
| Item Mágico | `pi pi-sparkles` | — |
| Miscellaneous | `pi pi-box` | — |

---

## 8. Interações Críticas e Regras de Negócio na UI

### Equipar item com requisito não atendido

**Jogador:** botão `Equipar` desabilitado com `aria-disabled="true"` e tooltip com lista dos requisitos não atendidos. Nenhuma confirmação necessária — o botão simplesmente não funciona.

**Mestre:** botão `Equipar` habilitado mas com ícone de aviso `pi pi-exclamation-triangle` ao lado. Ao clicar, `p-confirmDialog` pergunta "Requisitos não atendidos. Equipar mesmo assim?" — Mestre pode forçar.

### Item com Durabilidade Zero (Quebrado)

- Card recebe classe `opacity-60` e badge vermelho "QUEBRADO" no canto superior direito
- Botão `Equipar` desabilitado para todos os roles
- Item continua visível no inventário (não é removido)
- Apenas o Mestre pode editar a durabilidade de volta via drawer de detalhes
- Toast informativo ao tentar equipar: "Este item está quebrado e precisa ser consertado antes de ser equipado."

### Sobrecarga de Peso (>100% capacidade)

- Barra de capacidade vira vermelha (`var(--red-500)`)
- Mensagem de aviso abaixo da barra: "Sobrecarregado! Movimentação reduzida."
- Ao adicionar item que vai causar sobrecarga: `p-confirmDialog` com `icon="pi pi-exclamation-triangle"` — "Adicionar este item vai sobrecarregar [Nome] (X kg / Y kg). Continuar?"
- NÃO bloquear a ação — o Mestre pode validar situações de role-play

### Remover item Equipado

Se o item está equipado e o usuário clica em "Remover":
- Confirmação `p-confirmDialog`: "Este item está equipado. Removê-lo vai retirar seus bônus ativos. Confirmar?"
- Ao confirmar: desequipar + remover em sequência

### Salvar seleção do Wizard antes de avançar

- Validar que todos os grupos de escolha têm ao menos 1 opção selecionada
- Botão "Próximo" desabilitado com tooltip "Selecione uma opção em cada grupo" enquanto incompleto
- `aria-describedby` no botão Próximo apontando para texto de validação

---

## 9. Componentes Reutilizáveis Propostos

### ItemRaridadeBadgeComponent (Shared)

```typescript
// src/app/shared/components/item-raridade-badge/item-raridade-badge.component.ts
@Component({
  selector: 'app-item-raridade-badge',
  standalone: true,
  imports: [TagModule],
  template: `
    <p-tag
      [value]="raridade().nome"
      [style.background-color]="raridade().cor + '22'"
      [style.color]="raridade().cor"
      [style.border]="'1px solid ' + raridade().cor"
      [class]="sizeClass()"
      [attr.aria-label]="'Raridade: ' + raridade().nome"
    />
  `
})
export class ItemRaridadeBadgeComponent {
  raridade = input.required<{ nome: string; cor: string }>();
  size = input<'xs' | 'sm' | 'md'>('sm');

  protected readonly sizeClass = computed(() => ({
    'xs': 'text-xs',
    'sm': 'text-sm',
    'md': '',
  })[this.size()]);
}
```

### ItemDurabilidadeBarComponent (Shared)

```typescript
// src/app/shared/components/item-durabilidade-bar/item-durabilidade-bar.component.ts
@Component({
  selector: 'app-item-durabilidade-bar',
  standalone: true,
  imports: [ProgressBarModule, CommonModule],
  template: `
    @if (durablidadeMaxima() > 0) {
      @let perc = (durablidadeAtual() / durablidadeMaxima()) * 100;
      <div>
        <div class="flex justify-content-between text-xs text-color-secondary mb-1">
          <span>Durabilidade</span>
          @if (durablidadeAtual() === 0) {
            <span class="text-red-500 font-semibold">QUEBRADO</span>
          } @else {
            <span class="font-mono">{{ durablidadeAtual() }}/{{ durablidadeMaxima() }}</span>
          }
        </div>
        <p-progressBar
          [value]="perc"
          [style.height]="height()"
          [styleClass]="perc < 25 ? 'dur-critical' : perc < 50 ? 'dur-low' : 'dur-ok'"
          [attr.aria-label]="'Durabilidade: ' + durablidadeAtual() + ' de ' + durablidadeMaxima()"
        />
      </div>
    } @else {
      <span class="text-xs text-color-secondary">Durabilidade: N/A</span>
    }
  `
})
export class ItemDurabilidadeBarComponent {
  durablidadeAtual = input.required<number>();
  durablidadeMaxima = input.required<number>();
  height = input<string>('6px');
}
```

### FichaItemCardComponent (Feature: ficha-detail)

Componente dumb de card de item para a aba inventário. Recebe o item como input e emite eventos tipados.

```typescript
// src/app/features/jogador/pages/ficha-detail/components/ficha-equipamentos-tab/
// ficha-item-card/ficha-item-card.component.ts

@Component({
  selector: 'app-ficha-item-card',
  inputs: ['item', 'podeEditar', 'isMestre'],
  outputs: ['equipar', 'desequipar', 'remover', 'verDetalhes'],
  ...
})
```

---

## Checklist de Implementação

### Fase 1 — Configurações (Mestre)

- [ ] Atualizar `ConfigSidebarComponent` com grupos e novos 3 itens
- [ ] `RaridadeItemConfigComponent` — tabela + drawer + color picker
- [ ] `TipoItemConfigComponent` — p-tree + painel de detalhe + drawer
- [ ] `ItemConfigComponent` — grid de cards + drawer largo com 3 abas
- [ ] `ItemRaridadeBadgeComponent` (shared)
- [ ] `ItemDurabilidadeBarComponent` (shared)

### Fase 2 — Inventário na FichaDetail

- [ ] `FichaEquipamentosTabComponent` — aba completa
- [ ] `FichaItemCardComponent` — card de item
- [ ] Modal de adicionar item (dialog com p-table de catálogo)
- [ ] Drawer de detalhes do item
- [ ] Lógica de permissão por role (podeRemover, podeAdicionar)
- [ ] Barra de capacidade de carga
- [ ] Integrar nova aba no `FichaDetailComponent` (valor 5, deslocar Anotacoes para 6)

### Fase 3 — Wizard

- [ ] `WizardEquipamentosIniciais` — passo do wizard
- [ ] Lógica de grupos de escolha e itens obrigatórios
- [ ] Resumo de peso em tempo real via computed()
- [ ] Persistência da seleção no estado do wizard (signal de rascunho)

---

## Referências

- Componentes existentes: `/ficha-controlador-front-end/src/app/features/jogador/pages/ficha-detail/ficha-detail.component.ts`
- Padrão sidebar atual: `/ficha-controlador-front-end/src/app/features/mestre/pages/config/config-sidebar.component.ts`
- Memória de padrões: `/.claude/agent-memory/primeng-ux-architect/patterns-primeng.md`
- Memória de configs: `/.claude/agent-memory/primeng-ux-architect/project-config-patterns.md`
- Memória da FichaDetail: `/.claude/agent-memory/primeng-ux-architect/ficha-detail-design.md`
