# Auditoria: Integração Config → Ficha

> Documento: `docs/analises/INTEGRACAO-CONFIG-FICHA.md`
> Produzido por: Business Analyst/PO
> Data: 2026-04-03
> Propósito: Guiar a implementação do Spec 007 (Motor de Cálculos) e Spec 012 (Level Up)

---

## Sumário Executivo

Esta auditoria mapeia exaustivamente como cada uma das 13 configurações do jogo contribui para a ficha do personagem, identifica lacunas críticas na implementação atual e documenta as regras de negócio implícitas que não estão explícitas nos specs existentes.

**Resultado da auditoria:** O `FichaCalculationService` cobre a estrutura base (atributos, bônus, vida, essência, ameaça), mas tem **7 lacunas críticas** que tornam os cálculos matematicamente incorretos para fichas com vantagens, bônus de classe, bônus de raça e pontos disponíveis.

---

## 1. Mapa de Dependências: Config → Ficha

### 1.1 Tabela Mestre de Integrações

| Configuração | Campo(s) afetado(s) na Ficha | Como calcula | Status | Severidade |
|---|---|---|---|---|
| `AtributoConfig.formulaImpeto` | `FichaAtributo.impeto` | `formulaEvaluator.calcularImpeto(formula, total)` | IMPLEMENTADO | OK |
| `AtributoConfig.valorMinimo/Maximo` | `FichaAtributo.base`, `FichaAtributo.nivel` | Validação: base/nivel não podem ultrapassar limites | GAP — sem validação no FichaCalculationService | ALTO |
| `NivelConfig.pontosAtributo` | `pontosAtributoDisponiveis` (FichaResumoResponse) | `SUM(pontosAtributo) para niveis <= nivelAtual` - `SUM(FichaAtributo.nivel)` | GAP — campo ausente do FichaResumoResponse | CRITICO |
| `NivelConfig.pontosAptidao` | `pontosAptidaoDisponiveis` (FichaResumoResponse) | `SUM(pontosAptidao) para niveis <= nivelAtual` - `SUM(FichaAptidao.base)` | GAP — campo ausente do FichaResumoResponse | CRITICO |
| `NivelConfig.limitadorAtributo` | `FichaAtributo.base` e `FichaAtributo.nivel` | Teto máximo por atributo no nível atual | GAP — sem enforcement no FichaCalculationService | ALTO |
| `NivelConfig.xpNecessaria` | `Ficha.nivel` | Nivel = maior NivelConfig onde xpNecessaria <= ficha.xp | GAP — nivel não é recalculado automaticamente ao salvar | CRITICO |
| `NivelConfig.permitirRenascimento` | `Ficha.renascimentos` (permissão) | Flag de controle: renascimento só disponível se true | GAP — sem validação no service | MEDIO |
| `PontosVantagemConfig.pontosGanhos` | `pontosVantagemDisponiveis` (FichaResumoResponse) | `SUM(pontosGanhos) para niveis <= nivelAtual` - `SUM(FichaVantagem.custoPago)` | GAP — campo ausente do FichaResumoResponse | CRITICO |
| `ClassePersonagem → ClasseBonus.valorPorNivel` | `FichaBonus.classe` | `SUM(classeBonus.valorPorNivel * ficha.nivel)` para cada BonusConfig associado | GAP — FichaBonus.classe nunca é calculado pelo FichaCalculationService | CRITICO |
| `ClassePersonagem → ClasseAptidaoBonus.bonus` | `FichaAptidao.classe` | Valor fixo por aptidão configurada | GAP — FichaAptidao.classe nunca é populado pelo FichaCalculationService | CRITICO |
| `Raca → RacaBonusAtributo.bonus` | `FichaAtributo.outros` | Valor fixo (pode ser negativo) por atributo configurado | GAP — FichaAtributo.outros não é inicializado com bônus de raça no FichaCalculationService | CRITICO |
| `BonusConfig.formulaBase` | `FichaBonus.base` | `formulaEvaluator.calcularDerivado(formulaBase, atributosTotais)` | IMPLEMENTADO | OK |
| `MembroCorpoConfig.porcentagemVida` | `FichaVidaMembro.vida` | `floor(vidaTotal * porcentagem)` | IMPLEMENTADO | OK |
| `VantagemConfig + VantagemEfeito` (8 tipos) | múltiplos campos (ver seção 1.3) | switch por TipoEfeito — ver seção 1.3 | GAP — VantagemEfeito nunca processado no FichaCalculationService | CRITICO (GAP-03) |
| `IndoleConfig` | nenhum campo calculado | Dado identitário — usado apenas como FK e exibição | CORRETO — não gera cálculo | N/A |
| `PresencaConfig` | nenhum campo calculado | Dado identitário — usado apenas como FK e exibição | CORRETO — não gera cálculo | N/A |
| `GeneroConfig` | peso (cálculo de BMI, fora do escopo atual) | Regra de BMI fora do escopo do MVP | FORA DO ESCOPO MVP | BAIXO |
| `DadoProspeccaoConfig` | `FichaProspeccao.quantidade` + `DADO_UP` via VantagemEfeito | Mestre concede; DADO_UP via VantagemEfeito avança o tipo disponível | PARCIAL — quantidade persiste, mas DADO_UP não integrado | ALTO |
| `TipoAptidao` | nenhum campo calculado | Organização das aptidões; pode influenciar VantagemEfeito BONUS_APTIDAO de tipo | CORRETO — organização apenas | N/A |

