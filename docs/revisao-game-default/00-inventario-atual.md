# DefaultGameConfigProvider — Snapshot Completo

> Tudo que `DefaultGameConfigProviderImpl` popula hoje, config por config.

---

## 1. AtributoConfig (7)
| nome | abrev | descricao | formulaImpeto | unidade | ordem |
|---|---|---|---|---|---|
| Força | FOR | Capacidade física bruta, determina capacidade de carga | `total * 3` | kg | 1 |
| Agilidade | AGI | Velocidade e reflexos, determina deslocamento | `total / 3` | metros | 2 |
| Vigor | VIG | Resistência física, redução de dano físico | `total / 10` | RD | 3 |
| Sabedoria | SAB | Resistência mágica, redução de dano mágico | `total / 10` | RDM | 4 |
| Intuição | INTU | Sorte e percepção instintiva, pontos de sorte | `min(total / 20, 3)` | pontos | 5 |
| Inteligência | INT | Capacidade de comando e raciocínio | `total / 20` | comando | 6 |
| Astúcia | AST | Pensamento estratégico e tático | `total / 10` | estratégia | 7 |

> **NÃO populado**: `valorMinimo` (default 0), `valorMaximo` (default 999)

---

## 2. TipoAptidao (2 — hardcoded no serviço, não no provider)
| nome | descricao | ordem |
|---|---|---|
| FISICA | Aptidões físicas e corporais | 1 |
| MENTAL | Aptidões mentais e sociais | 2 |

---

## 3. AptidaoConfig (24)
**FISICA (12):** Acrobacia · Guarda · Aparar · Atletismo · Resvalar · Resistência · Perseguição · Natação · Furtividade · Prestidigitação · Conduzir · Arte da Fuga

**MENTAL (12):** Idiomas · Observação · Falsificar · Prontidão · Auto Controle · Sentir Motivação · Sobrevivência · Investigar · Blefar · Atuação · Diplomacia · Operação de Mecanismos

> Todos têm `nome`, `tipo`, `descricao`, `ordemExibicao`

---

## 4. NivelConfig (36 — níveis 0 a 35)
| nivel | xpNecessaria | pontosAtributo | pontosAptidao | limitadorAtributo | permitirRenascimento |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 10 | false |
| 1 | 1.000 | 3 | 3 | 10 | false |
| 2–20 | 3.000→210.000 | 3 | 3 | 50 | false |
| 21–25 | 231.000→325.000 | 3 | 3 | 75 | false |
| 26–30 | 351.000→465.000 | 3 | 3 | 100 | false |
| 31–35 | 496.000→595.000 | 3 | 3 | 120 | **true** |

> **Nota**: `pontosVantagem` existe no DTO mas é ignorado pelo initializer (não existe na entidade).
> `permitirRenascimento` calculado inline: `nivel >= 31`.

---

## 5. LimitadorConfigDTO (5 faixas — **NÃO usado pelo initializer!**)
| nivelInicio | nivelFim | limiteAtributo |
|---|---|---|
| 0 | 1 | 10 |
| 2 | 20 | 50 |
| 21 | 25 | 75 |
| 26 | 30 | 100 |
| 31 | 35 | 120 |

> ⚠️ `getDefaultLimitadores()` existe mas **nunca é chamado** pelo initializer. O limitador vem do `NivelConfigDTO.limitadorAtributo`.

---

## 6. ClassePersonagem (12)
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

> **NÃO populado**: ClasseBonus, ClasseAptidaoBonus, ClassePontosConfig, ClasseVantagemPreDefinida

---

## 7. Raca (4)
| nome | descricao | ordem | bonusAtributos |
|---|---|---|---|
| Humano | Raça versátil e adaptável | 1 | nenhum |
| Elfo | Seres longevos com afinidade mágica | 2 | AGI +2, VIG -1 |
| Anão | Raça resistente e trabalhadora | 3 | VIG +2, AGI -1 |
| Meio-Elfo | Híbrido entre humano e elfo | 4 | AGI +1, INT +1 |

> **NÃO populado**: RacaClassePermitida, RacaPontosConfig, RacaVantagemPreDefinida

---

## 8. DadoProspeccaoConfig (6)
| nome | numeroFaces | ordem |
|---|---|---|
| d3 | 3 | 1 |
| d4 | 4 | 2 |
| d6 | 6 | 3 |
| d8 | 8 | 4 |
| d10 | 10 | 5 |
| d12 | 12 | 6 |

> **NÃO populado**: `descricao`

---

