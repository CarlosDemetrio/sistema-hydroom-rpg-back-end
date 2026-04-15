# Spec 026 — UX Gaps: Fluxo do Mestre e Perfil de Usuário

**Data:** 2026-04-15
**Autor:** BA/PO — ficha-controlador
**Status:** Rascunho — aguarda validação do PO

---

## 1. Visão Geral

### O que existe hoje

O fluxo do Mestre cobre as seguintes áreas:
- Lista de jogos (`/mestre/jogos`) — CRUD de jogos
- Detalhe de jogo (`/mestre/jogos/:id`) — participantes, fichas, ações
- Formulário de jogo (`/mestre/jogos/:id/edit`) — editar nome/descrição
- Módulo de configurações (`/mestre/config`) — 19 entidades de config com sidebar e layout próprio
- Dashboard do Mestre (`/mestre/dashboard`) — estatísticas do jogo atual
- NPCs (`/mestre/npcs`) — listagem e criação
- Prospecções Pendentes (`/mestre/prospeccao-pendentes`) — revisão de usos
- Perfil de usuário (`/profile`) — visualização de dados do Google OAuth2

### O que deveria existir (gaps identificados)

A auditoria identificou **12 gaps** distribuídos em cinco categorias: navegação, seletor de jogo, gestão de NPCs, funcionalidades ausentes e perfil de usuário.

---

## 2. Inventário de Componentes Auditados

| Componente | Arquivo | Botão Voltar | Breadcrumb |
|---|---|---|---|
| `JogosListComponent` | `jogos-list.component.ts` | Ausente | Ausente |
| `JogoDetailComponent` | `jogo-detail.component.ts` | Presente (`PageHeaderComponent`) | Ausente |
| `JogoFormComponent` | `jogo-form.component.ts` | Presente (`PageHeaderComponent`) | Ausente |
| `ConfigLayoutComponent` | `config-layout.component.ts` | Ausente | Ausente |
| `AtributosConfigComponent` | `atributos-config.component.ts` | Ausente | Ausente |
| (todas as outras 18 configs) | `*/configs/*/` | Ausente | Ausente |
| `DashboardMestreComponent` | `dashboard-mestre.component.ts` | Ausente | Ausente |
| `NpcsComponent` | `npcs.component.ts` | Ausente | Ausente |
| `ProspeccaoPendentesComponent` | `prospeccao-pendentes.component.ts` | Ausente | Ausente |
| `ProfileComponent` | `profile.component.ts` | Presente (hardcoded `/dashboard`) | Ausente |
| `SidebarComponent` | `sidebar.component.ts` | N/A | N/A |
| `HeaderComponent` | `header.component.ts` | N/A | N/A |

---

## 3. GAPs Identificados

### GAP-026-01 — Ausência de botão "Voltar" nas telas de config

**Severidade:** P1

**Descrição:** Nenhuma das 19 telas de configuração dentro de `/mestre/config/*` usa o `PageHeaderComponent` nem qualquer outro elemento de navegação de retorno. O `PageHeaderComponent` já existe e é usado em `JogoDetailComponent` e `JogoFormComponent`, mas não foi adotado nas configs.

**Evidência no código:**
- `grep` em `features/mestre/pages/config/configs/**/` por `app-page-header`, `backRoute`, `pi-arrow-left` retornou **zero resultados**
- `PageHeaderComponent` está disponível em `shared/components/page-header/page-header.component.ts` com suporte a `backRoute` e `location.back()`

**Impacto:** O Mestre que entra em `/mestre/config/vantagens` não tem caminho visível de retorno ao dashboard ou à lista de jogos. Deve usar o navegador ou clicar em outro item da sidebar.

**Premissa:** O botão "Voltar" das configs deve navegar para `/mestre/config` (a rota-raiz do módulo), não para `/mestre/jogos`, pois o Mestre está dentro de um contexto de configuração.

---

