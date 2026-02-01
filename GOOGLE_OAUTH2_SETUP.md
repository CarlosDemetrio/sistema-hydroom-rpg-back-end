# 🔐 Configuração Google OAuth2 - Passo a Passo

**Erro Atual:** `Error 400: invalid_request` - App doesn't comply with Google's OAuth 2.0 policy

**Data:** 31/01/2026

---

## ❌ Problema

Sua aplicação não está configurada corretamente no Google Cloud Console, resultando em:
- Erro 400: invalid_request
- "This app doesn't comply with Google's OAuth 2.0 policy"
- Impossível fazer login

---

## ✅ Solução Completa

### Passo 1: Acessar Google Cloud Console

1. Acesse: https://console.cloud.google.com/
2. Login com: **demetrio7500@gmail.com**

---

### Passo 2: Criar/Selecionar Projeto

#### Se não tem projeto:
1. Clique em **"Select a project"** (topo da página)
2. Clique em **"NEW PROJECT"**
3. Nome do projeto: `Ficha Controlador RPG`
4. Clique em **"CREATE"**

#### Se já tem projeto:
1. Selecione o projeto existente

---

### Passo 3: Habilitar Google+ API

1. No menu lateral (☰), vá em: **APIs & Services** > **Library**
2. Procure por: `Google+ API`
3. Clique em **"ENABLE"**
4. **OU** procure por: `People API` e habilite também

---

### Passo 4: Configurar Tela de Consentimento OAuth

1. No menu lateral, vá em: **APIs & Services** > **OAuth consent screen**

2. **Escolher tipo de usuário:**
   - **External** (se quer que qualquer pessoa possa usar)
   - **Internal** (se é só para sua organização G Suite)
   - **Para desenvolvimento:** Escolha **External**
   - Clique **CREATE**

3. **App information:**
   ```
   App name: Ficha Controlador RPG
   User support email: demetrio7500@gmail.com
   App logo: (opcional, pode pular)
   ```

4. **App domain:**
   ```
   Application home page: http://localhost:4200 (desenvolvimento)
   Application privacy policy: http://localhost:4200/privacy (deixe vazio por ora)
   Application terms of service: http://localhost:4200/terms (deixe vazio por ora)
   ```

5. **Authorized domains:**
   ```
   localhost (adicionar)
   ```

6. **Developer contact information:**
   ```
   Email: demetrio7500@gmail.com
   ```

7. Clique **SAVE AND CONTINUE**

8. **Scopes:**
   - Clique em **ADD OR REMOVE SCOPES**
   - Selecione:
     - ✅ `.../auth/userinfo.email`
     - ✅ `.../auth/userinfo.profile`
     - ✅ `openid`
   - Clique **UPDATE**
   - Clique **SAVE AND CONTINUE**

9. **Test users** (IMPORTANTE para desenvolvimento):
   - Clique em **ADD USERS**
   - Adicione: `demetrio7500@gmail.com`
   - Clique **ADD**
   - Clique **SAVE AND CONTINUE**

10. **Summary:**
    - Revise tudo
    - Clique **BACK TO DASHBOARD**

---

### Passo 5: Criar Credenciais OAuth2

1. No menu lateral, vá em: **APIs & Services** > **Credentials**

2. Clique em **CREATE CREDENTIALS** > **OAuth client ID**

3. **Application type:**
   - Selecione: **Web application**

4. **Name:**
   ```
   Ficha Controlador RPG - Local Development
   ```

5. **Authorized JavaScript origins:**
   ```
   http://localhost:4200
   http://localhost:8080
   http://localhost
   ```
   (Adicione os 3 URLs)

6. **Authorized redirect URIs:**
   ```
   http://localhost:8080/login/oauth2/code/google
   http://localhost:8080/oauth2/callback/google
   ```
   (Adicione os 2 URLs)

7. Clique **CREATE**

8. **Copiar credenciais:**
   - Aparecerá um modal com:
     - **Client ID:** `xxx.apps.googleusercontent.com`
     - **Client Secret:** `GOCSPX-xxxxx`
   - **COPIE AMBOS!**

---

### Passo 6: Configurar no Backend

#### Arquivo: `/ficha-controlador/.env`

```bash
# Google OAuth2 Credentials
GOOGLE_CLIENT_ID=sua-client-id-aqui.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-seu-client-secret-aqui
```

**⚠️ IMPORTANTE:**
- Substitua `sua-client-id-aqui` pelo Client ID copiado
- Substitua `seu-client-secret-aqui` pelo Client Secret copiado
- **NÃO COMMITE** este arquivo no Git!

#### Verificar `.gitignore`:

```bash
# Deve conter:
.env
*.env
.env.local
```

---

### Passo 7: Verificar Configuração do Spring Boot

#### Arquivo: `src/main/resources/application-dev.properties`

