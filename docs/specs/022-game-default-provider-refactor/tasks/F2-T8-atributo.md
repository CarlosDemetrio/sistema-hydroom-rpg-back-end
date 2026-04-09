# F2-T8 — buildAtributo() · 8 vantagens

## Objetivo
Preencher `buildAtributo()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 17 | VCFM | Capacidade de Forca Maxima | Desbloqueia uso da Força máxima em danos por contusão. Desbloqueada a cada 10 em Força. | 1 | 6 | Concede 1D3 de dano por contusao com Forca maxima | VANTAGEM |
| 18 | VDM | Dominio de Forca | Eleva o dado de dano por contusão concedido pela Capacidade de Força Máxima. Requer CFM. | 6 | 2 | Eleva dado de dano por contusao 1x por nivel (D3→D4→...→D9) | VANTAGEM |
| 19 | VTEN | Tenacidade | Desbloqueia uso do Vigor máximo em RD por contusão. Desbloqueada a cada 10 em Vigor. | 1 | 6 | Concede 1D3 de RD por contusao com Vigor maximo | VANTAGEM |
| 20 | VDV | Dominio de Vigor | Eleva o dado de RD por contusão concedido pela Tenacidade. Requer Tenacidade. | 2 | 2 | Eleva dado de RD por contusao 1x por nivel (D3→D4→D5) | VANTAGEM |
| 21 | VDF | Destreza Felina | Reduz penalidades em locais e terrenos difíceis. Desbloqueada a cada 10 em Agilidade. | 1 | 5 | -1 em penalidade de local ou terreno dificil | VANTAGEM |
| 22 | VSG | Sabedoria de Gamaiel | Aumenta um aspecto mágico por nível. Aspectos: Dano, Defesa, Bônus, Duração ou Área. Desbloqueada a cada 10 em SAB. | 3 | 3 | +1 nivel em aspecto magico por nivel de vantagem | VANTAGEM |
| 23 | VSAG | Sentidos Agucados | Aguça um sentido específico concedendo bônus de percepção. Requer Sabedoria 3+. | 5 | 3 | +2 de Percepcao em 1 sentido especifico por nivel | VANTAGEM |
| 24 | VIN | Inteligencia de Nyck | Aumenta o multiplicador de raciocínio em 0.5x por nível. Requer Base de RAC 7+. | 3 | 2 | +0.5x no multiplicador de Raciocinio por nivel | VANTAGEM |

**categoriaNome:** `"Vantagem de Atributo"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Atributo (VCFM, VDM, VTEN, VDV, VDF, VSG, VSAG, VIN) [Copilot R07 T8]
```
