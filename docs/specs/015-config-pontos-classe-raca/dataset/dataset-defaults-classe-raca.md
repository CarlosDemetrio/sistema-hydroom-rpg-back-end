# Dataset Default — ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig, RacaVantagemPreDefinida

> Produzido por: BA/PO | 2026-04-04
> Destino: `DefaultGameConfigProviderImpl.java` — Task P2-T5 (Spec 015)
> Status: REQUER REVISÃO — ver nota abaixo

> ⚠️ **CORREÇÃO OBRIGATÓRIA (decisão PO 2026-04-04):** `pontosAptidao` foi removido de `ClassePontosConfig` e `RacaPontosConfig`. Aptidões servem APENAS para testes específicos e são completamente independentes de classe/raça. O pool de pontosAptidao vem **somente** de `NivelConfig.pontosAptidao` (global). Todos os valores de `ptAptidao` nas tabelas de classes e raças abaixo devem ser **IGNORADOS**. Apenas `pontosAtributo` e `pontosVantagem` são válidos nas entidades ClassePontosConfig e RacaPontosConfig.

---

## Contexto de Calibracao

### Base Global (NivelConfig — uniforme para todos)

| Nivel | ptAtrib/nivel | ptAptidao/nivel | Acumulado ptAtrib | Acumulado ptAptidao |
|-------|--------------|-----------------|-------------------|---------------------|
| 1     | 3            | 1               | 3                 | 1                   |
| 5     | 3            | 1               | 15                | 5                   |
| 10    | 3            | 1               | 30                | 10                  |
| 15    | 3            | 1               | 45                | 15                  |
| 20    | 3            | 1               | 60                | 20                  |
| 25    | 3            | 1               | 75                | 25                  |
| 30    | 3            | 1               | 90                | 30                  |
| 35    | 3            | 1               | 105               | 35                  |

### Principios de Design dos Extras

- Pontos de classe/raca sao EXTRAS por cima da base global
- Total de extras por classe ao nivel 35: entre 10 e 20 pontosAtributo OU entre 5 e 12 pontosAptidao (nunca ambos em volume alto)
- Classes fisicas (Guerreiro, Berserker) — foco em ptAtrib; classes mentais (Mago, Negociante) — foco em ptAptidao
- Marcos principais: niveis 1, 5, 10, 15, 20, 25, 30, 35
- Racas dao volumes menores (complementares ao flavor racial, nao a especializacao)

---

## 1. ClassePontosConfig — Pontos Extras por Classe e Nivel

### Guerreiro
**Sabor:** Especialista em combate fisico. Foco em FOR e VIG. Mais pontos de atributo que qualquer outra classe. Poucas aptidoes.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 2        | 0          | 1           | Treinamento inicial intensivo em combate |
| 5     | 2        | 0          | 1           | Consolidacao do dominio marcial |
| 10    | 2        | 0          | 1           | Marco de veterano de batalha |
| 15    | 2        | 0          | 1           | Especialista consumado |
| 20    | 2        | 0          | 1           | Mestre da guerra |
| 25    | 2        | 0          | 1           | Lenda viva |
| 30    | 2        | 0          | 1           | Transcendente marcial |
| 35    | 2        | 0          | 1           | Auge da perfeicao fisica |

**Total extras nivel 35:** +16 ptAtrib, +0 ptAptidao, +8 ptVantagem

---

### Arqueiro
**Sabor:** Combate a distancia. Foco em AGI. Requer precisao e observacao. Equilibrio entre atributo e aptidao.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 1        | 1          | 1           | Base: precisao e agilidade |
| 5     | 1        | 1          | 0           | Aperfeicoamento da tecnica |
| 10    | 1        | 1          | 1           | Marco: arqueiro experiente |
| 15    | 1        | 1          | 0           | Mestre do arco |
| 20    | 1        | 1          | 1           | Atirador lendario |
| 25    | 1        | 1          | 0           | Precisao sobre-humana |
| 30    | 1        | 1          | 1           | Fenomeno do combate a distancia |
| 35    | 1        | 1          | 0           | Perfeicao do arco |

**Total extras nivel 35:** +8 ptAtrib, +8 ptAptidao, +4 ptVantagem

---

### Monge
**Sabor:** Combate disciplinado e espiritualidade. Foco em FOR, SAB e AGI. Equilibrio corpo-mente. Algumas aptidoes.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 1        | 1          | 0           | Iniciacao na disciplina monastica |
| 5     | 1        | 1          | 1           | Aprofundamento nas artes monasticas |
| 10    | 2        | 1          | 0           | Mestre das artes marciais internas |
| 15    | 1        | 1          | 1           | Iluminacao parcial |
| 20    | 2        | 1          | 0           | Harmonia perfeita corpo-mente |
| 25    | 1        | 1          | 1           | Mestre venerado |
| 30    | 2        | 1          | 0           | Transcendencia fisica |
| 35    | 1        | 1          | 1           | Iluminacao completa |

**Total extras nivel 35:** +11 ptAtrib, +8 ptAptidao, +4 ptVantagem

---

### Berserker
**Sabor:** Furia bruta e instintiva. Maximo de FOR e VIG. Nenhuma finesse. Nenhuma aptidao — tudo e instinto e musculo.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 3        | 0          | 1           | A furia e o dom inato do berserker |
| 5     | 3        | 0          | 1           | A raiva cresce com a experiencia |
| 10    | 3        | 0          | 1           | Marco: berserker aterrador |
| 15    | 3        | 0          | 1           | Destruicao pura |
| 20    | 2        | 0          | 1           | Potencia começa a se estabilizar |
| 25    | 2        | 0          | 1           | Berserker lendario |
| 30    | 2        | 0          | 1           | Forca transcendente |
| 35    | 2        | 0          | 1           | Encarnacao da destruicao |

