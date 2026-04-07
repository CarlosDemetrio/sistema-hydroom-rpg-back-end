# Spec 017 — Correcoes Pre-RC: Erros HTTP, Navegacao e UX

> Spec: `017-correcoes-rc`
> Origem: Auditorias 2026-04-07 (angular-tech-lead + primeng-ux-architect) + bug PO 2026-04-07 (T22 — overlay clipping)
> Status: PLANEJADO
> Depende de: Spec 015 T5 (DefaultProvider — sobreposicao parcial em P3)
> Bloqueia: RC primeira parte (apenas tasks P0)
> Estimativa total: ~32-40h distribuidas em 4 fases (23 tasks: 16 ativas + 7 backlog)

---

## 1. Visao Geral

Esta spec consolida os achados de duas auditorias independentes realizadas em 2026-04-07:

1. **Auditoria de Rotas e Erros (angular-tech-lead)** — `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` — identifica um BUG CRITICO no backend (SecurityConfig devolvendo 302 em vez de 401) que cascateia para o frontend mostrando "erro interno" quando a sessao expira. Inclui 8 problemas (P1-P8) no fluxo de tratamento de erros, interceptors, guards e double-toast.

2. **Auditoria de UX/UI (primeng-ux-architect)** — `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` — identifica que 8 telas internas nao tem botao "Voltar", o `SidebarComponent` existe mas nao esta renderizado, o seletor de jogo no header esta ausente e ha bugs logicos pontuais (`hasBothRoles`, `verFicha` Mestre→rota Jogador, `jogo-form` sem `<p-toast>`).

A spec organiza as correcoes em 4 fases (P0-P3) priorizadas para que apenas o **P0 seja bloqueante para a RC**. As fases P1-P3 podem ser executadas pre ou pos-RC dependendo da janela disponivel.

---

## 2. Motivacao

### Sintoma reportado pelo PO

> "Quando da erro de nao estar logado, aparece mensagem de erro interno do servidor."

### Causa-raiz identificada (Tech Lead)

`SecurityConfig.java` nao tem `AuthenticationEntryPoint` configurado. Quando a sessao expira, o Spring Security devolve **302 redirect** para `/oauth2/authorization/google` (em vez de **401 JSON**). O browser tenta seguir o redirect cross-origin, falha com `status: 0`, e o `error.interceptor.ts` cai no branch `default:` exibindo "Erro 0:" — uma mensagem genérica de erro interno.

Esse unico bug backend cascateia para 4 sintomas frontend (P1-P4 do relatorio do tech lead). Sem corrigir o backend, qualquer fix isolado no frontend e cosmetico.

### Sintoma reportado pelo PO (UX)

> "Falta botao voltar nas telas, nao consigo sair de varias telas filhas."

### Causa-raiz identificada (UX Architect)

`MainLayoutComponent` so renderiza `<app-header>` + `<router-outlet>`. Nao ha sidebar (existe mas nao foi importado), nao ha breadcrumb global, e nenhuma das 8 telas filhas (`config-layout`, `jogo-detail`, `jogo-form`, `npcs`, `ficha-wizard`, `fichas-list`, `ficha-detail`, `jogos-disponiveis`) tem botao Voltar local. O usuario depende 100% do botao do browser.

---

## 3. Escopo

### IN — Esta spec cobre

- **Backend**: corrigir `SecurityConfig` (HttpStatusEntryPoint para `/api/**`) + teste de integracao
- **Frontend — interceptor/erros**:
  - Criar `HttpContextToken SKIP_ERROR_INTERCEPTOR`
  - Refatorar `error.interceptor.ts` (401/403/0/500 com tratamento distinto)
  - Eliminar double-toast em 19 componentes
  - `auth.guard.ts` salvar `state.url` em `REDIRECT_URL`
  - `oauth-callback.component.ts` remover `setTimeout(1000)`
- **Frontend — navegacao/UX**:
  - Criar `PageHeaderComponent` reutilizavel com botao Voltar
  - Aplicar `PageHeaderComponent` em 8 telas
  - Bug `hasBothRoles()` (`||` → `&&`)
  - Bug `verFicha()` para Mestre (rota inexistente)
  - `jogo-form` adicionar `<p-toast>` ou usar `ToastService`
  - **Fix overlay clipping (T22)**: configurar `overlayOptions: { appendTo: 'body' }` global no `app.config.ts` para resolver clipping de selects/dropdowns/multiselects/datepickers/autocompletes dentro de dialogs (achado tardio do PO, P0 bloqueante)
- **Documentacao**: atualizar `CLAUDE.md` do frontend com contrato de erros HTTP
- **Sobreposicao com Spec 015**: BUG-DC-06..08 (Membro corpo, Indoles, Presencas) ja estao na Spec 015 T5 — esta spec NAO duplica, apenas referencia

### OUT — Fora desta spec

