# Glossário e Contextualização — Klayrah RPG

> 📖 Documento de referência para IA (Claude, Copilot) e desenvolvedores.
> Explica **o que é**, **para que serve** e **como se relaciona** cada conceito do sistema Klayrah RPG.

---

## 1. O QUE É O KLAYRAH RPG

Klayrah é um **sistema de RPG de mesa** (Role-Playing Game) com regras próprias. O projeto **Ficha Controlador** é a aplicação web que digitaliza a experiência de preencher e gerenciar fichas de personagem desse sistema.

No RPG de mesa, cada jogador tem uma **ficha de personagem em papel** com dezenas de campos: atributos, habilidades, vida, recursos mágicos, equipamentos, etc. O Ficha Controlador substitui esse papel por uma aplicação web com cálculos automáticos, validações e controle de acesso.

### Quem usa o sistema

- **Mestre** — o narrador e árbitro do jogo. Ele cria o "mundo" (o Jogo/Campanha), define todas as regras (configurações) e gerencia os participantes. Pense nele como o administrador de uma sessão de RPG.
- **Jogador** — cada participante que cria e controla seu personagem dentro do jogo do Mestre. Preenche e edita sua própria ficha.

### Princípio fundamental: tudo configurável

O grande diferencial do sistema é que **nada é fixo** (hardcoded). O Mestre pode alterar atributos, classes, raças, fórmulas de cálculo, aptidões — absolutamente tudo. Isso permite que o sistema sirva para variações do Klayrah ou até para outros RPGs caseiros.

Quando o Mestre cria um jogo, o sistema oferece um **template "Klayrah Padrão"** com todos os valores default (7 atributos, 24 aptidões, 12 classes, etc.), mas ele pode customizar qualquer coisa depois.

---

## 2. CONCEITOS ESTRUTURAIS

### Jogo (Campanha)

O contêiner raiz de tudo. Representa uma **campanha de RPG** — um mundo com suas regras, seus participantes e suas fichas. Todas as configurações são vinculadas a um Jogo, permitindo que jogos diferentes tenham regras diferentes.

> *Exemplo: o Mestre cria o jogo "A Queda de Eldoria". Dentro dele, configura os atributos, classes e raças desse mundo. Os jogadores entram, criam seus personagens e jogam dentro dessas regras.*

### Ficha (Ficha de Personagem)

O documento central do sistema. Contém todos os dados de um personagem: quem ele é, o que sabe fazer, quanta vida tem, que recursos possui. Uma ficha pertence a um Jogo e a um Jogador (ou ao Mestre, no caso de NPCs).

A ficha não "sabe" suas próprias regras — ela aponta para as **configurações** do Jogo para saber quais atributos existem, como calcular vida, quais classes estão disponíveis, etc.

### NPC (Non-Player Character)

Personagem controlado pelo Mestre, não por um jogador. Tem ficha como qualquer personagem, mas sem um jogador dono. O Mestre usa NPCs para representar aliados, vilões, comerciantes e qualquer personagem do mundo.

### Configuração

Qualquer regra ou opção definida pelo Mestre para seu Jogo. As configurações são **tabelas `_CONFIG`** no banco de dados. Elas dizem "quais atributos existem", "quantos níveis há", "quais classes estão disponíveis", etc. A ficha do personagem apenas **referencia** essas configurações e armazena os valores específicos daquele personagem.

---

## 3. CONFIGURAÇÕES DO JOGO (o que estamos desenvolvendo agora)

As configurações são criadas pelo Mestre e definem as regras do jogo. Atualmente temos **13 tipos de configuração** com CRUD implementado. Cada uma é explicada abaixo.

### 3.1 Atributo (`AtributoConfig`)

**O que é:** As características fundamentais que definem o potencial físico e mental de um personagem. São os "pilares" sobre os quais quase tudo é calculado.

**Para que serve:** Cada atributo tem um valor numérico que influencia diretamente nos bônus de combate, nos cálculos de vida, essência, e em praticamente toda mecânica do jogo.

**Campos importantes:**
- **Nome** — ex: Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia
- **Fórmula de Ímpeto** — uma fórmula matemática que calcula um efeito derivado do total daquele atributo (ver Ímpeto abaixo)
- **Valor mínimo / máximo** — limites do atributo

**No template Klayrah Padrão:** 7 atributos.

