# Handoff de Sessao — 2026-04-07 (sessao 16, auditoria + Spec 017 criada + T22 incluida)

> Branch atual: `main`
> Backend: **613 testes** passando, 0 falhas
> Frontend: **848 testes** passando, 0 falhas
> Sprint 3: Rodada 13 concluida; **Rodada 14 aberta (2026-04-07)** — sessao de auditoria + planejamento Spec 017; execucao aguarda decisoes PA-017-01/02
> Ultima atualizacao: 2026-04-07 [sessao auditoria + Spec 017 criada + reorganizacao docs/ + **T22 (overlay clipping) incluida apos bug PO**]

---

## Resumo Executivo

**Sessao 2026-04-07** foi de auditoria + planejamento (sem codigo de producao alterado):

1. **Auditoria Tech Lead** (`docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md`) identificou 8 problemas (P1-P8) no fluxo de tratamento de erros HTTP do frontend. Causa-raiz principal: `SecurityConfig.java` nao tem `AuthenticationEntryPoint` configurado → backend devolve 302 (redirect OAuth2) em vez de 401 quando sessao expira → frontend mostra "erro interno do servidor" em vez de fluxo limpo de re-login.

2. **Auditoria UX Architect** (`docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`) identificou 9 achados (P1-P3 + 9 extras). Principais: 8 telas sem botao Voltar; `SidebarComponent` existe mas nao foi ativado; bugs pontuais (`hasBothRoles` logica errada, `verFicha` Mestre cai em rota de Jogador, `jogo-form` sem `<p-toast>`); sobreposicao parcial com Spec 015 T5 (bugs no DefaultProvider).

3. **Spec 017 criada** (`docs/specs/017-correcoes-rc/`) consolidando ambas as auditorias em 22 tasks distribuidas em 4 fases (P0-P3). Apos a criacao, o PO reportou um bug adicional de UX nao capturado pela auditoria (overlay clipping de selects dentro de dialogs) que foi incluido como **T22 P0**. Spec 017 agora tem **23 tasks** (16 ativas + 7 backlog). P0 cresceu de 7 para 8 tasks (~10h).

4. **Bug adicional T22 (capturado direto do PO):** "Os selects e coisas que abrem estao sendo cortados e nao listados ate o final." Causa-raiz: efeito colateral classico do PrimeNG quando overlay components (`p-select`, `p-multiselect`, `p-autocomplete`, `p-datepicker`) sao renderizados dentro de containers com `overflow: hidden` (caso do `p-dialog`). Provavelmente disparado pela migracao recente `p-drawer` → `p-dialog`. Fix: configurar `overlayOptions: { appendTo: 'body' }` global no `app.config.ts`. Auditoria UX nao capturou porque inspecao foi estatica (sem abrir dialogs). **Decidido P0** porque afeta TODAS as telas de formulario do sistema e bloqueia operacao basica de selecao em dropdowns longos.

---

## Priorizacao PRE-RC (bloqueante)

### NOVO: Spec 017 P0 (8 tasks, ~10h)
| Task | Descricao | Tipo |
|------|-----------|------|
| T1 | Backend: `SecurityConfig` HttpStatusEntryPoint 401 | BE |
| T2 | Backend: Teste integracao 401 vs 302 | BE |
| T3 | Frontend: Criar `SKIP_ERROR_INTERCEPTOR` token | FE |
| T4 | Frontend: Refatorar `error.interceptor` (401/403/0/500) | FE |
| T5 | Frontend: `auth.guard` salva `state.url` + `getUserInfo` skip | FE |
| T6 | Frontend: Fix bug `hasBothRoles()` (`\|\|` → `&&`) | FE |
| T7 | Frontend: Fix bug `verFicha()` Mestre + nova rota | FE |
| **T22** | **Frontend: Fix overlay clipping de selects/dropdowns dentro de dialogs (`appendTo: 'body'` global)** | **FE** |

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

### Rodada 14 (proxima)
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

## Pos-RC (ordem de prioridade — decidida pelo PO em 2026-04-06)

Apos aprovacao da homologacao, retomar nesta ordem:
1. **Spec 017 P2** — pending (T13, T14, T-DOC1 — ~4h)
2. **Spec 011** — Galeria de imagens + Anotacoes com pastas
3. **Spec 016** — Sistema de Itens/Equipamentos (11 tasks: 7B + 4F)
4. **Spec 014** — Cobertura de testes (JaCoCo, Vitest)
5. **Spec 013** — Documentacao tecnica (Javadoc, OpenAPI, TSDoc)
6. **Spec 010** — Roles ADMIN/MESTRE/JOGADOR refactor
7. **Spec 017 P3** — backlog de qualidade (T15-T21, ~16h, sem ordem rigida)

---

## Stand-by (pos-homologacao)
- Spec 010, 011, 013, 014, 016
- Spec 017 P2 e P3
- S007-T10 (PA-004 — FORMULA_CUSTOMIZADA sem alvo)

---

## Bloqueados / Pontos em Aberto
- S007-T10: FormulaEditorEfeito — PA-004 aguarda decisao PO
- **PA-017-01**: `verFicha` Mestre — criar rota nova `/mestre/fichas/:id` ou adaptar guard? (bloqueia Spec 017 T7) — **Recomendacao PM: Estrategia A (nova rota)**
- **PA-017-02**: `PageHeaderComponent` com Breadcrumb ou simples? (bloqueia Spec 017 T8) — **Recomendacao PM: simples**
- **PA-017-03**: Reativar `SidebarComponent`? (Spec 017 T15, P3, pos-RC)
- **PA-017-04**: Exportar/Importar config — formato? (Spec 017 T18, P3, pos-RC)

---

## Observacoes Tecnicas
- Frontend budget warning pre-existente: bundle 1.14MB vs limite 1MB (nao bloqueia)
- ficha-wizard.component.spec.ts e ficha-wizard-passo4: timeout de worker pre-existente (nao bloqueia)
- Spec 017 T10 deve rodar APOS Spec 012 T11 (mesmo arquivo `ficha-detail.component.ts`)
- Spec 017 nao duplica BUG-DC-06..08 (ja estao em Spec 015 T5)
- Detalhes completos da rodada 13 em `docs/tracking/rodadas/RODADA-13.md`
- Sessao atual (auditoria + Spec 017) em `docs/tracking/rodadas/RODADA-14.md` (EM ANDAMENTO, planejamento)
- Auditorias em `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` e `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`
- Spec 017 em `docs/specs/017-correcoes-rc/` (spec.md, plan.md, 15 tasks ativas + 7 backlog)
- **Reorganizacao docs/ completa** nesta sessao: ver `docs/README.md` para o novo mapa. Tracking files movidos para `docs/tracking/rodadas/`; backlogs iniciais arquivados em `docs/historico/backlogs-iniciais/`; INDEX/TEAM-PLAN/PROXIMA-SESSAO em `docs/historico/arquivado/`.
