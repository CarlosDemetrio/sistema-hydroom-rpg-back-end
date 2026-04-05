# Proxima Sessao -- Ponto de Retomada

> Atualizado: 2026-04-04 (consolidacao final pos-rodada 2, sessao 10)
> Branch backend: `feature/009-npc-fichas-mestre`
> Branch frontend: verificar com `git status` no repo frontend

---

## Estado Atual (resumo executivo)

| Metrica | Valor |
|---------|-------|
| Backend testes | **474 passando**, 0 falhas |
| Frontend testes | **359 passando**, 0 falhas |
| Sprint 2 progresso | **6/35 concluidas** (17%) |
| Riscos resolvidos | URG-01, URG-02, GAP-CALC-01..08, S007-T0/T1, S015-T5, QW-Bug3 |
| Caminho critico | S007-T2..T7 **DESBLOQUEADOS** (T1 concluida) |
| Regra de agentes | **1 task por agente** (max); tasks triviais <30min agrupam 2-3 |

---

## Comando de Retomada Rapida

Ao iniciar nova sessao, execute exatamente estes passos:

### Passo 1: PM le contexto

```
Ler docs/HANDOFF-SESSAO.md         -- estado completo, plano anti-conflito, agentes
Ler docs/SPRINT-ATUAL.md           -- sprint tracking detalhado
```

### Passo 2: Verificar testes (sanidade)

```bash
cd /Users/carlosdemetrio/IdeaProjects/ficha-controlador
./mvnw test 2>&1 | tail -5        # deve mostrar 474 testes, 0 falhas

cd /Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end
npx vitest run 2>&1 | tail -10    # deve mostrar 359 testes, 0 falhas
```

### Passo 3: Lancar 4 agentes (Rodada 3)

**IMPORTANTE:** Agente 2 (S007-T3) deve lancar APOS Agente 1 (S007-T2) concluir. Eles tocam o mesmo arquivo.

---

## AGENTE 1 -- S007-T2: BONUS_ATRIBUTO + BONUS_APTIDAO (Backend Caminho Critico)

**Objetivo:** Implementar os dois primeiros tipos de efeito de vantagem no FichaCalculationService.

**System prompt resumido:**
```
Voce e um desenvolvedor backend Java 25 / Spring Boot 4 especializado em logica de calculo de fichas RPG.

TASK: Implementar BONUS_ATRIBUTO e BONUS_APTIDAO no FichaCalculationService.

1. Ler: docs/specs/007-vantagem-efeito/tasks/P1-T2-bonus-atributo-aptidao.md (LEIA INTEIRO)
2. Ler: CLAUDE.md para convencoes do projeto
3. Implementar os 4 passos descritos na task:
   - calcularValorEfeito(efeito, nivelVantagem)
   - zerarContribuicoesVantagens() para idempotencia
   - BONUS_ATRIBUTO no switch de aplicarEfeitosVantagens()
   - BONUS_APTIDAO + adicionar List<FichaAptidao> ao metodo
4. Rodar: ./mvnw test
5. Reportar: quantos testes passam, se houve regressao

Arquivo principal: service/FichaCalculationService.java
NAO TOCAR: model/ClassePontos*, model/RacaPontos*, service/configuracao/Default*, qualquer arquivo frontend

Convencoes:
- @Transactional(readOnly = true) na classe, @Transactional em metodos de escrita
- Lookup por mapa, nao iteracao O(n) dentro do loop
- log.warn para dados faltantes, nunca lancar excecao
- Soft delete: ignorar efeitos com deletedAt != null
```

---

## AGENTE 2 -- S007-T3: BONUS_VIDA + BONUS_ESSENCIA (Backend -- APOS T2)

**Objetivo:** Adicionar dois cases simples ao switch ja criado por T2.

**DEPENDENCIA CRITICA:** Lancar APOS Agente 1 concluir (mesmo arquivo).

**System prompt resumido:**
```
Voce e um desenvolvedor backend Java 25 / Spring Boot 4.

TASK: Implementar BONUS_VIDA e BONUS_ESSENCIA no FichaCalculationService.

1. Ler: docs/specs/007-vantagem-efeito/tasks/P1-T3-bonus-vida-essencia.md
2. Ler: CLAUDE.md
3. Adicionar 2 cases no switch de aplicarEfeitosVantagens():
   - BONUS_VIDA: vida.setVt(vida.getVt() + bonus)
   - BONUS_ESSENCIA: essencia.setVantagens(essencia.getVantagens() + bonus)
4. Rodar: ./mvnw test
5. Reportar: quantos testes passam

Arquivo: service/FichaCalculationService.java
NAO TOCAR: model/ClassePontos*, model/RacaPontos*, frontend
```

---

