# P2-T1 — Integração no FichaService + Nível Automático

**Fase:** 2 — Integração
**Complexidade:** 🟡 Média
**Depende de:** P1-T1, P1-T2, P1-T3
**Bloqueia:** P3-T1

## Objetivo

Injetar FichaCalculationService no FichaService para que os cálculos ocorram automaticamente ao salvar.

## Checklist

### 1. FichaService — integrar cálculos

- [ ] Injetar `FichaCalculationService fichaCalculationService`
- [ ] Em `criar()`: após salvar Ficha e sub-registros, chamar `recalcular(ficha)` antes de retornar
- [ ] Em `atualizar()`: chamar `recalcular(ficha)` antes de persistir
- [ ] Método privado `recalcular(Ficha ficha)`:
  - Carregar todos os sub-registros via repositories
  - Identificar FichaAtributo de VIG e SAB por atributoConfig.abreviacao
  - Chamar recalcularAtributos, recalcularBonus, recalcularEstado em ordem
  - Salvar todos os sub-registros atualizados

### 2. Nível automático por XP

- [ ] Método `calcularNivelPorXp(Long jogoId, int xp)` → NivelConfig:
  - `NivelConfigRepository.findTopByJogoIdAndXpNecessariaLessThanEqualOrderByXpNecessariaDesc(jogoId, xp)`
  - Retorna NivelConfig com maior xpNecessaria ≤ xp atual
- [ ] Em `atualizar()`: quando `ficha.xp` muda, chamar `calcularNivelPorXp` e atualizar `ficha.nivel`

## Arquivos afetados
- `service/FichaService.java` (MODIFICAR — integrar cálculos)
- Sem novos arquivos

## Verificações de aceitação
- [ ] Criar Ficha → FichaAtributo.total calculado após criar
- [ ] Atualizar XP → nivel atualizado automaticamente conforme NivelConfig
- [ ] `./mvnw test` passa
