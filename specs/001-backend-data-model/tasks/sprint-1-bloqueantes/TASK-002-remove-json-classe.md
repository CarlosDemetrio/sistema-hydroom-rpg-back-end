# TASK-002: Remover JSON de ClassePersonagem

**Sprint**: 1 - Bloqueantes  
**Prioridade**: P0 - CRÍTICO  
**Estimativa**: 3 horas  
**Status**: ❌ Não Iniciado

---

## 🎯 OBJETIVO

Refatorar `ClassePersonagem` para remover campo JSON `bonusPorNivel` e substituir por tabela relacional `classe_bonus_nivel`, respeitando requisito de "TUDO configurável via tabelas".

---

## 🔴 PROBLEMA ATUAL

### Código Atual (ERRADO)
```java
@Entity
@Table(name = "classes_personagem")
public class ClassePersonagem {
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "bonus_por_nivel", columnDefinition = "JSON")
    private Map<Integer, Map<String, Integer>> bonusPorNivel;  // ❌ JSON!
}
```

**Exemplo de dados**:
```json
{
  "1": { "FOR": 1, "DES": 0, "CON": 1 },
  "2": { "FOR": 0, "DES": 1, "CON": 0 },
  "3": { "FOR": 1, "DES": 0, "CON": 1 }
}
```

**Problemas**:
- ❌ Viola requisito "SEM JSON"
- ❌ Não é consultável pelo SQL
- ❌ Dificulta validações
- ❌ Impossível criar índices
- ❌ Não segue normalização

---

## ✅ SOLUÇÃO

### Nova Estrutura Relacional

```
classes_personagem (mantém apenas dados da classe)
    ├── id
    ├── jogo_id
    ├── nome
    ├── descricao
    ├── ativa
    └── ...

classe_bonus_nivel (NOVA tabela - muitos-para-muitos com atributos)
    ├── id
    ├── classe_id          FK → classes_personagem
    ├── nivel              1-36
    ├── atributo_id        FK → config_atributos
    ├── bonus_valor        INT
    └── created_at
```

**Exemplo de dados**:
```sql
-- Classe "Guerreiro" ganha +1 FOR no nível 1
INSERT INTO classe_bonus_nivel VALUES
(1, classe_guerreiro_id, 1, atributo_for_id, 1, NOW());

-- Classe "Guerreiro" ganha +1 CON no nível 1
INSERT INTO classe_bonus_nivel VALUES
(2, classe_guerreiro_id, 1, atributo_con_id, 1, NOW());

-- Classe "Guerreiro" ganha +1 DES no nível 2
INSERT INTO classe_bonus_nivel VALUES
(3, classe_guerreiro_id, 2, atributo_des_id, 1, NOW());
```

---

## 📋 CHECKLIST DE EXECUÇÃO

### 1. Criar Nova Entidade (30 min)

#### ClasseBonusNivel.java
- [ ] Criar entidade em `src/main/java/.../model/`
```java
@Entity
@Table(name = "classe_bonus_nivel")
public class ClasseBonusNivel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;
    
    @Column(name = "nivel", nullable = false)
    @Min(1) @Max(36)
    private Integer nivel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_id", nullable = false)
    private ConfigAtributo atributo;
    
    @Column(name = "bonus_valor", nullable = false)
    private Integer bonusValor;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Getters, Setters, equals, hashCode
}
```

#### Repository
- [ ] Criar `ClasseBonusNivelRepository.java`
```java
public interface ClasseBonusNivelRepository extends JpaRepository<ClasseBonusNivel, Long> {
    
    List<ClasseBonusNivel> findByClasseId(Long classeId);
    
    List<ClasseBonusNivel> findByClasseIdAndNivel(Long classeId, Integer nivel);
    
    List<ClasseBonusNivel> findByClasseIdAndNivelLessThanEqual(Long classeId, Integer nivel);
}
```

### 2. Atualizar ClassePersonagem (15 min)

