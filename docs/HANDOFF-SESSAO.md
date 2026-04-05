# Handoff de Sessao -- 2026-04-05 (pos-sessao 12, rodada 8 parcial)

> Documento de transferencia de contexto para a proxima sessao de trabalho.
> Branch atual: `main`
> Backend: **581 testes** passando, 0 falhas | Frontend: **459 testes** passando, 0 falhas
> Sprint 2: **25/35 tasks concluidas** (71%)
> Ultima atualizacao: 2026-04-05 [15:30]

---

## Resumo Executivo

A Rodada 8 (parcial) concluiu 2 tasks frontend, elevando o Sprint 2 para 71% (25/35). Destaques: Wizard Passo 2 Descricao com StepDescricaoComponent e 21 testes (11 step + 10 wizard) (S006-T7), e Participantes API/Service com endpoints banir/desbanir/remover/meu-status/cancelarSolicitacao alinhados ao backend (S005-P2T1).

**Proximo foco (Rodada 9):** S006-T8 (wizard passo 3 atributos, complexidade alta 6-8h), S007-T11 (DadoUp preview seletor, 2-3h), S005-P2T2 (JogoDetail do Mestre semantica remover/banir), S006-T9 ou T10 (wizard passo 4 aptidoes ou passo 5 vantagens).

---

## O que Foi Feito (acumulado Sprint 2: 25/35)

### Backend -- 581 testes (+10 desde Rodada 6)

| ID | Descricao | Rodada | Horario | Detalhes |
|----|-----------|--------|---------|----------|
| S015-T4 | Auto-concessao vantagens pre-definidas | R7 | [14:54] | Enum `OrigemVantagem` (JOGADOR/MESTRE/SISTEMA), campo `origem` em FichaVantagem, `VantagemAutoConcessaoService.concederVantagensParaNivel()`, integracao em `FichaService.criar()` e `concederXp()` (loop pulo niveis), `existsByFichaIdAndVantagemConfigId`, `concederInsolitus()` atualizado para origem=MESTRE. 8 testes novos em `VantagemAutoConcessaoIntegrationTest`. Commit `1dec7db`. |
| S005-P1T3 | Testes integracao participantes | R7 | [14:54] | 2 testes novos em `JogoParticipanteServiceIntegrationTest`: `naoDeveCancelarSolicitacaoInexistente` e `banirNaoDeveSetarDeletedAt`. 27 testes ja existiam (P1T1+P1T2). Commit `32d4b94`. |
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

### Frontend -- 459/459 testes (+35 desde Rodada 7, +188 desde Sprint 1)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| S006-T7 | Wizard Passo 2 Descricao | R8 | `StepDescricaoComponent` criado, `formPasso2` no wizard, labels atualizados (Descricao/Atributos/Aptidoes), `salvarPasso2` via PUT /fichas/{id}, `ficha.model.ts` + `UpdateFichaDto` atualizados com campo `descricao`. 11 testes no step + 10 no wizard. |
| S005-P2T1 | Participantes API/Service | R8 | `JogosApiService`: banirParticipante PUT /banir, removerParticipante DELETE /{pid}, desbanirParticipante PUT /desbanir, meuStatusParticipacao GET /meu-status (404->null), cancelarSolicitacao DELETE /minha-solicitacao, listParticipantes com filtro status. `ParticipanteBusinessService`: banir/desbanir/remover/meuStatus/cancelarSolicitacao. Testes atualizados. |
| S007-T9 | VantagensConfig secao de efeitos | R7 | `vantagem-efeito.model.ts`, `vantagem-efeito.service.ts`, `EfeitoFormComponent` standalone com formulario dinamico por tipo, preview calculado, validacao client-side, nova aba "Efeitos" com badge de contagem. FORMULA_CUSTOMIZADA bloqueada (PA-004) com aviso "em breve". 31 testes novos em `efeito-form.component.spec.ts`. Commit `f19c213`. |
| S006-T6 | Wizard Passo 1 Identificacao | R7 | `FichaWizardComponent` (orquestrador), `StepIdentificacaoComponent` (dumb), tipos `EstadoSalvamento`/`FormPasso1`, rotas `criar`/`criar-npc`, retomada de rascunho via `?fichaId=`, classesFiltradas computed por raca, toggle isNpc para MESTRE, placeholders passos 2-6. FichaFormComponent antigo substituido (re-exporta). 34 testes novos em `ficha-wizard.component.spec.ts`. Commit `064d648`. |
| QW-Bug1/2 | Barras vida/essencia + pontos vantagem | R3 | Ja estavam corrigidos (da rodada 2). |
| URG-02 | Fix 38 testes falhando | R2 | 359/359 testes passando. |
| QW-Bug3 | Rota NPC errada | R2 | verFicha() corrigido. |

