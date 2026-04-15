# Spec 025 — Dossiê de GAPs UX: Fluxo do Jogador

**Versão:** 1.0  
**Data:** 2026-04-15  
**Autor:** BA/PO Agent  
**Status:** Em análise — aguarda validação PO

---

## 1. Visão Geral do Negócio

### O que existe hoje

O fluxo do Jogador está parcialmente implementado. Estão funcionando:

- Listagem de jogos onde o usuário é participante (`/jogador/jogos`)
- Solicitação/cancelamento de entrada em jogos
- Wizard de criação de ficha em 6 passos (Spec 006)
- Visualização de ficha com 8 abas (Resumo, Atributos, Aptidões, Vantagens, Anotações, Equipamentos, Prospecção, Galeria)
- Level-up dialog com 3 steps (atributos, aptidões, vantagens)
- XP concedido pelo Mestre com detecção de level-up
- Prospecção (usar/listar usos)
- Galeria e Anotações

### O que deveria existir mas não existe ou está incompleto

Foram identificados **14 GAPs** distribuídos em 6 domínios funcionais:

1. Status de ficha `MORTA/ATIVA/ABANDONADA` (backend e frontend incompletos)
2. Rota de edição de ficha quebrada (`/fichas/:id/editar` não existe)
3. Fluxo de descoberta de jogos (sem endpoint público)
4. Ausência de modo sessão com atualização de vida/essência/membros na UI
5. Edição direta de atributos e aptidões fora do wizard (em sessão)
6. Polling de modo sessão não implementado

---

## 2. Atores Envolvidos

| Ator | Role | Contexto principal |
|---|---|---|
| Jogador | JOGADOR | Solicita entrada, cria ficha, joga sessões |
| Mestre | MESTRE | Aprova jogadores, concede XP, gerencia fichas |

---

## 3. GAPs Identificados

---

### GAP-025-01 — Status de Ficha: Backend e Frontend Desalinhados (CRITICO)

**Severidade:** P0  
**Domínio:** Ciclo de vida da ficha

**Situação atual:**

- **Frontend** (`ficha.model.ts`): `FichaStatus = 'RASCUNHO' | 'ATIVA' | 'MORTA' | 'ABANDONADA'`
- **Backend** (`FichaStatus.java`): apenas `RASCUNHO | COMPLETA`

O modelo frontend define 4 status (`RASCUNHO`, `ATIVA`, `MORTA`, `ABANDONADA`) mas o backend só persiste 2 (`RASCUNHO`, `COMPLETA`). Quando o wizard é concluído, o backend chama `PUT /fichas/{id}/completar` e marca como `COMPLETA`. O frontend espera receber `ATIVA`.

**Consequências:**

- A ficha-list só exibe badge "Incompleta" para `RASCUNHO`. Para fichas `COMPLETA` (retornadas pelo backend), nenhum badge é exibido — correto visualmente, mas semanticamente incoerente.
- Não existe endpoint para marcar ficha como `MORTA` ou `ABANDONADA`.
- A regra de negócio "fichas NUNCA são deletadas, ficam com status morta/abandonada" (decisão INCONS-02 do PO) não está implementada no backend.
- O botão "Deletar Ficha" está presente na `fichas-list.component.ts` (linha 306) e em `ficha-detail.component.ts` (linha 165) — chama `DELETE /api/v1/fichas/{id}` que existe no backend com role MESTRE. O Jogador também tem o botão "Excluir" na lista de fichas e pode chamar `deleteFicha()` — o backend aceita (role `JOGADOR` tem acesso ao serviço via `FichaBusinessService`).

**O que deveria existir:**

- Backend: adicionar `ATIVA` e `ABANDONADA` ao enum (ou `COMPLETA` → `ATIVA` por rename)
- Backend: endpoint `PATCH /api/v1/fichas/{id}/status` para marcar `MORTA` ou `ABANDONADA` (apenas MESTRE)
- Frontend: remover botão "Deletar/Excluir" da visão do Jogador — Jogador nunca deleta ficha
- Frontend: exibir badge de status (`MORTA`, `ABANDONADA`) na `fichas-list`
- Frontend: ao clicar "Deletar" (somente Mestre), questionar se quer marcar como `ABANDONADA` em vez de deletar

**User Story:**