### GAP-026-02 — Troca de jogo nas configs não tem proteção contra perda de dados

**Severidade:** P1

**Descrição:** O `ConfigLayoutComponent` exibe um `<p-select>` de troca de jogo que chama `currentGameService.selectGame(gameId)` imediatamente em `onGameChange()`, sem nenhum guard, confirm dialog ou verificação de estado "dirty" do formulário ativo.

**Evidência no código (`config-layout.component.ts` linha 176-183):**
```
protected onGameChange(gameId: number | null): void {
  if (gameId === null) {
    this.currentGameService.clearGame();
  } else {
    this.currentGameService.selectGame(gameId);
  }
}
```

**Impacto:** Se o Mestre está com um formulário de atributo aberto (`p-dialog` visível) e seleciona outro jogo no dropdown do layout, o jogo muda imediatamente. O formulário em edição permanece visualmente aberto, mas ao salvar estará operando no contexto do jogo antigo (ou novo, dependendo de quando o `currentGameId` é lido). Risco de corrupção silenciosa de dados.

**Ponto em aberto PA-026-01:** Qual é o comportamento esperado? Opções:
- (a) Mostrar confirm dialog "Você tem alterações não salvas. Deseja trocar de jogo?"
- (b) Fechar automaticamente qualquer drawer/dialog aberto antes de trocar o jogo
- (c) Desabilitar a troca de jogo enquanto um drawer estiver aberto

---

### GAP-026-03 — Botões "Exportar" e "Importar" são decorativos (sem handler)

**Severidade:** P2

**Descrição:** O `ConfigLayoutComponent` renderiza dois botões: "Exportar" e "Importar". Nenhum deles tem `(onClick)` binding. São botões completamente inertes além do estado `[disabled]`.

**Evidência no código (`config-layout.component.ts` linhas 64-79):**
```
<p-button icon="pi pi-download" label="Exportar" [outlined]="true"
  [disabled]="!hasCurrentGame()"
  pTooltip="Exportar configurações do jogo atual">
</p-button>
```
Nenhuma função de handler existe na classe `ConfigLayoutComponent`.

**Impacto:** O Mestre vê botões habilitados, clica, nada acontece. Cria expectativa falsa de funcionalidade. Deve ser removido até a funcionalidade ser implementada, ou marcado como "Em breve" desabilitado com tooltip explicativo.

**Ponto em aberto PA-026-02:** Exportar/Importar está no roadmap? Se não, os botões devem ser removidos. Se sim, qual spec cobre isso?

---

### GAP-026-04 — `JogosListComponent` não mostra data de criação do jogo

**Severidade:** P2

**Descrição:** A tabela em `jogos-list.component.ts` tem uma coluna "Data Criação" (linha 126), mas a célula correspondente sempre renderiza `—` (literal, linha 151: `<td>—</td>`). O campo `dataCriacao` não existe no model `JogoResumo` (`jogo.model.ts`), que só tem: `id`, `nome`, `descricao`, `totalParticipantes`, `ativo`, `meuRole`.

**Evidência:** Model `JogoResumo` não possui campo de data. Célula da tabela é hardcoded `—`.

**Impacto:** Coluna ocupa espaço visual mas não entrega informação.

**Ponto em aberto PA-026-03:** A data de criação deve ser adicionada ao `JogoResumoResponse` do backend? Ou a coluna deve ser removida?

---

### GAP-026-05 — Sidebar global (`SidebarComponent`) não é usada; menu hamburguer é stub

**Severidade:** P2

**Descrição:** O `MainLayoutComponent` não inclui `SidebarComponent` — usa apenas `HeaderComponent` e `RouterOutlet`. O `SidebarComponent` existe mas está desconectado da rota principal. No `HeaderComponent` há um botão hamburguer (linha 38-44) que chama `onMenuToggle()`, mas esse método está vazio (linha 227-228).

