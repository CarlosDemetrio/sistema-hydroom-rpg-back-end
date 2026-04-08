# DadoProspeccaoConfig

> Dados de prospecção disponíveis no jogo (d3, d4, d6, etc.). Usados para rolagens de prospecção de personagem.

---

## Entidade: `DadoProspeccaoConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(20) | ✅ | — |
| `descricao` | String(200) | — | — |
| `numeroFaces` | Integer | ✅ | — |
| `ordemExibicao` | Integer | — | `0` |

---

## DTO atual: `ProspeccaoConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `numLados` | `numeroFaces` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | numeroFaces | descricao |
|---|---|---|
| d3 | 3 | *(vazio)* |
| d4 | 4 | *(vazio)* |
| d6 | 6 | *(vazio)* |
| d8 | 8 | *(vazio)* |
| d10 | 10 | *(vazio)* |
| d12 | 12 | *(vazio)* |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição para cada dado (ex: d6 = "Dado padrão, equilibrado")
- [ ] **Adicionar d20?** — d20 é comum em RPGs mas não está listado. Necessário no sistema Klayrah?
- [ ] **DTO** — adicionar campo `descricao` ao `ProspeccaoConfigDTO` e ao provider
