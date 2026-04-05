# T3 — ProspeccaoUso Entity + Endpoints de Semantica

> Tipo: Backend
> Dependencias: nenhuma (paralelo com T1/T2)
> Desbloqueia: T6, T9

---

## Objetivo

Implementar a entidade `ProspeccaoUso` e os endpoints que respeitam a semantica de negocio da prospecção: Jogador usa (decrementa + cria registro PENDENTE), Mestre confirma ou reverte, Mestre concede. O endpoint `PUT /fichas/{id}/prospeccao` legado permanece funcionando.

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `model/ProspeccaoUso.java` | Criar |
| `model/enums/ProspeccaoUsoStatus.java` | Criar |
| `repository/ProspeccaoUsoRepository.java` | Criar |
| `service/ProspeccaoService.java` | Criar |
| `dto/request/ConcederProspeccaoRequest.java` | Criar |
| `dto/request/UsarProspeccaoRequest.java` | Criar |
| `dto/response/ProspeccaoUsoResponse.java` | Criar |
| `mapper/ProspeccaoUsoMapper.java` | Criar |
| `controller/FichaController.java` | Adicionar 6 endpoints de prospeccao |
| `repository/FichaProspeccaoRepository.java` | Adicionar metodo de busca com lock otimista |

---

## Passos

### Passo 1 — model/enums/ProspeccaoUsoStatus.java

```java
public enum ProspeccaoUsoStatus {
    PENDENTE, CONFIRMADO, REVERTIDO
}
```

### Passo 2 — model/ProspeccaoUso.java

```java
@Entity
@Table(name = "prospeccao_usos",
    indexes = {
        @Index(name = "idx_pros_uso_ficha_pros", columnList = "ficha_prospeccao_id"),
        @Index(name = "idx_pros_uso_status", columnList = "status")
    })
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
public class ProspeccaoUso extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_prospeccao_id", nullable = false)
    private FichaProspeccao fichaProspeccao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProspeccaoUsoStatus status = ProspeccaoUsoStatus.PENDENTE;
}
```

> ProspeccaoUso NAO estende BaseEntity com soft delete no sentido de remocao — o historico deve ser preservado. O campo `deleted_at` da BaseEntity fica sempre nulo.

### Passo 3 — ProspeccaoUsoRepository.java

```java
public interface ProspeccaoUsoRepository extends JpaRepository<ProspeccaoUso, Long> {
    List<ProspeccaoUso> findByFichaProspeccaoFichaIdAndStatus(Long fichaId, ProspeccaoUsoStatus status);
    List<ProspeccaoUso> findByFichaProspeccaoFichaId(Long fichaId);
    // Para painel global do Mestre:
    List<ProspeccaoUso> findByFichaProspeccaoFichaJogoIdAndStatus(Long jogoId, ProspeccaoUsoStatus status);
}
```

### Passo 4 — ProspeccaoService.java

**Metodo usar():**
```
1. Buscar FichaProspeccao por (fichaId, dadoProspeccaoConfigId)
2. Verificar permissao: Jogador so usa propria ficha; Mestre usa qualquer ficha
3. Verificar quantidade > 0 — se zero, lancar ValidationException (HTTP 422)
4. Decrementar quantidade (usar @Version ou SELECT ... FOR UPDATE para evitar concorrencia)
5. Criar ProspeccaoUso com status=PENDENTE
6. Salvar e retornar ProspeccaoUsoResponse
```

**Metodo reverter():**
```
1. Buscar ProspeccaoUso por id
2. Verificar que pertence a uma ficha do jogo do Mestre (seguranca)
3. Verificar que status == PENDENTE — se CONFIRMADO ou REVERTIDO, lancar ValidationException (HTTP 422)
4. Atualizar status para REVERTIDO
5. Incrementar FichaProspeccao.quantidade em 1
6. Salvar e retornar ProspeccaoUsoResponse
```

**Metodo confirmar():**
```
1. Buscar ProspeccaoUso por id
2. Verificar pertence ao jogo do Mestre
3. Verificar status == PENDENTE
4. Atualizar status para CONFIRMADO
5. Retornar ProspeccaoUsoResponse (quantidade nao muda)
```

**Metodo conceder():**
```
1. Verificar role MESTRE
2. Buscar FichaProspeccao por (fichaId, dadoProspeccaoConfigId)
3. Se nao existir, criar novo registro com quantidade=0
4. Incrementar quantidade pelo valor informado
5. Retornar FichaResumoResponse atualizado
```