---

### 1.2 Mapa de Campos de Ficha por Origem dos Dados

```
FichaAtributo.base       ← Jogador distribui na criação (Passo 2 do wizard)
FichaAtributo.nivel      ← Jogador distribui pontos de level up (pontosAtributo de NivelConfig)
FichaAtributo.outros     ← [GAP-RACA] RacaBonusAtributo.bonus (inicializado na criação)
                           + [GAP-VANTAGEM] VantagemEfeito BONUS_ATRIBUTO.valorBonus
FichaAtributo.total      ← base + nivel + outros (IMPLEMENTADO via recalcularTotal)
FichaAtributo.impeto     ← formulaImpeto(total) via FormulaEvaluatorService (IMPLEMENTADO)

FichaAptidao.base        ← Jogador distribui na criação (Passo 3 do wizard)
FichaAptidao.sorte       ← Mestre concede manualmente
FichaAptidao.classe      ← [GAP-CLASSE] ClasseAptidaoBonus.bonus (fixo, nunca calculado)
                           + [GAP-VANTAGEM] VantagemEfeito BONUS_APTIDAO.valorBonus
FichaAptidao.total       ← base + sorte + classe (IMPLEMENTADO via recalcularTotal)

FichaBonus.base          ← formulaBase via FormulaEvaluatorService com atributosTotais (IMPLEMENTADO)
FichaBonus.vantagens     ← [GAP-VANTAGEM] VantagemEfeito BONUS_DERIVADO.valorBonus (nunca calculado)
FichaBonus.classe        ← [GAP-CLASSE] ClasseBonus.valorPorNivel * ficha.nivel (nunca calculado)
FichaBonus.itens         ← Mestre concede manualmente
FichaBonus.gloria        ← Mestre concede manualmente
FichaBonus.outros        ← Mestre concede manualmente
FichaBonus.total         ← base + vantagens + classe + itens + gloria + outros (IMPLEMENTADO)

FichaVida.vt             ← [GAP-VANTAGEM] VantagemEfeito BONUS_VIDA.vt (nunca calculado; hardcoded 0)
FichaVida.outros         ← Mestre concede manualmente
FichaVida.vidaTotal      ← vigorTotal + nivel + vt + renascimentos + outros (IMPLEMENTADO)
                           Obs: vigorTotal é FichaAtributo.total onde abreviacao == "VIG"
FichaVida.vidaAtual      ← estado de combate, editado via PUT /fichas/{id}/vida

FichaVidaMembro.vida     ← floor(vidaTotal * MembroCorpoConfig.porcentagemVida) (IMPLEMENTADO)
                           + [GAP-VANTAGEM] VantagemEfeito BONUS_VIDA_MEMBRO.valorBonus (não integrado)
FichaVidaMembro.danoRecebido ← estado de combate, editado via PUT

FichaEssencia.renascimentos ← Ficha.renascimentos (passado via calcularEssenciaTotal)
FichaEssencia.vantagens  ← [GAP-VANTAGEM] VantagemEfeito BONUS_ESSENCIA.valorBonus (nunca calculado)
FichaEssencia.outros     ← Mestre concede manualmente
FichaEssencia.total      ← floor((VIG + SAB) / 2) + nivel + renascimentos + vantagens + outros (IMPLEMENTADO)
                           Nota: a fórmula usa VIG.total e SAB.total como variáveis fixas no código

FichaAmeaca.itens        ← Mestre concede manualmente
FichaAmeaca.titulos      ← Mestre concede manualmente
FichaAmeaca.renascimentos ← Mestre concede manualmente
FichaAmeaca.outros       ← Mestre concede manualmente
FichaAmeaca.total        ← nivel + itens + titulos + renascimentos + outros (IMPLEMENTADO)
                           Nota: nivel aparece na fórmula do glossário mas NAO está no recalcularTotal() da entity

FichaProspeccao.quantidade ← Mestre concede via endpoint (persiste)
FichaProspeccao.dadoDisponivel ← [GAP-VANTAGEM] VantagemEfeito DADO_UP (não integrado)
```

---

### 1.3 VantagemEfeito — 8 Tipos e Destinos na Ficha

| TipoEfeito | Campo na Ficha afetado | Fórmula de cálculo | Precisa de alvo FK? |
|---|---|---|---|
| `BONUS_ATRIBUTO` | `FichaAtributo.outros` onde `atributoConfig == atributoAlvo` | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Sim — `atributoAlvo` |
| `BONUS_APTIDAO` | `FichaAptidao.outros` (campo ausente) ou `FichaAptidao.sorte`? | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Sim — `aptidaoAlvo` |
| `BONUS_DERIVADO` | `FichaBonus.vantagens` onde `bonusConfig == bonusAlvo` | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Sim — `bonusAlvo` |
| `BONUS_VIDA` | `FichaVida.vt` (soma acumulada de todas as vantagens BONUS_VIDA) | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Não |
| `BONUS_VIDA_MEMBRO` | `FichaVidaMembro.vida` adicional (sem campo dedicado atualmente) | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Sim — `membroAlvo` |
| `BONUS_ESSENCIA` | `FichaEssencia.vantagens` | `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual` | Não |
| `DADO_UP` | determina tipo de dado disponível para prospecção | posicional: sequência de dados indexada por nivelAtual | Não |
| `FORMULA_CUSTOMIZADA` | campo configurável pelo atributoAlvo/bonusAlvo/membroAlvo | `formulaEvaluator.avaliar(formula, variaveis)` | Opcional |