---

## O que Esta Pendente (10 tasks restantes)

### Caminho Critico: Spec 007 (VantagemEfeito + Motor)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S007-T2 | Backend | BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA | T1 | **CONCLUIDA** (R3) |
| S007-T3+T4+T5 | Backend | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | T2 | **CONCLUIDA** (R4) |
| S007-T5alt | Backend | FORMULA_CUSTOMIZADA | T1, PA-004 | **BLOQUEADO** (PA-004 nao resolvido) |
| S007-T7 | Backend | Insolitus + endpoint concessao/revogacao | T1 | **CONCLUIDA** (R5) |
| S007-T8 | Backend | Testes integracao todos efeitos | T3-T7 | **CONCLUIDA** (R6) |
| S007-T9 | Frontend | VantagensConfig secao de efeitos | T8 | **CONCLUIDA** (R7) |
| S007-T10 | Frontend | FormulaEditorEfeito | T9 | **DESBLOQUEADO** |
| S007-T11 | Frontend | DadoUp seletor | T9 | **DESBLOQUEADO** |
| S007-T12 | Frontend | UI concessao Insolitus pelo Mestre | T9 | **DESBLOQUEADO** |

**NOTA:** 7 de 8 TipoEfeito implementados no motor. Falta apenas FORMULA_CUSTOMIZADA (bloqueado por PA-004). Insolitus completo com concessao + revogacao. Testes de integracao (T8) CONCLUIDOS com 20 testes. Frontend efeitos (T9) CONCLUIDO com 31 testes.

### Track B: Spec 015 (ConfigPontos)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S015-T1 | Backend | 4 entidades, repos, DTOs, mappers | Nenhuma | **CONCLUIDA** (R3) |
| S015-T2 | Backend | CRUD endpoints sub-recursos | S015-T1 | **CONCLUIDA** (R4) |
| S015-T3 | Backend | Integrar pontos no FichaResumoResponse | S015-T1, S007-T1 | **CONCLUIDA** (R5) |
| S015-T4 | Backend | Auto-concessao vantagens pre-definidas | S015-T1, S007-T7 | **CONCLUIDA** (R7) |

**NOTA:** Spec 015 backend 100% CONCLUIDO (5/5 backend tasks, contando T5). Restam apenas T6/T7 (frontend ConfigPontos).

### Track C: Spec 006 (Wizard Ficha)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S006-T1 | Backend | Campo status + /completar | Nenhuma | **CONCLUIDA** (R4) |
| S006-T2 | Backend | Validacao RacaClassePermitida | Nenhuma | **CONCLUIDA** (R5) |
| S006-T3 | Backend | (ABSORVIDA por URG-01) | -- | -- |
| S006-T4 | Backend | PUT /fichas/{id}/xp acumulativo + motivo | Nenhuma | **CONCLUIDA** (R6) |
| S006-T5 | Backend | pontosDisponiveis no response | Nenhuma | **CONCLUIDA** (R5) |
| S006-T6 | Frontend | Passo 1: Identificacao (rewrite wizard) | S006-T1 | **CONCLUIDA** (R7) |
| S006-T7 | Frontend | Passo 2: Descricao fisica | S006-T6 | **CONCLUIDA** (R8) |
| S006-T8 | Frontend | Passo 3: Distribuicao de atributos | S006-T7 | **DESBLOQUEADO** |
| S006-T9 | Frontend | Passo 4: Distribuicao de aptidoes | S006-T5, T6 | **DESBLOQUEADO** |
| S006-T10 | Frontend | Passo 5: Compra de vantagens iniciais | S006-T5, T6 | **DESBLOQUEADO** |
| S006-T11 | Frontend | Passo 6: Revisao e confirmacao | S006-T1, T6 | **DESBLOQUEADO** |
| S006-T12 | Frontend | Auto-save visual (indicador) | S006-T6 | **DESBLOQUEADO** |
| S006-T13 | Frontend | Badge "incompleta" na listagem | S006-T1 | **DESBLOQUEADO** |

