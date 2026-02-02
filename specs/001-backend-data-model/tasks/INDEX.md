# 📋 INDEX DE TASKS - Backend Data Model

**Feature**: 001-backend-data-model  
**Data Criação**: 2026-02-01  
**Última Atualização**: 2026-02-01 14:45  
**Status**: 📝 Planejamento Completo com TODAS as Regras de Negócio

---

## 📊 RESUMO EXECUTIVO

### Status Atual
```
✅ Implementado Correto:   60%  (Models base, Repositories, Auth)
⚠️  Implementado Parcial:   25%  (Falta seeds, controllers, migrations)
❌ Não Implementado:        15%  (Tabelas relacionamento, validações)
```

### ✅ BOM: Sem JSON!
Após análise completa, **NÃO HÁ campos JSON** implementados:
- ✅ `ClassePersonagem` - sem JSON (bônus será em tabela separada)
- ✅ `Raca` - sem JSON (bônus será em tabela separada)  
- ✅ `VantagemConfig` - sem JSON (efeitos será em tabela separada)

### Estimativa Total
- **P0 (Bloqueantes)**: 14h → **2 dias**
- **P1 (Importantes)**: 31h → **5 dias**
- **P2 (Desejáveis)**: 56h → **8 dias**
- **TOTAL GERAL**: **101h → ~15 dias úteis**

---

## 🎯 SPRINTS

### Sprint 1 - Bloqueantes (P0) - 2 dias
```
Dia 1 (7h):
- TASK-001: Criar 34 Migrations (4h)
- TASK-002: ClasseBonusNivel (3h)

Dia 2 (7h):
- TASK-003: RacaBonusAtributo (2h)
- TASK-004: VantagemEfeito (4h)
- TASK-005: Remover Envers (1h)
```

### Sprint 2 - Entidades e Controllers (P1) - 5 dias
```
Dia 3 (7h):
- TASK-006: FichaPersonalidade (3h)
- TASK-007: JogoController - Parte 1 (4h)

Dia 4 (7h):
- TASK-007: JogoController - Parte 2 (2h)
- TASK-008: FichaController - Parte 1 (5h)

Dia 5 (7h):
- TASK-008: FichaController - Parte 2 (3h)
- TASK-009: ConfigController - Parte 1 (4h)

Dia 6-7 (14h):
- TASK-009: ConfigController - Parte 2 (4h)
- TASK-010: Seeds Klayrah (6h)
- Testes e ajustes (4h)
```

### Sprint 3 - Melhorias (P2) - 8 dias
```
Dia 8-9 (14h):
- TASK-011: Testes (12h)
- TASK-012: Validações - Parte 1 (2h)

Dia 10 (7h):
- TASK-012: Validações - Parte 2 (2h)
- TASK-013+: Features Futuras - conforme prioridade (5h)

Dia 11-15:
- Features Futuras restantes
```

---

## 📋 TASKS DETALHADAS

Clique nos links para ver os detalhes completos de cada task com todas as regras de negócio.

### 🔴 P0 - Bloqueantes (CRÍTICO)

