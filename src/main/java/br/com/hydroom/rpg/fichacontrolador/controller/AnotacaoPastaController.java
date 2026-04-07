package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarPastaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.AnotacaoPastaResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.service.AnotacaoPastaService;
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
 * Controller para gerenciamento de pastas de anotações em fichas.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Pastas de Anotações", description = "Organização de anotações em pastas hierárquicas")
@RequestMapping("/api/v1/fichas/{fichaId}/anotacao-pastas")
public class AnotacaoPastaController {

    private final AnotacaoPastaService anotacaoPastaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar pastas como árvore",
               description = "Retorna a estrutura hierárquica de pastas da ficha.")
    public ResponseEntity<List<AnotacaoPastaResponse>> listar(@PathVariable Long fichaId) {
        Long usuarioAtualId = getUsuarioAtualId();
        List<AnotacaoPastaResponse> arvore = anotacaoPastaService.listarArvore(fichaId, usuarioAtualId);
        return ResponseEntity.ok(arvore);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar pasta",
               description = "Cria uma nova pasta na ficha. Jogadores só podem criar em suas próprias fichas.")
    public ResponseEntity<AnotacaoPastaResponse> criar(
            @PathVariable Long fichaId,
            @Valid @RequestBody CriarPastaRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        AnotacaoPastaResponse response = anotacaoPastaService.criar(fichaId, request, usuarioAtualId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar pasta",
               description = "Atualiza nome e/ou ordem de exibição de uma pasta.")
    public ResponseEntity<AnotacaoPastaResponse> atualizar(
            @PathVariable Long fichaId,
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPastaRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        AnotacaoPastaResponse response = anotacaoPastaService.atualizar(fichaId, id, request, usuarioAtualId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Deletar pasta",
               description = "Deleta uma pasta. Sub-pastas diretas são movidas para a raiz.")
    public ResponseEntity<Void> deletar(
            @PathVariable Long fichaId,
            @PathVariable Long id) {
        Long usuarioAtualId = getUsuarioAtualId();
        anotacaoPastaService.deletar(fichaId, id, usuarioAtualId);
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