> **PROBLEMA CRITICO (BONUS_APTIDAO):** A entidade `FichaAptidao` tem campos `base`, `sorte`, `classe`, `total`, mas **não tem campo `outros`**. A Spec 007 especifica que `BONUS_APTIDAO` deve ir para `FichaAptidao.outros`, mas esse campo não existe na entidade. Isso requer uma migração de schema.

> **PROBLEMA CRITICO (BONUS_VIDA_MEMBRO):** `FichaVidaMembro` não tem campo `bonusVantagens`. A Spec 007 referencia `FichaVidaMembro.bonusVantagens += bonusMembro`, mas esse campo não existe na entidade.

---

## 2. Gaps Críticos Identificados

### GAP-CALC-01 — FichaBonus.classe nunca é calculado

**Severidade:** CRITICO
**Entidades envolvidas:** `ClasseBonus`, `FichaBonus`

**Problema:** `ClasseBonus.valorPorNivel` define quanto bônus uma classe dá em um BonusConfig por nível do personagem. O campo `FichaBonus.classe` existe na entidade, mas o `FichaCalculationService` nunca o preenche. Resultado: fichas de Guerreiro (que deveria ter B.B.A de classe) mostram `FichaBonus.classe = 0`.

**Fórmula correta:**
```
FichaBonus.classe = SUM(classeBonus.valorPorNivel * ficha.nivel)
                    para cada ClasseBonus onde classeBonus.classe == ficha.classe
                    e classeBonus.bonus == fichaBonus.bonusConfig
```

**O que implementar em Spec 007:**
Antes de `recalcularBonus()`, carregar os `ClasseBonus` da classe da ficha e preencher `FichaBonus.classe` para cada `FichaBonus` correspondente.

---

### GAP-CALC-02 — FichaAptidao.classe nunca é calculado

**Severidade:** CRITICO
**Entidades envolvidas:** `ClasseAptidaoBonus`, `FichaAptidao`

**Problema:** `ClasseAptidaoBonus.bonus` define um bônus fixo de uma classe em uma aptidão específica. O campo `FichaAptidao.classe` existe, mas o `FichaCalculationService` nunca o preenche. Resultado: fichas de Ladrão (que deveria ter bônus em Furtividade) mostram `FichaAptidao.classe = 0`.

**Fórmula correta:**
```
FichaAptidao.classe = classeAptidaoBonus.bonus
                      onde classeAptidaoBonus.classe == ficha.classe
                      e classeAptidaoBonus.aptidao == fichaAptidao.aptidaoConfig
```

**Diferença em relação a ClasseBonus:** `ClasseAptidaoBonus.bonus` é **fixo** (não multiplica por nível). É um bônus que a classe concede permanentemente naquela aptidão.

---

### GAP-CALC-03 — FichaAtributo.outros não inicializado com RacaBonusAtributo

**Severidade:** CRITICO
**Entidades envolvidas:** `RacaBonusAtributo`, `FichaAtributo`

**Problema:** `RacaBonusAtributo.bonus` (pode ser negativo) define modificadores raciais em atributos. A Spec 006 especifica que "bônus de raça vão para o campo `outros` do FichaAtributo". Porém, ao criar a ficha ou ao recalcular, o `FichaCalculationService` não lê os `RacaBonusAtributo` da raça da ficha. O campo `FichaAtributo.outros` fica em 0 para bônus raciais.

**Quando deve ser aplicado:**
- Na **criação da ficha** (campo `outros` inicializado com o bônus racial)
- No **recálculo** (`recalcular()`) o bônus racial deve ser re-aplicado se `outros` for resete

**Questão em aberto (PA-RACA-01):** O campo `outros` acumula múltiplas fontes (bônus racial + VantagemEfeito BONUS_ATRIBUTO). Isso significa que ao recalcular, o serviço deve **zerar e recalcular `outros`** em vez de somar em cima do valor existente — caso contrário, cada recálculo somaria o bônus racial novamente.

---

### GAP-CALC-04 — VantagemEfeito não integrado ao FichaCalculationService

**Severidade:** CRITICO (GAP-03 confirmado anteriormente)
**Entidades envolvidas:** `VantagemEfeito`, `FichaVantagem`, múltiplos campos de Ficha

**Problema:** Os 8 tipos de `VantagemEfeito` estão implementados como configuração (CRUD), mas o `FichaCalculationService` não os processa. Todos os campos alimentados por vantagens ficam zerados:
- `FichaBonus.vantagens` = 0 sempre (TCO nível 3 deveria dar +3 B.B.A)
- `FichaVida.vt` = 0 sempre (Saúde de Ferro nível 2 deveria dar +10 vida)
- `FichaEssencia.vantagens` = 0 sempre
- `FichaAtributo.outros` = 0 para bônus de vantagem

**Campos de modelo ausentes identificados:**
- `FichaAptidao` não tem campo `outros` — necessário para `BONUS_APTIDAO`
- `FichaVidaMembro` não tem campo `bonusVantagens` — necessário para `BONUS_VIDA_MEMBRO`