**US-025-01: Marcar Ficha como Morta**  
Como Mestre,  
Quero poder marcar uma ficha como "morta" quando o personagem morre em jogo,  
Para preservar o histórico sem deletar o personagem.

Cenário 1: Mestre marca ficha como morta  
  Dado que sou Mestre e a ficha está com status ATIVA  
  Quando clico em "Marcar como Morta" na ficha  
  Então o status muda para MORTA  
  E a ficha continua visível na lista com badge vermelho "Morta"  
  E não pode ser deletada

Cenário 2: Jogador não pode deletar ficha  
  Dado que sou Jogador  
  Quando acesso a lista de minhas fichas  
  Então não vejo botão "Excluir" em nenhuma ficha  
  E não consigo chamar DELETE /fichas/{id}

**US-025-02: Criar Nova Ficha após Morte**  
Como Jogador aprovado em um jogo,  
Quero poder criar uma nova ficha após meu personagem morrer,  
Para continuar participando da campanha.

Cenário 1: Jogador com ficha MORTA cria nova  
  Dado que minha ficha anterior está com status MORTA  
  Quando acesso "Criar Ficha"  
  Então consigo criar uma nova ficha no mesmo jogo  
  E minha ficha morta continua listada com badge "Morta"

---

### GAP-025-02 — Rota de Edição de Ficha Quebrada (CRITICO)

**Severidade:** P0  
**Domínio:** Navegação

**Situação atual:**

Em `ficha-detail.component.ts` linha 799:
```typescript
this.router.navigate(['/fichas', fichaId, 'editar']);
```

A rota registrada em `app.routes.ts` é:
```
/jogador/fichas/:id/edit
```

O botão "Editar" na ficha-detail navega para `/fichas/:id/editar` (sem o prefixo `/jogador` e com segmento `editar` em vez de `edit`). Essa rota **não existe** — o usuário é redirecionado para `/404`.

Já na `fichas-list.component.ts` (linha 378) e em `jogador-dashboard.component.ts` (linha 261), a navegação está correta: `/jogador/fichas/:id/edit`.

**O que deveria existir:**

- Corrigir `ficha-detail.component.ts` linha 799: `this.router.navigate(['/jogador/fichas', fichaId, 'edit'])`

**User Story:**

**US-025-03: Editar Ficha pela Tela de Detalhe**  
Como Jogador,  
Quero clicar em "Editar" na tela de detalhe da ficha e ser redirecionado ao wizard de edição,  
Para atualizar os dados do meu personagem.

Cenário 1: Botão editar navega corretamente  
  Dado que estou na tela `/jogador/fichas/42`  
  Quando clico no botão "Editar"  
  Então sou redirecionado para `/jogador/fichas/42/edit`  
  E o wizard carrega os dados atuais da ficha

---

### GAP-025-03 — Sem Tela para Editar Vida e Essência em Sessão

**Severidade:** P1  
**Domínio:** Modo sessão / Combate

**Situação atual:**

O backend tem endpoint `PUT /api/v1/fichas/{id}/vida` que aceita `vidaAtual`, `essenciaAtual` e `membros`. O frontend tem o método `atualizarVida()` no `FichasApiService`. Porém, **não existe nenhuma tela ou componente na ficha-detail que permite ao Jogador atualizar sua vida atual, essência atual ou dano em membros**.

O `FichaHeaderComponent` exibe a barra de vida e essência em modo somente leitura. O botão "Resetar Estado" existe mas é apenas para Mestre.

**O que deveria existir:**

Uma aba "Sessão" (ou seção dedicada dentro da aba Resumo) com controles para:
- Ajustar vidaAtual (+/- ou input numérico)
- Ajustar essenciaAtual (+/- ou input numérico)
- Visualizar dano em membros do corpo (FichaVida por MembroCorpoConfig)
- Botão "Gastar Essência" (decisão PO GAP-07: `POST /fichas/{id}/essencia/gastar`)

**User Story:**

**US-025-04: Atualizar Vida e Essência em Sessão**  
Como Jogador,  
Quero poder ajustar minha vida atual e essência atual durante uma sessão de jogo,  
Para refletir dano recebido e consumo de essência.

