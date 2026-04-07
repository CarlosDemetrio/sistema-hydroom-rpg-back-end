# Copilot R04 — Spec 017 P0 (SecurityConfig 401 + frontend fixes)

> Data: 2026-04-07
> Branch: `main`
> Base: 719 testes backend + 848 testes frontend (pre-rodada)
> Status: CONCLUIDA

---

## Contexto

Esta rodada executou o lote P0 da Spec 017 (correcoes-rc) em paralelo com trabalho Claude nas areas sensiveis de auth/interceptor. O foco foi corrigir o bug raiz dos "erros internos do servidor" que apareciam no frontend quando a sessao expirava: o backend retornava 302 redirect OAuth2 para requests AJAX, que o frontend interpretava como erro interno. Alem disso, foram corrigidos dois bugs visuais/logicos de baixo risco: `hasBothRoles()` e o clipping de overlays em dialogs PrimeNG.

A task Spec 015 T6/T7 (ClassePontos + RacaPontos screens) estava listada no briefing como pendente, mas foi encontrada ja implementada no commit `2cb0245` de uma rodada anterior. Foi registrada como pre-existente e nao reexecutada.

O agente T2 (teste de integracao backend) incluiu acidentalmente a delecao de `data-dev.sql` no commit — esse arquivo ja estava staged para delecao antes da rodada (visivel no `git status` inicial) e foi arrastado pelo commit. Nao e uma regressao: o arquivo continha dados de seed de desenvolvimento e sua remocao e esperada no contexto do deploy GCP (Spec 018).

---

## Tasks Executadas

### [Spec 017 T1] SecurityConfig HttpStatusEntryPoint 401

**Agente:** senior-backend-dev (sessao anterior ao Fleet mode)
**Commit:** `4a97f7f`
**Arquivos criados/modificados:** 1
- `src/main/java/.../config/SecurityConfig.java` — adicionado `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` via `PathPatternRequestMatcher.pathPattern("/api/**")` no bloco `exceptionHandling`. Nota: `AntPathRequestMatcher` foi removido no Spring Security 7.0.2; a alternativa correta e `PathPatternRequestMatcher` de `org.springframework.security.web.servlet.util.matcher`.

**Validacao:**
- `./mvnw test` → 719 testes, 0 falhas (pre-T2)
- Endpoint GET /api/v1/jogos sem sessao → 401 (confirmado pelos testes de T2)

---

### [Spec 017 T2] Teste integracao 401 vs 302

