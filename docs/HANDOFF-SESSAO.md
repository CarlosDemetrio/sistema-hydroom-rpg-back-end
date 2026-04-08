# Handoff de Sessao — 2026-04-07 (sessao 16 + Copilot R01-R04 paralelo)

> Branch atual: `main`
> Backend: **723 testes** passando, 0 falhas (0 skipped)
> Frontend: **950 testes** (881 passando + 2 falhas pre-existentes ficha-vantagens-tab + 2 OOM ficha-wizard-passo4)
> Sprint 3: Rodada 14 em andamento; **4 rodadas Copilot executadas em paralelo** (R01-R04)
> Ultima atualizacao: 2026-04-07 [pos Copilot R04 — Spec 017 T1+T2+T6+T22 concluidos; Spec 015 T6+T7 pre-existentes em 2cb0245]

---

## Resumo Executivo

**Sessao 2026-04-07** teve duas frentes em paralelo:

### Frente A — Auditoria + Planejamento (Claude Code, sem codigo de producao)

1. **Auditoria Tech Lead** (`docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md`) identificou 8 problemas (P1-P8) no fluxo de tratamento de erros HTTP do frontend. Causa-raiz principal: `SecurityConfig.java` nao tem `AuthenticationEntryPoint` configurado → backend devolve 302 (redirect OAuth2) em vez de 401 quando sessao expira → frontend mostra "erro interno do servidor" em vez de fluxo limpo de re-login.

2. **Auditoria UX Architect** (`docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`) identificou 9 achados (P1-P3 + 9 extras). Principais: 8 telas sem botao Voltar; `SidebarComponent` existe mas nao foi ativado; bugs pontuais (`hasBothRoles` logica errada, `verFicha` Mestre cai em rota de Jogador, `jogo-form` sem `<p-toast>`); sobreposicao parcial com Spec 015 T5.

3. **Spec 017 criada** (`docs/specs/017-correcoes-rc/`) consolidando ambas as auditorias em 23 tasks (16 ativas + 7 backlog) distribuidas em 4 fases (P0-P3). T22 incluida apos bug PO sobre overlay clipping de selects dentro de dialogs.

### Frente B — Execucao Paralela Copilot (3 rodadas, ZERO conflito de merge)

Enquanto Claude Code trabalhava na Spec 017 (planejamento bloqueante para RC), o GitHub Copilot CLI executou em paralelo 3 rodadas que NAO tocavam em `ficha-detail`, layout/header ou rotas — escolha deliberada para evitar conflitos. Resultado: **+106 testes backend** (613 → 719) sem qualquer regressao.

| Rodada | Foco | Tasks entregues | Commits |
|--------|------|-----------------|---------|
| **COPILOT-R01** | Cobertura + entry de Itens | Spec 014 T1+T5, Spec 016 T1+T2+T6 | `55b8e37`, `b0fad1e`, `cd935f7`, `843ab73`, `cb42a39` (FE) |
| **COPILOT-R02** | Itens + Galeria backend | Spec 016 T3+T4+T7, Spec 011 T0+T1+T2+T4 | `ac41d29`, `8595831`, `a31067d`, `383416b`, `d25c56c`, `23138f9` |
| **COPILOT-R03** | Deploy total | Spec 018 (7 tasks GCP backend), Spec 019 (4 tasks Firebase frontend) | `0dbe923` |
| **COPILOT-R04** | Spec 017 P0 backend+frontend | Spec 017 T1+T2+T6+T22; Spec 015 T6+T7 pre-existentes | `4a97f7f`, `116e5e8`, `18fd0e3`, `e5d31a0` |

**Detalhes completos:** `docs/tracking/rodadas-copilot/COPILOT-R01.md`, `COPILOT-R02.md`, `COPILOT-R03.md`.

**Importante:** As specs 011, 014 e 016 estavam marcadas como STAND-BY pos-RC pelo PO. O Copilot anteciipou parte dessas tasks porque podiam rodar em paralelo seguro com a Spec 017 (zero risco de merge). Spec 018 e 019 sao novas (deploy GCP + Firebase) — substituem o caminho OCI descontinuado.

---

## Priorizacao PRE-RC (bloqueante)

