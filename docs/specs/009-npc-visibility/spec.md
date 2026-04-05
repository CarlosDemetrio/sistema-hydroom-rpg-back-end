# Spec 009-ext — Extensao de NPC: Visibilidade por Jogador, Prospeccao, Essencia e Reset de Estado

> Spec: `009-npc-visibility`
> Epic: EPIC 8 — Extensao (complemento da Spec 009 base)
> Status: Refinado — decisoes do PO confirmadas em 2026-04-02
> Depende de: Spec 009 base (NPC + Fichas implementados no branch `feature/009-npc-fichas-mestre`)
> Bloqueia: nada — feature de estado de combate e gerenciamento de sessao
> Design UX: `docs/design/NPC-VISIBILITY.md`, `docs/design/PROSPECCAO-SESSAO.md`

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O sistema atual trata NPCs como fichas binarias (visivel para todos os jogadores ou para nenhum). Nao ha controle granular de quem ve o que. Alem disso, a essencia nao e persistida como estado atual separado do total calculado — e prospecção usa um endpoint PUT generico que nao reflete a semantica de negocio (jogador usa, mestre reverte). Por fim, nao existe operacao de reset de estado para o Mestre encerrar uma sessao e restaurar todas as fichas ao estado base.

**Objetivo:** Implementar quatro extensoes ao modulo de fichas:
1. **Visibilidade granular de NPC** — Mestre controla quais jogadores veem os stats de cada NPC
2. **Essencia como estado de combate persistido** — campo `essenciaAtual` com endpoint dedicado (ja existe em `FichaEssencia`, precisa de endpoint simetrico a `/vida`)
3. **Prospeccao com semantica correta** — endpoints separados para uso pelo Jogador e reversao pelo Mestre
4. **Reset de estado** — endpoint unico para o Mestre restaurar vida + essencia + prospeccao de uma ficha ao estado base

**Valor entregue:**
- Mestre pode revelar stats de NPCs especificos para jogadores especificos (besta parceira, aliado)
- Jogadores so veem NPCs que o Mestre quis revelar
- Semantica de negocio da prospecção e respeitada: Jogador usa, Mestre arbitra
- Ao fim de sessao, Mestre pode resetar o estado de combate de todas as fichas com um clique

---

## 2. Atores Envolvidos

| Ator | Role | Acoes nesta spec |
|------|------|-----------------|
| Mestre | MESTRE | Revelar/ocultar NPC para jogadores especificos; conceder dados de prospeccao; reverter uso de prospeccao; atualizar essencia de qualquer ficha; resetar estado de ficha |
| Jogador | JOGADOR | Ver NPCs revelados para si com stats completos; usar dado de prospeccao (decrementa contador); ver estado de essencia atual da propria ficha |
| Sistema | — | Filtrar NPCs na listagem conforme `FichaVisibilidade`; validar permissoes de acesso granular |

---

## 3. Requisitos Funcionais

### 3.1 Visibilidade de NPC

**RF-VIS-001** O Mestre pode revelar as estatisticas de um NPC para um ou mais jogadores especificos (participantes aprovados no jogo).

**RF-VIS-002** A visibilidade e granular por jogador — revelar para o Jogador A nao implica revelar para o Jogador B.

**RF-VIS-003** O NPC pode ser marcado como `visivelGlobalmente = true` para aparecer na listagem de todos os jogadores sem stats completos. Jogadores sem acesso granular veem apenas nome, raca, classe.

**RF-VIS-004** Jogador com acesso granular ao NPC pode abrir a ficha completa (todos os tabs: atributos, aptidoes, vantagens, vida).

**RF-VIS-005** Jogador sem acesso granular ao NPC pode ver o card do NPC na lista mas sem acesso a tela de detalhe.

**RF-VIS-006** Jogador sem `visivelGlobalmente = true` nao ve o NPC em nenhuma listagem.

**RF-VIS-007** O Mestre pode consultar a lista de jogadores com acesso a um NPC especifico.

**RF-VIS-008** O Mestre pode revogar o acesso granular de um jogador a um NPC.

**RF-VIS-009** A visibilidade e persistida na entidade `FichaVisibilidade` — nova tabela com `(fichaId, jogadorId)` onde `fichaId` referencia um NPC (`isNpc = true`).

