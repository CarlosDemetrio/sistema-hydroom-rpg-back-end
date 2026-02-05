package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
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
 * Controller para gerenciar Níveis de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/niveis")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Níveis", description = "Gerenciamento de níveis e progressão de XP")
public class NivelController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar níveis de um jogo", description = "Retorna todos os níveis ativos do jogo especificado")
    public ResponseEntity<List<NivelConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarNiveis(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar nível por ID")
    public ResponseEntity<NivelConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarNivel(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar nível (Apenas MESTRE)", description = "Cria um novo nível para o jogo")
    public ResponseEntity<NivelConfig> criar(@Valid @RequestBody NivelConfig nivel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarNivel(nivel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar nível (Apenas MESTRE)", description = "Atualiza um nível existente")
    public ResponseEntity<NivelConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody NivelConfig nivel) {
        return ResponseEntity.ok(configuracaoService.atualizarNivel(id, nivel));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar nível (Apenas MESTRE)", description = "Soft delete - marca o nível como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarNivel(id);
        return ResponseEntity.noContent().build();
    }
}