**Na ficha do personagem:** Cada atributo tem valor **Base** (inicial), **Nível** (pontos ganhos ao subir de nível), **Outros** (bônus diversos) e **Total** (soma). O Ímpeto é calculado automaticamente a partir do Total.

---

### 3.2 Nível (`NivelConfig`)

**O que é:** A tabela de progressão do personagem. Define quanto de experiência (XP) é necessário para cada nível, quantos pontos o personagem ganha e qual o teto dos atributos.

**Para que serve:** Quando o personagem acumula XP durante o jogo, o sistema consulta essa tabela para saber em qual nível ele está e quais benefícios recebe.

**Campos importantes:**
- **Número do nível** — de 0 (iniciante) a 35 (transcendente)
- **XP necessária** — experiência acumulada para atingir esse nível
- **Pontos de atributo** — quantos pontos o personagem pode distribuir nos atributos ao atingir esse nível (padrão: 3 por nível)
- **Limitador de atributo** — valor máximo que qualquer atributo pode atingir nesse nível
- **Permite renascimento** — se o personagem pode renascer ao atingir esse nível

**Na ficha do personagem:** O nível é calculado automaticamente pela XP. Se o personagem tem 15.000 XP, o sistema procura o maior nível cuja XP necessária seja ≤ 15.000 (nível 5, nesse caso).

---

### 3.3 Aptidão (`AptidaoConfig`)

**O que é:** Habilidades específicas e treináveis do personagem. São perícias ou competências que definem o que o personagem sabe fazer bem.

**Para que serve:** Determinam o sucesso em ações específicas durante o jogo. Quando o personagem tenta escalar um muro, o Mestre pede um teste de "Atletismo". Quando tenta mentir, um teste de "Blefar".

**Campos importantes:**
- **Nome** — ex: Acrobacia, Furtividade, Diplomacia, Investigar
- **Tipo de Aptidão** — a qual categoria pertence (Física ou Mental)

**No template Klayrah Padrão:** 24 aptidões — 12 físicas e 12 mentais.

**Na ficha do personagem:** Cada aptidão tem valor **Base**, **Sorte** (bônus de sorte) e **Classe** (bônus da classe). O Total é a soma.

---

### 3.4 Tipo de Aptidão (`TipoAptidao`)

**O que é:** A categoria que agrupa aptidões semelhantes. Funciona como um "rótulo organizacional" para as aptidões.

**Para que serve:** Organizar as aptidões na interface e permitir que certas mecânicas afetem "todas as aptidões de um tipo" (ex: uma vantagem que dá +2 em todas as aptidões mentais).

**No template Klayrah Padrão:** 2 tipos — Física e Mental.

---

### 3.5 Bônus (`BonusConfig`)

**O que é:** Valores calculados automaticamente a partir dos atributos. Representam a competência do personagem em áreas amplas como combate, defesa e percepção.

**Para que serve:** São usados como "modificadores" nas rolagens de dados durante o jogo. Quando o personagem ataca, soma o B.B.A à jogada. Quando tenta perceber algo, soma a Percepção.

**Campos importantes:**
- **Nome** — ex: B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio
- **Fórmula base** — como o bônus é calculado a partir dos atributos. Ex: B.B.A = `FLOOR((FORCA + AGILIDADE) / 3)`

**No template Klayrah Padrão:** 6 bônus calculados.

**Na ficha do personagem:** Além do valor base (calculado pela fórmula), cada bônus pode receber acréscimos de 5 fontes: **Vantagens**, **Classe**, **Itens**, **Glória** e **Outros**.

---

### 3.6 Classe de Personagem (`ClassePersonagem`)

**O que é:** O "papel" ou "profissão" do personagem no mundo do jogo. Define a especialização e os bônus que o personagem recebe.

**Para que serve:** Dá identidade mecânica ao personagem. Um Guerreiro terá bônus em combate corpo a corpo, um Mago em poder mágico, um Ladrão em furtividade.

**No template Klayrah Padrão:** 12 classes — Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno (Herdeiro), Mago, Feiticeiro, Necromante, Sacerdote, Ladrão, Negociante.

**Relacionamentos:**
- Uma classe pode dar bônus em certos **Bônus** (`ClasseBonus`) — ex: Guerreiro dá +1 em B.B.A por nível
- Uma classe pode dar bônus em certas **Aptidões** (`ClasseAptidaoBonus`) — ex: Ladrão dá +2 em Furtividade

---

### 3.7 Raça (`Raca`)

