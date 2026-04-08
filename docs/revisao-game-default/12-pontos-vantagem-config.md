# PontosVantagemConfig

> Define quantos pontos de vantagem o personagem ganha ao atingir determinados níveis.

---

## Entidade: `PontosVantagemConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nivel` | Integer | ✅ | — | nível em que os pontos são concedidos |
| `pontosGanhos` | Integer | ✅ | `1` | pontos de vantagem ganhos neste nível |

---

## DTO atual: `PontosVantagemConfigDTO` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nivel` | `nivel` | ✅ |
| `pontos` | `pontosGanhos` | ✅ |

> ✅ **DTO completo** — cobre todos os campos da entidade.

---

## Dados atuais no provider

| nivel | pontosGanhos | total acumulado |
|---|---|---|
| 1 | 6 | 6 |
| 5 | 3 | 9 |
| 10 | 10 | 19 |
| 15 | 3 | 22 |
| 20 | 10 | 32 |
| 25 | 3 | 35 |
| 30 | 15 | 50 |
| 35 | 3 | 53 |

> Total ao atingir nível 35: **53 pontos de vantagem**

---

## O que falta / revisar

- [ ] **Total de 53 pontos** — está correto para o sistema Klayrah? Suficiente para comprar todas as vantagens desejadas?
- [ ] **Distribuição** — os saltos grandes nos níveis 10, 20, 30 são intencionais (marcos)?
- [ ] **Níveis intermediários** — níveis 2-4, 6-9 etc. não concedem pontos. Correto?
- [ ] Config completa, nenhum campo faltando.
