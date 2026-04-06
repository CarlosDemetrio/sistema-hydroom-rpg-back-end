# Handoff de Sessao -- 2026-04-06 (pos-sessao 13, rodada 10 concluida)

> Documento de transferencia de contexto para a proxima sessao de trabalho.
> Branch atual: `main`
> Backend: **581 testes** passando, 0 falhas | Frontend: **603 testes** passando, 0 falhas
> Sprint 2: **33/35 tasks concluidas** (94%) — 2 tasks restantes (todas frontend)
> Ultima atualizacao: 2026-04-06 [07:10]

---

## Resumo Executivo

A Rodada 10 concluiu com **todos os 4 agentes entregando com sucesso**, elevando o Sprint 2 para 94% (33/35) com **603 testes frontend** (+113 vs Rodada 9). Entregas: S006-T9 (wizard passo 4 aptidoes, commit `1895648`, 22 testes), S006-T10 (wizard passo 5 vantagens, commit `0702b03`, 40 testes), S005-P2T3 (JogosDisponiveis Jogador com solicitar/cancelar/status, commit `d2c262c`, 34 testes), e S007-T12 (UI concessao Insolitus pelo Mestre, commit `d23a3cf`, 17 testes).

**Restam apenas 2 tasks do escopo original Sprint 2 (todas frontend):** S006-T11 (wizard passo 6 revisao + completar) e S007-T10 (FormulaEditorEfeito — bloqueado por PA-004, opcao mockar/pular). Tasks bonus disponiveis: S006-T12 (auto-save visual) e S006-T13 (badge incompleta na listagem).

**Proximo foco (Rodada 11):** S006-T11 (wizard passo 6 revisao — fechamento do wizard), S006-T12 (auto-save visual), S006-T13 (badge incompleta) — todas em paralelo. S007-T10 fica para tratamento de PA-004 com PO.

**Observacoes da Rodada 10:**
- Agente A ficou sem tokens antes de concluir S006-T9; trabalho foi finalizado e bugfixes aplicados manualmente pelo PM
- `ficha-wizard.component.spec.ts` e `ficha-wizard-passo4.component.spec.ts` tem timeout de worker pre-existente (setTimeout 3s) — testes passam mas worker crasha no shutdown. Nao bloqueia build.
- Build: 0 erros TypeScript, apenas budget warning pre-existente (bundle 1.14MB vs limite 1MB)

---

## O que Foi Feito (acumulado Sprint 2: 33/35)

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

### Frontend -- 603 testes (+113 desde Rodada 9, +332 desde Sprint 1)

