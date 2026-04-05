# Spec 007 — VantagemEfeito e Motor de Calculos

> Spec: `007-vantagem-efeito` / `007-motor-calculos`
> Epic: EPIC 1 (sub-entidades de Vantagem) + EPIC 5 (Motor de Calculos)
> Status: PRIORIDADE ABSOLUTA — decisao do PO em 2026-04-02
> Depende de: Spec 004 (VantagemConfig, BonusConfig, AtributoConfig, AptidaoConfig — todos implementados)
> Bloqueia: Spec 006 (Ficha completa), Spec 008, Spec 009

---

## 1. Visao Geral do Negocio

**Problema resolvido:** `VantagemEfeito` esta implementado no backend (entidade, CRUD, 8 tipos de efeito) mas o `FichaCalculationService` NAO consome esses efeitos. O resultado: fichas com vantagens de bonus tem valores matematicamente errados. O campo `VT` (Vida de Vantagens) esta hardcoded. Toda a camada de calculo da ficha e comprometida.

**Decisao do PO (2026-04-02):**
> "TODOS as configuracoes tem de estar 100% funcionais ANTES de comecar a se preocupar com o modulo de ficha."

**Objetivo:** Integrar os 8 tipos de efeito de `VantagemEfeito` ao `FichaCalculationService`. Implementar `InsolitusCo` como entidade configuravel similar a `VantagemConfig`. Garantir que o motor de calculos produza valores corretos para todas as fichas.

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Mestre | MESTRE | Cria/edita/deleta VantagemEfeito em uma VantagemConfig |
| Sistema | — | Aplica efeitos no FichaCalculationService ao salvar/calcular ficha |
| Backend | — | Expoe endpoints CRUD de VantagemEfeito; calcula valores via FormulaEvaluatorService |

---

## 3. Os 8 Tipos de Efeito — Regras de Negocio

### 3.1 BONUS_ATRIBUTO

**O que faz:** Adiciona bonus permanente ao campo `outros` de um atributo especifico da ficha.

**Campos obrigatorios:** `atributoAlvo` (FK → AtributoConfig)
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** `aptidaoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Formula de calculo:** `valorBonus = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaAtributo.outros += valorBonus` (onde `FichaAtributo.atributoConfig == atributoAlvo`)

**Regra de validacao ao criar efeito:**
- `atributoAlvo` deve pertencer ao mesmo jogo da VantagemConfig
- Exatamente um FK de alvo preenchido por efeito — os demais devem ser null

---

### 3.2 BONUS_APTIDAO

**O que faz:** Adiciona bonus permanente a uma aptidao especifica da ficha (campo `outros` da FichaAptidao).

**Campos obrigatorios:** `aptidaoAlvo` (FK → AptidaoConfig)
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** `atributoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Formula de calculo:** `valorBonus = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaAptidao.outros += valorBonus`

---

### 3.3 BONUS_DERIVADO

**O que faz:** Adiciona bonus ao componente "Vantagens" de um bonus calculado (BonusConfig).

**Campos obrigatorios:** `bonusAlvo` (FK → BonusConfig)
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** `atributoAlvo`, `aptidaoAlvo`, `membroAlvo`, `formula`

**Formula de calculo:** `valorBonus = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaBonus.vantagens += valorBonus`

**Contexto:** Bônus de combate tem 5 fontes — Vantagens, Classe, Itens, Gloria, Outros. Este efeito alimenta a fonte "Vantagens".

---

### 3.4 BONUS_VIDA

**O que faz:** Adiciona pontos ao componente VT (Vida de Vantagens) da formula de Vida Total.

**Campos obrigatorios:** nenhum FK de alvo
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Formula de calculo:** `vt = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaVida.vt += vt`

**Formula de Vida Total:** `vidaTotal = VG (Vigor) + Nivel + VT + Renascimentos + Outros`

---

### 3.5 BONUS_VIDA_MEMBRO

**O que faz:** Adiciona pontos de vida a um membro do corpo especifico, SEM alterar o pool de vida global.

**Campos obrigatorios:** `membroAlvo` (FK → MembroCorpoConfig)
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `formula`

**Formula de calculo:** `bonusMembro = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaVidaMembro.vidaMembro += bonusMembro` (onde membro == membroAlvo)

**Diferenca critica de BONUS_VIDA:** BONUS_VIDA aumenta o pool global (VT); BONUS_VIDA_MEMBRO aumenta diretamente a vida de um membro especifico sem alterar o pool.

---

### 3.6 BONUS_ESSENCIA

**O que faz:** Adiciona pontos ao componente "Vantagens" da formula de Essencia Total.

**Campos obrigatorios:** nenhum FK de alvo
**Campos opcionais:** `valorFixo`, `valorPorNivel`
**Campos nulos:** todos os FK de alvo, `formula`

**Formula de calculo:** `bonusEssencia = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`
**Aplicacao:** `FichaEssencia.vantagens += bonusEssencia`

