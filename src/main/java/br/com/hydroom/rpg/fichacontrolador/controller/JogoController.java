package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller para gerenciamento de Jogos/Campanhas.
 * CRUD completo - Apenas MESTREs podem criar, editar e deletar jogos.
 */
@RestController
@RequestMapping("/api/jogos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Jogos", description = "Gerenciamento de jogos/campanhas de RPG")
public class JogoController {

    private final JogoService jogoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar jogos do usuário", description = "Retorna todos os jogos onde o usuário é Mestre ou Jogador")
    public ResponseEntity<List<Jogo>> listar() {
        return ResponseEntity.ok(jogoService.listarJogosDoUsuario());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar jogo por ID", description = "Retorna detalhes do jogo se o usuário tiver acesso")
    public ResponseEntity<Jogo> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(jogoService.buscarJogo(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar novo jogo (Apenas MESTRE)", description = "Cria um novo jogo/campanha com o usuário como Mestre")
    public ResponseEntity<Jogo> criar(@Valid @RequestBody Jogo jogo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jogoService.criarJogo(jogo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar jogo (Apenas MESTRE)", description = "Atualiza informações do jogo - apenas o Mestre do jogo pode fazer isso")
    public ResponseEntity<Jogo> atualizar(@PathVariable Long id, @Valid @RequestBody Jogo jogo) {
        return ResponseEntity.ok(jogoService.atualizarJogo(id, jogo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar jogo (Apenas MESTRE)", description = "Soft delete - marca o jogo como inativo - apenas o Mestre do jogo pode fazer isso")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        jogoService.deletarJogo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reativar jogo (Apenas MESTRE)", description = "Reativa um jogo marcado como inativo")
    public ResponseEntity<Jogo> ativar(@PathVariable Long id) {
        return ResponseEntity.ok(jogoService.ativarJogo(id));
    }
}
