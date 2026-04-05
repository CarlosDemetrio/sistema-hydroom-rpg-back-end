# T10 — Frontend: UI de ClasseEquipamentoInicial na Tela de ClassePersonagem

> Fase: Frontend — Configuracao | Prioridade: P3
> Dependencias: T9 (catalogo de itens disponivel)
> Bloqueia: nada
> Estimativa: 1 dia

---

## Objetivo

Adicionar uma nova aba "Equipamentos Iniciais" na tela de detalhe de `ClassePersonagem`. O Mestre pode configurar quais itens um personagem recebe ao criar uma ficha com essa classe, incluindo grupos de escolha.

---

## Arquivos a Criar/Editar (Angular)

| Arquivo | Descricao |
|---------|-----------|
| `features/mestre/pages/classes-personagem/classe-detail/classe-equip-inicial/classe-equip-inicial.component.ts` | Novo sub-componente |
| `features/mestre/pages/classes-personagem/classe-detail/classe-equip-inicial/classe-equip-inicial.component.html` | Template |
| `features/mestre/pages/classes-personagem/classe-detail/classe-detail.component.ts` | EDITAR: adicionar aba |
| `core/models/classe-equipamento-inicial.model.ts` | Interface |
| `core/services/classe-equipamento-inicial.service.ts` | HTTP service |

---

## Modelo TypeScript

```typescript
export interface ClasseEquipamentoInicialResponse {
  id: number;
  classeId: number;
  classeNome: string;
  itemConfigId: number;
  itemConfigNome: string;
  itemRaridade: string;
  itemRaridadeCor: string;
  itemCategoria: string;
  obrigatorio: boolean;
  grupoEscolha?: number;
  quantidade: number;
  dataCriacao: string;
}
```

---

## Layout da Aba "Equipamentos Iniciais"

```
[Header] Equipamentos Iniciais — Guerreiro

[Secao: Itens Obrigatorios]
  Lista de itens com obrigatorio=true:
  - [Chip Raridade] [Nome do Item] [Qtd: 1] [Botao: X remover]
  [Botao: + Adicionar Item Obrigatorio]

[Secao: Grupos de Escolha]
  [Grupo 1] — "Arma Primaria"
    - [Chip Raridade] [Espada Longa] [Qtd: 1] [Botao: X]
    - [Chip Raridade] [Machado de Batalha] [Qtd: 1] [Botao: X]
    - [Chip Raridade] [Martelo de Guerra] [Qtd: 1] [Botao: X]
  [Botao: + Adicionar ao Grupo 1]
  [Botao: + Novo Grupo de Escolha]
```

---

## Formulario de Adicionar Item

```
[Select: Item * — busca no catalogo do jogo]
  (autocomplete com filtro por nome, exibe chip de raridade ao lado)

[Input: Quantidade * — number, min=1, max=99]
[Radio: Obrigatorio? / Grupo de Escolha?]
  SE "Grupo de Escolha": [Select: Grupo Existente] ou [Criar Novo Grupo]
```

---

## UX de Grupos de Escolha

- Cada grupo e exibido como um card com borda colorida
- Header do card: "Grupo X — Jogador escolhe 1 de N"
- Reordenar itens dentro do grupo via drag-and-drop
- Botao "Remover Grupo Inteiro" remove todos os itens do grupo

---

## Criterios de Aceitacao

- [ ] Aba "Equipamentos Iniciais" aparece na tela de detalhe de ClassePersonagem
- [ ] Itens obrigatorios e grupos de escolha exibidos separadamente
- [ ] Select de item com busca/autocomplete no catalogo do jogo
- [ ] Adicionar item obrigatorio funciona
- [ ] Adicionar item a grupo existente funciona
- [ ] Criar novo grupo de escolha (novo grupoEscolha) funciona
- [ ] Remover item (DELETE) funciona
- [ ] Chip de raridade exibe cor correta

---

*Produzido por: Business Analyst/PO | 2026-04-04*
