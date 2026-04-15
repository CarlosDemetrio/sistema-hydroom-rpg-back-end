package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateNpcDificuldadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateNpcDificuldadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.NpcDificuldadeConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.NpcDificuldadeConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.NpcDificuldadeConfiguracaoService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Controller REST para gerenciamento de configurações de nível de dificuldade de NPCs.
 * Coordena requisições HTTP e transformação de dados.
 *
 * <p>Path: /api/v1/configuracoes/npc-dificuldades</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/npc-dificuldades")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Configurações - Níveis de Dificuldade NPC",
    description = "API para gerenciamento de níveis de dificuldade de NPCs (Fácil, Médio, Difícil, etc). " +
                  "Apenas MESTRE pode criar/editar/deletar. JOGADOR pode apenas visualizar."
)
public class NpcDificuldadeConfigController {

    private final NpcDificuldadeConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final NpcDificuldadeConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar níveis de dificuldade de NPC de um jogo",
        description = "Retorna todos os níveis de dificuldade ativos do jogo. " +
                      "Pode ser filtrado por foco (FISICO ou MAGICO)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = NpcDificuldadeConfigResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<NpcDificuldadeConfigResponse>> listar(
            @Parameter(description = "ID do jogo", required = true, example = "1")
            @RequestParam Long jogoId,
            @Parameter(description = "Filtro por foco (FISICO ou MAGICO)", example = "FISICO")
            @RequestParam(required = false) FocoNpc foco) {

        log.info("Listando níveis de dificuldade NPC do jogo: {}, foco: {}", jogoId, foco);

        List<NpcDificuldadeConfig> configs = foco != null
                ? configuracaoService.listarPorFoco(jogoId, foco)
                : configuracaoService.listar(jogoId);

        List<NpcDificuldadeConfigResponse> response = configs.stream()
                .map(mapper::toResponse)
                .toList();

        log.info("Total de níveis de dificuldade encontrados: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Buscar nível de dificuldade NPC por ID",
        description = "Retorna os detalhes de um nível de dificuldade específico incluindo valores de atributos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nível de dificuldade encontrado",
            content = @Content(schema = @Schema(implementation = NpcDificuldadeConfigResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    public ResponseEntity<NpcDificuldadeConfigResponse> buscar(
            @Parameter(description = "ID do nível de dificuldade", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Buscando nível de dificuldade NPC ID: {}", id);
        NpcDificuldadeConfig config = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(config));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Criar novo nível de dificuldade NPC (Apenas MESTRE)",
        description = "Cria um novo nível de dificuldade com valores de atributos pré-definidos. " +
                      "O nome deve ser único dentro do jogo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Nível de dificuldade criado com sucesso",
            content = @Content(schema = @Schema(implementation = NpcDificuldadeConfigResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão (não é MESTRE)"),
        @ApiResponse(responseCode = "404", description = "Jogo ou atributo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe nível de dificuldade com este nome neste jogo")
    })
    public ResponseEntity<NpcDificuldadeConfigResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do nível de dificuldade a ser criado",
                required = true
            )
            @Valid @RequestBody CreateNpcDificuldadeConfigRequest request) {

        log.info("Criando nível de dificuldade NPC para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        NpcDificuldadeConfig config = mapper.toEntity(request);

        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        config.setJogo(jogo);

        if (request.valoresAtributo() != null && !request.valoresAtributo().isEmpty()) {
            List<NpcDificuldadeAtributo> valores = new ArrayList<>();
            for (var atributoReq : request.valoresAtributo()) {
                AtributoConfig atributo = configuracaoService.buscarAtributo(atributoReq.atributoId());
                valores.add(NpcDificuldadeAtributo.builder()
                        .npcDificuldadeConfig(config)
                        .atributoConfig(atributo)
                        .valorBase(atributoReq.valorBase())
                        .build());
            }
            config.setValoresAtributo(valores);
        }

        NpcDificuldadeConfig criado = configuracaoService.criar(config);

        log.info("Nível de dificuldade NPC criado com sucesso. ID: {}, Nome: {}", criado.getId(), criado.getNome());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Atualizar nível de dificuldade NPC (Apenas MESTRE)",
        description = "Atualiza um nível de dificuldade existente. Campos não enviados (null) não serão alterados. " +
                      "Quando valoresAtributo é enviado, substitui completamente a lista existente."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nível de dificuldade atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = NpcDificuldadeConfigResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Configuração ou atributo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflito com nome existente")
    })
    public ResponseEntity<NpcDificuldadeConfigResponse> atualizar(
            @Parameter(description = "ID do nível de dificuldade a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados a serem atualizados (apenas campos enviados serão alterados)",
                required = true
            )
            @Valid @RequestBody UpdateNpcDificuldadeConfigRequest request) {

        log.info("Atualizando nível de dificuldade NPC ID: {}", id);

        NpcDificuldadeConfig existente = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, existente);

        if (request.valoresAtributo() != null) {
            List<NpcDificuldadeAtributo> novosValores = new ArrayList<>();
            for (var atributoReq : request.valoresAtributo()) {
                AtributoConfig atributo = configuracaoService.buscarAtributo(atributoReq.atributoId());
                novosValores.add(NpcDificuldadeAtributo.builder()
                        .npcDificuldadeConfig(existente)
                        .atributoConfig(atributo)
                        .valorBase(atributoReq.valorBase())
                        .build());
            }
            configuracaoService.substituirValoresAtributo(existente, novosValores);
        }

        NpcDificuldadeConfig atualizado = configuracaoService.atualizar(id, existente);

        log.info("Nível de dificuldade NPC ID: {} atualizado com sucesso", id);
        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Deletar nível de dificuldade NPC (Apenas MESTRE)",
        description = "Soft delete - marca o registro como inativo, mantendo dados históricos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Nível de dificuldade deletado (inativado) com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do nível de dificuldade a ser deletado", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Deletando (soft delete) nível de dificuldade NPC ID: {}", id);
        configuracaoService.deletar(id);
        log.info("Nível de dificuldade NPC ID: {} deletado com sucesso", id);

        return ResponseEntity.noContent().build();
    }
}
