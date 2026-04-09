# F2-T11 — buildRenascimento() · 11 vantagens

## Objetivo
Preencher `buildRenascimento()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | formulaCusto | efeito | tipo |
|-------|-------|------|-----------|----------|-------------|--------|------|
| 37 | VCDA | Controle de Dano | Permite rolar o dado de dano e decidir quanto efetivamente aplicar. Requer 1 Renascimento. | 1 | 5 | Decide quanto do dano rolado aplicar ao alvo | VANTAGEM |
| 38 | VUSI | Ultimo Sigilo | Oculta completamente a manifestação visual e sensorial de habilidades mágicas. Requer 1 Renascimento. | 1 | 5 | Oculta manifestacao magica de habilidades | VANTAGEM |
| 39 | VESC | Escaramuca | Permite realizar ataques falsos e manobras avançadas de combate. Requer 1 Renascimento. | 3 | 3 | Ataques falsos e manobras avancadas de combate por nivel | VANTAGEM |
| 40 | VPCO | Previsao em Combate | Antecipa ações em combate afetando Defesa, Ofensiva ou Reatividade. Requer 2 Renascimentos. | 3 | 15 | Por nivel: +1 em Defesa, Ofensiva ou Reatividade (escolha) | VANTAGEM |
| 41 | VAI | Armas Improvisadas | Permite usar armas improvisadas com eficácia crescente. Requer 1 Renascimento. | 10 | 4 | +1 B.B.A e 1 dado (D3→D.UP) por nivel com armas improvisadas | VANTAGEM |
| 42 | VDNL | Dano Nao Letal | Converte todos os danos causados em danos não letais por contusão. Requer 1 Renascimento. | 1 | 5 | Converte danos para tipo contusao (nao letal) | VANTAGEM |
| 43 | VAEC | Acao em Cadeia | Permite agir e atacar durante o Ataque Adicional no mesmo turno. Requer 1 Renascimento e Ataque Adicional. | 1 | 10 | Agir e atacar durante o Ataque Adicional | VANTAGEM |
| 44 | VATD | Atencao Difusa | Expande o raio de atenção ao redor do personagem em 1 metro por nível. Requer 1 Renascimento. | 10 | 5 | +1 metro de raio de atencao ao redor por nivel | VANTAGEM |
| 45 | VSNM | Senso Numerico | Habilidade de precisão numérica excepcional para cálculos instantâneos. Requer 1 Renascimento. | 1 | 10 | Precisao numerica instantanea em qualquer calculo | VANTAGEM |
| 46 | VPBF | Pensamento Bifurcado | Permite executar ações Independente e Padrão simultaneamente no mesmo turno. Requer 1 Renascimento. | 1 | 10 | Executa acoes Independente e Padrao ao mesmo tempo | VANTAGEM |
| 47 | VMEI | Memoria Eidetica | Lembra de experiências com detalhes completos de todos os sentidos. Requer 1 Renascimento. | 1 | 10 | Memoria completa e precisa com todos os sentidos | VANTAGEM |

**categoriaNome:** `"Vantagem de Renascimento"` para todas.

> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Renascimento (VCDA, VUSI, VESC, VPCO, VAI, VDNL, VAEC, VATD, VSNM, VPBF, VMEI) [Copilot R07 T11]
```
