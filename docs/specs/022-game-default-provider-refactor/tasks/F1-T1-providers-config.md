# F1-T1 — 10 Providers de Configs (exceto vantagens)

## Objetivo
Criar 10 classes `@Component` no pacote `config/defaults/`, cada uma com os dados dos CSVs hardcoded em Java.

## Regras gerais
- `@Component` em cada classe
- Dados via `List.of()` / `Map.of()` imutaveis
- Sem estado mutavel
- Valores exatos dos CSVs — copiar fielmente
- Manter factory methods existentes dos DTOs (`DTO.of(...)`, `DTO.builder()`)

## Validacao
```bash
./mvnw compile
```

## Commit
```
refactor(defaults): cria 10 providers em config/defaults/ [Copilot R07 T1]
```

---

## P1 — `DefaultAtributosProvider` (7 entradas)

> Arquivo: `config/defaults/DefaultAtributosProvider.java`
> Metodo: `get()` → `List<AtributoConfigDTO>`
> CSV fonte: `08-atributo-config.csv`

| sigla | nome | descricao | formulaImpeto | unidadeImpeto | valorMinimo | valorMaximo | ordem |
|-------|------|-----------|---------------|---------------|-------------|-------------|-------|
| FOR | Força | Capacidade física bruta, determina capacidade de carga | total * 3 | kg | 1 | 120 | 1 |
| AGI | Agilidade | Velocidade e reflexos, determina deslocamento | total / 3 | metros | 1 | 120 | 2 |
| VIG | Vigor | Resistência física, redução de dano físico | total / 10 | RD | 1 | 120 | 3 |
| SAB | Sabedoria | Resistência mágica, redução de dano mágico | total / 10 | RDM | 1 | 120 | 4 |
| INTU | Intuição | Sorte e percepção instintiva, pontos de sorte | min(total / 20, 3) | pontos | 1 | 120 | 5 |
| INT | Inteligência | Capacidade de comando e raciocínio | total / 20 | comando | 1 | 120 | 6 |
| AST | Astúcia | Pensamento estratégico e tático | total / 10 | estratégia | 1 | 120 | 7 |

---

## P2 — `DefaultBonusProvider` (9 entradas)

> Arquivo: `config/defaults/DefaultBonusProvider.java`
> Metodo: `get()` → `List<BonusConfigDTO>`
> CSV fonte: `09-bonus-config.csv`

| sigla | nome | formulaBase | descricao | ordem |
|-------|------|-------------|-----------|-------|
| BBA | B.B.A | (FOR + AGI) / 3 | Bônus base de ataque físico; derivado da Força e Agilidade do personagem | 1 |
| BBM | B.B.M | (SAB + INT) / 3 | Bônus base de ação mental ou mágica; derivado da Sabedoria e Inteligência | 2 |
| DEF | Defesa | VIG / 5 | Redução passiva de dano físico recebido; derivada do Vigor | 3 |
| ESQ | Esquiva | AGI / 5 | Valor de referência para desviar de ataques; derivado da Agilidade | 4 |
| INI | Iniciativa | INTU / 5 | Determina a ordem de ação em combate; derivada da Intuição | 5 |
| PER | Percepção | INTU / 3 | Capacidade de notar detalhes, ameaças e pistas no ambiente; derivada da Intuição | 6 |
| RAC | Raciocínio | INT / 3 | Qualidade do pensamento analítico e resolução de problemas; derivado da Inteligência | 7 |
| BLO | Bloqueio | VIG / 3 | Capacidade de absorver impactos com escudo ou arma; derivado do Vigor | 8 |
| REF | Reflexo | AGI / 3 | Velocidade de reação a eventos imprevistos ou ataques surpresa; derivado da Agilidade | 9 |

---

## P3 — `DefaultAptidoesProvider` (24 entradas)

> Arquivo: `config/defaults/DefaultAptidoesProvider.java`
> Metodo: `get()` → `List<AptidaoConfigDTO>`
> CSV fonte: `13-aptidao-config.csv`
> Tipos de aptidao: `FISICA`, `MENTAL` (referencia `01-tipo-aptidao.csv`)

### Fisicas (12)