**Formula de Essencia Total:** `FLOOR((VIG + SAB) / 2) + Nivel + Renascimentos + Vantagens + Outros`

---

### 3.7 DADO_UP

**O que faz:** Faz o dado de prospeccao do personagem "evoluir" uma posicao na sequencia de dados.

**Decisao do PO (2026-04-02):**
> "O dado e para setar apenas o dado que sera jogado, ele nao tem um valor atual. O jogador pode escolher jogar com um dado de 6, 8, 10, 12 ou 20 faces."

**Semantica:**
- Cada nivel da vantagem com DADO_UP avanca o dado em +1 posicao na sequencia
- Sequencia: d3 → d4 → d6 → d8 → d10 → d12 → d20
- O dado resultante e determinado pelo nivel atual da vantagem com DADO_UP
- Nao ha "valor atual do dado" — e apenas a face do dado disponivel para rolar

**Campos obrigatorios:** nenhum
**Campos de valor:** todos null (o calculo e posicional, nao numerico)

**Calculo:** `indiceDado = nivelVantagem - 1` (0-indexed na sequencia acima)
**Exemplo:** vantagem DADO_UP nivel 3 → posicao 2 → d6

**Aplicacao:**
- `FichaProspeccao.dadoDisponivel = sequencia[min(nivelVantagem - 1, sequencia.length - 1)]`
- O personagem pode ter multiplas vantagens DADO_UP — a maior posicao vence (MAX)

**Sequencia de dados padrao (configuravel pelo Mestre via DadoProspeccaoConfig):**
d3, d4, d6, d8, d10, d12, d20

---

### 3.8 FORMULA_CUSTOMIZADA

**O que faz:** Aplica uma formula matematica customizavel para calcular o bonus. Para casos nao cobertos pelos 7 tipos anteriores.

**Decisao do PO (2026-04-02):**
> "Todas as variaveis que fizerem sentido no contexto."

