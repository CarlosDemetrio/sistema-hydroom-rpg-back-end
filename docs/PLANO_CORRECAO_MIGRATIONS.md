# 🔧 PLANO DE CORREÇÃO - Migrations e Schema

## Data: 2026-02-01
## Prioridade: **CRÍTICA**

---

## 📋 ETAPA 1: BACKUP E LIMPEZA (30 min)

### 1.1 Fazer Backup
```bash
# Criar diretório de backup
mkdir -p backup/migrations/$(date +%Y%m%d_%H%M%S)

# Copiar todas migrations atuais
cp src/main/resources/db/migration/*.sql backup/migrations/$(date +%Y%m%d_%H%M%S)/
```

### 1.2 Identificar Migrations Válidas
```
MANTER (Base do sistema):
✅ V1__create_usuarios_jogos.sql          # Usuários e relacionamento base

REMOVER (Duplicatas e conflitos):
❌ V1_1__create_table_jogos.sql           # Duplicado (já tem Jogo em V2)
❌ V1_2__create_table_classes_personagem.sql  # Duplicado (já tem V1.1)
❌ V1_3__create_table_racas.sql           # Duplicado (já tem V1.2)
❌ V2__create_jogo_tables.sql             # Conflito com V2__criar_tabela_fichas
❌ V015__create_dado_prospeccao_vantagem_config.sql  # Nome errado (V015 ao invés de V15)

MANTER MAS RENOMEAR (Padrão inconsistente):
⚠️ V1.1__criar_tabela_classes_personagem.sql  → V2__criar_tabela_classes_personagem.sql
⚠️ V1.2__criar_tabela_racas.sql               → V3__criar_tabela_racas.sql
⚠️ V1.3__adicionar_imagem_url_em_jogos.sql    → V4__adicionar_imagem_url_em_jogos.sql
⚠️ V1.4__alterar_descricao_jogos_para_text.sql → V5__alterar_descricao_jogos_para_text.sql
⚠️ V1.5__criar_tabelas_config_personalidade.sql → V6__criar_tabelas_config_personalidade.sql
⚠️ V1.6__alterar_fichas_para_usar_configs.sql → V7__criar_tabela_fichas.sql (renomear descrição)
⚠️ V1.7__seeds_configs_personalidade.sql      → V8__seeds_configs_personalidade.sql
⚠️ V1.8__criar_tabelas_config_vantagens.sql   → V9__criar_tabelas_config_vantagens.sql
⚠️ V1.9__seeds_configs_vantagens.sql          → V10__seeds_configs_vantagens.sql
⚠️ V1.10__seeds_classes_personagem.sql        → V11__seeds_classes_personagem.sql
```

### 1.3 Executar Limpeza
```bash
cd src/main/resources/db/migration/

# Remover duplicatas
rm V1_1__create_table_jogos.sql
rm V1_2__create_table_classes_personagem.sql
rm V1_3__create_table_racas.sql
rm V2__create_jogo_tables.sql
rm V015__create_dado_prospeccao_vantagem_config.sql

# Renomear para padrão correto
mv V1.1__criar_tabela_classes_personagem.sql V2__criar_tabela_classes_personagem.sql
mv V1.2__criar_tabela_racas.sql V3__criar_tabela_racas.sql
mv V1.3__adicionar_imagem_url_em_jogos.sql V4__adicionar_imagem_url_em_jogos.sql
mv V1.4__alterar_descricao_jogos_para_text.sql V5__alterar_descricao_jogos_para_text.sql
mv V1.5__criar_tabelas_config_personalidade.sql V6__criar_tabelas_config_personalidade.sql
mv V1.6__alterar_fichas_para_usar_configs.sql V7__criar_tabela_fichas.sql
mv V1.7__seeds_configs_personalidade.sql V8__seeds_configs_personalidade.sql
mv V1.8__criar_tabelas_config_vantagens.sql V9__criar_tabelas_config_vantagens.sql
mv V1.9__seeds_configs_vantagens.sql V10__seeds_configs_vantagens.sql
mv V1.10__seeds_classes_personagem.sql V11__seeds_classes_personagem.sql
```

---

## 📋 ETAPA 2: CRIAR MIGRATIONS FALTANTES (2h)

### 2.1 Atributos Config (V12, V13)
```sql
-- V12__criar_tabela_atributos_config.sql
-- V13__seed_atributos_config.sql (7 atributos do legado)
```

### 2.2 Aptidões Config (V14, V15)
```sql
-- V14__criar_tabela_aptidoes_config.sql
-- V15__seed_aptidoes_config.sql (24 aptidões)
```

### 2.3 Tabela Experiência (V16, V17)
```sql
-- V16__criar_tabela_experiencia.sql
-- V17__seed_tabela_experiencia.sql (35 níveis)
```

