# T4 — Validacao End-to-End Frontend + Documentacao

> Fase: Validacao | Prioridade: P0
> Dependencias: T2 (dominio configurado), T3 (GitHub Actions funcional), Spec 018 T5 (backend no Cloud Run)
> Bloqueia: Tag v0.0.1-RC
> Estimativa: 0.5 dia

---

## Objetivo

Validar que o frontend no Firebase Hosting funciona end-to-end com o backend no Cloud Run: carregamento do app, SPA routing, OAuth2 login, comunicacao com API, headers de seguranca, cache. Documentar e deprecar docs antigos (OCI).

---

## Arquivos a Editar

| Arquivo | Acao | Descricao |
|---------|------|-----------|
| `docs/DEPLOY-FRONTEND.md` | EDITAR | Adicionar banner DESCONTINUADO (OCI), referenciar Firebase |
| `README.md` | EDITAR | Adicionar secao de deploy com link para docs |

---

## Checklist de Validacao

### 1. Carregamento do App

```bash
curl -I https://seu-dominio.com
# Esperado: HTTP/2 200
# Headers: X-Frame-Options: DENY, X-Content-Type-Options: nosniff
```

### 2. SPA Routing

```bash
# Rota Angular direta
curl -I https://seu-dominio.com/jogos
# Esperado: HTTP/2 200 (retorna index.html via rewrite)

curl -I https://seu-dominio.com/jogos/1/fichas
# Esperado: HTTP/2 200
```

### 3. Cache de Assets

```bash
# Encontrar um asset hasheado
ASSET=$(curl -s https://seu-dominio.com | grep -oP 'main\.[a-f0-9]+\.js' | head -1)
curl -I "https://seu-dominio.com/$ASSET"
# Esperado: Cache-Control: public, max-age=31536000, immutable
```

### 4. No-Cache de index.html

```bash
curl -I https://seu-dominio.com/index.html
# Esperado: Cache-Control: no-cache, no-store, must-revalidate
```

### 5. Redirect www → non-www

```bash
curl -I https://www.seu-dominio.com
# Esperado: HTTP/2 301 Location: https://seu-dominio.com/
```

### 6. OAuth2 Login End-to-End

```
1. Abrir https://seu-dominio.com no navegador
2. Clicar "Login com Google"
3. Verificar que redireciona para Google OAuth
4. Autenticar com conta de teste
5. Verificar redirect de volta para https://seu-dominio.com
6. Verificar que o usuario esta logado (nome exibido no header)
7. Verificar que session cookie esta presente (DevTools → Application → Cookies)
```

### 7. Comunicacao com Backend

```
1. Apos login, navegar para lista de jogos
2. Verificar que a requisicao vai para https://api.seu-dominio.com/api/v1/jogos
3. Verificar que a resposta chega (sem erros de CORS)
4. Criar um jogo de teste e verificar persistencia
```

### 8. Performance

```bash
# Lighthouse (via Chrome DevTools → Audit)
# Target: Performance > 80, Accessibility > 90

# Verificar compressao
curl -I -H "Accept-Encoding: gzip, br" https://seu-dominio.com
# Esperado: Content-Encoding: br (brotli) ou gzip
```

### 9. Rollback

```bash
# Via CLI
firebase hosting:rollback
# Via Console: Firebase Console → Hosting → Release History → Rollback

# Verificar que a versao anterior esta ativa
curl https://seu-dominio.com
```

---

## Deprecar Docs OCI do Frontend

Adicionar no topo de `docs/DEPLOY-FRONTEND.md`:

```markdown
> ⚠️ **DESCONTINUADO** — Este documento descreve o deploy na OCI (Oracle Cloud),
> que foi abandonado por falta de maquinas disponiveis no free tier.
> O deploy atual usa **Firebase Hosting (CDN Global)**.
> Veja: Spec 019 (`docs/specs/019-deploy-frontend-firebase/`)
```

---

## Criterios de Aceitacao

- [ ] App carrega em `https://seu-dominio.com`
- [ ] SPA routing funcional (rotas diretas Angular)
- [ ] Assets hasheados com cache imutavel
- [ ] index.html sem cache
- [ ] Redirect www → non-www
- [ ] OAuth2 login end-to-end funcional
- [ ] CORS funcional (frontend → backend)
- [ ] Headers de seguranca presentes (X-Frame-Options, etc.)
- [ ] Deploy via GitHub Actions completa em < 5 min
- [ ] Rollback funcional
- [ ] Docs OCI marcados como DESCONTINUADO

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