**RF-VIS-010** O endpoint de listagem de NPCs do Jogador (`GET /jogos/{id}/fichas`) retorna somente NPCs onde `visivelGlobalmente = true` e inclui o campo `jogadorTemAcessoStats` indicando se o jogador atual tem acesso granular.

### 3.2 Essencia como Estado de Combate

**RF-ESS-001** A entidade `FichaEssencia` ja possui o campo `essenciaAtual`. Esta spec adiciona dois endpoints semanticos dedicados ao gasto e restauracao de essencia.

**RF-ESS-002** O campo `essenciaAtual` deve ser inicializado com o valor de `total` ao criar a ficha (ja implementado via `FichaService`).

**RF-ESS-003** O `FichaResumoResponse` deve expor `vidaAtual`, `essenciaAtual` e `essenciaTotal` separadamente. O campo `vidaAtual` tambem deve ser incluido (era exibido apenas como `vidaTotal`).

**RF-ESS-004** A barra de essencia e a barra de vida no frontend devem ser reativas: `essenciaAtual / essenciaTotal` e `vidaAtual / vidaTotal`. Nao hardcoded.

**RF-ESS-005** O Jogador pode gastar essencia via `POST /fichas/{id}/essencia/gastar` com payload `{ "quantidade": N }`. O endpoint valida que `essenciaAtual - N >= 0` (essencia nao pode ir negativo).

**RF-ESS-006** O Mestre pode restaurar essencia de qualquer ficha via `POST /fichas/{id}/essencia/curar` com payload `{ "quantidade": N }`. O endpoint valida que `essenciaAtual + N <= total`.

**RF-ESS-007** Ambos os endpoints retornam o `FichaResumoResponse` atualizado.

> **Decisao de design (2026-04-03):** A auditoria `docs/analises/UX-FICHAS-AUDITORIA.md` (REC-009-01) identificou inconsistencia entre o design `MODO-SESSAO.md` (sec.12, que descreve `POST /essencia/gastar` e `POST /essencia/curar`) e a versao anterior desta spec (que reutilizava `PUT /fichas/{id}/vida` passando `essenciaAtual`). A abordagem adotada e usar os dois endpoints semanticos dedicados: sao mais expressivos, mais seguros (JOGADOR so pode chamar `/gastar`, MESTRE pode chamar ambos) e alinham com o contrato de API documentado em `MODO-SESSAO.md`. O `PUT /fichas/{id}/vida` continua existindo para atualizacao direta do Mestre, mas nao deve ser usado pelo frontend para gasto incremental de essencia em Modo Sessao.

### 3.3 Prospeccao com Semantica de Negocio

**RF-PROS-001** O Jogador pode registrar o uso de um dado de prospeccao especifico. Esta acao decrementa a `quantidade` do dado em 1 e cria um registro de uso com status `PENDENTE`.

**RF-PROS-002** O Jogador NAO pode reverter o proprio uso sozinho.

**RF-PROS-003** O Mestre pode reverter um uso PENDENTE, devolvendo o dado ao inventario do Jogador.

**RF-PROS-004** O Mestre pode confirmar um uso PENDENTE, finalizando o consumo.

**RF-PROS-005** O Mestre pode conceder dados de prospeccao a uma ficha (incrementa quantidade de um tipo de dado).

**RF-PROS-006** Um jogador nao pode usar um dado se `quantidade = 0` — endpoint retorna HTTP 422.

**RF-PROS-007** O sistema exibe para o Mestre todos os usos pendentes de confirmacao (globalmente por jogo e individualmente por ficha).

**RF-PROS-008** O resultado fisico do dado NAO e registrado pelo sistema — o sistema registra apenas que o dado foi usado e quando.

**RF-PROS-009** A nova entidade `ProspeccaoUso` registra cada uso com: `fichaProspeccaoId`, `status` (PENDENTE / CONFIRMADO / REVERTIDO), `criadoEm`.

### 3.4 Reset de Estado

**RF-RESET-001** O Mestre pode resetar o estado de combate de uma ficha com um unico endpoint.

**RF-RESET-002** O reset inclui: `vidaAtual = vidaTotal` de FichaVida; `danoRecebido = 0` em todos os FichaVidaMembro; `essenciaAtual = total` de FichaEssencia; quantidade de todos os FichaProspeccao volta ao total concedido (interpretado como quantidade atual — sem alterar o historico).