**Total extras nivel 35:** +20 ptAtrib, +0 ptAptidao, +8 ptVantagem

---

### Assassino
**Sabor:** Precisao letal e furtividade. Foco em AGI e AST. Muitas aptidoes (furtividade, prestidigitacao, blefar). Poucos pontos de atributo.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 2          | 1           | Treinamento em tecnicas de sombra |
| 5     | 1        | 2          | 0           | Refinamento das tecnicas assassinas |
| 10    | 0        | 2          | 1           | Marco: assassino profissional |
| 15    | 1        | 2          | 0           | Mestre da furtividade |
| 20    | 0        | 2          | 1           | Sombra entre sombras |
| 25    | 1        | 2          | 0           | Lenda das guilds de assassinos |
| 30    | 0        | 2          | 1           | Morte personificada |
| 35    | 1        | 2          | 0           | Perfeicao do assassino |

**Total extras nivel 35:** +4 ptAtrib, +16 ptAptidao, +4 ptVantagem

---

### Fauno (Herdeiro)
**Sabor:** Mistura rara de combate e magia. Foco em SAB e FOR. Personagem versatil com acesso a ambos os mundos. Pontos equilibrados.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 1        | 1          | 1           | O herdeiro desperta: corpo e espirito |
| 5     | 1        | 1          | 1           | Equilibrio entre os dois caminhos |
| 10    | 1        | 1          | 1           | Marco: dominio dual |
| 15    | 1        | 1          | 1           | Mestre dos dois mundos |
| 20    | 1        | 1          | 1           | O herdeiro pleno |
| 25    | 1        | 1          | 1           | Convergencia de poderes |
| 30    | 1        | 1          | 1           | Transcendencia dual |
| 35    | 1        | 1          | 1           | O herdeiro ascendido |

**Total extras nivel 35:** +8 ptAtrib, +8 ptAptidao, +8 ptVantagem

---

### Mago
**Sabor:** Poder magico puro. Foco em INT e SAB. Corpo fragil, mente poderosa. Minimo de atributo fisico, maximo de aptidoes mentais.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 2          | 1           | Iniciacao nos arcanos: aptidoes mentais |
| 5     | 0        | 2          | 1           | Profundidade arcana |
| 10    | 0        | 3          | 1           | Marco: Mago graduado (salto de aptidoes) |
| 15    | 0        | 2          | 1           | Arcanista veterano |
| 20    | 0        | 3          | 1           | Arquimago em formacao |
| 25    | 0        | 2          | 1           | Arquimago reconhecido |
| 30    | 0        | 3          | 1           | Mago lendario |
| 35    | 0        | 2          | 1           | Transcendencia arcana |

**Total extras nivel 35:** +0 ptAtrib, +19 ptAptidao, +8 ptVantagem

---

### Feiticeiro
**Sabor:** Magia instintiva, derivada do sangue ou de um pacto. Foco em INT e INTU. Menos disciplinado que o Mago, mais espontaneo. Aptidoes moderadas.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 2          | 1           | O dom magico se manifesta |
| 5     | 1        | 1          | 1           | Dominio crescente sobre o dom |
| 10    | 0        | 2          | 1           | Marco: feiticeiro de poder intermediario |
| 15    | 1        | 1          | 1           | Refinamento do dom inato |
| 20    | 0        | 2          | 1           | Feiticeiro poderoso |
| 25    | 1        | 1          | 1           | Dom transcendente |
| 30    | 0        | 2          | 1           | Feiticeiro lendario |
| 35    | 1        | 1          | 1           | Magia encarnada |

**Total extras nivel 35:** +4 ptAtrib, +12 ptAptidao, +8 ptVantagem

---

### Sacerdote
**Sabor:** Devocao divina, cura e suporte. Foco em SAB e VIG. Aptidoes mentais e fisicas. Mais pontos de vantagem (habilidades divinas). Equilibrado.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 1        | 1          | 2           | Unção sagrada: dom divino inicial |
| 5     | 1        | 1          | 1           | Aprofundamento na fe |
| 10    | 1        | 1          | 2           | Marco: sacerdote ungido |
| 15    | 1        | 1          | 1           | Profeta reconhecido |
| 20    | 1        | 1          | 2           | Alto sacerdote |
| 25    | 1        | 1          | 1           | Enviado divino |
| 30    | 1        | 1          | 2           | Santo vivo |
| 35    | 1        | 1          | 1           | Encarnacao da divindade |

**Total extras nivel 35:** +8 ptAtrib, +8 ptAptidao, +12 ptVantagem

---

### Ladrao
**Sabor:** Versatilidade e habilidades variadas. Foco em AGI e AST. Maximo de aptidoes (furtividade, blefar, prestidigitacao, operacao de mecanismos). Minimo de atributo.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 3          | 0           | Treinamento: aptidoes de ladrao |
| 5     | 0        | 2          | 1           | Especializacao nas artes do crime |
| 10    | 0        | 3          | 0           | Marco: ladrao experiente |
| 15    | 0        | 2          | 1           | Mestre da furtividade |
| 20    | 0        | 3          | 0           | Lenda dos subterraneos |
| 25    | 0        | 2          | 1           | Intocavel |
| 30    | 0        | 3          | 0           | Transcendencia das sombras |
| 35    | 0        | 2          | 1           | O fantasma |