| nome | tipo | descricao | ordem |
|------|------|-----------|-------|
| Acrobacia | FISICA | Execução de manobras ágeis como saltos, rolamentos e equilíbrio em terreno difícil | 1 |
| Guarda | FISICA | Técnica defensiva de posicionar corpo e armas para absorver ou redirecionar impactos | 2 |
| Aparar | FISICA | Desviar ou neutralizar ataques com arma ou escudo com precisão de timing | 3 |
| Atletismo | FISICA | Força física bruta aplicada a escalada, arremesso, luta corporal e resistência de esforço | 4 |
| Resvalar | FISICA | Técnica de esquiva dinâmica que usa o movimento do corpo para evitar golpes e projéteis | 5 |
| Resistência | FISICA | Capacidade de suportar condições extremas: fome, veneno, dor, temperatura e exaustão | 6 |
| Perseguição | FISICA | Habilidade de rastrear e perseguir alvos em movimento, ou de fugir eficientemente | 7 |
| Natação | FISICA | Habilidade de nadar e se mover em ambientes aquáticos, incluindo mergulho e combate na água | 8 |
| Furtividade | FISICA | Habilidade de mover-se silenciosamente, esconder-se e realizar ações sem ser detectado | 9 |
| Prestidigitação | FISICA | Destreza manual para realizar truques, pickpocket, esconder objetos e manipulação fina | 10 |
| Conduzir | FISICA | Habilidade de montar animais ou pilotar veículos, incluindo manobras em alta velocidade | 11 |
| Arte da Fuga | FISICA | Habilidade de escapar de amarras, algemas, prisões e outras situações de captura | 12 |

### Mentais (12)

| nome | tipo | descricao | ordem |
|------|------|-----------|-------|
| Idiomas | MENTAL | Conhecimento de idiomas estrangeiros, dialetos e sistemas de escrita do mundo de Klayrah | 13 |
| Observação | MENTAL | Percepção aguçada para notar detalhes, pistas ocultas e anomalias no ambiente | 14 |
| Falsificar | MENTAL | Habilidade de criar documentos falsos, imitar assinaturas e forjar selos oficiais | 15 |
| Prontidão | MENTAL | Estado de alerta elevado; evita ser surpreendido e age rapidamente em situações de crise | 16 |
| Auto Controle | MENTAL | Domínio das emoções e resistência à manipulação, medo, sedução e pressão psicológica | 17 |
| Sentir Motivação | MENTAL | Habilidade de perceber as verdadeiras intenções e emoções ocultas de outras pessoas | 18 |
| Sobrevivência | MENTAL | Conhecimento de orientação, caça, armadilhas e sobrevivência em ambientes hostis | 19 |
| Investigar | MENTAL | Habilidade de reunir pistas, interrogar testemunhas e deduzir conclusões a partir de evidências | 20 |
| Blefar | MENTAL | Capacidade de mentir convincentemente, criar desvios e manipular a percepção alheia | 21 |
| Atuação | MENTAL | Habilidade de interpretar personagens, disfarçar-se e convencer por meio de performance | 22 |
| Diplomacia | MENTAL | Arte da negociação, persuasão e mediação de conflitos com argumentos e charme | 23 |
| Operação de Mecanismos | MENTAL | Habilidade de operar, reparar e arrombar mecanismos, fechaduras e engenhocas | 24 |

---

## P4 — `DefaultNiveisProvider` (36 niveis + 5 tiers)

> Arquivo: `config/defaults/DefaultNiveisProvider.java`
> Metodos: `getNiveis()` → `List<NivelConfigDTO>`, `getLimitadores()` → `List<LimitadorConfigDTO>`
> CSV fonte: `11-nivel-config.csv`
>
> Formula XP: `xp(N) = N*(N+1)/2 * 1000` (triangular)
> Pontos por nivel: 3 atributo + 1 aptidao (exceto nivel 0)

### Tiers de Limitador (para `getLimitadores()`)

| faixa | limitador | descricao |
|-------|-----------|-----------|
| 0-1 | 10 | Tutorial |
| 2-20 | 50 | Progressao normal |
| 21-25 | 75 | Tier intermediario |
| 26-30 | 100 | Tier avancado |
| 31-35 | 120 | Renascimento |

### Niveis (para `getNiveis()`)

| nivel | xpNecessaria | pontosAtributo | pontosAptidao | limitadorAtributo |
|-------|-------------|----------------|---------------|-------------------|
| 0 | 0 | 0 | 0 | 10 |
| 1 | 1000 | 3 | 1 | 10 |
| 2 | 3000 | 3 | 1 | 50 |
| 3 | 6000 | 3 | 1 | 50 |
| 4 | 10000 | 3 | 1 | 50 |
| 5 | 15000 | 3 | 1 | 50 |
| 6 | 21000 | 3 | 1 | 50 |
| 7 | 28000 | 3 | 1 | 50 |
| 8 | 36000 | 3 | 1 | 50 |
| 9 | 45000 | 3 | 1 | 50 |
| 10 | 55000 | 3 | 1 | 50 |
| 11 | 66000 | 3 | 1 | 50 |
| 12 | 78000 | 3 | 1 | 50 |
| 13 | 91000 | 3 | 1 | 50 |
| 14 | 105000 | 3 | 1 | 50 |
| 15 | 120000 | 3 | 1 | 50 |
| 16 | 136000 | 3 | 1 | 50 |
| 17 | 153000 | 3 | 1 | 50 |
| 18 | 171000 | 3 | 1 | 50 |
| 19 | 190000 | 3 | 1 | 50 |
| 20 | 210000 | 3 | 1 | 50 |
| 21 | 231000 | 3 | 1 | 75 |
| 22 | 253000 | 3 | 1 | 75 |
| 23 | 276000 | 3 | 1 | 75 |
| 24 | 300000 | 3 | 1 | 75 |
| 25 | 325000 | 3 | 1 | 75 |
| 26 | 351000 | 3 | 1 | 100 |
| 27 | 378000 | 3 | 1 | 100 |
| 28 | 406000 | 3 | 1 | 100 |
| 29 | 435000 | 3 | 1 | 100 |
| 30 | 465000 | 3 | 1 | 100 |
| 31 | 496000 | 3 | 1 | 120 |
| 32 | 528000 | 3 | 1 | 120 |
| 33 | 561000 | 3 | 1 | 120 |
| 34 | 595000 | 3 | 1 | 120 |
| 35 | 630000 | 3 | 1 | 120 |

