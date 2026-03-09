# P3-T1 — CategoriaVantagem: CRUD completo

## Objetivo
Corrigir a entity CategoriaVantagem (Lombok + ConfiguracaoEntity) e criar o CRUD completo.

## Regras de negócio
- CategoriaVantagem tem unicidade por `(jogo_id, nome)`
- Campo `cor` é hexadecimal (#RRGGBB), opcional
- Padrão de referência: `AtributoController` + `AtributoConfiguracaoService`

## Steps

### 1. Corrigir model/CategoriaVantagem.java

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias_vantagem",
    uniqueConstraints = @UniqueConstraint(name = "uk_categoria_vantagem_jogo_nome",
                                          columnNames = {"jogo_id", "nome"}))
@SQLRestriction("deleted_at IS NULL")
public class CategoriaVantagem extends BaseEntity implements ConfiguracaoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    @NotNull
    private Jogo jogo;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Size(max = 7)
    @Column(name = "cor", length = 7)  // #RRGGBB
    private String cor;

    @Column(name = "ordem_exibicao", nullable = false)
    @Builder.Default
    private Integer ordemExibicao = 0;
}
```

> **Nota**: renomear coluna `ordem` → `ordem_exibicao`. Verificar se há dados no banco de testes que precisem migração (H2 usa ddl-auto=create-drop, então não há problema).

### 2. Repository

```java
public interface CategoriaVantagemRepository extends JpaRepository<CategoriaVantagem, Long> {
    List<CategoriaVantagem> findByJogoIdOrderByOrdemExibicao(Long jogoId);
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);
    boolean existsByJogoIdAndNomeIgnoreCaseAndIdNot(Long jogoId, String nome, Long id);
}
```

### 3. DTOs (records)

```java
// CreateCategoriaVantagemRequest.java
public record CreateCategoriaVantagemRequest(
    @NotBlank @Size(max=100) String nome,
    @Size(max=1000) String descricao,
    @Size(max=7, message="Cor deve ser no formato #RRGGBB") String cor,
    Integer ordemExibicao
) {}

// UpdateCategoriaVantagemRequest.java — mesmos campos, todos nullable
public record UpdateCategoriaVantagemRequest(
    @Size(max=100) String nome,
    @Size(max=1000) String descricao,
    @Size(max=7) String cor,
    Integer ordemExibicao
) {}

// CategoriaVantagemResponse.java
public record CategoriaVantagemResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String cor,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 4. Mapper (MapStruct)

```java
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoriaVantagemMapper {
    @Mapping(target = "jogo", ignore = true)
    CategoriaVantagem toEntity(CreateCategoriaVantagemRequest request);

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    CategoriaVantagemResponse toResponse(CategoriaVantagem entity);

    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromRequest(UpdateCategoriaVantagemRequest request,
                           @MappingTarget CategoriaVantagem entity);
}
```

### 5. Service

```java
@Service
public class CategoriaVantagemService
    extends AbstractConfiguracaoService<CategoriaVantagem, CategoriaVantagemRepository> {

    @Override
    public List<CategoriaVantagem> listar(Long jogoId) {
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(CategoriaVantagem c) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(c.getJogo().getId(), c.getNome())) {
            throw new ConflictException(ValidationMessages.CategoriaVantagem.NOME_DUPLICADO);
        }
    }

    @Override
    protected void validarAntesAtualizar(CategoriaVantagem existente, CategoriaVantagem atualizado) {
        if (atualizado.getNome() != null
            && !existente.getNome().equalsIgnoreCase(atualizado.getNome())
            && repository.existsByJogoIdAndNomeIgnoreCaseAndIdNot(
                existente.getJogo().getId(), atualizado.getNome(), existente.getId())) {
            throw new ConflictException(ValidationMessages.CategoriaVantagem.NOME_DUPLICADO);
        }
    }

    @Override
    protected void atualizarCampos(CategoriaVantagem existente, CategoriaVantagem atualizado) {
        if (atualizado.getNome() != null) existente.setNome(atualizado.getNome());
        if (atualizado.getDescricao() != null) existente.setDescricao(atualizado.getDescricao());
        if (atualizado.getCor() != null) existente.setCor(atualizado.getCor());
        if (atualizado.getOrdemExibicao() != null) existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
```

### 6. Controller

Seguir padrão de `AtributoController`:
- `GET /api/jogos/{jogoId}/config/categorias-vantagem`
- `POST /api/jogos/{jogoId}/config/categorias-vantagem`
- `PUT /api/jogos/{jogoId}/config/categorias-vantagem/{id}`
- `DELETE /api/jogos/{jogoId}/config/categorias-vantagem/{id}`

### 7. Teste de integração

```java
class CategoriaVantagemServiceIntegrationTest
    extends BaseConfiguracaoServiceIntegrationTest<CategoriaVantagem, CategoriaVantagemService, CategoriaVantagemRepository> {

    @Override protected CategoriaVantagemService getService() { ... }
    @Override protected CategoriaVantagemRepository getRepository() { ... }

    @Override
    protected CategoriaVantagem criarConfiguracaoValida() {
        return CategoriaVantagem.builder()
            .jogo(jogo).nome("Treinamento Físico").cor("#FF0000").ordemExibicao(1).build();
    }

    @Override
    protected CategoriaVantagem criarConfiguracaoComNomeDuplicado() {
        return CategoriaVantagem.builder()
            .jogo(jogo).nome("Treinamento Físico").build();
    }

    @Override
    protected void atualizarCamposParaTeste(CategoriaVantagem c) { c.setNome("Mental"); }

    @Override
    protected void verificarCamposAtualizados(CategoriaVantagem c) {
        assertThat(c.getNome()).isEqualTo("Mental");
    }
}
```

## Acceptance Checks
- [ ] CategoriaVantagem criada com nome, descrição e cor
- [ ] Duplicata de nome rejeitada com ConflictException
- [ ] GET lista ordenado por ordemExibicao
- [ ] Soft delete funciona
- [ ] Testes de integração passam (herdam ~10 testes do BaseConfiguracaoServiceIntegrationTest)

## File Checklist
- `model/CategoriaVantagem.java`
- `repository/CategoriaVantagemRepository.java`
- `dto/request/configuracao/CreateCategoriaVantagemRequest.java`
- `dto/request/configuracao/UpdateCategoriaVantagemRequest.java`
- `dto/response/configuracao/CategoriaVantagemResponse.java`
- `mapper/configuracao/CategoriaVantagemMapper.java`
- `service/configuracao/CategoriaVantagemService.java`
- `controller/configuracao/CategoriaVantagemController.java`
- `test/.../CategoriaVantagemServiceIntegrationTest.java`
- `exception/ValidationMessages.java`
