# Handoff de Sessao -- 2026-04-04 (pos-sessao 10, rodada 4)

> Documento de transferencia de contexto para a proxima sessao de trabalho.
> Branch atual: `feature/009-npc-fichas-mestre`
> Backend: **509 testes** passando, 0 falhas | Frontend: **359 testes** passando, 0 falhas
> Sprint 2: **11/35 tasks concluidas** (31%)
> Ultima atualizacao: 2026-04-04 (Rodada 4)

---

## Resumo Executivo

A Rodada 4 concluiu 5 tasks em paralelo: S007-T3+T4+T5 (BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP no motor de efeitos), S015-T2 (14 endpoints CRUD sub-recursos ConfigPontos) e S006-T1 (FichaStatus RASCUNHO/COMPLETA + endpoint /completar). O motor agora aplica 7 dos 8 tipos de efeito (falta apenas FORMULA_CUSTOMIZADA, bloqueado por PA-004). Os bugs de frontend QW-Bug1/Bug2 ja estavam corrigidos desde a rodada 2.

**Proximo foco:** S007-T7 (Insolitus + endpoint concessao), S006-T2 (validacao RacaClassePermitida), S006-T5 (pontosDisponiveis no response).

---

## O que Foi Feito (acumulado Sprint 2: 11/35)

### Backend -- 509 testes (+35 desde Rodada 3)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| S007-T3+T4+T5 | BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP | R4 | FichaCalculationService: BONUS_DERIVADO via bonusMap, BONUS_VIDA_MEMBRO via membrosMap, calcularDadoUp() para DADO_UP. Assinatura recalcular() estendida com dadosOrdenados e prospeccoes. FichaService carrega DadoProspeccaoConfig e FichaProspeccao. Commit `0621bc8`. |
| S015-T2 | 14 endpoints CRUD sub-recursos | R4 | 4 services (ClassePontos, ClasseVantagemPreDef, RacaPontos, RacaVantagemPreDef) + 7 endpoints em ClasseController + 7 endpoints em RacaController + 2 testes integracao. Commit `ba52d29`. |
| S006-T1 | FichaStatus + endpoint /completar | R4 | Enum FichaStatus (RASCUNHO, COMPLETA), campo status em Ficha com @Builder.Default, endpoint PUT /fichas/{id}/completar, validarCompletude() em FichaValidationService, 9 testes novos. Commit `d55e312`. |
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

## O que Esta Pendente (24 tasks restantes)

### Caminho Critico: Spec 007 (VantagemEfeito + Motor)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S007-T2 | Backend | BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA | T1 | **CONCLUIDA** (R3) |
| S007-T3+T4+T5 | Backend | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | T2 | **CONCLUIDA** (R4) |
| S007-T5alt | Backend | FORMULA_CUSTOMIZADA | T1, PA-004 | **BLOQUEADO** (PA-004 nao resolvido) |
| S007-T7 | Backend | Insolitus + endpoint concessao | T1 | **DESBLOQUEADO** |
| S007-T8 | Backend | Testes integracao todos efeitos | T3-T7 | PENDENTE |
| S007-T9..T12 | Frontend | UI efeitos (4 tasks) | T8 | PENDENTE |

**NOTA:** 7 de 8 TipoEfeito implementados no motor. Falta apenas FORMULA_CUSTOMIZADA (bloqueado por PA-004).

### Track B: Spec 015 (ConfigPontos)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S015-T1 | Backend | 4 entidades, repos, DTOs, mappers | Nenhuma | **CONCLUIDA** (R3) |
| S015-T2 | Backend | CRUD endpoints sub-recursos | S015-T1 | **CONCLUIDA** (R4) |
| S015-T3 | Backend | Integrar pontos no FichaResumoResponse | S015-T1, S007-T1 | **DESBLOQUEADO** |
| S015-T4 | Backend | Auto-concessao vantagens pre-definidas | S015-T1, S007-T7 | PENDENTE |

