# Dataset de Itens — D&D 5e SRD Adaptado ao Klayrah

> Documento de referencia para a task P2-T6-default-dataset.md
> Produzido por: Business Analyst/PO | 2026-04-04
> Consumido por: agente BA-016-02 ao implementar DefaultGameConfigProviderImpl

---

## Premissas e Decisoes de Produto

- **Idioma:** todos os nomes em portugues (traducao livre do SRD)
- **Moeda:** moedas de ouro (po) — sem conversao para prata/cobre no MVP
- **Durabilidade:** `null` = indestrutivel (desgaste e narrativo no Klayrah). Apenas itens de uso unico (pocoes) e itens magicos tem durabilidade numerica.
- **Efeitos de armas comuns:** armas Comuns nao tem `ItemEfeito`. O dano de combate e calculado pelo B.B.A mais sistema de dados (pos-MVP). Armas magicas (+1/+2) tem `BONUS_DERIVADO` em B.B.A ou B.B.M.
- **Escudos:** concedem `BONUS_DERIVADO` em Bloqueio (nao em Defesa). A Defesa vem de armaduras.
- **Armaduras:** todas as armaduras tem `BONUS_DERIVADO` em Defesa. Armaduras Pesadas podem ter `BONUS_DERIVADO` negativo em Reflexo (penalidade).
- **RN-10 aplicada:** ItemEfeito usa FKs diretas — os nomes de BonusConfig abaixo sao os nomes configurados no DefaultGameConfigProvider para o mesmo jogo. O implementador deve resolver por nome na mesma inicializacao.
- **Classes Klayrah (12):** Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno (Herdeiro), Mago, Feiticeiro, Necromante, Sacerdote, Ladrao, Negociante.

---

## Secao 1 — Raridades (7 raridades)

Baseadas nas raridades canonicas do D&D 5e SRD, com dois adicionais para o Klayrah (Epico e Unico).

| ordem | nome | cor hex | podeJogadorAdicionar | bonusAtributoMin | bonusAtributoMax | bonusDerivadoMin | bonusDerivadoMax | descricao narrativa |
|-------|------|---------|---------------------|-----------------|-----------------|-----------------|-----------------|---------------------|
| 1 | Comum | #9d9d9d | true | 0 | 0 | 0 | 0 | Itens mundanos sem encantamento. Encontrados em qualquer mercado. |
| 2 | Incomum | #1eff00 | false | 1 | 1 | 1 | 1 | Levemente encantado ou de qualidade excepcional. Forjado por artesao habilidoso ou com leve toque arcano. |
| 3 | Raro | #0070dd | false | 1 | 2 | 1 | 2 | Encantamento moderado, raramente encontrado. Requer mestre artesao ou rituais especializados para criar. |
| 4 | Muito Raro | #a335ee | false | 2 | 3 | 2 | 3 | Encantamento poderoso, obra de mestre arcano. Poucos exemplares existem em uma regiao inteira. |
| 5 | Epico | #ff8000 | false | 3 | 4 | 3 | 4 | Artefato de grande poder com historia propria. Criado em eras antigas ou por entidades sobrenaturais. |
| 6 | Lendario | #e6cc80 | false | 4 | 5 | 4 | 5 | Um dos poucos existentes no mundo inteiro. Muitos sao descritos apenas em lendas e textos antigos. |
| 7 | Unico | #e268a8 | false | 0 | 0 | 0 | 0 | Criacao singular do Mestre, sem referencia de custo ou poder. Cada peca e irreproduzivel. |

**Notas sobre a paleta de cores:**
- As cores seguem a convencao cromatica do D&D 5e (cinza Comum, verde Incomum, azul Raro, roxo Muito Raro).
- Epico (laranja) e Unico (rosa) sao adicoes proprias do Klayrah, para distinguir artefatos de era antiga (Epico) e criacoes singulares do Mestre (Unico).
- Cores sugeridas para chips PrimeNG: usar `p-tag` com `[style]="{'background-color': raridade.cor}"`.

---

## Secao 2 — Tipos de Item (20 tipos)

Hierarquia de dois niveis: `categoria` (ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, AVENTURA) e `subcategoria` (refinamento).