## 9. GeneroConfig (3)
Masculino (1) · Feminino (2) · Outro (3) — só `nome` e `ordemExibicao`. **Falta**: `descricao`.

---

## 10. IndoleConfig (3)
Bom (1) · Mau (2) · Neutro (3) — só `nome` e `ordemExibicao`. **Falta**: `descricao`.

---

## 11. PresencaConfig (4)
Bom (1) · Leal (2) · Caótico (3) · Neutro (4) — só `nome` e `ordemExibicao`. **Falta**: `descricao`.

---

## 12. MembroCorpoConfig (7)
| nome | porcentagemVida | ordem |
|---|---|---|
| Cabeça | 0.75 | 1 |
| Tronco | 0.35 | 2 |
| Braço Direito | 0.10 | 3 |
| Braço Esquerdo | 0.10 | 4 |
| Perna Direita | 0.10 | 5 |
| Perna Esquerda | 0.10 | 6 |
| Sangue | 1.00 | 9 |

---

## 13. BonusConfig (9)
| nome | sigla | formulaBase | ordem |
|---|---|---|---|
| B.B.A | BBA | `(FOR + AGI) / 3` | 1 |
| B.B.M | BBM | `(SAB + INT) / 3` | 2 |
| Defesa | DEF | `VIG / 5` | 3 |
| Esquiva | ESQ | `AGI / 5` | 4 |
| Iniciativa | INI | `INTU / 5` | 5 |
| Percepção | PER | `INTU / 3` | 6 |
| Raciocínio | RAC | `INT / 3` | 7 |
| Bloqueio | BLO | `VIG / 3` | 8 |
| Reflexo | REF | `AGI / 3` | 9 |

> **NÃO populado**: `descricao`

---

## 14. PontosVantagemConfig (8)
| nivel | pontosGanhos |
|---|---|
| 1 | 6 |
| 5 | 3 |
| 10 | 10 |
| 15 | 3 |
| 20 | 10 |
| 25 | 3 |
| 30 | 15 |
| 35 | 3 |

---

## 15. CategoriaVantagem (8)
| nome | cor | ordem |
|---|---|---|
| Treinamento | #e74c3c | 1 |
| Ação | #e67e22 | 2 |
| Reação | #27ae60 | 3 |
| Vantagem de Atributo | #2980b9 | 4 |
| Vantagem Geral | #95a5a6 | 5 |
| Vantagem Histórica | #f39c12 | 6 |
| Vantagem de Renascimento | #1abc9c | 7 |
| Treinamento Mental | #8e44ad | 8 |

> **NÃO populado**: `descricao`

---

## 16. VantagemConfig (33)
| nome | tipoBonus (legado) | valorBonusFormula | custoBase | formulaCusto | nivelMin | podeEvoluir | nivelMaxVant | ordem |
|---|---|---|---|---|---|---|---|---|
| Treinamento de Combate Ofensivo | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 1 |
| Treinamento de Combate Defensivo | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 2 |
| Treinamento de Combate com Escudo | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 3 |
| Treinamento Mágico | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 4 |
| Treinamento de Poder Mental | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 5 |
| Treinamento de Liderança | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 6 |
| Treinamento de Meditação | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 7 |
| Ataque Adicional | ESPECIAL | — | 4 | `custo_base * nivel_vantagem` | 5 | true | — | 8 |
| Ataque Sentai | ESPECIAL | — | 5 | `custo_base * nivel_vantagem` | 10 | true | — | 9 |
| Contra-Ataque | ESPECIAL | — | 4 | `custo_base * nivel_vantagem` | 5 | true | — | 10 |
| Interceptação | ESPECIAL | — | 4 | `custo_base * nivel_vantagem` | 5 | true | — | 11 |
| Corpo Fechado | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 12 |
| Destreza Mental | ATRIBUTO_INT | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 13 |
| Destreza Física | ATRIBUTO_AGI | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 14 |
| Determinação Vital | ATRIBUTO_VIG | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 15 |
| Sexto Sentido | ATRIBUTO_INTU | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 16 |
| Inspiração Natural | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 17 |
| Saúde de Ferro | VIDA | `nivel_vantagem * 5` | — | `custo_base * nivel_vantagem` | 1 | true | — | 18 |
| Ambidestria | ESPECIAL | — | 4 | `custo_base` | 1 | false (presumido) | 1 | 19 |
| Riqueza | ESPECIAL | — | 3 | `custo_base * nivel_vantagem` | 1 | true | — | 20 |
| Capangas | ESPECIAL | — | 4 | `custo_base * nivel_vantagem` | 1 | true | — | 21 |
| Último Sigilo | ESPECIAL | — | 5 | `custo_base` | 1 | false (presumido) | 1 | 22 |
| Pensamento Bifurcado | ATRIBUTO_INT | `nivel_vantagem * 1` | — | `custo_base * nivel_vantagem` | 1 | true | — | 23 |
| Fortitude | ATRIBUTO_VIG | `nivel_vantagem * 1` | — | `custo_base * nivel_vantagem` | 1 | true | — | 24 |
| Força Aprimorada | ATRIBUTO_FORCA | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 25 |
| Agilidade Aprimorada | ATRIBUTO_AGILIDADE | `nivel_vantagem * 2` | — | `custo_base * nivel_vantagem` | 1 | true | — | 26 |
| Ataque Aprimorado | BBA | `nivel_vantagem * 1` | — | `custo_base * nivel_vantagem` | 1 | true | — | 27 |
| Defesa Mágica | BBM | `nivel_vantagem * 1` | — | `custo_base * nivel_vantagem` | 1 | true | — | 28 |
| Golpe Crítico | CRITICO | `nivel_vantagem * 5` | — | `custo_base * nivel_vantagem` | 5 | true | — | 29 |
| Vida Extra | VIDA | `nivel_vantagem * 10` | — | `custo_base * nivel_vantagem` | 1 | true | — | 30 |
| Essência Ampliada | ESSENCIA | `nivel_vantagem * 5` | — | `custo_base * nivel_vantagem` | 1 | true | — | 31 |
| Visão no Escuro | ESPECIAL | — | 5 | `custo_base` | 31 | false | 1 | 32 |
| Resistência a Veneno | ESPECIAL | — | 4 | `custo_base` | 31 | false | 1 | 33 |

