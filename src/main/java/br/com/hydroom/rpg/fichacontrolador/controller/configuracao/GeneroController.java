package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
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
 * Controller para gerenciar Gêneros de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/configuracoes/generos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Gêneros", description = "Gerenciamento de gêneros disponíveis (Masculino, Feminino, Neutro, etc)")
public class GeneroController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar gêneros de um jogo", description = "Retorna todos os gêneros ativos do jogo especificado")
    public ResponseEntity<List<GeneroConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarGeneros(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar gênero por ID")
    public ResponseEntity<GeneroConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarGenero(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar gênero (Apenas MESTRE)", description = "Cria um novo gênero para o jogo")
    public ResponseEntity<GeneroConfig> criar(@Valid @RequestBody GeneroConfig genero) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarGenero(genero));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar gênero (Apenas MESTRE)", description = "Atualiza um gênero existente")
    public ResponseEntity<GeneroConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody GeneroConfig genero) {
        return ResponseEntity.ok(configuracaoService.atualizarGenero(id, genero));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar gênero (Apenas MESTRE)", description = "Soft delete - marca o gênero como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarGenero(id);
        return ResponseEntity.noContent().build();
    }
}
