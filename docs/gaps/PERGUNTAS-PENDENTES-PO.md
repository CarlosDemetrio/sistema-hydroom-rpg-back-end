# Perguntas Pendentes — Decisões do PO

> Criado em: 2026-04-03 | Responda diretamente abaixo de cada pergunta.

---

## Bloco 1 — Essência (GAP-07)

**Q1:** `essenciaGasta` persiste entre sessões ou reseta ao "descansar"?

**R:** reseta ao descansar e quando o mestre quiser, pois pode haver saltos de tempo e situacoes do tipo

**Q2:** Quem controla o reset da essência — o Mestre manualmente, ou é automático no início de cada sessão?

**R:** o mestre manualmente, pois pode haver situações onde o reset não é desejado (ex: descanso curto, recuperação parcial, eventos narrativos)

---

## Bloco 2 — Prospecção (GAP-08)

**Q3:** Dois endpoints separados ou único?
- Opção A: `POST /fichas/{id}/prospeccao/conceder` (Mestre adiciona) + `POST /fichas/{id}/prospeccao/usar` (Jogador usa)
- Opção B: Endpoint único genérico

**R:** pode ser os 2, mas no geral que controla isso é o mestre

---

## Bloco 3 — Deleção de Ficha (INCONS-02)

**Q4:** `DELETE /fichas/{id}` — Jogador pode excluir a própria ficha, ou apenas o Mestre pode excluir fichas?

**R:** não pode, todas fichas ficam no jogo, pode ocorrer da ficha ser morta ou abandonada, mas não deletada, para preservar histórico e evitar confusão (ex: jogador deletar ficha por engano e perder progresso)

---

## Bloco 4 — Role ADMIN (P-03 / Spec 010)

**Q5:** ADMIN faz bypass total de `canAccessJogo()`?
- Opção A: SIM — ADMIN acessa qualquer jogo sem ser participante (superuser completo)
- Opção B: NÃO — ADMIN precisa ser adicionado como participante em cada jogo que quer acessar

**R:** vamos deixar a parte de admin só com controle de usuários no momento. vamos fechar nosso MVP assim, mais pra frente incluimos nvoas funcionalidades para ele

---

## Bloco 5 — Sub-recursos de Classes (Spec 008)

**Q6:** Existe endpoint `PUT` para editar `valorPorNivel` de um `ClasseBonus` já existente? Se não, o Mestre precisa remover e re-adicionar para corrigir um valor errado.

**R:**  mas isso deve acontecer nas configuracoes, na ficha pode ocorrer sim de o mestre retirar ou dar pontos por motivos especifico, mas ele não seria conectado a nenhuma configuracao, e sim a um "outros"

**Q7:** Ao remover um `ClasseBonus` de uma classe que já tem fichas ativas, deve haver uma confirmação explícita para o Mestre ("isso vai alterar os cálculos das fichas existentes")?

**R:**  sim, deve haver um aviso claro para o mestre, e ele deve confirmar a ação, pois isso pode impactar as fichas ativas, e ele precisa estar ciente disso antes de prosseguir com a remoção do bônus da classe.

**Q8:** A tabela de listagem de Classes deve ter uma coluna de preview dos bônus (ex: "B.B.A +1/nív, B.Apt +0/nív")?

**R:** sim, ao listar para o usuário deve ter uma coluna bem simplificada com os bonus, as habilidades não precisam aparecer, e devem aparecer apenas se ela for escolhida, para evitar má fé dos usuáios

**Q9:** O que acontece ao tentar deletar uma `ClassePersonagem` que está referenciada em fichas ativas? (403, 409 ou o backend permite e remove a referência?)

**R:** não permite, o sistema deve impedir a deleção de uma classe que está sendo usada por fichas ativas, para evitar inconsistências e erros no cálculo das fichas. O backend deve retornar um erro 409 Conflict, indicando que a classe não pode ser deletada porque está em uso. O Mestre precisaria primeiro remover ou alterar as fichas que estão usando essa classe antes de poder deletá-la.

---

## Bloco 6 — Renascimento / Level Up (Spec 012)

**Q10:** Comportamento exato do renascimento (Nível 31+)

**R:** ~~Fora do MVP. Ignorar T12 e T13 da Spec 012.~~

**Q11:** Como calcular `pontosAtributoDisponiveis`?

