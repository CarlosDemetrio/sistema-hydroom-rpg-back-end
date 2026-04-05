# T9 — Frontend: UI de Catalogo de Itens (ItemConfig + Efeitos + Requisitos)

> Fase: Frontend — Configuracao | Prioridade: P3
> Dependencias: T2 (backend), T8 (raridades e tipos carregados)
> Bloqueia: T10 (ClasseEquipamentoInicial depende de itens disponiveis)
> Estimativa: 3 dias

---

## Objetivo

Implementar a tela de configuracao do catalogo de itens do jogo. Esta e a tela mais complexa da Spec 016 no frontend: listagem com filtros multicriteria, formulario com abas (Dados Basicos, Efeitos, Requisitos), formulario dinamico de ItemEfeito (campos diferentes por tipo de efeito), e formulario de ItemRequisito.

---

## Arquivos a Criar (Angular)

| Arquivo | Descricao |
|---------|-----------|
| `features/mestre/pages/itens-config/itens-config.component.ts` | Listagem com filtros |
| `features/mestre/pages/itens-config/itens-config.component.html` | Template de listagem |
| `features/mestre/pages/itens-config/item-form/item-form.component.ts` | Formulario de item (3 abas) |
| `features/mestre/pages/itens-config/item-form/item-form.component.html` | Template de formulario |
| `features/mestre/pages/itens-config/item-efeito-form/item-efeito-form.component.ts` | Formulario de efeito dinamico |
| `features/mestre/pages/itens-config/item-requisito-form/item-requisito-form.component.ts` | Formulario de requisito |
| `core/models/item-config.model.ts` | Interfaces completas |
| `core/services/item-config.service.ts` | HTTP service |
| `features/mestre/stores/itens-config.store.ts` | Signal store |

---

## Modelos TypeScript

```typescript
// item-config.model.ts
export type TipoItemEfeito = 'BONUS_ATRIBUTO' | 'BONUS_APTIDAO' | 'BONUS_DERIVADO' |
  'BONUS_VIDA' | 'BONUS_ESSENCIA' | 'FORMULA_CUSTOMIZADA' | 'EFEITO_DADO';

export type TipoRequisito = 'NIVEL' | 'ATRIBUTO' | 'BONUS' | 'APTIDAO' | 'VANTAGEM' | 'CLASSE' | 'RACA';

export interface ItemEfeitoResponse {
  id: number;
  tipoEfeito: TipoItemEfeito;
  atributoAlvoId?: number;
  atributoAlvoNome?: string;
  aptidaoAlvoId?: number;
  aptidaoAlvoNome?: string;
  bonusAlvoId?: number;
  bonusAlvoNome?: string;
  valorFixo?: number;
  formula?: string;
  descricaoEfeito?: string;
}

export interface ItemRequisitoResponse {
  id: number;
  tipo: TipoRequisito;
  alvo?: string;
  valorMinimo?: number;
}

export interface ItemConfigResponse {
  id: number;
  jogoId: number;
  nome: string;
  raridade: { id: number; nome: string; cor: string };
  tipo: { id: number; nome: string; categoria: string };
  peso: number;
  valor?: number;
  duracaoPadrao?: number;
  nivelMinimo: number;
  propriedades?: string;
  descricao?: string;
  ordemExibicao: number;
  efeitos: ItemEfeitoResponse[];
  requisitos: ItemRequisitoResponse[];
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface ItemConfigResumoResponse {
  id: number;
  nome: string;
  raridade: { id: number; nome: string; cor: string };
  tipo: { id: number; nome: string; categoria: string };
  peso: number;
  valor?: number;
  nivelMinimo: number;
}
```

---

## Layout da Tela de Listagem (itens-config.component)

```
[Header] Catalogo de Itens do Jogo
  [Botao: + Novo Item]

[Filtros]
  [Select: Raridade] [Select: Categoria/Tipo] [Input: Buscar por nome]
  [Botao: Limpar filtros]

[DataTable PrimeNG]
Colunas: [Chip Raridade] [Nome] [Tipo] [Peso] [Valor] [Nivel Min] [Efeitos (count)] [Acoes]

Efeitos count: badge com numero de efeitos. Tooltip mostra lista de efeitos.
```

---

## Formulario de Item (item-form.component) — 3 Abas

### Aba 1: Dados Basicos