**Total extras nivel 35:** +0 ptAtrib, +20 ptAptidao, +4 ptVantagem

---

### Negociante
**Sabor:** Habilidades sociais e mentais. Foco em AST e INT. O maior total de aptidoes do jogo. Nenhum ponto de atributo fisico.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 3          | 1           | Redes de contatos e conhecimento de mercado |
| 5     | 0        | 2          | 1           | Expansao dos negocios |
| 10    | 0        | 3          | 1           | Marco: negociante estabelecido |
| 15    | 0        | 2          | 1           | Influencia regional |
| 20    | 0        | 3          | 1           | Magnata do comercio |
| 25    | 0        | 2          | 1           | Poder economico continental |
| 30    | 0        | 3          | 1           | Senhor do comercio |
| 35    | 0        | 2          | 1           | Monopolio absoluto |

**Total extras nivel 35:** +0 ptAtrib, +20 ptAptidao, +8 ptVantagem

---

### Necromante
**Sabor:** Magia das trevas e dominio da morte. Foco em INT e SAB. Aptidoes mentais. Diferente do Mago: recebe alguns pontos de atributo (para resistencia ao proprio uso de necromancia).

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 2          | 1           | Iniciacao nas artes obscuras |
| 5     | 1        | 2          | 0           | Dominio sobre os mortos |
| 10    | 0        | 2          | 1           | Marco: necromante de poder intermediario |
| 15    | 1        | 2          | 0           | Senhor dos mortos-vivos |
| 20    | 0        | 2          | 1           | Necromante temido |
| 25    | 1        | 2          | 0           | Lorde da morte |
| 30    | 0        | 2          | 1           | Necromante ascendido |
| 35    | 1        | 2          | 0           | Encarnacao da morte |

**Total extras nivel 35:** +4 ptAtrib, +16 ptAptidao, +4 ptVantagem

---

## 2. ClasseVantagemPreDefinida — Vantagens Auto-Concedidas por Classe

> Regra: concedidas ao atingir o nivel indicado, custo zero, origem = SISTEMA.
> Apenas vantagens canonicas listadas na Spec 015 sao usadas aqui.

### Guerreiro
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCO               | Guerreiro nasce sabendo combater ofensivamente |
| 1     | TCD               | Defesa fisica e parte do treinamento basico |
| 10    | Ataque Adicional  | Veterano pode realizar ataques extras |
| 20    | Golpe Critico     | Mestre de armas domina golpes decisivos |

### Arqueiro
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCE               | Arqueiro depende de evasao, nao de bloqueio |
| 1     | TM                | Precisao mental e necessaria para o arco |
| 10    | Ataque Adicional  | Salva de flechas: multiplos projéteis |
| 20    | Percepcao Apurada | Olho aguçado para alvos distantes |

> Nota: "Percepcao Apurada" e uma vantagem do tipo Vantagem de Atributo. Se nao existir como VantagemConfig, usar "SG" (Sexto Sentido) como substituta.

### Monge
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCO               | Combate desarmado como arte marcial |
| 1     | T.M               | Meditacao e fundamental para o Monge |
| 10    | TCE               | Evasao monástica: esquivar em vez de bloquear |
| 20    | Ataque Sentai     | Ataque em estilo sentai: poder concentrado |

### Berserker
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCO               | Ataque bruto e o unico modo do berserker |
| 1     | CFM               | Forca maxima e o dom natural do berserker |
| 10    | Saude de Ferro    | Corpo forjado pela batalha — resistencia brutal |
| 20    | Ataque Adicional  | Furia: multiplos golpes desenfreados |

### Assassino
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCE               | Assassino evade, nao bloqueia |
| 1     | DF                 | Destreza Fisica: agilidade de gatuno |
| 10    | Contra-Ataque     | Aproveitar a abertura do inimigo |
| 20    | Interceptacao     | Interceptar ataques com contra-golpes |

> Nota: "DF" = Destreza Fisica (VantagemConfig da categoria Vantagem de Atributo).

### Fauno (Herdeiro)
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCO               | O herdeiro mantem o dominio fisico |
| 1     | TM                | O herdeiro e tambem um feiticeiro |
| 10    | TPM               | Treinamento de Poder Mental: aprofundamento magico |
| 20    | Saude de Ferro    | Durabilidade do herdeiro que transita entre mundos |

### Mago
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TM                | O Mago nasce treinado no arcano |
| 1     | TPM               | Poder mental e o cerne do Mago |
| 10    | DM                | Destreza Mental: mente afiada |
| 20    | Pensamento Bifurcado | Marco do Arquimago: dois feticos simultaneos |

> Nota: "Pensamento Bifurcado" e Vantagem de Renascimento — concedida somente no nivel 20+ seria incomum. PONTO EM ABERTO PA-DV-01: confirmar com PO se Mago pode receber Pensamento Bifurcado sem Renascimento, ou substituir por outra vantagem.

### Feiticeiro
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TM                | O dom magico inclui treinamento magico basico |
| 1     | INTU              | Intuicao magica e o cerne do Feiticeiro |
| 10    | TPM               | Dom em plena expansao |
| 20    | DM                | Destreza Mental madura |

> Nota: "INTU" aqui se refere a vantagem "IN" (Inspiracao Natural) da categoria Vantagem de Atributo, que representa insight e intuicao. Confirmar sigla.

