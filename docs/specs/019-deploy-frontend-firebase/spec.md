# Spec 019 — Deploy Frontend: Firebase Hosting (CDN Global)

> Spec: `019-deploy-frontend-firebase`
> Epic: Infraestrutura e Deploy
> Status: PLANEJADO — spec+plan+tasks PRONTOS, implementacao PENDENTE
> Depende de: Backlog funcional concluido, Spec 018 (backend no Cloud Run — para validacao E2E)
> Bloqueia: Tag `v0.0.1-RC`
> Prioridade: P0 — Obrigatorio para RC

---

## 1. Visao Geral

**Problema resolvido:** O plano original previa deploy do frontend como arquivos estaticos na VM OCI, servidos pelo Caddy. Com a migracao para GCP, a VM e2-micro e dedicada exclusivamente ao PostgreSQL (1GB RAM — sem espaco para servir frontend). O Firebase Hosting oferece CDN global gratuita, SSL automatico, e deploy via CLI — ideal para uma SPA Angular.

**Objetivo:** Migrar o deploy do frontend Angular de arquivos estaticos na VM OCI para o Firebase Hosting, com pipeline CI/CD via GitHub Actions, dominio customizado (`seu-dominio.com`), e cache otimizado para assets com hash.

**Valor entregue:**
- Frontend em CDN global com custo R$ 0,00/mes (Plano Spark)
- Deploy em ~2 minutos (npm ci → build → firebase deploy)
- SSL automatico gerenciado pelo Firebase
- Cache imutavel para assets hasheados (performance maxima)
- Zero downtime em deploys
- Dominio customizado com redirect www → non-www

---

## 2. Contexto: Migracao OCI → Firebase

### Antes (OCI — descontinuado)

```
VM OCI → Caddy serve /opt/frontend/ (arquivos estaticos Angular)
         Caddy gerencia SSL + cache + compressao gzip/zstd
```

### Agora (Firebase Hosting)

```
Firebase Hosting (CDN Global) → serve dist/browser/ (arquivos Angular)
                                SSL automatico pelo Google
                                Cache headers configurados no firebase.json
                                Deploy via firebase-tools CLI
```

### Impactos da Migracao

| Aspecto | OCI (antes) | Firebase (agora) |
|---------|-------------|------------------|
| Hospedagem | VM + Caddy | CDN Firebase (global) |
| SSL/TLS | Caddy gerenciava | Firebase gerencia automaticamente |
| Compressao | Caddy (gzip + zstd) | Firebase (gzip + brotli automatico) |
| Cache | Caddyfile manual | firebase.json declarativo |
| Deploy | SCP para /opt/frontend/ | `firebase deploy` via CLI |
| Rollback | cp de /opt/frontend-backup/ | `firebase hosting:rollback` (1 comando) |
| Custo | $0 (OCI Free Tier) | $0 (Firebase Spark: 10GB storage, 10GB banda/mes) |
| Latencia | Servidor unico (EUA) | CDN global (edge servers proximos ao usuario) |

---

## 3. Arquitetura

```
Internet (Usuarios)
   |
   +-- [Frontend] -> https://seu-dominio.com
           Servico: Firebase Hosting (CDN Global)
           Tecnologia: Angular 21 (Static Files — dist/browser/)
           Custo: Gratis (Plano Spark: 10GB storage, 10GB banda/mes)
           SSL: Automatico pelo Google
           Cache: Imutavel para assets hasheados (1 ano)
                  No-cache para index.html (sempre fresco)
```

---

## 4. Mudancas no Projeto Frontend

### 4.1 Novos Arquivos

| Arquivo | Descricao |
|---------|-----------|
| `firebase.json` | Configuracao do Firebase Hosting (rewrites, headers, cache) |
| `.firebaserc` | Projeto Firebase associado |
| `.github/workflows/deploy-firebase.yml` | GitHub Actions: build + deploy |

### 4.2 Arquivos a Editar

| Arquivo | Mudanca |
|---------|---------|
| `src/environments/environment.prod.ts` | Verificar que apiUrl continua derivando de `window.location.hostname` |
| `package.json` | Adicionar script `deploy` (opcional) |
| `.gitignore` | Adicionar `.firebase/` |

### 4.3 Arquivos NAO afetados

- `angular.json` — build de producao ja esta configurado corretamente
- `Dockerfile` — mantido para desenvolvimento, mas NAO usado no deploy Firebase
- `nginx.conf` — mantido para desenvolvimento Docker, NAO usado no Firebase
- Codigo Angular — ZERO mudancas em components, services, stores

---

## 5. Configuracao do Firebase

### 5.1 firebase.json

```json
{
  "hosting": {
    "public": "dist/ficha-controlador-front-end/browser",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "rewrites": [
      {
        "source": "**",
        "destination": "/index.html"
      }
    ],
    "headers": [
      {
        "source": "**/*.@(js|css|woff2|ttf|eot|svg|png|jpg|ico)",
        "headers": [
          {
            "key": "Cache-Control",
            "value": "public, max-age=31536000, immutable"
          }
        ]
      },
      {
        "source": "index.html",
        "headers": [
          {
            "key": "Cache-Control",
            "value": "no-cache, no-store, must-revalidate"
          }
        ]
      },
      {
        "source": "**",
        "headers": [
          {
            "key": "X-Frame-Options",
            "value": "DENY"
          },
          {
            "key": "X-Content-Type-Options",
            "value": "nosniff"
          },
          {
            "key": "X-XSS-Protection",
            "value": "1; mode=block"
          },
          {
            "key": "Referrer-Policy",
            "value": "strict-origin-when-cross-origin"
          },
          {
            "key": "Permissions-Policy",
            "value": "camera=(), microphone=(), geolocation=()"
          }
        ]
      }
    ]
  }
}
```