**Campos obrigatorios:** `formula` (string exp4j)
**Campos opcionais:** `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `membroAlvo` (onde aplicar o resultado), `descricaoEfeito`
**Campos de valor:** `valorFixo`, `valorPorNivel` (podem ser usados como variaveis na formula)

**Variaveis disponiveis na formula:**
- `nivel_vantagem` — nivel atual da vantagem na ficha
- `nivel_personagem` — nivel atual da ficha
- Siglas de atributos do jogo (ex: `FOR`, `AGI`, `VIG`) — totais calculados
- `valor_fixo` — valorFixo do efeito (se preenchido)
- `valor_por_nivel` — valorPorNivel do efeito (se preenchido)
- Funcoes matematicas: `floor`, `ceil`, `round`, `sqrt`, `abs`, `min`, `max`

**Validacao ao criar/editar efeito:**
- Formula e validada via `FormulaEvaluatorService.validarFormula(formula, variaveisPermitidas)` — HTTP 422 se invalida
- Se `atributoAlvo` preenchido: resultado e aplicado ao `outros` do FichaAtributo alvo
- Se `bonusAlvo` preenchido: resultado e aplicado ao `vantagens` do FichaBonus alvo
- Se nenhum alvo: formula retorna valor que o servico registra como bonus geral (a definir pelo contexto)

---

## 4. InsolitusCo — Entidade Configuravel

**Decisao do PO (2026-04-02):**
> "Insolitus entra na mesma categoria das vantagens — pode conceder praticamente qualquer coisa: uma raca, uma classe, uma vantagem, bonus, aptidao e etc."

### O que e Insolitus

Insolitus e um traco especial com impacto mecanico real que o Mestre pode conceder ao personagem. Diferente de uma vantagem comum (comprada com pontos), o Insolitus e **concedido livremente pelo Mestre** e pode liberar qualquer coisa que o Mestre decidir.

### Modelagem como entidade configuravel

`InsolitusCo` e modelado como uma variacao de `VantagemConfig` com as seguintes diferencas:

| Aspecto | VantagemConfig | InsolitusCo |
|---------|---------------|-------------|
| Como e obtido | Comprado com pontos de vantagem | Concedido pelo Mestre gratuitamente |
| Custo | `formulaCusto` calculado | Sempre 0 (sem custo) |
| Nivel maximo | Configuravel | Normalmente 1 (mas pode ter niveis) |
| Pré-requisitos | Validados | Ignorados (Mestre concede livremente) |
| Efeitos | 8 tipos de VantagemEfeito | Identico — pode ter qualquer tipo de efeito |
| Visibilidade para Jogador | Sempre visivel | Visivel se `visivelParaJogador = true` |

### Opcao de implementacao recomendada

**Opcao A — Campo discriminador em VantagemConfig:**
Adicionar campo `tipoVantagem: VANTAGEM | INSOLITUS` em `VantagemConfig`. Insolitus e uma vantagem com `tipoVantagem = INSOLITUS` e `formulaCusto = "0"`. Reutiliza toda a infraestrutura existente.

**Opcao B — Entidade separada `InsolitusCo`:**
Nova entidade que replica os campos relevantes de VantagemConfig. Mais limpo semanticamente, mais codigo para manter.

> Recomendacao: Opcao A (campo discriminador). Confirmar com Tech Lead antes da implementacao.

### Endpoint de concessao

| Metodo | Path | Roles | Descricao |
|--------|------|-------|-----------|
| POST | `/api/v1/fichas/{id}/insolitus` | MESTRE | Mestre concede Insolitus a uma ficha |
| GET | `/api/v1/fichas/{id}/insolitus` | MESTRE, JOGADOR (dono) | Lista Insolitus da ficha |
| DELETE | `/api/v1/fichas/{id}/insolitus/{iid}` | MESTRE | Remove Insolitus (reversivel — Mestre decide) |

> Diferenca de FichaVantagem: FichaInsolitus PODE ser removida pelo Mestre (e uma concessao graciosa, nao uma compra permanente). Confirmar com PO.

---

## 5. Contrato de API — VantagemEfeito

> Nota: Endpoints de VantagemEfeito ja existem no backend. Esta secao documenta o contrato e as regras de validacao.

| Metodo | Path | Roles | Descricao |
|--------|------|-------|-----------|
| GET | `/api/v1/configuracoes/vantagens/{vantagemId}/efeitos` | MESTRE, JOGADOR | Listar efeitos de uma vantagem |
| POST | `/api/v1/configuracoes/vantagens/{vantagemId}/efeitos` | MESTRE | Criar efeito |
| PUT | `/api/v1/configuracoes/vantagens/{vantagemId}/efeitos/{efeitoId}` | MESTRE | Atualizar efeito |
| DELETE | `/api/v1/configuracoes/vantagens/{vantagemId}/efeitos/{efeitoId}` | MESTRE | Deletar efeito (soft delete) |

### VantagemEfeitoRequest (POST/PUT)

```json
{
  "tipoEfeito": "BONUS_DERIVADO",
  "bonusAlvoId": 3,
  "atributoAlvoId": null,
  "aptidaoAlvoId": null,
  "membroAlvoId": null,
  "valorFixo": null,
  "valorPorNivel": 1.0,
  "formula": null,
  "descricaoEfeito": "+1 em B.B.A por nivel de TCO"
}
```

### Validacoes por tipo de efeito

| TipoEfeito | FK obrigatorio | Outros FK | Formula | ValorNumerico |
|------------|----------------|-----------|---------|---------------|
| BONUS_ATRIBUTO | `atributoAlvoId` | null | null | ao menos um de valorFixo/valorPorNivel |
| BONUS_APTIDAO | `aptidaoAlvoId` | null | null | ao menos um de valorFixo/valorPorNivel |
| BONUS_DERIVADO | `bonusAlvoId` | null | null | ao menos um de valorFixo/valorPorNivel |
| BONUS_VIDA | nenhum | null | null | ao menos um de valorFixo/valorPorNivel |
| BONUS_VIDA_MEMBRO | `membroAlvoId` | null | null | ao menos um de valorFixo/valorPorNivel |
| BONUS_ESSENCIA | nenhum | null | null | ao menos um de valorFixo/valorPorNivel |
| DADO_UP | nenhum | null | null | todos null |
| FORMULA_CUSTOMIZADA | qualquer (opcional) | null nos nao usados | obrigatorio | opcionais |

---

## 6. FichaCalculationService — Integracao

> **Nota:** Esta secao descreve o fluxo especifico de VantagemEfeito (Passo 5 da sequencia completa). Para a sequencia completa de 10 passos — incluindo reset, bônus de raca, bônus de classe e VantagemEfeito — ver **Secao 11** desta spec.

### Fluxo de processamento de VantagemEfeito (Passo 5 da sequencia)

```
Para cada FichaVantagem (nivelAtual >= 1):
  Para cada VantagemEfeito da vantagem (nao soft-deleted):
    switch(tipoEfeito):
      BONUS_ATRIBUTO   → FichaAtributo(atributoAlvo).outros += calcular(efeito, nivelAtual)
      BONUS_APTIDAO    → FichaAptidao(aptidaoAlvo).outros += calcular(efeito, nivelAtual)
      BONUS_DERIVADO   → FichaBonus(bonusAlvo).vantagens += calcular(efeito, nivelAtual)
      BONUS_VIDA       → FichaVida.vt += calcular(efeito, nivelAtual)
      BONUS_VIDA_MEMBRO → FichaVidaMembro(membroAlvo).bonusVantagens += calcular(efeito, nivelAtual)
      BONUS_ESSENCIA   → FichaEssencia.vantagens += calcular(efeito, nivelAtual)
      DADO_UP          → FichaProspeccao.posicaoDado = MAX(posicaoAtual, posicao(nivelAtual))
      FORMULA_CUSTOMIZADA → FormulaEvaluatorService.avaliar(formula, variaveis) → aplicar ao alvo

