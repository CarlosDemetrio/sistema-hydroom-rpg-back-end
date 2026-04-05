---
name: Design do Sistema de Equipamentos (Spec 016)
description: Decisões de UX para RaridadeItemConfig, TipoItemConfig, ItemConfig, aba Inventário da FichaDetail e passo do Wizard
type: project
---

## Componentes escolhidos por área

### RaridadeItemConfig
- p-table (lista 7 raridades fixas) + p-drawer (formulário)
- p-colorPicker para escolha de cor da raridade
- Preview ao vivo da cor com cálculo de contraste (getContrastColor)
- Color swatch na coluna da tabela: span inline com background-color

### TipoItemConfig
- p-tree para hierarquia de tipos (não p-table)
- Split layout: árvore à esquerda (300px) + painel de detalhe à direita (flex-1)
- Mobile: painel de detalhe vira p-drawer position="bottom" height="50vh"
- p-drawer à direita para formulário (criar/editar tipo)
- Nós desabilitados de excluir quando têm filhos ou itens associados

### ItemConfig (Catálogo)
- p-dataView em grid de cards como padrão, com toggle para p-table (modo denso)
- Card tem border-top colorida pela cor da raridade
- p-drawer largo (max 900px) para formulário com 3 abas: Básico / Requisitos / Efeitos
- p-treeSelect para campo Tipo (hierarquia)
- Listas dinâmicas de requisitos e efeitos com [+ Adicionar] e [x Remover]
- Estado vazio com dois CTAs: "Importar padrão" e "Criar item"
- p-dialog para importar itens padrão (seleção em tabela com checkboxes)

### Aba Inventário (FichaDetail)
- 5ª aba, valor=5 — Anotacoes deslocado para valor=6
- Divisão Equipado / Inventário via p-tabs interno
- Barra de capacidade de carga: peso / (FOR × 3), severidade dinâmica (verde/amarelo/vermelho)
- Item card: border-left colorida pela raridade + p-progressBar para durabilidade (6px alto)
- Item quebrado (dur=0): opacity-60 + badge "QUEBRADO" + Equipar desabilitado para todos
- Modal de adicionar item: p-dialog com p-table do catálogo filtrada por role
- Drawer de detalhes: p-drawer right, 480px desktop / full-width mobile

### Passo do Wizard: Equipamentos Iniciais
- Seção "Inclusos Automaticamente": p-fieldset com grid de cards informativos (só visual)
- Grupos de escolha: p-fieldset + radio cards (input radio sr-only + label clicável)
- Borda azul-primária no card selecionado (border-2 border-primary)
- Resumo de peso via computed() ao vivo
- role="radiogroup" nos grupos + role="list" nos itens obrigatórios

## Tokens CSS de raridade definidos

```css
--rarity-comum-color: #6b7280;       /* Comum */
--rarity-incomum-color: #22c55e;     /* Incomum */
--rarity-raro-color: #3b82f6;        /* Raro */
--rarity-muito-raro-color: #8b5cf6;  /* Muito Raro */
--rarity-epico-color: #f97316;       /* Epico */
--rarity-lendario-color: #eab308;    /* Lendario */
--rarity-unico-color: #ef4444;       /* Unico */
```

Badge de raridade usa cor + alpha 22% como background, cor pura como texto e borda 1px.

## Componentes shared propostos

- `ItemRaridadeBadgeComponent` — badge de raridade reutilizável em toda a aplicação
- `ItemDurabilidadeBarComponent` — barra de durabilidade com lógica de cores críticas

## Regras de permissão no inventário

| Ação | MESTRE | JOGADOR (dono) |
|------|--------|----------------|
| Adicionar item Comum | Sim | Sim |
| Adicionar item Incomum+ | Sim | Nao |
| Equipar/Desequipar | Sim | Sim |
| Remover item | Sim | Só Comuns |
| Editar efeitos/durabilidade | Sim | Nao |

## Sidebar de configurações: reorganização por grupos

3 grupos: PERSONAGEM (Tipos Aptidão, Atributos, Aptidões, Bônus, Classes, Raças, Vantagens, Níveis),
IDENTIDADE (Gêneros, Índoles, Presenças, Prospecção, Membros do Corpo),
EQUIPAMENTOS (Raridades de Itens, Tipos de Itens, Catálogo de Itens)

Separados por p-divider + cabeçalho de seção text-xs uppercase.

**Why:** Foram documentados no dossiê UX-EQUIPAMENTOS.md da Spec 016 em 2026-04-04.
**How to apply:** Ao implementar qualquer componente de equipamentos, seguir estes padrões. Ao atualizar a sidebar, usar a estrutura de grupos.
