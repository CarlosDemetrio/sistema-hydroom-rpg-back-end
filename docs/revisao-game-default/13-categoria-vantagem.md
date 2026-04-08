# CategoriaVantagem

> Agrupa as vantagens por tema/tipo (Combate, Magia, Social, etc.). Usada para organização no sistema.

---

## Entidade: `CategoriaVantagem`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(100) | ✅ | — |
| `descricao` | TEXT | — | — |
| `cor` | String(7) | — | — | formato `#RRGGBB` |
| `ordemExibicao` | Integer | ✅ | `0` |

---

## DTO atual: `CategoriaVantagemDTO` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `cor` | `cor` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | cor | ordem | descricao |
|---|---|---|---|
| Treinamento Físico | `#e74c3c` | 1 | *(vazio)* |
| Treinamento Mental | `#8e44ad` | 2 | *(vazio)* |
| Ação | `#e67e22` | 3 | *(vazio)* |
| Reação | `#27ae60` | 4 | *(vazio)* |
| Vantagem de Atributo | `#2980b9` | 5 | *(vazio)* |
| Vantagem Geral | `#95a5a6` | 6 | *(vazio)* |
| Vantagem Histórica | `#f39c12` | 7 | *(vazio)* |
| Vantagem de Renascimento | `#1abc9c` | 8 | *(vazio)* |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição de cada categoria (ex: "Treinamento Físico = vantagens de combate corpo a corpo")
- [ ] **DTO** — adicionar `descricao` ao `CategoriaVantagemDTO` se quiser populá-la
- [ ] **Categorias adicionais?** — revisar se as 8 categorias cobrem todos os tipos de vantagem do sistema
- [ ] **Nomes de categoria devem bater exatamente** com os usados em `VantagemConfigDTO` (o initializer resolve por nome)
