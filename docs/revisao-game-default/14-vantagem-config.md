# VantagemConfig

> Vantagens que o personagem pode comprar com pontos de vantagem, ou receber do Mestre (INSOLITUS).

---

## Entidade: `VantagemConfig`

| campo | tipo | obrigatório | default | obs |
|---|---|---|---|---|
| `nome` | String(100) | ✅ | — | único por jogo |
| `sigla` | String(2-5) | — | — | única cross-entity por jogo |
| `descricao` | String(1000) | — | — | |
| `nivelMaximo` | Integer (min 1) | ✅ | `10` | max de níveis compráveis |
| `formulaCusto` | String(100) | ✅ | — | exp4j (ex: `NIVEL * 2`) |
| `descricaoEfeito` | String(500) | — | — | descrição do efeito visível ao jogador |
| `categoriaVantagem` | FK → CategoriaVantagem | — | — | resolvida por nome no initializer |
| `tipoVantagem` | enum TipoVantagem | ✅ | `VANTAGEM` | VANTAGEM ou INSOLITUS |
| `ordemExibicao` | Integer | ✅ | `0` | |

---

## Sub-entidades

### `VantagemEfeito` (OneToMany — CascadeAll)

| campo | tipo | obs |
|---|---|---|
| `tipoEfeito` | enum TipoEfeito | BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA, DADO_UP, FORMULA_CUSTOMIZADA |
| `atributoAlvo` | FK → AtributoConfig | usado em BONUS_ATRIBUTO |
| `aptidaoAlvo` | FK → AptidaoConfig | usado em BONUS_APTIDAO |
| `bonusAlvo` | FK → BonusConfig | usado em BONUS_DERIVADO |
| `membroAlvo` | FK → MembroCorpoConfig | usado em BONUS_VIDA_MEMBRO |
| `valorFixo` | BigDecimal | bônus fixo independente do nível |
| `valorPorNivel` | BigDecimal | bônus multiplicado pelo nível da vantagem |
| `formula` | String(200) | exp4j para FORMULA_CUSTOMIZADA |
| `descricaoEfeito` | String(500) | texto visível ao jogador |

### `VantagemPreRequisito` (referência bidirecional)

| campo | tipo | obs |
|---|---|---|
| `vantagem` | FK → VantagemConfig | a vantagem que TEM o pré-requisito |
| `requisito` | FK → VantagemConfig | a vantagem que é EXIGIDA |
| `nivelMinimo` | Integer (min 1) | nível mínimo do pré-requisito (default: 1) |

---

## DTO atual: `VantagemConfigDTO` (class com @Builder)

| campo DTO | campo entidade | status | obs |
|---|---|---|---|
| `nome` | `nome` | ✅ | |
| `descricao` | `descricao` | ✅ | |
| `formulaCusto` | `formulaCusto` | ✅ | |
| `nivelMaximoVantagem` | `nivelMaximo` | ✅ | |
| `ordemExibicao` | `ordemExibicao` | ✅ | |
| `tipoBonus` | *(sem campo na entidade)* | ❌ LEGADO | campo obsoleto — o mecanismo real é VantagemEfeito |
| `valorBonusFormula` | *(sem campo na entidade)* | ❌ LEGADO | campo obsoleto |
| `custoBase` | *(sem campo na entidade)* | ⚠️ | usado na fórmula, mas não persiste na entidade |
| `podeEvoluir` | *(sem campo na entidade)* | ❌ | campo inexistente na entidade |
| `nivelMinimoPersonagem` | *(sem campo na entidade)* | ❌ | campo inexistente na entidade |
| *(ausente)* | `sigla` | ❌ | não populado pelo provider |
| *(ausente)* | `descricaoEfeito` | ❌ | não populado pelo provider |
| *(ausente)* | `tipoVantagem` | ❌ | fixo como VANTAGEM pelo initializer; INSOLITUS não pode ser criado via provider |
| *(ausente)* | `categoriaVantagem` | ⚠️ | resolvido por `categoriaNome` separado no initializer (não no DTO) |
| *(ausente)* | efeitos (VantagemEfeito) | ❌ | **não há suporte a efeitos no DTO ou initializer** |
| *(ausente)* | pré-requisitos | ❌ | **não há suporte a pré-requisitos no DTO ou initializer** |

---

## Dados atuais no provider

Todas as vantagens usam `formulaCusto = "custo_base * nivel_vantagem"`.

### Treinamento Físico
| nome | custoBase |
|---|---|
| Treinamento de Combate Ofensivo | 3 |
| Treinamento de Combate Defensivo | 3 |
| Treinamento de Combate com Escudo | 3 |
| Treinamento Mágico | 3 |
| Treinamento de Poder Mental | 3 |
| Treinamento de Liderança | 3 |
| Treinamento de Meditação | 3 |

### Ação
| nome | custoBase |
|---|---|
| Ataque Adicional | 4 |
| Ataque Sentai | 5 |
| Contra-Ataque | 4 |
| Interceptação | 4 |

### Vantagem de Atributo
| nome | custoBase |
|---|---|
| Corpo Fechado | 3 |
| Destreza Mental | 3 |
| Destreza Física | 3 |
| Determinação Vital | 3 |

> ⚠️ **NENHUMA VANTAGEM tem `VantagemEfeito` definida** — todas ficam sem efeito mecânico concreto ao criar jogo padrão.

---

## O que falta / revisar

- [ ] **`VantagemEfeito`** — **crítico**: adicionar efeitos mecânicos para cada vantagem (ex: "Destreza Física" deveria dar +1 AGI por nível?)
- [ ] **`sigla`** — adicionar siglas únicas para vantagens que precisam ser referenciadas em fórmulas
- [ ] **`descricaoEfeito`** — adicionar descrição do efeito visível no frontend
- [ ] **Categoria de cada vantagem** — o initializer resolve por `categoriaNome`; o DTO não tem esse campo. Verificar como o provider passa a categoria para o initializer
- [ ] **`tipoBonus` / `valorBonusFormula`** — remover do `VantagemConfigDTO` (campos obsoletos)
- [ ] **`podeEvoluir` / `nivelMinimoPersonagem`** — remover do `VantagemConfigDTO` (campos sem mapeamento na entidade)
- [ ] **`VantagemPreRequisito`** — não há suporte no provider; pré-requisitos precisarão ser criados manualmente ou via API após criação do jogo
- [ ] **Vantagens por categoria faltando** — verificar se as categorias "Treinamento Mental", "Vantagem Geral", "Vantagem Histórica", "Vantagem de Renascimento" têm vantagens
- [ ] **`formulaCusto`** — `"custo_base * nivel_vantagem"` usa `custo_base` que **não existe como variável no FormulaEvaluatorService** — rever fórmula
