# Implementation Plan: Aprofundamento das Configurações — Siglas, Fórmulas e Relacionamentos

**Spec**: `004-configuracoes-siglas-formulas`
**Branch**: `004-configuracoes-siglas-formulas`
**Epics**: EPIC 1 + EPIC 2 do EPICS-BACKLOG.md

---

## Phase 0 — Documentation Discovery (Completed)

### Fontes Consultadas

| Fonte | O que foi lido |
|---|---|
| `docs/glossario/04-siglas-formulas.md` | Regra de unicidade cross-entity, variáveis das fórmulas, funções suportadas |
| `docs/glossario/02-configuracoes-jogo.md` | Detalhes de cada config, campos, relacionamentos |
| `docs/EPICS-BACKLOG.md` | EPIC 1 (sub-entities, siglas) e EPIC 2 (fórmulas) |
| `service/FormulaEvaluatorService.java` | Assinaturas: `evaluate()`, `isValid()`, `calcularImpeto()`, `calcularDerivado()`, `calcularCustoVantagem()` |
| `service/configuracao/AbstractConfiguracaoService.java` | Hook points: `validarAntesCriar()`, `validarAntesAtualizar()` |
| `model/AtributoConfig.java` | Campo `abreviacao`, DB constraints |
| `model/VantagemConfig.java` | Sem sigla, sem categoria, sem pré-requisitos |
| `model/BonusConfig.java` | Sem sigla |
| `model/CategoriaVantagem.java` | Exists, Lombok errado, sem ConfiguracaoEntity |
| `model/PontosVantagemConfig.java` | Exists, Lombok errado |
| `model/ClassePersonagem.java` | Sem ClasseBonus, sem ClasseAptidaoBonus |
| `model/Raca.java` | Sem RacaClassePermitida |

### APIs Confirmadas (não inventar outras)

```java
// FormulaEvaluatorService — métodos existentes
double evaluate(String formula, Map<String, Double> variables)
boolean isValid(String formula, String... expectedVariables)
double calcularImpeto(String formula, int totalAtributo)
double calcularDerivado(String formula, Map<String, Integer> atributos)
int calcularCustoVantagem(String formula, int custoBase, int nivelVantagem)

// AbstractConfiguracaoService — hook points
protected void validarAntesCriar(T configuracao)     // override para validação
protected void validarAntesAtualizar(T existente, T atualizado)
```

### Anti-patterns identificados

- ❌ NÃO usar variáveis hardcoded (FOR, AGI, etc.) no FormulaEvaluatorService para validação de formulaBase — buscar siglas reais do jogo no DB
- ❌ NÃO adicionar lógica de negócio nos Controllers
- ❌ NÃO chamar mappers nos Services
- ❌ NÃO expor entities nos responses de Controller
- ❌ NÃO criar constraint única por sigla na DB cruzando tabelas — a unicidade cross-entity é validada no service

---

## Phase 1 — Sistema de Siglas

### O que implementar

#### 1.1 — Adicionar campo `sigla` em BonusConfig e VantagemConfig

**Arquivos a modificar:**
- `model/BonusConfig.java` — adicionar campo `sigla` (`@Size(min=2, max=5)`, `@NotBlank`, unique constraint DB: `(jogo_id, sigla)`)
- `model/VantagemConfig.java` — adicionar campo `sigla` (`@Size(min=2, max=5)`, nullable=true, unique constraint DB: `(jogo_id, sigla)` com `nullsNotDistinct` ou partial index)
- DTOs correspondentes: `CreateBonusRequest`, `UpdateBonusRequest`, `BonusResponse`, `CreateVantagemRequest`, `UpdateVantagemRequest`, `VantagemResponse`
- Mappers: `BonusConfigMapper`, `VantagemConfigMapper`

**Referência de padrão**: `AtributoConfig.java:abreviacao` para o campo, `AtributoResponse.java` para o DTO.

#### 1.2 — SiglaValidationService (cross-entity)

**Novo arquivo**: `service/configuracao/SiglaValidationService.java`

