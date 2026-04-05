# Spec 012 — Níveis, Progressão e Level Up Frontend

> Spec: `012-niveis-progressao-frontend`
> Epic: EPIC 10 — Progressão de Personagens
> Status: Pronto para implementação
> Data: 2026-04-02
> Depende de: Spec 006 (Ficha base), Spec 007 (motor de cálculos)
> Análise de negócio: `docs/analises/BA-NIVEIS-PROGRESSAO.md`
> Design UX: `docs/design/LEVEL-UP.md`

---

## 1. Visão Geral

O sistema de progressão conecta três entidades de configuração já 100% implementadas no backend (`NivelConfig`, `PontosVantagemConfig`, `CategoriaVantagem`) com a experiência de jogo do personagem. O frontend precisa:

1. Completar a tela de configuração de Níveis (NivelConfigComponent existe, mas faltam PontosVantagem e ajustes de UX).
2. Criar do zero o componente de configuração de Pontos de Vantagem (zero cobertura atual).
3. Extrair CategoriaVantagem do componente de Vantagens para uma tela dedicada com color picker.
4. Implementar o fluxo completo de level up automático: detecção, notificação, wizard de distribuição de pontos.
5. Adicionar `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis` ao `FichaResumoResponse` (task de backend).

---

## 2. Atores Envolvidos

| Ator | Role | Ações nesta spec |
|------|------|-----------------|
| Mestre | MESTRE | Configura NivelConfig, PontosVantagemConfig, CategoriaVantagem; concede XP; aprova renascimento |
| Jogador | JOGADOR | Visualiza progressão; recebe level up; distribui pontos de atributo e aptidão; gasta pontos de vantagem |
| Sistema | — | Detecta level up após concessão de XP; calcula pontos disponíveis; exibe notificações |

---

## 3. Estado Atual do Frontend (Auditoria 2026-04-02)

### O que existe e funciona

| Componente | Rota | Estado |
|-----------|------|--------|
| `NiveisConfigComponent` | `/mestre/config/niveis` | Funcional — CRUD com drawer; todos os campos incluindo `permitirRenascimento`; sem formatacao de milhar; sem validacao de consistencia XP |
| `VantagensConfigComponent` | `/mestre/config/vantagens` | Funcional — CategoriaVantagem gerenciada **dentro** deste componente como select; sem CRUD dedicado de categorias |
| `NivelConfigService` | core/services | Funcional — estende BaseConfigService |
| `ConfigApiService.listCategoriasVantagem()` | core/services/api | Implementado — todos os 5 métodos HTTP de CategoriaVantagem existem |
| `ConfigApiService.listNiveis()` | core/services/api | Implementado |
| Modelo `CategoriaVantagem` | core/models | Existe (`categoria-vantagem.model.ts`) com campos nome/descricao/cor |
| Rota `niveis` no sidebar | config-sidebar | Existe com badge de contagem |

### O que não existe (gaps confirmados)

| Gap | Severidade | Detalhe |
|-----|-----------|---------|
| `PontosVantagemConfig` | CRITICO | Zero cobertura: sem model TypeScript, sem API service, sem business service, sem componente, sem rota |
| `CategoriaVantagemComponent` dedicado | MEDIO | Gerenciado dentro de VantagensConfig como select. Sem CRUD próprio, sem color picker visual, sem rota dedicada |
| Sidebar sem entrada para CategoriaVantagem e PontosVantagem | MEDIO | Rotas não existem; sidebar não lista essas configurações |
| `FichaResumoResponse` sem pontos disponíveis | CRITICO | `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` ausentes do backend e do modelo TypeScript |
| Level up detection | CRITICO | Nenhum mecanismo de detecção de mudança de nível após concessão de XP |
| Endpoint conceder XP | CRITICO | `PUT /api/v1/fichas/{id}` aceita `xp` e `renascimentos` via `UpdateFichaRequest`, mas faltam: (a) endpoint dedicado de concessão de XP na UI do Mestre; (b) campo `xp` não está exposto na tela de detalhes da ficha para o Mestre editar |
| `LevelUpDialogComponent` | CRITICO | Não existe — wizard de distribuição de pontos pós-level-up ausente |
| Painel de distribuição de atributos (level up) | CRITICO | Não existe |
| Painel de distribuição de aptidões (level up) | CRITICO | Não existe |
| Renascimento UI | MEDIO | Sem botão nem confirmação de renascimento na ficha |
| Validação de consistência de XP | BAIXO | NivelConfigComponent não alerta se XP de nível N < N-1 ou se há buracos na sequência |
| Formatação de milhar em XP | BAIXO | Valores como 1800000 exibidos sem separador |

