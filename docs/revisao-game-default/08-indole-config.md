# IndoleConfig

> Índole (alinhamento moral) do personagem: Bom, Mau, Neutro.

---

## Entidade: `IndoleConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `descricao` | String(200) | — | — |
| `ordemExibicao` | Integer | ✅ | `0` |

---

## DTO atual: `IndoleConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | descricao | ordem |
|---|---|---|
| Bom | *(vazio)* | 1 |
| Mau | *(vazio)* | 2 |
| Neutro | *(vazio)* | 3 |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição de cada índole para o jogador entender a escolha
- [ ] **Mais opções?** — sistemas D&D usam 9 alinhamentos. O Klayrah precisa de mais granularidade?
- [ ] **DTO** — adicionar `descricao` ao `IndoleConfigDTO` se quiser populá-la
