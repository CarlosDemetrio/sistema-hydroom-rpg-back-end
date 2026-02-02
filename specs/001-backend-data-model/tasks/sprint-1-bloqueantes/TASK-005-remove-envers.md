# TASK-005: Remover Hibernate Envers

**Sprint**: 1 - Bloqueantes  
**Prioridade**: P0 - CRÍTICO  
**Estimativa**: 1 hora  
**Status**: ❌ Não Iniciado

---

## 🎯 OBJETIVO

Remover Hibernate Envers do projeto para simplificar arquitetura, mantendo apenas timestamps de auditoria básica (createdAt, updatedAt).

---

## 🔴 PROBLEMA ATUAL

### Complexidade Desnecessária
```java
@Audited  // ❌ Em TODAS as entidades
@Entity
public class Usuario extends AuditableEntity { ... }

@Entity
@RevisionEntity(CustomRevisionListener.class)  // ❌ 
public class CustomRevisionEntity { ... }

CustomRevisionListener.java  // ❌
```

**Problemas**:
- Tabelas `*_AUD` para cada entidade (30+ tabelas extras)
- Tabela `revinfo` com metadados
- Complexidade no código
- Overhead de performance
- Não é necessário no momento

---

## ✅ SOLUÇÃO SIMPLES

### Manter Apenas Timestamps
```java
@MappedSuperclass
public abstract class AuditableEntity {
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters/Setters
}
```

---

## 📋 CHECKLIST

### 1. Remover Dependência (2 min)
- [ ] Editar `pom.xml`
```xml
<!-- ❌ REMOVER -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-envers</artifactId>
</dependency>
```

### 2. Deletar Classes (5 min)
- [ ] Deletar `CustomRevisionEntity.java`
- [ ] Deletar `CustomRevisionListener.java`

### 3. Simplificar AuditableEntity (10 min)
- [ ] Editar `AuditableEntity.java`
- [ ] Remover imports de Envers
- [ ] Manter apenas timestamps
- [ ] Adicionar `@PrePersist` e `@PreUpdate`

### 4. Remover @Audited (15 min)
- [ ] Buscar por `@Audited` no projeto
- [ ] Remover de TODAS entidades:
  - `Usuario.java`
  - `Jogo.java`
  - `JogoParticipante.java`
  - `ClassePersonagem.java`
  - `Raca.java`
  - `VantagemConfig.java`
  - `Ficha.java`
  - `FichaAtributo.java`
  - `FichaAptidao.java`
  - ... (todas)

### 5. Remover Imports (5 min)
- [ ] Remover `import org.hibernate.envers.*`
- [ ] IDE: Optimize Imports

### 6. Atualizar Migrations (10 min)
- [ ] Verificar se há migrations criando tabelas `*_AUD`
- [ ] Deletar migrations de auditoria
- [ ] Manter apenas `created_at` e `updated_at` nas tabelas

### 7. Limpar Configuração (5 min)
- [ ] Verificar `application.properties`
- [ ] Remover configs de Envers (se houver)

### 8. Validar (10 min)
- [ ] Rodar `./mvnw clean compile`
- [ ] Rodar `./mvnw test`
- [ ] Confirmar SEM erros de Envers
- [ ] Confirmar timestamps funcionando

---

## 📁 ARQUIVOS A MODIFICAR

### Deletar
```
src/main/java/.../model/audit/
├── CustomRevisionEntity.java         ❌ DELETAR
└── CustomRevisionListener.java       ❌ DELETAR
```

### Editar
```
pom.xml                                ✏️ Remover dependência
AuditableEntity.java                   ✏️ Simplificar
Usuario.java                           ✏️ Remover @Audited
Jogo.java                              ✏️ Remover @Audited
... (todas entidades)                  ✏️ Remover @Audited
```

---

## ✅ DEFINITION OF DONE

- [ ] Dependência Envers removida do `pom.xml`
- [ ] Classes de auditoria deletadas
- [ ] `AuditableEntity` simplificado (só timestamps)
- [ ] `@Audited` removido de todas entidades
- [ ] Imports limpos
- [ ] Migrations de audit deletadas
- [ ] Build completo SUCCESS
- [ ] Testes passando
- [ ] Timestamps funcionando

---

## 📊 PROGRESSO

```
Remover Dependência:    ⬜ 0/1
Deletar Classes:        ⬜ 0/2
Simplificar Auditable:  ⬜ 0/1
Remover @Audited:       ⬜ 0/15
Remover Imports:        ⬜ 0/1
Limpar Migrations:      ⬜ 0/1
Limpar Config:          ⬜ 0/1
Validar:                ⬜ 0/1
───────────────────────────────
TOTAL:                  0/23 (0%)
```

---

## 🎁 BENEFÍCIOS

### Antes (com Envers)
- 30+ entidades
- 30+ tabelas `*_AUD`
- 1 tabela `revinfo`
- Complexidade alta
- Performance overhead

### Depois (sem Envers)
- 30+ entidades
- 2 campos simples (createdAt, updatedAt)
- Zero overhead
- Código limpo

---

**Criado**: 2026-02-01  
**Atualizado**: 2026-02-01  
**Responsável**: Dev Team
