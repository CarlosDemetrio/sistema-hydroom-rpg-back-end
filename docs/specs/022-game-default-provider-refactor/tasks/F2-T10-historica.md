# F2-T10 — buildHistorica() · 7 vantagens

## Objetivo
Preencher `buildHistorica()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 30 | VHER | Heranca | O personagem herdou bens e recursos de família ou mentor. Rola 1D3 de Riqueza aplicada. | 1 | 5 | Rola 1D3 de Riqueza inicial aplicada | VANTAGEM |
| 31 | VRIQ | Riqueza | Representa acumulação de bens materiais e riquezas. Cada nível custa mais PN que o anterior. | 3 | **nivel * 5** | +1 Grade de Riqueza por nivel (Nv1=5PN, Nv2=10PN, Nv3=15PN) | VANTAGEM |
| 32 | VIA | Indole Aplicada | Permite alterar a índole do personagem em relação a um alvo adicional por nível. | 5 | 2 | Muda indole com 1 alvo adicional por nivel | VANTAGEM |
| 33 | VOFI | Oficios | O personagem conhece profissões e ofícios variados do mundo. | 2 | 2 | +1 Profissao ou Oficio conhecida por nivel | VANTAGEM |
| 34 | VTOF | Treino de Oficio | Aprimora a performance do personagem no exercício de ofícios e profissões. | 10 | 4 | +1 por nivel em testes para exercer oficios | VANTAGEM |
| 35 | VVO | Vinculo com Organizacao | O personagem possui influência e contatos em uma organização com dezenas de membros. | 3 | 7 | Influencia em organizacao com dezenas de membros por nivel | VANTAGEM |
| 36 | VCAP | Capangas | O personagem possui aliados ou capangas leais que o seguem. | 5 | 5 | +1 aliado ou capanga leal por nivel | VANTAGEM |

**categoriaNome:** `"Vantagem Histórica"` para todas.

> ⚠️ VRIQ tem `formulaCusto = "nivel * 5"` — NAO e numero fixo. E uma expressao exp4j valida.
> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Historica (VHER, VRIQ, VIA, VOFI, VTOF, VVO, VCAP) [Copilot R07 T10]
```
