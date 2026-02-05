package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
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
 * Controller para gerenciar Membros do Corpo de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/membros-corpo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Membros do Corpo", description = "Gerenciamento de membros do corpo para integridade física (Cabeça, Torso, Braços, Pernas)")
public class MembroCorpoController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar membros do corpo de um jogo", description = "Retorna todos os membros ativos do jogo especificado")
    public ResponseEntity<List<MembroCorpoConfig>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarMembrosCorpo(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar membro do corpo por ID")
    public ResponseEntity<MembroCorpoConfig> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarMembroCorpo(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar membro do corpo (Apenas MESTRE)", description = "Cria um novo membro do corpo para o jogo")
    public ResponseEntity<MembroCorpoConfig> criar(@Valid @RequestBody MembroCorpoConfig membro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarMembroCorpo(membro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar membro do corpo (Apenas MESTRE)", description = "Atualiza um membro do corpo existente")
    public ResponseEntity<MembroCorpoConfig> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MembroCorpoConfig membro) {
        return ResponseEntity.ok(configuracaoService.atualizarMembroCorpo(id, membro));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar membro do corpo (Apenas MESTRE)", description = "Soft delete - marca o membro como inativo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarMembroCorpo(id);
        return ResponseEntity.noContent().build();
    }
}