### Sacerdote
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TM                | Fe e magia andam juntas para o sacerdote |
| 1     | Saude de Ferro    | A graca divina protege o corpo do fiel |
| 10    | TL                | Treinamento de Lideranca: o sacerdote guia seu grupo |
| 20    | DV                | Determinacao Vital: a fe sustenta a vida |

### Ladrao
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TCE               | Ladrao foge, nao enfrenta |
| 1     | DF                | Destreza Fisica: fundamental para o ladrao |
| 10    | Ambidestria       | Duas maos igualmente habeis |
| 20    | Contra-Ataque     | Golpe de oportunidade ao ser atacado |

### Negociante
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TL                | Treinamento de Lideranca: negociador nato |
| 1     | IN                | Inspiracao Natural: feeling para negocios |
| 10    | Riqueza           | Marco: negociante estabelecido (recursos proprios) |
| 20    | Capangas          | Influencia suficiente para ter segurança pessoal |

### Necromante
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | TM                | Necromancia requer treinamento magico |
| 1     | DM                | Destreza Mental: mente necro e acutissima |
| 10    | TPM               | Poder mental expandido para controle de mortos-vivos |
| 20    | SG                | Sexto Sentido: o Necromante sente a morte ao redor |

---

## 3. RacaPontosConfig — Pontos Extras por Raca e Nivel

> Design: racas dao volumes MENORES que classes. Foco em poucos marcos de progressao.
> Humano recebe mais pontos distribuiveis (compensacao pela ausencia de bonus fixos de raca).
> Total maximo por raca ao nivel 35: 8-12 ptAtrib OU 6-10 ptAptidao.

### Humano
**Sabor:** A raca mais versatil. Sem bonus fixos de atributo. Compensado por mais pontos livres para distribuir onde quiser.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 2        | 2          | 1           | Versatilidade humana: pontos em tudo |
| 5     | 1        | 1          | 0           | Adaptabilidade continua |
| 10    | 2        | 1          | 1           | Marco humano: determinacao |
| 15    | 1        | 1          | 0           | Persistencia e o dom humano |
| 20    | 2        | 1          | 1           | Ambicao e evolucao constante |
| 25    | 1        | 1          | 0           | Superacao dos limites |
| 30    | 2        | 1          | 1           | Apice da civilizacao humana |
| 35    | 1        | 1          | 0           | Humano transcendente |

**Total extras nivel 35:** +12 ptAtrib, +9 ptAptidao, +4 ptVantagem

---

### Elfo
**Sabor:** +2 AGI, -1 VIG (definido em RacaBonusAtributo). Gracioso e agil. Bonus extras em aptidoes (longevidade = muito conhecimento acumulado).

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 0        | 2          | 0           | Séculos de aprendizado: aptidoes naturais |
| 5     | 0        | 1          | 0           | Graca elfica em expansao |
| 10    | 0        | 2          | 1           | Marco: conhecimento elfico profundo |
| 15    | 0        | 1          | 0           | Sabedoria das eras |
| 20    | 0        | 2          | 1           | Elfo veterano de muitas decadas |
| 25    | 0        | 1          | 0           | Memoria de seculos |
| 30    | 0        | 2          | 1           | Elfo anciao |
| 35    | 0        | 1          | 0           | Elfo imortal |

**Total extras nivel 35:** +0 ptAtrib, +12 ptAptidao, +3 ptVantagem

---

### Anao
**Sabor:** +2 VIG, -1 AGI (definido em RacaBonusAtributo). Resistente e teimoso. Bonus em atributos fisicos. Pouca flexibilidade em aptidoes.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 2        | 0          | 0           | Forja anã: corpo treinado desde a infancia |
| 5     | 1        | 0          | 1           | Resistencia anã se consolida |
| 10    | 2        | 0          | 0           | Marco: anao guerreiro forjado |
| 15    | 1        | 0          | 1           | Durabilidade de pedra |
| 20    | 2        | 0          | 0           | Anao veterano das minas e batalhas |
| 25    | 1        | 0          | 1           | Resistencia lendaria |
| 30    | 2        | 0          | 0           | Anao anciao de ferro |
| 35    | 1        | 0          | 1           | Pedra imovivel |

**Total extras nivel 35:** +12 ptAtrib, +0 ptAptidao, +4 ptVantagem

---

### Meio-Elfo
**Sabor:** Herdeiro do melhor dos dois mundos. Sem bonus fixos definidos ainda (a definir). Design: flexibilidade moderada — nem tao apto em aptidoes quanto o Elfo, nem tao resistente quanto o Humano.

| Nivel | +ptAtrib | +ptAptidao | +ptVantagem | Justificativa |
|-------|----------|------------|-------------|---------------|
| 1     | 1        | 1          | 0           | Heranca dupla: equilibrio desde o nascimento |
| 5     | 0        | 1          | 1           | Aptidoes elficas afloram |
| 10    | 1        | 1          | 0           | Marco: identidade propria |
| 15    | 0        | 1          | 1           | Transcendencia da dualidade |
| 20    | 1        | 1          | 0           | Equilibrio pleno |
| 25    | 0        | 1          | 1           | Sabedoria da dualidade |
| 30    | 1        | 1          | 0           | Meio-Elfo veterano |
| 35    | 0        | 1          | 1           | Uniao das herancas |

**Total extras nivel 35:** +4 ptAtrib, +8 ptAptidao, +4 ptVantagem

---

## 4. RacaVantagemPreDefinida — Vantagens Auto-Concedidas por Raca

