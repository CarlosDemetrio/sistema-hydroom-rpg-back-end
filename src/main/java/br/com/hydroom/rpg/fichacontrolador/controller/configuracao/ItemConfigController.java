package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemConfigUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemConfigResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemEfeitoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ItemRequisitoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ItemConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ItemEfeitoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ItemRequisitoMapper;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ItemConfigService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ItemEfeitoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ItemRequisitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de configurações de itens.
 *
 * <p>Base path: /api/v1/configuracoes/itens</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/itens")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Itens", description = "Gerenciamento do catálogo de itens do jogo")
public class ItemConfigController {

    private final ItemConfigService itemConfigService;
    private final ItemEfeitoService efeitoService;
    private final ItemRequisitoService requisitoService;
    private final JogoService jogoService;
    private final ItemConfigMapper mapper;
    private final ItemEfeitoMapper efeitoMapper;
    private final ItemRequisitoMapper requisitoMapper;

    // ===== ItemConfig CRUD =====

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar itens de um jogo com filtros opcionais")
    public ResponseEntity<Page<ItemConfigResumoResponse>> listar(
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nomeQuery,
            @RequestParam(required = false) Long raridadeId,
            @RequestParam(required = false) CategoriaItem categoriaItem,
            Pageable pageable) {
        log.info("Listando itens do jogo: {} com filtros", jogoId);
        Page<ItemConfig> page = itemConfigService.listarComFiltros(jogoId, nomeQuery, raridadeId, categoriaItem, pageable);
        Page<ItemConfigResumoResponse> response = page.map(mapper::toResumoResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar item por ID (inclui efeitos e requisitos)")
    public ResponseEntity<ItemConfigResponse> buscar(@PathVariable Long id) {
        log.info("Buscando item ID: {}", id);
        ItemConfig item = itemConfigService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(item));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar novo item (Apenas MESTRE)")
    public ResponseEntity<ItemConfigResponse> criar(@Valid @RequestBody ItemConfigRequest request) {
        log.info("Criando item para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        ItemConfig item = mapper.toEntity(request);
        item.setJogo(jogoService.buscarJogo(request.jogoId()));
        item.setRaridade(itemConfigService.buscarRaridade(request.raridadeId()));
        item.setTipo(itemConfigService.buscarTipo(request.tipoId()));

        ItemConfig criado = itemConfigService.criar(item);
        log.info("Item criado com sucesso. ID: {}", criado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar item (Apenas MESTRE)")
    public ResponseEntity<ItemConfigResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ItemConfigUpdateRequest request) {
        log.info("Atualizando item ID: {}", id);

        ItemConfig item = itemConfigService.buscarPorId(id);
        mapper.updateEntity(request, item);

        if (request.raridadeId() != null) {
            item.setRaridade(itemConfigService.buscarRaridade(request.raridadeId()));
        }
        if (request.tipoId() != null) {
            item.setTipo(itemConfigService.buscarTipo(request.tipoId()));
        }

        ItemConfig atualizado = itemConfigService.atualizar(id, item);
        log.info("Item ID: {} atualizado com sucesso", id);

        return ResponseEntity.ok(mapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar item (soft delete - Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("Deletando item ID: {}", id);
        itemConfigService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ItemEfeito sub-resource =====

    @GetMapping("/{id}/efeitos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar efeitos de um item")
    public ResponseEntity<List<ItemEfeitoResponse>> listarEfeitos(@PathVariable Long id) {
        return ResponseEntity.ok(
            efeitoService.listarEfeitos(id).stream().map(efeitoMapper::toResponse).toList()
        );
    }

    @PostMapping("/{id}/efeitos")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar efeito a um item (Apenas MESTRE)")
    public ResponseEntity<ItemEfeitoResponse> adicionarEfeito(
            @PathVariable Long id,
            @Valid @RequestBody ItemEfeitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            efeitoMapper.toResponse(efeitoService.adicionarEfeito(id, request))
        );
    }

    @PutMapping("/{id}/efeitos/{efeitoId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar efeito de um item (Apenas MESTRE)")
    public ResponseEntity<ItemEfeitoResponse> atualizarEfeito(
            @PathVariable Long id,
            @PathVariable Long efeitoId,
            @Valid @RequestBody ItemEfeitoRequest request) {
        return ResponseEntity.ok(
            efeitoMapper.toResponse(efeitoService.atualizarEfeito(id, efeitoId, request))
        );
    }

    @DeleteMapping("/{id}/efeitos/{efeitoId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover efeito de um item (Apenas MESTRE)")
    public ResponseEntity<Void> removerEfeito(@PathVariable Long id, @PathVariable Long efeitoId) {
        efeitoService.removerEfeito(id, efeitoId);
        return ResponseEntity.noContent().build();
    }

    // ===== ItemRequisito sub-resource =====

    @GetMapping("/{id}/requisitos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar requisitos de um item")
    public ResponseEntity<List<ItemRequisitoResponse>> listarRequisitos(@PathVariable Long id) {
        return ResponseEntity.ok(
            requisitoService.listarRequisitos(id).stream().map(requisitoMapper::toResponse).toList()
        );
    }

    @PostMapping("/{id}/requisitos")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar requisito a um item (Apenas MESTRE)")
    public ResponseEntity<ItemRequisitoResponse> adicionarRequisito(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequisitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            requisitoMapper.toResponse(requisitoService.adicionarRequisito(id, request))
        );
    }

    @DeleteMapping("/{id}/requisitos/{reqId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover requisito de um item (Apenas MESTRE)")
    public ResponseEntity<Void> removerRequisito(@PathVariable Long id, @PathVariable Long reqId) {
        requisitoService.removerRequisito(id, reqId);
        return ResponseEntity.noContent().build();
    }
}
