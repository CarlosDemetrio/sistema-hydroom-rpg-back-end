# T3 — Inline Comments nas Regras de Negocio Complexas

> Fase: Backend | Dependencias: Nenhuma | Bloqueia: Nenhuma
> Estimativa: 2–3 horas

---

## Objetivo

Adicionar inline comments explicativos nas secoes de codigo com logica de negocio complexa ou nao-obvia. Foco em decisoes de design e regras do dominio Klayrah que nao sao evidentes lendo apenas o codigo.

---

## Areas Prioritarias

### 1. FichaCalculationService — Ordem de Calculo

```java
// A ordem de calculo e critica — BONUS_ATRIBUTO deve ser aplicado ANTES
// de recalcularBonus(), porque bonus derivados (ex: BBA = FOR + AGI)
// dependem dos totais de atributo ja atualizados com contribuicoes de vantagens.
//
// Sequencia:
// 1. zerarContribuicoesVantagens()  — previne dupla contagem
// 2. aplicarEfeitosVantagens()      — popula .outros, .vantagens, .vt
// 3. recalcularAtributos()          — total = base + nivel + outros
// 4. recalcularBonus()              — formula exp4j com atributos atualizados
// 5. recalcularEstado()             — vida, essencia, ameaca
```

### 2. VantagemEfeito — 8 Tipos

Cada tipo de efeito altera um campo diferente. Documentar em `aplicarEfeitosVantagens()`:

```java
// BONUS_ATRIBUTO  → FichaAtributo.outros  (acumula de multiplas vantagens)
// BONUS_APTIDAO   → FichaAptidao.outros   (acumula)
// BONUS_DERIVADO  → FichaBonus.vantagens  (acumula)
// BONUS_VIDA      → FichaVida.vt          (acumula — pool global)
// BONUS_VIDA_MEMBRO → FichaVidaMembro.bonusVantagens (acumula — membro especifico)
// BONUS_ESSENCIA  → FichaEssencia.vantagens (acumula)
// DADO_UP         → FichaProspeccao.dadoDisponivel (MAX posicao — nao acumula)
// FORMULA_CUSTOMIZADA → campo variavel dependendo do FK preenchido
```

### 3. ClasseBonus vs ClasseAptidaoBonus vs RacaBonusAtributo

Tres fontes diferentes de bonus que sao frequentemente confundidas:

```java
// ClasseBonus: bonus fixo ou por nivel dado pela classe do personagem
//   → aplica em FichaBonus.classe = valorPorNivel * ficha.nivel
//   → fonte: ClassePersonagem → ClasseBonus → BonusConfig
//
// ClasseAptidaoBonus: bonus fixo dado pela classe a uma aptidao
//   → aplica em FichaAptidao.classe = ClasseAptidaoBonus.bonus
//   → fonte: ClassePersonagem → ClasseAptidaoBonus → AptidaoConfig
//
// RacaBonusAtributo: bonus fixo dado pela raca a um atributo (pode ser negativo)
//   → aplica em FichaAtributo.outros += RacaBonusAtributo.bonus
//   → fonte: Raca → RacaBonusAtributo → AtributoConfig
//   → ATENCAO: compartilha o campo .outros com BONUS_ATRIBUTO (VantagemEfeito)
```

### 4. SiglaValidationService — Unicidade Cross-Entity

```java
// Siglas/abreviacoes devem ser UNICAS por jogo, validadas ACROSS entities:
// - AtributoConfig.abreviacao
// - BonusConfig (usa sigla derivada?)
// - VantagemConfig (pode ter sigla?)
//
// Motivo: siglas sao usadas como variaveis em formulas exp4j.
// Se dois atributos tiverem a mesma abreviacao, a formula seria ambigua.
// Exemplo: formula "FOR + AGI" — FOR deve mapear para exatamente 1 atributo.
```

### 5. FormulaEvaluatorService — Variaveis Injetadas

```java
// Variaveis disponíveis para formulas exp4j:
// - Abreviacoes de atributos do jogo (ex: FOR, AGI, VIG, SAB, INT, INTU, AST)
//   → valor = FichaAtributo.total do atributo correspondente
// - "total" → soma de todos os atributos (uso raro)
// - "nivel" → ficha.nivel
// - "base" → valor base do campo sendo calculado
//
// Para VantagemEfeito FORMULA_CUSTOMIZADA, variaveis adicionais:
// - "nivel_vantagem" → FichaVantagem.nivelAtual
// - "nivel_personagem" → ficha.nivel
// - "valor_fixo" → VantagemEfeito.valorFixo (0 se null)
// - "valor_por_nivel" → VantagemEfeito.valorPorNivel (0 se null)
```

---

## O que NAO comentar

- Codigo autoexplicativo (ex: `findById(id)`)
- Fluxos CRUD padrao
- Logica que ja esta documentada em Javadoc (T1) — evitar duplicacao

---

## Criterios de Aceitacao

- [ ] `FichaCalculationService`: comentario de ordem de calculo no metodo `recalcular()`
- [ ] `aplicarEfeitosVantagens()`: tabela de tipos de efeito → campo alvo
- [ ] Comentario distinguindo ClasseBonus / ClasseAptidaoBonus / RacaBonusAtributo
- [ ] `SiglaValidationService`: comentario explicando unicidade cross-entity
- [ ] `FormulaEvaluatorService`: comentario listando variaveis disponiveis
- [ ] Nenhum comentario redundante com Javadoc (T1)
- [ ] `./mvnw test` continua passando