Cenário 1: Jogador sofre dano  
  Dado que minha vida atual é 30 e meu total é 40  
  Quando abro a aba Sessão e insiro 5 como dano recebido  
  Então minha vidaAtual é atualizada para 25  
  E a barra de vida no header reflete o novo valor

Cenário 2: Jogador gasta essência  
  Dado que minha essência atual é 20  
  Quando clico em "Gastar Essência" e informo 3  
  Então minha essenciaAtual cai para 17  
  E o Mestre pode ver o novo valor

Cenário 3 (exceção): Vida não pode ser negativa  
  Dado que minha vida atual é 5  
  Quando tento registrar 10 de dano  
  Então o sistema limita vidaAtual a 0 (não negativo)

---

### GAP-025-04 — Ausência de Modo Sessão com Polling

**Severidade:** P1  
**Domínio:** Sincronização em tempo real

**Situação atual:**

A decisão PO Q17 definiu: "Modo Sessão = Polling 30s no MVP". No entanto, **não existe nenhuma implementação de polling no frontend**. A ficha-detail não recarrega automaticamente dados. Se o Mestre conceder XP, alterar visibilidade de NPC ou resetar estado, o Jogador só verá as mudanças ao recarregar manualmente a página.

**O que deveria existir:**

- Polling automático de 30 segundos na tela `ficha-detail.component.ts` para `GET /api/v1/fichas/{id}/resumo`
- Indicador visual de "última atualização" ou spinner discreto durante o polling
- Botão "Atualizar" manual como alternativa

**User Story:**

**US-025-05: Ficha Atualiza Automaticamente Durante Sessão**  
Como Jogador,  
Quero que minha ficha se atualize periodicamente durante a sessão,  
Para ver mudanças feitas pelo Mestre (XP, reset, Insólitus) sem precisar recarregar.

Cenário 1: Atualização automática detecta XP novo  
  Dado que estou na tela da minha ficha  
  E o Mestre concede 200 XP para mim  
  Quando passam 30 segundos  
  Então meu XP exibido é atualizado automaticamente  
  E se houve level-up, o dialog de level-up é aberto

---

### GAP-025-05 — Sem Endpoint Público para Descoberta de Jogos

**Severidade:** P1  
**Domínio:** Descoberta de jogos / Fluxo de entrada

**Situação atual:**

O componente `jogos-disponiveis.component.ts` contém o seguinte TODO (linhas 191-194):
```
// TODO: backend precisa de endpoint GET /api/v1/jogos/publicos para mostrar
// jogos disponíveis onde o usuário ainda não é participante e pode solicitar acesso.
// Atualmente listJogos() retorna apenas jogos onde o usuário já tem um role.
```

O backend `GET /api/v1/jogos` (`listarJogosDoUsuario()`) retorna apenas os jogos onde o usuário já tem um role (MESTRE ou JOGADOR). **Não existe endpoint para listar jogos públicos onde o usuário ainda não é participante.**

Consequência: um Jogador novo, sem nenhum jogo, vê a tela "Jogos Disponíveis" completamente vazia. A mensagem de estado vazio diz "Peça ao Mestre para te adicionar ou aguarde um convite" — modelo pull (Mestre convida), não push (Jogador descobre e solicita).

**O que deveria existir:**

- Backend: `GET /api/v1/jogos/publicos` retorna jogos com `ativo=true` onde o usuário não é participante (ou foi removido/rejeitado)
- Frontend: exibir seção "Outros Jogos Disponíveis" na tela de jogos com os jogos públicos encontrados
- Botão "Solicitar Entrada" nesses jogos ainda funcionaria no endpoint existente `POST /api/v1/jogos/{jogoId}/participantes/solicitar`

**Pontos em aberto:** O PO precisa decidir se existe o conceito de "jogo público vs privado" (campo `publico` no Jogo?) ou se todos os jogos ativos são descobríveis.

**User Story:**

**US-025-06: Descobrir Jogos Disponíveis**  
Como Jogador sem nenhum jogo,  
Quero ver uma lista de jogos onde posso solicitar entrada,  
Para começar a jogar sem depender de convite do Mestre.

Cenário 1: Jogador descobre jogos disponíveis  
  Dado que sou um Jogador sem nenhuma participação ativa  
  Quando acesso "Jogos Disponíveis"  
  Então vejo uma lista de jogos ativos onde posso solicitar entrada  
  E posso clicar "Solicitar Entrada" em cada um

