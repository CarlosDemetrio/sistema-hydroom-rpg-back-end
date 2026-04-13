package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.HabilidadeConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.HabilidadeConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.HabilidadeConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.HabilidadeConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.HabilidadeConfigService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller REST para gerenciamento de habilidades do jogo.
 *
 * <p>Diferença crítica: MESTRE e JOGADOR têm permissões simétricas — ambos podem
 * criar, editar e deletar habilidades. Esta é a única configuração com esta característica.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/jogos/{jogoId}/config/habilidades")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Habilidades", description = "Gerenciamento de habilidades do jogo (MESTRE e JOGADOR podem criar/editar/deletar)")
public class HabilidadeConfigController {

    private final HabilidadeConfigService service;
    private final JogoService jogoService;
    private final HabilidadeConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar habilidades do jogo")
    public ResponseEntity<List<HabilidadeConfigResponse>> listar(@PathVariable Long jogoId) {
        log.info("Listando habilidades do jogo: {}", jogoId);
        return ResponseEntity.ok(service.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar habilidade por ID")
    public ResponseEntity<HabilidadeConfigResponse> buscar(
            @PathVariable Long jogoId,
            @PathVariable Long id) {
        log.info("Buscando habilidade ID: {} do jogo: {}", id, jogoId);
        return ResponseEntity.ok(mapper.toResponse(service.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar nova habilidade (MESTRE e JOGADOR)")
    public ResponseEntity<HabilidadeConfigResponse> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody HabilidadeConfigRequest request) {
        log.info("Criando habilidade '{}' para jogo ID: {}", request.nome(), jogoId);
        HabilidadeConfig habilidade = mapper.toEntity(request);
        habilidade.setJogo(jogoService.buscarJogo(jogoId));
        HabilidadeConfig criada = service.criar(habilidade);
        log.info("Habilidade criada com sucesso. ID: {}", criada.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar habilidade (MESTRE e JOGADOR)")
    public ResponseEntity<HabilidadeConfigResponse> atualizar(
            @PathVariable Long jogoId,
            @PathVariable Long id,
            @Valid @RequestBody HabilidadeConfigUpdateRequest request) {
        log.info("Atualizando habilidade ID: {} do jogo: {}", id, jogoId);
        HabilidadeConfig habilidade = service.buscarPorId(id);
        mapper.updateEntity(request, habilidade);
        HabilidadeConfig atualizada = service.atualizar(id, habilidade);
        log.info("Habilidade ID: {} atualizada com sucesso", id);
        return ResponseEntity.ok(mapper.toResponse(atualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Deletar habilidade (MESTRE e JOGADOR)")
    public ResponseEntity<Void> deletar(
            @PathVariable Long jogoId,
            @PathVariable Long id) {
        log.info("Deletando habilidade ID: {} do jogo: {}", id, jogoId);
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
