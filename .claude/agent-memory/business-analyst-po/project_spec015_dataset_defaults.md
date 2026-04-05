---
name: Spec 015 — Dataset defaults ClassePontosConfig e RacaPontosConfig
description: Valores default definidos pelo BA/PO para pontos extras e vantagens pré-definidas por classe e raça, desbloqueando task T5
type: project
---

Dataset completo criado em `docs/specs/015-config-pontos-classe-raca/dataset/dataset-defaults-classe-raca.md` (2026-04-04).

**Why:** Task P2-T5 da Spec 015 precisa popular o DefaultGameConfigProviderImpl com valores default para ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig e RacaVantagemPreDefinida. PA-015-01/02/03 respondidos neste entregável.

**How to apply:** Usar o código Java da seção 7 do dataset como referência direta para implementação. Verificar os 5 pontos em aberto (PA-DV-01 a PA-DV-05) antes de fechar a task T5.

### Resumo dos valores definidos

**Marcos de progressão:** níveis 1, 5, 10, 15, 20, 25, 30, 35 (8 entradas por classe/raça)

**Perfis de classe:**
- Berserker: máximo ptAtrib (+20 ao nível 35) — foco físico extremo
- Guerreiro: alto ptAtrib (+16) + ptVantagem (+8)
- Mago: máximo ptAptidao (+19) — zero ptAtrib
- Ladrao/Negociante: máximo ptAptidao (+20) — zero ptAtrib
- Assassino/Necromante: +4 ptAtrib, +16 ptAptidao
- Sacerdote: equilibrado com mais ptVantagem (+12) — único com vantagens como destaque
- Fauno (Herdeiro): totalmente equilibrado (+8 em tudo)
- Arqueiro/Monge: +8 ptAtrib, +8 ptAptidao

**Perfis de raça:**
- Humano: máximo versatilidade (+12 ptAtrib, +9 ptAptidao, +4 ptVantagem)
- Elfo: aptidões puras (+12 ptAptidao, zero ptAtrib)
- Anão: atributos (+12 ptAtrib, zero ptAptidao)
- Meio-Elfo: equilíbrio moderado (+4 ptAtrib, +8 ptAptidao)

**NivelConfig base (uniforme):** 3 ptAtrib + 1 ptAptidao por nível — os extras são complementares.

### Pontos em aberto para PO
- PA-DV-01: Mago nível 20 — "Pensamento Bifurcado" sem Renascimento? (substituído por T.M até confirmar)
- PA-DV-02: "Fortitude" é VantagemConfig distinta ou alias de "Saúde de Ferro"?
- PA-DV-03: Nome da classe no DefaultProvider: "Fauno" ou "Fauno (Herdeiro)"?
- PA-DV-04: "Golpe Crítico" e "Percepção Apurada" existem como VantagemConfig?
- PA-DV-05: Verificar ortografia exata de "Interceptação" no provider
