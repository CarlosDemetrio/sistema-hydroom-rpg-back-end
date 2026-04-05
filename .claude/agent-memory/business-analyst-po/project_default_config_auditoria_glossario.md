---
name: Auditoria DefaultGameConfigProviderImpl vs Glossário
description: 7 divergências identificadas entre o provider de config padrão e o glossário Klayrah; 6 perguntas críticas para o PO (Q-DC-06 a Q-DC-11)
type: project
---

Auditoria realizada em 2026-04-03 cruzando `DefaultGameConfigProviderImpl.java` com `docs/glossario/`.
Resultado completo em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`, seção 8.

## Divergências críticas

**DIV-01 — Typo no nome da classe (código):** linha 185 do provider tem `"Necromance"` — correto é `"Necromante"` (glossário 02, linha 91). Correção trivial.

**DIV-02 — Índole (ALTA):** Código tem 9 alinhamentos D&D (Ordeiro Bondoso etc.). Glossário define 3 valores simples: Bom, Mau, Neutro. Design diferente — aguarda Q-DC-06.

**DIV-03 — Presença (ALTA):** Código tem escala de intensidade (Insignificante→Dominante). Glossário define postura ética (Bom/Leal/Caótico/Neutro). Conceitos completamente diferentes — aguarda Q-DC-07.

**DIV-04 — Gênero (BAIXA):** Glossário tem "Outro", código tem "Não-Binário" + "Prefiro não informar". Provável que o código seja mais atual — glossário precisa de update.

**DIV-05 — Membro "Sangue" ausente:** Glossário declara 7 membros com "Sangue" (100% vida, venenos/hemorragias). Provider tem 6 membros, omite Sangue. Correção: adicionar `MembroCorpoConfigDTO.of("Sangue", new BigDecimal("1.00"), 7)`.

**DIV-06 — CRITICA: Cabeça 25% vs 75%:** Provider linha 277: `0.25` (25%). Glossário `03-termos-dominio.md` linha 44: explicitamente "Cabeça = 75% da vida total". Diferença de 3x. Deve ser respondida ANTES de Spec 006. Q-DC-09.

**DIV-07 — Vantagens genéricas vs canônicas (ALTA):** As 11 vantagens no provider (Fortitude, Força Aprimorada etc.) não correspondem às vantagens canônicas Klayrah documentadas no glossário (TCO, TCD, TCE, TM, CFM). Q-DC-10 aguarda decisão do PO.

## Ausências do provider (já documentadas em seção 3, confirmadas pelo glossário)

- BonusConfig (6 bônus: B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio)
- CategoriaVantagem (8 categorias canônicas)
- Membro "Sangue"
- PontosVantagemConfig

## Conflito de fórmula B.B.A e B.B.M

Glossário `03-termos-dominio.md` linhas 25-26:
- B.B.A = `(FOR + AGI) / 3`
- B.B.M = `(SAB + INT) / 3`

Seção 3.1 do doc de auditoria propõe `nivel/5` para ambos.
Q-DC-11: qual fórmula correta?

**Why:** Divergências entre código e glossário podem causar inconsistências mecânicas graves quando Spec 006 (fichas) for implementada.
**How to apply:** Ao revisar Spec 006 ou 007, checar estas divergências antes de aceitar cálculos de membros do corpo, bônus ou índole/presença.
