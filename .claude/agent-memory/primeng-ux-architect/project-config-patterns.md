---
name: Padrões arquiteturais da área de Configurações do Mestre
description: Padrões de componente, estrutura e UX estabelecidos para as 13 telas de config do Mestre no projeto Klayrah
type: project
---

## Padrão arquitetural atual (todas as 13 config pages)

- **BaseConfigTableComponent** — componente DUMB reutilizado em todos os 13 configs. Recebe `titulo`, `subtitulo`, `labelNovo`, `items`, `loading`, `columns`, `canReorder`, `rowsPerPage`. Emite `onCreate`, `onEdit`, `onDelete`, `onReorder`, `onSearch`.
- **BaseConfigComponent** — Directive abstrata com CRUD genérico, signals de estado (`items`, `dialogVisible`, `editMode`, `currentEditId`), `loadData()`, `save()`, `delete()`, `confirmDelete()`.
- **p-drawer** (posição right) para formulários — NÃO p-dialog. Largura: `w-full md:w-30rem` (configs simples) ou `w-full md:w-35rem` (configs com sub-recursos/tabs).
- **p-tabs** dentro do drawer para configs com sub-recursos (Classes, Raças, Vantagens).
- **p-confirmDialog** + `ConfirmationService` para exclusões — sempre incluir `<p-confirmDialog />` no template.
- `p-input-number` com `[showButtons]="true"` para campos numéricos.
- `p-select` para dropdowns (FK: TipoAptidao, Categoria, etc.).
- Indicador de jogo ativo: bandeirinha `surface-100` com `pi pi-book` e nome do jogo — REPETIDO em cada componente (código duplicado — oportunidade de refatoração).
- Loading local via `signal(false)` + `loading.set(true/false)` em volta do subscribe (LoadingInterceptor global também existe).

## Sub-recursos (Classe, Raça, Vantagem)

- 3 abas: dados gerais + 2 abas de sub-recursos (bônus, aptidões/classes/pré-requisitos).
- Aba de sub-recursos fica DISABLED enquanto `editMode() === false` (o usuário precisa criar o item primeiro).
- Após criar, o componente automaticamente muda para a aba de sub-recursos: `this.activeTab.set('bonus')`.
- Sub-recursos são add/remove via API direta — sem form intermediário (apenas p-select + botão "Adicionar").
- `refreshSelected*()` após cada add/remove para re-fetch do item e atualizar badges de contagem.
- Badge de contagem de sub-recursos nas tabs: `<span class="ml-1 badge-atributo">N</span>`.

## Configurações simples (sem sub-recursos)

- Gênero, Índole, Presença, Prospecção, TipoAptidao, MembroCorpo: formulário só com Nome + Descrição + Ordem.
- Esses não precisam de tabs no drawer.

## Reordenação

- `canReorder=true` em todas as tabelas exceto Níveis (onde a ordem é determinística pelo campo `nivel`).
- Drag-and-drop funcional na UI mas o endpoint de PATCH reordenar ainda não é chamado — apenas exibe toast informativo.
- **Gap crítico**: a reordenação salva apenas localmente, NÃO persiste no servidor.

## Sidebar de configurações

- `ConfigSidebarComponent` com 13 itens em ordem ALFABÉTICA/ARBITRÁRIA, sem indicação de dependência ou ordem sugerida.
- Falta indicação visual de qual item precisa ser configurado antes de outro (ex: TiposAptidão antes de Aptidões).
- Mobile: sidebar aparece como coluna acima do conteúdo (col-12) — sem toggle/collapse.

## Fórmulas nos formulários

- `formulaImpeto` (AtributoConfig): campo pInputText com font-family monospace. SEM validação visual. SEM preview.
- `formulaBase` (BonusConfig): idem. SEM validação visual. SEM preview.
- `formulaCusto` (VantagemConfig): idem.
- Hint text de variáveis disponíveis: texto estático (`<small>`), não dinâmico.

**Why:** Ainda não existe um FormulaEditorComponent. É o maior gap de UX na área de configs.
**How to apply:** Ao projetar qualquer tela que envolva fórmulas, planejar o FormulaEditorComponent como componente reutilizável.
