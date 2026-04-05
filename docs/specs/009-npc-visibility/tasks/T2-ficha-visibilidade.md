# T2 — FichaVisibilidade Entity + Endpoints

> Tipo: Backend
> Dependencias: T1
> Desbloqueia: T6, T7

---

## Objetivo

Implementar a entidade `FichaVisibilidade` e os endpoints para o Mestre controlar quais jogadores tem acesso aos stats de um NPC especifico. Alem disso, finalizar a integracao desta entidade com o acesso de leitura de fichas NPC pelos Jogadores.

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `model/FichaVisibilidade.java` | Criar |
| `repository/FichaVisibilidadeRepository.java` | Criar |
| `service/FichaVisibilidadeService.java` | Criar |
| `dto/request/AtualizarVisibilidadeRequest.java` | Criar |
| `dto/request/AtualizarVisibilidadeGlobalRequest.java` | Criar |
| `dto/response/FichaVisibilidadeResponse.java` | Criar |
| `mapper/FichaVisibilidadeMapper.java` | Criar |
| `controller/FichaController.java` | Adicionar 4 endpoints de visibilidade |
| `service/FichaVidaService.java` | Atualizar `verificarAcessoLeitura()` para verificar FichaVisibilidade |
| `service/FichaService.java` | Atualizar `listarComFiltros()` para popular `jogadorTemAcessoStats` |

---

## Passos

### Passo 1 — model/FichaVisibilidade.java

```java
@Entity
@Table(name = "ficha_visibilidades",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ficha_visibilidade",
        columnNames = {"ficha_id", "jogador_id"}),
    indexes = {
        @Index(name = "idx_ficha_vis_ficha", columnList = "ficha_id"),
        @Index(name = "idx_ficha_vis_jogador", columnList = "jogador_id")
    })
@SQLRestriction("deleted_at IS NULL")
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
public class FichaVisibilidade extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "jogador_id", nullable = false)
    private Long jogadorId;
}
```

### Passo 2 — FichaVisibilidadeRepository.java

```java
public interface FichaVisibilidadeRepository extends JpaRepository<FichaVisibilidade, Long> {
    List<FichaVisibilidade> findByFichaId(Long fichaId);
    Optional<FichaVisibilidade> findByFichaIdAndJogadorId(Long fichaId, Long jogadorId);
    boolean existsByFichaIdAndJogadorId(Long fichaId, Long jogadorId);
    List<Long> findJogadorIdsByFichaId(Long fichaId); // projecao
}
```

### Passo 3 — FichaVisibilidadeService.java

Metodos:
- `listar(fichaId)` — retorna `FichaVisibilidadeResponse` para o NPC
- `atualizar(fichaId, request)` — se `substituir=true`, soft-deleta existentes e recria; se `false`, apenas adiciona novos; idempotente por par (fichaId, jogadorId)
- `revogar(fichaId, jogadorId)` — soft-delete do registro
- `atualizarGlobal(fichaId, visivelGlobalmente)` — atualiza campo em Ficha

**Validacoes no service:**
- `ficha.isNpc() == false` → lancar `ValidationException("Visibilidade granular e valida apenas para NPCs")`
- `jogadorId` deve ser participante aprovado do jogo da ficha → consultar `JogoParticipanteRepository`
- Apenas MESTRE do jogo pode modificar visibilidade

### Passo 4 — DTOs

**AtualizarVisibilidadeRequest:**
```java
public record AtualizarVisibilidadeRequest(
    @NotNull List<Long> jogadoresIds,
    @Builder.Default boolean substituir = false
) {}
```

**AtualizarVisibilidadeGlobalRequest:**
```java
public record AtualizarVisibilidadeGlobalRequest(
    @NotNull Boolean visivelGlobalmente
) {}
```

**FichaVisibilidadeResponse:**
```java
public record FichaVisibilidadeResponse(
    Long fichaId,
    boolean visivelGlobalmente,
    List<JogadorAcessoResponse> jogadoresComAcesso
) {
    public record JogadorAcessoResponse(
        Long jogadorId,
        String jogadorNome,
        String nomePersonagem
    ) {}
}
```

### Passo 5 — FichaController.java (adicionar endpoints)

```java
// ==================== VISIBILIDADE NPC ====================

@GetMapping("/api/v1/fichas/{id}/visibilidade")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Listar jogadores com acesso aos stats do NPC (Apenas MESTRE)")
public ResponseEntity<FichaVisibilidadeResponse> listarVisibilidade(@PathVariable Long id)

@PostMapping("/api/v1/fichas/{id}/visibilidade")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Revelar stats do NPC para jogadores especificos (Apenas MESTRE)")
public ResponseEntity<FichaVisibilidadeResponse> atualizarVisibilidade(
    @PathVariable Long id,
    @Valid @RequestBody AtualizarVisibilidadeRequest request)

@DeleteMapping("/api/v1/fichas/{id}/visibilidade/{jogadorId}")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Revogar acesso de jogador ao NPC (Apenas MESTRE)")
public ResponseEntity<Void> revogarVisibilidade(
    @PathVariable Long id,
    @PathVariable Long jogadorId)

@PatchMapping("/api/v1/fichas/{id}/visibilidade/global")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Atualizar visibilidade global do NPC (Apenas MESTRE)")
public ResponseEntity<FichaResponse> atualizarVisibilidadeGlobal(
    @PathVariable Long id,
    @Valid @RequestBody AtualizarVisibilidadeGlobalRequest request)
```

### Passo 6 — Atualizar FichaVidaService.verificarAcessoLeitura()

Substituir o bloco que lanca 403 para NPCs:

```java
// Antes:
if (ficha.isNpc()) {
    throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
}

// Depois:
if (ficha.isNpc()) {
    if (!fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(
            ficha.getId(), usuarioAtual.getId())) {
        throw new ForbiddenException("Acesso negado: você não tem acesso às estatísticas deste NPC.");
    }
}
```

### Passo 7 — Atualizar FichaService.listarComFiltros()

Para Jogadores, apos retornar as proprias fichas, buscar NPCs visiveis e calcular `jogadorTemAcessoStats`:

```java
// Para cada NPC visivel globalmente:
boolean temAcesso = fichaVisibilidadeRepository
    .existsByFichaIdAndJogadorId(npc.getId(), usuarioAtual.getId());
// Popular fichaResponse.jogadorTemAcessoStats = temAcesso
```

---

## Criterios de Aceitacao

- [ ] `FichaVisibilidade` persiste com soft delete
- [ ] `POST /fichas/{id}/visibilidade` e idempotente (sem duplicar registro)
- [ ] `POST /fichas/{id}/visibilidade` para ficha de jogador (isNpc=false) retorna HTTP 422
- [ ] `DELETE /fichas/{id}/visibilidade/{jogadorId}` faz soft delete
- [ ] `GET /jogos/{id}/fichas` para Jogador retorna NPCs globais com `jogadorTemAcessoStats` correto
- [ ] `GET /fichas/{id}` para Jogador com FichaVisibilidade ativa retorna HTTP 200
- [ ] `GET /fichas/{id}` para Jogador sem FichaVisibilidade retorna HTTP 403
- [ ] `GET /fichas/{id}/visibilidade` retorna lista com nomes dos jogadores
- [ ] Apenas MESTRE pode modificar visibilidade (outros retornam HTTP 403)
- [ ] Revelar NPC para jogador nao participante aprovado retorna HTTP 422