### Track D: Spec 005 (Participantes)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S005-P1T1 | Backend | Re-solicitacao strategy Reactivate | Nenhuma | **CONCLUIDA** (R6) |
| S005-P1T2 | Backend | Endpoints faltantes (banir, desbanir, remover, meu-status, filtro) | S005-P1T1 | **CONCLUIDA** (R6) |
| S005-P1T3 | Backend | Testes de integracao completos | S005-P1T1, P1T2 | **CONCLUIDA** (R7) |
| S005-P2T1 | Frontend | API service + business service | S005-P1T2 | **CONCLUIDA** (R8) |
| S005-P2T2 | Frontend | JogoDetail do Mestre (semantica remover/banir) | S005-P2T1 | **DESBLOQUEADO** |
| S005-P2T3 | Frontend | JogosDisponiveis do Jogador (solicitar, status) | S005-P2T1 | [PENDENTE] |

### Quick Wins Frontend -- 0 bugs restantes (todos corrigidos)

---

## Rodada 7 -- CONCLUIDA [14:54] (4 commits, +10B +65F testes, 581B+424F testes)

### Resultado da Rodada 7

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S005-P1T3) | Testes integracao participantes | **CONCLUIDO** -- 2 testes novos (cenarios ausentes: cancelar inexistente + banir nao seta deletedAt). Commit `32d4b94`. |
| Agente B (S015-T4) | Auto-concessao vantagens pre-definidas | **CONCLUIDO** -- enum OrigemVantagem, VantagemAutoConcessaoService, integracao criar/concederXp, 8 testes. Commit `1dec7db`. |
| Agente C (S007-T9) | VantagensConfig secao de efeitos (frontend) | **CONCLUIDO** -- EfeitoFormComponent standalone, formulario dinamico por tipo, preview calculado, aba "Efeitos" com badge. 31 testes. Commit `f19c213`. |
| Agente D (S006-T6) | Wizard Passo 1 Identificacao (frontend) | **CONCLUIDO** -- FichaWizardComponent + StepIdentificacaoComponent, rotas criar/criar-npc, retomada rascunho, classesFiltradas. 34 testes. Commit `064d648`. |

---

## Rodada 6 -- CONCLUIDA [14:01] (4 commits, +48 testes, 571 testes)

### Resultado da Rodada 6

| Agente | Task | Horario | Resultado |
|--------|------|---------|-----------|
| Agente A (S005-P1T1) | Re-solicitacao strategy Reactivate | [11:14] | **CONCLUIDO** -- nativeQuery bypass @SQLRestriction, strategy Reactivate, 6 testes. Commit `32b984f`. |
| Agente B (S005-P1T2) | Endpoints faltantes participantes | [11:19] | **CONCLUIDO** -- 4 novos endpoints, correcao critica DELETE, 12 testes. Commit `7a71a55`. |
| Agente C (S006-T4) | XP acumulativo + motivo + FichaResumoResponse | [11:19] | **CONCLUIDO** -- XP acumulativo, motivo opcional, guard nivel, response FichaResumo, 14 testes. Commit `d37b227`. |
| Agente D (S007-T8) | Testes integracao 7 tipos VantagemEfeito | [13:20] | **CONCLUIDO** -- 20 testes, 7 tipos cobertos, cenarios de borda, entityManager flush/clear pattern. Commit `e1bbe50`. |

---

## Proxima Rodada (Rodada 8) -- Planejamento

### Agente 1 -- Frontend: S006-T7 (Wizard Passo 2 -- Descricao Fisica)

**Task:** Passo 2 do wizard de criacao de ficha: descricao fisica do personagem (peso, altura, cor dos olhos, cabelo, pele, etc.)
**Estimativa:** 3-4 horas
**Dependencia:** S006-T6 (CONCLUIDA R7) -- DESBLOQUEADA

### Agente 2 -- Frontend: S006-T8 (Wizard Passo 3 -- Distribuicao de Atributos)

**Task:** Passo 3 do wizard: distribuicao de pontos de atributo com validacao de limites (min/max, limitadorAtributo por nivel)
**Estimativa:** 4-6 horas
**Dependencia:** S006-T5 (CONCLUIDA R5), S006-T6 (CONCLUIDA R7) -- DESBLOQUEADA

