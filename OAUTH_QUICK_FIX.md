# ⚡ Quick Fix - Google OAuth2 Error 400

**Erro:** `invalid_request` - App doesn't comply with Google's OAuth 2.0 policy

---

## 🚀 Solução Rápida (5 minutos)

### 1. Google Cloud Console
👉 https://console.cloud.google.com/

### 2. OAuth Consent Screen
1. Menu: **APIs & Services** > **OAuth consent screen**
2. Se não configurado:
   - Type: **External**
   - App name: `Ficha Controlador RPG`
   - User support email: `demetrio7500@gmail.com`
   - Developer contact: `demetrio7500@gmail.com`
3. **Scopes:** Adicionar `.../auth/userinfo.email`, `.../auth/userinfo.profile`, `openid`
4. **⚠️ IMPORTANTE - Test users:** Adicionar `demetrio7500@gmail.com`

### 3. Credentials
1. Menu: **APIs & Services** > **Credentials**
2. **CREATE CREDENTIALS** > **OAuth client ID**
3. Type: **Web application**
4. **Authorized JavaScript origins:**
   ```
   http://localhost:4200
   http://localhost:8080
   ```
5. **Authorized redirect URIs:**
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
6. **COPIAR:** Client ID e Client Secret

### 4. Configurar .env
Arquivo: `ficha-controlador/.env`
```bash
GOOGLE_CLIENT_ID=seu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-seu-secret
```

### 5. Reiniciar
```bash
cd ficha-controlador
docker compose restart backend
```

### 6. Testar
```
http://localhost:4200
Clicar em "Entrar com Google"
```

---

## ✅ Deve Funcionar!

Se não funcionar, consulte o guia completo em `GOOGLE_OAUTH2_SETUP.md`

---

## 🔑 Informações Importantes

- **Email de teste:** demetrio7500@gmail.com (DEVE estar em Test users!)
- **Callback URL:** http://localhost:8080/login/oauth2/code/google
- **Scopes:** openid, profile, email
- **Status:** External (em desenvolvimento)

---

**Tempo estimado:** 5-10 minutos  
**Dificuldade:** Fácil