**Scope da Spec 007:** Este é o ponto central da Spec 007 P0-ABSOLUTA.

---

### GAP-CALC-05 — FichaResumoResponse sem campos de pontos disponíveis

**Severidade:** CRITICO
**Entidades envolvidas:** `FichaResumoResponse`, `NivelConfig`, `PontosVantagemConfig`

**Problema:** `FichaResumoResponse` não inclui `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`. Frontend não pode controlar a distribuição de pontos, badge de level up, nem wizard de atribuição.

**Calculado em:** `FichaResumoService.getResumo()` — que atualmente não consulta `NivelConfig` nem `PontosVantagemConfig`.

**Detalhamento das fórmulas (decisão do PO 2026-04-03):**

```
pontosAtributoDisponiveis =
    SUM(NivelConfig.pontosAtributo para niveis 1 até ficha.nivel)
    + bonus de Raca (se houver campo de pontos de atributo por raça — ver PA-PONTOS-01)
    + bonus de Classe (se houver campo de pontos de atributo por classe — ver PA-PONTOS-01)
    + SUM(VantagemEfeito BONUS_ATRIBUTO com alvo nulo — ver PA-PONTOS-02)
    - SUM(FichaAtributo.nivel)   ← pontos gastos em level up

pontosAptidaoDisponiveis =
    SUM(NivelConfig.pontosAptidao para niveis 1 até ficha.nivel)
    - SUM(FichaAptidao.base)   ← pontos gastos (base editado no wizard/level up)

pontosVantagemDisponiveis =
    SUM(PontosVantagemConfig.pontosGanhos para niveis 1 até ficha.nivel)
    + bonus de Raca (se houver — ver PA-PONTOS-01)
    + bonus de Classe (se houver — ver PA-PONTOS-01)
    - SUM(FichaVantagem.custoPago excluindo Insólitus com custoPago=0)
```

---

### GAP-CALC-06 — Nível não recalculado automaticamente ao mudar XP

**Severidade:** CRITICO
**Entidades envolvidas:** `Ficha.nivel`, `Ficha.xp`, `NivelConfig.xpNecessaria`

**Problema:** Quando o Mestre concede XP via `PUT /fichas/{id}/xp`, o nível deve ser recalculado automaticamente (Spec 006 RF-009). Não há evidência de que o `FichaService` execute esse recálculo ao salvar XP.

**Algoritmo esperado:**
```
novoNivel = MAX(NivelConfig.nivel) onde NivelConfig.xpNecessaria <= ficha.xp e mesmoJogo
ficha.nivel = novoNivel
```

Se `novoNivel > nivelAnterior` (level up), então:
- Liberar pontos adicionais (recalcular `pontosAtributoDisponiveis` etc.)
- Retornar flag de level up no response para o frontend exibir notificação

---

### GAP-CALC-07 — FichaAmeaca.total não inclui o campo `nivel`

**Severidade:** MEDIO
**Entidades envolvidas:** `FichaAmeaca`, `FichaCalculationService.calcularAmeacaTotal()`

**Problema:** O glossário define Ameaça = `nivel + itens + titulos + renascimentos + outros`. O método `FichaAmeaca.recalcularTotal()` calcula apenas `itens + titulos + renascimentos + outros` — sem o nível. Já o `FichaCalculationService.calcularAmeacaTotal()` **inclui o nível** corretamente (`nivelFicha + itens + titulos + renascimentos + outros`). Porém, `recalcularTotal()` na entidade não o inclui.

**Inconsistência:** O service está correto, mas `FichaAmeaca.recalcularTotal()` está errado. Se alguém chamar `ameaca.recalcularTotal()` diretamente (por exemplo em testes), obterá um valor incorreto.

**Recomendação:** Corrigir `FichaAmeaca.recalcularTotal()` para incluir o nível — ou remover o método da entidade e concentrar o cálculo apenas no service (padrão preferível).

---

### GAP-CALC-08 — FichaVida.recalcularTotal() ignora vigorTotal e nivel

**Severidade:** MEDIO
**Entidades envolvidas:** `FichaVida`, `FichaCalculationService.calcularVidaTotal()`

**Problema:** O glossário define Vida Total = `vigorTotal + nivel + vt + renascimentos + outros`. A `FichaVida.recalcularTotal()` calcula apenas `vt + outros`. Porém, o `FichaCalculationService.calcularVidaTotal()` **inclui** vigorTotal e nível corretamente. A inconsistência é a mesma do GAP-CALC-07: o método `recalcularTotal()` na entidade está errado se usado isoladamente.

---

### GAP-CALC-09 — Fórmula de Essência usa VIG e SAB como abreviações hardcoded

**Severidade:** ALTO → **ACEITO PARA MVP**
**Entidades envolvidas:** `FichaCalculationService.calcularEssenciaTotal()` e `recalcular()`

**Problema:** O `FichaCalculationService` busca Vigor e Sabedoria por abreviação hardcoded (`"VIG"` e `"SAB"`) para calcular vida e essência:
```java
.filter(a -> "VIG".equalsIgnoreCase(a.getAtributoConfig().getAbreviacao()))
.filter(a -> "SAB".equalsIgnoreCase(a.getAtributoConfig().getAbreviacao()))
```

Isso viola o princípio central do sistema ("tudo configurável"). Se o Mestre criar um jogo com um atributo chamado Vigor mas com abreviação `"VGR"`, o cálculo de vida quebrará silenciosamente (retornará 0).

