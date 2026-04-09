# Rodada 15 — 2026-04-08 (Claude)

> Branch: `main`
> Sessao: 17

---

## Resumo

Rodada focada em dois eixos: (1) bugs de producao bloqueantes para RC e (2) Spec 011 completa (Cloudinary + Anotacoes Markdown).

---

## Entregas

### BUG-PROD-01 — CSS quebrado em hydrooon.com.br
**Commit:** `13945d6` (frontend)
- `angular.json`: primeicons/primeflex movidos para `styles[]` (bundling garantido em prod)
- `src/styles.css`: `@import` redundantes removidos
- `src/index.html`: CSP hardcoded removida (bloqueava `api.hydrooon.com.br`)
- `firebase.json`: CSP correto via HTTP header com dominios de producao
- `login.component.css`: CSS morto limpo

### BUG-PROD-02+03 — CORS multi-origem e Cloudinary prod
**Commit:** `4fa546e` (backend)
- `application-prod.properties`: CORS agora aceita CSV `${CORS_ALLOWED_ORIGINS}` (antes: 1 origem so)
- `SecurityConfig.java`: trim() + filter() no split de origens
- `application-prod.properties`: vars Cloudinary lendo de env vars GCP
- `.github/workflows/deploy-gcp.yml`: 4 novos --set-secrets
- `infra/gcp/setup-secrets.sh`: script interativo para criar secrets
- `docs/deploy/DEPLOY-GCP-BACKEND.md`: tabela completa de 10 secrets

**Secrets manuais pendentes (usuario):**
- `rpg-cors-allowed-origins` = `https://hydrooon.com.br,https://www.hydrooon.com.br`
- `rpg-cloudinary-cloud-name`, `rpg-cloudinary-api-key`, `rpg-cloudinary-api-secret`
- Script: `./infra/gcp/setup-secrets.sh`

### Spec 017 P1 — PageHeader + Toast cleanup
| Task | Commit | Descricao |
|------|--------|-----------|
| T8 | `37696e4` | PageHeaderComponent criado (6 testes) |
| T9 | `3be4b46` | PageHeader em jogo-detail + jogo-form |
| T10 | `94fd4af` | PageHeader no ficha-wizard |
| T11 | `2852fae` | step-vantagens migrado para ToastService |
| T12 | `6de29bc` | Double-toast removido de 5 componentes |

### Spec 011 T3 — Backend Cloudinary (FichaImagemService + Controller)
**Backend — 743 testes, 0 falhas**
- `UploadImagemRequest.java`, `AtualizarImagemRequest.java`, `FichaImagemResponse.java`
- `FichaImagemMapper.java` (MapStruct)
- `CloudinaryUploadService.java` (separado — facilita mock)
- `FichaImagemService.java` (listar, adicionar, atualizar, deletar)
- `FichaImagemController.java` (4 endpoints multipart + JSON)
- `ExternalServiceException.java` (nova — HTTP 502 para falhas externas)
- `GlobalExceptionHandler`: ForbiddenException → 403, ExternalServiceException → 502
- `FichaImagemServiceIntegrationTest`: 21 cenarios (T4 antecipado)

### Spec 011 T5 — Frontend Markdown + Pastas
| Commit | Descricao |
|--------|-----------|
| `a4c001c` | AnotacaoPasta model + AtualizarAnotacaoDto + campos visivelParaTodos/pastaPaiId |
| `08249af` | FichasApiService + FichaBusinessService: pastas e editarAnotacao |
| `493c8b4` | MarkdownPipe com sanitizacao (alternativa a ngx-markdown) |
| `73569cb` | AnotacaoCardComponent: modo edicao inline com Markdown |
| `2ab9a19` | FichaAnotacoesTabComponent: arvore de pastas p-tree + filtro |

**Nota:** `marked` precisa ser instalado (`npm install marked`) para renderizacao Markdown completa. Sem isso, pipe usa fallback basico (negrito, italico, headers, code inline).

### Spec 011 T6+T7 — Frontend Galeria Cloudinary
| Commit | Descricao |
|--------|-----------|
| `132db35` | FichaImagem model + 4 metodos no FichasApiService e FichaBusinessService |
| `5bd60f7` | ImagemCardComponent dumb (badge avatar, deletar, expandir) |
| `26bbd02` | FichaGaleriaTabComponent smart (upload, lightbox, avatar-first, limite 20) |
| `e2822ca` | Aba Galeria adicionada ao FichaDetailPage |

---

## Pendente para proxima sessao

### T8 — Testes frontend galeria + anotacoes (~40 testes novos)
- Suite 1: `anotacao-card.component.spec.ts` (13 cenarios — Markdown, permissoes, edicao)
- Suite 2: `imagem-card.component.spec.ts` (9 cenarios — badge, deletar, expandir)
- Suite 3: `ficha-galeria-tab.component.spec.ts` (16 cenarios — upload, lightbox, limite, pastas)
- Suite 4: `ficha-anotacoes-tab.component.spec.ts` (5 cenarios — pastas, filtro)
- Suite 5: `ficha-business.service.spec.ts` extensao (7 cenarios — delegacao)
- Spec: `docs/specs/011-galeria-anotacoes/tasks/P2-T8-frontend-testes.md`

### Acoes manuais (usuario)
1. `./infra/gcp/setup-secrets.sh` → criar 4 secrets GCP
2. `npm install marked` (no frontend) → Markdown completo
3. Re-deploy backend + frontend apos secrets

### Pos-T8: RC
- Smoke tests overlay, vantagens-tab badge, wizard OOM
- Homologacao em hydrooon.com.br

---

## Estado dos Testes

| Frente | Antes | Depois | Delta |
|--------|-------|--------|-------|
| Backend | 723 | 743 | +20 (T3 integration tests) |
| Frontend | 907 | ~907* | T8 pendente — novos componentes sem spec ainda |

*Novos componentes criados (ImagemCard, FichaGaleriaTab) nao tem spec — T8 cobre isso.