---

## P5 — `DefaultPontosVantagemProvider` (35 entradas)

> Arquivo: `config/defaults/DefaultPontosVantagemProvider.java`
> Metodo: `get()` → `List<PontosVantagemConfigDTO>`
> CSV fonte: `12-pontos-vantagem-config.csv`
> Total acumulado no nivel 35: 53 pontos

| nivel | pontosGanhos |
|-------|-------------|
| 1 | 6 |
| 2 | 0 |
| 3 | 0 |
| 4 | 0 |
| 5 | 3 |
| 6 | 0 |
| 7 | 0 |
| 8 | 0 |
| 9 | 0 |
| 10 | 10 |
| 11 | 0 |
| 12 | 0 |
| 13 | 0 |
| 14 | 0 |
| 15 | 3 |
| 16 | 0 |
| 17 | 0 |
| 18 | 0 |
| 19 | 0 |
| 20 | 10 |
| 21 | 0 |
| 22 | 0 |
| 23 | 0 |
| 24 | 0 |
| 25 | 3 |
| 26 | 0 |
| 27 | 0 |
| 28 | 0 |
| 29 | 0 |
| 30 | 15 |
| 31 | 0 |
| 32 | 0 |
| 33 | 0 |
| 34 | 0 |
| 35 | 3 |

> Milestones: niveis 1(6), 5(3), 10(10), 15(3), 20(10), 25(3), 30(15), 35(3). Demais = 0.

---

## P6 — `DefaultClassesProvider` (12 entradas)

> Arquivo: `config/defaults/DefaultClassesProvider.java`
> Metodo: `get()` → `List<ClasseConfigDTO>`
> CSV fonte: `15-classe-personagem.csv`

| nome | descricao | ordem |
|------|-----------|-------|
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

> Sub-entidades (bonus, aptidao-bonus, pontos, vantagens-predefinidas) estao vazias — TODO do PO.

---

## P7 — `DefaultRacasProvider` (6 racas + 12 bonus + 20 vantagens)

> Arquivo: `config/defaults/DefaultRacasProvider.java`
> Metodos: `getRacas()`, `getBonusRaciais()`, `getVantagensPreDefinidas()`
> CSV fonte: `16-raca.csv` + `16b-raca-bonus-atributo.csv` + `16e-raca-vantagem-predefinida.csv`

### Racas (para `getRacas()`)

| nome | descricao | ordem |
|------|-----------|-------|
| Humano | Raça versátil e adaptável, capaz de aprender qualquer arte ou ofício. Sua maior força é a adaptabilidade e a capacidade de superação. Não possuem vantagens raciais físicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliência e o potencial ilimitado da humanidade. | 1 |
| Karzarcryer | Descendentes de dragões do plano do fogo, os Karzarcryer possuem escamas ígnicas e sangue quente. Bônus: +8 Resistência (+24 VIG), -3 Percepção (-9 INTU). São conhecidos por seu temperamento explosivo (Antecedente: Falta de autocontrole) e pela Ignomia inicial 5, que representa sua reputação de destruição instintiva. Dominam o elemento fogo e possuem resistência sobrenatural ao calor. | 2 |
| Ikarúz | Raça de seres alados com afinidade à sabedoria celestial. Possuem asas funcionais e adaptação a diferentes altitudes. Bônus: +5 Sabedoria (+5 SAB), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Nanismo — apesar das asas grandiosas, seu corpo é pequeno e delicado, tornando-os vulneráveis em combate corpo a corpo. | 3 |
| Hankráz | Seres esguios de inteligência aguçada que habitam entre planos de existência paralelos. Seus piercings mágicos amplificam capacidades sobrenaturais. Bônus: +5 Inteligência (+5 INT), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Baixo vigor — corpos frágeis que compensam com mente analítica e habilidades multidimensionais. | 4 |
| Atlas | Gigantes de força incomparável, os Atlas são a raça mais fisicamente poderosa de Klayrah. Bônus: +8 Força (+8 FOR), -3 Inteligência (-3 INT). Antecedente: É burro — já representado pela penalidade em INT. São guerreiros natos mas carecem de sofisticação intelectual. Possuem capacidade de força máxima inata, ambidestria natural e um ataque adicional devastador. | 5 |
| Anakarys | Raça ágil de predadores naturais com garras, presas e instintos aguçados. Bônus: +3 Agilidade (+3 AGI), +2 Percepção (+6 INTU). Possuem armas naturais aprimoradas, formas únicas de deslocamento e um ataque adicional racial com restrições específicas de uso. | 6 |