| ordem | nome | categoria | subcategoria | requerDuasMaos | notas |
|-------|------|-----------|-------------|----------------|-------|
| 1 | Espada Curta | ARMA | ESPADA | false | Arma de uma mao, finura, leve |
| 2 | Espada Longa | ARMA | ESPADA | false | Versatil (pode usar com duas maos) |
| 3 | Espada Dupla | ARMA | ESPADA | true | Obrigatoriamente duas maos |
| 4 | Arco Curto | ARMA | ARCO | true | Exige municao (Flecha) |
| 5 | Arco Longo | ARMA | ARCO | true | Maior alcance, exige municao |
| 6 | Adaga | ARMA | ADAGA | false | Finura, arremesso, leve |
| 7 | Machado de Batalha | ARMA | MACHADO | false | Versatil |
| 8 | Machado Grande | ARMA | MACHADO | true | Pesado, duas maos |
| 9 | Martelo de Guerra | ARMA | MARTELO | false | Versatil |
| 10 | Cajado | ARMA | CAJADO | true | Foco arcano para magos |
| 11 | Lanca | ARMA | LANCA | false | Arremesso, versatil |
| 12 | Armadura Leve | ARMADURA | ARMADURA_LEVE | false | Sem penalidade em Reflexo |
| 13 | Armadura Media | ARMADURA | ARMADURA_MEDIA | false | Penalidade leve em Reflexo em versoes pesadas |
| 14 | Armadura Pesada | ARMADURA | ARMADURA_PESADA | false | Penalidade em Reflexo, requer FOR minima |
| 15 | Escudo | ARMADURA | ESCUDO | false | Ocupa mao secundaria, bonus em Bloqueio |
| 16 | Anel | ACESSORIO | ANEL | false | Itens magicos de dedo |
| 17 | Amuleto | ACESSORIO | AMULETO | false | Itens magicos de pescoco |
| 18 | Pocao | CONSUMIVEL | POCAO | false | Consumivel de uso unico (duracaoPadrao = 1) |
| 19 | Municao | CONSUMIVEL | MUNICAO | false | Empilha automaticamente (RN-ITEM-20) |
| 20 | Equipamento de Aventura | AVENTURA | OUTROS | false | Ferramentas, kits, itens utilitarios |

---

## Secao 3 — Catalogo de Itens (40 itens)

### 3.1 Armas (15 itens)

#### Itens Comuns — Armas sem efeito automatico

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|
| 1 | Adaga | Comum | Adaga | 0.45 | 2 | null | 1 | finura, arremesso (alcance 6/18m), leve | nenhum |
| 2 | Espada Curta | Comum | Espada Curta | 0.90 | 10 | null | 1 | finura, leve | nenhum |
| 3 | Espada Longa | Comum | Espada Longa | 1.36 | 15 | null | 1 | versatil | nenhum |
| 6 | Machadinha | Comum | Machado de Batalha | 0.90 | 5 | null | 1 | leve, arremesso (alcance 6/12m) | nenhum |
| 7 | Machado de Batalha | Comum | Machado de Batalha | 1.80 | 10 | null | 1 | versatil | nenhum |
| 8 | Machado Grande | Comum | Machado Grande | 3.17 | 30 | null | 3 | pesado, duas maos | nenhum |
| 9 | Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | null | 1 | versatil | nenhum |
| 10 | Arco Curto | Comum | Arco Curto | 0.90 | 25 | null | 1 | duas maos, municao | nenhum |
| 11 | Arco Longo | Comum | Arco Longo | 1.80 | 50 | null | 2 | duas maos, municao, pesado | nenhum |
| 13 | Cajado de Madeira | Comum | Cajado | 1.80 | 5 | null | 1 | versatil, duas maos, foco arcano | nenhum |
| 15 | Lanca | Comum | Lanca | 1.36 | 1 | null | 1 | arremesso (alcance 6/12m), versatil | nenhum |

#### Itens Magicos — Armas com efeito automatico

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|
| 4 | Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | 1 | versatil, magica | BONUS_DERIVADO(B.B.A, +1) |
| 5 | Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | 5 | versatil, magica | BONUS_DERIVADO(B.B.A, +2) |
| 12 | Arco Longo +1 | Incomum | Arco Longo | 1.80 | 500 | 10 | 4 | duas maos, municao, magico | BONUS_DERIVADO(B.B.A, +1) |
| 14 | Cajado Arcano +1 | Incomum | Cajado | 2.00 | 500 | 10 | 3 | magico, foco arcano | BONUS_DERIVADO(B.B.M, +1) |

**Flavour text das armas:**

