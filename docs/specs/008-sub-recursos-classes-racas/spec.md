# Spec 008 — Sub-recursos de Classes e Raças no Frontend

> **Status:** PLANEJADO | Criado em: 2026-04-02
> **Branch alvo:** `feature/008-sub-recursos-classes-racas`
> **Prerequisito:** Spec 004 (configuracoes base) concluida. Backend 100% implementado.
> **Analise de dominio:** `docs/analises/BA-CLASSES-RACAS.md` (nao duplicar)

---

## 1. Visao Geral do Negocio

**Problema:** O frontend possui CRUD basico para `ClassePersonagem` e `Raca`, mas os sub-recursos
(`ClasseBonus`, `ClasseAptidaoBonus`, `RacaBonusAtributo`, `RacaClassePermitida`) nao estao
conectados ao backend — as abas existem nos componentes, mas as chamadas de API usam tipagem
`unknown` e varios campos criticos estao ausentes na interface (ex: `valorPorNivel` em `ClasseBonus`).

**Valor entregue:** O Mestre passa a conseguir configurar completamente uma classe (bônus por nivel
e bônus fixos em aptidoes) e uma raca (penalidades/bônus de atributo e restricoes de classe)
diretamente pelo frontend, sem necessidade de acesso direto ao banco ou chamadas manuais de API.

**Objetivo de negocio:** Desbloquear a criacao de fichas (Spec 006) com dados de classe e raca
corretamente configurados, garantindo que o motor de calculos (Spec 007) tenha insumo valido.

---

## 2. Atores Envolvidos

| Ator | Role | Contexto |
|------|------|----------|
| Mestre | `ROLE_MESTRE` | Configura classes e racas antes de criar fichas para o jogo |
| Backend | — | 100% implementado; todos os endpoints REST existem e funcionam |
| Jogador | `ROLE_JOGADOR` | Nao configura sub-recursos; apenas lê ao criar ficha |

---

## 3. Requisitos Funcionais

### 3.1 ClassePersonagem — Aba "Bônus"

- **RF-01:** O Mestre deve conseguir visualizar a lista de `ClasseBonus` de uma classe (BonusConfig + valorPorNivel).
- **RF-02:** O Mestre deve conseguir adicionar um `ClasseBonus`, selecionando BonusConfig e informando `valorPorNivel` (decimal, ex: 0.5).
- **RF-03:** O Mestre deve conseguir remover um `ClasseBonus` existente.
- **RF-04:** O dropdown de BonusConfig disponiveis deve ocultar os bônus ja vinculados a classe.
- **RF-05:** O campo `valorPorNivel` deve aceitar valores decimais com passo 0.01 (ex: 0.50).
- **RF-06:** Deve exibir preview textual: "No nivel N: +X em [BonusNome]" conforme o usuario digita.

### 3.2 ClassePersonagem — Aba "Aptidoes c/ Bônus"

- **RF-07:** O Mestre deve conseguir visualizar a lista de `ClasseAptidaoBonus` de uma classe (AptidaoConfig + bonus inteiro).
- **RF-08:** O Mestre deve conseguir adicionar um `ClasseAptidaoBonus`, selecionando AptidaoConfig e informando `bonus` (inteiro >= 0).
- **RF-09:** O Mestre deve conseguir remover um `ClasseAptidaoBonus` existente.
- **RF-10:** O dropdown de AptidaoConfig disponiveis deve ocultar aptidoes ja vinculadas a classe.

### 3.3 Raca — Aba "Bônus em Atributos"

- **RF-11:** O Mestre deve conseguir visualizar a lista de `RacaBonusAtributo` de uma raca (AtributoConfig + bonus, podendo ser negativo).
- **RF-12:** O Mestre deve conseguir adicionar um `RacaBonusAtributo`, selecionando AtributoConfig e informando `bonus` (inteiro, pode ser negativo — penalidade racial).
- **RF-13:** O Mestre deve conseguir remover um `RacaBonusAtributo` existente.
- **RF-14:** O dropdown de AtributoConfig disponiveis deve ocultar atributos ja vinculados a raca.
- **RF-15:** Valores negativos de bonus devem ser exibidos em vermelho com indicador visual de penalidade.

### 3.4 Raca — Aba "Classes Permitidas"