### Humano
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | Ambidestria       | Humanos sao naturalmente bi-manuais |
| 10    | Fortitude         | Determinacao humana: resistencia a adversidade |

> Nota: "Fortitude" pode ser mapeada para "Saude de Ferro" (Vantagem Geral) se nao existir como entrada separada no VantagemConfig. PONTO EM ABERTO PA-DV-02: confirmar se "Fortitude" e uma VantagemConfig distinta ou alias de "Saude de Ferro".

### Elfo
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | SG                | Sexto Sentido: percepcao aguçada elfica |
| 1     | TCE               | Evasao gracil: o elfo nao bloqueia, esquiva |
| 10    | DM                | Destreza Mental: mente elfica afiada por seculos |

### Anao
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | Saude de Ferro    | Resistencia fisica anã e lendaria |
| 1     | TCD               | Anoes sao treinados em combate defensivo desde cedo |
| 10    | CFM               | Forca maxima: o anao conhece seus limites e os supera |

### Meio-Elfo
| Nivel | Vantagem concedida | Justificativa |
|-------|-------------------|---------------|
| 1     | SG                | Intuicao elfica herdada |
| 10    | Ambidestria       | Adaptabilidade: aprende dos dois lados da heranca |

---

## 5. Resumo Comparativo por Classe — Total ao Nivel 35

| Classe           | Total ptAtrib extras | Total ptAptidao extras | Total ptVantagem extras | Perfil |
|------------------|---------------------|----------------------|------------------------|--------|
| Guerreiro        | +16                 | +0                   | +8                     | Fisico puro |
| Arqueiro         | +8                  | +8                   | +4                     | Fisico/Mental equilibrado |
| Monge            | +11                 | +8                   | +4                     | Corpo-mente |
| Berserker        | +20                 | +0                   | +8                     | Fisico extremo |
| Assassino        | +4                  | +16                  | +4                     | Habilidades especialistas |
| Fauno (Herdeiro) | +8                  | +8                   | +8                     | Versatil dual |
| Mago             | +0                  | +19                  | +8                     | Mental puro |
| Feiticeiro       | +4                  | +12                  | +8                     | Mental com corpo |
| Sacerdote        | +8                  | +8                   | +12                    | Suporte (mais vantagens) |
| Ladrao           | +0                  | +20                  | +4                     | Habilidades puras |
| Negociante       | +0                  | +20                  | +8                     | Social/mental |
| Necromante       | +4                  | +16                  | +4                     | Mental especialista |

## 6. Resumo Comparativo por Raca — Total ao Nivel 35

| Raca       | Total ptAtrib extras | Total ptAptidao extras | Total ptVantagem extras | Perfil |
|------------|---------------------|----------------------|------------------------|--------|
| Humano     | +12                 | +9                   | +4                     | Maximo versatilidade |
| Elfo       | +0                  | +12                  | +3                     | Aptidoes e conhecimento |
| Anao       | +12                 | +0                   | +4                     | Atributos e resistencia |
| Meio-Elfo  | +4                  | +8                   | +4                     | Equilibrio moderado |

---

## 7. Codigo Java — DefaultGameConfigProviderImpl

