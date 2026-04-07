# T2 — Dominio Customizado + DNS + SSL Automatico

> Fase: Infra | Prioridade: P0
> Dependencias: T1 concluido (Firebase inicializado)
> Bloqueia: T4 (validacao E2E)
> Estimativa: 0.5 dia

---

## Objetivo

Mapear o dominio customizado `seu-dominio.com` (e `www.seu-dominio.com`) ao Firebase Hosting, configurar registros DNS no registrador de dominio, e verificar que o SSL e provisionado automaticamente pelo Google.

---

## Passo a Passo

### 1. Adicionar dominio no Firebase Console

1. Firebase Console → **Hosting** → **Custom domains**
2. Clicar **Add custom domain**
3. Inserir: `seu-dominio.com`
4. Firebase exibe registros DNS necessarios (A records + TXT de verificacao)
5. Repetir para `www.seu-dominio.com`

### 2. Configurar DNS no registrador de dominio

```
Tipo: A      Nome: @      Valor: 199.36.158.100   TTL: 300
Tipo: A      Nome: @      Valor: 199.36.158.101   TTL: 300
Tipo: TXT    Nome: @      Valor: (token de verificacao do Firebase)
Tipo: CNAME  Nome: www    Valor: seu-dominio.com    TTL: 300
```

> **NOTA:** Os IPs acima sao os IPs padrao do Firebase Hosting. O Firebase Console mostrara os valores exatos.

### 3. Aguardar propagacao

- Verificacao DNS: ~5-10 minutos
- Provisionamento SSL: ~10-30 minutos
- Firebase mostra status "Connected" quando pronto

### 4. Verificar redirect www → non-www

O Firebase permite configurar redirect automatico:
- Firebase Console → Hosting → Custom domains → `www.seu-dominio.com`
- Marcar como redirect para `seu-dominio.com`

### 5. Verificar SSL

```bash
# Verificar que HTTPS funciona
curl -I https://seu-dominio.com
# Esperado: HTTP/2 200

# Verificar redirect www
curl -I https://www.seu-dominio.com
# Esperado: HTTP/2 301 → Location: https://seu-dominio.com

# Verificar certificado
openssl s_client -connect seu-dominio.com:443 -servername seu-dominio.com < /dev/null 2>/dev/null | openssl x509 -noout -dates
# Esperado: datas validas (renovacao automatica pelo Google)
```

---

## Relacao com Backend (api.seu-dominio.com)

O subdominio `api.seu-dominio.com` e gerenciado pelo Cloud Run (Spec 018 T5), NAO pelo Firebase.

```
seu-dominio.com       → Firebase Hosting (A records → Firebase IPs)
www.seu-dominio.com   → Redirect para seu-dominio.com
api.seu-dominio.com   → Cloud Run (CNAME → ghs.googlehosted.com)
```

---

## Criterios de Aceitacao

- [ ] `https://seu-dominio.com` carrega o app Angular
- [ ] `https://www.seu-dominio.com` redireciona para `https://seu-dominio.com`
- [ ] SSL automatico ativo (certificado valido)
- [ ] SPA routing funciona com dominio customizado
- [ ] `environment.prod.ts` resolve `apiUrl` corretamente para `https://api.seu-dominio.com/api/v1`

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
