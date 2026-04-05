# Handoff de Sessao -- 2026-04-05 (pos-sessao 11, rodada 5)

> Documento de transferencia de contexto para a proxima sessao de trabalho.
> Branch atual: `main`
> Backend: **523 testes** passando, 0 falhas | Frontend: **359 testes** passando, 0 falhas
> Sprint 2: **15/35 tasks concluidas** (43%)
> Ultima atualizacao: 2026-04-05 (Rodada 5)

---

## Resumo Executivo

A Rodada 5 concluiu 4 tasks: S007-T7 (TipoVantagem INSOLITUS com endpoints de concessao e revogacao), S006-T2 (validacao RacaClassePermitida na criacao de ficha), S006-T5 (pontosDisponiveis no FichaResumoResponse) e S015-T3 (integracao ClassePontosConfig/RacaPontosConfig no calculo de pontos). O motor de vantagens agora suporta Insolitus (concedido pelo Mestre sem custo) e revogacao de qualquer vantagem. O resumo da ficha inclui pontos disponiveis para atributos, aptidoes e vantagens com somatoria completa de NivelConfig + ClassePontosConfig + RacaPontosConfig.

**Proximo foco:** S007-T8 (testes integracao todos efeitos), S005-P1T1/T2 (participantes), S006-T4 (XP MESTRE-only).

---

## O que Foi Feito (acumulado Sprint 2: 15/35)

### Backend -- 523 testes (+14 desde Rodada 4)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| S007-T7 | Insolitus + endpoints concessao/revogacao | R5 | Enum TipoVantagem (VANTAGEM/INSOLITUS), campo tipoVantagem em VantagemConfig, campo concedidoPeloMestre em FichaVantagem. POST /fichas/{id}/vantagens/insolitus/{vantagemConfigId} (MESTRE-only, custo 0). DELETE /fichas/{id}/vantagens/{vid} (MESTRE-only, soft delete). Atualizado DTOs, mapper, VantagemConfiguracaoService. 6 testes. Commit `bd75582`. |
| S006-T2 | Validacao RacaClassePermitida na criacao | R5 | Adicionada chamada fichaValidationService.validarClassePermitidaPorRaca() no metodo criar() de FichaService. Se existem restricoes para a raca, classe deve estar na lista. 3 testes. Commit `1cb523a`. |
| S006-T5 | pontosDisponiveis no FichaResumoResponse | R5 | Novos campos: pontosAtributoDisponiveis, pontosAptidaoDisponiveis, pontosVantagemDisponiveis. Calculo: total concedido por nivel - pontos gastos. 3 testes. Commit `61b0bb4`. |
| S015-T3 | Integrar ConfigPontos no FichaResumo | R5 | pontosAtributoTotais = NivelConfig + ClassePontosConfig + RacaPontosConfig. pontosVantagemTotais idem. pontosAptidao = APENAS NivelConfig (decisao PO). 2 testes. Commit `5dc8bf2`. |
| S007-T3+T4+T5 | BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP | R4 | FichaCalculationService: BONUS_DERIVADO via bonusMap, BONUS_VIDA_MEMBRO via membrosMap, calcularDadoUp() para DADO_UP. Commit `0621bc8`. |
| S015-T2 | 14 endpoints CRUD sub-recursos | R4 | 4 services + 14 endpoints + 2 testes integracao. Commit `ba52d29`. |
| S006-T1 | FichaStatus + endpoint /completar | R4 | Enum FichaStatus, campo status, endpoint PUT /fichas/{id}/completar, 9 testes. Commit `d55e312`. |
| S007-T2 | BONUS_ATRIBUTO + BONUS_APTIDAO | R3 | Tambem implementou BONUS_VIDA e BONUS_ESSENCIA (escopo expandido). Commit `52738da`. |
| S015-T1 | 4 entidades ConfigPontos | R3 | ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig, RacaVantagemPreDefinida + repos + DTOs + mappers. Commit `9ac2465`. |
| S007-T0 | Corrigir 6 bugs motor calculos | R1 | GAP-CALC-01/02/03/06/07/08. 7 testes novos. |
| S007-T1 | Adaptar modelo dados para efeitos | R2 | SCHEMA-01/02, FichaProspeccao.dadoDisponivel, findByFichaIdWithEfeitos, stub aplicarEfeitosVantagens. |
| S015-T5 | DefaultProvider: 8 bugs + defaults | R2 | BUG-DC-02..09 (exceto DC-03). 9 BonusConfig, 8 PontosVantagem, 8 CategoriaVantagem, 22 vantagens canonicas. |
| URG-01 | Bug XP verificado | R2 | PUT /fichas/{id}/xp ja tinha @PreAuthorize. |

### Frontend -- 359/359 testes (+88 desde Sprint 1)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| QW-Bug1/2 | Barras vida/essencia + pontos vantagem | R3 | Ja estavam corrigidos (da rodada 2). |
| URG-02 | Fix 38 testes falhando | R2 | 359/359 testes passando. |
| QW-Bug3 | Rota NPC errada | R2 | verFicha() corrigido. |

---

## O que Esta Pendente (20 tasks restantes)

### Caminho Critico: Spec 007 (VantagemEfeito + Motor)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S007-T2 | Backend | BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA | T1 | **CONCLUIDA** (R3) |
| S007-T3+T4+T5 | Backend | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | T2 | **CONCLUIDA** (R4) |
| S007-T5alt | Backend | FORMULA_CUSTOMIZADA | T1, PA-004 | **BLOQUEADO** (PA-004 nao resolvido) |
| S007-T7 | Backend | Insolitus + endpoint concessao/revogacao | T1 | **CONCLUIDA** (R5) |
| S007-T8 | Backend | Testes integracao todos efeitos | T3-T7 | **DESBLOQUEADO** |
| S007-T9..T12 | Frontend | UI efeitos (4 tasks) | T8 | PENDENTE |