Apos processar todos os efeitos (Passos 6–10):
  FichaAtributo.total = base + nivel + outros
  FichaAtributo.impeto = FormulaEvaluatorService.calcularImpeto(formulaImpeto, total)
  FichaBonus.base = FormulaEvaluatorService.calcularDerivado(formulaBase, atributosTotais)
  FichaBonus.total = base + vantagens + classe + itens + gloria + outros
  FichaVida.total = VIG.total + nivel + vt + renascimentos + outros
  FichaVidaMembro.vida = floor(vidaTotal * porcentagem) + bonusVantagens
  FichaEssencia.total = FLOOR((VIG.total + SAB.total) / 2) + nivel + renascimentos + vantagens + outros
  FichaAmeaca.total = nivel + itens + titulos + renascimentos + outros
```

### Metodo calcular(efeito, nivelAtual)

```
calcular(VantagemEfeito efeito, int nivelAtual):
  return (efeito.valorFixo ?? 0) + (efeito.valorPorNivel ?? 0) * nivelAtual
```

### N+1 Prevention

O `FichaCalculationService` nao deve disparar queries adicionais ao processar efeitos. O carregamento deve ser feito com JOIN FETCH no service antes de chamar o calculo:

```java
// Correto: carregar tudo de uma vez
fichaVantagemRepository.findByFichaIdWithEfeitos(fichaId)
// Incorreto: lazy load dentro do loop de calculo
```

---

## 7. UI de Gerenciamento de Efeitos

### Tela de configuracao: VantagemConfig → Efeitos

O formulario de criacao/edicao de VantagemConfig deve incluir uma secao "Efeitos" que permita ao Mestre adicionar, editar e remover efeitos.

**Comportamento do formulario de efeito por tipo:**

| TipoEfeito selecionado | Campos exibidos |
|------------------------|-----------------|
| BONUS_ATRIBUTO | Dropdown "Atributo alvo" (obrigatorio) + "Valor fixo" + "Valor por nivel" |
| BONUS_APTIDAO | Dropdown "Aptidao alvo" (obrigatorio) + "Valor fixo" + "Valor por nivel" |
| BONUS_DERIVADO | Dropdown "Bonus alvo" (obrigatorio) + "Valor fixo" + "Valor por nivel" |
| BONUS_VIDA | "Valor fixo" + "Valor por nivel" (sem dropdown de alvo) |
| BONUS_VIDA_MEMBRO | Dropdown "Membro alvo" (obrigatorio) + "Valor fixo" + "Valor por nivel" |
| BONUS_ESSENCIA | "Valor fixo" + "Valor por nivel" (sem dropdown de alvo) |
| DADO_UP | Apenas descricao — sem campos numericos ou FK (calculo e posicional) |
| FORMULA_CUSTOMIZADA | Campo "Formula" (obrigatorio, com preview) + dropdown alvo opcional + link "Ver variaveis disponíveis" |

**Preview calculado (em tempo real no formulario):**
- Para tipos numericos: "No nivel 3: +X em [nome do alvo]"
- Para DADO_UP: "No nivel 3: dado d6"
- Para FORMULA_CUSTOMIZADA: botao "Testar formula" que chama POST /formulas/preview

**FormulaEditor component:**
Para FORMULA_CUSTOMIZADA, usar o `FormulaEditorComponent` existente em `src/app/shared/components/formula-editor/`.

---

## 8. Epico e User Stories

### Epic 1 (sub) + Epic 5 — VantagemEfeito e Motor de Calculos

---

**US-007-01: Criar efeito em VantagemConfig**
Como Mestre configurando uma vantagem,
Quero adicionar um ou mais efeitos mecanicos a ela,
Para que o motor de calculos aplique os bonus corretamente nas fichas dos jogadores.

Criterios de Aceite:

Cenario 1: Criar efeito BONUS_DERIVADO
  Dado que acesso a configuracao da vantagem TCO
  Quando seleciono tipo "BONUS_DERIVADO", seleciono "B.B.A" como alvo, informo valorPorNivel=1
  E confirmo
  Entao POST /configuracoes/vantagens/{id}/efeitos retorna HTTP 201
  E o efeito aparece na lista de efeitos da vantagem
  E o preview exibe "No nivel 5: +5 em B.B.A"

Cenario 2: Criar efeito DADO_UP
  Dado que seleciono tipo "DADO_UP"
  Entao os campos numericos e de alvo estao ocultos/desabilitados
  E ao confirmar, o efeito e criado sem valores numericos

Cenario 3: FK de alvo de jogo diferente
  Dado que seleciono um atributo de outro jogo como alvo
  Quando confirmo
  Entao recebo HTTP 422 com mensagem "O atributo alvo deve pertencer ao mesmo jogo da vantagem"

---

**US-007-02: Calculos aplicados corretamente na ficha**
Como Jogador com a vantagem TCO nivel 3,
Quero que meu B.B.A seja calculado corretamente incluindo o bonus da vantagem,
Para que minha ficha reflita meu personagem com precisao.

Criterios de Aceite:

Cenario 1: Bonus derivado aplicado
  Dado que TCO tem efeito BONUS_DERIVADO em B.B.A com valorPorNivel=1
  E minha FichaVantagem tem nivelAtual=3
  Quando chamo GET /fichas/{id}/resumo
  Entao FichaBonus.BBA.vantagens = 3
  E FichaBonus.BBA.total inclui esses 3 pontos

Cenario 2: Vida de vantagem aplicada (VT)
  Dado que "Saude de Ferro" tem efeito BONUS_VIDA com valorPorNivel=5
  E minha FichaVantagem tem nivelAtual=4
  Quando chamo GET /fichas/{id}/resumo
  Entao vidaTotal inclui +20 de VT (5 * 4)

---

**US-007-03: Preview de formula customizada**
Como Mestre criando um efeito FORMULA_CUSTOMIZADA,
Quero testar a formula antes de salvar,
Para verificar que o calculo esta correto.

Criterios de Aceite:

Cenario 1: Preview bem-sucedido
  Dado que informo formula "floor(FOR / 2) + nivel_vantagem * 2"
  E clico "Testar formula"
  Entao o sistema chama POST /formulas/preview
  E exibe o resultado calculado com os valores de exemplo

Cenario 2: Formula invalida
  Dado que informo formula "FOR + VARIAVEL_INEXISTENTE"
  Quando clico "Testar"
  Entao recebo HTTP 422 com "Variavel nao reconhecida: VARIAVEL_INEXISTENTE"

---

## 9. Bugs Pre-Existentes no FichaCalculationService

> Esta secao documenta bugs ativos no `FichaCalculationService` que existem INDEPENDENTEMENTE da integracao de `VantagemEfeito`. Devem ser corrigidos na task P0-T0, antes de qualquer outra task desta spec.

---

### GAP-CALC-01 — FichaBonus.classe nunca e calculado

**Severidade:** CRITICO
**Entidades envolvidas:** `ClasseBonus`, `FichaBonus`

**Problema:** `ClasseBonus.valorPorNivel` define quanto bonus uma classe da em um `BonusConfig` por nivel do personagem. O campo `FichaBonus.classe` existe na entidade, mas o `FichaCalculationService.calcularTotalBonus()` nunca o preenche. Fichas de Guerreiro (que deveriam ter B.B.A de classe) mostram `FichaBonus.classe = 0`.

**Formula correta:**
```
FichaBonus.classe = SUM(classeBonus.valorPorNivel * ficha.nivel)
                    para cada ClasseBonus onde classeBonus.classe == ficha.classe
                    e classeBonus.bonus == fichaBonus.bonusConfig