| nome | descricao |
|------|-----------|
| Adaga | Uma lamina curta e discreta, favorita de viajantes e assassinos. Cabe em qualquer bolso e resolve problemas que espadas nao conseguem. |
| Espada Curta | Equilibrio perfeito entre velocidade e dano. Usada por exploradores e por aqueles que preferem agilidade a forca bruta. |
| Espada Longa | A arma classica do guerreiro. Pode ser empunhada com uma ou duas maos, adaptando-se ao estilo de quem a usa. |
| Espada Longa +1 | A lamina pulsa com um leve brilho azulado. Um encantamento basico de precisao guia o golpe quase sem que o portador perceba. |
| Espada Longa +2 | Forjada em aaco estelar e encantada por um Arcano de segunda ordem. A lamina treme levemente ao detectar inimigos proximos. |
| Machadinha | Compacta e letal. Pode ser lancada com precisao surpreendente ou usada em combate de curto alcance. |
| Machado de Batalha | Uma arma robusta que amplifica cada golpe com o peso do cabo longo. Versatil nas maos de um guerreiro experiente. |
| Machado Grande | Forjado para destruicao maxima. Exige forca e destreza para ser empunhado, mas o resultado e devastador. |
| Martelo de Guerra | O peso concentrado na cabeca transforma cada golpe em um impacto ensurdecedor. Favorito de sacerdotes guerreiros. |
| Arco Curto | Leve e agil, ideal para cavalaria ou explorador em movimento. Sacrifica alcance por velocidade de disparo. |
| Arco Longo | A arma dos arqueiros de elite. Exige treino e forca, mas dominar o arco longo transforma o portador numa ameaca a distancia. |
| Arco Longo +1 | O cabo foi entalhado com sigilos de precisao. Flechas disparadas por este arco corrigem levemente a propria trajetoria. |
| Cajado de Madeira | Um bastao robusto de carvalho anciao. Serve como ponto de foco para magias ou simplesmente para abrir caminho. |
| Cajado Arcano +1 | Cristais arcanos incrustados no topo concentram energia magica. Magos que o empunham sentem seus feiiticos fluirem com mais facilidade. |
| Lanca | Alcance e versatilidade numa unica arma. Pode ser lancada ou usada corpo a corpo, adaptando-se ao campo de batalha. |

---

### 3.2 Armaduras e Escudos (10 itens)

#### Armaduras Leves

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito | requisitos |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|-----------| 
| 16 | Gibao de Couro | Comum | Armadura Leve | 4.50 | 10 | null | 1 | armadura leve, sem penalidade de movimento | BONUS_DERIVADO(Defesa, +1) | nenhum |
| 17 | Couro Batido | Comum | Armadura Leve | 11.30 | 45 | null | 1 | armadura leve, tratada com resina | BONUS_DERIVADO(Defesa, +2) | nenhum |

#### Armaduras Medias

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito | requisitos |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|-----------| 
| 18 | Camisao de Malha | Comum | Armadura Media | 13.60 | 50 | null | 2 | armadura media | BONUS_DERIVADO(Defesa, +3) | nenhum |
| 19 | Cota de Escamas | Comum | Armadura Media | 20.40 | 50 | null | 3 | armadura media, desvantagem em Furtividade | BONUS_DERIVADO(Defesa, +4) | nenhum |

#### Armaduras Pesadas

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito | requisitos |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|-----------| 
| 20 | Cota de Malha | Comum | Armadura Pesada | 27.20 | 75 | null | 4 | armadura pesada, exige FOR minima 10 | BONUS_DERIVADO(Defesa, +5) | ATRIBUTO(FOR, 10) |
| 21 | Meia Placa | Comum | Armadura Pesada | 19.90 | 750 | null | 5 | armadura pesada, placas parciais | BONUS_DERIVADO(Defesa, +5), BONUS_DERIVADO(Reflexo, +1) | nenhum |
| 22 | Placa Completa | Raro | Armadura Pesada | 29.50 | 1500 | 15 | 7 | armadura pesada, encantada, exige FOR minima 12 | BONUS_DERIVADO(Defesa, +6) | ATRIBUTO(FOR, 12) |

**Nota sobre Meia Placa:** e a unica armadura pesada no dataset que concede bonus positivo em Reflexo (+1), representando o design ergonomico das placas parciais que nao restringem tanto o movimento quanto a Placa Completa. A Cota de Malha comum e pesada mas nao penaliza Reflexo (a penalidade e narrativa via "desvantagem em Furtividade" no campo `propriedades`).

#### Escudos

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|
| 23 | Escudo de Madeira | Comum | Escudo | 2.72 | 10 | null | 1 | escudo, mao secundaria | BONUS_DERIVADO(Bloqueio, +1) |
| 24 | Escudo de Aco | Comum | Escudo | 2.72 | 20 | null | 1 | escudo, mao secundaria | BONUS_DERIVADO(Bloqueio, +2) |
| 25 | Escudo Enfeiticado +1 | Incomum | Escudo | 2.72 | 500 | 10 | 3 | escudo, mao secundaria, magico | BONUS_DERIVADO(Bloqueio, +2), BONUS_DERIVADO(Defesa, +1) |

**Flavour text das armaduras e escudos:**