**RF-RESET-003** O reset NAO altera atributos, nivel, xp, vantagens ou qualquer outro dado calculado da ficha.

**RF-RESET-004** O reset e irreversivel — o Mestre deve confirmar na UI antes de executar.

**RF-RESET-005** Somente o MESTRE pode executar o reset.

**RF-RESET-006** O endpoint retorna o `FichaResumoResponse` atualizado apos o reset.

---

## 4. Requisitos Nao Funcionais

- **Seguranca:** `FichaVisibilidade` e verificada no service antes de retornar dados de NPC para Jogador. Nunca expor dados de NPC para jogador sem acesso.
- **Consistencia:** Reset e atomico — tudo ou nada dentro de uma unica transacao.
- **Performance:** A query de listagem de NPCs para Jogador deve usar JOIN em `FichaVisibilidade` sem N+1. Projecao em vez de entity completa para listagem.
- **Soft delete:** `FichaVisibilidade` usa soft delete padrao do `BaseEntity`.
- **Idempotencia:** Revelar acesso para um jogador que ja tem acesso deve ser idempotente (HTTP 200, sem duplicar registro).

---

## 5. Modelo de Dados

### 5.1 Nova Entidade: FichaVisibilidade

```
ficha_visibilidades
  id                BIGSERIAL PK
  ficha_id          BIGINT NOT NULL FK → fichas(id)     -- sempre um NPC (isNpc=true)
  jogador_id        BIGINT NOT NULL                     -- ID do usuario (nao da ficha)
  created_at        TIMESTAMP
  updated_at        TIMESTAMP
  deleted_at        TIMESTAMP                           -- soft delete
  created_by        VARCHAR
  updated_by        VARCHAR

  UNIQUE (ficha_id, jogador_id) WHERE deleted_at IS NULL
  INDEX (ficha_id)
  INDEX (jogador_id)
```

### 5.2 Nova Entidade: ProspeccaoUso

```
prospeccao_usos
  id                    BIGSERIAL PK
  ficha_prospeccao_id   BIGINT NOT NULL FK → ficha_prospeccao(id)
  status                VARCHAR(20) NOT NULL  -- PENDENTE | CONFIRMADO | REVERTIDO
  created_at            TIMESTAMP             -- momento do uso
  updated_at            TIMESTAMP
  deleted_at            TIMESTAMP

  INDEX (ficha_prospeccao_id)
  INDEX (status)
```

### 5.3 Alteracao em Ficha: campo visivelGlobalmente

Adicionar coluna `visivel_globalmente BOOLEAN NOT NULL DEFAULT FALSE` na tabela `fichas`.
Relevante apenas quando `is_npc = true`. Para fichas de jogadores, ignorado.

### 5.4 FichaEssencia: campo essenciaAtual (JA EXISTE)

O campo `essencia_atual` ja existe em `ficha_essencia` conforme leitura do codigo. Nenhuma migracao necessaria.

---

## 6. Endpoints

### 6.1 Visibilidade de NPC

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| `GET` | `/api/v1/fichas/{id}/visibilidade` | MESTRE | Listar jogadores com acesso aos stats do NPC |
| `POST` | `/api/v1/fichas/{id}/visibilidade` | MESTRE | Revelar stats do NPC para jogadores especificos |
| `DELETE` | `/api/v1/fichas/{id}/visibilidade/{jogadorId}` | MESTRE | Revogar acesso de jogador especifico |
| `PATCH` | `/api/v1/fichas/{id}/visibilidade/global` | MESTRE | Atualizar flag visivelGlobalmente |

**POST /fichas/{id}/visibilidade — Payload**
```json
{
  "jogadoresIds": [1, 2, 5],
  "substituir": true
}
```
> `substituir = true` apaga os registros existentes e cria novos. `substituir = false` apenas adiciona os novos sem remover os existentes.

**GET /fichas/{id}/visibilidade — Response**
```json
{
  "fichaId": 42,
  "visivelGlobalmente": true,
  "jogadoresComAcesso": [
    { "jogadorId": 1, "jogadorNome": "Ana", "nomePersonagem": "Aldric" },
    { "jogadorId": 2, "jogadorNome": "Joao", "nomePersonagem": "Lyra" }
  ]
}
```

