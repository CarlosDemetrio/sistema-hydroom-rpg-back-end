---
name: Padrões de componentes PrimeNG estabelecidos
description: Componentes PrimeNG escolhidos para casos de uso recorrentes no projeto Klayrah RPG
type: project
---

## Componentes escolhidos por caso de uso

### Abas de seção (FichaDetailPage e similares)
- Usar `p-tabs` + `p-tablist` + `p-tab` + `p-tabpanels` + `p-tabpanel` (PrimeNG 18+ API nova)
- NÃO usar o antigo `p-tabView` — API mudou na v18
- Módulo: `TabsModule` de `primeng/tabs`
- Propriedade `scrollable="true"` para mobile

### Tabelas de dados (atributos, aptidoes, configs)
- `p-table` com `styleClass="p-datatable-sm p-datatable-striped"`
- `responsiveLayout="scroll"` para mobile
- `ng-template #header` e `ng-template #body` para templates customizados
- Valores numéricos: `font-family: monospace` via classe CSS inline

### Agrupamento de listas (aptidoes por tipo, vantagens por categoria)
- `p-fieldset` com `legend` = nome do grupo, `toggleable="true"`
- Para listas de vantagens: `p-tag` como cabeçalho de grupo + cards abaixo

### Cards de configuração / vantagens / jogos
- `p-card` com `styleClass` customizada
- Layout interno: `flex flex-col justify-between h-full` para altura uniforme no grid
- Efeito hover: `transition: box-shadow 200ms ease, transform 200ms ease`

### Barras de progresso
- `p-progressBar` com `value` em porcentagem (0-100)
- Vida: cor `var(--green-500)` (crítico < 25%: `var(--red-500)`)
- Essência: cor `var(--blue-400)`
- XP: cor `var(--primary-color)`
- Sempre com `aria-label` descritivo

### Avatar de personagem
- `p-avatar` com `label` = primeira letra do nome, `size="xlarge"`, `shape="circle"`
- Background: `var(--primary-color)`

### Confirmações destrutivas
- `ConfirmationService.confirm()` com `acceptButtonStyleClass: 'p-button-danger'`
- Sempre incluir `<p-confirmdialog />` no template da página host
- Mensagem deve descrever o impacto ("Esta ação não pode ser desfeita")

### Busca com ícone
- `p-iconfield` + `p-inputicon` + `pInputText`
- Módulos: `IconFieldModule`, `InputIconModule`, `InputTextModule`

### Skeleton loading
- Sempre cobrir as 3 áreas: header, stats e conteúdo
- Grid de 8 cards skeleton para listas de jogos
- Grid de 4 cards skeleton para atributos

### Badges de status
- `p-tag severity="success"` — ATIVO, APROVADO
- `p-tag severity="warn"` — PENDENTE, NPC
- `p-tag severity="danger"` — REJEITADO, BANIDO, INATIVO, erro
- `p-tag severity="secondary"` — estado neutro/inativo
- `p-tag severity="info"` — nível do personagem, informativo

### Toast de feedback
- Sucesso: `severity="success"`, duração 3s
- Erro: `severity="error"`, duração 5s (mais tempo para ler)
- Provido via `ToastService` (abstração interna do projeto)

## Tipografia RPG

- Títulos de personagem e jogos: `font-family: Georgia, 'Times New Roman', serif`
- Valores numéricos (atributos, ímpeto, totais): `font-family: 'Courier New', Courier, monospace`
- Corpo e labels: `font-family: var(--font-family)` (padrão Aura)