---

### GAP-025-06 — Notificação de Aprovação/Rejeição Ausente para o Jogador

**Severidade:** P1  
**Domínio:** Feedback ao Jogador

**Situação atual:**

Quando o Jogador solicita entrada em um jogo, seu status muda para `PENDENTE`. Quando o Mestre aprova, o status muda para `APROVADO`. Porém, **não existe nenhum mecanismo de notificação para avisar o Jogador que sua solicitação foi aprovada ou rejeitada**.

O Jogador precisa entrar manualmente em "Jogos Disponíveis" e clicar em "Atualizar" para verificar seu status. Não há badge, notificação push, toast ou polling automático nessa tela.

No sidebar, também não há indicador de solicitações pendentes ou aprovadas.

**O que deveria existir:**

- Tela "Jogos Disponíveis" com polling automático (30s, consistente com modo sessão)
- Ou: badge no item de menu "Jogos Disponíveis" indicando mudança de status

**User Story:**

**US-025-07: Jogador é Notificado de Aprovação**  
Como Jogador com solicitação pendente,  
Quero ser notificado quando o Mestre aprovar ou rejeitar minha solicitação,  
Para poder acessar o jogo imediatamente após aprovação.

Cenário 1: Aprovação detectada via polling  
  Dado que tenho uma solicitação com status PENDENTE  
  E o Mestre me aprova  
  Quando a tela "Jogos Disponíveis" faz sua atualização automática  
  Então meu status muda de "Aguardando aprovação" para "Aprovado"  
  E o botão "Entrar" fica disponível  
  E um toast exibe "Sua solicitação foi aprovada!"

---

### GAP-025-07 — Ficha-detail Sem Aba de Combate / Membros do Corpo

**Severidade:** P1  
**Domínio:** Modo sessão

**Situação atual:**

O backend tem `FichaVida` por `MembroCorpoConfig` (cabeça, tronco, etc.) com `porcentagemVida` e `danoRecebido`. O endpoint `PUT /api/v1/fichas/{id}/vida` suporta atualização de dano por membro. No `FichaResumoResponse` existem campos `membros`.

No frontend, a ficha-detail tem 8 abas: Resumo, Atributos, Aptidões, Vantagens, Anotações, Equipamentos, Prospecção, Galeria. **Não existe aba de Sessão ou Combate**. O Jogador não consegue ver nem atualizar o dano por membro do corpo.

O header da ficha exibe apenas vida e essência totais (não por membro).

**O que deveria existir:**

Uma aba "Sessão" ou "Combate" com:
- Lista de membros do corpo com barra de vida por membro
- Controle para registrar dano por membro
- Atualização de vidaAtual e essenciaAtual
- Status de essência com botão "Gastar"

**User Story:**

**US-025-08: Visualizar e Atualizar Membros do Corpo em Sessão**  
Como Jogador,  
Quero ver o estado de saúde de cada membro do meu personagem e registrar dano localizado,  
Para simular combate com localização de ferimentos.

Cenário 1: Visualizar membros  
  Dado que estou na aba "Sessão" da minha ficha  
  Então vejo a lista de membros (ex: Cabeça, Tronco, Braço Esquerdo)  
  E cada membro exibe sua vida atual e total  
  E membros com dano exibem barra de vida parcial em vermelho

---

### GAP-025-08 — Wizard de Edição Não Diferencia Criação de Edição

**Severidade:** P2  
**Domínio:** Experiência de edição de ficha

**Situação atual:**

A rota `/jogador/fichas/:id/edit` usa o mesmo `FichaWizardComponent`. O componente detecta `fichaId` via query param (`?fichaId=N`) — mas a rota `/jogador/fichas/:id/edit` usa o `id` no path, não em query param.

Em `app.routes.ts` linha 216-218:
```typescript
path: 'fichas/:id/edit',
loadComponent: () => import('.../ficha-wizard.component')
```

O wizard em `ngOnInit()` lê apenas `this.route.snapshot.queryParamMap.get('fichaId')`. Para a rota de edição com path param `:id`, o wizard não encontra o `fichaId` via queryParam e começa do passo 1 como nova ficha.

**O que deveria existir:**