### Bonus Raciais de Atributo (para `getBonusRaciais()`)

> Mapeamento: +3 Percepção = +9 INTU; +8 Resistência = +24 VIG. Bonus pode ser negativo.

| raca | atributoSigla | bonus |
|------|--------------|-------|
| Karzarcryer | VIG | +24 |
| Karzarcryer | INTU | -9 |
| Ikarúz | SAB | +5 |
| Ikarúz | INTU | +9 |
| Ikarúz | VIG | -9 |
| Hankráz | INT | +5 |
| Hankráz | INTU | +9 |
| Hankráz | VIG | -9 |
| Atlas | FOR | +8 |
| Atlas | INT | -3 |
| Anakarys | AGI | +3 |
| Anakarys | INTU | +6 |

> Humano: sem bonus/penalidades de atributo (compensado pelas 5 vantagens raciais).

### Vantagens Pre-definidas por Raca (para `getVantagensPreDefinidas()`)

| raca | vantagemNome | nivelInicial |
|------|-------------|-------------|
| Humano | Adaptabilidade Humana | 1 |
| Humano | Resiliência Humana | 1 |
| Humano | Versatilidade Humana | 1 |
| Humano | Espírito Inabalável | 1 |
| Humano | Legado de Civilização | 1 |
| Karzarcryer | Elemento Natural: Fogo | 1 |
| Karzarcryer | Imunidade Elemental: Fogo | 1 |
| Karzarcryer | Estômago de Dragão | 1 |
| Ikarúz | Membro Adicional: Asas | 1 |
| Ikarúz | Adaptação Atmosférica | 1 |
| Ikarúz | Combate Alado | 1 |
| Hankráz | Piercings Raciais | 1 |
| Hankráz | Corpo Esguio | 1 |
| Hankráz | Vagante entre Mundos | 1 |
| Atlas | Capacidade de Força Máxima | 1 |
| Atlas | Ambidestria | 1 |
| Atlas | Ataque Adicional | 1 |
| Anakarys | Armas Naturais Aprimoradas | 1 |
| Anakarys | Deslocamento Especial | 1 |
| Anakarys | Ataque Adicional Racial | 1 |

> **NOTA:** Atlas usa vantagens JA EXISTENTES (VCFM, VAMB, VAA) — nao sao INSOLITUS.
> Sub-entidades vazias (TODO PO): `16c-raca-classe-permitida`, `16d-raca-pontos-config`.

---

## P8 — `DefaultProspeccoesProvider` (6 entradas)

> Arquivo: `config/defaults/DefaultProspeccoesProvider.java`
> Metodo: `get()` → `List<ProspeccaoConfigDTO>`
> CSV fonte: `05-dado-prospeccao-config.csv`

| nome | numLados | descricao | ordem |
|------|----------|-----------|-------|
| d3 | 3 | Dado de 3 faces; incerteza mínima, usado em situações triviais ou de baixíssimo risco | 1 |
| d4 | 4 | Dado de 4 faces; pequena variação, para situações simples e controladas | 2 |
| d6 | 6 | Dado de 6 faces; o dado padrão do sistema, para situações cotidianas e moderadas | 3 |
| d8 | 8 | Dado de 8 faces; dificuldade moderada, para desafios com risco real | 4 |
| d10 | 10 | Dado de 10 faces; alta dificuldade, para situações arriscadas ou complexas | 5 |
| d12 | 12 | Dado de 12 faces; extremo ou raro, reservado para proezas épicas e eventos críticos | 6 |

---

## P9 — `DefaultConfigSimpleProvider` (3+3+4+7 = 17 entradas)

> Arquivo: `config/defaults/DefaultConfigSimpleProvider.java`
> Metodos: `getGeneros()`, `getIndoles()`, `getPresencas()`, `getMembrosCorpo()`
> CSV fonte: `02,03,04,10-*.csv`

### Generos (para `getGeneros()`)