**O que é:** A espécie ou povo do personagem no mundo do jogo. Raças concedem modificadores nos atributos, refletindo características biológicas.

**Para que serve:** Dá variedade na criação de personagens. Um Elfo pode ter +2 em Agilidade mas -1 em Vigor. Um Anão pode ter +2 em Vigor mas -1 em Agilidade.

**Relacionamentos:**
- Uma raça tem **bônus de atributo** (`RacaBonusAtributo`) — valores positivos ou negativos aplicados a atributos específicos
- Uma raça pode restringir **classes permitidas** (`RacaClassePermitida`) — nem toda raça pode ser qualquer classe

---

### 3.8 Membro do Corpo (`MembroCorpoConfig`)

**O que é:** As partes do corpo do personagem que podem sofrer dano individualmente. O sistema Klayrah tem um sistema de **dano localizado** — não é apenas "tirou 10 de vida", mas "tirou 10 de vida no braço direito".

**Para que serve:** Permite um sistema de combate mais tático e detalhado. Se a cabeça do personagem chega a 0, o efeito é diferente de se a perna chegar a 0.

**Campos importantes:**
- **Nome** — ex: Cabeça, Tronco, Braço Direito, Perna Esquerda, Sangue
- **Porcentagem da vida** — quanto da vida total aquele membro aguenta. Cabeça = 75%, Braço = 25%, Tronco = 100%

**No template Klayrah Padrão:** 7 membros.

**Na ficha do personagem:** Cada membro tem seu valor de vida calculado (vida total × porcentagem) e um campo de dano recebido.

---

### 3.9 Dado de Prospecção (`DadoProspeccaoConfig`)

**O que é:** Tipos de dados (de RPG) que o Mestre pode conceder aos jogadores como recurso especial e limitado.

**Para que serve:** Prospecção é um recurso **muito raro** que o Mestre distribui com parcimônia. Quando um jogador usa um dado de prospecção, ele adiciona aquele dado a uma jogada de alto risco, aumentando suas chances. É como um "coringa" — poderoso, mas escasso.

> *Exemplo: o Mestre dá ao jogador 1 dado d6 de prospecção. Em um momento crítico, o jogador decide usá-lo e soma o resultado do d6 à sua jogada.*

**Campos importantes:**
- **Nome** — ex: d3, d4, d6, d8, d10, d12
- **Número de faces** — valor máximo do dado

**No template Klayrah Padrão:** 6 tipos de dados (d3 a d12).

**Na ficha do personagem:** Um contador para cada dado, mostrando quantos o personagem possui atualmente.

---

### 3.10 Gênero (`GeneroConfig`)

**O que é:** Opções de gênero disponíveis para os personagens do jogo.

**Para que serve:** Define as opções que aparecem no dropdown de gênero da ficha. No Klayrah, o gênero influencia no cálculo automático do peso (via BMI).

**No template Klayrah Padrão:** Masculino, Feminino, Outro.

---

### 3.11 Índole (`IndoleConfig`)

**O que é:** O alinhamento moral do personagem — se ele tende ao bem, ao mal ou é neutro.

**Para que serve:** Guia a interpretação do personagem e pode ser pré-requisito para certas vantagens ou mecânicas do jogo.

**No template Klayrah Padrão:** Bom, Mau, Neutro.

---

### 3.12 Presença (`PresencaConfig`)

**O que é:** O alinhamento ético/comportamental do personagem — como ele se posiciona frente a regras e estruturas sociais.

**Para que serve:** Complementa a Índole para dar profundidade à personalidade. Um personagem pode ser "Bom e Caótico" (bem-intencionado mas rebelde) ou "Mau e Leal" (cruel mas previsível).

**No template Klayrah Padrão:** Bom, Leal, Caótico, Neutro.

---

### 3.13 Vantagem (`VantagemConfig`)

**O que é:** Habilidades especiais, talentos e poderes que o personagem pode adquirir gastando **pontos de vantagem**. São capacidades que vão além dos atributos e aptidões básicos.

**Para que serve:** Permite especialização e diferenciação entre personagens. Dois Guerreiros com os mesmos atributos podem ser radicalmente diferentes pelas vantagens que escolheram — um pode ter "Ataque Adicional" e outro "Contra-Ataque".