**R:** Soma de TODAS as fontes configuradas: pontos por NivelConfig, pontos por Classe, pontos por Raça, pontos por Vantagem — menos os pontos já gastos. É a soma acumulada de todas as configurações.

**Q12:** `PontosVantagemConfig` — rota própria ou integrado em Níveis?

**R:** Pontos de vantagem são dados por nível, por up de raça ou por up de classe. Múltiplas fontes, similar aos pontos de atributo.

---

## Bloco 7 — Anotações / Pastas (Spec 011)

**Q13 (PA-008):** Ao deletar uma pasta que tem sub-pastas, as sub-pastas:
- Opção A: ficam na raiz (comportamento padrão implementado)
- Opção B: são deletadas em cascata junto com a pasta pai

**R:** Opção A: ficam na raiz. Deletar uma pasta não deve deletar as anotações ou sub-pastas dentro dela, para evitar perda acidental de dados. As sub-pastas e anotações devem ser movidas para a raiz ou para uma pasta pai alternativa, garantindo que o conteúdo do usuário seja preservado mesmo que a organização seja alterada.

---

## Bloco 8 — Cálculos da Ficha (auditoria BA — 2026-04-03)

> Auditoria completa em `docs/analises/INTEGRACAO-CONFIG-FICHA.md`
> **9 gaps críticos encontrados** — 3 deles são bugs ativos independentes de Spec 007.

**Q14 (PA-PONTOS-01):** `ClassePersonagem` e `Raca` têm campos próprios de pontos de atributo/aptidão disponíveis (além de `ClasseBonus`/`RacaBonusAtributo`)? Ou todos os bônus de pontos vêm exclusivamente de `ClasseBonus`, `RacaBonusAtributo`, `NivelConfig` e `VantagemEfeito`?

**R:** todos podem liberar pontos. e ele pode liberar pontos extras quando usuário chegar a um certo nivel, isto deveria ser configuravel pelo mestre

**Q15 (PA-APTIDAO-01):** Como calcular `pontosAptidaoGastos`? Cada `FichaAptidao` tem campo `base` que pode ter sido definido na criação da ficha (valor inicial fixo) OU distribuído pelo jogador via level up. Como distinguir o que foi gasto em level up do que é valor de criação?

**R:** ele é distribuido pelo jogador, o mestre configura quantos pontos de aptidao o jogador tem para distribuir, e o jogador distribui como quiser, ele pode colocar tudo em uma aptidao ou distribuir entre varias, o sistema não precisa distinguir o que é gasto em level up do que é valor de criação, pois ambos são parte do total de pontos disponíveis para a ficha.

**Q16 (PA-FORMULA-01):** `FichaCalculationService` hoje identifica VIG (Vigor) e SAB (Sabedoria) pelo nome da abreviação — hardcoded. Se o Mestre criar um jogo com atributos diferentes (ex: sem VIG), o cálculo quebra. Como resolver: VIG/SAB devem ser marcados com um "papel" (role) no `AtributoConfig` (ex: `papel = VIGOR`, `papel = SABEDORIA`), ou aceita-se que o sistema exige essas abreviações?

**R:** Sistema exige estas abreviacoes, vamos focar em fechar o MVP

---

## Bloco 9 — Modo Sessão / Tempo Real (auditoria UX — 2026-04-03)

> Auditoria completa em `docs/analises/UX-FICHAS-AUDITORIA.md`

**Q17 (PA-UX-01 — CRÍTICO, bloqueia toda implementação do Modo Sessão):**
Quando o Mestre concede XP, reseta vida ou altera stats durante uma sessão ativa, como o Jogador vê a mudança?
- Opção A: **Polling** — frontend consulta o backend a cada N segundos automaticamente
- Opção B: **SSE/WebSocket** — backend notifica o frontend em tempo real (mais complexo)
- Opção C: **Manual** — Jogador precisa recarregar a página para ver atualizações

**R:** para fechar o MVP vamos de opcao A, e futuramente iremos implementar a B

---

## Bloco 10 — DefaultGameConfigProvider (auditoria 2026-04-04)