**Evidência (`header.component.ts` linha 227-229):**
```typescript
onMenuToggle() {
}
```

**Impacto em mobile:** Em telas menores (`lg:hidden` no botão), o único caminho de navegação do usuário é o header. A sidebar nunca aparece. Usuário em mobile não tem como navegar pelo menu lateral.

**Ponto em aberto PA-026-04:** A sidebar deve ser implementada como drawer/panel lateral que aparece ao clicar no hamburguer em mobile? Ou a estratégia de navegação mobile deve usar outro padrão?

---

### GAP-026-06 — NPCs não têm ação de exclusão

**Severidade:** P1

**Descrição:** O `NpcsComponent` (`npcs.component.ts`) exibe uma tabela de NPCs com apenas uma ação: "Ver Ficha" (botão `pi-eye` que navega para `/mestre/fichas/:id`). Não há botão de exclusão, edição de dados básicos (nome, raça, classe) nem de duplicação de NPC.

**Evidência:** Template do `NpcsComponent` — `<td class="text-center">` contém apenas um `p-button` com `pi-eye`.

**Impacto:** O Mestre não consegue remover NPCs que criou por engano ou que não são mais relevantes à campanha. A única forma de alterar dados de um NPC é acessar a ficha completa e editar lá.

**Ponto em aberto PA-026-05:** Qual o escopo de edição direta de NPC na lista? Opções:
- (a) Apenas exclusão na listagem, edição via ficha
- (b) Edição inline de nome/tipo na listagem
- (c) Drawer de edição completo na listagem (raça, classe, gênero, índole, presença)

---

### GAP-026-07 — Notificações de participantes pendentes ausentes no header/sidebar

**Severidade:** P1

**Descrição:** Não há nenhum mecanismo de alerta visual para participantes pendentes de aprovação. O Mestre precisa navegar manualmente até `/mestre/jogos/:id` e ir na aba "Participantes" para saber se há solicitações novas. O componente `ProspeccaoPendentesComponent` tem badge para prospecções, mas participantes não têm equivalente.

**Evidência:** Nenhuma referência a `PENDENTE` ou a contagem de participantes em `header.component.ts` ou `sidebar.component.ts`.

**Comparação:** O `ProspeccaoPendentesComponent` (linha 60-65) usa `<p-badge [value]="totalPendentes()">` para exibir contagem de prospecções pendentes. O mesmo padrão poderia ser aplicado ao menu lateral para participantes.

**Impacto:** O Mestre pode perder solicitações de participação por dias sem saber. Fluxo de onboarding de jogadores é passivo e dependente de checagem manual.

---

### GAP-026-08 — Sidebar do menu Mestre ausente na rota `/mestre/npcs` e `/mestre/dashboard`

**Severidade:** P2

**Descrição:** O módulo de configurações (`/mestre/config/*`) tem sua própria sidebar (`ConfigSidebarComponent`). Porém as páginas `NpcsComponent`, `DashboardMestreComponent` e `ProspeccaoPendentesComponent` ficam fora desse módulo — usam apenas o `MainLayoutComponent` com header. Não há sidebar de navegação entre essas seções do Mestre.

**Impacto:** A navegação entre "Dashboard Mestre", "NPCs" e "Prospecções Pendentes" exige passar pelo menu do hamburguer (que não funciona — GAP-026-05) ou pelo header.

---

### GAP-026-09 — `config.component.ts` é um componente obsoleto sem rota ativa

**Severidade:** P3

**Descrição:** O arquivo `config.component.ts` contém um componente estático com texto "Em Desenvolvimento — Configurações serão implementadas na Phase 2". Esse componente não está referenciado em `app.routes.ts` — a rota `/mestre/config` carrega `ConfigLayoutComponent` diretamente. O arquivo é dead code.

**Evidência:** `app.routes.ts` linha 99 carrega `ConfigLayoutComponent`; `config.component.ts` exporta `ConfigComponent` que não aparece em nenhuma rota.

