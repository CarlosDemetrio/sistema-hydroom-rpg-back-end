# F3-T13 — Corrigir testes quebrados + novos testes de invariantes

## Objetivo
Corrigir T5-02 e T5-09 (quebrados desde R06) e adicionar 9 testes novos (T5-11 a T5-19).

## Arquivo modificado
`src/test/.../config/DefaultGameConfigProviderImplTest.java`

## Dependencias
Todas as tasks T4-T12 concluidas (64 vantagens preenchidas).

## Correcoes obrigatorias

### T5-02 (`deveRetornarOitoPontosVantagemDefaults`)
- Mudar `hasSize(8)` → `hasSize(35)`
- Manter/ajustar assertions de nivel 1 (6 pts), nivel 10 (10 pts), nivel 30 (15 pts)

### T5-09 (`deveRetornarOitoCategoriasVantagemDefaults`)
- Mudar `hasSize(8)` → `hasSize(9)`
- Adicionar `"Vantagem Racial"` ao `containsExactlyInAnyOrder`

## Novos testes

### T5-11 — getDefaultVantagens retorna 64
```java
@Test
@DisplayName("T5-11: getDefaultVantagens() retorna exatamente 64 vantagens")
void deveRetornarSesentaEQuatroVantagens() {
    var vantagens = provider.getDefaultVantagens();
    assertThat(vantagens).hasSize(64);
}
```

### T5-12 — siglas unicas, 2-5 chars e prefixo V
```java
@Test
@DisplayName("T5-12: siglas das vantagens sao unicas, tem 2-5 caracteres e comecam com V")
void siglasVantagensDevemSerUnicasE2a5CharsComPrefixoV() {
    var vantagens = provider.getDefaultVantagens();
    var siglas = vantagens.stream().map(VantagemConfigDTO::getSigla).toList();
    // todas nao-nulas
    assertThat(siglas).doesNotContainNull();
    // todas 2-5 chars
    siglas.forEach(s -> assertThat(s.length()).isBetween(2, 5));
    // todas comecam com V (RN-08)
    siglas.forEach(s -> assertThat(s).startsWith("V"));
    // todas unicas
    assertThat(siglas).doesNotHaveDuplicates();
}
```

### T5-13 — INSOLITUS com formulaCusto "0"
```java
@Test
@DisplayName("T5-13: todos os INSOLITUS tem formulaCusto = 0")
void insolitusDeverTerFormulaCustoZero() {
    var insolitus = provider.getDefaultVantagens().stream()
        .filter(v -> "INSOLITUS".equals(v.getTipoVantagem()))
        .toList();
    assertThat(insolitus).hasSize(17);
    insolitus.forEach(v -> assertThat(v.getFormulaCusto()).isEqualTo("0"));
}
```

### T5-14 — categoriaNome valida
```java
@Test
@DisplayName("T5-14: todas as categoriaNome existem em getDefaultCategoriasVantagem()")
void categoriaNomeDeveExistirNasCategorias() {
    var categoriasNomes = provider.getDefaultCategoriasVantagem().stream()
        .map(CategoriaVantagemDTO::getNome)
        .collect(Collectors.toSet());
    provider.getDefaultVantagens().forEach(v ->
        assertThat(categoriasNomes).contains(v.getCategoriaNome())
    );
}
```

### T5-15 — sem "custo_base"
```java
@Test
@DisplayName("T5-15: nenhuma formulaCusto usa custo_base")
void formulaCustoNaoDeveTerCustoBase() {
    provider.getDefaultVantagens().forEach(v ->
        assertThat(v.getFormulaCusto()).doesNotContain("custo_base")
    );
}
```

### T5-16 — 7 atributos com abreviacoes unicas
```java
@Test
@DisplayName("T5-16: 7 atributos com abreviacoes unicas")
void deveRetornarSeteAtributosComAbreviacoesUnicas() {
    var atributos = provider.getDefaultAtributos();
    assertThat(atributos).hasSize(7);
    var abreviacoes = atributos.stream().map(AtributoConfigDTO::getAbreviacao).toList();
    assertThat(abreviacoes).doesNotHaveDuplicates();
}
```

### T5-17 — 6 racas com nomes unicos
```java
@Test
@DisplayName("T5-17: 6 racas com nomes unicos")
void deveRetornarSeisRacasComNomesUnicos() {
    var racas = provider.getDefaultRacas();
    assertThat(racas).hasSize(6);
    var nomes = racas.stream().map(RacaConfigDTO::getNome).toList();
    assertThat(nomes).doesNotHaveDuplicates();
}
```

### T5-18 — 36 niveis com XP crescente
```java
@Test
@DisplayName("T5-18: 36 niveis (0-35) com XP crescente")
void deveRetornar36NiveisComXpCrescente() {
    var niveis = provider.getDefaultNiveis();
    assertThat(niveis).hasSize(36);
    for (int i = 1; i < niveis.size(); i++) {
        assertThat(niveis.get(i).getXpNecessaria())
            .isGreaterThanOrEqualTo(niveis.get(i - 1).getXpNecessaria());
    }
}
```

### T5-19 — 40 itens com raridade e tipo existentes
```java
@Test
@DisplayName("T5-19: 40 itens com raridade e tipo existentes")
void deveRetornar40ItensComRaridadeETipoValidos() {
    var itens = provider.getDefaultItens();
    assertThat(itens).hasSize(40);
    var raridadeNomes = provider.getDefaultRaridades().stream()
        .map(RaridadeItemConfigDefault::nome).collect(Collectors.toSet());
    var tipoNomes = provider.getDefaultTipos().stream()
        .map(TipoItemConfigDefault::nome).collect(Collectors.toSet());
    itens.forEach(item -> {
        assertThat(raridadeNomes).contains(item.raridadeNome());
        assertThat(tipoNomes).contains(item.tipoNome());
    });
}
```

## Validacao final
```bash
./mvnw test
# Deve passar com >= 743 testes
```

## Commit
```
test(defaults): corrige T5-02/T5-09 + testes T5-11..T5-19 [Copilot R07 T13]
```

## Acceptance Checks
- [ ] T5-02 passa com `hasSize(35)`
- [ ] T5-09 passa com `hasSize(9)` incluindo "Vantagem Racial"
- [ ] T5-11 a T5-19 todos verdes
- [ ] `./mvnw test` 100% verde (>= 743 testes)