```properties
# OAuth2 Google (lê de variáveis de ambiente)
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# URLs permitidas (sem barra no final!)
app.frontend.url=${FRONTEND_URL:http://localhost:4200}
app.backend.url=${BACKEND_URL:http://localhost:8080}
```

---

### Passo 8: Reiniciar Aplicação

```bash
cd ficha-controlador
docker compose down
docker compose up --build
```

---

## 🧪 Testar OAuth2

### 1. Acessar Aplicação
```
http://localhost:4200
```

### 2. Clicar em "Entrar com Google"

### 3. Deve aparecer:
```
Choose an account
demetrio7500@gmail.com
```

### 4. Selecionar conta

### 5. Tela de Consentimento:
```
Ficha Controlador RPG wants to access your Google Account

This will allow Ficha Controlador RPG to:
✓ See your primary Google Account email address
✓ See your personal info, including any personal info you've made publicly available

[Cancel] [Continue]
```

### 6. Clicar em **Continue**

### 7. **SUCESSO!** Redireciona para home logado

---

## 🐛 Erros Comuns

### Erro 1: "invalid_request"
**Causa:** Redirect URI não está configurado corretamente

**Solução:**
1. Google Cloud Console > Credentials
2. Editar OAuth client ID
3. Verificar **Authorized redirect URIs:**
   ```
   http://localhost:8080/login/oauth2/code/google
   ```

---

### Erro 2: "redirect_uri_mismatch"
**Causa:** URL de redirect não coincide exatamente

**Solução:**
1. Verificar logs do backend:
   ```bash
   docker compose logs backend | grep redirect
   ```
2. Copiar a URL exata que o Spring está usando
3. Adicionar no Google Cloud Console

---

### Erro 3: "access_denied"
**Causa:** Usuário não está na lista de test users

**Solução:**
1. Google Cloud Console > OAuth consent screen
2. Scroll até **Test users**
3. Adicionar: `demetrio7500@gmail.com`

---

### Erro 4: "unauthorized_client"
**Causa:** Client ID ou Secret incorretos

**Solução:**
1. Verificar `.env`:
   ```bash
   cat .env
   ```
2. Comparar com Google Cloud Console
3. Reiniciar: `docker compose restart backend`

---

## 🔒 Segurança

### ⚠️ NUNCA COMMITAR:
```bash
.env
application-dev-local.properties
```

### ✅ Sempre usar variáveis de ambiente:
```properties
${GOOGLE_CLIENT_ID}
${GOOGLE_CLIENT_SECRET}
```

### 🌍 Para Produção:

1. **Mudar para domínio real:**
   ```
   Authorized JavaScript origins:
   https://seu-dominio.com

   Authorized redirect URIs:
   https://seu-dominio.com/login/oauth2/code/google
   ```

2. **Publicar App (após testes):**
   - Google Cloud Console > OAuth consent screen
   - Clicar em **PUBLISH APP**
   - Submeter para verificação do Google

---

## 📋 Checklist de Configuração

- [ ] Projeto criado no Google Cloud Console
- [ ] Google+ API ou People API habilitada
- [ ] OAuth consent screen configurado
- [ ] Email adicionado em Test users
- [ ] OAuth client ID criado
- [ ] Client ID e Secret copiados
- [ ] `.env` criado com credenciais
- [ ] `.env` adicionado ao `.gitignore`
- [ ] Redirect URIs configurados corretamente
- [ ] Aplicação reiniciada
- [ ] Login testado com sucesso

---

## 🎯 URLs Importantes

| Item | URL |
|------|-----|
| Google Cloud Console | https://console.cloud.google.com/ |
| OAuth Consent Screen | https://console.cloud.google.com/apis/credentials/consent |
| Credentials | https://console.cloud.google.com/apis/credentials |
| APIs Library | https://console.cloud.google.com/apis/library |
| Frontend Local | http://localhost:4200 |
| Backend Local | http://localhost:8080 |
| OAuth Callback | http://localhost:8080/login/oauth2/code/google |

---

## 📞 Suporte

Se ainda tiver problemas:

1. **Verificar logs do backend:**
   ```bash
   docker compose logs backend | grep -i oauth
   ```

2. **Verificar logs do frontend:**
   ```bash
   docker compose logs frontend | grep -i error
   ```

3. **Testar endpoint diretamente:**
   ```bash
   curl http://localhost:8080/oauth2/authorization/google
   ```

---

## ✅ Após Configurar

Você poderá:
- ✅ Fazer login com Google
- ✅ Obter informações do usuário (email, nome, foto)
- ✅ Criar sessão autenticada
- ✅ Acessar rotas protegidas
- ✅ Fazer logout

---

**Última Atualização:** 31/01/2026 23:20  
**Status:** Guia completo de configuração OAuth2