```java
// Contrato esperado
@Service
public class SiglaValidationService {
    // Injeta: ConfiguracaoAtributoRepository, BonusConfigRepository, VantagemConfigRepository

    void validarSiglaDisponivel(String sigla, Long jogoId, Long excludeId, TipoSigla tipo);
    List<SiglaEmUso> listarSiglasDoJogo(Long jogoId);
}

// Enum auxiliar
enum TipoSigla { ATRIBUTO, BONUS, VANTAGEM }

// Record de resposta
record SiglaEmUso(String sigla, TipoSigla tipo, Long entityId, String nome) {}
```

**Lógica de validação**: Para cada repositório, chamar `existsByJogoIdAndSiglaIgnoreCaseAndIdNot()` e fazer union dos resultados. Lançar `ConflictException` se duplicado.

#### 1.3 — Integrar SiglaValidationService nos services

**Arquivos a modificar:**
- `AtributoConfiguracaoService.java` — injetar `SiglaValidationService`, chamar em `validarAntesCriar` e `validarAntesAtualizar` para o campo `abreviacao`; também no `atualizarCampos` incluir atualização de `abreviacao`, `valorMinimo`, `valorMaximo` (atualmente ignorados)
- `BonusConfiguracaoService.java` — injetar `SiglaValidationService`, chamar em `validarAntesCriar` e `validarAntesAtualizar`
- `VantagemConfiguracaoService.java` — idem, mas sigla é opcional (só validar se não nula)

#### 1.4 — Endpoint de siglas do jogo

**Arquivo a criar**: `controller/configuracao/SiglaController.java`
**Rota**: `GET /api/jogos/{jogoId}/siglas`
**Swagger**: `@Operation(summary = "Lista todas as siglas em uso no jogo")`
**Security**: `hasAnyRole('MESTRE', 'JOGADOR')` — leitura

**Arquivo a criar**: `dto/response/configuracao/SiglaEmUsoResponse.java` (record)

#### 1.5 — Novos métodos de repositório necessários

- `AtributoConfigRepository`: `existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot(Long jogoId, String abreviacao, Long id)`
- `BonusConfigRepository`: `existsByJogoIdAndSiglaIgnoreCaseAndIdNot(Long jogoId, String sigla, Long id)` + `findSiglasByJogoId(Long jogoId)`
- `VantagemConfigRepository`: idem para sigla

### Checklist de verificação

- [ ] `BonusConfig.sigla` existe no model com @NotBlank e unique constraint `(jogo_id, sigla)`
- [ ] `VantagemConfig.sigla` existe no model (nullable), unique constraint `(jogo_id, sigla)` ignorando nulos
- [ ] `SiglaValidationService` cobre todos os 3 tipos de entidade
- [ ] `AtributoConfiguracaoService` chama validação cross-entity (não só unicidade interna)
- [ ] `GET /api/jogos/{id}/siglas` retorna lista com tipo e entityId
- [ ] Teste: criar sigla duplicada cross-entity rejeita
- [ ] Teste: excluir entidade remove sigla da lista

---

## Phase 2 — Validação de Fórmulas

### O que implementar

#### 2.1 — FormulaEvaluatorService: suporte a variáveis dinâmicas

**Arquivo a modificar**: `service/FormulaEvaluatorService.java`

Adicionar método de validação com conjunto de variáveis esperadas (sem hardcode):

```java
// Novo método
public ValidationResult validarFormula(String formula, Set<String> variaveisPermitidas)
// Returns: isValid + lista de variáveis não reconhecidas + mensagem de erro de sintaxe

record ValidationResult(boolean valid, List<String> variaveisInvalidas, String erroSintaxe) {}
```

O método existente `isValid(String formula, String... expectedVariables)` pode ser mantido para compatibilidade.

#### 2.2 — Validação de formulaImpeto (AtributoConfig)

**Arquivo a modificar**: `AtributoConfiguracaoService.java`

Em `validarAntesCriar` e `validarAntesAtualizar`, se `formulaImpeto` não nula:
- Chamar `formulaEvaluatorService.validarFormula(formula, Set.of("total"))`
- Se inválida, lançar `ValidationException` com mensagem descritiva

#### 2.3 — Validação de formulaBase (BonusConfig)

**Arquivo a modificar**: `BonusConfiguracaoService.java`

