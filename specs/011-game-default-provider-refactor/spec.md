# Spec 011 — Refatoração do GameDefaultConfigProvider

> Status: 📝 Planejado
> Criado: 2026-04-09
> Depende de: nada (refatoração interna)
> Bloqueia: implementação das 64 vantagens, futuras configs default

## Contexto

O `DefaultGameConfigProviderImpl` é um monolito de ~887 linhas que contém **todas** as configurações default do jogo (18 tipos). Problemas atuais:

1. **Arquivo imenso** — difícil navegar e manter
2. **Vantagens inconsistentes** — 33 placeholders usam campos errados (`tipoBonus`, `custoBase`) em vez dos campos corretos do CSV (`sigla`, `tipoVantagem`, `categoriaNome`)
3. **Sem separação por config** — editar atributos exige navegar por centenas de linhas de níveis, raças, itens
4. **Difícil para múltiplos desenvolvedores** — conflitos de merge são frequentes
5. **Testes monolíticos** — testar uma config requer instanciar o provider inteiro

## Solução: Composição por Config Type

### Princípio

Cada **grupo de configuração** vive em sua própria classe provider dentro de um sub-pacote `defaults/`. A classe principal (`DefaultGameConfigProviderImpl`) torna-se uma **thin facade** que compõe todos os providers parciais.

### Estrutura de Pacotes

```
config/
├── GameDefaultConfigProvider.java              ← interface (NÃO MUDA)
├── DefaultGameConfigProviderImpl.java          ← thin facade, delega para providers
└── defaults/                                   ← NOVO sub-pacote
    ├── DefaultAtributosProvider.java           ← 7 atributos
    ├── DefaultAptidoesProvider.java            ← 24 aptidões + 2 tipos aptidão
    ├── DefaultNiveisProvider.java              ← 36 níveis + 5 limitadores
    ├── DefaultClassesProvider.java             ← 12 classes
    ├── DefaultRacasProvider.java               ← 6 raças + bônus raciais
    ├── DefaultProspeccoesProvider.java         ← 6 dados de prospecção
    ├── DefaultConfigSimpleProvider.java        ← gêneros (3) + índoles (3) + presenças (4) + membros corpo (7)
    ├── DefaultBonusProvider.java               ← 9 bônus calculados (BBA, BBM, etc.)
    ├── DefaultPontosVantagemProvider.java      ← 35 pontos de vantagem por nível
    ├── DefaultVantagensProvider.java           ← 9 categorias + 64 vantagens (9 métodos internos por categoria)
    └── DefaultItensProvider.java               ← 7 raridades + 20 tipos + 40 itens
```

### Tamanho por arquivo (estimativa)

| Arquivo | Linhas | Configs |
|---------|--------|---------|
| `DefaultGameConfigProviderImpl` (facade) | ~80 | 0 (delega tudo) |
| `DefaultAtributosProvider` | ~50 | 7 atributos |
| `DefaultAptidoesProvider` | ~90 | 24 aptidões |
| `DefaultNiveisProvider` | ~100 | 36 níveis + 5 limitadores |
| `DefaultClassesProvider` | ~50 | 12 classes |
| `DefaultRacasProvider` | ~80 | 6 raças + bônus |
| `DefaultProspeccoesProvider` | ~40 | 6 dados |
| `DefaultConfigSimpleProvider` | ~60 | 3+3+4+7 = 17 configs simples |
| `DefaultBonusProvider` | ~50 | 9 bônus |
| `DefaultPontosVantagemProvider` | ~60 | 35 pontos |
| `DefaultVantagensProvider` | ~350 | 9 categorias + 64 vantagens |
| `DefaultItensProvider` | ~250 | 7+20+40 = 67 itens |

**Total**: ~1260 linhas em 12 arquivos vs ~887 em 1 arquivo. Pouco mais de código total, mas **cada arquivo é focado e fácil de navegar**.

---

## Padrão de Cada Provider

### Regras

1. **Classe `@Component`** — Spring gerencia o ciclo de vida
2. **Sem estado mutável** — todos os métodos retornam `List.of()` / `Map.of()` imutáveis
3. **Dados inline** — sem arquivo externo (YAML, JSON, CSV). Código Java puro
4. **`List.of()` para listas imutáveis** — nunca `new ArrayList<>()`
5. **Factory methods estáticos nos DTOs** — usar `DTO.of(...)` ou `DTO.builder()...build()` conforme complexidade
6. **Comentários de seção** — agrupar itens com `// === Categoria ===`