| nome | descricao |
|------|-----------|
| Gibao de Couro | Couro cru costurado em camadas sobrepostas. Nao impressiona ninguem, mas e melhor do que nada entre a carne e a lamina inimiga. |
| Couro Batido | Couro curtido em resina e endurecido ao sol. Os guerreiros que o vestem jurariam que a resina endurece com o tempo de uso. |
| Camisao de Malha | Argolas de aco tecidas em padrao duplo. Flexivel o suficiente para nao atrapalhar e solida o suficiente para fazer a diferenca. |
| Cota de Escamas | Escamas metalicas sobrepostas como plumagem de dragao. Resistente, mas o barulho delata o portador a distancia. |
| Cota de Malha | Aneis de aco soldados em camadas densas. Pesada, quente e tatica — mas os veteranos sabem que esse peso salva vidas. |
| Meia Placa | Placas de aco protegendo os pontos vitais, combinadas com malha nas articulacoes. O melhor equilibrio entre protecao e movimento. |
| Placa Completa | Obra-prima da forja. Cada peca foi moldada para o corpo do portador por um mestre ferreiro. Encantamentos basicos a tornam ainda mais resistente ao tempo. |
| Escudo de Madeira | Madeira carvalho reforcada com aro de ferro. O primeiro escudo de todo aventureiro — simples, barato e eficaz. |
| Escudo de Aco | Disco de aco solido. Mais pesado, mas cada golpe desviado ressoa num clangor que intimida quem ataca. |
| Escudo Enfeiticado +1 | O aco desta peca foi temperado com agua de fonte sagrada e inscricoes de warding. Ataques parecem escorregar da superficie. |

---

### 3.3 Acessorios e Itens Magicos (5 itens)

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito | requisitos |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|-----------| 
| 26 | Anel da Forca +1 | Raro | Anel | 0.01 | 2000 | null | 5 | magico | BONUS_ATRIBUTO(Forca, +1) | nenhum |
| 27 | Anel de Protecao +1 | Raro | Anel | 0.01 | 2000 | null | 5 | magico | BONUS_DERIVADO(Defesa, +1), BONUS_DERIVADO(Bloqueio, +1) | nenhum |
| 28 | Amuleto de Saude | Incomum | Amuleto | 0.05 | 500 | null | 3 | magico | BONUS_VIDA(+5) | nenhum |
| 29 | Amuleto da Essencia | Incomum | Amuleto | 0.05 | 500 | null | 3 | magico | BONUS_ESSENCIA(+5) | nenhum |
| 30 | Manto de Elvenkind | Muito Raro | Amuleto | 0.45 | 5000 | null | 7 | magico, talhado por elfos das florestas profundas | BONUS_DERIVADO(Reflexo, +3), BONUS_DERIVADO(Percepcao, +2) | nenhum |

**Nota sobre Anel da Forca:** usa `BONUS_ATRIBUTO` com `atributoAlvo` resolvido pelo nome "Forca" no mesmo jogo. O campo `itens` de `FichaAtributo` recebe o `valorFixo` do efeito.

**Flavour text dos acessorios:**

| nome | descricao |
|------|-----------|
| Anel da Forca +1 | Uma banda simples de aco negro sem ornamentos. O portador sente os musculos responderem com uma leve resistencia a mais, como se o anel ampliasse cada tensao muscular. |
| Anel de Protecao +1 | Um sigilho de escudo esta gravado na face interna do anel. Invisivel ao olhar, mas o portador sente um leve calor sempre que um golpe e desviado. |
| Amuleto de Saude | Um pequeno berloque de osso esculpido, cravado com rubi. Pulsantes quando encostados a pele, parecem sincronizar com os batimentos cardiacos do portador. |
| Amuleto da Essencia | Cristal azul lapidado em formato gota, preso por fios de prata. Magos que o usam descrevem a sensacao de um reservatorio ligeiramente mais amplo de onde tirar suas magias. |
| Manto de Elvenkind | Tecido com fios da floresta de Elvrath, esta capa muda de cor para combinar com o ambiente. O portador parece sentir o ambiente ao redor mais nitidamente. |

---

### 3.4 Consumiveis (5 itens)

**Nota sobre consumiveis:** Pocoes tem `duracaoPadrao = 1` (usadas uma vez) e `equipado = false` permanente (nunca sao "equipadas" — sao consumidas via endpoint dedicado, pos-MVP). No MVP, pocoes sao apenas informativas no inventario — os efeitos de cura nao sao aplicados automaticamente.

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|
| 31 | Pocao de Cura Menor | Comum | Pocao | 0.45 | 25 | 1 | 1 | consumivel, recupera 5 de vida | nenhum (MVP: informativo) |
| 32 | Pocao de Cura | Comum | Pocao | 0.45 | 50 | 1 | 1 | consumivel, recupera 10 de vida | nenhum (MVP: informativo) |
| 33 | Pocao de Cura Superior | Incomum | Pocao | 0.45 | 200 | 1 | 3 | consumivel, recupera 25 de vida | nenhum (MVP: informativo) |
| 34 | Flecha Comum (20) | Comum | Municao | 0.45 | 1 | null | 1 | municao para arcos, empilha automaticamente | nenhum |
| 35 | Virote (20) | Comum | Municao | 0.36 | 1 | null | 1 | municao para bestas, empilha automaticamente | nenhum |

