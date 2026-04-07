# T1 — Inicializar Firebase: firebase.json + .firebaserc + Headers + Cache

> Fase: Config | Prioridade: P0
> Dependencias: Nenhuma
> Bloqueia: T2 (dominio), T3 (GitHub Actions)
> Estimativa: 0.5-1 dia

---

## Objetivo

Inicializar o Firebase Hosting no projeto frontend Angular. Criar `firebase.json` com configuracao de SPA rewrite, headers de seguranca, cache imutavel para assets hasheados, e no-cache para `index.html`. Testar deploy local antes de automatizar.

---

## Pre-requisitos

1. Conta Google com acesso ao Firebase Console
2. Projeto Firebase criado (pode reutilizar o projeto GCP da Spec 018)
3. Node.js 22+ instalado
4. `firebase-tools` instalado globalmente

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `firebase.json` | Configuracao do Firebase Hosting |
| `.firebaserc` | Projeto Firebase associado |

## Arquivos a Editar

| Arquivo | Mudanca |
|---------|---------|
| `.gitignore` | Adicionar `.firebase/` e `.firebase-debug.log` |
| `package.json` | (Opcional) Adicionar script `"deploy": "firebase deploy --only hosting"` |

---

## Passo a Passo

### 1. Instalar firebase-tools

```bash
npm install -g firebase-tools
firebase login
```

### 2. Criar projeto Firebase (se nao existir)

```bash
# Opcao A: Via Console → https://console.firebase.google.com
# Opcao B: Via CLI
firebase projects:create ficha-controlador-rpg --display-name "Ficha Controlador RPG"
```

> **NOTA:** Se ja existe um projeto GCP da Spec 018, pode associar o Firebase ao mesmo projeto.

### 3. Inicializar no repositorio

```bash
cd /path/to/ficha-controlador-front-end
firebase init hosting
# Selecionar projeto: ficha-controlador-rpg
# Public directory: dist/ficha-controlador-front-end/browser
# Configure as SPA? Yes
# Set up automatic builds with GitHub? No (faremos manualmente no T3)
```

### 4. Customizar firebase.json

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

### 5. Criar .firebaserc

```json
{
  "projects": {
    "default": "ficha-controlador-rpg"
  }
}
```

> Substituir `ficha-controlador-rpg` pelo ID real do projeto Firebase.

### 6. Atualizar .gitignore

Adicionar:
```
# Firebase
.firebase/
.firebaserc.bak
firebase-debug.log
```

### 7. Testar localmente

```bash
npm run build:prod
firebase emulators:start --only hosting
# Abrir http://localhost:5000 — deve carregar o app Angular
```

### 8. Deploy manual de teste

```bash
firebase deploy --only hosting
# Deve imprimir a URL publica (ex: ficha-controlador-rpg.web.app)
```

---

## Validacao

```bash
# Verificar na URL do Firebase
curl -I https://ficha-controlador-rpg.web.app
# Esperado: HTTP/2 200

# Verificar SPA rewrite (rota Angular)
curl -I https://ficha-controlador-rpg.web.app/jogos/1
# Esperado: HTTP/2 200 (retorna index.html)

# Verificar cache de asset
curl -I https://ficha-controlador-rpg.web.app/main.abc12345.js
# Esperado: Cache-Control: public, max-age=31536000, immutable

# Verificar no-cache do index.html
curl -I https://ficha-controlador-rpg.web.app/index.html
# Esperado: Cache-Control: no-cache, no-store, must-revalidate
```

---

## Criterios de Aceitacao

- [ ] `firebase.json` criado com rewrites SPA, headers de seguranca e cache
- [ ] `.firebaserc` com projeto correto
- [ ] `.gitignore` atualizado com `.firebase/`
- [ ] `firebase emulators:start` serve o app localmente
- [ ] `firebase deploy` funciona e app acessivel na URL do Firebase
- [ ] SPA routing funcional (rotas diretas retornam index.html)
- [ ] Assets hasheados com `Cache-Control: immutable`
- [ ] `index.html` com `Cache-Control: no-cache`

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
