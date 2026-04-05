# Spec 010 — Refatoracao de Roles: ADMIN / MESTRE / JOGADOR

> Spec: `010-roles-refactor`
> Epic: EPIC 10
> Status: Aguardando implementacao
> Depende de: Spec 001 (Usuario entity), Spec 005 (JogoParticipante)
> Bloqueia: nenhuma spec futura diretamente (melhoria transversal de seguranca)

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O sistema possui apenas dois perfis globais — MESTRE e JOGADOR — definidos manualmente no banco de dados. Nao existe um super-administrador com visibilidade total, nao existe fluxo de onboarding para usuario novo definir seu perfil, e nao existe tela de administracao para promover ou revogar roles.

**Objetivo:** Introduzir a role ADMIN como super-administrador do sistema, preservar MESTRE e JOGADOR com suas semânticas atuais, e criar um fluxo de onboarding para que usuarios novos se autodeclarem como MESTRE ou JOGADOR. O primeiro ADMIN sera provisionado via seed SQL.

**Valor entregue:**
- ADMIN tem acesso irrestrito a todos os recursos, util para suporte e operacao
- MESTRE continua controlando tudo relacionado aos jogos que criou
- JOGADOR continua acessando tudo nos jogos em que participa (aprovado)
- Usuario novo tem fluxo claro de primeiro acesso sem depender de promocao manual
- ADMIN pode promover e revogar roles de qualquer usuario via tela de administracao

---

## 2. Atores Envolvidos

| Ator | Role global | Descricao |
|------|-------------|-----------|
| Administrador | ADMIN | Super-admin do sistema. Acesso irrestrito a todos os recursos e usuarios. Provisionado via seed SQL. |
| Mestre | MESTRE | Acesso total aos jogos que criou e suas fichas/configuracoes. |
| Jogador | JOGADOR | Acesso aos jogos em que e participante APROVADO. |
| Novo usuario | (sem role) | Acabou de se autenticar via OAuth2. Deve completar o onboarding antes de acessar o sistema. |

---

## 3. Modelo de Roles

### 3.1 Hierarquia de permissoes

```
ADMIN
  |-- herda tudo de MESTRE
  |-- herda tudo de JOGADOR
  |-- acoes exclusivas: gerenciar usuarios, promover/revogar roles

MESTRE
  |-- todas as acoes de escrita em jogos proprios (POST, PUT, DELETE)
  |-- acoes de leitura em jogos proprios
  |-- nao pode acessar jogos de outros Mestres via role global

JOGADOR
  |-- leitura de configuracoes dos jogos onde e participante APROVADO
  |-- escrita limitada em proprias fichas
```

**Nota critica:** A role global (ADMIN / MESTRE / JOGADOR) e diferente da role no jogo (RoleJogo.MESTRE / RoleJogo.JOGADOR em JogoParticipante). Um usuario com role global JOGADOR pode ser o Mestre de um jogo especifico — isso e definido pelo RoleJogo no JogoParticipante, nao pela role global.

**Decisao de produto (2026-04-02):** A role global define o PERFIL de uso do sistema, nao o papel em um jogo especifico. Um usuario pode ter role global JOGADOR e ainda assim ser Mestre de um jogo porque o JogoParticipante define isso. O principal efeito da role global e no onboarding e nos guards de UI.

### 3.2 Estado do usuario apos primeiro login OAuth2

```
Novo usuario faz login com Google
         |
         v
  role = null (sem role definida)
         |
         v
  Redirecionar para tela de onboarding
         |
    Escolha do usuario:
    [Quero jogar como Jogador] --> role = JOGADOR
    [Quero ser Mestre de campanha] --> role = MESTRE
```

### 3.3 Promocao e revogacao de roles

- Apenas ADMIN pode promover ou revogar a role de qualquer usuario
- Promocao: JOGADOR -> MESTRE, JOGADOR -> ADMIN, MESTRE -> ADMIN
- Revogacao: ADMIN -> MESTRE, ADMIN -> JOGADOR, MESTRE -> JOGADOR
- ADMIN nao pode revogar a propria role ADMIN (prevencao de lock-out)
- Deve existir sempre ao menos um ADMIN no sistema (validacao antes de revogar)

---

## 4. Requisitos Funcionais

### Backend

**RF-01** O enum/campo `role` na entidade `Usuario` deve suportar tres valores: `JOGADOR`, `MESTRE`, `ADMIN`.

**RF-02** Usuario novo (primeiro login OAuth2) recebe `role = null` (sem role) e e redirecionado para o endpoint de onboarding.

