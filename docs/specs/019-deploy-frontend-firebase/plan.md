# Spec 019 — Plano de Implementacao: Deploy Frontend Firebase Hosting

> Spec: `019-deploy-frontend-firebase`
> Baseado em: spec.md v1.0 | 2026-04-07
> Estimativa total: ~2-3 dias de trabalho
> Depende de: Spec 018 (backend no Cloud Run — para validacao E2E)

---

## Fases e Dependencias

```
FASE 1 — Configuracao Firebase (local)
  T1: Inicializar Firebase no projeto + firebase.json + .firebaserc
  T2: Configurar dominio customizado + DNS + SSL
  [T1 primeiro; T2 depende de T1]

FASE 2 — CI/CD + Validacao
  T3: GitHub Actions workflow (build + deploy Firebase)
  T4: Validacao end-to-end + documentacao
  [T3 depende de T1; T4 depende de T2+T3 + Spec 018 T5]
```

---

## Fase 1 — Configuracao Firebase

### T1 — Inicializar Firebase + firebase.json
**Estimativa:** 0.5-1 dia
**Arquivos novos:**
- `firebase.json` — configuracao de hosting (rewrites, headers, cache)
- `.firebaserc` — projeto Firebase associado
- `.gitignore` — adicionar `.firebase/`

**Passos:**
1. Criar projeto Firebase no console (ou associar ao projeto GCP existente)
2. `npm install -g firebase-tools`
3. `firebase login`
4. `firebase init hosting` (selecionar projeto, configurar como SPA)
5. Customizar `firebase.json` com headers de seguranca e cache
6. Testar localmente: `npm run build:prod && firebase emulators:start`
7. Deploy manual de teste: `firebase deploy --only hosting`

---

### T2 — Dominio Customizado + DNS + SSL
**Estimativa:** 0.5 dia
**Dependencias:** T1 concluido
**Passos:**
1. Firebase Console → Hosting → Custom domain → `seu-dominio.com`
2. Firebase Console → Hosting → Custom domain → `www.seu-dominio.com`
3. Configurar registros DNS (A + TXT)
4. Aguardar propagacao DNS e provisionamento SSL (~10-30 min)
5. Verificar redirect www → non-www (configuravel no Firebase Console)

---

## Fase 2 — CI/CD + Validacao

### T3 — GitHub Actions: Build + Deploy Firebase
**Estimativa:** 0.5-1 dia
**Dependencias:** T1 concluido
**Arquivos novos:**
- `.github/workflows/deploy-firebase.yml` — workflow completo

**Jobs:**
1. `test` — `npm test --watch=false` (pula se skip_tests)
2. `build-and-deploy` — `npm ci` + `npm run build:prod` + `firebase deploy`

**Secrets GitHub:**
- `FIREBASE_SERVICE_ACCOUNT` — JSON da SA

---

### T4 — Validacao End-to-End + Documentacao
**Estimativa:** 0.5 dia
**Dependencias:** T2, T3, Spec 018 T5 (backend rodando)
**Arquivos novos:**
- Documentacao no README ou docs sobre deploy Firebase

**Validacoes:**
1. `https://seu-dominio.com` carrega o app
2. SPA routing funciona (rotas diretas como `/jogos/1`)
3. OAuth2 login end-to-end funcional
4. Assets com cache imutavel (verificar headers)
5. `index.html` sem cache
6. Rollback funcional: `firebase hosting:rollback`

---

## Estimativa Consolidada

| Task | Tipo | Estimativa | Dependencias |
|------|------|-----------|-------------|
| T1 | Config | 0.5-1 dia | Nenhuma |
| T2 | Infra | 0.5 dia | T1 |
| T3 | CI/CD | 0.5-1 dia | T1 |
| T4 | Validacao | 0.5 dia | T2, T3, Spec 018 |
| **TOTAL** | | **~2-3 dias** | |

> **Nota:** Spec 019 e significativamente mais simples que Spec 018 porque o frontend e apenas arquivos estaticos — sem build nativo, sem banco de dados, sem sessions.

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