```java
// =============================================================================
// ClassePontosConfig defaults
// Formato: ClassePontosConfigDTO.of(nivel, pontosAtributo, pontosAptidao, pontosVantagem)
// =============================================================================

@Override
public Map<String, List<ClassePontosConfigDTO>> getDefaultClassePontos() {
    return Map.ofEntries(
        Map.entry("Guerreiro", List.of(
            ClassePontosConfigDTO.of(1,  2, 0, 1),
            ClassePontosConfigDTO.of(5,  2, 0, 1),
            ClassePontosConfigDTO.of(10, 2, 0, 1),
            ClassePontosConfigDTO.of(15, 2, 0, 1),
            ClassePontosConfigDTO.of(20, 2, 0, 1),
            ClassePontosConfigDTO.of(25, 2, 0, 1),
            ClassePontosConfigDTO.of(30, 2, 0, 1),
            ClassePontosConfigDTO.of(35, 2, 0, 1)
        )),
        Map.entry("Arqueiro", List.of(
            ClassePontosConfigDTO.of(1,  1, 1, 1),
            ClassePontosConfigDTO.of(5,  1, 1, 0),
            ClassePontosConfigDTO.of(10, 1, 1, 1),
            ClassePontosConfigDTO.of(15, 1, 1, 0),
            ClassePontosConfigDTO.of(20, 1, 1, 1),
            ClassePontosConfigDTO.of(25, 1, 1, 0),
            ClassePontosConfigDTO.of(30, 1, 1, 1),
            ClassePontosConfigDTO.of(35, 1, 1, 0)
        )),
        Map.entry("Monge", List.of(
            ClassePontosConfigDTO.of(1,  1, 1, 0),
            ClassePontosConfigDTO.of(5,  1, 1, 1),
            ClassePontosConfigDTO.of(10, 2, 1, 0),
            ClassePontosConfigDTO.of(15, 1, 1, 1),
            ClassePontosConfigDTO.of(20, 2, 1, 0),
            ClassePontosConfigDTO.of(25, 1, 1, 1),
            ClassePontosConfigDTO.of(30, 2, 1, 0),
            ClassePontosConfigDTO.of(35, 1, 1, 1)
        )),
        Map.entry("Berserker", List.of(
            ClassePontosConfigDTO.of(1,  3, 0, 1),
            ClassePontosConfigDTO.of(5,  3, 0, 1),
            ClassePontosConfigDTO.of(10, 3, 0, 1),
            ClassePontosConfigDTO.of(15, 3, 0, 1),
            ClassePontosConfigDTO.of(20, 2, 0, 1),
            ClassePontosConfigDTO.of(25, 2, 0, 1),
            ClassePontosConfigDTO.of(30, 2, 0, 1),
            ClassePontosConfigDTO.of(35, 2, 0, 1)
        )),
        Map.entry("Assassino", List.of(
            ClassePontosConfigDTO.of(1,  0, 2, 1),
            ClassePontosConfigDTO.of(5,  1, 2, 0),
            ClassePontosConfigDTO.of(10, 0, 2, 1),
            ClassePontosConfigDTO.of(15, 1, 2, 0),
            ClassePontosConfigDTO.of(20, 0, 2, 1),
            ClassePontosConfigDTO.of(25, 1, 2, 0),
            ClassePontosConfigDTO.of(30, 0, 2, 1),
            ClassePontosConfigDTO.of(35, 1, 2, 0)
        )),
        Map.entry("Fauno", List.of(
            ClassePontosConfigDTO.of(1,  1, 1, 1),
            ClassePontosConfigDTO.of(5,  1, 1, 1),
            ClassePontosConfigDTO.of(10, 1, 1, 1),
            ClassePontosConfigDTO.of(15, 1, 1, 1),
            ClassePontosConfigDTO.of(20, 1, 1, 1),
            ClassePontosConfigDTO.of(25, 1, 1, 1),
            ClassePontosConfigDTO.of(30, 1, 1, 1),
            ClassePontosConfigDTO.of(35, 1, 1, 1)
        )),
        Map.entry("Mago", List.of(
            ClassePontosConfigDTO.of(1,  0, 2, 1),
            ClassePontosConfigDTO.of(5,  0, 2, 1),
            ClassePontosConfigDTO.of(10, 0, 3, 1),
            ClassePontosConfigDTO.of(15, 0, 2, 1),
            ClassePontosConfigDTO.of(20, 0, 3, 1),
            ClassePontosConfigDTO.of(25, 0, 2, 1),
            ClassePontosConfigDTO.of(30, 0, 3, 1),
            ClassePontosConfigDTO.of(35, 0, 2, 1)
        )),
        Map.entry("Feiticeiro", List.of(
            ClassePontosConfigDTO.of(1,  0, 2, 1),
            ClassePontosConfigDTO.of(5,  1, 1, 1),
            ClassePontosConfigDTO.of(10, 0, 2, 1),
            ClassePontosConfigDTO.of(15, 1, 1, 1),
            ClassePontosConfigDTO.of(20, 0, 2, 1),
            ClassePontosConfigDTO.of(25, 1, 1, 1),
            ClassePontosConfigDTO.of(30, 0, 2, 1),
            ClassePontosConfigDTO.of(35, 1, 1, 1)
        )),
        Map.entry("Sacerdote", List.of(
            ClassePontosConfigDTO.of(1,  1, 1, 2),
            ClassePontosConfigDTO.of(5,  1, 1, 1),
            ClassePontosConfigDTO.of(10, 1, 1, 2),
            ClassePontosConfigDTO.of(15, 1, 1, 1),
            ClassePontosConfigDTO.of(20, 1, 1, 2),
            ClassePontosConfigDTO.of(25, 1, 1, 1),
            ClassePontosConfigDTO.of(30, 1, 1, 2),
            ClassePontosConfigDTO.of(35, 1, 1, 1)
        )),
        Map.entry("Ladrao", List.of(
            ClassePontosConfigDTO.of(1,  0, 3, 0),
            ClassePontosConfigDTO.of(5,  0, 2, 1),
            ClassePontosConfigDTO.of(10, 0, 3, 0),
            ClassePontosConfigDTO.of(15, 0, 2, 1),
            ClassePontosConfigDTO.of(20, 0, 3, 0),
            ClassePontosConfigDTO.of(25, 0, 2, 1),
            ClassePontosConfigDTO.of(30, 0, 3, 0),
            ClassePontosConfigDTO.of(35, 0, 2, 1)
        )),
        Map.entry("Negociante", List.of(
            ClassePontosConfigDTO.of(1,  0, 3, 1),
            ClassePontosConfigDTO.of(5,  0, 2, 1),
            ClassePontosConfigDTO.of(10, 0, 3, 1),
            ClassePontosConfigDTO.of(15, 0, 2, 1),
            ClassePontosConfigDTO.of(20, 0, 3, 1),
            ClassePontosConfigDTO.of(25, 0, 2, 1),
            ClassePontosConfigDTO.of(30, 0, 3, 1),
            ClassePontosConfigDTO.of(35, 0, 2, 1)
        )),
        Map.entry("Necromante", List.of(
            ClassePontosConfigDTO.of(1,  0, 2, 1),
            ClassePontosConfigDTO.of(5,  1, 2, 0),
            ClassePontosConfigDTO.of(10, 0, 2, 1),
            ClassePontosConfigDTO.of(15, 1, 2, 0),
            ClassePontosConfigDTO.of(20, 0, 2, 1),
            ClassePontosConfigDTO.of(25, 1, 2, 0),
            ClassePontosConfigDTO.of(30, 0, 2, 1),
            ClassePontosConfigDTO.of(35, 1, 2, 0)
        ))
    );
}


// =============================================================================
// ClasseVantagemPreDefinida defaults
// Formato: ClasseVantagemPreDefinidaDTO.of(nivel, vantagemSiglaOuNome)
// =============================================================================

@Override
public Map<String, List<ClasseVantagemPreDefinidaDTO>> getDefaultClasseVantagensPreDefinidas() {
    return Map.ofEntries(
        Map.entry("Guerreiro", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCO"),
            ClasseVantagemPreDefinidaDTO.of(1,  "TCD"),
            ClasseVantagemPreDefinidaDTO.of(10, "Ataque Adicional"),
            ClasseVantagemPreDefinidaDTO.of(20, "Golpe Critico")
        )),
        Map.entry("Arqueiro", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCE"),
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(10, "Ataque Adicional"),
            ClasseVantagemPreDefinidaDTO.of(20, "SG")
        )),
        Map.entry("Monge", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCO"),
            ClasseVantagemPreDefinidaDTO.of(1,  "T.M"),
            ClasseVantagemPreDefinidaDTO.of(10, "TCE"),
            ClasseVantagemPreDefinidaDTO.of(20, "Ataque Sentai")
        )),
        Map.entry("Berserker", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCO"),
            ClasseVantagemPreDefinidaDTO.of(1,  "CFM"),
            ClasseVantagemPreDefinidaDTO.of(10, "Saude de Ferro"),
            ClasseVantagemPreDefinidaDTO.of(20, "Ataque Adicional")
        )),
        Map.entry("Assassino", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCE"),
            ClasseVantagemPreDefinidaDTO.of(1,  "DF"),
            ClasseVantagemPreDefinidaDTO.of(10, "Contra-Ataque"),
            ClasseVantagemPreDefinidaDTO.of(20, "Interceptacao")
        )),
        Map.entry("Fauno", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCO"),
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(10, "TPM"),
            ClasseVantagemPreDefinidaDTO.of(20, "Saude de Ferro")
        )),
        Map.entry("Mago", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(1,  "TPM"),
            ClasseVantagemPreDefinidaDTO.of(10, "DM"),
            ClasseVantagemPreDefinidaDTO.of(20, "T.M")
            // PONTO EM ABERTO PA-DV-01: nivel 20 era "Pensamento Bifurcado" mas
            // esta e Vantagem de Renascimento — substituida por T.M ate confirmacao do PO
        )),
        Map.entry("Feiticeiro", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(1,  "IN"),
            ClasseVantagemPreDefinidaDTO.of(10, "TPM"),
            ClasseVantagemPreDefinidaDTO.of(20, "DM")
        )),
        Map.entry("Sacerdote", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(1,  "Saude de Ferro"),
            ClasseVantagemPreDefinidaDTO.of(10, "TL"),
            ClasseVantagemPreDefinidaDTO.of(20, "DV")
        )),
        Map.entry("Ladrao", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TCE"),
            ClasseVantagemPreDefinidaDTO.of(1,  "DF"),
            ClasseVantagemPreDefinidaDTO.of(10, "Ambidestria"),
            ClasseVantagemPreDefinidaDTO.of(20, "Contra-Ataque")
        )),
        Map.entry("Negociante", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TL"),
            ClasseVantagemPreDefinidaDTO.of(1,  "IN"),
            ClasseVantagemPreDefinidaDTO.of(10, "Riqueza"),
            ClasseVantagemPreDefinidaDTO.of(20, "Capangas")
        )),
        Map.entry("Necromante", List.of(
            ClasseVantagemPreDefinidaDTO.of(1,  "TM"),
            ClasseVantagemPreDefinidaDTO.of(1,  "DM"),
            ClasseVantagemPreDefinidaDTO.of(10, "TPM"),
            ClasseVantagemPreDefinidaDTO.of(20, "SG")
        ))
    );
}


// =============================================================================
// RacaPontosConfig defaults
// Formato: RacaPontosConfigDTO.of(nivel, pontosAtributo, pontosAptidao, pontosVantagem)
// =============================================================================

@Override
public Map<String, List<RacaPontosConfigDTO>> getDefaultRacaPontos() {
    return Map.of(
        "Humano", List.of(
            RacaPontosConfigDTO.of(1,  2, 2, 1),
            RacaPontosConfigDTO.of(5,  1, 1, 0),
            RacaPontosConfigDTO.of(10, 2, 1, 1),
            RacaPontosConfigDTO.of(15, 1, 1, 0),
            RacaPontosConfigDTO.of(20, 2, 1, 1),
            RacaPontosConfigDTO.of(25, 1, 1, 0),
            RacaPontosConfigDTO.of(30, 2, 1, 1),
            RacaPontosConfigDTO.of(35, 1, 1, 0)
        ),
        "Elfo", List.of(
            RacaPontosConfigDTO.of(1,  0, 2, 0),
            RacaPontosConfigDTO.of(5,  0, 1, 0),
            RacaPontosConfigDTO.of(10, 0, 2, 1),
            RacaPontosConfigDTO.of(15, 0, 1, 0),
            RacaPontosConfigDTO.of(20, 0, 2, 1),
            RacaPontosConfigDTO.of(25, 0, 1, 0),
            RacaPontosConfigDTO.of(30, 0, 2, 1),
            RacaPontosConfigDTO.of(35, 0, 1, 0)
        ),
        "Anao", List.of(
            RacaPontosConfigDTO.of(1,  2, 0, 0),
            RacaPontosConfigDTO.of(5,  1, 0, 1),
            RacaPontosConfigDTO.of(10, 2, 0, 0),
            RacaPontosConfigDTO.of(15, 1, 0, 1),
            RacaPontosConfigDTO.of(20, 2, 0, 0),
            RacaPontosConfigDTO.of(25, 1, 0, 1),
            RacaPontosConfigDTO.of(30, 2, 0, 0),
            RacaPontosConfigDTO.of(35, 1, 0, 1)
        ),
        "Meio-Elfo", List.of(
            RacaPontosConfigDTO.of(1,  1, 1, 0),
            RacaPontosConfigDTO.of(5,  0, 1, 1),
            RacaPontosConfigDTO.of(10, 1, 1, 0),
            RacaPontosConfigDTO.of(15, 0, 1, 1),
            RacaPontosConfigDTO.of(20, 1, 1, 0),
            RacaPontosConfigDTO.of(25, 0, 1, 1),
            RacaPontosConfigDTO.of(30, 1, 1, 0),
            RacaPontosConfigDTO.of(35, 0, 1, 1)
        )
    );
}


// =============================================================================
// RacaVantagemPreDefinida defaults
// Formato: RacaVantagemPreDefinidaDTO.of(nivel, vantagemSiglaOuNome)
// =============================================================================

@Override
public Map<String, List<RacaVantagemPreDefinidaDTO>> getDefaultRacaVantagensPreDefinidas() {
    return Map.of(
        "Humano", List.of(
            RacaVantagemPreDefinidaDTO.of(1,  "Ambidestria"),
            RacaVantagemPreDefinidaDTO.of(10, "Saude de Ferro")
            // PONTO EM ABERTO PA-DV-02: "Fortitude" e VantagemConfig distinta ou
            // alias de "Saude de Ferro"? Mapeado para "Saude de Ferro" ate confirmacao do PO.
        ),
        "Elfo", List.of(
            RacaVantagemPreDefinidaDTO.of(1,  "SG"),
            RacaVantagemPreDefinidaDTO.of(1,  "TCE"),
            RacaVantagemPreDefinidaDTO.of(10, "DM")
        ),
        "Anao", List.of(
            RacaVantagemPreDefinidaDTO.of(1,  "Saude de Ferro"),
            RacaVantagemPreDefinidaDTO.of(1,  "TCD"),
            RacaVantagemPreDefinidaDTO.of(10, "CFM")
        ),
        "Meio-Elfo", List.of(
            RacaVantagemPreDefinidaDTO.of(1,  "SG"),
            RacaVantagemPreDefinidaDTO.of(10, "Ambidestria")
        )
    );
}
```