- [ ] Remover campo `bonusPorNivel`
- [ ] Adicionar relacionamento OneToMany
```java
@Entity
@Table(name = "classes_personagem")
public class ClassePersonagem {
    
    // ...existing code...
    
    // ❌ REMOVER
    // private Map<Integer, Map<String, Integer>> bonusPorNivel;
    
    // ✅ ADICIONAR
    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClasseBonusNivel> bonusPorNivel = new ArrayList<>();
    
    // Helper methods
    public void adicionarBonus(Integer nivel, ConfigAtributo atributo, Integer valor) {
        ClasseBonusNivel bonus = new ClasseBonusNivel();
        bonus.setClasse(this);
        bonus.setNivel(nivel);
        bonus.setAtributo(atributo);
        bonus.setBonusValor(valor);
        bonus.setCreatedAt(LocalDateTime.now());
        this.bonusPorNivel.add(bonus);
    }
    
    public Map<String, Integer> getBonusPorNivel(Integer nivel) {
        return bonusPorNivel.stream()
            .filter(b -> b.getNivel().equals(nivel))
            .collect(Collectors.toMap(
                b -> b.getAtributo().getCodigo(),
                ClasseBonusNivel::getBonusValor
            ));
    }
}
```

### 3. Criar Migration (20 min)

- [ ] Criar `20260201122100__create_table_classe_bonus_nivel.sql`
```sql
CREATE TABLE classe_bonus_nivel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    classe_id BIGINT NOT NULL,
    nivel INT NOT NULL,
    atributo_id BIGINT NOT NULL,
    bonus_valor INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_classe_bonus_classe 
        FOREIGN KEY (classe_id) REFERENCES classes_personagem(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_classe_bonus_atributo 
        FOREIGN KEY (atributo_id) REFERENCES config_atributos(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT chk_nivel_valido 
        CHECK (nivel BETWEEN 1 AND 36),
    
    CONSTRAINT uk_classe_nivel_atributo 
        UNIQUE (classe_id, nivel, atributo_id)
);

CREATE INDEX idx_classe_bonus_classe ON classe_bonus_nivel(classe_id);
CREATE INDEX idx_classe_bonus_nivel ON classe_bonus_nivel(nivel);
CREATE INDEX idx_classe_bonus_atributo ON classe_bonus_nivel(atributo_id);
```

- [ ] Remover coluna `bonus_por_nivel` da migration de `classes_personagem`

### 4. Atualizar Mapper (20 min)

- [ ] Atualizar `ClassePersonagemMapper.java`
```java
@Mapper(componentModel = "spring")
public interface ClassePersonagemMapper {
    
    ClassePersonagemDTO toDTO(ClassePersonagem entity);
    
    @Mapping(target = "bonusPorNivel", ignore = true)  // Será mapeado manualmente
    ClassePersonagem toEntity(ClassePersonagemDTO dto);
    
    // Método auxiliar para mapear bônus
    default Map<Integer, Map<String, Integer>> mapBonusPorNivel(List<ClasseBonusNivel> bonus) {
        return bonus.stream()
            .collect(Collectors.groupingBy(
                ClasseBonusNivel::getNivel,
                Collectors.toMap(
                    b -> b.getAtributo().getCodigo(),
                    ClasseBonusNivel::getBonusValor
                )
            ));
    }
}
```

### 5. Atualizar DTO (15 min)

- [ ] Manter DTO com estrutura Map (para compatibilidade API)
```java
public class ClassePersonagemDTO {
    private Long id;
    private String nome;
    private String descricao;
    
    // Mantém estrutura Map para API
    private Map<Integer, Map<String, Integer>> bonusPorNivel;
    
    // Getters, Setters
}
```

### 6. Atualizar Service (20 min)