- **RF-16:** O Mestre deve conseguir visualizar a lista de `RacaClassePermitida` de uma raca.
- **RF-17:** O Mestre deve conseguir adicionar uma `RacaClassePermitida`, selecionando ClassePersonagem.
- **RF-18:** O Mestre deve conseguir remover uma `RacaClassePermitida` existente.
- **RF-19:** O dropdown de ClassePersonagem disponiveis deve ocultar classes ja na lista.
- **RF-20:** Quando a lista estiver vazia, exibir mensagem informativa: "Todas as classes sao permitidas para esta raca".
- **RF-21:** Na tabela principal de racas, exibir badge com indicador de restricao de classe (vazio = "Sem restricoes", com itens = "X classes").

### 3.5 Alinhamento de Tipos e API Service

- **RF-22:** A interface `ClasseBonusConfig` deve incluir o campo `valorPorNivel: number`.
- **RF-23:** Os metodos de API service que retornam `unknown` devem ser tipados com as interfaces corretas.
- **RF-24:** O metodo `addClasseBonus` deve aceitar `{ bonusConfigId: number; valorPorNivel: number }`.
- **RF-25:** O metodo `addClasseAptidaoBonus` deve aceitar `{ aptidaoConfigId: number; bonus: number }`.

---

## 4. Requisitos Nao Funcionais

- **RNF-01 Performance:** Ao abrir o drawer de uma classe/raca, os sub-recursos devem ser carregados via `GET /{id}` (endpoint unico que ja retorna sub-recursos embarcados) — sem chamadas extras separadas por sub-recurso.
- **RNF-02 Feedback de erro:** Conflito (HTTP 409 — mesmo bonus/atributo duas vezes) deve exibir toast de erro com mensagem clara, sem crash.
- **RNF-03 Estado vazio:** Cada aba deve ter estado vazio com mensagem e icone PrimeNG adequados.
- **RNF-04 Consistencia visual:** Seguir o padrao das abas ja implementadas nos componentes (surface-100, p-button text danger para remover, p-select para dropdown).
- **RNF-05 Acessibilidade:** Valores negativos nao podem ser comunicados apenas por cor; deve haver indicador textual (ex: sinal de menos ou label "penalidade").
- **RNF-06 Nao regressao:** Zero quebra nos 271 testes Vitest existentes.

---

## 5. Epico e User Stories

**EPICO 008 — Configuracao completa de Classes e Racas via Frontend**

> Como Mestre, quero configurar completamente as classes e racas do meu jogo pelo frontend,
> para que as fichas dos jogadores sejam criadas com bônus e restricoes corretos sem necessidade
> de acesso direto ao banco.

---

**US-008-01: Visualizar e gerenciar bônus por nivel de uma classe**

Como Mestre,
Quero adicionar, visualizar e remover bônus por nivel (ClasseBonus) em uma classe,
Para definir quanto cada nivel concede de bônus em cada BonusConfig (ex: +1 B.B.A por nivel).

**Criterios de Aceite:**

Cenario 1: Listar bônus existentes
  Dado que estou no drawer de edicao de uma classe
  Quando acesso a aba "Bônus"
  Entao vejo a lista de bônus vinculados com nome do BonusConfig e valorPorNivel
  E bônus com valorPorNivel fracionario sao exibidos corretamente (ex: 0.50)

Cenario 2: Adicionar bônus por nivel
  Dado que estou na aba "Bônus" de uma classe
  Quando seleciono um BonusConfig no dropdown e informo valorPorNivel = 1.0
  E clico em "Adicionar"
  Entao o bônus e adicionado via POST /classes/{id}/bonus
  E a lista e atualizada imediatamente
  E o BonusConfig adicionado some do dropdown de opcoes disponiveis

Cenario 3: Adicionar bônus fracionario
  Dado que estou na aba "Bônus" de uma classe
  Quando informo valorPorNivel = 0.5
  Entao o campo aceita o valor decimal sem erro de validacao

Cenario 4: Remover bônus
  Dado que existe um ClasseBonus na lista
  Quando clico no botao de remover
  Entao o bônus e removido via DELETE /classes/{id}/bonus/{bonusId}
  E a lista e atualizada

