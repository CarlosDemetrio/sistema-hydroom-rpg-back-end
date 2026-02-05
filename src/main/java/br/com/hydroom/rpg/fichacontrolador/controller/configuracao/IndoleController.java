package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import br.com.hydroom.rpg.fichacontrolador.service.ConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Controller para gerenciar Indoles/Alinhamentos de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/indoles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Indoles", description = "Gerenciamento de indoles/alinhamentos (Leal e Bom, Caótico e Mal, etc)")
public class IndoleController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar indoles de um jogo", description = "Retorna todas as indoles ativas do jogo especificado")
    public ResponseEntity<List<IndoleConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarIndoles(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar indole por ID")
    public ResponseEntity<IndoleConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarIndole(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar indole (Apenas MESTRE)", description = "Cria uma nova indole para o jogo")
    public ResponseEntity<IndoleConfig> criar(@Valid @RequestBody IndoleConfig indole) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarIndole(indole));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar indole (Apenas MESTRE)", description = "Atualiza uma indole existente")
    public ResponseEntity<IndoleConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody IndoleConfig indole) {
        return ResponseEntity.ok(configuracaoService.atualizarIndole(id, indole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar indole (Apenas MESTRE)", description = "Soft delete - marca a indole como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarIndole(id);
        return ResponseEntity.noContent().build();
    }
}