> **NÃO populado**: `sigla`, `descricaoEfeito`, `categoriaVantagem` (FK), `VantagemEfeito`, `VantagemPreRequisito`, `tipoVantagem`
> ⚠️ `tipoBonus` e `valorBonusFormula` no DTO são campos **legados** — a entidade não os tem. Devem virar `VantagemEfeito`.

---

## 17. RaridadeItemConfig (7)
| nome | cor | ordem | podeJogadorAdicionar | bonusAtributo min/max | bonusDerivado min/max | descricao |
|---|---|---|---|---|---|---|
| Comum | #9d9d9d | 1 | true | 0/0 | 0/0 | Itens mundanos sem encantamento |
| Incomum | #1eff00 | 2 | false | 1/1 | 1/1 | Levemente encantado ou de qualidade excepcional |
| Raro | #0070dd | 3 | false | 1/2 | 1/2 | Encantamento moderado, raramente encontrado |
| Muito Raro | #a335ee | 4 | false | 2/3 | 2/3 | Encantamento poderoso, obra de artesão mestre |
| Epico | #ff8000 | 5 | false | 3/4 | 3/4 | Artefato de grande poder, história própria |
| Lendario | #e6cc80 | 6 | false | 4/5 | 4/5 | Um dos poucos existentes no mundo |
| Unico | #e268a8 | 7 | false | 0/0 | 0/0 | Criação única do Mestre, sem referência de custo |

> ⚠️ Nomes com acentuação faltando: "Epico" (deveria ser "Épico"), "Lendario" ("Lendário"), "Unico" ("Único")

---

## 18. TipoItemConfig (20)
| nome | categoria | subcategoria | duasMaos | ordem |
|---|---|---|---|---|
| Espada Curta | ARMA | ESPADA | false | 1 |
| Espada Longa | ARMA | ESPADA | false | 2 |
| Espada Dupla | ARMA | ESPADA | true | 3 |
| Arco Curto | ARMA | ARCO | true | 4 |
| Arco Longo | ARMA | ARCO | true | 5 |
| Adaga | ARMA | ADAGA | false | 6 |
| Machado de Batalha | ARMA | MACHADO | false | 7 |
| Machado Grande | ARMA | MACHADO | true | 8 |
| Martelo de Guerra | ARMA | MARTELO | false | 9 |
| Cajado | ARMA | CAJADO | true | 10 |
| Lanca | ARMA | LANCA | false | 11 |
| Armadura Leve | ARMADURA | ARMADURA_LEVE | false | 12 |
| Armadura Media | ARMADURA | ARMADURA_MEDIA | false | 13 |
| Armadura Pesada | ARMADURA | ARMADURA_PESADA | false | 14 |
| Escudo | ARMADURA | ESCUDO | false | 15 |
| Anel | ACESSORIO | ANEL | false | 16 |
| Amuleto | ACESSORIO | AMULETO | false | 17 |
| Pocao | CONSUMIVEL | POCAO | false | 18 |
| Municao | CONSUMIVEL | MUNICAO | false | 19 |
| Equipamento de Aventura | AVENTURA | OUTROS | false | 20 |