**RF-03** O endpoint `POST /api/v1/usuarios/me/role` permite que um usuario sem role defina sua propria role como `MESTRE` ou `JOGADOR`. Nao e possivel autodefinir `ADMIN`.

**RF-04** Todos os `@PreAuthorize("hasRole('MESTRE')")` devem ser atualizados para `@PreAuthorize("hasAnyRole('ADMIN', 'MESTRE')")`.

**RF-05** Todos os `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` devem ser atualizados para `@PreAuthorize("hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')")`.

**RF-06** O endpoint `GET /api/v1/admin/usuarios` lista todos os usuarios do sistema (paginado). Apenas ADMIN.

**RF-07** O endpoint `PUT /api/v1/admin/usuarios/{id}/role` promove ou revoga a role de um usuario. Apenas ADMIN.

**RF-08** O endpoint `GET /api/v1/usuarios/me` deve retornar o campo `role` no response. Se `role = null`, o frontend redireciona para onboarding.

**RF-09** A validacao `ParticipanteSecurityService.isMestreDoJogo()` deve continuar funcionando via RoleJogo no JogoParticipante — nao deve depender da role global.

**RF-10** Seed SQL deve prover o primeiro ADMIN com email configuravel via properties.

**RF-11** Usuarios existentes com role MESTRE ou JOGADOR nao devem ser afetados pela migracao — compatibilidade retroativa garantida.

### Frontend

**RF-12** Guard de rotas deve verificar: se `role = null`, redirecionar para `/onboarding`.

**RF-13** Wizard de onboarding: tela de boas-vindas com escolha de perfil (MESTRE ou JOGADOR) e chamada `POST /api/v1/usuarios/me/role`.

**RF-14** Tela `/admin/usuarios` exibe tabela paginada de usuarios com nome, email, role e acoes de promocao/revogacao. Acessivel apenas para ADMIN.

**RF-15** NavBar ou menu lateral deve exibir link para `/admin/usuarios` apenas quando `role = ADMIN`.

---

## 5. Requisitos Nao Funcionais

**RNF-01 Compatibilidade:** Usuarios existentes com role MESTRE ou JOGADOR nao perdem acesso. A migracao deve ser zero-downtime.

**RNF-02 Idempotencia:** O endpoint de definicao de role deve ser idempotente — chamadas repetidas com a mesma role nao devem lançar erro.

**RNF-03 Auditoria:** Todas as promocoes e revogacoes de role devem ser registradas no campo `updated_by` / `updated_at` da entidade `Usuario`.

**RNF-04 Seguranca:** Nenhum usuario pode autodefinir role ADMIN. A validacao deve existir tanto no backend (service layer) quanto ser documentada no Swagger.

**RNF-05 Prevencao de lock-out:** Antes de revogar a role ADMIN de um usuario, o sistema verifica se existe pelo menos um outro ADMIN ativo. Se nao houver, a operacao e rejeitada com HTTP 409.

**RNF-06 Performance:** O endpoint de listagem de usuarios deve suportar paginacao (`page`, `size`, `sort`).

---

## 6. Epico e User Stories

**EPIC 10 — Refatoracao de Roles: ADMIN / MESTRE / JOGADOR**

### US-10-01: Onboarding — definicao de role no primeiro acesso

**Como** usuario novo (sem role definida),
**Quero** poder escolher meu perfil (MESTRE ou JOGADOR) no primeiro acesso,
**Para** acessar o sistema com as permissoes corretas sem depender de intervencao manual.

**Cenario 1: Definicao bem-sucedida como JOGADOR**
```
Dado que o usuario autenticado possui role = null
Quando o usuario envia POST /api/v1/usuarios/me/role com { "role": "JOGADOR" }
Entao o sistema atualiza a role para JOGADOR
E retorna HTTP 200 com o usuario atualizado
E o usuario pode acessar os endpoints de JOGADOR

Cenario 2: Definicao bem-sucedida como MESTRE
Dado que o usuario autenticado possui role = null
Quando o usuario envia POST /api/v1/usuarios/me/role com { "role": "MESTRE" }
Entao o sistema atualiza a role para MESTRE
E retorna HTTP 200 com o usuario atualizado

Cenario 3: Tentativa de autodefinir ADMIN
Dado que o usuario autenticado possui role = null
Quando o usuario envia POST /api/v1/usuarios/me/role com { "role": "ADMIN" }
Entao o sistema retorna HTTP 403
E a mensagem de erro indica que ADMIN nao pode ser autodefinido

Cenario 4: Usuario ja possui role definida
Dado que o usuario autenticado possui role = JOGADOR
Quando o usuario envia POST /api/v1/usuarios/me/role com { "role": "MESTRE" }
Entao o sistema retorna HTTP 409
E a mensagem indica que o perfil ja foi definido e nao pode ser alterado sem intervencao de ADMIN
```