```

**Onde corrigir:** `FichaCalculationService` — adicionar metodo `aplicarClasseBonus()` chamado no Passo 3 da sequencia de calculo (antes de `recalcularBonus()`).

**Requer:** Query para buscar `ClasseBonus` da classe da ficha + nivel atual da ficha.

---

### GAP-CALC-02 — FichaAptidao.classe nunca e calculado

**Severidade:** CRITICO
**Entidades envolvidas:** `ClasseAptidaoBonus`, `FichaAptidao`

**Problema:** `ClasseAptidaoBonus.bonus` define um bonus fixo de uma classe em uma aptidao especifica. O campo `FichaAptidao.classe` existe, mas o `FichaCalculationService.recalcularAptidoes()` nunca o preenche. Fichas de Ladrao (que deveriam ter bonus em Furtividade) mostram `FichaAptidao.classe = 0`.

**Diferenca critica em relacao a GAP-CALC-01:** `ClasseAptidaoBonus.bonus` e **fixo** — nao multiplica pelo nivel. O Ladrao que tem `+2 em Furtividade` mantem esse bonus independente do nivel.

**Formula correta:**
```
FichaAptidao.classe = classeAptidaoBonus.bonus
                      onde classeAptidaoBonus.classe == ficha.classe
                      e classeAptidaoBonus.aptidao == fichaAptidao.aptidaoConfig
