# NivelConfig (+ LimitadorConfigDTO)

> Define o que o personagem ganha ao subir cada nível (XP necessária, pontos de atributo, de aptidão, limitador de atributo).

---

## Entidade: `NivelConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nivel` | Integer | ✅ | — |
| `xpNecessaria` | Long | ✅ | — |
| `pontosAtributo` | Integer | ✅ | `3` |
| `pontosAptidao` | Integer | — | `3` |
| `limitadorAtributo` | Integer | ✅ | — | valor máximo que qualquer atributo pode ter neste nível |
| `permitirRenascimento` | Boolean | ✅ | `false` | true para níveis 31+ |

---

## DTO atual: `NivelConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nivel` | `nivel` | ✅ |
| `experienciaNecessaria` | `xpNecessaria` | ✅ |
| `pontosAtributo` | `pontosAtributo` | ✅ |
| `pontosAptidao` | `pontosAptidao` | ✅ |
| `limitadorAtributo` | `limitadorAtributo` | ✅ |
| `pontosVantagem` | *(não existe na entidade)* | ❌ **ignorado** pelo initializer |
| *(ausente)* | `permitirRenascimento` | calculado inline: `nivel >= 31` |

---

## LimitadorConfigDTO (DTO órfão)

> `getDefaultLimitadores()` existe no provider mas **nunca é chamado** pelo initializer. O limitador vem diretamente do `NivelConfigDTO.limitadorAtributo`.

| nivelInicio | nivelFim | limiteAtributo |
|---|---|---|
| 0 | 1 | 10 |
| 2 | 20 | 50 |
| 21 | 25 | 75 |
| 26 | 30 | 100 |
| 31 | 35 | 120 |

---

## Dados atuais no provider (36 níveis)

| nivel | xpNecessaria | ptAtrib | ptAptidao | limitador | renascimento |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 10 | false |
| 1 | 1.000 | 3 | 3 | 10 | false |
| 2 | 3.000 | 3 | 3 | 50 | false |
| 3 | 6.000 | 3 | 3 | 50 | false |
| 4 | 10.000 | 3 | 3 | 50 | false |
| 5 | 15.000 | 3 | 3 | 50 | false |
| 6 | 21.000 | 3 | 3 | 50 | false |
| 7 | 28.000 | 3 | 3 | 50 | false |
| 8 | 36.000 | 3 | 3 | 50 | false |
| 9 | 45.000 | 3 | 3 | 50 | false |
| 10 | 55.000 | 3 | 3 | 50 | false |
| 11 | 66.000 | 3 | 3 | 50 | false |
| 12 | 78.000 | 3 | 3 | 50 | false |
| 13 | 91.000 | 3 | 3 | 50 | false |
| 14 | 105.000 | 3 | 3 | 50 | false |
| 15 | 120.000 | 3 | 3 | 50 | false |
| 16 | 136.000 | 3 | 3 | 50 | false |
| 17 | 153.000 | 3 | 3 | 50 | false |
| 18 | 171.000 | 3 | 3 | 50 | false |
| 19 | 190.000 | 3 | 3 | 50 | false |
| 20 | 210.000 | 3 | 3 | 50 | false |
| 21 | 231.000 | 3 | 3 | 75 | false |
| 22 | 253.000 | 3 | 3 | 75 | false |
| 23 | 276.000 | 3 | 3 | 75 | false |
| 24 | 300.000 | 3 | 3 | 75 | false |
| 25 | 325.000 | 3 | 3 | 75 | false |
| 26 | 351.000 | 3 | 3 | 100 | false |
| 27 | 378.000 | 3 | 3 | 100 | false |
| 28 | 406.000 | 3 | 3 | 100 | false |
| 29 | 435.000 | 3 | 3 | 100 | false |
| 30 | 465.000 | 3 | 3 | 100 | false |
| 31 | 496.000 | 3 | 3 | 120 | **true** |
| 32 | 528.000 | 3 | 3 | 120 | **true** |
| 33 | 561.000 | 3 | 3 | 120 | **true** |
| 34 | 595.000 | 3 | 3 | 120 | **true** |
| 35 | 595.000 | 3 | 3 | 120 | **true** |

---

## O que falta / revisar

- [ ] **XP do nível 35** — igual ao 34 (595.000). Intencional?
- [ ] **`pontosVantagem` no DTO** — campo existe no DTO mas não na entidade, e não é usado. Remover do DTO ou mover lógica para `PontosVantagemConfig`?
- [ ] **Pontos uniformes** — todos os níveis têm 3 ptAtrib e 3 ptAptidao. Deveria ter variação por nível?
- [ ] **`LimitadorConfigDTO`** — método `getDefaultLimitadores()` nunca é chamado. Remover ou implementar?
- [ ] **`permitirRenascimento`** hardcoded para `nivel >= 31` — correto para o sistema?
