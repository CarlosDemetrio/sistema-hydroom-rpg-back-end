# P0-T1 — JogoConfigValidationService (Pré-requisito para criar Ficha)

**Fase:** 0 — Validação de Pré-requisitos
**Complexidade:** 🟡 Média
**Depende de:** Specs 004 e 005 concluídos
**Bloqueia:** P1-T2 (FichaService precisa chamar este service ao criar Ficha)

## Objetivo

Validar que um Jogo está **completamente configurado** antes de permitir a criação de Fichas. A pré-configuração padrão (via `GameConfigInitializerService`) garante que jogos novos já passem nesta validação — mas Mestres que customizaram configs ou removeram items precisam ser bloqueados se deixaram o jogo em estado inválido.

## Regras de Validação

### 🔴 Bloqueantes (lançam `ConfiguracaoInsuficienteException` — 422 Unprocessable Entity)

| Regra | Motivo |
|-------|--------|
| Jogo deve ter pelo menos 1 `NivelConfig` (nivel >= 1) | Sem nível, não há `pontosAtributo`, `limitadorAtributo` nem `pontosAptidao` para validar distribuição de pontos |
| Jogo deve ter pelo menos 1 `MembroCorpoConfig` | Sem membros do corpo, `FichaVidaMembro` não pode ser inicializado |
| `MembroCorpoConfig`: soma das `porcentagemVida` deve ser 1.00 (±0.001 de tolerância) | Distribuição de vida por membro inconsistente viola integridade dos dados |
| Jogo deve ter pelo menos 1 `AtributoConfig` | Sem atributos, Ficha não tem base mecânica |

### 🟡 Avisos (log WARN, não bloqueiam)

| Situação | Aviso |
|----------|-------|
| `BonusConfig` com `formulaBase` referenciando sigla não cadastrada como `AtributoConfig.abreviacao` | Bônus com fórmula inválida será calculado como 0 até ser corrigido |
| Nenhum `AtributoConfig` com `abreviacao = "VIG"` | Cálculo de vidaTotal e essência assume VIG=0; resultado pode ser 0 |
| Nenhum `AtributoConfig` com `abreviacao = "SAB"` | Cálculo de essência assume SAB=0; resultado pode ser incorreto |
| `VantagemConfig` com `formulaCusto` null ou blank | Vantagem sem custo calculável — custo será retornado como 0 |

## Checklist

### 1. Exceção customizada

- [ ] `exception/ConfiguracaoInsuficienteException.java`:
  ```java
  public class ConfiguracaoInsuficienteException extends RuntimeException {
      private final List<String> problemas; // lista dos problemas encontrados
      public ConfiguracaoInsuficienteException(List<String> problemas) {
          super("Jogo não está pronto para criar fichas: " + String.join("; ", problemas));
          this.problemas = problemas;
      }
  }
  ```
- [ ] Registrar no `GlobalExceptionHandler` → HTTP 422 com lista de problemas no response body

### 2. JogoConfigValidationService

- [ ] `@Service JogoConfigValidationService` com injeção de:
  - `NivelConfigRepository`
  - `MembroCorpoConfigRepository`
  - `AtributoConfigRepository`
  - `BonusConfigRepository`
  - `VantagemConfigRepository` (para avisos)

- [ ] Método principal: `validarProntoParaFicha(Long jogoId)`:
  - Coleta TODOS os problemas antes de lançar (fail-all, não fail-fast)
  - Lança `ConfiguracaoInsuficienteException` com a lista de problemas se houver qualquer bloqueante

- [ ] Método: `validarNivelConfig(Long jogoId, List<String> problemas)`:
  ```java
  if (!nivelConfigRepository.existsByJogoIdAndNivelGreaterThanEqual(jogoId, 1)) {
      problemas.add("Nenhum NivelConfig cadastrado para nivel >= 1");
  }
  ```

- [ ] Método: `validarMembrosCorpo(Long jogoId, List<String> problemas)`:
  ```java
  List<MembroCorpoConfig> membros = repository.findByJogoId(jogoId);
  if (membros.isEmpty()) {
      problemas.add("Nenhum MembroCorpoConfig cadastrado");
      return; // sem dados para verificar soma
  }
  BigDecimal soma = membros.stream()
      .map(MembroCorpoConfig::getPorcentagemVida)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  if (soma.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.001")) > 0) {
      problemas.add(String.format(
          "MembroCorpoConfig: soma das porcentagens = %s (esperado: 1.00)", soma
      ));
  }
  ```

- [ ] Método: `validarAtributos(Long jogoId, List<String> problemas)`:
  - Verifica que existe pelo menos 1 AtributoConfig
  - WARN se não existe abreviacao "VIG"
  - WARN se não existe abreviacao "SAB"

