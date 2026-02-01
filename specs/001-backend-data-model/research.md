# Research: Modelagem de Dados e API Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Date**: 2026-02-01  
**Revisado**: 2026-02-01 - Mudança para sistema totalmente configurável

## Princípios de Design (Atualizados)

### ✅ Decisões Chave

1. **Tudo configurável pelo Mestre** - Nenhum valor hardcoded
2. **Banco de dados normalizado** - Sem colunas JSON
3. **MapStruct para mapeamento** - Sem JPA Converters
4. **Tabelas para tudo** - Classes, aptidões, atributos são entidades
5. **Histórico de alterações** - Audit trail completo
6. **Mínimo de Enums** - Apenas para tipos fixos do sistema

---

## Research 1: Sistema de Atributos Klayrah (REVISADO)

### Decision: Tabela de configuração de atributos + Tabela de valores por ficha

### Rationale

O Mestre precisa poder:
- Criar/editar/remover atributos
- Definir fórmulas de Ímpeto por atributo
- Configurar valores mínimos/máximos
- Aplicar configurações por Jogo

### Estrutura de Tabelas

```
ATRIBUTO_CONFIG (configuração do atributo - definido pelo Mestre)
├── id
├── jogo_id (FK) - Cada jogo pode ter atributos diferentes
├── nome (ex: "Força", "Agilidade")
├── descricao
├── formula_impeto (ex: "TOTAL * 3", "TOTAL / 10")
├── descricao_impeto (ex: "Capacidade de carga em kg")
├── valor_minimo (default 0)
├── valor_maximo (default 999)
├── ordem_exibicao
├── ativo
└── criado_em, atualizado_em

FICHA_ATRIBUTO (valores do atributo na ficha)
├── id
├── ficha_id (FK)
├── atributo_config_id (FK)
├── base
├── nivel
├── outros_bonus
└── criado_em, atualizado_em
```

### Fórmulas de Ímpeto (Configuráveis)

O Mestre define a fórmula como string que será interpretada:
- `TOTAL * 3` → Força (Capacidade de carga)
- `TOTAL / 3` → Agilidade (Deslocamento)
- `TOTAL / 10` → Vigor (RD), Sabedoria (RDM), Astúcia (Estratégia)
- `MIN(TOTAL / 20, 3)` → Intuição (Pontos de Sorte)
- `TOTAL / 20` → Inteligência (Comando)

### Alternatives Considered

1. ~~JSON column~~ - Rejeitada por requisito do usuário
2. ~~@Embeddable~~ - Rejeitada - não permite configuração dinâmica
3. **Tabelas separadas**: ✅ Escolhida - flexibilidade total

---

## Research 2: Sistema de Experiência e Níveis (REVISADO)

### Decision: Tabelas de configuração de níveis por Jogo

### Rationale

O Mestre precisa poder:
- Definir quantos níveis existem
- Configurar XP necessário para cada nível
- Configurar Limitador por nível
- Configurar pontos de atributo por nível

### Estrutura de Tabelas

```
NIVEL_CONFIG (configuração de nível - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── numero_nivel (0, 1, 2, ... 35)
├── experiencia_necessaria
├── limitador_atributo (valor máximo de atributo neste nível)
├── pontos_atributo (pontos para distribuir ao atingir este nível)
├── descricao (ex: "Novato", "Veterano", "Lenda")
├── permite_renascimento (boolean)
└── criado_em, atualizado_em

-- Valores padrão que o Mestre pode alterar:
-- Nível 0: XP=0, Limitador=10, Pontos=0
-- Nível 1: XP=1000, Limitador=10, Pontos=3
-- Nível 2-20: XP variado, Limitador=50, Pontos=3
-- Nível 21-25: Limitador=75
-- Nível 26-30: Limitador=100
-- Nível 31-35: Limitador=120, permite_renascimento=true
```

### Cálculo de Nível

```java
// No service, não mais constante
public int calcularNivel(Long jogoId, int experiencia) {
    return nivelConfigRepository
        .findByJogoIdAndExperienciaNecessariaLessThanEqual(jogoId, experiencia)
        .stream()
        .mapToInt(NivelConfig::getNumeroNivel)
        .max()
        .orElse(0);
}
```

---