> Auditoria completa em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`

**Q18 (Q-DC-01):** Quais `BonusConfig` padrão e suas fórmulas?
Sugestões: B.B.A = `nivel/5`, B.B.M = `nivel/5`, Defesa = `VIG/5`, Esquiva = `AGI/5`, Iniciativa = `INTU/5`. Correto ou diferentes?

**R:** pode seguir os mesmos calculos, mas o mestre deve ter a liberdade de configurar isso, para que ele possa criar jogos com regras diferentes, então o sistema deve ser flexível para permitir que o mestre defina as fórmulas de bônus como desejar.

**Q19 (Q-DC-02):** Tabela de marcos de `PontosVantagemConfig` por nível. Quantos pontos de vantagem o jogador ganha e em quais níveis?

**R:** pode seguir com 6 pontos de vantagem no nivel 1, e 3 vantagens a cada 5 niveis, e nos niveis 10 e 20, ganham 10 pontos, e no nivel 30 ganham 15 pontos, mas o mestre deve ter a liberdade de configurar isso, para que ele possa criar jogos com regras diferentes, então o sistema deve ser flexível para permitir que o mestre defina os pontos de vantagem por nível como desejar.

**Q20 (Q-DC-03):** `ClasseBonus` por classe — qual bônus cada classe dá por nível?
Ex: Guerreiro → +1 B.B.A/nível; Mago → +1 B.B.M/nível. Como fica a tabela completa para as 12 classes?

**R:** as classes não dão bonus diretamente em si, dão pontos de atributos e algumas vantagens pré definidas

**Q21 (Q-DC-04):** Nível 35 tem XP = 595.000, igual ao nível 34. Foi intencional (nível máximo não exige mais XP) ou typo?

**R:** foi intencional e considere que este é o teto mesmo

**Q22 (Q-DC-05):** `RacaClassePermitida` — o sistema Klayrah restringe quais classes cada raça pode escolher? (ex: Elfo não pode ser Berserker?)

**R:** Deve ter esta possibilidade, mas neste default game provider, todas as raças podem escolher todas as classes, para dar mais liberdade aos mestres, mas o sistema deve ser flexível para permitir que o mestre defina restrições de raça/classe como desejar.

**OBS** - Revise o glossário para verificar se o gameDefault está coerente com as definições lá (ex: nomes de atributos, classes, raças, vantagens)..

---

## Bloco 11 — Coerência Glossário × DefaultGameConfigProvider (auditoria 2026-04-04)

> Auditoria completa em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`, Seção 8.
> Várias divergências encontradas entre o glossário e o código. Responder antes de implementar o provider definitivo.

**Q-DC-06 (DIV-02 — ALTA):** O sistema de Índole no Klayrah é:
- Opção A: 3 valores simples — Bom, Mau, Neutro (glossário atual)
- Opção B: 9 alinhamentos D&D — Ordeiro Bondoso, Neutro Bondoso, Caótico Bondoso, Ordeiro Neutro, Neutro, Caótico Neutro, Ordeiro Maligno, Neutro Maligno, Caótico Maligno (código atual)

Se for Opção B, o glossário deve ser atualizado. Se for Opção A, o provider deve ser simplificado.

**R:** opcao A

---

**Q-DC-07 (DIV-03 — ALTA):** O que é "Presença" no sistema Klayrah?
- Opção A: Postura ética/comportamental — valores: Bom, Leal, Caótico, Neutro (glossário atual, segunda dimensão do alinhamento)
- Opção B: Escala de intensidade/carisma — valores: Insignificante, Fraco, Normal, Notável, Impressionante, Dominante (código atual)

**R:** A

---

**Q-DC-08 (DIV-04 — BAIXA):** O template padrão de Gênero deve ter:
- Opção A: 3 valores — Masculino, Feminino, Outro (glossário)
- Opção B: 4 valores — Masculino, Feminino, Não-Binário, Prefiro não informar (código)

**R:** A

---

**Q-DC-09 (DIV-06 — CRÍTICA, responder antes de Spec 006):** A porcentagem de vida da Cabeça é:
- Opção A: **25%** da vida total (código atual — `0.25`)
- Opção B: **75%** da vida total (glossário — `03-termos-dominio.md`, linha 44: "Cabeça = 75% da vida total")

Esta divergência impacta diretamente como golpes na cabeça funcionam. Se 75%, a Cabeça é extremamente vulnerável e golpes são muito mais letais.

**R:** B. claro que um golpe na cabeça é mais letal, então faz sentido que ela tenha 75% da vida total, tornando os golpes nessa região mais perigosos e estratégicos durante o combate.

---

