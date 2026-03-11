package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.BonusResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.BonusConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.ReordenacaoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.BonusConfiguracaoService;
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
 * Controller REST para gerenciamento de Bônus Calculados.
 *
 * @author Carlos Demétrio
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/bonus")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Configurações - Bônus",
    description = "API para gerenciamento de bônus calculados do jogo (B.B.A, B.B.D, etc). " +
                  "Apenas MESTRE pode criar/editar/deletar. JOGADOR pode apenas visualizar."
)
public class BonusController {

    private final BonusConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final BonusConfigMapper mapper;
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar bônus (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarBonus(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar bônus de um jogo", description = "Retorna todos os bônus ativos do jogo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<BonusResponse>> listar(
            @Parameter(description = "ID do jogo", required = true, example = "1")
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {

        log.info("Listando bônus do jogo: {}", jogoId);
        List<BonusConfig> bonus = configuracaoService.listar(jogoId, nome);
        List<BonusResponse> response = bonus.stream().map(mapper::toResponse).toList();
        log.info("Total de bônus encontrados: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar bônus por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bônus encontrado"),
        @ApiResponse(responseCode = "404", description = "Bônus não encontrado")
    })
    public ResponseEntity<BonusResponse> buscar(@PathVariable Long id) {
        log.info("Buscando bônus ID: {}", id);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar bônus (Apenas MESTRE)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bônus criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Bônus já existe")
    })
    public ResponseEntity<BonusResponse> criar(@Valid @RequestBody CreateBonusRequest request) {
        log.info("Criando bônus para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());
        BonusConfig bonus = mapper.toEntity(request);
        bonus.setJogo(jogoService.buscarJogo(request.jogoId()));
        BonusConfig criado = configuracaoService.criar(bonus);
        log.info("Bônus criado. ID: {}", criado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar bônus (Apenas MESTRE)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bônus atualizado"),
        @ApiResponse(responseCode = "404", description = "Bônus não encontrado")
    })
    public ResponseEntity<BonusResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateBonusRequest request) {
        log.info("Atualizando bônus ID: {}", id);
        BonusConfig bonus = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, bonus);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, bonus)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar bônus (Apenas MESTRE)", description = "Soft delete")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Bônus deletado"),
        @ApiResponse(responseCode = "404", description = "Bônus não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("Deletando bônus ID: {}", id);
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