### Spec 017 P0 (8 tasks) — STATUS ATUALIZADO pos Copilot R04
| Task | Descricao | Tipo | Status |
|------|-----------|------|--------|
| T1 | Backend: `SecurityConfig` HttpStatusEntryPoint 401 | BE | **CONCLUIDO R04** (`4a97f7f`) |
| T2 | Backend: Teste integracao 401 vs 302 | BE | **CONCLUIDO R04** (`116e5e8`) |
| T3 | Frontend: Criar `SKIP_ERROR_INTERCEPTOR` token | FE | PENDENTE (Claude R14/R15) |
| T4 | Frontend: Refatorar `error.interceptor` (401/403/0/500) | FE | PENDENTE (Claude R14/R15) |
| T5 | Frontend: `auth.guard` salva `state.url` + `getUserInfo` skip | FE | PENDENTE (Claude R14/R15) |
| T6 | Frontend: Fix bug `hasBothRoles()` (`\|\|` → `&&`) | FE | **CONCLUIDO R04** (`18fd0e3`) |
| T7 | Frontend: Fix bug `verFicha()` Mestre + nova rota | FE | PENDENTE (PA-017-01 decidida: Estrategia A) |
| **T22** | **Fix overlay clipping (`appendTo: 'body'` global)** | **FE** | **CONCLUIDO R04** (`e5d31a0`) |

### Spec 012 fase 2 (T6-T11) — MANTIDA
| Task | Descricao |
|------|-----------|
| T6  | Modelo TypeScript FichaResumo com 3 campos pontosDisponiveis |
| T7  | Painel XP do Mestre + deteccao de level up (badge/toast) |
| T8  | LevelUpDialogComponent + Step 1 (distribuicao de atributos) |
| T9  | Step 2 — distribuicao de aptidoes |
| T10 | Step 3 — vantagens (informativo) + fechar com confirmacao |
| T11 | Conectar saldo de vantagens em FichaVantagensTab |

### Spec 015 T6/T7 — MANTIDA
| Task | Descricao | Depende de |
|------|-----------|-----------|
| T6 | ClassePontosConfig frontend | backend T2 OK |
| T7 | RacaPontosConfig frontend | backend T2 OK |

---

## Avancos Copilot (entregue, mas NAO bloqueante para RC)

### Spec 014 — Cobertura de Testes (parcial — antes 0%, agora 28%)
| Task | Descricao | Status |
|------|-----------|--------|
| T1 | JaCoCo plugin + threshold 50% (0.8.13 — Java 25 compat) | **CONCLUIDO (Copilot R01)** |
| T5 | Vitest coverage v8 + thresholds | **CONCLUIDO (Copilot R01)** |
| T2 | Testes de integracao FichaCalculationService | PENDENTE (pos-RC) |
| T3 | Testes FormulaEvaluator + GameConfigInitializer | PENDENTE (pos-RC) |
| T4 | Testes DefaultGameConfigProviderImpl | PENDENTE (pos-RC) |
| T6 | Frontend tests faltantes | PENDENTE (pos-RC) |

### Spec 016 — Sistema de Itens (backend ~85% — falta T5 calculo + frontend)
| Task | Descricao | Status |
|------|-----------|--------|
| T1 | RaridadeItemConfig + TipoItemConfig CRUDs | **CONCLUIDO (Copilot R01)** |
| T2 | ItemConfig + ItemEfeito + ItemRequisito | **CONCLUIDO (Copilot R01)** |
| T6 | Default dataset 40 itens SRD | **CONCLUIDO (Copilot R01)** |
| T3 | ClasseEquipamentoInicial sub-recurso | **CONCLUIDO (Copilot R02)** |
| T4 | FichaItem inventario + 7 endpoints | **CONCLUIDO (Copilot R02)** |
| T7 | Testes integracao backend (Raridade, Tipo, Item, ClasseEquip) | **CONCLUIDO (Copilot R02)** |
| **T5** | **FichaCalculationService Passo 6 (ItemEfeito de itens equipados)** | **PENDENTE (PA-R02-01 — bloqueada)** |
| T8-T11 | Frontend (UI Raridade/Tipo, Catalogo, ClasseEquip, Inventario) | PENDENTE (pos-RC) |

### Spec 011 — Galeria + Anotacoes (backend ~50% — falta T3 service Cloudinary)
| Task | Descricao | Status |
|------|-----------|--------|
| T0 | AnotacaoPasta entity + CRUD (hierarquia 3 niveis) | **CONCLUIDO (Copilot R02)** |
| T1 | PUT /api/v1/anotacoes/{id} + pastaPaiId + visivelParaTodos | **CONCLUIDO (Copilot R02)** |
| T2 | FichaImagem entity + repository + Cloudinary v2.3.0 dep | **CONCLUIDO (Copilot R02)** |
| T4 | Testes integracao AnotacaoPasta + skeleton FichaImagem (@Disabled) | **CONCLUIDO (Copilot R02)** |
| **T3** | **FichaImagemService + Controller (upload Cloudinary)** | **PENDENTE (PA-R02-02 — bloqueada, aguarda security review)** |
| T5-T8 | Frontend (edicao Markdown, model, componentes, testes) | PENDENTE (pos-RC) |