## Research 3: Sistema de Vida por Membro (REVISADO)

### Decision: Tabela de configuração de membros + Tabela de vida por ficha

### Rationale

O Mestre precisa poder:
- Definir quais membros do corpo existem
- Configurar porcentagem de vida de cada membro
- Adicionar/remover membros conforme o sistema de RPG

### Estrutura de Tabelas

```
MEMBRO_CORPO_CONFIG (configuração - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── nome (ex: "Cabeça", "Tronco", "Braço Direito")
├── porcentagem_vida (0.25, 0.75, 1.0)
├── ordem_exibicao
├── ativo
└── criado_em, atualizado_em

FICHA_VIDA (valores de vida na ficha)
├── id
├── ficha_id (FK)
├── vantagens_bonus
├── outros_bonus
└── criado_em, atualizado_em

FICHA_VIDA_MEMBRO (dano por membro)
├── id
├── ficha_id (FK)
├── membro_config_id (FK)
├── dano_atual
└── criado_em, atualizado_em
```

---

## Research 4: Sistema de Aptidões (REVISADO)

### Decision: Tabela de configuração de aptidões + Tabela de valores por ficha

### Rationale

O Mestre precisa poder:
- Criar aptidões customizadas
- Definir tipo (física/mental ou outros tipos)
- Configurar por jogo

### Estrutura de Tabelas

```
TIPO_APTIDAO (tipos de aptidão - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── nome (ex: "Física", "Mental", "Social", "Mágica")
├── descricao
├── ordem_exibicao
└── ativo

APTIDAO_CONFIG (configuração de aptidão - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── tipo_aptidao_id (FK)
├── nome (ex: "Acrobacia", "Diplomacia")
├── descricao
├── ordem_exibicao
├── ativo
└── criado_em, atualizado_em

FICHA_APTIDAO (valores da aptidão na ficha)
├── id
├── ficha_id (FK)
├── aptidao_config_id (FK)
├── base
├── sorte
├── classe_bonus
└── criado_em, atualizado_em
```

---

## Research 5: Sistema de Bônus (REVISADO)

### Decision: Tabela de configuração de bônus + Tabela de valores por ficha

### Rationale

O Mestre precisa poder:
- Definir quais bônus existem
- Configurar fórmulas de cálculo base
- Definir quais modificadores cada bônus aceita

### Estrutura de Tabelas

```
BONUS_CONFIG (configuração de bônus - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── nome (ex: "B.B.A", "Bloqueio", "Reflexo")
├── descricao
├── formula_base (ex: "(FORCA + AGILIDADE) / 3")
├── ordem_exibicao
├── ativo
└── criado_em, atualizado_em

FICHA_BONUS (valores do bônus na ficha)
├── id
├── ficha_id (FK)
├── bonus_config_id (FK)
├── vantagens
├── classe_bonus
├── itens
├── gloria
├── outros_bonus
└── criado_em, atualizado_em
```

---

## Research 6: Sistema de Classes (REVISADO)

### Decision: Tabela de classes cadastráveis pelo Mestre

### Rationale

O Mestre precisa poder:
- Criar classes customizadas
- Configurar bônus por classe
- Associar classes a raças (opcional)
- Definir restrições por nível

### Estrutura de Tabelas

```
CLASSE_PERSONAGEM (classes - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── nome (ex: "Guerreiro", "Mago", "Arqueiro")
├── descricao
├── nivel_minimo (nível necessário para usar esta classe)
├── ativo
└── criado_em, atualizado_em

CLASSE_BONUS (bônus que a classe fornece)
├── id
├── classe_id (FK)
├── bonus_config_id (FK) -- qual bônus recebe
├── valor -- quanto recebe
└── nivel_necessario (em qual nível ganha este bônus)

CLASSE_APTIDAO_BONUS (bônus de aptidão que a classe fornece)
├── id
├── classe_id (FK)
├── aptidao_config_id (FK)
├── valor
└── nivel_necessario
```

---

## Research 7: Sistema de Raças (NOVO)

### Decision: Tabela de raças cadastráveis pelo Mestre

### Rationale

O Mestre pode configurar bônus e restrições por raça.

### Estrutura de Tabelas

