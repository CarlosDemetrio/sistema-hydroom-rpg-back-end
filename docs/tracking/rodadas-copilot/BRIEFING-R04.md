# Briefing Copilot R04 — Lote 1 paralelo a Spec 017 P0 sensível

> Criado: 2026-04-07 pelo PM
> Para execução: GitHub Copilot CLI (fleet mode)
> Estimativa: ~9h em ~5 agentes (waves paralelas)
> Pré-requisito: branch `main` limpa, base **719 testes backend** + **848 testes frontend**, 0 falhas
> Estratégia: enquanto Claude trabalha em Spec 017 T3+T4+T5+T7 (sensível, fluxo auth) e Spec 012 fase 2 (level up em ficha-detail), Copilot executa este lote sem tocar nas mesmas áreas

---

## Regras de ouro (NÃO QUEBRAR)

1. **NÃO TOCAR** em nenhum desses arquivos/áreas (Claude está mexendo neles em paralelo):
   - `**/ficha-detail.component.*`
   - `**/layout/**` (header, sidebar, app-layout)
   - `**/auth.guard.ts`, `**/error.interceptor.ts`, `**/auth/**` (fluxo OAuth)
   - `**/app.routes.ts` (rotas raiz) — adicionar rotas via feature module próprio é OK
   - `**/level-up*`, `**/ficha-vantagens-tab*`, `**/ficha-aptidoes-tab*`

2. **NÃO usar** `git add .` ou `git add -A` — sempre `git add <arquivos-específicos>`. Lição do INC-R02-01 (commit acidental T4+T1).

3. **NÃO commitar** `.claude/`, `.copilot/`, ou qualquer arquivo de memória de agente. Já está no `.gitignore`, mas conferir antes de commit.

4. **NÃO atualizar** specs (`docs/specs/**`) — responsabilidade do BA. Apenas referenciar nos commits e no relatório final.

5. **NÃO commitar** com `--no-verify`. Hooks devem passar.

6. **NÃO usar** "Co-Authored-By" no commit message (regra do projeto, vale para todos os agentes).

7. **NÃO atualizar** `docs/HANDOFF-SESSAO.md`, `docs/SPRINT-ATUAL.md`, `MEMORY.md`, `docs/tracking/rodadas/RODADA-N.md` — esses arquivos são do PM. Copilot só atualiza o relatório de rodada (item "Como registrar" abaixo).

---

## Tasks do Lote 1 (6 tasks)

> Agentes sugeridos: `senior-backend-dev` (backend), `angular-frontend-dev` (frontend).
> Cada task = 1 agente (regra do projeto: máx 1 task por agente).

### Task 1 — Spec 017 T1 [BE] SecurityConfig HttpStatusEntryPoint 401

| Item | Valor |
|------|-------|
| Tipo | Backend |
| Spec/Task | `docs/specs/017-correcoes-rc/tasks/P0-T1-*` (ler antes) |
| Agente | `senior-backend-dev` |
| Estimativa | 1h |

**Objetivo:** Configurar `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` no `SecurityConfig.java` para que requests AJAX recebam 401 em vez de 302 (redirect OAuth2) quando a sessão expira. Esse é o bug raiz dos "erros internos do servidor" que aparecem aleatoriamente no frontend.

**Arquivo principal:** `src/main/java/.../config/SecurityConfig.java`

**Critério de aceite:**
- `./mvnw test` continua verde (719+ testes)
- Endpoint protegido devolve 401 quando chamado sem sessão (não 302)
- OAuth2 redirect ainda funciona para requisições de browser (não-AJAX)

**Commit message sugerido:**
```
fix(security): SecurityConfig devolve 401 em vez de 302 para AJAX [Spec 017 T1]
```

---

### Task 2 — Spec 017 T2 [BE] Teste integração 401 vs 302

| Item | Valor |
|------|-------|
| Tipo | Backend |
| Spec/Task | `docs/specs/017-correcoes-rc/tasks/P0-T2-*` |
| Agente | `senior-backend-dev` |
| Estimativa | 1h |
| Depende de | T1 |

**Objetivo:** Teste de integração validando que endpoint protegido devolve 401 (sem sessão) e 302 (browser request com Accept: text/html).

**Arquivo principal:** novo, em `src/test/java/.../config/SecurityConfigIntegrationTest.java`

**Critério de aceite:**
- Cenário 1: GET `/api/v1/jogos` sem sessão + `Accept: application/json` → 401
- Cenário 2: GET `/api/v1/jogos` sem sessão + `Accept: text/html` → 302 redirect OAuth2
- `./mvnw test` continua verde