**Q-DC-10 (DIV-07 — ALTA):** As vantagens padrão do sistema Klayrah devem ser:
- Opção A: As 11 vantagens genéricas atuais (Fortitude, Força Aprimorada, Golpe Crítico, etc.)
- Opção B: As vantagens canônicas do glossário — TCO (Treinamento em Combate Ofensivo), TCD (Treinamento em Combate Defensivo), TCE (Treinamento em Combate Evasivo), TM (Treinamento Mágico), CFM (Capacidade de Força Máxima), etc.
- Opção C: Ambas — manter as genéricas e adicionar as canônicas

Se Opção B ou C, o provider precisará ser reescrito com siglas corretas e categorias do glossário.

**R:**  C

---

**Q-DC-11 (ALTA):** As fórmulas de B.B.A e B.B.M são:
- Opção A: `(FOR + AGI) / 3` para B.B.A e `(SAB + INT) / 3` para B.B.M (glossário — `03-termos-dominio.md`, linhas 25-26)
- Opção B: `nivel / 5` para ambos (proposta da auditoria técnica)

Note: Regra de negócio confirmada — bônus B.B.A e B.B.M são sempre **calculados** a partir de atributos ou nível (nunca hardcoded). VantagemEfeito pode adicionar um delta por cima do valor calculado.

**R:**  A

---

## Bloco 12 — Sistema de Itens/Equipamentos (Spec 016 — MVP confirmado 2026-04-04)

> Equipamentos são parte essencial do MVP (decisão PO 2026-04-04). Precisam ser especificados antes de criar a Spec 016.


**Requisitos de equipamentos**: preciso que os itens tenham: nome, tipo, raridade,classificacao, peso, valor(custo para comprar, pode não ter preco), durabilidade,
propriedades,nivel,requisitos(pode ser nivel, atributo, bonus, aptidao ou vantagem), descricao, efeitos no dado, bonus (ex: bonus direto em atributo, bonus direto em bonusConfig, bonus direto em vida/essencia, bonus direto em aptidao, formula customizada), e o mestre pode configurar isso como quiser, para que ele possa criar um sistema de equipamentos que se encaixe no estilo do jogo que ele quer criar.
e vamos fazer o seguinte. nas configuracoes, vamos criar uma classificacao dos itens, comum, incomum, raro, muito raro, epico, lendário, Unico. nestas classificacoes, vamos criar um bonus minimo geral, que seria a media para cada
         um dos itens. e além desta classificacao, teria o tipo Armas, Armaduras e etc. mas isso poderia ser melhor elaborado por um BA, para que a gente consiga criar um sistema de itens mais robusto, com mais categorias e subcategorias, e o mestre poderia criar os itens a partir dessas classificações, ou criar itens personalizados do zero, dando a ele total liberdade para criar o sistema de equipamentos que desejar para o seu jogo.

quero que os BAs criem um dataset já pré fabricado também. os bonus por raridade, classificacao e etc, irei deixar inicialmente por conta dos BAs, eles têm nocao geral do sistema, e conseguem criar configuracoes boas.0


### Pontos do Dataset D&D 5e SRD (PA-016-DS — novos, 2026-04-04)

> Originados em `docs/specs/016-sistema-itens/dataset/dataset-itens-srd.md`, Secao 6.

**PA-016-DS-01:** Pocoes devem gerar `ItemEfeito` com `BONUS_VIDA` no MVP ou continuar apenas informativas? Se sim, o endpoint de consumo de pocao precisa ser planejado como parte do MVP.

**R:** _(pendente)_

---

**PA-016-DS-02:** O campo `propriedades` de "Cota de Escamas" menciona "desvantagem em Furtividade". Isso deve gerar um `ItemEfeito` do tipo `BONUS_APTIDAO` com valor negativo em Furtividade? Ou e apenas descritivo?

**R:** _(pendente)_

---

**PA-016-DS-03:** Itens de aventura (Kit de Curandeiro com durabilidade 10 usos) devem ter o decremento de durabilidade ligado ao uso do kit (ex: endpoint de usar item de aventura) ou e apenas manual pelo Mestre?

**R:** _(pendente)_

---

**PA-016-DS-04:** O Manto de Elvenkind (item 30) e do tipo Amuleto. Considera-se adequado ou deve ser um tipo proprio "Capa"? A spec tem "CAPA" como subcategoria possivel mas o dataset de tipos nao inclui esse tipo.

**R:** _(pendente)_

---

