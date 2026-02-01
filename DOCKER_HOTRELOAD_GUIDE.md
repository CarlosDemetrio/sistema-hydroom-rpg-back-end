# 🔥 Hot-Reload com Docker Compose

**Status:** ✅ Configurado para desenvolvimento com hot-reload

---

## 🎯 O Que Foi Configurado

### Backend (Spring Boot)
- ✅ **Spring DevTools** habilitado
- ✅ Volume mount do código fonte (`./src`)
- ✅ Volume mount do target (`./target`)
- ✅ Maven cache compartilhado (`~/.m2`)
- ✅ LiveReload na porta 35729
- ✅ Modo desenvolvimento com `mvnw spring-boot:run`

### Frontend (Angular)
- ✅ **Angular Dev Server** com hot-reload
- ✅ Volume mount do código fonte (`./src`)
- ✅ Polling habilitado (`--poll=2000`)
- ✅ Live reload na porta 49153
- ✅ Node_modules isolado no container

---

## 🚀 Como Usar

### 1. Primeira Execução (Build Inicial)
```bash
cd ficha-controlador
docker compose up --build
```

Isso vai:
- Construir as imagens com stage `development`
- Instalar dependências
- Montar volumes do código fonte
- Iniciar os serviços

### 2. Executar Novamente (Sem Rebuild)
```bash
docker compose up
```

As imagens já estão construídas, só precisa iniciar.

### 3. Ver Logs em Tempo Real
```bash
# Todos os serviços
docker compose logs -f

# Apenas backend
docker compose logs -f backend

# Apenas frontend
docker compose logs -f frontend
```

---

## 🔄 Hot-Reload Funcionando

### Backend (Spring Boot)
**Altere qualquer arquivo `.java` em:**
```
ficha-controlador/src/main/java/**/*.java
```

**O que acontece:**
1. Spring DevTools detecta a mudança
2. Recompila apenas as classes alteradas
3. Reinicia o contexto do Spring (rápido ~2-5s)
4. Aplicação atualizada automaticamente

**Logs esperados:**
```
Restarting due to 1 class path change
...
Started FichaControladorApplication in 2.345 seconds
```

### Frontend (Angular)
**Altere qualquer arquivo em:**
```
ficha-controlador-front-end/src/**/*
```

**O que acontece:**
1. Angular CLI detecta a mudança (polling a cada 2s)
2. Recompila incrementalmente
3. Live reload atualiza o browser automaticamente

**Logs esperados:**
```
✔ Browser application bundle generation complete.
✔ Compiled successfully.
```

---

## 📋 Estrutura de Volumes

### Backend
```yaml
volumes:
  - ./src:/app/src:ro              # Código fonte (read-only)
  - ./target:/app/target           # Classes compiladas
  - ~/.m2:/root/.m2:ro             # Maven cache
```

### Frontend
```yaml
volumes:
  - ../ficha-controlador-front-end/src:/app/src:rw  # Código fonte
  - /app/node_modules              # Isolado (não sobrescreve)
  - /app/.angular                  # Cache Angular isolado
```

---

## 🔧 Comandos Úteis

### Reconstruir Apenas um Serviço
```bash
# Backend
docker compose up -d --build backend

# Frontend
docker compose up -d --build frontend
```

### Reiniciar um Serviço (Sem Rebuild)
```bash
docker compose restart backend
docker compose restart frontend
```

### Parar e Remover Containers
```bash
docker compose down
```

### Parar, Remover e Limpar Volumes
```bash
docker compose down -v
```

### Entrar no Container
```bash
# Backend
docker exec -it rpg-backend bash

# Frontend
docker exec -it rpg-frontend sh
```

---

## ⚡ Performance do Hot-Reload

### Backend (Spring DevTools)
| Tipo de Mudança | Tempo |
|-----------------|-------|
| Classe Java simples | ~2-3s |
| Controller/Service | ~3-5s |
| Entity/Model | ~5-7s |
| application.properties | ~5-7s |

