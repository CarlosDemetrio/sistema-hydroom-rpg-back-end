# ClassePersonagem

> Classes do personagem (Guerreiro, Mago, etc.). Cada classe tem bônus, aptidões, vantagens e pontos especiais.

---

## Entidade: `ClassePersonagem`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String | ✅ | — |
| `descricao` | String (TEXT) | — | — |
| `ordemExibicao` | Integer | ✅ | `0` |
| `bonusConfig` | Set<ClasseBonus> | — | vazio | bônus em derivados (BBA, DEF etc.) por nível |
| `aptidaoBonus` | Set<ClasseAptidaoBonus> | — | vazio | bônus em aptidões específicas |
| `pontosConfig` | Set<ClassePontosConfig> | — | vazio | pontos extras (atributo, vantagem) por nível |
| `vantagensPreDefinidas` | Set<ClasseVantagemPreDefinida> | — | vazio | vantagens com que a classe começa |

### Sub-entidade: `ClasseBonus`
| campo | tipo | observação |
|---|---|---|
| `classe` | FK → ClassePersonagem | |
| `bonus` | FK → BonusConfig | qual derivado recebe o bônus |
| `valorPorNivel` | BigDecimal | valor adicionado ao derivado por nível |

### Sub-entidade: `ClasseAptidaoBonus`
| campo | tipo | observação |
|---|---|---|
| `classe` | FK → ClassePersonagem | |
| `aptidao` | FK → AptidaoConfig | qual aptidão recebe o bônus |
| `bonus` | Integer | valor de bônus fixo |

### Sub-entidade: `ClassePontosConfig`
| campo | tipo | observação |
|---|---|---|
| `classePersonagem` | FK → ClassePersonagem | |
| `nivel` | Integer | nível em que esses pontos são concedidos |
| `pontosAtributo` | Integer | default 0 |
| `pontosVantagem` | Integer | default 0 |

### Sub-entidade: `ClasseVantagemPreDefinida`
| campo | tipo | observação |
|---|---|---|
| `classePersonagem` | FK → ClassePersonagem | |
| `nivel` | Integer | nível em que a vantagem é concedida |
| `vantagemConfig` | FK → VantagemConfig | qual vantagem |

---

## DTO atual: `ClasseConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ |
| `descricao` | `descricao` | ✅ |
| `ordemExibicao` | `ordemExibicao` | ✅ |
| *(ausente)* | `bonusConfig` | 🚫 **TODO PA-015-01** |
| *(ausente)* | `aptidaoBonus` | 🚫 **TODO PA-015-01** |
| *(ausente)* | `pontosConfig` | 🚫 **TODO PA-015-01** — `getDefaultClassePontos()` retorna `Map.of()` |
| *(ausente)* | `vantagensPreDefinidas` | 🚫 **TODO PA-015-01** |

---

## Dados atuais no provider

| nome | descricao | ordem |
|---|---|---|
| Guerreiro | Especialista em combate corpo a corpo | 1 |
| Arqueiro | Mestre em combate à distância | 2 |
| Monge | Lutador desarmado com disciplina espiritual | 3 |
| Berserker | Guerreiro selvagem de fúria incontrolável | 4 |
| Assassino | Especialista em ataques furtivos e letais | 5 |
| Fauno (Herdeiro) | Herdeiro com poderes especiais | 6 |
| Mago | Conjurador de magias arcanas | 7 |
| Feiticeiro | Usuário de magia inata | 8 |
| Necromante | Manipulador de forças da morte | 9 |
| Sacerdote | Servo divino com poderes sagrados | 10 |
| Ladrão | Especialista em subterfúgio e furto | 11 |
| Negociante | Mestre em comércio e persuasão | 12 |

> Todos os campos de sub-entidade estão **vazios**.

---

## O que falta / revisar

- [ ] **`ClasseBonus`** — definir quais derivados cada classe bônus (ex: Guerreiro ganha mais BBA por nível?)
- [ ] **`ClasseAptidaoBonus`** — definir aptidões com bônus por classe (ex: Ladrão tem bônus em Furtividade?)
- [ ] **`ClassePontosConfig`** — definir pontos extras por nível por classe (ex: Mago ganha mais pontos de vantagem mágica?)
- [ ] **`ClasseVantagemPreDefinida`** — definir vantagens iniciais por classe (ex: Guerreiro começa com "Treinamento de Combate Ofensivo"?)
- [ ] **DTO** — precisará ser expandido para incluir sub-entidades. Hoje só tem nome/descricao/ordem.
- [ ] **`getDefaultClassePontos()`** — método retorna `Map.of()` (vazio). Implementar quando os dados acima forem definidos.