### Template — Provider Simples (ex: Gêneros)

```java
package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.GeneroConfigDTO;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Configurações default de Gênero para novos jogos.
 *
 * <p>Para alterar: edite as entradas abaixo e rode os testes.</p>
 * <p>Para adicionar: crie novo DTO.of(...) mantendo ordemExibicao sequencial.</p>
 * <p>Para remover: delete a linha e ajuste ordemExibicao dos restantes.</p>
 */
@Component
public class DefaultGenerosProvider {

    public List<GeneroConfigDTO> get() {
        return List.of(
            GeneroConfigDTO.of("Masculino", "Personagem de identidade masculina",                            1),
            GeneroConfigDTO.of("Feminino",  "Personagem de identidade feminina",                             2),
            GeneroConfigDTO.of("Outro",     "Personagem com identidade de gênero não binária ou indefinida", 3)
        );
    }
}
```

### Template — Provider com Sub-Métodos (ex: Vantagens)

```java
@Component
public class DefaultVantagensProvider {

    /**
     * 9 categorias de vantagem.
     */
    public List<CategoriaVantagemDTO> getCategorias() {
        return List.of(
            CategoriaVantagemDTO.of("Treinamento Físico",       "#e74c3c", 1),
            CategoriaVantagemDTO.of("Treinamento Mental",       "#8e44ad", 2),
            // ...
        );
    }

    /**
     * 64 vantagens completas (9 categorias).
     */
    public List<VantagemConfigDTO> getVantagens() {
        List<VantagemConfigDTO> result = new java.util.ArrayList<>();
        result.addAll(buildTreinamentoFisico());
        result.addAll(buildTreinamentoMental());
        result.addAll(buildAcao());
        result.addAll(buildReacao());
        result.addAll(buildAtributo());
        result.addAll(buildGeral());
        result.addAll(buildHistorica());
        result.addAll(buildRenascimento());
        result.addAll(buildRaciais());
        return java.util.Collections.unmodifiableList(result);
    }

    // === Treinamento Físico (3) ===
    private List<VantagemConfigDTO> buildTreinamentoFisico() {
        return List.of(
            vantagem("TCO", "Treinamento em Combate Ofensivo", "...", 10, "4",
                     "+1 B.B.A e 1 dado de dano (D3→D.UP) por nível",
                     "VANTAGEM", "Treinamento Físico", 1),
            // ...
        );
    }

    // === Método helper para reduzir boilerplate ===
    private VantagemConfigDTO vantagem(String sigla, String nome, String descricao,
                                       int nivelMax, String formulaCusto,
                                       String valorBonusFormula,
                                       String tipo, String categoria, int ordem) {
        return VantagemConfigDTO.builder()
                .sigla(sigla)
                .nome(nome)
                .descricao(descricao)
                .nivelMaximoVantagem(nivelMax)
                .formulaCusto(formulaCusto)
                .valorBonusFormula(valorBonusFormula)
                .tipoVantagem(tipo)
                .categoriaNome(categoria)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .ordemExibicao(ordem)
                .build();
    }
}
```

### Template — Facade (DefaultGameConfigProviderImpl refatorado)

```java
@Component
public class DefaultGameConfigProviderImpl implements GameDefaultConfigProvider {

    private final DefaultAtributosProvider atributosProvider;
    private final DefaultAptidoesProvider aptidoesProvider;
    private final DefaultNiveisProvider niveisProvider;
    private final DefaultClassesProvider classesProvider;
    private final DefaultRacasProvider racasProvider;
    private final DefaultProspeccoesProvider prospeccoesProvider;
    private final DefaultConfigSimpleProvider simpleProvider;
    private final DefaultBonusProvider bonusProvider;
    private final DefaultPontosVantagemProvider pontosVantagemProvider;
    private final DefaultVantagensProvider vantagensProvider;
    private final DefaultItensProvider itensProvider;

    // Constructor injection via @RequiredArgsConstructor ou manual

    @Override
    public List<AtributoConfigDTO> getDefaultAtributos() {
        return atributosProvider.get();
    }

    @Override
    public List<AptidaoConfigDTO> getDefaultAptidoes() {
        return aptidoesProvider.get();
    }

    // ... um método por config, cada um delega ao provider correspondente
}
```

