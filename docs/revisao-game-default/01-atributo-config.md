# AtributoConfig

> Atributos base do personagem (FOR, AGI, VIG, etc.). Cada atributo tem fórmula de ímpeto.

---

## Entidade: `AtributoConfig`

| campo | tipo | obrigatório | default | observação |
|---|---|---|---|---|
| `nome` | String(50) | ✅ | — | unique por jogo |
| `abreviacao` | String(5) | — | — | deve ser única cross-entity no jogo (sigla para fórmulas) |
| `descricao` | String(500) | — | — | |
| `formulaImpeto` | String(100) | — | — | expressão exp4j, variável: `total` |
| `descricaoImpeto` | String(200) | — | — | unidade de medida (ex: "kg", "metros") |
| `valorMinimo` | Integer | — | `0` | valor mínimo permitido para o atributo |
| `valorMaximo` | Integer | — | `999` | valor máximo permitido |
| `ordemExibicao` | Integer | — | `0` | |

---

## DTO atual: `AtributoConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ populado |
| `abreviacao` | `abreviacao` | ✅ populado |
| `descricao` | `descricao` | ✅ populado |
| `formulaImpeto` | `formulaImpeto` | ✅ populado |
| `unidadeImpeto` | `descricaoImpeto` | ✅ mapeado (nome diferente!) |
| `ordemExibicao` | `ordemExibicao` | ✅ populado |
| *(ausente)* | `valorMinimo` | ⚠️ usa default 0 |
| *(ausente)* | `valorMaximo` | ⚠️ usa default 999 |

---

## Dados atuais no provider

| nome | abrev | formulaImpeto | unidade | valorMin | valorMax |
|---|---|---|---|---|---|
| Força | FOR | `total * 3` | kg | **0** *(default)* | **999** *(default)* |
| Agilidade | AGI | `total / 3` | metros | **0** | **999** |
| Vigor | VIG | `total / 10` | RD | **0** | **999** |
| Sabedoria | SAB | `total / 10` | RDM | **0** | **999** |
| Intuição | INTU | `min(total / 20, 3)` | pontos | **0** | **999** |
| Inteligência | INT | `total / 20` | comando | **0** | **999** |
| Astúcia | AST | `total / 10` | estratégia | **0** | **999** |

---

## O que falta / revisar

- [ ] **`valorMinimo`** — todos usam 0 por default. Correto para o sistema? Algum atributo tem mínimo diferente?
- [ ] **`valorMaximo`** — todos usam 999. Correto? Ou cada atributo deveria ter um cap próprio?
- [ ] **DTO não tem `valorMinimo`/`valorMaximo`** — se quiser customizar, adicionar ao `AtributoConfigDTO`
- [ ] **Fórmulas de ímpeto** — revisar se estão corretas para o sistema Klayrah (ex: FOR usa `total * 3` = carga em kg)
- [ ] **`descricaoImpeto`** — campo na entidade é "descricaoImpeto" mas no DTO é "unidadeImpeto" — renomear para consistência?