```

**Onde corrigir:** `FichaCalculationService` — adicionar metodo `aplicarClasseAptidaoBonus()` chamado no Passo 4 da sequencia de calculo (antes de `recalcularAptidoes()`).

---

### GAP-CALC-03 — FichaAtributo.outros nao inicializado com RacaBonusAtributo

**Severidade:** CRITICO
**Entidades envolvidas:** `RacaBonusAtributo`, `FichaAtributo`

**Problema:** `RacaBonusAtributo.bonus` (pode ser negativo) define modificadores raciais em atributos. A Spec 006 especifica que "bonus de raca vao para o campo `outros` do FichaAtributo". Porem, ao criar a ficha ou ao recalcular, o `FichaCalculationService.recalcularAtributos()` nao le os `RacaBonusAtributo` da raca da ficha. O campo `FichaAtributo.outros` fica em 0 para bonus raciais.

**Caracteristicas:**
- `RacaBonusAtributo.bonus` e **fixo** — nao escala com nivel
- Pode ser **negativo** (penalidade racial)
- O campo `outros` acumula multiplas fontes (bonus racial + VantagemEfeito BONUS_ATRIBUTO)

**Regra critica de idempotencia:** Ao recalcular, o sistema deve sempre ZERAR `outros` e re-calcular do zero (raca + vantagens). Somar em cima do valor existente causaria acumulo incorreto a cada recalculo.

**Onde corrigir:** `FichaCalculationService` — no Passo 2 da sequencia de calculo, apos o reset, aplicar `RacaBonusAtributo` em `FichaAtributo.outros`.

---

### GAP-CALC-06 — Nivel nao recalculado ao conceder XP

**Severidade:** CRITICO
**Entidades envolvidas:** `Ficha.nivel`, `Ficha.xp`, `NivelConfig.xpNecessaria`

**Problema:** Quando o Mestre concede XP via `PUT /fichas/{id}/xp`, o nivel deve ser recalculado automaticamente (Spec 006 RF-009). O `FichaService` nao executa esse recalculo ao salvar XP.

**Algoritmo esperado:**
```
novoNivel = MAX(NivelConfig.nivel)
            onde NivelConfig.xpNecessaria <= ficha.xp
            e mesmoJogo
ficha.nivel = novoNivel
```

Se `novoNivel > nivelAnterior` (level up):
- Liberar pontos adicionais (`pontosAtributoDisponiveis` etc.)
- Retornar flag `levelUp: true` no response para o frontend exibir notificacao

**Onde corrigir:** `FichaService` — ao processar `PUT /fichas/{id}/xp`, executar o algoritmo de nivel apos somar o XP.

---

### GAP-CALC-07 — FichaAmeaca.recalcularTotal() nao inclui `nivel`

**Severidade:** MEDIO
**Entidades envolvidas:** `FichaAmeaca`

**Problema:** O glossario define Ameaca = `nivel + itens + titulos + renascimentos + outros`. O metodo `FichaAmeaca.recalcularTotal()` calcula apenas `itens + titulos + renascimentos + outros` — sem o nivel. Ja o `FichaCalculationService.calcularAmeacaTotal()` inclui o nivel corretamente. A inconsistencia: se alguem chamar `ameaca.recalcularTotal()` diretamente (ex: em testes), obtera um valor incorreto.

**Onde corrigir:** `FichaAmeaca.recalcularTotal()` — adicionar `nivel` no calculo, ou remover o metodo da entidade e concentrar o calculo apenas no service.

---

### GAP-CALC-08 — FichaVida.recalcularTotal() ignora vigorTotal e nivel

**Severidade:** MEDIO
**Entidades envolvidas:** `FichaVida`

**Problema:** O glossario define Vida Total = `vigorTotal + nivel + vt + renascimentos + outros`. O metodo `FichaVida.recalcularTotal()` calcula apenas `vt + outros`. O `FichaCalculationService.calcularVidaTotal()` esta correto, mas o metodo da entidade esta errado se usado isoladamente.

**Onde corrigir:** `FichaVida.recalcularTotal()` — adicionar `vigorTotal` e `nivel`, ou remover o metodo da entidade.

---

### GAP-CALC-09 — VIG e SAB hardcoded por abreviacao

**Severidade:** ALTO
**Entidades envolvidas:** `FichaCalculationService`

**Status:** Aguarda decisao do PO (Q16 — ver Pontos em Aberto). Nao implementar na P0-T0.

**Problema:** O `FichaCalculationService` busca Vigor e Sabedoria por abreviacao hardcoded (`"VIG"` e `"SAB"`). Viola o principio "tudo configuravel" — se o Mestre criar um jogo com abreviacao `"VGR"`, o calculo de vida quebrara silenciosamente.

---

## 10. Mudancas de Schema Necessarias (pre-requisito da Spec 007)

Estas duas alteracoes de schema sao pre-requisito para a Spec 007 funcionar. Devem ser executadas na task P1-T1.

### SCHEMA-01 — FichaAptidao: adicionar campo `outros`

A entidade `FichaAptidao` tem campos `base`, `sorte`, `classe`, `total` mas **nao tem campo `outros`**. O tipo de efeito `BONUS_APTIDAO` precisa de um campo dedicado para receber bonus de VantagemEfeito separado de `sorte` e `classe`.

**Migracao SQL:**
```sql
ALTER TABLE ficha_aptidoes ADD COLUMN outros INTEGER NOT NULL DEFAULT 0;
```

**Atualizacao de codigo:**
```java
// FichaAptidao.recalcularTotal():
this.total = base + sorte + classe + outros;
```

### SCHEMA-02 — FichaVidaMembro: adicionar campo `bonus_vantagens`

A entidade `FichaVidaMembro` nao tem campo `bonusVantagens`. O tipo de efeito `BONUS_VIDA_MEMBRO` precisa de um campo dedicado para armazenar bonus de vantagem SEM alterar o pool de vida global.

**Migracao SQL:**
```sql
ALTER TABLE ficha_vida_membros ADD COLUMN bonus_vantagens INTEGER NOT NULL DEFAULT 0;
```

**Atualizacao de codigo:**
```java
// FichaVidaMembro: calculo de vida deve incluir bonusVantagens:
vida = floor(vidaTotal * MembroCorpoConfig.porcentagemVida) + bonusVantagens
```

---

## 11. Ordem de Calculo — FichaCalculationService.recalcular()

O metodo `recalcular()` deve seguir rigorosamente esta sequencia de 10 passos. A ordem importa porque ha dependencias entre os passos — alterar a sequencia produz valores matematicamente incorretos.

```
PASSO 1 — RESET: zerar todos os campos derivados (obrigatorio para idempotencia)
  Para cada FichaAtributo:    outros = 0
  Para cada FichaAptidao:     outros = 0  [campo a criar em SCHEMA-01]
  Para cada FichaBonus:       vantagens = 0
  FichaVida.vt = 0
  Para cada FichaVidaMembro:  bonusVantagens = 0  [campo a criar em SCHEMA-02]
  FichaEssencia.vantagens = 0

  [NAO zerar: FichaBonus.classe, FichaAptidao.classe — serao sobrescritos nos Passos 2-4]

