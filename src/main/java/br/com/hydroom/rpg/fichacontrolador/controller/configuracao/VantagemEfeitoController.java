package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CriarVantagemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemEfeitoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.VantagemEfeitoMapper;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.VantagemEfeitoService;
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
 * Controller REST para efeitos de vantagem.
 *
 * <p>Base path: /api/v1/jogos/{jogoId}/configuracoes/vantagens/{vantagemId}/efeitos</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jogos/{jogoId}/configuracoes/vantagens/{vantagemId}/efeitos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Efeitos de Vantagem", description = "Gerenciamento dos efeitos concretos de cada vantagem")
public class VantagemEfeitoController {

    private final VantagemEfeitoService efeitoService;
    private final VantagemEfeitoMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar efeitos de uma vantagem")
    public ResponseEntity<List<VantagemEfeitoResponse>> listar(
            @PathVariable Long jogoId,
            @PathVariable Long vantagemId) {
        return ResponseEntity.ok(
            efeitoService.listarPorVantagem(vantagemId).stream()
                .map(mapper::toResponse)
                .toList()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar efeito a uma vantagem (Apenas MESTRE)")
    public ResponseEntity<VantagemEfeitoResponse> criar(
            @PathVariable Long jogoId,
            @PathVariable Long vantagemId,
            @Valid @RequestBody CriarVantagemEfeitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapper.toResponse(efeitoService.criar(vantagemId, jogoId, request))
        );
    }

    @DeleteMapping("/{efeitoId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover efeito de uma vantagem (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(
            @PathVariable Long jogoId,
            @PathVariable Long vantagemId,
            @PathVariable Long efeitoId) {
        efeitoService.deletar(efeitoId, jogoId);
        return ResponseEntity.noContent().build();
    }
}
