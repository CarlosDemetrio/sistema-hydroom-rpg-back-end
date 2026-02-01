# 📊 ANÁLISE REAL: Planejado vs Executado

**Data**: 2026-02-01  
**Status**: Em Desenvolvimento  

---

## ✅ O QUE FOI FEITO CORRETAMENTE

### 1. ✅ Entidades Core (100% Completo)
**Planejado**: Criar entidades Usuario, Jogo, JogoParticipante, Ficha  
**Executado**: ✅ TODAS criadas e funcionando

- ✅ `Usuario.java` - Autenticação OAuth2
- ✅ `Jogo.java` - Campanhas/Jogos
- ✅ `JogoParticipante.java` - Relacionamento M:N com role
- ✅ `Ficha.java` - Ficha de personagem
- ✅ Repositories e Services criados
- ✅ Testes de integração passando (H2)

**Evidência**: `./mvnw test` mostra 24 testes passando (Usuario + JogoParticipante)

---

### 2. ✅ Configuração do Sistema (100% Completo)
**Planejado**: OAuth2, Security, Exception Handler, Rate Limiting  
**Executado**: ✅ TUDO implementado

- ✅ `SecurityConfig.java` - OAuth2 + CSRF + CORS
- ✅ `CustomOAuth2UserService.java` - Auto-criação de usuários
- ✅ `GlobalExceptionHandler.java` - Tratamento global de erros
- ✅ `RateLimitFilter.java` - Bucket4j integrado
- ✅ Profiles configurados (dev, test, prod)

---

### 3. ✅ Configurações do Jogo - FEITO MAS COM PROBLEMA
**Planejado**: Tudo configurável por tabelas (SEM JSON)  
**Executado**: ⚠️ **PARCIALMENTE** - Algumas coisas usam JSON

#### ✅ O que está CORRETO:
- ✅ `AtributoConfig` - Tabela para atributos configuráveis
- ✅ `AptidaoConfig` - Tabela para aptidões configuráveis
- ✅ `BonusConfig` - Tabela para bônus configuráveis
- ✅ `MembroCorpoConfig` - Tabela para membros do corpo
- ✅ `NivelConfig` - Tabela para XP e limitadores
- ✅ `ClassePersonagem` - Tabela para classes
- ✅ `Raca` - Tabela para raças
- ✅ `GeneroConfig, IndoleConfig, PresencaConfig` - Configs de personalidade
- ✅ `VantagemConfig, CategoriaVantagem, PontosVantagemConfig` - Sistema de vantagens

#### ❌ O que está ERRADO (JSON encontrado):
```java
// Em VantagemConfig.java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Object> efeitos;  // ❌ JSON!

// Em ClassePersonagem.java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<Integer, Map<String, Integer>> bonusPorNivel;  // ❌ JSON!

// Em Raca.java
@Type(JsonType.class)
@Column(columnDefinition = "jsonb")
private Map<String, Integer> bonusAtributos;  // ❌ JSON!
```

**🔧 CORREÇÃO NECESSÁRIA**: Criar tabelas relacionadas:
- `ClasseBonusNivel` (classe_id, nivel, atributo/bonus, valor)
- `RacaBonusAtributo` (raca_id, atributo_config_id, valor)
- `VantagemEfeito` (vantagem_id, tipo_efeito, valor, formula)

---

### 4. ⚠️ Entidades da Ficha - FEITO MAS INCOMPLETO
**Planejado**: Ficha + entidades relacionadas para valores configuráveis  
**Executado**: ✅ Maioria criada, ⚠️ faltam algumas

#### ✅ O que existe:
- ✅ `Ficha.java` - Entidade principal
- ✅ `FichaAtributo` - Valores dos atributos
- ✅ `FichaAptidao` - Valores das aptidões
- ✅ `FichaBonus` - Valores dos bônus
- ✅ `FichaVida` - Sistema de vida
- ✅ `FichaVidaMembro` - Dano por membro
- ✅ `FichaEssencia` - Sistema de essência
- ✅ `FichaAmeaca` - Sistema de ameaça
- ✅ `FichaProspeccao` - Sistema de prospecção
- ✅ `FichaVantagem` - Vantagens compradas

#### ❌ O que FALTA:
- ❌ `FichaPersonalidade` - Para índole, presença, arquétipo
- ❌ `FichaBonusAdicional` - Para rastrear bônus de vantagens/classe/raça/itens por campo
- ❌ Controllers e Services para todas essas entidades
- ❌ Endpoints REST completos

---

### 5. ❌ Migrations Flyway - BAGUNÇA TOTAL

**Problema Crítico**: Arquivos duplicados e numeração errada!