### Spec 018 — Deploy Backend GCP (NOVO — substitui OCI)
**Status: 100% concluido (Copilot R03)**
- T1: GraalVM Native Image profile + NativeConfig + NativeHintsRegistrar
- T2: `Dockerfile.native` (primario Cloud Run) + `Dockerfile.jvm-cloudrun` (fallback)
- T3: `application-prod.properties` Cloud Run (HikariCP=3, sessao in-memory + min-instances=1)
- T4: Scripts GCP VM e2-micro PostgreSQL (setup, docker-compose-db, .env.example, backup)
- T5: Cloud Run deploy + Secret Manager scripts
- T6: GitHub Actions `deploy-gcp.yml` (input native|jvm)
- T7: `docs/deploy/DEPLOY-GCP-BACKEND.md` + `.dockerignore`
- **OCI descontinuado:** `docs/deploy/DEPLOY-BACKEND.md` e `DEPLOY-OCI.md` marcados ⚠️

### Spec 019 — Deploy Frontend Firebase (NOVO — substitui OCI)
**Status: 100% concluido (Copilot R03)** — repo `ficha-controlador-front-end`
- T1: `firebase.json` + `.firebaserc` + `.gitignore`
- T2: Guia DNS + SSL custom domain (`docs/DEPLOY-FIREBASE-DNS.md`)
- T3: GitHub Actions `deploy-firebase.yml` (FirebaseExtended/action-hosting-deploy@v0)
- T4: `docs/DEPLOY-FRONTEND.md` atualizado + OCI descontinuado

**Acoes manuais pendentes (deploy):**
1. Criar projeto Firebase + GCP project
2. Service Accounts → secrets `FIREBASE_SERVICE_ACCOUNT`, `GCP_SA_KEY`
3. Configurar dominio customizado (Caddy → Cloud Run, Firebase Hosting)
4. Atualizar `environment.prod.ts` com URL real

---

## PRE-RC desejavel (nao bloqueante)

### Spec 017 P1 (5 tasks, ~10h)
| Task | Descricao |
|------|-----------|
| T8  | Criar `PageHeaderComponent` reutilizavel |
| T9  | Aplicar PageHeader em telas Mestre (config, jogo-detail, jogo-form, npcs) |
| T10 | Aplicar PageHeader em telas Jogador/wizard (fichas-list, ficha-detail, ficha-wizard, jogos-disponiveis) |
| T11 | `jogo-form` adicionar `<p-toast>` / usar `ToastService` |
| T12 | Remover double-toast em 19 componentes |

---

## Sequenciamento sugerido das proximas rodadas

### Rodada 14 oficial (proxima execucao Claude)
- Spec 012 T6-T8 (modelo, painel XP, level up step 1)
- **Spec 017 T1+T2** (backend SecurityConfig + teste)
- **Spec 017 T6** (hasBothRoles fix — 30min)
- **Spec 017 T7** (verFicha Mestre fix)
- **Spec 017 T22** (overlay clipping fix global em `app.config.ts` — 2h, independente)

### Rodada 15
- Spec 012 T9-T11 (level up steps)
- **Spec 017 T3+T4+T5** (interceptor refactor + guard)

### Rodada 16
- Spec 015 T6 (UI ClassePontos)
- Spec 015 T7 (UI RacaPontos)
- **Spec 017 T8** (criar PageHeader)
- **Spec 017 T9** (aplicar PageHeader Mestre)

### Rodada 17 (RC pronta apos esta)
- **Spec 017 T10** (PageHeader Jogador — apos Spec 012 T11 mergeada!)
- **Spec 017 T11** (jogo-form toast)
- **Spec 017 T12** (double-toast cleanup)

---

## Pos-RC (ordem de prioridade — atualizada apos avancos Copilot)

