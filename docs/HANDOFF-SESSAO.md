# Handoff de Sessao -- 2026-04-05 (pos-sessao 12, rodada 6 concluida)

> Documento de transferencia de contexto para a proxima sessao de trabalho.
> Branch atual: `main`
> Backend: **571 testes** passando, 0 falhas | Frontend: **359 testes** passando, 0 falhas
> Sprint 2: **19/35 tasks concluidas** (54%)
> Ultima atualizacao: 2026-04-05 [14:01]

---

## Resumo Executivo

A Rodada 6 concluiu 4 tasks com merge limpo em `main`, adicionando +48 testes ao backend (523 -> 571). Os destaques: testes de integracao cobrindo 7 tipos de VantagemEfeito (S007-T8), correcao completa da re-solicitacao de participantes com strategy Reactivate (S005-P1T1), 4 novos endpoints de participantes + correcao critica do DELETE que era banimento (S005-P1T2), e endpoint XP refatorado para ser acumulativo com motivo opcional e retornando FichaResumoResponse (S006-T4).

**Proximo foco (Rodada 7):** S015-T4 (auto-concessao vantagens pre-definidas), S005-P1T3 (testes integracao participantes), S007-T9..T12 (frontend efeitos), S006-T6..T13 (wizard frontend).

---

## O que Foi Feito (acumulado Sprint 2: 19/35)

### Backend -- 571 testes (+48 desde Rodada 5)

| ID | Descricao | Rodada | Horario | Detalhes |
|----|-----------|--------|---------|----------|
| S007-T8 | Testes integracao 7 tipos VantagemEfeito | R6 | [13:20] | Criado `FichaEfeitosCalculationIntegrationTest.java` com 20 testes. Cobre: BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA, DADO_UP. Cenarios de borda: idempotencia (3x), soft-delete ignorado, lista vazia. FORMULA_CUSTOMIZADA pulada (bloqueada PA-004). Descoberta: necessario `entityManager.flush()+clear()` antes de recalcular para bypass do Hibernate L1 cache. Commit `e1bbe50`. |
| S005-P1T1 | Corrigir re-solicitacao (strategy Reactivate) | R6 | [11:14] | `findByJogoIdAndUsuarioIdIncluindoRemovidos` com nativeQuery (bypass @SQLRestriction). `solicitar()` reescrito: REJEITADO/REMOVIDO reutilizam mesmo registro (ID inalterado). Usado `p.restore()` (BaseEntity) para limpar deletedAt. 6 testes. Commit `32b984f`. |
| S005-P1T2 | Endpoints faltantes participantes | R6 | [11:19] | 4 novos endpoints: banir, desbanir, remover, meu-status + filtro. Correcao critica: DELETE /{pid} era banimento, agora e soft delete. `banir()` corrigido para aceitar apenas APROVADO (assertStatus). 12 testes. Commit `7a71a55`. |
| S006-T4 | Endpoint PUT /fichas/{id}/xp acumulativo | R6 | [11:19] | `ConcederXpRequest`: adicionado `motivo` (opcional, max 500 chars), `@Min` mudado de 0 para 1. XP agora acumulativo (`getXp() + request.xp()`), guard nivel nao desce (`Math.max`). Response mudado para `FichaResumoResponse`. 14 testes (2 ajustados + 12 novos). Commit `d37b227`. |
| S007-T7 | Insolitus + endpoints concessao/revogacao | R5 | -- | Enum TipoVantagem (VANTAGEM/INSOLITUS), campo concedidoPeloMestre em FichaVantagem. POST insolitus, DELETE revogacao. 6 testes. Commit `bd75582`. |
| S006-T2 | Validacao RacaClassePermitida na criacao | R5 | -- | Validacao adicionada no criar() de FichaService. 3 testes. Commit `1cb523a`. |
| S006-T5 | pontosDisponiveis no FichaResumoResponse | R5 | -- | 3 novos campos no response, calculo NivelConfig-based. 3 testes. Commit `61b0bb4`. |
| S015-T3 | Integrar ConfigPontos no FichaResumo | R5 | -- | ClassePontosConfig+RacaPontosConfig somados ao total. 2 testes. Commit `5dc8bf2`. |
| S007-T3+T4+T5 | BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP | R4 | -- | FichaCalculationService: BONUS_DERIVADO via bonusMap, BONUS_VIDA_MEMBRO via membrosMap, calcularDadoUp() para DADO_UP. Commit `0621bc8`. |
| S015-T2 | 14 endpoints CRUD sub-recursos | R4 | -- | 4 services + 14 endpoints + 2 testes integracao. Commit `ba52d29`. |
| S006-T1 | FichaStatus + endpoint /completar | R4 | -- | Enum FichaStatus, campo status, endpoint PUT /fichas/{id}/completar, 9 testes. Commit `d55e312`. |
| S007-T2 | BONUS_ATRIBUTO + BONUS_APTIDAO | R3 | -- | Tambem implementou BONUS_VIDA e BONUS_ESSENCIA (escopo expandido). Commit `52738da`. |
| S015-T1 | 4 entidades ConfigPontos | R3 | -- | ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig, RacaVantagemPreDefinida + repos + DTOs + mappers. Commit `9ac2465`. |
| S007-T0 | Corrigir 6 bugs motor calculos | R1 | -- | GAP-CALC-01/02/03/06/07/08. 7 testes novos. |
| S007-T1 | Adaptar modelo dados para efeitos | R2 | -- | SCHEMA-01/02, FichaProspeccao.dadoDisponivel, findByFichaIdWithEfeitos, stub aplicarEfeitosVantagens. |
| S015-T5 | DefaultProvider: 8 bugs + defaults | R2 | -- | BUG-DC-02..09 (exceto DC-03). 9 BonusConfig, 8 PontosVantagem, 8 CategoriaVantagem, 22 vantagens canonicas. |
| URG-01 | Bug XP verificado | R2 | -- | PUT /fichas/{id}/xp ja tinha @PreAuthorize. |

