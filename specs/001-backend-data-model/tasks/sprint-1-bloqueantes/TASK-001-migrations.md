# TASK-001: Reorganizar Migrations Flyway

**Sprint**: 1 - Bloqueantes  
**Prioridade**: P0 - CRÍTICO  
**Estimativa**: 2 horas  
**Status**: ❌ Não Iniciado

---

## 🎯 OBJETIVO

Reorganizar todas as migrations do Flyway usando padrão timestamp para evitar conflitos de versionamento e garantir ordem de execução correta.

---

## 🔴 PROBLEMA ATUAL

### Migrations Duplicadas/Conflitantes
```
V1_1__create_table_jogos.sql                    ❌ 
V1.1__criar_tabela_classes_personagem.sql       ❌ CONFLITO!
V1_2__create_table_classes_personagem.sql       ❌
V1.2__criar_tabela_racas.sql                    ❌ CONFLITO!
V2__create_jogo_tables.sql                      ❌
V2__criar_tabela_fichas.sql                     ❌ CONFLITO!
... (mais duplicatas)
```

**Problemas**:
- Flyway não executa na ordem correta
- Conflitos de versionamento (V1.1 vs V1_1)
- Impossível saber ordem de criação
- Build pode falhar aleatoriamente

---

## ✅ SOLUÇÃO

### Novo Padrão: Timestamp

```
YYYYMMDDHHMMSS__descricao_clara.sql

Exemplo:
20260201120000__create_table_usuarios.sql
20260201120100__create_table_jogos.sql
20260201120200__create_table_classes_personagem.sql
```

**Vantagens**:
- ✅ Ordem cronológica garantida
- ✅ Sem conflitos de versão
- ✅ Fácil identificar quando foi criado
- ✅ Padrão recomendado Flyway

---

## 📋 CHECKLIST DE EXECUÇÃO

### 1. Backup (5 min)
- [ ] Fazer backup da pasta `src/main/resources/db/migration/`
- [ ] Copiar para `src/main/resources/db/migration.bak/`
- [ ] Commit atual do git (safety)

### 2. Mapear Ordem Correta (15 min)
- [ ] Listar TODAS as migrations atuais
- [ ] Definir ordem lógica de dependências:
  1. Core (usuarios, autenticacao)
  2. Jogos (jogos, participantes)
  3. Configs Base (atributos, aptidoes, bonus)
  4. Configs RPG (classes, racas, vantagens, niveis)
  5. Fichas (ficha, ficha_*, relacionamentos)
  6. Seeds (dados padrão)

### 3. Deletar Migrations Antigas (5 min)
- [ ] Deletar TODAS migrations em `db/migration/`
- [ ] Confirmar pasta vazia
- [ ] Limpar histórico Flyway no H2 (se existir)

### 4. Criar Novas Migrations (60 min)

#### Grupo 1: Core (20260201120000 - 20260201120500)
```sql
- [ ] 20260201120000__create_table_usuarios.sql
      CREATE TABLE usuarios (
          id BIGINT PRIMARY KEY AUTO_INCREMENT,
          email VARCHAR(255) NOT NULL UNIQUE,
          nome VARCHAR(255) NOT NULL,
          provider_id VARCHAR(255) UNIQUE,
          provider VARCHAR(50),
          avatar_url VARCHAR(500),
          ativo BOOLEAN DEFAULT TRUE,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      );

- [ ] 20260201120100__create_indexes_usuarios.sql
      CREATE INDEX idx_usuarios_email ON usuarios(email);
      CREATE INDEX idx_usuarios_provider_id ON usuarios(provider_id);
      CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);
```

#### Grupo 2: Jogos (20260201120500 - 20260201121000)
```sql
- [ ] 20260201120500__create_table_jogos.sql
- [ ] 20260201120600__create_table_jogo_participantes.sql
- [ ] 20260201120700__create_indexes_jogos.sql
```