- Wizard deve ler `fichaId` tanto de `queryParams.fichaId` quanto de `route.snapshot.params['id']`
- Ou: rota de edição redireciona para `?fichaId=N` explicitamente

**User Story:**

**US-025-09: Editar Ficha Existente pelo Wizard**  
Como Jogador,  
Quero que ao clicar em "Editar" em uma ficha existente, o wizard carregue os dados atuais,  
Para não precisar preencher tudo do zero.

Cenário 1: Wizard carrega dados de ficha existente  
  Dado que acesso `/jogador/fichas/42/edit`  
  Quando o wizard carrega  
  Então o passo 1 já está preenchido com nome, raça, classe, gênero, índole e presença  
  E posso navegar diretamente para o passo que quero editar

---

### GAP-025-09 — Ausência de Feedback de Status na Lista de Fichas

**Severidade:** P2  
**Domínio:** UX da lista de fichas

**Situação atual:**

A `fichas-list.component.ts` só exibe badge para fichas `RASCUNHO` ("Incompleta"). Para fichas completadas, o badge "Novo" é exibido quando `ficha.nivel == null`, e "Nv. X" quando tem nível. Não há distinção visual entre ficha `COMPLETA/ATIVA`, `MORTA` ou `ABANDONADA`.

**O que deveria existir:**

| Status | Badge | Cor | Ação disponível |
|---|---|---|---|
| RASCUNHO | Incompleta | Amarelo | Clique para retomar |
| ATIVA/COMPLETA | nenhum badge extra | — | Ver / Editar |
| MORTA | Morta | Vermelho | Apenas Ver (readonly) |
| ABANDONADA | Abandonada | Cinza | Apenas Ver (readonly) |

**User Story:**

**US-025-10: Identificar Status da Ficha na Listagem**  
Como Jogador,  
Quero ver visualmente o status de cada ficha na lista,  
Para saber quais fichas estão ativas, mortas ou abandonadas.

---

### GAP-025-10 — Botão "Editar" Visível em Fichas MORTA/ABANDONADA

**Severidade:** P2  
**Domínio:** Permissões de edição

**Situação atual:**

`canEdit()` em `ficha-business.service.ts` retorna `true` para qualquer ficha do próprio Jogador, independente do status. Fichas mortas ou abandonadas também exibiriam os botões "Editar" e "Excluir" na lista.

**O que deveria existir:**

- `canEdit()` deve retornar `false` para fichas com status `MORTA` ou `ABANDONADA`
- Fichas mortas/abandonadas são readonly (exceto para Mestre)

---

### GAP-025-11 — Ficha-detail Não Exibe Status Contextual

**Severidade:** P2  
**Domínio:** Feedback visual

**Situação atual:**

O `FichaHeaderComponent` não exibe o status atual da ficha (somente nome, nível, raça, classe). Uma ficha `COMPLETA` e uma ficha `RASCUNHO` têm o mesmo header visual.

**O que deveria existir:**

- Badge de status no header da ficha: "Rascunho" (warn) / "Morta" (danger) / "Abandonada" (secondary)
- Para status `ATIVA/COMPLETA`, nenhum badge adicional

---

### GAP-025-12 — Wizard Steps Desalinhados com Labels

**Severidade:** P2  
**Domínio:** UX do wizard

**Situação atual:**

Os labels dos passos no `FichaWizardComponent` (linha 441-448) são:
```typescript
readonly passos: MenuItem[] = [
  { label: 'Identificacao' },
  { label: 'Descricao' },
  { label: 'Atributos' },
  { label: 'Aptidoes' },
  { label: 'Revisao' },  // Passo 5 é Vantagens, não Revisao
  { label: 'Conclusao' }, // Passo 6 é Revisao, não Conclusao
];
```

O passo 5 chama `StepVantagensComponent` mas o label diz "Revisao". O passo 6 chama `StepRevisaoComponent` mas o label diz "Conclusao".

**O que deveria existir:**

```typescript
readonly passos: MenuItem[] = [
  { label: 'Identificacao' },
  { label: 'Descricao' },
  { label: 'Atributos' },
  { label: 'Aptidoes' },
  { label: 'Vantagens' },   // corrigido
  { label: 'Revisao' },     // corrigido
];
```

---

### GAP-025-13 — Tela "Criar Ficha" Duplicada no Sidebar do Jogador

