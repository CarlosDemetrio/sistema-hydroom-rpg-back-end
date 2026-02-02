package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
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
 * Controller para gerenciar Bônus de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/configuracoes/bonus")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Bônus", description = "Gerenciamento de bônus configuráveis (modificadores por raça, classe, etc)")
public class BonusController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar bônus de um jogo", description = "Retorna todos os bônus ativos do jogo especificado")
    public ResponseEntity<List<BonusConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarBonus(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar bônus por ID")
    public ResponseEntity<BonusConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarBonus(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar bônus (Apenas MESTRE)", description = "Cria um novo bônus para o jogo")
    public ResponseEntity<BonusConfig> criar(@Valid @RequestBody BonusConfig bonus) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarBonus(bonus));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar bônus (Apenas MESTRE)", description = "Atualiza um bônus existente")
    public ResponseEntity<BonusConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody BonusConfig bonus) {
        return ResponseEntity.ok(configuracaoService.atualizarBonus(id, bonus));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar bônus (Apenas MESTRE)", description = "Soft delete - marca o bônus como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarBonus(id);
        return ResponseEntity.noContent().build();
    }
}