### 6.2 Essencia (endpoints semanticos dedicados)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| `POST` | `/api/v1/fichas/{id}/essencia/gastar` | JOGADOR, MESTRE | Gastar N pontos de essencia (essenciaAtual - N) |
| `POST` | `/api/v1/fichas/{id}/essencia/curar` | MESTRE | Restaurar N pontos de essencia (essenciaAtual + N) |

**POST /fichas/{id}/essencia/gastar — Payload**
```json
{ "quantidade": 5 }
```

**POST /fichas/{id}/essencia/curar — Payload**
```json
{ "quantidade": 10 }
```

**Response (ambos os endpoints):** `FichaResumoResponse` atualizado.

**Validacoes:**
- `/gastar`: `essenciaAtual - quantidade >= 0`. Se negativo: HTTP 422 `"Essencia insuficiente"`.
- `/curar`: `essenciaAtual + quantidade <= total`. Se ultrapassar o total: clampear ao total (sem erro — curar alem do maximo restaura ao total).

**Nota de implementacao — FichaResumoResponse:**
Verificar se o DTO inclui `vidaAtual` e `essenciaAtual`. Se nao, adicionar ambos antes de implementar as tasks de frontend (T8 e T-QW Bug 1 dependem desses campos).

> **Decisao de design (2026-04-03):** Versao anterior desta spec reutilizava `PUT /fichas/{id}/vida`. Alterado para endpoints semanticos apos auditoria UX (REC-009-01). Ver secao 3.2 RF-ESS-005 a RF-ESS-007 para a justificativa completa. O `MODO-SESSAO.md` sec.12 ja usava esta convencao corretamente.

### 6.3 Prospeccao com Semantica

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| `POST` | `/api/v1/fichas/{id}/prospeccao/conceder` | MESTRE | Conceder quantidade de um tipo de dado |
| `POST` | `/api/v1/fichas/{id}/prospeccao/usar` | JOGADOR | Registrar uso de um dado (cria ProspeccaoUso PENDENTE) |
| `PATCH` | `/api/v1/fichas/{id}/prospeccao/usos/{usoId}/confirmar` | MESTRE | Confirmar uso PENDENTE |
| `PATCH` | `/api/v1/fichas/{id}/prospeccao/usos/{usoId}/reverter` | MESTRE | Reverter uso PENDENTE (devolve dado) |
| `GET` | `/api/v1/fichas/{id}/prospeccao/usos` | MESTRE, JOGADOR | Listar usos (Mestre ve todos; Jogador ve apenas proprios) |
| `GET` | `/api/v1/jogos/{jogoId}/prospeccao/pendentes` | MESTRE | Listar todos os usos pendentes do jogo |

**POST /fichas/{id}/prospeccao/conceder — Payload**
```json
{ "dadoProspeccaoConfigId": 3, "quantidade": 2 }
```

**POST /fichas/{id}/prospeccao/usar — Payload**
```json
{ "dadoProspeccaoConfigId": 3 }
```

**POST /fichas/{id}/prospeccao/usar — Response (ProspeccaoUsoResponse)**
```json
{
  "usoId": 101,
  "dadoNome": "d6",
  "dadoProspeccaoConfigId": 3,
  "status": "PENDENTE",
  "criadoEm": "2026-04-02T14:30:00"
}
```

### 6.4 Reset de Estado

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| `POST` | `/api/v1/fichas/{id}/resetar-estado` | MESTRE | Resetar vida, essencia e prospeccao ao estado base |

**POST /fichas/{id}/resetar-estado — Response**
Retorna `FichaResumoResponse` atualizado.

---

## 7. Epico e User Stories

### Epic 8-ext — Visibilidade de NPC, Prospeccao e Reset

---

**US-009ext-01: Revelar stats de NPC para jogadores especificos**
Como Mestre,
Quero controlar quais jogadores podem ver as estatisticas completas de um NPC especifico,
Para que apenas os jogadores com vínculo ao NPC (parceiro, aliado) tenham acesso ao detalhe da ficha.

Criterios de Aceite:

Cenario 1: Revelar para jogador especifico
  Dado que sou MESTRE de um jogo
  E existe um NPC com id=42 no jogo
  E o Jogador Ana (id=1) e participante aprovado
  Quando envio POST /api/v1/fichas/42/visibilidade com { "jogadoresIds": [1], "substituir": false }
  Entao um registro FichaVisibilidade e criado com (fichaId=42, jogadorId=1)
  E recebo HTTP 200 com a lista de jogadores com acesso