**Severidade:** P3  
**Domínio:** Navegação/UX

**Situação atual:**

O sidebar do Jogador tem dois itens separados: "Minhas Fichas" e "Criar Ficha". O acesso a "Criar Ficha" sem um jogo selecionado exibe um toast de aviso e redireciona para `/jogador/fichas`. Já a tela "Minhas Fichas" tem um botão "Nova Ficha" que faz o mesmo.

O item "Criar Ficha" no sidebar pode confundir o Jogador, pois a ação correta é ir às "Minhas Fichas" e clicar em "Nova Ficha" dentro do contexto do jogo ativo.

**O que deveria existir:**

- Remover o item "Criar Ficha" do sidebar
- Manter apenas "Minhas Fichas" como ponto de entrada, com o botão "Nova Ficha" contextualizado

---

### GAP-025-14 — Habilidades do Jogador: Permissões Divergentes do Domínio

**Severidade:** P3  
**Domínio:** Permissões

**Situação atual:**

A tela `jogador-habilidades.component.ts` diz (linha 25-26):
> "MESTRE e JOGADOR têm permissões simétricas nesta entidade. O Jogador pode criar, editar e deletar habilidades do jogo."

O backend `HabilidadeConfigController.java` também aceita MESTRE e JOGADOR em POST/PUT/DELETE. Isso é incomum — todas as outras configs do jogo são gerenciadas exclusivamente pelo Mestre. A habilidade parece ser uma entidade diferente (skill pessoal do personagem?).

**Ponto em aberto para PO:** HabilidadeConfig é uma config do jogo (Mestre cria e compartilha) ou uma habilidade pessoal de cada personagem (Jogador cria para sua própria ficha)? A regra atual parece conflitar com o princípio "tudo configurável pelo Mestre, nada hardcoded".

---

## 4. Priorização Sugerida

| ID | GAP | Prioridade | Esforço estimado |
|---|---|---|---|
| GAP-025-01 | Status MORTA/ATIVA/ABANDONADA (backend+FE) | P0 | 3-4 dias |
| GAP-025-02 | Rota de edição quebrada | P0 | 30 min (fix cirúrgico) |
| GAP-025-03 | Sem UI para atualizar vida/essência em sessão | P1 | 2 dias |
| GAP-025-04 | Polling de modo sessão ausente | P1 | 1 dia |
| GAP-025-05 | Sem endpoint de descoberta de jogos públicos | P1 | 2 dias BE + 1 dia FE |
| GAP-025-06 | Sem notificação de aprovação/rejeição | P1 | 1 dia |
| GAP-025-07 | Sem aba de combate/membros do corpo | P1 | 2 dias |
| GAP-025-08 | Wizard de edição não lê path param | P2 | 1 dia |
| GAP-025-09 | Lista de fichas sem badge de status | P2 | 0.5 dia |
| GAP-025-10 | canEdit() ignora status | P2 | 0.5 dia |
| GAP-025-11 | Header sem badge de status | P2 | 0.5 dia |
| GAP-025-12 | Labels do wizard errados | P2 | 15 min (fix cirúrgico) |
| GAP-025-13 | Item "Criar Ficha" duplicado no sidebar | P3 | 15 min |
| GAP-025-14 | Permissões de HabilidadeConfig para Jogador | P3 | Requer decisão PO |

---

## 5. Épico e User Stories

**Épico E-025: Completar o Fluxo do Jogador (Ciclo de Vida da Ficha)**

| Story | Título | Prioridade |
|---|---|---|
| US-025-01 | Marcar Ficha como Morta (Mestre) | P0 |
| US-025-02 | Criar Nova Ficha após Morte | P0 |
| US-025-03 | Editar Ficha pela Tela de Detalhe (fix rota) | P0 |
| US-025-04 | Atualizar Vida e Essência em Sessão | P1 |
| US-025-05 | Ficha Atualiza Automaticamente (polling) | P1 |
| US-025-06 | Descobrir Jogos Disponíveis (endpoint público) | P1 |
| US-025-07 | Jogador Notificado de Aprovação (polling) | P1 |
| US-025-08 | Visualizar Membros do Corpo em Sessão | P1 |
| US-025-09 | Editar Ficha Existente pelo Wizard (fix path param) | P2 |
| US-025-10 | Identificar Status na Listagem | P2 |

