package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
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
 * Controller para gerenciar Classes de Personagem de um jogo.
 * CRUD completo - GET (MESTRE/JOGADOR), POST/PUT/DELETE (apenas MESTRE).
 */
@RestController
@RequestMapping("/api/v1/configuracoes/classes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Classes", description = "Gerenciamento de classes do jogo (Guerreiro, Mago, etc)")
public class ClasseController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar classes de um jogo", description = "Retorna todas as classes ativas do jogo especificado")
    public ResponseEntity<List<ClassePersonagem>> listar(
            @Parameter(description = "ID do jogo", required = true) @RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listarClasses(jogoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar classe por ID")
    public ResponseEntity<ClassePersonagem> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(configuracaoService.buscarClasse(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar classe (Apenas MESTRE)", description = "Cria uma nova classe para o jogo")
    public ResponseEntity<ClassePersonagem> criar(@Valid @RequestBody ClassePersonagem classe) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracaoService.criarClasse(classe));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar classe (Apenas MESTRE)", description = "Atualiza uma classe existente")
    public ResponseEntity<ClassePersonagem> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClassePersonagem classe) {
        return ResponseEntity.ok(configuracaoService.atualizarClasse(id, classe));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar classe (Apenas MESTRE)", description = "Soft delete - marca a classe como inativa")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletarClasse(id);
        return ResponseEntity.noContent().build();
    }
}