**Metodo listarUsos():**
```
- MESTRE: retorna todos os usos da ficha
- JOGADOR: retorna apenas usos da propria ficha (verificar ficha.jogadorId == usuarioId)
```

**Metodo listarPendentesJogo():**
```
- Apenas MESTRE
- Buscar todos ProspeccaoUso com status=PENDENTE do jogo
- Incluir nome do personagem em ProspeccaoUsoResponse
```

### Passo 5 — DTOs

**ConcederProspeccaoRequest:**
```java
public record ConcederProspeccaoRequest(
    @NotNull Long dadoProspeccaoConfigId,
    @NotNull @Min(1) @Max(99) Integer quantidade
) {}
```

**UsarProspeccaoRequest:**
```java
public record UsarProspeccaoRequest(
    @NotNull Long dadoProspeccaoConfigId
) {}
```

**ProspeccaoUsoResponse:**
```java
public record ProspeccaoUsoResponse(
    Long usoId,
    String dadoNome,
    Long dadoProspeccaoConfigId,
    Long fichaId,
    String personagemNome,    // para painel global do Mestre
    ProspeccaoUsoStatus status,
    LocalDateTime criadoEm
) {}
```

### Passo 6 — FichaController.java (adicionar endpoints)

```java
// ==================== PROSPECCAO (SEMANTICA) ====================

@PostMapping("/api/v1/fichas/{id}/prospeccao/conceder")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Conceder dados de prospeccao (Apenas MESTRE)")
public ResponseEntity<FichaResumoResponse> concederProspeccao(
    @PathVariable Long id,
    @Valid @RequestBody ConcederProspeccaoRequest request)

@PostMapping("/api/v1/fichas/{id}/prospeccao/usar")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Registrar uso de dado de prospeccao")
@ResponseStatus(HttpStatus.CREATED)
public ProspeccaoUsoResponse usarProspeccao(
    @PathVariable Long id,
    @Valid @RequestBody UsarProspeccaoRequest request)

@PatchMapping("/api/v1/fichas/{id}/prospeccao/usos/{usoId}/confirmar")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Confirmar uso de prospeccao (Apenas MESTRE)")
public ResponseEntity<ProspeccaoUsoResponse> confirmarUso(
    @PathVariable Long id,
    @PathVariable Long usoId)

@PatchMapping("/api/v1/fichas/{id}/prospeccao/usos/{usoId}/reverter")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Reverter uso de prospeccao (Apenas MESTRE)")
public ResponseEntity<ProspeccaoUsoResponse> reverterUso(
    @PathVariable Long id,
    @PathVariable Long usoId)

@GetMapping("/api/v1/fichas/{id}/prospeccao/usos")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Listar usos de prospeccao da ficha")
public ResponseEntity<List<ProspeccaoUsoResponse>> listarUsos(@PathVariable Long id)

@GetMapping("/api/v1/jogos/{jogoId}/prospeccao/pendentes")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Listar usos pendentes de prospeccao do jogo (Apenas MESTRE)")
public ResponseEntity<List<ProspeccaoUsoResponse>> listarPendentesJogo(
    @PathVariable Long jogoId)
```

---

## Criterios de Aceitacao

- [ ] `POST /fichas/{id}/prospeccao/usar` decrementa quantidade e cria ProspeccaoUso PENDENTE
- [ ] `POST /fichas/{id}/prospeccao/usar` com quantidade=0 retorna HTTP 422
- [ ] `POST /fichas/{id}/prospeccao/usar` por Jogador em ficha de outro Jogador retorna HTTP 403
- [ ] `PATCH .../reverter` atualiza status para REVERTIDO e incrementa quantidade
- [ ] `PATCH .../reverter` em uso CONFIRMADO retorna HTTP 422
- [ ] `PATCH .../confirmar` em uso REVERTIDO retorna HTTP 422
- [ ] `PATCH .../reverter` por Jogador retorna HTTP 403
- [ ] `GET .../usos` por Jogador retorna apenas usos da propria ficha
- [ ] `POST /fichas/{id}/prospeccao/conceder` incrementa quantidade
- [ ] `GET /jogos/{jogoId}/prospeccao/pendentes` retorna apenas usos PENDENTES do jogo
- [ ] Concorrencia: dois requests simultaneos de `usar` nao resultam em quantidade negativa