**NOTA:** 7 de 8 TipoEfeito implementados no motor. Falta apenas FORMULA_CUSTOMIZADA (bloqueado por PA-004). Insolitus completo com concessao + revogacao.

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
| S006-T4 | Backend | PUT /fichas/{id}/xp MESTRE-only | Nenhuma | [PENDENTE] |
| S006-T5 | Backend | pontosDisponiveis no response | Nenhuma | **CONCLUIDA** (R5) |
| S006-T6..T13 | Frontend | Wizard 6 passos + auto-save + badge | S006-T1, T5, Spec 007 | PENDENTE |

### Track D: Spec 005 (Participantes) -- 6 tasks, todas PENDENTES

### Quick Wins Frontend -- 0 bugs restantes (todos corrigidos)

---

## Rodada 5 -- CONCLUIDA (4 commits, merge limpo, 523 testes)

### Resultado da Rodada 5

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S007-T7) | Insolitus + endpoints concessao/revogacao | **CONCLUIDO** -- TipoVantagem enum, campo concedidoPeloMestre, POST insolitus, DELETE revogacao, 6 testes. Commit `bd75582`. |
| Agente B (S006-T2) | Validacao RacaClassePermitida na criacao | **CONCLUIDO** -- Validacao adicionada no criar() de FichaService, 3 testes. Commit `1cb523a`. |
| Agente C (S006-T5) | pontosDisponiveis no FichaResumoResponse | **CONCLUIDO** -- 3 novos campos no response, calculo NivelConfig-based, 3 testes. Commit `61b0bb4`. |
| Agente D (S015-T3) | Integrar ConfigPontos no FichaResumo | **CONCLUIDO** -- ClassePontosConfig+RacaPontosConfig somados ao total, aptidao independente, 2 testes. Commit `5dc8bf2`. |

**Merge:** Cherry-pick de 3 worktrees isolados + commit direto para T3 sequencial. 523 testes passando, 0 falhas.

---

## Proxima Rodada (Rodada 6) -- Planejamento

### Agente 1 -- Backend: S007-T8 (Testes integracao todos efeitos)

**Task:** Testes de integracao cobrindo todos os 7 tipos de efeito em cenarios completos
**Estimativa:** 4-6 horas
**Dependencia:** S007-T2..T7 (todas CONCLUIDAS)

### Agente 2 -- Backend: S005-P1T1 (Re-solicitacao constraint)

**Task:** Constraint de re-solicitacao apos rejeicao
**Estimativa:** 2-3 horas
**Dependencia:** Nenhuma

### Agente 3 -- Backend: S005-P1T2 (Endpoints faltantes participantes)

**Task:** Endpoints CRUD faltantes para JogoParticipante
**Estimativa:** 3-4 horas
**Dependencia:** Nenhuma

### Agente 4 -- Backend: S006-T4 (PUT /fichas/{id}/xp MESTRE-only)

**Task:** Endpoint XP ja existe, verificar constraint MESTRE-only
**Estimativa:** 1-2 horas
**Dependencia:** Nenhuma

---

## Sequencia Completa

```
RODADA 3 — CONCLUIDA:
  [CONCLUIDO] S007-T2: BONUS_ATRIBUTO+APTIDAO+VIDA+ESSENCIA  commit 52738da
  [CONCLUIDO] S015-T1: 4 entidades ConfigPontos ............. commit 9ac2465
  [CONCLUIDO] QW-Bug1/2: frontend ja corrigido .............. verificado R3

RODADA 4 — CONCLUIDA:
  [CONCLUIDO] S007-T3+T4+T5: DERIVADO+VIDA_MEMBRO+DADO_UP .. commit 0621bc8
  [CONCLUIDO] S006-T1: FichaStatus + /completar ............. commit d55e312
  [CONCLUIDO] S015-T2: 14 CRUD endpoints sub-recursos ....... commit ba52d29

RODADA 5 — CONCLUIDA:
  [CONCLUIDO] S007-T7: Insolitus + concessao/revogacao ...... commit bd75582
  [CONCLUIDO] S006-T2: Validacao RacaClassePermitida ........ commit 1cb523a
  [CONCLUIDO] S006-T5: pontosDisponiveis no response ........ commit 61b0bb4
  [CONCLUIDO] S015-T3: ConfigPontos no FichaResumo .......... commit 5dc8bf2

RODADA 6 (proxima):
  S007-T8 (testes integracao todos efeitos) ................. 4-6h
  S005-P1T1 (re-solicitacao constraint) ..................... 2-3h
  S005-P1T2 (endpoints faltantes) ........................... 3-4h
  S006-T4 (PUT /fichas/{id}/xp MESTRE-only) ................. 1-2h

RODADA 7+:
  [Backend]  S015-T4 (auto-concessao vantagens pre-definidas)  [DESBLOQUEADO]
  [Frontend] S007-T9-T12 (frontend efeitos, 4 tasks) ........ [PENDENTE]
  [Frontend] S006-T6-T13 (wizard frontend, 8 tasks) ......... [PENDENTE]
  [Backend]  S005-P1T3 (testes integracao) ................... [PENDENTE]
  [Frontend] S005-P2T1 a P2T3 (participantes frontend) ...... [PENDENTE]
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

*Produzido por: PM/Scrum Orchestrator | 2026-04-05 (pos-rodada 5: 15/35, 523B+359F testes, merge limpo)*
*Proxima revisao: inicio da rodada 6*