### Frontend -- 359/359 testes (+88 desde Sprint 1)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| QW-Bug1/2 | Barras vida/essencia + pontos vantagem | R3 | Ja estavam corrigidos (da rodada 2). |
| URG-02 | Fix 38 testes falhando | R2 | 359/359 testes passando. |
| QW-Bug3 | Rota NPC errada | R2 | verFicha() corrigido. |

---

## O que Esta Pendente (16 tasks restantes)

### Caminho Critico: Spec 007 (VantagemEfeito + Motor)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S007-T2 | Backend | BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA | T1 | **CONCLUIDA** (R3) |
| S007-T3+T4+T5 | Backend | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | T2 | **CONCLUIDA** (R4) |
| S007-T5alt | Backend | FORMULA_CUSTOMIZADA | T1, PA-004 | **BLOQUEADO** (PA-004 nao resolvido) |
| S007-T7 | Backend | Insolitus + endpoint concessao/revogacao | T1 | **CONCLUIDA** (R5) |
| S007-T8 | Backend | Testes integracao todos efeitos | T3-T7 | **CONCLUIDA** (R6) |
| S007-T9..T12 | Frontend | UI efeitos (4 tasks) | T8 | **DESBLOQUEADO** |

**NOTA:** 7 de 8 TipoEfeito implementados no motor. Falta apenas FORMULA_CUSTOMIZADA (bloqueado por PA-004). Insolitus completo com concessao + revogacao. Testes de integracao (T8) CONCLUIDOS com 20 testes cobrindo 7 tipos.

### Track B: Spec 015 (ConfigPontos)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S015-T1 | Backend | 4 entidades, repos, DTOs, mappers | Nenhuma | **CONCLUIDA** (R3) |
| S015-T2 | Backend | CRUD endpoints sub-recursos | S015-T1 | **CONCLUIDA** (R4) |
| S015-T3 | Backend | Integrar pontos no FichaResumoResponse | S015-T1, S007-T1 | **CONCLUIDA** (R5) |
| S015-T4 | Backend | Auto-concessao vantagens pre-definidas | S015-T1, S007-T7 | **DESBLOQUEADO** |

### Track C: Spec 006 (Wizard Ficha)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S006-T1 | Backend | Campo status + /completar | Nenhuma | **CONCLUIDA** (R4) |
| S006-T2 | Backend | Validacao RacaClassePermitida | Nenhuma | **CONCLUIDA** (R5) |
| S006-T3 | Backend | (ABSORVIDA por URG-01) | -- | -- |
| S006-T4 | Backend | PUT /fichas/{id}/xp acumulativo + motivo | Nenhuma | **CONCLUIDA** (R6) |
| S006-T5 | Backend | pontosDisponiveis no response | Nenhuma | **CONCLUIDA** (R5) |
| S006-T6..T13 | Frontend | Wizard 6 passos + auto-save + badge | S006-T1, T5, Spec 007 | **DESBLOQUEADO** |

