package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RaridadeItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RaridadeItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RaridadeItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RaridadeItemConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RaridadeItemConfigService;
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
 * Controller REST para gerenciamento de raridades de itens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/raridades-item")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Raridades de Item",
    description = "API para gerenciamento de raridades de itens (Comum, Incomum, Raro, etc.)")
public class RaridadeItemConfigController {

    private final RaridadeItemConfigService configuracaoService;
    private final JogoService jogoService;
    private final RaridadeItemConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar raridades de item de um jogo")
    public ResponseEntity<List<RaridadeItemConfigResponse>> listar(@RequestParam Long jogoId) {
        log.info("Listando raridades de item do jogo: {}", jogoId);
        List<RaridadeItemConfig> raridades = configuracaoService.listar(jogoId);
        return ResponseEntity.ok(raridades.stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar raridade de item por ID")
    public ResponseEntity<RaridadeItemConfigResponse> buscar(@PathVariable Long id) {
        log.info("Buscando raridade de item ID: {}", id);
        RaridadeItemConfig raridade = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(raridade));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar nova raridade de item (Apenas MESTRE)")
    public ResponseEntity<RaridadeItemConfigResponse> criar(@Valid @RequestBody RaridadeItemConfigRequest request) {
        log.info("Criando raridade de item para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        RaridadeItemConfig raridade = mapper.toEntity(request);
        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        raridade.setJogo(jogo);

        RaridadeItemConfig criada = configuracaoService.criar(raridade);
        log.info("Raridade de item criada com sucesso. ID: {}", criada.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar raridade de item (Apenas MESTRE)")
    public ResponseEntity<RaridadeItemConfigResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody RaridadeItemConfigUpdateRequest request) {
        log.info("Atualizando raridade de item ID: {}", id);

        RaridadeItemConfig raridade = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, raridade);

        RaridadeItemConfig atualizada = configuracaoService.atualizar(id, raridade);
        log.info("Raridade de item ID: {} atualizada com sucesso", id);

        return ResponseEntity.ok(mapper.toResponse(atualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar raridade de item (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("Deletando raridade de item ID: {}", id);
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