---

### GAP-026-10 — Perfil: nenhum campo é editável pelo usuário

**Severidade:** P1

**Descrição:** O `ProfileComponent` (`profile.component.ts`) é somente-leitura. Exibe nome, email, role, status, data de criação. Não há botão "Editar", campo de nome editável, nem upload de foto.

**Backend:** O `UsuarioController` (`/api/v1/usuarios/me`) tem `PUT /me` que aceita `AtualizarUsuarioRequest { String nome }`. O campo permitido pelo backend é apenas o **nome** — email e foto são controlados pelo Google OAuth2 (docstring do controller confirma isso).

**Discrepância do frontend:** O `ProfileComponent` usa `HttpClient` diretamente chamando `/api/v1/auth/me`, ignorando o `UsuarioController` em `/api/v1/usuarios/me`. Também não chama `PUT /api/v1/usuarios/me`.

**Impacto:** O usuário não consegue personalizar nem o nome que aparece no header.

**Premissa:** A foto de perfil vem do Google e não pode ser trocada aqui (confirmado no docstring do backend: "Email e foto são gerenciados pelo Google OAuth2"). O único campo editável é o nome.

---

### GAP-026-11 — Perfil: `console.log` em produção

**Severidade:** P3 (qualidade de código)

**Descrição:** O `ProfileComponent` tem três chamadas `console.log`/`console.error` em código de produção (linhas 113, 147, 149).

**Evidência:**
```typescript
console.error('Erro ao carregar perfil:', error);
console.log('ProfileComponent: Iniciando logout...');
console.log('ProfileComponent: Logout concluído, redirecionando para /login');
```

---

### GAP-026-12 — `JogoFormComponent` usa `setTimeout` para navegar após salvar

**Severidade:** P2 (qualidade de código / UX)

**Descrição:** Após criar ou editar um jogo com sucesso, o `JogoFormComponent` aguarda 1500ms antes de navegar (`setTimeout(() => this.router.navigate(['/mestre/jogos']), 1500)`). O correto é navegar imediatamente no `next:` do subscribe, sem delay artificial.

**Evidência (`jogo-form.component.ts` linhas 176 e 193):**
```typescript
setTimeout(() => this.router.navigate(['/mestre/jogos']), 1500);
```

**Impacto:** Usuário fica preso na tela de formulário por 1,5s após o toast de sucesso, sem feedback visual de que algo está acontecendo.

---

## 4. Dossiê de Regras por Funcionalidade

### 4.1 Troca de Jogo no ConfigLayout

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| `<p-select>` jogo | Select | Trocar jogo quando drawer/dialog estiver fechado | Client-side: verificar `drawerVisible()` dos filhos (ou estado global) | MESTRE |
| `<p-select>` jogo | Select | Se drawer aberto: exibir confirm dialog antes de trocar | UX confirm | MESTRE |
| Botão "Exportar" | Ação | Deve ter handler ou ser removido | N/A | MESTRE |
| Botão "Importar" | Ação | Deve ter handler ou ser removido | N/A | MESTRE |
| Botão "Voltar" | Navegação | Navegar para `/mestre/jogos` (sair das configs) | N/A | MESTRE |

### 4.2 Telas de Config (atributos, classes, raças, etc.)

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| `PageHeaderComponent` | Navegação | Presente no topo, `backRoute="/mestre/config"` | N/A | MESTRE |
| Indicador de jogo | Info | Já existe em `AtributosConfigComponent` (linha 65) — replicar em todas | N/A | MESTRE |

