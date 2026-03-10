package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ComprarVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.service.FichaPreviewService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaResumoService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVantagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de Fichas de personagem.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Fichas", description = "Gerenciamento de fichas de personagem")
public class FichaController {

    private final FichaService fichaService;
    private final FichaMapper fichaMapper;
    private final FichaVantagemService fichaVantagemService;
    private final FichaVantagemMapper fichaVantagemMapper;
    private final FichaPreviewService fichaPreviewService;
    private final FichaResumoService fichaResumoService;

    @GetMapping("/api/v1/jogos/{jogoId}/fichas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar fichas do jogo", description = "Mestre vê todas as fichas; Jogador vê apenas as suas. Suporta filtros opcionais.")
    public ResponseEntity<List<FichaResponse>> listar(
            @PathVariable Long jogoId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long racaId,
            @RequestParam(required = false) Integer nivel) {
        var fichas = fichaService.listarComFiltros(jogoId, nome, classeId, racaId, nivel);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/jogos/{jogoId}/fichas/minhas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar minhas fichas", description = "Retorna apenas as fichas do usuário atual no jogo")
    public ResponseEntity<List<FichaResponse>> listarMinhas(@PathVariable Long jogoId) {
        var fichas = fichaService.listarMinhas(jogoId);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/jogos/{jogoId}/fichas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar ficha", description = "Cria uma nova ficha com inicialização automática de sub-registros")
    public ResponseEntity<FichaResponse> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody CreateFichaRequest request) {
        // Garantir que o jogoId da URL seja usado
        CreateFichaRequest requestComJogo = new CreateFichaRequest(
                jogoId,
                request.nome(),
                request.jogadorId(),
                request.racaId(),
                request.classeId(),
                request.generoId(),
                request.indoleId(),
                request.presencaId(),
                request.isNpc()
        );
        var ficha = fichaService.criar(requestComJogo);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar ficha por ID")
    public ResponseEntity<FichaResponse> buscarPorId(@PathVariable Long id) {
        var ficha = fichaService.buscarPorId(id);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar ficha", description = "Mestre pode editar qualquer ficha; Jogador só edita as próprias")
    public ResponseEntity<FichaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFichaRequest request) {
        var ficha = fichaService.atualizar(id, request);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar ficha (Apenas MESTRE)", description = "Soft delete da ficha")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fichaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/jogos/{jogoId}/npcs")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Listar NPCs do jogo (Apenas MESTRE)")
    public ResponseEntity<List<FichaResponse>> listarNpcs(@PathVariable Long jogoId) {
        var fichas = fichaService.listarNpcs(jogoId);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    // ==================== RESUMO ====================

    @GetMapping("/api/v1/fichas/{id}/resumo")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Resumo calculado da ficha", description = "Retorna valores calculados agregados da ficha: atributos, bônus, vida, essência e ameaça")
    public ResponseEntity<FichaResumoResponse> getResumo(@PathVariable Long id) {
        var resumo = fichaResumoService.getResumo(id);
        return ResponseEntity.ok(resumo);
    }

    // ==================== PREVIEW ====================

    @PostMapping("/api/v1/fichas/{id}/preview")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Preview de cálculos sem persistir",
            description = "Simula mudanças de atributos/XP e retorna valores recalculados sem salvar")
    public ResponseEntity<FichaPreviewResponse> preview(
            @PathVariable Long id,
            @Valid @RequestBody FichaPreviewRequest request) {
        var result = fichaPreviewService.simular(id, request);
        return ResponseEntity.ok(result);
    }

    // ==================== VANTAGENS ====================

    @GetMapping("/api/v1/fichas/{id}/vantagens")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens da ficha")
    public ResponseEntity<List<FichaVantagemResponse>> listarVantagens(@PathVariable Long id) {
        var vantagens = fichaVantagemService.listar(id);
        var response = vantagens.stream().map(fichaVantagemMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/fichas/{id}/vantagens")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Comprar vantagem para a ficha", description = "Verifica pré-requisitos e compra a vantagem no nível 1")
    public ResponseEntity<FichaVantagemResponse> comprarVantagem(
            @PathVariable Long id,
            @Valid @RequestBody ComprarVantagemRequest request) {
        var fichaVantagem = fichaVantagemService.comprar(id, request.vantagemConfigId());
        var response = fichaVantagemMapper.toResponse(fichaVantagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/v1/fichas/{id}/vantagens/{vid}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Aumentar nível de vantagem", description = "Incrementa o nível da vantagem (não pode exceder nivelMaximo)")
    public ResponseEntity<FichaVantagemResponse> aumentarNivelVantagem(
            @PathVariable Long id,
            @PathVariable Long vid) {
        var fichaVantagem = fichaVantagemService.aumentarNivel(id, vid);
        var response = fichaVantagemMapper.toResponse(fichaVantagem);
        return ResponseEntity.ok(response);
    }
}