> ⚠️ Nomes com acentuação faltando: "Lanca" ("Lança"), "Armadura Media" ("Média"), "Pocao" ("Poção"), "Municao" ("Munição")
> **NÃO populado**: `descricao`

---

## 19. ItemConfig (40)

### Armas (15)
| nome | raridade | tipo | peso | valor | nivelMin | propriedades | efeitos |
|---|---|---|---|---|---|---|---|
| Adaga | Comum | Adaga | 0.45 | 2 | 1 | finura, arremesso, leve | — |
| Espada Curta | Comum | Espada Curta | 0.90 | 10 | 1 | finura, leve | — |
| Espada Longa | Comum | Espada Longa | 1.36 | 15 | 1 | versatil | — |
| Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | versatil, magica | BBA +1 |
| Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | versatil, magica | BBA +2 |
| Machadinha | Comum | Machado de Batalha | 0.90 | 5 | 1 | leve, arremesso | — |
| Machado de Batalha | Comum | Machado de Batalha | 1.80 | 10 | 1 | versatil | — |
| Machado Grande | Comum | Machado Grande | 3.17 | 30 | 3 | pesado, duas maos | — |
| Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | 1 | versatil | — |
| Arco Curto | Comum | Arco Curto | 0.90 | 25 | 1 | duas maos, municao | — |
| Arco Longo | Comum | Arco Longo | 1.80 | 50 | 2 | duas maos, municao, pesado | — |
| Arco Longo +1 | Incomum | Arco Longo | 1.80 | 500 | 10 | duas maos, municao, magico | BBA +1 |
| Cajado de Madeira | Comum | Cajado | 1.80 | 5 | 1 | versatil, duas maos | — |
| Cajado Arcano +1 | Incomum | Cajado | 2.00 | 500 | 10 | magico, foco arcano | BBM +1 |
| Lanca | Comum | Lanca | 1.36 | 1 | 1 | arremesso, versatil | — |

### Armaduras e Escudos (10)
| nome | raridade | tipo | peso | valor | efeitos |
|---|---|---|---|---|---|
| Gibao de Couro | Comum | Armadura Leve | 4.50 | 10 | DEF +1 |
| Couro Batido | Comum | Armadura Leve | 11.30 | 45 | DEF +2 |
| Camisao de Malha | Comum | Armadura Media | 13.60 | 50 | DEF +3 |
| Cota de Escamas | Comum | Armadura Media | 20.40 | 50 | DEF +4 |
| Cota de Malha | Comum | Armadura Pesada | 27.20 | 75 | DEF +5 |
| Meia Placa | Comum | Armadura Pesada | 19.90 | 750 | DEF +5, Reflexo +1 |
| Placa Completa | Raro | Armadura Pesada | 29.50 | 1500 | DEF +6 |
| Escudo de Madeira | Comum | Escudo | 2.72 | 10 | BLO +1 |
| Escudo de Aco | Comum | Escudo | 2.72 | 20 | BLO +2 |
| Escudo Enfeiticado +1 | Incomum | Escudo | 2.72 | 500 | BLO +2, DEF +1 |

### Acessórios (5)
| nome | raridade | valor | nivelMin | efeitos |
|---|---|---|---|---|
| Anel da Forca +1 | Raro | 2000 | 5 | FOR atrib +1 |
| Anel de Protecao +1 | Raro | 2000 | 5 | DEF +1, BLO +1 |
| Amuleto de Saude | Incomum | 500 | 3 | VIDA +5 |
| Amuleto da Essencia | Incomum | 500 | 3 | ESSENCIA +5 |
| Manto de Elvenkind | Muito Raro | 5000 | — | ESQ +3, PER +2 |

### Consumíveis (5)
Poção de Cura Menor (25g) · Poção de Cura (50g) · Poção de Cura Superior (200g) · Flecha Comum 20un (1g) · Virote 20un (1g)

### Equipamentos de Aventura (5)
Kit de Aventureiro · Kit de Curandeiro · Kit de Ladroa · Lanterna Bullseye · Tomo Arcano

> **NÃO populado**: `descricao` de nenhum item, `ItemRequisito` de nenhum item
> ⚠️ Nomes com acentuação faltando: "Lanca", "Camisao", "Pocao", "Aco", etc.