---

## Seção A — Telas de Configuração do Mestre

### A.1 NivelConfigComponent — Ajustes

**Rota existente:** `/mestre/config/niveis`
**Arquivo:** `src/app/features/mestre/pages/config/configs/niveis-config/niveis-config.component.ts`
**Status:** Funcional, mas incompleto em UX.

#### Ajustes necessários

**A.1.1 Coluna `permitirRenascimento` na tabela**

A tabela (`BaseConfigTableComponent`) não exibe a coluna `permitirRenascimento`. O array `columns` atual tem apenas: nivel, xpNecessaria, pontosAtributo, pontosAptidao, limitadorAtributo. Adicionar coluna com ícone/tag visual.

**Regra de negócio:** O Mestre precisa identificar visualmente quais níveis permitem renascimento (níveis 31-35 no template padrão). O toggle existe no drawer de edição mas não na tabela de listagem.

**A.1.2 Formatação de milhar em xpNecessaria**

Valores como `1800000` devem ser exibidos como `1.800.000` na tabela e no campo do formulário. Usar `p-inputnumber` com `useGrouping: true` (já existe no drawer, verificar se está ativo).

**A.1.3 Validação de consistência (aviso inline)**

Ao renderizar a tabela, verificar:
1. Se algum nível N tem `xpNecessaria` menor que o nível N-1 → exibir badge "XP inválida" na linha.
2. Se há lacunas na sequência de níveis (ex: existe nível 4 e nível 6 mas não nível 5) → exibir aviso global abaixo do cabeçalho.

**Regra de negócio:** A lacuna não bloqueia o Mestre de salvar (o backend não valida isso), mas a UI deve alertar porque personagens "pulam" o nível inexistente.

**A.1.4 Seção de Pontos de Vantagem integrada**

Ver A.2 abaixo. O NivelConfigComponent deve exibir, ao lado ou abaixo da tabela principal, os dados de `PontosVantagemConfig` com acumulado por nível.

#### Dossiê de Regras — NivelConfigComponent

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Campo `nivel` | Input | 0-35; único por jogo | min=0, max=35 | MESTRE |
| Campo `xpNecessaria` | Input | >= 0; deveria ser >= xp do nível anterior | client-side warning | MESTRE |
| Campo `pontosAtributo` | Input | >= 0; ganhos AO atingir o nível | min=0 | MESTRE |
| Campo `pontosAptidao` | Input | >= 0; ganhos AO atingir o nível | min=0 | MESTRE |
| Campo `limitadorAtributo` | Input | >= 1; teto máximo de qualquer atributo neste nível | min=1 | MESTRE |
| Toggle `permitirRenascimento` | Checkbox | default false; apenas níveis >= 31 no template padrão | — | MESTRE |
| Botão Excluir | Ação | Aviso se fichas ativas estão neste nível (backend soft delete — não bloqueia) | confirmação modal | MESTRE |
| Tabela (coluna xpNecessaria) | Display | Formatar com separador de milhar | — | MESTRE, JOGADOR |
| Linha com XP inválida | Display | Badge de aviso quando xpNecessaria < xp do nível anterior | client-side computed | MESTRE |
| Aviso de lacunas | Display | Mensagem quando sequência tem buracos | client-side computed | MESTRE |

---

### A.2 PontosVantagemConfigComponent — Criar do Zero

**Rota nova:** `/mestre/config/pontos-vantagem`
**Arquivo novo:** `src/app/features/mestre/pages/config/configs/pontos-vantagem-config/pontos-vantagem-config.component.ts`

#### Visão geral

Tabela de configuração de quantos pontos de vantagem o personagem ganha ao atingir cada nível. A tabela é esparsa: o Mestre só cadastra entradas para níveis com ganhos (ausência de entrada = zero pontos naquele nível).

#### Layout

```
Pontos de Vantagem por Nível
[busca]                        [+ Novo]

Nível   Pontos Ganhos   Acumulado   Ações
  1          1               1       [Editar] [Excluir]
  2          1               2       [Editar] [Excluir]
  ...
 10          3              12  *    [Editar] [Excluir]
 20          3              21  *    [Editar] [Excluir]

* Marcos especiais destacados com badge dourado
Níveis sem entrada: 0 pontos (não listados)
```

**Coluna "Acumulado":** soma dos pontosGanhos de todos os registros com `nivel <= nivelAtual`. Calculada no frontend, não no backend.

