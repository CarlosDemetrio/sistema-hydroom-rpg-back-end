# ItemConfig

> Catálogo de itens do jogo: armas, armaduras, acessórios, consumíveis, equipamentos. Cada item tem efeitos e requisitos.

---

## Entidade: `ItemConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(100) | ✅ | — | único por jogo |
| `raridade` | FK → RaridadeItemConfig | ✅ | — | resolvida por nome no initializer |
| `tipo` | FK → TipoItemConfig | ✅ | — | resolvida por nome no initializer |
| `peso` | BigDecimal(5,2) | ✅ | — | em kg |
| `valor` | Integer | — | — | valor em moedas |
| `duracaoPadrao` | Integer | — | — | usos máximos (null = sem limite) |
| `nivelMinimo` | int | ✅ | — | nível mínimo do personagem para usar |
| `propriedades` | String(1000) | — | — | tags de propriedades (ex: "versatil, magica") |
| `descricao` | String(2000) | — | — | |
| `ordemExibicao` | int | ✅ | — | |

---

## Sub-entidades

### `ItemEfeito` (OneToMany — CascadeAll, orphanRemoval)

| campo | tipo | obs |
|---|---|---|
| `tipoEfeito` | enum TipoItemEfeito | BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_ESSENCIA, FORMULA_CUSTOMIZADA, EFEITO_DADO |
| `atributoAlvo` | FK → AtributoConfig | para BONUS_ATRIBUTO; resolvido por abreviação |
| `aptidaoAlvo` | FK → AptidaoConfig | para BONUS_APTIDAO |
| `bonusAlvo` | FK → BonusConfig | para BONUS_DERIVADO; resolvido por nome (ex: "B.B.A") |
| `valorFixo` | Integer | valor do bônus |
| `formula` | String(200) | para FORMULA_CUSTOMIZADA |
| `descricaoEfeito` | String(300) | texto visível ao jogador |

### `ItemRequisito` (OneToMany — CascadeAll, orphanRemoval)

| campo | tipo | obs |
|---|---|---|
| `tipo` | enum TipoRequisito | NIVEL, ATRIBUTO, BONUS, APTIDAO, VANTAGEM, CLASSE, RACA |
| `alvo` | String(50) | nome/sigla do alvo (ex: "FOR", "B.B.A") |
| `valorMinimo` | Integer | valor mínimo exigido |

---

## DTO atual: `ItemConfigDefault` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `raridadeNome` | `raridade` (FK) | ✅ resolvido por nome |
| `tipoNome` | `tipo` (FK) | ✅ resolvido por nome |
| `peso` | `peso` | ✅ |
| `valor` | `valor` | ✅ |
| `duracaoPadrao` | `duracaoPadrao` | ✅ |
| `nivelMinimo` | `nivelMinimo` | ✅ |
| `propriedades` | `propriedades` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| `efeitos` | `efeitos` (ItemEfeito) | ✅ via `ItemEfeitoDefault` |
| *(ausente)* | `descricao` | ❌ não populado |
| *(ausente)* | `requisitos` (ItemRequisito) | ❌ **nenhum item tem requisitos no provider** |

### `ItemEfeitoDefault` (record)

| campo DTO | campo entidade | status |
|---|---|---|
| `tipoEfeito` | `tipoEfeito` | ✅ |
| `bonusAlvoNome` | `bonusAlvo` (FK) | ✅ resolvido por nome |
| `atributoAlvoNome` | `atributoAlvo` (FK) | ✅ resolvido por abreviação |
| `valorFixo` | `valorFixo` | ✅ |
| *(ausente)* | `aptidaoAlvo` | ❌ não suportado no DTO |
| *(ausente)* | `valorPorNivel` (BigDecimal) | ❌ não suportado |
| *(ausente)* | `formula` | ❌ não suportado |
| *(ausente)* | `descricaoEfeito` | ❌ não populado |

---

## Dados atuais no provider

### Armas (15 itens)
| nome | raridade | tipo | peso | valor | dur | nivelMin | efeito |
|---|---|---|---|---|---|---|---|
| Adaga | Comum | Adaga | 0.45 | 2 | — | 1 | — |
| Espada Curta | Comum | Espada Curta | 0.90 | 10 | — | 1 | — |
| Espada Longa | Comum | Espada Longa | 1.36 | 15 | — | 1 | — |
| Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | 1 | +1 BBA |
| Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | 5 | +2 BBA |
| Machadinha | Comum | Machado de Batalha | 0.90 | 5 | — | 1 | — |
| Machado de Batalha | Comum | Machado de Batalha | 1.80 | 10 | — | 1 | — |
| Machado Grande | Comum | Machado Grande | 3.17 | 30 | — | 3 | — |
| Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | — | 1 | — |
| Arco Curto | Comum | Arco Curto | 0.90 | 25 | — | 1 | — |
| Arco Longo | Comum | Arco Longo | 1.80 | 50 | — | 2 | — |
| Arco Longo +1 | Incomum | Arco Longo | 1.80 | 500 | 10 | 4 | +1 BBA |
| Cajado de Madeira | Comum | Cajado | 1.80 | 5 | — | 1 | — |
| Cajado Arcano +1 | Incomum | Cajado | 2.00 | 500 | 10 | 3 | +1 BBM |
| Lanca | Comum | Lanca | 1.36 | 1 | — | 1 | — |