**Regra de negócio implícita:** A fórmula de Vida Total no glossário usa `VIGOR + NIVEL + VT + RD + OUTROS`, onde VIG é o atributo Vigor. A regra real é: vida total usa o total do atributo configurado como "fonte de vida" — que hoje é identificado pela abreviação `VIG`, mas deveria ser configurável.

> **ACEITO PARA MVP — Decisão do PO 2026-04-03 (Q16):** O sistema Klayrah exige as abreviações VIG e SAB. As abreviações estão hardcoded no `FichaCalculationService` e permanecem assim para o MVP. Revisitar pós-MVP com campo `papel` (ex: `VIGOR | SABEDORIA | GENERICO`) em `AtributoConfig`, que permitiria ao Mestre marcar qual atributo alimenta cada fórmula. PA-FORMULA-01 encerrada.

---

### GAP-PONTOS-CONFIG — Classe e Raça não têm campos para liberar pontos extras por nível

**Severidade:** MEDIO
**Entidades envolvidas:** `ClassePersonagem`, `Raca`, `NivelConfig`
**Origem:** Decisão do PO 2026-04-03 (Q14)

**Problema:** O PO confirmou que "Todos (Classe e Raça) podem liberar pontos extras quando o usuário chegar a um certo nível — isso deve ser configurável pelo Mestre." No entanto, as entidades `ClassePersonagem` e `Raca` **não têm** qualquer campo para pontos de atributo, aptidão ou vantagem. O que existe hoje:
- `ClasseBonus` — bônus em `BonusConfig` (derivados), escala por nível via `valorPorNivel`
- `RacaBonusAtributo` — bônus fixo em atributo específico (não é ponto livre para distribuir)
- `PontosVantagemConfig` — pontos de vantagem por nível (vinculado ao Jogo, não a Classe/Raça)

**Funcionalidade ausente:** Sub-recursos `ClassePontosNivelConfig` e `RacaPontosNivelConfig` (ou estrutura equivalente) que permitiriam ao Mestre configurar: "ao atingir o nível X com esta Classe/Raça, o personagem ganha Y pontos de atributo, Z pontos de aptidão e W pontos de vantagem extras".

**Exemplo de uso:** Classe "Mago" ganha +1 ponto de aptidão extra ao nível 5 e ao nível 10. Raça "Elfo" ganha +2 pontos de atributo ao nível 1 (bônus de criação racial em pontos livres, não em atributo fixo).

**Impacto no cálculo de T5 (Spec 012):** Enquanto esta entidade não existir, `pontosAtributoGanhos` e `pontosAptidaoGanhos` vêm **apenas de `NivelConfig`**. O T5 MVP está correto ao usar somente `NivelConfig` como fonte. Quando GAP-PONTOS-CONFIG for resolvido, o cálculo de T5 deverá ser revisado para incluir os pontos de classe e raça.

**Decisão para MVP:** Não implementar. Usar somente `NivelConfig` como fonte de pontos no cálculo de `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis`. Spec 012 T5 documentada com essa limitação.

**Pós-MVP:** Criar sub-recursos `ClassePontosNivelConfig` e `RacaPontosNivelConfig` com campos `(nivel, pontosAtributo, pontosAptidao, pontosVantagem)`, controller separado e integração no cálculo de T5.

---

## 3. Regras de Negócio Implícitas (não documentadas nos Specs)

### RNI-01 — ClasseBonus multiplica pelo nível da FICHA, não da classe

A entidade `ClasseBonus` tem `valorPorNivel` que é multiplicado pelo `Ficha.nivel`. Isso significa que ao subir de nível, o bônus de classe nos bônus derivados (B.B.A, Bloqueio etc.) cresce automaticamente sem que o Jogador precise fazer nada. Exemplo: Guerreiro com `ClasseBonus.valorPorNivel = 1` em B.B.A no nível 10 ganha `+10` em B.B.A apenas pela classe.

**Implicação:** Ao recalcular bônus, o `nivel` da ficha é uma variável obrigatória para calcular `FichaBonus.classe`.

---

### RNI-02 — ClasseAptidaoBonus é fixo, não por nível

`ClasseAptidaoBonus.bonus` é um valor inteiro fixo — não multiplica pelo nível. O Ladrão que tem `+2 em Furtividade` mantém esse bônus independente do nível. Isso contrasta com `ClasseBonus` (que cresce por nível).

**Implicação:** `FichaAptidao.classe` é preenchido uma vez na criação e não muda com level up, a menos que o Mestre edite a configuração `ClasseAptidaoBonus`.

---

### RNI-03 — Bônus de Raça não multiplica por nível e pode ser negativo

`RacaBonusAtributo.bonus` é fixo e aplicado uma vez (campo `outros` de `FichaAtributo`). Diferente do bônus de classe nos bônus derivados, bônus racial não escala. Pode ser negativo (penalidade racial).

**Implicação crítica:** Ao recalcular `FichaAtributo.outros`, o sistema deve sempre partir do bônus racial base e somar os bônus de VantagemEfeito BONUS_ATRIBUTO por cima — nunca somar em cima do valor já armazenado, pois isso resultaria em acúmulo incorreto a cada recálculo.

---

### RNI-04 — FichaVida.vt é a soma de TODOS os VantagemEfeito BONUS_VIDA ativos