**Commit message:**
```
test(security): integração 401 vs 302 conforme Accept header [Spec 017 T2]
```

---

### Task 3 — Spec 017 T6 [FE] Fix hasBothRoles bug

| Item | Valor |
|------|-------|
| Tipo | Frontend |
| Spec/Task | `docs/specs/017-correcoes-rc/tasks/P0-T6-*` |
| Agente | `angular-frontend-dev` |
| Estimativa | 30min |

**Objetivo:** Corrigir lógica em `hasBothRoles()` que usa `||` em vez de `&&`. Bug pequeno mas afeta navegação Mestre+Jogador.

**Arquivo:** procurar por `hasBothRoles` (provavelmente em `core/services/auth*` ou `shared/`).

**Critério de aceite:**
- Função retorna `true` apenas quando o usuário tem AMBAS as roles
- Adicionar/atualizar teste unitário cobrindo o bug
- `npx vitest run` continua verde (848+ testes)

**Commit message:**
```
fix(auth): hasBothRoles usa && em vez de || [Spec 017 T6]
```

---

### Task 4 — Spec 017 T22 [FE] Overlay clipping global em dialogs

| Item | Valor |
|------|-------|
| Tipo | Frontend |
| Spec/Task | `docs/specs/017-correcoes-rc/tasks/P0-T22-fix-overlay-clipping-dialogs.md` |
| Agente | `angular-frontend-dev` |
| Estimativa | 2h |

**Objetivo:** Configurar `overlayOptions: { appendTo: 'body' }` global no `providePrimeNG` do `app.config.ts`. Bug atual: `p-select`, `p-multiselect`, `p-autocomplete`, `p-datepicker` ficam clipados dentro de `p-dialog` por causa de `overflow: hidden`. Afeta TODAS as telas de formulário do sistema.

**Arquivo principal:** `src/app/app.config.ts`

**Critério de aceite:**
- Configuração global aplicada
- Smoke test manual em pelo menos 5 telas com dialog que tem dropdown longo (Configurações de Atributos, Vantagens, NPCs, Wizard de Ficha, Jogo Form)
- Testes existentes continuam passando
- Documentar no relatório quais telas foram validadas

**Commit message:**
```
fix(ui): overlayOptions appendTo body global para resolver clipping em dialogs [Spec 017 T22]
```

---

### Task 5 — Spec 015 T6 [FE] ClassePontosConfig screen

| Item | Valor |
|------|-------|
| Tipo | Frontend |
| Spec/Task | `docs/specs/015-config-pontos-classe-raca/tasks/P2-T6-ui-classe-pontos.md` |
| Agente | `angular-frontend-dev` |
| Estimativa | 3h |

**Objetivo:** Tela CRUD para ClassePontosConfig (pontos extras de atributo concedidos por classe + vantagens pré-definidas opcionais). Backend já está pronto (Spec 015 T1+T2 OK).

**Pasta destino:** `src/app/features/configuracoes/classe-pontos/` (criar nova feature)

**Permitido tocar:** rotas `/configuracoes/classes-pontos/*` adicionadas via `app.routes.ts` ou via feature route próprio. Se tocar `app.routes.ts`, **adicionar apenas no final**, NÃO modificar rotas existentes.

**Critério de aceite:**
- Componente standalone com Signals
- DataTable PrimeNG com CRUD
- Form de criação/edição com validação
- Testes Vitest cobrindo componente + service (`@testing-library/angular`)
- `npx vitest run` continua verde

**Commit message:**
```
feat(config): ClassePontosConfig CRUD screen [Spec 015 T6]
```

---

### Task 6 — Spec 015 T7 [FE] RacaPontosConfig screen

| Item | Valor |
|------|-------|
| Tipo | Frontend |
| Spec/Task | `docs/specs/015-config-pontos-classe-raca/tasks/P2-T7-ui-raca-pontos.md` |
| Agente | `angular-frontend-dev` |
| Estimativa | 3h |
| Depende de | T5 (mesmos padrões) |

**Objetivo:** Tela CRUD para RacaPontosConfig. Espelho de T5, mas para Raça.

**Pasta destino:** `src/app/features/configuracoes/raca-pontos/`

**Critério de aceite:** mesmo de T5.

**Commit message:**
```
feat(config): RacaPontosConfig CRUD screen [Spec 015 T7]
```

---

## Sequenciamento sugerido (waves)

