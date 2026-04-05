---
name: Spec 007 — Revisão 2026-04-03: Bugs Pré-Existentes e Sequência de 10 Passos
description: Decisões tomadas ao atualizar a Spec 007 com bugs de cálculo pré-existentes, task P0-T0 criada, sequência canônica de 10 passos documentada
type: project
---

Atualização da Spec 007 (`docs/specs/007-vantagem-efeito/`) realizada em 2026-04-03 com base na auditoria `docs/analises/INTEGRACAO-CONFIG-FICHA.md`.

**Bugs documentados na spec (seção 9) e corrigidos em P0-T0:**
- GAP-CALC-01: `FichaBonus.classe` nunca calculado (`ClasseBonus.valorPorNivel * ficha.nivel`)
- GAP-CALC-02: `FichaAptidao.classe` nunca calculado (`ClasseAptidaoBonus.bonus`, valor fixo)
- GAP-CALC-03: `FichaAtributo.outros` sem bônus racial (`RacaBonusAtributo.bonus`, pode ser negativo)
- GAP-CALC-06: Nível não recalculado ao ganhar XP via `FichaService`
- GAP-CALC-07: `FichaAmeaca.recalcularTotal()` ignora `nivel`
- GAP-CALC-08: `FichaVida.recalcularTotal()` ignora `vigorTotal` e `nivel`
- GAP-CALC-09: VIG/SAB hardcoded — aguarda decisão do PO (PA-006), fora do escopo de P0-T0

**Schema changes documentadas (seção 10), executadas em T1:**
- SCHEMA-01: `ficha_aptidoes.outros INTEGER NOT NULL DEFAULT 0` (para BONUS_APTIDAO)
- SCHEMA-02: `ficha_vida_membros.bonus_vantagens INTEGER NOT NULL DEFAULT 0` (para BONUS_VIDA_MEMBRO)

**Sequência canônica de 10 passos (seção 11):**
1. RESET (zerar campos derivados para idempotência)
2. Aplicar RacaBonusAtributo → FichaAtributo.outros
3. Aplicar ClasseBonus → FichaBonus.classe (valorPorNivel × nivel da FICHA)
4. Aplicar ClasseAptidaoBonus → FichaAptidao.classe (fixo, não escala)
5. Processar VantagemEfeito (8 tipos)
6. Recalcular totais de Atributos
7. Recalcular totais de Bônus
8. Recalcular totais de Aptidões
9. Recalcular Vida e Membros
10. Recalcular Essência e Ameaça

**Estrutura de tasks atualizada:**
- T0 criada como P0 bloqueante (arquivo: `tasks/P0-T0-corrigir-bugs-calc-base.md`)
- INDEX.md: total agora 13 tasks, grafo de dependências atualizado, PA-006 adicionado
- T1 atualizada: dependência T0, Passo 0 de migrations explicitado

**Por que:** A auditoria descobriu que o `FichaCalculationService` produzia valores incorretos em fichas com vantagens, bônus de classe e bônus de raça — campos zerados que deveriam ter valores. Estes bugs existem independentemente da integração de VantagemEfeito e precisam ser corrigidos antes.

**How to apply:** Ao estimar ou planejar P0-T0, considerar que envolve queries novas em 3 repositories (ClasseBonus, ClasseAptidaoBonus, NivelConfig), refatoração de `recalcular()`, e 7 cenários de testes de integração.
