# P1-T5 — SiglaController e SiglaEmUsoResponse

## Objetivo
Criar o endpoint `GET /api/jogos/{jogoId}/siglas` que lista todas as siglas em uso no jogo.

## Depende de
P1-T3 (SiglaValidationService com método `listarSiglasDoJogo`)

## Steps

### 1. SiglaEmUsoResponse (se não criado em P1-T2)
```java
// dto/response/configuracao/SiglaEmUsoResponse.java
public record SiglaEmUsoResponse(
    String tipo,      // "ATRIBUTO" | "BONUS" | "VANTAGEM"
    String sigla,
    Long entityId,
    String nome       // nome amigável da configuração
) {}
```

### 2. SiglaController
```java
@RestController
@RequestMapping("/api/jogos/{jogoId}/siglas")
@RequiredArgsConstructor
@Tag(name = "Siglas", description = "Gestão de siglas e abreviações por jogo")
public class SiglaController {

    private final SiglaValidationService siglaValidationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Lista todas as siglas em uso no jogo",
               description = "Retorna siglas de atributos, bônus e vantagens, ordenadas alfabeticamente. " +
                             "Usado para verificar disponibilidade antes de criar configurações.")
    public ResponseEntity<List<SiglaEmUsoResponse>> listar(@PathVariable Long jogoId) {
        return ResponseEntity.ok(siglaValidationService.listarSiglasDoJogo(jogoId));
    }
}
```

**Sem lógica de negócio no controller** — apenas delega ao service.

## Acceptance Checks
- [ ] `GET /api/jogos/{id}/siglas` retorna 200 com lista vazia quando não há siglas
- [ ] Com atributos e bônus cadastrados, retorna todos com tipo correto
- [ ] Ordenado alfabeticamente por sigla
- [ ] Swagger mostra o endpoint documentado

## File Checklist
- `dto/response/configuracao/SiglaEmUsoResponse.java`
- `controller/configuracao/SiglaController.java`

## References
- `controller/configuracao/AtributoController.java` — padrão de controller thin
- `docs/backend/07-controllers.md`
- `docs/backend/08-security.md`