Em `validarAntesCriar` e `validarAntesAtualizar`, se `formulaBase` não nula:
- Buscar siglas de atributos do jogo: `atributoRepository.findAbreviacoesByJogoId(jogoId)`
- Variáveis permitidas = siglas dos atributos + `{"nivel", "base"}`
- Chamar `formulaEvaluatorService.validarFormula(formula, variaveis)`
- Se inválida, lançar `ValidationException`

**Novo método de repositório**: `AtributoConfigRepository.findAbreviacoesByJogoId(Long jogoId)` → `List<String>`

#### 2.4 — Validação de formulaCusto (VantagemConfig)

**Arquivo a modificar**: `VantagemConfiguracaoService.java`

Variáveis fixas para custo de vantagem: `{"custoBase", "nivelVantagem"}`
Mesmo padrão de validação.

#### 2.5 — Endpoint de preview de fórmulas

**Arquivo a criar**: `controller/configuracao/FormulaController.java`
**Rotas**:
- `POST /api/jogos/{jogoId}/formulas/preview`
- `GET /api/jogos/{jogoId}/formulas/variaveis`

**DTOs a criar**:
- `FormulaPreviewRequest` (record): `formula`, `tipo` (IMPETO | BONUS | CUSTO_VANTAGEM), `valores` (Map<String, Double>)
- `FormulaPreviewResponse` (record): `resultado`, `variaveis`, `erros`
- `VariaveisDisponiveisResponse` (record): `atributos` (List<SiglaInfo>), `bonus` (List<SiglaInfo>), `vantagens` (List<SiglaInfo>), `fixas` (List<String>)

**Arquivo a criar**: `service/FormulaPreviewService.java` (lógica do preview, injeta FormulaEvaluatorService + repositórios)

### Checklist de verificação

- [ ] `formulaImpeto` inválida é rejeitada com variáveis listadas
- [ ] `formulaBase` com variável não registrada no jogo é rejeitada
- [ ] `formulaBase` válida com siglas de atributos reais do jogo é aceita
- [ ] `formulaCusto` aceita somente `custoBase` e `nivelVantagem`
- [ ] `POST /api/jogos/{id}/formulas/preview` retorna resultado numérico sem persistir
- [ ] `GET /api/jogos/{id}/formulas/variaveis` lista agrupada por tipo

---

## Phase 3 — CategoriaVantagem e PontosVantagemConfig

### O que implementar

#### 3.1 — Corrigir CategoriaVantagem

**Arquivo a modificar**: `model/CategoriaVantagem.java`

- Substituir `@Getter @Setter` por `@Data @EqualsAndHashCode(callSuper=true) @Builder @NoArgsConstructor @AllArgsConstructor`
- Adicionar `implements ConfiguracaoEntity`
- Renomear campo `ordem` → `ordemExibicao` (para consistência com todas as outras configs)
- Verificar se `getId()` e `getJogo()` estão acessíveis via `@Data`

**Arquivos a criar** (seguir padrão de `AtributoController`):
- `dto/request/configuracao/CreateCategoriaVantagemRequest.java` (record): `nome`, `descricao`, `cor`, `ordemExibicao`
- `dto/request/configuracao/UpdateCategoriaVantagemRequest.java` (record)
- `dto/response/configuracao/CategoriaVantagemResponse.java` (record): campos + `jogoId`, `dataCriacao`, `dataUltimaAtualizacao`
- `repository/CategoriaVantagemRepository.java` — `extends JpaRepository` + `existsByJogoIdAndNomeIgnoreCase` + `findByJogoIdOrderByOrdemExibicao`
- `mapper/configuracao/CategoriaVantagemMapper.java` (MapStruct)
- `service/configuracao/CategoriaVantagemService.java` (extends AbstractConfiguracaoService)
- `controller/configuracao/CategoriaVantagemController.java`
- `test/...CategoriaVantagemServiceIntegrationTest.java` (extends BaseConfiguracaoServiceIntegrationTest)

#### 3.2 — Corrigir PontosVantagemConfig

**Arquivo a modificar**: `model/PontosVantagemConfig.java`

- Substituir `@Getter @Setter` por `@Data @EqualsAndHashCode(callSuper=true) @Builder @NoArgsConstructor @AllArgsConstructor`
- **Nota**: PontosVantagemConfig tem unicidade por `(jogo_id, nivel)`, não por `nome` — o `AbstractConfiguracaoService` pode não se aplicar diretamente se exigir `getNome()`. Avaliar se deve estender `AbstractConfiguracaoService` ou ter service próprio. Provavelmente não implementa ConfiguracaoEntity pois não tem `nome`.

