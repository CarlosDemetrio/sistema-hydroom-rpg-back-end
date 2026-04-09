# F2-T9 — buildGeral() · 5 vantagens

## Objetivo
Preencher `buildGeral()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 25 | VSFE | Saude de Ferro | Aumenta os pontos de vida do personagem. Requer Vigor 3+. | 4 | 3 | +5 de Vida por nivel | VANTAGEM |
| 26 | VCON | Concentracao | Aumenta os pontos de animus (essência) do personagem. | 4 | 3 | +5 de Animus por nivel | VANTAGEM |
| 27 | VSRQ | Saque Rapido | Permite sacar armas sem gastar Ponto de Ação. Requer Agilidade 10+. | 2 | 3 | Saca armas sem custo de P.A por nivel | VANTAGEM |
| 28 | VAMB | Ambidestria | Remove penalidade de usar mão não dominante. Domínio bilateral de armas e ações. | 1 | 5 | Dominio bilateral sem penalidade para mao nao dominante | VANTAGEM |
| 29 | VMF | Memoria Fotografica | Possibilita memória visual plena de tudo que foi visto. Requer Raciocínio 10+. | 1 | 10 | Memoria visual completa e fotografica | VANTAGEM |

**categoriaNome:** `"Vantagem Geral"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Geral (VSFE, VCON, VSRQ, VAMB, VMF) [Copilot R07 T9]
```
