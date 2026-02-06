package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.AtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.AtributoConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.AtributoConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de Atributos de um jogo.
 * Coordena requisições HTTP e transformação de dados.
 *
 * Padrão: Controller (thin) → Mapper → Service (business logic) → Repository
 *
 * @author Carlos Demétrio
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/atributos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Configurações - Atributos",
    description = "API para gerenciamento de atributos do jogo (Força, Agilidade, Constituição, etc). " +
                  "Apenas MESTRE pode criar/editar/deletar. JOGADOR pode apenas visualizar."
)
public class AtributoController {

    private final AtributoConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final AtributoConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar atributos de um jogo",
        description = "Retorna todos os atributos ativos do jogo especificado. " +
                      "Atributos são características fundamentais dos personagens (ex: Força, Destreza, Inteligência)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de atributos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = AtributoResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Jogo não encontrado"
        )
    })
    public ResponseEntity<List<AtributoResponse>> listar(
            @Parameter(
                description = "ID do jogo para listar os atributos",
                required = true,
                example = "1"
            )
            @RequestParam Long jogoId) {

        log.info("Listando atributos do jogo: {}", jogoId);
        List<AtributoConfig> atributos = configuracaoService.listar(jogoId);
        List<AtributoResponse> response = atributos.stream()
            .map(mapper::toResponse)
            .toList();

        log.info("Total de atributos encontrados: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Buscar atributo por ID",
        description = "Retorna os detalhes de um atributo específico"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Atributo encontrado",
            content = @Content(schema = @Schema(implementation = AtributoResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Atributo não encontrado"
        )
    })
    public ResponseEntity<AtributoResponse> buscar(
            @Parameter(
                description = "ID do atributo",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {

        log.info("Buscando atributo ID: {}", id);
        AtributoConfig atributo = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(atributo));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Criar novo atributo (Apenas MESTRE)",
        description = "Cria um novo atributo para o jogo. Apenas o MESTRE pode criar atributos. " +
                      "O nome e abreviação devem ser únicos dentro do jogo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Atributo criado com sucesso",
            content = @Content(schema = @Schema(implementation = AtributoResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos (validação falhou)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuário não tem permissão (não é MESTRE do jogo)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Jogo não encontrado"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Já existe atributo com este nome ou abreviação neste jogo"
        )
    })
    public ResponseEntity<AtributoResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do atributo a ser criado",
                required = true
            )
            @Valid @RequestBody CreateAtributoRequest request) {

        log.info("Criando atributo para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        // Mapper: Request → Entity
        AtributoConfig atributo = mapper.toEntity(request);

        // Buscar e setar o jogo (com validação de permissão)
        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        atributo.setJogo(jogo);

        // Service: business logic + validações
        AtributoConfig criado = configuracaoService.criar(atributo);

        log.info("Atributo criado com sucesso. ID: {}, Nome: {}", criado.getId(), criado.getNome());

        // Mapper: Entity → Response
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Atualizar atributo (Apenas MESTRE)",
        description = "Atualiza um atributo existente. Apenas o MESTRE do jogo pode atualizar. " +
                      "Campos não enviados (null) não serão alterados."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Atributo atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = AtributoResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuário não tem permissão (não é MESTRE do jogo)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Atributo não encontrado"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito com nome ou abreviação existente"
        )
    })
    public ResponseEntity<AtributoResponse> atualizar(
            @Parameter(
                description = "ID do atributo a ser atualizado",
                required = true,
                example = "1"
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do atributo a serem atualizados (apenas campos enviados serão alterados)",
                required = true
            )
            @Valid @RequestBody UpdateAtributoRequest request) {

        log.info("Atualizando atributo ID: {}", id);

        // Buscar entidade existente (com validação de permissão no service)
        AtributoConfig atributo = configuracaoService.buscarPorId(id);

        // Mapper: atualiza apenas campos não-null (OWASP: validação de dados)
        mapper.updateEntity(request, atributo);

        // Service: business logic + validações
        AtributoConfig atualizado = configuracaoService.atualizar(id, atributo);

        log.info("Atributo ID: {} atualizado com sucesso", id);

        // Mapper: Entity → Response
        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Deletar atributo (Apenas MESTRE)",
        description = "Soft delete - marca o atributo como inativo, mantendo dados históricos. " +
                      "Apenas o MESTRE do jogo pode deletar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Atributo deletado (inativado) com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Usuário não tem permissão (não é MESTRE do jogo)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Atributo não encontrado"
        )
    })
    public ResponseEntity<Void> deletar(
            @Parameter(
                description = "ID do atributo a ser deletado",
                required = true,
                example = "1"
            )
            @PathVariable Long id) {

        log.info("Deletando (soft delete) atributo ID: {}", id);
        configuracaoService.deletar(id);
        log.info("Atributo ID: {} deletado com sucesso", id);

        return ResponseEntity.noContent().build();
    }
}