Cenario 2: Idempotencia ao revelar para jogador que ja tem acesso
  Dado que o Jogador Ana (id=1) ja tem acesso ao NPC 42
  Quando envio POST /api/v1/fichas/42/visibilidade com { "jogadoresIds": [1], "substituir": false }
  Entao nao e criado registro duplicado
  E recebo HTTP 200

Cenario 3: Revogar acesso
  Dado que o Jogador Ana (id=1) tem acesso ao NPC 42
  Quando envio DELETE /api/v1/fichas/42/visibilidade/1
  Entao o registro FichaVisibilidade e soft-deletado
  E Ana nao ve mais os stats do NPC

Cenario 4: Tentativa de revelar NPC que nao e NPC
  Dado que a ficha 10 e uma ficha de jogador (isNpc=false)
  Quando envio POST /api/v1/fichas/10/visibilidade
  Entao recebo HTTP 422 com mensagem "Visibilidade granular e valida apenas para NPCs"

---

**US-009ext-02: Jogador ve NPC revelado com stats completos**
Como Jogador,
Quero ver os stats completos de um NPC que o Mestre revelou para mim,
Para que eu possa usar essas informacoes durante a sessao.

Criterios de Aceite:

Cenario 1: Jogador com acesso granular ve stats completos
  Dado que sou Jogador com acesso granular ao NPC 42 (FichaVisibilidade registrado)
  E o NPC tem visivelGlobalmente=true
  Quando acesso GET /api/v1/fichas/42
  Entao recebo HTTP 200 com a ficha completa do NPC
  E o campo jogadorTemAcessoStats = true na resposta

Cenario 2: Jogador sem acesso granular ve NPC listado mas sem stats
  Dado que sou Jogador sem FichaVisibilidade para o NPC 42
  E o NPC tem visivelGlobalmente=true
  Quando acesso GET /api/v1/jogos/5/fichas
  Entao o NPC aparece na lista com jogadorTemAcessoStats = false
  Quando tento acessar GET /api/v1/fichas/42
  Entao recebo HTTP 403

Cenario 3: NPC com visivelGlobalmente=false nao aparece para nenhum jogador
  Dado que o NPC 43 tem visivelGlobalmente=false
  Quando acesso GET /api/v1/jogos/5/fichas
  Entao o NPC 43 nao aparece na listagem

---

**US-009ext-03: Jogador usa dado de prospeccao**
Como Jogador,
Quero registrar o uso de um dado de prospeccao,
Para que o Mestre saiba que usei o dado e possa confirmar ou reverter.

Criterios de Aceite:

Cenario 1: Uso bem-sucedido
  Dado que tenho 2 dados d6 de prospeccao disponíveis
  Quando envio POST /api/v1/fichas/{id}/prospeccao/usar com { "dadoProspeccaoConfigId": 3 }
  Entao a quantidade do dado d6 decrementada para 1
  E um ProspeccaoUso e criado com status=PENDENTE
  E recebo HTTP 201 com o ProspeccaoUsoResponse

Cenario 2: Tentativa de usar dado sem estoque
  Dado que tenho 0 dados d6 de prospeccao disponiveis
  Quando envio POST /api/v1/fichas/{id}/prospeccao/usar com { "dadoProspeccaoConfigId": 3 }
  Entao recebo HTTP 422 com mensagem "Sem estoque de dados d6 de prospecção"

Cenario 3: Jogador nao pode reverter o proprio uso
  Dado que tenho um ProspeccaoUso PENDENTE com id=101
  Quando envio PATCH /api/v1/fichas/{id}/prospeccao/usos/101/reverter
  Entao recebo HTTP 403

---

**US-009ext-04: Mestre reverte ou confirma uso de prospeccao**
Como Mestre,
Quero confirmar ou reverter usos de dados de prospeccao,
Para arbitrar situacoes especiais durante a sessao.

Criterios de Aceite:

Cenario 1: Mestre reverte uso PENDENTE
  Dado que existe ProspeccaoUso 101 com status=PENDENTE pertencente a ficha do meu jogo
  Quando envio PATCH /api/v1/fichas/{id}/prospeccao/usos/101/reverter
  Entao o ProspeccaoUso tem status=REVERTIDO
  E a quantidade do dado correspondente e incrementada em 1
  E recebo HTTP 200