```
V1__create_usuarios_jogos.sql          ✅ OK
V1_1__create_table_jogos.sql           ❌ DUPLICADO (conflita com V1.1)
V1.1__criar_tabela_classes_personagem.sql  ❌ Notação errada (ponto)
V1_2__create_table_classes_personagem.sql  ❌ DUPLICADO
V1.2__criar_tabela_racas.sql           ❌ DUPLICADO
V1_3__create_table_racas.sql           ❌ DUPLICADO
V2__create_jogo_tables.sql             ❌ DUPLICADO
V2__criar_tabela_fichas.sql            ❌ DUPLICADO (mesmo V2!)
```

**🚨 Flyway vai QUEBRAR** por causa dessas duplicações!

**🔧 CORREÇÃO NECESSÁRIA**:
1. Deletar TODAS as migrations
2. Recriar com numeração sequencial correta:
   - `V1__` - Base (usuarios)
   - `V2__` - Jogos
   - `V3__` - Configs
   - `V4__` - Fichas
   - `V5__` - Seeds

---

### 6. ❌ Auditoria Hibernate Envers - COMPLEXIDADE DESNECESSÁRIA

**Planejado**: Histórico de alterações  
**Executado**: ✅ Implementado com Envers  
**Problema**: **Adiciona complexidade que não precisamos agora**

Arquivos criados:
- `AuditableEntity.java`
- `CustomRevisionEntity.java`
- `CustomRevisionListener.java`
- `@Audited` em todas as entidades

**🔧 AÇÃO**: ❌ **REMOVER** Envers completamente

---

## 📋 RESUMO DO STATUS

| Componente | Planejado | Executado | Status | Ação |
|-----------|-----------|-----------|--------|------|
| Entidades Core | ✅ | ✅ | 100% | Nenhuma |
| Security/OAuth2 | ✅ | ✅ | 100% | Nenhuma |
| Rate Limiting | ✅ | ✅ | 100% | Nenhuma |
| Configs (sem JSON) | ✅ | ⚠️ | 70% | Remover JSON |
| Entidades Ficha | ✅ | ⚠️ | 80% | Completar |
| Migrations Flyway | ✅ | ❌ | 30% | Refazer tudo |
| Seeds Padrão | ✅ | ⚠️ | 50% | Completar |
| Auditoria Envers | ❓ | ✅ | N/A | **REMOVER** |
| Controllers REST | ✅ | ❌ | 20% | Implementar |
| Testes H2 | ✅ | ⚠️ | 40% | Completar |

---

## 🎯 PRIORIDADES DE CORREÇÃO

### 🔴 P0 - BLOQUEANTES (Fazer AGORA)

1. **Remover Hibernate Envers**
   - Deletar `AuditableEntity`, `CustomRevisionEntity`, `CustomRevisionListener`
   - Remover `@Audited` de todas as entidades
   - Remover dependência do pom.xml
   - Remover tabelas `revinfo` das migrations

2. **Corrigir Migrations Flyway**
   - Deletar TODAS as migrations atuais
   - Recriar numeração sequencial correta
   - Testar com Flyway clean + migrate

3. **Remover JSON das Configs**
   - Criar tabelas: `classe_bonus_nivel`, `raca_bonus_atributo`, `vantagem_efeito`
   - Migrar dados para tabelas relacionadas
   - Remover campos JSON

### 🟡 P1 - IMPORTANTES (Próximos dias)

4. **Completar Entidades da Ficha**
   - Criar `FichaPersonalidade`
   - Criar `FichaBonusAdicional` (para rastrear origem dos bônus)
   - Criar relacionamentos

5. **Criar Controllers REST**
   - `JogoController` completo
   - `FichaController` completo
   - `ConfigController` (para mestres configurarem o jogo)

6. **Criar Seeds Completos**
   - Template "Klayrah Padrão" com TODOS os dados do legado
   - Atributos, aptidões, bônus, membros, níveis, classes, raças, vantagens

### 🟢 P2 - DESEJÁVEIS (Depois)

7. **Completar Testes**
   - Testes de integração para todas as entidades
   - Testes de cálculos automáticos
   - Testes de validações

8. **Funcionalidades Extras**
   - Sistema de imagens
   - Sistema de anotações
   - NPCs

---

## 📝 CONCLUSÃO

### ✅ O que está BOM:
- Entidades core funcionando
- Security/OAuth2 configurado
- Rate Limiting funcionando
- Estrutura de configs criada

### ❌ O que está RUIM:
- **JSON em vez de tabelas** (vai contra os requisitos!)
- **Migrations duplicadas** (Flyway vai quebrar!)
- **Auditoria Envers** (complexidade desnecessária)
- **Controllers incompletos** (API não está pronta)

### 🎯 Próximos Passos:
1. Remover Envers (1h)
2. Limpar e refazer migrations (2h)
3. Remover JSON e criar tabelas (4h)
4. Completar entidades da ficha (2h)
5. Criar controllers REST (8h)

**Estimativa para corrigir**: 2-3 dias de trabalho focado