**Campos importantes:**
- **Nome** — ex: Treinamento em Combate Ofensivo, Saúde de Ferro, Contra-Ataque
- **Sigla** — abreviação (ex: TCO, TCD)
- **Custo base** — quantos pontos de vantagem custa
- **Níveis máximos** — quantas vezes pode ser comprada/melhorada
- **Fórmula de custo** — como calcular o custo por nível (geralmente `CUSTO_BASE * NIVEL`)
- **Categoria** — a qual categoria pertence (ver abaixo)

**Relacionamentos:**
- Cada vantagem pertence a uma **Categoria de Vantagem**
- Vantagens podem ter **pré-requisitos** (`VantagemPreRequisito`) — condições que o personagem deve cumprir para comprar (ex: "B.B.A 5+" ou "1 Renascimento")
- Vantagens concedem **efeitos** (`VantagemEfeito`) — bônus em bônus, atributos, aptidões, vida, etc.

**Na ficha do personagem:** Uma vez comprada, a vantagem **não pode ser removida** (é um investimento permanente). O nível só pode subir, nunca descer.

---

### 3.14 Categoria de Vantagem (`CategoriaVantagem`)

**O que é:** Agrupamento organizacional das vantagens por tema.

**Para que serve:** Organiza as dezenas de vantagens disponíveis em categorias para facilitar a navegação e entendimento.

**No template Klayrah Padrão:** 8 categorias:
1. **Treinamento Físico** — aprimoramento do combate corporal (TCO, TCD, TCE)
2. **Treinamento Mental** — habilidades mágicas e raciocínio (TM, TPM, TL)
3. **Ação** — habilidades ativas em combate (Ataque Adicional, Ataque Sentai)
4. **Reação** — habilidades reativas em combate (Contra-Ataque, Intercepção)
5. **Vantagem de Atributo** — poderes derivados dos atributos (CFM, Destreza Felina)
6. **Vantagem Geral** — utilitárias e de sobrevivência (Saúde de Ferro, Ambidestria)
7. **Vantagem Histórica** — background e recursos sociais (Riqueza, Capangas)
8. **Vantagem de Renascimento** — exclusivas para quem renasceu (Último Sigilo, Pensamento Bifurcado)

---

## 4. GLOSSÁRIO DE TERMOS DO DOMÍNIO

### Termos do Sistema de Atributos

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Atributo** | Característica fundamental do personagem (física ou mental) | Força, Agilidade, Vigor |
| **Base** | Valor inicial do atributo, definido na criação do personagem | Força Base = 10 |
| **Nível (campo)** | Pontos de atributo distribuídos ao subir de nível | +3 em Força por nível |
| **Outros** | Bônus diversos de fontes variadas (itens, magias temporárias, etc.) | +2 de item mágico |
| **Total** | Soma automática: Base + Nível + Outros | 10 + 3 + 2 = 15 |
| **Ímpeto** | Efeito derivado calculado a partir do Total de um atributo. Cada atributo gera um ímpeto diferente com significado narrativo | Ímpeto de Força = Total × 3 (capacidade de carga em kg). Se Força Total = 15, Ímpeto = 45 kg |
| **Limitador** | Teto máximo que qualquer atributo pode atingir no nível atual. Impede que personagens de baixo nível tenham atributos desproporcionais | Nível 1: Limitador 10. Nível 5: Limitador 50 |

### Termos do Sistema de Bônus

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **B.B.A** | Bônus Base de Ataque — mede a competência ofensiva em combate físico. Calculado pela média de Força e Agilidade | (Força 15 + Agilidade 12) / 3 = 9 |
| **B.B.M** | Bônus Base Mágico — mede o poder mágico. Calculado pela média de Sabedoria e Inteligência | (Sabedoria 20 + Inteligência 18) / 3 = 12 |
| **Bloqueio** | Capacidade de bloquear ataques físicos com escudo ou arma. Derivado de Força e Vigor | (Força 15 + Vigor 18) / 3 = 11 |
| **Reflexo** | Velocidade de reação e esquiva. Derivado de Agilidade e Astúcia | (Agilidade 12 + Astúcia 9) / 3 = 7 |
| **Percepção** | Capacidade de notar detalhes, ameaças e mudanças no ambiente | (Inteligência 18 + Intuição 14) / 3 = 10 |
| **Raciocínio** | Capacidade de raciocínio lógico, dedução e estratégia | (Inteligência 18 + Astúcia 9) / 3 = 9 |
| **Glória** | Fonte de bônus vinda de conquistas e feitos heróicos no jogo | +2 Glória em B.B.A por matar um dragão |

