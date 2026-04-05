# T1 — Backend: PUT Anotacao + Campo pastaPaiId + Markdown

> Fase: P1 (Backend)
> Estimativa: 1 dia
> Depende de: T0 (Entity AnotacaoPasta — criar antes desta task)
> Bloqueia: T5 (Frontend: Edicao inline de Anotacao)

---

## Objetivo

Implementar o endpoint `PUT /api/v1/fichas/{fichaId}/anotacoes/{id}` para permitir edicao de titulo, conteudo e visibilidade. Simultaneamente, adaptar a entidade `FichaAnotacao` e os endpoints existentes para suportar:
- Campo `pastaPaiId` (FK para `AnotacaoPasta`, nullable) — onde a anotacao esta na hierarquia.
- Campo `visivelParaTodos` (BOOLEAN, default false) — compartilhamento com todos os participantes.
- Campo `conteudoMarkdown` — renomear conceitualmente o `conteudo` existente (conteudo continua sendo armazenado como TEXT; apenas o nome do campo e semantica mudam).

A infraestrutura base (entity, repository, service, controller, mapper, DTOs) ja existe. Esta task estende o modelo e adiciona a operacao de update.

---

## Regras de Negocio

| Regra | Detalhe |
|-------|---------|
| MESTRE pode editar qualquer anotacao | Nao importa quem criou |
| JOGADOR pode editar apenas anotacoes onde `autor.id == usuarioAtual.id` | Se tentar editar anotacao de outro: HTTP 403 |
| `tipoAnotacao` e imutavel | Nao aceitar mudanca de tipo no PUT; campo ignorado se enviado |
| `visivelParaJogador` so e alterado pelo MESTRE | Se JOGADOR enviar este campo, e ignorado silenciosamente |
| `visivelParaTodos` pode ser alterado pelo autor ou pelo MESTRE | JOGADOR pode marcar a propria anotacao como compartilhada; MESTRE pode alterar em qualquer anotacao |
| `pastaPaiId` pode ser alterado pelo autor ou pelo MESTRE | Permite mover anotacao entre pastas |
| Anotacao deve pertencer a ficha informada no path | Se `anotacao.fichaId != fichaId`: HTTP 403 |
| Ficha de NPC: apenas MESTRE pode editar anotacoes | JOGADOR recebe HTTP 403 |

---

## Mudancas no Modelo Existente

### 1. Adicionar campos em `FichaAnotacao.java`

Adicionar os seguintes campos a entidade existente:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "pasta_pai_id")
private AnotacaoPasta pastaPai;  // nullable — anotacao na raiz quando null

@Column(name = "visivel_para_todos", nullable = false)
@Builder.Default
private Boolean visivelParaTodos = false;
```

> O campo `conteudo` ja existe como TEXT — manter o nome no banco mas documentar que aceita Markdown.
> Nao renomear a coluna no banco no MVP para evitar migracoes complexas. O campo continua sendo `conteudo` internamente, mas o DTO pode expor como `conteudoMarkdown` para clareza.

### 2. Migracao DDL (se usando Flyway em producao)

```sql
ALTER TABLE ficha_anotacoes
    ADD COLUMN pasta_pai_id       BIGINT REFERENCES anotacao_pastas(id),
    ADD COLUMN visivel_para_todos BOOLEAN NOT NULL DEFAULT false;
```

> Em testes (H2 + ddl-auto=create-drop): as colunas serao criadas automaticamente pelo Hibernate ao subir o contexto.

---

## Arquivos Afetados

### Modificar
- `src/main/java/.../model/FichaAnotacao.java` — adicionar `pastaPai` e `visivelParaTodos`
- `src/main/java/.../service/FichaAnotacaoService.java` — adicionar metodo `atualizar()`; atualizar `listar()` para aceitar filtro por `pastaPaiId`
- `src/main/java/.../controller/FichaAnotacaoController.java` — adicionar handler `@PutMapping("/{id}")`; atualizar `@GetMapping` para aceitar `?pastaPaiId=X`
- `src/main/java/.../mapper/FichaAnotacaoMapper.java` — adicionar metodo de update; mapear `pastaPai.id`
- `src/main/java/.../dto/request/CriarAnotacaoRequest.java` — adicionar `pastaPaiId` e `visivelParaTodos`
- `src/main/java/.../dto/response/AnotacaoResponse.java` — adicionar `pastaPaiId` e `visivelParaTodos`
- `src/test/.../service/FichaAnotacaoServiceIntegrationTest.java` — adicionar cenarios de PUT e pasta

### Criar
- `src/main/java/.../dto/request/AtualizarAnotacaoRequest.java` — DTO para PUT

---

## Passos de Implementacao

### 1. Criar AtualizarAnotacaoRequest

```java
public record AtualizarAnotacaoRequest(
    @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
    String titulo,

    String conteudo,         // aceita Markdown; null = nao alterar

    Boolean visivelParaJogador,  // ignorado silenciosamente se enviado por JOGADOR

    Boolean visivelParaTodos,    // autor ou MESTRE podem alterar

    Long pastaPaiId              // id da pasta destino; null = mover para raiz
) {}
```

### 2. Atualizar FichaAnotacaoMapper

```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void atualizarEntidade(AtualizarAnotacaoRequest request, @MappingTarget FichaAnotacao anotacao);

