package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.AptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.AptidaoConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.AptidaoConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.TipoAptidaoConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Controller REST para gerenciamento de Aptidões.
 *
 * @author Carlos Demétrio
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/aptidoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Configurações - Aptidões",
    description = "API para gerenciamento de aptidões do jogo (Acrobacia, Guarda, Rastreamento, etc). " +
                  "Apenas MESTRE pode criar/editar/deletar. JOGADOR pode apenas visualizar."
)
public class AptidaoController {

    private final AptidaoConfiguracaoService configuracaoService;
    private final TipoAptidaoConfiguracaoService tipoAptidaoService;
    private final JogoService jogoService;
    private final AptidaoConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar aptidões de um jogo",
        description = "Retorna todas as aptidões ativas do jogo especificado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de aptidões retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<AptidaoResponse>> listar(
            @Parameter(description = "ID do jogo", required = true, example = "1")
            @RequestParam Long jogoId) {

        log.info("Listando aptidões do jogo: {}", jogoId);
        List<AptidaoConfig> aptidoes = configuracaoService.listar(jogoId);
        List<AptidaoResponse> response = aptidoes.stream()
            .map(mapper::toResponse)
            .toList();

        log.info("Total de aptidões encontradas: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar aptidão por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aptidão encontrada"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Aptidão não encontrada")
    })
    public ResponseEntity<AptidaoResponse> buscar(
            @Parameter(description = "ID da aptidão", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Buscando aptidão ID: {}", id);
        AptidaoConfig aptidao = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(aptidao));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Criar nova aptidão (Apenas MESTRE)",
        description = "Cria uma nova aptidão para o jogo. O nome deve ser único dentro do jogo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Aptidão criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Jogo ou TipoAptidao não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe aptidão com este nome neste jogo")
    })
    public ResponseEntity<AptidaoResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da aptidão a ser criada",
                required = true
            )
            @Valid @RequestBody CreateAptidaoRequest request) {

        log.info("Criando aptidão para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        AptidaoConfig aptidao = mapper.toEntity(request);
        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        TipoAptidao tipoAptidao = tipoAptidaoService.buscarPorId(request.tipoAptidaoId());

        aptidao.setJogo(jogo);
        aptidao.setTipoAptidao(tipoAptidao);

        AptidaoConfig criada = configuracaoService.criar(aptidao);
        log.info("Aptidão criada com sucesso. ID: {}, Nome: {}", criada.getId(), criada.getNome());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponse(criada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Atualizar aptidão (Apenas MESTRE)",
        description = "Atualiza uma aptidão existente. Campos null não serão alterados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aptidão atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Aptidão não encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflito com nome existente")
    })
    public ResponseEntity<AptidaoResponse> atualizar(
            @Parameter(description = "ID da aptidão", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da aptidão a serem atualizados",
                required = true
            )
            @Valid @RequestBody UpdateAptidaoRequest request) {

        log.info("Atualizando aptidão ID: {}", id);

        AptidaoConfig aptidao = configuracaoService.buscarPorId(id);

        // Se o tipo de aptidão foi alterado, buscar e setar
        if (request.tipoAptidaoId() != null) {
            TipoAptidao tipoAptidao = tipoAptidaoService.buscarPorId(request.tipoAptidaoId());
            aptidao.setTipoAptidao(tipoAptidao);
        }

        mapper.updateEntity(request, aptidao);

        AptidaoConfig atualizada = configuracaoService.atualizar(id, aptidao);
        log.info("Aptidão ID: {} atualizada com sucesso", id);

        return ResponseEntity.ok(mapper.toResponse(atualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Deletar aptidão (Apenas MESTRE)",
        description = "Soft delete - marca a aptidão como inativa, mantendo dados históricos"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aptidão deletada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Aptidão não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da aptidão", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Deletando (soft delete) aptidão ID: {}", id);
        configuracaoService.deletar(id);
        log.info("Aptidão ID: {} deletada com sucesso", id);

        return ResponseEntity.noContent().build();
    }
}
