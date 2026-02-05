package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Raca;
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
 * Controller para gerenciar Raças de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/racas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Raças", description = "Gerenciamento de raças do jogo (Humano, Elfo, Anão, etc)")
public class RacaController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar raças de um jogo", description = "Retorna todas as raças ativas do jogo especificado")
    public ResponseEntity<List<Raca>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarRacas(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar raça por ID")
    public ResponseEntity<Raca> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarRaca(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar raça (Apenas MESTRE)", description = "Cria uma nova raça para o jogo")
    public ResponseEntity<Raca> criar(@Valid @RequestBody Raca raca) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarRaca(raca));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar raça (Apenas MESTRE)", description = "Atualiza uma raça existente")
    public ResponseEntity<Raca> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Raca raca) {
        return ResponseEntity.ok(configuracaoService.atualizarRaca(id, raca));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar raça (Apenas MESTRE)", description = "Soft delete - marca a raça como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarRaca(id);
        return ResponseEntity.noContent().build();
    }
}
