# Rodada 19 — 2026-04-16 (PM/Scrum Orchestrator — Sessao 22 continuada)

> Branch: `main`
> Sessao: 22 (continuacao)
> Backend: **+1 endpoint** (JogoResumoResponse ganha `criadoEm`) — 21 testes passando nos afetados
> Frontend: 7 entregas funcionais + integracoes de aba Sessao, badges de status e sidebar

---

## Resumo

Rodada de continuacao ao pacote de entregas da R18, focada em concluir o fluxo de `FichaStatus` no frontend (badges, bloqueios de edicao, remocao de botao Excluir), implementar a **aba Sessao** em `ficha-detail` (vida/essencia/membros com polling 30s), e finalizar Spec 026 T05/T08 (badge pendentes no sidebar do Mestre + coluna Data Criacao restaurada) + T-025-13 (remover "Criar Ficha" do sidebar do Jogador).

Durante a implementacao, foi identificado um **gap tecnico**: o `FichaResumoResponse` nao expoe `danoRecebido` dos membros do corpo. O PO decidiu criar um endpoint dedicado (`GET /fichas/{id}/estado-combate` ou similar) que sera implementado na R20.

Todas as 8 entregas foram commitadas nesta sessao.

---

## Entregas Frontend (FE)

### FE-1 — T-025-12 — Fix labels do wizard
- Passo 5 renomeado para "Vantagens"
- Passo 6 renomeado para "Revisao"
- Commit `27fd3b8`

### FE-2 — T-025-08 — Wizard le fichaId do path param
- Wizard agora aceita fichaId tanto via queryParam quanto via path param `:id`
- Desbloqueia continuacao de rascunhos via URL direta
- Commit `3d04f3b`

### FE-3 — T-025-01-FE + T-025-09 + T-025-10 — Badges de status + INCONS-02 no FE
- Badges de status (`RASCUNHO`, `ATIVA`, `MORTA`, `ABANDONADA`) na `fichas-list`
- `canEdit()` bloqueia edicao de fichas com status MORTA / ABANDONADA
- Remove botao "Excluir" do Jogador (alinha com decisao PO INCONS-02 — fichas de jogador nao sao deletadas)
- **+33 testes**
- Commit `9b5ba1b`

### FE-4 — T-025-03 + T-025-04 + T-025-07 + T-025-11 — Aba Sessao em ficha-detail
- Nova aba "Sessao" em `ficha-detail` com:
  - Barras de vida
  - Barra de essencia
  - Lista de membros do corpo (com dano visualizado)
  - **Polling 30s** (alinhado a decisao PO Q14 — modo sessao no MVP)
- Badge de status adicionado ao `FichaHeaderComponent`
- **+25 testes**
- Commit `65f0c19`

### FE-5 — S026-T08 FE — Coluna Data Criacao na lista de jogos
- Coluna "Data Criacao" restaurada em `JogosListComponent`
- Consumindo o campo `criadoEm` do `JogoResumoResponse` (ver BE-1)
- Commit `ea0bb09`

### FE-6 — S026-T05 — Badge de participantes pendentes no sidebar do Mestre
- Sidebar do Mestre passa a exibir badge com numero de participantes pendentes de aprovacao
- Facilita fluxo operacional do Mestre ao logar
- Commit `aaa1abc`

### FE-7 — T-025-13 — Remove "Criar Ficha" do sidebar do Jogador
- Entrada "Criar Ficha" removida do menu lateral do Jogador
- Criacao continua disponivel via fluxo contextual (dentro de um jogo aprovado)
- Commit `aaa1abc` (mesmo commit de S026-T05)

---

## Entregas Backend (BE)

### BE-1 — S026-T08 BE — `criadoEm` no JogoResumoResponse
- Campo `criadoEm` adicionado ao DTO `JogoResumoResponse`
- `JogoMapper` atualizado para propagar o valor
- **21 testes passando** nos arquivos afetados
- Commit `2fc8ece`

---

## Gap Tecnico Identificado — Proxima Rodada (R20)

Durante a implementacao de **FE-4 (aba Sessao)**, foi identificado que o `FichaResumoResponse` atual **nao expoe `danoRecebido`** para os membros do corpo. Isso faz com que, ao abrir a aba Sessao, o frontend exiba `dano=0` mesmo que exista estado persistido no backend.

**Decisao do PO:** em vez de sobrecarregar o `FichaResumoResponse`, criar um endpoint dedicado `GET /fichas/{id}/estado-combate` (ou nome similar) que retorne o estado de combate da ficha (vida atual, essencia atual, dano por membro).

