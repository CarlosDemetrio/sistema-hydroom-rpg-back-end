# Handoff de Sessao — 2026-04-08 (sessao 17 — Rodada 15 + Copilot R05)

> Branch atual: `main`
> Backend: **743 testes** passando, 0 falhas (+20 da Spec 011 T3)
> Frontend: **~907 testes** (novos componentes T6+T7 sem spec ainda — T8 pendente)
> Sprint 3: **Rodada 15 CONCLUIDA** (BUG-PROD + Spec 017 P1 + Spec 011 T3/T5/T6/T7) + **Copilot R05** (FichaPreviewService fix)
> Ultima atualizacao: 2026-04-08 [Copilot R05 — FichaPreviewService overload deprecated removido]

---

## Resumo Executivo

**Sessao 2026-04-08** entregou:
1. **3 bugs de producao corrigidos** (CSS, CORS, Cloudinary prod config)
2. **Spec 017 P1 concluida** (PageHeader T8-T12)
3. **Spec 011 completa exceto T8** (Cloudinary backend + frontend galeria + Markdown annotations)

Deploy ja em producao (Firebase + Cloud Run). Pendente apenas: 4 secrets GCP do Cloudinary.

---

## Estado das Specs

### Spec 017 — Correcoes Pre-RC

#### P0 — CONCLUIDO 8/8 (Claude R14 + Copilot R04)
#### P1 — CONCLUIDO 5/5 (Claude R15)
| Task | Status | Commit |
|------|--------|--------|
| T8 PageHeaderComponent | ✅ | `37696e4` |
| T9 PageHeader Mestre | ✅ | `3be4b46` |
| T10 PageHeader Jogador | ✅ | `94fd4af` |
| T11 jogo-form ToastService | ✅ | `2852fae` |
| T12 double-toast cleanup | ✅ | `6de29bc` |

#### P2 — PENDENTE (pos-RC)
#### P3 — BACKLOG (pos-RC)

### Spec 011 — Galeria + Anotacoes

| Task | Status | Commit/Nota |
|------|--------|-------------|
| T0 AnotacaoPasta entity (BE) | ✅ Copilot R02 | — |
| T1 PUT anotacao + pastaPaiId (BE) | ✅ Copilot R02 | — |
| T2 FichaImagem entity + dep Cloudinary (BE) | ✅ Copilot R02 | — |
| T3 FichaImagemService + Controller (BE) | ✅ Claude R15 | 743 testes |
| T4 Testes integracao (BE) | ✅ Claude R15 | Antecipado no T3 (21 cenarios) |
| T5 Markdown + pastas (FE) | ✅ Claude R15 | `a4c001c`..`2ab9a19` |
| T6 FichaImagem model + API (FE) | ✅ Claude R15 | `132db35` |
| T7 Componentes galeria (FE) | ✅ Claude R15 | `5bd60f7`..`e2822ca` |
| **T8 Testes frontend** | **PENDENTE** | Proxima sessao |

**Spec 011: 8/9 tasks completas. Apenas T8 (testes) pendente.**

### Copilot R05 — Bug fix FichaPreviewService

| Commit | Descricao |
|--------|-----------|
| `46b92d8` | Fix: FichaPreviewService usava overload deprecated do recalcular (7 params) |

**O que estava errado:** `simular()` chamava overload `@Deprecated(since="Spec-007-T0")` de 7 params, ignorando aptidoes, bônus de raca/classe e efeitos de vantagens.
**Fix:** migrado para overload completo (14 params), 6 repos injetados, `aptidaoBase` agora aplicado, overload deprecated removido de `FichaCalculationService`.
**Testes:** 743 (0 falhas) — sem delta (bug de logica, nao de cobertura).

**PAs abertos pelo Copilot R05:**
- `PA-R05-01`: `FichaPreviewResponse` nao expoe aptidoes nem dado de prospeccao — decisao de produto
- `PA-R05-02`: Testes de integracao do `FichaPreviewService` nao cobrem BONUS_APTIDAO, DADO_UP, raca/classe com bônus, aplicacao de `aptidaoBase`

### Spec 012 — CONCLUIDO 6/6
### Spec 015 — Parcial (T4 bloqueado)
### Spec 016 — Parcial (T5 bloqueado PA-R02-01, T8-T11 frontend pendente)
### Spec 018+019 — Deploy GCP+Firebase CONCLUIDO

---

## Bugs de Producao Corrigidos (Claude R15)