#### Grupo 3: Configs Base (20260201121000 - 20260201122000)
```sql
- [ ] 20260201121000__create_table_config_atributos.sql
- [ ] 20260201121100__create_table_config_aptidoes.sql
- [ ] 20260201121200__create_table_config_bonus.sql
- [ ] 20260201121300__create_table_config_membros_corpo.sql
- [ ] 20260201121400__create_table_config_niveis.sql
- [ ] 20260201121500__create_indexes_configs_base.sql
```

#### Grupo 4: Configs RPG (20260201122000 - 20260201123000)
```sql
- [ ] 20260201122000__create_table_classes_personagem.sql
- [ ] 20260201122100__create_table_classe_bonus_nivel.sql        ⭐ NOVA
- [ ] 20260201122200__create_table_racas.sql
- [ ] 20260201122300__create_table_raca_bonus_atributo.sql       ⭐ NOVA
- [ ] 20260201122400__create_table_categorias_vantagem.sql
- [ ] 20260201122500__create_table_vantagens.sql
- [ ] 20260201122600__create_table_vantagem_efeitos.sql          ⭐ NOVA
- [ ] 20260201122700__create_table_config_generos.sql
- [ ] 20260201122800__create_table_config_indoles.sql
- [ ] 20260201122900__create_table_config_presencas.sql
- [ ] 20260201123000__create_indexes_configs_rpg.sql
```

#### Grupo 5: Fichas (20260201123000 - 20260201124000)
```sql
- [ ] 20260201123100__create_table_fichas.sql
- [ ] 20260201123200__create_table_ficha_atributos.sql
- [ ] 20260201123300__create_table_ficha_aptidoes.sql
- [ ] 20260201123400__create_table_ficha_bonus.sql
- [ ] 20260201123500__create_table_ficha_vida.sql
- [ ] 20260201123600__create_table_ficha_vida_membros.sql
- [ ] 20260201123700__create_table_ficha_essencia.sql
- [ ] 20260201123800__create_table_ficha_ameaca.sql
- [ ] 20260201123900__create_table_ficha_prospeccao.sql
- [ ] 20260201124000__create_table_ficha_vantagens.sql
- [ ] 20260201124100__create_indexes_fichas.sql
```

#### Grupo 6: Seeds (20260201130000 - 20260201140000)
```sql
- [ ] 20260201130000__seed_config_atributos.sql
- [ ] 20260201130100__seed_config_aptidoes.sql
- [ ] 20260201130200__seed_config_bonus.sql
- [ ] 20260201130300__seed_config_membros_corpo.sql
- [ ] 20260201130400__seed_config_niveis.sql
- [ ] 20260201130500__seed_classes_personagem.sql
- [ ] 20260201130600__seed_classe_bonus_nivel.sql
- [ ] 20260201130700__seed_racas.sql
- [ ] 20260201130800__seed_raca_bonus_atributo.sql
- [ ] 20260201130900__seed_categorias_vantagem.sql
- [ ] 20260201131000__seed_vantagens.sql
- [ ] 20260201131100__seed_vantagem_efeitos.sql
- [ ] 20260201131200__seed_config_generos.sql
- [ ] 20260201131300__seed_config_indoles.sql
- [ ] 20260201131400__seed_config_presencas.sql
```

### 5. Validar Migrations (15 min)
- [ ] Rodar `./mvnw flyway:clean`
- [ ] Rodar `./mvnw flyway:migrate`
- [ ] Verificar TODAS as tabelas criadas no H2
- [ ] Verificar ordem de execução no log
- [ ] Confirmar SEM erros

### 6. Testar Build Completo (5 min)
- [ ] Rodar `./mvnw clean compile`
- [ ] Rodar `./mvnw test`
- [ ] Build SUCCESS
- [ ] Todos testes passando

---

## 📁 ESTRUTURA FINAL