**Flavour text dos consumiveis:**

| nome | descricao |
|------|-----------|
| Pocao de Cura Menor | Um frasco de vidro contendo liquido vermelho brilhante com sabor adocicado. Feridas superficiais fecham em segundos. |
| Pocao de Cura | A versao aprimorada da pocao menor, com concentracao dupla de extratos curativos. Ervas raras do Bosque de Milthar conferem o aroma de pinheiro. |
| Pocao de Cura Superior | Elaborada por um alquimista de renome, esta pocao tem camadas de efeito: fecha feridas, reduz inflamacao e acelera a regeneracao ossea. |
| Flecha Comum (20) | Flechas de hastes de abeto com penas de ganso e pontas forjadas. Baratas e consfiaveis — o suprimento de todo arqueiro. |
| Virote (20) | Projeis curtos e pesados para bestas. O aco da ponta e tratado para menor resistencia ao ar, maximizando o alcance. |

---

### 3.5 Equipamentos de Aventura (5 itens)

| # | nome | raridade | tipo | peso kg | valor po | duracaoPadrao | nivelMin | propriedades | efeitos ItemEfeito |
|---|------|----------|------|---------|----------|--------------|----------|-------------|-------------------|
| 36 | Kit de Aventureiro | Comum | Equipamento de Aventura | 12.00 | 12 | null | 1 | mochila, racao 10 dias, corda 15m, archote 5, isqueiro | nenhum |
| 37 | Kit de Curandeiro | Comum | Equipamento de Aventura | 1.50 | 5 | 10 | 1 | 10 usos de bandagem, 5 usos de antidoto, agulha e linha | nenhum |
| 38 | Kit de Ladroa | Comum | Equipamento de Aventura | 0.90 | 25 | null | 1 | ferramentas de forca, limas, gazuas, espelho, tesoura | nenhum |
| 39 | Lanterna Bullseye | Comum | Equipamento de Aventura | 1.00 | 10 | null | 1 | iluminacao direcional 18m em cone, 6h por frasco de oleo | nenhum |
| 40 | Tomo Arcano | Comum | Equipamento de Aventura | 1.50 | 25 | null | 1 | livro de feiticos para Magos e Feiticeiros, 100 paginas | nenhum |

**Flavour text dos equipamentos de aventura:**

| nome | descricao |
|------|-----------|
| Kit de Aventureiro | O pacote essencial de todo viajante: mochila de couro com alcas reforjadas, racao seca para dez dias, corda de canhamo e archotes. Barato e indispensavel. |
| Kit de Curandeiro | Uma bolsa de couro com compartimentos para bandagens, frascos de antidoto e os instrumentos basicos de sutura. Dez aplicacoes de primeiros socorros em campo. |
| Kit de Ladroa | Um estojo de veludo contendo ferramentas de precisao para abrir fechaduras e desativar mecanismos. Ilegal em alguns reinos, indispensavel em outros. |
| Lanterna Bullseye | Um cilindro de lata polida que concentra a luz da vela em um feixe direcional. Ideal para cavernas e corredores escuros sem revelar a posicao lateral do portador. |
| Tomo Arcano | Um volume encadernado em couro de dragao juvenil com paginas de pergaminho extra-fino. Padrao entre os magos da Torre Cinzenta para registro de feiticos. |

---

## Secao 4 — Equipamentos Iniciais por Classe

Os grupos de escolha sao numerados por classe (nao globalmente). Itens do mesmo numero de grupo sao mutuamente exclusivos — o Jogador escolhe um.

### Guerreiro

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Cota de Malha | true | null | 1 | Armadura de base do guerreiro |
| Escudo de Aco | true | null | 1 | Defesa complementar obrigatoria |
| Espada Longa | false | 1 | 1 | Escolha de arma principal |
| Machado de Batalha | false | 1 | 1 | Alternativa de arma principal |
| Martelo de Guerra | false | 1 | 1 | Alternativa de arma principal |

### Arqueiro

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Couro Batido | true | null | 1 | Armadura leve para mobilidade |
| Arco Longo | true | null | 1 | Arma principal da classe |
| Flecha Comum (20) | true | null | 1 | Municao inicial |
| Adaga | true | null | 1 | Arma secundaria de ultimo recurso |

