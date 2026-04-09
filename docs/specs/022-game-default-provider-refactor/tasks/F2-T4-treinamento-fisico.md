40cara# F2-T4 — buildTreinamentoFisico() · 3 vantagens

## Objetivo
Preencher `buildTreinamentoFisico()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 1 | VTCO | Treinamento em Combate Ofensivo | Treinamento especializado em técnicas ofensivas de combate físico. O dado é elevado até D10 onde um novo dado se inicia no D3. | 10 | 4 | +1 B.B.A e 1 dado de dano (D3→D.UP) por nivel | VANTAGEM |
| 2 | VTCD | Treinamento em Combate Defensivo | Treinamento especializado em técnicas defensivas. Os dados geram RD natural resistente a danos por contusão. | 10 | 4 | +1 Bloqueio e 1 dado de RD natural (D3→D.UP) por nivel | VANTAGEM |
| 3 | VTCE | Treinamento em Combate Evasivo | Treinamento especializado em evasão. Recebe 2 de bônus de Reflexo por nível ao invés de 1. | 10 | 2 | +2 Reflexo por nivel | VANTAGEM |

**categoriaNome:** `"Treinamento Físico"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Treinamento Fisico (VTCO, VTCD, VTCE) [Copilot R07 T4]
```
