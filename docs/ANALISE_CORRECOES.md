# 🔍 Análise e Correções - Backend Klayrah RPG

**Data**: 2026-02-01  
**Revisão**: Modelagem de Dados vs Implementado

---

## ❌ PROBLEMAS CRÍTICOS ENCONTRADOS

### 1. **VIOLAÇÃO DO REQUISITO: JSON não deve ser usado**

**Problema**: A entidade `Ficha.java` está planejada com campos JSON no data-model, mas a spec diz explicitamente:
- ❌ "Sem JSON columns - Tudo normalizado"
- ❌ "praticamente nada será por enum, pois iremos fazer boa parte de tudo configurável pelo mestre"

**Onde está**: 
- Comentário na classe `Ficha.java`: "Usa campos JSON para armazenar dados flexíveis e configuráveis"
- Mas a implementação atual NÃO tem campos JSON (está correto!)

**Status**: ✅ **IMPLEMENTAÇÃO ESTÁ CORRETA** - não há campos JSON na entidade

**Ação**: Remover comentário enganoso da classe

---

### 2. **MIGRATIONS DO FLYWAY DESORGANIZADAS E DUPLICADAS**

**Problema**: Migrations com nomenclatura inconsistente e duplicadas:

```
❌ V1_1__create_table_jogos.sql  (underscore duplo errado)
❌ V1_2__create_table_classes_personagem.sql
❌ V1_3__create_table_racas.sql
❌ V1.1__criar_tabela_classes_personagem.sql (duplicada!)
❌ V1.2__criar_tabela_racas.sql (duplicada!)
❌ V1.3__adicionar_imagem_url_em_jogos.sql
❌ V1.4__alterar_descricao_jogos_para_text.sql
❌ V1__create_usuarios_jogos.sql
❌ V2__create_jogo_tables.sql
❌ V2__create_table_fichas.sql (duplicada de V2!)
❌ V2__criar_tabela_fichas.sql (duplicada de V2!)
```

**Problemas identificados**:
1. Mistura de inglês (`create`) e português (`criar`)
2. Mistura de `_` (underscore) e `.` (ponto) na numeração de versão
3. Migrations duplicadas (V1.1, V1.2, V2)
4. Sequência numérica quebrada

**Padrão correto do Flyway**:
```
✅ V1__create_usuarios_jogos.sql
✅ V2__create_jogo_participantes.sql
✅ V3__create_classes_personagem.sql
✅ V4__create_racas.sql
✅ V5__create_fichas.sql
✅ V6__add_imagem_url_to_jogos.sql
✅ V7__alter_descricao_jogos_to_text.sql
```

**Melhores práticas Flyway**:
- ✅ Versão sequencial simples: V1, V2, V3...
- ✅ Dois underscores `__` separando versão de descrição
- ✅ Descrição em snake_case e inglês
- ✅ Um arquivo = uma mudança lógica
- ✅ Jamais alterar migrations já aplicadas
- ❌ Não usar ponto `.` para subversão (V1.1, V1.2)

**Ação**: Renomear todas as migrations seguindo o padrão

---

### 3. **MÉTODO DEPRECATED: Refill.intervally()**

**Problema**: `RateLimitConfig.java` usa método deprecated do Bucket4j:

```java
❌ Refill.intervally(100, Duration.ofMinutes(1))  // DEPRECATED
```

**Solução**: Usar o método atual:

```java
✅ Refill.greedy(100, Duration.ofMinutes(1))
```

**Documentação Bucket4j**:
- `intervally()` → deprecated desde v7.x
- `greedy()` → método atual (refill imediato e constante)
- `smooth()` → alternativa (refill suave e gradual)

**Diferenças**:
- **greedy**: Adiciona tokens imediatamente no início do período
- **smooth**: Distribui tokens uniformemente ao longo do período
- **intervally**: Comportamento legado (deprecated)

**Recomendação**: Usar `greedy()` para APIs REST (comportamento esperado)

**Ação**: Substituir todos os `Refill.intervally()` por `Refill.greedy()`

---

### 4. **FALTA DE ENTIDADES DE CONFIGURAÇÃO**

**Problema**: Segundo a spec, TUDO deve ser configurável por tabelas, mas faltam entidades:

**Planejado mas NÃO implementado**:
- ❌ `AtributoConfig` - Configuração de atributos por jogo
- ❌ `NivelConfig` - Tabela de XP e limitadores por nível
- ❌ `TipoAptidao` - Categorias de aptidões (Física, Mental)
- ❌ `AptidaoConfig` - Aptidões configuráveis
- ❌ `BonusConfig` - Tipos de bônus configuráveis
- ❌ `MembroCorpoConfig` - Membros do corpo configuráveis
- ❌ `EssenciaConfig` - Configuração de cálculo de essência
- ❌ `AmeacaConfig` - Configuração de cálculo de ameaça
- ❌ `VidaConfig` - Configuração de cálculo de vida
- ❌ `DadoProspeccaoConfig` - Dados de prospecção configuráveis
- ❌ `CategoriaVantagem` - Categorias de vantagens
- ❌ `VantagemConfig` - Vantagens configuráveis
- ❌ `VantagemPreRequisito` - Pré-requisitos de vantagens
- ❌ `VantagemEfeito` - Efeitos concedidos por vantagens
- ❌ `ClasseBonus` - Bônus concedidos por classe
- ❌ `ClasseAptidaoBonus` - Bônus de aptidões por classe
- ❌ `RacaAtributoBonus` - Bônus de atributos por raça
- ❌ `RacaClassePermitida` - Classes permitidas por raça

**Entidades de Ficha que faltam**:
- ❌ `FichaAtributo` - Valores de atributos por ficha
- ❌ `FichaAptidao` - Valores de aptidões por ficha
- ❌ `FichaBonus` - Bônus aplicados à ficha
- ❌ `FichaVida` - Dados de vida da ficha
- ❌ `FichaVidaMembro` - Danos por membro
- ❌ `FichaEssencia` - Dados de essência
- ❌ `FichaAmeaca` - Dados de ameaça
- ❌ `FichaProspeccao` - Dados de prospecção
- ❌ `FichaVantagem` - Vantagens adquiridas

**Já implementadas** ✅:
- ✅ `Usuario`
- ✅ `Jogo`
- ✅ `JogoParticipante`
- ✅ `Ficha`
- ✅ `ClassePersonagem`
- ✅ `Raca`
- ✅ `Anotacao`

**Ação**: Criar todas as entidades de configuração e de ficha

---

### 5. **EMBEDDED vs NORMALIZADO**

**Problema**: A classe `Atributos.java` está como `@Embeddable`, mas deveria ser uma tabela separada:

```java
❌ @Embeddable
   public class Atributos { ... }
```

**Deve ser**:

```java
✅ @Entity
   @Table(name = "ficha_atributos")
   public class FichaAtributo { 
       @ManyToOne private Ficha ficha;
       @ManyToOne private AtributoConfig atributo;
       private Integer valorBase;
       private Integer valorNivel;
       private Integer valorOutros;
       // total calculado
   }
```

**Por quê**: 
- Flexibilidade: Mestre pode adicionar/remover atributos
- Configurável: Cada jogo tem seus próprios atributos
- Histórico: Envers pode auditar mudanças em cada atributo
- Extensível: Pode adicionar novos campos sem alterar schema

**Ação**: Transformar `Atributos` em `AtributoConfig` + `FichaAtributo`

---

### 6. **FALTA CAMPO is_npc NA FICHA**

**Problema**: Data model especifica campo `is_npc BOOLEAN` mas não está na entidade `Ficha.java`

**Planejado**:
```sql
is_npc BOOLEAN DEFAULT false
```

**Ação**: Adicionar campo `isNpc` na entidade `Ficha`

---

### 7. **VALIDAÇÃO: nivel vs experiencia**

**Problema**: A entidade `Ficha` tem campo `nivel` com validação fixa, mas segundo a spec:

> "quando o jogador comprar uma vantagem, a compra não pode ser revertida"  
> "o custo para subir de nível também deve ser configurável, o padrão é ponto * nivel"

**Implementado**:
```java
@Min(value = 1, message = ValidationMessages.Ficha.NIVEL_MINIMO)
@Max(value = 99, message = ValidationMessages.Ficha.NIVEL_MAXIMO)
private Integer nivel = 1;
```

**Deveria ser**:
- Nível calculado automaticamente pela tabela `NivelConfig`
- Baseado na experiência acumulada
- Sem validação fixa de máximo