**Arquivos a criar**:
- `dto/request/configuracao/CreatePontosVantagemRequest.java` (record): `nivel`, `pontosGanhos`
- `dto/request/configuracao/UpdatePontosVantagemRequest.java` (record)
- `dto/response/configuracao/PontosVantagemResponse.java` (record)
- `repository/PontosVantagemConfigRepository.java` — `existsByJogoIdAndNivel`, `findByJogoIdOrderByNivel`
- `service/configuracao/PontosVantagemService.java` (service simples, não extend AbstractConfiguracaoService)
- `controller/configuracao/PontosVantagemController.java`
- Testes de integração

#### 3.3 — VantagemConfig → CategoriaVantagem FK

**Arquivo a modificar**: `model/VantagemConfig.java`
- Adicionar: `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="categoria_vantagem_id") CategoriaVantagem categoriaVantagem`
- DTOs de Vantagem: incluir `categoriaVantagemId` no Create/Update e `categoriaVantagemResponse` no Response

### Checklist de verificação

- [ ] CategoriaVantagem usa `@Data @Builder @EqualsAndHashCode(callSuper=true)`
- [ ] CategoriaVantagem implements ConfiguracaoEntity
- [ ] CRUD de CategoriaVantagem funciona com testes de integração
- [ ] PontosVantagemConfig com CRUD e testes
- [ ] VantagemConfig.categoriaVantagem aparece no Response (nullable)
- [ ] Duplicate name para CategoriaVantagem é rejeitado
- [ ] Duplicate nivel para PontosVantagemConfig é rejeitado

---

## Phase 4 — VantagemPreRequisito

### O que implementar

#### 4.1 — Entity VantagemPreRequisito

**Arquivo a criar**: `model/VantagemPreRequisito.java`

```java
// Estrutura sugerida
@Entity @Table(name = "vantagem_pre_requisitos")
public class VantagemPreRequisito extends BaseEntity {
    @ManyToOne(fetch=LAZY) @JoinColumn(name="vantagem_id")
    VantagemConfig vantagem;

    @ManyToOne(fetch=LAZY) @JoinColumn(name="vantagem_requisito_id")
    VantagemConfig requisito;

    Integer nivelMinimo; // nível mínimo que o pré-requisito deve estar
}
```

#### 4.2 — Lógica de pré-requisito no VantagemConfiguracaoService

- Ao criar relação A → B, verificar se B não já tem A como pré-requisito (ciclo direto)
- Para ciclos transitivos: verificar toda a cadeia de dependências de B antes de adicionar
- Lançar `ConflictException` se ciclo detectado

#### 4.3 — VantagemConfig CRUD atualizado

**Arquivos a modificar**:
- `CreateVantagemRequest` / `UpdateVantagemRequest`: adicionar `List<VantagemPreRequisitoRequest> preRequisitos`
- `VantagemResponse`: adicionar `List<VantagemPreRequisitoResponse> preRequisitos`
- `VantagemConfigMapper`: mapear a lista

### Checklist de verificação

- [ ] VantagemPreRequisito persistida corretamente
- [ ] Ciclo direto (A → B, B → A) é rejeitado
- [ ] Ciclo transitivo (A → B → C, C → A) é rejeitado
- [ ] nivelMinimo é respeitado e retornado no response
- [ ] GET VantagemConfig retorna lista de pré-requisitos

---

## Phase 5 — ClasseBonus e ClasseAptidaoBonus

### O que implementar

#### 5.1 — Entities

**Arquivo a criar**: `model/ClasseBonus.java`
```java
@Entity @Table(name = "classe_bonus")
public class ClasseBonus extends BaseEntity {
    @ManyToOne ClassePersonagem classe;
    @ManyToOne BonusConfig bonus;
    BigDecimal valorPorNivel; // ex: +1 B.B.A por nível
}
```

