# Spec 013 — Documentacao Tecnica

> Spec: `013-documentacao-tecnica`
> Epic: Qualidade e Sustentabilidade
> Status: BACKLOG — executar APOS todas as specs funcionais (005-012) estarem implementadas
> Depende de: Specs 005-012 (codigo estavel para documentar)
> Bloqueia: Nenhuma spec funcional

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O projeto possui 457+ testes backend e 271+ testes frontend, 13 CRUDs, motor de calculos, wizard de ficha e multiplas features complexas — mas a documentacao tecnica inline (Javadoc, TSDoc, OpenAPI annotations) e minima ou inexistente. Isso dificulta onboarding de novos desenvolvedores, integracao frontend-backend, e manutencao a longo prazo.

**Documentacao existente:**
- `CLAUDE.md`: instrucoes de codigo para agentes AI (nao substitui Javadoc/TSDoc)
- `docs/backend/*.md`: padroes de arquitetura e convencoes (11 documentos)
- `docs/glossario/*.md`: dominio Klayrah RPG (5 documentos)
- `docs/specs/`: specs de features (005-012)

**O que falta:**
1. Javadoc em metodos publicos de services criticos
2. OpenAPI annotations enriquecidas nos controllers (descricoes reais, exemplos, response codes)
3. Inline comments em logica complexa de negocio
4. TSDoc em services e componentes Angular complexos
5. Documentacao de componentes frontend (README ou Storybook)
6. OpenAPI spec versionada (swagger.json) para contract testing

**Principio:** Documentar o "por que", nao o "o que". O codigo deve ser autoexplicativo para fluxos simples; comments e docs so onde a logica de negocio e nao-obvia.

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Backend Dev | — | Adiciona Javadoc, inline comments, OpenAPI annotations |
| Frontend Dev | — | Adiciona TSDoc, component README |
| DevOps/CI | — | Configura exportacao automatica de swagger.json |

---

## 3. Escopo

### 3.1 Backend — Javadoc (T1)

**Arquivos prioritarios:**
- `FichaCalculationService` — motor de calculos, metodo `recalcular()`, `aplicarEfeitosVantagens()`
- `FichaService` — operacoes CRUD de ficha, wizard flow
- `FormulaEvaluatorService` — avaliacao de formulas exp4j
- `AbstractConfiguracaoService` — CRUD generico para 13+ configuracoes
- `SiglaValidationService` — validacao cross-entity de siglas
- `GameConfigInitializerService` — populacao de configs padrao

**Padrao de Javadoc:**
```java
/**
 * Recalcula todos os valores derivados de uma ficha de personagem.
 *
 * Ordem de calculo:
 * 1. Zera contribuicoes de vantagens
 * 2. Aplica efeitos de vantagens (8 tipos)
 * 3. Recalcula atributos (base + nivel + outros)
 * 4. Recalcula bonus derivados (formulas exp4j)
 * 5. Recalcula estado (vida, essencia, ameaca)
 *
 * @param ficha entidade principal da ficha
 * @param atributos lista de FichaAtributo para recalculo
 * @param vantagens lista de FichaVantagem com efeitos carregados via JOIN FETCH
 * @throws IllegalStateException se a ficha nao tiver atributos configurados
 */
```

### 3.2 Backend — OpenAPI Annotations (T2)

**Situacao atual:** Controllers usam `@Operation` basico ou nenhum. Swagger UI mostra endpoints sem descricoes uteis.

**Target:** Todo endpoint com:
- `@Operation(summary = "...", description = "...")`
- `@ApiResponse` para cada HTTP status retornado (200, 201, 400, 404, 409, 403)
- `@Parameter` com descricoes em path variables
- `@Schema` com exemplos nos DTOs de request/response

**Controllers prioritarios:** FichaController, VantagemEfeitoController, JogoController, JogoParticipanteController

### 3.3 Backend — Inline Comments (T3)

**Areas que exigem inline comments:**
- `FichaCalculationService`: ordem de calculo, por que BONUS_ATRIBUTO deve vir antes de recalcularBonus()
- `VantagemEfeito`: os 8 tipos e por que cada um altera um campo diferente
- `ClasseBonus` vs `ClasseAptidaoBonus` vs `RacaBonusAtributo`: 3 fontes diferentes de bonus, como sao aplicados
- `SiglaValidationService`: por que siglas sao unicas cross-entity
- Formulas `exp4j`: quais variaveis sao injetadas e de onde vem

### 3.4 Frontend — TSDoc (T4)

**Services prioritarios:**
- Signal stores (ficha store, jogo store)
- `FichaApiService` e `FichaBusinessService`
- Services de configuracao (mapear qual endpoint cada metodo chama)

### 3.5 Frontend — Component README (T5)

**Componentes prioritarios:**
- `ficha-header` — barra de vida/essencia, resumo de ficha
- `ficha-vantagens-tab` — tab de vantagens com pontos
- Wizard steps (passo 1 a 6 da criacao de ficha)
- `level-up-dialog` — dialogo de distribuicao de pontos

### 3.6 Shared — OpenAPI Spec Versionada (T6)

- Script para gerar `docs/api/swagger.json` a partir da aplicacao rodando
- swagger.json commitado e versionado no repositorio
- Pode ser usado para contract testing frontend vs backend
- Atualizar `docs/MASTER.md` para apontar para o swagger.json

---

## 4. O que NAO esta no Escopo

- Documentacao de usuario final (manual do jogador/mestre)
- Documentacao de deploy/infra (Docker, CI/CD pipeline)
- Storybook completo com todos os componentes — apenas README por componente
- Refatoracao de codigo — apenas documentacao do codigo existente

---

## 5. Criterios de Aceitacao Globais

- [ ] Todo metodo publico de service critico tem Javadoc com descricao e `@param`/`@return`
- [ ] Todo endpoint no Swagger UI mostra descricao, parametros e responses documentados
- [ ] Inline comments presentes em toda logica de calculo nao-obvia
- [ ] TSDoc nos services Angular principais
- [ ] README.md por componente critico do frontend
- [ ] `docs/api/swagger.json` gerado e commitado
- [ ] `docs/MASTER.md` atualizado com referencia a Spec 013

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