### Agente 3 -- Frontend: S007-T10 ou S007-T11 (FormulaEditorEfeito ou DadoUp Seletor)

**Task:** Componente frontend para editar formulas de efeito customizado OU seletor de dado para DADO_UP
**Estimativa:** 3-4 horas
**Dependencia:** S007-T9 (CONCLUIDA R7) -- DESBLOQUEADA

### Agente 4 -- Frontend: S005-P2T1 (Participantes API/Service)

**Task:** Alinhar API service e business service Angular com novos endpoints backend (banir, desbanir, remover, meu-status, filtro)
**Estimativa:** 2-3 horas
**Dependencia:** S005-P1T3 (CONCLUIDA R7) -- DESBLOQUEADA

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

RODADA 7 -- CONCLUIDA [14:54]:
  [CONCLUIDO] S005-P1T3: testes integracao participantes .... commit 32d4b94
  [CONCLUIDO] S015-T4: auto-concessao vantagens ............. commit 1dec7db
  [CONCLUIDO] S007-T9: frontend efeitos UI .................. commit f19c213
  [CONCLUIDO] S006-T6: wizard passo 1 identificacao ......... commit 064d648

RODADA 8 (proxima):
  S006-T7 (wizard passo 2 -- descricao fisica) .............. 3-4h
  S006-T8 (wizard passo 3 -- atributos) .................... 4-6h
  S007-T10 ou T11 (frontend efeitos formula/dado) ........... 3-4h
  S005-P2T1 (participantes frontend API/service) ............ 2-3h

RODADA 9+:
  [Frontend] S007-T10-T12 restantes (frontend efeitos) ...... [PENDENTE]
  [Frontend] S006-T9-T13 (wizard passos 4-6 + auto-save) .... [PENDENTE]
  [Frontend] S005-P2T2/P2T3 (participantes frontend) ........ [PENDENTE]
  [Frontend] S015-T6/T7 (frontend ConfigPontos) .............. [PENDENTE]
```

---

## Decisoes de Design Pendentes

| Decisao | Contexto | Impacto |
|---------|----------|---------|
| PA-004 FORMULA_CUSTOMIZADA | Sem alvo definido (onde aplica o resultado?) | Bloqueia S007-T5alt |
| PA-006 VIG/SAB hardcoded | Abreviacao hardcoded (GAP-CALC-09) | Pos-MVP |
| FichaStatus MORTA/ABANDONADA | PO decidiu fichas nunca deletadas. Faltam status no enum. | Futuro (pos-wizard) |
| PA-015-04 | Campo `origem` em FichaVantagem | **RESOLVIDO** (R7 - enum OrigemVantagem JOGADOR/MESTRE/SISTEMA implementado em S015-T4) |

---

## GAPs Pendentes (nao cobertos por nenhuma task)

| GAP | Descricao | Onde deveria estar | Status |
|-----|-----------|-------------------|--------|
| **GAP-MVP-02** | FichaStatus MORTA/ABANDONADA + DELETE retornar 405 | Spec 006 | NAO ENDERECADO |
| **GAP-MVP-04** | Polling 30s no frontend | Spec 009-ext | PARCIALMENTE ENDERECADO |
| **GAP-MVP-05** | FichaVantagem revogacao normal | Spec 007 | **RESOLVIDO** (R5 - DELETE endpoint) |
| **GAP-MVP-08** | Formulario de criacao de NPC no frontend | Spec 009-ext ou 006 | **PARCIALMENTE RESOLVIDO** (R7 - wizard suporta rota criar-npc + toggle isNpc) |

---

## Perguntas Abertas para o PO

| ID | Pergunta | Spec | Bloqueia |
|----|----------|------|---------|
| PA-004 | FORMULA_CUSTOMIZADA sem alvo definido | 007 | S007-T5alt |
| PA-006 | VIG/SAB hardcoded por abreviacao (GAP-CALC-09) | 007 | Pos-MVP |
| PA-015-01..03 | Dados default ClassePontosConfig/RacaPontosConfig | 015 | Nao bloqueia |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-05 [14:54] (pos-rodada 7: 23/35, 581B+424F testes)*
*Proxima revisao: inicio da rodada 8*
