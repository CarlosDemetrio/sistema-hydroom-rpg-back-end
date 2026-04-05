# Spec 013 — Plano de Implementacao: Documentacao Tecnica

> Spec: `013-documentacao-tecnica`
> Status: BACKLOG — executar apos specs funcionais implementadas
> Dependencias: Codigo estavel (Specs 005-012 implementadas)
> Bloqueia: Nenhuma spec funcional

---

## 1. Estrategia de Documentacao

### Principio: documentar o "por que", nao o "o que"

O codigo deve ser autoexplicativo para fluxos simples. Documentacao inline e necessaria apenas onde:
1. A logica de negocio e nao-obvia (ordem de calculo, regras de acumulacao)
2. Ha decisoes de design nao evidentes no codigo (por que Set em vez de List, por que campo separado)
3. Ha dependencias implicitas (metodo A deve ser chamado antes de metodo B)
4. A API tem semantica nao padrao (status codes especificos, campos condicionais)

### Prioridade de documentacao

A documentacao deve seguir a regra **80/20**: focar nos 20% do codigo que concentram 80% da complexidade.

| Area | Complexidade | Prioridade |
|------|-------------|-----------|
| FichaCalculationService | Muito alta — 8 tipos de efeito, ordem dependente | **P0** |
| FormulaEvaluatorService | Alta — exp4j, variaveis dinamicas | **P0** |
| Controllers (OpenAPI) | Media — padronizado, mas muitos endpoints | **P1** |
| Frontend stores/services | Media — signals, estado derivado | **P1** |
| Componentes frontend | Baixa/Media — UI autoexplicativa na maioria | **P2** |

---

## 2. Sequencia de Implementacao

### Fase 1 — Backend Critico (T1 + T3)

Javadoc e inline comments nos services criticos. Foco no motor de calculos e logica de negocio complexa.

```
T1: Javadoc em FichaCalculationService, FichaService, FormulaEvaluatorService,
    AbstractConfiguracaoService, SiglaValidationService
    Estimativa: 3-4 horas

T3: Inline comments nas regras de negocio complexas (ordem de calculo,
    VantagemEfeito tipos, ClasseBonus vs RacaBonusAtributo)
    Estimativa: 2-3 horas
```

### Fase 2 — OpenAPI (T2 + T6)

Enriquecer annotations Swagger e gerar spec versionada.

```
T2: OpenAPI annotations em todos os controllers (~15 controllers)
    @Operation, @ApiResponse, @Parameter, @Schema com exemplos
    Estimativa: 4-5 horas

T6: Script para exportar swagger.json + commit no repositorio
    Estimativa: 1-2 horas
```

### Fase 3 — Frontend (T4 + T5)

TSDoc e READMEs nos componentes e services Angular.

```
T4: TSDoc nos services Angular e signal stores
    Estimativa: 2-3 horas

T5: README.md por componente critico (ficha-header, wizard steps, etc.)
    Estimativa: 2-3 horas
```

---

## 3. Dependencias entre Tasks

```
T1 (Javadoc backend) ──┐
T3 (Inline comments) ──┤── podem rodar em PARALELO
                        │
T2 (OpenAPI annotations)┤── independente de T1/T3
                        │
T6 (swagger.json) ──────┘── depende de T2 (annotations devem estar prontas)

T4 (TSDoc frontend) ────┐── podem rodar em PARALELO com backend
T5 (Component README) ──┘── independente de T4
```

**Paralelo maximo possivel:** T1+T3 (backend docs) em paralelo com T4+T5 (frontend docs). T2 pode rodar em paralelo com todos. T6 depende de T2.

---

## 4. Riscos

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| Documentacao desatualiza rapidamente | Docs ficam incorretas apos refatoracao | Executar APOS specs funcionais estarem estaveis |
| Escopo creep — documentar demais | Tasks demoram o dobro | Manter foco nos 20% criticos |
| swagger.json diverge do codigo | Contract testing falha | Script de geracao automatica no CI |
| Frontend muda durante Sprint 2/3 | TSDoc fica obsoleto antes de servir | Documentar apenas apos componentes estaveis |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
