# T1 — HabilidadeConfig: Entity + Repository + Service + DTOs + Mapper + Controller + Testes

> Fase: Backend | Dependencias: nenhuma | Bloqueia: T2 (Frontend)
> Estimativa: 2–3 horas

---

## Objetivo

Implementar `HabilidadeConfig` como uma entidade de configuracao completa, seguindo o padrao das 13 entidades existentes. A diferenca critica em relacao ao padrao e que **todos os endpoints (inclusive POST, PUT, DELETE) sao acessiveis para MESTRE e JOGADOR** — nenhuma operacao e exclusiva de MESTRE.

---

## Contexto

O PO definiu: "Apenas nome, descricao, dano/efeito (texto), e libere para jogador tambem criar."

`HabilidadeConfig` e a primeira entidade de configuracao do sistema com permissoes simetricas entre MESTRE e JOGADOR. O modelo de dados e simples: nome, descricao opcional, danoEfeito opcional, ordemExibicao. Sem siglas, sem formulas, sem sub-entidades.

**Padrao de referencia:** `AtributoConfig` / `AtributoConfiguracaoService` / `AtributoController` / `AtributoConfiguracaoServiceIntegrationTest`

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `model/HabilidadeConfig.java` | Entity JPA |
| `repository/HabilidadeConfigRepository.java` | Spring Data JPA |
| `service/configuracao/HabilidadeConfigService.java` | Logica de negocio |
| `dto/request/configuracao/CreateHabilidadeConfigDTO.java` | DTO de criacao (record) |
| `dto/request/configuracao/UpdateHabilidadeConfigDTO.java` | DTO de atualizacao (record) |
| `dto/response/configuracao/HabilidadeConfigResponseDTO.java` | DTO de resposta (record) |
| `mapper/configuracao/HabilidadeConfigMapper.java` | MapStruct |
| `controller/configuracao/HabilidadeConfigController.java` | Controller REST |
| `test/.../HabilidadeConfigServiceIntegrationTest.java` | Testes de integracao |

---

## Passos de Implementacao

### Passo 1 — Entity: `HabilidadeConfig`

```java
package br.com.hydroom.rpg.model;

import br.com.hydroom.rpg.model.base.BaseEntity;
import br.com.hydroom.rpg.model.interfaces.ConfiguracaoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "habilidade_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_habilidade_config_jogo_nome",
        columnNames = {"jogo_id", "nome"}
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabilidadeConfig extends BaseEntity implements ConfiguracaoEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(max = 1000)
    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Size(max = 500)
    @Column(name = "dano_efeito", length = 500)
    private String danoEfeito;

    @NotNull
    @Min(0)
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao;
}
```

**Notas:**
- Herda de `BaseEntity`: `id`, `deletedAt`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `@SQLRestriction("deleted_at IS NULL")`
- Implementa `ConfiguracaoEntity`: garante `getId()` (herdado) e `getJogo()` (campo proprio)
- Unique constraint `(jogo_id, nome)` — nome unico por jogo
- Sem campo `sigla`/`abreviacao` — nao participa do namespace cross-entity

---

### Passo 2 — Repository: `HabilidadeConfigRepository`

```java
package br.com.hydroom.rpg.repository;

import br.com.hydroom.rpg.model.HabilidadeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabilidadeConfigRepository extends JpaRepository<HabilidadeConfig, Long> {

    List<HabilidadeConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    Optional<HabilidadeConfig> findByJogoIdAndNome(Long jogoId, String nome);

    boolean existsByJogoIdAndNome(Long jogoId, String nome);
}
```

---

### Passo 3 — DTOs

**CreateHabilidadeConfigDTO (record):**

```java
package br.com.hydroom.rpg.dto.request.configuracao;

import jakarta.validation.constraints.*;

public record CreateHabilidadeConfigDTO(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 1000) String descricao,
    @Size(max = 500) String danoEfeito,
    @NotNull @Min(0) Integer ordemExibicao
) {}
```

**UpdateHabilidadeConfigDTO (record):**

```java
package br.com.hydroom.rpg.dto.request.configuracao;

import jakarta.validation.constraints.*;

public record UpdateHabilidadeConfigDTO(
    @Size(max = 100) String nome,
    @Size(max = 1000) String descricao,
    @Size(max = 500) String danoEfeito,
    @Min(0) Integer ordemExibicao
) {}
```