| ID | Descricao | Rodada | Detalhes |
|----|-----------|--------|----------|
| S007-T12 | UI concessao Insolitus pelo Mestre | R10 | `tipoVantagem` adicionado em VantagemConfig + FichaVantagemResponse. `concederInsolitus()` + `revogarVantagem()` em fichas-api.service e ficha-business.service. UI no `FichaVantagensTabComponent` (dumb): botao "Conceder Insolitus" (apenas Mestre) abre dialog com busca por nome filtrando vantagens INSOLITUS, tag "Insolitus" e label "Concedida pelo Mestre", botao revogar (trash) para todas vantagens (Mestre). `viewChild(FichaVantagensTabComponent)` no smart `FichaDetailComponent` para chamar `resetarConcedendo`. 17 testes em ficha-vantagens-tab.spec.ts. Commit `d23a3cf`. |
| S005-P2T3 | JogosDisponiveis do Jogador | R10 | `statusPorJogo = signal<Map<number, StatusParticipante \| null>>(new Map())` carregado para cada jogo. `solicitandoJogo` signal para loading individual. `carregarStatusParticipacao()` em paralelo, 404 = null. Helpers `getMeuStatus`/`podeEntrar`/`podeSolicitar`/`podeCancelar`. `solicitarEntrada` + `cancelarSolicitacao` atualizam statusPorJogo localmente sem re-fetch. Template com `@switch` no status, badges p-tag, TooltipModule. 34 testes. Commit `d2c262c`. |
| S006-T10 | Wizard Passo 5 Vantagens | R10 | `StepVantagensComponent` (Smart) em `steps/step-vantagens/`. `vantagensAgrupadasPorCategoria` computed por categoria. `estadoBotao(id, formulaCusto)` retorna 'comprar'/'comprada'/'sem-pontos'/'comprando'. `comprar()` chama POST /fichas/{id}/vantagens + GET /fichas/{id}/resumo. `pontosAtualizados` output. `filtroCategoria + termoBusca` signals. `idsComprados` computed Set O(1). Wizard: `pontosVantagemDisponiveis` signal + effect ao entrar passo 5. Passo 5 nao salva no avancar (compras persistidas individualmente). 40 testes em step-vantagens.component.spec.ts. Commit `0702b03`. |
| S006-T9 | Wizard Passo 4 Aptidoes | R10 | `StepAptidoesComponent` (dumb) com distribuicao de pontos pool vindo de NivelConfig.pontosAptidao, agrupamento por TipoAptidao. Wizard: `formPasso4` + `carregarDadosPasso4` (forkJoin getAptidoes+getFichaResumo+listAptidoesConfig) + `salvarPasso4` via PUT /fichas/{id}/aptidoes. 22 testes em step-aptidoes.spec.ts. Bugfixes manuais aplicados pelo PM apos exhaust de tokens do agente. Commit `1895648`. |
| S005-P2T2 | JogoDetail Mestre remover/banir | R9 | Bug critico corrigido: `removerParticipante()` chamava `banirParticipante()` — corrigido para `service.removerParticipante()` (soft delete). Botao Banir (confirmacao) para APROVADO, Desbanir para BANIDO. Filtro por status com SelectButton. 11 testes. Commit `f3637e9`. |
| S007-T11 | DadoUp seletor visual progressao | R9 | EfeitoFormComponent DADO_UP: seletor de dado base + slider de nivel + dado resultante calculado. VantagensConfigComponent carrega `DadoProspeccaoConfig[]` e repassa via `dadosDisponiveis` input. Commit `0c5fb29`. |
| S006-T8 | Wizard Passo 3 Atributos | R9 | `StepAtributosComponent` (dumb) com UI distribuicao de pontos (incremento/decremento, limites min/max, limitadorAtributo). Wizard: `carregarDadosPasso3` (forkJoin getAtributos+getFichaResumo+listNiveis), `salvarPasso3` via PUT /fichas/{id}/atributos. `FichaAtributoEditavel` e `AtualizarAtributoDto` no modelo. 9 testes step + 8 wizard. Commit `dd2677d`. |
| S005-P2T1 | Participantes API testes | R8→R9 | `jogos-api.service.spec.ts` atualizado com testes para banir/desbanir/remover/meu-status/cancelarSolicitacao. Cobertura completa dos endpoints S005-P1T2. Pendente de commit desde R8, formalizado R9. Commit `d6c3b34`. |
| S006-T7 | Wizard Passo 2 Descricao | R8→R9 | `StepDescricaoComponent` criado, `formPasso2` no wizard, labels atualizados (Descricao/Atributos/Aptidoes), `salvarPasso2` via PUT /fichas/{id}, `ficha.model.ts` + `UpdateFichaDto` atualizados com campo `descricao`. 11 testes no step + 10 no wizard. Pendente de commit desde R8, formalizado R9. Commit `dd2677d`. |
| S007-T9 | VantagensConfig secao de efeitos | R7 | `vantagem-efeito.model.ts`, `vantagem-efeito.service.ts`, `EfeitoFormComponent` standalone com formulario dinamico por tipo, preview calculado, validacao client-side, nova aba "Efeitos" com badge de contagem. FORMULA_CUSTOMIZADA bloqueada (PA-004) com aviso "em breve". 31 testes novos em `efeito-form.component.spec.ts`. Commit `f19c213`. |
| S006-T6 | Wizard Passo 1 Identificacao | R7 | `FichaWizardComponent` (orquestrador), `StepIdentificacaoComponent` (dumb), tipos `EstadoSalvamento`/`FormPasso1`, rotas `criar`/`criar-npc`, retomada de rascunho via `?fichaId=`, classesFiltradas computed por raca, toggle isNpc para MESTRE, placeholders passos 2-6. FichaFormComponent antigo substituido (re-exporta). 34 testes novos em `ficha-wizard.component.spec.ts`. Commit `064d648`. |
| QW-Bug1/2 | Barras vida/essencia + pontos vantagem | R3 | Ja estavam corrigidos (da rodada 2). |
| URG-02 | Fix 38 testes falhando | R2 | 359/359 testes passando. |
| QW-Bug3 | Rota NPC errada | R2 | verFicha() corrigido. |

---