### US-10-02: ADMIN — listagem de usuarios

**Como** usuario com role ADMIN,
**Quero** visualizar todos os usuarios do sistema com suas roles atuais,
**Para** ter visibilidade de quem esta cadastrado e gerenciar acessos.

**Cenario 1: Listagem com sucesso**
```
Dado que o usuario autenticado possui role = ADMIN
Quando o usuario acessa GET /api/v1/admin/usuarios?page=0&size=20
Entao o sistema retorna HTTP 200 com lista paginada de usuarios
E cada item contem: id, nome, email, role, dataCriacao

Cenario 2: Acesso negado para nao-ADMIN
Dado que o usuario autenticado possui role = MESTRE ou JOGADOR
Quando o usuario acessa GET /api/v1/admin/usuarios
Entao o sistema retorna HTTP 403
```

### US-10-03: ADMIN — promocao e revogacao de roles

**Como** usuario com role ADMIN,
**Quero** poder promover ou revogar a role de qualquer usuario,
**Para** controlar quem tem acesso de ADMIN, MESTRE e JOGADOR no sistema.

**Cenario 1: Promocao de JOGADOR para MESTRE**
```
Dado que o usuario autenticado possui role = ADMIN
E existe um usuario alvo com role = JOGADOR
Quando o ADMIN envia PUT /api/v1/admin/usuarios/{id}/role com { "role": "MESTRE" }
Entao o sistema atualiza a role do usuario alvo para MESTRE
E retorna HTTP 200 com o usuario atualizado

Cenario 2: Prevencao de auto-revogacao de ADMIN
Dado que o usuario autenticado possui role = ADMIN e e o unico ADMIN do sistema
Quando o ADMIN envia PUT /api/v1/admin/usuarios/{idProprio}/role com { "role": "MESTRE" }
Entao o sistema retorna HTTP 409
E a mensagem indica que o ultimo ADMIN nao pode se revogar

Cenario 3: Promocao para ADMIN
Dado que o usuario autenticado possui role = ADMIN
E existe um usuario alvo com role = MESTRE
Quando o ADMIN envia PUT /api/v1/admin/usuarios/{id}/role com { "role": "ADMIN" }
Entao o sistema atualiza a role do usuario alvo para ADMIN
E retorna HTTP 200
```

### US-10-04: ADMIN acessa todos os endpoints do sistema

**Como** usuario com role ADMIN,
**Quero** que minha role seja reconhecida em todos os endpoints de MESTRE e JOGADOR,
**Para** poder suportar e debugar qualquer problema sem precisar de promocao contextual.

**Cenario 1: ADMIN acessa endpoint restrito a MESTRE**
```
Dado que o usuario autenticado possui role = ADMIN
Quando o usuario acessa POST /api/v1/jogos/{id}/atributos
Entao o sistema retorna HTTP 201 (nao 403)

Cenario 2: ADMIN acessa endpoint restrito a JOGADOR
Dado que o usuario autenticado possui role = ADMIN
Quando o usuario acessa GET /api/v1/jogos/{id}/fichas
Entao o sistema retorna HTTP 200 (nao 403)
```

### US-10-05: Seed SQL — primeiro ADMIN

**Como** operador do sistema na instalacao inicial,
**Quero** que o primeiro ADMIN seja criado automaticamente via seed SQL,
**Para** nao precisar de acesso direto ao banco para o bootstrap inicial.

**Cenario 1: Seed aplicado**
```
Dado que o banco esta vazio e o seed foi executado
Quando o usuario com o email configurado faz login via OAuth2
Entao o sistema reconhece o email, associa o providerId e mantem role = ADMIN
E o usuario tem acesso completo ao sistema

Cenario 2: Seed idempotente
Dado que o seed ja foi executado anteriormente
Quando o seed e executado novamente
Entao o sistema nao duplica o usuario (ON CONFLICT DO NOTHING)
```

### US-10-06: Frontend — wizard de onboarding

**Como** usuario novo sem role,
**Quero** ser direcionado automaticamente para uma tela de boas-vindas ao fazer login,
**Para** escolher meu perfil de forma intuitiva e comecar a usar o sistema.

