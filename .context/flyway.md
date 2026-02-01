# Flyway - Best Practices Context

## Core Principles

Flyway is a **database migration tool** that versions your database schema using SQL or Java-based migrations.

## Configuration

### Maven Dependency
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

### Application Properties
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true
spring.flyway.out-of-order=false
```

## Migration File Naming Convention

### Format
```
V{version}__{description}.sql
```

### Examples
```
V1__criar_tabela_usuarios.sql
V2__criar_tabela_fichas.sql
V3__adicionar_coluna_ativo_usuarios.sql
V4__criar_indices_performance.sql
V1.1__adicionar_dados_seed.sql
```

### Rules
1. **Version** must be unique and sequential
2. **Separator** is double underscore `__`
3. **Description** uses underscores instead of spaces
4. **Extension** must be `.sql`
5. **Never modify** an applied migration

## Best Practices

### ✅ DO

1. **Use sequential versioning** - V1, V2, V3, etc.
2. **Descriptive names** - V1__criar_tabela_usuarios.sql
3. **One logical change per migration** - Don't mix table creation with data
4. **Always add IF NOT EXISTS** for production safety
5. **Add comments** explaining complex changes
6. **Use transactions** - Wrap in BEGIN/COMMIT when supported
7. **Create separate migrations for data** - V1.1__seed_data.sql
8. **Test migrations** before committing
9. **Use DOWN migrations sparingly** - Flyway supports undo migrations
10. **Version control all migrations** - Never delete from git

### ❌ DON'T

1. **DON'T modify applied migrations** - Create new migration instead
2. **DON'T use spaces in filenames** - Use underscores
3. **DON'T skip versions** - Keep sequential
4. **DON'T put multiple logical changes** in one file
5. **DON'T use database-specific syntax** unless necessary
6. **DON'T forget indexes** - Add in separate migration after data
7. **DON'T ignore failed migrations** - Fix and clean before retry

## Migration Structure

### 1. Table Creation
```sql
-- V1__criar_tabela_usuarios.sql
-- Cria tabela de usuários do sistema

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'google',
    foto_url VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0,
    
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_provider_id UNIQUE (provider_id),
    CONSTRAINT chk_usuario_nome CHECK (CHAR_LENGTH(nome) >= 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentários
ALTER TABLE usuarios COMMENT = 'Armazena usuários autenticados via OAuth2';
```

### 2. Adding Column
```sql
-- V3__adicionar_coluna_ultimo_acesso.sql
-- Adiciona coluna para rastrear último acesso do usuário

ALTER TABLE usuarios 
ADD COLUMN ultimo_acesso TIMESTAMP NULL 
AFTER ativo;

-- Comentário
ALTER TABLE usuarios 
MODIFY COLUMN ultimo_acesso TIMESTAMP NULL 
COMMENT 'Data/hora do último acesso do usuário';
```

### 3. Creating Indexes
```sql
-- V4__criar_indices_performance.sql
-- Adiciona índices para melhorar performance de queries

-- Índice para busca por email
CREATE INDEX IF NOT EXISTS idx_usuario_email 
ON usuarios(email);

-- Índice para busca por provider_id
CREATE INDEX IF NOT EXISTS idx_usuario_provider_id 
ON usuarios(provider_id);

-- Índice composto para usuários ativos
CREATE INDEX IF NOT EXISTS idx_usuario_ativo_email 
ON usuarios(ativo, email);

-- Comentário nos índices
ALTER TABLE usuarios 
ADD KEY idx_usuario_email (email) 
COMMENT 'Índice para busca rápida por email';
```

### 4. Adding Foreign Key
```sql
-- V5__criar_relacionamento_ficha_usuario.sql
-- Adiciona FK entre fichas e usuários

ALTER TABLE fichas 
ADD CONSTRAINT fk_ficha_usuario 
FOREIGN KEY (usuario_id) 
REFERENCES usuarios(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

-- Índice na FK
CREATE INDEX IF NOT EXISTS idx_ficha_usuario_id 
ON fichas(usuario_id);
```

### 5. Seed Data
```sql
-- V1.1__seed_dados_iniciais.sql
-- Popula dados iniciais do sistema

-- Usuário admin padrão (apenas para DEV)
INSERT INTO usuarios (nome, email, provider_id, provider, ativo)
VALUES ('Admin', 'admin@example.com', 'admin-google-id', 'google', TRUE)
ON DUPLICATE KEY UPDATE nome = nome; -- Não sobrescreve se já existe
```

### 6. Altering Table
```sql
-- V6__modificar_tamanho_coluna_nome.sql
-- Aumenta tamanho da coluna nome para 150 caracteres

ALTER TABLE usuarios 
MODIFY COLUMN nome VARCHAR(150) NOT NULL 
COMMENT 'Nome completo do usuário';

-- Atualiza constraint
ALTER TABLE usuarios 
DROP CONSTRAINT chk_usuario_nome,
ADD CONSTRAINT chk_usuario_nome 
CHECK (CHAR_LENGTH(nome) >= 3 AND CHAR_LENGTH(nome) <= 150);
```

## Advanced Patterns

### 1. Conditional Migration
```sql
-- V7__migrar_dados_antigos.sql
-- Migra dados do sistema antigo

-- Criar tabela temporária
CREATE TEMPORARY TABLE IF NOT EXISTS temp_migration AS
SELECT * FROM old_users WHERE migrated = FALSE;

-- Inserir dados
INSERT INTO usuarios (nome, email, provider_id, provider)
SELECT name, email, CONCAT('legacy-', id), 'legacy'
FROM temp_migration;

-- Marcar como migrado
UPDATE old_users 
SET migrated = TRUE 
WHERE id IN (SELECT id FROM temp_migration);

-- Limpar temporária
DROP TEMPORARY TABLE IF EXISTS temp_migration;
```

### 2. Data Transformation
```sql
-- V8__normalizar_emails.sql
-- Normaliza emails para lowercase

UPDATE usuarios 
SET email = LOWER(email)
WHERE email != LOWER(email);
```

### 3. Schema Evolution
```sql
-- V9__adicionar_auditoria_envers.sql
-- Adiciona tabelas de auditoria para Envers

CREATE TABLE IF NOT EXISTS usuarios_AUD (
    id BIGINT NOT NULL,
    REV INT NOT NULL,
    REVTYPE TINYINT,
    nome VARCHAR(150),
    email VARCHAR(255),
    ativo BOOLEAN,
    PRIMARY KEY (id, REV)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS REVINFO (
    REV INT AUTO_INCREMENT PRIMARY KEY,
    REVTSTMP BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- FK
ALTER TABLE usuarios_AUD 
ADD CONSTRAINT fk_usuarios_aud_revinfo 
FOREIGN KEY (REV) REFERENCES REVINFO(REV);
```

## Testing Migrations

### Local Test
```bash
# Apply migrations
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Repair if needed (caution!)
./mvnw flyway:repair

# Validate migrations
./mvnw flyway:validate
```

### Test with H2
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

## Rollback Strategies

### 1. Undo Migration (Not Recommended)
```sql
-- U9__remover_auditoria_envers.sql
DROP TABLE IF EXISTS usuarios_AUD;
DROP TABLE IF EXISTS REVINFO;
```

### 2. Forward Fix (Recommended)
```sql
-- V10__corrigir_erro_migracao_9.sql
-- Corrige erro na migração V9

ALTER TABLE usuarios_AUD 
MODIFY COLUMN nome VARCHAR(150) NULL;
```

## Production Deployment

### Before Deploy
1. **Test migrations** on staging
2. **Backup database** before running migrations
3. **Review migration** scripts
4. **Check dependencies** between migrations
5. **Verify rollback strategy**

### During Deploy
1. **Run migrations first** before deploying application
2. **Monitor logs** for errors
3. **Verify schema** after migration

### After Deploy
1. **Validate data integrity**
2. **Check application** connects properly
3. **Monitor performance** of new indexes

## Common Issues

### 1. Checksum Mismatch
```bash
# Cause: Migration file was modified after being applied
# Fix: Use repair (caution!) or create new migration
./mvnw flyway:repair
```

### 2. Failed Migration
```bash
# Cause: SQL error in migration
# Fix: 
# 1. Fix the error manually in database
# 2. Mark as successful: UPDATE flyway_schema_history SET success = 1 WHERE version = 'X';
# OR
# 3. Delete failed entry and fix migration file
```

### 3. Out of Order
```properties
# Allow out-of-order migrations (not recommended for production)
spring.flyway.out-of-order=true
```

## Monitoring

### Check Migration History
```sql
SELECT * FROM flyway_schema_history 
ORDER BY installed_rank;
```

### Find Failed Migrations
```sql
SELECT * FROM flyway_schema_history 
WHERE success = 0;
```

## Integration with Spring Boot

### Auto-configuration
```java
@Configuration
public class FlywayConfig {
    
    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            // Only for DEV - NEVER in production
            if (isProdProfile()) {
                flyway.migrate();
            } else {
                flyway.clean(); // Dangerous!
                flyway.migrate();
            }
        };
    }
}
```

### Custom Callbacks
```java
@Component
public class FlywayCallback implements org.flywaydb.core.api.callback.Callback {
    
    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.AFTER_MIGRATE;
    }
    
    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }
    
    @Override
    public void handle(Event event, Context context) {
        log.info("Migration completed successfully!");
    }
}
```

## Resources

- Official Docs: https://flywaydb.org/documentation/
- Spring Boot Integration: https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway
- Best Practices: https://flywaydb.org/documentation/concepts/migrations