### Monge

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Gibao de Couro | true | null | 1 | Protecao minima, foco em movimento |
| Cajado de Madeira | false | 1 | 1 | Arma de alcance e foco |
| Adaga | false | 1 | 2 | Alternativa de combate rapido |

### Berserker

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Couro Batido | true | null | 1 | Berserkers preferem mobilidade |
| Machado Grande | false | 1 | 1 | Arma pesada de duas maos |
| Espada Longa | false | 1 | 1 | Alternativa versatil |

### Assassino

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Gibao de Couro | true | null | 1 | Armadura silenciosa |
| Adaga | true | null | 2 | Duas adagas: combate e arremesso |
| Arco Curto | true | null | 1 | Para ataques a distancia furtivos |
| Flecha Comum (20) | true | null | 1 | Municao inicial |

### Fauno (Herdeiro)

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Gibao de Couro | true | null | 1 | Protecao basica |
| Arco Curto | false | 1 | 1 | Opcao a distancia |
| Espada Curta | false | 1 | 1 | Opcao corpo a corpo |

### Mago

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Tomo Arcano | true | null | 1 | Indispensavel para registro de feiticos |
| Cajado de Madeira | false | 1 | 1 | Foco arcano e defesa minima |
| Adaga | false | 1 | 1 | Alternativa para combate emergencial |

### Feiticeiro

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Tomo Arcano | false | 1 | 1 | Registro de feiticos |
| Cajado de Madeira | false | 1 | 1 | Alternativa: foco + defesa |
| Adaga | false | 1 | 1 | Alternativa: combate rapido |

**Nota:** o Feiticeiro nao tem nenhum item obrigatorio — o poder dele e inato, nao depende de objetos.

### Necromante

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Cajado de Madeira | true | null | 1 | Foco para invocacoes |
| Tomo Arcano | true | null | 1 | Indispensavel para rituais |

### Sacerdote

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Camisao de Malha | true | null | 1 | Protecao media adequada a sacerdotes |
| Escudo de Madeira | true | null | 1 | Defesa complementar |
| Martelo de Guerra | false | 1 | 1 | Arma sagrada tradicional |
| Lanca | false | 1 | 1 | Alternativa com alcance |

### Ladrao

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Couro Batido | true | null | 1 | Armadura silenciosa e resistente |
| Adaga | true | null | 2 | Par de adagas para combate e arremesso |
| Kit de Ladroa | true | null | 1 | Ferramenta de trabalho essencial |

### Negociante

| item | obrigatorio | grupoEscolha | qtd | justificativa |
|------|------------|-------------|-----|---------------|
| Gibao de Couro | true | null | 1 | Protecao discreta |
| Espada Curta | false | 1 | 1 | Arma que cabe sob o casaco |
| Adaga | false | 1 | 1 | Alternativa ainda mais discreta |

---

## Secao 5 — Notas de Implementacao

### 5.1 Mapeamento Efeito → Campo da Ficha

Esta tabela mapeia cada tipo de `ItemEfeito` usado no dataset ao campo exato que o `FichaCalculationService` deve modificar ao aplicar bonus de itens equipados.

| TipoItemEfeito | bonusAlvo (nome BonusConfig) | atributoAlvo (nome AtributoConfig) | Campo da Entidade afetado | Metodo de Acesso |
|----------------|------------------------------|------------------------------------|--------------------------|-----------------|
| BONUS_DERIVADO | B.B.A | — | `FichaBonus.itens` | `fichaBonus.setItens(fichaBonus.getItens() + valorFixo)` |
| BONUS_DERIVADO | B.B.M | — | `FichaBonus.itens` | idem |
| BONUS_DERIVADO | Defesa | — | `FichaBonus.itens` | idem |
| BONUS_DERIVADO | Bloqueio | — | `FichaBonus.itens` | idem |
| BONUS_DERIVADO | Reflexo | — | `FichaBonus.itens` | idem (pode ser negativo — Cota de Escamas) |
| BONUS_DERIVADO | Percepcao | — | `FichaBonus.itens` | idem |
| BONUS_ATRIBUTO | — | Forca | `FichaAtributo.itens` | `fichaAtributo.setItens(fichaAtributo.getItens() + valorFixo)` |
| BONUS_VIDA | — | — | `FichaVida.itens` | `fichaVida.setItens(fichaVida.getItens() + valorFixo)` |
| BONUS_ESSENCIA | — | — | `FichaEssencia.itens` | `fichaEssencia.setItens(fichaEssencia.getItens() + valorFixo)` |