```
[Nome *]                 [Raridade * — Select com chip colorido]
[Tipo * — Select com categoria]   [Nivel Minimo * — input number, min=1]
[Peso (kg) * — input decimal]     [Valor (po) — input number, opcional]
[Durabilidade padrao — input number, null=indestrutivel]
[Propriedades — input text, ex: "versatil, finura, arremesso"]
[Descricao — textarea, 2000 chars]
```

**UX:** Chip colorido ao lado do Select de Raridade (mostra a cor quando selecionada). Select de Tipo agrupa por categoria.

### Aba 2: Efeitos

```
[Lista de efeitos existentes]
  - Chip de tipo (BONUS_DERIVADO, etc.)
  - Descricao do alvo + valor
  - [Botao: X remover]

[Botao: + Adicionar Efeito] → abre ItemEfeitoForm inline ou dialog
```

### Aba 3: Requisitos

```
[Lista de requisitos existentes]
  - Tipo (NIVEL, ATRIBUTO, etc.) + alvo + valor minimo
  - [Botao: X remover]

[Botao: + Adicionar Requisito] → abre ItemRequisitoForm
```

---

## ItemEfeitoForm — Formulario Dinamico

Este e o componente mais critico da T9. O formulario muda completamente baseado no `tipoEfeito` selecionado:

```
[Select: Tipo de Efeito *] — muda o que aparece abaixo

SE BONUS_ATRIBUTO:
  [Select: Atributo Alvo *]  [Valor Fixo * — input number]
  [Descricao do Efeito]

SE BONUS_APTIDAO:
  [Select: Aptidao Alvo *]   [Valor Fixo * — input number]
  [Descricao do Efeito]

SE BONUS_DERIVADO:
  [Select: Bonus Alvo *]     [Valor Fixo * — input number]
  [Descricao do Efeito]

SE BONUS_VIDA ou BONUS_ESSENCIA:
  [Valor Fixo * — input number]
  [Descricao do Efeito]

SE FORMULA_CUSTOMIZADA:
  [Select Alvo Opcional: Atributo / Bonus / Nenhum]
  [Editor de Formula — FormulaEditor component reutilizado de Spec 007]
  [Validar Formula — botao]
  [Descricao do Efeito]

SE EFEITO_DADO:
  [Valor Fixo * — posicoes para avancar no dado]
  [Descricao: automaticamente preenchida: "+N posicoes no dado de prospeccao"]
```

**Implementacao via `@if` com sinal reativo:**

```typescript
tipoEfeitoSelecionado = signal<TipoItemEfeito | null>(null);

// No template:
@if (tipoEfeitoSelecionado() === 'BONUS_DERIVADO') {
  <app-select-bonus [jogoId]="jogoId()" [control]="form.controls.bonusAlvoId" />
}
```

---

## Traducoes de Labels

```typescript
export const TIPO_EFEITO_LABELS: Record<TipoItemEfeito, string> = {
  BONUS_ATRIBUTO: 'Bonus em Atributo',
  BONUS_APTIDAO: 'Bonus em Aptidao',
  BONUS_DERIVADO: 'Bonus em Bonus Derivado',
  BONUS_VIDA: 'Bonus de Vida',
  BONUS_ESSENCIA: 'Bonus de Essencia',
  FORMULA_CUSTOMIZADA: 'Formula Customizada',
  EFEITO_DADO: 'Efeito de Dado'
};

export const TIPO_REQUISITO_LABELS: Record<TipoRequisito, string> = {
  NIVEL: 'Nivel Minimo',
  ATRIBUTO: 'Atributo Minimo',
  BONUS: 'Bonus Minimo',
  APTIDAO: 'Aptidao Minima',
  VANTAGEM: 'Possui Vantagem',
  CLASSE: 'Classe Especifica',
  RACA: 'Raca Especifica'
};
```

---

## Criterios de Aceitacao

- [ ] Listagem de itens com filtros por raridade, categoria e nome funciona
- [ ] Chip colorido de raridade exibe cor correta na listagem
- [ ] Formulario de item tem 3 abas funcionais (Dados Basicos, Efeitos, Requisitos)
- [ ] ItemEfeitoForm muda campos dinamicamente ao trocar tipo de efeito
- [ ] FormulaEditor reutilizado em FORMULA_CUSTOMIZADA com validacao de formula
- [ ] Adicionar e remover ItemEfeito funciona sem refresh de pagina
- [ ] Adicionar e remover ItemRequisito funciona
- [ ] Erro de nome duplicado exibe mensagem amigavel
- [ ] `npx vitest run` passa para novos arquivos de teste

---

*Produzido por: Business Analyst/PO | 2026-04-04*
