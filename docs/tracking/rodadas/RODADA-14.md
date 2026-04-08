# Rodada 14 — Sessao de Auditoria + Spec 017 Planejamento

> Iniciada: 2026-04-07
> Concluida: 2026-04-07
> Branch: `main`
> Base: 723B + 879F testes (pos Copilot R04)
> Status: **CONCLUIDA** — Spec 017 P0 100% + Spec 012 fase 2 100% + Spec 015 T6/T7 pre-existentes

---

## Contexto

Apos o encerramento da Rodada 13 (Sprint 3), o PO solicitou duas auditorias
paralelas antes de abrir o proximo ciclo de implementacao:

1. Auditoria tecnica das rotas e tratamento de erros HTTP (`angular-tech-lead`)
2. Auditoria de UX/UI das telas do Mestre e Jogador (`primeng-ux-architect`)

O objetivo era consolidar todos os achados em uma unica spec bloqueante pre-RC
para nao fragmentar o backlog.

**Nota:** Existia previamente um arquivo `RODADA-14-TRACKING.md` criado em
2026-04-06 [18:30] que foi abandonado sem execucao. Ele foi movido para
`docs/historico/arquivado/RODADA-14-TRACKING-abandonada-2026-04-06.md`.
A rodada 14 oficial comeca agora (2026-04-07) com esta sessao.

---

## Atividades

### 1. Auditoria Tech Lead — Rotas e Erros HTTP

**Agente:** `angular-tech-lead`
**Entregavel:** `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md`

**8 achados (P1-P8):**

| ID | Problema | Severidade |
|----|----------|-----------|
| P1 | `SecurityConfig.java` sem `AuthenticationEntryPoint` — backend devolve 302 (redirect OAuth2) em vez de 401 quando sessao expira | ALTA |
| P2 | `error.interceptor` nao distingue 401 de 500 | ALTA |
| P3 | `auth.guard` nao salva `state.url` para redirect pos-login | MEDIA |
| P4 | Sem token para skippar interceptor em chamadas como `getUserInfo` | MEDIA |
| P5 | `hasBothRoles()` usa `\|\|` em vez de `&&` | BAIXA |
| P6 | `verFicha()` para Mestre cai em rota de Jogador | MEDIA |
| P7 | `jogo-form` sem `<p-toast>` | BAIXA |
| P8 | Double-toast em 19 componentes | BAIXA |

**Causa-raiz principal:** P1 — a falta de `HttpStatusEntryPoint(401)` faz com que
requisicoes AJAX recebam redirect HTML quando a sessao expira, causando a cadeia
de erros de UX reportados pelo PO ("erro interno do servidor" aleatorio).

### 2. Auditoria UX — Telas Mestre e Jogador

**Agente:** `primeng-ux-architect`
**Entregavel:** `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`

**9 achados principais + gaps menores:**

- 8 telas sem botao "Voltar" (navegacao quebrada sem historico do browser)
- `SidebarComponent` existe mas nao foi ativado no layout principal
- `verFicha` do Mestre cai em rota de Jogador (sobreposicao com P6 acima)
- `hasBothRoles` logica errada (sobreposicao com P5 acima)
- `jogo-form` sem `<p-toast>` (sobreposicao com P7 acima)
- Sobreposicao parcial com **Spec 015 T5** (bugs ja corrigidos no DefaultProvider)

**Recomendacao:** criar `PageHeaderComponent` reutilizavel para consolidar o
padrao de titulo + botao voltar em todas as telas.

### 3. Criacao da Spec 017

**Diretorio:** `docs/specs/017-correcoes-rc/`

Consolidou os achados das duas auditorias em **22 tasks iniciais** (15 ativas + 7 backlog)
distribuidas em 4 fases. Apos a criacao, o PO reportou um bug adicional de UX
que foi incluido como **T22** (ver secao 4 abaixo). A spec passou a ter
**23 tasks** (16 ativas + 7 backlog).