**Regra critica de calculo (RN-ITEM-22 + RN-ITEM-23):**
1. No inicio de cada `recalcular()`, zerar todos os campos `itens` de `FichaBonus`, `FichaAtributo`, `FichaVida` e `FichaEssencia`
2. Iterar apenas os `FichaItem` com `equipado = true` e `duracaoAtual != 0`
3. Para cada item equipado, iterar seus `ItemEfeito` e aplicar o `valorFixo` ao campo correspondente
4. Total de cada entidade e recalculado APOS aplicar itens (Total = Base + Nivel + Vantagens + Classe + Itens + Gloria + Outros)

### 5.2 Resolucao de FKs no DefaultGameConfigProviderImpl

O `DefaultGameConfigProviderImpl` deve criar entidades na seguinte ordem estrita, pois cada etapa depende dos IDs da etapa anterior:

```
Etapa 1: criar RaridadeItemConfig x7 → salvar em Map<String, Long> raridadesIds
Etapa 2: criar TipoItemConfig x20   → salvar em Map<String, Long> tiposIds
Etapa 3: criar BonusConfig (ja existente no provider) → garantir que B.B.A, B.B.M, 
         Defesa, Bloqueio, Reflexo, Percepcao existam antes dos ItemEfeito
Etapa 4: criar ItemConfig x40 (referenciando raridadesIds e tiposIds por nome)
         → para cada item magico, criar ItemEfeito com FK para BonusConfig ou AtributoConfig
         → salvar em Map<String, Long> itensIds
Etapa 5: criar ClasseEquipamentoInicial 
         (referenciando classesIds pre-existentes e itensIds por nome)
```

**Resolucao de FKs de ItemEfeito:**

| Item | TipoItemEfeito | alvo | FK a resolver |
|------|----------------|------|---------------|
| Espada Longa +1 | BONUS_DERIVADO | B.B.A | bonusConfigRepository.findByNomeAndJogoId("B.B.A", jogoId) |
| Espada Longa +2 | BONUS_DERIVADO | B.B.A | idem |
| Arco Longo +1 | BONUS_DERIVADO | B.B.A | idem |
| Cajado Arcano +1 | BONUS_DERIVADO | B.B.M | bonusConfigRepository.findByNomeAndJogoId("B.B.M", jogoId) |
| Gibao de Couro | BONUS_DERIVADO | Defesa | bonusConfigRepository.findByNomeAndJogoId("Defesa", jogoId) |
| Couro Batido | BONUS_DERIVADO | Defesa | idem |
| Camisao de Malha | BONUS_DERIVADO | Defesa | idem |
| Cota de Escamas | BONUS_DERIVADO | Defesa | idem |
| Cota de Malha | BONUS_DERIVADO | Defesa | idem |
| Meia Placa | BONUS_DERIVADO | Defesa | idem |
| Meia Placa | BONUS_DERIVADO | Reflexo | bonusConfigRepository.findByNomeAndJogoId("Reflexo", jogoId) |
| Placa Completa | BONUS_DERIVADO | Defesa | bonusConfigRepository.findByNomeAndJogoId("Defesa", jogoId) |
| Escudo de Madeira | BONUS_DERIVADO | Bloqueio | bonusConfigRepository.findByNomeAndJogoId("Bloqueio", jogoId) |
| Escudo de Aco | BONUS_DERIVADO | Bloqueio | idem |
| Escudo Enfeiticado +1 | BONUS_DERIVADO | Bloqueio | idem |
| Escudo Enfeiticado +1 | BONUS_DERIVADO | Defesa | bonusConfigRepository.findByNomeAndJogoId("Defesa", jogoId) |
| Anel da Forca +1 | BONUS_ATRIBUTO | Forca | atributoConfigRepository.findByNomeAndJogoId("Forca", jogoId) |
| Anel de Protecao +1 | BONUS_DERIVADO | Defesa | bonusConfigRepository.findByNomeAndJogoId("Defesa", jogoId) |
| Anel de Protecao +1 | BONUS_DERIVADO | Bloqueio | bonusConfigRepository.findByNomeAndJogoId("Bloqueio", jogoId) |
| Amuleto de Saude | BONUS_VIDA | — | (sem FK — campo direto de FichaVida) |
| Amuleto da Essencia | BONUS_ESSENCIA | — | (sem FK — campo direto de FichaEssencia) |
| Manto de Elvenkind | BONUS_DERIVADO | Reflexo | bonusConfigRepository.findByNomeAndJogoId("Reflexo", jogoId) |
| Manto de Elvenkind | BONUS_DERIVADO | Percepcao | bonusConfigRepository.findByNomeAndJogoId("Percepcao", jogoId) |

**IMPORTANTE:** Os nomes B.B.A, B.B.M, Defesa, Bloqueio, Reflexo, Percepcao devem ser os nomes exatos configurados pelo `DefaultGameConfigProvider` para BonusConfig. Verificar consistencia com `getDefaultBonus()` no provider existente antes de implementar T6.