| nome | descricao | ordem |
|------|-----------|-------|
| Masculino | Personagem de identidade masculina | 1 |
| Feminino | Personagem de identidade feminina | 2 |
| Outro | Personagem com identidade de gênero não binária ou indefinida | 3 |

### Indoles (para `getIndoles()`)

| nome | descricao | ordem |
|------|-----------|-------|
| Bom | Movido por compaixão e altruísmo; tende a ajudar os necessitados e defender os fracos | 1 |
| Mau | Guiado por ambição cruel ou egoísmo; usa os outros como meios para seus próprios fins | 2 |
| Neutro | Sem viés moral definido; age conforme as circunstâncias, nem bondoso nem cruel | 3 |

### Presencas (para `getPresencas()`)

| nome | descricao | ordem |
|------|-----------|-------|
| Bom | Aura de benevolência e proteção; aqueles próximos sentem conforto e confiança | 1 |
| Leal | Aura de ordem e autoridade; transmite disciplina e respeito pelas leis e hierarquias | 2 |
| Caótico | Aura imprevisível e perturbadora; semeia instabilidade e desconforto ao redor | 3 |
| Neutro | Aura equilibrada, sem inclinação evidente; passa despercebido pela maioria | 4 |

### Membros do Corpo (para `getMembrosCorpo()`)

> `porcentagemVida`: BigDecimal 0.01–1.00. Ordens 7-8 disponiveis para Mestre adicionar.

| nome | porcentagemVida | descricao | ordem |
|------|----------------|-----------|-------|
| Cabeça | 0.75 | Ao chegar a 0 VT, o personagem fica incapacitado; pode resultar em morte instantânea ou estado vegetativo dependendo do dano excedente | 1 |
| Tronco | 0.35 | Ao chegar a 0 VT, o personagem entra em colapso e fica totalmente incapaz de agir | 2 |
| Braço Direito | 0.10 | Ao chegar a 0 VT, o membro fica inutilizado; ações que o exigem sofrem penalidade grave | 3 |
| Braço Esquerdo | 0.10 | Ao chegar a 0 VT, o membro fica inutilizado; ações que o exigem sofrem penalidade grave | 4 |
| Perna Direita | 0.10 | Ao chegar a 0 VT, a mobilidade é severamente reduzida; penalidade no deslocamento e ações de esquiva | 5 |
| Perna Esquerda | 0.10 | Ao chegar a 0 VT, a mobilidade é severamente reduzida; penalidade no deslocamento e ações de esquiva | 6 |
| Sangue | 1.00 | Representa o pool total de vida do personagem; ao chegar a 0 o personagem morre por sangramento crítico | 9 |

---

## P10 — `DefaultItensProvider` (7 raridades + 20 tipos + 40 itens + 29 efeitos + 7 requisitos)

> Arquivo: `config/defaults/DefaultItensProvider.java`
> Metodos: `getRaridades()`, `getTipos()`, `getItens()`, `getEfeitos()`, `getRequisitos()`
> CSV fonte: `07,14,18,18b,18c-*.csv`

### Raridades (para `getRaridades()`)

| nome | cor | ordem | podeJogadorAdicionar | bonusAtribMin | bonusAtribMax | bonusDeriMin | bonusDeriMax | descricao |
|------|-----|-------|---------------------|---------------|---------------|-------------|-------------|-----------|
| Comum | #9d9d9d | 1 | true | 0 | 0 | 0 | 0 | Itens mundanos sem encantamento |
| Incomum | #1eff00 | 2 | false | 1 | 1 | 1 | 1 | Levemente encantado ou de qualidade excepcional |
| Raro | #0070dd | 3 | false | 1 | 2 | 1 | 2 | Encantamento moderado, raramente encontrado |
| Muito Raro | #a335ee | 4 | false | 2 | 3 | 2 | 3 | Encantamento poderoso, obra de artesão mestre |
| Épico | #ff8000 | 5 | false | 3 | 4 | 3 | 4 | Artefato de grande poder, história própria |
| Lendário | #e6cc80 | 6 | false | 4 | 5 | 4 | 5 | Um dos poucos existentes no mundo |
| Único | #e268a8 | 7 | false | 0 | 0 | 0 | 0 | Criação única do Mestre, sem referência de custo |

### Tipos de Item (para `getTipos()`)

| nome | categoria | subcategoria | requerDuasMaos | ordem |
|------|-----------|-------------|----------------|-------|
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
| Lança | ARMA | LANCA | false | 11 |
| Armadura Leve | ARMADURA | ARMADURA_LEVE | false | 12 |
| Armadura Média | ARMADURA | ARMADURA_MEDIA | false | 13 |
| Armadura Pesada | ARMADURA | ARMADURA_PESADA | false | 14 |
| Escudo | ARMADURA | ESCUDO | false | 15 |
| Anel | ACESSORIO | ANEL | false | 16 |
| Amuleto | ACESSORIO | AMULETO | false | 17 |
| Poção | CONSUMIVEL | POCAO | false | 18 |
| Munição | CONSUMIVEL | MUNICAO | false | 19 |
| Equipamento de Aventura | AVENTURA | OUTROS | false | 20 |