PASSO 2 — Aplicar RacaBonusAtributo → FichaAtributo.outros
  Para cada RacaBonusAtributo da raca da ficha:
    FichaAtributo(atributoConfig).outros += racaBonusAtributo.bonus
  Nota: bonus pode ser negativo (penalidade racial).
  Nota: nao escala com nivel — valor fixo.

PASSO 3 — Aplicar ClasseBonus → FichaBonus.classe
  Para cada ClasseBonus da classe da ficha:
    FichaBonus(bonusConfig).classe = classeBonus.valorPorNivel * ficha.nivel
  Nota: multiplica pelo nivel da FICHA, nao da classe.

PASSO 4 — Aplicar ClasseAptidaoBonus → FichaAptidao.classe
  Para cada ClasseAptidaoBonus da classe da ficha:
    FichaAptidao(aptidaoConfig).classe = classeAptidaoBonus.bonus
  Nota: valor fixo — nao escala com nivel.

PASSO 5 — Processar VantagemEfeito (8 tipos)
  Para cada FichaVantagem onde nivelAtual >= 1:
    Para cada VantagemEfeito da vantagem (nao soft-deleted):
      BONUS_ATRIBUTO    → FichaAtributo(atributoAlvo).outros += calcular(efeito, nivel)
      BONUS_APTIDAO     → FichaAptidao(aptidaoAlvo).outros += calcular(efeito, nivel)
      BONUS_DERIVADO    → FichaBonus(bonusAlvo).vantagens += calcular(efeito, nivel)
      BONUS_VIDA        → FichaVida.vt += calcular(efeito, nivel)
      BONUS_VIDA_MEMBRO → FichaVidaMembro(membroAlvo).bonusVantagens += calcular(efeito, nivel)
      BONUS_ESSENCIA    → FichaEssencia.vantagens += calcular(efeito, nivel)
      DADO_UP           → FichaProspeccao.posicao = MAX(posicao, index(nivel))
      FORMULA_CUSTOMIZADA → FormulaEvaluatorService.avaliar → aplicar ao alvo

PASSO 6 — Recalcular totais de Atributos
  Para cada FichaAtributo:
    total = base + nivel + outros
    impeto = formulaEvaluator.calcularImpeto(formulaImpeto, total)

PASSO 7 — Recalcular totais de Bonus (depende de atributos do Passo 6)
  variaveis = { abreviacao → total } para cada FichaAtributo
  Para cada FichaBonus:
    base = formulaEvaluator.calcularDerivado(formulaBase, variaveis)
    total = base + vantagens + classe + itens + gloria + outros

PASSO 8 — Recalcular totais de Aptidoes
  Para cada FichaAptidao:
    total = base + sorte + classe + outros

PASSO 9 — Recalcular Vida e Membros (depende de VIG.total do Passo 6)
  vigorTotal = FichaAtributo.total onde atributoConfig == atributo de Vigor
  vidaTotal = vigorTotal + ficha.nivel + vida.vt + ficha.renascimentos + vida.outros
  Para cada FichaVidaMembro:
    vida = floor(vidaTotal * MembroCorpoConfig.porcentagemVida) + bonusVantagens