| # | Task | Estimativa | Dependências | Status |
|---|------|------------|--------------|--------|
| [001](#task-001) | Criar 34 Migrations | 4h | - | ⏳ Pendente |
| [002](#task-002) | ClasseBonusNivel | 3h | 001 | ⏳ Pendente |
| [003](#task-003) | RacaBonusAtributo | 2h | 001 | ⏳ Pendente |
| [004](#task-004) | VantagemEfeito | 4h | 001 | ⏳ Pendente |
| [005](#task-005) | Remover Envers | 1h | - | ⏳ Pendente |

### 🟡 P1 - Importantes (Alta Prioridade)

| # | Task | Estimativa | Dependências | Status |
|---|------|------------|--------------|--------|
| [006](#task-006) | FichaPersonalidade | 3h | 001 | ⏳ Pendente |
| [007](#task-007) | JogoController | 6h | 001, 005 | ⏳ Pendente |
| [008](#task-008) | FichaController | 8h | 001-006 | ⏳ Pendente |
| [009](#task-009) | ConfigController | 8h | 001-004 | ⏳ Pendente |
| [010](#task-010) | Seeds Klayrah | 6h | 001-004 | ⏳ Pendente |

### 🟢 P2 - Desejáveis (Futuro)

| # | Task | Estimativa | Status |
|---|------|------------|--------|
| [011](#task-011) | Testes (80%+) | 12h | ⏳ Pendente |
| [012](#task-012) | Validações Completas | 4h | ⏳ Pendente |
| [013+](#task-013) | Features Futuras | 40h | ⏳ Pendente |

---

## 🔴 TASK-001: Criar 34 Migrations

**Prioridade**: P0 - Bloqueante  
**Estimativa**: 4h  
**Dependências**: Nenhuma  
**Issue**: #001

### Descrição
Criar TODAS as migrations do zero com padrão timestamp `YYYYMMDDHHMMSS__descricao.sql`.

### Por que fazer AGORA?
- Pasta `db/migration` está vazia
- Flyway não cria banco automaticamente
- Bloqueador para todos os outros tasks

### Regras de Negócio
- **Padrão**: `YYYYMMDDHHMMSS__descricao.sql`
- **Ordem**: core → configs → fichas → relacionamentos → seeds
- **Compatível**: MySQL/MariaDB (prod) e H2 (testes)
- **Soft Delete**: `ativo TINYINT DEFAULT 1` em todas tabelas
- **Timestamps**: `created_at`, `updated_at` em todas tabelas

### Migrations Necessárias (34)

Ver detalhes completos no arquivo [TASK-001-MIGRATIONS.md](./sprint-1-bloqueantes/TASK-001-migrations.md)

**Resumo**:
1. Core (5): usuarios, jogos, jogo_participantes
2. Configs Base (12): atributos, aptidões, bônus, membros, níveis, etc
3. Configs Complexas (3): classes, raças, vantagens
4. Fichas Base (2): fichas, ficha_atributos
5. Fichas Subsistemas (8): aptidões, bônus, vida, essência, etc
6. **Relacionamentos (4)**: classe_bonus, raca_bonus, vantagem_efeito ⚠️ NOVO
7. Índices Finais (1)

### Exemplo: usuarios.sql
```sql
-- 20260201000001__criar_tabela_usuarios.sql
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    imagem_url VARCHAR(500),
    ativo TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_provider_id (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Checklist
- [ ] Deletar migrations atuais (se existirem)
- [ ] Criar 5 migrations core
- [ ] Criar 12 migrations configs base
- [ ] Criar 3 migrations configs complexas
- [ ] Criar 10 migrations fichas
- [ ] Criar 4 migrations relacionamentos
- [ ] Criar 1 migration índices finais
- [ ] Testar `flyway clean` + `flyway migrate` no H2
- [ ] Validar todas tabelas criadas
- [ ] Validar constraints e FKs

---

## 🔴 TASK-002: ClasseBonusNivel

**Prioridade**: P0 - Bloqueante  
**Estimativa**: 3h  
**Dependências**: TASK-001  
**Issue**: #002

### Descrição
Criar tabela e entidade JPA para armazenar os bônus que cada classe ganha por nível.

### Por que fazer AGORA?
- **Sem essa tabela, não há como configurar classes** corretamente
- Bloqueador para seeds de classes
- Bloqueador para cálculo de bônus de ficha

### Regras de Negócio (FR-035)
- Cada classe pode ter **N bônus por nível** (1-35)
- Exemplo Bárbaro:
  - Nível 1: +1 BBA, +1 Vigor
  - Nível 2: +1 BBA
  - Nível 3: +1 BBA, +1 Vigor
  - ... até nível 35
- Valores podem ser positivos ou negativos
- Único por `(classe_id, nivel, bonus_config_id)`
- Usado em cálculo de ficha: somar bônus até nível atual

### Estrutura SQL
```sql
-- 20260201000050__criar_tabela_classe_bonus_nivel.sql
CREATE TABLE classe_bonus_nivel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    classe_id BIGINT NOT NULL,
    nivel INT NOT NULL,
    bonus_config_id BIGINT NOT NULL,
    valor INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (classe_id) REFERENCES classes_personagem(id) ON DELETE CASCADE,
    FOREIGN KEY (bonus_config_id) REFERENCES bonus_config(id),
    UNIQUE KEY uk_classe_nivel_bonus (classe_id, nivel, bonus_config_id),
    INDEX idx_classe_bonus_nivel (classe_id, nivel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Entidade JPA
```java
package br.com.hydroom.rpg.fichacontrolador.model;

@Entity
@Table(name = "classe_bonus_nivel",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_classe_nivel_bonus",
        columnNames = {"classe_id", "nivel", "bonus_config_id"}
    ),
    indexes = @Index(
        name = "idx_classe_bonus_nivel",
        columnList = "classe_id, nivel"
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseBonusNivel extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Classe é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassePersonagem classe;
    
    @NotNull(message = "Nível é obrigatório")
    @Min(value = 1, message = "Nível mínimo é 1")
    @Max(value = 35, message = "Nível máximo é 35")
    @Column(nullable = false)
    private Integer nivel;
    
    @NotNull(message = "Bônus é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_config_id", nullable = false)
    private BonusConfig bonus;
    
    @NotNull(message = "Valor é obrigatório")
    @Column(nullable = false)
    private Integer valor; // Pode ser negativo
}
```

### Repository
```java
package br.com.hydroom.rpg.fichacontrolador.repository;

public interface ClasseBonusNivelRepository extends JpaRepository<ClasseBonusNivel, Long> {
    
    /**
     * Busca todos os bônus de uma classe até determinado nível.
     * Usado para calcular bônus total da ficha.
     */
    List<ClasseBonusNivel> findByClasseAndNivelLessThanEqual(
        ClassePersonagem classe, 
        Integer nivel
    );
    
    /**
     * Busca bônus de um nível específico.
     */
    List<ClasseBonusNivel> findByClasseAndNivel(
        ClassePersonagem classe, 
        Integer nivel
    );
    
    /**
     * Verifica se há bônus cadastrados para uma classe.
     * Usado antes de deletar classe.
     */
    boolean existsByClasse(ClassePersonagem classe);
}
```

### Exemplo de Uso - Service
```java
@Service
public class FichaService {
    
    /**
     * Calcula bônus total de classe para a ficha.
     * Soma TODOS os bônus da classe até o nível atual.
     */
    public Map<BonusConfig, Integer> calcularBonusClasse(Ficha ficha) {
        return classeBonusNivelRepository
            .findByClasseAndNivelLessThanEqual(
                ficha.getClasse(), 
                ficha.getNivel()
            )
            .stream()
            .collect(Collectors.groupingBy(
                ClasseBonusNivel::getBonus,
                Collectors.summingInt(ClasseBonusNivel::getValor)
            ));
    }
}
```

### Checklist
- [ ] Criar migration `20260201000050__criar_tabela_classe_bonus_nivel.sql`
- [ ] Criar entidade `ClasseBonusNivel.java`
- [ ] Criar repository `ClasseBonusNivelRepository.java`
- [ ] Atualizar `ClassePersonagem.java`:
  ```java
  @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClasseBonusNivel> bonusPorNivel = new ArrayList<>();
  ```
- [ ] Criar teste de repository:
  - Teste: salvar e buscar bônus
  - Teste: buscar bônus até nível X
  - Teste: unique constraint
- [ ] Rodar `mvn clean compile` e validar
- [ ] Rodar testes

---

## 🔴 TASK-003: RacaBonusAtributo

**Prioridade**: P0 - Bloqueante  
**Estimativa**: 2h  
**Dependências**: TASK-001  
**Issue**: #003

### Descrição
Criar tabela e entidade JPA para armazenar os bônus/penalidades de atributo por raça.

### Por que fazer AGORA?
- **Sem essa tabela, não há como configurar raças** corretamente
- Bloqueador para seeds de raças
- Bloqueador para cálculo de atributos de ficha

### Regras de Negócio
- Cada raça pode ter bônus em **N atributos**
- Exemplo Elfo: +2 AGI, +1 INT, -1 VIG
- Valores podem ser **positivos ou negativos**
- Único por `(raca_id, atributo_config_id)`
- Aplicado automaticamente ao criar ficha

### Estrutura SQL
```sql
-- 20260201000051__criar_tabela_raca_bonus_atributo.sql
CREATE TABLE raca_bonus_atributo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    raca_id BIGINT NOT NULL,
    atributo_config_id BIGINT NOT NULL,
    valor INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (raca_id) REFERENCES racas(id) ON DELETE CASCADE,
    FOREIGN KEY (atributo_config_id) REFERENCES atributo_config(id),
    UNIQUE KEY uk_raca_atributo (raca_id, atributo_config_id),
    INDEX idx_raca_bonus (raca_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Entidade JPA
```java
package br.com.hydroom.rpg.fichacontrolador.model;

@Entity
@Table(name = "raca_bonus_atributo",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_raca_atributo",
        columnNames = {"raca_id", "atributo_config_id"}
    ),
    indexes = @Index(
        name = "idx_raca_bonus",
        columnList = "raca_id"
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacaBonusAtributo extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Raça é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raca_id", nullable = false)
    private Raca raca;
    
    @NotNull(message = "Atributo é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atributo_config_id", nullable = false)
    private AtributoConfig atributo;
    
    @NotNull(message = "Valor é obrigatório")
    @Column(nullable = false)
    private Integer valor; // Pode ser negativo (ex: -1 VIG para Elfo)
}
```

### Repository
```java
package br.com.hydroom.rpg.fichacontrolador.repository;

public interface RacaBonusAtributoRepository extends JpaRepository<RacaBonusAtributo, Long> {
    
    /**
     * Busca todos os bônus de uma raça.
     */
    List<RacaBonusAtributo> findByRaca(Raca raca);
    
    /**
     * Busca bônus de um atributo específico de uma raça.
     */
    Optional<RacaBonusAtributo> findByRacaAndAtributo(
        Raca raca, 
        AtributoConfig atributo
    );
    
    /**
     * Verifica se há bônus cadastrados para uma raça.
     */
    boolean existsByRaca(Raca raca);
}
```

### Exemplo de Uso - Service
```java
@Service
public class FichaService {
    
    /**
     * Aplica bônus racial nos atributos da ficha.
     * Chamado ao criar ficha ou alterar raça.
     */
    public void aplicarBonusRacial(Ficha ficha) {
        // Buscar bônus raciais
        Map<AtributoConfig, Integer> bonusRacial = racaBonusAtributoRepository
            .findByRaca(ficha.getRaca())
            .stream()
            .collect(Collectors.toMap(
                RacaBonusAtributo::getAtributo,
                RacaBonusAtributo::getValor
            ));
        
        // Aplicar nos atributos da ficha
        ficha.getAtributos().forEach(fichaAtributo -> {
            Integer bonus = bonusRacial.getOrDefault(
                fichaAtributo.getAtributoConfig(), 
                0
            );
            fichaAtributo.setBonusRaca(bonus);
        });
    }
}
```

### Exemplo de Dados (Seeds)
```sql
-- Elfo: +2 AGI, +1 INT, -1 VIG
INSERT INTO raca_bonus_atributo (raca_id, atributo_config_id, valor) VALUES
(2, 2, 2),   -- +2 AGI
(2, 6, 1),   -- +1 INT
(2, 4, -1);  -- -1 VIG

-- Anão: +2 CON, +1 FOR, -1 AGI
INSERT INTO raca_bonus_atributo (raca_id, atributo_config_id, valor) VALUES
(3, 3, 2),   -- +2 CON
(3, 1, 1),   -- +1 FOR
(3, 2, -1);  -- -1 AGI
```

### Checklist
- [ ] Criar migration `20260201000051__criar_tabela_raca_bonus_atributo.sql`
- [ ] Criar entidade `RacaBonusAtributo.java`
- [ ] Criar repository `RacaBonusAtributoRepository.java`
- [ ] Atualizar `Raca.java`:
  ```java
  @OneToMany(mappedBy = "raca", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RacaBonusAtributo> bonusAtributos = new ArrayList<>();
  ```
- [ ] Criar teste de repository
- [ ] Rodar `mvn clean compile` e validar
- [ ] Rodar testes

---

## 🔴 TASK-004: VantagemEfeito

**Prioridade**: P0 - Bloqueante  
**Estimativa**: 4h  
**Dependências**: TASK-001  
**Issue**: #004

### Descrição
Criar tabela e entidade JPA para armazenar os efeitos que cada nível de vantagem concede. Inclui criação de service para avaliar fórmulas.

### Por que fazer AGORA?
- **Sem essa tabela, vantagens não funcionam**
- Bloqueador para seeds de vantagens
- Bloqueador para compra de vantagens
- **COMPLEXIDADE**: Requer parser de fórmulas

### Regras de Negócio (FR-056, FR-057, FR-059)
- Vantagens têm **níveis** (1 a N configurável, ex: 1-10)
- Cada nível pode ter **múltiplos efeitos**
- Efeitos podem ser: BONUS, ATRIBUTO, APTIDAO
- **PN = Por Nível da Vantagem** (NÃO do personagem!)
- Fórmulas suportadas: "1" (fixo), "PN", "PN*2", "PN+5"
- Exemplo "Treinamento Físico":
  - Nível 1: +1 BBA/PN (PN=1 → +1), +1 Vigor
  - Nível 2: +2 BBA/PN (PN=2 → +2), +1 Vigor
  - Nível 10: +10 BBA/PN (PN=10 → +10), +1 Vigor

### Enum TipoEfeito
```java
package br.com.hydroom.rpg.fichacontrolador.model.enums;

public enum TipoEfeito {
    /**
     * Aplica efeito em BonusConfig (BBA, Esquiva, Bloqueio, etc)
     */
    BONUS,
    
    /**
     * Aplica efeito em AtributoConfig (FOR, AGI, VIG, etc)
     */
    ATRIBUTO,
    
    /**
     * Aplica efeito em AptidaoConfig (Acrobacia, Luta, etc)
     */
    APTIDAO
}
```

### Estrutura SQL
```sql
-- 20260201000052__criar_tabela_vantagem_efeito.sql
CREATE TABLE vantagem_efeito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vantagem_config_id BIGINT NOT NULL,
    nivel_vantagem INT NOT NULL,
    tipo_efeito VARCHAR(50) NOT NULL,
    alvo_id BIGINT NOT NULL,
    formula VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vantagem_config_id) REFERENCES vantagem_config(id) ON DELETE CASCADE,
    INDEX idx_vantagem_efeito (vantagem_config_id, nivel_vantagem),
    CHECK (tipo_efeito IN ('BONUS', 'ATRIBUTO', 'APTIDAO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Entidade JPA
```java
package br.com.hydroom.rpg.fichacontrolador.model;

@Entity
@Table(name = "vantagem_efeito",
    indexes = @Index(
        name = "idx_vantagem_efeito",
        columnList = "vantagem_config_id, nivel_vantagem"
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemEfeito extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Vantagem é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_config_id", nullable = false)
    private VantagemConfig vantagem;
    
    @NotNull(message = "Nível da vantagem é obrigatório")
    @Min(value = 1, message = "Nível mínimo é 1")
    @Column(name = "nivel_vantagem", nullable = false)
    private Integer nivelVantagem;
    
    @NotNull(message = "Tipo de efeito é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_efeito", nullable = false, length = 50)
    private TipoEfeito tipo;
    
    @NotNull(message = "ID do alvo é obrigatório")
    @Column(name = "alvo_id", nullable = false)
    private Long alvoId; // ID do BonusConfig, AtributoConfig ou AptidaoConfig
    
    @NotBlank(message = "Fórmula é obrigatória")
    @Size(max = 100, message = "Fórmula deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String formula; // "1", "PN", "PN*2", "PN+5"
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String descricao;
}
```

### Repository
```java
package br.com.hydroom.rpg.fichacontrolador.repository;

public interface VantagemEfeitoRepository extends JpaRepository<VantagemEfeito, Long> {
    
    /**
     * Busca todos os efeitos de um nível específico de vantagem.
     * Usado ao comprar vantagem.
     */
    List<VantagemEfeito> findByVantagemAndNivelVantagem(
        VantagemConfig vantagem, 
        Integer nivelVantagem
    );
    
    /**
     * Busca todos os efeitos de uma vantagem (todos níveis).
     */
    List<VantagemEfeito> findByVantagem(VantagemConfig vantagem);
}
```

### Service - FormulaEvaluator
```java
package br.com.hydroom.rpg.fichacontrolador.service;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

@Service
public class FormulaEvaluator {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * Avalia uma fórmula com PN (Por Nível da Vantagem).
     * 
     * Exemplos:
     * - "1" → sempre 1
     * - "PN" → nivelVantagem (1, 2, 3...)
     * - "PN*2" → nivelVantagem * 2
     * - "PN+5" → nivelVantagem + 5
     * 
     * @param formula Fórmula a avaliar (ex: "PN*2")
     * @param nivelVantagem Nível atual da vantagem (1-10)
     * @return Valor calculado
     * @throws IllegalArgumentException se fórmula inválida
     */
    public int avaliar(String formula, int nivelVantagem) {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("Fórmula não pode ser vazia");
        }
        
        // Substituir PN pelo valor do nível
        String formulaProcessada = formula.replaceAll("PN", String.valueOf(nivelVantagem));
        
        try {
            Expression exp = parser.parseExpression(formulaProcessada);
            Number result = exp.getValue(Number.class);
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Fórmula inválida: " + formula + " → " + formulaProcessada, e
            );
        }
    }
}
```

### Service - VantagemService
```java
@Service
public class VantagemService {
    
    @Inject
    private VantagemEfeitoRepository vantagemEfeitoRepository;
    
    @Inject
    private FormulaEvaluator formulaEvaluator;
    
    /**
     * Aplica os efeitos de uma vantagem na ficha.
     * 
     * @param ficha Ficha do personagem
     * @param fichaVantagem Vantagem adquirida
     */
    public void aplicarEfeitosVantagem(Ficha ficha, FichaVantagem fichaVantagem) {
        List<VantagemEfeito> efeitos = vantagemEfeitoRepository
            .findByVantagemAndNivelVantagem(
                fichaVantagem.getVantagem(), 
                fichaVantagem.getNivel()
            );
        
        efeitos.forEach(efeito -> {
            int valor = formulaEvaluator.avaliar(
                efeito.getFormula(), 
                fichaVantagem.getNivel()
            );
            
            switch (efeito.getTipo()) {
                case BONUS -> aplicarBonusVantagem(ficha, efeito.getAlvoId(), valor);
                case ATRIBUTO -> aplicarAtributoVantagem(ficha, efeito.getAlvoId(), valor);
                case APTIDAO -> aplicarAptidaoVantagem(ficha, efeito.getAlvoId(), valor);
            }
        });
    }
    
    private void aplicarBonusVantagem(Ficha ficha, Long bonusConfigId, int valor) {
        // Buscar FichaBonus correspondente e adicionar valor
    }
    
    private void aplicarAtributoVantagem(Ficha ficha, Long atributoConfigId, int valor) {
        // Buscar FichaAtributo correspondente e adicionar valor
    }
    
    private void aplicarAptidaoVantagem(Ficha ficha, Long aptidaoConfigId, int valor) {
        // Buscar FichaAptidao correspondente e adicionar valor
    }
}
```

### Exemplo de Dados (Seeds)
```sql
-- Treinamento Físico (vantagem_config_id=1, max nivel=10)
-- Efeito: +1 BBA/PN (cumulativo), +1 Vigor (fixo)

-- Nível 1
INSERT INTO vantagem_efeito VALUES
(NULL, 1, 1, 'BONUS', 1, 'PN', '+1 BBA/PN'),      -- PN=1 → +1 BBA
(NULL, 1, 1, 'BONUS', 4, '1', '+1 Vigor');        -- +1 Vigor fixo

-- Nível 2
INSERT INTO vantagem_efeito VALUES
(NULL, 1, 2, 'BONUS', 1, 'PN', '+1 BBA/PN'),      -- PN=2 → +2 BBA
(NULL, 1, 2, 'BONUS', 4, '1', '+1 Vigor');        -- +1 Vigor fixo

-- ... repetir até Nível 10
```

### Checklist
- [ ] Criar enum `TipoEfeito.java`
- [ ] Criar migration `20260201000052__criar_tabela_vantagem_efeito.sql`
- [ ] Criar entidade `VantagemEfeito.java`
- [ ] Criar repository `VantagemEfeitoRepository.java`
- [ ] Atualizar `VantagemConfig.java`:
  ```java
  @OneToMany(mappedBy = "vantagem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VantagemEfeito> efeitos = new ArrayList<>();
  ```
- [ ] Criar service `FormulaEvaluator.java`
- [ ] Criar testes de fórmulas:
  - Teste: "1" → 1
  - Teste: "PN" com N=5 → 5
  - Teste: "PN*2" com N=3 → 6
  - Teste: "PN+5" com N=2 → 7
  - Teste: fórmula inválida → exception
- [ ] Criar testes de repository
- [ ] Rodar `mvn clean compile` e validar
- [ ] Rodar testes

---

## 🔴 TASK-005: Remover Envers

**Prioridade**: P1  
**Estimativa**: 1h  
**Dependências**: Nenhuma  
**Issue**: #005

### Descrição
Remover Hibernate Envers (auditoria complexa) - manter apenas soft delete e timestamps.

### Por que fazer AGORA?
- **Complexidade desnecessária** neste momento
- Soft delete + timestamps são suficientes
- Envers cria tabelas `*_AUD` e `revinfo` → overhead
- Pode ser adicionado no futuro se necessário

### O que MANTER ✅
- ✅ Soft delete: campo `ativo` (TINYINT DEFAULT 1)
- ✅ Timestamps: `created_at`, `updated_at`
- ✅ `BaseEntity` com timestamps automáticos

### O que REMOVER ❌
- ❌ `@Audited` em entidades
- ❌ `audit/CustomRevisionEntity.java`
- ❌ `audit/CustomRevisionListener.java`
- ❌ Dependência `hibernate-envers` do `pom.xml`
- ❌ Configurações Envers em `application.properties`

### BaseEntity Simplificado
```java
package br.com.hydroom.rpg.fichacontrolador.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Checklist
- [ ] Remover `@Audited` de todas entidades:
  - Usuario.java
  - Jogo.java
  - JogoParticipante.java
  - Ficha.java (e todos subsistemas)
  - Todas configs
- [ ] Deletar `src/main/java/.../audit/CustomRevisionEntity.java`
- [ ] Deletar `src/main/java/.../audit/CustomRevisionListener.java`
- [ ] Remover do `pom.xml`:
  ```xml
  <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-envers</artifactId>
  </dependency>
  ```
- [ ] Limpar `application.properties`:
  - Remover `spring.jpa.properties.org.hibernate.envers.*`
- [ ] Simplificar `BaseEntity.java` (ver código acima)
- [ ] Rodar `mvn clean compile` para validar
- [ ] Rodar testes
- [ ] Commit: "refactor: remover Hibernate Envers"

---

## 📝 CONTINUAÇÃO: TASK-006 a TASK-012

Para ver detalhes completos das tasks restantes, consulte:

- **TASK-006**: [FichaPersonalidade](./sprint-2-entidades/TASK-006-ficha-personalidade.md)
- **TASK-007**: [JogoController](./sprint-2-controllers/TASK-007-jogo-controller.md)
- **TASK-008**: [FichaController](./sprint-2-controllers/TASK-008-ficha-controller.md)
- **TASK-009**: [ConfigController](./sprint-2-controllers/TASK-009-config-controller.md)
- **TASK-010**: [Seeds Klayrah](./sprint-3-seeds/TASK-010-seeds-klayrah.md)
- **TASK-011+**: [Melhorias Futuras](./sprint-4-melhorias/TASK-011-plus-melhorias.md)

---

## ✅ PROGRESSO GERAL

### Sprint 1 - Bloqueantes (P0)
- [ ] TASK-001: Migrations (4h)
- [ ] TASK-002: ClasseBonusNivel (3h)
- [ ] TASK-003: RacaBonusAtributo (2h)
- [ ] TASK-004: VantagemEfeito (4h)
- [ ] TASK-005: Remover Envers (1h)

### Sprint 2 - Controllers (P1)
- [ ] TASK-006: FichaPersonalidade (3h)
- [ ] TASK-007: JogoController (6h)
- [ ] TASK-008: FichaController (8h)
- [ ] TASK-009: ConfigController (8h)

### Sprint 3 - Seeds (P1)
- [ ] TASK-010: Seeds Klayrah (6h)

### Sprint 4 - Melhorias (P2)
- [ ] TASK-011: Testes (12h)
- [ ] TASK-012: Validações (4h)
- [ ] TASK-013+: Features Futuras (40h)

---

**FIM DO INDEX** ✅

**Última Atualização**: 2026-02-01 14:45  
**Próxima Revisão**: Após completar cada Sprint  
**Status**: 📝 Planejamento Completo com TODAS as Regras de Negócio