O campo `FichaVida.vt` não é uma constante configurável pelo Mestre — ele é o resultado da soma de todos os efeitos `BONUS_VIDA` de todas as `FichaVantagem` ativas da ficha. Assim como `FichaAtributo.outros`, precisa ser recalculado do zero a cada `recalcular()`.

---

### RNI-05 — Pontos de atributo distribuídos no level up vão para FichaAtributo.nivel

O campo `FichaAtributo.nivel` registra especificamente os pontos de atributo investidos via level up — é distinto de `FichaAtributo.base` (valor inicial na criação). Portanto:
- `pontosAtributoGastos = SUM(FichaAtributo.nivel)` — pontos de level up investidos
- `pontosAtributoDisponiveisTotal = SUM(NivelConfig.pontosAtributo)` — pontos que o personagem ganhou
- `pontosAtributoDisponiveis = total - gastos`

**Implicação:** A Spec 012 T5 está correta ao usar `SUM(FichaAtributo.nivel)` como "pontos gastos". O `base` é o valor inicial e não conta como ponto de level up gasto.

---

### RNI-06 — Insólitus tem custoPago = 0 e não deve ser contado nos pontos gastos

Quando `VantagemConfig.tipoVantagem == INSOLITUS`, a vantagem é concedida gratuitamente pelo Mestre. O `FichaVantagem.custoPago` deve ser 0. O cálculo de `pontosVantagemGastos` deve **excluir** registros com `custoPago == 0` para não distorcer o saldo.

**Confirmação:** Spec 007 (seção 4) e regras críticas do MEMORY.md confirmam isso.

---

### RNI-07 — Pontos acumulam entre níveis (não resetam)

Pontos de atributo, aptidão e vantagem acumulam. Personagem que não gastou pontos no nível 3 ainda tem esses pontos disponíveis no nível 5. O saldo é sempre `ganhos totais - gastos totais`, nunca um pool "deste nível".

---

### RNI-08 — Vida total depende de VIG.total, não de VIG.base

A fórmula `Vida Total = VIG.total + nivel + vt + renascimentos + outros` usa o **total** do atributo Vigor (que inclui base + nivel + outros + bônus de raça + bônus de vantagem). Isso significa que ao aplicar bônus de VantagemEfeito BONUS_ATRIBUTO em VIG, a vida total também aumenta indiretamente. O `FichaCalculationService` está correto nisso — recalcula atributos primeiro, depois vida.

---

### RNI-09 — Dado de Prospecção e DADO_UP: modelo atual é de quantidade, não de tipo disponível

O modelo atual (`FichaProspeccao`) armazena um **contador de quantidades** por tipo de dado (`DadoProspeccaoConfig`). O efeito `DADO_UP` de VantagemEfeito avança o **tipo** de dado disponível para uso na prospecção (d3 → d4 → ...). São dois mecanismos distintos:

1. **Prospecção normal (Mestre concede):** O Mestre adiciona X unidades de um dado específico (d6, d8 etc.). `FichaProspeccao.quantidade` rastreia isso.
2. **DADO_UP (VantagemEfeito):** A vantagem muda qual é o "dado base" disponível para rolar. Não acumula quantidade — apenas avança a progressão de dado.

**Questão em aberto (PA-007-05 da Spec 007):** A sequência de DADO_UP é fixa (d3→d4→d6→d8→d10→d12→d20) ou usa a ordem dos `DadoProspeccaoConfig` configurados pelo Mestre? Isso afeta como calcular a posição na sequência.

---

### RNI-10 — FichaBonus.base é recalculado do zero a cada recálculo

Diferente de campos como `FichaBonus.itens` e `FichaBonus.gloria` que são editados manualmente pelo Mestre, `FichaBonus.base` é sempre sobrescrito pela `formulaBase` do `BonusConfig`. O `FichaCalculationService` já faz isso corretamente.

---

## 4. Ordem de Cálculo no FichaCalculationService

A ordem importa porque há dependências entre os passos. A sequência correta é:

```
PASSO 1 — Resetar campos derivados (evitar acúmulo em recálculos)
  Para cada FichaAtributo:
    outros = RacaBonusAtributo.bonus da raça da ficha (fixo, pode ser negativo)
             [após Spec 007: + acumulado de VantagemEfeito BONUS_ATRIBUTO]
  Para cada FichaAptidao:
    classe = ClasseAptidaoBonus.bonus da classe da ficha (fixo)
             [após Spec 007: + acumulado de VantagemEfeito BONUS_APTIDAO via campo outros]
  Para cada FichaBonus:
    vantagens = 0       [após Spec 007: preenchido na Fase 2]
    classe = ClasseBonus.valorPorNivel * ficha.nivel (para cada ClasseBonus da classe)
  vida.vt = 0           [após Spec 007: preenchido na Fase 2]
  essencia.vantagens = 0 [após Spec 007: preenchido na Fase 2]

PASSO 2 — [Spec 007] Aplicar VantagemEfeito (ANTES de calcular totais)
  Para cada FichaVantagem onde nivelAtual >= 1:
    Para cada VantagemEfeito da vantagem (não soft-deleted):
      switch(tipoEfeito):
        BONUS_ATRIBUTO   → FichaAtributo(atributoAlvo).outros += calcular(efeito, nivelAtual)
        BONUS_APTIDAO    → FichaAptidao(aptidaoAlvo).outros += calcular(efeito, nivelAtual)
        BONUS_DERIVADO   → FichaBonus(bonusAlvo).vantagens += calcular(efeito, nivelAtual)
        BONUS_VIDA       → FichaVida.vt += calcular(efeito, nivelAtual)
        BONUS_VIDA_MEMBRO→ FichaVidaMembro(membroAlvo).bonusVantagens += calcular(efeito, nivelAtual)
        BONUS_ESSENCIA   → FichaEssencia.vantagens += calcular(efeito, nivelAtual)
        DADO_UP          → FichaProspeccao.posicao = MAX(posicao, index(nivelAtual))
        FORMULA_CUSTOMIZADA → FormulaEvaluatorService.avaliar → aplicar ao alvo

PASSO 3 — Recalcular totais de Atributos (dependem de outros populado no Passo 1+2)
  Para cada FichaAtributo:
    total = base + nivel + outros
    impeto = formulaEvaluator.calcularImpeto(formulaImpeto, total)

PASSO 4 — Construir mapa de variáveis para fórmulas de Bônus
  variaveis = { abreviacao → total } para cada FichaAtributo

PASSO 5 — Recalcular totais de Bônus (dependem de atributos do Passo 3 e classe/vantagens do Passo 1+2)
  Para cada FichaBonus:
    base = formulaEvaluator.calcularDerivado(formulaBase, variaveis)
    total = base + vantagens + classe + itens + gloria + outros

PASSO 6 — Recalcular totais de Aptidões
  Para cada FichaAptidao:
    total = base + sorte + classe + [outros — campo a criar]

PASSO 7 — Recalcular Vida (depende de VIG.total do Passo 3 e vida.vt do Passo 2)
  vigorTotal = FichaAtributo.total onde abreviacao == "VIG" (ou campo configurável)
  vidaTotal = vigorTotal + ficha.nivel + vida.vt + ficha.renascimentos + vida.outros

PASSO 8 — Recalcular Vida por Membro (depende de vidaTotal do Passo 7)
  Para cada FichaVidaMembro:
    vida = floor(vidaTotal * MembroCorpoConfig.porcentagemVida)
           + bonusVantagens [campo a criar, populado no Passo 2]

PASSO 9 — Recalcular Essência (depende de VIG.total e SAB.total do Passo 3)
  sabedoriaTotal = FichaAtributo.total onde abreviacao == "SAB"
  total = floor((vigorTotal + sabedoriaTotal) / 2) + nivel + renascimentos + essencia.vantagens + essencia.outros

PASSO 10 — Recalcular Ameaça
  total = ficha.nivel + ameaca.itens + ameaca.titulos + ameaca.renascimentos + ameaca.outros
```

> **Por que o Passo 1 (reset) é obrigatório:** O `recalcular()` é chamado múltiplas vezes. Sem o reset, cada chamada acumularia os bônus de raça, classe e vantagem em cima dos valores já existentes, causando valores cada vez maiores (bug de acúmulo).

---

## 5. Mudanças de Schema Necessárias (Spec 007)

### 5.1 FichaAptidao — campo `outros` ausente

A Spec 007 (BONUS_APTIDAO) precisa de `FichaAptidao.outros` para armazenar o bônus de VantagemEfeito separado do `sorte` e `classe`.

```sql
ALTER TABLE ficha_aptidoes ADD COLUMN outros INTEGER NOT NULL DEFAULT 0;
```

E atualizar `FichaAptidao.recalcularTotal()`:
```java
this.total = base + sorte + classe + outros;
```

### 5.2 FichaVidaMembro — campo `bonus_vantagens` ausente

A Spec 007 (BONUS_VIDA_MEMBRO) precisa de `FichaVidaMembro.bonusVantagens`.

```sql
ALTER TABLE ficha_vida_membros ADD COLUMN bonus_vantagens INTEGER NOT NULL DEFAULT 0;
```

E atualizar o cálculo: `vida = floor(vidaTotal * porcentagem) + bonusVantagens`

---

## 6. Recomendações por Spec

### Spec 007 — VantagemEfeito e Motor de Cálculos

**Prioridade P0-ABSOLUTA já confirmada pelo PO.**

1. **Criar migrações de schema:** Adicionar `FichaAptidao.outros` e `FichaVidaMembro.bonus_vantagens` antes de qualquer outra mudança.
2. **Refatorar `recalcular()` para suportar o Passo 1 (reset):** Antes de aplicar efeitos, os campos derivados devem ser zerados e os fixos (raça, classe) re-aplicados.
3. **Implementar processamento de VantagemEfeito:** O switch por TipoEfeito descrito na seção 4, Passo 2.
4. **Implementar ClasseBonus no cálculo de FichaBonus.classe:** Passo 1 da nova sequência.
5. **Implementar RacaBonusAtributo em FichaAtributo.outros:** Passo 1 da nova sequência.
6. **Carregar VantagemEfeito com JOIN FETCH:** Evitar N+1 ao processar efeitos no loop.
7. **Corrigir `FichaAmeaca.recalcularTotal()` e `FichaVida.recalcularTotal()`:** Alinhar com a fórmula do glossário ou remover os métodos inconsistentes das entidades.

### Spec 012 — Nível, Progressão e Level Up Frontend

