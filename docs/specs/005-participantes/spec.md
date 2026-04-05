# Spec 005 — Gestao de Participantes

> Spec: `005-gestao-participantes`
> Epic: EPIC 3
> Status: Desbloqueado — decisoes do PO confirmadas em 2026-04-02
> Depende de: Spec 001 (Jogo, JogoParticipante entity existente)
> Bloqueia: Spec 006 (Ficha do zero — jogador precisa ser participante aprovado)

---

## 1. Visao Geral do Negocio

**Problema resolvido:** Atualmente qualquer usuario autenticado pode acessar qualquer jogo. Nao existe controle de quem pertence a qual campanha, nem fluxo de aprovacao. O Mestre nao tem mecanismo para aceitar, rejeitar ou remover jogadores.

**Objetivo:** Implementar o ciclo de vida completo de participacao em um Jogo — desde a solicitacao do Jogador ate a aprovacao, rejeicao, remocao e banimento pelo Mestre — com uma maquina de estados clara e todos os endpoints necessarios.

**Valor entregue:**
- Mestre tem controle total sobre quem participa do seu jogo
- Jogador tem visibilidade do status da propria participacao
- A Spec 006 (Ficha) pode usar participacao aprovada como pre-condicao de acesso

---

## 2. Atores Envolvidos

| Ator | Role | Acoes permitidas |
|------|------|-----------------|
| Jogador | JOGADOR | Solicitar entrada, cancelar propria solicitacao pendente, ver proprio status |
| Mestre | MESTRE | Aprovar, rejeitar, remover, banir, desbanir, listar todos os participantes |
| Sistema | — | Calcular status atual, impedir acesso a recursos quando nao aprovado |

---

## 3. State Machine — JogoParticipante

```
                     Jogador solicita
                          |
                          v
                      [PENDENTE]
                     /         \
          Mestre aprova    Mestre rejeita
                /                 \
          [APROVADO]           [REJEITADO]
           /    \                    |
  Mestre remove  Mestre bane    Jogador re-solicita
       |              |              |
   [REMOVIDO]      [BANIDO]       [PENDENTE]
   (soft delete)      |
                 Mestre desbane
                       |
                   [APROVADO]
```

### Regras de transicao

| De | Para | Ator | Endpoint | Restricao |
|----|------|------|----------|-----------|
| (novo) | PENDENTE | JOGADOR | POST /solicitar | Jogador deve estar autenticado; jogo deve existir e nao estar deletado; jogador nao pode ter participacao APROVADA ou BANIDA ativa |
| PENDENTE | APROVADO | MESTRE | PUT /{pid}/aprovar | Mestre deve ser dono do jogo |
| PENDENTE | REJEITADO | MESTRE | PUT /{pid}/rejeitar | Mestre deve ser dono do jogo |
| APROVADO | REMOVIDO | MESTRE | DELETE /{pid} | Nao e banimento definitivo — reversivel re-solicitando; soft delete logico |
| APROVADO | BANIDO | MESTRE | PUT /{pid}/banir | Impede re-solicitacao enquanto banido |
| BANIDO | APROVADO | MESTRE | PUT /{pid}/desbanir | Reintegra o jogador sem necessidade de nova solicitacao |
| REJEITADO | PENDENTE | JOGADOR | POST /solicitar | Sem cooldown — jogador pode re-solicitar imediatamente |
| REMOVIDO | PENDENTE | JOGADOR | POST /solicitar | Remocao e provisoria; jogador pode re-solicitar |

### Regras de estado que NAO existem (explicitamente fora do escopo)

- BANIDO nao pode solicitar novamente enquanto BANIDO — o sistema bloqueia a solicitacao
- Jogador APROVADO nao pode se auto-remover (apenas o Mestre remove)
- Jogador nao pode se autobanir
- Nao ha cooldown entre REJEITADO → PENDENTE (decisao do PO: sem cooldown)
- DELETE /participantes/{pid} == remocao provisoria, nao banimento

---

## 4. Requisitos Funcionais

**RF-001** O Jogador pode solicitar entrada em um Jogo. O status inicial e PENDENTE.

**RF-002** Se o Jogador ja possui participacao BANIDA no jogo, a solicitacao deve ser rejeitada com HTTP 409.

**RF-003** Se o Jogador ja possui participacao APROVADA no jogo, a solicitacao deve ser rejeitada com HTTP 409.