### Termos do Sistema de Vida

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Vida Total** | Quantidade total de pontos de vida do personagem. Fórmula: Vigor + Nível + VT + Renascimentos + Outros | Vigor 18 + Nível 5 + VT 10 + Renasc 0 + Outros 2 = 35 |
| **VG** | Vida do Vigor — componente de vida que vem do atributo Vigor. É o Vigor Total do personagem | Se Vigor Total = 18, então VG = 18 |
| **VT** | Vida de Vantagens — bônus de vida vindo de vantagens compradas (ex: "Saúde de Ferro" dá +5 por nível) | Saúde de Ferro nível 2 = +10 VT |
| **RD** | Redução de Dano (físico) — quantidade de dano físico que o personagem ignora em cada golpe. Ímpeto do Vigor | Vigor Total 30 → RD = 3 |
| **RDM** | Redução de Dano Mágico — quantidade de dano mágico que o personagem ignora. Ímpeto da Sabedoria | Sabedoria Total 20 → RDM = 2 |
| **Membro do corpo** | Parte do corpo que pode sofrer dano localizado. Cada membro tem uma porcentagem da vida total | Cabeça = 75% da vida total. Se vida total = 100, cabeça aguenta 75 de dano |
| **Sangue** | Membro especial que representa o sistema circulatório. Dano em sangue geralmente vem de venenos, hemorragias | 100% da vida total |

### Termos do Sistema de Essência

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Essência** | Recurso mágico/espiritual do personagem. Usado para lançar magias, ativar habilidades especiais e poderes sobrenaturais | Essência Total = 25. Gasta 5 para lançar uma magia, resta 20 |
| **Essência Restante** | Essência Total menos o que já foi gasto. Quando chega a 0, o personagem não pode mais usar magias | Total 25 - Gastos 5 = 20 restante |

### Termos do Sistema de Progressão

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Experiência (XP)** | Pontos acumulados por completar desafios, derrotar inimigos e progredir na história. Determina o nível do personagem | 15.000 XP = Nível 5 |
| **Nível** | Grau de poder e experiência do personagem, calculado automaticamente pela XP. Vai de 0 (iniciante) a 35 (transcendente) | Nível 10 = Veterano |
| **Renascimento** | Ciclo de "morte e ressurreição" especial disponível a partir do nível 31. Ao renascer, o personagem ganha poderes exclusivos e bônus em vida, essência e ameaça. Representa transcender a mortalidade | Personagem com 2 renascimentos tem acesso a vantagens exclusivas como "Previsão em Combate" |
| **Pontos de Atributo** | Pontos que o personagem recebe ao subir de nível para distribuir entre seus atributos (padrão: 3 por nível) | Subiu para nível 5 → ganhou 3 pontos → colocou 2 em Força e 1 em Agilidade |
| **Pontos de Vantagem** | Pontos que o personagem recebe ao subir de nível para comprar vantagens (padrão: 3 por nível) | 15 pontos acumulados → gasta 4 para comprar "TCO nível 1" |

### Termos do Sistema de Ameaça

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Ameaça** | Indicador numérico do nível de perigo que o personagem representa. Combina nível, equipamentos, títulos e renascimentos | Nível 10 + Itens 3 + Títulos 2 + Renasc 0 + Outros 1 = 16 |

### Termos do Sistema de Prospecção

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Prospecção** | Recurso **extremamente raro** e limitado que o Mestre concede. É um dado extra que o jogador pode somar a uma jogada de alto risco. O Mestre decide quais dados dar e em que quantidade, geralmente poucos por nível ou a cada X níveis | Mestre dá 1d6 de prospecção. Jogador usa em um ataque decisivo: soma +4 (resultado do d6) ao seu ataque |
| **Dado de Prospecção** | O tipo de dado usado na prospecção (d3, d4, d6, d8, d10, d12). Quanto mais faces, mais poderoso | d12 de prospecção pode somar até 12 a uma jogada |