**Destaque de marcos especiais:** linhas com `pontosGanhos >= 2` recebem badge dourado (severity="warn") na coluna de pontos.

#### Formulário (Drawer)

Campos:
- `nivel` (obrigatório): InputNumber 1-35. Nível 0 não ganha pontos (personagem recém-criado).
- `pontosGanhos` (obrigatório): InputNumber >= 0, default 1.

#### Dossiê de Regras — PontosVantagemConfigComponent

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Campo `nivel` | Input | 1-35; único por jogo (nível 0 não tem pontos de vantagem) | min=1, max=35, unique | MESTRE |
| Campo `pontosGanhos` | Input | >= 0; valor 0 é válido (nível sem ganho explícito) | min=0 | MESTRE |
| Coluna Acumulado | Display | Soma acumulada do nível 1 até o nível da linha | client-side computed | MESTRE |
| Badge dourado | Display | Exibido quando pontosGanhos >= 2 | client-side | MESTRE |
| Ausência de registro | Regra | Nível sem registro = 0 pontos de vantagem naquele nível | — | — |
| Excluir registro | Ação | Apenas soft delete; pontos já ganhos por fichas são mantidos no saldo | confirmação modal | MESTRE |

---

### A.3 CategoriaVantagemComponent — Extrair e Completar

**Rota nova:** `/mestre/config/categorias-vantagem`
**Arquivo novo:** `src/app/features/mestre/pages/config/configs/categorias-vantagem-config/categorias-vantagem-config.component.ts`

#### Contexto atual

`CategoriaVantagem` hoje existe apenas como um `<p-select>` dentro do drawer de `VantagensConfigComponent`. Há um `configApi.listCategoriasVantagem()` funcional, mas sem CRUD próprio. Não há como criar, editar ou excluir categorias separadamente.

#### O que criar

CRUD completo com:
- Tabela: nome, cor (chip colorido), descrição (truncada), ordemExibicao, contagem de vantagens vinculadas.
- Drawer de criação/edição com:
  - `nome` (obrigatório): InputText max 100 chars.
  - `descricao` (opcional): Textarea.
  - `cor` (opcional): `p-colorpicker` (output hex) + campo manual hex + preview do chip colorido em tempo real.
  - `ordemExibicao` (obrigatório): InputNumber >= 0, default 0.

#### Color Picker

