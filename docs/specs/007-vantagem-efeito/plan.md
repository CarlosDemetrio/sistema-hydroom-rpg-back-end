# Spec 007 — Plano de Implementacao: VantagemEfeito e Motor de Calculos

> Spec: `007-vantagem-efeito`
> Status: PRIORIDADE ABSOLUTA — decisao do PO em 2026-04-02
> Dependencias: Spec 004 (VantagemConfig, VantagemEfeito CRUD, todos os configs — implementados)
> Bloqueia: Spec 006 (Ficha completa), Spec 008, Spec 009

---

## 1. Estado Atual do FichaCalculationService

### O que ja esta calculando (correto)

| Metodo | Responsabilidade | Status |
|--------|-----------------|--------|
| `calcularTotalAtributo()` | `base + nivel + outros` | Funcional |
| `calcularImpeto()` | Formula exp4j via FormulaEvaluatorService | Funcional |
| `recalcularAtributos()` | Loop em todos os atributos | Funcional |
| `calcularBaseBonus()` | Formula exp4j com atributos como variaveis | Funcional |
| `calcularTotalBonus()` | `base + vantagens + classe + itens + gloria + outros` | Funcional |
| `recalcularBonus()` | Loop em todos os bonus | Funcional |
| `calcularVidaTotal()` | `vigorTotal + nivel + vt + renascimentos + outros` | Funcional |
| `calcularVidaMembro()` | `floor(vidaTotal * porcentagem)` | Funcional |
| `calcularEssenciaTotal()` | `floor((VIG+SAB)/2) + nivel + renascimentos + vantagens + outros` | Funcional |
| `calcularAmeacaTotal()` | `nivel + itens + titulos + renascimentos + outros` | Funcional |
| `recalcular()` (orquestrador) | Chama atributos → bonus → estado | Funcional |

### O que esta IGNORANDO (problema central desta spec)

- `FichaVantagem` e completamente ignorada no `recalcular()` — o metodo nao recebe nem `List<FichaVantagem>`
- `VantagemEfeito` nunca e consultado durante o calculo
- `FichaVida.vt` nao recebe contribuicao de `BONUS_VIDA` — permanece 0 ou manual
- `FichaBonus.vantagens` nao recebe contribuicao de `BONUS_DERIVADO`
- `FichaAtributo.outros` nao recebe bonus de `BONUS_ATRIBUTO`
- `FichaAptidao.outros` nao existe como campo (ver secao 2.5)
- `FichaVidaMembro.vida` nao recebe bonus de `BONUS_VIDA_MEMBRO` (nao ha campo separado)
- `FichaEssencia.vantagens` nao recebe bonus de `BONUS_ESSENCIA`
- `FichaProspeccao` nao e gerenciada pelo motor (DADO_UP ignorado)
- `FORMULA_CUSTOMIZADA` nao e avaliada

### Lacunas de modelo identificadas

| Lacuna | Impacto | Resolucao proposta |
|--------|---------|-------------------|
| `FichaAptidao` nao tem campo `outros` | Nao ha onde depositar bonus de BONUS_APTIDAO | Adicionar campo `outros` na entidade + migration |
| `FichaVidaMembro` nao tem campo `bonusVantagens` | O bonus de BONUS_VIDA_MEMBRO some no ar | Adicionar campo `bonus_vantagens` na entidade + migration |
| `FichaVantagemRepository` sem query com efeitos | N+1 ao iterar efeitos no loop | Adicionar `findByFichaIdWithEfeitos(@Param fichaId)` com JOIN FETCH |
| `FichaCalculationService.recalcular()` sem parametro vantagens | Metodo nao pode processar efeitos | Adicionar `List<FichaVantagem> vantagens` ao metodo + novo metodo `aplicarEfeitosVantagens()` |

---

## 2. Como Integrar Cada Tipo de Efeito

### 2.1 BONUS_ATRIBUTO