**Acao:** abrir tarefa **S027-T01 BE** (ou equivalente) na R20 para especificacao + implementacao.

---

## Backlog Sprint 4 — Estado Apos R19

### Tasks Fechadas na Rodada

| ID | Descricao | Tipo |
|----|-----------|------|
| T-025-01-FE | Badges de status na fichas-list (`RASCUNHO`/`ATIVA`/`MORTA`/`ABANDONADA`) | FE |
| T-025-03 | Aba Sessao — vida + essencia | FE |
| T-025-04 | Aba Sessao — membros do corpo | FE |
| T-025-07 | Polling 30s (modo sessao) | FE |
| T-025-08 | Wizard le fichaId do path param `:id` | FE |
| T-025-09 | `canEdit()` bloqueia MORTA/ABANDONADA | FE |
| T-025-10 | Remove botao Excluir do Jogador (INCONS-02) | FE |
| T-025-11 | Badge de status no `FichaHeaderComponent` | FE |
| T-025-12 | Labels wizard: "Vantagens" + "Revisao" | FE |
| T-025-13 | Remove item "Criar Ficha" do sidebar do Jogador | FE |
| S026-T05 | Badge pendentes no sidebar do Mestre | FE |
| S026-T08 | Coluna Data Criacao (FE) + `criadoEm` (BE) | FE + BE |

**12 tasks fechadas (11 FE + 1 BE).** Sprint 4 permanece **ENCERRADO na pratica**; R19 entrega os residuais de `FichaStatus`/modo-sessao e fecha os gaps recem-descobertos de listagem de jogos e sidebar.

### P2 — Estado apos R19

| ID | Tipo | Descricao | Status |
|----|------|-----------|--------|
| AUDIT-BE-FE | Auditoria | Auditar demais endpoints sem tela | [PENDENTE] (baixa prioridade) |
| **NOVO** | BE | `GET /fichas/{id}/estado-combate` — expor `danoRecebido` por membro (gap R19) | [PENDENTE] (R20) |

---

## Atualizacoes de Tracking

| Documento | Mudancas |
|-----------|----------|
| `HANDOFF-SESSAO.md` | Header rev sessao 22 / rodada 19; entregas R19 adicionadas; proxima acao = endpoint `estado-combate` |
| `MASTER.md` | rev.23; Rodada 19 registrada; tasks T-025-01/03/04/07/08/09/10/11/12/13 + S026-T05/T08 CONCLUIDAS |
| `PM.md` | rev.23; entrada de R19 na tabela; completude ajustada; gap tecnico `estado-combate` registrado |
| `docs/tracking/rodadas/RODADA-19.md` | Este arquivo (novo) |

---

## Proxima Rodada (R20) — Foco Recomendado

1. **PRIORIDADE:** novo endpoint `GET /fichas/{id}/estado-combate` (ou `GET /fichas/{id}/combate`) — expor `danoRecebido` por membro + `vidaAtual` + `essenciaAtual`. FE atualiza aba Sessao para consumir esse endpoint em vez do `FichaResumoResponse`.
2. **Auditoria de testes backend** — reconciliar contagem global apos BE-1 R19 (+21 afetados) e deltas R18 (FichaStatus, PUT /status, DELETE NPC).
3. **Reconciliar contagem total de testes FE** apos R18 + R19 (muitos commits acumulados).
4. **AUDIT-BE-FE (P2)** — auditar endpoints backend sem tela correspondente; decidir se entram no pos-MVP.
5. **Fechamento v0.0.1-RC** — avaliar se, apos R20, o backlog funcional esta pronto para tag de versao.

---

## Observacoes

- A aba Sessao (FE-4) ja foi entregue **estruturalmente completa**: a unica limitacao atual e que `danoRecebido` inicia em 0 ate o novo endpoint estar disponivel. Isso foi validado com o PO — aceito como estado transitorio ate R20.
- R19 consolida o fluxo `FichaStatus` no frontend (badges + canEdit + remocao do Excluir do Jogador) — alinhamento completo com a decisao PO INCONS-02 agora end-to-end (BE entregue R18, FE entregue R19).
- S026-T05 (badge pendentes) e S026-T08 (Data Criacao) sao pequenos ajustes de visibilidade do Mestre que facilitam o uso diario da aplicacao.
- T-025-13 (remover "Criar Ficha" do sidebar) ficou escondido como ajuste de UX — criacao por contexto (dentro de jogo aprovado) e o fluxo oficial.

---

*Rodada 19 — PM/Scrum Orchestrator — 2026-04-16*