PASSO 10 — Recalcular Essencia e Ameaca (depende de VIG/SAB do Passo 6)
  sabedoriaTotal = FichaAtributo.total onde atributoConfig == atributo de Sabedoria
  essencia.total = floor((vigorTotal + sabedoriaTotal) / 2) + nivel + renascimentos + vantagens + outros
  ameaca.total = ficha.nivel + ameaca.itens + ameaca.titulos + ameaca.renascimentos + ameaca.outros
```

> **Por que o Passo 1 (reset) e obrigatorio:** `recalcular()` e chamado multiplas vezes (ao salvar ficha, ao conceder XP, ao comprar vantagem). Sem o reset, cada chamada acumularia os bonus de raca, classe e vantagem em cima dos valores ja existentes, causando valores cada vez maiores (bug de acumulo).

---

## 12. Regras de Negocio Criticas do Dominio

**RN-001 — Exatamente um FK de alvo por efeito:** A camada de servico deve validar que apenas o FK correspondente ao tipo esta preenchido. Os demais devem ser null.

**RN-002 — Calculo DADO_UP e posicional:** O dado resultante e determinado pela posicao na sequencia configurada via DadoProspeccaoConfig, nao por um valor numerico.

**RN-003 — FORMULA_CUSTOMIZADA valida via exp4j:** Toda formula customizada deve ser validada antes de persistir. Variaveis invalidas geram HTTP 422.

**RN-004 — Sigla de VantagemConfig ja validada:** A unicidade de sigla cross-entity ja e garantida pela Spec 004. Nao revalidar neste fluxo.

**RN-005 — Soft delete de efeito:** Deletar um efeito usa soft delete. Se o efeito estava sendo calculado em fichas ativas, o calculo deve ignorar efeitos com `deleted_at != null`.

**RN-006 — Cascade ALL em efeitos:** Ao deletar uma VantagemConfig (soft delete), seus efeitos tambem sao soft-deleted automaticamente.

**RN-007 — Motor recalcula sempre:** O `FichaCalculationService` recalcula TODOS os efeitos a cada save/recalculo. Nao ha cache de calculos intermediarios — os valores finais sao sempre recalculados do zero.

---

## 13. Pontos em Aberto

| ID | Questao | Impacto |
|----|---------|---------|
| PA-001 | FichaInsolitus: pode ser removida pelo Mestre? Ou segue a regra de FichaVantagem (nunca remove)? | Afeta o endpoint DELETE /fichas/{id}/insolitus |
| PA-002 | InsolitusCo: Opcao A (campo discriminador) ou Opcao B (entidade separada)? | Afeta o modelo de dados e endpoints |
| PA-003 | DADO_UP com multiplas vantagens: usa a de maior nivel ou acumula? | Afeta o algoritmo de calculo de FichaProspeccao |
| PA-004 | FORMULA_CUSTOMIZADA sem alvo definido: o resultado vai para onde? (campo generico de bonus?) | Afeta a aplicacao do calculo |
| PA-005 | Sequencia de dados DADO_UP: e rigidamente d3→d4→d6→d8→d10→d12→d20 ou configuravel por jogo via DadoProspeccaoConfig? | Afeta a implementacao do calculo posicional |
| PA-006 (GAP-CALC-09) | VIG e SAB hardcoded por abreviacao: manter convencao ou tornar configuravel (campo tipoAtributo: VIGOR / SABEDORIA / GENERICO)? | Afeta a generalizacao do motor para jogos com nomes de atributos diferentes |

---

## 14. Checklist de Validacao UX

- [ ] Formulario de efeito: campos mostrados/ocultos dinamicamente por tipo de efeito selecionado
- [ ] Preview em tempo real do calculo (ex: "No nivel 3: +3 em B.B.A")
- [ ] Badge visual na lista de vantagens indicando quantos efeitos cada vantagem tem
- [ ] Para DADO_UP: exibir a sequencia de dados graficamente (d3 d4 d6 d8...)
- [ ] Para FORMULA_CUSTOMIZADA: botao "Ver variaveis disponiveis" abre tooltip/modal com lista
- [ ] Validacao de FK de alvo: dropdown so exibe configs do mesmo jogo
- [ ] Confirmacao ao deletar efeito de vantagem ativa em fichas existentes

---

## 15. Dependencias

- **Depende de:** Spec 004 (VantagemConfig, BonusConfig, AtributoConfig, AptidaoConfig, MembroCorpoConfig — todos implementados e funcionais)
- **Bloqueia:** Spec 006 (motor de calculos deve funcionar antes de Ficha ser considerada "pronta")
- **Usa:** `FormulaEvaluatorService` existente para FORMULA_CUSTOMIZADA
- **Usa:** `FormulaEditorComponent` existente no frontend

---

*Produzido por: Business Analyst/PO | 2026-04-02 | Revisado: 2026-04-03 — adicionadas secoes 9 (bugs pre-existentes), 10 (schema changes), 11 (ordem de calculo 10 passos), PA-006*
