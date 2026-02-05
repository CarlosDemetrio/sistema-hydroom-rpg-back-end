package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
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
 * Controller para gerenciar Atributos de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/atributos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Atributos", description = "Gerenciamento de atributos do jogo (Força, Agilidade, etc)")
public class AtributoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar atributos de um jogo", description = "Retorna todos os atributos ativos do jogo especificado")
    public ResponseEntity<List<AtributoConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarAtributos(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar atributo por ID")
    public ResponseEntity<AtributoConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarAtributo(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar atributo (Apenas MESTRE)", description = "Cria um novo atributo para o jogo - apenas o Mestre pode fazer isso")
    public ResponseEntity<AtributoConfig> criar(@Valid @RequestBody AtributoConfig atributo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarAtributo(atributo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar atributo (Apenas MESTRE)", description = "Atualiza um atributo existente - apenas o Mestre pode fazer isso")
    public ResponseEntity<AtributoConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtributoConfig atributo) {
        return ResponseEntity.ok(configuracaoService.atualizarAtributo(id, atributo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar atributo (Apenas MESTRE)", description = "Soft delete - marca o atributo como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarAtributo(id);
        return ResponseEntity.noContent().build();
    }
}
