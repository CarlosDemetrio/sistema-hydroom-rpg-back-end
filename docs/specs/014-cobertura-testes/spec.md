# Spec 014 — Cobertura de Testes

> Spec: `014-cobertura-testes`
> Epic: Qualidade e Sustentabilidade
> Status: BACKLOG — executar APOS todas as specs funcionais (005-012) estarem implementadas
> Depende de: Specs 005-012 (codigo estavel para medir cobertura)
> Bloqueia: Nenhuma spec funcional

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O projeto tem 457+ testes backend e 271+ testes frontend, mas a cobertura de branch nao e mensurada. Nao ha garantia de que o codigo critico (motor de calculos, validacoes de seguranca, regras de negocio) esta adequadamente coberto. Sem metricas de cobertura, e impossivel saber se novas features introduzem codigo nao testado.

**Estado atual:**
- Backend: 457 testes passando (H2 in-memory), padrao 80% integracao / 20% unitario
- Frontend: 271 testes passando (Vitest), cobertura desconhecida
- Nenhum relatorio de cobertura gerado
- Nenhum threshold que falhe o build

**Objetivo:**
1. Backend: **75%+ branch coverage** mensurado via JaCoCo, com threshold no build
2. Frontend: cobertura mensurada e relatada, gaps identificados
3. Testes adicionais para services criticos sem cobertura adequada

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Backend Dev | — | Configura JaCoCo, escreve testes faltantes |
| Frontend Dev | — | Configura Vitest coverage, escreve testes faltantes |
| CI/CD | — | Executa cobertura no pipeline, falha build se abaixo do threshold |

---

## 3. Escopo

### 3.1 Backend — JaCoCo (T1)

Configurar o plugin JaCoCo no `pom.xml` com:
- Geracao de relatorio HTML em `target/site/jacoco/`
- Threshold de 75% de branch coverage que **falha o build** se nao atingido
- Exclusoes: classes geradas (MapStruct, Lombok), DTOs (records), configuracao Spring

### 3.2 Backend — FichaCalculationService (T2)

Service mais critico do sistema. Responsavel por recalcular TODOS os valores de uma ficha. Deve ter cobertura exaustiva:
- Cada um dos 8 tipos de VantagemEfeito
- Ordem de calculo (dependencias entre etapas)
- Idempotencia (recalcular N vezes = mesmo resultado)
- Edge cases: lista vazia, soft-deleted, nivel 0, overflow

### 3.3 Backend — FormulaEvaluatorService e GameConfigInitializerService (T3)

- `FormulaEvaluatorService`: formulas validas, invalidas, variaveis faltantes, divisao por zero
- `GameConfigInitializerService`: verificar que todas as configs padrao sao criadas corretamente
- `DefaultGameConfigProviderImpl`: verificar coerencia dos defaults (nomes, abreviacoes, valores)

### 3.4 Backend — DefaultGameConfigProviderImpl (T4)

Provider que retorna as configuracoes padrao de um novo jogo. Verificar:
- Todas as 13+ configuracoes sao retornadas
- Abreviacoes nao duplicadas entre configs
- Valores dentro dos ranges validos (min/max)
- Formulas default sao validas (parseiam sem erro)

### 3.5 Frontend — Vitest Coverage (T5)

- Configurar `@vitest/coverage-v8` ou `@vitest/coverage-istanbul`
- Gerar relatorio HTML em `coverage/`
- Identificar componentes com 0% de cobertura
- Adicionar `coverage/` ao `.gitignore` (nao commitar relatorios)

### 3.6 Frontend — Testes para Componentes Sem Cobertura (T6)

Adicionar testes para componentes criticos identificados em T5:
- `ficha-header` (barras de vida/essencia, estado da ficha)
- `ficha-vantagens-tab` (pontos, categorias, pre-requisitos)
- Wizard steps (validacao de campos, navegacao entre passos)
- Signal stores (estado derivado, loading/error states)

---

## 4. O que NAO esta no Escopo

- Cobertura de 100% — o objetivo e 75%+ branch coverage, nao perfeicao
- Testes E2E (Cypress/Playwright) — fora do escopo deste projeto por ora
- Testes de performance/carga
- Mutation testing

---

## 5. Metricas Target

| Area | Metrica | Target | Ferramenta |
|------|---------|--------|-----------|
| Backend | Branch coverage | >= 75% | JaCoCo |
| Backend | Testes totais | >= 500 (atualmente 457) | Maven Surefire |
| Frontend | Branch coverage | Mensurado (sem threshold inicial) | Vitest + v8/istanbul |
| Frontend | Testes totais | >= 300 (atualmente 271) | Vitest |

---

## 6. Criterios de Aceitacao Globais

- [ ] JaCoCo configurado e gerando relatorio HTML no backend
- [ ] Build backend falha se branch coverage < 75%
- [ ] FichaCalculationService com cobertura >= 90% de branches
- [ ] FormulaEvaluatorService com cobertura >= 85% de branches
- [ ] Vitest coverage configurado e gerando relatorio HTML no frontend
- [ ] Componentes criticos com pelo menos 1 teste por fluxo principal
- [ ] Total backend >= 500 testes, total frontend >= 300 testes

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
