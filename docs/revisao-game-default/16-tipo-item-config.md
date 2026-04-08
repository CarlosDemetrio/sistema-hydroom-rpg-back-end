# TipoItemConfig

> Tipos de item por categoria e subcategoria (Arma/Espada, Armadura/Leve, Consumível/Poção, etc.).

---

## Entidade: `TipoItemConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(100) | ✅ | — | único por jogo |
| `categoria` | enum CategoriaItem | ✅ | — | ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA |
| `subcategoria` | enum SubcategoriaItem | — | — | ver enum abaixo |
| `requerDuasMaos` | boolean | ✅ | — | |
| `ordemExibicao` | Integer | ✅ | — | |
| `descricao` | String(300) | — | — | |

### Enum `SubcategoriaItem`
`ESPADA, ARCO, LANCA, MACHADO, MARTELO, CAJADO, ADAGA, ARREMESSO, BESTA,`
`ARMADURA_LEVE, ARMADURA_MEDIA, ARMADURA_PESADA, ESCUDO,`
`ANEL, AMULETO, BOTAS, CAPA, LUVAS,`
`POCAO, MUNICAO, KIT, OUTROS`

---

## DTO atual: `TipoItemConfigDefault` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `categoria` | `categoria` | ✅ |
| `subcategoria` | `subcategoria` | ✅ |
| `requerDuasMaos` | `requerDuasMaos` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `descricao` | ❌ não populado |

---

## Dados atuais no provider

| nome | categoria | subcategoria | 2 mãos? | ord |
|---|---|---|---|---|
| Espada Curta | ARMA | ESPADA | ❌ | 1 |
| Espada Longa | ARMA | ESPADA | ❌ | 2 |
| Espada Dupla | ARMA | ESPADA | ✅ | 3 |
| Arco Curto | ARMA | ARCO | ✅ | 4 |
| Arco Longo | ARMA | ARCO | ✅ | 5 |
| Adaga | ARMA | ADAGA | ❌ | 6 |
| Machado de Batalha | ARMA | MACHADO | ❌ | 7 |
| Machado Grande | ARMA | MACHADO | ✅ | 8 |
| Martelo de Guerra | ARMA | MARTELO | ❌ | 9 |
| Cajado | ARMA | CAJADO | ✅ | 10 |
| Lanca | ARMA | LANCA | ❌ | 11 |
| Armadura Leve | ARMADURA | ARMADURA_LEVE | ❌ | 12 |
| Armadura Media | ARMADURA | ARMADURA_MEDIA | ❌ | 13 |
| Armadura Pesada | ARMADURA | ARMADURA_PESADA | ❌ | 14 |
| Escudo | ARMADURA | ESCUDO | ❌ | 15 |
| Anel | ACESSORIO | ANEL | ❌ | 16 |
| Amuleto | ACESSORIO | AMULETO | ❌ | 17 |
| Pocao | CONSUMIVEL | POCAO | ❌ | 18 |
| Municao | CONSUMIVEL | MUNICAO | ❌ | 19 |
| Equipamento de Aventura | AVENTURA | OUTROS | ❌ | 20 |

---

## O que falta / revisar

- [ ] **Acentuação**: `Lanca` → `Lança`, `Armadura Media` → `Armadura Média`, `Pocao` → `Poção`, `Municao` → `Munição`
- [ ] **`descricao`** — adicionar descrição se quiser (campo opcional)
- [ ] **Subcategorias não usadas** — `BESTA, ARREMESSO, BOTAS, CAPA, LUVAS, KIT` existem no enum mas sem tipos no provider. Adicionar?
- [ ] **FERRAMENTA** — categoria existe no enum mas nenhum tipo de item a usa. Necessário?