Cenario 2: Mestre confirma uso PENDENTE
  Dado que existe ProspeccaoUso 101 com status=PENDENTE
  Quando envio PATCH /api/v1/fichas/{id}/prospeccao/usos/101/confirmar
  Entao o ProspeccaoUso tem status=CONFIRMADO
  E a quantidade do dado nao muda
  E recebo HTTP 200

Cenario 3: Tentativa de reverter uso ja CONFIRMADO
  Dado que o ProspeccaoUso 102 tem status=CONFIRMADO
  Quando envio PATCH /api/v1/fichas/{id}/prospeccao/usos/102/reverter
  Entao recebo HTTP 422 com mensagem "Nao e possivel reverter um uso ja confirmado"

---

**US-009ext-05: Mestre concede dados de prospeccao**
Como Mestre,
Quero conceder dados de prospeccao a uma ficha,
Para que o Jogador possa usar esses dados na sessao.

Criterios de Aceite:

Cenario 1: Concessao bem-sucedida
  Dado que sou MESTRE do jogo
  E a ficha existe no meu jogo
  Quando envio POST /api/v1/fichas/{id}/prospeccao/conceder com { "dadoProspeccaoConfigId": 3, "quantidade": 2 }
  Entao a quantidade do dado d6 e incrementada em 2
  E recebo HTTP 200 com o FichaResumoResponse atualizado

---

**US-009ext-06: Mestre reseta estado de combate da ficha**
Como Mestre,
Quero resetar o estado de combate de uma ficha ao estado base com um clique,
Para que eu possa preparar as fichas para a proxima sessao de jogo.

Criterios de Aceite:

Cenario 1: Reset bem-sucedido
  Dado que sou MESTRE do jogo
  E a ficha 10 tem vidaAtual=15 (vidaTotal=30), essenciaAtual=5 (total=20), dano em membros e prospeccao com 3 dados d6
  Quando envio POST /api/v1/fichas/10/resetar-estado
  Entao FichaVida.vidaAtual = FichaVida.vidaTotal (30)
  E FichaEssencia.essenciaAtual = FichaEssencia.total (20)
  E todos os FichaVidaMembro.danoRecebido = 0
  E a quantidade de dados de prospeccao nao e alterada (reset nao afeta prospeccao)
  E recebo HTTP 200 com FichaResumoResponse atualizado

Cenario 2: Jogador nao pode executar reset
  Dado que sou Jogador
  Quando envio POST /api/v1/fichas/10/resetar-estado
  Entao recebo HTTP 403

---

## 8. Dossie de Regras por Funcionalidade

### 8.1 Visibilidade de NPC

| Regra | Detalhe |
|-------|---------|
| Somente NPCs tem FichaVisibilidade | Tentar criar visibilidade para ficha de jogador retorna HTTP 422 |
| jogadorId em FichaVisibilidade e o userId | Nao e o fichaId do jogador — e o ID do usuario na tabela `usuarios` |
| visivelGlobalmente nao da acesso aos stats | Apenas aparece na listagem. Stats completos exigem FichaVisibilidade |
| Soft delete padrao | Revogar acesso usa `deletedAt`, nao DELETE fisico |
| Unico por (fichaId, jogadorId) ativo | Nao pode haver dois registros ativos para o mesmo par |
| MESTRE do jogo vê todos os NPCs | Independente de visibilidade — FichaVisibilidade e irrelevante para o Mestre |

### 8.2 Prospeccao

| Regra | Detalhe |
|-------|---------|
| Jogador usa, Mestre arbitra | O uso do Jogador cria ProspeccaoUso PENDENTE. Nao e confirmado automaticamente |
| Quantidade nao pode ser negativa | Validar no service antes de decrementar |
| Reverter apenas PENDENTE | Status CONFIRMADO e REVERTIDO nao podem ser revertidos novamente |
| Confirmar apenas PENDENTE | Status REVERTIDO nao pode ser confirmado |
| Mestre pode usar prospeccao de NPC | NPCs nao tem jogador dono; Mestre gerencia diretamente |
| PUT /fichas/{id}/prospeccao legado | O endpoint PUT existente continua funcionando para concessao direta. Os novos endpoints de semantica coexistem |