## O que Esta Pendente (2 tasks restantes do escopo Sprint 2 + 2 bonus opcionais)

### Caminho Critico: Spec 007 (VantagemEfeito + Motor)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S007-T2 | Backend | BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA | T1 | **CONCLUIDA** (R3) |
| S007-T3+T4+T5 | Backend | BONUS_DERIVADO + BONUS_VIDA_MEMBRO + DADO_UP | T2 | **CONCLUIDA** (R4) |
| S007-T5alt | Backend | FORMULA_CUSTOMIZADA | T1, PA-004 | **BLOQUEADO** (PA-004 nao resolvido) |
| S007-T7 | Backend | Insolitus + endpoint concessao/revogacao | T1 | **CONCLUIDA** (R5) |
| S007-T8 | Backend | Testes integracao todos efeitos | T3-T7 | **CONCLUIDA** (R6) |
| S007-T9 | Frontend | VantagensConfig secao de efeitos | T8 | **CONCLUIDA** (R7) |
| S007-T10 | Frontend | FormulaEditorEfeito | T9 | **PENDENTE** (afetado por PA-004) |
| S007-T11 | Frontend | DadoUp seletor (progressao visual) | T9 | **CONCLUIDA** (R9, commit `0c5fb29`) |
| S007-T12 | Frontend | UI concessao Insolitus pelo Mestre | T9 | **CONCLUIDA** (R10, commit `d23a3cf`) |

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
| S006-T7 | Frontend | Passo 2: Descricao fisica | S006-T6 | **CONCLUIDA** (R8→R9, commit `dd2677d`) |
| S006-T8 | Frontend | Passo 3: Distribuicao de atributos (17 testes) | S006-T7 | **CONCLUIDA** (R9, commit `dd2677d`) |
| S006-T9 | Frontend | Passo 4: Distribuicao de aptidoes | S006-T5, T6 | **CONCLUIDA** (R10, commit `1895648`) |
| S006-T10 | Frontend | Passo 5: Compra de vantagens iniciais | S006-T5, T6 | **CONCLUIDA** (R10, commit `0702b03`) |
| S006-T11 | Frontend | Passo 6: Revisao e confirmacao | S006-T1, T6 | **PENDENTE** (proximo - R11) |
| S006-T12 | Frontend | Auto-save visual (indicador) | S006-T6 | **DESBLOQUEADO** (bonus R11) |
| S006-T13 | Frontend | Badge "incompleta" na listagem | S006-T1 | **DESBLOQUEADO** (bonus R11) |

### Track D: Spec 005 (Participantes)

| ID | Tipo | Descricao | Dependencia | Status |
|----|------|-----------|-------------|--------|
| S005-P1T1 | Backend | Re-solicitacao strategy Reactivate | Nenhuma | **CONCLUIDA** (R6) |
| S005-P1T2 | Backend | Endpoints faltantes (banir, desbanir, remover, meu-status, filtro) | S005-P1T1 | **CONCLUIDA** (R6) |
| S005-P1T3 | Backend | Testes de integracao completos | S005-P1T1, P1T2 | **CONCLUIDA** (R7) |
| S005-P2T1 | Frontend | API service testes completos | S005-P1T2 | **CONCLUIDA** (R8→R9, commit `d6c3b34`) |
| S005-P2T2 | Frontend | JogoDetail do Mestre (semantica remover/banir) | S005-P2T1 | **CONCLUIDA** (R9, commit `f3637e9`) |
| S005-P2T3 | Frontend | JogosDisponiveis do Jogador (solicitar, status) | S005-P2T1 | **CONCLUIDA** (R10, commit `d2c262c`) |

### Quick Wins Frontend -- 0 bugs restantes (todos corrigidos)

---

## Rodada 10 -- CONCLUIDA [07:00] (4 tasks concluidas, +113F testes, 581B+603F testes)