---

## 8. Pontos em Aberto

| ID | Questao | Impacto | Decisao sugerida |
|----|---------|---------|-----------------|
| PA-DV-01 | Mago nivel 20: "Pensamento Bifurcado" e Vantagem de Renascimento. Pode ser concedida por pre-definicao sem renascimento? | ClasseVantagemPreDefinida do Mago nivel 20 | Substituir por "T.M" ate o PO confirmar |
| PA-DV-02 | "Fortitude" e uma VantagemConfig distinta ou alias de "Saude de Ferro"? | RacaVantagemPreDefinida do Humano nivel 10 | Mapear para "Saude de Ferro" ate confirmacao |
| PA-DV-03 | Nome da classe no DefaultProvider: "Fauno" ou "Fauno (Herdeiro)"? | Map.entry key no Java | Verificar nome exato em getDefaultClasses() no provider |
| PA-DV-04 | "Golpe Critico" (Guerreiro nivel 20) e "Percepcao Apurada" (Arqueiro nivel 20): existem como VantagemConfig? | ClasseVantagemPreDefinida Guerreiro/Arqueiro | Se nao existirem, substituir por vantagens canonicas disponíveis |
| PA-DV-05 | "Interceptacao" vs "Intercepcao": verificar nome exato da vantagem no VantagemConfig | ClasseVantagemPreDefinida do Assassino nivel 20 | Verificar no DefaultGameConfigProviderImpl.getDefaultVantagens() |