### 8.3 Reset de Estado

| Campo resetado | Valor apos reset | Origem do valor |
|---------------|-----------------|-----------------|
| FichaVida.vidaAtual | FichaVida.vidaTotal | Campo calculado existente |
| FichaEssencia.essenciaAtual | FichaEssencia.total | Campo calculado existente |
| FichaVidaMembro.danoRecebido | 0 | Todos os membros |
| FichaProspeccao.quantidade | Sem alteracao | Nao e resetado |

> Decisao do PO (2026-04-02): prospeccao NAO e resetada automaticamente. Dados sao recursos raros concedidos pelo Mestre — o reset de sessao so afeta vida e essencia.

---

## 9. Pontos em Aberto / Perguntas para Stakeholder

**P1 — ProspeccaoUso: historico ou apenas estado atual?**
A entidade ProspeccaoUso como descrita cria um registro por uso. Isso gera historico completo. O PO quer historico permanente ou apenas estado atual (coluna status no FichaProspeccao)?
> Premissa adotada: historico completo via ProspeccaoUso.

**P2 — Painel global de pendentes: escopo desta spec?**
O design em `PROSPECCAO-SESSAO.md` descreve `GET /jogos/{jogoId}/prospeccao/pendentes` e um `ProspeccaoPainelMestreComponent`. Este componente esta no escopo desta spec ou vai para a Spec 008 (dashboards)?
> Premissa adotada: endpoint backend incluso nesta spec; componente frontend e opcional (T9 frontend).

**P3 — visivelGlobalmente vs. acesso granular: os dois sao necessarios no MVP?**
O design descreve dois niveis (global + granular). O caso mais simples seria apenas granular. Confirmado que precisamos dos dois niveis?
> Premissa adotada: ambos os niveis, conforme design NPC-VISIBILITY.md.

**P4 — Reset inclui historico de ProspeccaoUso?**
O reset de estado deve marcar usos PENDENTES como CANCELADOS ou apenas reseta a quantidade?
> Premissa adotada: reset nao altera ProspeccaoUso. Usos pendentes continuam pendentes apos reset.

**PA-UX-01 — Modo Sessao: Polling ou conexao persistente (SSE/WebSocket)?** ✅ RESOLVIDO (2026-04-03)

**Contexto:** Quando o Mestre concede XP, reseta vida, altera visibilidade de NPC ou confirma uso de prospeccao durante uma sessao ativa, o Jogador precisa enxergar a mudanca. Tres opcoes avaliadas:
- Opcao A: Polling — frontend consulta o backend a cada N segundos automaticamente
- Opcao B: SSE/WebSocket — backend notifica o frontend em tempo real (implementacao mais complexa)
- Opcao C: Manual — Jogador recarrega a pagina para ver atualizacoes

**Decisao do PO (Q17 em `docs/gaps/PERGUNTAS-PENDENTES-PO.md`):** MVP usa **Opcao A — Polling**. SSE/WebSocket ficam reservados para versao futura.

**Impacto nas tasks:** O `PainelSessaoComponent` e o `FichaStatsCombateComponent` (Modo Sessao do Jogador) devem implementar polling periodico ao conectar. A frequencia recomendada e 30 segundos. Nenhuma task adicional de infraestrutura e necessaria — apenas logica de `setInterval` + chamada ao endpoint `/fichas/{id}/resumo` no componente Smart responsavel.

---

## 10. Checklist de Validacao UX

- [ ] Badge "Aliado" vs badge "NPC" na listagem — distinguir visualmente sem confundir o Jogador
- [ ] Painel de visibilidade no FichaDetailPage: sao 2 colunas no desktop. Em tablets (768px-1024px) o painel colapsa ou fica em tab separada?
- [ ] Confirmacao de reset: qual o texto do dialogo? E irreversivel mas nao e destrutivo de dados permanentes — tom do dialogo deve refletir isso
- [ ] Estado PENDENTE de prospeccao: Jogador ve "aguardando mestre" em amarelo — o que acontece se o Mestre fecha a sessao sem confirmar? Usos ficam pendentes para sempre?
- [ ] Acessibilidade do p-multiselect de visibilidade: em telas de alto contraste, o chip de jogador selecionado tem contraste suficiente?