### Resultado da Rodada 10

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S006-T9) | Wizard Passo 4 Aptidoes | **CONCLUIDO** -- StepAptidoesComponent (dumb), distribuicao de pontos pool de NivelConfig.pontosAptidao, formPasso4 + carregarDadosPasso4 + salvarPasso4 via PUT /fichas/{id}/aptidoes. 22 testes em step-aptidoes.spec.ts. Agente A ficou sem tokens; bugfixes finais aplicados manualmente pelo PM. Commit `1895648`. |
| Agente B (S006-T10) | Wizard Passo 5 Vantagens | **CONCLUIDO** -- StepVantagensComponent (smart, ConfigApiService + FichasApiService), filtro categoria + busca, vantagensAgrupadasPorCategoria computed, estadoBotao (comprar/comprada/sem-pontos/comprando), POST /fichas/{id}/vantagens, GET /fichas/{id}/resumo para saldo, pontosAtualizados output. 40 testes em step-vantagens.component.spec.ts. Commit `0702b03`. |
| Agente C (S005-P2T3) | JogosDisponiveis Jogador | **CONCLUIDO** -- statusPorJogo signal carregado para cada jogo, helpers podeEntrar/podeSolicitar/podeCancelar, badges p-tag por status, solicitarEntrada + cancelarSolicitacao com atualizacao local sem re-fetch. 34 testes em jogos-disponiveis.spec.ts. Commit `d2c262c`. |
| Agente D (S007-T12) | UI Concessao Insolitus Mestre | **CONCLUIDO** -- Botao "Conceder Insolitus" (Mestre only) + dialog com busca, filtra vantagens INSOLITUS via ConfigApiService, tag "Insolitus" + label "Concedida pelo Mestre", botao revogar (trash) para todas vantagens (Mestre), endpoints concederInsolitus + revogarVantagem. 17 testes em ficha-vantagens-tab.spec.ts. Commit `d23a3cf`. |

**Pontos de atencao:**
- Agente A (S006-T9) ficou sem tokens antes de concluir; trabalho foi finalizado e bugfixes aplicados manualmente pelo PM
- `ficha-wizard.component.spec.ts` (61 testes) e `ficha-wizard-passo4.component.spec.ts` (14 testes) tem worker timeout pre-existente (setTimeout 3s nos testes de salvamento) — testes passam mas worker crasha no shutdown. Nao bloqueia build.
- Build: 0 erros TypeScript, apenas budget warning pre-existente (bundle 1.14MB vs limite 1MB)

---

## Rodada 9 -- CONCLUIDA [21:05] (5 tasks concluidas, +31F testes, 581B+490F testes)

### Resultado da Rodada 9

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S005-P2T1) | Participantes API testes | **CONCLUIDO** -- `jogos-api.service.spec.ts` com testes banir/desbanir/remover/meu-status/cancelarSolicitacao. Pendente de commit desde R8, formalizado R9. Commit `d6c3b34`. |
| Agente B (S006-T7) | Wizard Passo 2 Descricao | **CONCLUIDO** -- StepDescricaoComponent, formPasso2, salvarPasso2 via PUT /fichas/{id}. Pendente de commit desde R8, formalizado R9. Commit `dd2677d`. |
| Agente C (S006-T8) | Wizard Passo 3 Atributos | **CONCLUIDO** -- StepAtributosComponent (dumb), UI distribuicao pontos (incremento/decremento, min/max, limitadorAtributo), carregarDadosPasso3 (forkJoin), salvarPasso3 via PUT /fichas/{id}/atributos. FichaAtributoEditavel + AtualizarAtributoDto. 9 testes step + 8 wizard. Commit `dd2677d`. |
| Agente D (S005-P2T2) | JogoDetail Mestre remover/banir | **CONCLUIDO** -- Bug critico corrigido: `removerParticipante()` chamava `banirParticipante()`. Botoes Banir/Desbanir adicionados, filtro por status implementado. Commit `f3637e9`. |
| Agente E (S007-T11) | DadoUp Preview Seletor | **CONCLUIDO** -- Seletor de dado base + preview visual de progressao de dado no EfeitoFormComponent. Commit `0c5fb29`. |

**NOTA:** S005-P2T1 e S006-T7 estavam com implementacao pronta mas sem commit desde R8. Nesta rodada foram commitados (formalizados) junto com as entregas novas S006-T8, S005-P2T2 e S007-T11. Todos os 5 agentes entregaram com sucesso.

---

## Rodada 8 -- CONCLUIDA [15:30] (implementacao de 2 tasks, commits formalizados na R9)

### Resultado da Rodada 8

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S006-T7) | Wizard Passo 2 Descricao | **CONCLUIDO** -- Implementacao pronta R8, commit formalizado R9 (`dd2677d`). |
| Agente B (S005-P2T1) | Participantes API/Service | **CONCLUIDO** -- Implementacao pronta R8, commit formalizado R9 (`d6c3b34`). |

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

## Rodada 10 -- CONCLUIDA [2026-04-06 07:10] (4 tasks, +113F testes, 581B+603F)