## AGENTE 3 -- S015-T1: 4 Entidades ConfigPontos (Backend Paralelo)

**Objetivo:** Criar camada de modelo e persistencia para pontos extras por classe/raca.

**System prompt resumido:**
```
Voce e um desenvolvedor backend Java 25 / Spring Boot 4.

TASK: Criar 4 novas entidades, repos, DTOs, mappers para ConfigPontos Classe/Raca.

1. Ler: docs/specs/015-config-pontos-classe-raca/tasks/P1-T1-entidades-config-pontos.md (LEIA INTEIRO)
2. Ler: CLAUDE.md
3. Criar:
   - 4 entidades (ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig, RacaVantagemPreDefinida)
   - 4 repositories
   - 8 DTOs (4 request + 4 response) como records
   - 4 mappers MapStruct
   - Adicionar Set<> em ClassePersonagem.java e Raca.java
4. Rodar: ./mvnw test
5. Reportar: quantos testes passam, se H2 criou tabelas

Convencoes:
- Lombok: @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
- Usar Set<> (nao List<>) para colecoes OneToMany
- FetchType.LAZY em todos os @ManyToOne
- @SQLRestriction("deleted_at IS NULL")
- MapStruct: NullValuePropertyMappingStrategy.IGNORE no update

NAO TOCAR: service/FichaCalculation*, qualquer arquivo frontend
```

---

## AGENTE 4 -- QW-Bug1 + QW-Bug2: Barras e Pontos Hardcoded (Frontend)

**Objetivo:** Corrigir 2 bugs de valores hardcoded na tela de ficha.

**System prompt resumido:**
```
Voce e um desenvolvedor Angular 21 / PrimeNG 21 especializado em signals e componentes reativas.

TASK: Corrigir 2 bugs de valores hardcoded.

1. QW-Bug1: ficha-header.component.ts L82/95
   - Barras vida/essencia: [value]="100" deve usar ficha.vida.total e ficha.essencia.total
   - Se dados nao disponiveis, usar 0 ou ocultar barra

2. QW-Bug2: ficha-vantagens-tab.component.ts L107
   - Pontos vantagem: hardcoded 0 deve usar valor real ou mostrar traco "--"

3. Rodar: npx vitest run
4. Reportar: quantos testes passam

Convencoes Angular:
- inject() para DI, signal()/computed()/model()/input()/output()
- @if/@for (nunca *ngIf/*ngFor)
- Testes com Vitest: vi.fn(), @testing-library/angular

NAO TOCAR: qualquer arquivo backend
```

---

## Apos Rodada 3: Proximos Alvos

**Backend (rodada 4, podem rodar em paralelo entre si):**
- S007-T4: BONUS_DERIVADO + BONUS_VIDA_MEMBRO (2-3h)
- S007-T5: DADO_UP (1-2h)
- S015-T2: CRUD endpoints sub-recursos (3-4h)
- S006-T1: Campo status + endpoint /completar (2-3h)

**Backend (rodada 5):**
- S007-T7: Insolitus + endpoint concessao (3-4h)
- S005-P1T1: Re-solicitacao constraint (2-3h)
- S006-T2: Validacao RacaClassePermitida (2-3h)
- S006-T5: pontosDisponiveis no response (2-3h)

**Frontend (apos backend correspondente):**
- S007-T9-T12 (VantagemEfeito UI -- apos T8)
- S006-T6-T13 (Wizard -- apos T1/T5 backend)

---

## Decisao Arquitetural Pendente

**FichaAptidao.classe: sobrescrever vs somar**

Contexto: T0 implementou sobrescrita. Campo `outros` existe para ajustes manuais.
Recomendacao PM: Opcao A (sobrescrever). Validar com Tech Lead.

---

## Arquivos-chave para ler antes de comecar

| Arquivo | Por que ler |
|---------|-------------|
| `docs/HANDOFF-SESSAO.md` | **LEIA PRIMEIRO** -- estado completo, plano anti-conflito, 4 agentes |
| `docs/MASTER.md` | Indice mestre atualizado |
| `docs/SPRINT-ATUAL.md` | Sprint 2 tracking com 6/35 concluidas |
| `docs/specs/007-vantagem-efeito/tasks/P1-T2-bonus-atributo-aptidao.md` | Task do Agente 1 |
| `docs/specs/007-vantagem-efeito/tasks/P1-T3-bonus-vida-essencia.md` | Task do Agente 2 |
| `docs/specs/015-config-pontos-classe-raca/tasks/P1-T1-entidades-config-pontos.md` | Task do Agente 3 |
| `docs/gaps/PERGUNTAS-PENDENTES-PO.md` | Perguntas abertas |

---

*Atualizado: 2026-04-04 (consolidacao final pos-rodada 2) | PM/Scrum Master*