**RF-004** O Mestre pode listar todos os participantes do seu jogo, com status e dados do usuario.

**RF-005** O Mestre pode aprovar uma solicitacao PENDENTE — transicao para APROVADO.

**RF-006** O Mestre pode rejeitar uma solicitacao PENDENTE — transicao para REJEITADO.

**RF-007** O Mestre pode remover um participante APROVADO — transicao logica para REMOVIDO (soft delete no registro de participacao). O jogador pode re-solicitar.

**RF-008** O Mestre pode banir um participante APROVADO — transicao para BANIDO. O jogador NAO pode re-solicitar enquanto banido.

**RF-009** O Mestre pode desbanir um participante BANIDO — transicao direta para APROVADO sem nova solicitacao.

**RF-010** O Jogador pode consultar o proprio status de participacao em um jogo especifico.

**RF-011** O Jogador pode cancelar uma solicitacao propria que esteja PENDENTE (auto-remove a solicitacao).

**RF-012** Um Jogador nao aprovado em um jogo nao pode acessar nenhum recurso daquele jogo (fichas, configs, NPCs). O SecurityService deve validar participacao aprovada antes de liberar acesso.

**RF-013** O Mestre tem acesso irrestrito ao proprio jogo, independente de ter registro de participacao como JOGADOR.

**RF-014** A listagem de participantes deve suportar filtro por status (PENDENTE, APROVADO, REJEITADO, BANIDO).

---

## 5. Requisitos Nao Funcionais

- **Seguranca:** Somente o Mestre dono do jogo pode aprovar/rejeitar/banir/desbanir. Outros Mestres nao tem acesso.
- **Consistencia:** O status deve sempre refletir a transicao mais recente. Historico de transicoes nao e requisito MVP (decisao do PO: Envers descartado).
- **Visibilidade:** Jogador ve apenas a propria participacao; Mestre ve todas.
- **Soft delete:** Remocao (DELETE) usa soft delete no registro — o registro permanece no banco com `deleted_at` preenchido, mas reaparece com status diferente ao re-solicitar (ou e criado novo registro).

---

## 6. Endpoints

### Contrato de API

| Metodo | Path | Roles | Descricao |
|--------|------|-------|-----------|
| POST | `/api/v1/jogos/{jogoId}/participantes/solicitar` | JOGADOR | Solicitar entrada no jogo |
| GET | `/api/v1/jogos/{jogoId}/participantes` | MESTRE | Listar todos os participantes (com filtro por status) |
| GET | `/api/v1/jogos/{jogoId}/participantes/meu-status` | JOGADOR | Ver proprio status de participacao |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{pid}/aprovar` | MESTRE | Aprovar solicitacao PENDENTE |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{pid}/rejeitar` | MESTRE | Rejeitar solicitacao PENDENTE |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{pid}/banir` | MESTRE | Banir participante APROVADO |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{pid}/desbanir` | MESTRE | Desbanir participante BANIDO |
| DELETE | `/api/v1/jogos/{jogoId}/participantes/{pid}` | MESTRE | Remover participante APROVADO (provisorio) |
| DELETE | `/api/v1/jogos/{jogoId}/participantes/minha-solicitacao` | JOGADOR | Cancelar propria solicitacao PENDENTE |

### ParticipanteResponse

```json
{
  "id": 42,
  "jogoId": 5,
  "usuarioId": 18,
  "usuarioNome": "Carlos Demétrio",
  "usuarioEmail": "carlos@example.com",
  "status": "PENDENTE",
  "dataSolicitacao": "2026-04-01T10:30:00",
  "dataAtualizacao": "2026-04-01T10:30:00"
}
```

### Enum JogoParticipanteStatus

```
PENDENTE, APROVADO, REJEITADO, BANIDO
```

> Nota: REMOVIDO nao e um status persistido — e representado por soft delete (deleted_at) no registro. Ao re-solicitar, um novo registro e criado com status PENDENTE.

---

## 7. Epico e User Stories

### Epic 3 — Gestao de Participantes

---

**US-005-01: Solicitar entrada em jogo**
Como Jogador autenticado,
Quero solicitar entrada em um Jogo pelo seu ID,
Para que o Mestre possa me aprovar como participante.

Criterios de Aceite:

Cenario 1: Solicitacao bem-sucedida
  Dado que sou um Jogador autenticado
  E o Jogo existe e nao foi deletado
  E nao tenho participacao APROVADA ou BANIDA neste jogo
  Quando envio POST /jogos/{jogoId}/participantes/solicitar
  Entao um registro JogoParticipante e criado com status PENDENTE
  E recebo HTTP 201 com o ParticipanteResponse

Cenario 2: Tentativa com participacao ja ativa
  Dado que sou um Jogador com participacao APROVADA no jogo
  Quando envio POST /jogos/{jogoId}/participantes/solicitar
  Entao recebo HTTP 409 com mensagem "Voce ja e participante aprovado deste jogo"

Cenario 3: Tentativa com banimento ativo
  Dado que sou um Jogador BANIDO do jogo
  Quando envio POST /jogos/{jogoId}/participantes/solicitar
  Entao recebo HTTP 409 com mensagem "Voce foi banido deste jogo pelo Mestre"

Cenario 4: Re-solicitacao apos rejeicao (sem cooldown)
  Dado que minha participacao anterior tem status REJEITADO
  Quando envio POST /jogos/{jogoId}/participantes/solicitar
  Entao um novo registro JogoParticipante e criado com status PENDENTE
  E recebo HTTP 201

---

**US-005-02: Aprovar solicitacao**
Como Mestre dono do jogo,
Quero aprovar solicitacoes pendentes de Jogadores,
Para liberar o acesso deles as fichas e recursos do jogo.

Criterios de Aceite:

Cenario 1: Aprovacao bem-sucedida
  Dado que sou o Mestre dono do jogo
  E existe um participante com status PENDENTE e id {pid}
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/aprovar
  Entao o status muda para APROVADO
  E recebo HTTP 200 com o ParticipanteResponse atualizado

Cenario 2: Tentativa de aprovar participante nao pendente
  Dado que o participante tem status APROVADO (ja aprovado)
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/aprovar
  Entao recebo HTTP 422 com mensagem descrevendo a transicao invalida

Cenario 3: Tentativa de outro Mestre
  Dado que nao sou o Mestre dono deste jogo
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/aprovar
  Entao recebo HTTP 403

---

**US-005-03: Rejeitar solicitacao**
Como Mestre dono do jogo,
Quero rejeitar solicitacoes que nao quero aprovar,
Para manter controle sobre quem entra na campanha.

Criterios de Aceite:

Cenario 1: Rejeicao bem-sucedida
  Dado que existe um participante PENDENTE
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/rejeitar
  Entao o status muda para REJEITADO
  E recebo HTTP 200

---

**US-005-04: Banir participante**
Como Mestre dono do jogo,
Quero banir um participante aprovado,
Para impedir que ele re-solicite e acesse o jogo.

Criterios de Aceite:

Cenario 1: Banimento bem-sucedido
  Dado que o participante tem status APROVADO
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/banir
  Entao o status muda para BANIDO
  E o Jogador nao pode mais acessar recursos do jogo
  E o Jogador nao pode re-solicitar entrada

Cenario 2: Tentativa de banir jogador nao aprovado
  Dado que o participante tem status PENDENTE
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/banir
  Entao recebo HTTP 422 com mensagem de transicao invalida

---

**US-005-05: Desbanir participante**
Como Mestre dono do jogo,
Quero desbanir um jogador banido,
Para reintegra-lo ao jogo sem exigir nova solicitacao.

Criterios de Aceite:

Cenario 1: Desbanimento bem-sucedido
  Dado que o participante tem status BANIDO
  Quando envio PUT /jogos/{jogoId}/participantes/{pid}/desbanir
  Entao o status muda diretamente para APROVADO
  E o Jogador recupera acesso ao jogo imediatamente

---

**US-005-06: Remover participante (provisorio)**
Como Mestre dono do jogo,
Quero remover um jogador aprovado sem bani-lo permanentemente,
Para que ele possa re-solicitar entrada futuramente se quiser.

Criterios de Aceite:

Cenario 1: Remocao bem-sucedida
  Dado que o participante tem status APROVADO
  Quando envio DELETE /jogos/{jogoId}/participantes/{pid}
  Entao o registro e marcado com soft delete (deleted_at preenchido)
  E o Jogador perde acesso imediato ao jogo
  E o Jogador pode re-solicitar entrada (novo registro PENDENTE)
  E recebo HTTP 204