---

## 6. Critérios de Aceite Detalhados

### US-025-01: Marcar Ficha como Morta

```
Cenário 1: Mestre marca ficha como morta
  Dado que sou Mestre e visualizo ficha de status ATIVA
  Quando clico em "Marcar como Morta" e confirmo
  Então POST /api/v1/fichas/{id}/status com body {status: "MORTA"} é enviado
  E a ficha volta ao header com badge vermelho "Morta"
  E os botões "Editar" e "Excluir" somem da visão do Jogador

Cenário 2: Backend rejeita deleção de ficha morta
  Dado que uma ficha está com status MORTA
  Quando chamo DELETE /api/v1/fichas/{id}
  Então o backend retorna 409 com mensagem "Ficha não pode ser deletada: status MORTA"

Cenário 3: Mestre pode reverter status
  Dado que uma ficha está com status MORTA
  Quando o Mestre clica em "Reativar Ficha"
  Então o status muda para ATIVA
```

### US-025-03: Fix Rota de Edição

```
Cenário 1: Botão Editar navega corretamente (ficha-detail)
  Dado que estou em /jogador/fichas/42
  Quando clico em "Editar"
  Então navego para /jogador/fichas/42/edit (não /fichas/42/editar)

Cenário 2: Wizard carrega dados ao editar via path param
  Dado que acesso /jogador/fichas/42/edit
  Quando o FichaWizardComponent inicializa
  Então lê fichaId = 42 do route.snapshot.params['id']
  E chama carregarRascunho(42)
  E exibe os dados atuais da ficha no passo 1
```

### US-025-09: Fix Wizard Edição

```
Cenário 1: Wizard lê fichaId do path param
  Dado que acesso /jogador/fichas/42/edit
  Quando FichaWizardComponent.ngOnInit() executa
  Então lê fichaId do route.snapshot.params['id'] se queryParam 'fichaId' não existe
  E define fichaId.set(42)
  E chama carregarRascunho(42)

Cenário 2: determinarPassoInicial usa dados carregados
  Dado que a ficha 42 está COMPLETA com todos os campos preenchidos
  Quando o wizard termina de carregar o rascunho
  Então inicia no passo 1 (para edição total)
  Ou oferece opção de pular para o passo específico
```

---

## 7. Dossiê de Regras por Tela/Funcionalidade

### Tela: Fichas List (`/jogador/fichas`)

| Elemento | Tipo | Regra de Negócio | Gap |
|---|---|---|---|
| Botão "Excluir" | Ação | Visível apenas para Mestre. Jogador NÃO pode excluir fichas. | GAP-025-01 |
| Badge de status | Tag | Exibir: "Incompleta" (RASCUNHO), "Morta" (MORTA), "Abandonada" (ABANDONADA). ATIVA/COMPLETA = sem badge extra | GAP-025-09 |
| Botão "Editar" | Ação | Oculto para fichas MORTA ou ABANDONADA | GAP-025-10 |
| Botão "Criar Ficha" | Ação | Presente e funcional — acessível sem jogo (redireciona com aviso) | OK |

### Tela: Ficha Detail (`/jogador/fichas/:id`)

| Elemento | Tipo | Regra de Negócio | Gap |
|---|---|---|---|
| Botão "Editar" | Ação | Navega para `/jogador/fichas/:id/edit` (com prefixo correto) | GAP-025-02 |
| Botão "Deletar" | Ação | Visível somente para Mestre | OK (já está assim) |
| Badge status MORTA | Tag | Exibir no header quando status = MORTA | GAP-025-11 |
| Aba "Sessão/Combate" | Tab | Não existe — deve ser criada | GAP-025-07 |
| Polling 30s | Behaviour | Não existe — deve ser implementado | GAP-025-04 |
| Barra de vida/essência | Display | Presente no header mas sem controles de edição | GAP-025-03 |

### Tela: Wizard de Ficha (`/jogador/fichas/nova` ou `/edit`)

| Elemento | Tipo | Regra de Negócio | Gap |
|---|---|---|---|
| Leitura de fichaId | Param | Deve ler de `queryParams.fichaId` E de `route.params.id` | GAP-025-08 |
| Labels dos passos | UI | Passo 5 = "Vantagens", Passo 6 = "Revisão" (não "Revisao"/"Conclusao") | GAP-025-12 |
| Header do wizard | UI | Título deve ser "Criar Personagem" (nova) ou "Editar Personagem" (edição) | GAP-025-08 |