### Track D: Spec 005 (Participantes)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S005-P1T1 | Backend | Re-solicitacao strategy Reactivate | Nenhuma | **CONCLUIDA** (R6) |
| S005-P1T2 | Backend | Endpoints faltantes (banir, desbanir, remover, meu-status, filtro) | S005-P1T1 | **CONCLUIDA** (R6) |
| S005-P1T3 | Backend | Testes de integracao completos | S005-P1T1, P1T2 | **DESBLOQUEADO** |
| S005-P2T1..P2T3 | Frontend | Participantes frontend (3 tasks) | S005-P1T2 | **DESBLOQUEADO** |

### Quick Wins Frontend -- 0 bugs restantes (todos corrigidos)

---

## Rodada 6 -- CONCLUIDA [14:01] (4 commits, +48 testes, merge limpo, 571 testes)

### Resultado da Rodada 6

| Agente | Task | Horario | Resultado |
|--------|------|---------|-----------|
| Agente A (S005-P1T1) | Re-solicitacao strategy Reactivate | [11:14] | **CONCLUIDO** -- nativeQuery bypass @SQLRestriction, strategy Reactivate, 6 testes. Commit `32b984f`. |
| Agente B (S005-P1T2) | Endpoints faltantes participantes | [11:19] | **CONCLUIDO** -- 4 novos endpoints, correcao critica DELETE, 12 testes. Commit `7a71a55`. |
| Agente C (S006-T4) | XP acumulativo + motivo + FichaResumoResponse | [11:19] | **CONCLUIDO** -- XP acumulativo, motivo opcional, guard nivel, response FichaResumo, 14 testes. Commit `d37b227`. |
| Agente D (S007-T8) | Testes integracao 7 tipos VantagemEfeito | [13:20] | **CONCLUIDO** -- 20 testes, 7 tipos cobertos, cenarios de borda, entityManager flush/clear pattern. Commit `e1bbe50`. |

**Merge:** Cherry-pick/merge em `main` [14:01]. 571 testes passando, 0 falhas.

---

## Proxima Rodada (Rodada 7) -- Planejamento

### Agente 1 -- Backend: S015-T4 (Auto-concessao vantagens pre-definidas)

**Task:** Ao criar ficha (ou atribuir classe/raca), conceder automaticamente vantagens pre-definidas via ClasseVantagemPreDefinida/RacaVantagemPreDefinida
**Estimativa:** 3-4 horas
**Dependencia:** S015-T1 (CONCLUIDA R3), S007-T7 (CONCLUIDA R5) -- DESBLOQUEADA

### Agente 2 -- Backend: S005-P1T3 (Testes integracao participantes)

**Task:** Testes de integracao cobrindo todos os endpoints e fluxos de participantes (solicitar, aprovar, rejeitar, re-solicitar, banir, desbanir, remover, meu-status, filtro)
**Estimativa:** 3-4 horas
**Dependencia:** S005-P1T1 (CONCLUIDA R6), S005-P1T2 (CONCLUIDA R6) -- DESBLOQUEADA

### Agente 3 -- Frontend: S007-T9 (VantagensConfigComponent -- secao de efeitos)

**Task:** UI para exibir e configurar efeitos de vantagem no frontend
**Estimativa:** 4-6 horas
**Dependencia:** S007-T8 (CONCLUIDA R6) -- DESBLOQUEADA

### Agente 4 -- Frontend: S006-T6 (Wizard Passo 1 -- Identificacao)

**Task:** Rewrite do wizard de criacao de ficha, passo 1 (nome, raca, classe, genero, indole, presenca)
**Estimativa:** 4-6 horas
**Dependencia:** S006-T1 (CONCLUIDA R4), S006-T5 (CONCLUIDA R5) -- DESBLOQUEADA

---

## Sequencia Completa