### Frontend (Angular)
| Tipo de Mudança | Tempo |
|-----------------|-------|
| Component TS | ~1-2s |
| Template HTML | ~1s |
| Styles CSS | ~1s |
| Service | ~1-2s |

---

## 🐛 Troubleshooting

### Backend não está recarregando

**Problema:** Mudanças no código Java não refletem

**Solução 1:** Verificar se DevTools está habilitado
```bash
docker compose logs backend | grep "LiveReload"
```

Deve mostrar:
```
LiveReload server is running on port 35729
```

**Solução 2:** Reconstruir container
```bash
docker compose up -d --build backend
```

**Solução 3:** Limpar target e rebuildar
```bash
docker exec -it rpg-backend rm -rf /app/target/*
docker compose restart backend
```

---

### Frontend não está recarregando

**Problema:** Mudanças no código Angular não refletem

**Solução 1:** Verificar polling
```bash
docker compose logs frontend | grep "Compiled"
```

**Solução 2:** Aumentar intervalo de polling
Edite `compose.yaml`:
```yaml
command: npm start -- --host 0.0.0.0 --poll=1000
```

**Solução 3:** Limpar cache Angular
```bash
docker exec -it rpg-frontend rm -rf /app/.angular
docker compose restart frontend
```

---

### "Too many open files" Error

**Problema:** Sistema operacional atingiu limite de file watchers

**Solução (Linux/Mac):**
```bash
# Aumentar limite
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

---

### Portas em Uso

**Problema:** Porta já está em uso

**Solução:** Verificar processos usando as portas
```bash
# Backend (8080)
lsof -i :8080

# Frontend (4200)
lsof -i :4200

# Matar processo
kill -9 <PID>
```

---

## 📊 Portas Expostas

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| PostgreSQL | 5432 | Banco de dados |
| Backend | 8080 | API REST |
| Backend LiveReload | 35729 | Spring DevTools |
| Frontend | 4200 | Angular Dev Server |
| Frontend LiveReload | 49153 | Angular Live Reload |

---

## 🎯 Workflow de Desenvolvimento

### 1. Iniciar Ambiente
```bash
docker compose up
```

### 2. Desenvolver
- Edite código no seu editor favorito
- Salve o arquivo (Ctrl+S / Cmd+S)
- Mudanças aplicadas automaticamente!

### 3. Ver Logs
```bash
# Terminal separado
docker compose logs -f backend frontend
```

### 4. Testar
- Backend: http://localhost:8080
- Frontend: http://localhost:4200
- Swagger: http://localhost:8080/swagger-ui.html

### 5. Parar Ambiente
```bash
docker compose down
```

---

## 🔄 Produção vs Desenvolvimento

### Desenvolvimento (Atual)
```yaml
target: development
command: mvnw spring-boot:run  # Backend
command: npm start             # Frontend
volumes: código fonte montado
```

### Produção
```yaml
target: production
# Backend usa JAR compilado
# Frontend usa Nginx com build estático
# Sem volumes de código fonte
```

**Para mudar para produção:**
```bash
# Remover target: development dos builds
docker compose -f compose.prod.yaml up
```

---

## ✅ Checklist

Antes de desenvolver, verifique:

- [ ] Docker Desktop rodando
- [ ] Portas 4200, 8080, 5432 livres
- [ ] `.env` com `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET`
- [ ] `docker compose up` executado com sucesso
- [ ] Logs sem erros críticos
- [ ] http://localhost:4200 acessível
- [ ] http://localhost:8080/actuator/health retorna `UP`

---

## 📝 Dicas

### 1. Use Logs Coloridos
```bash
docker compose logs -f | grep --color=auto "ERROR\|WARN"
```

### 2. Monitore Recursos
```bash
docker stats
```

### 3. Limpe Periodicamente
```bash
# Remover imagens não usadas
docker image prune -a

# Remover volumes não usados
docker volume prune
```

### 4. Use .dockerignore
Garanta que `.dockerignore` existe para evitar copiar arquivos desnecessários.

---

**Última Atualização:** 31/01/2026  
**Status:** ✅ Hot-reload configurado e funcionando
