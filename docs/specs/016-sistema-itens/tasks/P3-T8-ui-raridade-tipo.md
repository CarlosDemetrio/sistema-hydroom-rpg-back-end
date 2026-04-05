# T8 — Frontend: UI de Raridades e Tipos de Item

> Fase: Frontend — Configuracao | Prioridade: P3
> Dependencias: T1 (backend endpoints disponiveis)
> Bloqueia: T9 (UI Catalogo depende de Tipos e Raridades carregados)
> Estimativa: 1 dia

---

## Objetivo

Implementar as telas de configuracao de `RaridadeItemConfig` e `TipoItemConfig` no painel do Mestre. Seguir exatamente o mesmo padrao visual e de UX das 13 configuracoes existentes (signal store, PrimeNG DataTable, formularios inline ou dialog).

---

## Arquivos a Criar (Angular)

| Arquivo | Descricao |
|---------|-----------|
| `features/mestre/pages/raridades-item/raridades-item.component.ts` | Componente principal |
| `features/mestre/pages/raridades-item/raridades-item.component.html` | Template |
| `features/mestre/pages/raridades-item/raridades-item.component.spec.ts` | Testes |
| `features/mestre/pages/tipos-item/tipos-item.component.ts` | Componente principal |
| `features/mestre/pages/tipos-item/tipos-item.component.html` | Template |
| `features/mestre/pages/tipos-item/tipos-item.component.spec.ts` | Testes |
| `core/models/raridade-item-config.model.ts` | Interface do modelo |
| `core/models/tipo-item-config.model.ts` | Interface do modelo |
| `core/services/raridade-item-config.service.ts` | HTTP service |
| `core/services/tipo-item-config.service.ts` | HTTP service |
| `features/mestre/stores/raridades-item.store.ts` | Signal store (@ngrx/signals) |
| `features/mestre/stores/tipos-item.store.ts` | Signal store |

---

## Modelos TypeScript

```typescript
// raridade-item-config.model.ts
export interface RaridadeItemConfig {
  id: number;
  jogoId: number;
  nome: string;
  cor: string; // hex #RRGGBB
  ordemExibicao: number;
  podeJogadorAdicionar: boolean;
  bonusAtributoMin?: number;
  bonusAtributoMax?: number;
  bonusDerivadoMin?: number;
  bonusDerivadoMax?: number;
  descricao?: string;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

// tipo-item-config.model.ts
export type CategoriaItem = 'ARMA' | 'ARMADURA' | 'ACESSORIO' | 'CONSUMIVEL' | 'FERRAMENTA' | 'AVENTURA';
export type SubcategoriaItem = 'ESPADA' | 'ARCO' | 'LANCA' | 'MACHADO' | 'MARTELO' | 'CAJADO' | 'ADAGA' |
  'ARREMESSO' | 'BESTA' | 'ARMADURA_LEVE' | 'ARMADURA_MEDIA' | 'ARMADURA_PESADA' |
  'ESCUDO' | 'ANEL' | 'AMULETO' | 'BOTAS' | 'CAPA' | 'LUVAS' | 'POCAO' | 'MUNICAO' | 'KIT' | 'OUTROS';

export interface TipoItemConfig {
  id: number;
  jogoId: number;
  nome: string;
  categoria: CategoriaItem;
  subcategoria?: SubcategoriaItem;
  requerDuasMaos: boolean;
  ordemExibicao: number;
  descricao?: string;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}
```

---

## UX Critico — RaridadeItemConfig

### Tabela de Raridades
- Colunas: Cor (chip colorido), Nome, Jogador pode adicionar (badge verde/vermelho), Ranges de bonus, Acoes
- Chip de cor: exibir quadrado colorido com a cor hex da raridade + nome da cor ao lado
- Coluna "Jogador pode adicionar": badge `Sim` verde ou `Nao` vermelho
- Reordenacao via drag-and-drop (padrao das outras configs)

### Formulario de Raridade
- Campo Cor: input tipo `color` do HTML5 (color picker nativo) + input texto para valor hex manual
- Validacao em tempo real: exibir preview do chip com a cor selecionada
- Campos de range (bonusAtributoMin/Max): inputs numericos side-by-side com label "Min" e "Max"
- Toggle para "Jogador pode adicionar"

---

## UX Critico — TipoItemConfig

### Tabela de Tipos
- Colunas: Nome, Categoria (badge), Subcategoria, 2 Maos (icone), Ordem, Acoes
- Agrupamento visual por Categoria (expandable row groups no PrimeNG)
- Badge de Categoria com cor por tipo: ARMA=vermelho, ARMADURA=cinza, ACESSORIO=dourado, CONSUMIVEL=verde, FERRAMENTA=laranja, AVENTURA=azul

### Formulario de Tipo
- Select de Categoria: enum traduzido para portugues
- Select de Subcategoria: filtra as opcoes disponiveis com base na Categoria escolhida
- Toggle "Requer Duas Maos"

### Mapeamento de traducao

```typescript
export const CATEGORIA_LABELS: Record<CategoriaItem, string> = {
  ARMA: 'Arma',
  ARMADURA: 'Armadura',
  ACESSORIO: 'Acessorio',
  CONSUMIVEL: 'Consumivel',
  FERRAMENTA: 'Ferramenta',
  AVENTURA: 'Aventura'
};

export const SUBCATEGORIA_POR_CATEGORIA: Record<CategoriaItem, SubcategoriaItem[]> = {
  ARMA: ['ESPADA', 'ARCO', 'LANCA', 'MACHADO', 'MARTELO', 'CAJADO', 'ADAGA', 'ARREMESSO', 'BESTA'],
  ARMADURA: ['ARMADURA_LEVE', 'ARMADURA_MEDIA', 'ARMADURA_PESADA', 'ESCUDO'],
  ACESSORIO: ['ANEL', 'AMULETO', 'BOTAS', 'CAPA', 'LUVAS'],
  CONSUMIVEL: ['POCAO', 'MUNICAO'],
  FERRAMENTA: ['KIT'],
  AVENTURA: ['OUTROS']
};
```

---

## Integracao no Menu

Adicionar as duas telas na secao "Configuracao de Equipamentos" do menu do Mestre:
- "Raridades de Item" → `/mestre/configuracoes/raridades-item`
- "Tipos de Item" → `/mestre/configuracoes/tipos-item`

Esta secao e separada das 13 configuracoes existentes na UI.

---

## Padroes Angular Obrigatorios

- `inject()` para DI (sem constructor injection)
- `signal()`, `computed()`, `input()`, `output()` para reatividade
- `@if` / `@for` (sem `*ngIf` / `*ngFor`)
- Sem `CommonModule`
- Signal store com `@ngrx/signals`
- Testes com `vi.fn()` (Vitest), nao Jest

---

## Criterios de Aceitacao

- [ ] Tabela de raridades exibe chip colorido com cor da raridade
- [ ] Formulario de raridade tem color picker funcional com preview
- [ ] Tabela de tipos agrupa por categoria com badge colorido
- [ ] Formulario de tipo filtra subcategorias baseado na categoria selecionada
- [ ] CRUD completo funciona (criar, editar, deletar, reordenar raridades)
- [ ] Erro de nome duplicado exibe mensagem clara (nao apenas "409")
- [ ] `npx vitest run` passa para os novos arquivos de teste

---

*Produzido por: Business Analyst/PO | 2026-04-04*
