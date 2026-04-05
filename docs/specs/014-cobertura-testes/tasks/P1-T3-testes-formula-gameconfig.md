# T3 — Testes para FormulaEvaluatorService e GameConfigInitializerService

> Fase: Backend Testes | Dependencias: T1 (JaCoCo setup) | Bloqueia: Nenhuma
> Estimativa: 2–3 horas

---

## Objetivo

Criar testes de integracao e unitarios para `FormulaEvaluatorService` (avaliacao de formulas matematicas via exp4j) e `GameConfigInitializerService` (populacao de configs padrao ao criar jogo).

---

## Parte 1 — FormulaEvaluatorService

### Arquivo de Teste

`test/java/.../service/FormulaEvaluatorServiceTest.java`

### Cenarios

| Cenario | Formula | Variaveis | Resultado esperado |
|---------|---------|-----------|-------------------|
| TC-F-01 | `"FOR + AGI"` | FOR=15, AGI=12 | 27 |
| TC-F-02 | `"floor(VIG / 2)"` | VIG=21 | 10 |
| TC-F-03 | `"nivel * 2 + base"` | nivel=5, base=10 | 20 |
| TC-F-04 | `"total"` | total=100 | 100 |
| TC-F-05 | `""` (vazia) | — | Excecao ou 0 |
| TC-F-06 | `"INVALIDA +++"` | — | Excecao tratada |
| TC-F-07 | `"FOR / 0"` | FOR=10 | Infinity ou excecao tratada |
| TC-F-08 | `"nivel_vantagem * valor_por_nivel + valor_fixo"` | nivel_vantagem=3, valor_por_nivel=2, valor_fixo=5 | 11 |
| TC-F-09 | Variavel inexistente `"XYZ + 1"` | — | Excecao tratada |
| TC-F-10 | `isValid()` com formula valida | `"FOR + 1"`, variaveis={"FOR"} | true |
| TC-F-11 | `isValid()` com formula invalida | `"+++"`, variaveis={"FOR"} | false |
| TC-F-12 | Formula com todas as abreviacoes padrao | FOR, AGI, VIG, SAB, INT, INTU, AST | Resultado correto |

### Tipo de teste: UNITARIO

`FormulaEvaluatorService` nao depende de repositorios — usa apenas exp4j. Testes unitarios sao apropriados e rapidos.

---

## Parte 2 — GameConfigInitializerService

### Arquivo de Teste

`test/java/.../service/GameConfigInitializerServiceIntegrationTest.java`

### Cenarios

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-G-01 | Inicializar jogo cria todas as configs padrao | Verificar que cada tipo de config tem pelo menos 1 registro |
| TC-G-02 | Inicializar jogo com configs ja existentes nao duplica | Chamar 2x, verificar que nao ha duplicatas |
| TC-G-03 | Configs padrao tem nomes validos (nao vazios) | Iterar todas as configs e verificar nome != null/blank |
| TC-G-04 | Configs padrao tem ordemExibicao sequencial | Verificar que ordemExibicao comeca em 1 e incrementa |
| TC-G-05 | AtributoConfig padrao tem abreviacoes unicas | Verificar que nao ha abreviacoes duplicadas |
| TC-G-06 | Formulas padrao sao validas | Para cada config com formula, validar via FormulaEvaluatorService.isValid() |

### Tipo de teste: INTEGRACAO

Requer Spring context, repositorios e banco H2 para verificar persistencia real.

---

## Criterios de Aceitacao

- [ ] 12 testes para FormulaEvaluatorService cobrindo formulas validas, invalidas e edge cases
- [ ] 6 testes para GameConfigInitializerService cobrindo inicializacao e idempotencia
- [ ] `FormulaEvaluatorService` com >= 85% branch coverage no relatorio JaCoCo
- [ ] `GameConfigInitializerService` com >= 75% branch coverage
- [ ] `./mvnw test` passa com todos os testes existentes + novos (~18 novos)