PrimeNG tem `p-colorpicker` (módulo `ColorPickerModule`). Retorna valor em formato hex. O campo deve:
1. Exibir o seletor de cores visual.
2. Sincronizar com campo de texto hex (#RRGGBB).
3. Exibir preview: chip com o nome da categoria na cor selecionada.
4. Valor default: sem cor (null) — a categoria sem cor usa a cor padrão da UI.

#### Aviso ao excluir categoria com vantagens vinculadas

Ao clicar em excluir, verificar se existem `VantagemConfig` usando aquela categoria:
- Se sim: modal de confirmação com texto "X vantagens perderão a categoria ao excluir. Deseja continuar?"
- A contagem de vantagens vinculadas pode ser exibida na coluna da tabela.

**Premissa:** O backend não expõe a contagem de vantagens por categoria diretamente. O frontend deve calcular a partir da lista de `VantagemConfig` carregada no `ConfigStore`.

#### Dossiê de Regras — CategoriaVantagemComponent

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Campo `nome` | Input | Max 100 chars; único por jogo | @NotBlank, @Size(max=100) | MESTRE |
| Campo `cor` | ColorPicker | Formato #RRGGBB; opcional | regex /^#[0-9A-Fa-f]{6}$/ | MESTRE |
| Preview do chip | Display | Atualiza em tempo real conforme nome e cor digitados | client-side | MESTRE |
| Campo `ordemExibicao` | Input | >= 0; controla ordem de exibição na seleção de vantagens | min=0 | MESTRE |
| Excluir categoria com vantagens | Ação | Aviso com contagem; vantagens perdem a categoria (categoriaNome fica null/vazio) | modal de confirmação | MESTRE |
| Tabela — coluna Vantagens | Display | Contagem de VantagemConfig vinculadas, calculada no frontend | — | MESTRE |

---

## Seção B — Level Up Automático

O level up é disparado quando o Mestre concede XP via `PUT /api/v1/fichas/{id}` (campo `xp` no `UpdateFichaRequest`). O backend calcula o `nivel` a partir da tabela `NivelConfig` e retorna a `Ficha` atualizada com `nivel` atualizado. O frontend detecta a mudança de nível comparando o valor anterior com o recebido.

### B.1 Concessão de XP pelo Mestre

**Onde:** FichaDetailPage (tanto na view do Mestre quanto, futuramente, em um painel de sessão).

#### UI de Concessão de XP

Painel visível apenas para MESTRE no FichaHeaderComponent ou FichaResumoTab:

```
XP: 12.500 / 15.000 (próximo nível)
[+XP]  ← botão que abre um mini-form inline ou dialog
```

O botão `[+XP]` abre um `p-dialog` simples com:
- Campo: `quantidade` (InputNumber, obrigatório, min=1).
- Botão Confirmar: chama `PUT /api/v1/fichas/{id}` com `{ xp: fichaAtual.xp + quantidade }`.
- Após resposta: atualiza o sinal `ficha()` com a ficha retornada e detecta level up.

**Regra de negócio:** XP é read-only para o Jogador. Apenas o Mestre concede XP. O Jogador vê o valor mas não pode editá-lo.

#### Dossiê de Regras — Painel de XP

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Display de XP atual | Display | Formatar com separador de milhar | — | MESTRE, JOGADOR |
| Display de XP próximo nível | Display | Buscar no NivelConfig o próximo nível acima do atual | — | MESTRE, JOGADOR |
| Botão `[+XP]` | Ação | Visível e habilitado apenas para MESTRE | roleGuard/hasRole | MESTRE |
| Campo quantidade XP | Input | min=1, obrigatório | Validators.min(1), required | MESTRE |
| Barra de progresso XP | Display | (xpAtual - xpNivelAtual) / (xpProximoNivel - xpNivelAtual) × 100% | — | MESTRE, JOGADOR |

---

### B.2 Detecção de Level Up

Após chamar `PUT /api/v1/fichas/{id}` com novo XP, comparar:

```typescript
const nivelAnterior = this.ficha().nivel;
// Após atualizar o sinal com a ficha retornada:
if (this.ficha().nivel > nivelAnterior) {
  this.onLevelUp(nivelAnterior, this.ficha().nivel);
}
```

`onLevelUp()` dispara:
1. Toast especial de level up (seção B.3).
2. Animação CSS no FichaHeaderComponent (seção B.4).
3. Abertura automática do `LevelUpDialogComponent` se `pontosAtributoDisponiveis > 0 || pontosAptidaoDisponiveis > 0` (seção B.5).

**Pré-requisito:** `FichaResumo` deve incluir `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis`. Ver Task T5 (backend).

---

### B.3 Toast de Level Up

Toast com estilo especial diferenciado do toast padrão:

```
summary: "NÍVEL {N}!"
detail: "{nome} subiu para o Nível {N}! Distribua os pontos ganhos."
severity: 'success'
life: 8000
styleClass: 'level-up-toast'
```

CSS: borda esquerda amarela (`var(--yellow-500)`), fundo com gradiente sutil, summary em negrito dourado.

---

### B.4 Animação de Level Up

Classe CSS `ficha-header-level-up` adicionada ao FichaHeaderComponent por 1,5s:

```css
@keyframes levelUpFlash {
  0%   { box-shadow: 0 0 0 0 var(--yellow-400); }
  50%  { box-shadow: 0 0 0 16px rgba(234, 179, 8, 0.3); }
  100% { box-shadow: 0 0 0 0 rgba(234, 179, 8, 0); }
}
```

A classe é removida automaticamente após a animação via `setTimeout(1500)`.

---

### B.5 Badge de Pontos Pendentes (FichaHeaderComponent)

Enquanto `pontosAtributoDisponiveis > 0 || pontosAptidaoDisponiveis > 0`, exibir no header da ficha:

- Botão/badge com label dinâmica: ex. "3 atrib. + 5 apt. para distribuir"
- severity="warn"
- Clique abre o `LevelUpDialogComponent`
- Visível para MESTRE e para o Jogador dono da ficha

**Regra de negócio:** O badge persiste entre sessões. Os pontos pendentes ficam disponíveis até serem distribuídos. Ao carregar o FichaDetailPage, se `pontosDisponiveis > 0`, o badge já aparece (sem depender de um level up recente).

---

### B.6 LevelUpDialogComponent — Wizard de Distribuição

**Arquivo:** `src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/level-up-dialog.component.ts`
**Tipo:** Smart — orquestra requests; contém sub-components dumb para cada step.

#### Estrutura

`p-dialog` com `p-stepper` de 3 steps:

```
[1. Atributos] → [2. Aptidões] → [3. Vantagens]
```

**Abertura:** Automaticamente ao detectar level up E quando o usuário clica no badge de pontos pendentes.

**Fechamento:** Confirmação se há pontos não distribuídos.

#### Step 1 — Distribuição de Pontos de Atributo

**Componente dumb:** `LevelUpAtributosStepComponent`
**Inputs:** `atributos: FichaAtributoResponse[]`, `pontosDisponiveis: number`, `limitadorAtributo: number`
**Output:** `distribuicaoChanged: Record<string, number>` (sigla → pontos adicionados nesta sessão)

**Regras:**
- Contador de pontos restantes: destaque warn quando > 0, success quando = 0.
- Botão `[+]`: desabilitado quando `pontosRestantes === 0` OU `atributo.total >= limitadorAtributo`.
- Botão `[-]`: desabilitado quando `pontosAdicionados[sigla] === 0` (não pode retirar além do que adicionou nesta sessão).
- Barra de progresso por atributo: `total / limitadorAtributo` — amarela ao atingir 90%, vermelha ao atingir 100%.
- Aviso quando todos os atributos atingiram o limitador e ainda há pontos: "Todos os atributos atingiram o teto. Os N pontos restantes não podem ser distribuídos."

**Salvar:** `PUT /api/v1/fichas/{id}/atributos` com os valores acumulados.

#### Step 2 — Distribuição de Pontos de Aptidão

**Componente dumb:** `LevelUpAptidoesStepComponent`
**Layout:** `p-accordion` por `tipoAptidao`; apenas o campo `base` é editável.
**Campos readonly:** `sorte` e `classe` (com tooltip "Controlado pelo Mestre").
**Sem limitador de atributo** — não há barra de progresso.

**Salvar:** `PUT /api/v1/fichas/{id}/aptidoes` com os valores acumulados.

#### Step 3 — Pontos de Vantagem (Informativo)

**Componente dumb:** `LevelUpVantagensStepComponent`
**Conteúdo:** exibir saldo de pontos de vantagem (ganhos totais, gastos totais, disponíveis).
**Ação:** botão "Ir para Vantagens" (fecha o dialog e navega para a aba Vantagens do FichaDetail).

**Regra:** Pontos de vantagem NÃO são distribuídos neste wizard. A compra de vantagens ocorre na aba Vantagens do FichaDetail (pois envolve pré-requisitos, categorias e nivel máximo).

#### Navegação entre steps

- "Próximo" está sempre disponível — o usuário pode avançar sem distribuir todos os pontos.
- Pontos não distribuídos persistem no backend (não há perda).
- Ao tentar fechar com pontos pendentes: `p-confirmDialog` com "Os pontos serão mantidos para distribuir mais tarde."

#### Estados do wizard

| Estado | Comportamento |
|--------|--------------|
| Loading (saving) | Botões desabilitados, spinner visível |
| Erro ao salvar | Toast de erro; estado local mantido (não reverter) |
| Todos no limite | Aviso `p-message` severity="warn" |
| Sem pontos | Dialog não abre; badge não aparece |

#### Dossiê de Regras — LevelUpDialogComponent

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Abertura automática | Trigger | Apenas quando pontosAtrib > 0 ou pontosApt > 0 | sinal computed | MESTRE, JOGADOR dono |
| Contador Step 1 | Display | Decrementa a cada `[+]` clicado | client-side signal | MESTRE, JOGADOR dono |
| Botão `[+]` | Ação | Desabilitado quando pontos = 0 OU total >= limitador | computed disabled | MESTRE, JOGADOR dono |
| Botão `[-]` | Ação | Desabilitado quando pontosAdicionados[sigla] === 0 | computed disabled | MESTRE, JOGADOR dono |
| Barra de progresso | Display | `total / limitadorAtributo * 100` | — | MESTRE, JOGADOR dono |
| Salvar Step 1 | Request | PUT /api/v1/fichas/{id}/atributos | server-side | MESTRE, JOGADOR dono |
| Salvar Step 2 | Request | PUT /api/v1/fichas/{id}/aptidoes | server-side | MESTRE, JOGADOR dono |
| Fechar com pontos | Ação | p-confirmDialog antes de fechar | client-side | MESTRE, JOGADOR dono |
| Ir para Vantagens | Navegação | Fecha dialog + navega/scrolls para aba Vantagens | — | MESTRE, JOGADOR dono |

---

### B.7 Compra de Vantagens com Pontos de Vantagem

**Localização:** `FichaVantagensTabComponent` (já existe).

A aba de vantagens deve exibir o saldo de pontos disponíveis e bloquear a compra quando o saldo for insuficiente.

**Regras:**
- Display "Pontos disponíveis: N" no topo da aba.
- Botão "Comprar" desabilitado quando `custo > pontosVantagemDisponiveis`.
- Ao comprar: `POST /api/v1/fichas/{id}/vantagens`. Após sucesso, recarregar resumo para atualizar saldo.
- Gastos são irreversíveis.
- O saldo é calculado pelo backend: `SUM(pontosGanhos dos níveis atingidos) - SUM(custoPago de FichaVantagem)`.

**Nota:** O campo `pontosVantagemRestantes` já existe no template de FichaVantagensTabComponent mas está mockado como 0 (`[pontosVantagemRestantes]="0"`). Precisa ser conectado ao valor real vindo do `FichaResumo`.

---

## Seção C — Renascimento (Nível 31+)

### C.1 Conceito de Negócio

Renascimento é um evento raro disponível apenas em personagens que atingiram um nível configurado com `permitirRenascimento = true` (padrão: níveis 31-35). Ao renascer:

- O nível do personagem é resetado para 1.
- O XP volta a 0 (ou ao XP mínimo para o nível 1).
- O contador `Ficha.renascimentos` é incrementado.
- Pontos de vantagem, vantagens compradas e bônus permanentes de renascimentos anteriores são mantidos.

**Quem inicia:** o Mestre decide quando o personagem pode renascer (não é automático como o level up).

### C.2 Endpoint de Renascimento

**Gap crítico:** o backend não tem endpoint dedicado para renascimento. Atualmente `PUT /api/v1/fichas/{id}` aceita `renascimentos` no body — mas não executa a lógica de reset de nível e XP. Esta é uma task de backend (T10).

**Endpoint proposto:** `POST /api/v1/fichas/{id}/renascer`
- Body: `{ confirmado: true }` (requer confirmação explícita)
- Executa: reset xp=0, nivel=0 ou 1 (conforme NivelConfig), renascimentos++
- Role: MESTRE apenas

### C.3 UI de Renascimento

**Onde:** FichaDetailPage, visível apenas para MESTRE, apenas quando `ficha.nivel` está em um nível com `permitirRenascimento = true`.

**Componente:** Botão "Iniciar Renascimento" no FichaHeaderComponent ou aba Resumo, com `p-confirmDialog` de duas etapas:

1. Aviso de consequências: "Ao renascer, o nível de {nome} será resetado para 1 e o XP voltará a zero. Vantagens e bônus de renascimento são mantidos. Esta ação não pode ser desfeita."
2. Confirmação final: campo de confirmação textual ("Digite RENASCER para confirmar") ou botão com severity="danger".

**Após renascer:**
- Toast de confirmação.
- FichaDetail recarregado com os novos valores.
- Badge do número de renascimentos atualizado no header.

#### Dossiê de Regras — Renascimento

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Botão "Iniciar Renascimento" | Ação | Visível apenas se `nivelConfig[ficha.nivel].permitirRenascimento === true` | client-side | MESTRE |
| Modal Etapa 1 | Info | Exibe consequências do renascimento | — | MESTRE |
| Modal Etapa 2 | Confirmação | Requer confirmação explícita (botão danger) | client-side | MESTRE |
| Após confirmar | Request | POST /api/v1/fichas/{id}/renascer | server-side | MESTRE |
| Display renascimentos | Display | Badge no header com o contador de renascimentos | — | MESTRE, JOGADOR |

---

## 4. Requisitos Não Funcionais

- **Performance:** PontosVantagemConfigComponent deve calcular o acumulado de forma reativa via `computed()`, nunca em loops imperativos no template.
- **Acessibilidade:** Contador de pontos restantes com `aria-live="polite"`; controles +/- com `aria-label` descritivos; dialog com `aria-modal="true"` e foco inicial no primeiro campo.
- **Mobile:** LevelUpDialogComponent em tela cheia em viewport < 768px; controles +/- com touch target mínimo de 48px.
- **Consistência visual:** Usar PrimeNG 21 (Aura Styled). Sem CSS custom em arquivos de componente (PrimeFlex apenas). Color picker via `p-colorpicker` nativo.
- **Signals:** Todo estado reativo via `signal()`, `computed()`, `model()`. Sem BehaviorSubject ou RxJS para estado.

---

## 5. Épico e User Stories

### EPIC 10 — Progressão de Personagens

**Descrição:** Como Mestre e Jogador, quero que a progressão de nível seja fluida e visualmente clara — configurável pelo Mestre e celebrada para o Jogador.

---

**US-01: Configurar Pontos de Vantagem por Nível**

Como Mestre com role MESTRE,
Quero configurar quantos pontos de vantagem os personagens ganham em cada nível,
Para que o sistema de vantagens seja balanceado conforme a campanha.

Cenário 1: Criar configuração de pontos de vantagem
  Dado que sou Mestre com jogo selecionado
  Quando acesso `/mestre/config/pontos-vantagem` e crio um registro com nivel=10, pontosGanhos=3
  Então o registro é criado e aparece na tabela com acumulado calculado
  E a linha tem destaque de marco especial (badge dourado, pois pontosGanhos >= 2)

Cenário 2: Tentar criar com nível duplicado
  Dado que já existe registro com nivel=10 para o jogo atual
  Quando tento criar outro com nivel=10
  Então recebo erro "Já existe configuração para o nível 10"

---

**US-02: Gerenciar Categorias de Vantagem com Color Picker**

Como Mestre com role MESTRE,
Quero criar e editar categorias de vantagem com cor personalizada,
Para organizar visualmente as vantagens por tema na interface do Jogador.

Cenário 1: Criar categoria com cor
  Dado que acesso `/mestre/config/categorias-vantagem`
  Quando crio uma categoria com nome="Combate", cor="#E74C3C"
  Então vejo o chip colorido no preview antes de salvar
  E após salvar a tabela exibe o chip com a cor correta

Cenário 2: Excluir categoria com vantagens vinculadas
  Dado que a categoria "Combate" tem 5 vantagens vinculadas
  Quando clico em Excluir "Combate"
  Então vejo modal de confirmação com "5 vantagens perderão a categoria"
  E posso confirmar ou cancelar

---

**US-03: Receber Level Up e Distribuir Pontos de Atributo**

Como Jogador ou Mestre com ficha aberta,
Quero ser notificado imediatamente quando o personagem sobe de nível e poder distribuir os pontos de atributo,
Para que a progressão seja celebrada e os pontos sejam alocados estrategicamente.

Cenário 1: Level up detectado após concessão de XP
  Dado que o Mestre concede XP suficiente para subir de nível
  Quando o backend retorna a ficha com nivel maior que o anterior
  Então toast de level up com estilo dourado é exibido por 8 segundos
  E animação de "flash" ocorre no header da ficha
  E o dialog de level up é aberto automaticamente no Step 1

Cenário 2: Distribuir pontos com limitador de nível
  Dado que estou no Step 1 do wizard com 3 pontos disponíveis
  Quando o atributo FOR já está no limitador (total = limitadorAtributo)
  Então o botão [+] de FOR está desabilitado com tooltip "Limite do nível atingido"
  E posso distribuir os 3 pontos em outros atributos

---

**US-04: Distribuir Pontos de Aptidão após Level Up**

Como Jogador ou Mestre com ficha aberta,
Quero distribuir pontos de aptidão no campo `base` de cada aptidão,
Para que meu personagem evolua nas habilidades que utiliza.

Cenário 1: Distribuir ponto em aptidão
  Dado que estou no Step 2 do wizard com 5 pontos de aptidão disponíveis
  Quando incremento o campo base da aptidão "Espadas"
  Então o contador de pontos restantes diminui de 5 para 4

Cenário 2: Campos sorte e classe são readonly
  Dado que estou no Step 2 do wizard
  Quando tento editar os campos sorte ou classe de qualquer aptidão
  Então eles estão bloqueados com tooltip "Controlado pelo Mestre"

---

**US-05: Ver Pontos de Vantagem Disponíveis e Comprar Vantagens**

Como Jogador ou Mestre com ficha aberta,
Quero ver meu saldo de pontos de vantagem e poder comprar vantagens,
Para que o personagem evite acumular pontos sem uso e evolua suas capacidades especiais.

Cenário 1: Saldo exibido na aba Vantagens
  Dado que minha ficha tem pontosVantagemDisponiveis=3
  Quando acesso a aba Vantagens da ficha
  Então vejo "Pontos disponíveis: 3" no topo da aba

Cenário 2: Botão comprar desabilitado quando saldo insuficiente
  Dado que meu saldo é 1 ponto e uma vantagem custa 3 pontos
  Quando visualizo a lista de vantagens disponíveis
  Então o botão "Comprar" daquela vantagem está desabilitado

---

**US-06: Pontos Pendentes Persistentes**

Como Jogador ou Mestre com ficha aberta,
Quero que pontos não distribuídos permaneçam disponíveis mesmo após fechar o dialog,
Para poder distribuí-los quando for conveniente, sem urgência.

Cenário 1: Badge de pontos pendentes ao carregar ficha
  Dado que minha ficha tem 2 pontos de atributo por distribuir
  Quando acesso o FichaDetail
  Então o badge "2 atrib. para distribuir" aparece no header imediatamente
  E posso clicar nele para abrir o wizard

Cenário 2: Fechar dialog com pontos pendentes
  Dado que estou no wizard com 1 ponto não distribuído
  Quando clico no X do dialog
  Então vejo confirmação "Os pontos serão mantidos para distribuir mais tarde"
  E ao confirmar o dialog fecha sem perda de pontos

---

**US-07: Iniciar Renascimento (Mestre)**

Como Mestre com role MESTRE,
Quero iniciar o renascimento de um personagem no nível 31+,
Para que o personagem inicie um novo ciclo com bônus permanentes acumulados.

Cenário 1: Renascimento disponível no nível correto
  Dado que a ficha está no nível 31 (NivelConfig com permitirRenascimento=true)
  Quando acesso a ficha como Mestre
  Então vejo o botão "Iniciar Renascimento" no header da ficha

Cenário 2: Renascimento não disponível em nível menor
  Dado que a ficha está no nível 20 (NivelConfig com permitirRenascimento=false)
  Quando acesso a ficha como Mestre
  Então o botão "Iniciar Renascimento" não está visível

Cenário 3: Confirmar renascimento
  Dado que cliquei em "Iniciar Renascimento" e li as consequências
  Quando confirmo no modal de confirmação
  Então o backend reseta o nível para 1 e o XP para 0
  E o contador de renascimentos é incrementado
  E a ficha recarrega com os novos valores

---

## 6. Pontos em Aberto / Perguntas para Stakeholder

**P-01 (CRITICO):** O endpoint `POST /api/v1/fichas/{id}/renascer` precisa ser criado no backend. O comportamento exato do reset (nível resetado para 0 ou para 1? XP vai para 0 ou para o XP mínimo do nível 1?) precisa ser definido antes da implementação da UI (Task T10).

**P-02 (CRITICO):** O `FichaResumoResponse` precisa de 3 novos campos: `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`. Como esses campos são calculados:
- `pontosAtributoDisponiveis` = soma de `pontosAtributo` de todos os NivelConfig com `nivel <= fichaAtual.nivel` - pontos já gastos (como rastrear pontos gastos em atributos?)
- **Premissa atual:** O backend sabe os pontos ganhos por nível mas não rastreia explicitamente os pontos gastos em atributos. Como o sistema distingue pontos do wizard de level up de pontos editados diretamente pelo Mestre? Esta é uma decisão arquitetural que precisa ser definida antes de T5.

**P-03 (MEDIO):** PontosVantagemConfig deve ter rota própria no sidebar (`/mestre/config/pontos-vantagem`) ou deve ser integrado como seção dentro de NivelConfig? O BA-NIVEIS-PROGRESSAO.md sugere ambas as abordagens. Decisão do PO necessária antes de T3.

**P-04 (MEDIO):** No Step 3 do wizard (Vantagens), o botão "Ir para Vantagens" deve fechar o dialog e navegar para a aba Vantagens do FichaDetail. Como a navegação entre abas é controlada atualmente no FichaDetailComponent? Verificar se usa `p-tabs` com binding de aba selecionada ou rota.

**P-05 (BAIXO):** Validação de consistência de XP (XP crescente + sem lacunas) deve bloquear o Mestre de salvar ou apenas avisar? A spec atual propõe apenas aviso (não bloqueio). Confirmar com PO.

---

## 7. Checklist de Validação UX

- [ ] Wireframe do `LevelUpDialogComponent` validado (ver `docs/design/LEVEL-UP.md` — já detalhado)
- [ ] Comportamento mobile do dialog (fullscreen < 768px, touch targets 48px) validado
- [ ] Color picker de CategoriaVantagem: paleta pré-definida vs. seletor livre — confirmar com UX
- [ ] Posição do badge de pontos pendentes no FichaHeaderComponent validada
- [ ] Toast de level up: texto em português e estilo dourado validados
- [ ] Tabela de NivelConfig: edição inline vs. drawer — UX atual usa drawer; BA sugere inline para tabela de 36 linhas. Confirmar se o drawer é suficiente ou se inline é prioritário
- [ ] Exibição de XP na ficha: onde exatamente fica o campo XP e o botão `[+XP]` do Mestre?