### 4.3 Perfil de Usuário

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Campo Nome | Input (editável) | Min 2, max 100 chars. Chama `PUT /api/v1/usuarios/me` | `@NotBlank`, `@Size` (já implementado no backend) | Qualquer autenticado |
| Foto de Perfil | Display | Read-only, vem do Google OAuth2. Não há upload | N/A | Qualquer autenticado |
| Email | Display | Read-only, gerenciado pelo Google | N/A | Qualquer autenticado |
| Botão "Salvar" | Ação | Habilitado apenas se nome foi alterado | Client-side dirty check | Qualquer autenticado |
| Botão "Voltar" | Navegação | `location.back()` (já implementado, hardcoded `/dashboard`) | N/A | Qualquer autenticado |

### 4.4 Lista de NPCs

| Elemento | Tipo | Regra de Negócio | Validação | Role |
|---|---|---|---|---|
| Botão "Excluir NPC" | Ação | Exibir confirm dialog. NPCs podem ser excluídos pelo Mestre | Confirm dialog | MESTRE |
| Botão "Editar NPC" | Ação (opcional) | Drawer ou navegação para ficha | TBD (PA-026-05) | MESTRE |

---

## 5. User Stories

### US-026-01: Botão Voltar nas Telas de Config

**Como Mestre,**
**quero** um botão "Voltar" visível em cada tela de configuração (atributos, classes, raças, etc.),
**para** poder retornar facilmente ao hub de configurações sem usar o navegador.

**Critérios de Aceite:**

Cenário 1: Navegação de retorno a partir de uma config
```
Dado que estou na tela de configuração de Atributos (/mestre/config/atributos)
Quando clico no botão "Voltar" no topo da tela
Então sou redirecionado para /mestre/config
E nenhum dado não salvo é perdido (drawer fechado sem envio)
```

Cenário 2: Botão visível em todas as 19 configs
```
Dado que navego para qualquer rota filha de /mestre/config/*
Então um botão de retorno usando PageHeaderComponent está visível no topo da área de conteúdo
E o título da seção está exibido ao lado do botão
```

---

### US-026-02: Guard de Troca de Jogo com Edição em Andamento

**Como Mestre,**
**quero** que o sistema me avise antes de trocar de jogo quando tenho um formulário aberto,
**para** não perder acidentalmente dados que estava editando.

**Critérios de Aceite:**

Cenário 1: Troca de jogo com drawer fechado
```
Dado que estou em /mestre/config/atributos sem nenhum drawer/dialog aberto
Quando seleciono outro jogo no seletor do ConfigLayoutComponent
Então o jogo é trocado imediatamente sem confirmação
E a lista de itens recarrega para o novo jogo
```

Cenário 2: Troca de jogo com drawer aberto
```
Dado que estou em /mestre/config/atributos com o dialog de edição aberto
Quando seleciono outro jogo no seletor do ConfigLayoutComponent
Então um confirm dialog aparece: "Você tem alterações não salvas. Deseja continuar?"
Se confirmar: dialog é fechado, jogo é trocado, lista recarrega
Se cancelar: seleção reverte ao jogo anterior, drawer permanece aberto
```

---

### US-026-03: Remover / Ocultar Botões Exportar/Importar sem Funcionalidade

**Como Mestre,**
**quero** que os botões "Exportar" e "Importar" não apareçam até estarem implementados,
**para** não me frustrar clicando em botões que não fazem nada.

**Critérios de Aceite:**

Cenário 1: Botões removidos ou desabilitados com feedback
```
Dado que acesso /mestre/config com um jogo selecionado
Quando visualizo o header do ConfigLayout
Então os botões "Exportar" e "Importar" estão ausentes OU exibem tooltip "Funcionalidade em breve" e estão permanentemente desabilitados
```

---

### US-026-04: Edição de Nome no Perfil

**Como usuário autenticado,**
**quero** poder editar meu nome de exibição na tela de perfil,
**para** personalizar como apareço para outros participantes do jogo.

**Critérios de Aceite:**

Cenário 1: Editar nome com sucesso
```
Dado que estou na tela /profile
Quando clico em "Editar" (ou o campo nome se torna editável inline)
E altero o nome para um valor válido (2-100 chars)
E clico em "Salvar"
Então o sistema chama PUT /api/v1/usuarios/me com o novo nome
E o nome atualizado aparece no header (avatar dropdown) imediatamente
E um toast de sucesso é exibido
```