Cenario 5: Conflito — bônus duplicado
  Dado que um BonusConfig ja esta vinculado a classe
  Quando o usuario tenta adicionar o mesmo BonusConfig novamente (via API direta ou bug)
  Entao o backend retorna HTTP 409
  E o frontend exibe toast de erro "Este bônus ja esta configurado para esta classe"

Cenario 6: Lista vazia
  Dado que a classe nao tem ClasseBonus configurados
  Entao a aba exibe estado vazio com mensagem "Nenhum bônus configurado"

---

**US-008-02: Gerenciar bônus fixos de aptidao de uma classe**

Como Mestre,
Quero adicionar, visualizar e remover bônus fixos de aptidao (ClasseAptidaoBonus) em uma classe,
Para definir quais aptidoes a classe tem bônus permanente (ex: Ladino +2 Furtividade).

**Criterios de Aceite:**

Cenario 1: Adicionar bônus de aptidao
  Dado que estou na aba "Aptidoes c/ Bônus" de uma classe
  Quando seleciono uma AptidaoConfig no dropdown e informo bonus = 2
  E clico em "Adicionar"
  Entao o bônus e adicionado via POST /classes/{id}/aptidao-bonus
  E a lista e atualizada

Cenario 2: Campo bonus nao aceita valores negativos
  Dado que estou no formulario de adicionar ClasseAptidaoBonus
  Quando informo bonus = -1
  Entao o campo exibe erro de validacao "Valor minimo e 0"
  E o botao Adicionar permanece desabilitado

Cenario 3: Remover aptidao com bônus
  Dado que existe um ClasseAptidaoBonus na lista
  Quando clico em remover
  Entao o item e removido via DELETE /classes/{id}/aptidao-bonus/{id}

Cenario 4: Aptidoes ja vinculadas nao aparecem no dropdown
  Dado que "Furtividade" ja foi adicionada como ClasseAptidaoBonus
  Quando abro o dropdown de aptidoes para adicionar outro
  Entao "Furtividade" nao aparece nas opcoes

---

**US-008-03: Gerenciar bônus e penalidades de atributo de uma raca**

Como Mestre,
Quero adicionar, visualizar e remover bônus (positivos ou negativos) de atributo por raca,
Para modelar vantagens e penalidades raciais (ex: Elfo +2 Agilidade, -1 Vigor).

**Criterios de Aceite:**

Cenario 1: Adicionar bônus positivo
  Dado que estou na aba "Bônus em Atributos" de uma raca
  Quando seleciono "Agilidade" e informo bonus = 2
  E clico em "Adicionar"
  Entao o bônus e adicionado via POST /racas/{id}/bonus-atributos
  E exibido em verde com prefixo "+"

Cenario 2: Adicionar penalidade racial (valor negativo)
  Dado que estou na aba "Bônus em Atributos" de uma raca
  Quando seleciono "Vigor" e informo bonus = -1
  E clico em "Adicionar"
  Entao a penalidade e adicionada via POST /racas/{id}/bonus-atributos com bonus=-1
  E exibida em vermelho com sinal de menos e indicador textual de penalidade

Cenario 3: Campo bonus aceita valores negativos
  Dado que estou no campo de bonus de atributo
  Quando informo bonus = -5
  Entao o campo aceita o valor sem erro
  E o botao Adicionar fica habilitado

Cenario 4: Remover bônus/penalidade de atributo
  Dado que existe um RacaBonusAtributo na lista
  Quando clico em remover
  Entao o item e removido via DELETE /racas/{id}/bonus-atributos/{id}

Cenario 5: Atributos ja vinculados nao aparecem no dropdown
  Dado que "Agilidade" ja foi vinculada a raca
  Quando abro o dropdown de atributos
  Entao "Agilidade" nao aparece nas opcoes

---

**US-008-04: Gerenciar restricoes de classe de uma raca**

Como Mestre,
Quero definir quais classes uma raca pode escolher,
Para restringir combinacoes de raca+classe que nao fazem sentido no lore do jogo (ex: Elfos nao podem ser Guerreiros).

**Criterios de Aceite:**

Cenario 1: Lista vazia = sem restricoes
  Dado que a lista de RacaClassePermitida esta vazia
  Entao a aba exibe "Todas as classes sao permitidas para esta raca"
  E a tabela principal exibe badge "Sem restricoes" em verde

