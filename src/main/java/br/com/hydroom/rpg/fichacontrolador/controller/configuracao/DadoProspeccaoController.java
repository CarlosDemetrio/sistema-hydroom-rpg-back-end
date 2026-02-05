package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
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
 * Controller para gerenciar Dados de Prospecção de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/dados-prospeccao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Dados de Prospecção", description = "Gerenciamento de dados disponíveis para prospecção (d3, d4, d6, d8, d10, d12, d20, d100)")
public class DadoProspeccaoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar dados de prospecção de um jogo", description = "Retorna todos os dados ativos do jogo especificado")
    public ResponseEntity<List<DadoProspeccaoConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarDadosProspeccao(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar dado de prospecção por ID")
    public ResponseEntity<DadoProspeccaoConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarDadoProspeccao(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar dado de prospecção (Apenas MESTRE)", description = "Cria um novo dado para o jogo")
    public ResponseEntity<DadoProspeccaoConfig> criar(@Valid @RequestBody DadoProspeccaoConfig dado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarDadoProspeccao(dado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar dado de prospecção (Apenas MESTRE)", description = "Atualiza um dado existente")
    public ResponseEntity<DadoProspeccaoConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DadoProspeccaoConfig dado) {
        return ResponseEntity.ok(configuracaoService.atualizarDadoProspeccao(id, dado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar dado de prospecção (Apenas MESTRE)", description = "Soft delete - marca o dado como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarDadoProspeccao(id);
        return ResponseEntity.noContent().build();
    }
}
