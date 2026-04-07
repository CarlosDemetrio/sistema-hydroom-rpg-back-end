package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaAnotacaoMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FichaAnotacaoService;
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

import java.util.List;

/**
 * Controller para gerenciamento de anotações de fichas.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Anotações de Ficha", description = "Gerenciamento de anotações em fichas de personagem")
@RequestMapping("/api/v1/fichas/{fichaId}/anotacoes")
public class FichaAnotacaoController {

    private final FichaAnotacaoService fichaAnotacaoService;
    private final FichaAnotacaoMapper fichaAnotacaoMapper;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar anotações da ficha",
               description = "Mestre vê todas as anotações. Jogador vê apenas as próprias e as do Mestre marcadas como visíveis.")
    public ResponseEntity<List<AnotacaoResponse>> listar(
            @PathVariable Long fichaId,
            @RequestParam(required = false) Long pastaPaiId) {
        var anotacoes = fichaAnotacaoService.listar(fichaId, pastaPaiId);
        var response = anotacoes.stream().map(fichaAnotacaoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar anotação na ficha",
               description = "Cria uma nova anotação. Jogadores só podem criar anotações do tipo JOGADOR.")
    public ResponseEntity<AnotacaoResponse> criar(
            @PathVariable Long fichaId,
            @Valid @RequestBody CriarAnotacaoRequest request) {
        Long autorId = getUsuarioAtualId();
        var anotacao = fichaAnotacaoService.criar(fichaId, request, autorId);
        var response = fichaAnotacaoMapper.toResponse(anotacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Editar anotação",
               description = "Atualiza parcialmente uma anotação. Campos nulos são ignorados. Jogador só edita as próprias anotações.")
    public ResponseEntity<AnotacaoResponse> atualizar(
            @PathVariable Long fichaId,
            @PathVariable Long id,
            @Valid @RequestBody AtualizarAnotacaoRequest request) {
        Long autorId = getUsuarioAtualId();
        var anotacao = fichaAnotacaoService.atualizar(fichaId, id, request, autorId);
        return ResponseEntity.ok(fichaAnotacaoMapper.toResponse(anotacao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Deletar anotação",
               description = "Mestre pode deletar qualquer anotação da ficha. Jogador só pode deletar as próprias.")
    public ResponseEntity<Void> deletar(
            @PathVariable Long fichaId,
            @PathVariable Long id) {
        fichaAnotacaoService.deletar(fichaId, id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVADOS ====================

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