---

## Regras para os Campos de Vantagens

### Campos obrigatórios (usados pelo `GameConfigInitializerService.createVantagens()`)

| Campo | Tipo | Regra |
|-------|------|-------|
| `sigla` | String | 2-5 chars, ÚNICA cross-entity por jogo |
| `nome` | String | obrigatório |
| `descricao` | String | obrigatório |
| `nivelMaximoVantagem` | Integer | ≥ 1 |
| `formulaCusto` | String | exp4j válida. Variáveis: `nivel`, `base`, `total`, siglas atributos. **NÃO usar** `custo_base` ou `nivel_vantagem` |
| `valorBonusFormula` | String | texto livre descritivo do efeito (mapeado para `descricaoEfeito` na entity) |
| `tipoVantagem` | String | `"VANTAGEM"` ou `"INSOLITUS"` |
| `categoriaNome` | String | deve existir em `getDefaultCategoriasVantagem()` |
| `nivelMinimoPersonagem` | Integer | ≥ 1 |
| `podeEvoluir` | Boolean | `true` se nivelMaximo > 1 |
| `ordemExibicao` | Integer | sequencial global (1-64) |

### Campos IGNORADOS pelo initializer (não usar)

| Campo | Motivo |
|-------|--------|
| `custoBase` | Não existe no model. Usar `formulaCusto` diretamente |
| `tipoBonus` | Legado removido. Efeito descrito em `valorBonusFormula` |

---

## Como Editar Configurações

### Adicionar uma nova vantagem

1. Abra `DefaultVantagensProvider.java`
2. Encontre o método `build{Categoria}()` correto
3. Adicione o `VantagemConfigDTO` com todos os campos obrigatórios
4. Incremente `ordemExibicao` (sequencial global)
5. Rode `./mvnw test -Dtest=DefaultGameConfigProviderImplTest`

### Remover uma vantagem

1. Delete a entrada do `build{Categoria}()` correspondente
2. **NÃO** precisa ajustar `ordemExibicao` dos restantes (gaps são aceitos)
3. Atualize o teste `T5-11` (contagem total)

### Alterar campos de uma vantagem

1. Edite diretamente no `build{Categoria}()` correspondente
2. Se alterar `sigla`, verifique unicidade cross-entity
3. Se alterar `categoriaNome`, confirme que existe em `getCategorias()`
4. Rode testes

### Adicionar uma nova configuração (ex: HabilidadeConfig)

1. Crie `DefaultHabilidadesProvider.java` em `config/defaults/`
2. Adicione o método na interface `GameDefaultConfigProvider`
3. Adicione a delegação em `DefaultGameConfigProviderImpl`
4. Implemente em `GameConfigInitializerService.initializeGameConfigs()`
5. Adicione testes no `DefaultGameConfigProviderImplTest`

---

## Testes

### Estratégia de testes

**Um test class para o facade** (`DefaultGameConfigProviderImplTest`) que testa:
- Contagem de cada config type
- Unicidade de siglas/nomes dentro de cada type
- Referências cruzadas (categoriaNome existe, abreviação existe, etc.)
- Invariantes (INSOLITUS → formulaCusto = "0", etc.)

**Opcionalmente**, testes unitários para providers individuais se tiverem lógica complexa.

### Testes que devem existir

| ID | Descrição |
|----|-----------|
| T5-01 | 9 BonusConfig defaults com nome, sigla e formula |
| T5-02 | 35 PontosVantagemConfig defaults com valores corretos |
| T5-03 | Cabeça deve ter 75% da vida |
| T5-04 | Índole deve ter 3 valores |
| T5-05 | Presença deve ter 4 valores |
| T5-06 | Gênero deve ter 3 valores |
| T5-07 | Classe 'Necromante' existe |
| T5-08 | Membro 'Sangue' com 100% |
| T5-09 | 9 CategoriaVantagem defaults |
| T5-10 | Siglas BonusConfig únicas e 2-5 chars |
| T5-11 | 64 vantagens total |
| T5-12 | Siglas vantagens únicas e 2-5 chars |
| T5-13 | Todos INSOLITUS têm formulaCusto = "0" |
| T5-14 | Todas categoriaNome existem em getCategorias() |
| T5-15 | Nenhuma formulaCusto usa "custo_base" |
| T5-16 | 7 atributos com abreviações únicas |
| T5-17 | 6 raças com bônus raciais consistentes |
| T5-18 | 36 níveis (0-35) com XP crescente |
| T5-19 | 40 itens com raridade e tipo existentes |

