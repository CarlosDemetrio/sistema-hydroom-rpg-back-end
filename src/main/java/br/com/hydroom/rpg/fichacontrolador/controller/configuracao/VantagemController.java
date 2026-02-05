package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
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
 * Controller para gerenciar Vantagens de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/vantagens")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Vantagens", description = "Gerenciamento de vantagens compráveis do jogo")
public class VantagemController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens de um jogo", description = "Retorna todas as vantagens ativas do jogo especificado")
    public ResponseEntity<List<VantagemConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarVantagens(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar vantagem por ID")
    public ResponseEntity<VantagemConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarVantagem(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar vantagem (Apenas MESTRE)", description = "Cria uma nova vantagem para o jogo")
    public ResponseEntity<VantagemConfig> criar(@Valid @RequestBody VantagemConfig vantagem) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarVantagem(vantagem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar vantagem (Apenas MESTRE)", description = "Atualiza uma vantagem existente")
    public ResponseEntity<VantagemConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VantagemConfig vantagem) {
        return ResponseEntity.ok(configuracaoService.atualizarVantagem(id, vantagem));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar vantagem (Apenas MESTRE)", description = "Soft delete - marca a vantagem como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarVantagem(id);
        return ResponseEntity.noContent().build();
    }
}
