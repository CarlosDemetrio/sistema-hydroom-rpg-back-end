# T2 — Enriquecer OpenAPI Annotations em Todos os Controllers

> Fase: Backend | Dependencias: Nenhuma | Bloqueia: T6 (swagger.json export)
> Estimativa: 4–5 horas

---

## Objetivo

Enriquecer as annotations OpenAPI/Swagger em todos os controllers REST para que o Swagger UI seja uma documentacao de API completa e utilizavel pelo frontend team.

---

## Situacao Atual

Os controllers usam `@Operation` basico ou nenhum. O Swagger UI (`/swagger-ui.html`) mostra endpoints mas sem:
- Descricoes uteis dos endpoints
- Documentacao dos parametros de path/query
- Documentacao dos response codes (400, 404, 409, 403)
- Exemplos nos schemas de request/response

---

## Controllers Afetados

| Controller | Endpoints estimados | Prioridade |
|-----------|-------------------|-----------|
| `FichaController` | ~15 (CRUD, wizard, XP, status, duplicar) | **P0** |
| `VantagemEfeitoController` | ~5 (CRUD sub-recurso) | **P0** |
| `JogoController` | ~5 (CRUD jogo) | **P1** |
| `JogoParticipanteController` | ~8 (solicitar, aprovar, banir, etc.) | **P1** |
| 13 controllers de configuracao | ~5 cada (~65 total) | **P2** |

---

## Padrao de Annotations

### Exemplo completo — endpoint de criacao:
```java
@Operation(
    summary = "Criar nova configuracao de atributo",
    description = "Cria uma configuracao de atributo para o jogo. "
        + "A abreviacao deve ser unica no escopo do jogo (cross-entity). "
        + "Requer role MESTRE."
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Configuracao criada com sucesso"),
    @ApiResponse(responseCode = "400", description = "Dados invalidos (campo obrigatorio ausente ou formato incorreto)"),
    @ApiResponse(responseCode = "404", description = "Jogo nao encontrado"),
    @ApiResponse(responseCode = "409", description = "Nome ou abreviacao ja existe neste jogo")
})
@PostMapping
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<AtributoConfigResponse> criar(
    @Parameter(description = "ID do jogo", example = "1")
    @PathVariable Long jogoId,
    @Valid @RequestBody CreateAtributoConfigRequest request) {
```

### Padrao para DTOs (request):
```java
@Schema(description = "Dados para criacao de configuracao de atributo")
public record CreateAtributoConfigRequest(
    @Schema(description = "Nome do atributo", example = "Forca", maxLength = 100)
    @NotBlank String nome,

    @Schema(description = "Abreviacao unica (2-5 chars)", example = "FOR", minLength = 2, maxLength = 5)
    @NotBlank @Size(min = 2, max = 5) String abreviacao
) {}
```

---

## Passos de Implementacao

1. **P0 — FichaController e VantagemEfeitoController** (2h)
   - Documentar todos os endpoints com `@Operation`, `@ApiResponse`, `@Parameter`
   - Incluir descricoes de permissao (MESTRE vs JOGADOR)
   - Documentar campos condicionais (NPC vs jogador, status da ficha)

2. **P1 — JogoController e JogoParticipanteController** (1h)
   - Documentar fluxo de participantes (solicitar → aprovar/rejeitar → banir/desbanir)
   - Documentar filtros de query (status, role)

3. **P2 — 13 controllers de configuracao** (2h)
   - Padrao repetitivo: aplicar template `@Operation` para CRUD generico
   - Documentar campo `ordemExibicao` e endpoint de reordenacao

4. **DTOs de request/response** (1h)
   - Adicionar `@Schema` com `description`, `example`, constraints

---

## Criterios de Aceitacao

- [ ] Todo endpoint no Swagger UI mostra summary e description preenchidos
- [ ] Todo endpoint documenta pelo menos os response codes 200/201, 400, 404 (e 409 onde aplicavel)
- [ ] Path variables com `@Parameter(description, example)`
- [ ] DTOs de request com `@Schema(description, example)` em todos os campos
- [ ] `./mvnw test` continua passando (annotations nao quebram compilacao)
- [ ] Swagger UI renderiza sem erros ao acessar `/swagger-ui.html`
