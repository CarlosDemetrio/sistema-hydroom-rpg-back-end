# Spec 017 — Plano de Execucao

> Spec: `017-correcoes-rc`
> Data do plano: 2026-04-07 (atualizado 2026-04-07 com inclusao de T22)
> Estimativa total: ~32-38h (16 tasks ativas)

---

## 1. Estrategia Geral

O plano divide as correcoes em **4 fases priorizadas (P0-P3)**. Apenas o **P0 e bloqueante para a RC**. As fases P1-P3 podem ser executadas pre ou pos-RC dependendo da janela disponivel apos Spec 012 fase 2 e Spec 015 T6/T7.

A motivacao da divisao:
- **P0** — bugs CRITICOS que afetam o sintoma principal reportado pelo PO ("erro interno quando expira sessao")
- **P1** — melhorias de navegacao com alto impacto percebido (botao Voltar)
- **P2** — qualidade de vida (testes, documentacao, refactor de paths obvios)
- **P3** — itens grandes que exigem decisao de produto ou tem baixa urgencia

---

## 2. Decisao de Priorizacao Pre-RC vs Pos-RC

### PRE-RC BLOQUEANTE (P0) — 8 tasks, ~10h
Justificativa: o sintoma "erro interno quando expira sessao" e bloqueante para homologacao. Sem o fix backend, o usuario tem experiencia ruim em qualquer cenario de timeout. Os bugs de logica (`hasBothRoles`, `verFicha`) afetam o Mestre em fluxos basicos. **T22 (overlay clipping)** foi adicionada como P0 apos achado tardio do PO porque o bug bloqueia a interacao com QUALQUER select dentro de qualquer dialog do sistema (ver secao "Achados Tardios").

| Task | Tipo | Estimativa |
|------|------|-----------|
| T1 (BE) — `SecurityConfig` HttpStatusEntryPoint | Backend | 1h |
| T2 (BE) — Teste integracao 401 | Backend | 1h |
| T3 (FE) — `SKIP_ERROR_INTERCEPTOR` token | Frontend | 1h |
| T4 (FE) — Refatorar `error.interceptor` | Frontend | 2h |
| T5 (FE) — `auth.guard` salvar `state.url` + `getUserInfo` skip | Frontend | 1h |
| T6 (FE) — Bug `hasBothRoles()` (`||` → `&&`) | Frontend | 0.5h |
| T7 (FE) — Bug `verFicha()` Mestre + nova rota | Frontend | 1.5h |
| T22 (FE) — Fix overlay clipping em dialogs (`appendTo: 'body'`) | Frontend | 2h |

**Total P0: ~10h** — pode ser executado em 2-3 rodadas (2 backend + multiplas frontend em paralelo).

### PRE-RC DESEJAVEL (P1) — 5 tasks, ~10h
Justificativa: alto impacto percebido pelo PO ("nao consigo voltar das telas"). Se houver janela apos P0 e antes da entrega da RC, executar.

| Task | Tipo | Estimativa |
|------|------|-----------|
| T8 (FE) — Criar `PageHeaderComponent` | Frontend | 2h |
| T9 (FE) — Aplicar PageHeader em telas Mestre (config, jogo-detail, jogo-form, npcs) | Frontend | 2h |
| T10 (FE) — Aplicar PageHeader em telas Jogador/wizard (fichas-list, ficha-detail, ficha-wizard, jogos-disponiveis) | Frontend | 2h |
| T11 (FE) — `jogo-form` adicionar `<p-toast>` ou usar `ToastService` | Frontend | 1h |
| T12 (FE) — Remover double-toast em 19 componentes | Frontend | 3h |

**Total P1: ~10h** — pode ser executado em 2-3 rodadas.

### POS-RC (P2) — 2 tasks, ~6h
Justificativa: qualidade tecnica e divida documental. Nao impacta o usuario final diretamente.

| Task | Tipo | Estimativa |
|------|------|-----------|
| T13 (FE) — `oauth-callback` remover `setTimeout(1000)` | Frontend | 1h |
| T14 (FE) — Testes de interceptor (cobrir 401, 403, 500, 0, SKIP) | Frontend | 2h |
| T-DOC1 — Atualizar `CLAUDE.md` (raiz + frontend) com contrato erros HTTP | Doc | 1h |

**Total P2: ~4h**

### POS-RC ADIADO (P3) — itens grandes, ~12h+
Justificativa: itens que exigem decisao de produto, refactor estrutural ou tem baixa urgencia. Podem virar specs proprias se ficarem grandes demais.