Cenário 2: Tentar salvar nome vazio
```
Dado que estou editando o nome no perfil
Quando deixo o campo vazio e clico em "Salvar"
Então uma mensagem de erro inline aparece: "Nome é obrigatório"
E o botão "Salvar" está desabilitado
```

Cenário 3: Foto de perfil é read-only
```
Dado que estou na tela /profile
Então não há botão de upload de foto
E a foto exibe a imagem atual do Google OAuth2
E uma nota explica: "Foto gerenciada pelo Google"
```

---

### US-026-05: Exclusão de NPC na Listagem

**Como Mestre,**
**quero** poder excluir um NPC diretamente na lista de NPCs,
**para** remover personagens não-jogadores que não são mais relevantes à campanha.

**Critérios de Aceite:**

Cenário 1: Excluir NPC com confirmação
```
Dado que estou em /mestre/npcs com um jogo selecionado
Quando clico no botão "Excluir" de um NPC
Então um confirm dialog aparece: "Tem certeza que deseja excluir este NPC?"
Quando confirmo
Então o NPC é removido da lista
E um toast de sucesso é exibido
```

Cenário 2: Cancelar exclusão
```
Dado que o confirm dialog de exclusão está aberto
Quando clico em "Cancelar"
Então o dialog fecha e o NPC permanece na lista
```

---

### US-026-06: Indicador Visual de Participantes Pendentes

**Como Mestre,**
**quero** ver um badge de contagem na navegação quando há jogadores aguardando aprovação,
**para** não precisar verificar manualmente cada jogo periodicamente.

**Critérios de Aceite:**

Cenário 1: Badge visível no menu do Mestre
```
Dado que há jogadores com status PENDENTE em algum jogo do Mestre
Quando acesso qualquer página autenticada
Então o sidebar ou header exibe um badge numérico ao lado de "Meus Jogos" ou de um ícone dedicado
```

Cenário 2: Badge zerado quando não há pendentes
```
Dado que não há participantes com status PENDENTE
Então o badge está ausente ou exibe 0 de forma não intrusiva
```

**Ponto em aberto PA-026-06:** O badge deve ser carregado por polling ou apenas ao entrar em cada página? E deve ser por jogo específico ou total cross-jogos?

---

### US-026-07: Menu Mobile Funcional (hamburguer)

**Como Mestre em dispositivo mobile,**
**quero** clicar no botão hamburguer e ver um menu lateral de navegação,
**para** acessar todas as seções sem precisar digitar URLs manualmente.

**Critérios de Aceite:**

Cenário 1: Abrir menu mobile
```
Dado que estou em tela mobile (viewport < 1024px)
E o botão hamburguer está visível no header
Quando clico no hamburguer
Então um drawer lateral aparece com os itens de navegação do Mestre
```

Cenário 2: Fechar menu ao navegar
```
Dado que o menu mobile está aberto
Quando clico em um item de menu
Então navego para a rota correspondente
E o drawer fecha automaticamente
```

---

## 6. Priorização Sugerida

