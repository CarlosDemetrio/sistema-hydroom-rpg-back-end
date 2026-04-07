# T3 — GitHub Actions: Build + Deploy Firebase Hosting

> Fase: CI/CD | Prioridade: P0
> Dependencias: T1 concluido (firebase.json existe)
> Bloqueia: T4 (validacao)
> Estimativa: 0.5-1 dia

---

## Objetivo

Criar workflow do GitHub Actions no repositorio frontend que automatiza: testes → build producao → deploy no Firebase Hosting. Trigger: push na branch `main` ou manual.

---

## Arquivo a Criar

| Arquivo | Descricao |
|---------|-----------|
| `.github/workflows/deploy-firebase.yml` | Workflow CI/CD completo |

---

## Workflow: deploy-firebase.yml

```yaml
name: Deploy Frontend to Firebase

on:
  push:
    branches: [main]
    paths:
      - 'src/**'
      - 'package.json'
      - 'package-lock.json'
      - 'angular.json'
      - 'firebase.json'
      - 'tsconfig*.json'
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests?'
        type: boolean
        default: false

jobs:
  # -----------------------------------------------
  # Job 1: Testes
  # -----------------------------------------------
  test:
    name: 🧪 Testes
    runs-on: ubuntu-latest
    if: ${{ github.event_name == 'push' || !inputs.skip_tests }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
          cache: npm
      - run: npm ci
      - run: npm test -- --watch=false

  # -----------------------------------------------
  # Job 2: Build + Deploy
  # -----------------------------------------------
  build-and-deploy:
    name: 🏗️ Build & Deploy Firebase
    needs: [test]
    if: always() && (needs.test.result == 'success' || needs.test.result == 'skipped')
    runs-on: ubuntu-latest
    environment: production
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: 22
          cache: npm

      - name: Install dependencies
        run: npm ci

      - name: Build production
        run: npm run build:prod

      - name: Verify build output
        run: |
          echo "=== Build output ==="
          ls -la dist/ficha-controlador-front-end/browser/
          test -f dist/ficha-controlador-front-end/browser/index.html || exit 1
          echo "=== Bundle size ==="
          du -sh dist/ficha-controlador-front-end/browser/

      - name: Deploy to Firebase Hosting
        uses: FirebaseExtended/action-hosting-deploy@v0
        with:
          repoToken: ${{ secrets.GITHUB_TOKEN }}
          firebaseServiceAccount: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
          channelId: live
          projectId: ficha-controlador-rpg

      - name: Post-deploy verification
        run: |
          sleep 10
          STATUS=$(curl -s -o /dev/null -w "%{http_code}" "https://seu-dominio.com")
          echo "Frontend status: $STATUS"
          if [ "$STATUS" != "200" ]; then
            echo "WARNING: Frontend may not be responding yet"
          fi
```

> **NOTA:** Substituir `ficha-controlador-rpg` e `seu-dominio.com` pelos valores reais.

---

## Secrets Necessarios (GitHub)

| Secret | Descricao | Como obter |
|--------|-----------|------------|
| `FIREBASE_SERVICE_ACCOUNT` | JSON da Service Account do Firebase | Firebase Console → Project Settings → Service Accounts → Generate new private key |
| `GITHUB_TOKEN` | Automatico | Automatico |

### Gerar Service Account

1. Firebase Console → **Project Settings** (engrenagem)
2. **Service Accounts** tab
3. **Generate new private key**
4. Baixar JSON
5. GitHub repo → **Settings** → **Secrets** → **New repository secret**
6. Nome: `FIREBASE_SERVICE_ACCOUNT`
7. Valor: conteudo completo do JSON

---

## GitHub Environment

Criar environment `production` no repositorio frontend:
- **Settings** → **Environments** → **New environment** → `production`
- (Opcional) Adicionar reviewers para aprovacao manual

---

## Tempo de Deploy Esperado

| Etapa | Tempo |
|-------|-------|
| npm ci | ~30-60s |
| ng build --configuration production | ~30-60s |
| firebase deploy | ~15-30s |
| **Total** | **~2-3 min** |

---

## Rollback

```bash
# Via Firebase CLI
firebase hosting:rollback

# Ou via Console: Firebase Console → Hosting → Release History → Rollback
```

---

## Criterios de Aceitacao

- [ ] Workflow `.github/workflows/deploy-firebase.yml` criado e valido
- [ ] Push na `main` (com mudancas em src/) dispara build + deploy
- [ ] `workflow_dispatch` permite pular testes
- [ ] Deploy completa em < 5 minutos
- [ ] App acessivel na URL do Firebase apos deploy
- [ ] Rollback funcional via `firebase hosting:rollback`
- [ ] Secret `FIREBASE_SERVICE_ACCOUNT` configurado no GitHub

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