**Campo alvo:** `FichaAtributo.outros`
**Campo de entrada:** `VantagemEfeito.atributoAlvo` (FK → AtributoConfig)
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaAtributo.outros = 0` para todos os atributos (reset antes de recalcular)
2. Para cada `FichaVantagem` com `nivelAtual >= 1`:
   - Para cada `VantagemEfeito` com `tipoEfeito == BONUS_ATRIBUTO`:
     - Encontrar o `FichaAtributo` cujo `atributoConfig.id == efeito.atributoAlvo.id`
     - `fichaAtributo.outros += calcularValor(efeito, nivelAtual)`
3. Recalcular `FichaAtributo.total = base + nivel + outros` depois

**Impacto no `recalcular()`:** A chamada a `aplicarEfeitosVantagens()` deve ocorrer ANTES de `recalcularAtributos()`, pois os totais de atributos dependem do campo `outros` ja populado.

**Ordem correta:**
```
aplicarEfeitosVantagens() → recalcularAtributos() → recalcularBonus() → recalcularEstado()
```

---

### 2.2 BONUS_APTIDAO

**Campo alvo:** `FichaAptidao.outros` (campo a adicionar)
**Campo de entrada:** `VantagemEfeito.aptidaoAlvo` (FK → AptidaoConfig)
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaAptidao.outros = 0` para todas as aptidoes (reset)
2. Para cada efeito `BONUS_APTIDAO`:
   - Encontrar `FichaAptidao` cujo `aptidaoConfig.id == efeito.aptidaoAlvo.id`
   - `fichaAptidao.outros += calcularValor(efeito, nivelAtual)`
3. `FichaAptidao.total = base + sorte + classe + outros`

**Atencao:** `FichaAptidao.recalcularTotal()` precisa ser atualizado para incluir `outros` na soma.

---

### 2.3 BONUS_DERIVADO

**Campo alvo:** `FichaBonus.vantagens`
**Campo de entrada:** `VantagemEfeito.bonusAlvo` (FK → BonusConfig)
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaBonus.vantagens = 0` para todos os bonus (reset)
2. Para cada efeito `BONUS_DERIVADO`:
   - Encontrar `FichaBonus` cujo `bonusConfig.id == efeito.bonusAlvo.id`
   - `fichaBonus.vantagens += calcularValor(efeito, nivelAtual)`
3. `recalcularTotalBonus()` ja inclui `vantagens` na soma — sem mudanca necessaria

**Ordem no recalcular():** `aplicarEfeitosVantagens()` deve ser chamado ANTES de `recalcularBonus()`, pois o bonus base depende dos atributos (que por sua vez dependem do BONUS_ATRIBUTO).

---

### 2.4 BONUS_VIDA

**Campo alvo:** `FichaVida.vt`
**Campos de entrada:** nenhum FK de alvo
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaVida.vt = 0` (reset)
2. Para cada efeito `BONUS_VIDA`:
   - `fichaVida.vt += calcularValor(efeito, nivelAtual)`
3. `calcularVidaTotal()` ja soma `vt` na formula — sem mudanca necessaria

**Atencao critica:** O reset de `vt` deve ser feito APENAS para a contribuicao de vantagens. O `vt` pode ter outras fontes (raca, por exemplo). Por ora, o motor zera e recalcula tudo do zero a cada passagem (RN-007 do spec: motor recalcula sempre). As outras fontes devem ser somadas antes ou depois de forma explicita. Para esta spec, assumir que `vt` vem exclusivamente de vantagens (fontes adicionais serao tratadas em specs futuras).

---

### 2.5 BONUS_VIDA_MEMBRO

