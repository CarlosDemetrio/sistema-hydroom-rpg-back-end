# Spec 014 — Plano de Implementacao: Cobertura de Testes

> Spec: `014-cobertura-testes`
> Status: BACKLOG — executar apos specs funcionais implementadas
> Dependencias: Codigo estavel (Specs 005-012 implementadas)
> Bloqueia: Nenhuma spec funcional

---

## 1. Estrategia de Cobertura

### Principio: cobertura como rede de seguranca, nao como objetivo

O target de 75% de branch coverage e um minimo de seguranca, nao um numero a atingir a qualquer custo. Testes devem ser uteis (validar comportamento de negocio), nao escritos apenas para inflar metricas.

### Padrao do projeto: 80% integracao, 20% unitario

O projeto ja segue este padrao. Testes de integracao (H2 in-memory, Spring context) sao preferidos porque:
- Validam o fluxo completo (controller → service → repository → DB)
- Detectam problemas de mapeamento JPA, queries JPQL, transacoes
- Sao mais confiaveis que mocks para detectar regressoes

Testes unitarios sao usados apenas para:
- Logica pura sem dependencias externas (ex: formulas matematicas)
- Validacao de edge cases que seriam custosos de montar em integracao

---

## 2. Sequencia de Implementacao

### Fase 1 — Infraestrutura (T1 + T5)

Configurar ferramentas de cobertura sem escrever novos testes.

```
T1: JaCoCo no pom.xml — gera relatorio + threshold 75% branch
    Estimativa: 1-2 horas

T5: Vitest coverage no frontend — gera relatorio HTML
    Estimativa: 1-2 horas
```

**PARALELO:** T1 e T5 sao completamente independentes.

### Fase 2 — Backend Critico (T2 + T3 + T4)

Escrever testes para os services mais criticos.

```
T2: Testes para FichaCalculationService (motor de calculos)
    Estimativa: 4-6 horas (8 tipos de efeito + edge cases)

T3: Testes para FormulaEvaluatorService e GameConfigInitializerService
    Estimativa: 2-3 horas

T4: Testes para DefaultGameConfigProviderImpl
    Estimativa: 1-2 horas
```

**PARALELO:** T2, T3, T4 sao independentes entre si. T2 e o mais longo.

### Fase 3 — Frontend (T6)

Escrever testes para componentes sem cobertura, priorizados pelo relatorio de T5.

```
T6: Testes para componentes criticos (ficha-header, ficha-vantagens-tab, etc.)
    Estimativa: 3-4 horas
    Depende de T5 (para saber quais componentes priorizar)
```

---

## 3. Dependencias entre Tasks

```
T1 (JaCoCo setup) ──────────┐
T5 (Vitest coverage setup) ──┤── PARALELO (infraestrutura)
                              │
T2 (FichaCalculation tests) ─┤
T3 (Formula + GameConfig) ───┤── PARALELO (backend tests)
T4 (DefaultProvider tests) ──┤
                              │
T5 ──> T6 (Frontend tests) ──┘── depende de T5 (relatorio identifica gaps)
```

---

## 4. Exclusoes de Cobertura (JaCoCo)

Classes/pacotes excluidos do threshold:

```xml
<exclude>**/dto/**</exclude>           <!-- records sem logica -->
<exclude>**/mapper/**</exclude>         <!-- gerados por MapStruct -->
<exclude>**/config/**</exclude>         <!-- configuracao Spring -->
<exclude>**/*Application.java</exclude> <!-- main class -->
<exclude>**/model/**</exclude>          <!-- entities com Lombok (getters/setters gerados) -->
```

**Nota:** Excluir `model/**` pode parecer agressivo, mas as entities sao quase 100% Lombok. Metodos com logica real (ex: `recalcularTotal()`, `delete()`, `restore()`) podem ser cobertos indiretamente pelos testes de integracao dos services.

---

## 5. Relatorio de Cobertura — Onde Encontrar

| Area | Comando | Relatorio |
|------|---------|-----------|
| Backend | `./mvnw verify` | `target/site/jacoco/index.html` |
| Frontend | `npx vitest run --coverage` | `coverage/index.html` |

---

## 6. Riscos

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| Threshold 75% e inatingivel no primeiro run | Build quebra antes de escrever testes | Iniciar com threshold baixo (50%), subir apos T2-T4 |
| Testes de integracao lentos apos +50 novos | CI demora mais | Paralelizar com `maven-surefire-plugin` forkCount |
| Vitest coverage add overhead ao CI | Frontend CI demora | Gerar coverage apenas no CI, nao no dev |
| Cobertura infla por testes triviais | Falsa sensacao de seguranca | Revisar testes: cada teste deve validar comportamento, nao apenas executar codigo |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
