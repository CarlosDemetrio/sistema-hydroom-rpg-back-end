# T2 — Testes de Integracao para FichaCalculationService

> Fase: Backend Testes | Dependencias: T1 (JaCoCo setup) | Bloqueia: Nenhuma
> Estimativa: 4–6 horas

---

## Objetivo

Criar testes de integracao exaustivos para o `FichaCalculationService`, cobrindo os 8 tipos de `VantagemEfeito`, a ordem de calculo, idempotencia e edge cases. Este e o service mais critico do sistema — erros aqui produzem fichas com valores matematicamente incorretos.

---

## Contexto

O `FichaCalculationService` recalcula todos os valores derivados de uma ficha. A Spec 007 T8 ja define cenarios de teste para os 8 tipos de efeito. Esta task (014-T2) complementa com:
- Cenarios que T8 nao cobre (combinacoes de multiplos tipos simultaneos)
- Testes de regressao para bugs corrigidos em T0 (ClasseBonus, RacaBonus, etc.)
- Testes de performance (muitas vantagens/efeitos sem N+1)

**Nota:** Se Spec 007 T8 ja estiver implementada quando esta task iniciar, verificar o relatorio JaCoCo para identificar branches nao cobertos e adicionar apenas os testes faltantes. Nao duplicar cenarios.

---

## Arquivo de Teste

`test/java/.../service/FichaCalculationServiceCoverageTest.java`

Usar `@ActiveProfiles("test")`, H2 in-memory, `@Transactional`.

---

## Cenarios Obrigatorios

### Grupo 1 — Calculo Base (regressao T0)

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-REG-01 | ClasseBonus aplicado corretamente | `FichaBonus.classe == ClasseBonus.valorPorNivel * ficha.nivel` |
| TC-REG-02 | ClasseAptidaoBonus aplicado corretamente | `FichaAptidao.classe == ClasseAptidaoBonus.bonus` |
| TC-REG-03 | RacaBonusAtributo aplicado (inclusive negativo) | `FichaAtributo.outros` inclui bonus de raca |
| TC-REG-04 | Nivel recalculado ao ganhar XP | `ficha.nivel` atualizado conforme NivelConfig |
| TC-REG-05 | FichaAmeaca inclui nivel no total | `ameaca.total` inclui `ficha.nivel` |
| TC-REG-06 | FichaVida inclui vigorTotal e nivel | `vida.total` inclui `vigorTotal + nivel` |

### Grupo 2 — Combinacoes de Multiplos Efeitos

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-COMBO-01 | 3 vantagens com BONUS_ATRIBUTO no mesmo atributo | Valores acumulam corretamente |
| TC-COMBO-02 | BONUS_ATRIBUTO + BONUS_DERIVADO (cascata) | Bonus derivado usa atributo ja atualizado |
| TC-COMBO-03 | BONUS_VIDA + BONUS_VIDA_MEMBRO | Pool global aumenta + membro tem bonus direto |
| TC-COMBO-04 | Todos os 8 tipos simultaneos em uma ficha | Todos aplicados sem interferencia |
| TC-COMBO-05 | Vantagem nivel 1 + Insolitus nivel 3 | Ambos contribuem corretamente |

### Grupo 3 — Edge Cases

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-EDGE-01 | Ficha sem vantagens | Todos os campos de contribuicao zerados |
| TC-EDGE-02 | Vantagem com nivel 0 | Efeito nao aplicado |
| TC-EDGE-03 | VantagemEfeito soft-deleted | Efeito ignorado |
| TC-EDGE-04 | FichaVantagem soft-deleted | Vantagem inteira ignorada |
| TC-EDGE-05 | Ficha nivel 1 sem raca nem classe | Bonus de classe e raca = 0 |
| TC-EDGE-06 | Recalcular 3x seguidas (idempotencia) | Valores identicos nas 3 chamadas |

### Grupo 4 — Performance (N+1)

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-PERF-01 | Ficha com 20 vantagens e 50 efeitos | Sem N+1 (verificar log de queries) |

---

## Estimativa de Testes

- Grupo 1: 6 testes
- Grupo 2: 5 testes
- Grupo 3: 6 testes
- Grupo 4: 1 teste
- **Total: ~18 testes**

---

## Criterios de Aceitacao

- [ ] Todos os cenarios dos 4 grupos passando
- [ ] `FichaCalculationService` com >= 90% branch coverage no relatorio JaCoCo
- [ ] Nenhum cenario duplicado com Spec 007 T8
- [ ] `./mvnw test` passa com todos os testes existentes + novos
- [ ] Total de testes backend >= 475 (457 + ~18)