**Cenario 1: Redirect automatico**
```
Dado que o usuario fez login e GET /api/v1/usuarios/me retorna role = null
Quando o Angular inicializa o AuthGuard
Entao o usuario e redirecionado para /onboarding

Cenario 2: Onboarding completo
Dado que o usuario esta na tela /onboarding
Quando o usuario clica em "Quero ser Mestre" ou "Quero jogar"
E a chamada POST /api/v1/usuarios/me/role retorna HTTP 200
Entao o usuario e redirecionado para a pagina inicial
E o guard nao redireciona mais para /onboarding
```

### US-10-07: Frontend — tela de administracao

**Como** usuario com role ADMIN,
**Quero** uma tela /admin/usuarios para visualizar e gerenciar roles,
**Para** ter controle do acesso ao sistema sem precisar de acesso direto ao banco.

**Cenario 1: Listagem e acao**
```
Dado que o usuario autenticado possui role = ADMIN
Quando o usuario acessa /admin/usuarios
Entao ve tabela com nome, email, role atual, data de criacao
E pode clicar em "Alterar role" para cada usuario
E a alteracao e confirmada via dialog modal

Cenario 2: Rota protegida
Dado que o usuario autenticado possui role = MESTRE ou JOGADOR
Quando o usuario tenta acessar /admin/usuarios diretamente
Entao e redirecionado para pagina de acesso negado (403)
```

---

## 7. Dossie de Regras por Funcionalidade

### 7.1 Endpoint POST /api/v1/usuarios/me/role

| Elemento | Regra de Negocio | Validacao | HTTP |
|---|---|---|---|
| Campo `role` | Aceita apenas `MESTRE` ou `JOGADOR` | Enum validation + custom validator | 400 se invalido |
| Campo `role = ADMIN` | Proibido por qualquer usuario | Service layer check | 403 |
| Precondicao | Usuario deve ter role = null | Service layer check | 409 se ja tem role |
| Idempotencia | Mesmo role enviado novamente | Opcional: aceitar silenciosamente | 200 |

### 7.2 Endpoint PUT /api/v1/admin/usuarios/{id}/role

| Elemento | Regra de Negocio | Validacao | HTTP |
|---|---|---|---|
| Autenticacao | Apenas ADMIN | `@PreAuthorize("hasRole('ADMIN')")` | 403 |
| Alvo inexistente | Usuario {id} nao encontrado | Repository check | 404 |
| Auto-revogacao de ADMIN | ADMIN nao pode remover propria role se for o unico | Count query | 409 |
| Auditoria | updated_by = id do ADMIN que fez a acao | `@CreatedBy` / `@LastModifiedBy` | — |

### 7.3 Campo role no Usuario

| Estado | Valor | Comportamento |
|---|---|---|
| Usuario novo | null | Redireciona para onboarding. Nenhum endpoint alem de /me e /me/role e acessivel |
| Jogador | JOGADOR | Acesso a endpoints `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')` |
| Mestre | MESTRE | Acesso a endpoints `hasAnyRole('ADMIN', 'MESTRE')` e `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')` |
| Admin | ADMIN | Acesso irrestrito a todos os endpoints de qualquer role |

### 7.4 Impacto nos @PreAuthorize existentes

| Padrao atual | Novo padrao | Quantidade de ocorrencias |
|---|---|---|
| `hasRole('MESTRE')` | `hasAnyRole('ADMIN', 'MESTRE')` | ~50 ocorrencias em 25 controllers |
| `hasAnyRole('MESTRE', 'JOGADOR')` | `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')` | ~30 ocorrencias em 25 controllers |
| `isAuthenticated()` | `isAuthenticated()` (sem mudanca) | 2 ocorrencias em UsuarioController |

Controllers afetados (todos os 25 controllers com @PreAuthorize):
- `JogoController`, `JogoParticipanteController`, `FichaController`
- `FichaAnotacaoController`, `DashboardController`
- `configuracao/`: AtributoController, AptidaoController, BonusController, ClasseController, DadoProspeccaoController, FormulaController, GeneroController, IndoleController, MembroCorpoController, NivelController, PresencaController, PontosVantagemController, RacaController, SiglaController, TipoAptidaoController, VantagemController, VantagemEfeitoController, CategoriaVantagemController

---

## 8. Regras de Negocio Criticas do Dominio

**RN-01: Separacao entre role global e role no jogo**
A role global (`Usuario.role`) define o perfil de uso do sistema. A role no jogo (`JogoParticipante.roleJogo`) define o papel em uma campanha especifica. Um usuario com role global JOGADOR pode ter `RoleJogo.MESTRE` em um jogo — isso e valido e deve continuar funcionando.

**RN-02: ParticipanteSecurityService nao deve ser alterado**
A validacao de acesso contextual ao jogo (`isMestreDoJogo`, `isParticipanteAprovado`, `canAccessJogo`) opera sobre `JogoParticipante.roleJogo` e deve permanecer intocada. Apenas os `@PreAuthorize` de role global sao afetados por esta spec.