### Track C: Spec 006 (Wizard Ficha)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S006-T1 | Backend | Campo status + /completar | Nenhuma | **CONCLUIDA** (R4) |
| S006-T2 | Backend | Validacao RacaClassePermitida | Nenhuma | **DESBLOQUEADO** |
| S006-T3 | Backend | (ABSORVIDA por URG-01) | -- | -- |
| S006-T4 | Backend | PUT /fichas/{id}/xp MESTRE-only | Nenhuma | [PENDENTE] |
| S006-T5 | Backend | pontosDisponiveis no response | Nenhuma | **DESBLOQUEADO** |
| S006-T6..T13 | Frontend | Wizard 6 passos + auto-save + badge | S006-T1, T5, Spec 007 | PENDENTE |

### Track D: Spec 005 (Participantes) -- 6 tasks, todas PENDENTES

### Quick Wins Frontend -- 0 bugs restantes (todos corrigidos)

---

## Rodada 4 -- CONCLUIDA (3 commits, merge limpo, 509 testes)

### Resultado da Rodada 4

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente 1 (S007-T3+T4+T5) | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | **CONCLUIDO** — commit `0621bc8`. 7 de 8 tipos de efeito implementados. |
| Agente 3 (S015-T2) | 14 CRUD endpoints sub-recursos | **CONCLUIDO** — 4 services + 14 endpoints + 2 testes integracao. Commit `ba52d29`. |
| Agente 4 (S006-T1) | FichaStatus + /completar | **CONCLUIDO** — enum FichaStatus, campo status, endpoint, validarCompletude, 9 testes. Commit `d55e312`. |

**Merge:** Conflito potencial FichaService.java (S007 vs S006-T1) resolvido manualmente — reconciliacao bem-sucedida. 509 testes passando, 0 falhas.

---

## Proxima Rodada (Rodada 5) -- Planejamento

### Agente 1 -- Backend Caminho Critico: S007-T7 (Insolitus)

**Task:** Insolitus + endpoint de concessao
**Estimativa:** 3-4 horas
**Dependencia:** S007-T1 (CONCLUIDA)
**Escopo:**
- Campo tipoVantagem em VantagemConfig (enum VANTAGEM | INSOLITUS)
- Endpoint POST /fichas/{id}/vantagens/conceder (MESTRE-only, sem custo de pontos)
- Endpoint DELETE /fichas/{id}/vantagens/{vantagemId} (MESTRE-only, revogacao)
**Arquivos principais:** `model/VantagemConfig.java`, `service/FichaService.java`, `controller/FichaController.java`

### Agente 2 -- Backend Paralelo: S006-T2

**Task:** Validacao RacaClassePermitida na criacao
**Estimativa:** 2-3 horas
**Dependencia:** Nenhuma
**Escopo:** Validar na criacao/atualizacao de ficha se a combinacao raca+classe e permitida
**Arquivos:** `service/FichaValidationService.java`

### Agente 3 -- Backend Paralelo: S006-T5

**Task:** pontosDisponiveis no FichaResumoResponse
**Estimativa:** 2-3 horas
**Dependencia:** Nenhuma
**Escopo:** Calcular e incluir pontosAtributoDisponiveis, pontosAptidaoDisponiveis, pontosVantagemDisponiveis no response
**Arquivos:** `dto/response/FichaResumoResponse.java`, `service/FichaResumoService.java`

### Agente 4 -- Backend Paralelo: S015-T3

**Task:** Integrar pontos ConfigPontos no FichaResumoResponse
**Estimativa:** 2-3 horas
**Dependencia:** S015-T1 (CONCLUIDA), S007-T1 (CONCLUIDA)
**Escopo:** Somar pontos de ClassePontosConfig + RacaPontosConfig com NivelConfig no FichaResumo
**Arquivos:** `service/FichaResumoService.java`

**NOTA:** Agente 3 e Agente 4 tocam o mesmo arquivo (FichaResumoService). Mitigacao: S006-T5 roda PRIMEIRO, S015-T3 roda APOS.

---

## Plano Anti-Conflito de Merge (Rodada 5)

