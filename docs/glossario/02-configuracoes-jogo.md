# Configurações do Jogo

> 📖 Parte 2 do Glossário. Voltar ao [índice](../GLOSSARIO.md).

As configurações são criadas pelo Mestre e definem as regras do jogo. Atualmente temos **13 tipos de configuração** com CRUD implementado. Cada uma é explicada abaixo.

---

## Atributo (`AtributoConfig`)

**O que é:** As características fundamentais que definem o potencial físico e mental de um personagem. São os "pilares" sobre os quais quase tudo é calculado.

**Para que serve:** Cada atributo tem um valor numérico que influencia diretamente nos bônus de combate, nos cálculos de vida, essência, e em praticamente toda mecânica do jogo.

**Campos importantes:**
- **Nome** — ex: Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia
- **Fórmula de Ímpeto** — uma fórmula matemática que calcula um efeito derivado do total daquele atributo (ver Ímpeto abaixo)
- **Valor mínimo / máximo** — limites do atributo

**No template Klayrah Padrão:** 7 atributos.

**Na ficha do personagem:** Cada atributo tem valor **Base** (inicial), **Nível** (pontos ganhos ao subir de nível), **Outros** (bônus diversos) e **Total** (soma). O Ímpeto é calculado automaticamente a partir do Total.

---

## Nível (`NivelConfig`)

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

## Aptidão (`AptidaoConfig`)

**O que é:** Habilidades específicas e treináveis do personagem. São perícias ou competências que definem o que o personagem sabe fazer bem.

**Para que serve:** Determinam o sucesso em ações específicas durante o jogo. Quando o personagem tenta escalar um muro, o Mestre pede um teste de "Atletismo". Quando tenta mentir, um teste de "Blefar".

**Campos importantes:**
- **Nome** — ex: Acrobacia, Furtividade, Diplomacia, Investigar
- **Tipo de Aptidão** — a qual categoria pertence (Física ou Mental)

**No template Klayrah Padrão:** 24 aptidões — 12 físicas e 12 mentais.

**Na ficha do personagem:** Cada aptidão tem valor **Base**, **Sorte** (bônus de sorte) e **Classe** (bônus da classe). O Total é a soma.

---

## Tipo de Aptidão (`TipoAptidao`)

**O que é:** A categoria que agrupa aptidões semelhantes. Funciona como um "rótulo organizacional" para as aptidões.

**Para que serve:** Organizar as aptidões na interface e permitir que certas mecânicas afetem "todas as aptidões de um tipo" (ex: uma vantagem que dá +2 em todas as aptidões mentais).

**No template Klayrah Padrão:** 2 tipos — Física e Mental.

---

## Bônus (`BonusConfig`)

**O que é:** Valores calculados automaticamente a partir dos atributos. Representam a competência do personagem em áreas amplas como combate, defesa e percepção.

**Para que serve:** São usados como "modificadores" nas rolagens de dados durante o jogo. Quando o personagem ataca, soma o B.B.A à jogada. Quando tenta perceber algo, soma a Percepção.

**Campos importantes:**
- **Nome** — ex: B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio
- **Fórmula base** — como o bônus é calculado a partir dos atributos. Ex: B.B.A = `FLOOR((FORCA + AGILIDADE) / 3)`

**No template Klayrah Padrão:** 6 bônus calculados.

**Na ficha do personagem:** Além do valor base (calculado pela fórmula), cada bônus pode receber acréscimos de 5 fontes: **Vantagens**, **Classe**, **Itens**, **Glória** e **Outros**.

---

## Classe de Personagem (`ClassePersonagem`)

**O que é:** O "papel" ou "profissão" do personagem no mundo do jogo. Define a especialização e os bônus que o personagem recebe.

**Para que serve:** Dá identidade mecânica ao personagem. Um Guerreiro terá bônus em combate corpo a corpo, um Mago em poder mágico, um Ladrão em furtividade.

**No template Klayrah Padrão:** 12 classes — Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno (Herdeiro), Mago, Feiticeiro, Necromante, Sacerdote, Ladrão, Negociante.