Nota sobre update: todos os campos sao opcionais. O mapper usa `NullValuePropertyMappingStrategy.IGNORE` para nao sobrescrever campos com null. Se `nome` for enviado no update, deve ser `@NotBlank` — mas como e record, a validacao de "se presente, nao pode ser blank" precisa ser tratada no servico (ou usar `@NotBlank` quando o campo for nao-null via validacao customizada).

**HabilidadeConfigResponseDTO (record):**

```java
package br.com.hydroom.rpg.dto.response.configuracao;

import java.time.LocalDateTime;

public record HabilidadeConfigResponseDTO(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String danoEfeito,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

---

### Passo 4 — Mapper: `HabilidadeConfigMapper`

```java
package br.com.hydroom.rpg.mapper.configuracao;

import br.com.hydroom.rpg.dto.request.configuracao.CreateHabilidadeConfigDTO;
import br.com.hydroom.rpg.dto.request.configuracao.UpdateHabilidadeConfigDTO;
import br.com.hydroom.rpg.dto.response.configuracao.HabilidadeConfigResponseDTO;
import br.com.hydroom.rpg.model.HabilidadeConfig;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HabilidadeConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    HabilidadeConfig toEntity(CreateHabilidadeConfigDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jogo", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateHabilidadeConfigDTO dto, @MappingTarget HabilidadeConfig entity);

    @Mapping(target = "jogoId", source = "jogo.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    HabilidadeConfigResponseDTO toResponse(HabilidadeConfig entity);
}
```

---

### Passo 5 — Service: `HabilidadeConfigService`

```java
package br.com.hydroom.rpg.service.configuracao;

import br.com.hydroom.rpg.dto.response.configuracao.HabilidadeConfigResponseDTO;
import br.com.hydroom.rpg.exception.ConfiguracaoDuplicadaException;
import br.com.hydroom.rpg.model.HabilidadeConfig;
import br.com.hydroom.rpg.repository.HabilidadeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HabilidadeConfigService
        extends AbstractConfiguracaoService<HabilidadeConfig, HabilidadeConfigResponseDTO> {

    private final HabilidadeConfigRepository habilidadeRepository;

    public HabilidadeConfigService(HabilidadeConfigRepository habilidadeRepository) {
        super(habilidadeRepository);
        this.habilidadeRepository = habilidadeRepository;
    }

    @Override
    protected void atualizarCampos(HabilidadeConfig existente, HabilidadeConfig atualizado) {
        if (atualizado.getNome() != null) existente.setNome(atualizado.getNome());
        if (atualizado.getDescricao() != null) existente.setDescricao(atualizado.getDescricao());
        if (atualizado.getDanoEfeito() != null) existente.setDanoEfeito(atualizado.getDanoEfeito());
        if (atualizado.getOrdemExibicao() != null) existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    @Override
    protected void validarAntesCriar(HabilidadeConfig configuracao) {
        if (habilidadeRepository.existsByJogoIdAndNome(
                configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConfiguracaoDuplicadaException(
                "Ja existe uma habilidade com o nome '" + configuracao.getNome() +
                "' neste jogo.");
        }
    }

    @Override
    protected void validarAntesAtualizar(HabilidadeConfig existente, HabilidadeConfig atualizado) {
        if (atualizado.getNome() != null &&
                !atualizado.getNome().equals(existente.getNome()) &&
                habilidadeRepository.existsByJogoIdAndNome(
                        existente.getJogo().getId(), atualizado.getNome())) {
            throw new ConfiguracaoDuplicadaException(
                "Ja existe uma habilidade com o nome '" + atualizado.getNome() +
                "' neste jogo.");
        }
    }
}
```

**Notas sobre o service:**
- `@Transactional(readOnly = true)` na classe — padrao do projeto
- Metodos de escrita herdam `@Transactional` de `AbstractConfiguracaoService`
- `atualizarCampos()` e `validarAntesCriar()` sao os dois metodos obrigatorios da base abstrata
- `validarAntesAtualizar()` e opcional mas necessario para revalidar unicidade no update

---

### Passo 6 — Controller: `HabilidadeConfigController`

```java
package br.com.hydroom.rpg.controller.configuracao;

import br.com.hydroom.rpg.dto.request.configuracao.CreateHabilidadeConfigDTO;
import br.com.hydroom.rpg.dto.request.configuracao.UpdateHabilidadeConfigDTO;
import br.com.hydroom.rpg.dto.response.configuracao.HabilidadeConfigResponseDTO;
import br.com.hydroom.rpg.mapper.configuracao.HabilidadeConfigMapper;
import br.com.hydroom.rpg.service.configuracao.HabilidadeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jogos/{jogoId}/habilidades")
@Tag(name = "Habilidades", description = "Gerenciamento de habilidades por jogo")
public class HabilidadeConfigController {

    private final HabilidadeConfigService service;
    private final HabilidadeConfigMapper mapper;

    public HabilidadeConfigController(HabilidadeConfigService service,
                                      HabilidadeConfigMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar habilidades do jogo")
    public ResponseEntity<List<HabilidadeConfigResponseDTO>> listar(
            @PathVariable Long jogoId) {
        return ResponseEntity.ok(service.listar(jogoId)
                .stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar habilidade por ID")
    public ResponseEntity<HabilidadeConfigResponseDTO> buscarPorId(
            @PathVariable Long jogoId,
            @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.buscarPorId(jogoId, id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar nova habilidade")
    public ResponseEntity<HabilidadeConfigResponseDTO> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody CreateHabilidadeConfigDTO dto) {
        var entity = mapper.toEntity(dto);
        var criado = service.criar(jogoId, entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar habilidade existente")
    public ResponseEntity<HabilidadeConfigResponseDTO> atualizar(
            @PathVariable Long jogoId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateHabilidadeConfigDTO dto) {
        var entity = new br.com.hydroom.rpg.model.HabilidadeConfig();
        mapper.updateEntity(dto, entity);
        var atualizado = service.atualizar(jogoId, id, entity);
        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Deletar habilidade (soft delete)")
    public ResponseEntity<Void> deletar(
            @PathVariable Long jogoId,
            @PathVariable Long id) {
        service.deletar(jogoId, id);
        return ResponseEntity.noContent().build();
    }
}
```

**Ponto critico de seguranca:**
Todos os 5 metodos usam `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`. Este e o unico lugar no projeto onde POST, PUT e DELETE sao acessiveis ao JOGADOR. Isso e intencional e foi explicitamente definido pelo PO.

---

### Passo 7 — Testes de Integracao: `HabilidadeConfigServiceIntegrationTest`

```java
package br.com.hydroom.rpg.service.configuracao;

import br.com.hydroom.rpg.model.HabilidadeConfig;
import br.com.hydroom.rpg.repository.HabilidadeConfigRepository;
import br.com.hydroom.rpg.service.base.BaseConfiguracaoServiceIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class HabilidadeConfigServiceIntegrationTest
        extends BaseConfiguracaoServiceIntegrationTest<HabilidadeConfig, HabilidadeConfigService, HabilidadeConfigRepository> {

    @Autowired
    private HabilidadeConfigService habilidadeService;

    @Autowired
    private HabilidadeConfigRepository habilidadeRepository;

    @Override
    protected HabilidadeConfigService getService() {
        return habilidadeService;
    }

    @Override
    protected HabilidadeConfigRepository getRepository() {
        return habilidadeRepository;
    }

    @Override
    protected HabilidadeConfig criarConfiguracaoValida() {
        return HabilidadeConfig.builder()
                .nome("Golpe Brutal")
                .descricao("Um ataque poderoso que causa dano maximo")
                .danoEfeito("2D6+FOR de dano fisico")
                .ordemExibicao(1)
                .build();
    }

    @Override
    protected HabilidadeConfig criarConfiguracaoComNomeDuplicado() {
        return HabilidadeConfig.builder()
                .nome("Golpe Brutal")   // mesmo nome
                .ordemExibicao(2)
                .build();
    }

    @Override
    protected void atualizarCamposParaTeste(HabilidadeConfig configuracao) {
        configuracao.setNome("Golpe Devastador");
        configuracao.setDanoEfeito("3D8+FOR de dano fisico");
    }

    @Override
    protected void verificarCamposAtualizados(HabilidadeConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Golpe Devastador");
        assertThat(configuracao.getDanoEfeito()).isEqualTo("3D8+FOR de dano fisico");
    }

    // Testes adicionais especificos de HabilidadeConfig

    @Test
    @DisplayName("deve criar habilidade com campos opcionais nulos")
    void deveCriarHabilidadeComCamposOpcionaisNulos() {
        // Arrange
        var habilidade = HabilidadeConfig.builder()
                .nome("Habilidade Simples")
                .descricao(null)
                .danoEfeito(null)
                .ordemExibicao(10)
                .build();

        // Act
        var criado = habilidadeService.criar(jogoId, habilidade);

        // Assert
        assertThat(criado.getDescricao()).isNull();
        assertThat(criado.getDanoEfeito()).isNull();
        assertThat(criado.getNome()).isEqualTo("Habilidade Simples");
    }

    @Test
    @DisplayName("deve listar habilidades ordenadas por ordemExibicao")
    void deveListarHabilidadesOrdenadas() {
        // Arrange
        habilidadeService.criar(jogoId, HabilidadeConfig.builder()
                .nome("Habilidade Z").ordemExibicao(10).build());
        habilidadeService.criar(jogoId, HabilidadeConfig.builder()
                .nome("Habilidade A").ordemExibicao(1).build());
        habilidadeService.criar(jogoId, HabilidadeConfig.builder()
                .nome("Habilidade M").ordemExibicao(5).build());

        // Act
        var lista = habilidadeService.listar(jogoId);

        // Assert
        assertThat(lista).hasSize(3);
        assertThat(lista.get(0).getOrdemExibicao()).isEqualTo(1);
        assertThat(lista.get(1).getOrdemExibicao()).isEqualTo(5);
        assertThat(lista.get(2).getOrdemExibicao()).isEqualTo(10);
    }
}
```

**Cobertura automatica via `BaseConfiguracaoServiceIntegrationTest` (~10 testes):**
- Criar configuracao com sucesso
- Criar com nome duplicado — espera HTTP 409 / excecao
- Listar configuracoes do jogo
- Buscar por ID existente
- Buscar por ID inexistente — espera 404
- Atualizar campos
- Deletar (soft delete)
- Verificar que deletado nao aparece na listagem
- Restaurar (se aplicavel)
- Verificar isolamento por jogo (habilidades de jogo A nao aparecem em jogo B)

---

## Checklist de Implementacao

- [ ] Entity `HabilidadeConfig` com Lombok `@Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor`
- [ ] Entity extends `BaseEntity` implements `ConfiguracaoEntity`
- [ ] Unique constraint `(jogo_id, nome)` na anotacao `@Table`
- [ ] Repository com `findByJogoIdOrderByOrdemExibicao` e `existsByJogoIdAndNome`
- [ ] DTOs como records (Create, Update, Response)
- [ ] Mapper MapStruct com `NullValuePropertyMappingStrategy.IGNORE` no update
- [ ] Mapper com `@Mapping(target="jogoId", source="jogo.id")` no toResponse
- [ ] Mapper com `@Mapping(target="dataCriacao", source="createdAt")` e `dataUltimaAtualizacao`
- [ ] Service extends `AbstractConfiguracaoService<HabilidadeConfig, HabilidadeConfigResponseDTO>`
- [ ] Service implementa `atualizarCampos()` e `validarAntesCriar()`
- [ ] Service implementa `validarAntesAtualizar()` para revalidar unicidade no update
- [ ] Controller com `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` em TODOS os metodos (inclusive POST, PUT, DELETE)
- [ ] Testes: `extends BaseConfiguracaoServiceIntegrationTest`
- [ ] Testes adicionais: campos opcionais nulos, ordenacao por ordemExibicao
- [ ] `./mvnw test` 100% verde apos implementacao

---

## Pontos de Atencao

**Permissoes:** Esta e a unica entidade de configuracao onde JOGADOR pode criar/editar/deletar. Nao copiar o padrao das outras 13 configs (onde writes sao `hasRole('MESTRE')`). Verificar que nenhum `@PreAuthorize` no controller usa apenas `MESTRE`.

**Sem sigla:** Nao adicionar campo `sigla` nem registrar em `SiglaValidationService`. Esta entidade nao participa do namespace cross-entity de siglas.

**sem Flyway:** O projeto usa `ddl-auto=update`. A tabela `habilidade_config` sera criada automaticamente. Nao criar script SQL de migration.

**Verificacao de jogo:** O metodo `criar(jogoId, entity)` em `AbstractConfiguracaoService` ja busca e valida o Jogo. Nao e necessario implementar essa verificacao no service filho.

---

## Commit

```
feat(config): HabilidadeConfig entity + CRUD + testes [Copilot R07 T-HAB]
```

---

*Produzido por: Business Analyst/PO | 2026-04-12*