- [ ] Atualizar `ClassePersonagemService.java`
```java
@Service
public class ClassePersonagemService {
    
    private final ClassePersonagemRepository repository;
    private final ClasseBonusNivelRepository bonusRepository;
    private final ConfigAtributoRepository atributoRepository;
    
    public ClassePersonagemDTO salvar(ClassePersonagemDTO dto) {
        ClassePersonagem entity = mapper.toEntity(dto);
        
        // Mapear bônus do Map para entidades
        if (dto.getBonusPorNivel() != null) {
            dto.getBonusPorNivel().forEach((nivel, atributos) -> {
                atributos.forEach((codigoAtributo, valorBonus) -> {
                    ConfigAtributo atributo = atributoRepository
                        .findByCodigo(codigoAtributo)
                        .orElseThrow();
                    entity.adicionarBonus(nivel, atributo, valorBonus);
                });
            });
        }
        
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }
    
    public Map<String, Integer> calcularBonusTotaisAteNivel(Long classeId, Integer nivel) {
        List<ClasseBonusNivel> bonus = bonusRepository
            .findByClasseIdAndNivelLessThanEqual(classeId, nivel);
        
        return bonus.stream()
            .collect(Collectors.groupingBy(
                b -> b.getAtributo().getCodigo(),
                Collectors.summingInt(ClasseBonusNivel::getBonusValor)
            ));
    }
}
```

### 7. Migrar Dados Existentes (30 min)

- [ ] Criar script de migração de dados (se houver)
```sql
-- 20260201122110__migrate_classe_bonus_data.sql

-- Exemplo: se já houver dados em JSON, converter para tabela
-- (Provavelmente não há dados ainda, mas deixar preparado)

-- Script manual para popular:
-- Será feito na TASK-011 (Seeds)
```

### 8. Criar Testes (45 min)

#### ClasseBonusNivelRepositoryTest.java
- [ ] Criar testes de repository
```java
@SpringBootTest
@ActiveProfiles("test")
class ClasseBonusNivelRepositoryTest {
    
    @Test
    void deveSalvarBonusNivel() {
        // Given
        ClassePersonagem classe = criarClasse("Guerreiro");
        ConfigAtributo forca = criarAtributo("FOR");
        ClasseBonusNivel bonus = new ClasseBonusNivel();
        bonus.setClasse(classe);
        bonus.setNivel(1);
        bonus.setAtributo(forca);
        bonus.setBonusValor(1);
        
        // When
        ClasseBonusNivel salvo = repository.save(bonus);
        
        // Then
        assertNotNull(salvo.getId());
        assertEquals(1, salvo.getNivel());
        assertEquals(1, salvo.getBonusValor());
    }
    
    @Test
    void deveBuscarBonusPorClasseENivel() {
        // Arrange, Act, Assert
    }
    
    @Test
    void deveSomarBonusTotaisAteNivel() {
        // Arrange, Act, Assert
    }
}
```

#### ClassePersonagemServiceTest.java
- [ ] Atualizar testes existentes
- [ ] Adicionar testes para novos métodos de bônus

### 9. Validar (10 min)

- [ ] Rodar `./mvnw clean compile`
- [ ] Rodar `./mvnw test`
- [ ] Verificar warnings de JSON removidos
- [ ] Confirmar build SUCCESS

---

## ✅ DEFINITION OF DONE

- [ ] Entidade `ClasseBonusNivel` criada
- [ ] Repository criado
- [ ] Migration criada e testada
- [ ] `ClassePersonagem` refatorada (sem JSON)
- [ ] Mapper atualizado
- [ ] Service atualizado
- [ ] DTOs compatíveis com API
- [ ] Testes criados e passando
- [ ] Build completo SUCCESS
- [ ] Zero warnings de JSON

---

## 📊 PROGRESSO

```
Entidade:               ⬜ 0/1
Repository:             ⬜ 0/1
Migration:              ⬜ 0/1
Refatorar Classe:       ⬜ 0/1
Mapper:                 ⬜ 0/1
Service:                ⬜ 0/1
Testes:                 ⬜ 0/2
Validar:                ⬜ 0/1
───────────────────────────────
TOTAL:                  0/9 (0%)
```

---

**Criado**: 2026-02-01  
**Atualizado**: 2026-02-01  
**Responsável**: Dev Team