**Q-ITEM-01:** Equipamentos são **configurados pelo Mestre como catálogo** (ItemConfig — igual às outras 13 entidades de configuração) ou o Mestre cria itens **livremente direto na ficha** do personagem, sem catálogo prévio?

**R:**  vamos fazer o seguinte. nas configuracoes, vamos criar uma classificacao dos itens, comum, incomum, raro, muito raro, epico, lendário, Unico. nestas classificacoes, vamos criar um bonus minimo geral, que seria a media para cada
         um dos itens. e além desta classificacao, teria o tipo Armas, Armaduras e etc. mas isso poderia ser melhor elaborado por um BA, para que a gente consiga criar um sistema de itens mais robusto, com mais categorias e subcategorias, e o mestre poderia criar os itens a partir dessas classificações, ou criar itens personalizados do zero, dando a ele total liberdade para criar o sistema de equipamentos que desejar para o seu jogo.

---

**Q-ITEM-02:** Quais bônus um item pode dar? (marque todos que se aplicam)
- Bônus direto em atributo (ex: +2 FOR)
- Bônus direto em BonusConfig (B.B.A, B.B.M, Defesa, etc.)
- Bônus direto em Vida / Essência
- Bônus direto em aptidão
- Fórmula customizada (via FormulaEvaluatorService)

**R:** pode dar bonus direto, atributo direto, bonus de vida, bonus de aptidao. ele pode dar basicamente tudo pois é bem aberto, e item pode ser qualquer coisa

---

**Q-ITEM-03:** Itens têm **slots por membro do corpo** (Cabeça, Tronco, Braço, Mão, Perna, Pé, Sangue) — ou apenas uma lista geral de "equipado / inventário"?

**R:** vamos trabalhar apenas com uma lista geral de equipado/inventário, para simplificar o MVP. O sistema de slots por membro do corpo pode ser uma evolução futura, mas para o MVP, um sistema mais simples de equipado vs inventário é suficiente para permitir que os mestres criem e gerenciem itens sem a complexidade adicional de slots específicos.

---

**Q-ITEM-04:** Item precisa ser **equipado** para dar bônus, ou qualquer item na ficha (inventário) já aplica?

**R:** PRECISA estar equipado para dar bonus.

---

**Q-ITEM-05:** Itens têm **peso** (que deduz da capacidade de carga — Ímpeto de Força)?

**R:** sim, todos itens tem peso e impactam no impeto, mas vamos apenas adicionar o peso e esta parte adicionamos mais tarde

---

**Q-ITEM-06:** Itens têm **durabilidade** (desgaste e quebra)?

**R:** com toda certeza, itens tem durabilidade, e o mestre pode configurar a durabilidade de cada item, e quando a durabilidade chegar a zero, o item quebra e deixa de dar bônus, ou pode ter regras personalizadas para isso, como ficar enferrujado, ou perder parte do bônus, etc. mas a ideia é que os itens tenham durabilidade para adicionar mais uma camada de estratégia e gerenciamento para os jogadores.

---

**Q-ITEM-07:** Quem pode adicionar/remover itens da ficha?
- Opção A: Apenas o Mestre
- Opção B: Mestre adiciona, Jogador pode usar/equipar
- Opção C: Ambos podem adicionar e remover

**R:**  vamos deixar o jogador adicionar no maximo itens comuns, itens com classificacao acima de incomum, apenas o mestre pode adicionar. isso para facilitar para o mestre durante a sessao,e facilitar, pois muitas vezes o jogador pode pegar por exemplo, uma tocha. mas a parte de bonus efeitos e etc. apenas o mestre pode

---

**Q-ITEM-08 (Spec 015 PA-015-01/02/03):** Valores default de pontos configuráveis por Classe e Raça:
- Guerreiro nível 1 → quantos pontos de atributo extras?
- Elfo nível 1 → quantos pontos extras?
- Alguma classe/raça tem vantagem pré-definida automática? (ex: Mago nível 1 ganha TM automaticamente?)

**R:** como os BAs tem uma visão geral, vamos deixar para eles definirem isso. mas a ideia é que cada classe e raça tenha uma quantidade de pontos extras para distribuir e algumas fixas, apenas os humanos podem distribuir avontade tudo, e algumas vantagens pré-definidas, para dar mais sabor e diferenciação entre as classes e raças, mas o sistema deve ser flexível para permitir que o mestre configure isso como desejar.