---

## Migração (Passo a Passo)

### Fase 1: Criar estrutura + migrar configs simples
1. Criar pacote `config/defaults/`
2. Extrair configs simples (gênero, índole, presença, membros) → `DefaultConfigSimpleProvider`
3. Extrair atributos → `DefaultAtributosProvider`
4. Extrair bônus → `DefaultBonusProvider`
5. Atualizar facade para delegar
6. Testes devem continuar passando sem alteração

### Fase 2: Migrar configs de dados de jogo
7. Extrair aptidões → `DefaultAptidoesProvider`
8. Extrair níveis + limitadores → `DefaultNiveisProvider`
9. Extrair classes → `DefaultClassesProvider`
10. Extrair raças + bônus raciais → `DefaultRacasProvider`
11. Extrair prospecções → `DefaultProspeccoesProvider`
12. Extrair pontos de vantagem → `DefaultPontosVantagemProvider`

### Fase 3: Migrar vantagens (corrigindo dados)
13. Criar `DefaultVantagensProvider` com categorias + 9 métodos `build{Categoria}()`
14. Preencher as 64 vantagens do CSV com campos corretos
15. Remover vantagens legadas com campos errados
16. Corrigir testes

### Fase 4: Migrar itens
17. Extrair raridades + tipos + itens → `DefaultItensProvider`
18. Limpar `DefaultGameConfigProviderImpl` (apenas delegações)

---

## Fonte de Verdade dos Dados

Os CSVs em `docs/revisao-game-default/csv/` são a **fonte de verdade** dos dados default:

| CSV | Config |
|-----|--------|
| `01-tipo-aptidao.csv` | TipoAptidao |
| `02-genero-config.csv` | GeneroConfig |
| `03-indole-config.csv` | IndoleConfig |
| `04-presenca-config.csv` | PresencaConfig |
| `05-dado-prospeccao-config.csv` | DadoProspeccaoConfig |
| `06-categoria-vantagem.csv` | CategoriaVantagem |
| `07-raridade-item-config.csv` | RaridadeItemConfig |
| `08-atributo-config.csv` | AtributoConfig |
| `09-bonus-config.csv` | BonusConfig |
| `10-membro-corpo-config.csv` | MembroCorpoConfig |
| `11-nivel-config.csv` | NivelConfig |
| `12-pontos-vantagem-config.csv` | PontosVantagemConfig |
| `13-aptidao-config.csv` | AptidaoConfig |
| `14-tipo-item-config.csv` | TipoItemConfig |
| `15-classe-personagem.csv` | ClassePersonagem |
| `16-raca.csv` | Raca + bônus |
| `17-vantagem-config.csv` | VantagemConfig (64 vantagens) |
| `18-item-config.csv` | ItemConfig + efeitos |

Quando há divergência entre CSV e código Java, o **CSV é a referência canônica**. O código deve ser atualizado para refletir o CSV.

---

## Decisões de Design

### Por que não carregar de CSV/YAML em runtime?
- **Simplicidade** — código Java é type-safe, IDE ajuda com refactoring
- **Performance** — sem I/O de arquivo na inicialização
- **Testabilidade** — testar é instanciar a classe, sem mock de filesystem
- **Rastreabilidade** — git diff mostra exatamente o que mudou

### Por que composição e não herança?
- Providers são independentes, não compartilham estado
- Composição permite injetar/mockar providers individuais nos testes
- Sem acoplamento entre configs de tipos diferentes

### Por que `@Component` e não classes estáticas?
- Spring DI permite substituir providers em testes ou customizações
- `@Primary` permite que o Mestre crie seu próprio provider sem alterar o original
- Consistente com o pattern do projeto (tudo gerenciado pelo Spring)
