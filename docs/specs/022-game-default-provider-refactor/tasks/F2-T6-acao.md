# F2-T6 — buildAcao() · 2 vantagens

## Objetivo
Preencher `buildAcao()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 8 | VAA | Ataque Adicional | Permite realizar um ataque extra após a ação ofensiva principal. Requer Bônus Ofensivo 15+. | 1 | 10 | Um ataque adicional apos a acao ofensiva | VANTAGEM |
| 9 | VAS | Ataque Sentai | Em ataque conjunto força percepção do alvo usando a maior soma dos atacantes. Requer Raciocínio 5+. | 1 | 10 | Ataque conjunto usa maior soma e forca percepcao do alvo | VANTAGEM |

**categoriaNome:** `"Ação"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Acao (VAA, VAS) [Copilot R07 T6]
```