### Armaduras e Escudos (10 itens)
| nome | raridade | tipo | peso | valor | nivelMin | efeito |
|---|---|---|---|---|---|---|
| Gibao de Couro | Comum | Armadura Leve | 4.50 | 10 | 1 | +1 DEF |
| Couro Batido | Comum | Armadura Leve | 11.30 | 45 | 1 | +2 DEF |
| Camisao de Malha | Comum | Armadura Media | 13.60 | 50 | 2 | +3 DEF |
| Cota de Escamas | Comum | Armadura Media | 20.40 | 50 | 3 | +4 DEF |
| Cota de Malha | Comum | Armadura Pesada | 27.20 | 75 | 4 | +5 DEF |
| Meia Placa | Comum | Armadura Pesada | 19.90 | 750 | 5 | +5 DEF, +1 REF |
| Placa Completa | Raro | Armadura Pesada | 29.50 | 1500 | 7 | +6 DEF |
| Escudo de Madeira | Comum | Escudo | 2.72 | 10 | 1 | +1 BLO |
| Escudo de Aco | Comum | Escudo | 2.72 | 20 | 1 | +2 BLO |
| Escudo Enfeiticado +1 | Incomum | Escudo | 2.72 | 500 | 3 | +2 BLO, +1 DEF |

### Acessórios e Itens Mágicos (5 itens)
| nome | raridade | tipo | peso | valor | nivelMin | efeito |
|---|---|---|---|---|---|---|
| Anel da Forca +1 | Raro | Anel | 0.01 | 2000 | 5 | +1 FOR (atributo) |
| Anel de Protecao +1 | Raro | Anel | 0.01 | 2000 | 5 | +1 DEF, +1 BLO |
| Amuleto de Saude | Incomum | Amuleto | 0.05 | 500 | 3 | +5 VIDA |
| Amuleto da Essencia | Incomum | Amuleto | 0.05 | 500 | 3 | +5 ESSENCIA |
| Manto de Elvenkind | Muito Raro | Amuleto | 0.45 | 5000 | 7 | +3 ESQ, +2 PER |

### Consumíveis (5 itens)
| nome | raridade | tipo | dur | valor | efeito |
|---|---|---|---|---|---|
| Pocao de Cura Menor | Comum | Pocao | 1 | 25 | — (sem ItemEfeito, apenas prop.) |
| Pocao de Cura | Comum | Pocao | 1 | 50 | — |
| Pocao de Cura Superior | Incomum | Pocao | 1 | 200 | — |
| Flecha Comum (20) | Comum | Municao | — | 1 | — |
| Virote (20) | Comum | Municao | — | 1 | — |

### Equipamentos de Aventura (5 itens)
| nome | raridade | tipo | valor |
|---|---|---|---|
| Kit de Aventureiro | Comum | Equipamento de Aventura | 12 |
| Kit de Curandeiro | Comum | Equipamento de Aventura | 5 |
| Kit de Ladroa | Comum | Equipamento de Aventura | 25 |
| Lanterna Bullseye | Comum | Equipamento de Aventura | 10 |
| Tomo Arcano | Comum | Equipamento de Aventura | 25 |

---

## O que falta / revisar

- [ ] **`descricao`** — nenhum item tem descrição preenchida
- [ ] **`ItemRequisito`** — nenhum item tem requisitos de uso (ex: Cota de Malha requer Força mínima, mas não está como ItemRequisito — está apenas em `propriedades`)
- [ ] **Acentuação em nomes**: `Lanca` → `Lança`, `Camisao de Malha` → `Camisão de Malha`, `Pocao*` → `Poção*`, `Municao` → `Munição`, `Escudo de Aco` → `Escudo de Aço`, `Anel da Forca` → `Anel da Força`, `Kit de Ladroa` → `Kit de Ladroagem` (ou similar), `Manto de Elvenkind` → sem acento, ok
- [ ] **Poções sem ItemEfeito** — "recupera 5 de vida" está apenas em `propriedades` como texto. Precisa de `ItemEfeito(BONUS_VIDA, valorFixo=5)`?
- [ ] **`ItemEfeitoDefault` sem suporte a `aptidaoAlvo`** — nenhum item atual usa, mas o campo existe na entidade
- [ ] **`ItemEfeitoDefault` sem `descricaoEfeito`** — campo existe na entidade mas nunca é populado
- [ ] **TipoItemConfig não mapeado**: "Lanca" no provider usa tipo "Lanca" mas a entidade do tipo foi criada como "Lanca" (sem cedilha) — a resolução por nome no initializer vai falhar se corrigir o nome do tipo sem corrigir o item