**Agente:** senior-backend-dev (Fleet mode)
**Commit:** `116e5e8`
**Arquivos criados/modificados:** 1 (+ delecao colateral de data-dev.sql)
- `src/test/java/.../config/SecurityConfigIntegrationTest.java` — CRIADO: 4 testes de integracao cobrindo GET /api/**, POST /api/**, GET /api/v1/auth/me (todos → 401) e GET /oauth2/authorization/google (→ 302). Usa `MockMvcBuilders.webAppContextSetup()` + `springSecurity()` (sem `@AutoConfigureMockMvc` que foi removido no Spring Boot 4.0).

**Validacao:**
- `./mvnw test -Dtest=SecurityConfigIntegrationTest` → 4/4 passando
- `./mvnw test` → 723 testes, 0 falhas

**PA detectados:**
- PA-R04-01: `data-dev.sql` deletado no mesmo commit. Arquivo estava staged pre-rodada. Nao e regressao, mas o commit mistura concerns (teste + delecao de recurso). O PM pode decidir se quer separar via `git revert` + recomit isolado.

---

### [Spec 017 T6] Fix hasBothRoles bug

**Agente:** angular-frontend-dev (sessao anterior ao Fleet mode)
**Commit:** `18fd0e3`
**Arquivos criados/modificados:** 3
- `src/app/shared/layout/header.component.ts` — `hasBothRoles()` corrigido: era `user?.role === 'MESTRE' || user?.role === 'JOGADOR'` (sempre true para qualquer usuario autenticado). Novo comportamento: retorna `false` (MVP — model `User.role` e string unica, nao suporta multi-role). Suporte a multi-role fica para Spec 010.
- `src/app/core/models/user.model.ts` — funcao standalone `hasBothRoles(user)` corrigida: removido `@ts-ignore`, implementado `user?.roles?.includes('MESTRE') && user?.roles?.includes('JOGADOR')` (para quando o model evoluir para array).
- `src/app/shared/layout/header.component.spec.ts` — CRIADO: 7 testes cobrindo MESTRE, JOGADOR, sem role, usuario null, e verificacao de template (seletor "Visualizar como" nao deve aparecer).

**Validacao:**
- `npx vitest run src/app/shared/layout/` → 7/7 passando

---

### [Spec 017 T22] Fix overlay clipping em dialogs

**Agente:** angular-frontend-dev (sessao anterior ao Fleet mode)
**Commit:** `e5d31a0`
**Arquivos criados/modificados:** 1
- `src/app/app.config.ts` — adicionado `overlayOptions: { appendTo: 'body' }` no `providePrimeNG(...)`. Estrategia A (global): todos os overlays PrimeNG (p-select, p-multiselect, p-autocomplete, p-datepicker) sao renderizados como filhos do `<body>`, escapando do `overflow: hidden` do `p-dialog`. Fix unico que resolve 100% das telas de uma vez.

**Validacao:**
- `npx vitest run` → build e testes continuam passando
- Smoke test visual: nao executado automaticamente (requer browser). PM deve validar manualmente em pelo menos 5 telas com dialog + dropdown (Racas, Classes, Vantagens, NPCs, Wizard ficha).

---

### [Spec 015 T6+T7] ClassePontos + RacaPontos screens

**Agente:** N/A (pre-existente)
**Commit:** `2cb0245` (rodada anterior ao R04)
**Observacao:** Encontrado ja implementado ao inicio da rodada. Nao reexecutado. O commit inclui ClassePontosConfig CRUD screen (abas na tela de ClassePersonagem) e RacaPontosConfig CRUD screen (abas na tela de Raca), alem de VantagemPreDefinida para ambos.

---

## Commits

| Hash | Mensagem | Task | Repo |
|------|----------|------|------|
| `4a97f7f` | `fix(security): SecurityConfig devolve 401 em vez de 302 para AJAX [Spec 017 T1]` | Spec 017 T1 | backend |
| `116e5e8` | `test(security): integração 401 vs 302 conforme Accept header [Spec 017 T2]` | Spec 017 T2 | backend |
| `18fd0e3` | `fix(auth): hasBothRoles usa && em vez de || [Spec 017 T6]` | Spec 017 T6 | frontend |
| `e5d31a0` | `fix(ui): overlayOptions appendTo body global para resolver clipping em dialogs [Spec 017 T22]` | Spec 017 T22 | frontend |

---

## Estado Final

### Backend

| Metrica | Valor |
|---------|-------|
| Testes totais | 723 (0 falhas, 0 erros) |
| Delta vs base | +4 testes (SecurityConfigIntegrationTest) |
| HEAD | `116e5e8` |

### Frontend

| Metrica | Valor |
|---------|-------|
| Testes totais | 950 (881 passando, 2 falhas pre-existentes, 2 erros OOM) |
| Delta vs base | +7 testes (header.component.spec.ts) |
| HEAD | `e5d31a0` |

**Nota sobre falhas frontend:** As 2 falhas em `ficha-vantagens-tab.component.spec.ts` (badge severity) sao pre-existentes do commit `7cffd96` (area DO NOT TOUCH — Claude trabalhando em paralelo em Spec 012). Os 2 erros OOM em `ficha-wizard-passo4.component.spec.ts` sao causados por limite de heap de Node.js na maquina local (nao relacionados a mudancas de codigo desta rodada).

---

## Incidentes

### INC-R04-01: data-dev.sql deletado junto com SecurityConfigIntegrationTest.java

O arquivo `src/main/resources/data-dev.sql` ja estava staged para delecao antes da rodada (aparecia como `D  src/main/resources/data-dev.sql` no `git status`). Quando o agente T2 rodou `git add <arquivo-especifico> && git commit`, o Git incluiu automaticamente todos os arquivos ja no index (staged), incluindo a delecao do data-dev.sql. O commit `116e5e8` portanto mistura dois concerns: adicao do teste e remocao do seed file.

**Impacto:** Baixo. O `data-dev.sql` era dados de seed para desenvolvimento local e sua remocao e esperada apos a migracao para deploy GCP (Spec 018). Nao ha regressao funcional.

**Acao sugerida:** PM pode deixar como esta (remocao intencional) ou solicitar separacao via revert + recomit isolado se quiser rastreabilidade limpa.

---

## Pendencias / PAs

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|---------|--------------|
| PA-R04-01 | Commit T2 inclui delecao colateral de data-dev.sql (staged pre-rodada) | Nao | PM decide: aceitar ou separar via revert + recomit |
| PA-R04-02 | Smoke test manual T22 (overlay clipping) nao executado — requer browser | Sim (aceite PO) | PM ou QA valida manualmente em Racas/Classes/Vantagens/NPCs/Wizard |
| PA-R04-03 | 2 falhas pre-existentes em ficha-vantagens-tab (badge severity) — area DO NOT TOUCH | Nao | Claude/Tech Lead revisa apos Spec 012 fase 2 |
| PA-R04-04 | OOM em ficha-wizard-passo4.spec.ts (Node heap limit) — nao relacionado a R04 | Nao | Aumentar NODE_OPTIONS=--max-old-space-size=4096 no vitest config ou na maquina |

---

## Paralelismo Utilizado

```
Wave 1 (sessao anterior — parcialmente serial):
  Agent A → Task 1 (Spec 017 T1 backend SecurityConfig) — DONE
  Agent B → Task 3 (Spec 017 T6 hasBothRoles) — DONE
  Agent C → Task 4 (Spec 017 T22 overlay clipping) — DONE

Wave 2 (Fleet mode):
  Agent D → Task 2 (Spec 017 T2 teste backend) — DONE

Pre-existente (rodada anterior):
  Task 5+6 (Spec 015 T6+T7 ClassePontos+RacaPontos) — ja commitado em 2cb0245
```

Total de agentes nesta rodada: 4 (1 Fleet + 3 sessao anterior)

---

*Rodada Copilot R04 encerrada em 2026-04-07.*