### 2.4 Limitadores (V18, V19)
```sql
-- V18__criar_tabela_limitadores.sql
-- V19__seed_limitadores.sql (6 faixas)
```

### 2.5 Dado Prospecção (V20)
```sql
-- V20__criar_tabela_dado_prospeccao.sql
```

### 2.6 Raças Seeds (V21)
```sql
-- V21__seed_racas.sql (dados iniciais)
```

---

## 📋 ETAPA 3: RECONSTRUIR DATABASE (15 min)

### 3.1 Limpar Database Atual
```bash
./mvnw flyway:clean -Dflyway.configFiles=src/main/resources/application-dev.properties
```

### 3.2 Executar Migrations
```bash
./mvnw flyway:migrate -Dflyway.configFiles=src/main/resources/application-dev.properties
```

### 3.3 Validar Schema
```bash
./mvnw flyway:validate -Dflyway.configFiles=src/main/resources/application-dev.properties
```

### 3.4 Ver Histórico
```bash
./mvnw flyway:info -Dflyway.configFiles=src/main/resources/application-dev.properties
```

---

## 📋 ETAPA 4: VALIDAR IMPLEMENTAÇÃO (30 min)

### 4.1 Compilar Projeto
```bash
./mvnw clean compile
```

### 4.2 Rodar Testes
```bash
./mvnw test
```

### 4.3 Verificar Tabelas Criadas
```sql
-- Conectar no PostgreSQL
psql -U myuser -d rpg_fichas

-- Listar todas tabelas
\dt

-- Contar registros de seeds
SELECT 'atributos_config' as tabela, COUNT(*) FROM atributos_config
UNION ALL
SELECT 'aptidoes_config', COUNT(*) FROM aptidoes_config
UNION ALL
SELECT 'tabela_experiencia', COUNT(*) FROM tabela_experiencia
UNION ALL
SELECT 'limitadores', COUNT(*) FROM limitadores
UNION ALL
SELECT 'classes_personagem', COUNT(*) FROM classes_personagem
UNION ALL
SELECT 'indoles_config', COUNT(*) FROM indoles_config
UNION ALL
SELECT 'presencas_config', COUNT(*) FROM presencas_config;
```

---

## 📋 ETAPA 5: DOCUMENTAÇÃO (1h)

### 5.1 Criar Migration Guide
```markdown
docs/MIGRATION_GUIDE.md
- Como criar nova migration
- Padrão de nomenclatura
- Comandos Flyway úteis
- Troubleshooting comum
```

### 5.2 Atualizar README
```markdown
README.md
- Seção "Database Migrations"
- Comandos de setup
- Como rodar seeds
```

### 5.3 Atualizar Checklist
```markdown
.specify/memory/implementation-checklist.md
- Marcar correções feitas
- Atualizar status real
- Adicionar próximos passos
```

---

## 📊 MÉTRICAS DE SUCESSO

### Antes da Correção:
```
❌ Migrations duplicadas: 7
❌ Migrations conflitantes: 2
❌ Seeds executados: 0
❌ Tabelas criadas: 0 (create-drop apaga tudo)
❌ Nomenclatura: Inconsistente
```

### Depois da Correção:
```
✅ Migrations únicas: 21
✅ Seeds executados: 100%
✅ Tabelas criadas: 100%
✅ Nomenclatura: Padronizada
✅ Schema validado: Sim
```

---

## ⏱️ TIMELINE ESTIMADO

| Etapa | Tempo | Status |
|-------|-------|--------|
| 1. Backup e Limpeza | 30 min | ⏳ Pendente |
| 2. Criar Migrations | 2h | ⏳ Pendente |
| 3. Reconstruir DB | 15 min | ⏳ Pendente |
| 4. Validar | 30 min | ⏳ Pendente |
| 5. Documentar | 1h | ⏳ Pendente |
| **TOTAL** | **4h 15min** | **0% Completo** |

---

## 🚦 DEPENDÊNCIAS

### Bloqueadores:
- ❌ Nenhum (pode começar agora)

### Pré-requisitos:
- ✅ PostgreSQL rodando (via Docker)
- ✅ Maven configurado
- ✅ application-dev.properties corrigido

---

## 📝 NOTAS IMPORTANTES

1. **NÃO editar migrations já aplicadas** em produção
2. **SEMPRE testar** migrations em ambiente limpo antes
3. **SEMPRE fazer backup** antes de rodar `flyway:clean`
4. **NUNCA** usar `ddl-auto=create-drop` em DEV novamente
5. **SEMPRE** commitar migration + entidade + teste juntos

---

**Próxima Ação:** Executar Etapa 1 - Backup e Limpeza