**Campo alvo:** `FichaVidaMembro.bonusVantagens` (campo a adicionar)
**Campo de entrada:** `VantagemEfeito.membroAlvo` (FK → MembroCorpoConfig)
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaVidaMembro.bonusVantagens = 0` para todos os membros (reset)
2. Para cada efeito `BONUS_VIDA_MEMBRO`:
   - Encontrar `FichaVidaMembro` cujo `membroCorpoConfig.id == efeito.membroAlvo.id`
   - `fichaVidaMembro.bonusVantagens += calcularValor(efeito, nivelAtual)`
3. Atualizar `calcularVidaMembro()` para incluir `bonusVantagens`:
   - `vida = floor(vidaTotal * porcentagem) + bonusVantagens`

**Diferenca de BONUS_VIDA:** BONUS_VIDA aumenta o pool global (VT), que propaga para todos os membros proporcionalmente. BONUS_VIDA_MEMBRO e um bonus direto em um membro especifico — sem alterar o pool.

---

### 2.6 BONUS_ESSENCIA

**Campo alvo:** `FichaEssencia.vantagens`
**Campos de entrada:** nenhum FK de alvo
**Formula de calculo:** `(valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem`

**Logica de integracao:**
1. Zerar `FichaEssencia.vantagens = 0` (reset)
2. Para cada efeito `BONUS_ESSENCIA`:
   - `fichaEssencia.vantagens += calcularValor(efeito, nivelAtual)`
3. `calcularEssenciaTotal()` ja soma `vantagens` na formula — sem mudanca necessaria

---

### 2.7 DADO_UP

**Campo alvo:** `FichaProspeccao` — determinacao da face do dado disponivel
**Campos de entrada:** nenhum (calculo posicional)
**Formula de calculo:** posicional — `posicao = nivelVantagem - 1` na sequencia ordenada de DadoProspeccaoConfig

**Logica de integracao:**

```
dadosOrdenados = DadoProspeccaoConfig por jogo, ordenados por ordemExibicao ASC
posicaoMaxima  = 0

Para cada FichaVantagem com efeito DADO_UP:
  posicaoCandidata = nivelAtual - 1   // 0-indexed
  posicaoMaxima = MAX(posicaoMaxima, posicaoCandidata)

dadoResultante = dadosOrdenados[ MIN(posicaoMaxima, dadosOrdenados.length - 1) ]

// Atribuir ao FichaProspeccao correspondente ao dado resultante
// (pode haver multiplos FichaProspeccao se o jogo tiver mais de um tipo de dado)
```

**Semantica de FichaProspeccao:** A entidade `FichaProspeccao` tem `dadoProspeccaoConfig` (FK) e `quantidade`. O efeito DADO_UP nao altera quantidade — ele determina qual dado o personagem usa. Modelagem: adicionar campo `dadoDisponivel` em `FichaProspeccao` apontando para o `DadoProspeccaoConfig` resultante do DADO_UP, ou simplesmente retornar o dado calculado no response sem persistir (calculado on-the-fly).

**Decisao de modelagem recomendada:** Persistir `dadoDisponivel` em `FichaProspeccao` como FK opcional para `DadoProspeccaoConfig`. Valor null = sem DADO_UP ativo (usa o dado default do jogo).

**Multiplas vantagens DADO_UP:** Se o personagem tem duas vantagens com DADO_UP (niveis 2 e 4), a maior posicao vence — o personagem usa o dado da posicao 3 (0-indexed: posicao 3 = d8 na sequencia padrao).

---

### 2.8 FORMULA_CUSTOMIZADA

**Campo alvo:** variavel — pode ser atributo, bonus, aptidao, ou sem alvo definido
**Campos de entrada:** `formula` (obrigatorio), FK de alvo (opcional)
**Formula de calculo:** avaliada via `FormulaEvaluatorService`

**Variaveis disponíveis na formula:**

| Variavel | Fonte |
|----------|-------|
| `nivel_vantagem` | `FichaVantagem.nivelAtual` |
| `nivel_personagem` | `Ficha.nivel` |
| `valor_fixo` | `VantagemEfeito.valorFixo` (0 se null) |
| `valor_por_nivel` | `VantagemEfeito.valorPorNivel` (0 se null) |
| `[SIGLA_ATRIBUTO]` | Total calculado do atributo (ex: `FOR`, `AGI`, `VIG`) |

**Logica de integracao:**
1. Montar mapa de variaveis: `buildVariaveisFormula(efeito, fichaVantagem, atributosTotais)`
2. Chamar `FormulaEvaluatorService.calcularDerivado(formula, variaveis)` (adaptar assinatura se necessario)
3. Resultado (int arredondado) aplicado conforme FK de alvo:
   - `atributoAlvo != null` → `FichaAtributo.outros += resultado`
   - `bonusAlvo != null` → `FichaBonus.vantagens += resultado`
   - `aptidaoAlvo != null` → `FichaAptidao.outros += resultado`
   - `membroAlvo != null` → `FichaVidaMembro.bonusVantagens += resultado`
   - Nenhum alvo → registrar como log de aviso (sem alvo = efeito descartado; PO deve clarificar PA-004)

**Validacao ao criar/editar (ja implementado em VantagemEfeitoService):** Formula validada via `FormulaEvaluatorService.isValid()` antes de persistir. Adaptar para passar conjunto de variaveis permitidas para o jogo.

---

## 3. Insolitus — Modelagem Tecnica

### Decisao: Opcao A (campo discriminador em VantagemConfig)

Adicionar campo `tipoVantagem: enum TipoVantagem { VANTAGEM, INSOLITUS }` em `VantagemConfig`.

**Justificativa:**
- Reutiliza toda a infraestrutura existente: CRUD, efeitos, pre-requisitos, mapper, controller
- Insolitus tem os mesmos campos mas semantica diferente: custo = 0, sem validacao de pontos, concedido livremente
- Um campo discriminador e suficiente para diferenciar o comportamento nas camadas de servico e frontend
- Evita duplicacao de codigo (Option B criaria ~10 arquivos novos identicos)

**Mudancas necessarias em VantagemConfig:**

```java
@Enumerated(EnumType.STRING)
@Column(name = "tipo_vantagem", nullable = false, length = 20)
@Builder.Default
private TipoVantagem tipoVantagem = TipoVantagem.VANTAGEM;

@Builder.Default
@Column(name = "visivel_para_jogador", nullable = false)
private Boolean visivelParaJogador = true;
```

**Novo enum:**
```java
public enum TipoVantagem {
    VANTAGEM,   // comprada com pontos — padrao
    INSOLITUS   // concedida pelo Mestre — sem custo
}
```

**Regras de negocio Insolitus:**
- `formulaCusto` deve ser `"0"` para INSOLITUS — o service deve ignorar o custo ao conceder
- Pre-requisitos definidos na config sao ignorados ao conceder (Mestre decide livremente)
- `FichaInsolitus` (ou `FichaVantagem` com flag) PODE ser removida pelo Mestre (reversivel) — diferente de FichaVantagem permanente
- Efeitos mecanicos sao identicos a uma vantagem normal — os 8 tipos se aplicam da mesma forma

**Endpoint de concessao:**
- `POST /api/v1/fichas/{id}/insolitus` com body `{ vantagemConfigId, nivel }` onde a config tem `tipoVantagem = INSOLITUS`
- O service valida que a VantagemConfig tem `tipoVantagem == INSOLITUS` antes de conceder
- Armazenado na mesma tabela `ficha_vantagens` com flag adicional ou tabela separada (PA-001: confirmar com PO)

### Ponto em aberto PA-002 — alternativa simplificada

Se o PO aprovar, a modelagem mais simples e: adicionar `isInsolitus: boolean` diretamente em `VantagemConfig` (campo booleano em vez de enum). O enum `TipoVantagem` e mais extensivel (permite novos tipos no futuro), mas o boolean e mais direto. A decisao final e com o Tech Lead.

---

## 4. Sequencia de Implementacao

### Fase 1 — Backend (bloqueante para frontend)

```
T1: Adaptar modelo de dados
    - Adicionar FichaAptidao.outros + FichaVidaMembro.bonusVantagens
    - Adicionar FichaVantagemRepository.findByFichaIdWithEfeitos (JOIN FETCH)
    - Adaptar FichaAptidao.recalcularTotal() e calcularVidaMembro()

T2: BONUS_ATRIBUTO + BONUS_APTIDAO no FichaCalculationService
    - Novo metodo zerarContribuicoesVantagens()
    - Novo metodo aplicarEfeitosVantagens()
    - Refatorar assinatura de recalcular() para incluir List<FichaVantagem>

T3: BONUS_VIDA + BONUS_ESSENCIA no FichaCalculationService
    - Integrar ao aplicarEfeitosVantagens()
    - Nenhum campo novo necessario

T4: BONUS_DERIVADO + BONUS_VIDA_MEMBRO no FichaCalculationService
    - Integrar ao aplicarEfeitosVantagens()
    - calcularVidaMembro() atualizado para incluir bonusVantagens

T5: DADO_UP no FichaCalculationService
    - Logica posicional com DadoProspeccaoConfig
    - Adicionar campo dadoDisponivel em FichaProspeccao (FK opcional)
    - Novo metodo calcularDadoUp()

T6: FORMULA_CUSTOMIZADA no FichaCalculationService
    - Montar mapa de variaveis estendido (nivel_vantagem, nivel_personagem, valor_fixo, etc.)
    - Reutilizar FormulaEvaluatorService.calcularDerivado() com variaveis String→Double

T7: Insolitus — VantagemConfig com campo tipoVantagem
    - Enum TipoVantagem + migration
    - Endpoint de concessao POST /fichas/{id}/insolitus
    - VantagemEfeitoService: validar formula de FORMULA_CUSTOMIZADA com variaveis do jogo

T8: Testes de integracao (um teste por tipo de efeito)
```

### Fase 2 — Frontend (apos T8 passando)

```
T9:  VantagensConfigComponent — secao de efeitos (lista + add/delete)
T10: FormulaEditor integrado para FORMULA_CUSTOMIZADA
T11: Seletor de dado (dropdown DadoProspeccaoConfig) para DADO_UP
T12: UI de concessao de Insolitus pelo Mestre
```

---

## 5. Dependencias entre Tasks

```
T1 (modelo) → T2 (BONUS_ATRIBUTO/APTIDAO)
            → T3 (BONUS_VIDA/ESSENCIA)
            → T4 (BONUS_DERIVADO/VIDA_MEMBRO)
            → T5 (DADO_UP)
            → T6 (FORMULA_CUSTOMIZADA)

T2 + T3 + T4 + T5 + T6 → T7 (Insolitus)
                        → T8 (Testes)

T8 → T9 (Frontend efeitos)
   → T10 (FormulaEditor)
   → T11 (DADO_UP UI)
   → T12 (Insolitus UI)
```

---

## 6. Riscos e Decisoes Pendentes

| ID | Risco / Decisao | Impacto | Resolucao |
|----|----------------|---------|-----------|
| PA-001 | FichaInsolitus pode ser removida pelo Mestre? | Afeta endpoint DELETE e tabela de armazenamento | Confirmar com PO antes de T7 |
| PA-002 | Enum TipoVantagem vs campo boolean isInsolitus | Afeta modelo de dados | Confirmar com Tech Lead antes de T7 |
| PA-003 | DADO_UP com multiplas vantagens: MAX ou acumula? | Afeta algoritmo em T5 | Spec diz MAX — assumido neste plano |
| PA-004 | FORMULA_CUSTOMIZADA sem alvo: onde aplica? | Afeta T6 | Descartar com log de aviso — confirmar com PO |
| PA-005 | Sequencia DADO_UP: rigida ou via DadoProspeccaoConfig.ordemExibicao? | Afeta T5 | Este plano usa ordemExibicao — mais flexivel |
| — | FichaAptidao.outros: migration breaking em banco existente? | Afeta dados de producao | Adicionar com `DEFAULT 0` — retrocompativel |
| — | FormulaEvaluatorService.calcularDerivado() aceita apenas Map<String,Integer> | Afeta T6 (nivel_vantagem pode ser decimal?) | Adaptar para Map<String,Double> ou sobrecarregar |

---

*Produzido por: Business Analyst/PO | 2026-04-02*