**Arquivo a criar**: `model/ClasseAptidaoBonus.java`
```java
@Entity @Table(name = "classe_aptidao_bonus")
public class ClasseAptidaoBonus extends BaseEntity {
    @ManyToOne ClassePersonagem classe;
    @ManyToOne AptidaoConfig aptidao;
    Integer bonus; // bônus fixo (ex: +2 em Furtividade)
}
```

#### 5.2 — ClassePersonagem CRUD atualizado

- Adicionar `List<ClasseBonus> bonusConfig` no modelo ClassePersonagem (cascade)
- Adicionar `List<ClasseAptidaoBonus> aptidaoBonus` no modelo
- DTOs: incluir listas nos Request/Response
- Service: gerenciar adição/remoção via PUT na classe (substituir lista completa)

### Checklist de verificação

- [ ] ClasseBonus persistida com valorPorNivel
- [ ] ClasseAptidaoBonus persistida com bonus
- [ ] GET ClassePersonagem retorna os dois tipos de bônus
- [ ] PUT ClassePersonagem substitui listas corretamente

---

## Phase 6 — RacaClassePermitida

### O que implementar

#### 6.1 — Entity

**Arquivo a criar**: `model/RacaClassePermitida.java`
```java
@Entity @Table(name = "raca_classes_permitidas",
               uniqueConstraints = @UniqueConstraint(columnNames = {"raca_id", "classe_id"}))
public class RacaClassePermitida extends BaseEntity {
    @ManyToOne Raca raca;
    @ManyToOne ClassePersonagem classe;
}
```

#### 6.2 — Raca CRUD atualizado

- Adicionar `List<RacaClassePermitida> classesPermitidas` em Raca
- DTOs: incluir no Response lista de `classeId + nome`
- Service: gerenciar via PUT (substituir lista)

### Checklist de verificação

- [ ] RacaClassePermitida persistida com unique constraint
- [ ] GET Raca retorna classesPermitidas com classeId e nome
- [ ] Tentativa de duplicar mesma classe na mesma raça é rejeitada

---

## Phase 7 — Testes e Validação Final

### O que verificar

```bash
# Rodar todos os testes
./mvnw test

# Verificar sigla cross-entity
grep -r "SiglaValidationService" src/main/
grep -r "validarSiglaDisponivel" src/main/

# Verificar validação de fórmulas
grep -r "validarFormula\|isValid" src/main/java/*/service/configuracao/

# Garantir que não há entities expostas nos controllers (só DTOs)
grep -r "AtributoConfig\|BonusConfig\|VantagemConfig" src/main/java/*/controller/

# Garantir padrão Lombok nas entidades novas
grep -A5 "@Entity" src/main/java/*/model/ClasseBonus.java
grep -A5 "@Entity" src/main/java/*/model/VantagemPreRequisito.java
```

### Checklist final

- [ ] `./mvnw test` passa 100%
- [ ] Sigla duplicada cross-entity é rejeitada (teste de integração)
- [ ] Fórmula com variável inexistente é rejeitada (teste de integração)
- [ ] CategoriaVantagem e PontosVantagemConfig têm CRUD funcionando
- [ ] VantagemPreRequisito detecta ciclos
- [ ] ClasseBonus e ClasseAptidaoBonus retornados no GET de ClassePersonagem
- [ ] RacaClassePermitida retornada no GET de Raca
- [ ] Nenhum controller expõe entities diretamente
- [ ] Todos os novos endpoints têm `@Operation` Swagger
- [ ] Todos os writes têm `@PreAuthorize("hasRole('MESTRE')")`
- [ ] Todos os reads têm `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

---

## Ordem de Execução Recomendada

```
Phase 1 (Siglas) → Phase 2 (Fórmulas) → Phase 3 (CategoriaVantagem + PontosVantagem)
       ↓
Phase 4 (VantagemPreRequisito)
       ↓
Phase 5 (ClasseBonus + ClasseAptidaoBonus)
       ↓
Phase 6 (RacaClassePermitida)
       ↓
Phase 7 (Testes + Validação Final)
```

**Phase 1 é pré-requisito para Phase 2** (validação de fórmulas usa as siglas registradas).
**Phase 3 é pré-requisito para Phase 4** (VantagemConfig precisa de CategoriaVantagem antes dos pré-requisitos).
**Phases 5 e 6 são independentes entre si** e podem ser feitas em paralelo.
