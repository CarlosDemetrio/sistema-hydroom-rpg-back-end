# P3-T2 — PontosVantagemConfig: CRUD completo

## Objetivo
Corrigir a entity PontosVantagemConfig (Lombok) e criar o CRUD completo. Tem unicidade por `(jogo_id, nivel)` — não por nome — então **não usa `AbstractConfiguracaoService`** nem implementa `ConfiguracaoEntity`.

## Steps

### 1. Corrigir model/PontosVantagemConfig.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pontos_vantagem_config",
    uniqueConstraints = @UniqueConstraint(name = "uk_pontos_vantagem_jogo_nivel",
                                          columnNames = {"jogo_id", "nivel"}))
@SQLRestriction("deleted_at IS NULL")
public class PontosVantagemConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull
    private Jogo jogo;

    @NotNull
    @Min(1)
    @Column(name = "nivel", nullable = false)
    private Integer nivel;

    @NotNull
    @Min(0)
    @Column(name = "pontos_ganhos", nullable = false)
    @Builder.Default
    private Integer pontosGanhos = 1;
}
```

### 2. Repository

```java
public interface PontosVantagemConfigRepository extends JpaRepository<PontosVantagemConfig, Long> {
    List<PontosVantagemConfig> findByJogoIdOrderByNivel(Long jogoId);
    boolean existsByJogoIdAndNivel(Long jogoId, Integer nivel);
    boolean existsByJogoIdAndNivelAndIdNot(Long jogoId, Integer nivel, Long id);
}
```

### 3. DTOs (records)

```java
// CreatePontosVantagemRequest.java
public record CreatePontosVantagemRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull @Min(0) Integer pontosGanhos
) {}

// UpdatePontosVantagemRequest.java
public record UpdatePontosVantagemRequest(
    @Min(0) Integer pontosGanhos   // nivel não pode mudar (é a chave de negócio)
) {}

// PontosVantagemResponse.java
public record PontosVantagemResponse(
    Long id,
    Long jogoId,
    Integer nivel,
    Integer pontosGanhos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 4. Mapper

```java
@Mapper(componentModel = "spring")
public interface PontosVantagemMapper {
    @Mapping(target = "jogo", ignore = true)
    PontosVantagemConfig toEntity(CreatePontosVantagemRequest request);

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    PontosVantagemResponse toResponse(PontosVantagemConfig entity);
}
```

### 5. Service (não extende AbstractConfiguracaoService)

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PontosVantagemService {

    private final PontosVantagemConfigRepository repository;
    private final JogoRepository jogoRepository; // para buscar o Jogo

    public List<PontosVantagemConfig> listar(Long jogoId) {
        return repository.findByJogoIdOrderByNivel(jogoId);
    }

    public PontosVantagemConfig buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PontosVantagemConfig", id));
    }

    @Transactional
    public PontosVantagemConfig criar(Long jogoId, PontosVantagemConfig config) {
        Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", jogoId));
        config.setJogo(jogo);
        if (repository.existsByJogoIdAndNivel(jogoId, config.getNivel())) {
            throw new ConflictException(
                ValidationMessages.PontosVantagem.NIVEL_DUPLICADO.formatted(config.getNivel()));
        }
        return repository.save(config);
    }

    @Transactional
    public PontosVantagemConfig atualizar(Long id, PontosVantagemConfig atualizado) {
        PontosVantagemConfig existente = buscarPorId(id);
        // Nível não pode mudar — é a chave de negócio
        if (atualizado.getPontosGanhos() != null) {
            existente.setPontosGanhos(atualizado.getPontosGanhos());
        }
        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        PontosVantagemConfig config = buscarPorId(id);
        config.delete(); // soft delete via BaseEntity
        repository.save(config);
    }
}
```

### 6. Controller

Rota: `/api/jogos/{jogoId}/config/pontos-vantagem`

Endpoints:
- `GET /` — listar por jogo
- `POST /` — criar (jogoId no path, nivel+pontosGanhos no body)
- `PUT /{id}` — atualizar pontosGanhos
- `DELETE /{id}` — soft delete

### 7. Testes de integração

Testes manuais (não usa BaseConfiguracaoServiceIntegrationTest):
- Criar com nível válido → salvo
- Criar com nível duplicado → ConflictException
- Atualizar pontosGanhos → salvo
- Deletar → soft delete
- Listar → ordenado por nível

## Acceptance Checks
- [ ] Criar PontosVantagemConfig com nivel=1, pontosGanhos=2 → salvo
- [ ] Criar segundo com nivel=1 no mesmo jogo → ConflictException
- [ ] Nível pode repetir em jogos diferentes
- [ ] Update altera só pontosGanhos
- [ ] Soft delete funciona (deleted_at preenchido)

## File Checklist
- `model/PontosVantagemConfig.java`
- `repository/PontosVantagemConfigRepository.java`
- `dto/request/configuracao/CreatePontosVantagemRequest.java`
- `dto/request/configuracao/UpdatePontosVantagemRequest.java`
- `dto/response/configuracao/PontosVantagemResponse.java`
- `mapper/configuracao/PontosVantagemMapper.java`
- `service/configuracao/PontosVantagemService.java`
- `controller/configuracao/PontosVantagemController.java`
- `test/.../PontosVantagemServiceIntegrationTest.java`
- `exception/ValidationMessages.java`
