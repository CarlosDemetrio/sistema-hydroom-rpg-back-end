# F2-T5 — buildTreinamentoMental() · 4 vantagens

## Objetivo
Preencher `buildTreinamentoMental()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 4 | VTM | Treinamento Magico | Treinamento especializado em técnicas mágicas. O dado é elevado até D10 onde um novo dado se inicia no D3. | 10 | 4 | +1 B.B.M e 1 dado de dano magico (D3→D.UP) por nivel | VANTAGEM |
| 5 | VTPM | Treinamento em Percepcao Magica | Treinamento em percepção de auras e manifestações mágicas. Recebe 2 de bônus por nível. | 10 | 2 | +2 Percepcao por nivel | VANTAGEM |
| 6 | VTL | Treinamento Logico | Treinamento especializado em raciocínio lógico e dedutivo. Requer B.RAC 5+. | 5 | 4 | +1 B.B.M por nivel | VANTAGEM |
| 7 | VTMA | Treinamento em Manipulacao | Treinamento em técnicas mentais de manipulação. Requer B.B.M 8+. | 3 | 3 | +2 em Aptidoes Mentais por nivel | VANTAGEM |

**categoriaNome:** `"Treinamento Mental"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Treinamento Mental (VTM, VTPM, VTL, VTMA) [Copilot R07 T5]
```