### Itens — Armas (15)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | descricao | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-----------|-------|
| Adaga | Comum | Adaga | 0.45 | 2 | | 1 | finura, arremesso, leve | Lâmina curta e balanceada, letal em espaços confinados. Favorita de ladrões e assassinos. | 1 |
| Espada Curta | Comum | Espada Curta | 0.90 | 10 | | 1 | finura, leve | Lâmina afiada e ágil, ideal para combatentes que priorizam velocidade sobre alcance. | 2 |
| Espada Longa | Comum | Espada Longa | 1.36 | 15 | | 1 | versátil | Lâmina versátil e equilibrada, padrão dos combatentes treinados do reino. | 3 |
| Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | 1 | versátil, mágica | Espada longa encantada por artesão élfico, cuja lâmina vibra levemente ao toque. | 4 |
| Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | 5 | versátil, mágica | Obra mestra de forjaria arcana, cuja lâmina corta com precisão sobre-humana. | 5 |
| Machadinha | Comum | Machado de Batalha | 0.90 | 5 | | 1 | leve, arremesso | Machado leve de cabo curto, eficaz tanto em combate quanto arremessado a média distância. | 6 |
| Machado de Batalha | Comum | Machado de Batalha | 1.80 | 10 | | 1 | versátil | Lâmina larga de ferro que vence armaduras com golpes brutais e devastadores. | 7 |
| Machado Grande | Comum | Machado Grande | 3.17 | 30 | | 3 | pesado, duas mãos | Machado de duas mãos com lâmina dupla, capaz de partir escudos. Exige braços poderosos. | 8 |
| Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | | 1 | versátil | Cabeça de ferro maciço que esmaga ossos e amassa armaduras com brutalidade eficaz. | 9 |
| Arco Curto | Comum | Arco Curto | 0.90 | 25 | | 1 | duas mãos, munição | Arco compacto de madeira curvada, preciso em curtas e médias distâncias de combate. | 10 |
| Arco Longo | Comum | Arco Longo | 1.80 | 50 | | 2 | duas mãos, munição, pesado | Arco alto de teixo com alcance e penetração superiores, exige braço forte para disparar. | 11 |
| Arco Longo +1 | Incomum | Arco Longo | 1.80 | 500 | 10 | 4 | duas mãos, munição, mágico | Arco encantado com runas de precisão, cujas flechas raramente desviam do alvo. | 12 |
| Cajado de Madeira | Comum | Cajado | 1.80 | 5 | | 1 | versátil, duas mãos | Bastão robusto de carvalho que serve como arma e foco para canalização de magias básicas. | 13 |
| Cajado Arcano +1 | Incomum | Cajado | 2.00 | 500 | 10 | 3 | mágico, foco arcano | Cajado gravado com siglos arcanos que amplifica o poder dos feitiços canalizados. | 14 |
| Lança | Comum | Lança | 1.36 | 1 | | 1 | arremesso, versátil | Haste longa com ponta de ferro, temida na linha de batalha e eficaz quando arremessada. | 15 |

### Itens — Armaduras e Escudos (10)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | descricao | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-----------|-------|
| Gibão de Couro | Comum | Armadura Leve | 4.50 | 10 | | 1 | armadura leve | Proteção simples de couro curtido que não restringe movimentos. Acessível a iniciantes. | 16 |
| Couro Batido | Comum | Armadura Leve | 11.30 | 45 | | 1 | armadura leve | Couro reforçado com aplicações de metal, boa proteção sem comprometer a agilidade. | 17 |
| Camisão de Malha | Comum | Armadura Média | 13.60 | 50 | | 2 | armadura média | Cota de anéis de ferro entrelaçados que equilibra proteção robusta com liberdade de movimentos. | 18 |
| Cota de Escamas | Comum | Armadura Média | 20.40 | 50 | | 3 | armadura média, desvantagem Furtividade | Placas metálicas sobrepostas que deflectem golpes com eficiência, mas restringem movimentos furtivos. | 19 |
| Cota de Malha | Comum | Armadura Pesada | 27.20 | 75 | | 4 | armadura pesada, Força mínima | Armadura de malha densa que cobre o corpo inteiro, exige robustez física considerável para ser usada. | 20 |
| Meia Placa | Comum | Armadura Pesada | 19.90 | 750 | | 5 | armadura pesada | Conjunto de placas metálicas sobre cota de malha, proteção elevada com mobilidade razoável. | 21 |
| Placa Completa | Raro | Armadura Pesada | 29.50 | 1500 | 15 | 7 | armadura pesada, mágica | Armadura completa de placas encantadas que cobre o guerreiro do topo ao fundo, a proteção definitiva. | 22 |
| Escudo de Madeira | Comum | Escudo | 2.72 | 10 | | 1 | escudo | Escudo redondo de madeira reforçada, leve e confiável contra ataques comuns. | 23 |
| Escudo de Aço | Comum | Escudo | 2.72 | 20 | | 1 | escudo | Escudo de aço polido e resistente a lâminas, padrão das guarnições do reino. | 24 |
| Escudo Enfeitiçado +1 | Incomum | Escudo | 2.72 | 500 | 10 | 3 | escudo, mágico | Escudo com encantamento protetor que pulsa levemente ao receber golpes poderosos. | 25 |