---

## 9. Premissas Declaradas

1. NivelConfig padrao: 3 ptAtrib + 1 ptAptidao por nivel (niveis 1-35), uniforme — conforme codigo atual
2. Nivel 0 nao gera pontos de nenhuma fonte (personagem sem experiencia)
3. Pontos de vantagem do NivelConfig sao gerenciados por PontosVantagemConfig (separado) — os campos pontosVantagem aqui sao EXTRAS da classe/raca, acumulaveis
4. ClasseVantagemPreDefinida e RacaVantagemPreDefinida usam nome OU sigla para lookup — implementacao deve resolver pelo nome do VantagemConfig dentro do mesmo jogo
5. Nome da classe "Fauno" assumido conforme lista do prompt; verificar se DefaultProvider usa "Fauno (Herdeiro)" ou apenas "Fauno"
6. Vantagens mencionadas (TCO, TCD, TCE, TM, TPM, TL, T.M, CFM, DM, DF, DV, SG, IN, Saude de Ferro, Ambidestria, Ataque Adicional, Ataque Sentai, Contra-Ataque, Riqueza, Capangas) existem no DefaultProvider apos T5 ser implementado
7. "Golpe Critico" e "Percepcao Apurada" PODEM nao existir no catalogo — PO deve confirmar (PA-DV-04)
8. Meio-Elfo nao tem RacaBonusAtributo definido ainda — seus pontos extras foram calibrados moderadamente entre Elfo e Humano

---

*Produzido por: BA/PO | 2026-04-04*
*Baseado em: glossario Klayrah (docs/glossario/), NivelConfig atual (3+1 por nivel), Spec 015, decisoes PO 2026-04-03*
