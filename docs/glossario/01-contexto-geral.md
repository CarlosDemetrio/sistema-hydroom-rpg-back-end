# Contexto Geral — Klayrah RPG

> 📖 Parte 1 do Glossário. Voltar ao [índice](../GLOSSARIO.md).

---

## O QUE É O KLAYRAH RPG

Klayrah é um **sistema de RPG de mesa** (Role-Playing Game) com regras próprias. O projeto **Ficha Controlador** é a aplicação web que digitaliza a experiência de preencher e gerenciar fichas de personagem desse sistema.

No RPG de mesa, cada jogador tem uma **ficha de personagem em papel** com dezenas de campos: atributos, habilidades, vida, recursos mágicos, equipamentos, etc. O Ficha Controlador substitui esse papel por uma aplicação web com cálculos automáticos, validações e controle de acesso.

### Quem usa o sistema

- **Mestre** — o narrador e árbitro do jogo. Ele cria o "mundo" (o Jogo/Campanha), define todas as regras (configurações) e gerencia os participantes. Pense nele como o administrador de uma sessão de RPG.
- **Jogador** — cada participante que cria e controla seu personagem dentro do jogo do Mestre. Preenche e edita sua própria ficha.

### Princípio fundamental: tudo configurável

O grande diferencial do sistema é que **nada é fixo** (hardcoded). O Mestre pode alterar atributos, classes, raças, fórmulas de cálculo, aptidões — absolutamente tudo. Isso permite que o sistema sirva para variações do Klayrah ou até para outros RPGs caseiros.

Quando o Mestre cria um jogo, o sistema oferece um **template "Klayrah Padrão"** com todos os valores default (7 atributos, 24 aptidões, 12 classes, etc.), mas ele pode customizar qualquer coisa depois.

---

## CONCEITOS ESTRUTURAIS

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

*Última atualização: Março 2026*