### Itens — Acessorios e Magicos (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | descricao | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-----------|-------|
| Anel da Força +1 | Raro | Anel | 0.01 | 2000 | | 5 | mágico, único | Anel de ônix engastado em prata que amplifica a musculatura e o vigor físico do portador. | 26 |
| Anel de Proteção +1 | Raro | Anel | 0.01 | 2000 | | 5 | mágico | Anel gravado com runas de proteção que cria um campo mágico sutil ao redor do portador. | 27 |
| Amuleto de Saúde | Incomum | Amuleto | 0.05 | 500 | | 3 | mágico | Pingente de âmbar com fragmento de raiz de mandrágora que fortalece a vitalidade do usuário. | 28 |
| Amuleto da Essência | Incomum | Amuleto | 0.05 | 500 | | 3 | mágico | Cristal azulado em corrente de prata que potencializa a reserva arcana de conjuradores. | 29 |
| Manto de Elvenkind | Muito Raro | Amuleto | 0.45 | 5000 | | 7 | mágico | Capa tecida com fios de seda élfica que mimetiza o ambiente, tornando o portador difuso à vista. | 30 |

### Itens — Consumiveis (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | descricao | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-----------|-------|
| Poção de Cura Menor | Comum | Poção | 0.45 | 25 | 1 | 1 | consumível, recupera 5 de vida | Líquido rubro com propriedades regenerativas leves, fecha ferimentos superficiais com rapidez. | 31 |
| Poção de Cura | Comum | Poção | 0.45 | 50 | 1 | 1 | consumível, recupera 10 de vida | Elixir de ervas medicinais que restaura danos moderados e alivia a dor rapidamente. | 32 |
| Poção de Cura Superior | Incomum | Poção | 0.45 | 200 | 1 | 3 | consumível, recupera 25 de vida | Elixir concentrado de alta potência capaz de fechar até ferimentos graves em instantes. | 33 |
| Flecha Comum (20) | Comum | Munição | 0.45 | 1 | | 1 | munição para arcos | Flechas de madeira com ponta de ferro e penas de ganso, vendidas em pacotes de 20. | 34 |
| Virote (20) | Comum | Munição | 0.36 | 1 | | 1 | munição para bestas | Projéteis curtos e densos para bestas, com superior penetração em armaduras. Pacote com 20. | 35 |

### Itens — Equipamentos de Aventura (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | descricao | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-----------|-------|
| Kit de Aventureiro | Comum | Equipamento de Aventura | 12.00 | 12 | | 1 | mochila, ração 10 dias, corda, archote | Conjunto essencial para expedições longas: mochila, rações, corda e fonte de iluminação. | 36 |
| Kit de Curandeiro | Comum | Equipamento de Aventura | 1.50 | 5 | 10 | 1 | 10 usos de bandagem, 5 usos de antídoto | Bolsa de couro com bandagens, antídotos e ervas medicinais para até 10 tratamentos de campo. | 37 |
| Kit de Ladrão | Comum | Equipamento de Aventura | 0.90 | 25 | | 1 | ferramentas de ladrão, forçado VIG para abrir fechaduras | Ferramentas de precisão para abrir fechaduras, desativar armadilhas e realizar trabalhos sorrateiros. | 38 |
| Lanterna Bullseye | Comum | Equipamento de Aventura | 1.00 | 10 | | 1 | iluminação direcional 18m, 6h de óleo | Lanterna de foco direcional que projeta luz concentrada a 18 metros, ideal para exploração. | 39 |
| Tomo Arcano | Comum | Equipamento de Aventura | 1.50 | 25 | | 1 | livro de feitiços para Magos e Feiticeiros | Grimório encadernado em couro com páginas de pergaminho para registro e estudo de feitiços. | 40 |

### Efeitos de Itens (para `getEfeitos()`)

> Tipos validos: BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_ESSENCIA, FORMULA_CUSTOMIZADA, EFEITO_DADO