| Task | Tipo | Estimativa | Razao do adiamento |
|------|------|-----------|--------------------|
| T15 (FE) — Reativar `SidebarComponent` no `MainLayoutComponent` | Frontend | 3h | Decisao de produto: sidebar vs PageHeader-only |
| T16 (FE) — Seletor de jogo no `HeaderComponent` | Frontend | 4h | Componente novo, integracao com `CurrentGameService`, persistencia |
| T17 (FE) — Refactor/remover `ErrorHandlerService` | Frontend | 2h | Wrapper raso, sem urgencia |
| T18 (FE) — Botoes Exportar/Importar `config-layout` | Frontend | 3h | Depende de discussao do contrato CSV/JSON com PO |
| T19 (FE) — Renomear `drawerVisible` → `dialogVisible` (13 configs + npcs) | Frontend | 2h | Divida nominal sem impacto runtime |
| T20 (FE) — `racas-config` loading states em chamadas auxiliares | Frontend | 1h | Bug menor, raramente reproduz |
| T21 (FE) — Auditar `confirmDelete` sem dialog em base-config | Frontend | 1h | Verificacao + fix se necessario |

**Total P3: ~16h** — fica em backlog pos-homologacao, sem ordem rigida.

---

## 3. Sobreposicao com Specs Existentes

### Spec 015 T5 — DefaultProvider (BUG-DC-06..08)
A auditoria UX P3 menciona bugs em `DefaultGameConfigProviderImpl`:
- `MembroCorpoConfig.Cabeca = 0.25` (deveria ser 0.75)
- Indoles com 9 alinhamentos D&D em vez dos 3 do Klayrah
- Presencas com escala de intensidade em vez de postura etica

**Esses bugs JA estao na Spec 015 T5** e nao sao reduplicados nesta spec. Quando Spec 015 T5 for executada (atualmente CONCLUIDA segundo memoria), o problema sera resolvido. Esta spec apenas referencia.

### Spec 012 fase 2 + Spec 015 T6/T7
Estas specs sao a **primeira parte da RC** ja decidida pelo PO. Spec 017 P0 deve ser executada em **rodadas separadas** para nao competir por agentes/arquivos:

| Spec | Foco | Arquivos primarios |
|------|------|-------------------|
| 012 fase 2 | Modal LevelUp + paineis XP | `features/jogador/...`, `features/mestre/painel-xp` |
| 015 T6/T7 | UI ClassePontos/RacaPontos | `features/mestre/pages/classes-config`, `racas-config` |
| 017 P0 (FE) | `error.interceptor`, `auth.guard`, `header` | `interceptors/`, `guards/`, `shared/components/header` |
| 017 P0 (BE) | `SecurityConfig` | `config/SecurityConfig.java` |

Conflitos potenciais minimos. **Plano Anti-Conflito: Spec 017 nao toca `features/jogador/**` nem `features/mestre/pages/classes-config|racas-config/**` em P0.** Em P1, ao adicionar PageHeader em `fichas-list`, coordenar com agente de Spec 012.

---

## 4. Sequenciamento das Rodadas (Sugestao)

### Rodada 14 (proxima — atual prioridade do PO)
**Foco**: Spec 012 fase 2 T6-T8 + Spec 017 P0 backend + Spec 017 P0 logico-frontend
- Spec 012 T6 (modelo TS FichaResumo)
- Spec 012 T7 (painel XP do Mestre)
- Spec 017 T1+T2 (backend SecurityConfig + teste)
- Spec 017 T6 (`hasBothRoles` fix — independente)
- Spec 017 T7 (`verFicha` Mestre fix — independente)

### Rodada 15
**Foco**: Spec 012 fase 2 T9-T11 + Spec 017 P0 interceptor refactor
- Spec 012 T9-T11 (level up dialog steps)
- Spec 017 T3 (`SKIP_ERROR_INTERCEPTOR` token)
- Spec 017 T4 (refatorar `error.interceptor`)
- Spec 017 T5 (`auth.guard` + `getUserInfo`)

### Rodada 16
**Foco**: Spec 015 T6/T7 + Spec 017 P1 PageHeader
- Spec 015 T6 (UI ClassePontos)
- Spec 015 T7 (UI RacaPontos)
- Spec 017 T8 (criar `PageHeaderComponent`)
- Spec 017 T9 (aplicar em telas Mestre)

### Rodada 17 (RC pronta apos esta)
**Foco**: Spec 017 P1 finalizacao
- Spec 017 T10 (aplicar PageHeader em telas Jogador)
- Spec 017 T11 (`jogo-form` toast)
- Spec 017 T12 (remover double-toast 19 componentes)

