package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
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
 * Controller para gerenciar Tipos de Aptidão de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/configuracoes/tipos-aptidao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Tipos de Aptidão", description = "Gerenciamento de tipos de aptidão (Física, Mental, etc)")
public class TipoAptidaoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar tipos de aptidão de um jogo", description = "Retorna todos os tipos de aptidão ativos do jogo especificado")
    public ResponseEntity<List<TipoAptidao>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarTiposAptidao(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar tipo de aptidão por ID")
    public ResponseEntity<TipoAptidao> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarTipoAptidao(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar tipo de aptidão (Apenas MESTRE)", description = "Cria um novo tipo de aptidão para o jogo")
    public ResponseEntity<TipoAptidao> criar(@Valid @RequestBody TipoAptidao tipoAptidao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarTipoAptidao(tipoAptidao));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar tipo de aptidão (Apenas MESTRE)", description = "Atualiza um tipo de aptidão existente")
    public ResponseEntity<TipoAptidao> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoAptidao tipoAptidao) {
        return ResponseEntity.ok(configuracaoService.atualizarTipoAptidao(id, tipoAptidao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar tipo de aptidão (Apenas MESTRE)", description = "Soft delete - marca o tipo como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarTipoAptidao(id);
        return ResponseEntity.noContent().build();
    }
}
