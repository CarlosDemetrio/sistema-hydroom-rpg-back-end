# T7 — Validacao End-to-End + Rollback + Documentacao

> Fase: Validacao | Prioridade: P0
> Dependencias: T4, T5, T6 — TODOS concluidos
> Bloqueia: Tag v0.0.1-RC
> Estimativa: 1 dia

---

## Objetivo

Validar que toda a infraestrutura GCP funciona end-to-end: backend no Cloud Run acessando PostgreSQL na VM, OAuth2 funcional, formulas avaliadas, rate limiting ativo. Documentar o processo completo no novo `DEPLOY-GCP-BACKEND.md` e deprecar os docs OCI.

---

## Arquivos a Criar/Editar

| Arquivo | Acao | Descricao |
|---------|------|-----------|
| `docs/deploy/DEPLOY-GCP-BACKEND.md` | CRIAR | Guia completo de deploy backend no GCP |
| `docs/deploy/DEPLOY-BACKEND.md` | EDITAR | Adicionar banner DESCONTINUADO no topo |
| `docs/deploy/DEPLOY-OCI.md` | EDITAR | Adicionar banner DESCONTINUADO no topo |

---

## Checklist de Validacao

### 1. Health Check

```bash
curl https://api.seu-dominio.com/actuator/health
# Esperado: {"status":"UP"}
```

### 2. Cold Start (se Native)

```bash
# Parar todas as instancias (escalar para 0)
gcloud run services update rpg-api --min-instances=0 --region=us-central1
# Aguardar 15 minutos
# Medir cold start
time curl https://api.seu-dominio.com/actuator/health
# Esperado: < 500ms (native) ou < 30s (JVM)
```

### 3. OAuth2 End-to-End

```
1. Abrir https://seu-dominio.com (frontend)
2. Clicar "Login com Google"
3. Autenticar com conta Google de teste
4. Verificar redirect de volta ao frontend
5. Verificar que session cookie esta presente (HttpOnly, Secure, SameSite=Lax)
```

### 4. CRUD Basico

```bash
# Listar jogos (deve retornar 200, possivelmente vazio)
curl -b cookies.txt https://api.seu-dominio.com/api/v1/jogos

# Criar jogo (requer autenticacao)
curl -X POST https://api.seu-dominio.com/api/v1/jogos \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"nome": "Teste Deploy"}'
# Esperado: 201 Created
```

### 5. FormulaEvaluatorService (formulas exp4j)

```bash
# Criar um atributo com formula de impeto e verificar que o calculo funciona
# (requer jogo criado + atributo configurado + ficha criada)
# Testar via frontend: criar ficha, distribuir pontos, verificar impeto calculado
```

### 6. Rate Limiting (Bucket4j)

```bash
# Enviar 120 requests rapidos ao /actuator/health
for i in $(seq 1 120); do
  curl -s -o /dev/null -w "%{http_code}\n" https://api.seu-dominio.com/actuator/health
done
# Esperado: primeiros 100 → 200, depois → 429 Too Many Requests
```

### 7. CORS

```bash
curl -I -X OPTIONS \
  -H "Origin: https://seu-dominio.com" \
  -H "Access-Control-Request-Method: GET" \
  https://api.seu-dominio.com/api/v1/jogos
# Esperado: Access-Control-Allow-Origin: https://seu-dominio.com
```

### 8. Rollback

```bash
# Listar revisoes
gcloud run revisions list --service=rpg-api --region=us-central1

# Redirecionar para revisao anterior
gcloud run services update-traffic rpg-api \
  --region=us-central1 \
  --to-revisions=rpg-api-XXXXXXXX=100

# Verificar que ainda funciona
curl https://api.seu-dominio.com/actuator/health
```

### 9. Logs

```bash
gcloud run logs read --service=rpg-api --region=us-central1 --limit=20
# Verificar que logs de startup e requests estao visiveis
```

### 10. Backup do PostgreSQL

```bash
# Na VM e2-micro:
sudo -u deploy /opt/backups/backup-postgres.sh
ls -la /opt/backups/
# Verificar que o backup foi criado
```

---

## DEPLOY-GCP-BACKEND.md (estrutura)

```markdown
# Deploy Backend — GCP Free Tier (Cloud Run)

## Arquitetura
## Pre-requisitos
## Passo a Passo
  ### 1. VM e2-micro (PostgreSQL)
  ### 2. Cloud Run (Backend)
  ### 3. Dominio e SSL
  ### 4. GitHub Actions
  ### 5. OAuth2
## Rollback
## Logs e Monitoramento
## Troubleshooting
## Checklist Pre-Deploy
## Custos
```

---

## Deprecar Docs OCI

Adicionar no topo de `DEPLOY-BACKEND.md` e `DEPLOY-OCI.md`:

```markdown
> ⚠️ **DESCONTINUADO** — Este documento descreve o deploy na OCI (Oracle Cloud),
> que foi abandonado por falta de maquinas disponiveis no free tier.
> O deploy atual usa **GCP Free Tier (Cloud Run + VM e2-micro)**.
> Veja: [`DEPLOY-GCP-BACKEND.md`](./DEPLOY-GCP-BACKEND.md)
```

---

## Criterios de Aceitacao

- [ ] Health check HTTPS funcional (`api.seu-dominio.com`)
- [ ] OAuth2 login end-to-end funcional
- [ ] CRUD de jogo/configuracoes funciona
- [ ] Formulas exp4j avaliadas corretamente em native
- [ ] Rate limiting ativo (429 apos limite)
- [ ] CORS permite frontend
- [ ] Rollback funcional (revisao anterior restaurada)
- [ ] Logs acessiveis via `gcloud run logs`
- [ ] Backup do PostgreSQL funcional
- [ ] `DEPLOY-GCP-BACKEND.md` escrito e completo
- [ ] Docs OCI marcados como DESCONTINUADO

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