// Nos metodos toResponse e toEntity existentes, adicionar mapeamento:
@Mapping(target = "pastaPaiId", source = "pastaPai.id")
@Mapping(target = "visivelParaTodos", source = "visivelParaTodos")
AnotacaoResponse toResponse(FichaAnotacao anotacao);
```

### 3. Adicionar FichaAnotacaoService.atualizar()

Logica:
1. Buscar anotacao por `anotacaoId` — se nao encontrar: `ResourceNotFoundException`
2. Verificar que `anotacao.ficha.id == fichaId` — se nao: `ForbiddenException`
3. Verificar papel do usuario atual
4. Se nao isMestre:
   - Se `ficha.isNpc`: `ForbiddenException`
   - Se `autor.id != usuarioAtual.id`: `ForbiddenException`
5. Aplicar campos via mapper (IGNORE nulls)
6. Se nao isMestre: ignorar `visivelParaJogador` mesmo que enviado
7. Se `pastaPaiId` nao e null: buscar `AnotacaoPasta` pelo id e validar que pertence a mesma ficha
8. Salvar e retornar

### 4. Atualizar listar() para aceitar filtro por pasta

```java
public List<FichaAnotacao> listar(Long fichaId, Long pastaPaiId, Long usuarioAtualId) {
    // ... logica de acesso existente ...
    // Se pastaPaiId fornecido: filtrar apenas anotacoes nessa pasta
    // Se pastaPaiId null: retornar todas (sem filtro de pasta)
}
```

### 5. Adicionar handler no Controller

```java
@PutMapping("/{id}")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Editar anotacao",
           description = "MESTRE pode editar qualquer anotacao. JOGADOR so pode editar as proprias.")
public ResponseEntity<AnotacaoResponse> atualizar(
        @PathVariable Long fichaId,
        @PathVariable Long id,
        @Valid @RequestBody AtualizarAnotacaoRequest request) {
    Long autorId = getUsuarioAtualId();
    var anotacao = fichaAnotacaoService.atualizar(fichaId, id, request, autorId);
    return ResponseEntity.ok(fichaAnotacaoMapper.toResponse(anotacao));
}
```

Atualizar handler GET para aceitar `pastaPaiId` como query param:

```java
@GetMapping
public ResponseEntity<List<AnotacaoResponse>> listar(
        @PathVariable Long fichaId,
        @RequestParam(required = false) Long pastaPaiId) {
    // ...
}
```

---

## Testes a Adicionar

| Cenario | Dado | Quando | Entao |
|---------|------|--------|-------|
| Jogador edita propria anotacao | Jogador autenticado, anotacao criada pelo mesmo jogador | PUT com novo titulo e conteudo Markdown | HTTP 200, campos atualizados |
| Mestre edita anotacao do Jogador | Mestre autenticado | PUT em anotacao do Jogador | HTTP 200, campos atualizados |
| Mestre altera visivelParaJogador | Mestre, anotacao com visivelParaJogador=false | PUT com visivelParaJogador=true | Campo atualizado |
| Jogador nao pode editar anotacao de outro | Jogador A, anotacao de Jogador B | PUT | ForbiddenException |
| Jogador nao altera visivelParaJogador | Jogador, anotacao propria, envia visivelParaJogador=true | PUT | Campo ignorado, permanece false |
| Anotacao de NPC: JOGADOR recebe 403 | Jogador, ficha NPC | PUT | ForbiddenException |
| Campo null nao sobrescreve | Anotacao com titulo "X", request com titulo=null | PUT | Titulo permanece "X" |
| Jogador marca propria anotacao como visivelParaTodos | Jogador, anotacao propria | PUT com visivelParaTodos=true | Campo atualizado |
| Mover anotacao para pasta valida | Anotacao na raiz, pasta existente na mesma ficha | PUT com pastaPaiId valido | Anotacao aparece na pasta |
| Mover anotacao para pasta de outra ficha | Pasta pertence a outra ficha | PUT com pastaPaiId invalido | ForbiddenException |
| Listar anotacoes filtrado por pastaPaiId | Ficha com anotacoes em 2 pastas | GET com ?pastaPaiId=X | Apenas anotacoes da pasta X retornadas |

---

## Criterios de Aceite

- [ ] `PUT /fichas/{fichaId}/anotacoes/{id}` responde HTTP 200 com anotacao atualizada
- [ ] Campos null no request nao alteram o valor existente
- [ ] JOGADOR nao pode editar anotacao de outro usuario (HTTP 403)
- [ ] JOGADOR nao pode alterar `visivelParaJogador` (ignorado silenciosamente)
- [ ] MESTRE pode editar qualquer anotacao e alterar `visivelParaJogador`
- [ ] Jogador pode alterar `visivelParaTodos` na propria anotacao
- [ ] Anotacao em ficha de NPC: JOGADOR recebe HTTP 403
- [ ] `tipoAnotacao` nao pode ser alterado pelo PUT (campo ignorado se enviado)
- [ ] `AnotacaoResponse` inclui `pastaPaiId` e `visivelParaTodos`
- [ ] `CriarAnotacaoRequest` aceita `pastaPaiId` ao criar nova anotacao
- [ ] GET lista aceita `?pastaPaiId=X` como filtro opcional
- [ ] Todos os testes de integracao passam