| Fase | Prioridade | Tasks | Esforco estimado | Bloqueia RC? |
|------|-----------|-------|------------------|-------------|
| P0 | Bloqueante | T1-T7 + T22 (8) | ~10h | **SIM** |
| P1 | Desejavel pre-RC | T8-T12 (5) | ~10h | Nao |
| P2 | Pos-RC | T13, T14, T-DOC1 (3) | ~4h | Nao |
| P3 | Backlog qualidade | T15-T21 (7) | ~16h | Nao |

**Bloqueantes pre-RC (P0):**
- T1: Backend `SecurityConfig` HttpStatusEntryPoint 401
- T2: Backend teste integracao 401 vs 302
- T3: Frontend criar `SKIP_ERROR_INTERCEPTOR` token
- T4: Frontend refatorar `error.interceptor` (401/403/0/500)
- T5: Frontend `auth.guard` salva `state.url` + `getUserInfo` skip
- T6: Frontend fix `hasBothRoles()` (`||` → `&&`)
- T7: Frontend fix `verFicha()` Mestre + nova rota
- **T22 (NOVO)**: Frontend fix overlay clipping de selects/dropdowns dentro de dialogs

### 4. Achado Tardio — T22 (overlay clipping em dialogs)

**Origem:** Bug reportado diretamente pelo PO em 2026-04-07, **apos** a entrega
das duas auditorias e a criacao inicial da Spec 017.

**Reporte literal do PO:**
> "Os selects e coisas que abrem estao sendo cortados e nao listados ate o final."

**Diagnostico tecnico:**
- Problema classico do PrimeNG: componentes de overlay (`p-select`, `p-multiselect`,
  `p-autocomplete`, `p-datepicker`, `p-cascadeselect`) renderizados dentro de
  containers com `overflow: hidden` (caso do `p-dialog`) sao clipados pelo
  limite do container
- Suspeita de causa-raiz: efeito colateral da migracao `p-drawer` → `p-dialog`
  realizada pelo PO em sessoes anteriores. O `p-drawer` tinha overflow permissivo
- Em listas longas o usuario nao consegue rolar ate o item desejado, tornando
  qualquer formulario nao trivial inutilizavel

**Por que a auditoria UX nao capturou:**
A inspecao do `primeng-ux-architect` foi estatica (estrutura visual das telas),
sem abrir nenhum dialog para validar interacoes de overlay. Lacuna metodologica
conhecida — fica como nota para auditorias futuras: **incluir validacao
interativa de overlays dentro de dialogs**.

**Decisao do PM — fase P0 (bloqueante):**
1. Afeta 100% das telas de formulario (que agora usam dialog)
2. Bloqueia operacao basica do sistema (selecionar valor em dropdown)
3. Arguably mais critico que outros P0 logicos (`hasBothRoles`, `verFicha`)
   porque afeta TODOS os usuarios, nao apenas Mestre em cenarios especificos
4. Fix e simples (uma linha em `app.config.ts`) — custo baixo, impacto alto
5. Sem este fix, a homologacao sera frustrante mesmo com tudo o resto correto

**Task criada:** `docs/specs/017-correcoes-rc/tasks/P0-T22-fix-overlay-clipping-dialogs.md`
- Solucao recomendada: `overlayOptions: { appendTo: 'body' }` global no
  `providePrimeNG` do `app.config.ts`
- Fallback: `[appendTo]="'body'"` por componente caso a config global cause regressao
- Estimativa: 2h (config + auditoria manual em 5 telas + smoke testes)
- Agente: `angular-frontend-dev`
- Independente das demais tasks da Spec 017 — pode rodar em qualquer rodada P0

**Nota metodologica para auditorias futuras:**
Adicionar nas guidelines do `primeng-ux-architect` a obrigatoriedade de validacao
INTERATIVA de overlays dentro de dialogs (abrir o dialog, abrir cada select,
verificar se o painel renderiza completo).

---

## Decisoes

- **Sequenciamento R14-R17** definido pelo PM:
  - R14 (proxima execucao): Spec 012 T6-T8 + Spec 017 T1+T2+T6+T7+**T22**
  - R15: Spec 012 T9-T11 + Spec 017 T3+T4+T5
  - R16: Spec 015 T6+T7 + Spec 017 T8+T9
  - R17 (RC pronta apos): Spec 017 T10+T11+T12