Cenario 2: Adicionar classe permitida
  Dado que estou na aba "Classes Permitidas" de uma raca
  Quando seleciono uma ClassePersonagem no dropdown
  E clico em "Adicionar"
  Entao a classe e adicionada via POST /racas/{id}/classes-permitidas
  E a lista e atualizada com o nome da classe

Cenario 3: Remover classe permitida
  Dado que existe uma RacaClassePermitida na lista
  Quando clico em remover
  Entao o item e removido via DELETE /racas/{id}/classes-permitidas/{id}
  E se a lista ficar vazia, volta a mensagem "Todas as classes sao permitidas"

Cenario 4: Badge na tabela principal indica restricao
  Dado que uma raca tem 2 classes permitidas configuradas
  Entao a tabela principal de racas exibe badge "2 classes" em amarelo/laranja na linha da raca

Cenario 5: Classes ja na lista nao aparecem no dropdown
  Dado que "Mago" ja esta na lista de classes permitidas
  Quando abro o dropdown para adicionar outra classe
  Entao "Mago" nao aparece como opcao

---

## 6. Dossie de Regras por Elemento

### ClassesConfigComponent — Aba "Bônus"

| Elemento | Tipo | Regra de Negocio | Validacao | Role |
|---|---|---|---|---|
| Lista de ClasseBonus | Display | Exibe bonusNome + valorPorNivel | — | MESTRE |
| Campo `valorPorNivel` | Input number | Aceita decimais (step=0.01); sem minimo restrito | `min=0.01`, `step=0.01` | MESTRE |
| Preview "No nivel N" | Display calculado | Exibe `valorPorNivel * 5` como exemplo no nivel 5 | Recalcula ao digitar | MESTRE |
| Dropdown BonusConfig | Select | Filtra bônus ja vinculados (computed signal) | Desabilitado se lista vazia | MESTRE |
| Botao "Adicionar" ClasseBonus | Acao | Habilitado apenas se bonusConfigId selecionado E valorPorNivel > 0 | Client-side | MESTRE |
| Botao remover ClasseBonus | Acao | Remove sem confirmacao (operacao reversivel — pode re-adicionar) | — | MESTRE |

### ClassesConfigComponent — Aba "Aptidoes c/ Bônus"

| Elemento | Tipo | Regra de Negocio | Validacao | Role |
|---|---|---|---|---|
| Lista de ClasseAptidaoBonus | Display | Exibe aptidaoNome + bonus | — | MESTRE |
| Campo `bonus` | Input number | Inteiro; minimo 0 (bônus fixo nao pode ser negativo) | `min=0`, inteiro | MESTRE |
| Dropdown AptidaoConfig | Select | Filtra aptidoes ja vinculadas | — | MESTRE |
| Botao "Adicionar" | Acao | Habilitado apenas se aptidaoConfigId selecionado E bonus >= 0 | Client-side | MESTRE |
| Botao remover | Acao | Remove sem confirmacao | — | MESTRE |

### RacasConfigComponent — Aba "Bônus em Atributos"

| Elemento | Tipo | Regra de Negocio | Validacao | Role |
|---|---|---|---|---|
| Lista de RacaBonusAtributo | Display | Exibe atributoNome + bonus; negativo em vermelho + texto "(penalidade)" | — | MESTRE |
| Campo `bonus` | Input number | Inteiro; aceita negativos (min=-99, max=99) | Sem restricao de minimo=0 | MESTRE |
| Dropdown AtributoConfig | Select | Filtra atributos ja vinculados | — | MESTRE |
| Botao "Adicionar" | Acao | Habilitado se atributoConfigId selecionado | Client-side | MESTRE |
| Indicador de penalidade | Visual | Exibido quando bonus < 0: texto em vermelho + label "(penalidade)" | — | — |

### RacasConfigComponent — Aba "Classes Permitidas"

| Elemento | Tipo | Regra de Negocio | Validacao | Role |
|---|---|---|---|---|
| Estado vazio | Display | "Todas as classes sao permitidas" com icone verde | — | — |
| Lista de RacaClassePermitida | Display | Exibe classeNome | — | MESTRE |
| Dropdown ClassePersonagem | Select | Filtra classes ja na lista; lista todas as classes do jogo | — | MESTRE |
| Botao "Adicionar" | Acao | Habilitado se classeId selecionado | Client-side | MESTRE |
| Botao remover | Acao | Remove sem confirmacao | — | MESTRE |
| Badge na tabela principal | Visual | Vazio = badge verde "Sem restricoes"; com itens = badge amarelo "X classes" | computed | — |