### 5.3 Requisitos (ItemRequisito) dos Itens no Dataset

Apenas dois itens do dataset tem requisitos formais (via `ItemRequisito`):

| item | TipoRequisito | alvo | valorMinimo |
|------|--------------|------|-------------|
| Cota de Malha | ATRIBUTO | FOR | 10 |
| Placa Completa | ATRIBUTO | FOR | 12 |

Todos os outros requisitos (nivel minimo para armas pesadas, "municao para arcos", etc.) sao apenas descritivos no campo `propriedades` — nao geram `ItemRequisito` no MVP.

### 5.4 Grupos de Escolha — Contagem por Classe

| Classe | Total grupos | Itens por grupo |
|--------|-------------|----------------|
| Guerreiro | 1 (grupo 1) | 3 itens (Espada Longa, Machado de Batalha, Martelo de Guerra) |
| Arqueiro | 0 | todos obrigatorios |
| Monge | 1 (grupo 1) | 2 itens (Cajado, Adaga x2) |
| Berserker | 1 (grupo 1) | 2 itens (Machado Grande, Espada Longa) |
| Assassino | 0 | todos obrigatorios |
| Fauno (Herdeiro) | 1 (grupo 1) | 2 itens (Arco Curto, Espada Curta) |
| Mago | 1 (grupo 1) | 2 itens (Cajado de Madeira, Adaga) |
| Feiticeiro | 1 (grupo 1) | 3 itens (Tomo Arcano, Cajado de Madeira, Adaga) |
| Necromante | 0 | todos obrigatorios |
| Sacerdote | 1 (grupo 1) | 2 itens (Martelo de Guerra, Lanca) |
| Ladrao | 0 | todos obrigatorios |
| Negociante | 1 (grupo 1) | 2 itens (Espada Curta, Adaga) |

### 5.5 Contagem de Entidades Criadas por Jogo

Ao inicializar um novo Jogo com dataset completo de itens:

| Entidade | Quantidade |
|----------|-----------|
| RaridadeItemConfig | 7 |
| TipoItemConfig | 20 |
| ItemConfig | 40 |
| ItemEfeito | 24 (nos 15 itens magicos do dataset) |
| ItemRequisito | 2 (Cota de Malha, Placa Completa) |
| ClasseEquipamentoInicial | 40 registros (distribuidos entre 12 classes) |

### 5.6 Regras de Idempotencia

Conforme RN-T6-01: o dataset e criado apenas uma vez por Jogo. A verificacao de idempotencia deve ocorrer via flag `defaultsCreated` em `Jogo` ou via `GameConfigInitializerService.isAlreadyInitialized(jogoId)`. Nao usar `ON CONFLICT IGNORE` — a excecao de duplicata deve ser tratada explicitamente para evidenciar bug de inicializacao dupla.

---

## Secao 6 — Pontos em Aberto para o PO

| ID | Pergunta | Impacto |
|----|----------|---------|
| PA-016-DS-01 | Pocoes devem gerar `ItemEfeito` com `BONUS_VIDA` no MVP ou continuar apenas informativas? Se sim, o endpoint de consumo de pocao precisa ser planejado como parte do MVP. | Afeta itens 31, 32, 33 e requer endpoint adicional |
| PA-016-DS-02 | O campo `propriedades` de "Cota de Escamas" menciona "desvantagem em Furtividade". Isso deve gerar um `ItemEfeito` do tipo `BONUS_APTIDAO` com valor negativo em Furtividade? Ou e apenas descritivo? | Afeta item 19; se sim, precisa de `aptidaoAlvo` resolvido por nome |
| PA-016-DS-03 | Itens de aventura (Kit de Curandeiro com durabilidade 10 usos) devem ter o decremento de durabilidade ligado ao uso do kit (ex: endpoint de usar item de aventura) ou e apenas manual pelo Mestre? | Afeta Kit de Curandeiro (item 37) e o fluxo de uso de ferramentas |
| PA-016-DS-04 | O Manto de Elvenkind (item 30) e do tipo Amuleto. Considera-se adequado ou deve ser um tipo proprio "Capa"? A spec tem "CAPA" como subcategoria possivel mas o dataset de tipos nao inclui esse tipo. | Afeta tipo de TipoItemConfig a ser criado |

---

*Produzido por: Business Analyst/PO | 2026-04-04*
*Baseado em: spec.md (Spec 016), P2-T6-default-dataset.md, REGRAS-NEGOCIO.md, glossario/02-configuracoes-jogo.md, glossario/03-termos-dominio.md, glossario/04-siglas-formulas.md*
