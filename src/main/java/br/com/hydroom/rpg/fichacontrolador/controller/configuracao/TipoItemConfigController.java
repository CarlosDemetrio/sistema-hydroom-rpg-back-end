package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.TipoItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.TipoItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.TipoItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.TipoItemConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.TipoItemConfigService;
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
 * Controller REST para gerenciamento de tipos de itens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/tipos-item")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Tipos de Item",
    description = "API para gerenciamento de tipos de itens (Arma/Espada, Armadura/Leve, etc.)")
public class TipoItemConfigController {

    private final TipoItemConfigService configuracaoService;
    private final JogoService jogoService;
    private final TipoItemConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar tipos de item de um jogo")
    public ResponseEntity<List<TipoItemConfigResponse>> listar(@RequestParam Long jogoId) {
        log.info("Listando tipos de item do jogo: {}", jogoId);
        List<TipoItemConfig> tipos = configuracaoService.listar(jogoId);
        return ResponseEntity.ok(tipos.stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar tipo de item por ID")
    public ResponseEntity<TipoItemConfigResponse> buscar(@PathVariable Long id) {
        log.info("Buscando tipo de item ID: {}", id);
        TipoItemConfig tipo = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(tipo));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar novo tipo de item (Apenas MESTRE)")
    public ResponseEntity<TipoItemConfigResponse> criar(@Valid @RequestBody TipoItemConfigRequest request) {
        log.info("Criando tipo de item para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        TipoItemConfig tipo = mapper.toEntity(request);
        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        tipo.setJogo(jogo);

        TipoItemConfig criado = configuracaoService.criar(tipo);
        log.info("Tipo de item criado com sucesso. ID: {}", criado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar tipo de item (Apenas MESTRE)")
    public ResponseEntity<TipoItemConfigResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoItemConfigUpdateRequest request) {
        log.info("Atualizando tipo de item ID: {}", id);

        TipoItemConfig tipo = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, tipo);

        TipoItemConfig atualizado = configuracaoService.atualizar(id, tipo);
        log.info("Tipo de item ID: {} atualizado com sucesso", id);

        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar tipo de item (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("Deletando tipo de item ID: {}", id);
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
