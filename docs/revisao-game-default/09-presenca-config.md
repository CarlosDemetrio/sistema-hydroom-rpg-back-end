# PresencaConfig

> Nível de Presença do personagem (equivalente ao alinhamento ético: Bom, Leal, Caótico, Neutro).

---

## Entidade: `PresencaConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `descricao` | String(200) | — | — |
| `ordemExibicao` | Integer | ✅ | `0` |

---

## DTO atual: `PresencaConfigDTO`

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
| Leal | *(vazio)* | 2 |
| Caótico | *(vazio)* | 3 |
| Neutro | *(vazio)* | 4 |

---

## O que falta / revisar

- [ ] **`descricao`** — adicionar descrição de cada presença para o jogador entender a escolha
- [ ] **Nomenclatura** — "Presença" é o termo correto no sistema Klayrah? Verificar glossário.
- [ ] **DTO** — adicionar `descricao` ao `PresencaConfigDTO` se quiser populá-la