| Agente | Pacotes/Arquivos PERMITIDOS | NAO TOCAR |
|--------|---------------------------|-----------|
| Agente 1 (S007-T7) | `model/VantagemConfig.java`, `service/FichaService.java`, `controller/FichaController.java` | configuracao/*, FichaResumoService, FichaValidationService |
| Agente 2 (S006-T2) | `service/FichaValidationService.java`, `service/FichaService.java` | FichaCalculation*, configuracao/*, FichaResumoService |
| Agente 3 (S006-T5) | `dto/response/FichaResumoResponse.java`, `service/FichaResumoService.java` | FichaCalculation*, configuracao/*, FichaValidationService |
| Agente 4 (S015-T3) | `service/FichaResumoService.java` (APOS S006-T5) | FichaCalculation*, configuracao/*, FichaValidationService |

**Conflitos potenciais:**
- Agente 1 vs Agente 2: ambos tocam FichaService. Mitigacao: T7 adiciona metodos novos, T2 modifica validacoes existentes — areas distintas.
- Agente 3 vs Agente 4: MESMO ARQUIVO (FichaResumoService). Mitigacao: sequencial.
- Demais combinacoes: NENHUM conflito.

---

## Decisoes de Design Pendentes

| Decisao | Contexto | Impacto |
|---------|----------|---------|
| PA-004 FORMULA_CUSTOMIZADA | Sem alvo definido (onde aplica o resultado?) | Bloqueia S007-T5alt |
| PA-006 VIG/SAB hardcoded | Abreviacao hardcoded (GAP-CALC-09) | Pos-MVP |
| FichaStatus MORTA/ABANDONADA | PO decidiu fichas nunca deletadas. Faltam status no enum. | Futuro (pos-wizard) |
| FichaVantagem revogacao normal | Mestre pode revogar qualquer vantagem. Endpoint necessario. | S007-T7 |

---

## GAPs Pendentes (nao cobertos por nenhuma task)

| GAP | Descricao | Onde deveria estar | Status |
|-----|-----------|-------------------|--------|
| **GAP-MVP-02** | FichaStatus MORTA/ABANDONADA + DELETE retornar 405 | Spec 006 | NAO ENDERECADO |
| **GAP-MVP-04** | Polling 30s no frontend | Spec 009-ext | PARCIALMENTE ENDERECADO |
| **GAP-MVP-05** | FichaVantagem revogacao normal | Spec 007 | NAO ENDERECADO |
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

## Sequencia Apos Rodada 4

```
RODADA 3 — CONCLUIDA:
  [CONCLUIDO] S007-T2: BONUS_ATRIBUTO+APTIDAO+VIDA+ESSENCIA  commit 52738da
  [CONCLUIDO] S015-T1: 4 entidades ConfigPontos ............. commit 9ac2465
  [CONCLUIDO] QW-Bug1/2: frontend ja corrigido .............. verificado R3

RODADA 4 — CONCLUIDA:
  [CONCLUIDO] S007-T3+T4+T5: DERIVADO+VIDA_MEMBRO+DADO_UP .. commit 0621bc8
  [CONCLUIDO] S006-T1: FichaStatus + /completar ............. commit d55e312
  [CONCLUIDO] S015-T2: 14 CRUD endpoints sub-recursos ....... commit ba52d29

RODADA 5 (proxima):
  S007-T7 (Insolitus + endpoint concessao) .................. 3-4h
  S006-T2 (validacao RacaClassePermitida) ................... 2-3h
  S006-T5 (pontosDisponiveis no response) ................... 2-3h
  S015-T3 (integrar pontos no FichaResumo) .................. 2-3h (APOS S006-T5)

RODADA 6:
  S007-T8 (testes integracao todos efeitos) ................. 4-6h
  S005-P1T1 (re-solicitacao constraint) ..................... 2-3h
  S005-P1T2 (endpoints faltantes) ........................... 3-4h
  S006-T4 (PUT /fichas/{id}/xp MESTRE-only) ................. 1-2h

RODADA 7+:
  [Frontend] S007-T9-T12 (frontend efeitos, 4 tasks) ........ [PENDENTE]
  [Frontend] S006-T6-T13 (wizard frontend, 8 tasks) ......... [PENDENTE]
  [Backend]  S005-P1T3 (testes integracao) ................... [PENDENTE]
  [Frontend] S005-P2T1 a P2T3 (participantes frontend) ...... [PENDENTE]
```

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04 (pos-rodada 4: 11/35, 509B+359F testes, merge limpo)*
*Proxima revisao: inicio da rodada 5*
