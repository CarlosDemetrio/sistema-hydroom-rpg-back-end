---
name: Auditoria Integração Config → Ficha
description: Mapeamento completo de como cada configuração contribui para a ficha; 9 gaps críticos identificados; ordem de cálculo para Spec 007
type: project
---

Auditoria completa produzida em 2026-04-03. Documento em `docs/analises/INTEGRACAO-CONFIG-FICHA.md`.

**Por que:** PO percebeu que o cálculo de pontos de atributos não estava bem documentado — auditoria revelou que o problema era sistemático.

**Status do FichaCalculationService (pré-Spec 007):**
- IMPLEMENTADO: FichaAtributo.total, FichaAtributo.impeto, FichaBonus.base, FichaBonus.total, FichaVida.vidaTotal, FichaVidaMembro.vida, FichaEssencia.total, FichaAmeaca.total
- GAP CRITICO: FichaBonus.classe (ClasseBonus nunca calculado)
- GAP CRITICO: FichaAptidao.classe (ClasseAptidaoBonus nunca calculado)
- GAP CRITICO: FichaAtributo.outros não inicializado com RacaBonusAtributo
- GAP CRITICO: VantagemEfeito nunca processado (GAP-03)
- GAP CRITICO: FichaResumoResponse sem pontosAtributoDisponiveis, pontosAptidaoDisponiveis, pontosVantagemDisponiveis
- GAP CRITICO: nivel não recalculado ao mudar XP

**Mudanças de schema necessárias (Spec 007):**
- `FichaAptidao` precisa do campo `outros` (BONUS_APTIDAO sem destino atualmente)
- `FichaVidaMembro` precisa do campo `bonus_vantagens` (BONUS_VIDA_MEMBRO sem destino)

**Regras de negócio implícitas descobertas:**
- ClasseBonus.valorPorNivel multiplica pelo NIVEL DA FICHA (não da classe)
- ClasseAptidaoBonus.bonus é FIXO (não escala por nível) — contraste com ClasseBonus
- RacaBonusAtributo vai para FichaAtributo.outros e pode ser NEGATIVO
- FichaVida.vt é a soma de TODOS os VantagemEfeito BONUS_VIDA ativos
- FichaAtributo.outros acumula raça + vantagens — deve ser ZERADO e recalculado a cada recalcular()
- Ponto gastos de atributo = SUM(FichaAtributo.nivel) — não FichaAtributo.base
- FichaAmeaca.recalcularTotal() está ERRADO (não inclui nivel) — FichaCalculationService está correto
- FichaVida.recalcularTotal() está ERRADO (não inclui vigorTotal nem nivel)
- VIG/SAB hardcoded em FichaCalculationService — viola princípio de configurabilidade

**Ordem de cálculo correta (10 passos):**
1. Reset campos derivados + aplicar raça (outros) + classe (classe)
2. [Spec 007] Aplicar VantagemEfeito (switch 8 tipos)
3. Recalcular totais de atributos (total = base + nivel + outros)
4. Build mapa de variáveis (abreviacao → total)
5. Recalcular totais de bônus (base via formula + vantagens + classe + ...)
6. Recalcular totais de aptidões (base + sorte + classe + outros)
7. Recalcular vida (vigorTotal + nivel + vt + renascimentos + outros)
8. Recalcular vida por membro (floor(vidaTotal * porcentagem) + bonusVantagens)
9. Recalcular essência (floor((VIG+SAB)/2) + nivel + renascimentos + vantagens + outros)
10. Recalcular ameaça (nivel + itens + titulos + renascimentos + outros)

**Pontos em aberto — status atualizado 2026-04-03:**
- PA-PONTOS-01: RESOLVIDO (Q14). Raca/ClassePersonagem NÃO têm campos de pontos extras por nível. Documentado como GAP-PONTOS-CONFIG (pós-MVP). MVP usa apenas NivelConfig como fonte de pontos.
- PA-APTIDAO-01: RESOLVIDO (Q15). pontosAptidaoGastos = SUM(FichaAptidao.base). Sem distinção criação vs level up.
- PA-FORMULA-01: RESOLVIDO (Q16). VIG/SAB hardcoded ACEITO para MVP. Revisitar pós-MVP com campo `papel` em AtributoConfig.
- PA-RACA-01: Em aberto — FichaAtributo.outros zerado e recalculado vs campos separados por fonte.

**GAP-PONTOS-CONFIG (novo — Q14 2026-04-03):**
- ClassePersonagem e Raca não têm sub-recursos para liberar pontos extras por nível.
- Funcionalidade necessária: ClassePontosNivelConfig e RacaPontosNivelConfig (nivel, pontosAtributo, pontosAptidao, pontosVantagem).
- Impacto no cálculo T5: MVP correto ao usar só NivelConfig. Quando resolvido, T5 precisa ser revisado.

**How to apply:** Usar esta memória ao refinar Spec 007 (motor de cálculos), Spec 012 T5 (pontos disponíveis) e ao criar tasks de implementação para garantir que todos os 10 passos de cálculo estejam cobertos.
