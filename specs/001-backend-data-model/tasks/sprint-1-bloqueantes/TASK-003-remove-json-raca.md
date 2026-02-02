# TASK-003: Remover JSON de Raca

**Sprint**: 1 - Bloqueantes  
**Prioridade**: P0 - CRÍTICO  
**Estimativa**: 2 horas  
**Status**: ❌ Não Iniciado

---

## 🎯 OBJETIVO

Refatorar `Raca` para remover campo JSON `bonusAtributos` e substituir por tabela relacional `raca_bonus_atributo`.

---

## 🔴 PROBLEMA ATUAL

```java
@Entity
@Table(name = "racas")
public class Raca {
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "bonus_atributos", columnDefinition = "JSON")
    private Map<String, Integer> bonusAtributos;  // ❌ JSON!
}
```

**Exemplo**: `{"FOR": 2, "CON": 1, "CAR": -1}`

---

## ✅ SOLUÇÃO

### Nova Estrutura
```
racas
    ├── id, jogo_id, nome, descricao, ativa

raca_bonus_atributo (NOVA)
    ├── id
    ├── raca_id          FK → racas
    ├── atributo_id      FK → config_atributos
    ├── bonus_valor      INT (pode ser negativo)
    └── created_at
```

---

## 📋 CHECKLIST

### 1. Criar Entidade (20 min)
- [ ] `RacaBonusAtributo.java`
- [ ] `RacaBonusAtributoRepository.java`

### 2. Atualizar Raca (10 min)
- [ ] Remover campo JSON
- [ ] Adicionar `@OneToMany bonusAtributos`
- [ ] Helper methods

### 3. Migration (15 min)
- [ ] `20260201122300__create_table_raca_bonus_atributo.sql`
- [ ] Constraints e índices

### 4. Mapper/Service (20 min)
- [ ] Atualizar `RacaMapper`
- [ ] Atualizar `RacaService`

### 5. Testes (30 min)
- [ ] `RacaBonusAtributoRepositoryTest`
- [ ] Atualizar testes de `RacaService`

### 6. Validar (10 min)
- [ ] Build SUCCESS
- [ ] Testes passando

---

## ✅ DEFINITION OF DONE
- [ ] JSON removido
- [ ] Tabela relacional criada
- [ ] Testes passando
- [ ] Zero warnings

---

**Estimativa**: 2h  
**Status**: ❌