Apos aprovacao da homologacao, retomar nesta ordem:
1. **Spec 016 T5** (calculo automatico ao equipar — desbloqueia FichaItem) + **frontend T8-T11** (UI itens)
2. **Spec 011 T3** (FichaImagemService Cloudinary — apos security review) + **frontend T5-T8** (Markdown + galeria)
3. **Spec 014 T2-T4** (testes backend) + **T6** (testes frontend) — JaCoCo ja em 50%, target 75%
4. **Spec 017 P2** — pending (T13, T14, T-DOC1 — ~4h)
5. **Spec 013** — Documentacao tecnica (Javadoc, OpenAPI, TSDoc)
6. **Spec 010** — Roles ADMIN/MESTRE/JOGADOR refactor
7. **Spec 017 P3** — backlog de qualidade (T15-T21, ~16h, sem ordem rigida)

> **Nota:** A ordem original (PO 2026-04-06) era 011 → 016 → 014 → 013 → 010. A nova ordem prioriza desbloqueios (016 T5 e 011 T3 estao bloqueando o que ja foi entregue), mas o efeito final eh o mesmo: terminar 016 e 011 antes de seguir.

---

## Stand-by (pos-homologacao)
- Spec 010, Spec 013
- Spec 011 T3 + frontend (T5-T8)
- Spec 014 T2/T3/T4 + T6
- Spec 016 T5 + frontend (T8-T11)
- Spec 017 P2 e P3
- S007-T10 (PA-004 — FORMULA_CUSTOMIZADA sem alvo)

---

## Bloqueados / Pontos em Aberto
- S007-T10: FormulaEditorEfeito — PA-004 aguarda decisao PO
- **PA-017-01**: `verFicha` Mestre — criar rota nova `/mestre/fichas/:id` ou adaptar guard? (bloqueia Spec 017 T7) — **Recomendacao PM: Estrategia A (nova rota)**
- **PA-017-02**: `PageHeaderComponent` com Breadcrumb ou simples? (bloqueia Spec 017 T8) — **Recomendacao PM: simples**
- **PA-017-03**: Reativar `SidebarComponent`? (Spec 017 T15, P3, pos-RC)
- **PA-017-04**: Exportar/Importar config — formato? (Spec 017 T18, P3, pos-RC)
- **PA-R02-01**: Spec 016 T5 — `FichaItemService` tem 4x TODO `recalcularStats()`; teste `FichaCalculationItemEfeitoIntegrationTest` @Disabled aguarda T5
- **PA-R02-02**: Spec 011 T3 — `FichaImagemService`/Controller nao criados; teste @Disabled com 21 cenarios documentados; aguarda security review do upload Cloudinary
- **PA-014-T1-01**: JaCoCo threshold em 50% — target 75% atingivel apos Spec 014 T2/T3/T4
- **PA-016-T1-01**: TODO no `RaridadeItemConfigService.deletar()` para validar uso em ItemConfig — ja resolvido em T7

---

## Observacoes Tecnicas
- Frontend budget warning pre-existente: bundle 1.14MB vs limite 1MB (nao bloqueia)
- ficha-wizard.component.spec.ts e ficha-wizard-passo4: timeout de worker pre-existente (nao bloqueia)
- Spec 017 T10 deve rodar APOS Spec 012 T11 (mesmo arquivo `ficha-detail.component.ts`)
- Spec 017 nao duplica BUG-DC-06..08 (ja estao em Spec 015 T5)
- Detalhes completos da rodada 13 em `docs/tracking/rodadas/RODADA-13.md`
- Sessao atual (auditoria + Spec 017) em `docs/tracking/rodadas/RODADA-14.md` (planejamento)
- **Rodadas Copilot:** `docs/tracking/rodadas-copilot/COPILOT-R01.md`, `R02.md`, `R03.md`
- Auditorias em `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` e `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`
- Spec 017 em `docs/specs/017-correcoes-rc/` (spec.md, plan.md, 16 tasks ativas + 7 backlog)
- Spec 018 em `docs/specs/018-deploy-backend-gcp/` | Spec 019 em `docs/specs/019-deploy-frontend-firebase/`

---

## Incidentes (Copilot — para conhecimento)

### INC-R02-01 — Commit merge acidental (016 T4 + 011 T1)
Dois agentes paralelos rodando `git add .` simultaneamente. Codigo correto, mas commit `383416b` mistura T4 e T1. Mitigacao aplicada nas rodadas seguintes: usar `git add <arquivos-especificos>`.

### INC-R02-02 — `.claude/` commitado acidentalmente
Removido por commit corretivo `9d29190`. `.claude/` adicionado ao `.gitignore`.
