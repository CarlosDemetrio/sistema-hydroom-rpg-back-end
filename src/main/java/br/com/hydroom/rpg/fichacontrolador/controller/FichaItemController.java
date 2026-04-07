package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemAdicionarRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemCustomizadoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemDuracaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaInventarioResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaItemResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaItemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.FichaItem;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FichaItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para gerenciamento do inventário de itens de fichas de personagem.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Inventário de Ficha", description = "Gerenciamento do inventário de itens de fichas de personagem")
@RequestMapping("/api/v1/fichas/{fichaId}/itens")
public class FichaItemController {

    private final FichaItemService fichaItemService;
    private final FichaItemMapper fichaItemMapper;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar inventário da ficha",
               description = "Retorna o inventário completo separado por itens equipados e em estoque.")
    public ResponseEntity<FichaInventarioResponse> listarInventario(@PathVariable Long fichaId) {
        Long usuarioAtualId = getUsuarioAtualId();
        List<FichaItem> itens = fichaItemService.listarItens(fichaId, usuarioAtualId);

        List<FichaItem> equipados = itens.stream().filter(FichaItem::isEquipado).toList();
        List<FichaItem> inventario = itens.stream().filter(i -> !i.isEquipado()).toList();

        List<FichaItemResponse> equipadosResp = equipados.stream()
                .map(item -> toResponseComPesoEfetivo(item))
                .toList();
        List<FichaItemResponse> inventarioResp = inventario.stream()
                .map(item -> toResponseComPesoEfetivo(item))
                .toList();

        BigDecimal pesoTotal = fichaItemService.calcularPesoTotal(itens);
        BigDecimal capacidadeCarga = fichaItemService.calcularCapacidadeCarga(fichaId);
        boolean sobrecarregado = pesoTotal.compareTo(capacidadeCarga) > 0
                && capacidadeCarga.compareTo(BigDecimal.ZERO) > 0;

        return ResponseEntity.ok(new FichaInventarioResponse(
                equipadosResp, inventarioResp, pesoTotal, capacidadeCarga, sobrecarregado));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Adicionar item ao inventário",
               description = "Adiciona um item de catálogo ao inventário. Jogadores só podem adicionar itens cuja raridade permita.")
    public ResponseEntity<FichaItemResponse> adicionarItem(
            @PathVariable Long fichaId,
            @Valid @RequestBody FichaItemAdicionarRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaId, request, usuarioAtualId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponseComPesoEfetivo(fichaItem));
    }

    @PostMapping("/customizado")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar item customizado (Apenas MESTRE)",
               description = "Adiciona um item sem vinculação ao catálogo. Exclusivo do Mestre.")
    public ResponseEntity<FichaItemResponse> adicionarItemCustomizado(
            @PathVariable Long fichaId,
            @Valid @RequestBody FichaItemCustomizadoRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        FichaItem fichaItem = fichaItemService.adicionarItemCustomizado(fichaId, request, usuarioAtualId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponseComPesoEfetivo(fichaItem));
    }

    @PatchMapping("/{itemId}/equipar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Equipar item",
               description = "Marca o item como equipado. Item quebrado (duracaoAtual=0) não pode ser equipado.")
    public ResponseEntity<FichaItemResponse> equiparItem(
            @PathVariable Long fichaId,
            @PathVariable Long itemId) {
        Long usuarioAtualId = getUsuarioAtualId();
        FichaItem fichaItem = fichaItemService.equiparItem(fichaId, itemId, usuarioAtualId);
        return ResponseEntity.ok(toResponseComPesoEfetivo(fichaItem));
    }

    @PatchMapping("/{itemId}/desequipar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Desequipar item",
               description = "Remove o item da posição equipada sem removê-lo do inventário.")
    public ResponseEntity<FichaItemResponse> desequiparItem(
            @PathVariable Long fichaId,
            @PathVariable Long itemId) {
        Long usuarioAtualId = getUsuarioAtualId();
        FichaItem fichaItem = fichaItemService.desequiparItem(fichaId, itemId, usuarioAtualId);
        return ResponseEntity.ok(toResponseComPesoEfetivo(fichaItem));
    }

    @PostMapping("/{itemId}/durabilidade")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Ajustar durabilidade do item (Apenas MESTRE)",
               description = "Decrementa ou restaura a durabilidade de um item. Durabilidade 0 auto-desequipa.")
    public ResponseEntity<FichaItemResponse> ajustarDurabilidade(
            @PathVariable Long fichaId,
            @PathVariable Long itemId,
            @Valid @RequestBody FichaItemDuracaoRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        FichaItem fichaItem = fichaItemService.decrementarDurabilidade(fichaId, itemId, request, usuarioAtualId);
        return ResponseEntity.ok(toResponseComPesoEfetivo(fichaItem));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Remover item do inventário",
               description = "Soft delete do item. Jogadores não podem remover itens obrigatórios de classe inicial.")
    public ResponseEntity<Void> removerItem(
            @PathVariable Long fichaId,
            @PathVariable Long itemId) {
        Long usuarioAtualId = getUsuarioAtualId();
        fichaItemService.removerItem(fichaId, itemId, usuarioAtualId);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVADOS ====================

    private FichaItemResponse toResponseComPesoEfetivo(FichaItem item) {
        FichaItemResponse base = fichaItemMapper.toResponse(item);
        BigDecimal pesoEfetivo = fichaItemService.calcularPesoEfetivo(item);
        return new FichaItemResponse(
                base.id(),
                base.fichaId(),
                base.itemConfigId(),
                base.nome(),
                base.equipado(),
                base.duracaoAtual(),
                base.duracaoPadrao(),
                base.quantidade(),
                base.peso(),
                pesoEfetivo,
                base.notas(),
                base.adicionadoPor(),
                base.raridadeId(),
                base.raridadeNome(),
                base.raridadeCor(),
                base.dataCriacao()
        );
    }

    private Long getUsuarioAtualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
        return usuario.getId();
    }
}