### Tela: Jogos Disponíveis (`/jogador/jogos`)

| Elemento | Tipo | Regra de Negócio | Gap |
|---|---|---|---|
| Lista de jogos | Display | Exibe apenas jogos onde o usuário já tem role — não exibe jogos descobríveis | GAP-025-05 |
| Polling de status | Behaviour | Não existe — aprovação/rejeição não é notificada | GAP-025-06 |
| Estado vazio | UI | Mensagem diz "Peça ao Mestre para te adicionar" — mensagem aceitável até implementar discovery | OK (temporário) |

---

## 8. Regras de Negócio Críticas do Domínio Klayrah

- **Fichas NUNCA são deletadas** (decisão INCONS-02 do PO): o endpoint `DELETE /fichas/{id}` existe no backend e o Mestre pode usá-lo tecnicamente, mas a regra de negócio diz que fichas devem ter status `MORTA` ou `ABANDONADA` em vez de ser deletadas. O frontend para Jogadores não deve expor DELETE.
- **Jogador pode ter múltiplas fichas** por jogo, inclusive fichas mortas e ativas ao mesmo tempo.
- **Renascimento**: campo `renascimentos` existe no modelo (backend e frontend) mas não há UI para gerenciá-lo. Requer decisão do PO.
- **Modo Sessão** (polling 30s): aprovado pelo PO como MVP. Sem implementação atual.
- **Status de aprovação**: PENDENTE → APROVADO/REJEITADO. REJEITADO pode re-solicitar (sem cooldown). BANIDO não pode re-solicitar enquanto banido.

---

## 9. Pontos em Aberto / Perguntas para o PO

| Código | Pergunta | Impacto |
|---|---|---|
| PA-025-01 | O status `COMPLETA` do backend deve ser renomeado para `ATIVA`? Ou `ATIVA` é um terceiro status (após COMPLETA)? | Afeta enum backend, model frontend, todas as comparações de status |
| PA-025-02 | O campo `renascimentos` (já existe no modelo) tem lógica de negócio implementada? Qual o fluxo? O Jogador "renasce" perdendo atributos? | Afeta Spec do GAP-025-01 |
| PA-025-03 | Existe o conceito de "jogo público vs privado" para a descoberta de jogos? Ou todos os jogos ativos são descobríveis por qualquer Jogador? | Afeta GAP-025-05 e modelo do Jogo |
| PA-025-04 | O endpoint `POST /fichas/{id}/essencia/gastar` foi planejado mas não implementado (decisão PO GAP-07). Deve ser incluído nesta spec? | Afeta GAP-025-03 e GAP-025-07 |
| PA-025-05 | HabilidadeConfig: é uma config do jogo (Mestre controla) ou uma habilidade pessoal do personagem (Jogador controla)? | Afeta GAP-025-14 e possível refactor do módulo |
| PA-025-06 | Quando o Mestre clica "Deletar" em uma ficha, o sistema deve oferecer "Marcar como Abandonada" em vez de deletar, ou deletar mesmo? | Afeta implementação do GAP-025-01 |

---

## 10. Checklist de Validação UX

Os seguintes pontos precisam de revisão pelo Especialista UX antes da implementação:

- [ ] **Aba de Sessão/Combate**: onde encaixar na ordem das abas? Antes de Atributos (mais acessada em sessão) ou como aba dedicada? Mobile-first é crítico aqui.
- [ ] **Controles de vida/essência**: slider, input numérico ou botões +/-? Qual é mais rápido em sessão de mesa (celular na mão)?
- [ ] **Badge de status MORTA**: vermelho com ícone de caveira é aceitável culturalmente? Ou um ícone mais neutro?
- [ ] **Descoberta de jogos**: separar em seções "Meus Jogos" e "Outros Jogos Disponíveis" ou unificar com filtro?
- [ ] **Polling**: o spinner discreto de "atualizando" deve ser visível? Pode ser distrativo durante a sessão.
- [ ] **Wizard de edição**: deve iniciar no passo 1 (edição total) ou oferecer resumo com "editar passo X" como a revisão já faz?
