package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
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
 * Controller para gerenciar Presenças de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/configuracoes/presencas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Presenças", description = "Gerenciamento de níveis de presença disponíveis")
public class PresencaController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar presenças de um jogo", description = "Retorna todas as presenças ativas do jogo especificado")
    public ResponseEntity<List<PresencaConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarPresencas(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar presença por ID")
    public ResponseEntity<PresencaConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarPresenca(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar presença (Apenas MESTRE)", description = "Cria uma nova presença para o jogo")
    public ResponseEntity<PresencaConfig> criar(@Valid @RequestBody PresencaConfig presenca) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarPresenca(presenca));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar presença (Apenas MESTRE)", description = "Atualiza uma presença existente")
    public ResponseEntity<PresencaConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PresencaConfig presenca) {
        return ResponseEntity.ok(configuracaoService.atualizarPresenca(id, presenca));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar presença (Apenas MESTRE)", description = "Soft delete - marca a presença como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarPresenca(id);
        return ResponseEntity.noContent().build();
    }
}