### Termos de Personalidade e Identidade

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Insólitus** | Traço especial, incomum ou sobrenatural que torna o personagem único. Diferente de uma simples descrição narrativa, o Insólitus tem **impacto mecânico real**: pode desbloquear bônus extras em atributos, liberar vantagens exclusivas, conceder acesso a uma raça especial, dar resistências incomuns, ou praticamente qualquer benefício que o Mestre decidir. É a "carta coringa" do personagem — algo excepcional que vai além do que raça e classe oferecem | "Descendente de Reis" (pode liberar vantagens de liderança exclusivas), "Sangue de Dragão" (pode dar acesso a uma raça especial ou bônus em Vigor), "Abençoado por Gamaiel" (pode desbloquear bônus em Sabedoria ou vantagens mágicas extras) |
| **Índole** | Alinhamento moral do personagem — sua tendência entre bem e mal | Bom, Mau, Neutro |
| **Presença** | Alinhamento ético/comportamental — como o personagem se relaciona com regras e ordem | Bom (segue regras), Leal (devotado a algo), Caótico (rebelde), Neutro |
| **Título Heróico** | Alcunha ou título conquistado pelo personagem através de feitos no jogo | "O Matador de Dragões", "A Sombra de Eldoria" |
| **Origem** | De onde o personagem veio, sua terra natal ou background | "Nascido nas Montanhas Gélidas de Korth" |
| **Arquétipo de Referência** | Referência fictícia ou real que inspira a interpretação do personagem | "Aragorn de Senhor dos Anéis", "Geralt de Rívia" |

### Termos de Vantagens

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Vantagem** | Habilidade ou poder especial comprado com pontos de vantagem. Uma vez comprada, é permanente | "Treinamento em Combate Ofensivo" (TCO) |
| **Nível da Vantagem** | Quantas vezes a vantagem foi melhorada. Cada nível dá mais bônus e custa mais pontos | TCO nível 3 = +3 em B.B.A |
| **Pré-requisito** | Condição que o personagem deve atender para comprar uma vantagem | "B.B.A 5+" para comprar TCO. "1 Renascimento" para Último Sigilo |
| **Efeito** | O bônus ou poder que a vantagem concede | TCO: +1 em B.B.A por nível. Saúde de Ferro: +5 de Vida por nível |
| **TCO** | Treinamento em Combate Ofensivo — principal vantagem de ataque físico | +1 B.B.A por nível, até 10 níveis |
| **TCD** | Treinamento em Combate Defensivo — principal vantagem de defesa | +1 Bloqueio por nível + RD natural |
| **TCE** | Treinamento em Combate Evasivo — vantagem de esquiva | +2 Reflexo por nível (bônus dobrado) |
| **TM** | Treinamento Mágico — principal vantagem de poder mágico | +1 B.B.M por nível |
| **CFM** | Capacidade de Força Máxima — poder derivado da Força | 1D3 em danos por contusão |
| **D.UP** | Dado Up / Progressão de dado — mecanismo onde o dado evolui (d3 → d4 → d6 → d8 → d10) ao subir de nível na vantagem | TCO nível 1: 1D3. TCO nível 2: 1D4. TCO nível 5: 1D10 |

### Termos do Sistema de Aptidões

| Termo | O que é | Exemplo concreto |
|-------|---------|-------------------|
| **Aptidão** | Habilidade treinável que mede competência em uma ação específica | Furtividade, Diplomacia, Acrobacia |
| **Sorte (campo)** | Bônus de sorte em uma aptidão — talento natural ou favor divino | +2 de Sorte em Furtividade |
| **Classe (campo)** | Bônus de classe em uma aptidão — treinamento fornecido pela classe do personagem | Ladrão dá +3 em Furtividade |
| **Aptidão Física** | Aptidão relacionada ao corpo e movimento: Acrobacia, Guarda, Aparar, Atletismo, Resvalar, Resistência, Perseguição, Natação, Furtividade, Prestidigitação, Conduzir, Arte da Fuga | Teste de Atletismo para escalar um muro |
| **Aptidão Mental** | Aptidão relacionada à mente e percepção: Idiomas, Observação, Falsificar, Prontidão, Auto Controle, Sentir Motivação, Sobrevivência, Investigar, Blefar, Atuação, Diplomacia, Operação de Mecanismos | Teste de Blefar para mentir para o guarda |

---

## 5. SIGLAS, ABREVIAÇÕES E O SISTEMA DE FÓRMULAS

### Por que tudo tem sigla?

No Klayrah RPG, praticamente toda configuração pode ter uma **sigla ou abreviação** (campo `abreviacao` nos atributos, `sigla` nas vantagens, etc.). Isso não é apenas uma conveniência visual — é uma peça fundamental da arquitetura do sistema.

O sistema permite que o Mestre crie **fórmulas matemáticas personalizáveis** em diversas partes do jogo. Essas fórmulas usam as siglas como **variáveis**. É assim que o sistema consegue ser totalmente configurável sem código hardcoded.