```
RACA (raças - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── nome (ex: "Humano", "Elfo", "Anão")
├── descricao
├── ativo
└── criado_em, atualizado_em

RACA_ATRIBUTO_BONUS (bônus de atributo por raça)
├── id
├── raca_id (FK)
├── atributo_config_id (FK)
├── valor_bonus (pode ser negativo)

RACA_CLASSE_PERMITIDA (classes permitidas por raça - opcional)
├── id
├── raca_id (FK)
├── classe_id (FK)
```

---

## Research 8: Histórico de Alterações (NOVO)

### Decision: Audit trail completo com tabela de histórico

### Rationale

O Mestre precisa ver todas as alterações feitas nas fichas.

### Estrutura de Tabelas

```
HISTORICO_ALTERACAO
├── id
├── ficha_id (FK)
├── usuario_id (FK) -- quem fez a alteração
├── tipo_alteracao (CRIACAO, ATUALIZACAO, EXCLUSAO)
├── entidade_alterada (ex: "FICHA_ATRIBUTO", "FICHA_VIDA")
├── campo_alterado (ex: "base", "dano_atual")
├── valor_anterior (TEXT)
├── valor_novo (TEXT)
├── ip_origem
├── user_agent
├── criado_em
```

### Implementação com Spring Data Envers ou Custom

**Opção 1**: Hibernate Envers (automático)
```java
@Entity
@Audited
public class FichaAtributo { ... }
```

**Opção 2**: Custom Listener (mais controle)
```java
@Component
public class AuditListener {
    @PreUpdate
    public void beforeUpdate(Object entity) {
        // Salvar histórico manualmente
    }
}
```

**Decisão**: Usar Hibernate Envers para simplicidade, com tabelas `_AUD`.

---

## Research 9: MapStruct para Mapeamento (NOVO)

### Decision: MapStruct ao invés de JPA Converters

### Rationale

- Type-safe em tempo de compilação
- Melhor performance (código gerado)
- Mais flexível para transformações complexas
- Padrão da indústria

### Configuração

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### Exemplo de Mapper

```java
@Mapper(componentModel = "spring")
public interface FichaMapper {
    
    @Mapping(source = "usuario.nome", target = "nomeJogador")
    @Mapping(source = "jogo.nome", target = "nomeJogo")
    FichaResponse toResponse(Ficha ficha);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    Ficha toEntity(CriarFichaRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(AtualizarFichaRequest request, @MappingTarget Ficha ficha);
}
```

---

## Research 10: Enums Mínimos (REVISADO)

### Decision: Apenas enums para tipos fixos do sistema

### Enums Mantidos (fixos, não configuráveis)

```java
// Tipos de role em jogo (fixo)
public enum RoleJogo {
    MESTRE, JOGADOR
}

// Tipos de galeria (fixo)
public enum TipoGaleria {
    PERSONAGEM, ITEM
}

// Gênero (pode ser expandido mas é fixo por natureza)
public enum Genero {
    MASCULINO, FEMININO, OUTRO, NAO_INFORMADO
}

// Tipo de alteração no histórico (fixo)
public enum TipoAlteracao {
    CRIACAO, ATUALIZACAO, EXCLUSAO
}
```

### Removidos (agora são tabelas)

- ~~ClassePersonagem~~ → Tabela `CLASSE_PERSONAGEM`
- ~~NomeAtributo~~ → Tabela `ATRIBUTO_CONFIG`
- ~~NomeAptidao~~ → Tabela `APTIDAO_CONFIG`
- ~~TipoAptidao~~ → Tabela `TIPO_APTIDAO`
- ~~MembroCorpo~~ → Tabela `MEMBRO_CORPO_CONFIG`
- ~~NomeBonus~~ → Tabela `BONUS_CONFIG`
- ~~Indole~~ → Pode ser tabela ou campo livre
- ~~Presenca~~ → Pode ser tabela ou campo livre

---

## Research 11: Essência e Ameaça (REVISADO)

### Decision: Tabelas de configuração para fórmulas de Essência e Ameaça

### Estrutura de Tabelas

