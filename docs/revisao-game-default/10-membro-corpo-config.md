# MembroCorpoConfig

> Membros do corpo do personagem com sua porcentagem de vida. Usado para dano localizado.

---

## Entidade: `MembroCorpoConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `porcentagemVida` | BigDecimal(3,2) | ✅ | — | 0.01 a 1.00 |
| `ordemExibicao` | Integer | — | `0` |

---

## DTO atual: `MembroCorpoConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `porcentagemVida` | `porcentagemVida` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |

> ✅ **DTO completo** — cobre todos os campos da entidade.

---

## Dados atuais no provider

| nome | porcentagemVida | ordem | interpretação |
|---|---|---|---|
| Cabeça | 0.75 | 1 | dano à cabeça = 75% da VT total |
| Tronco | 0.35 | 2 | dano ao tronco = 35% da VT total |
| Braço Direito | 0.10 | 3 | 10% da VT total |
| Braço Esquerdo | 0.10 | 4 | 10% da VT total |
| Perna Direita | 0.10 | 5 | 10% da VT total |
| Perna Esquerda | 0.10 | 6 | 10% da VT total |
| Sangue | 1.00 | 9 | dano ao sangue = 100% da VT (morte) |

> ⚠️ **Ordem 9 para Sangue** — pulou ordens 7 e 8. Intencional?

---

## O que falta / revisar

- [ ] **Porcentagens** — revisar se os valores fazem sentido no sistema Klayrah (ex: Cabeça = 75% parece muito alto)
- [ ] **Sangue com ordem 9** — ordems 7 e 8 estão puladas. Intencional ou bug?
- [ ] **Adicionar membros?** — "Pescoço", "Mão Direita/Esquerda", "Pé Direito/Esquerdo"?
- [ ] Nenhum campo de entidade está faltando no DTO. Config completa.