### Regra crítica: sigla EXCLUSIVA por jogo

**Toda sigla/abreviação deve ser ÚNICA dentro de um mesmo Jogo, independente do tipo de configuração.** Se o atributo "Força" já usa a sigla `FOR`, nenhuma vantagem, bônus, aptidão ou qualquer outra configuração daquele jogo pode usar `FOR`. Isso é obrigatório porque o motor de fórmulas resolve variáveis por nome — se duas configurações diferentes tivessem a mesma sigla, o sistema não saberia qual valor usar, gerando cálculos incorretos ou ambíguos.

> ⚠️ **Para a IA**: ao implementar validações de criação/edição de qualquer configuração que tenha sigla ou abreviação, SEMPRE verificar unicidade da sigla **no escopo do jogo inteiro** (cross-entity), não apenas dentro da mesma tabela.

### Como funciona

1. O Mestre cria um atributo "Força" com abreviação `FOR`
2. O Mestre cria um bônus "B.B.A" com fórmula `FLOOR((FOR + AGI) / 3)`
3. Quando o sistema calcula o B.B.A de um personagem, ele substitui `FOR` e `AGI` pelos valores totais daqueles atributos na ficha e avalia a expressão
4. Se outra configuração usasse `FOR` como sigla, o motor não saberia se usar o valor do atributo ou da outra configuração — por isso a unicidade é obrigatória

### Onde fórmulas são usadas

| Onde | Exemplo de fórmula | Variáveis disponíveis |
|------|---------------------|-----------------------|
| **Ímpeto de atributo** | `TOTAL * 3` (Força → carga em kg) | `TOTAL` (valor total do atributo) |
| **Bônus calculado** | `FLOOR((FOR + AGI) / 3)` (B.B.A) | Abreviações de todos os atributos do jogo (`FOR`, `AGI`, `VIG`, `SAB`, `INT`, `INTU`, `AST`) |
| **Custo de vantagem** | `CUSTO_BASE * NIVEL_VANTAGEM` | `CUSTO_BASE`, `NIVEL_VANTAGEM` |
| **Vida total** | `VIGOR + NIVEL + VANTAGENS + RENASCIMENTOS + OUTROS` | Componentes de vida |
| **Essência total** | `FLOOR((VIGOR + SABEDORIA) / 2) + NIVEL + RENASCIMENTOS + VANTAGENS + OUTROS` | Componentes de essência |
| **Ameaça** | `NIVEL + ITENS + TITULOS + RENASCIMENTOS + OUTROS` | Componentes de ameaça |

### Funções matemáticas disponíveis

O motor de fórmulas (exp4j) suporta: `FLOOR()`, `CEIL()`, `MIN()`, `MAX()`, `ABS()`, `SQRT()` e operadores `+`, `-`, `*`, `/`, `^`, `%`.

### Tabela de siglas padrão (Template Klayrah)

| Sigla | Significado | Tipo |
|-------|-------------|------|
| `FOR` | Força | Atributo |
| `AGI` | Agilidade | Atributo |
| `VIG` | Vigor | Atributo |
| `SAB` | Sabedoria | Atributo |
| `INT` | Inteligência | Atributo |
| `INTU` | Intuição | Atributo |
| `AST` | Astúcia | Atributo |
| `TCO` | Treinamento em Combate Ofensivo | Vantagem |
| `TCD` | Treinamento em Combate Defensivo | Vantagem |
| `TCE` | Treinamento em Combate Evasivo | Vantagem |
| `TM` | Treinamento Mágico | Vantagem |
| `TPM` | Treinamento em Percepção Mágica | Vantagem |
| `TL` | Treinamento Lógico | Vantagem |
| `T.M` | Treinamento em Manipulação | Vantagem |
| `CFM` | Capacidade de Força Máxima | Vantagem de Atributo |
| `DM` | Domínio de Força | Vantagem de Atributo |
| `DF` | Destreza Felina | Vantagem de Atributo |
| `DV` | Domínio de Vigor | Vantagem de Atributo |
| `SG` | Sabedoria de Gamaiel | Vantagem de Atributo |
| `IN` | Inteligência de Nyck | Vantagem de Atributo |
| `B.B.A` | Bônus Base de Ataque | Bônus |
| `B.B.M` | Bônus Base Mágico | Bônus |
| `RD` | Redução de Dano | Derivado (Ímpeto do Vigor) |
| `RDM` | Redução de Dano Mágico | Derivado (Ímpeto da Sabedoria) |
| `VG` | Vida do Vigor | Componente de Vida |
| `VT` | Vida de Vantagens | Componente de Vida |
| `D.UP` | Dado Up (progressão de dado) | Mecânica de progressão |
| `NVS` | Nível de Vida Superior | Progressão |