### 5.2 .firebaserc

```json
{
  "projects": {
    "default": "ficha-controlador-rpg"
  }
}
```

> Substituir `ficha-controlador-rpg` pelo ID real do projeto Firebase.

---

## 6. Dominio Customizado

### 6.1 Configurar no Firebase Console

1. Firebase Console → Hosting → Custom domain
2. Adicionar `seu-dominio.com`
3. Adicionar `www.seu-dominio.com`
4. Firebase fornece registros DNS:
   - `A` record → IP do Firebase
   - `TXT` record → verificacao de propriedade

### 6.2 DNS (no registrador de dominio)

```
Tipo: A      Nome: @    Valor: 199.36.158.100  TTL: 300
Tipo: A      Nome: @    Valor: 199.36.158.101  TTL: 300
Tipo: CNAME  Nome: www  Valor: seu-dominio.com  TTL: 300
Tipo: TXT    Nome: @    Valor: (verificacao Firebase)
```

> SSL e provisionado automaticamente apos verificacao DNS (~10-30 min).

---

## 7. environment.prod.ts

O arquivo atual ja esta correto:

```typescript
export const environment = {
  production: true,
  apiUrl: `https://api.${window.location.hostname.replace(/^www\./, '')}/api/v1`,
  backendUrl: `https://api.${window.location.hostname.replace(/^www\./, '')}`
};
```

**Funciona porque:**
- Frontend em `seu-dominio.com` → `apiUrl` = `https://api.seu-dominio.com/api/v1`
- Frontend em `www.seu-dominio.com` → `apiUrl` = `https://api.seu-dominio.com/api/v1`
- Nenhuma variavel de ambiente necessaria no deploy!

---

## 8. GitHub Actions Workflow

```yaml
name: Deploy Frontend to Firebase

on:
  push:
    branches: [main]
    paths:
      - 'src/**'
      - 'package.json'
      - 'angular.json'
      - 'firebase.json'
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests?'
        type: boolean
        default: false

jobs:
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

      - name: Verify build
        run: |
          ls -la dist/ficha-controlador-front-end/browser/
          test -f dist/ficha-controlador-front-end/browser/index.html

      - name: Deploy to Firebase
        uses: FirebaseExtended/action-hosting-deploy@v0
        with:
          repoToken: ${{ secrets.GITHUB_TOKEN }}
          firebaseServiceAccount: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
          channelId: live
          projectId: ficha-controlador-rpg
```

---

## 9. Secrets Necessarios (GitHub)

| Secret | Descricao | Como obter |
|--------|-----------|------------|
| `FIREBASE_SERVICE_ACCOUNT` | JSON da Service Account do Firebase | Firebase Console → Project Settings → Service Accounts → Generate Key |
| `GITHUB_TOKEN` | Automatico (para preview comments em PRs) | Automatico |

---

## 10. Custos Firebase (Plano Spark)

| Recurso | Limite Gratuito | Consumo Estimado (50 usuarios) |
|---------|----------------|-------------------------------|
| Storage | 10 GB | < 100 MB (Angular build ~5-10MB) |
| Banda | 10 GB/mes | ~500 MB/mes (com cache agressivo) |
| SSL | Ilimitado | Automatico |
| Dominios | Ilimitado | 2 (dominio + www) |

---

## 11. Requisitos Funcionais

| # | Requisito |
|---|-----------|
| RF-01 | Build Angular production funcional (`npm run build:prod`) |
| RF-02 | Firebase Hosting configurado com `firebase.json` correto |
| RF-03 | Dominio customizado `seu-dominio.com` com SSL |
| RF-04 | SPA routing funcional (qualquer rota → index.html) |
| RF-05 | Assets hasheados com cache imutavel (1 ano) |
| RF-06 | index.html sem cache (sempre fresco) |
| RF-07 | Security headers (X-Frame-Options, CSP parcial, etc.) |
| RF-08 | Deploy automatizado via GitHub Actions (push na main) |
| RF-09 | Rollback via `firebase hosting:rollback` ou re-deploy |
| RF-10 | Frontend conecta ao backend via `api.seu-dominio.com` |

---

## 12. Criterios de Aceitacao

- [ ] `npm run build:prod` completa sem erros e dentro dos budgets
- [ ] `firebase deploy --only hosting` funciona localmente
- [ ] `https://seu-dominio.com` carrega o app Angular
- [ ] SPA routing funciona (navegar para `/jogos/1` → app carrega)
- [ ] `https://www.seu-dominio.com` redireciona para `https://seu-dominio.com`
- [ ] Assets JS/CSS tem header `Cache-Control: public, max-age=31536000, immutable`
- [ ] `index.html` tem header `Cache-Control: no-cache, no-store, must-revalidate`
- [ ] OAuth2 login funcional (frontend → backend → Google → callback)
- [ ] Deploy via GitHub Actions completa sem erros (~2-4 min)
- [ ] `firebase hosting:rollback` restaura versao anterior

---

## 13. Pontos em Aberto

| ID | Pergunta | Impacto |
|----|----------|---------|
| PA-019-01 | Content-Security-Policy completo deve ser configurado no firebase.json ou esta OK sem ele no MVP? | Seguranca — CSP restringe origens de scripts/styles |
| PA-019-02 | Firebase preview channels para PRs (deploy de preview automatico)? | UX de review — pode fazer no MVP ou pos-MVP |

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
*Baseado em: Documento de Arquitetura GCP, angular.json atual, environment.prod.ts*