**Relacionamentos:**
- Uma classe pode dar bônus em certos **Bônus** (`ClasseBonus`) — ex: Guerreiro dá +1 em B.B.A por nível
- Uma classe pode dar bônus em certas **Aptidões** (`ClasseAptidaoBonus`) — ex: Ladrão dá +2 em Furtividade

---

## Raça (`Raca`)

**O que é:** A espécie ou povo do personagem no mundo do jogo. Raças concedem modificadores nos atributos, refletindo características biológicas.

**Para que serve:** Dá variedade na criação de personagens. Um Elfo pode ter +2 em Agilidade mas -1 em Vigor. Um Anão pode ter +2 em Vigor mas -1 em Agilidade.

**Relacionamentos:**
- Uma raça tem **bônus de atributo** (`RacaBonusAtributo`) — valores positivos ou negativos aplicados a atributos específicos
- Uma raça pode restringir **classes permitidas** (`RacaClassePermitida`) — nem toda raça pode ser qualquer classe

---

## Membro do Corpo (`MembroCorpoConfig`)

**O que é:** As partes do corpo do personagem que podem sofrer dano individualmente. O sistema Klayrah tem um sistema de **dano localizado** — não é apenas "tirou 10 de vida", mas "tirou 10 de vida no braço direito".

**Para que serve:** Permite um sistema de combate mais tático e detalhado. Se a cabeça do personagem chega a 0, o efeito é diferente de se a perna chegar a 0.

**Campos importantes:**
- **Nome** — ex: Cabeça, Tronco, Braço Direito, Perna Esquerda, Sangue
- **Porcentagem da vida** — quanto da vida total aquele membro aguenta. Cabeça = 75%, Braço = 25%, Tronco = 100%

**No template Klayrah Padrão:** 7 membros.

**Na ficha do personagem:** Cada membro tem seu valor de vida calculado (vida total × porcentagem) e um campo de dano recebido.

---

## Dado de Prospecção (`DadoProspeccaoConfig`)

**O que é:** Tipos de dados (de RPG) que o Mestre pode conceder aos jogadores como recurso especial e limitado.

**Para que serve:** Prospecção é um recurso **muito raro** que o Mestre distribui com parcimônia. Quando um jogador usa um dado de prospecção, ele adiciona aquele dado a uma jogada de alto risco, aumentando suas chances. É como um "coringa" — poderoso, mas escasso.

> *Exemplo: o Mestre dá ao jogador 1 dado d6 de prospecção. Em um momento crítico, o jogador decide usá-lo e soma o resultado do d6 à sua jogada.*

**Campos importantes:**
- **Nome** — ex: d3, d4, d6, d8, d10, d12
- **Número de faces** — valor máximo do dado

**No template Klayrah Padrão:** 6 tipos de dados (d3 a d12).

**Na ficha do personagem:** Um contador para cada dado, mostrando quantos o personagem possui atualmente.

---

## Gênero (`GeneroConfig`)

**O que é:** Opções de gênero disponíveis para os personagens do jogo.

**Para que serve:** Define as opções que aparecem no dropdown de gênero da ficha. No Klayrah, o gênero influencia no cálculo automático do peso (via BMI).

**No template Klayrah Padrão:** Masculino, Feminino, Outro.

---

## Índole (`IndoleConfig`)

**O que é:** O alinhamento moral do personagem — se ele tende ao bem, ao mal ou é neutro.

**Para que serve:** Guia a interpretação do personagem e pode ser pré-requisito para certas vantagens ou mecânicas do jogo.

**No template Klayrah Padrão:** Bom, Mau, Neutro.

---

## Presença (`PresencaConfig`)

**O que é:** O alinhamento ético/comportamental do personagem — como ele se posiciona frente a regras e estruturas sociais.

**Para que serve:** Complementa a Índole para dar profundidade à personalidade. Um personagem pode ser "Bom e Caótico" (bem-intencionado mas rebelde) ou "Mau e Leal" (cruel mas previsível).

**No template Klayrah Padrão:** Bom, Leal, Caótico, Neutro.

---

## Vantagem (`VantagemConfig`)

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

## Categoria de Vantagem (`CategoriaVantagem`)

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

*Última atualização: Março 2026*