---

## 7. Regras de Negocio Criticas do Dominio

1. **valorPorNivel pode ser fracionario:** `ClasseBonus.valorPorNivel` aceita decimais como 0.5. Um Guerreiro pode ganhar +0.5 B.B.A por nivel, resultando em +5 no nivel 10. O campo HTML deve ter `step="0.01"` e tipo `number`.

2. **ClasseAptidaoBonus.bonus nao pode ser negativo:** Diferente de `RacaBonusAtributo`, o bônus de aptidao de classe e sempre positivo (treinamento da classe, nao penalidade). Minimo = 0.

3. **RacaBonusAtributo.bonus PODE ser negativo:** E uma penalidade racial explicita. O frontend NAO deve validar `min=0` neste campo. Deve exibir indicador visual de penalidade.

4. **Lista vazia de ClassesPermitidas = sem restricao:** Semantica importante para UX — a lista vazia NAO significa "nenhuma classe disponivel", significa "todas as classes disponiveis". O frontend deve comunicar isso claramente.

5. **Unique constraint (backend HTTP 409):** Adicionar o mesmo BonusConfig duas vezes a uma classe resulta em 409. Adicionar o mesmo AtributoConfig duas vezes a uma raca resulta em 409. O frontend filtra as opcoes ja selecionadas no dropdown (prevencao proativa), mas deve tratar o 409 caso ocorra.

6. **Sub-recursos retornam embarcados no GET /{id}:** Ao chamar `GET /classes/{id}` ou `GET /racas/{id}`, o backend ja retorna `bonusConfig`, `aptidaoBonus`, `bonusAtributos` e `classesPermitidas` na resposta. Nao e necessario chamada extra por sub-recurso.

7. **Soft delete:** Sub-recursos deletados ficam invisiveis. O user NAO verifica `deletedAt` no frontend — o backend ja filtra via `@SQLRestriction`.

---

## 8. Pontos em Aberto / Perguntas para Stakeholder

| ID | Pergunta | Impacto |
|----|----------|---------|
| P-01 | O Mestre deve conseguir EDITAR o `valorPorNivel` de um ClasseBonus existente, ou deve remover e re-adicionar? O backend tem endpoint PUT para ClasseBonus? | Se sim, adicionar campo de edicao inline na lista. Se nao, documentar que alterar = remover + re-adicionar. |
| P-02 | O Mestre deve conseguir EDITAR o `bonus` de ClasseAptidaoBonus existente, ou remove-e-recria? | Mesmo que P-01 |
| P-03 | Deve haver confirmacao antes de remover um sub-recurso (ClasseBonus, RacaBonusAtributo)? | Se a classe esta em uso em fichas, remover um bônus muda o calculo retroativamente. Considerar aviso. |
| P-04 | A coluna "Bônus" e "Aptidoes" na tabela de classes deve exibir preview (ex: "B.B.A +1/niv, B.B.M +0.5/niv")? | Afeta o design da BaseConfigTable — pode precisar de coluna customizada. |
| P-05 | Ao deletar uma `ClassePersonagem` que tem fichas vinculadas, o backend retorna 409 ou 500? | Impacta a mensagem de erro exibida ao Mestre no ConfirmDialog. |

---

## 9. Checklist de Validacao UX

- [ ] Confirmar que `p-input-number` com `step=0.01` renderiza campo decimal aceitavel no PrimeNG 21
- [ ] Validar que valores negativos no `p-input-number` com `[min]="-99"` sao exibiveis e editaveis no tema Aura
- [ ] Confirmar layout responsivo do drawer com tres abas e formulario inline de adicao (mobile: empilhar)
- [ ] Validar que o badge na tabela principal de racas ("Sem restricoes" / "X classes") nao quebra o layout da BaseConfigTable
- [ ] Confirmar que a diferenca visual entre bônus positivo (verde) e negativo (vermelho) e suficiente para daltônicos — considerar icone adicional
- [ ] Revisar se o preview "No nivel N: +X" no campo de ClasseBonus e util ou confuso para o Mestre
