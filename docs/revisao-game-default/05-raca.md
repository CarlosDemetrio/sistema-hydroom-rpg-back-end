# Raca (+ RacaBonusAtributo)

> Raças disponíveis para personagens, com bônus/penalidades em atributos e restrições de classe.

---

## Entidade: `Raca`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String | ✅ | — |
| `descricao` | String (TEXT) | — | — |
| `ordemExibicao` | Integer | ✅ | `0` |
| `bonusAtributos` | Set<RacaBonusAtributo> | — | vazio | bônus/penalidades em atributos |
| `classesPermitidas` | Set<RacaClassePermitida> | — | vazio | classes que a raça pode ser |
| `pontosConfig` | Set<RacaPontosConfig> | — | vazio | pontos iniciais extras por raça |
| `vantagensPreDefinidas` | Set<RacaVantagemPreDefinida> | — | vazio | vantagens raciais automáticas |

### Sub-entidade: `RacaBonusAtributo`
| campo | tipo | observação |
|---|---|---|
| `raca` | FK → Raca | |
| `atributo` | FK → AtributoConfig | resolvido por abreviação |
| `bonus` | Integer | pode ser negativo (penalidade) |

### Sub-entidade: `RacaClassePermitida`
| campo | tipo | observação |
|---|---|---|
| `raca` | FK → Raca | |
| `classe` | FK → ClassePersonagem | classe que esta raça pode jogar |

### Sub-entidade: `RacaPontosConfig`
| campo | tipo | observação |
|---|---|---|
| `raca` | FK → Raca | |
| `nivel` | Integer | nível em que os pontos são dados |
| `pontosAtributo` | Integer | default 0 |
| `pontosVantagem` | Integer | default 0 |

### Sub-entidade: `RacaVantagemPreDefinida`
| campo | tipo | observação |
|---|---|---|
| `raca` | FK → Raca | |
| `nivel` | Integer | nível em que a vantagem é concedida |
| `vantagemConfig` | FK → VantagemConfig | vantagem racial automática |

---

## DTO atual

| DTO | campos |
|---|---|
| `RacaConfigDTO` | `nome`, `descricao`, `ordemExibicao` |
| `BonusAtributoDTO` | `abreviacaoAtributo`, `bonus` |

Bônus raciais: `getDefaultBonusRaciais()` → `Map<String nomeRaca, List<BonusAtributoDTO>>`

---

## Dados atuais no provider

| Raça | descricao | ordem | bônus atributos |
|---|---|---|---|
| Humano | Raça versátil e adaptável | 1 | nenhum |
| Elfo | Seres longevos com afinidade mágica | 2 | AGI +2, VIG -1 |
| Anão | Raça resistente e trabalhadora | 3 | VIG +2, AGI -1 |
| Meio-Elfo | Híbrido entre humano e elfo | 4 | AGI +1, INT +1 |

> Sub-entidades `RacaClassePermitida`, `RacaPontosConfig`, `RacaVantagemPreDefinida` estão **todas vazias**.

---

## O que falta / revisar

- [ ] **`RacaClassePermitida`** — todas as raças podem jogar todas as classes? Ou Anão não pode ser Mago? Definir restrições.
- [ ] **`RacaPontosConfig`** — alguma raça começa com pontos extras de atributo ou vantagem? (ex: Humano ganha pontos bônus?)
- [ ] **`RacaVantagemPreDefinida`** — vantagens raciais automáticas (ex: Elfo nasce com "Visão no Escuro"?)
- [ ] **Novas raças** — adicionar mais (Orc, Halfling, Gnomo, Draconato, etc.)?
- [ ] **Bônus raciais** — revisar se os valores atuais estão corretos para o sistema Klayrah
- [ ] **`getDefaultRacaPontos()`** — retorna `Map.of()` (vazio). Implementar junto com RacaPontosConfig.
