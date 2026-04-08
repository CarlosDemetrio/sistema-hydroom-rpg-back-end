# BonusConfig

> Derivados calculados a partir dos atributos (BBA, DEF, ESQ, etc.). Usados em combate e testes.

---

## Entidade: `BonusConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `sigla` | String(5) | ✅ | — | única cross-entity por jogo |
| `descricao` | String(500) | — | — |
| `formulaBase` | String(200) | — | — | expressão exp4j com abreviações de atributos |
| `ordemExibicao` | Integer | — | `0` |

---

## DTO atual: `BonusConfigDTO` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `sigla` | `sigla` | ✅ |
| `formulaBase` | `formulaBase` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | sigla | formulaBase | descricao |
|---|---|---|---|
| B.B.A | BBA | `(FOR + AGI) / 3` | *(vazio)* |
| B.B.M | BBM | `(SAB + INT) / 3` | *(vazio)* |
| Defesa | DEF | `VIG / 5` | *(vazio)* |
| Esquiva | ESQ | `AGI / 5` | *(vazio)* |
| Iniciativa | INI | `INTU / 5` | *(vazio)* |
| Percepção | PER | `INTU / 3` | *(vazio)* |
| Raciocínio | RAC | `INT / 3` | *(vazio)* |
| Bloqueio | BLO | `VIG / 3` | *(vazio)* |
| Reflexo | REF | `AGI / 3` | *(vazio)* |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição de cada bônus (ex: BBA = "Bônus Base de Ataque corpo a corpo")
- [ ] **Fórmulas** — revisar se estão corretas. Ex: `(FOR + AGI) / 3` para BBA está certo?
- [ ] **Adicionar mais derivados?** — "Dano Físico", "Dano Mágico", "Velocidade de Movimento"?
- [ ] **DTO** — adicionar `descricao` ao `BonusConfigDTO` se quiser populá-la