---

**US-005-07: Ver proprio status**
Como Jogador autenticado,
Quero consultar meu status de participacao em um jogo,
Para saber se fui aprovado, rejeitado ou estou pendente.

Criterios de Aceite:

Cenario 1: Status encontrado
  Quando envio GET /jogos/{jogoId}/participantes/meu-status
  Entao recebo HTTP 200 com meu ParticipanteResponse

Cenario 2: Sem participacao registrada
  Quando envio GET /jogos/{jogoId}/participantes/meu-status
  E nunca solicitei entrada neste jogo
  Entao recebo HTTP 404

---

**US-005-08: Listar participantes**
Como Mestre dono do jogo,
Quero listar todos os participantes com filtro por status,
Para gerenciar solicitacoes pendentes e ver quem esta ativo.

Criterios de Aceite:

Cenario 1: Listagem sem filtro
  Quando envio GET /jogos/{jogoId}/participantes
  Entao recebo a lista de todos os participantes nao deletados

Cenario 2: Listagem com filtro por status
  Quando envio GET /jogos/{jogoId}/participantes?status=PENDENTE
  Entao recebo apenas os participantes com status PENDENTE

---

## 8. Regras de Negocio Criticas do Dominio

**RN-001 — Dono do jogo e sempre Mestre:** O usuario que criou o Jogo tem role MESTRE naquele jogo. O SecurityService deve verificar `jogo.mestreId == usuarioLogado.id` nas operacoes de gestao.

**RN-002 — Acesso baseado em participacao:** Jogadores so acessam recursos de jogos onde sao APROVADOS. Fichas, configuracoes e NPCs devem validar participacao antes de responder.

**RN-003 — BANIDO bloqueia solicitacao:** Diferente de REJEITADO e REMOVIDO, o status BANIDO e uma restricao ativa. O endpoint POST /solicitar deve checar ativamente por registros BANIDOS (incluindo os soft-deleted).

**RN-004 — Multiplicidade de participacoes:** Um usuario pode ser JOGADOR em varios jogos simultaneamente (diferentes registros de JogoParticipante). Nao ha restricao de numero de jogos.

**RN-005 — Mestre nao precisa ser participante:** O Mestre acessa seu proprio jogo por ser o criador, sem necessitar de registro em JogoParticipante.

**RN-006 — Envers descartado:** Nao ha rastreamento de historico de transicoes de status no MVP (decisao explicitada pelo PO em 2026-04-02).

---

## 9. Pontos em Aberto

| ID | Questao | Impacto |
|----|---------|---------|
| PA-001 | O que acontece com as fichas de um jogador BANIDO? Elas ficam visiveis para o Mestre? Sao ocultadas? | Afeta o FichaController e as queries de listagem |
| PA-002 | O Mestre pode transferir a propriedade do jogo para outro usuario? | Nao mencionado — assumimos que nao e MVP |
| PA-003 | Ha limite de participantes por jogo? | Nao mencionado — assumimos sem limite |
| PA-004 | Notificacao ao Jogador quando aprovado/rejeitado? (email, notificacao in-app) | Nao e requisito MVP, mas influencia UX |

---

## 10. Checklist de Validacao UX

- [ ] Fluxo do Jogador ao solicitar: feedback imediato de "aguardando aprovacao"
- [ ] Tela do Mestre: badge com contagem de solicitacoes PENDENTES (quick action)
- [ ] Confirmacao antes de banir (acao mais severa — irreversivel sem acao do Mestre)
- [ ] Diferenciacao visual clara entre REMOVIDO (provisorio) e BANIDO (restritivo) na listagem
- [ ] Estado vazio: jogo sem participantes ainda exibe instrucoes para compartilhar o jogo
- [ ] Mensagem de erro clara ao Jogador BANIDO tentando re-solicitar

---

## 11. Dependencias

- **Depende de:** Spec 001 (entidade JogoParticipante existe no model, sem status nem CRUD exposto)
- **Bloqueia:** Spec 006 (criacao de ficha requer participacao APROVADA)
- **Afeta:** Spec 010 (roles ADMIN/MESTRE/JOGADOR — ver Spec 010 para refatoracao de roles)

---

*Produzido por: Business Analyst/PO | 2026-04-02 | Decisoes do PO incorporadas*