### Pos-homologacao
- Spec 017 P2 (T13, T14, T-DOC1)
- Spec 011 → Spec 016 → Spec 014 → Spec 013 → Spec 010
- Spec 017 P3 (sem ordem rigida)

---

## 5. Plano Anti-Conflito

Para evitar conflitos de merge entre agentes em rodadas paralelas:

### Backend
- **Spec 017 T1+T2** modificam APENAS `config/SecurityConfig.java` e adicionam um novo arquivo de teste em `config/SecurityConfigIntegrationTest.java`. Sem conflito com nenhuma outra spec.

### Frontend
- **Spec 017 P0**: arquivos exclusivos
  - `core/tokens/skip-error.token.ts` (novo)
  - `interceptors/error.interceptor.ts`
  - `services/auth.service.ts` (apenas metodo `getUserInfo`)
  - `guards/auth.guard.ts`
  - `shared/components/header/header.component.ts` (apenas metodo `hasBothRoles`)
  - `features/mestre/pages/jogo-detail/jogo-detail.component.ts` (apenas metodo `verFicha`)
  - `app.routes.ts` (adicionar rota `/mestre/fichas/:id` se for a estrategia escolhida)

- **Spec 017 P1**: arquivos exclusivos
  - `shared/components/page-header/page-header.component.ts` (novo)
  - `features/mestre/pages/config/config-layout.component.ts`
  - `features/mestre/pages/jogo-detail/jogo-detail.component.ts`
  - `features/mestre/pages/jogo-form/jogo-form.component.ts`
  - `features/mestre/pages/npcs/npcs.component.ts`
  - `features/jogador/pages/fichas-list/fichas-list.component.ts`
  - `features/jogador/pages/ficha-detail/ficha-detail.component.ts`
  - `features/wizard/ficha-wizard.component.ts`
  - 19 componentes para limpeza de double-toast (lista no T12)

### Coordenacao com Spec 012 fase 2 e Spec 015 T6/T7
- Spec 012 toca `features/jogador/painel-xp/`, `features/mestre/painel-xp/` e `features/jogador/pages/ficha-detail/` (LevelUpDialog) — **conflito potencial em `ficha-detail.component.ts` se Spec 017 T10 rodar simultaneamente**. Mitigacao: rodar Spec 012 T11 ANTES de Spec 017 T10.
- Spec 015 toca `features/mestre/pages/classes-config/` e `racas-config/` — **sem conflito** com Spec 017.

---

## 6. Estrategia de Testes

### Backend (T2)
- Adicionar `SecurityConfigIntegrationTest.java` em `src/test/java/.../config/`
- Cobrir: GET `/api/v1/jogos` sem sessao retorna 401 (nao 302)
- GET `/oauth2/authorization/google` ainda redireciona normalmente
- Validar Content-Type `application/json` no 401

### Frontend (T14)
- Cobertura do `error.interceptor.ts`:
  - 401 → navega para `/login`, sem toast `.error()`
  - 403 → navega para `/unauthorized`, sem toast
  - 500 → toast `.error()` chamado
  - status 0 → toast com mensagem "sem conexao"
  - `SKIP_ERROR_INTERCEPTOR=true` → nenhuma chamada ao toast service
- Cobertura do `auth.guard.ts`:
  - sem usuario → salva `state.url` em sessionStorage e navega para `/login`
- Smoke test do `PageHeaderComponent`:
  - renderiza `titulo`
  - renderiza botao quando `backRoute` esta presente
  - clica no botao → chama `router.navigate`

---

## 7. Definition of Done por fase

### P0 done quando
- Backend testes passam (614+ — adicionando 1 novo)
- Frontend testes passam (850+ — adicionando 2 novos minimo)
- Validacao manual do PO: expirar sessao manualmente (limpar cookie) e verificar fluxo limpo
- CA-01, CA-02, CA-04, CA-05, CA-06 verdes

### P1 done quando
- 8 telas tem botao Voltar funcional
- `jogo-form` mostra toasts de sucesso/erro
- Zero `toastService.error()` em `subscribe.error` callbacks de componentes (validar com grep)
- CA-03, CA-07 verdes

### P2 done quando
- `oauth-callback` sem `setTimeout` fixo
- Testes do interceptor cobrindo todos os branches
- `CLAUDE.md` atualizado com secao "Tratamento de erros HTTP"
- CA-08 verde

### P3 done quando
- Decisoes de produto tomadas para sidebar e seletor de jogo
- Tasks executadas individualmente em rodadas pos-homologacao