| # | Gap | US | Prioridade | Esforço estimado | Dependências |
|---|---|---|---|---|---|
| 026-01 | Botão Voltar nas configs | US-026-01 | **P1** | XS — apenas adicionar `PageHeaderComponent` nos 19 arquivos | Nenhuma |
| 026-04 | Edição de nome no perfil | US-026-04 | **P1** | S — adicionar form + chamar PUT que já existe no backend | Nenhuma |
| 026-06 | NPCs sem exclusão | US-026-05 | **P1** | S — botão + confirm dialog | Nenhuma |
| 026-02 | Guard troca de jogo | US-026-02 | **P1** | M — exige mecanismo de "drawer state" cross-componente | PA-026-01 resolvido |
| 026-07 | Notificação participantes | US-026-06 | **P1** | M — exige endpoint de contagem + badge no layout | PA-026-06 resolvido |
| 026-03 | Botões Exportar/Importar | US-026-03 | **P2** | XS — remover dois botões do template | Nenhuma |
| 026-05 | Sidebar mobile | US-026-07 | **P2** | M — implementar `onMenuToggle` + p-sidebar | Nenhuma |
| 026-04-lista | Data Criação na lista de jogos | — | **P2** | S — backend + frontend | PA-026-03 resolvido |
| 026-11 | console.log no perfil | — | **P3** | XS — remover 3 linhas | Nenhuma |
| 026-12 | setTimeout no JogoForm | — | **P3** | XS — remover setTimeout | Nenhuma |
| 026-09 | Dead code config.component.ts | — | **P3** | XS — deletar arquivo | Nenhuma |
| 026-08 | Sidebar Mestre fora das configs | — | **P2** | M — criar layout específico do Mestre | Relacionado a 026-05 |

---

## 7. Regras de Negócio Críticas do Domínio

- **Troca de jogo durante edição:** Qualquer alteração não salva (form dirty) deve ser protegida contra troca de contexto. O `currentGameService.selectGame()` é uma mutação global de estado que afeta todos os serviços.
- **Edição de nome:** Apenas o nome do usuário pode ser alterado. Email e foto são propriedade do Google OAuth2. O backend já documenta isso explicitamente no `UsuarioController`.
- **Exclusão de NPC:** NPCs são fichas com `isNpc=true`. A regra "fichas nunca são deletadas" (status morta/abandonada) se aplica aqui? Este ponto precisa ser confirmado (PA-026-05).

---

## 8. Pontos em Aberto

| ID | Pergunta | Impacto |
|---|---|---|
| **PA-026-01** | Qual o comportamento ao trocar de jogo com drawer aberto? (a) confirm dialog, (b) fechar drawer automaticamente, (c) desabilitar seletor? | Bloqueia US-026-02 |
| **PA-026-02** | Exportar/Importar configs está no roadmap? Se sim, qual spec? Se não, remover os botões? | Bloqueia decisão de remoção dos botões |
| **PA-026-03** | A data de criação deve ser adicionada ao `JogoResumoResponse` (mudança de backend)? Ou a coluna "Data Criação" deve ser removida da tabela? | Bloqueia correção do GAP-026-04 |
| **PA-026-04** | A sidebar mobile deve ser implementada com `p-sidebar` (drawer lateral) ou outro padrão? | Bloqueia US-026-07 |
| **PA-026-05** | NPCs podem ser excluídos ou seguem a regra de "fichas nunca deletadas" (status morta)? Qual o escopo de edição direta na listagem? | Bloqueia US-026-05 |
| **PA-026-06** | O badge de participantes pendentes deve ser total cross-jogos ou por jogo? Polling ou one-shot? | Bloqueia US-026-06 |

---

## 9. Checklist de Validação UX

- [ ] O `PageHeaderComponent` como padrão de botão voltar é visualmente adequado para o módulo de configs? (fica no conteúdo, dentro do card, não no header do layout)
- [ ] O confirm dialog de "troca de jogo com edição em andamento" interrompe demasiadamente o fluxo do Mestre? Validar com testes de usabilidade
- [ ] O seletor de jogo duplicado (header global + ConfigLayout) causa confusão? O ConfigLayout tem um seletor mais proeminente — o do header pode ser ocultado dentro de `/mestre/config/*`?
- [ ] A notificação de participantes pendentes deve usar badge vermelho no ícone do jogo ou uma seção dedicada de notificações?
- [ ] A tela de perfil usa um layout centralizado com card (`w-full md:w-6 lg:w-4`). O formulário de edição de nome cabe no mesmo layout sem redesign?