### Resultado da Rodada 10

| Agente | Task | Resultado |
|--------|------|-----------|
| Agente A (S006-T9) | Wizard Passo 4 Aptidoes | **CONCLUIDO** -- StepAptidoesComponent (dumb), distribuicao de pontos pool de NivelConfig.pontosAptidao, agrupamento por TipoAptidao. Wizard: formPasso4 + carregarDadosPasso4 (forkJoin) + salvarPasso4 via PUT /fichas/{id}/aptidoes. 22 testes em step-aptidoes.spec.ts. Bugfixes manuais aplicados pelo PM apos exhaust de tokens do agente. Commit `1895648`. |
| Agente B (S006-T10) | Wizard Passo 5 Vantagens | **CONCLUIDO** -- StepVantagensComponent (Smart), filtro por categoria + busca, comprar vantagem via POST /fichas/{id}/vantagens, estadoBotao (comprar/comprada/sem-pontos/comprando), pontosVantagemDisponiveis signal no wizard, idsComprados Set O(1). 40 testes em step-vantagens.component.spec.ts. Commit `0702b03`. |
| Agente C (S005-P2T3) | JogosDisponiveis Jogador | **CONCLUIDO** -- statusPorJogo signal Map, carregarStatusParticipacao em paralelo (404=null), badges p-tag por status (PENDENTE/APROVADO/REJEITADO/BANIDO), botoes Solicitar/Cancelar/Entrar, atualizacao local sem re-fetch. 34 testes. Commit `d2c262c`. |
| Agente D (S007-T12) | UI Concessao Insolitus Mestre | **CONCLUIDO** -- tipoVantagem em VantagemConfig + FichaVantagemResponse. concederInsolitus + revogarVantagem em api+business. FichaVantagensTabComponent: botao "Conceder Insolitus" + dialog busca + tag "Insolitus" + botao revogar (todas vantagens). FichaDetailComponent orquestra via viewChild. 17 testes. Commit `d23a3cf`. |

**Pontos de atencao da Rodada 10:**
- Agente A (S006-T9) ficou sem tokens antes de concluir; trabalho foi finalizado e bugfixes aplicados manualmente pelo PM
- `ficha-wizard.component.spec.ts` (61 testes) e `ficha-wizard-passo4.component.spec.ts` (14 testes) tem worker timeout pre-existente (setTimeout 3s) — testes passam mas worker crasha no shutdown. Nao bloqueia build.
- Build: 0 erros TypeScript, apenas budget warning pre-existente (bundle 1.14MB vs limite 1MB)

---

## Proxima Rodada (Rodada 11) -- Planejamento

> **Foco:** Fechar Sprint 2 finalizando o wizard de criacao de ficha (passo 6 + auto-save + badge).
> S007-T10 (FormulaEditorEfeito) permanece pendente porque depende de PA-004 — sera tratado em rodada separada apos resposta do PO.

### Agente 1 -- Frontend: S006-T11 (Wizard Passo 6 -- Revisao e Confirmacao)

**Task:** Componente dumb mostrando resumo de todos os passos anteriores (identificacao, descricao, atributos, aptidoes, vantagens compradas). Botao "Completar Ficha" que chama PUT /fichas/{id}/completar — endpoint backend ja existente desde S006-T1.
**Critérios de aceitação:**
- Layout em cards/sections, um por passo do wizard
- Edicao via botao "Voltar para passo X" navega para o passo correspondente sem perder dados
- Botao "Completar" desabilitado se algum passo obrigatorio tiver erro
- Apos completar, redirecionar para FichaDetail (rota apropriada Mestre/Jogador)
- Auto-save de rascunho continua funcionando (componente readonly, nao precisa salvar)
- Testes >= 15 (visualizacao de cada secao + interacao botao Completar + redirect)
**Estimativa:** 4-5 horas
**Dependencia:** S006-T6 (concluida) e S006-T1 (backend, concluida)

### Agente 2 -- Frontend: S006-T12 (Auto-save Visual -- Indicador de Salvamento)

**Task:** Indicador visual no header do wizard mostrando estado de salvamento (rascunho salvo, salvando, erro). Usar o signal `EstadoSalvamento` ja presente no FichaWizardComponent.
**Critérios de aceitação:**
- Componente pequeno no canto superior direito do wizard
- Estados: "Salvo", "Salvando...", "Erro ao salvar - tentar novamente"
- Icone correspondente (check, spinner, alert)
- Tooltip com timestamp do ultimo salvamento bem-sucedido
- Testes >= 8
**Estimativa:** 2 horas
**Dependencia:** S006-T6 (concluida) — independente das demais

