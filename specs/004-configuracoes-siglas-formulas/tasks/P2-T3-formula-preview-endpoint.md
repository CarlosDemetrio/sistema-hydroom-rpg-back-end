# P2-T3 — Endpoints de preview e variáveis disponíveis

## Objetivo
Criar `FormulaPreviewService` e `FormulaController` com dois endpoints: preview de fórmula e listagem de variáveis disponíveis.

## Depende de
P2-T1 (FormulaEvaluatorService.validarFormula)

## Endpoints

```
POST /api/jogos/{jogoId}/formulas/preview   — testa fórmula com valores, retorna resultado
GET  /api/jogos/{jogoId}/formulas/variaveis — lista variáveis disponíveis por tipo
```

## Steps

### 1. DTOs

```java
// FormulaPreviewRequest.java
public record FormulaPreviewRequest(
    @NotBlank String formula,
    @NotNull TipoFormula tipo,  // enum: IMPETO, BONUS, CUSTO_VANTAGEM
    Map<String, Double> valores // valores de teste (pode ser vazio — usa defaults)
) {}

// FormulaPreviewResponse.java
public record FormulaPreviewResponse(
    boolean valida,
    Double resultado,          // null se inválida
    List<String> erros,        // mensagens de erro
    Set<String> variaveisUsadas
) {}

// SiglaInfoResponse.java (para agrupar no response de variáveis)
public record SiglaInfoResponse(String sigla, String nome) {}

// VariaveisDisponiveisResponse.java
public record VariaveisDisponiveisResponse(
    List<SiglaInfoResponse> atributos,   // siglas de atributos do jogo
    List<SiglaInfoResponse> bonus,       // siglas de bônus do jogo
    List<SiglaInfoResponse> vantagens,   // siglas de vantagens do jogo (não nulas)
    List<String> fixas                   // "total", "nivel", "base", "custoBase", "nivelVantagem"
) {}

// TipoFormula.java (enum)
public enum TipoFormula { IMPETO, BONUS, CUSTO_VANTAGEM }
```

### 2. FormulaPreviewService

```java
@Service
@RequiredArgsConstructor
public class FormulaPreviewService {

    private final FormulaEvaluatorService formulaEvaluator;
    private final ConfiguracaoAtributoRepository atributoRepo;
    private final BonusConfigRepository bonusRepo;
    private final VantagemConfigRepository vantagemRepo;

    public FormulaPreviewResponse preview(Long jogoId, FormulaPreviewRequest req) {
        Set<String> permitidas = resolverVariaveisPermitidas(jogoId, req.tipo());

        // Validar fórmula
        ValidationResult validation = formulaEvaluator.validarFormula(req.formula(), permitidas);
        if (!validation.valid()) {
            List<String> erros = new ArrayList<>();
            if (validation.erroSintaxe() != null) erros.add(validation.erroSintaxe());
            validation.variaveisInvalidas().forEach(v ->
                erros.add("Variável não reconhecida: " + v));
            return new FormulaPreviewResponse(false, null, erros, Set.of());
        }

        // Calcular com valores fornecidos ou defaults (0.0 para variáveis sem valor)
        Map<String, Double> valores = new HashMap<>();
        permitidas.forEach(v -> valores.put(v, 0.0));
        if (req.valores() != null) valores.putAll(req.valores());

        try {
            double resultado = formulaEvaluator.evaluate(req.formula(), valores);
            return new FormulaPreviewResponse(true, resultado, List.of(), permitidas);
        } catch (Exception e) {
            return new FormulaPreviewResponse(false, null, List.of("Erro ao calcular: " + e.getMessage()), Set.of());
        }
    }

    public VariaveisDisponiveisResponse listarVariaveis(Long jogoId) {
        List<SiglaInfoResponse> atributos = atributoRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        List<SiglaInfoResponse> bonus = bonusRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        List<SiglaInfoResponse> vantagens = vantagemRepo.findSiglasComInfoByJogoId(jogoId).stream()
            .map(s -> new SiglaInfoResponse(s.sigla(), s.nome()))
            .toList();
        List<String> fixas = List.of("total", "nivel", "base", "custoBase", "nivelVantagem");
        return new VariaveisDisponiveisResponse(atributos, bonus, vantagens, fixas);
    }

    private Set<String> resolverVariaveisPermitidas(Long jogoId, TipoFormula tipo) {
        return switch (tipo) {
            case IMPETO -> Set.of("total");
            case BONUS -> {
                List<String> siglas = atributoRepo.findAbreviacoesByJogoId(jogoId);
                Set<String> vars = new HashSet<>(siglas);
                vars.addAll(Set.of("nivel", "base"));
                yield vars;
            }
            case CUSTO_VANTAGEM -> Set.of("custoBase", "nivelVantagem");
        };
    }
}
```

### 3. FormulaController

```java
@RestController
@RequestMapping("/api/jogos/{jogoId}/formulas")
@RequiredArgsConstructor
@Tag(name = "Fórmulas", description = "Preview e variáveis disponíveis para fórmulas do jogo")
public class FormulaController {

    private final FormulaPreviewService formulaPreviewService;

    @PostMapping("/preview")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Testa uma fórmula com valores de exemplo",
               description = "Valida a fórmula e calcula o resultado sem persistir nada.")
    public ResponseEntity<FormulaPreviewResponse> preview(
            @PathVariable Long jogoId,
            @RequestBody @Valid FormulaPreviewRequest request) {
        return ResponseEntity.ok(formulaPreviewService.preview(jogoId, request));
    }

    @GetMapping("/variaveis")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Lista variáveis disponíveis para fórmulas",
               description = "Retorna siglas agrupadas por tipo (atributos, bônus, vantagens) e variáveis fixas.")
    public ResponseEntity<VariaveisDisponiveisResponse> variaveis(@PathVariable Long jogoId) {
        return ResponseEntity.ok(formulaPreviewService.listarVariaveis(jogoId));
    }
}
```

## Acceptance Checks
- [ ] `POST /formulas/preview` com fórmula válida + valores retorna resultado numérico
- [ ] `POST /formulas/preview` com fórmula inválida retorna `valida=false` + erros
- [ ] `POST /formulas/preview` sem `valores` usa 0.0 como default (não lança NPE)
- [ ] `GET /formulas/variaveis` retorna atributos do jogo no campo `atributos`
- [ ] Swagger documenta ambos os endpoints

## File Checklist
- `dto/request/configuracao/FormulaPreviewRequest.java`
- `dto/response/configuracao/FormulaPreviewResponse.java`
- `dto/response/configuracao/SiglaInfoResponse.java`
- `dto/response/configuracao/VariaveisDisponiveisResponse.java`
- `dto/enums/TipoFormula.java`
- `service/FormulaPreviewService.java`
- `controller/configuracao/FormulaController.java`

## References
- `controller/configuracao/AtributoController.java` — padrão
- `service/FormulaEvaluatorService.java`
- `docs/backend/07-controllers.md`