- **Spec 017 T10** deve rodar APOS Spec 012 T11 (mesmo arquivo `ficha-detail.component.ts`)
- **Spec 017 nao duplica BUG-DC-06..08** (ja resolvidos em Spec 015 T5)
- **Reorganizacao de docs/** executada nesta sessao (ver `docs/README.md`)
- **T22 (achado tardio)** classificada como P0 bloqueante; vai junto com T6+T7
  na Rodada 14 porque sao as 3 tasks frontend mais rapidas e independentes do P0
  (mesmo agente `angular-frontend-dev` pode encadear)

---

## Pendencias para o PO (decisoes bloqueantes)

| ID | Decisao necessaria | Bloqueia |
|----|--------------------|---------|
| **PA-017-01** | `verFicha` Mestre — criar rota nova `/mestre/fichas/:id` ou adaptar guard? | Spec 017 T7 |
| **PA-017-02** | `PageHeaderComponent` com Breadcrumb ou simples? | Spec 017 T8 |
| **PA-017-03** | Reativar `SidebarComponent`? | Spec 017 T15 (P3, pos-RC) |
| **PA-017-04** | Exportar/Importar config — formato? | Spec 017 T18 (P3, pos-RC) |

**Recomendacoes PM:**
- PA-017-01: Estrategia A (nova rota `/mestre/fichas/:id`)
- PA-017-02: Simples (sem breadcrumb; manter footprint pequeno)

---

## Proximos Passos

1. PO decide PA-017-01 e PA-017-02 (bloqueantes de T7 e T8)
2. PO aprova sequenciamento R14-R17
3. PM lanca a proxima execucao de Rodada 14 (Spec 012 T6-T8 + Spec 017 T1+T2+T6+T7)
4. Agentes atualizam este arquivo em tempo real conforme concluem tasks

---

## Progresso — Fase de Execucao (Claude R14)

| Task | Tipo | Status | Commit | Testes |
|------|------|--------|--------|--------|
| Spec 017 T7 — verFicha Mestre rota | FE | ✅ | `2d54886` | +2 |
| Spec 017 T3 — SKIP_ERROR_INTERCEPTOR token | FE | ✅ | `e0cadb7` | — |
| Spec 017 T5 — auth.guard REDIRECT_URL | FE | ✅ | `57dd3ff` | +4 |
| Spec 017 T4 — error.interceptor refactor | FE | ✅ | `8b2def4` | — |
| Spec 012 T6 — modelo FichaResumo | FE | ✅ pre-existente | `2cb0245` | — |
| Spec 012 T7 — painel XP + dialog + level up | FE | ✅ | `1251045` | — |
| Spec 012 T8 — LevelUpDialog + Step 1 atributos | FE | ✅ | `1d73fd7` | +15 |
| Spec 012 T9 — Step 2 aptidoes | FE | ✅ | `6f30b6d` | +5 |
| Spec 012 T10 — Step 3 vantagens | FE | ✅ | `406cb95` | +2 |
| Spec 012 T11 — saldo vantagens tab | FE | ✅ pre-existente | — | — |
| Spec 015 T6+T7 — ClassePontos+RacaPontos | FE | ✅ pre-existentes | `2cb0245` | — |

**Total testes novos: +28** (879 → 901 passando + 4 do guard spec)

## Observacoes

- Spec 017 T6 (hasBothRoles) e T22 (overlay) ja estavam feitos pelo Copilot R04
- T6 (modelo FichaResumo) e T11 (saldo vantagens) ja estavam implementados em `2cb0245`
- Spec 015 T6+T7 tambem pre-existentes no mesmo commit (rodada anterior ao R04)
- PA-017-01 resolvido: Estrategia A (nova rota) implementada
- `ficha-vantagens-tab` 2 falhas pre-existentes (PA-R04-03) — area DO NOT TOUCH, refatorar pos Spec 012 T11 merged
- `ficha-wizard-passo4` OOM pre-existente (PA-R04-04) — aumentar heap Node.js

---

*Rodada 14 concluida em 2026-04-07. Claude Code (Sonnet 4.6).*