| itemNome | tipoEfeito | bonusNome | atributoSigla | valorFixo | descricaoEfeito |
|----------|-----------|-----------|---------------|-----------|-----------------|
| Espada Longa +1 | BONUS_DERIVADO | B.B.A | | 1 | |
| Espada Longa +2 | BONUS_DERIVADO | B.B.A | | 2 | |
| Arco Longo +1 | BONUS_DERIVADO | B.B.A | | 1 | |
| Cajado Arcano +1 | BONUS_DERIVADO | B.B.M | | 1 | |
| Gibão de Couro | BONUS_DERIVADO | Defesa | | 1 | |
| Couro Batido | BONUS_DERIVADO | Defesa | | 2 | |
| Camisão de Malha | BONUS_DERIVADO | Defesa | | 3 | |
| Cota de Escamas | BONUS_DERIVADO | Defesa | | 4 | |
| Cota de Malha | BONUS_DERIVADO | Defesa | | 5 | |
| Meia Placa | BONUS_DERIVADO | Defesa | | 5 | |
| Meia Placa | BONUS_DERIVADO | Reflexo | | 1 | |
| Placa Completa | BONUS_DERIVADO | Defesa | | 6 | |
| Escudo de Madeira | BONUS_DERIVADO | Bloqueio | | 1 | |
| Escudo de Aço | BONUS_DERIVADO | Bloqueio | | 2 | |
| Escudo Enfeitiçado +1 | BONUS_DERIVADO | Bloqueio | | 2 | |
| Escudo Enfeitiçado +1 | BONUS_DERIVADO | Defesa | | 1 | |
| Anel da Força +1 | BONUS_ATRIBUTO | | FOR | 1 | |
| Anel de Proteção +1 | BONUS_DERIVADO | Defesa | | 1 | |
| Anel de Proteção +1 | BONUS_DERIVADO | Bloqueio | | 1 | |
| Amuleto de Saúde | BONUS_VIDA | | | 5 | |
| Amuleto da Essência | BONUS_ESSENCIA | | | 5 | |
| Manto de Elvenkind | BONUS_DERIVADO | Esquiva | | 3 | |
| Manto de Elvenkind | BONUS_DERIVADO | Percepção | | 2 | |
| Poção de Cura Menor | BONUS_VIDA | | | 5 | Recupera 5 pontos de vida ao ser consumida |
| Poção de Cura | BONUS_VIDA | | | 10 | Recupera 10 pontos de vida ao ser consumida |
| Poção de Cura Superior | BONUS_VIDA | | | 25 | Recupera 25 pontos de vida ao ser consumida |
| Kit de Curandeiro | FORMULA_CUSTOMIZADA | | | | Bônus em testes de medicina e primeiros socorros |
| Kit de Ladrão | FORMULA_CUSTOMIZADA | | | | Bônus em testes de furtividade e arrombamento |
| Tomo Arcano | BONUS_ESSENCIA | | | 2 | Potencializa a reserva arcana de magos e feiticeiros |

### Requisitos de Itens (para `getRequisitos()`)

> Tipos validos: NIVEL, ATRIBUTO, BONUS, APTIDAO, VANTAGEM, CLASSE, RACA

| itemNome | tipoRequisito | alvo | valorMinimo |
|----------|--------------|------|-------------|
| Cota de Escamas | ATRIBUTO | FOR | 12 |
| Cota de Malha | ATRIBUTO | FOR | 15 |
| Meia Placa | ATRIBUTO | FOR | 15 |
| Placa Completa | ATRIBUTO | FOR | 17 |
| Machado Grande | ATRIBUTO | FOR | 13 |
| Arco Longo | ATRIBUTO | FOR | 12 |
| Cajado Arcano +1 | ATRIBUTO | INT | 12 |

---

## Acceptance Checks

- [ ] 10 arquivos criados em `config/defaults/`
- [ ] Cada um e `@Component`
- [ ] Cada um retorna dados imutaveis (`List.of()` / `Map.of()`)
- [ ] **P1:** 7 atributos com siglas FOR, AGI, VIG, SAB, INTU, INT, AST
- [ ] **P2:** 9 bonus com formulas validas usando siglas de atributos
- [ ] **P3:** 24 aptidoes (12 FISICA + 12 MENTAL)
- [ ] **P4:** 36 niveis (0-35) com XP crescente + 5 tiers de limitador
- [ ] **P5:** 35 entradas de pontos de vantagem, total acumulado = 53
- [ ] **P6:** 12 classes com nomes unicos
- [ ] **P7:** 6 racas + 12 bonus raciais + 20 vantagens pre-definidas
- [ ] **P8:** 6 dados de prospeccao (d3 a d12)
- [ ] **P9:** 3 generos + 3 indoles + 4 presencas + 7 membros do corpo
- [ ] **P10:** 7 raridades + 20 tipos + 40 itens + 29 efeitos + 7 requisitos
- [ ] `./mvnw compile` passa