```
src/main/resources/db/migration/
├── 20260201120000__create_table_usuarios.sql
├── 20260201120100__create_indexes_usuarios.sql
├── 20260201120500__create_table_jogos.sql
├── 20260201120600__create_table_jogo_participantes.sql
├── 20260201120700__create_indexes_jogos.sql
├── 20260201121000__create_table_config_atributos.sql
├── 20260201121100__create_table_config_aptidoes.sql
├── 20260201121200__create_table_config_bonus.sql
├── 20260201121300__create_table_config_membros_corpo.sql
├── 20260201121400__create_table_config_niveis.sql
├── 20260201121500__create_indexes_configs_base.sql
├── 20260201122000__create_table_classes_personagem.sql
├── 20260201122100__create_table_classe_bonus_nivel.sql
├── 20260201122200__create_table_racas.sql
├── 20260201122300__create_table_raca_bonus_atributo.sql
├── 20260201122400__create_table_categorias_vantagem.sql
├── 20260201122500__create_table_vantagens.sql
├── 20260201122600__create_table_vantagem_efeitos.sql
├── 20260201122700__create_table_config_generos.sql
├── 20260201122800__create_table_config_indoles.sql
├── 20260201122900__create_table_config_presencas.sql
├── 20260201123000__create_indexes_configs_rpg.sql
├── 20260201123100__create_table_fichas.sql
├── 20260201123200__create_table_ficha_atributos.sql
├── 20260201123300__create_table_ficha_aptidoes.sql
├── 20260201123400__create_table_ficha_bonus.sql
├── 20260201123500__create_table_ficha_vida.sql
├── 20260201123600__create_table_ficha_vida_membros.sql
├── 20260201123700__create_table_ficha_essencia.sql
├── 20260201123800__create_table_ficha_ameaca.sql
├── 20260201123900__create_table_ficha_prospeccao.sql
├── 20260201124000__create_table_ficha_vantagens.sql
├── 20260201124100__create_indexes_fichas.sql
├── 20260201130000__seed_config_atributos.sql
├── 20260201130100__seed_config_aptidoes.sql
├── 20260201130200__seed_config_bonus.sql
├── 20260201130300__seed_config_membros_corpo.sql
├── 20260201130400__seed_config_niveis.sql
├── 20260201130500__seed_classes_personagem.sql
├── 20260201130600__seed_classe_bonus_nivel.sql
├── 20260201130700__seed_racas.sql
├── 20260201130800__seed_raca_bonus_atributo.sql
├── 20260201130900__seed_categorias_vantagem.sql
├── 20260201131000__seed_vantagens.sql
├── 20260201131100__seed_vantagem_efeitos.sql
├── 20260201131200__seed_config_generos.sql
├── 20260201131300__seed_config_indoles.sql
└── 20260201131400__seed_config_presencas.sql
```

---

## ✅ DEFINITION OF DONE

- [ ] Todas migrations antigas deletadas
- [ ] 45+ novas migrations criadas com padrão timestamp
- [ ] Ordem de execução validada
- [ ] Flyway migrate SUCCESS
- [ ] Build completo SUCCESS
- [ ] Testes passando
- [ ] Zero warnings no log
- [ ] Documentação atualizada

---

## 🚨 ATENÇÃO

1. **NÃO** misturar padrões (ou timestamp ou V1, V2)
2. **NÃO** reutilizar timestamps (incrementar sempre)
3. **SEMPRE** testar ordem de execução
4. **SEMPRE** fazer backup antes

---

## 📊 PROGRESSO

```
Backup:                 ⬜ 0/1
Mapeamento:             ⬜ 0/1
Deletar Antigas:        ⬜ 0/1
Criar Core:             ⬜ 0/2
Criar Jogos:            ⬜ 0/3
Criar Configs Base:     ⬜ 0/6
Criar Configs RPG:      ⬜ 0/11
Criar Fichas:           ⬜ 0/11
Criar Seeds:            ⬜ 0/15
Validar:                ⬜ 0/5
Testar:                 ⬜ 0/3
───────────────────────────────
TOTAL:                  0/59 (0%)
```

---

**Criado**: 2026-02-01  
**Atualizado**: 2026-02-01  
**Responsável**: Dev Team