- [ ] Método: `validarBonusFormulas(Long jogoId)` (apenas warnings, sem adicionar à lista de problemas):
  - Carrega set de abreviacoes dos AtributoConfig do jogo
  - Para cada BonusConfig com formulaBase: verificar se todas as variáveis da fórmula (regex `[A-Z]+`) existem no set

### 3. Response de Erro

- [ ] `ConfiguracaoInsuficienteResponse` record: `String mensagem, List<String> problemas`
- [ ] GlobalExceptionHandler lança HTTP 422 com este response

### 4. Endpoint de verificação (opcional mas útil)

- [ ] `GET /api/jogos/{id}/config/validar-pronto` → retorna `ConfiguracaoStatusResponse`:
  - `boolean pronto`: true se sem bloqueantes
  - `List<String> problemas`: lista de bloqueantes
  - `List<String> avisos`: lista de warnings
- [ ] `@PreAuthorize("hasRole('MESTRE')")`

## Arquivos afetados

- `exception/ConfiguracaoInsuficienteException.java` (NOVO)
- `service/JogoConfigValidationService.java` (NOVO)
- `dto/response/ConfiguracaoInsuficienteResponse.java` (NOVO)
- `dto/response/ConfiguracaoStatusResponse.java` (NOVO)
- `config/GlobalExceptionHandler.java` (MODIFICAR — registrar novo handler)
- `controller/JogoController.java` ou novo `ConfigValidacaoController.java` (MODIFICAR/NOVO — endpoint /validar-pronto)

## Testes Unitários — JogoConfigValidationServiceTest

```java
// @ExtendWith(MockitoExtension.class)

@Test
void devePassarValidacaoComConfigsCompletas() {
    // Mock: NivelConfig nivel=1 existe, 6 membros com soma=1.00, AtributoConfig com VIG e SAB
    // Resultado: validarProntoParaFicha não lança exceção
}

@Test
void deveLancarExcecaoSemNivelConfig() {
    // Mock: sem NivelConfig
    // Resultado: ConfiguracaoInsuficienteException com "Nenhum NivelConfig"
}

@Test
void deveLancarExcecaoSemMembrosCorpo() {
    // Mock: NivelConfig existe, sem MembroCorpoConfig
    // Resultado: exceção com "Nenhum MembroCorpoConfig"
}

@Test
void deveLancarExcecaoComSomaMembrosErrada() {
    // Mock: 3 membros com porcentagens 0.30 + 0.30 + 0.30 = 0.90 (faltam 10%)
    // Resultado: exceção com "soma das porcentagens = 0.90"
}

@Test
void deveColetarMultiplosProblemas() {
    // Mock: sem NivelConfig E sem MembroCorpoConfig
    // Resultado: exceção com lista de 2 problemas
}

@Test
void deveAceitarToleranciaDe001() {
    // Mock: soma = 0.9999 (ligeira imprecisão de ponto flutuante)
    // Resultado: não lança exceção
}

@Test
void deveLancarExcecaoSemAtributos() {
    // Mock: NivelConfig existe, membros com soma correta, mas nenhum AtributoConfig
    // Resultado: exceção com "Nenhum AtributoConfig"
}
```

## Testes de Integração — JogoConfigValidationServiceIntegrationTest

```java
// @ActiveProfiles("test"), @Transactional

@Test
void devePassarComJogoConfiguradoPorGameConfigInitializerService() {
    // Criar jogo via API → verifica que InitializerService popula configs corretas
    // Chamar validarProntoParaFicha → não lança exceção
}

@Test
void deveFalharComJogoSemNivelConfig() {
    // Criar jogo, deletar todos os NivelConfig
    // Chamar validarProntoParaFicha → ConfiguracaoInsuficienteException
}

@Test
void deveFalharComSomaMembrosIncorreta() {
    // Criar jogo, adicionar membros com soma != 1.00 (criar manualmente ignorando a config default)
    // Chamar validarProntoParaFicha → exceção com mensagem de soma
}

@Test
void deveRetornarEndpointValidarPronto() {
    // GET /api/jogos/{id}/config/validar-pronto
    // → 200 com pronto=true para jogo bem configurado
}

@Test
void deveRetornar422AoCriarFichaEmJogoInvalido() {
    // Criar jogo, remover todos os NivelConfig
    // POST /api/jogos/{id}/fichas → 422 com lista de problemas
}
```

## Verificações de aceitação

- [ ] Jogo recém-criado (com defaults) passa na validação sem erros
- [ ] Jogo sem NivelConfig → 422 ao criar Ficha com mensagem clara
- [ ] Jogo com membros corpo somando 0.90 → 422 com mensagem mostrando a soma
- [ ] Jogo sem AtributoConfig → 422 com mensagem clara
- [ ] Múltiplos problemas → 422 com lista de TODOS os problemas (não apenas o primeiro)
- [ ] `./mvnw test` passa
