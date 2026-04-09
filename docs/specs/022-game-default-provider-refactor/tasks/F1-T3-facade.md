# F1-T3 — Refatorar DefaultGameConfigProviderImpl como Facade

## Objetivo
Transformar o monolito em thin facade que delega para os 11 providers.

## Arquivo modificado
`config/DefaultGameConfigProviderImpl.java`

## O que fazer

1. Adicionar `@RequiredArgsConstructor` (Lombok)
2. Adicionar 11 campos `private final` para os providers
3. Substituir corpo de cada `getDefault*()` por delegacao ao provider
4. Manter `getDefaultClassePontos()` e `getDefaultRacaPontos()` como stubs `Map.of()`
5. Remover todos os dados inline e imports nao usados

## Resultado esperado

```java
@Component
@RequiredArgsConstructor
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

    @Override
    public List<AtributoConfigDTO> getDefaultAtributos() {
        return atributosProvider.get();
    }

    @Override
    public List<AptidaoConfigDTO> getDefaultAptidoes() {
        return aptidoesProvider.get();
    }

    @Override
    public List<NivelConfigDTO> getDefaultNiveis() {
        return niveisProvider.getNiveis();
    }

    @Override
    public List<LimitadorConfigDTO> getDefaultLimitadores() {
        return niveisProvider.getLimitadores();
    }

    @Override
    public List<ClasseConfigDTO> getDefaultClasses() {
        return classesProvider.get();
    }

    @Override
    public List<RacaConfigDTO> getDefaultRacas() {
        return racasProvider.getRacas();
    }

    @Override
    public Map<String, List<BonusAtributoDTO>> getDefaultBonusRaciais() {
        return racasProvider.getBonusRaciais();
    }

    @Override
    public List<ProspeccaoConfigDTO> getDefaultProspeccoes() {
        return prospeccoesProvider.get();
    }

    @Override
    public List<GeneroConfigDTO> getDefaultGeneros() {
        return simpleProvider.getGeneros();
    }

    @Override
    public List<IndoleConfigDTO> getDefaultIndoles() {
        return simpleProvider.getIndoles();
    }

    @Override
    public List<PresencaConfigDTO> getDefaultPresencas() {
        return simpleProvider.getPresencas();
    }

    @Override
    public List<MembroCorpoConfigDTO> getDefaultMembrosCorpo() {
        return simpleProvider.getMembrosCorpo();
    }

    @Override
    public List<VantagemConfigDTO> getDefaultVantagens() {
        return vantagensProvider.getVantagens();
    }

    @Override
    public List<BonusConfigDTO> getDefaultBonus() {
        return bonusProvider.get();
    }

    @Override
    public List<PontosVantagemConfigDTO> getDefaultPontosVantagem() {
        return pontosVantagemProvider.get();
    }

    @Override
    public List<CategoriaVantagemDTO> getDefaultCategoriasVantagem() {
        return vantagensProvider.getCategorias();
    }

    @Override
    public Map<String, List<?>> getDefaultClassePontos() {
        return Map.of();
    }

    @Override
    public Map<String, List<?>> getDefaultRacaPontos() {
        return Map.of();
    }

    @Override
    public List<RaridadeItemConfigDefault> getDefaultRaridades() {
        return itensProvider.getRaridades();
    }

    @Override
    public List<TipoItemConfigDefault> getDefaultTipos() {
        return itensProvider.getTipos();
    }

    @Override
    public List<ItemConfigDefault> getDefaultItens() {
        return itensProvider.getItens();
    }
}
```

## Validacao
```bash
./mvnw test -Dtest=DefaultGameConfigProviderImplTest
```
Testes T5-01, T5-03..T5-08, T5-10 devem passar. T5-02 e T5-09 continuam quebrados (corrigidos na T13).

## Commit
```
refactor(defaults): DefaultGameConfigProviderImpl como thin facade [Copilot R07 T3]
```

## Acceptance Checks
- [ ] Classe <= 100 linhas
- [ ] 11 providers injetados via `private final`
- [ ] Todos os `getDefault*()` delegam
- [ ] Stubs `getDefaultClassePontos()` e `getDefaultRacaPontos()` retornam `Map.of()`
- [ ] Testes T5-01, T5-03..T5-08, T5-10 passam