### Agente 3 -- Frontend: S006-T13 (Badge "Incompleta" na Listagem de Fichas)

**Task:** Adicionar badge p-tag "Incompleta" no FichaCardComponent (ou listagem equivalente) quando `ficha.status === 'INCOMPLETA'`. Click no card de ficha incompleta deve navegar de volta ao wizard com `?fichaId=` para continuar de onde parou.
**Critérios de aceitação:**
- Badge visivel apenas para fichas com status INCOMPLETA
- Click navega para rota do wizard com fichaId
- Cor distinta (warning/secondary) para destacar do estado normal
- Testes >= 5 (renderizacao + click)
**Estimativa:** 1-2 horas
**Dependencia:** S006-T1 (backend status, concluida) — independente

### NAO AGENDADO -- S007-T10 (FormulaEditorEfeito)

**Task:** Editor de formulas customizadas para FORMULA_CUSTOMIZADA no EfeitoFormComponent.
**Bloqueador:** PA-004 (FORMULA_CUSTOMIZADA sem alvo definido). Antes de implementar, precisa de decisao do PO sobre onde o resultado da formula sera aplicado (atributo? aptidao? bonus arbitrario?).
**Acao do PM:** Escalar PA-004 ao PO antes de iniciar Rodada 12. Se PO bloquear definitivamente, considerar mockar com aviso "em breve" para fechar Sprint 2.

### Plano Anti-Conflito da Rodada 11

| Agente | Pacotes/arquivos exclusivos | NAO TOCAR |
|--------|-------------------------------|-----------|
| 1 (S006-T11) | `steps/step-revisao/`, `ficha-wizard.component.ts` (metodo `completar()`) | nenhum auto-save, nenhum indicador |
| 2 (S006-T12) | novo componente `wizard-save-indicator/`, `ficha-wizard.component.html` (header) | passos individuais |
| 3 (S006-T13) | `ficha-card.component.*`, listagens de ficha | wizard inteiro |

Conflitos minimos: apenas o template do wizard pode ser tocado por agentes 1 e 2 — coordenar via merge sequencial (Agente 1 primeiro, Agente 2 depois).

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

RODADA 8 -- CONCLUIDA [15:30] (implementacoes prontas, commits formalizados R9):
  [CONCLUIDO] S006-T7: wizard passo 2 descricao ............. commit dd2677d
  [CONCLUIDO] S005-P2T1: participantes frontend API/service . commit d6c3b34

RODADA 9 -- CONCLUIDA [21:05] (5 tasks, +31F testes, 581B+490F):
  [CONCLUIDO] S005-P2T1: participantes API testes ........... commit d6c3b34
  [CONCLUIDO] S006-T7: wizard passo 2 descricao ............. commit dd2677d
  [CONCLUIDO] S006-T8: wizard passo 3 atributos ............. commit dd2677d
  [CONCLUIDO] S005-P2T2: JogoDetail Mestre remover/banir .... commit f3637e9
  [CONCLUIDO] S007-T11: DadoUp seletor progressao ........... commit 0c5fb29

RODADA 10 -- CONCLUIDA [07:10] (4 tasks, +113F testes, 581B+603F):
  [CONCLUIDO] S006-T9: wizard passo 4 aptidoes .............. commit 1895648
  [CONCLUIDO] S006-T10: wizard passo 5 vantagens ............ commit 0702b03
  [CONCLUIDO] S005-P2T3: JogosDisponiveis jogador ........... commit d2c262c
  [CONCLUIDO] S007-T12: UI concessao Insolitus .............. commit d23a3cf

RODADA 11 (proxima):
  S006-T11 (wizard passo 6 revisao + completar) ............. 4-5h
  S006-T12 (auto-save visual indicator) ..................... 2h
  S006-T13 (badge incompleta listagem) ...................... 1-2h

NAO AGENDADO:
  [Frontend] S007-T10 (FormulaEditorEfeito) ................. [BLOQUEADO PA-004]
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

*Produzido por: PM/Scrum Orchestrator | 2026-04-06 [07:10] (rodada 10 concluida: 33/35, 581B+603F testes)*
*Proxima revisao: inicio da rodada 11*