**Ação**: 
1. Remover validação `@Max(value = 99)`
2. Calcular nível dinamicamente via `NivelConfig`
3. Adicionar lógica de cálculo no service

---

## 📋 PLANO DE CORREÇÕES

### Prioridade 1 - CRÍTICO (fazer agora)

1. ✅ **Limpar migrations do Flyway**
   - Renomear todas seguindo padrão V1, V2, V3...
   - Remover duplicatas
   - Consolidar em sequência lógica

2. ✅ **Corrigir Refill.intervally() deprecated**
   - Substituir por `Refill.greedy()` em `RateLimitConfig.java`

3. ✅ **Remover comentário enganoso da classe Ficha**
   - Remover menção a "campos JSON"

4. ✅ **Adicionar campo isNpc em Ficha**

### Prioridade 2 - ALTA (próximas tasks)

5. ⬜ **Criar entidades de Configuração básicas**
   - `AtributoConfig`
   - `NivelConfig`
   - `TipoAptidao`
   - `AptidaoConfig`
   - `BonusConfig`
   - `MembroCorpoConfig`

6. ⬜ **Criar entidades de Configuração de sistema**
   - `EssenciaConfig`
   - `AmeacaConfig`
   - `VidaConfig`
   - `DadoProspeccaoConfig`

7. ⬜ **Criar entidades de Vantagens**
   - `CategoriaVantagem`
   - `VantagemConfig`
   - `VantagemPreRequisito`
   - `VantagemEfeito`

### Prioridade 3 - MÉDIA (depois das configs)

8. ⬜ **Criar entidades de relacionamento Classe**
   - `ClasseBonus`
   - `ClasseAptidaoBonus`

9. ⬜ **Criar entidades de relacionamento Raça**
   - `RacaAtributoBonus`
   - `RacaClassePermitida`

10. ⬜ **Criar entidades de Ficha**
    - `FichaAtributo`
    - `FichaAptidao`
    - `FichaBonus`
    - `FichaVida`
    - `FichaVidaMembro`
    - `FichaEssencia`
    - `FichaAmeaca`
    - `FichaProspeccao`
    - `FichaVantagem`

11. ⬜ **Transformar Atributos de Embedded para normalizado**

### Prioridade 4 - BAIXA (refinamentos)

12. ⬜ **Ajustar validação de nível**
    - Remover max fixo
    - Implementar cálculo dinâmico

13. ⬜ **Criar seeds de dados**
    - Popular tabelas de configuração com valores do legado
    - Atributos padrão (Força, Agilidade, etc)
    - Níveis padrão (0-35)
    - Aptidões padrão (Físicas e Mentais)

---

## 📊 RESUMO DA ANÁLISE

| Categoria | Status | Observação |
|-----------|--------|------------|
| **JSON columns** | ✅ OK | Não há JSON (comentário enganoso apenas) |
| **Flyway migrations** | ❌ CRÍTICO | Duplicadas e nomenclatura errada |
| **Bucket4j Refill** | ❌ CRÍTICO | Método deprecated |
| **Entidades Config** | ❌ CRÍTICO | 18 entidades faltando |
| **Entidades Ficha** | ❌ CRÍTICO | 9 entidades faltando |
| **Campo isNpc** | ❌ MÉDIO | Falta implementar |
| **Embedded Atributos** | ❌ MÉDIO | Deveria ser normalizado |
| **Validação nível** | ⚠️ BAIXO | Ajustar para dinâmico |

**Progresso atual**: ~20% do modelo de dados implementado

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ Fazer correções CRÍTICAS (Prioridade 1)
2. ✅ Atualizar checklist.md com status real
3. ⬜ Implementar entidades de configuração (Prioridade 2)
4. ⬜ Implementar entidades de ficha (Prioridade 3)
5. ⬜ Criar migrations consolidadas
6. ⬜ Criar seeds de dados do legado
7. ⬜ Implementar repositories
8. ⬜ Implementar services com lógica de cálculo
9. ⬜ Implementar DTOs com MapStruct
10. ⬜ Implementar controllers
11. ⬜ Implementar testes de integração

---

**Conclusão**: A implementação está no caminho certo estruturalmente, mas faltam MUITAS entidades para completar o sistema configurável. As correções críticas são rápidas, mas criar todas as entidades levará tempo.
