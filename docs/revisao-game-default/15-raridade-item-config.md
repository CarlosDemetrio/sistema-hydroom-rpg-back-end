# RaridadeItemConfig

> Níveis de raridade de itens (Comum, Incomum, Raro...). Controla poder máximo dos efeitos e permissões.

---

## Entidade: `RaridadeItemConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — | único por jogo |
| `cor` | String(7) | ✅ | — | formato `#RRGGBB` |
| `ordemExibicao` | Integer | ✅ | — | |
| `podeJogadorAdicionar` | boolean | ✅ | — | se o jogador pode adicionar itens desta raridade |
| `bonusAtributoMin` | Integer | — | — | bônus mínimo a atributo para itens desta raridade |
| `bonusAtributoMax` | Integer | — | — | bônus máximo a atributo |
| `bonusDerivadoMin` | Integer | — | — | bônus mínimo a derivado |
| `bonusDerivadoMax` | Integer | — | — | bônus máximo a derivado |
| `descricao` | String(500) | — | — | |

---

## DTO atual: `RaridadeItemConfigDefault` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `cor` | `cor` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| `podeJogadorAdicionar` | `podeJogadorAdicionar` | ✅ |
| `bonusAtributoMin` | `bonusAtributoMin` | ✅ |
| `bonusAtributoMax` | `bonusAtributoMax` | ✅ |
| `bonusDerivadoMin` | `bonusDerivadoMin` | ✅ |
| `bonusDerivadoMax` | `bonusDerivadoMax` | ✅ |
| `descricao` | `descricao` | ✅ |

> ✅ **DTO completo** — cobre todos os campos da entidade.

---

## Dados atuais no provider

| nome | cor | ord | jogador? | bAtrib (min-max) | bDeriv (min-max) | descricao |
|---|---|---|---|---|---|---|
| Comum | `#9d9d9d` | 1 | ✅ | 0-0 | 0-0 | "Itens mundanos sem encantamento" |
| Incomum | `#1eff00` | 2 | ❌ | 1-1 | 1-1 | "Levemente encantado ou de qualidade excepcional" |
| Raro | `#0070dd` | 3 | ❌ | 1-2 | 1-2 | "Encantamento moderado, raramente encontrado" |
| Muito Raro | `#a335ee` | 4 | ❌ | 2-3 | 2-3 | "Encantamento poderoso, obra de artesão mestre" |
| Epico | `#ff8000` | 5 | ❌ | 3-4 | 3-4 | "Artefato de grande poder, história própria" |
| Lendario | `#e6cc80` | 6 | ❌ | 4-5 | 4-5 | "Um dos poucos existentes no mundo" |
| Unico | `#e268a8` | 7 | ❌ | 0-0 | 0-0 | "Criação única do Mestre, sem referência de custo" |

---

## O que falta / revisar

- [ ] **Acentuação**: `Epico` → `Épico`, `Lendario` → `Lendário`, `Unico` → `Único`
- [ ] Revisar faixas de bônus — se fazem sentido para o sistema (ex: Único com 0-0 bônus é correto? É porque o Mestre define livremente?)
- [ ] Config completa, nenhum campo faltando no DTO.
