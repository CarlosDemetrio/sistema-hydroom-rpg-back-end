# Database

## Naming Conventions

### Tables
```sql
-- snake_case
CREATE TABLE user_games (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL
);
```

### Columns
```sql
-- snake_case
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP,
user_id BIGINT,
max_jogadores INTEGER
```

### Foreign Keys
```sql
-- {table}_id
user_id BIGINT REFERENCES users(id),
game_id BIGINT REFERENCES games(id),
mestre_id BIGINT REFERENCES users(id)
```

## JPA Naming Strategy

```properties
# application.properties
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
```

## Entity Mapping

```java
@Entity
@Table(name = "user_games")  // Plural em inglês
public class UserGame {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

## Hibernate DDL Auto

### Development
```properties
# application-dev.properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Production
```properties
# application-prod.properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## Common Types

```java
// String
@Column(length = 100, nullable = false)
private String name;

// Integer
@Column(nullable = false)
private Integer maxJogadores;

// Boolean
@Column(nullable = false)
private Boolean active = true;

// Decimal
@Column(precision = 10, scale = 2)
private BigDecimal value;

// Date/Time
@Column(nullable = false)
private LocalDateTime createdAt;

@Column
private LocalDate birthDate;

// Enum
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private UserRole role;

// Text (sem limite)
@Column(columnDefinition = "TEXT")
private String description;
```

## Relationships

### ManyToOne
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "mestre_id", nullable = false)
private User mestre;
```

### OneToMany
```java
@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Participante> participantes = new ArrayList<>();
```

### ManyToMany
```java
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

## Indexes

```java
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_role", columnList = "role")
    }
)
public class User {
    
    @Column(unique = true, nullable = false)
    private String email;
}
```

## Audit Fields

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;
    
    @LastModifiedBy
    private Long updatedBy;
}

// Habilitar auditing
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // Retorna ID do usuário atual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = (User) auth.getPrincipal();
                return Optional.of(user.getId());
            }
            return Optional.empty();
        };
    }
}
```

## Soft Delete

```java
@Entity
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class User {
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Column
    private LocalDateTime deletedAt;
}
```

## PostgreSQL Specific

### UUID
```java
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
@Column(columnDefinition = "uuid")
private UUID id;
```

### JSON Column
```java
@Column(columnDefinition = "jsonb")
@Type(JsonBinaryType.class)
private Map<String, Object> metadata;
```

### Array
```java
@Column(columnDefinition = "text[]")
@Type(StringArrayType.class)
private String[] tags;
```

## Connection Pool (HikariCP)

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

## Best Practices

### ✅ DO
- snake_case para tables/columns
- FetchType.LAZY para relationships
- Audit fields (createdAt, updatedAt)
- Indexes em foreign keys e colunas de busca
- Constraints no banco
- Connection pool configurado

### ❌ DON'T
- FetchType.EAGER (causa N+1)
- Cascade.ALL indiscriminadamente
- Missing indexes
- Hardcoded table/column names
- Missing audit fields
- Insecure connection strings

## Example Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    picture VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    max_jogadores INTEGER NOT NULL DEFAULT 5,
    mestre_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_game_nome_mestre UNIQUE (nome, mestre_id)
);

CREATE INDEX idx_games_mestre ON games(mestre_id);
CREATE INDEX idx_users_email ON users(email);
```