| Bug | Commit | Acoes Pendentes |
|-----|--------|----------------|
| BUG-PROD-01: CSS quebrado | `13945d6` (FE) | Re-deploy firebase |
| BUG-PROD-02: CORS "Erro 0" | `4fa546e` (BE) | Criar secret + re-deploy Cloud Run |
| BUG-PROD-03: Cloudinary sem credenciais | `4fa546e` (BE) | Criar 3 secrets + re-deploy Cloud Run |

### Acoes manuais urgentes (bloqueiam Cloudinary em prod)

```bash
# 1. Criar secrets no GCP (script interativo):
./infra/gcp/setup-secrets.sh

# 2. Ou manualmente:
echo -n "https://hydrooon.com.br,https://www.hydrooon.com.br" | gcloud secrets create rpg-cors-allowed-origins --data-file=-
echo -n "SEU_CLOUD_NAME"  | gcloud secrets create rpg-cloudinary-cloud-name --data-file=-
echo -n "SUA_API_KEY"     | gcloud secrets create rpg-cloudinary-api-key    --data-file=-
echo -n "SEU_API_SECRET"  | gcloud secrets create rpg-cloudinary-api-secret --data-file=-

# 3. Re-deploy backend (workflow deploy-gcp.yml) e frontend (firebase deploy)
```

**`npm install marked` no frontend** — para Markdown completo no AnotacaoCardComponent.

---

## Proxima Sessao — Rodada 16

### Obrigatorio antes do RC

| Task | Descricao | Agente | Status |
|------|-----------|--------|--------|
| Spec 011 T8 | Testes frontend (~40 novos testes) | angular-frontend-dev | PENDENTE |
| Secrets GCP (4 secrets) | rpg-cors-allowed-origins + 3x Cloudinary | Usuario | ✅ FEITO |
| Re-deploy backend + frontend | Cloud Run + Firebase | Usuario | ✅ FEITO |
| `npm install marked` | Markdown completo no frontend | Usuario | ✅ FEITO |

### RC (Rodada 17)

| ID | Cenario |
|----|---------|
| BUG-PROD-01 | Validar CSS em hydrooon.com.br (apos deploy) |
| BUG-PROD-02 | Validar login OAuth sem "Erro 0" |
| PA-R04-02 | Smoke test overlay clipping em 5 telas |
| PA-R04-03 | Decidir badge severity ficha-vantagens-tab (fix ou known issue) |
| PA-R04-04 | Decidir OOM ficha-wizard-passo4 (fix ou known issue) |

---

## Pos-RC (ordem de prioridade)

1. **Spec 016 T5** — recalcularStats ao equipar (4x TODO FichaItemService) + frontend T8-T11
2. **Spec 014 T2-T4+T6** — cobertura testes (JaCoCo 50% → 75%)
3. **Spec 017 P2** — T13, T14, T-DOC1 (~4h)
4. **Spec 013** — Documentacao tecnica
5. **Spec 010** — Roles ADMIN refactor
6. **Spec 017 P3** — backlog qualidade (T15-T21)

---

## Bloqueados / Pontos em Aberto

- **Cloudinary secrets** — criar manualmente no GCP antes do proximo deploy
- **`npm install marked`** — instalar no frontend para Markdown completo
- **PA-R02-01**: Spec 016 T5 — FichaItemService 4x TODO recalcularStats(); teste @Disabled
- **PA-R05-01**: FichaPreviewResponse incompleto (sem aptidoes/dado prospeccao) — decisao PO
- **PA-R05-02**: FichaPreviewService sem testes para BONUS_APTIDAO, DADO_UP, raca/classe — pos-RC
- **S007-T10**: FormulaEditorEfeito — PA-004 aguarda decisao PO
- **PA-017-03**: Reativar SidebarComponent? (Spec 017 T15, P3, pos-RC)
- **PA-017-04**: Exportar/Importar config — formato? (Spec 017 T18, P3, pos-RC)

---

## Observacoes Tecnicas

- Frontend budget warning pre-existente: bundle 1.14MB vs limite 1MB (nao bloqueia)
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- `application.properties` e `application-dev.properties` com linhas concatenadas: follow-up pos-RC
- Telas sem PageHeader por decisao tecnica: `fichas-list`, `jogos-disponiveis` (telas-destino)
- Toast com `key` isolado mantido: `npc-visibilidade`, `prospeccao`
- MarkdownPipe usa fallback basico sem `marked` instalado (negrito, italico, headers, code inline)
- `tipoImagem` imutavel apos upload — para promover GALERIA para AVATAR: novo upload com tipo AVATAR
- Spec 011 T8 spec: `docs/specs/011-galeria-anotacoes/tasks/P2-T8-frontend-testes.md`
- Tracking R15: `docs/tracking/rodadas/RODADA-15.md`