- Reativacao do `SidebarComponent` no `MainLayoutComponent` (movido para pos-RC se nao houver janela em P3)
- Seletor de jogo no header (P3, dependente de decisao de produto)
- Refatoracao/remocao do `ErrorHandlerService` (P3, qualidade)
- Renomear `drawerVisible` → `dialogVisible` (P3, divida nominal sem impacto runtime)
- Renomear `describe('drawer...')` nos `.spec.ts` (P3, divida nominal)
- Ativacao dos botoes Exportar/Importar do `config-layout` (P3, depende de discussao do contrato CSV/JSON)
- Tela de "Diagnostico GameDefault" (fora de escopo, e funcionalidade nova)
- Refactor de `LoadingService` para evitar spinner aderente (P8 do relatorio tech lead — BAIXA prioridade)
- Decisao sobre `/404` ser publico (P8 do relatorio — decisao de produto)

---

## 4. Atores

| Ator | Role | Acao |
|------|------|------|
| Usuario nao autenticado | — | Recebe 401 limpo + redirect para `/login` |
| Mestre | MESTRE | Ve botao Voltar em todas as telas filhas; clica em "ver ficha" sem cair em Unauthorized |
| Jogador | JOGADOR | Ve botao Voltar nas suas telas; nao recebe toast generico em CRUD |
| Backend | — | Devolve 401 JSON puro para `/api/**` nao autenticado |
| Frontend | — | Interceptor unificado; sem double-toast; navegacao previsivel |

---

## 5. Criterios de Aceite Gerais

### CA-01 — Sessao expirada (P0)
- Dado que o usuario tem sessao expirada
- Quando ele realiza qualquer chamada XHR para `/api/**`
- O backend deve devolver `401 Unauthorized` com body JSON `{ "error": "Unauthorized" }`
- O frontend deve detectar 401 e redirecionar para `/login`
- Apos login, o usuario deve voltar a rota originalmente solicitada (nao `/dashboard`)
- NAO deve aparecer toast generico de "erro interno"

### CA-02 — Primeira carga sem sessao (P0)
- Dado que o usuario abre a aplicacao sem sessao
- Quando o `authGuard` chama `/api/v1/auth/me`
- A chamada NAO deve disparar toast (passa por `SKIP_ERROR_INTERCEPTOR`)
- O usuario e redirecionado limpamente para `/login`

### CA-03 — Botao Voltar nas telas (P1)
- Dado que o usuario navega para qualquer das 8 telas listadas
- O usuario deve ver um botao "Voltar" no topo da tela com label e icone `pi pi-arrow-left`
- Clicar no botao volta para a rota pai apropriada (ex: `config-layout` → `/dashboard`, `jogo-detail` → `/mestre/jogos`)

### CA-04 — Sem double-toast em CRUD (P0)
- Dado que o usuario tenta uma operacao de CRUD que falha com 500
- Apenas UM toast de erro deve aparecer (do interceptor)
- Os 19 componentes que tinham `error: () => toastService.error(...)` no subscribe devem ser limpos

### CA-05 — Bug `hasBothRoles` corrigido (P0)
- Apenas usuarios que possuem AS DUAS roles (`MESTRE` E `JOGADOR`) veem o seletor "Visualizar como"
- Usuarios com apenas uma role nao veem o seletor

### CA-06 — Bug `verFicha` Mestre corrigido (P0)
- Quando o Mestre clica em "ver ficha" no `jogo-detail`, a navegacao funciona sem cair em `/unauthorized`
- Solucao: criar rota `/mestre/fichas/:id` (alias do componente `ficha-detail` com guard MESTRE) OU adaptar a rota `/jogador/fichas/:id` para aceitar `MESTRE | JOGADOR`

### CA-07 — `jogo-form` mostra toasts (P1)
- Apos criar/editar um jogo com sucesso, o usuario ve toast de sucesso
- Em caso de erro, ve toast de erro especifico (nao silencioso)

### CA-08 — Documentacao do contrato de erros (P1)
- O `CLAUDE.md` do frontend deve ter uma secao "Tratamento de erros HTTP" explicando:
  - O interceptor e a unica fonte de toasts genericos
  - Componentes nao devem chamar `toastService.error()` em `subscribe.error`
  - Como usar `SKIP_ERROR_INTERCEPTOR` para casos especiais

### CA-09 — Overlays de PrimeNG nao sao mais clipados em dialogs (P0)
- Dado que o usuario abre qualquer formulario dentro de um `p-dialog`
- Quando ele clica em um `p-select`, `p-multiselect`, `p-autocomplete`, `p-datepicker` ou similar
- O painel de overlay deve abrir com altura completa (limite da viewport, nao do dialog)
- Listas longas devem permitir scroll integral ate o ultimo item
- Validacao manual em pelo menos 5 telas representativas (Mestre racas/classes/vantagens, NPCs, Wizard de ficha)
- Selects FORA de dialogs continuam funcionando sem regressao