**RN-03: ADMIN nao e Mestre automatico de todos os jogos**
Ter role ADMIN nao insere o usuario como participante de todos os jogos. O ADMIN acessa endpoints pelo `@PreAuthorize`, mas metodos de service que fazem lookup por `mestreId` (ex: `jogoService.buscarJogo(id)`) devem tratar ADMIN com bypass explicito ou o ADMIN deve ser participante para operar naquele contexto.

**ATENCAO — Ponto em aberto:** Ver Secao 9, item P-03.

**RN-04: Onboarding e obrigatorio e nao pode ser pulado**
Um usuario com `role = null` nao pode acessar nenhum endpoint de negocio. O backend deve validar e retornar HTTP 403 com mensagem orientando o usuario a completar o onboarding. O frontend deve garantir o redirect automatico.

**RN-05: Seed do primeiro ADMIN**
O seed deve usar `ON CONFLICT (email) DO UPDATE SET role = 'ADMIN'` para garantir que se o usuario ja existia como JOGADOR, ele seja promovido na inicializacao. O `providerId` e atualizado no primeiro login OAuth2 como ja ocorre hoje.

---

## 9. Pontos em Aberto / Perguntas para Stakeholder

**P-01: Onboarding — usuario pode mudar de ideia?**
Uma vez que o usuario definiu sua role via onboarding como JOGADOR, ele pode mudar para MESTRE depois sem intervencao de ADMIN? Ou essa escolha e permanente ate um ADMIN intervir?

**Premissa adotada:** A escolha do onboarding e permanente. Apenas ADMIN pode alterar a role depois. Isso evita que usuarios burlem restricoes trocando de role livremente.

**P-02: Usuarios sem role tem acesso a endpoints publicos (Swagger, /me)?**
O endpoint `GET /api/v1/usuarios/me` deve ser acessivel por usuarios sem role para que o frontend possa detectar o estado de onboarding.

**Premissa adotada:** `GET /api/v1/usuarios/me` usa `@PreAuthorize("isAuthenticated()")` e permanece acessivel. Apenas endpoints de negocio requerem role definida.

**P-03: ADMIN e bypass total de validacao de acesso ao jogo ou precisa ser participante?**
Se o ADMIN acessa `GET /api/v1/jogos/{id}/fichas`, o service valida se ele e participante aprovado? Se sim, ADMIN precisa solicitar entrada no jogo para operar. Se nao, o service precisa de um bypass explicito para ADMIN.

**✅ DECIDIDO PELO PO (2026-04-03):** ADMIN pode tudo — bypass total de `canAccessJogo()`. O foco principal do ADMIN e gerenciar usuarios (promover/revogar roles), mas tecnicamente tem acesso irrestrito a todos os recursos. Implementar bypass explicito em `ParticipanteSecurityService`: se `role == ADMIN`, retorna `true` sem verificar participacao.

**P-04: Nomenclatura no onboarding**
O frontend deve usar os termos "Jogador" e "Mestre" literalmente, ou usar linguagem mais amigavel como "Quero jogar em campanhas de outros" vs "Quero criar e conduzir campanhas"?

**Recomendacao:** Usar linguagem descritiva no UI, mas manter os valores tecnicos `MESTRE` e `JOGADOR` na API.

**P-05: Tela de admin — filtros e busca**
A tela `/admin/usuarios` precisa de busca por nome/email e filtro por role? Ou listagem simples com paginacao e suficiente para o MVP?

---

## 10. Checklist de Validacao UX

- [ ] Fluxo de onboarding: a escolha de perfil e clara para usuarios nao-tecnicos? Os termos "Mestre" e "Jogador" fazem sentido sem contexto de RPG?
- [ ] Wizard de onboarding: quantas etapas? Apenas escolha de role, ou inclui nome de exibicao e foto de perfil?
- [ ] Feedback quando usuario sem role tenta acessar area restrita: mensagem de erro ou redirect silencioso para onboarding?
- [ ] Tela admin: a acao de alterar role exige confirmacao (modal/dialog) para prevenir cliques acidentais?
- [ ] NavBar: o link para /admin/usuarios deve aparecer como item separado ou dentro de um menu de configuracoes?
- [ ] Estado de loading durante a chamada de definicao de role no onboarding: o botao deve ser desabilitado durante a request?
- [ ] Tratamento de erro no onboarding: se a chamada POST /me/role falhar, o usuario ve mensagem de retry ou e redirecionado para login?