```
Wave 1 (paralelo, 4 agentes simultâneos):
  Agent A → Task 1 (Spec 017 T1 backend)
  Agent B → Task 3 (Spec 017 T6 hasBothRoles)
  Agent C → Task 4 (Spec 017 T22 overlay)
  Agent D → Task 5 (Spec 015 T6 ClassePontos)

Wave 2 (após Wave 1):
  Agent E → Task 2 (Spec 017 T2 teste, depende de T1)
  Agent F → Task 6 (Spec 015 T7 RacaPontos, espelho de T5)
```

Total: 6 agentes / ~9h reais paralelizados em 2 waves.

---

## Como registrar (saída para PM)

Ao final da rodada, criar **UM** arquivo de relatório no mesmo padrão de R01/R02/R03:

**Caminho:** `docs/tracking/rodadas-copilot/COPILOT-R04.md`

**Template obrigatório:**

```markdown
# Copilot R04 — <título resumido das specs trabalhadas>

> Data: 2026-04-07
> Branch: `main`
> Base: <N> testes backend + <M> testes frontend (pre-rodada)
> Status: CONCLUIDA | PARCIAL | ABORTADA

---

## Contexto

<2-3 parágrafos: por que esse lote, qual o escopo, quem coordenou>

---

## Tasks Executadas

### [Spec X TY] Título curto

**Agente:** <nome do agente>
**Commit:** `<hash>`
**Arquivos criados/modificados:** N

<descrição do que foi feito, decisões técnicas importantes, gotchas>

**Validação:**
- `./mvnw test` → <resultado>
- `npx vitest run` → <resultado>

**PA detectados (se houver):**
- PA-XXX: <descrição>

---

(repetir para cada task)

---

## Commits

| Hash | Mensagem | Task |
|------|----------|------|
| `<hash>` | `<mensagem>` | <Spec X TY> |

---

## Estado Final

### Backend
| Métrica | Valor |
|---------|-------|
| Testes totais | <N> (0 falhas, <K> skipped) |
| Delta vs base | +<N> testes |
| HEAD | `<hash>` |

### Frontend
| Métrica | Valor |
|---------|-------|
| Testes totais | <M> |
| Delta vs base | +<M> testes |
| HEAD | `<hash>` |

---

## Incidentes

(opcional — se houve race condition, commit acidental, etc., documentar como INC-R04-XX)

---

## Pendências / PAs

| ID | Descrição | Bloqueia | Próxima ação |
|----|-----------|---------|--------------|
| PA-R04-XX | <texto> | <sim/não> | <texto> |

---

## Paralelismo Utilizado

```
Wave 1 (paralelo):
  Agent A → ...
  Agent B → ...

Wave 2 (após X):
  Agent E → ...
```

Total de agentes: <N>

---

*Rodada Copilot R04 encerrada em 2026-04-07.*
```

**Importante:**
- Incluir os hashes reais dos commits
- Se algum task ficar parcial ou abortado, documentar o motivo
- Se descobrir TODOs, gotchas, ou áreas que precisam de atenção do Tech Lead, incluir como `PA-R04-XX`
- Não mexer em `HANDOFF-SESSAO.md`, `SPRINT-ATUAL.md`, `MEMORY.md` ou `docs/specs/**` — o PM consolidará essas atualizações depois de ler este relatório

---

## Verificação pré-merge (checklist Copilot)

Antes de declarar a rodada concluída, validar:

- [ ] Todos os 6 commits estão no branch `main` (ou em branch dedicado, dependendo da política)
- [ ] `./mvnw test` passa (≥ 719 testes backend + qualquer teste novo)
- [ ] `npx vitest run` passa (≥ 848 testes frontend + qualquer teste novo)
- [ ] Nenhum arquivo de `docs/specs/**` foi modificado
- [ ] Nenhum arquivo de `docs/HANDOFF-SESSAO.md`, `docs/SPRINT-ATUAL.md`, `MEMORY.md` foi modificado
- [ ] Nenhum arquivo `.claude/`, `.copilot/`, ou memória de agente foi commitado
- [ ] Cada commit tem 1 e somente 1 task referenciada (sem mistura como INC-R02-01)
- [ ] `COPILOT-R04.md` criado em `docs/tracking/rodadas-copilot/` no formato acima

---

## Em caso de bloqueio

Se algum task encontrar bloqueio (decisão de design, dependência inesperada, conflito), **NÃO improvisar**. Documentar como `PA-R04-XX` no relatório, marcar a task como PARCIAL ou ABORTADA, e seguir para a próxima.

O PM revisará bloqueios depois e decidirá se aciona Claude (BA/Tech Lead) ou volta com plano novo.