1. **Implementar T5 primeiro:** `FichaResumoResponse` com os 3 campos de pontos disponíveis — este é o bloco de tudo.
2. **Implementar recálculo de nível por XP:** `PUT /fichas/{id}/xp` deve executar o algoritmo de nível após somar o XP.
3. **PA-PONTOS-01 RESOLVIDO (Q14 2026-04-03):** `Raca` e `ClassePersonagem` não têm campos de pontos extras no MVP. Funcionalidade documentada como GAP-PONTOS-CONFIG para pós-MVP. Na T5, usar apenas `NivelConfig` como fonte de pontos de atributo e aptidão.
4. **PA-APTIDAO-01 RESOLVIDO (Q15 2026-04-03):** `pontosAptidaoGastos = SUM(FichaAptidao.base)`. Não há distinção entre valor inicial e incrementos de level up. T5 deve usar esta fórmula diretamente.

### Spec 006 — Wizard de Criação de Ficha

1. **Confirmar que a criação inicializa `FichaAtributo.outros` com `RacaBonusAtributo`:** O Passo 2 do wizard exibe os bônus de raça como "não consumindo pontos". O backend deve inicializar `FichaAtributo.outros = RacaBonusAtributo.bonus` na criação dos registros de atributo.
2. **Confirmar que a criação inicializa `FichaAptidao.classe` com `ClasseAptidaoBonus`:** O Passo 3 do wizard exibe bônus de classe automaticamente. O backend deve inicializar `FichaAptidao.classe = ClasseAptidaoBonus.bonus` na criação.
3. **Confirmar que a criação inicializa `FichaBonus.classe` com `ClasseBonus`:** O bônus de classe em bônus derivados deve ser calculado desde o nível 1.

---

## 7. Pontos em Aberto

| ID | Questão | Impacto | Spec afetada |
|----|---------|---------|--------------|
| PA-PONTOS-01 | ~~`Raca` e `ClassePersonagem` têm campos de "pontos de atributo" e "pontos de vantagem" extras?~~  **RESOLVIDO 2026-04-03 (Q14):** Não têm. Funcionalidade documentada como GAP-PONTOS-CONFIG (pós-MVP). MVP usa apenas `NivelConfig` como fonte de pontos. | Spec 012 T5 |
| PA-APTIDAO-01 | ~~`pontosAptidaoGastos`: `SUM(FichaAptidao.base)` ou campo dedicado?~~ **RESOLVIDO 2026-04-03 (Q15):** `pontosAptidaoGastos = SUM(FichaAptidao.base)`. O sistema não distingue valor inicial de criação vs incrementos de level up. | Spec 012 T5 |
| PA-FORMULA-01 | ~~Como o sistema identifica VIG e SAB para fórmulas de vida/essência?~~ **RESOLVIDO 2026-04-03 (Q16):** Abreviações VIG e SAB hardcoded aceitas para MVP. Revisitar pós-MVP com campo `papel` em `AtributoConfig`. | Spec 007 |
| PA-RACA-01 | Ao recalcular, `FichaAtributo.outros` deve ser zerado e recalculado do zero (raça + vantagens)? Ou deve manter um campo separado para cada fonte? | Afeta a estratégia de reset no início do `recalcular()` | Spec 007 |
| PA-CLASSE-01 | `ClasseAptidaoBonus.bonus` é fixo (não por nível). Isso está correto? Comparar com `ClasseBonus.valorPorNivel` que escala com nível | Afeta `FichaAptidao.classe` e a percepção do jogador sobre seu personagem | Spec 007 |
| PA-DADOS-01 | A sequência de DADO_UP é a ordem dos `DadoProspeccaoConfig.numeroFaces` ordenados por faces? Ou é uma sequência hardcoded (d3→d4→d6→d8→d10→d12→d20)? | Afeta implementação de DADO_UP na Spec 007 | Spec 007 |
| PA-CALC-01 | `FichaBonus.classe` deve usar `valorPorNivel * ficha.nivel` (nível do personagem) ou `valorPorNivel * nivelNaClasse` (nível específico na classe, se o sistema suportar multi-classe no futuro)? | Afeta cálculo de FichaBonus.classe | Spec 007 |

---

## 8. Checklist de Validação UX (impacto nos specs)

- [ ] Spec 006 Passo 2 (atributos): Exibir bônus de raça como "+X (bônus de raça)" em destaque — originado de `RacaBonusAtributo` via `FichaAtributo.outros`
- [ ] Spec 006 Passo 3 (aptidões): Exibir bônus de classe como "+X (bônus de classe)" — originado de `ClasseAptidaoBonus` via `FichaAptidao.classe`
- [ ] Spec 012 level up: Badge com pontos disponíveis usa `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` do `FichaResumoResponse`
- [ ] Spec 007 efeitos: Vantagem com efeito BONUS_DERIVADO deve exibir "+X por nível em [nome do bônus]" — originado de `FichaBonus.vantagens`
- [ ] Detalhe de Ficha: Breakdown de onde vem cada bônus (base + vantagens + classe + itens + glória) — campos já existem em `FichaBonus`, precisam ser expostos no response
- [ ] Detalhe de Ficha: Breakdown da vida total (VIG.total + nivel + VT + renascimentos + outros) — atualmente `FichaVida` não expõe VIG.total separado

---

*Produzido por: Business Analyst/PO | 2026-04-03 | Auditoria pré-implementação Spec 007 e Spec 012*