---

## 6. Restricoes Tecnicas

- **Sem mudanca de versao** de Spring Boot, Angular, PrimeNG ou demais libs
- **Nao tocar** em codigo de Spec 012 fase 2 e Spec 015 T6/T7 em paralelo (rodadas separadas)
- **Backend**: a mudanca em `SecurityConfig` deve afetar APENAS `/api/**` — o fluxo OAuth2 tradicional `/login`, `/oauth2/**` deve continuar funcionando
- **Frontend**: nao quebrar testes existentes (848 passando) — adicionar testes novos para o novo `error.interceptor`
- **PageHeaderComponent**: deve usar standalone components (padrao Angular 21), `@if`, `input.required`, sem `CommonModule`
- **Testes**: todas as alteracoes de frontend exigem cobertura de testes; backend exige teste de integracao MockMvc para o entry point

---

## 7. Sobreposicoes e Referencias

### 7.1 Spec 015 T5 — DefaultProvider
A auditoria de UX (P3) menciona BUG-DC-06..08 (`MembroCorpoConfig.Cabeca = 0.25` errado, Indoles com 9 alinhamentos D&D, Presencas com escala errada). **Esses bugs JA estao na Spec 015 T5** (`P2-T5-corrigir-default-provider.md`). Esta Spec 017 NAO duplica essas correcoes — apenas referencia. Quando Spec 015 T5 for executada, esses problemas serao resolvidos automaticamente.

### 7.2 EXTRA-09 — Seletor de jogo no header
A auditoria identifica que multiplos componentes mostram "Selecione um jogo no cabecalho" mas o `HeaderComponent` nao tem dropdown de selecao. Essa funcionalidade e MAIOR (envolve criar componente, integrar com `CurrentGameService`, persistir selecao em localStorage, atualizar todos os componentes que dependem). Movida para **P3** desta spec; pode ser bumped para spec separada se exigir mais de 4h.

### 7.3 SidebarComponent ativacao
Existe `SidebarComponent` em `shared/layout/sidebar.component.ts` mas nao esta no `MainLayoutComponent`. Decisao tatica: NAO reativar nesta spec. O `PageHeaderComponent` (P1) resolve o problema imediato de "voltar de tela filha". A reativacao do sidebar e decisao de produto e fica para pos-RC.

---

## 8. Riscos

| Risco | Probabilidade | Impacto | Mitigacao |
|-------|--------------|---------|-----------|
| Mudanca em `SecurityConfig` quebra fluxo OAuth2 | MEDIA | ALTO | Restringir entry point a `AntPathRequestMatcher("/api/**")`; testar `/oauth2/authorization/google` ainda funciona |
| Refactor do `error.interceptor` quebra testes existentes | MEDIA | MEDIO | Rodar `npx vitest run src/app/interceptors/` antes do commit; criar testes novos antes do refactor |
| `PageHeaderComponent` quebra layouts existentes | BAIXA | BAIXO | Componente novo, usado opcionalmente em cada tela; rollback isolado por tela |
| `verFicha()` para Mestre cria rota duplicada | BAIXA | MEDIO | Decidir entre criar rota nova OU adaptar guard da rota existente — registrar decisao no plan |
| 19 remocoes de double-toast escondem mensagem util | MEDIA | BAIXO | Em cada componente, verificar se a mensagem do `error: callback` era especifica de contexto; se sim, manter via `SKIP_ERROR_INTERCEPTOR` + toast local |
| T22 — Fix global de `appendTo: 'body'` causa regressao em algum overlay especifico | BAIXA | MEDIO | Validacao manual em selects FORA de dialogs apos mudanca; se houver regressao, aplicar override local com `[appendTo]="null"` no componente afetado |
| T22 — API `overlayOptions` nao existe no PrimeNG 21.1.1 instalado | BAIXA | MEDIO | Consultar `@primeng/mcp` para a API correta; se necessario, fallback para `[appendTo]="'body'"` em cada componente individualmente (~4h em vez de 2h) |

---

## 9. Metricas de Sucesso

- **CA-01 a CA-08** todos verdes
- Backend: testes passam (613+ apos novo teste de integracao)
- Frontend: testes passam (848+ apos novos testes do interceptor)
- Zero `error: () => this.toastService.error(...)` em `subscribe` de componentes (pode ser auditado com `grep`)
- Zero ocorrencia de "Erro 0:" ou "erro interno" em situacoes de sessao expirada (validacao manual pelo PO)

---

## 10. Documentos Relacionados

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` — relatorio tech lead
- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` — relatorio UX architect
- `docs/specs/015-config-pontos-classe-raca/tasks/P2-T5-corrigir-default-provider.md` — sobreposicao P3
- `CLAUDE.md` (raiz) — sera atualizado em T-DOC1
- `ficha-controlador-front-end/CLAUDE.md` — sera atualizado em T-DOC1