```
RODADA 3 -- CONCLUIDA:
  [CONCLUIDO] S007-T2: BONUS_ATRIBUTO+APTIDAO+VIDA+ESSENCIA  commit 52738da
  [CONCLUIDO] S015-T1: 4 entidades ConfigPontos ............. commit 9ac2465
  [CONCLUIDO] QW-Bug1/2: frontend ja corrigido .............. verificado R3

RODADA 4 -- CONCLUIDA:
  [CONCLUIDO] S007-T3+T4+T5: DERIVADO+VIDA_MEMBRO+DADO_UP .. commit 0621bc8
  [CONCLUIDO] S006-T1: FichaStatus + /completar ............. commit d55e312
  [CONCLUIDO] S015-T2: 14 CRUD endpoints sub-recursos ....... commit ba52d29

RODADA 5 -- CONCLUIDA:
  [CONCLUIDO] S007-T7: Insolitus + concessao/revogacao ...... commit bd75582
  [CONCLUIDO] S006-T2: validacao RacaClassePermitida ........ commit 1cb523a
  [CONCLUIDO] S006-T5: pontosDisponiveis no response ........ commit 61b0bb4
  [CONCLUIDO] S015-T3: ConfigPontos no FichaResumo .......... commit 5dc8bf2

RODADA 6 -- CONCLUIDA [14:01]:
  [CONCLUIDO] S005-P1T1: re-solicitacao Reactivate .......... commit 32b984f [11:14]
  [CONCLUIDO] S005-P1T2: endpoints faltantes participantes .. commit 7a71a55 [11:19]
  [CONCLUIDO] S006-T4: XP acumulativo + motivo .............. commit d37b227 [11:19]
  [CONCLUIDO] S007-T8: testes integracao 7 efeitos .......... commit e1bbe50 [13:20]

RODADA 7 (proxima):
  S015-T4 (auto-concessao vantagens pre-definidas) ........... 3-4h
  S005-P1T3 (testes integracao participantes) ................ 3-4h
  S007-T9 (frontend: efeitos UI) ............................ 4-6h
  S006-T6 (frontend: wizard passo 1) ........................ 4-6h

RODADA 8+:
  [Frontend] S007-T10-T12 (frontend efeitos, 3 tasks) ....... [PENDENTE]
  [Frontend] S006-T7-T13 (wizard passos 2-6 + auto-save) .... [PENDENTE]
  [Frontend] S005-P2T1 a P2T3 (participantes frontend) ...... [PENDENTE]
  [Backend]  S015-T6/T7 (frontend ConfigPontos) .............. [PENDENTE]
```

---

## Decisoes de Design Pendentes

| Decisao | Contexto | Impacto |
|---------|----------|---------|
| PA-004 FORMULA_CUSTOMIZADA | Sem alvo definido (onde aplica o resultado?) | Bloqueia S007-T5alt |
| PA-006 VIG/SAB hardcoded | Abreviacao hardcoded (GAP-CALC-09) | Pos-MVP |
| FichaStatus MORTA/ABANDONADA | PO decidiu fichas nunca deletadas. Faltam status no enum. | Futuro (pos-wizard) |

---

## GAPs Pendentes (nao cobertos por nenhuma task)

| GAP | Descricao | Onde deveria estar | Status |
|-----|-----------|-------------------|--------|
| **GAP-MVP-02** | FichaStatus MORTA/ABANDONADA + DELETE retornar 405 | Spec 006 | NAO ENDERECADO |
| **GAP-MVP-04** | Polling 30s no frontend | Spec 009-ext | PARCIALMENTE ENDERECADO |
| **GAP-MVP-05** | FichaVantagem revogacao normal | Spec 007 | **RESOLVIDO** (R5 - DELETE endpoint) |
| **GAP-MVP-08** | Formulario de criacao de NPC no frontend | Spec 009-ext ou 006 | PARCIALMENTE ENDERECADO |

---

## Perguntas Abertas para o PO

| ID | Pergunta | Spec | Bloqueia |
|----|----------|------|---------|
| PA-004 | FORMULA_CUSTOMIZADA sem alvo definido | 007 | S007-T5alt |
| PA-006 | VIG/SAB hardcoded por abreviacao (GAP-CALC-09) | 007 | Pos-MVP |
| PA-015-01..03 | Dados default ClassePontosConfig/RacaPontosConfig | 015 | Nao bloqueia |
| PA-015-04 | Campo `origem` em FichaVantagem: enum JOGADOR/MESTRE/SISTEMA? | 015 | T4 |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-05 [14:01] (pos-rodada 6: 19/35, 571B+359F testes, merge limpo)*
*Proxima revisao: inicio da rodada 7*