```
ESSENCIA_CONFIG (configuração - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── formula (ex: "(VIGOR + SABEDORIA) / 2 + NIVEL + RENASCIMENTOS")
├── descricao
└── criado_em, atualizado_em

FICHA_ESSENCIA
├── id
├── ficha_id (FK)
├── vantagens_bonus
├── outros_bonus
├── gastos_atual
└── criado_em, atualizado_em

AMEACA_CONFIG (configuração - definido pelo Mestre)
├── id
├── jogo_id (FK)
├── formula (ex: "NIVEL + ITENS + TITULOS + RENASCIMENTOS")
├── descricao
└── criado_em, atualizado_em

FICHA_AMEACA
├── id
├── ficha_id (FK)
├── itens_bonus
├── titulos_bonus
├── outros_bonus
└── criado_em, atualizado_em
```

---

## Research 12: Seed Data / Dados Iniciais

### Decision: Scripts de seed para configuração padrão Klayrah

### Rationale

Quando um Mestre cria um novo Jogo, ele pode:
1. Começar do zero (configurar tudo manualmente)
2. Usar template "Klayrah Padrão" (copia configurações default)

### Implementação

```java
@Service
public class TemplateJogoService {
    
    public void aplicarTemplateKlayrah(Long jogoId) {
        // Cria atributos padrão (Força, Agilidade, Vigor, etc.)
        criarAtributosPadrao(jogoId);
        
        // Cria aptidões padrão (24 aptidões)
        criarAptidoesPadrao(jogoId);
        
        // Cria níveis padrão (0-35)
        criarNiveisPadrao(jogoId);
        
        // Cria classes padrão (Guerreiro, Mago, etc.)
        criarClassesPadrao(jogoId);
        
        // Cria membros do corpo padrão
        criarMembrosPadrao(jogoId);
        
        // Cria bônus padrão (BBA, Bloqueio, etc.)
        criarBonusPadrao(jogoId);
    }
}
```

---

## Summary of Decisions (ATUALIZADO)

| Research | Decisão Anterior | Nova Decisão | Confidence |
|----------|------------------|--------------|------------|
| Atributos | @Embeddable JSON | Tabelas separadas (CONFIG + FICHA_) | High |
| XP/Níveis | Constante no código | Tabela NIVEL_CONFIG por jogo | High |
| Vida/Membros | Enum + JSON | Tabelas separadas (CONFIG + FICHA_) | High |
| Aptidões | JSON com Enum | Tabelas separadas (TIPO + CONFIG + FICHA_) | High |
| Bônus | JSON | Tabelas separadas (CONFIG + FICHA_) | High |
| Classes | Enum | Tabela CLASSE_PERSONAGEM | High |
| Raças | Não existia | Tabela RACA com bônus | High |
| Prospecção | Não persistido | Tabelas separadas (CONFIG + FICHA_) | High |
| Histórico | Não para MVP | Hibernate Envers (obrigatório) | High |
| Mapeamento | JPA Converter | MapStruct (@Mapping) | High |
| Enums | Muitos | Mínimo (apenas tipos fixos) | High |

---

## Impacto nas Outras Decisões

### Spring Boot 4
- Virtual Threads: ✅ Mantido
- Problem Details: ✅ Mantido

### JPA
- ~~JSON columns~~ → Tabelas normalizadas
- ~~@Convert~~ → Relacionamentos JPA normais
- Hibernate Envers para auditoria

### Performance
- Mais JOINs, mas queries mais flexíveis
- Considerar cache (Spring Cache) para configs que mudam pouco
- Índices adequados nas FKs

---

## Funcionalidades para Implementação Futura

- Exportação de Ficha em PDF
- Upload de Imagens/Galeria
- Análise de Imagem com IA (Gemini)
- Sugestão de Interpretação com IA

---

## Open Questions (Resolved)

- ✅ Como configurar atributos? → Tabela ATRIBUTO_CONFIG por jogo
- ✅ Como configurar níveis? → Tabela NIVEL_CONFIG por jogo
- ✅ Como configurar classes? → Tabela CLASSE_PERSONAGEM por jogo
- ✅ Como configurar prospecção? → Tabela DADO_PROSPECCAO_CONFIG por jogo
- ✅ Histórico é necessário? → Sim, Hibernate Envers
- ✅ Usar Converters? → Não, usar MapStruct
- ✅ JSON columns? → Não, tudo normalizado em tabelas