---

## 8. Pontos Em Aberto

| ID | Pergunta | Bloqueia |
|----|----------|---------|
| PA-017-01 | Para `verFicha()` do Mestre: criar rota nova `/mestre/fichas/:id` (alias do `ficha-detail` com guard MESTRE) OU adaptar a rota `/jogador/fichas/:id` para aceitar `MESTRE \| JOGADOR`? | T7 |
| PA-017-02 | `PageHeaderComponent` deve usar `BreadcrumbModule` do PrimeNG ou apenas botao + titulo + subtitulo simples? Auditor sugere ambos no template, mas breadcrumb adiciona complexidade. | T8 |
| PA-017-03 | A sidebar deve ser reativada (P3 T15)? Se sim, deprecate o `PageHeaderComponent` ou os dois coexistem? | T15, P3 |
| PA-017-04 | Botoes Exportar/Importar: contrato CSV ou JSON? Quais campos exportar? | T18, P3 |

PA-017-01 e PA-017-02 devem ser resolvidos antes do inicio das tasks correspondentes. PA-017-03 e PA-017-04 sao pos-RC.

---

## 9. Resumo

| Fase | Tasks | Estimativa | Status RC |
|------|-------|-----------|-----------|
| P0 | T1-T7 + T22 (8 tasks) | ~10h | **BLOQUEANTE PRE-RC** |
| P1 | T8-T12 (5 tasks) | ~10h | DESEJAVEL PRE-RC |
| P2 | T13, T14, T-DOC1 (3 tasks) | ~4h | POS-RC |
| P3 | T15-T21 (7 tasks) | ~16h | POS-RC adiado |
| **TOTAL** | **23 tasks** | **~40h** | — |

---

## 10. Achados Tardios (apos auditorias iniciais)

### T22 — Overlay clipping em dialogs (incluida em 2026-04-07, mesma sessao)

**Origem:** Bug reportado diretamente pelo PO durante uso da aplicacao, **apos**
a entrega das duas auditorias (`AUDITORIA-ROTAS-ERROS-2026-04-07.md` e
`AUDITORIA-UX-UI-2026-04-07.md`).

**Descricao do PO:**
> "Os selects e coisas que abrem estao sendo cortados e nao listados ate o final."

**Diagnostico:** Problema classico do PrimeNG. Componentes de overlay (`p-select`,
`p-multiselect`, `p-autocomplete`, `p-datepicker`, etc.) renderizados dentro de
um `p-dialog` (que tem `overflow: hidden`) sao clipados pelo limite do dialog.
Em listas longas o usuario nao consegue rolar ate o item desejado, tornando
qualquer formulario nao trivial inutilizavel.

**Causa-raiz suspeita:** Efeito colateral da migracao `p-drawer` → `p-dialog`
realizada pelo PO em sessoes anteriores. O `p-drawer` tinha overflow permissivo;
o `p-dialog` clipa.

**Por que a auditoria UX nao capturou:** A inspecao do `primeng-ux-architect`
foi estatica (estrutura visual das telas), sem abrir nenhum dialog para validar
interacoes de overlay. Lacuna metodologica conhecida — fica como nota para
auditorias futuras: **incluir validacao interativa de overlays dentro de dialogs**.

**Decisao de fase: P0 (bloqueante).** Justificativa:
1. Afeta 100% das telas de formulario (que agora usam dialog)
2. Bloqueia a operacao mais basica do sistema: selecionar valores em dropdowns
3. Arguably mais critico que outros P0 logicos (`hasBothRoles`, `verFicha`)
   porque afeta TODOS os usuarios, nao apenas Mestre em cenarios especificos
4. O fix e simples (uma linha em `app.config.ts`) — custo baixo, impacto alto
5. Sem este fix, a homologacao pelo PO sera frustrante mesmo com tudo o resto
   correto

**Impacto no sequenciamento:** T22 e independente de qualquer outra task da
Spec 017 e pode ser executada em qualquer rodada P0. Recomendacao: incluir na
Rodada 14 (proxima execucao) junto com T6 (`hasBothRoles`) — sao as duas tasks
mais rapidas e independentes do P0, podem ser feitas pelo mesmo agente
(`angular-frontend-dev`) em sequencia.

**Atualizacao do sequenciamento das rodadas:**
- **Rodada 14**: Spec 012 T6-T8 + Spec 017 T1+T2+T6+T7 + **T22**
- (R15-R17 inalteradas)

---

*Plano produzido por: PM/Scrum Master | 2026-04-07 (atualizado mesmo dia com T22)*
