# F2-T7 — buildReacao() · 7 vantagens

## Objetivo
Preencher `buildReacao()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 10 | VCA | Contra-Ataque | Pode reagir a um ataque atacando de volta com dificuldade +5. Requer Bônus Base 10+. | 1 | 5 | Reacao: atacar de volta com dificuldade +5 | VANTAGEM |
| 11 | VITC | Interceptacao | Interrompe uma ação antes dela de fato acontecer. Requer Bônus Base 10+. | 1 | 5 | Reacao: interrompe acao do oponente antes de ocorrer | VANTAGEM |
| 12 | VRE | Reflexos Especiais | Reações padrão podem ser executadas utilizando habilidades especiais. Requer B.REF ou P.MAG 30+. | 3 | 5 | Reacoes padrao executadas com habilidades por nivel | VANTAGEM |
| 13 | VIH | Instinto Heroico | Usar a ação padrão para salvar um aliado em perigo iminente. Requer Bônus Ofensivo 18+. | 1 | 5 | Acao padrao usada para salvar um aliado | VANTAGEM |
| 14 | VDH | Deflexao Heroica | Usar a ação padrão para salvar a si mesmo e outro com dificuldade +5. Requer Bônus Ofensivo 18+. | 1 | 5 | Acao padrao para salvar a si e outro com dificuldade +5 | VANTAGEM |
| 15 | VISB | Instinto de Sobrevivencia | Reduz somas de dificuldade para desviar ataques de múltiplos alvos. Requer Base Reflexos 7+. | 3 | 3 | -1 por nivel na dificuldade ao desviar de ataques multiplos | VANTAGEM |
| 16 | VRA | Reflexos Aprimorados | Reduz somas de dificuldade para reduzir dano pela metade. Requer Base Reflexos 7+. | 3 | 3 | -1 por nivel na dificuldade ao reduzir dano pela metade | VANTAGEM |

**categoriaNome:** `"Reação"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Reacao (VCA, VITC, VRE, VIH, VDH, VISB, VRA) [Copilot R07 T7]
```
