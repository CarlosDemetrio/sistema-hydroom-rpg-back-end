# Copilot R05 — Fix FichaPreviewService (overload deprecated + aptidaoBase)

> Data: 2026-04-08
> Branch: `main`
> Base: 743 testes backend (pre-rodada)
> Status: CONCLUIDA

---

## Contexto

Bug pré-existente identificado durante revisão de código pós-Rodada 15. O `FichaPreviewService.simular()` chamava a sobrecarga **deprecated** de `FichaCalculationService.recalcular()` (7 params), introduzida em Spec-007-T0 como shim retrocompatível. Isso fazia com que o preview ignorasse aptidões, bônus de raça/classe e efeitos de vantagens — retornando valores incorretos para fichas com raça, classe ou vantagens ativas.

O bug não estava registrado em nenhum arquivo de tracking.

---

## Tasks Executadas

### [Fix] FichaPreviewService — overload completo + aptidaoBase

**Agente:** GitHub Copilot CLI (direto)
**Commit:** `46b92d8`
**Arquivos modificados:** 3

**`FichaPreviewService.java`:**
- Adicionados 6 repositórios injetados: `FichaVantagemRepository`, `FichaProspeccaoRepository`, `DadoProspeccaoConfigRepository`, `ClasseBonusRepository`, `ClasseAptidaoBonusRepository`, `RacaBonusAtributoRepository`
- `simular()` agora carrega: `aptidoes` (com config), `vantagens` (**com efeitos** — `findByFichaIdWithEfeitos`), `prospeccoes`, `dadosOrdenados`, `racaBonusAtributos`, `classeBonus`, `classeAptidaoBonus`
- Adicionados clones de `FichaAptidao` e `FichaProspeccao` (ambas mutadas pelo cálculo — `setOutros`, `setClasse`, `setDadoDisponivel`)
- `request.aptidaoBase()` agora aplicado às `aptidoesSimuladas` (era silenciosamente ignorado apesar de estar no contrato do DTO)
- Chamada `recalcular()` migrada para o overload completo de 14 params

**`FichaCalculationService.java`:**
- Removida sobrecarga deprecated `@Deprecated(since = "Spec-007-T0", forRemoval = true)` (7 params) — `FichaPreviewService` era o único caller

**`FichaCalculationServiceTest.java`:**
- Duas chamadas `recalcular()` no teste migradas para assinatura completa (14 params com `List.of()` para campos não relevantes ao cenário)

**Validação:**
- `./mvnw test` → 743 testes, 0 falhas, 3 skipped (pre-existentes), BUILD SUCCESS

---

## Commits

| Hash | Mensagem | Task |
|------|----------|------|
| `46b92d8` | `fix(preview): FichaPreviewService usa overload completo do recalcular [Copilot R05]` | Fix preview deprecated |

---

## Estado Final

### Backend

| Métrica | Valor |
|---------|-------|
| Testes totais | 743 (0 falhas, 3 skipped) |
| Delta vs base | 0 (sem testes novos — bug de lógica, não de cobertura) |
| HEAD | `46b92d8` |

---

## Pendências / PAs

| ID | Descrição | Bloqueia | Próxima ação |
|----|-----------|---------|--------------|
| PA-R05-01 | `FichaPreviewResponse` não expõe aptidões nem dado de prospecção — contrato incompleto se PO quiser preview completo | Não (decisão de produto) | Tech Lead/PO decidem se ampliam resposta em Spec futura |
| PA-R05-02 | Testes de integração para `FichaPreviewService.simular()` não cobrem cenários com vantagem `BONUS_APTIDAO`, `DADO_UP`, raça/classe com bônus, e aplicação de `aptidaoBase` | Não | Adicionar em rodada futura (pós-RC) |

---

*Rodada Copilot R05 encerrada em 2026-04-08.*
