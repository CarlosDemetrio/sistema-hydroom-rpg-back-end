# F1-T2 — DefaultVantagensProvider Scaffold

## Objetivo
Criar `DefaultVantagensProvider` com categorias completas, 9 metodos build vazios e helper `vantagem()`.

## Arquivo novo
`config/defaults/DefaultVantagensProvider.java`

## Estrutura

```java
@Component
public class DefaultVantagensProvider {

    public List<CategoriaVantagemDTO> getCategorias() {
        return List.of(
            // 9 categorias copiadas de getDefaultCategoriasVantagem()
            // INCLUINDO "Vantagem Racial" (9a categoria adicionada em R06)
        );
    }

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

    // === Helper para reduzir boilerplate ===
    private VantagemConfigDTO vantagem(String sigla, String nome, String descricao,
                                       int nivelMax, String formulaCusto,
                                       String efeito, String tipo,
                                       String categoria, int ordem) {
        return VantagemConfigDTO.builder()
                .sigla(sigla)
                .nome(nome)
                .descricao(descricao)
                .nivelMaximoVantagem(nivelMax)
                .formulaCusto(formulaCusto)
                .valorBonusFormula(efeito)
                .tipoVantagem(tipo)
                .categoriaNome(categoria)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(nivelMax > 1)
                .ordemExibicao(ordem)
                .build();
    }

    // === Builds vazios (preenchidos em T4-T12) ===
    private List<VantagemConfigDTO> buildTreinamentoFisico()  { return List.of(); }
    private List<VantagemConfigDTO> buildTreinamentoMental()  { return List.of(); }
    private List<VantagemConfigDTO> buildAcao()               { return List.of(); }
    private List<VantagemConfigDTO> buildReacao()             { return List.of(); }
    private List<VantagemConfigDTO> buildAtributo()           { return List.of(); }
    private List<VantagemConfigDTO> buildGeral()              { return List.of(); }
    private List<VantagemConfigDTO> buildHistorica()          { return List.of(); }
    private List<VantagemConfigDTO> buildRenascimento()       { return List.of(); }
    private List<VantagemConfigDTO> buildRaciais()            { return List.of(); }
}
```

## Validacao
```bash
./mvnw compile
```

## Commit
```
refactor(defaults): scaffold DefaultVantagensProvider com 9 builds vazios [Copilot R07 T2]
```

## Acceptance Checks
- [ ] Arquivo criado com `@Component`
- [ ] `getCategorias()` retorna 9 categorias (incluindo "Vantagem Racial")
- [ ] `getVantagens()` retorna lista vazia (builds vazios temporariamente)
- [ ] Helper `vantagem()` compila
- [ ] `./mvnw compile` passa
