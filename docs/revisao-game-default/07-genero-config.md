# GeneroConfig

> Gêneros disponíveis para criação de personagem.

---

## Entidade: `GeneroConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `descricao` | String(200) | — | — |
| `ordemExibicao` | Integer | ✅ | `0` |

---

## DTO atual: `GeneroConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | descricao | ordem |
|---|---|---|
| Masculino | *(vazio)* | 1 |
| Feminino | *(vazio)* | 2 |
| Outro | *(vazio)* | 3 |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição ou deixar vazio (campo opcional, pode não ser necessário)
- [ ] **Adicionar mais opções?** — "Não-binário", "Prefiro não informar", etc.?
- [ ] **DTO** — adicionar `descricao` ao `GeneroConfigDTO` se quiser populá-la
