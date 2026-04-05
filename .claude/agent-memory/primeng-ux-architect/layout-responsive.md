---
name: Layout e responsividade do projeto
description: Breakpoints, grids e comportamentos de layout acordados para o Klayrah RPG frontend
type: project
---

## Breakpoints primários

| Nome | Largura | Comportamento |
|------|---------|---------------|
| Mobile | < 640px | 1 coluna, scroll vertical, tabs scrollable |
| Small tablet | 640px–768px | 2 colunas |
| Tablet | 768px–1024px | 2–3 colunas, filtros em linha |
| Desktop | 1024px–1280px | 3 colunas, sidebar opcional |
| Wide desktop | > 1280px | 4 colunas |

## Grids estabelecidos

### Grid de jogos (JogosDisponiveisComponent)
`grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4`

### Grid de atributos totais (FichaResumoTab)
`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3`

### Grid de bônus derivados (FichaResumoTab)
`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2`

## Padrões de paginação

- Desktop/Tablet: `p-paginator` com `rowsPerPageOptions=[8, 12, 24]`
- Mobile: botão "Ver mais N jogos" com incremento de 6 itens
- Detectar mobile via `signal(window.innerWidth < 768)`

## Padrões de navegação por abas (mobile)

- `p-tabs` com `scrollable="true"` em vez de quebrar em múltiplas linhas
- Abas com ícones + texto em desktop, ícones + texto curto em tablet, apenas ícones em mobile (se necessário)

## Header sticky

- FichaDetailPage (desktop): header sticky com `position: sticky; top: 64px` (abaixo toolbar)
- Mobile: header NÃO sticky — ocupa espaço estático para não reduzir área de conteúdo

## Touch targets

- Mínimo 44x44px para todos os elementos interativos em mobile
- Botões de ação em cards: preferir texto + ícone, nunca só ícone sem aria-label

## Comportamento de tabelas em mobile

- `responsiveLayout="scroll"` em todos os `p-table`
- Wrapper com `overflow-x: auto`
- `min-width` nas tabelas de atributos: 500px
