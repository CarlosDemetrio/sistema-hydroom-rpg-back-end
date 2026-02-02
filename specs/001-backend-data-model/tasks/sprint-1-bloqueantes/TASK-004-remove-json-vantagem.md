# TASK-004: Remover JSON de VantagemConfig

**Sprint**: 1 - Bloqueantes  
**Prioridade**: P0 - CRÍTICO  
**Estimativa**: 3 horas  
**Status**: ❌ Não Iniciado

---

## 🎯 OBJETIVO

Refatorar `VantagemConfig` para remover campo JSON `efeitos` e substituir por tabela relacional `vantagem_efeito`.

---

## 🔴 PROBLEMA ATUAL

```java
@Entity
@Table(name = "vantagens")
public class VantagemConfig {
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "efeitos", columnDefinition = "JSON")
    private Map<String, Object> efeitos;  // ❌ JSON!
}
```

**Exemplo**:
```json
{
  "bonusAtributo": {"FOR": 1, "DES": 1},
  "bonusBBA": 1,
  "habilidadeEspecial": "Ataque Furtivo +1d6"
}
```

---

## ✅ SOLUÇÃO

### Nova Estrutura
```
vantagens
    ├── id, jogo_id, nome, descricao, custoBase, categoria_id

vantagem_efeito (NOVA)
    ├── id
    ├── vantagem_id        FK → vantagens
    ├── tipo_efeito        ENUM (bonus_atributo, bonus_bba, habilidade, etc)
    ├── alvo               VARCHAR (código do atributo, bônus, etc)
    ├── valor_numerico     INT (para bônus numéricos)
    ├── valor_texto        TEXT (para descrições)
    ├── formula            VARCHAR (para cálculos: "+1/PN", etc)
    └── created_at
```

---

## 📋 CHECKLIST

### 1. Criar Enum (10 min)
- [ ] `TipoEfeitoVantagem.java`
```java
public enum TipoEfeitoVantagem {
    BONUS_ATRIBUTO,
    BONUS_BBA,
    BONUS_INICIATIVA,
    BONUS_DEFESA,
    BONUS_PV,
    HABILIDADE_ESPECIAL,
    MODIFICADOR_DANO,
    ACAO_ADICIONAL
}
```

### 2. Criar Entidade (25 min)
- [ ] `VantagemEfeito.java`
- [ ] `VantagemEfeitoRepository.java`

### 3. Atualizar VantagemConfig (15 min)
- [ ] Remover campo JSON
- [ ] Adicionar `@OneToMany efeitos`
- [ ] Helper methods

### 4. Migration (20 min)
- [ ] `20260201122600__create_table_vantagem_efeitos.sql`

### 5. Mapper/Service (30 min)
- [ ] Atualizar `VantagemMapper`
- [ ] Atualizar `VantagemService`
- [ ] Lógica de aplicação de efeitos

### 6. Testes (40 min)
- [ ] `VantagemEfeitoRepositoryTest`
- [ ] Testes de aplicação de efeitos

### 7. Validar (10 min)
- [ ] Build e testes

---

## ✅ DEFINITION OF DONE
- [ ] JSON removido
- [ ] Tabela de efeitos criada
- [ ] Enum de tipos criado
- [ ] Testes passando
- [ ] Lógica de aplicação funcional

---

**Estimativa**: 3h  
**Status**: ❌
