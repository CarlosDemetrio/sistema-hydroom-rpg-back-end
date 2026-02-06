# Repositories

## Template Base

```java
/**
 * Repositório para gerenciamento de MyEntity.
 * 
 * @author Seu Nome
 * @since 1.0
 */
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    
    /**
     * Busca entidade por nome.
     * 
     * @param name Nome exato
     * @return Optional contendo a entidade
     */
    Optional<MyEntity> findByName(String name);
    
    /**
     * Verifica se existe entidade com o nome.
     * 
     * @param name Nome a verificar
     * @return true se existe
     */
    boolean existsByName(String name);
    
    /**
     * Busca entidades por ID do usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de entidades (nunca null)
     */
    List<MyEntity> findByUserId(Long userId);
}
```

## Query Methods

### Naming Convention
```java
// find = SELECT
findByName(String name)
findByNameAndActive(String name, boolean active)
findByCreatedAtAfter(LocalDateTime date)

// exists = SELECT COUNT > 0
existsByName(String name)
existsByIdAndUserId(Long id, Long userId)

// count = SELECT COUNT
countByActive(boolean active)

// delete = DELETE
deleteByIdAndUserId(Long id, Long userId)
```

## Custom Queries

```java
/**
 * Busca entidades por parte do nome (case-insensitive).
 */
@Query("SELECT e FROM MyEntity e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
List<MyEntity> searchByName(@Param("name") String name);

/**
 * Busca com JOIN FETCH para evitar N+1.
 */
@Query("SELECT e FROM MyEntity e LEFT JOIN FETCH e.related WHERE e.id = :id")
Optional<MyEntity> findByIdWithRelated(@Param("id") Long id);
```

## Optional Best Practices

### ✅ Quando usar Optional
```java
// Métodos find* que retornam uma única entidade
Optional<User> findById(Long id);
Optional<User> findByEmail(String email);
Optional<Game> findByNomeAndMestreId(String nome, Long mestreId);
```

### ✅ Quando usar boolean
```java
// Verificações de existência (mais eficiente)
boolean existsById(Long id);
boolean existsByEmail(String email);
```

### ✅ Quando usar List
```java
// Métodos find* que retornam múltiplas entidades
List<Game> findByMestreId(Long mestreId);
List<Ficha> findByJogoId(Long jogoId);
```

## Performance Tips

### Use @EntityGraph para evitar N+1
```java
@EntityGraph(attributePaths = {"mestre", "participantes"})
List<Game> findAll();
```

### Use Projection para queries grandes
```java
public interface GameSummary {
    Long getId();
    String getNome();
    String getMestreNome();
}

List<GameSummary> findAllProjectedBy();
```

## Best Practices

### ✅ DO
- Extend JpaRepository
- Optional para single results
- List para multiple results
- boolean para exists checks
- JavaDoc completo
- @Query apenas para queries complexas

### ❌ DON'T
- Retornar null
- Lógica de negócio no repository
- Queries N+1
- Queries desnecessariamente complexas
- Missing documentation