> **Importante para a IA**: quando o Mestre cria um atributo ou vantagem nova, ele define a sigla. Essa sigla pode então ser usada em fórmulas de outros elementos. Todo o sistema é interligado por essas referências textuais. **A sigla DEVE ser única por jogo, cross-entity** (não pode existir a mesma sigla em atributos, bônus, vantagens ou qualquer outra configuração do mesmo jogo).

---

## 6. TERMOS TÉCNICOS DO SISTEMA

| Termo | O que é |
|-------|---------|
| **Fórmula** | Expressão matemática armazenada como texto que o sistema avalia em tempo de execução usando a biblioteca exp4j. Permite que o Mestre customize cálculos sem alterar código. As fórmulas usam **siglas/abreviações** das configurações como variáveis (ex: `FOR`, `AGI`), criando um sistema interligado onde alterar uma sigla pode afetar múltiplas fórmulas. Ex: `FLOOR((FOR + AGI) / 3)` |
| **Template Klayrah Padrão** | Conjunto de configurações default que é aplicado automaticamente quando o Mestre cria um novo Jogo. Inclui os 7 atributos, 35 níveis, 24 aptidões, 12 classes, 6 bônus, 7 membros do corpo, etc. O Mestre pode modificar tudo depois |
| **Soft Delete** | Exclusão lógica — o registro não é removido do banco, apenas marcado com `deleted_at`. Permite recuperação e auditoria |
| **Ordem de Exibição** | Campo numérico presente em todas as configurações que define em que ordem elas aparecem na interface. Permite que o Mestre reorganize a ficha visualmente |
| **Configuração por Jogo** | Cada jogo tem suas próprias configurações independentes. Alterar os atributos do Jogo A não afeta o Jogo B |
| **Auditoria (Envers)** | Todas as alterações em fichas são registradas automaticamente pelo Hibernate Envers, permitindo ver o histórico completo de mudanças |

---

## 7. FLUXO RESUMIDO: DA CONFIGURAÇÃO À FICHA

```
1. Mestre cria Jogo
   └─→ Sistema aplica Template Klayrah Padrão
       └─→ 13 tipos de configuração são populados automaticamente

2. Mestre ajusta configurações (opcional)
   └─→ Pode renomear atributos, mudar fórmulas, adicionar classes, etc.

3. Jogador entra no Jogo
   └─→ Cria sua Ficha de Personagem
       └─→ Ficha referencia as configurações do Jogo
           ├─→ Atributos da ficha apontam para AtributoConfig
           ├─→ Aptidões da ficha apontam para AptidaoConfig
           ├─→ Bônus da ficha apontam para BonusConfig
           ├─→ Vida por membro aponta para MembroCorpoConfig
           ├─→ Prospecção aponta para DadoProspeccaoConfig
           └─→ Vantagens apontam para VantagemConfig

4. Jogador edita ficha
   └─→ Frontend calcula valores derivados em tempo real (preview)
       └─→ Ao salvar, Backend recalcula tudo (fonte oficial)
           └─→ Frontend substitui valores temporários pelos oficiais
```

---

## 8. STATUS ATUAL DO DESENVOLVIMENTO

### ✅ Implementado (Configurações — CRUD completo)
- AtributoConfig
- NivelConfig
- AptidaoConfig
- TipoAptidao
- BonusConfig
- ClassePersonagem
- Raca (com RacaBonusAtributo)
- MembroCorpoConfig
- DadoProspeccaoConfig
- GeneroConfig
- IndoleConfig
- PresencaConfig
- VantagemConfig

### 🔜 Próximos passos
- Motor de cálculos (FormulaEvaluatorService avaliando fórmulas configuráveis)
- CRUD de Ficha de Personagem e todos os seus sub-componentes (FichaAtributo, FichaAptidao, FichaBonus, FichaVida, FichaVidaMembro, FichaEssencia, FichaAmeaca, FichaProspeccao, FichaVantagem)
- Galerias de imagens e anotações
- Frontend Angular com PrimeNG

---

*Última atualização: Março 2026*
