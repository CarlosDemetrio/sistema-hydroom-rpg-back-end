package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
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
 * Controller para gerenciar Aptidões de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/aptidoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Aptidões", description = "Gerenciamento de aptidões do jogo (Acrobacia, Guarda, etc)")
public class AptidaoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar aptidões de um jogo", description = "Retorna todas as aptidões ativas do jogo especificado")
    public ResponseEntity<List<AptidaoConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarAptidoes(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar aptidão por ID")
    public ResponseEntity<AptidaoConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarAptidao(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar aptidão (Apenas MESTRE)", description = "Cria uma nova aptidão para o jogo")
    public ResponseEntity<AptidaoConfig> criar(@Valid @RequestBody AptidaoConfig aptidao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarAptidao(aptidao));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar aptidão (Apenas MESTRE)", description = "Atualiza uma aptidão existente")
    public ResponseEntity<AptidaoConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AptidaoConfig aptidao) {
        return ResponseEntity.ok(configuracaoService.atualizarAptidao(id, aptidao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar aptidão (Apenas MESTRE)", description = "Soft delete - marca a aptidão como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarAptidao(id);
        return ResponseEntity.noContent().build();
    }
}
