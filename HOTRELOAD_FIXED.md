# ✅ Hot-Reload Configurado e Funcionando

**Data:** 31/01/2026  
**Status:** ✅ CORRIGIDO

---

## 🔧 Problemas Corrigidos

### 1. Backend - Volume Mount  
**Problema:** Código fonte não estava sendo montado no container  
**Solução:** Volume mount do diretório completo `.:/app:cached`

### 2. Backend - CMD Missing
**Problema:** Container não iniciava - sem comando definido  
**Solução:** Adicionado `CMD ["./mvnw", "spring-boot:run"]` no Dockerfile

### 3. Frontend - Erros TypeScript
**Problemas:**
- ❌ `currentUser$` não existe (deve usar `currentUser()` Signal)
- ❌ Parâmetros não usados `route` e `state`  
- ❌ Import `effect` não usado

**Soluções:**
- ✅ Prefixado parâmetros não usados com `_`
- ✅ Removido import `effect`
- ✅ `role.guard.ts` agora usa Signal direto

### 4. Maven Cache Volume
**Adicionado:** Volume nomeado `maven-cache` para cache do Maven

---

## 🚀 Como Usar Agora

### Comandos Rápidos

```bash
# Parar tudo
cd ficha-controlador
docker compose down

# Reconstruir e iniciar (primeira vez)
docker compose up --build

# Iniciar (já construído)
docker compose up

# Ver logs em tempo real
docker compose logs -f

# Parar sem remover
docker compose stop
```

### Usando o Script Helper

```bash
# Dar permissão
chmod +x dev.sh

# Ver comandos disponíveis
./dev.sh help

# Iniciar tudo
./dev.sh start

# Ver logs do backend
./dev.sh logs-be

# Ver logs do frontend
./dev.sh logs-fe

# Reconstruir backend
./dev.sh rebuild-be

# Limpar tudo
./dev.sh clean
```

---

## ✅ Hot-Reload Funcionando

### Backend
1. Edite qualquer `.java` em `src/main/java/`
2. Salve o arquivo (Ctrl+S / Cmd+S)
3. Aguarde ~3-5 segundos
4. Spring DevTools recarrega automaticamente

**Logs esperados:**
```
Restarting due to 1 class path change
...
Started FichaControladorApplication in 2.345 seconds
```

### Frontend
1. Edite qualquer arquivo em `src/app/`
2. Salve o arquivo
3. Aguarde ~1-2 segundos
4. Browser atualiza automaticamente

**Logs esperados:**
```
✔ Browser application bundle generation complete.
✔ Compiled successfully.
```

---

## 📋 Estrutura Final

### compose.yaml
```yaml
backend:
  volumes:
    - .:/app:cached          # Todo o projeto
    - /app/target            # Excluir target do host
    - maven-cache:/root/.m2  # Cache do Maven

frontend:
  volumes:
    - ../frontend/src:/app/src:rw  # Código fonte
    - /app/node_modules              # Excluir node_modules
```

### Dockerfile (Backend)
```dockerfile
# Development stage
FROM maven:3.9-eclipse-temurin-25 AS development
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B
EXPOSE 8080 35729
CMD ["./mvnw", "spring-boot:run"]  # ✅ ADICIONADO
```

---

## 🧪 Testar Hot-Reload

### Backend
```bash
# 1. Iniciar
docker compose up

# 2. Editar arquivo
# Abra: src/main/java/.../controller/AuthController.java
# Mude alguma string de resposta

# 3. Salvar e observar logs
docker compose logs -f backend

# 4. Testar
curl http://localhost:8080/api/user
```

### Frontend
```bash
# 1. Editar componente
# Abra: src/app/pages/login/login.component.html
# Mude algum texto

# 2. Salvar e observar
# Browser atualiza automaticamente
# OU veja logs:
docker compose logs -f frontend
```

---

## 🐛 Troubleshooting

### Backend continua "No sources to compile"
```bash
# Verificar se volumes estão montados
docker exec -it rpg-backend ls -la /app/src

# Deve mostrar os arquivos do projeto
# Se não mostrar, reconstruir:
docker compose down
docker compose up --build
```

### Frontend não recarrega
```bash
# Limpar cache Angular
docker exec -it rpg-frontend rm -rf /app/.angular

# Reiniciar
docker compose restart frontend
```

### Portas em uso
```bash
# Verificar portas
lsof -i :8080
lsof -i :4200

# Matar processos
kill -9 <PID>

# OU parar containers
docker compose down
```

---

## 📊 Portas

| Serviço | Porta | URL |
|---------|-------|-----|
| Backend | 8080 | http://localhost:8080 |
| Backend LiveReload | 35729 | (Interno) |
| Frontend | 4200 | http://localhost:4200 |
| Frontend LiveReload | 49153 | (Interno) |
| PostgreSQL | 5432 | localhost:5432 |

---

## ✅ Checklist

Antes de iniciar desenvolvimento:

- [ ] Docker Desktop rodando
- [ ] `.env` configurado com Google OAuth2
- [ ] Portas 8080, 4200, 5432 livres
- [ ] `docker compose up --build` executado
- [ ] Backend iniciou sem erros
- [ ] Frontend iniciou sem erros
- [ ] http://localhost:4200 acessível
- [ ] http://localhost:8080/actuator/health retorna UP

---

## 🎯 Workflow Ideal

```bash
# Terminal 1 - Logs
cd ficha-controlador
docker compose logs -f backend frontend

# Terminal 2 - Comandos
cd ficha-controlador
# editar código...
# salvar...
# mudanças aparecem automaticamente!

# Para parar (Ctrl+C nos dois terminais)
docker compose down
```

---

**Status:** ✅ HOT-RELOAD 100% FUNCIONAL  
**Última Atualização:** 31/01/2026 20:10
